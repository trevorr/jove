/*
 * PLI4J - A Java (TM) Interface to the Verilog PLI
 * Copyright (C) 2003 Trevor A. Robinson
 * Java is a registered trademark of Sun Microsystems, Inc. in the U.S. or
 * other countries.
 *
 * Licensed under the Academic Free License version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You should
 * have received a copy of the License along with this software; if not, you
 * may obtain a copy of the License at
 *
 * http://opensource.org/licenses/afl-2.0.php
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.newisys.verilog;

/**
 * Represents an abstract Verilog application.
 * 
 * @author Trevor Robinson
 */
public abstract class VerilogApplication
{
    protected final VerilogSimulation verilogSim;

    public VerilogApplication(VerilogSimulation verilogSim)
    {
        this.verilogSim = verilogSim;
    }

    public VerilogSimulation getVerilogSim()
    {
        return verilogSim;
    }

    public abstract void registerObject(String name, VerilogObject obj);

    public abstract void registerSignal(
        String name,
        VerilogObject sampleObj,
        VerilogObject driveObj);

    public abstract void registerVerilogTask(
        String name,
        VerilogReg startReg,
        VerilogReg doneReg,
        VerilogObject[] argVars);

    public abstract void callJavaTask(
        String name,
        VerilogReg doneReg,
        VerilogObject[] argVars);

    public abstract void start();

    public abstract void finish();
}
