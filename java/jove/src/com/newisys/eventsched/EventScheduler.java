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

package com.newisys.eventsched;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Implements the various event queues of a Verilog simulator.
 * 
 * @author Scott Diesing
 */
public class EventScheduler
{
    private final SortedMap<Long, SimulationTimeStep> timeStepQueue = Collections
        .synchronizedSortedMap(new TreeMap<Long, SimulationTimeStep>());
    private SimulationTimeStep currentTimeStep;
    private long currentTime = 0;
    private final List<SimulationEvent> simulationStartEvents = Collections
        .synchronizedList(new LinkedList<SimulationEvent>());
    private final List<SimulationEvent> simulationEndEvents = Collections
        .synchronizedList(new LinkedList<SimulationEvent>());
    private final List<SimulationEvent> nextSimTimeEvents = Collections
        .synchronizedList(new LinkedList<SimulationEvent>());
    private final List<SimulationEvent> activeNextSimTimeEvents = Collections
        .synchronizedList(new LinkedList<SimulationEvent>());
    private boolean terminateRequested = false;

    public EventScheduler()
    {
    }

    public long getCurrentTime()
    {
        return currentTime;
    }

    /**
     * Request that the EventScheduler leave its processing loop, even if there
     * are events left in the various queues. It will exit on its next iteration
     * of processEvents()
     */
    public void finish()
    {
        terminateRequested = true;
    }

    /**
     * Add an event that will be executed at the start of the simulation.
     *
     * @param event event to be added.
     */
    public void addSimulationStartEvent(SimulationEvent event)
    {
        simulationStartEvents.add(event);
    }

    /**
     * Add an event that will be executed at the start of the next sim time.
     *
     * @param event event to be added.
     */
    public void addNextSimTimeEvent(SimulationEvent event)
    {
        nextSimTimeEvents.add(event);
    }

    /**
     * Add an event that will be executed at the end of the simulation.
     *
     * @param event event to be added.
     */
    public void addSimulationEndEvent(SimulationEvent event)
    {
        simulationEndEvents.add(event);
    }

    /**
     * Add an event to the active queue.
     *
     * @param event event to be added.
     * @param time time relative to current time.
     */
    public void addEventToActiveQueue(SimulationEvent event, long time)
    {
        SimulationTimeStep timeStep = getTimeStep(time);
        timeStep.addEventToActiveQueue(event);
    }

    /**
     * Add an event to the front of the active queue.
     *
     * @param event event to be added.
     * @param time time relative to current time.
     */
    public void addEventToFrontOfActiveQueue(SimulationEvent event, long time)
    {
        SimulationTimeStep timeStep = getTimeStep(time);
        timeStep.addEventToFrontOfActiveQueue(event);
    }

    /**
     * Add an event to the inactive queue.
     *
     * @param event event to be added.
     * @param time time relative to current time.
     */
    public void addEventToInactiveQueue(SimulationEvent event, long time)
    {
        SimulationTimeStep timeStep = getTimeStep(time);
        timeStep.addEventToInactiveQueue(event);
    }

    /**
     * Add an event to the nonblocking assign update queue.
     *
     * @param event event to be added.
     * @param time time relative to current time.
     */
    public void addEventToNonblockingAssignUpdateQueue(
        SimulationEvent event,
        long time)
    {
        SimulationTimeStep timeStep = getTimeStep(time);
        timeStep.addEventToNonblockingAssignUpdateQueue(event);
    }

    /**
     * Add an event to the monitor queue.
     *
     * @param event event to be added.
     * @param time time relative to current time.
     */
    public void addEventToMonitorQueue(SimulationEvent event, long time)
    {
        SimulationTimeStep timeStep = getTimeStep(time);
        timeStep.addEventToMonitorQueue(event);
    }

    /**
     * Process all the events in the simulationStartEvents queue
     */
    public void processStartOfSimulationEvents()
    {
        // Process the Simulation Start events.
        synchronized (simulationStartEvents)
        {
            while (simulationStartEvents.size() != 0)
            {
                SimulationEvent event;
                event = simulationStartEvents.remove(0);
                event.execute();
            }
        }
    }

    /**
     * Process all the events in the simulationEndEvents queue
     */
    public void processEndOfSimulationEvents()
    {
        // Process the Simulation End events.
        synchronized (simulationEndEvents)
        {
            while (simulationEndEvents.size() != 0)
            {
                SimulationEvent event;
                event = simulationEndEvents.remove(0);
                event.execute();
            }
        }
    }

    /**
     * Process all the events in the timestep queues
     */
    public void processEvents()
    {
        while (!terminateRequested && isEmpty() == false)
        {
            processNextSimulationTimeStep();
        }
    }

    /**
     * Determine if the timeStepQueue is empty.
     *
     * @return true if the queue is empty.
     */
    public boolean isEmpty()
    {
        return (timeStepQueue.size() == 0);
    }

    /**
     * Process the implicit events scheduled in the previous time step. Then
     * process the events explicitly scheduled for this time step.
     */
    public void processNextSimulationTimeStep()
    {
        // Move all the events to the activeNextSimTimeEvents
        // queue and clear the future nextSimTimeEvents queue in case executing
        // these events cause events to be added to the nextSimTimeEvents.
        synchronized (nextSimTimeEvents)
        {
            if (nextSimTimeEvents.size() != 0)
            {
                synchronized (activeNextSimTimeEvents)
                {
                    activeNextSimTimeEvents.addAll(nextSimTimeEvents);
                    nextSimTimeEvents.clear();
                }
            }
        }

        // Execute all the events in the activeNextSimTimeEvents queue.
        synchronized (activeNextSimTimeEvents)
        {
            while (activeNextSimTimeEvents.size() != 0)
            {
                SimulationEvent event;
                event = activeNextSimTimeEvents.remove(0);
                event.execute();
            }
        }

        // Process the events for this time step.
        if (timeStepQueue.size() != 0)
        {
            currentTime = timeStepQueue.firstKey();
            currentTimeStep = timeStepQueue.remove(currentTime);
            currentTimeStep.processEvents();
        }
    }

    /**
     * Find the time step that goes with this time. If not found, create one and
     * add it to the timestep queue.
     *
     * @param time simulation time relative to the current simulation time.
     * @return SimulationTimeStep
     */
    private SimulationTimeStep getTimeStep(long time)
    {
        SimulationTimeStep step;
        long absoluteTime = time + currentTime;

        if (currentTime == absoluteTime)
        {
            step = currentTimeStep;
        }
        else
        {
            step = timeStepQueue.get(absoluteTime);
        }
        if (step == null)
        {
            step = new SimulationTimeStep(absoluteTime);
            timeStepQueue.put(absoluteTime, step);
            if (currentTimeStep == null)
            {
                currentTimeStep = step;
            }
        }
        return step;
    }

    /**
     * Find the event in one of the queues and remove it.
     *
     * @param event the event to remove
     * @return true if the event was found, false if it was not found.
     */
    public boolean cancelEvent(SimulationEvent event)
    {
        // check the current time step.
        if (currentTimeStep != null && currentTimeStep.remove(event))
        {
            return true;
        }
        if (simulationStartEvents.remove(event))
        {
            return true;
        }
        if (simulationEndEvents.remove(event))
        {
            return true;
        }
        if (nextSimTimeEvents.remove(event))
        {
            return true;
        }
        if (activeNextSimTimeEvents.remove(event))
        {
            return true;
        }
        if (removeFromQueue(event))
        {
            return true;
        }
        return false;
    }

    private boolean removeFromQueue(SimulationEvent event)
    {
        final Iterator<SimulationTimeStep> iter = timeStepQueue.values()
            .iterator();
        while (iter.hasNext())
        {
            final SimulationTimeStep timestep = iter.next();
            if (timestep.remove(event))
            {
                if (timestep.isEmpty())
                {
                    iter.remove();
                }
                return true;
            }
        }
        return false;
    }
}
