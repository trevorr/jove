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
 * Actions that can be performed on an {@link OVAEngine} via its
 * {@link OVAEngine#doAction} method.
 * 
 * @author Jon Nall
 */
public enum OVAEngineAction
{
    /**
     * Stops all attempts at matching assertions. Attempts are re-enabled on
     * the following clock cycle.
     */
    Reset(202),

    /**
     * Stops the OVA engine. All further matching attempts are stopped. The
     * engine cannot be re-enabled.
     */
    Finish(203),

    /**
     * TODO: document Terminate
     */
    Terminate(204);

    private int value;

    private OVAEngineAction(int value)
    {
        this.value = value;
    }

    /**
     * Returns the integer value associated with this OVAEngineAction.
     *
     * @return the integer value of this enumeration
     */
    public int getValue()
    {
        return value;
    }
}
