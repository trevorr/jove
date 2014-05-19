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

import junit.framework.TestCase;

public class EventSchedulerTest
    extends TestCase
{
    private EventScheduler eventScheduler;

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(EventSchedulerTest.class);
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    public EventSchedulerTest(String name)
    {
        super(name);
    }

    final public void testBasic()
    {
        for (int queue = 0; queue < 16; queue++)
        {
            runBasicTest(queue);
        }
    }

    final private void runBasicTest(int queue)
    {
        {
            SimpleUpdateEvent updateEvent = new SimpleUpdateEvent(99);
            eventScheduler = new EventScheduler();
            long expected = 0;
            assertTrue(expected == eventScheduler.getCurrentTime());
            long lastTime = 9999;
            long firstTime = 88;

            add(updateEvent, lastTime, eventScheduler, queue);
            add(updateEvent, firstTime, eventScheduler, queue);
            assertTrue(expected == eventScheduler.getCurrentTime());
            eventScheduler.processEvents();
            assertTrue(lastTime == eventScheduler.getCurrentTime());
        }

        {
            SimpleUpdateEvent updateEvent = new SimpleUpdateEvent(99);
            SimpleEvaluationEvent evaluationEvent = new SimpleEvaluationEvent(
                999);
            eventScheduler = new EventScheduler();

            assertTrue(updateEvent.x == 99);
            assertTrue(evaluationEvent.x == 999);
            assertTrue(updateEvent.y == -1);
            assertTrue(evaluationEvent.y == -1);

            long timeZero = 0;
            long lastTime = 9999;
            long firstTime = 88;

            assertTrue(timeZero == eventScheduler.getCurrentTime());
            add(updateEvent, lastTime, eventScheduler, queue);
            add(updateEvent, firstTime, eventScheduler, queue);
            add(evaluationEvent, lastTime, eventScheduler, queue);
            assertTrue(timeZero == eventScheduler.getCurrentTime());

            eventScheduler.processNextSimulationTimeStep();
            assertTrue(firstTime == eventScheduler.getCurrentTime());
            assertTrue(evaluationEvent.y == -1);
            assertTrue(updateEvent.y == (99 * 2));

            eventScheduler.processNextSimulationTimeStep();
            assertTrue(lastTime == eventScheduler.getCurrentTime());
            assertTrue(updateEvent.y == (99 * 2));
            assertTrue(evaluationEvent.y == 999);
        }
    }

    final private void add(
        SimulationEvent event,
        long time,
        EventScheduler sched,
        int queue)
    {
        switch (queue % 4)
        {
        case 0:
            sched.addEventToActiveQueue(event, time);
            break;
        case 1:
            sched.addEventToInactiveQueue(event, time);
            break;
        case 2:
            sched.addEventToMonitorQueue(event, time);
            break;
        case 3:
        // fall through on purpose
        default:
            sched.addEventToNonblockingAssignUpdateQueue(event, time);
            break;
        }
    }
}
