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

import junit.framework.TestCase;

import com.newisys.dv.DVRuntimeException;
import com.newisys.verilog.VerilogReg;
import com.newisys.verilog.VerilogSimTime;

public class BehavioralSimulationTest
    extends TestCase
{
    final private int MAX_REGS = 5;
    final private int MAX_CALLBACKS = 7;
    final private String s[] = { "Howdy", "my", "name", "is", "Rowdy" };
    final private VerilogReg regs[] = new VerilogReg[MAX_REGS];
    final private BehavioralSimulation sim = new BehavioralSimulation();
    final private BehavioralSimulationCallback cb[] = new BehavioralSimulationCallback[MAX_CALLBACKS];

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(BehavioralSimulationTest.class);
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

    public BehavioralSimulationTest(String name)
    {
        super(name);
    }

    final public void testCreateRegisterGetObjectByName()
    {

        for (int i = 0; i < MAX_REGS; i++)
        {
            regs[i] = sim.createRegister(s[i], i + 1);
        }

        for (int i = 0; i < MAX_REGS; i++)
        {
            VerilogReg r = (VerilogReg) sim.getObjectByName(s[i]);
            assertTrue(r.equals(regs[i]));
        }
    }

    final public void testCallbacks()
    {
        int i = 0;
        cb[i++] = (BehavioralSimulationCallback) sim.addDelayCallback(
            new VerilogSimTime(7), new TestCallbackHandler());
        cb[i++] = (BehavioralSimulationCallback) sim
            .addSimulationStartCallback(new TestCallbackHandler());
        cb[i++] = (BehavioralSimulationCallback) sim
            .addSimulationEndCallback(new TestCallbackHandler());
        cb[i++] = (BehavioralSimulationCallback) sim
            .addReadWriteSynchCallback(new TestCallbackHandler());
        cb[i++] = (BehavioralSimulationCallback) sim.addStartOfSimTimeCallback(
            new VerilogSimTime(8), new TestCallbackHandler());
        cb[i++] = (BehavioralSimulationCallback) sim
            .addReadOnlySynchCallback(new TestCallbackHandler());
        cb[i++] = (BehavioralSimulationCallback) sim
            .addNextSimTimeCallback(new TestCallbackHandler());

        for (i = 0; i < MAX_REGS; i++)
        {
            sim.cancelCallback(cb[i]);
        }
    }

    public void testCreateRegister()
    {
        VerilogReg reg1, reg2;

        reg1 = sim.createRegister("test_in", 5);
        reg2 = sim.createRegister("test_out", 5);
        assertTrue(reg1 == reg2);

        reg1 = sim.createRegister("boo_in", 5);
        reg2 = sim.createRegister("boo_in", 5);
        assertTrue(reg1 == reg2);

        String actualExceptionString = "NOPE";
        String expectedExceptionString = "com.newisys.dv.DVRuntimeException: "
            + "BehavioralReg size mismatch.  previous=" + 5 + " new=" + 8;
        try
        {
            reg1 = sim.createRegister("test_in", 5);
            reg2 = sim.createRegister("test_out", 8);
        }
        catch (DVRuntimeException e)
        {
            actualExceptionString = e.toString();
        }
        assertTrue(actualExceptionString.equals(expectedExceptionString));
    }

}
