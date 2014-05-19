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
 * Configuration options for the OVA Engine.
 * 
 * @author Jon Nall
 */
public enum OVAConfigSwitch
{
    /**
     * TODO: document ShowLineInfo
     */
    ShowLineInfo(1001),

    /**
     * Turns off all messages at runtime. Default: <code>false</code>.
     */
    Quiet(1002),

    /**
     * Enables printing of summary at end of simulation.
     * Default: <code>false</code>.
     */
    PrintReport(1003),

    /**
     * TODO: document ManageAttempts
     */
    ManageAttempts(1004);

    private int value;

    private OVAConfigSwitch(int value)
    {
        this.value = value;
    }

    /**
     * Returns the integer value for this OVAConfigSwitch.
     *
     * @return the integer value for this enumeration
     */
    public int getValue()
    {
        return value;
    }
}
