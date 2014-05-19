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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import com.newisys.eventsim.Event;
import com.newisys.eventsim.SimulationManager;
import com.newisys.eventsim.SimulationThread;
import com.newisys.ova.OVAEngine;
import com.newisys.random.PRNG;
import com.newisys.random.PRNGFactory;
import com.newisys.verilog.EdgeSet;
import com.newisys.verilog.ValueType;
import com.newisys.verilog.VerilogObject;
import com.newisys.verilog.VerilogReadValue;
import com.newisys.verilog.VerilogRuntimeException;
import com.newisys.verilog.VerilogSimulation;
import com.newisys.verilog.VerilogWriteValue;

/**
 * Provides access to events and structures in the current simulation.
 * <P>
 * This class is the user code's central access point to the simulation.
 * Methods provide:
 * <ul>
 * <li>Thread management</li>
 * <li>Random seed management</li>
 * <li>Signal management</li>
 * <li>Mailbox/Semaphore creation</li>
 * </ul>
 * 
 * @author Trevor Robinson
 */
public final class DVSimulation
{
    private static final String INPUT_SUFFIX = "_in";
    private static final String OUTPUT_SUFFIX = "_out";

    private transient ClockSignal defaultClock = null;

    final VerilogSimulation verilogSim;
    final SimulationManager simManager;
    final DVEventManager dvEventManager;
    final DVObjectDirectory dvObjDir;
    final VerilogTaskManager verilogTaskManager;
    final JavaTaskManager javaTaskManager;
    final OVAEngine ovaEngine;

    /**
     * Create a new DVSimulation with the given VerilogSimulation and
     * SimulationManager.
     *
     * @param verilogSim the VerilogSimulation to be used by this DVSimulation
     * @param simManager the SimulationManager to be used by this DVSimulation
     * @param ovaEngine the OVAEngine to be used by this DVSimulation
     */
    public DVSimulation(
        VerilogSimulation verilogSim,
        SimulationManager simManager,
        OVAEngine ovaEngine)
    {
        this.verilogSim = verilogSim;
        this.simManager = simManager;
        this.ovaEngine = ovaEngine;
        dvEventManager = new DVEventManager(verilogSim, simManager);
        dvObjDir = new DVObjectDirectory();
        verilogTaskManager = new VerilogTaskManager(dvEventManager);
        javaTaskManager = new JavaTaskManager(dvEventManager);
    }

    /**
     * Registers all static methods in the given class (and any superclasses)
     * whose names start with "task_" as callable from the HDL.
     *
     * @param cls the class to register static methods from
     */
    public void registerJavaTasks(Class cls)
    {
        javaTaskManager.registerTasks(cls);
    }

    /**
     * Registers all methods in the class of the given object (and any
     * superclasses) whose names start with "task_" as callable from the HDL.
     *
     * @param object the object to register methods from
     */
    public void registerJavaTasks(Object object)
    {
        javaTaskManager.registerTasks(object);
    }

    /**
     * Registers the given method as callable from the HDL. If the object
     * argument is not null, the method is called on the given object;
     * otherwise, the method must be static.
     *
     * @param method the method to register
     * @param object the object to invoke the method on
     */
    public void registerJavaTask(Method method, Object object)
    {
        javaTaskManager.registerTask(method, object);
    }

    /**
     * Execute the given HDL task.
     *
     * @param name the name of the HDL task
     * @param args an array of arguments to pass to the verilog task
     */
    public void callVerilogTask(String name, Object... args)
    {
        verilogTaskManager.callTask(name, args);
    }

    /**
     * Creates a new mailbox.
     *
     * @param <T> the type of objects contained by the mailbox
     * @return the new Mailbox object
     */
    public <T> Mailbox<T> createMailbox()
    {
        return new Mailbox<T>(simManager);
    }

    /**
     * Creates a new semaphore with the specified number of permits.
     *
     * @param permits the initial number of permits in the semaphore
     * @return the new Sempahore object
     */
    public Semaphore createSemaphore(int permits)
    {
        return new Semaphore(simManager, permits);
    }

    /**
     * Returns the current SimulationThread.
     *
     * @return the current SimulationThread
     */
    public SimulationThread currentThread()
    {
        return SimulationThread.currentThread();
    }

    /**
     * Starts a new simulation thread with the given name.
     *
     * @param name the name of this thread, for debugging purposes
     * @param r the code to run for this thread
     * @return the SimulationThread object associated with the new thread
     */
    public SimulationThread fork(String name, Runnable r)
    {
        return simManager.fork(name, r);
    }

    /**
     * Starts a new simulation thread with a generated name.
     *
     * @param r the code to run for this thread
     * @return the SimulationThread object associated with the new thread
     */
    public SimulationThread fork(Runnable r)
    {
        return simManager.fork(r);
    }

    /**
     * Starts a set of new simulation threads with generated names.
     *
     * @param rs the code to run for each thread
     * @return the SimulationThread objects associated with the new threads
     */
    public SimulationThread[] fork(Runnable... rs)
    {
        return simManager.fork(rs);
    }

    /**
     * Waits for the specified thread to complete.
     *
     * @param thread the thread to wait for
     */
    public void join(SimulationThread thread)
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.join(thread);
    }

    /**
     * Waits for all specified threads to complete.
     *
     * @param threads an array of SimulationThreads to wait for
     */
    public void joinAll(SimulationThread... threads)
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.joinAll(threads);
    }

    /**
     * Waits for any of the specified threads to complete. Once any of the
     * specified threads completes, this method will return.
     *
     * @param threads an array of SimulationThreads to wait for
     */
    public void joinAny(SimulationThread... threads)
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.joinAny(threads);
    }

    /**
     * Waits for all child threads of this thread to complete.
     */
    public void joinChildren()
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.joinChildren();
    }

    /**
     * Yields execution to another thread.
     */
    public void yield()
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.yield();
    }

    /**
     * Terminates this thread and all of its child threads.
     */
    public void terminate()
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.terminateChildren();
        t.joinChildren();
    }

    /**
     * Terminates all threads in the current simulation.
     */
    public void terminateAll()
    {
        simManager.terminateThreads();
    }

    /**
     * Stops the current simulation. This is equivalent to the Verilog $stop task.
     */
    public void stop()
    {
        verilogSim.stop();
    }

    /**
     * Ends the current simulation. This is equivalent to the Verilog $finish task.
     */
    public void finish()
    {
        verilogSim.finish();
    }

    /**
     * Notify the simulation that the specified event has occured. This may
     * cause other threads to become unblocked.
     *
     * @param e the event that has occurred
     */
    public void notifyOf(Event e)
    {
        simManager.notifyOf(e);
    }

    /**
     * Waits for the specified event to occur. If the event has already
     * occured, this method will return immediately.
     *
     * @param e the event to wait for
     */
    public void waitFor(Event e)
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.waitFor(e);
    }

    /**
     * Waits for all of the specified events to occur. If all events have
     * already occured, this method will return immediately.
     *
     * @param events an array of events to wait for
     */
    public void waitForAll(Event... events)
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.waitForAll(events);
    }

    /**
     * Waits for all of the specified events to occur. If all events have
     * already occured, this method will return immediately.
     *
     * @param events a collection of events to wait for
     */
    public void waitForAll(Collection< ? extends Event> events)
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.waitForAll(events);
    }

    /**
     * Waits for any of the specified events to occur. If any events have
     * already occured, this method will return immediately. Otherwise, this
     * method will return as soon as one of the specified events occurs.
     *
     * @param events an array of events to wait for
     */
    public void waitForAny(Event... events)
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.waitForAny(events);
    }

    /**
     * Waits for any of the specified events to occur. If any events have
     * already occured, this method will return immediately. Otherwise, this
     * method will return as soon as one of the specified events occurs.
     *
     * @param events a collection of events to wait for
     */
    public final void waitForAny(Collection< ? extends Event> events)
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.waitForAny(events);
    }

    /**
     * Returns the random number generator associated with the current thread.
     *
     * @return the current thread's random number generator
     */
    public PRNG getRandom()
    {
        final SimulationThread t = SimulationThread.currentThread();
        return t.getRandom();
    }

    /**
     * Sets the random number generator associated with the current thread.
     *
     * @param random the random number generator to associate with the current
     *      thread
     */
    public void setRandom(PRNG random)
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.setRandom(random);
    }

    /**
     * Returns the random number generator factory associated with the current
     * thread.
     *
     * @return the current thread's random number generator factory
     */
    public PRNGFactory getRandomFactory()
    {
        final SimulationThread t = SimulationThread.currentThread();
        return t.getRandomFactory();
    }

    /**
     * Sets the random number generator factory associated with the current
     * thread.
     *
     * @param randomFactory the random number generator factory to associate
     *      with the current thread
     */
    public void setRandomFactory(PRNGFactory randomFactory)
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.setRandomFactory(randomFactory);
    }

    /**
     * Returns the arguments passed to this simulation.
     *
     * @return a list of arguments passed to this simulation
     */
    public List<String> getArguments()
    {
        return verilogSim.getArguments();
    }

    /**
     * Returns the current simulation time.
     * TODO: write a note about the timescale of the returned value
     *
     * @return the current simulation time in ticks
     */
    public long getSimTime()
    {
        return verilogSim.getSimTime();
    }

    /**
     * Looks up an HDL object by name.
     *
     * @param name the name of the HDL object
     * @return the HDL object with the specified name
     */
    public VerilogObject getObjectByName(String name)
    {
        VerilogObject obj = dvObjDir.lookupObject(name);
        if (obj == null)
        {
            obj = verilogSim.getObjectByName(name);
            dvObjDir.registerObject(name, obj);
        }
        return obj;
    }

    /**
     * Returns the SignalInfo object for a signal based on the signal name.
     *
     * @param name the name of the signal
     * @return the SignalInfo object for the signal with the specified name
     */
    private SignalInfo getSignalInfoByName(String name)
    {
        SignalInfo info = dvObjDir.lookupSignal(name);
        if (info == null)
        {
            // look for input/output suffixed signals first
            VerilogObject sampleObj, driveObj;
            try
            {
                sampleObj = getObjectByName(name + INPUT_SUFFIX);
            }
            catch (VerilogRuntimeException e)
            {
                sampleObj = null;
            }
            try
            {
                driveObj = getObjectByName(name + OUTPUT_SUFFIX);
            }
            catch (VerilogRuntimeException e)
            {
                driveObj = null;
            }

            final VerilogReadValue sampleRV;
            final VerilogWriteValue driveWV;
            if (sampleObj != null || driveObj != null)
            {
                // found either or both suffixed signals
                sampleRV = (VerilogReadValue) sampleObj;
                driveWV = (VerilogWriteValue) driveObj;
            }
            else
            {
                // look for non-suffixed signal
                VerilogObject obj = getObjectByName(name);
                sampleRV = obj instanceof VerilogReadValue
                    ? (VerilogReadValue) obj : null;
                driveWV = obj instanceof VerilogWriteValue
                    ? (VerilogWriteValue) obj : null;
            }

            info = new SignalInfo(name, sampleRV, driveWV, false);

            // for input signals, go ahead and create an input monitor
            if (sampleRV != null)
            {
                info.inputMonitor = new InputMonitor(dvEventManager, name,
                    sampleRV, ValueType.OBJ_TYPE, 1);
            }

            dvObjDir.registerSignal(name, info);
        }
        return info;
    }

    /**
     * Checks that the given SignalInfo refers to an InputSignal.
     *
     * @param info the SignalInfo to check
     * @throws DVRuntimeException if info does not refer to an InputSignal
     */
    private void checkInputSignal(SignalInfo info)
    {
        if (info.sampleObj == null)
        {
            throw new DVRuntimeException("Signal is not an input signal: "
                + info.name);
        }
    }

    /**
     * Checks that the given SignalInfo refers to an OutputSignal.
     *
     * @param info the SignalInfo to check
     * @throws DVRuntimeException if info does not refer to an OutputSignal
     */
    private void checkOutputSignal(SignalInfo info)
    {
        if (info.driveObj == null)
        {
            throw new DVRuntimeException("Signal is not an output signal: "
                + info.name);
        }
    }

    /**
     * Returns a ClockMonitor for the given signal with the given depth.
     *
     * @param name the name of the signal
     * @param depth the requested depth of the ClockMonitor
     * @return a ClockMonitor for the specified signal
     */
    private ClockMonitor getClockMonitor(String name, int depth)
    {
        SignalInfo info = getSignalInfoByName(name);
        checkInputSignal(info);
        ClockMonitor clockMonitor = info.clockMonitor;
        if (clockMonitor == null)
        {
            // create new clock monitor
            clockMonitor = new ClockMonitor(dvEventManager, info.inputMonitor,
                0, depth);
            info.clockMonitor = clockMonitor;
        }
        else
        {
            // expand buffer depth on existing clock monitor if necessary
            if (clockMonitor.getBufferDepth() < depth)
            {
                clockMonitor.setBufferDepth(depth);
            }
        }
        return clockMonitor;
    }

    /**
     * Returns a ClockSignal object for the specified signal.
     *
     * @param name the name of the signal
     * @param depth the desired back-reference depth
     * @return a ClockSignal object for the specified signal
     */
    public ClockSignal getClockSignal(String name, int depth)
    {
        ClockMonitor clockMon = getClockMonitor(name, depth);
        return new ClockSignalImpl(this, name, clockMon);
    }

    /**
     * Returns the default ClockSignal object for the simulation. This is a
     * clock that can be used if no other clocks are available.
     *
     * @return the default clock signal
     */
    public ClockSignal getDefaultClockSignal()
    {
        if (defaultClock == null)
        {
            defaultClock = getClockSignal("DefaultClock", 1);
        }

        return defaultClock;
    }

    /**
     * Returns an InputSignal object for the specified signal.
     *
     * @param name the name of the signal
     * @param clock the ClockSignal used to sample this signal
     * @param inputEdges the clock edges to sample this signal on
     * @param inputSkew the input skew in ticks (should be negative)
     * @param depth the desired back-reference depth
     * @return an InputSignal object for the specified signal
     */
    public InputSignal getInputSignal(
        String name,
        ClockSignal clock,
        EdgeSet inputEdges,
        int inputSkew,
        int depth)
    {
        SignalInfo info = getSignalInfoByName(name);
        checkInputSignal(info);
        ClockMonitor clockMon = getClockMonitor(clock.getName(), 1);
        return new InputSignalImpl(this, name, info.inputMonitor, clock,
            clockMon, inputEdges, inputSkew, depth);
    }

    /**
     * Returns an InputSignal based on a bit range of another signal. The new
     * InputSignal will have a width of <code>highBit - lowBit + 1</code>.
     *
     * @param baseSignal the InputSignal containing the bits from which the new
     *      InputSignal will be created
     * @param highBit the bit of <code>baseSignal</code> that will become the
     *      MSB of the new InputSignal
     * @param lowBit the bit of <code>baseSignal</code> that will become the
     *      LSB of the new InputSignal
     * @return the partial InputSignal
     */
    public InputSignal getPartialInputSignal(
        InputSignal baseSignal,
        int highBit,
        int lowBit)
    {
        return new PartialInputSignalImpl(baseSignal, highBit, lowBit);
    }

    /**
     * Returns an InputSignal that is the concatenation of multiple signals.
     * The first signal will be placed in the highest order bits of the new
     * signals, the second index in the next highest order bits, and so on.
     *
     * @param signals an array of InputSignals to concatenate
     * @return the concatenated InputSignal
     */
    public InputSignal getConcatInputSignal(InputSignal... signals)
    {
        return new ConcatInputSignalImpl(signals);
    }

    /**
     * Returns an OutputSignal object for the specified signal.
     *
     * @param name the name of the signal
     * @param clock the ClockSignal used to drive this signal
     * @param outputEdges the clock edges to drive this signal on
     * @param outputSkew the output skew in ticks (should be positive)
     * @return an OutputSignal object for the specified signal
     */
    public OutputSignal getOutputSignal(
        String name,
        ClockSignal clock,
        EdgeSet outputEdges,
        int outputSkew)
    {
        SignalInfo info = getSignalInfoByName(name);
        checkOutputSignal(info);
        ClockMonitor clockMon = getClockMonitor(clock.getName(), 1);
        return new OutputSignalImpl(this, name, info.driveObj,
            info.definedInShell, info.inputMonitor, clock, clockMon,
            outputEdges, outputSkew);
    }

    /**
     * Returns an OutputSignal based on a bit range of another signal. The new
     * OutputSignal will have a width of <code>highBit - lowBit + 1</code>.
     *
     * @param baseSignal the OutputSignal containing the bits from which the new
     *      OutputSignal will be created
     * @param highBit the bit of <code>baseSignal</code> that will become the
     *      MSB of the new OutputSignal
     * @param lowBit the bit of <code>baseSignal</code> that will become the
     *      LSB of the new OutputSignal
     * @return the partial OutputSignal
     */
    public OutputSignal getPartialOutputSignal(
        OutputSignal baseSignal,
        int highBit,
        int lowBit)
    {
        return new PartialOutputSignalImpl(baseSignal, highBit, lowBit);
    }

    /**
     * Returns an OutputSignal that is the concatenation of multiple signals.
     * The first signal will be placed in the highest order bits of the new
     * signals, the second index in the next highest order bits, and so on.
     *
     * @param signals an array of OutputSignals to concatenate
     * @return the concatenated OutputSignal
     */
    public OutputSignal getConcatOutputSignal(OutputSignal... signals)
    {
        return new ConcatOutputSignalImpl(signals);
    }

    /**
     * Returns an InOutSignal object for the specified signal.
     *
     * @param name the name of the signal
     * @param clock the ClockSignal used to sample and drive this signal
     * @param inputEdges the clock edges to sample this signal on
     * @param inputSkew the input skew in ticks (should be negative)
     * @param outputEdges the clock edges to drive this signal on
     * @param outputSkew the output skew in ticks (should be positive)
     * @param depth the desired back-reference depth
     * @return an InOutSignal object for the specified signal
     */
    public InOutSignal getInOutSignal(
        String name,
        ClockSignal clock,
        EdgeSet inputEdges,
        int inputSkew,
        EdgeSet outputEdges,
        int outputSkew,
        int depth)
    {
        SignalInfo info = getSignalInfoByName(name);
        checkInputSignal(info);
        checkOutputSignal(info);
        ClockMonitor clockMon = getClockMonitor(clock.getName(), 1);
        return new InOutSignalImpl(this, name, info.inputMonitor,
            info.driveObj, info.definedInShell, clock, clockMon, inputEdges,
            inputSkew, outputEdges, outputSkew, depth);
    }

    /**
     * Returns an InOutSignal based on a bit range of another signal. The new
     * InOutSignal will have a width of <code>highBit - lowBit + 1</code>.
     *
     * @param baseSignal the InOutSignal containing the bits from which the new
     *      InOutSignal will be created
     * @param highBit the bit of <code>baseSignal</code> that will become the
     *      MSB of the new InOutSignal
     * @param lowBit the bit of <code>baseSignal</code> that will become the
     *      LSB of the new InOutSignal
     * @return the partial InOutSignal
     */
    public InOutSignal getPartialInOutSignal(
        InOutSignal baseSignal,
        int highBit,
        int lowBit)
    {
        return new PartialInOutSignalImpl(baseSignal, highBit, lowBit);
    }

    /**
     * Returns an InOutSignal that is the concatenation of multiple signals.
     * The first signal will be placed in the highest order bits of the new
     * signals, the second index in the next highest order bits, and so on.
     *
     * @param signals an array of InOutSignals to concatenate
     * @return the concatenated InOutSignal
     */
    public InOutSignal getConcatInOutSignal(InOutSignal... signals)
    {
        return new ConcatInOutSignalImpl(signals);
    }

    /**
     * Block the current thread until the specified simulation time.
     *
     * @param simTime the simulation time at which to unblock the current thread
     */
    public void waitForSimTime(long simTime)
    {
        dvEventManager.waitForSimTime(simTime);
    }

    /**
     * Block the current thread for the specified number of simulation ticks.
     *
     * @param ticks the number of ticks to wait before unblocking the thread
     */
    public void delay(long ticks)
    {
        dvEventManager.delay(ticks);
    }

    /**
     * Get the OVAEngine for this DVSimulation. If OVA is not supported in
     * this DVsimulation, a <code>DVRuntimeException</code> is thrown.
     *
     * @return The OVAEngine for this DVSimulation.
     * @throws DVRuntimeException if no OVA support is available.
     */
    public OVAEngine getOVAEngine()
    {
        if (ovaEngine == null)
        {
            throw new DVRuntimeException(
                "OVA support is not available in this DVSimulation");
        }

        return ovaEngine;
    }

    /**
     * Returns whether or not OVA is supported in this DVSimulation.
     *
     * @return <code>true</code> if OVA is supported in this simulation,
     *      <code>false</code> otherwise
     */
    public boolean hasOVASupport()
    {
        return (ovaEngine != null);
    }
}
