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
 * Actions that can be performed on an {@link OVAAssert} via its
 * {@link OVAAssert#doAction} method.
 * 
 * @author Jon Nall
 */
public enum OVAAssertAction
{
    /**
     * Resets temporal assertions, discarding the current state. This has no
     * effect on non-temporal assertions.
     */
    Reset(404),

    /**
     * Disables new attempts to match the assertion. This has no effect on
     * match attempts that started prior to and including the clock on which
     * this action is performed.
     */
    DisableNewAttempts(405),

    /**
     * Enables new attempts to match the assertion. The attempts are enabled
     * on the clock immediately after the clock on which this action is
     * performed.
     */
    EnableNewAttempts(406),

    /**
     * TODO: document KillAttmpt
     */
    KillAttempt(407),

    /**
     * TODO: document EnableTraceOn
     */
    EnableTraceOn(408),

    /**
     * TODO: document DisableNewAttemptReporting
     */
    DisableNewAttemptReporting(409),

    /**
     * TODO: document EnableNewAttemptReporting
     */
    EnableNewAttemptReporting(410);

    private int value;

    private OVAAssertAction(int value)
    {
        this.value = value;
    }

    /**
     * Returns the integer value associated with this OVAAssertAction.
     *
     * @return the integer value of this enumeration
     */
    public int getValue()
    {
        return value;
    }
}
