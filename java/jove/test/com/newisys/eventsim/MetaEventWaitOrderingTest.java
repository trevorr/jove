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

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import com.newisys.behsim.BehavioralSimulation;
import com.newisys.dv.DV;
import com.newisys.dv.DVApplication;
import com.newisys.dv.DVSimulation;
import com.newisys.eventsim.SimulationManager;
import com.newisys.eventsim.SimulationThread;
import com.newisys.ova.OVAEngine;

/**
 * This testcase checks that when multiple threads are waiting on separate
 * MetaEvents, each of which are satisfiable by the same Event, that the
 * threads are run in the order in which they started waiting on the MetaEvent.
 *
 * This insures reproducibility when running a test multiple times.
 *
 */
public class MetaEventWaitOrderingTest
    extends TestCase
{
    BehavioralSimulation sim;
    SimulationManager simManager;
    OVAEngine ovaEngine;

    @Override
    protected void setUp()
    {
        // create the simulation objects
        sim = new BehavioralSimulation();
        simManager = new SimulationManager();
        ovaEngine = null;

        DV.simulation = new DVSimulation(sim, simManager, ovaEngine);
    }

    private void launch(DVApplication dvApp)
    {
        try
        {
            // start the DVApplication
            dvApp.start();

            // start the behavioral simulator
            sim.run();
            simManager.executeThreads();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            assertFalse("Unexpected exception during test", true);
        }
        finally
        {
            // finish the application
            dvApp.finish();
        }
    }

    public void testSimpleHandShake()
    {
        class SyncOrderTest
            extends DVApplication
        {
            public final List<Integer> order = new LinkedList<Integer>();

            public SyncOrderTest(DVSimulation sim)
            {
                super(sim);
            }

            public void run()
            {
                final Event triggerEvent = new PulseEvent();

                DV.simulation.joinAll(new SimulationThread[] {
                    DV.simulation.fork(new Runnable()
                    {
                        public void run()
                        {
                            DV.simulation.waitForAny(triggerEvent);
                            order.add(1);
                        }

                    }), DV.simulation.fork(new Runnable()
                    {
                        public void run()
                        {
                            DV.simulation.waitForAny(triggerEvent);
                            order.add(2);
                        }

                    }), DV.simulation.fork(new Runnable()
                    {
                        public void run()
                        {
                            DV.simulation.waitForAny(triggerEvent);
                            order.add(3);
                        }

                    }), DV.simulation.fork(new Runnable()
                    {
                        public void run()
                        {
                            DV.simulation.waitForAny(triggerEvent);
                            order.add(4);
                        }

                    }), DV.simulation.fork(new Runnable()
                    {
                        public void run()
                        {
                            DV.simulation.waitForAny(triggerEvent);
                            order.add(5);
                        }

                    }), DV.simulation.fork(new Runnable()
                    {
                        public void run()
                        {
                            DV.simulation.notifyOf(triggerEvent);
                        }

                    }) });
            }
        }

        for (int i = 0; i < 200; ++i)
        {
            setUp();
            SyncOrderTest soTest = new SyncOrderTest(DV.simulation);
            launch(soTest);

            List<Integer> order = soTest.order;
            assertEquals(5, order.size());
            assertEquals(Integer.valueOf(1), order.get(0));
            assertEquals(Integer.valueOf(2), order.get(1));
            assertEquals(Integer.valueOf(3), order.get(2));
            assertEquals(Integer.valueOf(4), order.get(3));
            assertEquals(Integer.valueOf(5), order.get(4));

        }
    }
}
