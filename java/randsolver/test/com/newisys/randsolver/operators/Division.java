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

package com.newisys.randsolver.operators;

import static com.newisys.randsolver.RandSolverTestGlobals.RANDOM_ITERS;
import static com.newisys.randsolver.RandSolverTestGlobals.prng;
import junit.framework.TestCase;

import com.newisys.randsolver.Solver;
import com.newisys.randsolver.UnsolvableConstraintException;
import com.newisys.randsolver.annotation.Constraint;
import com.newisys.randsolver.annotation.Rand;
import com.newisys.randsolver.annotation.Randomizable;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.Length;

public class Division
    extends TestCase
{
    public void testConstantQuotient()
    {
        @Randomizable( { @Constraint(name = "c1", expr = "int1 == 48 / 6;"),
            @Constraint(name = "c2", expr = "int1 == -48 / 6;"),
            @Constraint(name = "c3", expr = "int1 == 48 / -6;"),
            @Constraint(name = "c4", expr = "int1 == -48 / -6;"),
            @Constraint(name = "c5", expr = "int1 == 0 / -6;"),
            @Constraint(name = "c6", expr = "int1 == -48 / 0;") })
        class Dut
        {
            @Rand
            int int1;
        }

        Dut dut = new Dut();

        // c1
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c1");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(dut.int1 == 48 / 6);
            }
        }
        // c2
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c2");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(dut.int1 == -48 / 6);
            }
        }
        // c3
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c3");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(dut.int1 == 48 / -6);
            }
        }
        // c4
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c4");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(dut.int1 == -48 / -6);
            }
        }
        // c5
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c5");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(dut.int1 == 0 / 6);
            }
        }
        // c6
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c6");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                try
                {
                    Solver.randomize(dut, prng);
                    assertTrue(false);
                }
                catch (ArithmeticException e)
                {
                    // good
                }
            }
        }
    }

    public void testConstantDivisor()
    {
        @Randomizable( { @Constraint(name = "c1", expr = "35 == int1 / 7;"),
            @Constraint(name = "c2", expr = "-35 == int1 / 7;"),
            @Constraint(name = "c3", expr = "35 == int1 / -7;"),
            @Constraint(name = "c4", expr = "-35 == int1 / -7;"),
            @Constraint(name = "c5", expr = "0 == int1 / -7;"),
            @Constraint(name = "c6", expr = "35 == int1 / 0;") })
        class Dut
        {
            @Rand
            int int1;
        }

        Dut dut = new Dut();

        // c1
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c1");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(35 == dut.int1 / 7);
            }
        }
        // c2
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c2");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(-35 == dut.int1 / 7);
            }
        }
        // c3
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c3");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(35 == dut.int1 / -7);
            }
        }
        // c4
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c4");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(-35 == dut.int1 / -7);
            }
        }
        // c5
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c5");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(0 == dut.int1 / -7);
            }
        }
        // c6
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c6");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                try
                {
                    Solver.randomize(dut, prng);
                    assertTrue(false);
                }
                catch (ArithmeticException e)
                {
                    // good
                }
            }
        }

    }

    public void testConstantDividend()
    {
        @Randomizable( { @Constraint(name = "c1", expr = "5 == 35 /int1;"),
            @Constraint(name = "c2", expr = "5 == -35 / int1;"),
            @Constraint(name = "c3", expr = "-5 == 35 / int1;"),
            @Constraint(name = "c4", expr = "-5 == -35 / int1;"),
            @Constraint(name = "c5", expr = "0 == -234 / int1;"),
            @Constraint(name = "c6", expr = "5 == 0 / int1;") })
        class Dut
        {
            @Rand
            int int1;
        }

        Dut dut = new Dut();

        // c1
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c1");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(5 == 35 / dut.int1);
            }
        }
        // c2
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c2");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(5 == -35 / dut.int1);
            }
        }
        // c3
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c3");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(-5 == 35 / dut.int1);
            }
        }
        // c4
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c4");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(-5 == -35 / dut.int1);
            }
        }
        // c5
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c5");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(0 == -234 / dut.int1);
            }
        }
        // c6
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c6");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                try
                {
                    Solver.randomize(dut, prng);
                    assertTrue(false);
                }
                catch (UnsolvableConstraintException e)
                {
                    // good
                }
            }
        }

    }

    public void testRandDivisorAndDividend()
    {
        // Note. When both dividend and divisor are random values, you
        // can't go much higher than 8 bits without exploding
        @Randomizable( { @Constraint(name = "c1", expr = "7 == (bv1 / bv2);"),
            @Constraint(name = "c2", expr = "13 == (bv1 / bv2);"),
            @Constraint(name = "c3", expr = "-8 == (bv1 / bv2);") })
        class Dut
        {
            @Rand
            @Length(8)
            BitVector bv1;

            @Rand
            @Length(8)
            BitVector bv2;

            public Dut()
            {
                Solver.disableAllConstraints(this);
            }
        }
        final Dut dut = new Dut();

        // c1
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c1");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(7 == dut.bv1.intValue() / dut.bv2.intValue());
            }
        }
        // c2
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c2");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(13 == dut.bv1.intValue() / dut.bv2.intValue());
            }
        }
        // c3
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c3");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                try
                {
                    Solver.randomize(dut, prng);
                    assertTrue(false);
                }
                catch (UnsolvableConstraintException e)
                {
                    // good
                }
            }
        }

    }

    public void testBitvectorsDontGoNegative()
    {
        @Randomizable( { @Constraint(name = "c1", expr = "-8 == (-64 / bv);"),
            @Constraint(name = "c2", expr = "-8 == (64 / bv);"),
            @Constraint(name = "c3", expr = "-8 == (64 / bv32);"),
            @Constraint(name = "c4", expr = "-8 == (64 / bv32) / 2;") })
        class Dut
        {
            @Rand
            @Length(8)
            BitVector bv = new BitVector(8);

            @Rand
            @Length(32)
            @SuppressWarnings("unused")
            BitVector bv32 = new BitVector(32);

            public Dut()
            {
                Solver.disableRand(this, "bv32");
            }
        }

        final Dut dut = new Dut();

        // c1
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c1");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(-8 == (-64 / dut.bv.intValue()));
            }
        }
        // c2
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c2");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                try
                {
                    Solver.randomize(dut, prng);
                    assertTrue(false);
                }
                catch (UnsolvableConstraintException e)
                {
                    // good
                }
            }
        }
        // c3
        {
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "bv32");
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c3");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                try
                {
                    Solver.randomize(dut, prng);
                    assertTrue(false);
                }
                catch (UnsolvableConstraintException e)
                {
                    // good
                }
            }
        }
        // c4
        {
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "bv32");
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c4");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                try
                {
                    Solver.randomize(dut, prng);
                    assertTrue(false);
                }
                catch (UnsolvableConstraintException e)
                {
                    // good
                }
            }
        }
    }

    public void testComplexExpressions()
    {
        // Note. When both dividend and divisor are random values, you
        // can't go much higher than 8 bits without exploding
        @Randomizable( { @Constraint(name = "c1", expr = "-8 == (64 / int1) / 2;") })
        class Dut
        {
            @Rand
            int int1;
        }
        final Dut dut = new Dut();

        // c1
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c1");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(-8 == (64 / dut.int1) / 2);
            }
        }
    }

}
