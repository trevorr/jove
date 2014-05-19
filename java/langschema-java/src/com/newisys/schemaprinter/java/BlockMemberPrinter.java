/*
 * LangSchema-Java - Programming Language Modeling Classes for Java (TM)
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

package com.newisys.schemaprinter.java;

import java.util.Collection;
import java.util.Iterator;

import com.newisys.langschema.LabeledStatement;
import com.newisys.langschema.java.*;

/**
 * Printer module used to print Java block members. Instances of this module
 * are created on the fly, since they depend on the type and package context.
 * 
 * @author Trevor Robinson
 */
public class BlockMemberPrinter
    extends JavaSchemaPrinterModule
    implements JavaBlockMemberVisitor
{
    private final JavaStructuredType typeContext;
    private final JavaPackage pkgContext;

    public BlockMemberPrinter(
        BasePrinter basePrinter,
        JavaStructuredType typeContext)
    {
        super(basePrinter);
        this.typeContext = typeContext;
        this.pkgContext = typeContext != null ? typeContext.getPackage() : null;
    }

    public void printMembers(Collection members)
    {
        Iterator iter = members.iterator();
        while (iter.hasNext())
        {
            JavaBlockMember member = (JavaBlockMember) iter.next();
            member.accept(this);
        }
    }

    private void printGuard(String token, JavaExpression expr)
    {
        fmt.printToken(token);
        fmt.printSpace();
        fmt.printLeadingToken("(");
        basePrinter.printExpression(expr, typeContext);
        fmt.printTrailingToken(")");
    }

    private void printNestedStmt(JavaStatement member)
    {
        if (member instanceof JavaBlock)
        {
            basePrinter.printBlock((JavaBlock) member, typeContext, true);
        }
        else
        {
            fmt.incIndent();
            member.accept(this);
            fmt.decIndent();
        }
    }

    public void visit(JavaClass obj)
    {
        basePrinter.printClass(obj);
        printNewLine();
    }

    public void visit(JavaLocalVariable obj)
    {
        basePrinter.printComments(obj, true, true);
        basePrinter.printAnnotations(obj, pkgContext, false);
        basePrinter.printVarDecl(obj, pkgContext);
        basePrinter.printVarInit(obj, typeContext);
        fmt.printTrailingToken(";");
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    public void visit(JavaAssertStatement obj)
    {
        basePrinter.printComments(obj, true, true);
        fmt.printToken("assert");
        fmt.printSpace();
        JavaExpression testExpr = obj.getTestExpression();
        basePrinter.printExpression(testExpr, typeContext);

        JavaExpression msgExpr = obj.getMessageExpression();
        if (msgExpr != null)
        {
            fmt.printSpace();
            fmt.printToken(":");
            fmt.printSpace();
            basePrinter.printExpression(msgExpr, typeContext);
        }

        fmt.printTrailingToken(";");
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    public void visit(JavaBlock obj)
    {
        basePrinter.printBlock(obj, typeContext, true);
    }

    public void visit(JavaBreakStatement obj)
    {
        basePrinter.printComments(obj, true, true);
        fmt.printToken("break");
        LabeledStatement target = obj.getTarget();
        if (target != null)
        {
            fmt.printSpace();
            basePrinter.printName(target.getName(), pkgContext);
        }
        fmt.printTrailingToken(";");
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    public void visit(JavaContinueStatement obj)
    {
        basePrinter.printComments(obj, true, true);
        fmt.printToken("continue");
        LabeledStatement target = obj.getTarget();
        if (target != null)
        {
            fmt.printSpace();
            basePrinter.printName(target.getName(), pkgContext);
        }
        fmt.printTrailingToken(";");
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    public void visit(JavaDoWhileStatement obj)
    {
        basePrinter.printComments(obj, true, true);
        fmt.printToken("do");
        printNewLine();

        printNestedStmt((JavaStatement) obj.getStatement());

        printGuard("while", (JavaExpression) obj.getCondition());
        fmt.printTrailingToken(";");
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    public void visit(JavaExpressionStatement obj)
    {
        basePrinter.printComments(obj, true, true);
        basePrinter.printExpression(obj.getExpression(), typeContext);
        fmt.printTrailingToken(";");
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    public void visit(JavaForStatement obj)
    {
        basePrinter.printComments(obj, true, true);
        fmt.printToken("for");
        fmt.printSpace();
        fmt.printLeadingToken("(");

        Iterator iter = obj.getInitStatements().iterator();
        while (iter.hasNext())
        {
            Object stmt = iter.next();
            if (stmt instanceof JavaLocalVariable)
            {
                JavaLocalVariable var = (JavaLocalVariable) stmt;
                basePrinter.printVarDecl(var, pkgContext);
                basePrinter.printVarInit(var, typeContext);
            }
            else
            {
                JavaExpressionStatement exprStmt = (JavaExpressionStatement) stmt;
                basePrinter.printExpression(exprStmt.getExpression(),
                    typeContext);
            }
            if (iter.hasNext())
            {
                fmt.printTrailingToken(",");
                fmt.printSpace();
            }
        }
        fmt.printTrailingToken(";");
        fmt.printSpace();

        JavaExpression condExpr = (JavaExpression) obj.getCondition();
        if (condExpr != null)
        {
            basePrinter.printExpression(condExpr, typeContext);
        }
        fmt.printTrailingToken(";");
        fmt.printSpace();

        iter = obj.getUpdateStatements().iterator();
        while (iter.hasNext())
        {
            JavaExpressionStatement exprStmt = (JavaExpressionStatement) iter
                .next();
            basePrinter.printExpression(exprStmt.getExpression(), typeContext);
            if (iter.hasNext())
            {
                fmt.printTrailingToken(",");
                fmt.printSpace();
            }
        }

        fmt.printTrailingToken(")");
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();

        printNestedStmt((JavaStatement) obj.getStatement());
    }

    public void visit(JavaIfStatement obj)
    {
        JavaIfStatement cur = obj;
        while (cur != null)
        {
            JavaStatement thenStmt = cur.getThenStatement();
            JavaStatement elseStmt = cur.getElseStatement();

            basePrinter.printComments(obj, true, true);
            printGuard("if", cur.getCondition());
            basePrinter.printComments(obj, false);

            if (elseStmt == null && isSimpleStmt(thenStmt))
            {
                fmt.printSpace();
                thenStmt.accept(this);
            }
            else
            {
                if (!fmt.isNewLine()) printNewLine();
                printNestedStmt(thenStmt);
            }

            cur = null;
            if (elseStmt != null)
            {
                fmt.printToken("else");
                if (elseStmt instanceof JavaIfStatement)
                {
                    fmt.printSpace();
                    cur = (JavaIfStatement) elseStmt;
                }
                else
                {
                    printNewLine();
                    printNestedStmt(elseStmt);
                }
            }
        }
    }

    private boolean isSimpleStmt(JavaStatement stmt)
    {
        return stmt instanceof JavaExpressionStatement
            || stmt instanceof JavaBreakStatement
            || stmt instanceof JavaContinueStatement
            || stmt instanceof JavaReturnStatement
            || stmt instanceof JavaThrowStatement;
    }

    public void visit(JavaLabeledStatement obj)
    {
        basePrinter.printComments(obj, true, true);
        basePrinter.printName(obj.getName(), pkgContext);
        fmt.printTrailingToken(":");
        fmt.printSpace();
        printNestedStmt(obj.getStatement());
        basePrinter.printComments(obj, false);
    }

    public void visit(JavaReturnStatement obj)
    {
        basePrinter.printComments(obj, true, true);
        fmt.printToken("return");
        JavaExpression returnValue = obj.getReturnValue();
        if (returnValue != null)
        {
            fmt.printSpace();
            basePrinter.printExpression(returnValue, typeContext);
        }
        fmt.printTrailingToken(";");
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    public void visit(JavaSwitchStatement obj)
    {
        basePrinter.printComments(obj, true, true);
        printGuard("switch", (JavaExpression) obj.getSelector());
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();

        fmt.printToken("{");
        printNewLine();

        Iterator iter = obj.getCases().iterator();
        while (iter.hasNext())
        {
            JavaSwitchCase c = (JavaSwitchCase) iter.next();
            if (c instanceof JavaSwitchValueCase)
            {
                JavaSwitchValueCase vc = (JavaSwitchValueCase) c;
                Iterator valueIter = vc.getValues().iterator();
                while (valueIter.hasNext())
                {
                    JavaExpression expr = (JavaExpression) valueIter.next();
                    fmt.printToken("case");
                    fmt.printSpace();
                    basePrinter.printExpression(expr, typeContext);
                    fmt.printTrailingToken(":");
                    printNewLine();
                }
            }
            else
            {
                assert (c instanceof JavaSwitchDefaultCase);
                fmt.printToken("default");
                fmt.printTrailingToken(":");
                printNewLine();
            }
            fmt.incIndent();
            Iterator memberIter = c.getMembers().iterator();
            while (memberIter.hasNext())
            {
                JavaBlockMember member = (JavaBlockMember) memberIter.next();
                member.accept(this);
            }
            fmt.decIndent();
        }

        fmt.printToken("}");
        printNewLine();
    }

    public void visit(JavaSynchronizedStatement obj)
    {
        basePrinter.printComments(obj, true, true);
        printGuard("synchronized", obj.getLock());
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();

        printNestedStmt(obj.getBlock());
    }

    public void visit(JavaThrowStatement obj)
    {
        basePrinter.printComments(obj, true, true);
        fmt.printToken("throw");
        fmt.printSpace();
        basePrinter.printExpression(obj.getException(), typeContext);
        fmt.printTrailingToken(";");
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    public void visit(JavaTryStatement obj)
    {
        basePrinter.printComments(obj, true, true);
        fmt.printToken("try");
        printNewLine();
        basePrinter.printBlock(obj.getTryBlock(), typeContext);

        Iterator iter = obj.getCatches().iterator();
        while (iter.hasNext())
        {
            JavaTryCatch c = (JavaTryCatch) iter.next();
            fmt.printToken("catch");
            fmt.printSpace();
            fmt.printLeadingToken("(");
            basePrinter.printVarDecl(c.getExceptionVariable(), pkgContext);
            fmt.printTrailingToken(")");
            printNewLine();
            basePrinter.printBlock(c.getCatchBlock(), typeContext);
        }

        JavaBlock finallyBlock = obj.getFinallyBlock();
        if (finallyBlock != null)
        {
            fmt.printToken("finally");
            printNewLine();
            basePrinter.printBlock(finallyBlock, typeContext);
        }
        basePrinter.printComments(obj, false);
    }

    public void visit(JavaWhileStatement obj)
    {
        basePrinter.printComments(obj, true, true);
        printGuard("while", obj.getCondition());
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();

        printNestedStmt(obj.getStatement());
    }
}
