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
/**
 * @author jon.nall
 *
 * TestEnumeration
 */

public class TestEnumerationMapper
    extends TestCase
{
    public void testEnumeration()
    {
        EnumTestClass test = new EnumTestClass();
        PRNG prng = PRNGFactoryFactory.getDefaultFactory().newInstance();
        for (int i = 0; i < 100; ++i)
        {
            Solver.randomize(test, prng);
            assertTrue(test.mEnum == InternalEnum.ZERO
                || test.mEnum == InternalEnum.ONE
                || test.mEnum == InternalEnum.TWO
                || test.mEnum == InternalEnum.TEN);
            assertEquals(Integer.MAX_VALUE, test.bar);
        }
    }
}

class InternalEnum
    implements RandomHooks
{
    private String mString;

    static
    {
        RandomMapperRegistry.registerMapper(InternalEnum.class,
            new TestEnumerationRandomMapper());
    }

    private InternalEnum(String s)
    {
        mString = s;
    }

    @Override
    public String toString()
    {
        return mString;
    }

    public void preRandomize()
    {
        // do nothing
    }

    public void postRandomize()
    {
        // do nothing
    }

    public final static InternalEnum ZERO = new InternalEnum("ZERO");
    public final static InternalEnum ONE = new InternalEnum("ONE");
    public final static InternalEnum TWO = new InternalEnum("TWO");
    public final static InternalEnum TEN = new InternalEnum("TEN");

}

@Randomizable(@Constraint(expr = "bar == java.lang.Integer.MAX_VALUE;"))
class EnumTestClass
{
    class SubClass
    {
        public int foo;
    }

    @Rand
    InternalEnum mEnum;

    @Rand
    int bar;

    SubClass boom;
}
