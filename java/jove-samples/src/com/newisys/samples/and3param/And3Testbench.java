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

package com.newisys.samples.and3param;

import com.newisys.dv.DVApplication;
import com.newisys.dv.DVSimulation;
import com.newisys.dv.Mailbox;
import com.newisys.randsolver.Solver;
import com.newisys.verilog.EdgeSet;
import com.newisys.verilog.util.BitVector;

/**
 * Class containing testbench/testcase code for the and3 DUT. This testbench is
 * meant to be more exemplary than practical. The basic idea is to test out the
 * 3 input adder defined in and3.v. This function of this adder is described by:
 * <P>
 * <code>out = in1 & in2 & in3</code>
 * <P>
 * where out, in1, in2, and in3 are all 1 bit values.
 */
public final class And3Testbench
    extends DVApplication
{
    class ExpectedData
    {
        final int dut;
        final BitVector value;

        ExpectedData(int dut, BitVector value)
        {
            this.dut = dut;
            this.value = value;
        }
    }

    /**
     * The "port" through which all signal access occurs. {@link And3Port} is
     * generated from and3.if by jove-ifgen. The <code>ifgen</code> target in
     * <code>and3-project/build.xml</code> is responsible for this generation.
     */
    private And3Port[] and3 = new And3Port[2];

    /**
     * A mailbox used to pass expected data from the stimulus generation method
     * to the data checking method.
     */
    private Mailbox<ExpectedData> mbox;

    /**
     * A Boolean value which is <code>true</code> when the data at the output
     * of the DUT is valid and should be checked.
     */
    private boolean dataValid = false;

    /**
     * Sole constructor. All classes derived from {@link DVApplication} must
     * contain a constructor that takes a {@link DVSimulation} object and then
     * call <code>super(DVSimulation)</code>.
     * <P>
     * Note that signals cannot be accessed at this time as they may not have
     * been initialized yet. Signals can only be accessed once {@link #run} has
     * been called.
     *
     * @param dvSim the DVSimulation to be used by this DVApplication
     */
    public And3Testbench(DVSimulation dvSim)
    {
        super(dvSim);
    }

    /**
     * Contains the code for running this {@link DVApplication}. This method is
     * inherited from DVApplication and is called when the infrastructure has
     * been completey initialized and is ready to run user code. This method
     * can be considered your program's "main" method.
     * <P>
     * This implementation runs an exhaustive test, followed by a random test.
     */
    public void run()
    {
        for (int i = 0; i < 2; ++i)
        {
            and3[i] = And3Shell.getAnd3Bind(i);
        }
        mbox = dvSim.createMailbox();

        System.out.println("Running exhaustive test");
        runExhaustiveStim();
        System.out.println("Running random test");
        runRandomStim();
    }

    /**
     * Exhaustively tests the DUT. Tries each of the 8 combinations of input
     * values and checks the results. Note that after driving each value, this
     * method blocks until a positive edge is seen on the clock before
     * continuing.
     */
    private void runExhaustiveStim()
    {
        for (int dut = 0; dut < 1; ++dut)
        {
            for (int i = 0; i < 8; ++i)
            {
                driveData(dut, i);
                and3[dut].clk.syncEdge(EdgeSet.POSEDGE);
                if (!checkResults())
                {
                    throw new RuntimeException("checkResults failed");
                }
            }
        }
    }

    /**
     * Randomly tests the DUTs. This method uses an {@link And3Stim} for stimulus
     * generation. Each iteration it is randomized and the result is driven onto
     * the pins of a randomly chosen DUT. It then blocks until a positive edge is
     * seen on the clock, at which time it checks the output of the DUT.
     */
    private void runRandomStim()
    {
        // The And3Stim class is annotated as Randomizable
        And3Stim stim = new And3Stim();

        for (int i = 0; i < 100; ++i)
        {
            Solver.randomize(stim, dvSim.getRandom());

            final int dut = dvSim.getRandom().nextInt(2);
            driveData(dut, stim.getValue());
            and3[dut].clk.syncEdge(EdgeSet.POSEDGE);
            if (!checkResults())
            {
                throw new RuntimeException("checkResults failed");
            }
        }
    }

    /**
     * Drives data onto the DUT's input pins. Check that <code>i</code> is a
     * value from 0-7. Then convert this value to a 3-bit {@link BitVector}.
     * Finally drive this BitVector onto the <code>dataout</code> bus as defined
     * in <code>and3.if</code> and put the value in the mailbox.
     *
     * @param dut the dut on which to drive the value
     * @param i the value to drive
     */
    private void driveData(int dut, int i)
    {
        assert (i >= 0 && i <= 7);
        BitVector driveValue = new BitVector(3, i);
        and3[dut].dataout.drive(driveValue);
        mbox.put(new ExpectedData(dut, driveValue));
    }

    /**
     * Checks the results at the output pins of the DUT. This code checks if the
     * output data is valid this cycle and if so, compares it against the
     * contents of the mailbox. Since the mailbox contains a 3 bit value, a
     * reductive and is used to calculate the expected value.
     *
     * @return true if the actual data matched the expected data, false
     *      otherwise
     */
    private boolean checkResults()
    {
        if (!dataValid)
        {
            // Data will not be valid the first time this is called
            // since it takes one clock for the output to propagate
            dataValid = true;
            return true;
        }

        ExpectedData expected = mbox.getNoWait();
        BitVector expectedResult = new BitVector(1, expected.value
            .reductiveAnd());
        BitVector result = and3[expected.dut].datain.sample();
        assert (result.length() == 1);

        if (result.equalsExact(expectedResult))
        {
            return true;
        }
        System.out.println(dvSim.getSimTime() + ": dut: " + expected.dut
            + ", stimulus: " + expected.value + ", actual: " + result
            + ", expected: " + expectedResult);
        return false;
    }

}
