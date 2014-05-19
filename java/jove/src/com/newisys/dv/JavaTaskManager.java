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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

import com.newisys.verilog.DriveDelayMode;
import com.newisys.verilog.VerilogObject;
import com.newisys.verilog.VerilogReadValue;
import com.newisys.verilog.VerilogReg;
import com.newisys.verilog.VerilogSimTime;
import com.newisys.verilog.VerilogWriteValue;
import com.newisys.verilog.util.Bit;

/**
 * Manages Java tasks callable from Verilog.
 * 
 * @author Trevor Robinson
 */
final class JavaTaskManager
{
    private final DVEventManager dvEventManager;

    public JavaTaskManager(DVEventManager dvEventManager)
    {
        this.dvEventManager = dvEventManager;
    }

    private final Map<String, TaskInfo> taskMap = new LinkedHashMap<String, TaskInfo>();

    private static class TaskInfo
    {
        final String name;
        final Method method;
        final boolean varArgs;
        final Object object;

        public TaskInfo(
            String name,
            Method method,
            boolean varArgs,
            Object object)
        {
            this.name = name;
            this.method = method;
            this.varArgs = varArgs;
            this.object = object;
        }
    }

    private final static String taskPrefix = "task_";

    public void registerTasks(Class cls)
    {
        registerTasks(cls, null);
    }

    public void registerTasks(Object object)
    {
        registerTasks(object.getClass(), object);
    }

    private void registerTasks(Class< ? > cls, Object object)
    {
        while (cls != null)
        {
            Method[] methods = cls.getDeclaredMethods();
            int count = methods.length;
            for (int i = 0; i < count; ++i)
            {
                Method method = methods[i];
                boolean isStatic = (method.getModifiers() & Modifier.STATIC) != 0;
                boolean isCallable = (object != null) || isStatic;
                if (isCallable && method.getName().startsWith(taskPrefix))
                {
                    registerTask(method, object);
                }
            }
            cls = cls.getSuperclass();
        }
    }

    public void registerTask(Method method, Object object)
    {
        String name = getVerilogNameForTask(method.getName());
        Class[] paramTypes = method.getParameterTypes();
        boolean varArgs = paramTypes.length == 1
            && paramTypes[0].equals(Object[].class);
        taskMap.put(name, new TaskInfo(name, method, varArgs, object));
    }

    private static String getVerilogNameForTask(String methodName)
    {
        if (methodName.startsWith(taskPrefix))
        {
            final int prefixLen = taskPrefix.length();
            return methodName.substring(prefixLen);
        }
        return methodName;
    }

    private class TaskThread
        implements Runnable
    {
        private final TaskInfo info;
        private final VerilogReg doneReg;
        private final VerilogObject[] argVars;

        public TaskThread(
            TaskInfo info,
            VerilogReg doneReg,
            VerilogObject[] argVars)
        {
            this.info = info;
            this.doneReg = doneReg;
            this.argVars = argVars;
        }

        public void run()
        {
            try
            {
                Object[] values = new Object[argVars.length];

                // read current values of arguments
                for (int i = 0; i < argVars.length; ++i)
                {
                    VerilogReadValue argVar = (VerilogReadValue) argVars[i];
                    values[i] = argVar.getValue();
                }

                if (info.varArgs)
                {
                    // invoke method with single Object[] argument
                    Object[] invokeArgs = new Object[] { values };
                    info.method.invoke(info.object, invokeArgs);

                    // write back new values for output arguments
                    for (int i = 0; i < argVars.length; ++i)
                    {
                        if (argVars[i] instanceof VerilogWriteValue)
                        {
                            VerilogWriteValue argVar = (VerilogWriteValue) argVars[i];
                            argVar.putValue(values[i]);
                        }
                    }
                }
                else
                {
                    // invoke method with zero or more Object arguments
                    info.method.invoke(info.object, values);
                }
            }
            catch (IllegalArgumentException e)
            {
                throw new DVRuntimeException("Error calling Java task: "
                    + info.name, e);
            }
            catch (IllegalAccessException e)
            {
                throw new DVRuntimeException("Error calling Java task: "
                    + info.name, e);
            }
            catch (InvocationTargetException e)
            {
                throw new DVRuntimeException("Exception in Java task call: "
                    + info.name, e.getCause());
            }
            finally
            {
                doneReg.putValueDelay(Bit.ONE, VerilogSimTime.TIME0,
                    DriveDelayMode.PURE_TRANSPORT_DELAY);
            }
        }
    }

    public void callTask(
        String name,
        VerilogReg doneReg,
        VerilogObject[] argVars)
    {
        final TaskInfo info = taskMap.get(name);
        if (info == null)
        {
            throw new DVRuntimeException("Call to undefined Java task: " + name);
        }
        dvEventManager.simManager.fork(name, new TaskThread(info, doneReg,
            argVars));
        dvEventManager.executeThreads();
    }
}
