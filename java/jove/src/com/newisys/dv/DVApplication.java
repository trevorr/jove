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

import java.io.PrintStream;

import com.newisys.verilog.ValueType;
import com.newisys.verilog.VerilogApplication;
import com.newisys.verilog.VerilogConstant;
import com.newisys.verilog.VerilogObject;
import com.newisys.verilog.VerilogReadValue;
import com.newisys.verilog.VerilogReg;
import com.newisys.verilog.VerilogWriteValue;

/**
 * The base class from which all Jove applications are derived. Derived
 * classes will override the {@link Runnable#run} method.
 * 
 * @author Trevor Robinson
 */
public abstract class DVApplication
    extends VerilogApplication
    implements Runnable
{
    protected final DVSimulation dvSim;
    protected final PrintStream out;

    /**
     * Creates a new DVApplication.
     *
     * @param dvSim the DVSimulation that will be associated with this DVApplication
     */
    public DVApplication(DVSimulation dvSim)
    {
        super(dvSim.verilogSim);

        this.dvSim = dvSim;

        // initialize DV.simulation (if not initialized already) to allow
        // static access to DVSimulation (e.g. from signal interface classes);
        // if already initialized, do nothing and assume this application will
        // not use static simulation access
        if (DV.simulation == null) DV.simulation = dvSim;

        out = new PrintStream(verilogSim.getLogOutputStream(), true);

        dvSim.registerJavaTasks(this);
    }

    /**
     * Registers a Verilog object with this DVApplication.
     *
     * @param name the name of the object
     * @param obj the Verilog object to register
     */
    @Override
    public void registerObject(String name, VerilogObject obj)
    {
        if (Debug.enabled)
        {
            Debug.out.println("registerObject(" + name + "," + obj + ")");
        }

        dvSim.dvObjDir.registerObject(name, obj);
    }

    /**
     * Registers a signal with this DVApplication.
     *
     * @param name the name of the signal
     * @param sampleObj the Verilog object to be used when sampling the signal
     * @param driveObj the Verilog object to be used when driving the signal
     */
    @Override
    public void registerSignal(
        String name,
        VerilogObject sampleObj,
        VerilogObject driveObj)
    {
        if (Debug.enabled)
        {
            Debug.out.println("registerSignal(" + name + "," + sampleObj + ","
                + driveObj + ")");
        }

        final VerilogReadValue sampleRV;
        final InputMonitor inputMonitor;
        if (sampleObj instanceof VerilogReadValue && !isConstantZero(sampleObj))
        {
            sampleRV = (VerilogReadValue) sampleObj;
            inputMonitor = new InputMonitor(dvSim.dvEventManager, name,
                sampleRV, ValueType.OBJ_TYPE, 1);
        }
        else
        {
            assert (isConstantZero(sampleObj));
            sampleRV = null;
            inputMonitor = null;
        }

        final VerilogWriteValue driveWV;
        if (driveObj instanceof VerilogWriteValue && !isConstantZero(driveObj))
        {
            driveWV = (VerilogWriteValue) driveObj;
        }
        else
        {
            assert (isConstantZero(driveObj));
            driveWV = null;
        }

        final SignalInfo info = new SignalInfo(name, sampleRV, driveWV, true);
        info.inputMonitor = inputMonitor;
        dvSim.dvObjDir.registerSignal(name, info);
    }

    /**
     * Returns whether the given Verilog object is a constant zero.
     *
     * @param obj the Verilog object to check
     * @return true if <code>obj</code> is a constant zero, false otherwise.
     */
    private static boolean isConstantZero(VerilogObject obj)
    {
        if (obj instanceof VerilogConstant)
        {
            VerilogConstant constant = (VerilogConstant) obj;
            Integer value = (Integer) constant.getValue(ValueType.INT);
            return value.intValue() == 0;
        }
        return false;
    }

    /**
     * Registers a verilog task with this DVApplication.
     *
     * @param name the name of the task
     * @param startReg the Verilog register that is written when the task has started
     * @param doneReg the Verilog register that is written when the task has completed
     * @param argVars an array of VerilogObjects used to hold arguments to the task
     */
    @Override
    public void registerVerilogTask(
        String name,
        VerilogReg startReg,
        VerilogReg doneReg,
        VerilogObject[] argVars)
    {
        if (Debug.enabled)
        {
            Debug.out.println("registerVerilogTask(" + name + "," + startReg
                + "," + doneReg + ")");
        }

        dvSim.verilogTaskManager.registerTask(name, startReg, doneReg, argVars);
    }

    /**
     * Executes a registered Java task. The given task will be scheduled for
     * execution in a new simulation thread. When the task completes, the given
     * register will be set to 1'b1.
     * <p>
     * This method must be called from the main/HDL simulator thread.
     *
     * @param name the name of the java task
     * @param doneReg the Verilog register that will be written when the java
     * task is complete
     * @param argVars an array of VerilogObjects used to hold arguments to the
     * task
     */
    @Override
    public void callJavaTask(
        String name,
        VerilogReg doneReg,
        VerilogObject[] argVars)
    {
        if (Debug.enabled)
        {
            Debug.out.println("callJavaTask(" + name + "," + doneReg + ")");
        }

        dvSim.javaTaskManager.callTask(name, doneReg, argVars);
    }

    /**
     * Starts execution of this DVApplication. This method will fork a new
     * SimulationThread and begin executing the {@link Runnable#run} method
     * of the derived class.
     */
    @Override
    public void start()
    {
        if (Debug.enabled)
        {
            Debug.out.println("start()");
        }

        dvSim.simManager.fork("DVProgramWrapper", new DVProgramWrapper(dvSim,
            this));
        dvSim.dvEventManager.executeThreads();
    }

    /**
     * Terminates all threads and ends this DVApplication.
     */
    @Override
    public void finish()
    {
        if (Debug.enabled)
        {
            Debug.out.println("finish()");
        }

        dvSim.simManager.terminateThreads();
    }
}
