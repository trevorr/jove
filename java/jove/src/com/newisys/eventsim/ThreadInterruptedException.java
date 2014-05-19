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
 * Exception propagated from blocking methods when a simulation thread is
 * interrupted (by a call to Thread.interrupt()). An unhandled exception of this
 * concrete type usually indicates a programming error.
 * 
 * @author Trevor Robinson
 */
public class ThreadInterruptedException
    extends RuntimeException
{
    private static final long serialVersionUID = 3835150657916254518L;

    public ThreadInterruptedException()
    {
        super();
    }

    public ThreadInterruptedException(String message)
    {
        super(message);
    }

    public ThreadInterruptedException(Throwable cause)
    {
        super(cause);
    }

    public ThreadInterruptedException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
