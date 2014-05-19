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

package com.newisys.dv;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.newisys.eventsim.Event;
import com.newisys.eventsim.PulseEvent;
import com.newisys.eventsim.SimulationManager;
import com.newisys.eventsim.SimulationThread;
import com.newisys.eventsim.UnhandledExceptionException;
import com.newisys.verilog.EdgeSet;
import com.newisys.verilog.TimeType;
import com.newisys.verilog.ValueType;
import com.newisys.verilog.VerilogCallback;
import com.newisys.verilog.VerilogCallbackData;
import com.newisys.verilog.VerilogCallbackHandler;
import com.newisys.verilog.VerilogReadValue;
import com.newisys.verilog.VerilogSimTime;
import com.newisys.verilog.VerilogSimulation;
import com.newisys.verilog.util.Bit;

/**
 * Manages translation of Verilog simulator callbacks to listener and event notifications.
 * 
 * @author Trevor Robinson
 */
final class DVEventManager
{
    // default access for efficient access by inner class and package classes
    final VerilogSimulation verilogSim;
    final SimulationManager simManager;

    public DVEventManager(
        VerilogSimulation verilogSim,
        SimulationManager simManager)
    {
        this.verilogSim = verilogSim;
        this.simManager = simManager;
    }

    public void executeThreads()
    {
        try
        {
            simManager.executeThreads();
        }
        catch (UnhandledExceptionException e)
        {
            // display the exception and stack trace
            final SimulationThread thread = e.getThread();
            System.out.println("Unhandled exception in " + thread.getName()
                + ":");
            final Throwable cause = e.getCause();
            cause.printStackTrace();

            // terminate all simulation threads
            simManager.terminateThreads();

            // call $finish in the Verilog simulator
            verilogSim.finish();
        }
    }

    // default access for efficient access by inner class
    final SynchCallbackHandler synchHandler = new SynchCallbackHandler();
    List<SynchListener> synchListeners;
    List<Event> synchEvents;

    private class SynchCallbackHandler
        implements VerilogCallbackHandler
    {
        public void run(VerilogCallback cb, VerilogCallbackData data)
        {
            if (Debug.enabled)
            {
                Debug.out.println("R/W synch @ " + data.getTime());
            }

            // latch listeners and events
            List<SynchListener> curSynchListeners;
            List<Event> curSynchEvents;
            synchronized (synchHandler)
            {
                curSynchListeners = synchListeners;
                synchListeners = null;

                curSynchEvents = synchEvents;
                synchEvents = null;
            }

            // notify listeners
            if (curSynchListeners != null)
            {
                for (final SynchListener listener : curSynchListeners)
                {
                    listener.notifySynch();
                }
            }

            // notify events
            if (curSynchEvents != null)
            {
                for (final Event event : curSynchEvents)
                {
                    simManager.notifyOf(event);
                }
            }

            // execute pending threads before returning to simulator
            executeThreads();
        }
    }

    public void registerSynchCallback(SynchListener listener)
    {
        synchronized (synchHandler)
        {
            if (synchListeners == null && synchEvents == null)
            {
                verilogSim.addReadWriteSynchCallback(synchHandler);
            }
            if (synchListeners == null)
            {
                synchListeners = new LinkedList<SynchListener>();
            }
            synchListeners.add(listener);
        }
    }

    public void registerSynchCallback(Event event)
    {
        synchronized (synchHandler)
        {
            if (synchListeners == null && synchEvents == null)
            {
                verilogSim.addReadWriteSynchCallback(synchHandler);
            }
            if (synchEvents == null)
            {
                synchEvents = new LinkedList<Event>();
            }
            synchEvents.add(event);
        }
    }

    public void waitForSynch()
    {
        Event event = new PulseEvent("r/w synch");
        registerSynchCallback(event);
        SimulationThread.currentThread().waitFor(event);
    }

    // default access for efficient access by inner class
    final SortedMap<VerilogSimTime, SimTimeCallbackHandler> simTimeCallbackMap = new TreeMap<VerilogSimTime, SimTimeCallbackHandler>();

    private class SimTimeCallbackHandler
        implements VerilogCallbackHandler, SynchListener
    {
        final VerilogSimTime simTime;
        private List<DelayListener> listeners;
        private List<Event> events;

        SimTimeCallbackHandler(VerilogSimTime simTime)
        {
            this.simTime = simTime;
        }

        public void addListener(DelayListener listener)
        {
            if (listeners == null) listeners = new LinkedList<DelayListener>();
            listeners.add(listener);
        }

        public void addEvent(Event event)
        {
            if (events == null) events = new LinkedList<Event>();
            events.add(event);
        }

        public void run(VerilogCallback cb, VerilogCallbackData data)
        {
            if (Debug.enabled)
            {
                Debug.out.println("Sim time callback @ " + data.getTime());
            }

            // sanity check
            assert (simTime.equals(data.getTime()));

            // remove this handler from the delay callback map;
            // aside from garbage collection, this ensures that no more
            // listeners/events will be added to this handler while the
            // lists are being iterated
            synchronized (simTimeCallbackMap)
            {
                // we should be the first handler in the callback map;
                // otherwise, callbacks have not been handled
                assert (simTimeCallbackMap.headMap(simTime).isEmpty());

                simTimeCallbackMap.remove(simTime);
            }

            // wait until R/W synch to notify listeners
            registerSynchCallback(this);
        }

        public void notifySynch()
        {
            // notify listeners
            if (listeners != null)
            {
                for (final DelayListener listener : listeners)
                {
                    listener.notifyDelay();
                }
            }

            // notify events
            if (events != null)
            {
                for (final Event event : events)
                {
                    simManager.notifyOf(event);
                }
            }

            // execute pending threads before returning to simulator
            executeThreads();
        }
    }

    private SimTimeCallbackHandler getSimTimeHandler(VerilogSimTime time)
    {
        SimTimeCallbackHandler handler = simTimeCallbackMap.get(time);
        if (handler == null)
        {
            assert (time.getSimTime() >= 0);
            handler = new SimTimeCallbackHandler(time);
            simTimeCallbackMap.put(time, handler);
            verilogSim.addStartOfSimTimeCallback(time, handler);
        }
        return handler;
    }

    public void registerSimTimeCallback(
        VerilogSimTime time,
        DelayListener listener)
    {
        synchronized (simTimeCallbackMap)
        {
            SimTimeCallbackHandler handler = getSimTimeHandler(time);
            handler.addListener(listener);
        }
    }

    public void registerSimTimeCallback(VerilogSimTime time, Event event)
    {
        synchronized (simTimeCallbackMap)
        {
            SimTimeCallbackHandler handler = getSimTimeHandler(time);
            handler.addEvent(event);
        }
    }

    public void delay(long ticks)
    {
        Event event = new PulseEvent("delay");
        long simTime = verilogSim.getSimTime() + ticks;
        registerSimTimeCallback(new VerilogSimTime(simTime), event);
        SimulationThread.currentThread().waitFor(event);
    }

    public void waitForSimTime(long simTime)
    {
        Event event = new PulseEvent("sim time");
        registerSimTimeCallback(new VerilogSimTime(simTime), event);
        SimulationThread.currentThread().waitFor(event);
    }

    private class ChangeCallbackHandler
        implements VerilogCallbackHandler
    {
        private final Event event;

        public ChangeCallbackHandler(Event event)
        {
            this.event = event;
        }

        public void run(VerilogCallback cb, VerilogCallbackData data)
        {
            registerSynchCallback(event);
        }
    }

    public void waitForChange(VerilogReadValue signal)
    {
        Event event = new PulseEvent("value change");
        ChangeCallbackHandler handler = new ChangeCallbackHandler(event);
        VerilogCallback callback = signal.addValueChangeCallback(
            TimeType.SUPPRESS, ValueType.SUPPRESS, handler);
        SimulationThread.currentThread().waitFor(event);
        callback.cancel();
    }

    private class EdgeCallbackHandler
        implements VerilogCallbackHandler
    {
        private final Event event;
        private final EdgeSet edgeSet;
        private Bit curValue;

        public EdgeCallbackHandler(Event event, EdgeSet edgeSet, Bit curValue)
        {
            this.event = event;
            this.edgeSet = edgeSet;
            this.curValue = curValue;
        }

        public void run(VerilogCallback cb, VerilogCallbackData data)
        {
            Bit newValue = (Bit) data.getValue();
            if (edgeSet.matches(curValue, newValue))
            {
                registerSynchCallback(event);
            }
            curValue = newValue;
        }
    }

    public void waitForEdge(VerilogReadValue signal, EdgeSet edgeSet)
    {
        Bit curValue;
        try
        {
            curValue = (Bit) signal.getValue(ValueType.SCALAR);
        }
        catch (RuntimeException e)
        {
            throw new DVRuntimeException(
                "waitForEdge() requires a scalar value", e);
        }

        Event event = new PulseEvent("edge");
        EdgeCallbackHandler handler = new EdgeCallbackHandler(event, edgeSet,
            curValue);
        VerilogCallback callback = signal.addValueChangeCallback(
            TimeType.SUPPRESS, ValueType.SCALAR, handler);
        SimulationThread.currentThread().waitFor(event);
        callback.cancel();
    }
}
