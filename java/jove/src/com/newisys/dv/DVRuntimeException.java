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

/**
 * An exception to describe runtime errors that happen in the Jove
 * infrastructure.
 * 
 * @author Trevor Robinson
 */
public class DVRuntimeException
    extends RuntimeException
{
    private static final long serialVersionUID = 3833465102295642424L;

    /**
     * Constructs a new Jove runtime exception with null as its detail message.
     */
    public DVRuntimeException()
    {
        super();
    }

    /**
     * Constructs a new Jove runtime exception with the specified detail message.
     *
     * @param message the detail message. The detail message is saved for later
     * retrieval by the {@link Throwable#getMessage} method.
     */
    public DVRuntimeException(String message)
    {
        super(message);
    }

    /**
     * Constructs a new Jove runtime exception with the specified cause and a
     * detail message of (cause==null ? null : cause.toString())  (which
     * typically contains the class and detail message of cause). This constructor
     * is useful for Jove runtime exceptions that are little more than wrappers
     * for other throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the
     * {@link Throwable#getCause()} method). (A null value is permitted, and
     * indicates that the cause is nonexistent or unknown.)
     */
    public DVRuntimeException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructs a new Jove runtime exception with the specified detail message
     * and cause.
     * <P>
     * Note that the detail message associated with cause is not automatically
     * incorporated in this exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval by
     * the {@link Throwable#getMessage} method).
     * @param cause the cause (which is saved for later retrieval by the
     * {@link Throwable#getCause()} method). (A null value is permitted, and
     * indicates that the cause is nonexistent or unknown.)
     */
    public DVRuntimeException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
