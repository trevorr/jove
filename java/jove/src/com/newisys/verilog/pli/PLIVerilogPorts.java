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

import com.newisys.verilog.DirectionType;
import com.newisys.verilog.VerilogExpr;
import com.newisys.verilog.VerilogModule;
import com.newisys.verilog.VerilogPorts;

/**
 * PLI implementation of VerilogPorts.
 * 
 * @author Trevor Robinson
 */
public abstract class PLIVerilogPorts
    extends PLIVerilogDeclObject
    implements VerilogPorts
{
    public PLIVerilogPorts(PLIInterface pliIntf, PLIObjectType type, long handle)
    {
        super(pliIntf, type, handle);
    }

    public final boolean isScalar()
    {
        return pliIntf.getPropBool(PLIPropertyTypeConstants.vpiScalar, handle);
    }

    public final boolean isVector()
    {
        return pliIntf.getPropBool(PLIPropertyTypeConstants.vpiVector, handle);
    }

    public final boolean isExplicitName()
    {
        return pliIntf.getPropBool(PLIPropertyTypeConstants.vpiExplicitName,
            handle);
    }

    public final boolean isConnByName()
    {
        return pliIntf.getPropBool(PLIPropertyTypeConstants.vpiConnByName,
            handle);
    }

    public final int getSize()
    {
        return pliIntf.getPropInt(PLIPropertyTypeConstants.vpiSize, handle);
    }

    public final DirectionType getDirection()
    {
        return PLIDirectionType.getDirectionType(
            pliIntf.getPropInt(PLIPropertyTypeConstants.vpiDirection, handle))
            .getVerilogEnum();
    }

    public final VerilogModule getModule()
    {
        return (VerilogModule) getRelatedObject(PLIObjectTypeConstants.vpiModule);
    }

    public final VerilogExpr getHighConn()
    {
        return (VerilogExpr) getRelatedObject(PLIRelationTypeConstants.vpiHighConn);
    }

    public final VerilogExpr getLowConn()
    {
        return (VerilogExpr) getRelatedObject(PLIRelationTypeConstants.vpiLowConn);
    }
}
