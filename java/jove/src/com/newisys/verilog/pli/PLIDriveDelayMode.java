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

package com.newisys.verilog.pli;

import static com.newisys.verilog.pli.PLIDriveDelayModeConstants.vpiInertialDelay;
import static com.newisys.verilog.pli.PLIDriveDelayModeConstants.vpiNoDelay;
import static com.newisys.verilog.pli.PLIDriveDelayModeConstants.vpiPureTransportDelay;
import static com.newisys.verilog.pli.PLIDriveDelayModeConstants.vpiTransportDelay;

import com.newisys.verilog.DriveDelayMode;

/**
 * PLI version of the DriveDelayMode enumeration.
 * 
 * @author Trevor Robinson
 */
public enum PLIDriveDelayMode implements PLIVerilogEnum<DriveDelayMode>
{
    NO_DELAY(DriveDelayMode.NO_DELAY, vpiNoDelay),
    INERTIAL_DELAY(DriveDelayMode.INERTIAL_DELAY, vpiInertialDelay),
    TRANSPORT_DELAY(DriveDelayMode.TRANSPORT_DELAY, vpiTransportDelay),
    PURE_TRANSPORT_DELAY(DriveDelayMode.PURE_TRANSPORT_DELAY, vpiPureTransportDelay);

    private final DriveDelayMode baseEnum;
    private final int value;

    PLIDriveDelayMode(DriveDelayMode baseEnum, int value)
    {
        this.baseEnum = baseEnum;
        this.value = value;
    }

    public DriveDelayMode getVerilogEnum()
    {
        return baseEnum;
    }

    public int getValue()
    {
        return value;
    }

    public static PLIDriveDelayMode getDriveDelayMode(DriveDelayMode baseEnum)
    {
        for (PLIDriveDelayMode v : values())
        {
            if (v.getVerilogEnum() == baseEnum) return v;
        }
        throw new IllegalArgumentException("Enum object mapping not found: "
            + baseEnum);
    }

    public static PLIDriveDelayMode getDriveDelayMode(int value)
    {
        for (PLIDriveDelayMode v : values())
        {
            if (v.getValue() == value) return v;
        }
        throw new IllegalArgumentException("Enum value mapping not found: "
            + value);
    }
}
