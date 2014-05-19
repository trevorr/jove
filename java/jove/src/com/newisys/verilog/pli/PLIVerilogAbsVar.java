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

import java.util.Iterator;

import com.newisys.verilog.VerilogAbsVar;
import com.newisys.verilog.VerilogDeclObject;

/**
 * PLI implementation of VerilogAbsVar.
 * 
 * @author Trevor Robinson
 */
public abstract class PLIVerilogAbsVar
    extends PLIVerilogExpr
    implements VerilogAbsVar
{
    public PLIVerilogAbsVar(
        PLIInterface pliIntf,
        PLIObjectType type,
        long handle)
    {
        super(pliIntf, type, handle);
    }

    public final String getName()
    {
        return pliIntf.getPropStr(PLIPropertyTypeConstants.vpiName, handle);
    }

    public final String getFullName()
    {
        return pliIntf.getPropStr(PLIPropertyTypeConstants.vpiFullName, handle);
    }

    public final Iterator<VerilogDeclObject> getUses()
    {
        return pliIntf.iterate(PLIRelationTypeConstants.vpiUse, handle);
    }
}
