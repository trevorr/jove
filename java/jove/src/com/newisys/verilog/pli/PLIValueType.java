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

import static com.newisys.verilog.pli.PLIValueTypeConstants.*;

import com.newisys.verilog.ValueType;

/**
 * PLI version of the ValueType enumeration.
 * 
 * @author Trevor Robinson
 */
public enum PLIValueType implements PLIVerilogEnum<ValueType>
{
    BIN_STR(ValueType.BIN_STR, vpiBinStr),
    OCT_STR(ValueType.OCT_STR, vpiOctStr),
    DEC_STR(ValueType.DEC_STR, vpiDecStr),
    HEX_STR(ValueType.HEX_STR, vpiHexStr),
    SCALAR(ValueType.SCALAR, vpiScalar),
    INT(ValueType.INT, vpiInt),
    REAL(ValueType.REAL, vpiReal),
    STRING(ValueType.STRING, vpiString),
    VECTOR(ValueType.VECTOR, vpiVector),
    STRENGTH(ValueType.STRENGTH, vpiStrength),
    TIME(ValueType.TIME, vpiTime),
    OBJ_TYPE(ValueType.OBJ_TYPE, vpiObjType),
    SUPPRESS(ValueType.SUPPRESS, vpiSuppress);

    private final ValueType baseEnum;
    private final int value;

    PLIValueType(ValueType baseEnum, int value)
    {
        this.baseEnum = baseEnum;
        this.value = value;
    }

    public ValueType getVerilogEnum()
    {
        return baseEnum;
    }

    public int getValue()
    {
        return value;
    }

    public static PLIValueType getValueType(ValueType baseEnum)
    {
        for (PLIValueType v : values())
        {
            if (v.getVerilogEnum() == baseEnum) return v;
        }
        throw new IllegalArgumentException("Enum object mapping not found: "
            + baseEnum);
    }

    public static PLIValueType getValueType(int value)
    {
        for (PLIValueType v : values())
        {
            if (v.getValue() == value) return v;
        }
        throw new IllegalArgumentException("Enum value mapping not found: "
            + value);
    }
}
