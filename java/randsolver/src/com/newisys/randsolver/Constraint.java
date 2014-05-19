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

package com.newisys.randsolver;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.newisys.langschema.constraint.*;
import com.newisys.langschema.java.JavaBooleanType;
import com.newisys.langschema.java.JavaIntegralType;
import com.newisys.langschema.java.JavaName;
import com.newisys.langschema.java.JavaType;
import com.newisys.verilog.util.Bit;
import com.newisys.verilog.util.BitVector;

public final class Constraint
{
    private final Class mClass;
    private String mName = null;
    private final RandVarSet mVarSet; // RandVarSet of vars referenced in cons
    private ConsConstraint mConstraint;
    int mMaxWidth; // default access for use in inner class
    int mMinWidth; // default access for use in inner class

    Constraint()
    {
        mClass = null;
        mVarSet = new RandVarSet();
        mConstraint = new ConsConstraint(Solver.schema, new JavaName(
            "EMPTY_CONSTRAINT", null, null));
    }

    Constraint(Class klass, RandVarSet varSet, ConsConstraint constraint)
        throws InvalidConstraintException
    {
        mClass = klass;
        mVarSet = varSet;
        mConstraint = constraint;

        mMaxWidth = 0;
        mMinWidth = 0;

        analyzeConstraint();
    }

    Constraint(Constraint c)
    {
        this.mName = c.mName;
        this.mClass = c.mClass;
        this.mConstraint = ConsConstraint.newInstance(c.mConstraint);
        this.mMaxWidth = c.mMaxWidth;
        this.mMinWidth = c.mMinWidth;
        this.mVarSet = new RandVarSet();
        this.mVarSet.addAll(c.mVarSet);
    }

    public static Constraint newInstance(Constraint c)
    {
        return new Constraint(c);
    }

    public void analyzeConstraint()
    {
        class ConstraintAnalyzer
            extends AbsConsVisitor
        {
            LinkedList<ConsExpression> exprs;
            TreeMap<ConsVariableReference, ConsVariableReference> replacementMap;

            ConstraintAnalyzer(ConsConstraint c)
            {
                exprs = new LinkedList<ConsExpression>();
                replacementMap = new TreeMap<ConsVariableReference, ConsVariableReference>(
                    ConsVariableReferenceComparator.INSTANCE);
                c.accept(this);
            }

            public List<ConsExpression> getExprs()
            {
                return exprs;
            }

            public Map getReplacementMap()
            {
                return replacementMap;
            }

            public void visit(ConsBooleanLiteral literal)
            {
                debug(literal);
                mMaxWidth = Math.max(mMaxWidth, 1);
                mMinWidth = Math.max(mMinWidth, 1);
            }

            public void visit(ConsCharLiteral literal)
            {
                debug(literal);
                mMaxWidth = Math.max(mMaxWidth, Solver.schema.charType
                    .getWidth());
                mMinWidth = Math
                    .max(mMinWidth, getMinWidth(literal.getValue()));
            }

            public void visit(ConsIntLiteral literal)
            {
                debug(literal);
                mMaxWidth = Math.max(mMaxWidth, Solver.schema.intType
                    .getWidth());
                mMinWidth = Math
                    .max(mMinWidth, getMinWidth(literal.getValue()));
            }

            public void visit(ConsLongLiteral literal)
            {
                mMaxWidth = Math.max(mMaxWidth, Solver.schema.longType
                    .getWidth());
                mMinWidth = Math
                    .max(mMinWidth, getMinWidth(literal.getValue()));
            }

            public void visit(ConsBitVectorLiteral literal)
            {
                debug(literal);
                mMaxWidth = Math.max(mMaxWidth, literal.getValue().length());
                mMinWidth = Math
                    .max(mMinWidth, getMinWidth(literal.getValue()));
            }

            public void visit(ConsStringLiteral literal)
            {
                debug(literal);
                throw new InvalidConstraintException(
                    "Illegal to use a string in a constraint");
            }

            public void visit(ConsNullLiteral literal)
            {
                debug(literal);
                throw new InvalidConstraintException(
                    "Illegal to use null in a constraint");
            }

            public void visit(ConsDoubleLiteral literal)
            {
                debug(literal);
                throw new InvalidConstraintException(
                    "ConstraintDoubleLiteral is unsupported in constraints");
            }

            public void visit(ConsFloatLiteral literal)
            {
                debug(literal);
                throw new InvalidConstraintException(
                    "ConstraintFloatLiteral is unsupported in constraints");
            }

            public void visit(ConsTypeLiteral literal)
            {
                debug(literal);
                throw new InvalidConstraintException(
                    "Illegal to use type in contraint");
            }

            public void visit(ConsVariableReference var)
            {
                debug(var);

                int numBits = 0;
                JavaType type = var.getVariable().getType();
                if (type instanceof JavaBooleanType)
                {
                    numBits = 1;
                    mMaxWidth = Math.max(mMaxWidth, numBits);
                    mMinWidth = Math.max(mMinWidth, numBits);
                }
                else if (type instanceof ConsRandomizableType)
                {
                    ConsRandomizableType enumType = (ConsRandomizableType) type;
                    Constraint c = RandomMapperRegistry.getMapper(
                        enumType.getEnumClass()).getConstraint();

                    // TODO this replacement also occurs in
                    // ComplexExprSolver.java and
                    // should be centralized.
                    exprs.addAll(c.getSchemaConstraint().getExprs());
                    RandVarSet rvs = c.getVarSet();
                    assert (rvs.size() == 1);
                    ConsVariableReference varref = rvs.get(0).getVarRef();
                    replacementMap.put(varref, var);
                    mMinWidth = Math.max(mMinWidth, c.getMinWidth());
                    mMaxWidth = Math.max(mMaxWidth, mMinWidth);
                }
                else if (type instanceof JavaIntegralType)
                {
                    numBits = ((JavaIntegralType) (var.getVariable().getType()))
                        .getWidth();
                    mMaxWidth = Math.max(mMaxWidth, numBits);
                    mMinWidth = Math.max(mMinWidth, numBits);
                }
                else if (Solver.schema.isBitVector(type))
                {
                    //System.err.println("Unknown information for BitVector
                    // base class");
                }
                else
                {
                    throw new InvalidConstraintException(
                        "Non-integral types are not supported");
                }

            }
        }

        ConstraintAnalyzer analyzer = new ConstraintAnalyzer(mConstraint);
        mConstraint.addAllExpr(analyzer.getExprs());
        mConstraint = mConstraint.replace(analyzer.getReplacementMap());

    }

    public ConsConstraint getSchemaConstraint()
    {
        return mConstraint;
    }

    public Class getConstrainedClass()
    {
        return mClass;
    }

    public RandVarSet getVarSet()
    {
        return mVarSet;
    }

    public void and(Constraint c)
    {
        mConstraint.addAllExpr(c.mConstraint.getExprs());
        mVarSet.addAll(c.getVarSet());
    }

    public void setName(String name)
    {
        mName = name;
    }

    public String getName()
    {
        return mName;
    }

    public String toString()
    {
        return mName + ": [" + mConstraint + "] " + getMinWidth()
            + " bits; Vars: " + mVarSet;
    }

    public int hashCode()
    {
        // TODO: implement hashCode properly
        return toString().hashCode();
    }

    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (!(o instanceof Constraint)) return false;

        // TODO: implement equals properly
        return toString().equals(((Constraint) o).toString());
    }

    public int getBitVectorSize()
    {
        return mMaxWidth;
    }

    public int getMinWidth()
    {
        return mMinWidth;
    }

    // default access for use by inner class
    int getMinWidth(long l)
    {
        int highestBit = 0;
        for (int i = 0; i < 64; i++)
        {
            if ((l & 1L) != 0L)
            {
                highestBit = i;
            }
            l >>>= 1;
        }
        return highestBit + 1;
    }

    // default access for use by inner class
    int getMinWidth(BitVector v)
    {
        int highestBit = 0;
        int length = v.length();
        for (int i = 0; i < length; i++)
        {
            if (v.getBit(i) == Bit.ONE)
            {
                highestBit = i;
            }
        }

        return highestBit + 1;
    }

}
