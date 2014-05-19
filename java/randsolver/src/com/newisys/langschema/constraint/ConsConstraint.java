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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.newisys.langschema.Name;
import com.newisys.langschema.java.JavaClass;
import com.newisys.langschema.java.JavaName;
import com.newisys.langschema.java.JavaStructuredType;
import com.newisys.langschema.java.JavaStructuredTypeMemberVisitor;
import com.newisys.langschema.java.JavaVisibility;
import com.newisys.randsolver.RandVarSet;

public final class ConsConstraint
    extends ConsSchemaObjectImpl
{
    private static final long serialVersionUID = 3905239035201990962L;

    private final JavaName name;
    private JavaClass cls;
    private List<ConsExpression> exprs = new LinkedList<ConsExpression>();
    private RandVarSet myVars;

    private static final class ConstraintModifier
        extends AbsConsVisitor
    {
        private ConsConstraint mNewTree;
        private ConsExpression mCurExpr;
        private ConsSchema mSchema;
        private List<ConsSetMember> mSetMembers;
        private Map mReplacementMap;

        ConstraintModifier(ConsConstraint newTree, Map replacementMap)
        {
            mSchema = newTree.schema;
            mNewTree = newTree;
            mReplacementMap = replacementMap;
            mCurExpr = null;
        }

        public void visit(ConsConstraint obj)
        {
            Iterator exprIter = obj.getExprs().iterator();
            while (exprIter.hasNext())
            {
                ConsExpression expr = (ConsExpression) exprIter.next();
                expr.accept(this);
                assert (mCurExpr != null);
                mNewTree.addExpr(mCurExpr);
                mCurExpr = null;
            }
        }

        // Variable (only place where substitution occurs)
        public void visit(ConsVariableReference obj)
        {
            mCurExpr = obj;
            if (mReplacementMap.keySet().contains(obj))
            {
                mCurExpr = (ConsExpression) mReplacementMap.get(obj);
            }
        }

        public void visit(ConsUnaryMinus obj)
        {
            mCurExpr = obj;
        }

        public void visit(ConsUnaryPlus obj)
        {
            mCurExpr = obj;
        }

        // Literal values
        public void visit(ConsBooleanLiteral obj)
        {
            mCurExpr = obj;
        }

        public void visit(ConsBitVectorLiteral obj)
        {
            mCurExpr = obj;
        }

        public void visit(ConsCharLiteral obj)
        {
            mCurExpr = obj;
        }

        public void visit(ConsDoubleLiteral obj)
        {
            mCurExpr = obj;
        }

        public void visit(ConsFloatLiteral obj)
        {
            mCurExpr = obj;
        }

        public void visit(ConsIntLiteral obj)
        {
            mCurExpr = obj;
        }

        public void visit(ConsLongLiteral obj)
        {
            mCurExpr = obj;
        }

        // Set operations
        public void visit(ConsInSet expr)
        {
            ConsExpression cExpr = null;
            mSetMembers = new LinkedList<ConsSetMember>();

            cExpr = expr.getExpr();
            cExpr.accept(this);
            cExpr = mCurExpr;

            Iterator iter = expr.getMembers().iterator();
            while (iter.hasNext())
            {
                ConsSetMember member = (ConsSetMember) iter.next();
                member.accept(this);

            }
            ConsInSet inSet = null;
            if (expr instanceof ConsNotInSet)
            {
                inSet = new ConsNotInSet(cExpr);
            }
            else
            {
                inSet = new ConsInSet(cExpr);
            }

            iter = mSetMembers.iterator();
            while (iter.hasNext())
            {
                inSet.addMember((ConsSetMember) iter.next());
            }
            mSetMembers = null;
            mCurExpr = inSet;
        }

        public void visit(ConsDistSet expr)
        {
            ConsExpression cExpr = null;
            mSetMembers = new LinkedList<ConsSetMember>();

            cExpr = expr.getExpr();
            cExpr.accept(this);
            cExpr = mCurExpr;

            Iterator iter = expr.getMembers().iterator();
            while (iter.hasNext())
            {
                ConsSetMember member = (ConsSetMember) iter.next();
                member.accept(this);

            }
            ConsDistSet distSet = new ConsDistSet(cExpr);

            iter = mSetMembers.iterator();
            while (iter.hasNext())
            {
                distSet.addMember((ConsSetMember) iter.next());
            }
            mSetMembers = null;
            mCurExpr = distSet;
        }

        public void visit(ConsSetRange obj)
        {
            ConsExpression expr1 = obj.getLow();
            ConsExpression expr2 = obj.getHigh();

            expr1.accept(this);
            expr1 = mCurExpr;
            expr2.accept(this);
            expr2 = mCurExpr;

            mSetMembers.add(new ConsSetRange(expr1, expr2));
            mCurExpr = null;
        }

        public void visit(ConsSetValue obj)
        {
            ConsExpression expr1 = obj.getValue();

            expr1.accept(this);
            expr1 = mCurExpr;

            mSetMembers.add(new ConsSetValue(expr1));
            mCurExpr = null;
        }

        public void visit(ConsImplication obj)
        {

            ConsExpression predicate = obj.getOperand(0);
            ConsExpression constraint = obj.getOperand(1);

            predicate.accept(this);
            predicate = mCurExpr;

            if (constraint instanceof ConsConstraintSet)
            {
                ConsConstraintSet constraintSet = new ConsConstraintSet(mSchema);
                Iterator iter = ((ConsConstraintSet) constraint).getExprs()
                    .iterator();
                while (iter.hasNext())
                {
                    ((ConsExpression) iter.next()).accept(this);
                    constraintSet.addExpr(mCurExpr);
                }
                constraint = constraintSet;
            }
            else
            {
                constraint.accept(this);
                constraint = mCurExpr;
            }

            mCurExpr = new ConsImplication(predicate, constraint);
        }

        // Logical operators
        public void visit(ConsConditionalAnd obj)
        {
            ConsExpression expr1 = obj.getOperand(0);
            ConsExpression expr2 = obj.getOperand(1);

            expr1.accept(this);
            expr1 = mCurExpr;
            expr2.accept(this);
            expr2 = mCurExpr;

            mCurExpr = new ConsConditionalAnd(expr1, expr2);
        }

        public void visit(ConsConditionalOr obj)
        {
            ConsExpression expr1 = obj.getOperand(0);
            ConsExpression expr2 = obj.getOperand(1);

            expr1.accept(this);
            expr1 = mCurExpr;
            expr2.accept(this);
            expr2 = mCurExpr;

            mCurExpr = new ConsConditionalOr(expr1, expr2);
        }

        public void visit(ConsLogicalNot obj)
        {
            ConsExpression expr1 = obj.getOperand(0);

            expr1.accept(this);
            expr1 = mCurExpr;

            mCurExpr = new ConsLogicalNot(expr1);
        }

        public void visit(ConsAnd obj)
        {
            ConsExpression expr1 = obj.getOperand(0);
            ConsExpression expr2 = obj.getOperand(1);

            expr1.accept(this);
            expr1 = mCurExpr;
            expr2.accept(this);
            expr2 = mCurExpr;

            mCurExpr = new ConsAnd(expr1, expr2);
        }

        public void visit(ConsOr obj)
        {
            ConsExpression expr1 = obj.getOperand(0);
            ConsExpression expr2 = obj.getOperand(1);

            expr1.accept(this);
            expr1 = mCurExpr;
            expr2.accept(this);
            expr2 = mCurExpr;

            mCurExpr = new ConsOr(expr1, expr2);
        }

        public void visit(ConsXor obj)
        {
            ConsExpression expr1 = obj.getOperand(0);
            ConsExpression expr2 = obj.getOperand(1);

            expr1.accept(this);
            expr1 = mCurExpr;
            expr2.accept(this);
            expr2 = mCurExpr;

            mCurExpr = new ConsXor(expr1, expr2);
        }

        public void visit(ConsBitwiseNot obj)
        {
            ConsExpression expr1 = obj.getOperand(0);

            expr1.accept(this);
            expr1 = mCurExpr;

            mCurExpr = new ConsBitwiseNot(expr1);
        }

        // Shift operators
        public void visit(ConsLeftShift obj)
        {
            ConsExpression expr1 = obj.getOperand(0);
            ConsExpression expr2 = obj.getOperand(1);

            expr1.accept(this);
            expr1 = mCurExpr;
            expr2.accept(this);
            expr2 = mCurExpr;

            mCurExpr = new ConsLeftShift(expr1, expr2);
        }

        public void visit(ConsSignedRightShift obj)
        {
            ConsExpression expr1 = obj.getOperand(0);
            ConsExpression expr2 = obj.getOperand(1);

            expr1.accept(this);
            expr1 = mCurExpr;
            expr2.accept(this);
            expr2 = mCurExpr;

            mCurExpr = new ConsSignedRightShift(expr1, expr2);
        }

        public void visit(ConsUnsignedRightShift obj)
        {
            ConsExpression expr1 = obj.getOperand(0);
            ConsExpression expr2 = obj.getOperand(1);

            expr1.accept(this);
            expr1 = mCurExpr;
            expr2.accept(this);
            expr2 = mCurExpr;

            mCurExpr = new ConsUnsignedRightShift(expr1, expr2);
        }

        // Comparisons
        public void visit(ConsEqual obj)
        {
            ConsExpression expr1 = obj.getOperand(0);
            ConsExpression expr2 = obj.getOperand(1);

            expr1.accept(this);
            expr1 = mCurExpr;
            expr2.accept(this);
            expr2 = mCurExpr;

            mCurExpr = new ConsEqual(expr1, expr2);
        }

        public void visit(ConsNotEqual obj)
        {
            ConsExpression expr1 = obj.getOperand(0);
            ConsExpression expr2 = obj.getOperand(1);

            expr1.accept(this);
            expr1 = mCurExpr;
            expr2.accept(this);
            expr2 = mCurExpr;

            mCurExpr = new ConsNotEqual(expr1, expr2);
        }

        public void visit(ConsGreater obj)
        {
            ConsExpression expr1 = obj.getOperand(0);
            ConsExpression expr2 = obj.getOperand(1);

            expr1.accept(this);
            expr1 = mCurExpr;
            expr2.accept(this);
            expr2 = mCurExpr;

            mCurExpr = new ConsGreater(expr1, expr2);
        }

        public void visit(ConsGreaterOrEqual obj)
        {
            ConsExpression expr1 = obj.getOperand(0);
            ConsExpression expr2 = obj.getOperand(1);

            expr1.accept(this);
            expr1 = mCurExpr;
            expr2.accept(this);
            expr2 = mCurExpr;

            mCurExpr = new ConsGreaterOrEqual(expr1, expr2);
        }

        public void visit(ConsLess obj)
        {
            ConsExpression expr1 = obj.getOperand(0);
            ConsExpression expr2 = obj.getOperand(1);

            expr1.accept(this);
            expr1 = mCurExpr;
            expr2.accept(this);
            expr2 = mCurExpr;

            mCurExpr = new ConsLess(expr1, expr2);
        }

        public void visit(ConsLessOrEqual obj)
        {
            ConsExpression expr1 = obj.getOperand(0);
            ConsExpression expr2 = obj.getOperand(1);

            expr1.accept(this);
            expr1 = mCurExpr;
            expr2.accept(this);
            expr2 = mCurExpr;

            mCurExpr = new ConsLessOrEqual(expr1, expr2);
        }

        // Arithmetic operators
        public void visit(ConsAdd obj)
        {
            ConsExpression expr1 = obj.getOperand(0);
            ConsExpression expr2 = obj.getOperand(1);

            expr1.accept(this);
            expr1 = mCurExpr;
            expr2.accept(this);
            expr2 = mCurExpr;

            mCurExpr = new ConsAdd(expr1, expr2);
        }

        public void visit(ConsSubtract obj)
        {
            ConsExpression expr1 = obj.getOperand(0);
            ConsExpression expr2 = obj.getOperand(1);

            expr1.accept(this);
            expr1 = mCurExpr;
            expr2.accept(this);
            expr2 = mCurExpr;

            mCurExpr = new ConsSubtract(expr1, expr2);
        }

        public void visit(ConsMultiply obj)
        {
            ConsExpression expr1 = obj.getOperand(0);
            ConsExpression expr2 = obj.getOperand(1);

            expr1.accept(this);
            expr1 = mCurExpr;
            expr2.accept(this);
            expr2 = mCurExpr;

            mCurExpr = new ConsMultiply(expr1, expr2);
        }

        public void visit(ConsDivide obj)
        {
            ConsExpression expr1 = obj.getOperand(0);
            ConsExpression expr2 = obj.getOperand(1);

            expr1.accept(this);
            expr1 = mCurExpr;
            expr2.accept(this);
            expr2 = mCurExpr;

            mCurExpr = new ConsDivide(expr1, expr2);
        }

        public void visit(ConsModulo obj)
        {
            ConsExpression expr1 = obj.getOperand(0);
            ConsExpression expr2 = obj.getOperand(1);

            expr1.accept(this);
            expr1 = mCurExpr;
            expr2.accept(this);
            expr2 = mCurExpr;

            mCurExpr = new ConsModulo(expr1, expr2);
        }

    }

    public ConsConstraint(ConsSchema schema, JavaName name)
    {
        super(schema);
        this.name = name;
        myVars = new RandVarSet();
    }

    public Name getName()
    {
        return name;
    }

    public JavaStructuredType getStructuredType()
    {
        return cls;
    }

    public void setClass(JavaClass cls)
    {
        this.cls = cls;
    }

    public JavaVisibility getVisibility()
    {
        return JavaVisibility.PUBLIC;
    }

    public List<ConsExpression> getExprs()
    {
        return exprs;
    }

    public void addExpr(ConsExpression expr)
    {
        exprs.add(expr);
    }

    public void addAllExpr(List<ConsExpression> list)
    {
        exprs.addAll(list);
    }

    public void setStructuredType(JavaStructuredType container)
    {
        cls = (JavaClass) container;
    }

    public void accept(ConsConstraintVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(JavaStructuredTypeMemberVisitor visitor)
    {
        accept((ConsConstraintVisitor) visitor);
    }

    public String toSourceString()
    {
        StringBuffer buf = new StringBuffer(200);
        buf.append("\nconstraint " + getName() + " ");
        ConsConstraintSet.printExprSet(buf, exprs);
        return buf.toString();
    }

    public RandVarSet getVarList()
    {
        return myVars;
    }

    public static ConsConstraint newInstance(ConsConstraint cc)
    {
        ConsConstraint newTree = new ConsConstraint(cc.schema, cc.name);
        ConstraintModifier modifier = new ConstraintModifier(newTree,
            Collections.EMPTY_MAP);
        cc.accept(modifier);

        return newTree;
    }

    public ConsConstraint replace(final Map replacementMap)
    {
        ConsConstraint newTree = new ConsConstraint(schema, name);
        ConstraintModifier modifier = new ConstraintModifier(newTree,
            replacementMap);
        this.accept(modifier);

        exprs = new LinkedList<ConsExpression>();
        exprs.addAll(newTree.getExprs());

        return this;
    }

    public String toDebugString()
    {
        return "constraint " + name;
    }
}
