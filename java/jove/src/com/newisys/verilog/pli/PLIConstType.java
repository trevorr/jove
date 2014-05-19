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

import static com.newisys.verilog.pli.PLIConstTypeConstants.vpiBinaryConst;
import static com.newisys.verilog.pli.PLIConstTypeConstants.vpiDecConst;
import static com.newisys.verilog.pli.PLIConstTypeConstants.vpiHexConst;
import static com.newisys.verilog.pli.PLIConstTypeConstants.vpiIntConst;
import static com.newisys.verilog.pli.PLIConstTypeConstants.vpiOctConst;
import static com.newisys.verilog.pli.PLIConstTypeConstants.vpiRealConst;
import static com.newisys.verilog.pli.PLIConstTypeConstants.vpiStringConst;

import com.newisys.verilog.ConstType;

/**
 * PLI version of the ConstType enumeration.
 * 
 * @author Trevor Robinson
 */
public enum PLIConstType implements PLIVerilogEnum<ConstType>
{
    DEC(ConstType.DEC, vpiDecConst),
    REAL(ConstType.REAL, vpiRealConst),
    BINARY(ConstType.BINARY, vpiBinaryConst),
    OCT(ConstType.OCT, vpiOctConst),
    HEX(ConstType.HEX, vpiHexConst),
    STRING(ConstType.STRING, vpiStringConst),
    INT(ConstType.INT, vpiIntConst);

    private final ConstType baseEnum;
    private final int value;

    PLIConstType(ConstType baseEnum, int value)
    {
        this.baseEnum = baseEnum;
        this.value = value;
    }

    public ConstType getVerilogEnum()
    {
        return baseEnum;
    }

    public int getValue()
    {
        return value;
    }

    public static PLIConstType getConstType(ConstType baseEnum)
    {
        for (PLIConstType v : values())
        {
            if (v.getVerilogEnum() == baseEnum) return v;
        }
        throw new IllegalArgumentException("Enum object mapping not found: "
            + baseEnum);
    }

    public static PLIConstType getConstType(int value)
    {
        for (PLIConstType v : values())
        {
            if (v.getValue() == value) return v;
        }
        throw new IllegalArgumentException("Enum value mapping not found: "
            + value);
    }
}
