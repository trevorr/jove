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
 * Enumeration of event types for {@link OVAEngine} objects.
 * 
 * @author Jon Nall
 */
public enum OVAEngineEventType implements OVAEventType
{
    /**
     * Corresponds to all events that occur on an {@link OVAEngine}
     */
    All(101),

    /**
     * The {@link OVAEngine} has started initializing.
     */
    InitializeBegin(102),

    /**
     * The {@link OVAEngine} has completed initializing.
     */
    InitializeEnd(103),

    /**
     * The {@link OVAEngine} has started.
     */
    EngineStarted(104),

    /**
     * The {@link OVAEngine} has started resetting.
     */
    ResetBegin(105),

    /**
     * The {@link OVAEngine} has completed resetting.
     */
    ResetEnd(106),

    /**
     * TODO: document LoadBegin
     */
    LoadBegin(107),

    /**
     * TODO: document LoadBegin
     */
    LoadEnd(108),

    /**
     * The {@link OVAEngine} has finished.
     */
    EngineFinished(109),

    /**
     * The {@link OVAEngine} has started terminating.
     */
    TerminateBegin(110),

    /**
     * The {@link OVAEngine} has completed terminating.
     */
    TerminateEnd(111),

    /**
     * The {@link OVAEngine} has encountered an error.
     */
    EngineError(112);

    private int value;

    private OVAEngineEventType(int value)
    {
        this.value = value;
    }

    /**
     * Returns the integer value associated with this OVAEngineEventType.
     *
     * @return the integer value for this enumeration
     */
    public int getValue()
    {
        return value;
    }

    /**
     * Returns the OVAEngineEventType for the given integer value.
     *
     * @param value the value for which to return an OVAEngineEventType
     * @return the OVAEngineEventType corresponding to <code>value</code>
     * @throws IllegalArgumentException if <code>value</code> does not
     *      correspond to any OVAEngineEventType
     */
    public static OVAEngineEventType forValue(int value)
    {
        OVAEngineEventType[] enums = OVAEngineEventType.class
            .getEnumConstants();
        for (final OVAEngineEventType enumeration : enums)
        {
            if (enumeration.value == value)
            {
                return enumeration;
            }
        }

        throw new IllegalArgumentException("Value [" + value + "] does not"
            + " correspond to any value of class: " + OVAEngineEventType.class);
    }
}
