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

package com.newisys.dv.ifgen;

/**
 * Exception thrown when an Ifgen schema object cannot be translated to Java.
 * 
 * @author Trevor Robinson
 */
public class IfgenTranslatorException
    extends Exception
{
    private static final long serialVersionUID = 1L;

    public IfgenTranslatorException()
    {
        super();
    }

    public IfgenTranslatorException(String message)
    {
        super(message);
    }

    public IfgenTranslatorException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public IfgenTranslatorException(Throwable cause)
    {
        super(cause);
    }
}
