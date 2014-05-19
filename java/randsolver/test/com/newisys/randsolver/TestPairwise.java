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
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.Length;

@Randomizable(@Constraint(expr = "i in {1, 2, 3};" + "j in {3, 4, 5};"
    + "x == ((i << 3) | j);"))
class Pairwise
{
    @Rand
    @Length(2)
    BitVector i;
    @Rand
    @Length(3)
    BitVector j;
    @Randc
    @Length(5)
    BitVector x;
}

public class TestPairwise
    extends TestCase
{
    PRNG prng = PRNGFactoryFactory.getDefaultFactory().newInstance();

    public void testPairwise()
    {
        final Pairwise p = new Pairwise();

        final boolean[][] goldenPairs = {
            { false, false, false, false, false, false },
            { false, false, false, true, true, true },
            { false, false, false, true, true, true },
            { false, false, false, true, true, true } };

        for (int i = 0; i < 100; ++i)
        {
            boolean[][] pairs = new boolean[4][6];

            for (int j = 0; j < 9; ++j)
            {
                Solver.randomize(p, prng);
                pairs[p.i.intValue()][p.j.intValue()] = true;
            }

            // check that we hit all valid pairs
            for (int i1 = 0; i1 <= 3; ++i1)
            {
                for (int j1 = 0; j1 <= 5; ++j1)
                {
                    assertEquals(pairs[i1][j1], goldenPairs[i1][j1]);
                }
            }
        }
    }
}
