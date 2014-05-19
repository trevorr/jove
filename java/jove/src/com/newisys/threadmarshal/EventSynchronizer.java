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

/**
 * Interface and default implementation that the thread marshaller uses to wait
 * for and notify of the completion of a marshalled call. This default
 * implementation simply uses Object.wait() and Object.notifyAll().
 * 
 * @author Trevor Robinson
 */
public class EventSynchronizer
{
    /**
     * Blocks this thread until the method call represented by the given event
     * has completed.
     *
     * @param event the method call event to wait on
     * @throws ThreadMarshallerException if this thread is interrupted; the
     *             cause attribute of the exception will be the
     *             InterruptedException
     */
    public void waitFor(MethodCallEvent event)
        throws ThreadMarshallerException
    {
        synchronized (event)
        {
            if (!event.hasOccurred())
            {
                try
                {
                    event.wait();
                }
                catch (InterruptedException e)
                {
                    throw new ThreadMarshallerException(e);
                }
            }
        }
    }

    /**
     * Notifies that source thread that the method call represented by the given
     * event has completed and that it can now unblock.
     *
     * @param event the method call event to notify of
     */
    public void notifyOf(MethodCallEvent event)
    {
        synchronized (event)
        {
            event.setOccurred(true);
            event.notifyAll();
        }
    }
}
