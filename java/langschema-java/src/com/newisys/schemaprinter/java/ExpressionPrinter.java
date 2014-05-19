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

import java.util.Iterator;

import com.newisys.langschema.Name;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.Scope;
import com.newisys.langschema.StructuredType;
import com.newisys.langschema.Variable;
import com.newisys.langschema.java.*;

/**
 * Printer module used to print Java expressions. Instances of this module
 * are created on the fly, since they depend on the type and package context
 * and the precedence of the parent expression.
 * 
 * @author Trevor Robinson
 */
public class ExpressionPrinter
    extends JavaSchemaPrinterModule
    implements JavaExpressionVisitor
{
    private final JavaStructuredType< ? > typeContext;
    private final JavaPackage pkgContext;
    private final int parentPrecedence;

    public ExpressionPrinter(
        BasePrinter basePrinter,
        JavaStructuredType< ? > typeContext,
        JavaPackage pkgContext,
        int parentPrecedence)
    {
        super(basePrinter);
        this.typeContext = typeContext;
        this.pkgContext = pkgContext;
        this.parentPrecedence = parentPrecedence;
    }

    // Precedence:
    // 0: primary: <literal> this super.<ident> () new <type>.class <name>
    //     suffixes: .this .new [] .<ident> ()
    // 1: unary: + - ++ -- ~ ! <cast>
    // 2: * / %
    // 3: + -
    // 4: << >> >>>
    // 5: < > <= >=
    // 6: instanceof
    // 7: == !=
    // 8: &
    // 9: ^
    // 10: |
    // 11: &&
    // 12: ||
    // 13: ?:
    // 14: = += -= *= /= %= &= |= ^= <<= >>= >>>=

    private void printUnary(JavaUnaryOperation op, String token, boolean prefix)
    {
        final int precedence = 1;
        final boolean needParens = precedence > parentPrecedence;
        if (needParens) fmt.printLeadingToken("(");

        if (prefix) fmt.printLeadingToken(token);

        JavaExpression op1 = op.getOperand(0);
        basePrinter.printExpression(op1, typeContext, precedence
            - (prefix ? 0 : 1));

        if (!prefix) fmt.printTrailingToken(token);

        if (needParens) fmt.printTrailingToken(")");
    }

    private void printInfix(JavaBinaryOperation op, String token, int precedence)
    {
        final boolean needParens = precedence > parentPrecedence;
        if (needParens) fmt.printLeadingToken("(");

        JavaExpression op1 = op.getOperand(0);
        basePrinter.printExpression(op1, typeContext, precedence);

        fmt.printSpace();
        fmt.printLeadingToken(token);
        fmt.printSpace();

        JavaExpression op2 = op.getOperand(1);
        basePrinter.printExpression(op2, typeContext, precedence - 1);

        if (needParens) fmt.printTrailingToken(")");
    }

    private void checkQualify(JavaStructuredTypeMember member, boolean isStatic)
    {
        JavaStructuredType< ? > memberType = member.getStructuredType();
        if (!memberInScope(member, memberType, typeContext))
        {
            if (isStatic)
            {
                // static reference: qualify with member type
                basePrinter.printType(memberType, pkgContext);
                fmt.printLeadingToken(".");
            }
            else if (memberType.isAssignableFrom(typeContext))
            {
                // instance reference to hidden member of base class/interface:
                // qualify with 'this' cast to member type
                printCast(memberType, new JavaThisReference(typeContext), 0);
                fmt.printLeadingToken(".");
            }
            else
            {
                // check for instance reference to shadowed member of
                // (possibly base class or interface of) containing type
                JavaStructuredType< ? > containingType = typeContext
                    .getStructuredType();
                while (containingType != null
                    && !memberType.isAssignableFrom(containingType))
                {
                    containingType = containingType.getStructuredType();
                }
                if (containingType != null)
                {
                    // qualify with qualified-'this' of containing type
                    basePrinter.printType(containingType, pkgContext);
                    fmt.printLeadingToken(".");
                    fmt.printToken("this");
                    fmt.printLeadingToken(".");
                }
                else
                {
                    throw new RuntimeException(member + " not accessible from "
                        + typeContext);
                }
            }
        }
    }

    private boolean memberInScope(
        JavaStructuredTypeMember member,
        JavaStructuredType< ? > memberType,
        JavaStructuredType< ? > fromType)
    {
        if (memberType == fromType)
        {
            // member of same class?
            return true;
        }
        else if (fromType == null
            || containsNameOf(fromType, (NamedObject) member))
        {
            // no from-type or member hidden by member of from-type
            return false;
        }

        if (memberType instanceof JavaAbstractInterface)
        {
            // member of implemented interface?
            JavaAbstractInterface memberIntf = (JavaAbstractInterface) memberType;
            if (fromType.implementsInterface(memberIntf))
            {
                return true;
            }
            else if (baseInterfaceContainsNameOf(fromType, (NamedObject) member))
            {
                // hidden by member in base interface of from-type
                return false;
            }
        }

        if (memberType instanceof JavaAbstractClass
            && fromType instanceof JavaAbstractClass)
        {
            // member of superclass?
            JavaAbstractClass memberClass = (JavaAbstractClass) memberType;
            JavaAbstractClass fromClass = (JavaAbstractClass) fromType;
            if (memberClass.isSuperclassOf(fromClass))
            {
                return true;
            }
            else if (baseClassContainsNameOf(fromClass, (NamedObject) member))
            {
                // hidden by member in base class of from-type
                return false;
            }
        }

        // in scope for containing class?
        JavaStructuredType< ? > outerType = fromType.getStructuredType();
        if (outerType != null)
        {
            return memberInScope(member, memberType, outerType);
        }

        return false;
    }

    private boolean containsNameOf(Scope scope, NamedObject obj)
    {
        Name name = obj.getName();
        Iterator< ? > iter = scope.lookupObjects(name.getIdentifier(), name
            .getKind());
        return iter.hasNext();
    }

    private boolean baseInterfaceContainsNameOf(
        JavaStructuredType< ? > type,
        NamedObject obj)
    {
        final Iterator<JavaAbstractInterface> iter = type.getBaseInterfaces()
            .iterator();
        while (iter.hasNext())
        {
            final JavaAbstractInterface baseIntf = iter.next();
            if (containsNameOf(baseIntf, obj)) return true;
        }
        return false;
    }

    private boolean containsNameOf(JavaAbstractInterface intf, NamedObject obj)
    {
        return containsNameOf((Scope) intf, obj)
            || baseInterfaceContainsNameOf(intf, obj);
    }

    private boolean baseClassContainsNameOf(
        JavaAbstractClass cls,
        NamedObject obj)
    {
        final JavaAbstractClass baseCls = cls.getBaseClass();
        return baseCls != null
            && (containsNameOf(baseCls, obj) || baseClassContainsNameOf(
                baseCls, obj));
    }

    public void visit(JavaAdd obj)
    {
        printInfix(obj, "+", 3);
    }

    public void visit(JavaAnd obj)
    {
        printInfix(obj, "&", 9);
    }

    public void visit(JavaArrayAccess obj)
    {
        basePrinter.printExpression(obj.getArray(), typeContext, 0);
        Iterator<JavaExpression> iter = obj.getIndices().iterator();
        while (iter.hasNext())
        {
            fmt.printLeadingToken("[");
            basePrinter.printExpression(iter.next(), typeContext);
            fmt.printTrailingToken("]");
        }
    }

    public void visit(JavaArrayCreation obj)
    {
        fmt.printToken("new");
        fmt.printSpace();
        JavaArrayType arrayType = obj.getType();
        JavaArrayInitializer initializer = obj.getInitializer();
        if (initializer == null)
        {
            JavaType elementType = arrayType.getElementType();
            basePrinter.printType(elementType, pkgContext);
            Iterator<JavaExpression> iter = obj.getDimensions().iterator();
            while (iter.hasNext())
            {
                fmt.printLeadingToken("[");
                basePrinter.printExpression(iter.next(), typeContext);
                fmt.printTrailingToken("]");
            }
        }
        else
        {
            basePrinter.printType(arrayType, pkgContext);
            fmt.printSpace();
            basePrinter.printExpression(initializer, typeContext);
        }
    }

    public void visit(JavaArrayInitializer obj)
    {
        fmt.printLeadingToken("{");
        Iterator<JavaExpression> iter = obj.getElements().iterator();
        while (iter.hasNext())
        {
            fmt.printSpace();
            basePrinter.printExpression(iter.next(), typeContext);
            if (iter.hasNext()) fmt.printTrailingToken(",");
        }
        fmt.printSpace();
        fmt.printTrailingToken("}");
    }

    public void visit(JavaAssign obj)
    {
        printInfix(obj, "=", 14);
    }

    public void visit(JavaAssignAdd obj)
    {
        printInfix(obj, "+=", 14);
    }

    public void visit(JavaAssignAnd obj)
    {
        printInfix(obj, "&=", 14);
    }

    public void visit(JavaAssignDivide obj)
    {
        printInfix(obj, "/=", 14);
    }

    public void visit(JavaAssignLeftShift obj)
    {
        printInfix(obj, "<<=", 14);
    }

    public void visit(JavaAssignModulo obj)
    {
        printInfix(obj, "%=", 14);
    }

    public void visit(JavaAssignMultiply obj)
    {
        printInfix(obj, "*=", 14);
    }

    public void visit(JavaAssignOr obj)
    {
        printInfix(obj, "|=", 14);
    }

    public void visit(JavaAssignSignedRightShift obj)
    {
        printInfix(obj, ">>=", 14);
    }

    public void visit(JavaAssignSubtract obj)
    {
        printInfix(obj, "-=", 14);
    }

    public void visit(JavaAssignUnsignedRightShift obj)
    {
        printInfix(obj, ">>>=", 14);
    }

    public void visit(JavaAssignXor obj)
    {
        printInfix(obj, "^=", 14);
    }

    public void visit(JavaBitwiseNot obj)
    {
        printUnary(obj, "~", true);
    }

    public void visit(JavaBooleanLiteral obj)
    {
        fmt.printToken(String.valueOf(obj.getValue()));
    }

    public void visit(JavaCastExpression obj)
    {
        printCast(obj.getType(), obj.getExpression(), parentPrecedence);
    }

    private void printCast(
        JavaType type,
        JavaExpression expr,
        int parentPrecedence)
    {
        final int precedence = 1;
        final boolean needParens = precedence > parentPrecedence;
        if (needParens) fmt.printLeadingToken("(");

        fmt.printLeadingToken("(");
        basePrinter.printType(type, pkgContext);
        fmt.printTrailingToken(")");
        fmt.printSpace();

        basePrinter.printExpression(expr, typeContext, precedence);

        if (needParens) fmt.printTrailingToken(")");
    }

    public void visit(JavaCharLiteral obj)
    {
        char value = obj.getValue();
        String escape = (value == '\'' || value == '\\') ? "\\" : "";
        fmt.printToken('\'' + escape + value + '\'');
    }

    public void visit(JavaConditional obj)
    {
        final int precedence = 13;
        final boolean needParens = precedence > parentPrecedence;
        if (needParens) fmt.printLeadingToken("(");

        JavaExpression op1 = obj.getOperand(0);
        basePrinter.printExpression(op1, typeContext, precedence - 1);

        fmt.printSpace();
        fmt.printLeadingToken("?");
        fmt.printSpace();

        JavaExpression op2 = obj.getOperand(1);
        basePrinter.printExpression(op2, typeContext, precedence);

        fmt.printSpace();
        fmt.printLeadingToken(":");
        fmt.printSpace();

        JavaExpression op3 = obj.getOperand(2);
        basePrinter.printExpression(op3, typeContext, precedence);

        if (needParens) fmt.printTrailingToken(")");
    }

    public void visit(JavaConditionalAnd obj)
    {
        printInfix(obj, "&&", 11);
    }

    public void visit(JavaConditionalOr obj)
    {
        printInfix(obj, "||", 12);
    }

    public void visit(JavaConstructorInvocation obj)
    {
        basePrinter.printExpression(obj.getConstructor(), typeContext, 0);
        basePrinter.printArgs(obj.getArguments(), typeContext);
    }

    public void visit(JavaConstructorReference obj)
    {
        StructuredType ctorCls = obj.getConstructor().getStructuredType();
        fmt.printToken(ctorCls == typeContext ? "this" : "super");
    }

    public void visit(JavaDivide obj)
    {
        printInfix(obj, "/", 2);
    }

    public void visit(JavaDoubleLiteral obj)
    {
        fmt.printToken(String.valueOf(obj.getValue()) + "d");
    }

    public void visit(JavaEqual obj)
    {
        printInfix(obj, "==", 7);
    }

    public void visit(JavaFloatLiteral obj)
    {
        fmt.printToken(String.valueOf(obj.getValue()) + "f");
    }

    public void visit(JavaFunctionInvocation obj)
    {
        basePrinter.printExpression(obj.getFunction(), typeContext, 0);
        basePrinter.printArgs(obj.getArguments(), typeContext);
    }

    public void visit(JavaFunctionReference obj)
    {
        JavaFunction func = obj.getFunction();
        checkQualify(func, func.getModifiers().contains(
            JavaFunctionModifier.STATIC));
        fmt.printToken(func.getName().getIdentifier());
    }

    public void visit(JavaGreater obj)
    {
        printInfix(obj, ">", 5);
    }

    public void visit(JavaGreaterOrEqual obj)
    {
        printInfix(obj, ">=", 5);
    }

    public void visit(JavaInstanceCreation obj)
    {
        fmt.printToken("new");
        fmt.printSpace();
        basePrinter.printType(obj.getType(), pkgContext);
        basePrinter.printArgs(obj.getArguments(), typeContext);
        JavaClass anonCls = obj.getAnonymousClass();
        if (anonCls != null)
        {
            fmt.printSpace();
            basePrinter.printTypeBody(anonCls);
        }
    }

    public void visit(JavaIntLiteral obj)
    {
        final StringBuffer buf = new StringBuffer(14);
        int value = obj.getValue();
        // Java does not really have negative integer literals according to
        // the lexical spec (negative integers are produced by the unary
        // negation operator preceding a positive integer literal), but our
        // schema does not necessarily prohibit them
        if (value < 0)
        {
            buf.append('-');
            value = -value;
        }
        int radix = obj.getRadix();
        switch (radix)
        {
        case 16:
            buf.append("0x");
            break;
        case 8:
            buf.append('0');
            break;
        default:
            assert (radix == 10);
        }
        buf.append(Integer.toString(value, radix));
        fmt.printToken(buf.toString());
    }

    public void visit(JavaLeftShift obj)
    {
        printInfix(obj, "<<", 4);
    }

    public void visit(JavaLess obj)
    {
        printInfix(obj, "<", 5);
    }

    public void visit(JavaLessOrEqual obj)
    {
        printInfix(obj, "<=", 5);
    }

    public void visit(JavaLogicalNot obj)
    {
        printUnary(obj, "!", true);
    }

    public void visit(JavaLongLiteral obj)
    {
        final StringBuffer buf = new StringBuffer(25);
        long value = obj.getValue();
        // Java does not really have negative integer literals according to
        // the lexical spec (negative integers are produced by the unary
        // negation operator preceding a positive integer literal), but our
        // schema does not necessarily prohibit them
        if (value < 0)
        {
            buf.append('-');
            value = -value;
        }
        int radix = obj.getRadix();
        switch (radix)
        {
        case 16:
            buf.append("0x");
            break;
        case 8:
            buf.append('0');
            break;
        default:
            assert (radix == 10);
        }
        buf.append(Long.toString(value, radix));
        buf.append('L');
        fmt.printToken(buf.toString());
    }

    public void visit(JavaMemberAccess obj)
    {
        basePrinter.printExpression(obj.getObject(), typeContext, 0);
        fmt.printLeadingToken(".");
        NamedObject member = (NamedObject) obj.getMember();
        fmt.printToken(member.getName().getIdentifier());
    }

    public void visit(JavaModulo obj)
    {
        printInfix(obj, "%", 2);
    }

    public void visit(JavaMultiply obj)
    {
        printInfix(obj, "*", 2);
    }

    public void visit(JavaNotEqual obj)
    {
        printInfix(obj, "!=", 7);
    }

    public void visit(JavaNullLiteral obj)
    {
        fmt.printToken("null");
    }

    public void visit(JavaOr obj)
    {
        printInfix(obj, "|", 10);
    }

    public void visit(JavaPostDecrement obj)
    {
        printUnary(obj, "--", false);
    }

    public void visit(JavaPostIncrement obj)
    {
        printUnary(obj, "++", false);
    }

    public void visit(JavaPreDecrement obj)
    {
        printUnary(obj, "--", true);
    }

    public void visit(JavaPreIncrement obj)
    {
        printUnary(obj, "++", true);
    }

    public void visit(JavaSignedRightShift obj)
    {
        printInfix(obj, ">>", 4);
    }

    public void visit(JavaStringLiteral obj)
    {
        fmt.printToken('"' + JavaStringLiteral.escape(obj.getValue()) + '"');
    }

    public void visit(JavaSubtract obj)
    {
        printInfix(obj, "-", 3);
    }

    public void visit(JavaSuperReference obj)
    {
        JavaType type = (JavaType) obj.getType();
        if (type != typeContext)
        {
            basePrinter.printType(type, pkgContext);
            fmt.printLeadingToken(".");
        }
        fmt.printToken("super");
    }

    public void visit(JavaThisReference obj)
    {
        JavaType type = obj.getType();
        if (type != typeContext)
        {
            basePrinter.printType(type, pkgContext);
            fmt.printLeadingToken(".");
        }
        fmt.printToken("this");
    }

    public void visit(JavaTypeLiteral obj)
    {
        basePrinter.printType(obj.getType(), pkgContext);
        fmt.printLeadingToken(".");
        fmt.printToken("class");
    }

    public void visit(JavaTypeTest obj)
    {
        final int precedence = 6;
        final boolean needParens = precedence > parentPrecedence;
        if (needParens) fmt.printLeadingToken("(");

        JavaExpression expr = obj.getExpression();
        basePrinter.printExpression(expr, typeContext, precedence);

        fmt.printSpace();
        fmt.printLeadingToken("instanceof");
        fmt.printSpace();

        basePrinter.printType(obj.getType(), pkgContext);

        if (needParens) fmt.printTrailingToken(")");
    }

    public void visit(JavaUnaryMinus obj)
    {
        printUnary(obj, "-", true);
    }

    public void visit(JavaUnaryPlus obj)
    {
        printUnary(obj, "+", true);
    }

    public void visit(JavaUnsignedRightShift obj)
    {
        printInfix(obj, ">>>", 4);
    }

    public void visit(JavaVariableReference obj)
    {
        Variable var = obj.getVariable();
        if (var instanceof JavaMemberVariable)
        {
            checkQualify((JavaMemberVariable) var, var.getModifiers().contains(
                JavaVariableModifier.STATIC));
        }
        fmt.printToken(var.getName().getIdentifier());
    }

    public void visit(JavaXor obj)
    {
        printInfix(obj, "^", 9);
    }
}
