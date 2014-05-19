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
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.Length;

// TODO: add checking with JUnit

public class TestSubobject
    extends TestCase
{
    @Randomizable(@Constraint(expr = "s >= 7 && s <= 10 || s == constant || s == constantVect;"))
    static class SubSubClass
        implements RandomHooks
    {
        @Rand
        short s;
        int constant;

        @Rand
        @Length(5)
        BitVector foo;
        BitVector constantVect;

        public SubSubClass()
        {
            s = 12;
            constant = 15;
            constantVect = new BitVector(3, 2);
            foo = new BitVector(5);
        }

        public void preRandomize()
        {
            // do nothing
        }

        public void postRandomize()
        {
            ++s;
        }
    }

    @Randomizable(@Constraint(expr = "foo in {1, 10, 5'h15}; ssClass.s != 9;"))
    class SubClass
        implements RandomHooks
    {
        @Rand
        @Length(5)
        BitVector foo;
        @Rand
        SubSubClass ssClass;

        public SubClass()
        {
            ssClass = new SubSubClass();
            foo = new BitVector(5);
        }

        public void preRandomize()
        {
            // do nothing
        }

        public void postRandomize()
        {
            foo = foo.add(new BitVector(1, 1));
        }
    }

    @Randomizable(@Constraint(expr = "subClass.foo != 10; mInt == 4; subClass.ssClass.s != 8;"))
    class TestClass
        implements RandomHooks
    {
        @Rand
        private SubClass subClass;
        @Rand
        private int mInt;

        public TestClass()
        {
            mInt = 13;
            subClass = new SubClass();
        }

        public void preRandomize()
        {
            // do nothing
        }

        public void postRandomize()
        {
            subClass.foo = subClass.foo.add(new BitVector(1, 1));
        }
    }

    @SuppressWarnings("synthetic-access")
    public void testSubObject()
    {
        TestClass test = new TestClass();
        PRNG prng = PRNGFactoryFactory.getDefaultFactory().newInstance();
        for (int i = 0; i < 1; ++i)
        {
            Solver.randomize(test, prng);
            short s = test.subClass.ssClass.s;
            byte f1 = test.subClass.foo.byteValue();
            byte f2 = test.subClass.ssClass.foo.byteValue();

            assertEquals(4, test.mInt);
            assertTrue(f1 == 3 || f1 == 0x17);
            assertTrue(s == 3 || s == 8 || s == 11 || s == 16);
            assertTrue(f2 >= 0 && f2 <= 0x1f);
        }
    }
}
