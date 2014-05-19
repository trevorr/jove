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

import com.newisys.randsolver.InvalidConstraintException;
import com.newisys.randsolver.Solver;
import com.newisys.randsolver.UnsolvableConstraintException;
import com.newisys.randsolver.annotation.Constraint;
import com.newisys.randsolver.annotation.Rand;
import com.newisys.randsolver.annotation.Randomizable;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.Length;

public class Shifts
    extends TestCase
{
    public void testUnsignedRightShift()
    {
        @Randomizable( { @Constraint(name = "c1", expr = "int1 >>> 10 == 10;"),
            @Constraint(name = "c2", expr = "0x80 >>> int1 == 0x10;"),
            @Constraint(name = "c3", expr = "0x80 >>> 5 == int1;"),

            @Constraint(name = "c4", expr = "((16'h8A5A >>> bv) & 0x7) == 4;"),
            @Constraint(name = "c5", expr = "-1 >>> bv == 1;"),

            @Constraint(name = "c6", expr = "8 >>> int1 == 2; int1 < 0;") })
        class Dut
        {
            @Rand
            int int1;

            @Rand
            @Length(8)
            BitVector bv = new BitVector(8, 0);

            public Dut()
            {
                Solver.disableAllConstraints(this);
                Solver.disableAllRand(this);
            }
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
                assertEquals(10, dut.int1 >>> 10);
            }
        }

        { // c2
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c2");
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "int1");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertEquals(0x10, 0x80 >>> dut.int1);
            }
        }

        { // c3
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c3");
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "int1");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertEquals(dut.int1, 0x80 >>> 5);
            }
        }

        { // c4
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c4");
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "bv");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertEquals(4, (0x8A5A >> dut.bv.intValue()) & 0x7);
            }
        }

        { // c5
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c5");
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "bv");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertEquals(1, -1 >>> dut.bv.intValue());
            }
        }

        { // c6
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c6");
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
    }

    public void testSignedRightShift()
    {
        @Randomizable( {
            @Constraint(name = "c1", expr = "0x80000000 >> int1 == -1;"),
            @Constraint(name = "c2", expr = "0x40000000 >> int1 == 0;"),
            @Constraint(name = "c3", expr = "((0x40000000 >> int1) >> int2) == 0;"),

            @Constraint(name = "c4", expr = "8 >> int1 == 2; int1 < 0;") })
        class Dut
        {
            @Rand
            int int1;

            @Rand
            int int2;

            public Dut()
            {
                Solver.disableAllConstraints(this);
                Solver.disableAllRand(this);
            }
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
                assertTrue(dut.int1 >= 31);
            }
        }

        { // c2
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c2");
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "int1");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertTrue(dut.int1 >= 31);
            }
        }

        { // c3
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c3");
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "int1");
            Solver.enableRand(dut, "int2");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                // allow for two huge numbers (overflow)
                assertTrue(dut.int1 + dut.int2 >= 31 || dut.int1 + dut.int2 < 0);
            }
        }

        { // c4
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c4");
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
    }

    public void testLeftShift()
    {
        @Randomizable( {
            @Constraint(name = "c1", expr = "int1 << 16 == 0xff0000;"),
            @Constraint(name = "c2", expr = "32'hff << int1 == 0xff0000;"),
            @Constraint(name = "c3", expr = "int1 == 0xff << 16;"),
            @Constraint(name = "c4", expr = "int1 << (0-10) == 0xff0000;") })
        class Dut
        {
            @Rand
            int int1;

            public Dut()
            {
                Solver.disableAllConstraints(this);
                Solver.disableAllRand(this);
            }
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
                assertEquals(0x00FF0000, dut.int1 << 16);
            }
        }

        { // c2
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c2");
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "int1");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertEquals(0x00FF0000, 0xFF << dut.int1);
            }
        }

        { // c3
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c3");
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "int1");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                Solver.randomize(dut, prng);
                assertEquals(dut.int1, 0xFF << 16);
            }
        }

        { // c4
            Solver.disableAllConstraints(dut);
            Solver.enableConstraint(dut, "c4");
            Solver.disableAllRand(dut);
            Solver.enableRand(dut, "int1");
            for (int i = 0; i < RANDOM_ITERS; ++i)
            {
                try
                {
                    Solver.randomize(dut, prng);
                    assertTrue(false);
                }
                catch (InvalidConstraintException e)
                {
                    // good
                }
            }
        }
    }
}
