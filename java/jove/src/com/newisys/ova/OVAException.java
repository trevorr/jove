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

package com.newisys.ova;

/**
 * Class representing exceptions occurring when performing OVA related actions.
 * 
 * @author Jon Nall
 */
public final class OVAException
    extends RuntimeException
{
    private static final long serialVersionUID = 3545234726278477364L;

    /**
     * Constructs an OVAException with the specified message
     *
     * @param s the message associated with this OVAException
     */
    public OVAException(String s)
    {
        super(s);
    }

    /**
     * Constructs an OVAException from the specified exception
     *
     * @param e the the exception from which to create this OVAException
     */
    public OVAException(Exception e)
    {
        super(e);
    }
}
