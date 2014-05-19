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

package com.newisys.dv.ifgen.schemaprinter;

import com.newisys.dv.ifgen.schema.*;

/**
 * Printer module used to print signal references. Instances of this module
 * are created on the fly, since they depend on the package context and
 * default interface.
 * 
 * @author Trevor Robinson
 */
class SignalRefPrinter
    extends IfgenSchemaPrinterModule
    implements IfgenSignalRefVisitor
{
    private IfgenPackage pkgContext;
    private IfgenInterface defIntf;

    public SignalRefPrinter(
        BasePrinter basePrinter,
        IfgenPackage pkgContext,
        IfgenInterface defIntf)
    {
        super(basePrinter);
        this.pkgContext = pkgContext;
        this.defIntf = defIntf;
    }

    public void visit(IfgenConcatSignalRef obj)
    {
        fmt.printToken("{");
        fmt.printSpace();
        boolean first = true;
        for (IfgenSignalRef member : obj.getMembers())
        {
            if (!first)
            {
                fmt.printTrailingToken(",");
                fmt.printSpace();
            }
            member.accept(this);
            first = false;
        }
        fmt.printSpace();
        fmt.printToken("}");
    }

    public void visit(IfgenHDLSignalRef obj)
    {
        IfgenExpression hdl = obj.getHDL();
        assert (hdl instanceof IfgenStringLiteral);

        IfgenUnresolvedName qname = IfgenUnresolvedName
            .parse(((IfgenStringLiteral) hdl).getString());
        basePrinter.printQname(qname);
    }

    public void visit(IfgenInterfaceSignalRef obj)
    {
        IfgenInterfaceSignal signal = obj.getSignal();
        IfgenInterface intf = signal.getInterface();
        if (intf != defIntf)
        {
            basePrinter.printName(intf.getName(), pkgContext);
            fmt.printLeadingToken(".");
        }
        basePrinter.printID(signal.getName());
    }

    public void visit(IfgenSliceSignalRef obj)
    {
        obj.getSignal().accept(this);
        basePrinter.printRange(obj.getFromIndex(), obj.getToIndex());
    }
}
