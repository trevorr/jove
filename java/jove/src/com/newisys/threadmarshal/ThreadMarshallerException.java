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
 * Exception thrown when the thread marshaller is unable to invoke a marshalled
 * call in the target thread. The cause attribute of the exception indicates the
 * underlying reason for the failure.
 * 
 * @author Trevor Robinson
 */
public class ThreadMarshallerException
    extends Exception
{
    private static final long serialVersionUID = 3258135738901345843L;

    public ThreadMarshallerException(Throwable cause)
    {
        super(cause);
    }

    public ThreadMarshallerException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
