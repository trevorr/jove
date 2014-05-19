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

import com.newisys.verilog.DriveDelayMode;
import com.newisys.verilog.VerilogExpr;
import com.newisys.verilog.VerilogModule;
import com.newisys.verilog.VerilogPort;
import com.newisys.verilog.VerilogSchedEvent;
import com.newisys.verilog.VerilogScope;
import com.newisys.verilog.VerilogTime;
import com.newisys.verilog.VerilogVarArraySelect;
import com.newisys.verilog.VerilogVariable;

/**
 * PLI implementation of VerilogVariable.
 * 
 * @author Trevor Robinson
 */
public final class PLIVerilogVariable
    extends PLIVerilogAbsVar
    implements VerilogVariable
{
    public PLIVerilogVariable(
        PLIInterface pliIntf,
        PLIObjectType type,
        long handle)
    {
        super(pliIntf, type, handle);
    }

    public final boolean isArray()
    {
        return pliIntf.getPropBool(PLIPropertyTypeConstants.vpiArray, handle);
    }

    public final VerilogVarArraySelect getElement(int index)
    {
        return (VerilogVarArraySelect) getChildObject(index);
    }

    public final Iterator<VerilogVarArraySelect> getElements()
    {
        return pliIntf.iterate(PLIObjectTypeConstants.vpiVarSelect, handle);
    }

    public final VerilogExpr getLeftRange()
    {
        return (VerilogExpr) getRelatedObject(PLIRelationTypeConstants.vpiLeftRange);
    }

    public final VerilogExpr getRightRange()
    {
        return (VerilogExpr) getRelatedObject(PLIRelationTypeConstants.vpiRightRange);
    }

    public final VerilogModule getModule()
    {
        return (VerilogModule) getRelatedObject(PLIObjectTypeConstants.vpiModule);
    }

    public final VerilogScope getScope()
    {
        return (VerilogScope) getRelatedObject(PLIRelationTypeConstants.vpiScope);
    }

    public final Iterator<VerilogPort> getPorts()
    {
        return pliIntf.iterate(PLIRelationTypeConstants.vpiPorts, handle);
    }

    public final Iterator<VerilogPort> getPortInsts()
    {
        return pliIntf.iterate(PLIRelationTypeConstants.vpiPortInst, handle);
    }

    public final void putValue(Object value)
    {
        pliIntf.putValue(handle, value);
    }

    public final void putValueDelay(
        Object value,
        VerilogTime delay,
        DriveDelayMode mode)
    {
        pliIntf.putValueDelay(handle, value, delay, PLIDriveDelayMode
            .getDriveDelayMode(mode));
    }

    public final VerilogSchedEvent putValueDelayNotify(
        Object value,
        VerilogTime delay,
        DriveDelayMode mode)
    {
        return pliIntf.putValueDelayNotify(handle, value, delay,
            PLIDriveDelayMode.getDriveDelayMode(mode));
    }

    public final void forceValue(Object value)
    {
        pliIntf.forceValue(handle, value);
    }

    public final Object releaseForce()
    {
        return pliIntf.releaseForce(handle);
    }
}
