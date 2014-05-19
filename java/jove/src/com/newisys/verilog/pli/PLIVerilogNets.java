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

import com.newisys.verilog.*;

/**
 * PLI implementation of VerilogNets.
 * 
 * @author Trevor Robinson
 */
public abstract class PLIVerilogNets
    extends PLIVerilogAbsVar
    implements VerilogNets
{
    public PLIVerilogNets(PLIInterface pliIntf, PLIObjectType type, long handle)
    {
        super(pliIntf, type, handle);
    }

    public final NetType getNetType()
    {
        return PLINetType.getNetType(
            pliIntf.getPropInt(PLIPropertyTypeConstants.vpiNetType, handle))
            .getVerilogEnum();
    }

    public final boolean isScalar()
    {
        return pliIntf.getPropBool(PLIPropertyTypeConstants.vpiScalar, handle);
    }

    public final boolean isVector()
    {
        return pliIntf.getPropBool(PLIPropertyTypeConstants.vpiVector, handle);
    }

    public final boolean isExplicitScalared()
    {
        return pliIntf.getPropBool(
            PLIPropertyTypeConstants.vpiExplicitScalared, handle);
    }

    public final boolean isExplicitVectored()
    {
        return pliIntf.getPropBool(
            PLIPropertyTypeConstants.vpiExplicitVectored, handle);
    }

    public final boolean isExpanded()
    {
        return pliIntf
            .getPropBool(PLIPropertyTypeConstants.vpiExpanded, handle);
    }

    public final boolean isNetDeclAssign()
    {
        return pliIntf.getPropBool(PLIPropertyTypeConstants.vpiNetDeclAssign,
            handle);
    }

    public final boolean isImplicitDecl()
    {
        return pliIntf.getPropBool(PLIPropertyTypeConstants.vpiImplicitDecl,
            handle);
    }

    public final int getStrength0()
    {
        return pliIntf
            .getPropInt(PLIPropertyTypeConstants.vpiStrength0, handle);
    }

    public final int getStrength1()
    {
        return pliIntf
            .getPropInt(PLIPropertyTypeConstants.vpiStrength1, handle);
    }

    public final int getChargeStrength()
    {
        return pliIntf.getPropInt(PLIPropertyTypeConstants.vpiChargeStrength,
            handle);
    }

    public final VerilogExpr getLeftRange()
    {
        return (VerilogExpr) getRelatedObject(PLIRelationTypeConstants.vpiLeftRange);
    }

    public final VerilogExpr getRightRange()
    {
        return (VerilogExpr) getRelatedObject(PLIRelationTypeConstants.vpiRightRange);
    }

    public final Iterator<VerilogPort> getPorts()
    {
        return pliIntf.iterate(PLIRelationTypeConstants.vpiPorts, handle);
    }

    public final Iterator<VerilogPort> getPortInsts()
    {
        return pliIntf.iterate(PLIRelationTypeConstants.vpiPortInst, handle);
    }

    public final Iterator<VerilogContAssign> getContAssigns()
    {
        return pliIntf.iterate(PLIObjectTypeConstants.vpiContAssign, handle);
    }

    public final Iterator<VerilogPrimTerm> getPrimTerms()
    {
        return pliIntf.iterate(PLIObjectTypeConstants.vpiPrimTerm, handle);
    }

    public final Iterator<VerilogPathTerm> getPathTerms()
    {
        return pliIntf.iterate(PLIObjectTypeConstants.vpiPathTerm, handle);
    }

    public final Iterator<VerilogTchkTerm> getTchkTerms()
    {
        return pliIntf.iterate(PLIObjectTypeConstants.vpiTchkTerm, handle);
    }

    public final Iterator<VerilogNetDriver> getDrivers()
    {
        return pliIntf.iterate(PLIRelationTypeConstants.vpiDriver, handle);
    }

    public final Iterator<VerilogNetLoad> getLoads()
    {
        return pliIntf.iterate(PLIRelationTypeConstants.vpiLoad, handle);
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

    public final PLIVerilogSchedEvent putValueDelayNotify(
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
