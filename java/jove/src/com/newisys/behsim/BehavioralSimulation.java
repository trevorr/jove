/*
 * Jove - The Open Verification Environment for the Java (TM) Platform
 * Copyright (C) 2005 Newisys, Inc. or its licensors, as applicable.
 * Java is a registered trademark of Sun Microsystems, Inc. in the U.S. or
 * other countries.
 *
 * Licensed under the Open Software License version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You should
 * have received a copy of the License along with this software; if not, you
 * may obtain a copy of the License at
 *
 * http://opensource.org/licenses/osl-2.0.php
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.newisys.behsim;

import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.newisys.dv.DVRuntimeException;
import com.newisys.eventsched.EventScheduler;
import com.newisys.verilog.*;

/**
 * A bare-bones simulator intended to provide enough functionality for
 * behavioral simulations.
 * 
 * @author Scott Diesing
 */
public class BehavioralSimulation
    implements VerilogSimulation
{
    /**
     * Synchronize access and enforce type checking.
     */
    private static final class VerilogRegList
    {
        private final List<VerilogReg> regs = new LinkedList<VerilogReg>();

        public VerilogRegList()
        {
        }

        public synchronized boolean add(VerilogReg reg)
        {
            return regs.add(reg);
        }

        public synchronized VerilogReg getObjectByName(String name)
        {
            VerilogReg object = null;
            Iterator<VerilogReg> regIter = regs.iterator();
            while (regIter.hasNext() && (object == null))
            {
                VerilogReg reg = regIter.next();
                if (name.equals(reg.getFullName()))
                {
                    object = reg;
                }
            }
            return object;
        }

    }

    /**
     * Maintains a mapping of BehavioralCallback objects to
     * BehavioralCallbackEvent objects. Synchronize access and enforce type
     * checking.
     */
    private final static class CallbackEventMap
    {
        private final HashMap<BehavioralSimulationCallback, BehavioralCallbackEvent> map = new HashMap<BehavioralSimulationCallback, BehavioralCallbackEvent>();

        public synchronized void addEntry(
            BehavioralSimulationCallback callback,
            BehavioralCallbackEvent event)
        {
            map.put(callback, event);
        }

        public synchronized BehavioralCallbackEvent removeEntry(
            BehavioralSimulationCallback callback)
        {
            return map.remove(callback);
        }
    }

    private final EventScheduler eventScheduler = new EventScheduler();
    private final CallbackEventMap callbackEventMap = new CallbackEventMap();
    private final VerilogRegList regs = new VerilogRegList();
    private final List<String> arguments;

    public BehavioralSimulation()
    {
        this(Collections.<String> emptyList());
    }

    public BehavioralSimulation(List<String> arguments)
    {
        this.arguments = arguments;
    }

    /**
     * Process all the events in the queue.
     */
    public void run()
    {
        eventScheduler.processStartOfSimulationEvents();
        eventScheduler.processEvents();
        eventScheduler.processEndOfSimulationEvents();
    }

    EventScheduler getEventScheduler()
    {
        return eventScheduler;
    }

    public VerilogReg createRegister(String name, int size)
    {
        VerilogReg reg = (BehavioralReg) getObjectByName(name);
        if (reg == null)
        {
            reg = new BehavioralReg(this, stripSuffix(name), size);
            regs.add(reg);
        }
        else
        {
            if (reg.getSize() != size)
            {
                throw new DVRuntimeException(
                    "BehavioralReg size mismatch.  previous=" + reg.getSize()
                        + " new=" + size);
            }
        }
        return reg;
    }

    public VerilogObject getObjectByName(String name)
    {
        return regs.getObjectByName(stripSuffix(name));
    }

    String stripSuffix(String s)
    {
        String returnString = s;
        String inSuffix = "_in";
        String outSuffix = "_out";

        if (s.endsWith(inSuffix))
        {
            returnString = s.substring(0, s.length() - inSuffix.length());
        }

        if (s.endsWith(outSuffix))
        {
            returnString = s.substring(0, s.length() - outSuffix.length());
        }

        return returnString;
    }

    public VerilogCallback addDelayCallback(
        VerilogTime delay,
        VerilogCallbackHandler handler)
    {
        BehavioralCallbackEvent event = createCallBackEvent(handler,
            CallbackReason.AFTER_DELAY);
        VerilogSimTime simTime = (VerilogSimTime) delay;
        // By adding this event to the active queue, the eventscheduler
        // will
        // call execute on it when the given time step (delay + current) is
        // reached.
        eventScheduler.addEventToActiveQueue(event, simTime.getSimTime());

        return event.getCallback();
    }

    /**
     * @param handler the callback handler the caller wants to have handler.run
     *            called on when the simulation starts.
     *
     * @return a callback object that allows the caller to cancel the callback.
     */
    public VerilogCallback addSimulationStartCallback(
        VerilogCallbackHandler handler)
    {
        BehavioralCallbackEvent event = createCallBackEvent(handler,
            CallbackReason.START_OF_SIMULATION);
        eventScheduler.addSimulationStartEvent(event);
        return event.getCallback();
    }

    /**
     * @param handler the callback handler the caller wants to have handler.run
     *            called on when the simulation ends.
     *
     * @return a callback object that allows the caller to cancel the callback.
     */
    public VerilogCallback addSimulationEndCallback(
        VerilogCallbackHandler handler)
    {
        BehavioralCallbackEvent event = createCallBackEvent(handler,
            CallbackReason.END_OF_SIMULATION);
        eventScheduler.addSimulationEndEvent(event);
        return event.getCallback();
    }

    /**
     * Callback shall occur after execution of events for a specified time. Add
     * to the end of the inactive event queue. It is possible for subsequent
     * events to be added to the current time step after the callback (for this
     * reason, when the callback occurs, the next scheduled time step cannot be
     * determined).
     * @param handler the read/write synch callback handler
     * @return a callback object
     */
    public VerilogCallback addReadWriteSynchCallback(
        VerilogCallbackHandler handler)
    {
        BehavioralCallbackEvent event = createCallBackEvent(handler,
            CallbackReason.READ_WRITE_SYNCH);
        eventScheduler.addEventToInactiveQueue(event, 0);
        return event.getCallback();
    }

    /**
     * Callback shall occur before execution of events in a specified time
     * queue. A callback can be set for any time, even if no event is present.
     *
     * The following situation will generate an error and no callback will be
     * created: Attempting to place a cbAtStartOfSimTime callback with a delay
     * of zero when simulation has progressed into a time slice, and the
     * application is not currently within a cbAtStartOfSimTime callback.
     *
     * Placing a callback for cbAtStartOfSimTime and a delay of zero during a
     * callback for reason cbAtStartOf- SimTime will result in another
     * cbAtStartOfSimTime callback occurring during the same time slice.
     *
     * @param time the absolute time of the callback
     * @param handler the callback handler
     * @return a callback object
     */
    public VerilogCallback addStartOfSimTimeCallback(
        VerilogTime time,
        VerilogCallbackHandler handler)
    {
        VerilogSimTime simTime = (VerilogSimTime) time;
        long currentTime = getSimTime();
        long eventTime = simTime.getSimTime();
        long delay = eventTime - currentTime;
        if (delay < 0)
        {
            throw new DVRuntimeException(
                "Cannot add callback for time that has already occured.  eventTime="
                    + eventTime + " currentTime=" + currentTime);
        }

        BehavioralCallbackEvent event = createCallBackEvent(handler,
            CallbackReason.AT_START_OF_SIM_TIME);

        // By adding this event to the active queue, the eventscheduler
        // will
        // call execute on it when the given time step (time = delay +
        // current)
        // is
        // reached.
        eventScheduler.addEventToFrontOfActiveQueue(event, delay);

        return event.getCallback();
    }

    // I/O
    public OutputStream getLogOutputStream()
    {
        return System.out;
    }

    public String getProduct()
    {
        return getClass().getName();
    }

    public String getVersion()
    {
        return "1.0";
    }

    public List<String> getArguments()
    {
        return arguments;
    }

    /**
     * @return current simulation time (number of ticks since time zero).
     */
    public long getSimTime()
    {
        return eventScheduler.getCurrentTime();
    }

    /**
     * @return current simulation time (number of ticks since time zero). always
     *         assumes a time scale of 1 s / 1 s
     */
    public double getScaledRealTime()
    {
        Long tmp = Long.valueOf(eventScheduler.getCurrentTime());
        return tmp.doubleValue();
    }

    /**
     * Callback shall occur after execution of events for a specified time. Add
     * to the end of the monitor event queue. It is NOT possible for subsequent
     * events to be added to the current time step after the callback.
     * @param handler the read-only synch callback handler
     * @return a callback object
     */
    public VerilogCallback addReadOnlySynchCallback(
        VerilogCallbackHandler handler)
    {
        BehavioralCallbackEvent event = createCallBackEvent(handler,
            CallbackReason.READ_ONLY_SYNCH);
        eventScheduler.addEventToMonitorQueue(event, 0);
        return event.getCallback();
    }

    /**
     * Callback shall occur before execution of events in the next event queue.
     * start of next time step.
     * @param handler the next sim-time callback handler
     * @return a callback object
     */
    public VerilogCallback addNextSimTimeCallback(VerilogCallbackHandler handler)
    {
        BehavioralCallbackEvent event = createCallBackEvent(handler,
            CallbackReason.READ_ONLY_SYNCH);
        eventScheduler.addNextSimTimeEvent(event);
        return event.getCallback();
    }

    public Iterator<VerilogModule> getModules()
    {
        throw new UnsupportedOperationException();
    }

    public Iterator<VerilogUdpDefn> getUdpDefns()
    {
        throw new UnsupportedOperationException();
    }

    public Iterator<VerilogUserSystf> getUserSystfs()
    {
        throw new UnsupportedOperationException();
    }

    public Iterator<VerilogCallback> getCallbacks()
    {
        throw new UnsupportedOperationException();
    }

    public Iterator<VerilogTimeQueue> getTimeQueues()
    {
        throw new UnsupportedOperationException();
    }

    public VerilogInterModPath getInterModPath(
        VerilogPort port1,
        VerilogPort port2)
    {
        throw new UnsupportedOperationException();
    }

    private BehavioralCallbackEvent createCallBackEvent(
        VerilogCallbackHandler handler,
        CallbackReason reason)
    {
        // Wrap the callback handler.
        BehavioralSimulationCallback callback = new BehavioralSimulationCallback(
            this, handler, reason);
        // Create a callback data object.
        BehavioralCallbackData data = new BehavioralCallbackData(this);
        // Create an event that will translate the event scheduler
        // event.execute() call to call the handler.run() method.
        BehavioralCallbackEvent event = new BehavioralCallbackEvent(callback,
            data);
        callbackEventMap.addEntry(callback, event);
        return event;
    }

    /**
     * Remove the callback from the map and tell the event scheduler to cancel
     * the wrapped event.
     * @param callback the callback to cancel
     */
    void cancelCallback(BehavioralSimulationCallback callback)
    {
        BehavioralCallbackEvent event = removeCallback(callback);
        if (event != null)
        {
            eventScheduler.cancelEvent(event);
        }
    }

    /**
     * Remove a callback from the map.
     * @param callback the callback to remove
     * @return the event corresponding to the callback
     */
    public BehavioralCallbackEvent removeCallback(
        BehavioralSimulationCallback callback)
    {
        return callbackEventMap.removeEntry(callback);
    }

    public void stop()
    {
        // ignored
    }

    public void finish()
    {
        // Tell event scheduler to stop processing events
        eventScheduler.finish();
    }
}
