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

package com.newisys.samples.behavioral;

import com.newisys.dv.ClockSignal;
import com.newisys.dv.DV;
import com.newisys.dv.DVApplication;
import com.newisys.dv.DVSimulation;
import com.newisys.dv.Mailbox;
import com.newisys.eventsim.SimulationThread;
import com.newisys.verilog.EdgeSet;

/**
 * Example of a behavioral test. This class shows off a few Jove features:<br>
 * - Forks and joins<br>
 * - Mailboxes<br>
 * - Waiting for a particular clock signal edge<br>
 */
public final class BehavioralSample
    extends DVApplication
{

    /**
     * Required DVApplication constructor.
     * 
     * @param dvSim a reference to the DVSimulation
     */
    public BehavioralSample(DVSimulation dvSim)
    {
        super(dvSim);
    }

    /**
     * This test creates two threads. The first inserts an integer into a
     * mailbox on each posedge of the clock. The second waits until there is a
     * value in the mailbox and then retreives it.
     * <P>
     * After forking the threads, the run method does a join to wait until they
     * have both completed before returning.
     */
    public void run()
    {
        final int[] values = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final Mailbox<Integer> mbox = dvSim.createMailbox();
        final ClockSignal clk = dvSim.getDefaultClockSignal();

        final SimulationThread producer = dvSim.fork("producer-thread",
            new Runnable()
            {
                public void run()
                {
                    for (int i = 0; i < values.length; ++i)
                    {
                        // Block until the rising edge of the clock
                        clk.syncEdge(EdgeSet.POSEDGE);

                        // DV.simulation is a reference to the current
                        // DVSimulation object.
                        System.out.println(DV.simulation.getSimTime()
                            + ": Putting " + values[i] + " into mailbox");
                        mbox.put(values[i]);
                    }

                }
            });

        final SimulationThread consumer = dvSim.fork("consumer-thread",
            new Runnable()
            {
                public void run()
                {
                    for (int i = 0; i < values.length; ++i)
                    {
                        // Block until there is a value in the mailbox.
                        final int value = mbox.getWait();

                        // DV.simulation is a reference to the current
                        // DVSimulation object.
                        System.out.println(DV.simulation.getSimTime()
                            + ": Received " + values[i] + " from mailbox");

                        // Check the value
                        if (value != i)
                        {
                            throw new AssertionError(
                                "Unexpected value. Actual: " + value
                                    + ", expected: " + i);
                        }
                    }
                }
            });

        // Wait until both threads have completed before returning
        dvSim.joinAll(new SimulationThread[] { producer, consumer });
    }

}
