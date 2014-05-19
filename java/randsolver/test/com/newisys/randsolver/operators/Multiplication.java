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

public class Multiplication
    extends TestCase
{
    public void testConstantProducts()
    {
        @Randomizable( { @Constraint(name = "c1", expr = "mInt == (2 * 72);"),
            @Constraint(name = "c2", expr = "mInt == (-2 * 72);"),
            @Constraint(name = "c3", expr = "mInt == (-2 * -72);"),
            @Constraint(name = "c4", expr = "mInt == (0 * 4);"),
            @Constraint(name = "c5", expr = "mInt == (4 * 0);") })
        class Dut
        {
            @Rand
            int mInt;

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
                assertTrue(dut.mInt == (2 * 72));
            }
        }
        // c2
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c2");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(dut.mInt == (-2 * 72));
            }
        }
        // c3
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c3");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(dut.mInt == (-2 * -72));
            }
        }
        // c4
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c4");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(dut.mInt == (0 * 4));
            }
        }
        // c5
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c5");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(dut.mInt == (4 * 0));
            }
        }

    }

    public void testConstantMultiplier()
    {
        @Randomizable( { @Constraint(name = "c1", expr = "-4 == (mInt * -2);"),
            @Constraint(name = "c2", expr = "-4 == (mInt * 2);"),
            @Constraint(name = "c3", expr = "4 == (mInt * -2);"),
            @Constraint(name = "c4", expr = "4 == (mInt * 2);"),
            @Constraint(name = "c5", expr = "0 == (mInt * 4);") })
        class Dut
        {
            @Rand
            int mInt;

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
                assertTrue(-4 == (dut.mInt * -2));
            }
        }
        // c2
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c2");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(-4 == (dut.mInt * 2));
            }
        }
        // c3
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c3");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(4 == (dut.mInt * -2));
            }
        }
        // c4
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c4");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(4 == (dut.mInt * 2));
            }
        }
        // c5
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c5");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(0 == (dut.mInt * 4));
            }
        }

    }

    public void testConstantMultiplicand()
    {
        @Randomizable( { @Constraint(name = "c1", expr = "-8 == (-4 * mInt);"),
            @Constraint(name = "c2", expr = "-8 == (4 * mInt);"),
            @Constraint(name = "c3", expr = "8 == (-4 * mInt);"),
            @Constraint(name = "c4", expr = "8 == (4 * mInt);"),
            @Constraint(name = "c5", expr = "0 == (4 * mInt);") })
        class Dut
        {
            @Rand
            int mInt;

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
                assertTrue(-8 == (-4 * dut.mInt));
            }
        }
        // c2
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c2");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(-8 == (4 * dut.mInt));
            }
        }
        // c3
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c3");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(8 == (-4 * dut.mInt));
            }
        }
        // c4
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c4");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(8 == (4 * dut.mInt));
            }
        }
        // c5
        {
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c5");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(0 == (4 * dut.mInt));
            }
        }

    }

    public void testRandMultiplierAndMultiplicand()
    {
        // Note. When both multiplier and multiplicand are random values, you
        // can't go much higher than 8 bits without exploding
        @Randomizable( {
            @Constraint(name = "c1", expr = "63 == (bv1 * bv2);"),
            @Constraint(name = "c2", expr = "13 == (bv1 * bv2); bv1 != 1; bv2 != 1;") })
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
                assertTrue(63 == dut.bv1.setLengthHigh(32).intValue()
                    * dut.bv2.setLengthHigh(32).intValue());
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

    }

    public void testBitvectorsDontGoNegative()
    {
        @Randomizable( { @Constraint(name = "c1", expr = "-64 == (-8 * bv);"),
            @Constraint(name = "c2", expr = "-8 == (8 * bv);"),
            @Constraint(name = "c3", expr = "-8 == (8 * bv32);") })
        class Dut
        {
            @Rand
            @Length(8)
            @SuppressWarnings("unused")
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
                assertTrue(-64 == (-8 * dut.bv.intValue()));
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
    }
}
