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
 * Exception thrown by SimulationManager.executeThreads() when a simulation
 * thread is terminated by an unhandled exception.
 * 
 * @author Trevor Robinson
 * @see SimulationManager#executeThreads()
 */
public class UnhandledExceptionException
    extends RuntimeException
{
    private static final long serialVersionUID = 3762256331217844528L;
    private final SimulationThread thread;

    /**
     * Constructs a new UnhandledExceptionException for the given simulation
     * thread and unhandled exception.
     *
     * @param thread the simulation thread that was terminated by the unhandled
     *            exception
     * @param exception the unhandled exception
     */
    public UnhandledExceptionException(
        SimulationThread thread,
        Throwable exception)
    {
        super("Unhandled exception in thread " + thread.getName() + ": "
            + exception, exception);
        this.thread = thread;
    }

    /**
     * Returns the simulation thread that was terminated by the unhandled
     * exception.
     *
     * @return the simulation thread
     */
    public SimulationThread getThread()
    {
        return thread;
    }
}
