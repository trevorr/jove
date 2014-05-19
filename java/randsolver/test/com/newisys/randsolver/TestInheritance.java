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

//TODO: add checking with JUnit

public class TestInheritance
    extends TestCase
{
    private PRNG prng;

    public TestInheritance()
    {
        prng = PRNGFactoryFactory.getDefaultFactory().newInstance();
    }

    public void testInheritance()
    {
        A b = new B();

        Solver.randomize(b, prng);
        assertEquals(11, ((B) b).bInt);
        assertEquals(6, b.aInt);
    }

    public void testInheritanceOverride()
    {
        D d = new D();

        Solver.randomize(d, prng);
        assertEquals(2, d.cInt);
    }
}

@Randomizable(@Constraint(name = "c1", expr = "aInt > 4 && aInt < 7;"))
class A
{
    @Rand
    int aInt;
}

@Randomizable( { @Constraint(name = "c2", expr = "bInt == (aInt + 5);"),
    @Constraint(name = "c3", expr = "aInt == 6;") })
class B
    extends A
{
    @Rand
    int bInt;
    int cInt;
}

@Randomizable(@Constraint(name = "c1", expr = "cInt > 5;"))
class C
{
    @Rand
    int cInt;
}

@Randomizable(@Constraint(name = "c1", expr = "cInt == 2;"))
class D
    extends C
{
    // do nothing
}
