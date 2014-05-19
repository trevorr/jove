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

import java.util.Iterator;

import com.newisys.dv.ifgen.schema.*;

/**
 * Printer module used to print all first-class schema objects (i.e. those
 * appearing in IfgenSchemaObjectVisitor).
 * 
 * @author Trevor Robinson
 */
class SchemaObjectPrinter
    extends IfgenSchemaPrinterModule
    implements IfgenSchemaObjectVisitor
{
    private IfgenPackage pkgContext;
    private IfgenInterface defIntf;
    private IfgenSampleDef defSample;
    private IfgenDriveDef defDrive;

    public SchemaObjectPrinter(BasePrinter basePrinter)
    {
        super(basePrinter);
    }

    public void visit(IfgenBind obj)
    {
        final IfgenPackage savePkgContext = pkgContext;
        pkgContext = obj.getPackage();

        basePrinter.printComments(obj, true);
        fmt.printToken("bind");
        fmt.printSpace();
        basePrinter.printID(obj.getName());
        fmt.printSpace();
        if (obj.hasParameters())
        {
            fmt.printToken("<");
            Iterator<IfgenVariableDecl> iter = obj.getParameters().iterator();
            while (iter.hasNext())
            {
                visit(iter.next());
                if (iter.hasNext())
                {
                    fmt.printToken(",");
                    fmt.printSpace();
                }
            }
            fmt.printToken(">");
            fmt.printSpace();
        }

        fmt.printToken("is");
        fmt.printSpace();
        basePrinter.printName(obj.getPort().getName(), pkgContext);
        printNewLine();

        fmt.printToken("{");
        printNewLine();
        fmt.incIndent();

        for (IfgenBindMember member : obj.getMembers())
        {
            member.accept(this);
        }
        defIntf = null;

        fmt.decIndent();
        fmt.printToken("}");
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();

        pkgContext = savePkgContext;
    }

    public void visit(IfgenBindSignal obj)
    {
        basePrinter.printComments(obj, true);
        basePrinter.printID(obj.getPortSignal().getName());
        fmt.printSpace();
        obj.getIntfSignal().accept(
            new SignalRefPrinter(basePrinter, pkgContext, defIntf));
        fmt.printTrailingToken(";");
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    public void visit(IfgenDriveDef obj)
    {
        printEdgeDef(obj, "drive", true);
        defDrive = obj;
    }

    private void printEdgeDef(IfgenEdgeDef obj, String kind, boolean def)
    {
        basePrinter.printComments(obj, true);
        if (def)
        {
            fmt.printToken("default");
            fmt.printSpace();
        }
        fmt.printToken(kind);
        fmt.printLeadingToken("(");
        basePrinter.printEdge(obj.getEdge());
        fmt.printTrailingToken(",");
        fmt.printSpace();
        fmt.printToken(String.valueOf(obj.getSkew()));
        fmt.printTrailingToken(")");
        if (def) fmt.printTrailingToken(";");
        basePrinter.printComments(obj, false);
        if (def && !fmt.isNewLine()) printNewLine();
    }

    public void visit(IfgenHDLTask obj)
    {
        basePrinter.printComments(obj, true);
        fmt.printToken("hdl_task");
        fmt.printSpace();
        basePrinter.printID(obj.getName());
        if (obj.hasParameters())
        {
            fmt.printToken("<");
            Iterator<IfgenVariableDecl> iter = obj.getParameters().iterator();
            while (iter.hasNext())
            {
                visit(iter.next());
                if (iter.hasNext())
                {
                    fmt.printToken(",");
                    fmt.printSpace();
                }
            }
            fmt.printToken(">");
            fmt.printSpace();
        }
        printTaskArgs(obj);
        fmt.printSpace();
        basePrinter.printQname(obj.getInstancePath().getName());
        fmt.printTrailingToken(";");
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    public void visit(IfgenHVLTask obj)
    {
        basePrinter.printComments(obj, true);
        fmt.printToken("hvl_task");
        fmt.printSpace();
        basePrinter.printID(obj.getName());
        printTaskArgs(obj);
        fmt.printTrailingToken(";");
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    private void printTaskArgs(IfgenTask obj)
    {
        fmt.printLeadingToken("(");
        boolean first = true;
        for (IfgenTaskArg arg : obj.getArguments())
        {
            if (!first)
            {
                fmt.printTrailingToken(",");
                fmt.printSpace();
            }
            visit(arg);
            first = false;
        }
        fmt.printTrailingToken(")");
    }

    private void printSetExpression(IfgenExpression expr)
    {
        printExpression(expr, true);
    }

    private void printExpression(IfgenExpression expr, boolean allowSets)
    {
        if (expr instanceof IfgenIntegerLiteral)
        {
            visit((IfgenIntegerLiteral) expr);
        }
        else if (expr instanceof IfgenStringLiteral)
        {
            visit((IfgenStringLiteral) expr);
        }
        else if (expr instanceof IfgenEnumLiteral)
        {
            visit((IfgenEnumLiteral) expr);
        }
        else if (expr instanceof IfgenComplexVariableRef)
        {
            visit((IfgenComplexVariableRef) expr);
        }
        else if (expr instanceof IfgenVariableRef)
        {
            visit((IfgenVariableRef) expr);
        }
        else
        {
            if (!allowSets)
            {
                throw new AssertionError(
                    "Sets are not allowed in this context: " + expr);
            }
            assert (expr instanceof IfgenSetLiteral);
            visit((IfgenSetLiteral) expr);
        }
    }

    public void visit(IfgenInterface obj)
    {
        final IfgenPackage savePkgContext = pkgContext;
        pkgContext = obj.getPackage();

        basePrinter.printComments(obj, true);
        fmt.printToken("interface");
        fmt.printSpace();
        basePrinter.printID(obj.getName());
        if (obj.hasParameters())
        {
            fmt.printToken("<");
            Iterator<IfgenVariableDecl> iter = obj.getParameters().iterator();
            while (iter.hasNext())
            {
                visit(iter.next());
                if (iter.hasNext())
                {
                    fmt.printToken(",");
                    fmt.printSpace();
                }
            }
            fmt.printToken(">");
            fmt.printSpace();
        }

        printNewLine();

        fmt.printToken("{");
        printNewLine();
        fmt.incIndent();

        for (IfgenInterfaceMember member : obj.getMembers())
        {
            member.accept(this);
        }
        defSample = null;
        defDrive = null;

        fmt.decIndent();
        fmt.printToken("}");
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();

        pkgContext = savePkgContext;
    }

    public void visit(IfgenInterfaceDef obj)
    {
        IfgenInterface intf = obj.getIntf();
        defIntf = intf;

        fmt.printToken("default");
        fmt.printSpace();
        fmt.printToken("interface");
        fmt.printSpace();
        basePrinter.printName(intf.getName(), pkgContext);
        fmt.printTrailingToken(";");
        printNewLine();
    }

    public void visit(IfgenInterfaceSignal obj)
    {
        basePrinter.printComments(obj, true);
        basePrinter.printSignalType(obj.getType());
        basePrinter.printVector(obj.getSize());
        fmt.printSpace();
        basePrinter.printID(obj.getName());
        IfgenSampleDef sample = obj.getSample();
        if (sample != null && !sample.equals(defSample))
        {
            fmt.printSpace();
            printEdgeDef(sample, "sample", false);
        }
        IfgenDriveDef drive = obj.getDrive();
        if (drive != null && !drive.equals(defDrive))
        {
            fmt.printSpace();
            printEdgeDef(drive, "drive", false);
        }
        int depth = obj.getSampleDepth();
        if (depth > 1)
        {
            fmt.printSpace();
            fmt.printToken("depth");
            fmt.printSpace();
            fmt.printToken(String.valueOf(depth));
        }
        IfgenModuleDef module = obj.getModule();
        if (module != null)
        {
            fmt.printSpace();
            printModuleDef(module, false);
        }
        else
        {
            IfgenSignalRef node = obj.getHDLNode();
            if (node != null)
            {
                fmt.printSpace();
                fmt.printToken("hdl_node");
                fmt.printSpace();
                node.accept(new SignalRefPrinter(basePrinter, null, null));
            }
        }
        fmt.printTrailingToken(";");
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    public void visit(IfgenModuleDef obj)
    {
        printModuleDef(obj, true);
    }

    private void printModuleDef(IfgenModuleDef obj, boolean def)
    {
        basePrinter.printComments(obj, true);
        if (def)
        {
            fmt.printToken("default");
            fmt.printSpace();
        }
        fmt.printToken("module");
        fmt.printSpace();
        basePrinter.printQname(obj.getName());
        if (def) fmt.printTrailingToken(";");
        basePrinter.printComments(obj, false);
        if (def && !fmt.isNewLine()) printNewLine();
    }

    public void visit(IfgenPackage obj)
    {
        for (IfgenPackageMember member : obj.getMembers())
        {
            member.accept((IfgenPackageMemberVisitor) this);
        }
    }

    public void visit(IfgenPort obj)
    {
        printPort(obj, true);
    }

    private void printPort(IfgenPort obj, boolean printReverse)
    {
        basePrinter.printComments(obj, true);
        fmt.printToken("port");
        fmt.printSpace();
        basePrinter.printID(obj.getName());
        IfgenPort revPort = obj.getReverseOf();
        if (printReverse && revPort != null)
        {
            fmt.printSpace();
            basePrinter.printID(revPort.getName());
        }
        printNewLine();
        fmt.printToken("{");
        printNewLine();
        fmt.incIndent();
        for (IfgenPortSignal member : obj.getMembers())
        {
            printPortSignal(member, revPort != null
                || member.getDirection() != IfgenDirection.INOUT);
        }
        fmt.decIndent();
        fmt.printToken("}");
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    public void visit(IfgenPortSignal obj)
    {
        printPortSignal(obj, true);
    }

    private void printPortSignal(IfgenPortSignal obj, boolean printDir)
    {
        basePrinter.printComments(obj, true);
        if (printDir)
        {
            basePrinter.printDirection(obj.getDirection());
            fmt.printSpace();
        }
        basePrinter.printID(obj.getName());
        fmt.printTrailingToken(";");
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    public void visit(IfgenSampleDef obj)
    {
        printEdgeDef(obj, "sample", true);
        defSample = obj;
    }

    public void visit(IfgenTaskArg obj)
    {
        basePrinter.printComments(obj, true);
        basePrinter.printDirection(obj.getDirection());
        final IfgenType type = obj.getType();
        if (type != obj.getSchema().BIT_TYPE)
        {
            fmt.printSpace();
            basePrinter.printType(obj.getSchema(), type);
        }
        basePrinter.printVector(obj.getSize());
        fmt.printSpace();
        basePrinter.printID(obj.getName());
        basePrinter.printComments(obj, false);
    }

    public void visit(IfgenEnum obj)
    {
        basePrinter.printComments(obj, true);
        fmt.printToken("enum");
        fmt.printSpace();
        basePrinter.printID(obj.getName());
        printNewLine();
        fmt.printToken("{");
        printNewLine();
        fmt.incIndent();

        Iterator<IfgenEnumElement> iter = obj.getElements().iterator();
        while (iter.hasNext())
        {
            final IfgenEnumElement e = iter.next();
            visit(e);
            if (iter.hasNext())
            {
                fmt.printToken(",");
                printNewLine();
            }
            else
            {
                fmt.printToken(";");
            }
        }

        fmt.decIndent();
        fmt.printToken("}");
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    public void visit(IfgenEnumElement obj)
    {
        basePrinter.printComments(obj, true);
        basePrinter.printID(obj.getName());
    }

    public void visit(IfgenEnumLiteral obj)
    {
        basePrinter.printName(obj.getElement().getName(), null);
    }

    public void visit(IfgenIntegerLiteral obj)
    {
        fmt.printToken(Integer.toString(obj.getValue()));
    }

    public void visit(IfgenStringLiteral obj)
    {
        fmt.printToken("\"");
        fmt.printToken(obj.getString());
        fmt.printToken("\"");
    }

    public void visit(IfgenVariableRef obj)
    {
        fmt.printToken("$");
        basePrinter.printName(obj.getName(), null);
    }

    public void visit(IfgenVariableDecl obj)
    {
        fmt.printToken(obj.getType().toString());
        fmt.printSpace();
        basePrinter.printName(obj.getName(), null);
    }

    public void visit(IfgenComplexVariableRef obj)
    {
        fmt.printToken("\"");
        for (IfgenExpression e : obj.getExpressions())
        {
            if (e instanceof IfgenStringLiteral)
            {
                fmt.printToken(((IfgenStringLiteral) e).getString());
            }
            else
            {
                assert (e instanceof IfgenVariableRef);
                visit((IfgenVariableRef) e);
            }
        }
        fmt.printToken("\"");
    }

    public void visit(IfgenSetLiteral obj)
    {
        fmt.printToken("[");
        Iterator<IfgenExpression> exprIter = obj.getSet().getExpressions()
            .iterator();
        Iterator<IfgenRange> rangeIter = obj.getSet().getRanges().iterator();
        while (exprIter.hasNext())
        {
            printSetExpression(exprIter.next());
            if (exprIter.hasNext() || rangeIter.hasNext())
            {
                fmt.printToken(",");
                fmt.printSpace();
            }
        }

        while (rangeIter.hasNext())
        {
            final IfgenRange range = rangeIter.next();
            printSetExpression(range.getFromExpr());
            fmt.printToken("..");
            printSetExpression(range.getToExpr());
            if (rangeIter.hasNext())
            {
                fmt.printToken(",");
                fmt.printSpace();
            }
        }
        fmt.printToken("]");
    }

    public void visit(IfgenSetOperator obj)
    {
        printSetExpression(obj.getLHS());
        fmt.printSpace();
        fmt.printToken(obj.getOperatorString());
        fmt.printSpace();
        printSetExpression(obj.getRHS());
    }

    public void visit(IfgenTestbench obj)
    {
        basePrinter.printComments(obj, true);
        fmt.printToken("testbench");
        fmt.printSpace();
        basePrinter.printName(obj.getName(), null);

        if (obj.hasParameters())
        {
            fmt.printToken("<");
            Iterator<IfgenVariableDecl> iter = obj.getParameters().iterator();
            while (iter.hasNext())
            {
                visit(iter.next());
                if (iter.hasNext())
                {
                    fmt.printToken(",");
                    fmt.printSpace();
                }
            }
            fmt.printToken(">");
            fmt.printSpace();
        }
        printNewLine();
        fmt.printToken("{");
        printNewLine();
        fmt.incIndent();

        for (final IfgenWildname wname : obj.getImports())
        {
            fmt.printToken("import");
            fmt.printSpace();
            basePrinter.printWildname(wname);
            fmt.printToken(";");
            printNewLine();
        }
        printNewLine();
        for (final IfgenTestbenchMember member : obj.getMembers())
        {
            visit(member);
        }

        fmt.decIndent();
        fmt.printToken("}");
    }

    public void visit(IfgenTestbenchMember obj)
    {
        if (obj instanceof IfgenTemplateInst)
        {
            IfgenTemplateInst inst = (IfgenTemplateInst) obj;
            switch (inst.getTemplateKind())
            {
            case INTERFACE:
                fmt.printToken("interface");
                break;
            case BIND:
                fmt.printToken("bind");
                break;
            case HDL_TASK:
                fmt.printToken("hdl_task");
                break;
            default:
                throw new UnsupportedOperationException(
                    "Unknown Template Kind: " + inst.getTemplateKind());
            }
            fmt.printSpace();

            basePrinter.printName(inst.getName(), null);
            fmt.printSpace();

            basePrinter.printName(inst.getTemplate().getName(), null);
            fmt.printToken("<");
            for (final IfgenExpression ref : inst.getArgs())
            {
                printExpression(ref, false);
            }
            fmt.printToken(">;");
            printNewLine();
        }
        else
        {
            assert (obj instanceof IfgenForStatement);
            IfgenForStatement forStmt = (IfgenForStatement) obj;

            fmt.printToken("for");
            fmt.printSpace();
            visit(forStmt.getVarDecl());
            fmt.printSpace();
            fmt.printToken("(");
            printSetExpression(forStmt.getSet());
            fmt.printToken(")");
            printNewLine();
            fmt.printToken("{");
            printNewLine();
            fmt.incIndent();
            for (final IfgenTestbenchMember member : forStmt.getMembers())
            {
                visit(member);
            }
            fmt.decIndent();
            fmt.printToken("}");
            printNewLine();
        }
    }
}
