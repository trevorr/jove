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

import java.util.LinkedHashMap;
import java.util.Map;

import com.newisys.verilog.DriveDelayMode;
import com.newisys.verilog.EdgeSet;
import com.newisys.verilog.VerilogObject;
import com.newisys.verilog.VerilogReadValue;
import com.newisys.verilog.VerilogReg;
import com.newisys.verilog.VerilogSimTime;
import com.newisys.verilog.VerilogWriteValue;
import com.newisys.verilog.util.Bit;

/**
 * Manages Verilog tasks callable from Java.
 * 
 * @author Trevor Robinson
 */
final class VerilogTaskManager
{
    private final DVEventManager dvEventManager;

    public VerilogTaskManager(DVEventManager dvEventManager)
    {
        this.dvEventManager = dvEventManager;
    }

    private final Map<String, TaskInfo> taskMap = new LinkedHashMap<String, TaskInfo>();

    private static class TaskInfo
    {
        final String name;
        final VerilogReg startReg;
        final VerilogReg doneReg;
        final VerilogObject[] argVars;
        final Semaphore semaphore;

        public TaskInfo(
            String name,
            VerilogReg startReg,
            VerilogReg doneReg,
            VerilogObject[] argVars,
            Semaphore semaphore)
        {
            this.name = name;
            this.startReg = startReg;
            this.doneReg = doneReg;
            this.argVars = argVars;
            this.semaphore = semaphore;
        }
    }

    public void registerTask(
        String name,
        VerilogReg startReg,
        VerilogReg doneReg,
        VerilogObject[] argVars)
    {
        if (taskMap.containsKey(name))
        {
            throw new DVRuntimeException("Duplicate Verilog task registration");
        }
        Semaphore semaphore = new Semaphore(dvEventManager.simManager, 1);
        taskMap.put(name, new TaskInfo(name, startReg, doneReg, argVars,
            semaphore));
    }

    public void callTask(String name, Object[] args)
    {
        final TaskInfo info = taskMap.get(name);
        if (info == null)
        {
            throw new DVRuntimeException("Call to undefined Verilog task: "
                + name);
        }
        if (args.length != info.argVars.length)
        {
            throw new DVRuntimeException("Verilog task " + name + " expects "
                + info.argVars.length + " arguments; called with "
                + args.length);
        }

        // serialize access to task wrapper
        info.semaphore.acquire();
        try
        {
            // write actual arguments to task wrapper argument variables
            for (int i = 0; i < args.length; ++i)
            {
                VerilogWriteValue argVar = (VerilogWriteValue) info.argVars[i];
                Object argValue = args[i];

                // special handling for inout arguments:
                // if argument is an array, we assume it is a single-element
                // array and read the value from that element
                if (argValue instanceof Object[])
                {
                    Object[] argArray = (Object[]) argValue;
                    assert (argArray.length == 1);
                    argValue = argArray[0];
                    assert (!(argValue instanceof Object[]));
                }

                argVar.putValue(argValue);
            }

            // start task by setting start register
            info.startReg.putValueDelay(Bit.ONE, VerilogSimTime.TIME0,
                DriveDelayMode.PURE_TRANSPORT_DELAY);

            // wait for done register
            dvEventManager.waitForEdge(info.doneReg, EdgeSet.POSEDGE);

            // read task wrapper argument variables back into actual arguments
            for (int i = 0; i < args.length; ++i)
            {
                VerilogReadValue argVar = (VerilogReadValue) info.argVars[i];
                Object newValue = argVar.getValue();

                // special handling for inout arguments:
                // if argument is an array, we assume it is a single-element
                // array and write the value into that element
                Object oldValue = args[i];
                if (oldValue instanceof Object[])
                {
                    Object[] argArray = (Object[]) oldValue;
                    assert (argArray.length == 1);
                    argArray[0] = newValue;
                }
                else
                {
                    args[i] = newValue;
                }
            }

            // reset done register
            info.doneReg.putValueDelay(Bit.ZERO, VerilogSimTime.TIME0,
                DriveDelayMode.PURE_TRANSPORT_DELAY);
        }
        finally
        {
            info.semaphore.release();
        }
    }
}
