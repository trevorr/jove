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

import java.util.NoSuchElementException;

import junit.framework.TestCase;

import com.newisys.random.PRNG;
import com.newisys.random.PRNGFactoryFactory;
import com.newisys.randsolver.annotation.Rand;
import com.newisys.randsolver.annotation.Randomizable;
import com.newisys.verilog.util.BitVector;

//TODO: automate testing of RogueMapperFactory

class DUTMapperFactory
    implements RandomMapperFactory
{
    public RandomMapper getConstraintMapper(Class c)
    {
        if (c == TestEnum.class)
        {
            return new TestMapper();
        }

        return null;
    }
}

class RogueMapperFactory
    implements RandomMapperFactory
{
    public RandomMapper getConstraintMapper(Class c)
    {
        if (c == TestEnum.class)
        {
            return new TestMapper();
        }

        return null;
    }
}

class TestMapper
    implements RandomMapper
{
    @SuppressWarnings("unused")
    private BitVector randomBit = new BitVector(4);
    private Constraint constraint = null;

    public Constraint getConstraint()
    {
        if (constraint == null)
        {
            constraint = ConstraintCompiler.compile(getClass(),
                "randomBit in {0:2, 6, 10}; randomBit != 6;");
        }

        return constraint;
    }

    public int getID(Object o)
    {
        if (o == TestEnum.ZERO)
            return 0;
        else if (o == TestEnum.ONE)
            return 1;
        else if (o == TestEnum.TWO)
            return 2;
        else if (o == TestEnum.TEN)
            return 10;
        else
            throw new NoSuchElementException("Unknown TestEnum: " + o);
    }

    public Object getObject(int id)
    {
        switch (id)
        {
        case 0:
            return TestEnum.ZERO;
        case 1:
            return TestEnum.ONE;
        case 2:
            return TestEnum.TWO;
        case 10:
            return TestEnum.TEN;
        default:
            return null;
        }
    }
}

class TestEnum
{
    int mInt;
    String mStr;

    private TestEnum(int i, String s)
    {
        mInt = i;
        mStr = s;
    }

    @Override
    public String toString()
    {
        return mStr;
    }

    public static final TestEnum ZERO = new TestEnum(0, "ZERO");
    public static final TestEnum ONE = new TestEnum(1, "ONE");
    public static final TestEnum TWO = new TestEnum(2, "TWO");
    public static final TestEnum TEN = new TestEnum(10, "TEN");
}

public class TestMapperFactory
    extends TestCase
{
    private static PRNG rng = PRNGFactoryFactory.getDefaultFactory()
        .newInstance();

    @Randomizable
    class TestClass
    {
        @Rand
        TestEnum t = TestEnum.TWO;
    }

    @Randomizable
    class TestClass2
    {
        @Rand
        String t = "blah";
    }

    static
    {
        RandomMapperRegistry.registerMapperFactory(new DUTMapperFactory());

        // Uncomment to test 1-hot assertion in RandomMapperRegistry
        // RandomMapperRegistry.registerMapperFactory(new RogueMapperFactory());
    }

    public void testMapperFactory()
    {
        TestClass t = new TestClass();
        for (int i = 0; i < 100; ++i)
        {
            Solver.randomize(t, rng);
            assertTrue(t.t == TestEnum.ZERO || t.t == TestEnum.ONE
                || t.t == TestEnum.TWO || t.t == TestEnum.TEN);
        }

        TestClass2 t2 = new TestClass2();
        try
        {
            Solver.randomize(t2, rng);
            assertTrue("Exception wasn't thrown as expected", false);
        }
        catch (InvalidConstraintException e)
        {
            // should get here, since TestClass2.t is not randomizable and
            // a mapper does not exist for String
        }
    }
}
