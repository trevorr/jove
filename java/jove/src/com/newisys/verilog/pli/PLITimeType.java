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

import static com.newisys.verilog.pli.PLITimeTypeConstants.vpiScaledReal;
import static com.newisys.verilog.pli.PLITimeTypeConstants.vpiSim;
import static com.newisys.verilog.pli.PLITimeTypeConstants.vpiSuppress;

import com.newisys.verilog.TimeType;

/**
 * PLI version of the TimeType enumeration.
 * 
 * @author Trevor Robinson
 */
public enum PLITimeType implements PLIVerilogEnum<TimeType>
{
    SCALED_REAL(TimeType.SCALED_REAL, vpiScaledReal),
    SIM(TimeType.SIM, vpiSim),
    SUPPRESS(TimeType.SUPPRESS, vpiSuppress);

    private final TimeType baseEnum;
    private final int value;

    PLITimeType(TimeType baseEnum, int value)
    {
        this.baseEnum = baseEnum;
        this.value = value;
    }

    public TimeType getVerilogEnum()
    {
        return baseEnum;
    }

    public int getValue()
    {
        return value;
    }

    public static PLITimeType getTimeType(TimeType baseEnum)
    {
        for (PLITimeType v : values())
        {
            if (v.getVerilogEnum() == baseEnum) return v;
        }
        throw new IllegalArgumentException("Enum object mapping not found: "
            + baseEnum);
    }

    public static PLITimeType getTimeType(int value)
    {
        for (PLITimeType v : values())
        {
            if (v.getValue() == value) return v;
        }
        throw new IllegalArgumentException("Enum value mapping not found: "
            + value);
    }
}
