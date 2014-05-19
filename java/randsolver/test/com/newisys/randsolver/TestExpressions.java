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
import com.newisys.randsolver.annotation.Randomizable;
import com.newisys.verilog.util.Bit;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.Length;

import static com.newisys.randsolver.RandSolverTestGlobals.RANDOM_ITERS;

public class TestExpressions
    extends TestCase
{

    PRNG prng;
    long seed = System.currentTimeMillis();

    public TestExpressions()
    {
        super();
    }

    @Override
    public void setUp()
    {
        prng = PRNGFactoryFactory.getDefaultFactory().newInstance(seed);
    }

    public void testBDDVectExpr()
    {
        @Randomizable(@Constraint(expr = "mInt - 3; mInt > -5 && mInt < 5;"))
        class Dut
        {
            @Rand
            int mInt;
        }

        Dut dut = new Dut();
        for (int i = 0; i < RANDOM_ITERS; ++i)
        {
            Solver.randomize(dut, prng);
            assertTrue(dut.mInt != 3);
        }
    }

    public void testSignedExpr()
    {
        @Randomizable(@Constraint(expr = "mInt> -5; mInt < 4;"))
        class Dut
        {
            @Rand
            int mInt;
        }

        Dut dut = new Dut();
        for (int i = 0; i < RANDOM_ITERS; ++i)
        {
            Solver.randomize(dut, prng);
            assertTrue(dut.mInt > -5);
            assertTrue(dut.mInt < 4);
        }
    }

    public void testInAsExpression()
    {
        @Randomizable(@Constraint(expr = "mInt in {2, 4, 6, 8} == 1;"))
        class Dut
        {
            @Rand
            int mInt;
        }

        Dut dut = new Dut();
        for (int i = 0; i < RANDOM_ITERS; ++i)
        {
            Solver.randomize(dut, prng);
            assertTrue(dut.mInt >= 2);
            assertTrue(dut.mInt <= 8);
            assertTrue(dut.mInt % 2 == 0);
        }
    }

    public void testSimpleImplication()
    {
        @Randomizable(@Constraint(expr = "mInt == 4; mInt == 4 => mInt2 == 5;"))
        class Dut
        {
            @SuppressWarnings("unused")
            @Rand
            int mInt;

            @Rand
            int mInt2;

            @SuppressWarnings("unused")
            @Rand
            int mInt3;
        }

        Dut dut = new Dut();
        for (int i = 0; i < RANDOM_ITERS; ++i)
        {
            Solver.randomize(dut, prng);
            assertTrue(dut.mInt == 4);
            assertTrue(dut.mInt2 == 5);
        }

        // TODO when grammar supports constraint sets
        //        testObj
        //            .addConstraint("mInt == 4; mInt == 4 => {mInt2 == 5; mInt3 == 7;}");
        //        time = System.currentTimeMillis();
        //        for (int i = 0; i < RANDOM_ITERS; ++i)
        //        {
        //            Solver.randomize(testObj, prng);
        //            assertTrue(testObj.mInt == 4);
        //            assertTrue(testObj.mInt2 == 5);
        //            assertTrue(testObj.mInt3 == 7);
        //        }
        //        time = System.currentTimeMillis() - time;
        //        System.out.println("testSimpleImplication[2]: " + RANDOM_ITERS
        //            + " iterations in " + time + "ms");
    }

    public void testImplicationAsExpression()
    {
        @Randomizable( {
            @Constraint(name = "c1", expr = "mInt == 4; mInt2 == 0; mInt == 4 => mInt3 == 5;"),
            @Constraint(name = "c2", expr = "mInt == 4; mInt2 == (mInt == 1 => mInt3 == 5);"),
            @Constraint(name = "c3", expr = "mBv in {3'h0, 3'h1, 3'h2}; mBit => mBv != 3'h2;") })
        class Dut
        {
            @Rand
            int mInt;
            @Rand
            int mInt2;
            @Rand
            int mInt3;

            @Rand
            @Length(3)
            BitVector mBv;

            @SuppressWarnings("unused")
            Bit mBit = Bit.ONE;

            public Dut()
            {
                Solver.disableConstraint(this, "c2");
                Solver.disableConstraint(this, "c3");
                Solver.disableRand(this, "mBv");
            }
        }

        Dut dut = new Dut();
        for (int i = 0; i < RANDOM_ITERS; ++i)
        {
            Solver.randomize(dut, prng);
            assertTrue(dut.mInt == 4);
            assertTrue(dut.mInt2 == 0);
            assertTrue(dut.mInt3 == 5);
        }

        Solver.disableAllConstraints(dut);
        Solver.enableConstraint(dut, "c2");
        for (int i = 0; i < RANDOM_ITERS; ++i)
        {
            Solver.randomize(dut, prng);
            assertTrue(dut.mInt == 4);
            assertTrue(dut.mInt2 == 1);
        }

        Solver.disableAllConstraints(dut);
        Solver.disableAllRand(dut);
        Solver.enableRand(dut, "mBv");
        Solver.enableConstraint(dut, "c3");
        for (int i = 0; i < RANDOM_ITERS; ++i)
        {
            Solver.randomize(dut, prng);
            assertTrue(dut.mBv.intValue() == 0 || dut.mBv.intValue() == 1);
        }
    }

    public void testBit()
    {
        @Randomizable
        class Dut
        {
            @Rand
            Bit mBit;
        }

        Dut dut = new Dut();

        boolean globalSawZero = false;
        boolean globalSawOne = false;
        for (int i = 0; i < RANDOM_ITERS; ++i)
        {
            boolean sawZero = false;
            boolean sawOne = false;
            boolean sawX = false;
            boolean sawZ = false;
            Solver.randomize(dut, prng);
            sawZero = (dut.mBit == Bit.ZERO);
            sawOne = (dut.mBit == Bit.ONE);
            sawX = (dut.mBit == Bit.X);
            sawZ = (dut.mBit == Bit.Z);
            assertTrue(sawOne || sawZero);
            assertTrue(!sawX);
            assertTrue(!sawZ);

            globalSawZero |= sawZero;
            globalSawOne |= sawOne;
        }
        assertTrue(globalSawZero);
        assertTrue(globalSawOne);
    }

    public void testLiteralParsing()
    {
        @Randomizable( {
            @Constraint(name = "c1", expr = "mInt == 11'h0fc >>> 2;"),
            @Constraint(name = "c2", expr = "mVector >= 0xfd00000000L && mVector <= 0xfdf7ffffffL;") })
        class Dut
        {
            @Rand
            int mInt;

            @Rand
            @Length(40)
            BitVector mVector = new BitVector(40);
        }

        Dut dut = new Dut();
        Solver.disableConstraint(dut, "c2");
        for (int i = 0; i < RANDOM_ITERS; ++i)
        {
            Solver.randomize(dut, prng);
            assertEquals(0xfc >>> 2, dut.mInt);
        }

        Solver.disableConstraint(dut, "c1");
        Solver.enableConstraint(dut, "c2");
        for (int i = 0; i < RANDOM_ITERS; ++i)
        {
            Solver.randomize(dut, prng);
            assertTrue(dut.mVector.longValue() >= 0xfd00000000L);
            assertTrue(dut.mVector.longValue() <= 0xfdf7ffffffL);
        }
    }

    public void testInvertedInArgs()
    {
        @Randomizable( {
            @Constraint(name = "non-inverted", expr = "mInt in {5:11};"),
            @Constraint(name = "inverted", expr = "mInt in {11:5};"),
            @Constraint(name = "var_range", expr = "mInt in {mInt2:mInt3};") })
        class Dut
        {
            @Rand
            int mInt;

            @SuppressWarnings("unused")
            int mInt2;

            @SuppressWarnings("unused")
            int mInt3;

            public Dut()
            {
                Solver.disableConstraint(this, "inverted");
                Solver.disableConstraint(this, "var_range");
            }
        }

        Dut dut = new Dut();
        for (int i = 0; i < RANDOM_ITERS; ++i)
        {
            Solver.randomize(dut, prng);
            assertTrue(dut.mInt >= 5 && dut.mInt <= 11);
        }

        Solver.disableAllConstraints(dut);
        Solver.enableConstraint(dut, "inverted");
        try
        {
            Solver.randomize(dut, prng);
            assertTrue("Exception wasn't thrown as expected", false);
        }
        catch (UnsolvableConstraintException e)
        {
            // good.
        }

        Solver.disableAllConstraints(dut);
        Solver.enableConstraint(dut, "var_range");
        dut.mInt2 = 10;
        dut.mInt3 = 5;
        try
        {
            Solver.randomize(dut, prng);
            assertTrue("Exception wasn't thrown as expected", false);
        }
        catch (UnsolvableConstraintException e)
        {
            // good.
        }
    }

}
