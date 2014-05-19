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

import com.newisys.verilog.VerilogExpr;
import com.newisys.verilog.VerilogModule;
import com.newisys.verilog.VerilogNet;
import com.newisys.verilog.VerilogNetBit;

/**
 * PLI implementation of VerilogNet.
 * 
 * @author Trevor Robinson
 */
public final class PLIVerilogNet
    extends PLIVerilogNets
    implements VerilogNet
{
    public PLIVerilogNet(PLIInterface pliIntf, long handle)
    {
        super(pliIntf, PLIObjectType.NET, handle);
    }

    public final VerilogNetBit getBit(int index)
    {
        return (VerilogNetBit) getChildObject(index);
    }

    public final Iterator<VerilogNetBit> getBits()
    {
        return pliIntf.iterate(PLIRelationTypeConstants.vpiBit, handle);
    }

    public final VerilogModule getModule()
    {
        return (VerilogModule) getRelatedObject(PLIObjectTypeConstants.vpiModule);
    }

    public final VerilogExpr getDelay()
    {
        return (VerilogExpr) getRelatedObject(PLIRelationTypeConstants.vpiDelay);
    }
}
