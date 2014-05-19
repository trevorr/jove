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

import junit.framework.TestCase;

import com.newisys.random.PRNG;
import com.newisys.random.PRNGFactoryFactory;
import com.newisys.randsolver.annotation.Constraint;
import com.newisys.randsolver.annotation.Rand;
import com.newisys.randsolver.annotation.Randc;
import com.newisys.randsolver.annotation.Randomizable;
import com.newisys.verilog.util.Bit;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.BitVectorBuffer;
import com.newisys.verilog.util.Length;

@Randomizable( { @Constraint(name = "c1", expr = "mBv == mInt + 2;"),
    @Constraint(name = "c2", expr = "mBv == 8;") })
class AnnotatedObj
{
    private @Rand
    @Length(16)
    BitVector mBv;
    private @Rand
    int mInt;
    private @Randc
    byte mChar;

    public int getInt()
    {
        return mInt;
    }

    public void setInt(int i)
    {
        mInt = i;
    }

    public BitVector getBv()
    {
        return mBv;
    }

    public int getBvIntValue()
    {
        return mBv.intValue();
    }

    public int getChar()
    {
        return (mChar & 0x0FF);
    }
}

final class TestNoAnnotation
{
    @Rand
    public int bar;
}

@Randomizable
final class TestNoBitVectorAnnotation
{
    @Rand
    BitVector vector;
}

@Randomizable
final class Test0LengthBitVectorAnnotation
{
    @Rand
    @Length(0)
    BitVector vector;
}

@Randomizable(@Constraint(expr = "i == j;"))
final class TestXZStateVar
{
    @Rand
    Integer i;
    Integer j;
}

public class TestAnnotation
    extends TestCase
{
    AnnotatedObj obj1, obj2;
    PRNG prng;
    long seed = System.currentTimeMillis();
    static int RANDOM_ITERS = 100;

    @Override
    public void setUp()
    {
        prng = PRNGFactoryFactory.getDefaultFactory().newInstance(seed);
        obj1 = new AnnotatedObj();
        obj2 = new AnnotatedObj();
    }

    private void checkFail(AnnotatedObj obj)
    {
        try
        {
            Solver.randomize(obj, prng);

            // shouldn't get here
            assertFalse("Exception wasn't thrown as expected", true);
        }
        catch (UnsolvableConstraintException e)
        {
            // good both constraints are enabled
        }
    }

    public void testAnnotations()
    {
        // turn off mInt/mChar for now -- assert neither was previously disabled
        assertFalse(Solver.disableRand(obj1, "mInt"));
        assertFalse(Solver.disableRand(obj2, "mInt"));
        assertFalse(Solver.isRandEnabled(obj1, "mInt"));
        assertFalse(Solver.isRandEnabled(obj2, "mInt"));
        assertTrue(Solver.isRandEnabled(obj1, "mBv"));
        assertTrue(Solver.isRandEnabled(obj2, "mBv"));

        // 1. Try and solve an unsolvable constraint
        obj1.setInt(5);
        checkFail(obj1);

        // 2. Disable one of the constraints which should make the problem solvable
        Solver.disableConstraint(obj1, "c2");
        Solver.randomize(obj1, prng);
        assertEquals(obj1.getBvIntValue(), obj1.getInt() + 2);

        // 3. Try obj2 -- obj2.c2 has not been disabled. This should fail.
        obj2.setInt(5);
        checkFail(obj2);

        assertFalse(Solver.enableConstraint(obj1, "c2"));
        assertTrue(Solver.enableConstraint(obj1, "c2"));
        obj1.setInt(5);
        checkFail(obj1);

        // 4. check return values for disable/enable
        assertTrue(Solver.disableRand(obj1, "mInt"));
        assertFalse(Solver.isRandEnabled(obj1, "mInt"));
        assertTrue(Solver.disableRand(obj1, "mInt"));
        assertFalse(Solver.isRandEnabled(obj1, "mInt"));
        assertFalse(Solver.enableRand(obj1, "mInt"));
        assertTrue(Solver.isRandEnabled(obj1, "mInt"));
        assertTrue(Solver.enableRand(obj1, "mInt"));
        assertTrue(Solver.isRandEnabled(obj1, "mInt"));
        assertFalse(Solver.disableRand(obj1, "mInt"));
        assertFalse(Solver.isRandEnabled(obj1, "mInt"));

        assertTrue(Solver.enableConstraint(obj1, "c1"));
        assertTrue(Solver.isConstraintEnabled(obj1, "c1"));
        assertTrue(Solver.enableConstraint(obj1, "c1"));
        assertTrue(Solver.isConstraintEnabled(obj1, "c1"));
        assertFalse(Solver.disableConstraint(obj1, "c1"));
        assertFalse(Solver.isConstraintEnabled(obj1, "c1"));
        assertTrue(Solver.disableConstraint(obj1, "c1"));
        assertFalse(Solver.isConstraintEnabled(obj1, "c1"));
        assertFalse(Solver.enableConstraint(obj1, "c1"));
        assertTrue(Solver.isConstraintEnabled(obj1, "c1"));

        // 5. check enable/disable functionality
        // set up enabled constraints/vars
        assertTrue(Solver.enableConstraint(obj1, "c1"));
        assertFalse(Solver.disableConstraint(obj1, "c2"));
        assertTrue(Solver.enableRand(obj1, "mBv"));
        assertTrue(Solver.disableRand(obj1, "mInt"));

        // disable mInt and watch it stay constant
        obj1.setInt(4);
        for (int i = 0; i < 20; ++i)
        {
            Solver.randomize(obj1, prng);
            assertEquals(4, obj1.getInt());
            assertEquals(6, obj1.getBvIntValue());
        }

        // enable mInt and watch it get randomized
        assertFalse(Solver.enableRand(obj1, "mInt"));
        boolean sawIntChange = false;
        obj1.setInt(10);
        for (int i = 0; i < 20; ++i)
        {
            Solver.randomize(obj1, prng);
            sawIntChange |= (obj1.getInt() != 10);
            assertEquals(obj1.getBvIntValue(), obj1.getInt() + 2);
        }
        assertTrue(sawIntChange);
    }

    public void testRandc()
    {
        final int iters = 256;
        BitVectorBuffer mask = new BitVectorBuffer(iters);
        assertFalse(Solver.disableRand(obj1, "mInt"));
        assertFalse(Solver.disableRand(obj1, "mBv"));
        assertFalse(Solver.disableConstraint(obj1, "c1"));
        assertFalse(Solver.disableConstraint(obj1, "c2"));

        for (int i = 0; i < iters; ++i)
        {
            Solver.randomize(obj1, prng);
            int value = obj1.getChar();
            assertTrue(value >= 0);
            assertTrue(value < iters);
            mask.setBit(value, Bit.ONE);
        }
        assertEquals(Bit.ONE, mask.reductiveAnd());
    }

    public void testError()
    {
        TestNoAnnotation obj = new TestNoAnnotation();
        try
        {
            Solver.randomize(obj, prng);
            assertFalse("Exception wasn't thrown as expected", true);
        }
        catch (InvalidConstraintException e)
        {
            // good.
        }

        TestNoBitVectorAnnotation obj2 = new TestNoBitVectorAnnotation();
        try
        {
            Solver.randomize(obj2, prng);
            // shouldn't get here since @Length is required
            assertFalse("Exception wasn't thrown as expected", true);
        }
        catch (InvalidRandomVarException e)
        {
            // good.
        }

        Test0LengthBitVectorAnnotation obj3 = new Test0LengthBitVectorAnnotation();
        try
        {
            Solver.randomize(obj3, prng);
            // shouldn't get here since @Length <= 0 is invalid
            assertFalse("Exception wasn't thrown as expected", true);
        }
        catch (InvalidRandomVarException e)
        {
            // good.
        }
    }

    public void testXZStateVar()
    {
        TestXZStateVar o = new TestXZStateVar();
        try
        {
            Solver.randomize(o, prng);
            assertTrue("Exception wasn't thrown as expected", false);
        }
        catch (InvalidConstraintException e)
        {
            // good. X/Z state vars are not allowed.
        }

        o.j = new Integer(7);
        Solver.randomize(o, prng);
        assertEquals(7, o.j.intValue());
    }
}
