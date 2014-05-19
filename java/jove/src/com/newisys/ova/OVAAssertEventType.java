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
 * Enumeration of event types for {@link OVAAssert} objects.
 * 
 * @author Jon Nall
 */
public enum OVAAssertEventType implements OVAEventType
{
    /**
     * Corresponds to all events that occur on an {@link OVAAssert}.
     */
    All(303),

    /**
     * An {@link OVAAssert} is reset.
     *
     * @see OVAAssertAction#Reset
     */
    Reset(304),

    /**
     * A new matching attempt is started for an {@link OVAAssert} object.
     */
    NewAttemptStarted(305),

    /**
     * A matching attempt on an {@link OVAAssert} object is removed.
     */
    AttemptRemoved(306),

    /**
     * A matching attempt on an {@link OVAAssert} fails.
     */
    AttemptFailure(307),

    /**
     * A matching attempt on an {@link OVAAssert} succeeds.
     */
    AttemptSuccess(308),

    /**
     * TODO: document AttemptMarker
     */
    AttemptMarker(309),

    /**
     * New matching attempts on an {@link OVAAssert} are disabled.
     */
    DisableNewAttempts(310),

    /**
     * New matching attempts on an {@link OVAAssert} are ensabled.
     */
    EnableNewAttempts(311),

    /**
     * A matching attempt on an {@link OVAAssert} is killed.
     */
    AttemptKilled(312),

    /**
     * TODO: document TransitionSuccess
     */
    TransitionSuccess(313),

    /**
     * TODO: document TransitionFailure
     */
    TransitionFailure(314),

    /**
     * New matching attempt reporting is disabled.
     */
    DisableNewAttemptReporting(315),

    /**
     * New matching attempt reporting is enabled.
     */
    EnableNewAttemptReporting(316),

    /**
     * TODO: document LocalVarCreated.
     */
    LocalVarCreated(317),

    /**
     * TODO: document LocalVarDuplicated.
     */
    LocalVarDuplicated(318),

    /**
     * TODO: document LocalVarUpdated.
     */
    LocalVarUpdated(319),

    /**
     * TODO: document LocalVarDestroyed.
     */
    LocalVarDestroyed(320);

    private int value;

    private OVAAssertEventType(int value)
    {
        this.value = value;
    }

    /**
     * Returns the integer value associated with this OVAAssertEventType.
     *
     * @return the integer value for this enumeration
     */
    public int getValue()
    {
        return value;
    }

    /**
     * Returns the OVAAssertEventType for the given integer value.
     *
     * @param value the value for which to return an OVAAssertEventType
     * @return the OVAAssertEventType corresponding to <code>value</code>
     * @throws IllegalArgumentException if <code>value</code> does not
     *      correspond to any OVAAssertEventType
     */
    public static OVAAssertEventType forValue(int value)
    {
        OVAAssertEventType[] enums = OVAAssertEventType.class
            .getEnumConstants();
        for (final OVAAssertEventType enumeration : enums)
        {
            if (enumeration.value == value)
            {
                return enumeration;
            }
        }

        throw new IllegalArgumentException("Value [" + value + "] does not"
            + " correspond to any value of class: " + OVAAssertEventType.class);
    }
}
