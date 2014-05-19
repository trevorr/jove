/*
 * Jove Constraint-based Random Solver
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

package com.newisys.langschema.constraint;

public interface ConsExpressionVisitor
{
    void visit(ConsAdd obj);

    void visit(ConsAnd obj);

    void visit(ConsArrayAccess obj);

    void visit(ConsBitwiseNot obj);

    void visit(ConsBooleanLiteral obj);

    void visit(ConsCharLiteral obj);

    void visit(ConsConditional obj);

    void visit(ConsConditionalAnd obj);

    void visit(ConsConditionalOr obj);

    void visit(ConsConstraintSet obj);

    void visit(ConsDivide obj);

    void visit(ConsDoubleLiteral obj);

    void visit(ConsEqual obj);

    void visit(ConsFloatLiteral obj);

    void visit(ConsGreater obj);

    void visit(ConsGreaterOrEqual obj);

    void visit(ConsIntLiteral obj);

    void visit(ConsLeftShift obj);

    void visit(ConsLess obj);

    void visit(ConsLessOrEqual obj);

    void visit(ConsLogicalNot obj);

    void visit(ConsLongLiteral obj);

    void visit(ConsMemberAccess obj);

    void visit(ConsModulo obj);

    void visit(ConsMultiply obj);

    void visit(ConsNotEqual obj);

    void visit(ConsNullLiteral obj);

    void visit(ConsOr obj);

    void visit(ConsSignedRightShift obj);

    void visit(ConsStringLiteral obj);

    void visit(ConsSubtract obj);

    void visit(ConsSuperReference obj);

    void visit(ConsThisReference obj);

    void visit(ConsTypeLiteral obj);

    void visit(ConsUnaryMinus obj);

    void visit(ConsUnaryPlus obj);

    void visit(ConsUnsignedRightShift obj);

    void visit(ConsVariableReference obj);

    void visit(ConsXor obj);
}
