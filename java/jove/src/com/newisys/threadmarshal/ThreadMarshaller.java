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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;

/**
 * Manages marshalling calls from multiple source threads to a given target
 * thread. The source threads make calls on a proxy object, obtained using
 * getProxy(), which queues each call to be executed by the target thread and
 * blocks. The target thread dispatches the queued calls use the processCalls()
 * method, unblocking the source threads as each call completes.
 * 
 * @author Trevor Robinson
 */
public final class ThreadMarshaller
{
    // default access for efficient access by inner class
    final Thread targetThread;
    final LinkedList<MethodCallEvent> callQueue;
    final EventSynchronizer eventSync;

    /**
     * Constructs a new thread marshaller that dispatches calls to the given
     * target thread. The given EventSynchronizer argument determines how the
     * proxy thread waits on method calls to be completed, and how the target
     * thread notifies of completed method calls.
     *
     * @param targetThread the thread that will execute the calls
     * @param eventSync an instance of EventSynchronizer pr a subclass used to
     *            perform synchronization on the MethodCallEvents
     */
    public ThreadMarshaller(Thread targetThread, EventSynchronizer eventSync)
    {
        this.targetThread = targetThread;
        callQueue = new LinkedList<MethodCallEvent>();
        this.eventSync = eventSync;
    }

    /**
     * Generic handler for calls on thread marshaller proxy objects.
     */
    private final class MyInvocationHandler
        implements InvocationHandler
    {
        private final Object target;

        /**
         * Constructs a new invocation handler for the given target object.
         *
         * @param target the object this handler is forwarding proxy calls to
         */
        public MyInvocationHandler(Object target)
        {
            this.target = target;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
         *      java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable
        {
            // try to suppress access checking on the method, just in case
            if (!method.isAccessible())
            {
                try
                {
                    method.setAccessible(true);
                }
                catch (SecurityException ignored)
                {
                    // ignored; an exception here only indicates that we cannot
                    // suppress access checking, not that the call will fail
                }
            }

            // determine whether this call needs to be marshalled
            Thread thread = Thread.currentThread();
            if (thread == targetThread)
            {
                try
                {
                    return method.invoke(target, args);
                }
                catch (InvocationTargetException e)
                {
                    throw e.getCause();
                }
                catch (Exception e)
                {
                    throw new ThreadMarshallerException(e);
                }
            }
            else
            {
                // create a method call object and add it to the queue
                MethodCallEvent event = new MethodCallEvent(thread,
                    targetThread, method, target, args);
                synchronized (callQueue)
                {
                    callQueue.addLast(event);
                }

                // wait for the target thread to execute the call
                eventSync.waitFor(event);

                // throw an exception if one occurred during the call
                Throwable throwable = event.getThrowable();
                if (throwable != null)
                {
                    throw throwable;
                }

                // return the result from the call
                return event.getResult();
            }
        }
    }

    /**
     * Returns a proxy object (as generated by the java.lang.reflect.Proxy
     * class) that implements the same interfaces as the given target object and
     * uses this thread marshaller to dispatch those calls in its target thread.
     * For calls from threads other than the target thread, the proxy queues
     * calls in the thread marshaller, to later by executed in the target thread
     * using processCalls(). Calls made through the proxy from the target thread
     * are executed directly by the proxy.
     *
     * @param target the object to generate a proxy for
     * @return a proxy object derived from java.lang.reflect.Proxy that
     *         implements the same interfaces as the given target object
     * @see java.lang.reflect.Proxy for a detailed description of the properties
     *      of the returned proxy object
     */
    public Object getProxy(Object target)
    {
        // generate a proxy object for all the interfaces of the class
        // of the given target object
        Class cls = target.getClass();
        Object proxy = Proxy.newProxyInstance(cls.getClassLoader(), cls
            .getInterfaces(), new MyInvocationHandler(target));
        return proxy;
    }

    /**
     * Returns whether this thread marshaller has any queued calls pending.
     *
     * @return true iff marshalled calls have been made in other threads but
     *         have not yet been executed by the target thread
     */
    public boolean hasCalls()
    {
        synchronized (callQueue)
        {
            return !callQueue.isEmpty();
        }
    }

    /**
     * Executes any calls queued from other threads to be executed in the target
     * thread. This method must be called from the target thread specified when
     * this thread marshaller was created. This method will execute calls queued
     * up to the time that the last queued method returns. An exceptions
     * (including Errors) that occur during the execution of the queued calls
     * will be passed back to the source thread and will not affect the
     * execution of subsequent queued calls.
     */
    public void processCalls()
    {
        // calls must be dispatched from the target thread
        assert (Thread.currentThread() == targetThread);

        // execute calls until the call queue is empty
        while (true)
        {
            // get the next call from the call queue
            MethodCallEvent event = null;
            synchronized (callQueue)
            {
                if (!callQueue.isEmpty())
                {
                    event = callQueue.removeFirst();
                }
            }

            if (event != null)
            {
                // invoke method and store result or exception
                try
                {
                    Method method = event.getMethod();
                    Object target = event.getTarget();
                    Object[] arguments = event.getArguments();
                    Object result = method.invoke(target, arguments);
                    event.setResult(result);
                }
                catch (InvocationTargetException e)
                {
                    event.setThrowable(e.getCause());
                }
                catch (Throwable e)
                {
                    event.setThrowable(new ThreadMarshallerException(e));
                }

                // notify the source thread that the call has completed
                eventSync.notifyOf(event);
            }
            else
            {
                // no more calls in call queue
                break;
            }
        }
    }
}
