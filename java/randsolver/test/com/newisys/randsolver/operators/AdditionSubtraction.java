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

public class AdditionSubtraction
    extends TestCase
{
    public void testBitvectorsDontGoNegative()
    {
        @Randomizable( { @Constraint(name = "c1", expr = "-1 == -5 + bv;"),
            @Constraint(name = "c2", expr = "-1 == -5 + bv32;"),
            @Constraint(name = "c3", expr = "-7 == -5 + bv;"),
            @Constraint(name = "c4", expr = "-7 == -5 + bv32;"),
            @Constraint(name = "c5", expr = "-10 == -5 - bv;"),
            @Constraint(name = "c6", expr = "-10 == -5 - bv32;"),
            @Constraint(name = "c7", expr = "0 == -5 - bv;"),
            @Constraint(name = "c8", expr = "0 == -5 - bv32;") })
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
        }

        final Dut dut = new Dut();

        // c1
        {
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "bv");
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c1");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(-1 == -5 + dut.bv.intValue());
            }
        }
        // c2
        {
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "bv32");
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c2");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(-1 == -5 + dut.bv.intValue());
            }
        }
        // c3
        {
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "bv");
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
        // c5
        {
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "bv");
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c5");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(-10 == -5 - dut.bv.intValue());
            }
        }
        // c6
        {
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "bv32");
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c6");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(-10 == -5 - dut.bv.intValue());
            }
        }
        // c7
        {
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "bv");
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c7");
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
        // c8
        {
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "bv32");
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c8");
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
