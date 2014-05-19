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

public class SimulationTimeStepTest
    extends TestCase
{

    final private int MAX = 20;
    final private SimulationTimeStep s[] = new SimulationTimeStep[MAX];
    final private SimpleUpdateEvent updateEvent = new SimpleUpdateEvent(9);
    final private SimpleEvaluationEvent evaluationEvent = new SimpleEvaluationEvent(
        17);

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(SimulationTimeStepTest.class);
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        for (int i = 0; i < MAX; i++)
        {
            long x;
            x = i * i * i;
            s[i] = new SimulationTimeStep(x);
        }

    }

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    public SimulationTimeStepTest(String name)
    {
        super(name);
    }

    final public void testGetTime()
    {
        for (int i = 0; i < MAX; i++)
        {
            long actual;
            long expected = i * i * i;
            actual = s[i].getTime();
            assertTrue(expected == actual);
        }
    }

    final public void testAddEventToActiveQueue()
    {
        checkEmpty();

        s[0].addEventToActiveQueue(updateEvent);
        s[1].addEventToActiveQueue(evaluationEvent);
        s[0].addEventToActiveQueue(updateEvent);
        s[0].addEventToActiveQueue(updateEvent);
        s[0].addEventToActiveQueue(evaluationEvent);
        for (int i = 0; i < 10000; i++)
        {
            s[2].addEventToActiveQueue(evaluationEvent);
            s[2].addEventToActiveQueue(updateEvent);
        }
    }

    final public void testAddEventToInactiveQueue()
    {
        checkEmpty();

        s[0].addEventToInactiveQueue(updateEvent);
        s[1].addEventToInactiveQueue(evaluationEvent);
        s[0].addEventToInactiveQueue(updateEvent);
        s[0].addEventToInactiveQueue(updateEvent);
        s[0].addEventToInactiveQueue(evaluationEvent);
        for (int i = 0; i < 10000; i++)
        {
            s[2].addEventToInactiveQueue(evaluationEvent);
            s[2].addEventToInactiveQueue(updateEvent);
        }
    }

    final public void testAddEventToNonblockingAssignUpdateQueue()
    {
        checkEmpty();

        s[0].addEventToNonblockingAssignUpdateQueue(updateEvent);
        s[1].addEventToNonblockingAssignUpdateQueue(evaluationEvent);
        s[0].addEventToNonblockingAssignUpdateQueue(updateEvent);
        s[0].addEventToNonblockingAssignUpdateQueue(updateEvent);
        s[0].addEventToNonblockingAssignUpdateQueue(evaluationEvent);
        for (int i = 0; i < 10000; i++)
        {
            s[2].addEventToNonblockingAssignUpdateQueue(evaluationEvent);
            s[2].addEventToNonblockingAssignUpdateQueue(updateEvent);
        }
    }

    final public void testAddEventToMonitorQueue()
    {
        checkEmpty();

        s[0].addEventToMonitorQueue(updateEvent);
        s[1].addEventToMonitorQueue(evaluationEvent);
        s[0].addEventToMonitorQueue(updateEvent);
        s[0].addEventToMonitorQueue(updateEvent);
        s[0].addEventToMonitorQueue(evaluationEvent);
        for (int i = 0; i < 10000; i++)
        {
            s[2].addEventToMonitorQueue(evaluationEvent);
            s[2].addEventToMonitorQueue(updateEvent);
        }
    }

    final public void testProcessEvents()
    {
        // Show that adding events makes the the non-empty.
        // Show that processing the events make the step empty.
        {
            checkEmpty();
            for (int i = 0; i < MAX; i++)
            {
                s[i].addEventToMonitorQueue(evaluationEvent);
                checkNotEmpty(i);
                s[i].processEvents();
                checkEmpty(i);
            }
        }

        // Show that processing events causes the event's execute method to be
        // called.
        {
            checkEmpty();
            SimpleUpdateEvent updateEvent = new SimpleUpdateEvent(6);
            SimpleEvaluationEvent evaluationEvent = new SimpleEvaluationEvent(
                12);

            s[0].addEventToInactiveQueue(updateEvent);
            s[0].addEventToNonblockingAssignUpdateQueue(evaluationEvent);

            assertTrue(updateEvent.x == 6);
            assertTrue(evaluationEvent.x == 12);
            assertTrue(updateEvent.y == -1);
            assertTrue(evaluationEvent.y == -1);

            s[0].processEvents();

            assertTrue(updateEvent.y == 12);
            assertTrue(evaluationEvent.y == 12);
        }

        // Show that processing events that cause other events cause all the
        // new events to get executed as well.
        {
            checkEmpty();
            for (int queue = 0; queue < 16; queue++)
            {
                SimpleUpdateEvent e = new SimpleUpdateEvent(8, s[5], queue);
                s[5].addEventToInactiveQueue(e);
                assertTrue(e.x == 8);
                assertTrue(e.y == -1);
                assertTrue(e.evaluationEvent == null);
                s[5].processEvents();
                checkEmpty(5);
                assertTrue(e.x == 8);
                assertTrue(e.y == 32);
                assertTrue(e.evaluationEvent.y == 32);
            }
        }

    }

    void checkEmpty(int i)
    {
        assertTrue(s[i].isEmpty());
    }

    void checkEmpty()
    {
        for (int i = 0; i < MAX; i++)
        {
            checkEmpty(i);
        }
    }

    void checkNotEmpty(int i)
    {
        assertTrue(s[i].isEmpty() == false);
    }

}
