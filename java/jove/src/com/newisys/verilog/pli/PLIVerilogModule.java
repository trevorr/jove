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
 * PLI implementation of VerilogModule.
 * 
 * @author Trevor Robinson
 */
public final class PLIVerilogModule
    extends PLIVerilogScope
    implements VerilogModule
{
    public PLIVerilogModule(PLIInterface pliIntf, long handle)
    {
        super(pliIntf, PLIObjectType.MODULE, handle);
    }

    public final boolean isTopModule()
    {
        return pliIntf.getPropBool(PLIPropertyTypeConstants.vpiTopModule,
            handle);
    }

    public final boolean isCellInstance()
    {
        return pliIntf.getPropBool(PLIPropertyTypeConstants.vpiCellInstance,
            handle);
    }

    public final boolean isProtected()
    {
        return pliIntf.getPropBool(PLIPropertyTypeConstants.vpiProtected,
            handle);
    }

    public final int getTimeUnit()
    {
        return pliIntf.getPropInt(PLIPropertyTypeConstants.vpiTimeUnit, handle);
    }

    public final int getTimePrecision()
    {
        return pliIntf.getPropInt(PLIPropertyTypeConstants.vpiTimePrecision,
            handle);
    }

    public final NetType getDefNetType()
    {
        return PLINetType.getNetType(
            pliIntf.getPropInt(PLIPropertyTypeConstants.vpiDefNetType, handle))
            .getVerilogEnum();
    }

    public final UnconnDriveType getUnconnDrive()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public final ModuleDelayMode getDefDelayMode()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public final int getDefDecayTime()
    {
        return pliIntf.getPropInt(PLIPropertyTypeConstants.vpiDefDecayTime,
            handle);
    }

    public final String getDefName()
    {
        return pliIntf.getPropStr(PLIPropertyTypeConstants.vpiDefName, handle);
    }

    public final String getFile()
    {
        return pliIntf.getPropStr(PLIPropertyTypeConstants.vpiFile, handle);
    }

    public final int getLineNo()
    {
        return pliIntf.getPropInt(PLIPropertyTypeConstants.vpiLineNo, handle);
    }

    public final String getDefFile()
    {
        return pliIntf.getPropStr(PLIPropertyTypeConstants.vpiDefFile, handle);
    }

    public final int getDefLineNo()
    {
        return pliIntf
            .getPropInt(PLIPropertyTypeConstants.vpiDefLineNo, handle);
    }

    public final Iterator<VerilogPort> getPorts()
    {
        return pliIntf.iterate(PLIObjectTypeConstants.vpiPort, handle);
    }

    public final Iterator<VerilogIODecl> getIODecls()
    {
        return pliIntf.iterate(PLIObjectTypeConstants.vpiIODecl, handle);
    }

    public final Iterator<VerilogNet> getNets()
    {
        return pliIntf.iterate(PLIObjectTypeConstants.vpiNet, handle);
    }

    public final Iterator<VerilogParameter> getParameters()
    {
        return pliIntf.iterate(PLIObjectTypeConstants.vpiParameter, handle);
    }

    public final Iterator<VerilogSpecParam> getSpecParams()
    {
        return pliIntf.iterate(PLIObjectTypeConstants.vpiSpecParam, handle);
    }

    public final Iterator<VerilogParamAssign> getParamAssigns()
    {
        return pliIntf.iterate(PLIObjectTypeConstants.vpiParamAssign, handle);
    }

    public final Iterator<VerilogPrimitive> getPrimitives()
    {
        return pliIntf.iterate(PLIRelationTypeConstants.vpiPrimitive, handle);
    }

    public final Iterator<VerilogModule> getModules()
    {
        return pliIntf.iterate(PLIObjectTypeConstants.vpiModule, handle);
    }

    public final Iterator<VerilogContAssign> getContAssigns()
    {
        return pliIntf.iterate(PLIObjectTypeConstants.vpiContAssign, handle);
    }

    public final Iterator<VerilogProcedure> getProcesses()
    {
        return pliIntf.iterate(PLIRelationTypeConstants.vpiProcess, handle);
    }

    public final Iterator<VerilogModPath> getModPaths()
    {
        return pliIntf.iterate(PLIObjectTypeConstants.vpiModPath, handle);
    }

    public final Iterator<VerilogTchk> getTchks()
    {
        return pliIntf.iterate(PLIObjectTypeConstants.vpiTchk, handle);
    }
}
