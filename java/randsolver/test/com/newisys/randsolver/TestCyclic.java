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
import com.newisys.randsolver.annotation.Randc;
import com.newisys.randsolver.annotation.Randomizable;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.Length;

@Randomizable( { @Constraint(name = "c1", expr = "bar in {0:(count-1)};"),
    @Constraint(name = "c2", expr = "bar + bar2 == 2;") })
class CyclicTestClass
{
    final int count = 3;

    @Randc
    @Length(4)
    BitVector bar = new BitVector(4);

    @Randc
    @Length(1)
    BitVector bar2 = new BitVector(1);

    public CyclicTestClass()
    {
        Solver.disableRand(this, "bar2");
        Solver.disableConstraint(this, "c2");
    }

    public void addBar2Constraint()
    {
        Solver.enableRand(this, "bar2");
        Solver.enableConstraint(this, "c2");
    }
}

public class TestCyclic
    extends TestCase
{
    public void testCyclic()
    {
        CyclicTestClass testObj = new CyclicTestClass();
        PRNG prng = PRNGFactoryFactory.getDefaultFactory().newInstance();

        final int fullMask = (1 << testObj.count) - 1;
        int curMask = 0;
        for (int i = 0; i < 50; ++i)
        {
            Solver.randomize(testObj, prng);
            assertTrue(!testObj.bar.containsXZ());

            int value = testObj.bar.intValue();
            assertTrue(value >= 0);
            assertTrue(value <= (testObj.count - 1));

            // check cyclic property
            assertEquals(0, (1 << value) & curMask);
            curMask |= (1 << value);
            if (curMask == fullMask)
            {
                curMask = 0;
            }
        }
    }

    public void testTwoVars()
    {
        CyclicTestClass testObj = new CyclicTestClass();
        testObj.addBar2Constraint();

        PRNG prng = PRNGFactoryFactory.getDefaultFactory().newInstance();

        for (int i = 0; i < 50; ++i)
        {
            Solver.randomize(testObj, prng);
            assertTrue(!testObj.bar.containsXZ());
            assertTrue(!testObj.bar2.containsXZ());

            int value = testObj.bar.intValue();
            int value2 = testObj.bar2.intValue();
            assertTrue(value >= 0);
            assertTrue(value <= (testObj.count - 1));
            assertEquals(2, value + value2);
        }
    }
}
