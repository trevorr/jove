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

import static com.newisys.verilog.pli.PLINetTypeConstants.*;

import com.newisys.verilog.NetType;

/**
 * PLI version of the NetType enumeration.
 * 
 * @author Trevor Robinson
 */
public enum PLINetType implements PLIVerilogEnum<NetType>
{
    WIRE(NetType.WIRE, vpiWire),
    WAND(NetType.WAND, vpiWand),
    WOR(NetType.WOR, vpiWor),
    TRI(NetType.TRI, vpiTri),
    TRI_0(NetType.TRI_0, vpiTri0),
    TRI_1(NetType.TRI_1, vpiTri1),
    TRI_REG(NetType.TRI_REG, vpiTriReg),
    TRI_AND(NetType.TRI_AND, vpiTriAnd),
    TRI_OR(NetType.TRI_OR, vpiTriOr),
    SUPPLY_1(NetType.SUPPLY_1, vpiSupply1),
    SUPPLY_0(NetType.SUPPLY_0, vpiSupply0),
    NONE(NetType.NONE, vpiNone);

    private final NetType baseEnum;
    private final int value;

    PLINetType(NetType baseEnum, int value)
    {
        this.baseEnum = baseEnum;
        this.value = value;
    }

    public NetType getVerilogEnum()
    {
        return baseEnum;
    }

    public int getValue()
    {
        return value;
    }

    public static PLINetType getNetType(NetType baseEnum)
    {
        for (PLINetType v : values())
        {
            if (v.getVerilogEnum() == baseEnum) return v;
        }
        throw new IllegalArgumentException("Enum object mapping not found: "
            + baseEnum);
    }

    public static PLINetType getNetType(int value)
    {
        for (PLINetType v : values())
        {
            if (v.getValue() == value) return v;
        }
        throw new IllegalArgumentException("Enum value mapping not found: "
            + value);
    }
}
