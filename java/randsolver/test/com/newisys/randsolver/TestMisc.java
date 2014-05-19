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

@Randomizable
class FinalClass
{
    @Rand
    final int bar = 3;
}

@Randomizable(@Constraint(expr = "bar == 4; subclass.subbar == 2;"))
class FinalSubClass
{
    @Randomizable
    class SubClass
    {
        @Rand
        int subbar = 2;
    }

    @Rand
    int bar = 3;

    @Rand
    final SubClass subclass = new SubClass();
}

public class TestMisc
    extends TestCase
{
    private final PRNG prng = PRNGFactoryFactory.getDefaultFactory()
        .newInstance();

    public void testFinalException()
    {
        // final primitive types are not ok
        final FinalClass fclass = new FinalClass();
        try
        {
            Solver.randomize(fclass, prng);
            assertTrue(false);
        }
        catch (InvalidRandomVarException e)
        {
            assertEquals(fclass.bar, 3);
        }

        // final reference types are ok.
        final FinalSubClass fsubclass = new FinalSubClass();
        Solver.randomize(fsubclass, prng);
        assertEquals(fsubclass.bar, 4);
        assertEquals(fsubclass.subclass.subbar, 2);
    }
}
