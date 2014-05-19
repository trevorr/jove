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

package com.newisys.threadmarshal;

import java.lang.reflect.Method;

import com.newisys.eventsim.StepEvent;

/**
 * An event that represents a call to a method and any return value or exception
 * resulting from that call. The event is signaled when the execution of the
 * associated method has completed, either normally or abruptly, and any return
 * value or exception has been stored in the event object.
 * 
 * @author Trevor Robinson
 */
public final class MethodCallEvent
    extends StepEvent
{
    private final Thread sourceThread;
    private final Thread targetThread;
    private final Method method;
    private final Object target;
    private final Object[] arguments;
    private Object result;
    private Throwable throwable;

    public MethodCallEvent(
        Thread sourceThread,
        Thread targetThread,
        Method method,
        Object target,
        Object[] arguments)
    {
        super("MethodCallEvent[method=" + method.getName() + "; target="
            + target + "; sourceThread=" + sourceThread.getName()
            + "; targetThread=" + targetThread.getName() + "]");
        this.sourceThread = sourceThread;
        this.targetThread = targetThread;
        this.method = method;
        this.target = target;
        this.arguments = arguments;
    }

    public Thread getSourceThread()
    {
        return sourceThread;
    }

    public Thread getTargetThread()
    {
        return targetThread;
    }

    public Method getMethod()
    {
        return method;
    }

    public Object getTarget()
    {
        return target;
    }

    public Object[] getArguments()
    {
        return arguments;
    }

    public Object getResult()
    {
        return result;
    }

    public void setResult(Object result)
    {
        this.result = result;
    }

    public Throwable getThrowable()
    {
        return throwable;
    }

    public void setThrowable(Throwable exception)
    {
        this.throwable = exception;
    }

    @Override
    public void setOccurred(boolean occurred)
    {
        super.setOccurred(occurred);
    }
}
