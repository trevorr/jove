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

import org.sf.javabdd.BDD;
import org.sf.javabdd.BDDBitVector;
import org.sf.javabdd.BDDFactory;
import org.sf.javabdd.JavaFactory;
import org.sf.javabdd.BDDFactory.BDDOp;

import com.newisys.random.PRNG;
import com.newisys.verilog.util.Bit;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.BitVectorBuffer;
import com.newisys.verilog.util.XZException;

public final class BddUtils
{
    private static BDDFactory myBddFactory;
    private static BddUtils myself = null;

    private BddUtils()
    {
        myBddFactory = JavaFactory.init(100000, 100000);
    }

    private static void initializeUtils()
    {
        if (myself == null)
        {
            myself = new BddUtils();
        }
    }

    public static BDDFactory getFactory()
    {
        initializeUtils();
        return BddUtils.myBddFactory;
    }

    /**
     * Returns either one() or zero() depending on the boolean parameter.
     *
     * @param b <code>true</code> if the TRUE BDD should be returned, <code>false</code>
     *      if the FALSE BDD should be returned.
     * @return the TRUE or FALSE BDD, according to <code>b</code>
     */
    public static BDD getBDDForBoolean(boolean b)
    {
        return b ? getFactory().one() : getFactory().zero();
    }

    ////////////////////////////////////////////
    // boolean BDD functions
    ////////////////////////////////////////////
    // Relational operators
    public static BDD bddEq(BDDBitVector v1, BDDBitVector v2)
    {
        int size = bddMakeEqualLengths(v1, v2);

        boolean checkForNegative = v1.isSigned() ^ v2.isSigned();
        // check the case where one of the vectors is signed and one isn't.
        // the signed vector cannot be negative if the two are equals, so return
        // false if one of the vectors is negative.
        if (checkForNegative)
        {
            if (v1.isNegative() || v2.isNegative())
            {
                return getFactory().zero();
            }
        }

        BDD tmp = myBddFactory.one();

        for (int i = 0; i < size; ++i)
        {
            BDD b1 = v1.getBit(i);
            BDD b2 = v2.getBit(i);
            tmp = tmp.and((b1.xor(b2)).not());
        }

        return tmp;
    }

    public static BDD bddNeq(BDDBitVector v1, BDDBitVector v2)
    {
        BDD tmp = bddEq(v1, v2);
        return tmp.not();
    }

    public static BDD bddLeq(BDDBitVector v1, BDDBitVector v2)
    {
        int size = bddMakeEqualLengths(v1, v2);

        // check if we're comparing a signed vector with an unsigned vector
        if (v1.isSigned() ^ v2.isSigned())
        {
            if (v1.isNegative())
            {
                assert (!v2.isSigned());
                // [NEGATIVE] <= [UNSIGNED] is always true
                return getFactory().one();
            }
            else if (v2.isNegative())
            {
                assert (!v1.isSigned());
                // [UNSIGNED] <= [NEGATIVE] is always false
                return getFactory().zero();
            }
        }

        // At this point, one of the following is true:
        // 1. both vectors are signed and v1 is negative and v2 is positive
        // 2. both vectors are signed and v1 is positive and v2 is negative
        // 3. both vectors are signed and are positive
        // 4. both vectors are signed and are negative
        // 5. both vectors are unsigned
        // 6. the signed/unsigned case, but the signed vector isn't negative

        int msb = size - 1;

        BDD lEQ = myBddFactory.one();
        for (int i = 0; i <= msb; i++)
        {
            BDD b1 = v1.getBit(i);
            BDD b2 = v2.getBit(i);
            BDD tmp1 = (lEQ.and((b1.xor(b2)).not())); // test for ==
            BDD tmp2 = (b1.not()).and(b2); // test for <
            lEQ = tmp1.or(tmp2);
        }

        BDD gEQ = myBddFactory.one();
        for (int i = 0; i <= msb; i++)
        {
            BDD b1 = v1.getBit(i);
            BDD b2 = v2.getBit(i);
            BDD tmp1 = (gEQ.and((b2.xor(b1)).not())); // test for ==
            BDD tmp2 = b2.and(b1.not()); // test for <
            gEQ = tmp1.or(tmp2);
        }

        BDD tmp = getFactory().zero();
        BDD v1msb = v1.getBit(msb);
        BDD v2msb = v2.getBit(msb);
        if (v1.isSigned() && v2.isSigned())
        {
            // For signed quantities
            // if the highest bit of v1 is set, the absolute value of v2 must
            // either
            // be greater
            // than the absolute value of v1 or the highest bit of v2 must be
            // zero
            // (negative <= postive is always true)
            //
            // if the highest bit of v1 is not set, the absolute value of v2
            // must be
            // greater
            // than the absolute value of v1 and the highest bit of v2 must be
            // zero
            //
            // thus:
            // v1[msb] == 1 => (gEQ || v2[msb] == 0)
            // !v1[msb] == 1 => (lEQ)

            tmp = v1msb.imp(gEQ.or(v2msb.not()));
            tmp = tmp.and((v1msb.not()).imp(lEQ.and(v2msb.not())));
        }
        else if (v1.isSigned())
        {
            // for signed/unsigned, true if |a| <= |b| and a is not negative
            assert (!v2.isSigned());
            tmp = lEQ.and(v1msb.not());
        }
        else if (v2.isSigned())
        {
            // for unsigned/signed, true if |a| <= |b| and b is not negative
            assert (!v1.isSigned());
            tmp = lEQ.and(v2msb.not());
        }
        else
        {
            // for unsigned/unsigned, true if |a| <= |b|
            tmp = lEQ;
        }

        return tmp;
    }

    public static BDD bddLthan(BDDBitVector v1, BDDBitVector v2)
    {
        BDD tmpNeq = bddNeq(v1, v2);
        BDD tmpLeq = bddLeq(v1, v2);
        BDD tmp = tmpNeq.and(tmpLeq);
        return tmp;
    }

    public static BDD bddGthan(BDDBitVector v1, BDDBitVector v2)
    {
        // v1 > v2 == v2 < v1
        BDD tmp = bddLthan(v2, v1);
        return tmp;
    }

    public static BDD bddGeq(BDDBitVector v1, BDDBitVector v2)
    {
        BDD tmpEq = bddEq(v1, v2);
        BDD tmpGt = bddGthan(v1, v2);
        BDD tmp = tmpEq.or(tmpGt);
        return tmp;
    }

    // Bitwise operators
    public static BDDBitVector bddBitwiseAnd(BDDBitVector v1, BDDBitVector v2)
    {
        bddMakeEqualLengths(v1, v2);
        v1 = v1.map2(v2, BDDFactory.and);
        return v1;
    }

    public static BDDBitVector bddBitwiseOr(BDDBitVector v1, BDDBitVector v2)
    {
        bddMakeEqualLengths(v1, v2);
        v1 = v1.map2(v2, BDDFactory.or);
        return v1;
    }

    public static BDDBitVector bddBitwiseXor(BDDBitVector v1, BDDBitVector v2)
    {
        bddMakeEqualLengths(v1, v2);
        v1 = v1.map2(v2, BDDFactory.xor);
        return v1;
    }

    public static BDDBitVector bddBitwiseNot(BDDBitVector v1)
    {
        BDDBitVector tmp = v1.copy();

        for (int i = 0; i < v1.size(); i++)
        {
            tmp.setBit(i, v1.getBit(i).not());
        }
        return tmp;
    }

    // reduction operators
    public static BDD bddReductiveAnd(BDDBitVector v)
    {
        BDD tmp = getFactory().one();
        for (int i = 0; i < v.size(); ++i)
        {
            tmp = tmp.and(v.getBit(i));
        }
        return tmp;
    }

    public static BDD bddReductiveOr(BDDBitVector v)
    {
        BDD tmp = getFactory().zero();
        for (int i = 0; i < v.size(); ++i)
        {
            tmp = tmp.or(v.getBit(i));
        }
        return tmp;
    }

    public static BDD bddReductiveXor(BDDBitVector v)
    {
        BDD tmp = getFactory().zero();
        for (int i = 0; i < v.size(); ++i)
        {
            tmp = tmp.xor(v.getBit(i));
        }
        return tmp;
    }

    // Shift operators
    public static BDDBitVector bddShiftLeft(
        BDDBitVector v,
        BDDBitVector shiftVector)
    {
        if (shiftVector.isConst())
        {
            int shiftVal = (int) shiftVector.val();
            if (shiftVal < 0)
            {
                throw new IllegalArgumentException(
                    "Illegal negative shift count: " + shiftVal);
            }
            if (shiftVal > 0)
            {
                v = v.shl((int) shiftVector.val(), getFactory().zero());
            }
            return v;
        }
        else
        {
            // check for easy cases
            if (v.isZero() || shiftVector.isZero())
            {
                return v;
            }

            final int vSize = v.size();
            final BDD bitToShiftIn = getFactory().zero();
            final BDDBitVector result = getFactory().buildVector(vSize, false);
            result.setSigned(v.isSigned());

            for (int i = 0; i < vSize; ++i)
            {
                // create a constant vector equal to the current shift value
                // and a BDD saying whether or not the current shift value
                // is equal to the one one passed in
                final BDDBitVector curShiftVal = getFactory().constantVector(
                    shiftVector.size(), i);
                curShiftVal.setSigned(shiftVector.isSigned());
                final BDD curValIsEq = bddEq(shiftVector, curShiftVal);

                for (int j = 0; j < vSize; ++j)
                {
                    // Determine the bit to or in. This is effectively or'ing
                    // the bit "shiftVector" bits to the right if such a bit
                    // exists in v. Otherwise, we shift in a zero
                    final BDD orBit;
                    if ((j - i) >= 0)
                    {
                        orBit = curValIsEq.and(v.getBit(j - i));
                    }
                    else
                    {
                        orBit = curValIsEq.and(bitToShiftIn);
                    }

                    result.setBit(j, result.getBit(j).or(orBit));
                }
            }

            // Check if shiftVector > v.size(). If so, shift in zeros
            final BDDBitVector vSizeVector = getFactory().constantVector(
                shiftVector.size(), vSize);
            vSizeVector.setSigned(shiftVector.isSigned());
            final BDD svGthanVsize = bddGthan(shiftVector, vSizeVector);

            for (int i = 0; i < vSize; ++i)
            {
                result.setBit(i, result.getBit(i).or(svGthanVsize));
            }

            return result;
        }
    }

    public static BDDBitVector bddShiftRight(
        BDDBitVector v,
        BDDBitVector shiftVector,
        boolean signed)
    {
        final BDD bitToShiftIn = signed ? v.getBit(v.size() - 1) : getFactory()
            .zero();

        if (shiftVector.isConst())
        {
            int shiftVal = (int) shiftVector.val();
            if (shiftVal < 0)
            {
                throw new IllegalArgumentException(
                    "Illegal negative shift count: " + shiftVal);
            }
            if (shiftVal > 0)
            {
                v = v.shr(shiftVal, bitToShiftIn);
            }

            return v;
        }
        else
        {
            final int vSize = v.size();
            final BDDBitVector result = getFactory().buildVector(vSize, false);
            result.setSigned(v.isSigned());

            for (int i = 0; i < vSize; ++i)
            {
                // Generate a constant vector equal to the current value of i.
                // Then generate a BDD representing whether or not that vector
                // equals shiftValue
                final BDDBitVector curShiftVal = getFactory().constantVector(
                    vSize, i);
                curShiftVal.setSigned(shiftVector.isSigned());
                final BDD curValIsEq = bddEq(shiftVector, curShiftVal);

                for (int j = 0; j < vSize; ++j)
                {
                    // Determine the bit to or in
                    // This is effectively or'ing the bit "shiftVector" bits
                    // to the left if such a bit exists in v. Otherwise, we
                    // shift in a zero
                    final BDD orBit;
                    if (i + j < vSize)
                    {
                        orBit = curValIsEq.and(v.getBit(i + j));
                    }
                    else
                    {
                        orBit = curValIsEq.and(bitToShiftIn);
                    }

                    result.setBit(j, result.getBit(j).or(orBit));
                }
            }

            // Check if shiftVector > v.size(). If so, or in the appropriate bit
            final BDDBitVector vSizeVector = getFactory().constantVector(
                shiftVector.size(), vSize);
            vSizeVector.setSigned(shiftVector.isSigned());
            final BDD svGthanVsize = bddGthan(shiftVector, vSizeVector);

            for (int i = 0; i < vSize; ++i)
            {
                result.setBit(i, result.getBit(i).or(svGthanVsize));
            }

            return result;
        }
    }

    // Arithmetic operators
    public static BDDBitVector bddTwosCompliment(BDDBitVector v)
    {
        if (!v.isSigned())
        {
            v.coerce(v.size() + 1);
        }
        BDDBitVector tmp = bddBitwiseNot(v);
        BDDBitVector addVal = getFactory().constantVector(1, 1);
        BDDBitVector result = bddPlus(tmp, addVal, v.size());
        return result;
    }

    @SuppressWarnings("static-access")
    // for getFactory().and/or
    private static BDDBitVector bddPlusMinus(
        BDDBitVector v1,
        BDDBitVector v2,
        int resultLength,
        boolean isPlus)
    {
        int size = bddMakeEqualLengths(v1, v2);
        int length = Math.max(resultLength, size);
        BDDBitVector vec = getFactory().buildVector(length, false);
        BDD carry = getFactory().zero();

        final BDDOp op;
        final BDD opArg;

        if (isPlus)
        {
            op = getFactory().and;
            opArg = getFactory().zero();
        }
        else
        {
            op = getFactory().or;
            opArg = getFactory().one();
        }

        // If a signed quantity is being added to an unsigned quantity,
        // Force the MSB of the unsigned quantity to zero to avoid it being
        // interpreted as negative
        if (!v1.isSigned() && v2.isSigned())
        {
            v1.setBit(size - 1, v1.getBit(size - 1).apply(opArg, op));
        }
        if (!v2.isSigned() && v1.isSigned())
        {
            v2.setBit(size - 1, v2.getBit(size - 1).apply(opArg, op));
        }

        for (int i = 0; i < size; i++)
        {
            BDD b0 = v1.getBit(i);
            BDD b1 = v2.getBit(i);
            BDD tmp = b0.xor(b1.xor(carry));

            vec.setBit(i, tmp);
            BDD oflow1 = b0.and(b1);
            BDD oflow2 = carry.and(b0.or(b1));
            carry = oflow1.or(oflow2);
        }

        if (resultLength > size)
        {
            vec.setBit(size, carry);
        }

        vec.setSigned(v1.isSigned() || v2.isSigned());

        return vec;
    }

    public static BDDBitVector bddPlus(
        BDDBitVector v1,
        BDDBitVector v2,
        int resultLength)
    {
        return bddPlusMinus(v1, v2, resultLength, true);
    }

    public static BDDBitVector bddMinus(
        BDDBitVector v1,
        BDDBitVector v2,
        int resultLength)
    {
        bddMakeEqualLengths(v1, v2);
        BDDBitVector twosComp = bddTwosCompliment(v2);
        BDDBitVector vec = bddPlusMinus(v1, twosComp, resultLength, false);
        return vec;
    }

    public static BDDBitVector bddMultiply(
        BDDBitVector v1,
        BDDBitVector v2,
        int resultLength)
    {
        if (v1.isZero() || v2.isZero())
        {
            final BDDBitVector result = getFactory().buildVector(1, false);
            result.setSigned(v1.isSigned() || v2.isSigned());
            return result;
        }

        if (v2.isConst() || v2.size() < v1.size())
        {
            // This algorithm works best if v1 is as small as possible (or
            // constant). Otherwise it can explode. Thus use the good old
            // commutative property of multiplication if we see that v2 is
            // constant (or of less width).
            final BDDBitVector tmp = v1;
            v1 = v2;
            v2 = tmp;
        }

        v1.coerce(resultLength);
        final int size = bddMakeEqualLengths(v1, v2);
        final int productSize = size * 2;

        final boolean v1unsigned = !v1.isSigned() && v2.isSigned();
        final boolean v2unsigned = !v2.isSigned() && v1.isSigned();
        if (v1unsigned)
        {
            v1.setBit(size - 1, getFactory().zero());
        }
        if (v2unsigned)
        {
            v2.setBit(size - 1, getFactory().zero());
        }

        // Multiply using the absolute value
        final BDDBitVector v1Comp = bddTwosCompliment(v1);
        final BDDBitVector v2Comp = bddTwosCompliment(v2);
        final BDD v1Sign = v1.getBit(size - 1);
        final BDD v2Sign = v2.getBit(size - 1);
        for (int i = 0; i < size; ++i)
        {
            v1.setBit(i, v1Sign.ite(v1Comp.getBit(i), v1.getBit(i)));
            v2.setBit(i, v2Sign.ite(v2Comp.getBit(i), v2.getBit(i)));
        }

        final BDDBitVector product = getFactory().buildVector(productSize,
            false);
        product.setSigned(v1.isSigned() || v2.isSigned());

        BDDBitVector tmp = v1.copy();

        for (int i = 0; i < size; ++i)
        {
            // sum = product + tmp;
            final BDDBitVector sum = bddPlus(product, tmp, productSize);
            for (int j = 0; j < productSize; ++j)
            {
                // if(v2[i] == 1)
                //     product = product + tmp;
                // else
                //     product = product;
                final BDD tmpres = v2.getBit(i).ite(sum.getBit(j),
                    product.getBit(j));
                product.setBit(j, tmpres);
            }

            // tmp <<= 1;
            tmp = bddShiftLeft(tmp, getFactory().constantVector(1, 1));
        }

        // product is set to the size of the maximum operand
        product.coerce(size);

        // Negate the result appropriately (if either the multiplicand or
        // multiplier is negative)
        final BDDBitVector prodComp = bddTwosCompliment(product);
        final BDD prodSign = v1Sign.xor(v2Sign);

        for (int i = 0; i < size; ++i)
        {
            product.setBit(i, prodSign.ite(prodComp.getBit(i), product
                .getBit(i)));
        }

        final BDD prodSignBit = product.getBit(size - 1);
        if (!prodSignBit.isOne() && !prodSignBit.isZero())
        {
            if (v1unsigned)
            {
                // The sign bit of the product cannot come from v1 if v1 is unsigned
                if (v1.containsBDD(prodSignBit))
                {
                    product.setBit(size - 1, getFactory().zero());
                }
            }
            if (v2unsigned)
            {
                // The sign bit of the product cannot come from v2 if v2 is unsigned
                if (v2.containsBDD(prodSignBit))
                {
                    product.setBit(size - 1, getFactory().zero());
                }
            }
        }

        return product;
    }

    public static BDDBitVector bddDivide(
        BDDBitVector dividend,
        BDDBitVector divisor,
        int resultLength)
    {
        return bddDivMod(dividend, divisor, resultLength, false);
    }

    public static BDDBitVector bddModulo(
        BDDBitVector dividend,
        BDDBitVector divisor,
        int resultLength)
    {
        return bddDivMod(dividend, divisor, resultLength, true);
    }

    private static BDDBitVector bddDivMod(
        BDDBitVector dividend,
        BDDBitVector divisor,
        int resultLength,
        boolean returnMod)
    {
        // Note the state space created by this algorithm blows up if both
        // _dividend and _divisor are variable and both are > 8 bits wide.
        // This should be fixed if possible.
        // TODO: research division algorithm capable of dividing 2 variable
        // values > 8 bits wide

        if (divisor.isZero())
        {
            throw new ArithmeticException("Divide by zero");
        }
        else if (dividend.isZero())
        {
            // both quotient and remainder are 0 in this case.
            return getFactory().constantVector(dividend.size(), 0);
        }
        else
        {
            dividend.coerce(resultLength);
            final int size = bddMakeEqualLengths(dividend, divisor);

            final boolean divisorUnsigned = !divisor.isSigned()
                && dividend.isSigned();
            final boolean dividendUnsigned = !dividend.isSigned()
                && divisor.isSigned();
            if (divisorUnsigned)
            {
                divisor.setBit(size - 1, getFactory().zero());
            }
            if (dividendUnsigned)
            {
                dividend.setBit(size - 1, getFactory().zero());
            }

            // divide using absolute values
            final BDDBitVector divisorComp = bddTwosCompliment(divisor);
            final BDDBitVector dividendComp = bddTwosCompliment(dividend);
            final BDD divisorSign = divisor.getBit(size - 1);
            final BDD dividendSign = dividend.getBit(size - 1);
            for (int i = 0; i < size; ++i)
            {
                divisor.setBit(i, divisorSign.ite(divisorComp.getBit(i),
                    divisor.getBit(i)));
                dividend.setBit(i, dividendSign.ite(dividendComp.getBit(i),
                    dividend.getBit(i)));
            }

            final int resultSize = size * 2;
            final BDDBitVector remainder = dividend.copy();
            remainder.setSigned(false);
            remainder.coerce(resultSize);

            BDDBitVector tmp = divisor.copy();
            tmp.setSigned(false);
            tmp.coerce(resultSize);
            tmp = bddShiftLeft(tmp, getFactory().constantVector(32, size));

            final BDDBitVector quotient = getFactory().buildVector(size, false);

            for (int i = 0; i < size + 1; ++i)
            {
                BDD divisorLessThanRemainder = bddLeq(tmp, remainder);
                BDDBitVector difference = bddMinus(remainder, tmp, resultSize);

                for (int j = 0; j < resultSize; ++j)
                {
                    // if(tmp <= remainder)
                    //     remainder = difference
                    // else
                    //     remainder = remainder
                    BDD tmpBit = divisorLessThanRemainder.ite(difference
                        .getBit(j), remainder.getBit(j));
                    remainder.setBit(j, tmpBit);
                }

                if (i > 0)
                {
                    quotient.setBit(size - i, divisorLessThanRemainder);
                }

                // tmp >>>= 1;
                tmp = bddShiftRight(tmp, getFactory().constantVector(1, 1),
                    false);
            }

            // Negate the answer appropriately
            final BDDBitVector result;
            final BDD resultSign;
            if (returnMod)
            {
                // result of modulo operation is negative only if dividend is
                // negative
                resultSign = dividendSign;
                result = remainder;
            }
            else
            {
                // result of division operation is negative if exactly one of
                // either divisor and dividend is negative
                resultSign = divisorSign.xor(dividendSign);
                result = quotient;
            }

            final BDDBitVector resultComp = bddTwosCompliment(result);
            for (int i = 0; i < size; ++i)
            {
                result.setBit(i, resultSign.ite(resultComp.getBit(i), result
                    .getBit(i)));
            }

            result.setSigned(dividend.isSigned() || divisor.isSigned());
            result.coerce(size);

            final BDD resultSignBit = result.getBit(size - 1);
            if (!resultSignBit.isOne() && !resultSignBit.isZero())
            {
                if (divisorUnsigned && !returnMod)
                {
                    // The sign bit of the result cannot come from divisor
                    // if divisor is unsigned. this is ignored for modulo as
                    // the divisor shouldn't affect that result anyway
                    if (divisor.containsBDD(resultSignBit))
                    {
                        result.setBit(size - 1, getFactory().zero());
                    }
                }
                if (dividendUnsigned)
                {
                    // The sign bit of the result cannot come from dividend if
                    // dividend is unsigned
                    if (dividend.containsBDD(resultSignBit))
                    {
                        result.setBit(size - 1, getFactory().zero());
                    }
                }
            }

            return result;
        }
    }

    public static int bddMakeEqualLengths(BDDBitVector v1, BDDBitVector v2)
    {
        int v1Size = v1.size();
        int v2Size = v2.size();

        // return early if possible.
        if (v1Size == v2Size) return v1Size;

        // otherwise, figure out the max and lengthen the vectors
        // we never shrink bit vectors
        int maxSize = Math.max(v1Size, v2Size);
        v1.coerce(maxSize);
        v2.coerce(maxSize);

        return maxSize;
    }

    public static BitVector randWalk(PRNG prng, BDD solution)
    {
        // no possible solution for this constraint set
        if (solution.nodeWeight() == 0.0)
        {
            return null;
        }

        BitVectorBuffer sol = new BitVectorBuffer(solution.varProfile().length);
        int numSetBits = 0;

        int curLevel = -1;
        int lastLevel = -1;
        BDD node = solution;
        for (int i = 0; i < sol.length(); i++)
        {
            // if we get to the true/false node, we're done
            if (node.isZero() || node.isOne())
            {
                break;
            }

            lastLevel = curLevel;
            curLevel = node.level();
            // System.out.println("curLevel: " + curLevel + ", lastLevel: "
            // + lastLevel);
            // We may have stepped through some don't cares.
            int numDontCares = curLevel - lastLevel;
            boolean sawDontCares = (numDontCares > 1);
            if (sawDontCares)
            {
                // if curLevel is beyond the end of the BitVector, the
                // rest of the bits are don't cares. otherwise, the code
                // below this if will get executed for the bit at curLevel
                int lastDontCare = numDontCares
                    - (curLevel >= sol.length() ? 0 : 1);
                for (int j = 0; j < lastDontCare; j++)
                {
                    sawDontCares = true;
                    i++;
                    if (i >= sol.length()) break;
                }
                if (i >= sol.length()) break;
            }
            if (node.edgeWeight(false) == 0.0)
            {
                sol.setBit(i, Bit.ONE);
                ++numSetBits;
                node = node.high();
            }
            else if (node.edgeWeight(true) == 0.0)
            {
                sol.setBit(i, Bit.ZERO);
                ++numSetBits;
                node = node.low();
            }
            else
            {
                int path = prng.nextBits(1);
                if (path == 0)
                {
                    sol.setBit(i, Bit.ZERO);
                    ++numSetBits;
                    node = node.low();
                }
                else
                {
                    sol.setBit(i, Bit.ONE);
                    ++numSetBits;
                    node = node.high();
                }
            }
        }

        // uniformly randomize unset bits
        final int numRandBits = sol.length() - numSetBits;
        if (numRandBits > 0)
        {
            BitVector randBits = prng.nextBitVector(numRandBits);
            for (int i = 0, randIdx = 0; i < sol.length(); ++i)
            {
                if (sol.getBit(i) == Bit.X)
                {
                    sol.setBit(i, randBits.getBit(randIdx));
                    ++randIdx;
                }
            }
        }
        return sol.toBitVector();
    }

    public static BDDBitVector constantVector(BitVector val)
    {
        if (val.containsXZ())
        {
            throw new XZException("X/Z Values are unsupported");
        }

        final int length = val.length();
        BDDBitVector v = getFactory().buildVector(length, false);
        final BDD TRUE = getFactory().one();
        final BDD FALSE = getFactory().zero();
        for (int n = 0; n < length; n++)
        {
            if (val.getBit(n) == Bit.ONE)
            {
                v.setBit(n, TRUE);
            }
            else
            {
                v.setBit(n, FALSE);
            }
        }
        return v;
    }

}