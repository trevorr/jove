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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.sf.javabdd.BDD;
import org.sf.javabdd.BDDBitVector;
import org.sf.javabdd.BDDExpr;

import com.newisys.langschema.constraint.*;
import com.newisys.langschema.java.JavaIntegralType;
import com.newisys.random.PRNG;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.BitVectorBuffer;

// TODO: operator: foreach
// TODO: operator: dist
// TODO: performance, performance, performance
// TODO: this/super references
// TODO: array randomization
// TODO: bit slicing

public class ComplexExprSolver
    extends ExprSolver
{
    private class ConstraintEvaluator
        extends AbsConsVisitor
    {
        private BDDExpr returnVal = null;
        private List<ConsExpression> implicitConstraints = new LinkedList<ConsExpression>();

        public BDD getReturnVal(ConsExpression _exprToEval)
        {
            _exprToEval.accept(this);

            if (!(returnVal instanceof BDD))
            {
                // if we get back a BDDBitVector, there's an implicit
                // vector != 0 constraint
                BDDExpr rhs = internalEval(new ConsIntLiteral(Solver.schema, 0));
                returnVal = BddUtils.bddNeq((BDDBitVector) returnVal,
                    (BDDBitVector) rhs);
            }

            BDD finalReturnVal = (BDD) returnVal;
            returnVal = null;

            // some constraints might generate implicit constraints that must
            // be evaluated. continue doing that until no more implicit
            // constraints
            // are generated.
            while (!implicitConstraints.isEmpty())
            {
                List<ConsExpression> implicits = new LinkedList<ConsExpression>(
                    implicitConstraints);
                implicitConstraints.clear();
                for (Iterator iter = implicits.iterator(); iter.hasNext();)
                {
                    ConsExpression implicitExpr = (ConsExpression) iter.next();
                    BDD implicitBdd = getReturnVal(implicitExpr);
                    finalReturnVal.andWith(implicitBdd);
                }
            }

            return finalReturnVal;
        }

        private BDDBitVector toBDDBitVector(BDDExpr expr)
        {
            if (expr instanceof BDDBitVector)
            {
                return (BDDBitVector) expr;
            }
            assert (expr instanceof BDD);
            BDDBitVector vect = BddUtils.getFactory().constantVector(1, 0);
            vect.setBit(0, (BDD) expr);
            return vect;
        }

        private BDD toBDD(BDDExpr expr)
        {
            if (expr instanceof BDD)
            {
                return (BDD) expr;
            }
            assert (expr instanceof BDDBitVector);
            BDDBitVector vect = (BDDBitVector) expr;
            BDD tmp = BddUtils.getFactory().zero();
            for (int i = 0; i < vect.size(); ++i)
            {
                tmp = tmp.or(vect.getBit(i));
            }
            return tmp;
        }

        private BDDExpr internalGetReturnVal(ConsExpression _exprToEval)
        {
            _exprToEval.accept(this);
            return returnVal;
        }

        private BDDExpr internalEval(ConsExpression expr)
        {
            ConstraintEvaluator evaluator = new ConstraintEvaluator();
            BDDExpr result = evaluator.internalGetReturnVal(expr);

            // add any implicit constraints from this evaluation to the global
            // list
            implicitConstraints.addAll(evaluator.implicitConstraints);

            return result;
        }

        private void handleShiftRHS(
            ConsShiftOperation _expr,
            BDDBitVector _bddRhs)
        {
            ConsExpression exprRhs = _expr.getOperand(1);

            // check if RHS is negative. if so, throw an exception
            if (_bddRhs.isNegative())
            {
                throw new InvalidConstraintException(
                    "Illegal negative shift count in constraint:" + _expr);
            }

            // implicit constraint: rhs >= 0
            // the if(!isConst()) is just an optimization to avoid evaluating
            // constant constraints like: 10 >= 0. if the rhs is constant and
            // negative, we'll throw an exception above.
            if (!_bddRhs.isConst())
            {
                ConsExpression rhsConstraint = new ConsGreaterOrEqual(exprRhs,
                    new ConsIntLiteral(Solver.schema, 0));
                implicitConstraints.add(rhsConstraint);
            }
        }

        // Variables
        @Override
        public void visit(ConsVariableReference decl)
        {
            returnVal = getBddBitVector(decl);
        }

        // Literal values
        @Override
        public void visit(ConsBooleanLiteral decl)
        {
            int value = (decl.getValue() ? 1 : 0);
            int width = 1;
            BDDBitVector v = BddUtils.getFactory().constantVector(width, value);
            v.setSigned(false);
            returnVal = v;
        }

        @Override
        public void visit(ConsBitVectorLiteral decl)
        {
            BitVector value = decl.getValue();
            BDDBitVector v = BddUtils.constantVector(value);
            // assume unsigned bitvectors
            // FIXME: add signed bit vectors when BitVector adds support
            v.setSigned(false);
            returnVal = v;
        }

        @Override
        public void visit(ConsCharLiteral decl)
        {
            int width = Solver.schema.charType.getWidth();
            char value = decl.getValue();
            BDDBitVector v = BddUtils.getFactory().constantVector(width, value);
            v.setSigned(Solver.schema.charType.isSigned());
            returnVal = v;
        }

        @Override
        public void visit(ConsDoubleLiteral decl)
        {
            // TBD: support doubles in ConstraintEvaluator
            throw new RuntimeException("Doubles aren't implemented yet: "
                + decl);
        }

        @Override
        public void visit(ConsFloatLiteral decl)
        {
            // TBD: support floats in ConstraintEvaluator
            throw new RuntimeException("Floats aren't implemented yet: " + decl);
        }

        @Override
        public void visit(ConsIntLiteral decl)
        {
            int width = Solver.schema.intType.getWidth();
            int value = decl.getValue();
            BDDBitVector v = BddUtils.getFactory().constantVector(width, value);
            v.setSigned(Solver.schema.intType.isSigned());
            returnVal = v;
        }

        @Override
        public void visit(ConsLongLiteral decl)
        {
            int width = Solver.schema.longType.getWidth();
            long value = decl.getValue();
            BDDBitVector v = BddUtils.getFactory().constantVector(width, value);
            v.setSigned(Solver.schema.longType.isSigned());
            returnVal = v;
        }

        // Set operations
        @Override
        public void visit(ConsInSet expr)
        {
            // convert
            // y in { 2, 5:10 }
            // into:
            // (y == 2) || (y >= 5 && y <= 10)
            // we'll probably do this in the ConstraintParser, honestly.

            ConsExpression cExpr = null;
            BDD orBDD = BddUtils.getFactory().zero();

            assert (expr.getMembers().size() > 0);
            Iterator iter = expr.getMembers().iterator();
            while (iter.hasNext())
            {
                ConsSetMember member = (ConsSetMember) iter.next();
                if (member instanceof ConsSetValue)
                {
                    cExpr = new ConsEqual(expr.getExpr(),
                        ((ConsSetValue) member).getValue());
                }
                else if (member instanceof ConsSetRange)
                {
                    ConsExpression loExpr;
                    ConsExpression hiExpr;
                    loExpr = new ConsGreaterOrEqual(expr.getExpr(),
                        ((ConsSetRange) member).getLow());
                    hiExpr = new ConsLessOrEqual(expr.getExpr(),
                        ((ConsSetRange) member).getHigh());
                    cExpr = new ConsConditionalAnd(loExpr, hiExpr);
                }
                else
                {
                    throw new InvalidConstraintException(
                        "Unknown ConsSetMember subclass: " + member);
                }

                BDDExpr cBDD = internalEval(cExpr);
                assert (cBDD instanceof BDD);
                orBDD = orBDD.or((BDD) cBDD);
            }

            returnVal = orBDD;
            if (expr instanceof ConsNotInSet)
            {
                returnVal = ((BDD) returnVal).not();
            }
        }

        @Override
        public void visit(ConsDistSet expr)
        {
            // dist expressions
            // 1. dictate range
            // 2. affect randwalk

            throw new InvalidConstraintException("dist is not yet supported: "
                + expr);
        }

        @Override
        public void visit(ConsImplication expr)
        {
            // in a bit context, implication is true if either
            // a) the LHS evaluates to false
            // b) the RHS evaluates to true
            BDD lhs = toBDD(internalEval((ConsExpression) expr.getOperands()
                .get(0)));

            BDD tmp = BddUtils.getFactory().one();
            for (int i = 1; i < expr.getOperands().size(); ++i)
            {
                BDD rhs = toBDD(internalEval((ConsExpression) expr
                    .getOperands().get(i)));
                tmp = tmp.and(lhs.imp(rhs));
            }
            returnVal = tmp;
        }

        // Logical operators
        @Override
        public void visit(ConsConditionalAnd expr)
        {
            BDD lhs = toBDD(internalEval((ConsExpression) expr.getOperands()
                .get(0)));
            BDD rhs = toBDD(internalEval((ConsExpression) expr.getOperands()
                .get(1)));
            returnVal = lhs.and(rhs);
        }

        @Override
        public void visit(ConsConditionalOr expr)
        {
            BDD lhs = toBDD(internalEval((ConsExpression) expr.getOperands()
                .get(0)));
            BDD rhs = toBDD(internalEval((ConsExpression) expr.getOperands()
                .get(1)));
            returnVal = lhs.or(rhs);
        }

        @Override
        public void visit(ConsLogicalNot expr)
        {
            BDD lhs = toBDD(internalEval((ConsExpression) expr.getOperands()
                .get(0)));
            returnVal = (lhs).not();
        }

        // Bitwise operators
        @Override
        public void visit(ConsAnd expr)
        {
            BDDBitVector lhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(0)));
            BDDBitVector rhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(1)));

            returnVal = BddUtils.bddBitwiseAnd(lhs, rhs);
        }

        @Override
        public void visit(ConsOr expr)
        {
            BDDBitVector lhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(0)));
            BDDBitVector rhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(1)));
            returnVal = BddUtils.bddBitwiseOr(lhs, rhs);
        }

        @Override
        public void visit(ConsXor expr)
        {
            BDDBitVector lhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(0)));
            BDDBitVector rhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(1)));

            returnVal = BddUtils.bddBitwiseXor(lhs, rhs);
        }

        @Override
        public void visit(ConsBitwiseNot expr)
        {
            BDDBitVector lhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(0)));

            returnVal = BddUtils.bddBitwiseNot(lhs);
        }

        // Shift operators
        @Override
        public void visit(ConsLeftShift expr)
        {
            BDDBitVector lhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(0)));
            BDDBitVector rhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(1)));

            handleShiftRHS(expr, rhs);
            returnVal = BddUtils.bddShiftLeft(lhs, rhs);
        }

        @Override
        public void visit(ConsSignedRightShift expr)
        {
            BDDBitVector lhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(0)));
            BDDBitVector rhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(1)));

            handleShiftRHS(expr, rhs);
            returnVal = BddUtils.bddShiftRight(lhs, rhs, true);
        }

        @Override
        public void visit(ConsUnsignedRightShift expr)
        {
            BDDBitVector lhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(0)));
            BDDBitVector rhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(1)));

            handleShiftRHS(expr, rhs);
            returnVal = BddUtils.bddShiftRight(lhs, rhs, false);
        }

        // Comparisons
        @Override
        public void visit(ConsEqual expr)
        {
            BDDBitVector lhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(0)));
            BDDBitVector rhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(1)));

            returnVal = BddUtils.bddEq(lhs, rhs);
        }

        @Override
        public void visit(ConsNotEqual expr)
        {
            BDDBitVector lhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(0)));
            BDDBitVector rhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(1)));

            returnVal = BddUtils.bddNeq(lhs, rhs);
        }

        @Override
        public void visit(ConsGreater expr)
        {
            BDDBitVector lhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(0)));
            BDDBitVector rhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(1)));

            returnVal = BddUtils.bddGthan(lhs, rhs);
        }

        @Override
        public void visit(ConsGreaterOrEqual expr)
        {
            BDDBitVector lhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(0)));
            BDDBitVector rhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(1)));

            returnVal = BddUtils.bddGeq(lhs, rhs);
        }

        @Override
        public void visit(ConsLess expr)
        {
            BDDBitVector lhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(0)));
            BDDBitVector rhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(1)));

            returnVal = BddUtils.bddLthan(lhs, rhs);
        }

        @Override
        public void visit(ConsLessOrEqual expr)
        {
            BDDBitVector lhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(0)));
            BDDBitVector rhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(1)));

            returnVal = BddUtils.bddLeq(lhs, rhs);
        }

        // Arithmetic operators
        @Override
        public void visit(ConsAdd expr)
        {
            BDDBitVector lhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(0)));
            BDDBitVector rhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(1)));

            returnVal = BddUtils.bddPlus(lhs, rhs, mMaxConstraintBits);
        }

        @Override
        public void visit(ConsSubtract expr)
        {
            BDDBitVector lhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(0)));
            BDDBitVector rhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(1)));

            returnVal = BddUtils.bddMinus(lhs, rhs, mMaxConstraintBits);
        }

        @Override
        public void visit(ConsMultiply expr)
        {
            BDDBitVector lhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(0)));
            BDDBitVector rhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(1)));

            returnVal = BddUtils.bddMultiply(lhs, rhs, mMaxConstraintBits);
        }

        @Override
        public void visit(ConsDivide expr)
        {
            BDDBitVector lhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(0)));
            BDDBitVector rhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(1)));

            returnVal = BddUtils.bddDivide(lhs, rhs, mMaxConstraintBits);
        }

        @Override
        public void visit(ConsModulo expr)
        {
            BDDBitVector lhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(0)));
            BDDBitVector rhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(1)));

            returnVal = BddUtils.bddModulo(lhs, rhs, mMaxConstraintBits);
        }

        @Override
        public void visit(ConsUnaryMinus expr)
        {
            assert (expr.getOperands().size() == 1);
            BDDBitVector lhs = toBDDBitVector(internalEval((ConsExpression) expr
                .getOperands().get(0)));
            returnVal = BddUtils.bddTwosCompliment(lhs);
        }

        @Override
        public void visit(ConsUnaryPlus expr)
        {
            assert (expr.getOperands().size() == 1);
            // nothing to do here, leave returnVal unchanged
        }
    }

    public final Map<RandomVariable, BitVector> unrandomizedVarMap = new HashMap<RandomVariable, BitVector>();
    private Map<RandomVariable, BDDBitVector> mDomainVarMap = new HashMap<RandomVariable, BDDBitVector>();
    private int[] mSolnOffsets = null; // determined in solve()
    private int[] mSolnLengths = null; // determined in solve()
    int mMaxConstraintBits = 0;
    BDD mSolution;

    public ComplexExprSolver(String name)
    {
        super(name);
    }

    @Override
    public final boolean isComplex()
    {
        return true;
    }

    @Override
    protected final List solve(
        Constraint constraint,
        RandVarSet allVars,
        RandVarSet unconstrainedVars)
    {

        List<RandomVariable> cyclicList = new LinkedList<RandomVariable>();
        mVars = new RandVarSet();
        mVars.addAll(allVars);
        mVars.removeRandomizables();
        mSolnLengths = new int[mVars.size()];
        mSolnOffsets = new int[mVars.size()];

        BDD solution = BddUtils.getFactory().one();
        ConsConstraint schemaConstraint = constraint.getSchemaConstraint();
        mMaxConstraintBits = constraint.getBitVectorSize();

        initializeBDDVectors(mVars);

        // add unconstrained variables
        Iterator ucIter = unconstrainedVars.iterator();
        while (ucIter.hasNext())
        {
            RandomVariable var = (RandomVariable) ucIter.next();
            ConsVariableReference ref = var.getVarRef();
            getBddBitVector(ref);
            // we may have unconstrained variables with innate constraints (e.g.
            // enums)
            RandomMapper mapper = RandomMapperRegistry.getMapper(var
                .getClassType());
            if (mapper != null)
            {
                ConsConstraint c = mapper.getConstraint().getSchemaConstraint();

                // TODO this replacement also occurs in Constraint.java and
                // should be centralized.
                RandVarSet rvs = mapper.getConstraint().getVarSet();
                assert (rvs.size() == 1);
                ConsVariableReference varref = rvs.get(0).getVarRef();
                Map<ConsVariableReference, ConsVariableReference> ucVarMap = new TreeMap<ConsVariableReference, ConsVariableReference>(
                    ConsVariableReferenceComparator.INSTANCE);
                ucVarMap.put(varref, ref);
                c.replace(ucVarMap);
                schemaConstraint.addAllExpr(c.getExprs());
                mMaxConstraintBits = Math.max(mMaxConstraintBits, mapper
                    .getConstraint().getBitVectorSize());
            }
        }

        // now evaluate the constraints
        Iterator cIter = schemaConstraint.getExprs().iterator();
        while (cIter.hasNext())
        {
            ConsExpression expr = (ConsExpression) cIter.next();
            BDD soln = eval(expr);
            //soln.printDot();
            solution = solution.and(soln);
        }

        // Add cyclic constraints
        Iterator vIter = mVars.iterator();
        while (vIter.hasNext())
        {
            RandomVariable rv = (RandomVariable) vIter.next();
            if (rv.getMode() == RandVarMode.CYCLIC)
            {
                BDD oldSolution = solution;
                solution = solution.and(rv.getCyclicConstraint());

                // if adding this cyclic constraint causes the solution to
                // become unsolvable, reset the period of this variable
                if (solution.calcWeights() == 0.0)
                {
                    solution = oldSolution;
                    rv.addCyclicConstraint(BddUtils.getFactory().zero());
                    solution = solution.and(rv.getCyclicConstraint());
                    cyclicList.add(rv);
                }
            }
        }

        // solution.printDot();
        mSolution = solution;
        if (mSolution.calcWeights() == 0.0)
        {
            throw new UnsolvableConstraintException("No possible variable "
                + "assignments can satisfy the constraints given");
        }

        return cyclicList;
    }

    private BDD eval(ConsExpression _exprToEval)
    {
        ConstraintEvaluator evaluator = new ConstraintEvaluator();
        return evaluator.getReturnVal(_exprToEval);
    }

    static double getProbOfBitBeingOne(int bit, int lo, int hi)
    {
        int length = hi - lo + 1;
        assert(length > 0);
        int period = (int) Math.pow(2, bit + 1);
        int pdiv2 = (int) Math.pow(2, bit);
        int partial_start_bits = (period > lo) ? Math.min(period - lo, length)
            : (lo % period);
        int partial_end_bits = (length - partial_start_bits) % period;
        int num_total_periods = (length - partial_start_bits - partial_end_bits)
            / period;

        int num_zeros = (num_total_periods * pdiv2)
            + Math.min(partial_end_bits, pdiv2)
            + ((partial_start_bits != length) ? Math.max(partial_start_bits
                - pdiv2, 0) : (pdiv2 < lo) ? 0 : (Math.pow(2, bit) >= hi)
                ? length : length - (hi - (pdiv2 - 1)));
        int num_ones = length - num_zeros;

        //System.out.println("p1(" + bit + "): " + lo + ":" + hi + " = " +
        // num_ones + "/" + length);
        return (double) num_ones / (double) length;
    }

    void initializeBDDVectors(RandVarSet set)
    {
        int numBits = 0;
        int numVars = 0;
        Iterator iter = null;

        // Run through and get some information about all of the variables
        iter = set.iterator();
        while (iter.hasNext())
        {
            ++numVars;
            RandomVariable rv = (RandomVariable) iter.next();
            mSolnLengths[numVars - 1] = rv.getNumBits();
            numBits += mSolnLengths[numVars - 1];
        }

        // System.out.println("Solution is " + numBits + " bits long spanning "
        //    + numVars + " variables");
        assert (numBits > 0);

        BddUtils.getFactory().setVarNum(
            Math.max(BddUtils.getFactory().varNum(), numBits));
        int baseOffset = 0;

        // Now build the vectors and store the indices
        int curVar = -1;
        iter = set.iterator();
        while (iter.hasNext())
        {
            ++curVar;
            RandomVariable rv = (RandomVariable) iter.next();
            assert (!mDomainVarMap.containsKey(rv));
            int idx = set.getIndex(rv);
            int length = rv.getNumBits();
            int[] bddIdxs = new int[length];
            BDDBitVector vect = null;

            //            System.out.println("RANDVAR[" + length + "]: " + rv);
            int lastIdx = baseOffset + curVar;
            for (int i = 0; i < length; ++i)
            {
                int varsToSkip = 0;
                for (int k = 0; k < mSolnLengths.length; ++k)
                {
                    if (i + 1 < mSolnLengths[k]
                        || (i + 1 == mSolnLengths[k] && k >= curVar))
                    {
                        ++varsToSkip;
                    }
                }

                //                System.out.println("getBit: " + (lastIdx));
                bddIdxs[i] = BddUtils.getFactory().ithVar(lastIdx).var();
                lastIdx += varsToSkip;
            }

            assert (idx < mSolnOffsets.length);
            mSolnOffsets[idx] = baseOffset;

            vect = BddUtils.getFactory().buildVector(bddIdxs);
            if (rv.getVarRef().getResultType() instanceof JavaIntegralType)
            {
                JavaIntegralType type = (JavaIntegralType) rv.getVarRef()
                    .getResultType();
                vect.setSigned(type.isSigned());
            }
            else
            {
                vect.setSigned(false);
            }

            // this statement causes all BDDBitVectors to be sign-extended
            // instead of zero-extended. in practice, this causes constraints
            // like:
            // rand bit[7:0] foo;
            // foo == -2;
            // to be solvable, with foo being assigned the two's complement
            // of -2 (ie. 0xFE).
            //
            // The previous method would look at rv.getVarRef().getResultType()
            // and check if it was a signed type. This results in the above
            // constraint being unsolvable if foo < 31 bits and solvable if
            // foo >= 32 bits. In the latter case, foo is assigned the two's
            // complement representation of -2 (ie. 0xFFFFFFFE).
            //vect.setSigned(true);

            mDomainVarMap.put(rv, vect);
        }

    }

    private BitVector literalToBitVector(ConsExpression literal)
    {
        if (literal instanceof ConsBooleanLiteral)
        {
            return new BitVector(1, ((ConsBooleanLiteral) literal).getValue()
                ? 1 : 0);
        }
        else if (literal instanceof ConsCharLiteral)
        {
            return new BitVector(8, ((ConsCharLiteral) literal).getValue());
        }
        else if (literal instanceof ConsIntLiteral)
        {
            return new BitVector(32, ((ConsIntLiteral) literal).getValue());
        }
        else if (literal instanceof ConsLongLiteral)
        {
            return new BitVector(64, ((ConsLongLiteral) literal).getValue());
        }
        else if (literal instanceof ConsBitVectorLiteral)
        {
            return ((ConsBitVectorLiteral) literal).getValue();
        }
        else
        {
            throw new InvalidConstraintException(
                "Expected integral literal type: "
                    + literal.getClass().getName());
        }
    }

    @Override
    public boolean needsReevaluation(RandVarSet varsToCheck, Object obj)
    {
        boolean reeval = false;
        Iterator iter = varsToCheck.iterator();
        while (iter.hasNext())
        {
            RandomVariable rv = (RandomVariable) iter.next();
            Field f = rv.getField();
            BitVector literal = null;
            try
            {
                literal = literalToBitVector(NameParser.getExpr(f.getName(), f
                    .getDeclaringClass(), null, rv.getObjRef(obj)));
            }
            catch (NoSuchFieldException e)
            {
                throw new RuntimeException(e);
            }
            catch (IllegalAccessException e1)
            {
                throw new RuntimeException(e1);
            }

            if (unrandomizedVarMap.containsKey(rv))
            {
                BitVector literal2 = unrandomizedVarMap.get(rv);
                //    System.out.println("l1: " + literal + ", l2: " + literal2);
                reeval |= !literal2.equals(literal);
            }
            else
            {
                unrandomizedVarMap.put(rv, literal);
                reeval |= true;
            }
        }
        // check if any of the varsToCheck has changed since the last time
        return reeval;
    }

    BDDBitVector getBddBitVector(ConsVariableReference var)
    {
        BDDBitVector vect;
        int idx = mVars.getIndex(var);
        RandomVariable rv = mVars.get(idx);
        vect = mDomainVarMap.get(rv);
        assert (vect != null);
        return vect;
    }

    // throws InvalidConstraintException if an IllegalAccessException occurs
    // while committing the solution.
    @Override
    protected Map commit(Object obj, PRNG rng)
    {
        Map<RandomVariable, BDD> cyclicMap = new HashMap<RandomVariable, BDD>();

        BitVector randWalk = BddUtils.randWalk(rng, mSolution);
        BitVector[] solutions = new BitVector[mVars.size()];

        // System.out.println("randWalk: " + randWalk);
        for (int soln = 0; soln < solutions.length; soln++)
        {
            RandomVariable rv = mVars.get(soln);

            BitVectorBuffer vbuf = new BitVectorBuffer(mSolnLengths[soln]);
            int lastIdx = mSolnOffsets[soln] + soln;
            for (int bit = 0; bit < mSolnLengths[soln]; ++bit)
            {
                int varsToSkip = 0;
                for (int k = 0; k < mSolnLengths.length; ++k)
                {
                    if (bit + 1 < mSolnLengths[k]
                        || (bit + 1 == mSolnLengths[k] && k >= soln))
                    {
                        ++varsToSkip;
                    }
                }
                vbuf.setBit(bit, randWalk.getBit(lastIdx));
                lastIdx += varsToSkip;
            }
            solutions[soln] = vbuf.toBitVector();

            // Handle cyclic random variables, adding a new constraint
            if (rv.getMode() == RandVarMode.CYCLIC)
            {
                ConsVariableReference varRef = rv.getVarRef();
                assert (varRef != null);
                ConsExpression neq = new ConsNotEqual(varRef,
                    new ConsBitVectorLiteral(Solver.schema, solutions[soln]));
                cyclicMap.put(rv, eval(neq));
            }

            //System.out.println("solutions[" + soln + "]: " + solutions[soln]);
        }

        Field f = null;
        try
        {
            for (int i = 0; i < solutions.length; i++)
            {
                RandomVariable rv = mVars.get(i);

                // field has already been setAccessible().
                f = rv.getField();
                Class dataType = rv.getClassType();

                Object commitObj = rv.getObjRef(obj);

                if (dataType == boolean.class || dataType == Boolean.class)
                {
                    int value = solutions[i].intValue();
                    assert (value == 0 || value == 1);
                    boolean b = ((value == 1) ? true : false);

                    if (dataType == boolean.class)
                    {
                        f.setBoolean(commitObj, b);
                    }
                    else
                    {
                        f.set(commitObj, new Boolean(b));
                    }
                }
                else if (dataType == byte.class || dataType == Byte.class)
                {
                    int value = solutions[i].intValue();
                    assert (value >= 0 && value < 0x100);
                    byte b = (byte) value;

                    if (dataType == byte.class)
                    {
                        f.setByte(commitObj, b);
                    }
                    else
                    {
                        f.set(commitObj, new Byte(b));
                    }
                }
                else if (dataType == char.class)
                {
                    int value = solutions[i].intValue();
                    assert (value >= 0 && value < 0x100);
                    char c = (char) value;

                    // There is no "Char" class
                    f.setChar(commitObj, c);
                }
                else if (dataType == int.class || dataType == Integer.class)
                {
                    int value = solutions[i].intValue();
                    if (dataType == int.class)
                    {
                        f.setInt(commitObj, value);
                    }
                    else
                    {
                        f.set(commitObj, new Integer(value));
                    }
                }
                else if (dataType == long.class || dataType == Long.class)
                {
                    long value = solutions[i].longValue();

                    if (dataType == long.class)
                    {
                        f.setLong(commitObj, value);
                    }
                    else
                    {
                        f.set(commitObj, new Long(value));
                    }
                }
                else if (dataType == short.class || dataType == Short.class)
                {
                    int value = solutions[i].intValue();
                    assert (value >= 0 && value <= 0x10000);
                    short s = (short) value;

                    if (dataType == short.class)
                    {
                        f.setShort(commitObj, s);
                    }
                    else
                    {
                        f.set(commitObj, new Short(s));
                    }
                }
                else if (dataType == BitVector.class)
                {
                    f.set(commitObj, solutions[i]);
                }
                // NOTE: no special case for Enums. This is because they should
                // have an entry in the RandomMapperRegistry.
                else
                {
                    RandomMapper cp = RandomMapperRegistry.getMapper(dataType);
                    assert (cp != null);
                    int value = solutions[i].intValue();
                    f.set(commitObj, cp.getObject(value));
                }
            }
        }
        catch (IllegalAccessException e)
        {
            throw new InvalidConstraintException(e.getMessage() + ": " + f);
        }

        return cyclicMap;
    }
}
