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

import java.util.Iterator;
import java.util.ListIterator;

import com.newisys.langschema.java.*;
import com.newisys.randsolver.InvalidConstraintException;

// TODO change the RuntimeExceptions of schema objects we'll never see to asserts

public class AbsConsVisitor
    implements ConsConstraintVisitor, ConsConstraintExpressionVisitor
{
    protected static boolean DEBUG = false;
    protected ListIterator mListIterator;

    public AbsConsVisitor()
    {
        mListIterator = null;
    }

    protected void debug(Object obj)
    {
        if (DEBUG)
        {
            System.out.println("DEBUG[" + obj.getClass() + "]: " + obj);
        }
    }

    public void visit(ConsConstraint obj)
    {
        debug(obj);
        Iterator exprIter = obj.getExprs().iterator();
        while (exprIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) exprIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsAdd obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsAnd obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsArrayAccess obj)
    {
        debug(obj);
        throw new RuntimeException("FIXME?");
    }

    public void visit(ConsBitwiseNot obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsBitVectorLiteral obj)
    {
        debug(obj);
    }

    public void visit(ConsBooleanLiteral obj)
    {
        debug(obj);
        throw new RuntimeException("FIXME?");
    }

    public void visit(ConsCharLiteral obj)
    {
        debug(obj);
    }

    public void visit(ConsConditional obj)
    {
        debug(obj);
        throw new RuntimeException("FIXME?");
    }

    public void visit(ConsConditionalAnd obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsConditionalOr obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsConstraintSet obj)
    {
        debug(obj);
        Iterator opIter = obj.getExprs().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsDistSet obj)
    {
        debug(obj);
        obj.getExpr().accept(this);
        Iterator opIter = obj.getMembers().iterator();
        while (opIter.hasNext())
        {
            ConsSetMember member = (ConsSetMember) opIter.next();
            member.accept(this);
        }
    }

    public void visit(ConsDivide obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsDoubleLiteral obj)
    {
        debug(obj);
        throw new RuntimeException("FIXME?");
    }

    public void visit(ConsEqual obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsFloatLiteral obj)
    {
        debug(obj);
        throw new RuntimeException("FIXME?");
    }

    public void visit(ConsGreater obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsGreaterOrEqual obj)
    {
        debug(obj);
        assert (obj.getOperands().size() == 2);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsInSet obj)
    {
        debug(obj);
        obj.getExpr().accept(this);
        Iterator opIter = obj.getMembers().iterator();
        while (opIter.hasNext())
        {
            ConsSetMember member = (ConsSetMember) opIter.next();
            member.accept(this);
        }
    }

    public void visit(ConsImplication obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsIntLiteral obj)
    {
        debug(obj);
        // OK to do nothing here
    }

    public void visit(ConsLeftShift obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsLess obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsLessOrEqual obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsLogicalNot obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsLongLiteral obj)
    {
        debug(obj);
        throw new RuntimeException("FIXME?");
    }

    public void visit(ConsMemberAccess obj)
    {
        debug(obj);
        throw new RuntimeException("FIXME?");
    }

    public void visit(ConsModulo obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsMultiply obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsNotEqual obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsNullLiteral obj)
    {
        debug(obj);
        throw new RuntimeException("FIXME?");
    }

    public void visit(ConsOr obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsSetRange obj)
    {
        debug(obj);
        obj.getLow().accept(this);
        obj.getHigh().accept(this);
    }

    public void visit(ConsSetValue obj)
    {
        debug(obj);
        obj.getValue().accept(this);
    }

    public void visit(ConsSignedRightShift obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsStringLiteral obj)
    {
        debug(obj);
        throw new RuntimeException("FIXME?");
    }

    public void visit(ConsSubtract obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsSuperReference obj)
    {
        debug(obj);
        throw new RuntimeException("FIXME?");
    }

    public void visit(ConsThisReference obj)
    {
        debug(obj);
        throw new RuntimeException("FIXME?");
    }

    public void visit(ConsTypeLiteral obj)
    {
        debug(obj);
        throw new RuntimeException("FIXME?");
    }

    public void visit(ConsUnaryMinus obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsUnaryPlus obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsUnsignedRightShift obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(ConsVariableReference obj)
    {
        debug(obj);

    }

    public void visit(ConsXor obj)
    {
        debug(obj);
        Iterator opIter = obj.getOperands().iterator();
        while (opIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) opIter.next();
            expr.accept(this);
        }
    }

    public void visit(JavaAnnotationType obj)
    {
        debug(obj);
        throw new InvalidConstraintException("Operation not supported: " + obj);
    }

    public void visit(JavaClass obj)
    {
        debug(obj);
        throw new InvalidConstraintException("Operation not supported: " + obj);
    }

    public void visit(JavaEnum obj)
    {
        debug(obj);
        throw new InvalidConstraintException("Operation not supported: " + obj);
    }

    public void visit(JavaInterface obj)
    {
        debug(obj);
        throw new InvalidConstraintException("Operation not supported: " + obj);
    }

    public void visit(JavaMemberVariable obj)
    {
        debug(obj);
        throw new RuntimeException("FIXME?");
    }

    public void visit(JavaInitializerBlock obj)
    {
        debug(obj);
        throw new InvalidConstraintException("Operation not supported: " + obj);
    }

    public void visit(JavaConstructor obj)
    {
        debug(obj);
        throw new InvalidConstraintException("Operation not supported: " + obj);
    }

    public void visit(JavaFunction obj)
    {
        debug(obj);
        throw new InvalidConstraintException("Operation not supported: " + obj);
    }

    public void visit(JavaTypeLiteral obj)
    {
        debug(obj);
        throw new RuntimeException("FIXME?");
    }

    public void visit(JavaTypeTest obj)
    {
        debug(obj);
        throw new InvalidConstraintException("Operation not supported: " + obj);
    }

}