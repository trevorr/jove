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

import com.newisys.verilog.TimeType;
import com.newisys.verilog.ValueType;
import com.newisys.verilog.VerilogCallback;
import com.newisys.verilog.VerilogCallbackHandler;
import com.newisys.verilog.VerilogExpr;

/**
 * PLI implementation of VerilogExpr.
 * 
 * @author Trevor Robinson
 */
public abstract class PLIVerilogExpr
    extends PLIVerilogDeclObject
    implements VerilogExpr
{
    public PLIVerilogExpr(PLIInterface pliIntf, PLIObjectType type, long handle)
    {
        super(pliIntf, type, handle);
    }

    public final int getSize()
    {
        return pliIntf.getPropInt(PLIPropertyTypeConstants.vpiSize, handle);
    }

    public final Object getValue()
    {
        return pliIntf.getValue(handle, PLIValueTypeConstants.vpiObjType);
    }

    public final Object getValue(ValueType type)
    {
        return pliIntf.getValue(handle, PLIValueType.getValueType(type)
            .getValue());
    }

    public final VerilogCallback addValueChangeCallback(
        VerilogCallbackHandler handler)
    {
        return addValueChangeCallbackImpl(handler);
    }

    public final VerilogCallback addValueChangeCallback(
        TimeType timeType,
        ValueType valueType,
        VerilogCallbackHandler handler)
    {
        return addValueChangeCallbackImpl(timeType, valueType, handler);
    }
}
