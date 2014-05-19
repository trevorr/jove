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

/**
 * Describes the state of a simulation thread.
 * 
 * @author Trevor Robinson
 */
public enum ThreadState
{
    /**
     * Thread is waiting to be started.
     */
    STARTING,

    /**
     * Thread is ready and waiting for its turn to run.
     */
    PENDING,

    /**
     * Thread is currently running.
     */
    RUNNING,

    /**
     * Thread is waiting for the scheduler thread to complete a marshalled call.
     */
    MARSHALLING,

    /**
     * Thread is waiting for an event.
     */
    BLOCKED,

    /**
     * Thread has completed execution.
     */
    TERMINATED;
}
