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

package com.newisys.eventsim;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.newisys.random.PRNG;
import com.newisys.random.PRNGFactory;
import com.newisys.random.PRNGFactoryFactory;
import com.newisys.threadmarshal.ThreadMarshaller;
import com.newisys.util.logging.SimpleFormatter;

/**
 * Manages the creation, scheduling, and synchronization of serialized
 * simulation threads.
 * 
 * @author Trevor Robinson
 */
public class SimulationManager
{
    static final String pkgName = SimulationManager.class.getPackage()
        .getName();
    static final Logger logger = Logger.getLogger(pkgName);
    static
    {
        if ("1".equals(System.getProperty(pkgName + ".enableLogging")))
        {
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
            Handler handler;
            try
            {
                handler = new FileHandler(logger.getName() + ".log");
            }
            catch (IOException e)
            {
                throw new Error(e);
            }
            handler.setFormatter(new SimpleFormatter(false, false));
            handler.setLevel(Level.ALL);
            logger.addHandler(handler);
        }
    }

    /**
     * Maintains a mapping of Events to Sets of SimulationThreads.
     */
    final static class EventThreadsMap
    {
        private final Map<Event, Set<SimulationThread>> map = new HashMap<Event, Set<SimulationThread>>();

        public Set<SimulationThread> addThread(Event e, SimulationThread t)
        {
            Set<SimulationThread> set = getThreads(e);
            if (set == null)
            {
                set = new LinkedHashSet<SimulationThread>();
                map.put(e, set);
            }
            set.add(t);
            return set;
        }

        public boolean removeThread(Event e, SimulationThread t)
        {
            final Set<SimulationThread> threadSet = getThreads(e);
            assert (threadSet != null);
            boolean noMoreThreads = false;
            if (threadSet.remove(t) && threadSet.isEmpty())
            {
                removeThreads(e);
                noMoreThreads = true;
            }
            return noMoreThreads;
        }

        public Set<SimulationThread> getThreads(Event e)
        {
            return map.get(e);
        }

        public void removeThreads(Event e)
        {
            map.remove(e);
        }

        public Set<Event> getEvents()
        {
            return map.keySet();
        }
    }

    /**
     * Maintains a mapping of Events to Sets of MetaEvents that contain them.
     */
    final static class MetaEventMap
    {
        private final Map<Event, Set<MetaEvent>> map = new HashMap<Event, Set<MetaEvent>>();

        public void addMetaEvent(MetaEvent metaEvent)
        {
            // add meta event to mappings of each contained event;
            // do this recursively for contained meta events
            for (final Event event : metaEvent.getEvents())
            {
                if (event instanceof MetaEvent)
                {
                    final MetaEvent nestedMetaEvent = (MetaEvent) event;
                    addMetaEvent(nestedMetaEvent);
                }
                addMetaEvent(event, metaEvent);
            }
        }

        private Set<MetaEvent> addMetaEvent(Event e, MetaEvent m)
        {
            Set<MetaEvent> set = getMetaEvents(e);
            if (set == null)
            {
                set = new LinkedHashSet<MetaEvent>();
                map.put(e, set);
            }
            set.add(m);
            return set;
        }

        public Set<MetaEvent> getMetaEvents(Event e)
        {
            return map.get(e);
        }

        public void removeMetaEvent(MetaEvent metaEvent)
        {
            // remove meta event from mappings of each contained event
            for (final Event event : metaEvent.getEvents())
            {
                removeMetaEvent(event, metaEvent);
            }
        }

        private void removeMetaEvent(Event e, MetaEvent m)
        {
            final Set<MetaEvent> set = getMetaEvents(e);
            if (set != null)
            {
                set.remove(m);
                if (set.isEmpty())
                {
                    removeMetaEvents(e);
                }
            }
        }

        private void removeMetaEvents(Event e)
        {
            map.remove(e);
        }

        public Set<Event> getEvents()
        {
            return map.keySet();
        }
    }

    // name of this simulation manager
    private final String name;

    // used to create random number generators for each thread
    private final PRNGFactory defRandomFactory;

    // random number generator used to seed top-level thread RNGs
    private final PRNG seedSource;

    // thread used to schedule execution of threads managed by this object
    private final Thread managerThread;

    // used by simulation threads to execute calls from the manager thread
    private final ThreadMarshaller threadMarshaller;

    // list of unterminated threads managed by this object
    final List<SimulationThread> activeThreads = new LinkedList<SimulationThread>();

    // queue of threads ready and waiting to run
    private final LinkedList<SimulationThread> pendingQueue = new LinkedList<SimulationThread>();

    // mapping of events to threads waiting on them
    final EventThreadsMap eventThreadsMap = new EventThreadsMap();

    // mapping of events to meta events waiting on them
    final MetaEventMap metaEventMap = new MetaEventMap();

    /**
     * Constructs a new simulation manager with the default name, random
     * factory, and seed source.
     */
    public SimulationManager()
    {
        this(generateName(), PRNGFactoryFactory.getDefaultFactory());
    }

    // used to generate names for unnamed simulation managers
    private static int serialNo = 1;

    private static synchronized String generateName()
    {
        return "SimulationManager-" + serialNo++;
    }

    /**
     * Constructs a new simulation manager with the given name and random
     * factory, and a seed source created from the given factory with seed 0.
     *
     * @param name the name of this simulation manager (for debugging purposes)
     * @param defRandomFactory the factory used to create random number
     *            generators for top-level threads
     */
    public SimulationManager(String name, PRNGFactory defRandomFactory)
    {
        this(name, defRandomFactory, defRandomFactory.newInstance(0));
    }

    /**
     * Constructs a new simulation manager with the given name, random factory,
     * and seed source.
     *
     * @param name the name of this simulation manager (for debugging purposes)
     * @param defRandomFactory the factory used to create random number
     *            generators for top-level threads
     * @param seedSource the random number generator used to seed top-level
     *            random number generators
     */
    public SimulationManager(
        String name,
        PRNGFactory defRandomFactory,
        PRNG seedSource)
    {
        this.name = name;
        this.defRandomFactory = defRandomFactory;
        this.seedSource = seedSource;
        this.managerThread = Thread.currentThread();
        this.threadMarshaller = new ThreadMarshaller(managerThread,
            new SimulationEventSynchronizer());
    }

    /**
     * Returns the thread used to schedule execution of simulation threads
     * managed by this object. This is generally the thread that created the
     * simulation manager.
     *
     * @return the thread used to schedule execution of simulation threads
     */
    public Thread getManagerThread()
    {
        return managerThread;
    }

    /**
     * Returns the thread marshaller used to allow simulation threads to execute
     * calls from the scheduler thread.
     *
     * @return the thread marshaller for this simulation manager
     */
    public ThreadMarshaller getThreadMarshaller()
    {
        return threadMarshaller;
    }

    // SimulationThread interface

    /**
     * Adds the given thread to the list of active threads managed by this
     * object.
     *
     * @param t the simulation thread to add
     */
    final void addActiveThread(SimulationThread t)
    {
        synchronized (activeThreads)
        {
            assert (!activeThreads.contains(t));
            activeThreads.add(t);
        }
    }

    /**
     * Removes the given thread from the list of active threads managed by this
     * object.
     *
     * @param t the simulation thread to remove
     */
    final void removeActiveThread(SimulationThread t)
    {
        synchronized (activeThreads)
        {
            final boolean contained = activeThreads.remove(t);
            assert (contained);
        }
    }

    /**
     * Adds the given thread to the end of the pending thread queue.
     *
     * @param t the simulation thread to add
     */
    final void pushPendingThread(SimulationThread t)
    {
        synchronized (pendingQueue)
        {
            assert (!pendingQueue.contains(t));
            pendingQueue.addLast(t);
        }
    }

    /**
     * Removes and returns the first thread from the beginning of the pending
     * thread queue.
     *
     * @return the thread at the head of the pending queue
     */
    final SimulationThread popPendingThread()
    {
        SimulationThread t = null;
        synchronized (pendingQueue)
        {
            if (!pendingQueue.isEmpty())
            {
                t = pendingQueue.removeFirst();
            }
        }
        return t;
    }

    // controller interface

    private void checkNotSimThread()
    {
        final SimulationThread t = SimulationThread.currentThreadOrNull();
        if (t != null && t.manager == this)
        {
            throw new IllegalThreadException(
                "Cannot call this method from a managed thread");
        }
    }

    /**
     * Returns whether this object is currently managing any unterminated
     * threads.
     * <p>
     * This method must not be called from a managed simulation thread.
     *
     * @return true iff there are unterminated threads
     */
    public final boolean hasActiveThreads()
    {
        checkNotSimThread();

        synchronized (activeThreads)
        {
            // look for the first unterminated thread
            for (final SimulationThread t : activeThreads)
            {
                if (t.getState() != ThreadState.TERMINATED) return true;
            }
        }
        return false;
    }

    private volatile boolean terminateThreadsCalled;

    /**
     * Executes runnable threads until none remain (i.e. all threads have
     * blocked or terminated).
     * <p>
     * This method must be called from the thread that created this simulation
     * manager.
     *
     * @throws UnhandledExceptionException if a simulation thread is terminated
     *             by an unhandled exception
     */
    public void executeThreads()
        throws UnhandledExceptionException
    {
        logger.entering(getClass().getName(), "executeThreads");

        checkNotSimThread();
        assert (Thread.currentThread() == managerThread);

        // clear the flag that indicates terminateThreads() was called by a
        // simulation thread
        terminateThreadsCalled = false;

        // execute threads from the pending queue until none remain or a thread
        // has called terminateThreads()
        while (!terminateThreadsCalled)
        {
            // pop the first pending thread from the queue
            final SimulationThread t = popPendingThread();

            // done if no pending threads
            if (t == null) break;

            // make sure the thread is still pending and not terminated
            final ThreadState origState = t.getState();
            if (origState == ThreadState.PENDING)
            {
                ThreadState newState;
                while (true)
                {
                    // set the thread state to RUNNING, which runs the thread
                    t.setState(ThreadState.RUNNING);

                    // wait for the thread to yield, block, or terminate
                    newState = t.waitForState(ThreadState.RUNNING, true);
                    if (newState != ThreadState.MARSHALLING) break;

                    // execute call marshalled to this thread
                    assert (threadMarshaller.hasCalls());
                    threadMarshaller.processCalls();
                }

                // thread must be pending, blocked, or terminated
                assert (newState == ThreadState.PENDING
                    || newState == ThreadState.BLOCKED || newState == ThreadState.TERMINATED);

                // check for termination by unhandled exception
                if (newState == ThreadState.TERMINATED
                    && t.unhandledException != null)
                {
                    throw new UnhandledExceptionException(t,
                        t.unhandledException);
                }
            }
            else
            {
                // another thread terminated this thread before it could run
                assert (origState == ThreadState.TERMINATED);
            }
        }

        // if terminateThreads() was called from a simulation thread, wait to
        // return until all threads have terminated
        if (terminateThreadsCalled)
        {
            waitForThreadsTerminated();
        }

        logger.exiting(getClass().getName(), "executeThreads");
    }

    /**
     * Terminates all threads currently registered in the simulation. This
     * method initiates the termination process for all threads, then waits
     * until all threads have entered the TERMINATED state.
     * <p>
     * This method may be called from both managed simulation threads and
     * external threads.
     */
    public final void terminateThreads()
    {
        logger.entering(getClass().getName(), "terminateThreads");

        // if this method is called from a simulation thread, indicate to the
        // main thread waiting in executeThreads() that terminateThreads() has
        // been called, so that it can wait for all threads to terminate
        terminateThreadsCalled = true;

        // initiate termination process on all threads
        synchronized (activeThreads)
        {
            for (final SimulationThread cur : activeThreads)
            {
                cur.terminate(false);
            }
        }

        // wait until all threads have terminated
        // (if the current thread is a simulation thread, this wait will
        // cause an ThreadTerminatedException to be thrown)
        waitForThreadsTerminated();

        logger.exiting(getClass().getName(), "terminateThreads");
    }

    private void waitForThreadsTerminated()
    {
        waitLabel: while (true)
        {
            // find the first unterminated thread
            SimulationThread cur;
            searchLabel: synchronized (activeThreads)
            {
                final Iterator<SimulationThread> iter = activeThreads
                    .iterator();
                while (iter.hasNext())
                {
                    cur = iter.next();
                    if (cur.getState() != ThreadState.TERMINATED)
                    {
                        break searchLabel;
                    }
                }

                // no unterminated threads
                break waitLabel;
            }

            // wait for thread to terminate
            cur.waitForState(ThreadState.TERMINATED, false);
        }
    }

    // thread fork interface

    /**
     * Starts a new simulation thread with the specified name.
     * <p>
     * This method may be called from both managed simulation threads and
     * external threads.
     *
     * @param name the name of this thread, for debugging purposes
     * @param r the code to run for this thread
     * @return the SimulationThread object associated with the new thread
     */
    public final SimulationThread fork(String name, Runnable r)
    {
        logger.entering(getClass().getName(), "fork", name);

        // get the parent thread (or null if the current thread is not a
        // simulation thread)
        final SimulationThread parent = SimulationThread.currentThreadOrNull();

        // create the random number generator for the new thread
        final PRNGFactory randomFactory;
        final PRNG childRandom;
        if (parent != null)
        {
            randomFactory = parent.getRandomFactory();
            PRNG parentRandom = parent.getRandom();
            childRandom = randomFactory.newInstance(parentRandom);
        }
        else
        {
            randomFactory = defRandomFactory;
            childRandom = randomFactory.newInstance(seedSource);
        }

        // create the thread
        final SimulationThread t = new SimulationThread(this, parent, r,
            childRandom, randomFactory, name);

        // wait for the thread to start
        final ThreadState newState;
        try
        {
            newState = t.waitForState(ThreadState.STARTING, true);
        }
        catch (ThreadTerminatedException e)
        {
            // if the parent thread has been terminated at this point,
            // terminate the child thread also; this handles the case where
            // terminateThreads() is called but one of the terminated
            // threads forks before it terminates
            t.terminate();
            throw e;
        }

        if (newState == ThreadState.PENDING)
        {
            // add thread to pending queue
            pushPendingThread(t);
        }
        else
        {
            // thread must have terminated already
            assert (newState == ThreadState.TERMINATED);
        }

        logger.exiting(getClass().getName(), "fork", t);
        return t;
    }

    // used to generate names for unnamed threads
    private int threadSerialNo = 1;

    private synchronized String generateThreadName()
    {
        return "SimulationThread-" + threadSerialNo++;
    }

    /**
     * Starts a new simulation thread with a generated name.
     *
     * @param r the code to run for this thread
     * @return the SimulationThread object associated with the new thread
     */
    public final SimulationThread fork(Runnable r)
    {
        return fork(generateThreadName(), r);
    }

    /**
     * Starts a set of new simulation threads with generated names.
     *
     * @param rs the code to run for each thread
     * @return the SimulationThread objects associated with the new threads
     */
    public final SimulationThread[] fork(Runnable... rs)
    {
        SimulationThread[] result = new SimulationThread[rs.length];
        for (int i = 0; i < rs.length; ++i)
        {
            result[i] = fork(rs[i]);
        }
        return result;
    }

    // event notification interface

    /**
     * Notifies waiting threads that an event has occurred.
     * <p>
     * This method may be called from both managed simulation threads and
     * external threads.
     *
     * @param e the event to notify of
     */
    public final void notifyOf(Event e)
    {
        logger.entering(getClass().getName(), "notifyOf", e);

        // mark the event as having occurred
        e.setOccurred(true);

        // notify any threads waiting on this event
        synchronized (eventThreadsMap)
        {
            // get the set of threads waiting on this event
            final Set<SimulationThread> threadSet = eventThreadsMap
                .getThreads(e);
            if (threadSet != null)
            {
                // remove the thread set from the event map;
                // new waits will go into a new thread set
                eventThreadsMap.removeThreads(e);

                // change all threads in set to PENDING
                for (final SimulationThread t : threadSet)
                {
                    assert (t.blockingEvent == e);
                    t.blockingEvent = null;
                    assert (t.getState() == ThreadState.BLOCKED);
                    pushPendingThread(t);
                    t.setState(ThreadState.PENDING);
                }
            }
        }

        // notify any meta events waiting on this event
        synchronized (metaEventMap)
        {
            // get the set of meta events waiting on this event
            final Set<MetaEvent> metaEventSet = metaEventMap.getMetaEvents(e);
            if (metaEventSet != null)
            {
                // NOTE: meta events are notified in two phases, to handle
                // the case where one meta event contains another meta event
                // waiting for the same event

                // 1. notify each meta event that the event occurred
                for (final MetaEvent metaEvent : metaEventSet)
                {
                    metaEvent.notifyOf(e);
                }

                // 2. check whether each meta event is satisfied
                final LinkedList<MetaEvent> satisfiedMetaEvents = new LinkedList<MetaEvent>();
                for (final MetaEvent metaEvent : metaEventSet)
                {
                    if (metaEvent.hasOccurred())
                    {
                        // notify threads and other meta events waiting on
                        // this meta event
                        notifyOf(metaEvent);

                        // remember meta event for later removal from
                        // mappings
                        satisfiedMetaEvents.add(metaEvent);
                    }
                }

                // remove satisfied meta events from all event mappings
                for (final MetaEvent metaEvent : satisfiedMetaEvents)
                {
                    if (metaEvent.isAutoReset())
                    {
                        metaEvent.reset();
                    }
                    metaEventMap.removeMetaEvent(metaEvent);
                }
            }
        }

        logger.exiting(getClass().getName(), "notifyOf", e);
    }

    // overridden methods

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return name;
    }
}
