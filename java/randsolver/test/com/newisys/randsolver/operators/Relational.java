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

import com.newisys.randsolver.Solver;
import com.newisys.randsolver.UnsolvableConstraintException;
import com.newisys.randsolver.annotation.Constraint;
import com.newisys.randsolver.annotation.Rand;
import com.newisys.randsolver.annotation.Randomizable;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.Length;

import junit.framework.TestCase;

public class Relational
    extends TestCase
{
    public void testLessThan()
    {
        @Randomizable( { @Constraint(name = "c1", expr = "int1 < 5;"),
            @Constraint(name = "c2", expr = "bv < 0;"),
            @Constraint(name = "c3", expr = "bv32 < 0;") })
        class Dut
        {
            @Rand
            int int1;

            @Rand
            @Length(16)
            @SuppressWarnings("unused")
            BitVector bv = new BitVector(16);

            @Rand
            @Length(32)
            @SuppressWarnings("unused")
            BitVector bv32 = new BitVector(32);
        }

        final Dut dut = new Dut();

        { // c1
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c1");
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "int1");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(dut.int1 < 5);
            }
        }

        { // c2
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c2");
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "bv");
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

        { // c3
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c3");
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "bv32");
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

    public void testComplex()
    {
        @Randomizable( {
            @Constraint(name = "c1", expr = "int1 < -1; int1 > 0;"),
            @Constraint(name = "c2", expr = "bv < -1; bv > 0;"),
            @Constraint(name = "c3", expr = "bv32 < -1; bv32 > 0;") })
        class Dut
        {
            @Rand
            @SuppressWarnings("unused")
            int int1;

            @Rand
            @Length(16)
            @SuppressWarnings("unused")
            BitVector bv = new BitVector(16);

            @Rand
            @Length(32)
            @SuppressWarnings("unused")
            BitVector bv32 = new BitVector(32);
        }

        final Dut dut = new Dut();

        { // c1
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c1");
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "int1");
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

        { // c2
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c2");
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "bv");
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

        { // c3
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c3");
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "bv32");
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
