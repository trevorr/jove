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

import static com.newisys.verilog.pli.PLIDirectionTypeConstants.vpiInout;
import static com.newisys.verilog.pli.PLIDirectionTypeConstants.vpiInput;
import static com.newisys.verilog.pli.PLIDirectionTypeConstants.vpiMixedIO;
import static com.newisys.verilog.pli.PLIDirectionTypeConstants.vpiNoDirection;
import static com.newisys.verilog.pli.PLIDirectionTypeConstants.vpiOutput;
import static com.newisys.verilog.pli.PLIDirectionTypeConstants.vpiPassByRef;

import com.newisys.verilog.DirectionType;

/**
 * PLI version of the DirectionType enumeration.
 * 
 * @author Trevor Robinson
 */
public enum PLIDirectionType implements PLIVerilogEnum<DirectionType>
{
    INPUT(DirectionType.INPUT, vpiInput),
    OUTPUT(DirectionType.OUTPUT, vpiOutput),
    INOUT(DirectionType.INOUT, vpiInout),
    MIXED_IO(DirectionType.MIXED_IO, vpiMixedIO),
    NO_DIRECTION(DirectionType.NO_DIRECTION, vpiNoDirection),
    PASS_BY_REF(DirectionType.PASS_BY_REF, vpiPassByRef);

    private final DirectionType baseEnum;
    private final int value;

    PLIDirectionType(DirectionType baseEnum, int value)
    {
        this.baseEnum = baseEnum;
        this.value = value;
    }

    public DirectionType getVerilogEnum()
    {
        return baseEnum;
    }

    public int getValue()
    {
        return value;
    }

    public static PLIDirectionType getDirectionType(DirectionType baseEnum)
    {
        for (PLIDirectionType v : values())
        {
            if (v.getVerilogEnum() == baseEnum) return v;
        }
        throw new IllegalArgumentException("Enum object mapping not found: "
            + baseEnum);
    }

    public static PLIDirectionType getDirectionType(int value)
    {
        for (PLIDirectionType v : values())
        {
            if (v.getValue() == value) return v;
        }
        throw new IllegalArgumentException("Enum value mapping not found: "
            + value);
    }
}
