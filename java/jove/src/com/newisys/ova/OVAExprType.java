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
 * Enumeration of OVA expression types.
 * 
 * @author Jon Nall
 */
public enum OVAExprType
{
    /**
     * TODO: document OVAExprType.OVA
     */
    OVA(0),

    /**
     * The assertion is an ova_check_* expression.
     */
    Check(1),

    /**
     * The assertion is an ova_forbid_* expression.
     */
    Forbid(2),

    /**
     * The assertion is an ova_cover_* expression.
     */
    Cover(3),

    /**
     * TODO: document OVAExprType.NotCover.
     */
    NotCover(4);

    private int value;

    private OVAExprType(int value)
    {
        this.value = value;
    }

    /**
     * Returns the integer value associated with this OVAExprType.
     *
     * @return the integer value for this enumeration
     */
    public int getValue()
    {
        return value;
    }

    /**
     * Returns the OVAExprType for the given integer value.
     *
     * @param value the value for which to return an OVAExprType
     * @return the OVAExprType corresponding to <code>value</code>
     * @throws IllegalArgumentException if <code>value</code> does not
     *      correspond to any OVAExprType
     */
    public static OVAExprType forValue(int value)
    {
        OVAExprType[] enums = OVAExprType.class.getEnumConstants();
        for (final OVAExprType enumeration : enums)
        {
            if (enumeration.value == value)
            {
                return enumeration;
            }
        }

        throw new IllegalArgumentException("Value [" + value + "] does not"
            + " correspond to any value of class: " + OVAExprType.class);
    }
}
