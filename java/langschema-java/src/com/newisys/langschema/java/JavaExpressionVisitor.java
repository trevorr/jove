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

package com.newisys.langschema.java;

/**
 * Visitor over Java expressions.
 * 
 * @author Trevor Robinson
 */
public interface JavaExpressionVisitor
{
    void visit(JavaAdd obj);

    void visit(JavaAnd obj);

    void visit(JavaArrayAccess obj);

    void visit(JavaArrayCreation obj);

    void visit(JavaArrayInitializer obj);

    void visit(JavaAssign obj);

    void visit(JavaAssignAdd obj);

    void visit(JavaAssignAnd obj);

    void visit(JavaAssignDivide obj);

    void visit(JavaAssignLeftShift obj);

    void visit(JavaAssignModulo obj);

    void visit(JavaAssignMultiply obj);

    void visit(JavaAssignOr obj);

    void visit(JavaAssignSignedRightShift obj);

    void visit(JavaAssignSubtract obj);

    void visit(JavaAssignUnsignedRightShift obj);

    void visit(JavaAssignXor obj);

    void visit(JavaBitwiseNot obj);

    void visit(JavaBooleanLiteral obj);

    void visit(JavaCastExpression obj);

    void visit(JavaCharLiteral obj);

    void visit(JavaConditional obj);

    void visit(JavaConditionalAnd obj);

    void visit(JavaConditionalOr obj);

    void visit(JavaConstructorInvocation obj);

    void visit(JavaConstructorReference obj);

    void visit(JavaDivide obj);

    void visit(JavaDoubleLiteral obj);

    void visit(JavaEqual obj);

    void visit(JavaFloatLiteral obj);

    void visit(JavaFunctionInvocation obj);

    void visit(JavaFunctionReference obj);

    void visit(JavaGreater obj);

    void visit(JavaGreaterOrEqual obj);

    void visit(JavaInstanceCreation obj);

    void visit(JavaIntLiteral obj);

    void visit(JavaLeftShift obj);

    void visit(JavaLess obj);

    void visit(JavaLessOrEqual obj);

    void visit(JavaLogicalNot obj);

    void visit(JavaLongLiteral obj);

    void visit(JavaMemberAccess obj);

    void visit(JavaModulo obj);

    void visit(JavaMultiply obj);

    void visit(JavaNotEqual obj);

    void visit(JavaNullLiteral obj);

    void visit(JavaOr obj);

    void visit(JavaPostDecrement obj);

    void visit(JavaPostIncrement obj);

    void visit(JavaPreDecrement obj);

    void visit(JavaPreIncrement obj);

    void visit(JavaSignedRightShift obj);

    void visit(JavaStringLiteral obj);

    void visit(JavaSubtract obj);

    void visit(JavaSuperReference obj);

    void visit(JavaThisReference obj);

    void visit(JavaTypeLiteral obj);

    void visit(JavaTypeTest obj);

    void visit(JavaUnaryMinus obj);

    void visit(JavaUnaryPlus obj);

    void visit(JavaUnsignedRightShift obj);

    void visit(JavaVariableReference obj);

    void visit(JavaXor obj);
}
