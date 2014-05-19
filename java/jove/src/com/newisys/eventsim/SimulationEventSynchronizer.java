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

package com.newisys.eventsim;

import com.newisys.threadmarshal.EventSynchronizer;
import com.newisys.threadmarshal.MethodCallEvent;
import com.newisys.threadmarshal.ThreadMarshallerException;

/**
 * An implementation of EventSynchronizer that handles threads managed by a
 * com.newisys.eventsim.SimulationManager as well as unmanaged threads.
 * 
 * @author Trevor Robinson
 */
class SimulationEventSynchronizer
    extends EventSynchronizer
{
    @Override
    public void notifyOf(MethodCallEvent event)
    {
        final SimulationThread t = SimulationThread.forThreadOrNull(event
            .getSourceThread());
        if (t != null)
        {
            if (t.getState() == ThreadState.MARSHALLING
                && t.blockingEvent == event)
            {
                // if thread is marshalling this method call to the scheduler
                // thread, clear the blocking event but allow the scheduler to
                // set the state to RUNNING, to avoid a race where the source
                // thread runs and marshals a second call before the scheduler
                // thread sees the first call finish
                t.blockingEvent = null;
            }
            else
            {
                SimulationManager simMgr = t.getManager();
                simMgr.notifyOf(event);
            }
        }
        else
        {
            super.notifyOf(event);
        }
    }

    @Override
    public void waitFor(MethodCallEvent event)
        throws ThreadMarshallerException
    {
        final SimulationThread t = SimulationThread.currentThreadOrNull();
        if (t != null)
        {
            SimulationManager simMgr = t.getManager();
            if (event.getTargetThread() == simMgr.getManagerThread())
            {
                assert (t.getState() == ThreadState.RUNNING);
                t.blockingEvent = event;
                t.setState(ThreadState.MARSHALLING);
                t.waitForState(ThreadState.RUNNING, false);
            }
            else
            {
                t.waitFor(event);
            }
        }
        else
        {
            super.waitFor(event);
        }
    }
}
