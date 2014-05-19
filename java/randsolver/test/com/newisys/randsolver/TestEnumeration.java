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

import java.util.EnumSet;

import junit.framework.TestCase;

import com.newisys.random.PRNG;
import com.newisys.random.PRNGFactoryFactory;
import com.newisys.randsolver.annotation.Constraint;
import com.newisys.randsolver.annotation.Rand;
import com.newisys.randsolver.annotation.RandExclude;
import com.newisys.randsolver.annotation.Randomizable;
import com.newisys.randsolver.mappers.EnumMapper;
import com.newisys.verilog.util.Bit;
import com.newisys.verilog.util.BitVectorBuffer;

enum Color
{
    UNDEFINED, RED, BLUE, GREEN, BLACK;
}

enum ColorWithExcludes
{
    @RandExclude
    UNDEFINED, RED, BLUE, @RandExclude
    GREEN, BLACK;
}

@Randomizable
class TestClass
{
    @Rand
    Color c;
}

@Randomizable
class TestClassWithExcludes
{
    @Rand
    ColorWithExcludes c;
}

@Randomizable(@Constraint(expr = "c in {Color.BLUE, Color.RED};"))
class TestClassWithConstraint
{
    @Rand
    Color c = Color.UNDEFINED;
}

class EnumMapperDUT<E extends Enum<E>>
    extends EnumMapper<E>
{
    public EnumMapperDUT(Class<E> klass, EnumSet<E> excludeSet)
    {
        super(klass, excludeSet);
    }

    @Override
    public com.newisys.randsolver.Constraint getConstraint()
    {
        return null;
    }
}

public class TestEnumeration
    extends TestCase
{
    TestClass a;
    TestClassWithExcludes b;
    PRNG prng;

    @Override
    public void setUp()
    {
        a = new TestClass();
        b = new TestClassWithExcludes();
        prng = PRNGFactoryFactory.getDefaultFactory().newInstance();
    }

    public void testBasicEnum()
    {
        BitVectorBuffer b = new BitVectorBuffer(EnumSet.allOf(Color.class)
            .size(), 0);

        for (int i = 0; i < 100; ++i)
        {
            Solver.randomize(a, prng);
            b.setBit(a.c.ordinal(), Bit.ONE);
        }

        // assert that we choose each enumeration at least once.
        assertEquals(Bit.ONE, b.reductiveAnd());
    }

    public void testEnumWithExcludes()
    {
        BitVectorBuffer buf = new BitVectorBuffer(EnumSet.allOf(
            ColorWithExcludes.class).size(), 0);

        for (int i = 0; i < 100; ++i)
        {
            Solver.randomize(b, prng);
            buf.setBit(b.c.ordinal(), Bit.ONE);
        }

        // assert that we choose each non-RandExclude enumeration at least once
        // and never choose UNDEFINED
        assertEquals("5'b10110", buf.toString(2));
    }

    public void testEnumMapperExceptions()
    {
        EnumMapperDUT<Color> dut = new EnumMapperDUT<Color>(Color.class,
            EnumSet.of(Color.RED, Color.GREEN));
        try
        {
            dut.getID(Color.RED);
            assertTrue("Exception wasn't thrown as expected", false);
        }
        catch (IllegalArgumentException e)
        {
            // good dog
        }

        try
        {
            dut.getObject(Color.GREEN.ordinal());
            assertTrue("Exception wasn't thrown as expected", false);
        }
        catch (IllegalArgumentException e)
        {
            // good dog
        }
    }

    public void testDirectEnumSolving()
    {
        for (int i = 0; i < 100; ++i)
        {
            ColorWithExcludes c = Solver.randomizeEnumType(
                ColorWithExcludes.class, prng);
            assert (c != ColorWithExcludes.GREEN && c != ColorWithExcludes.UNDEFINED);
        }
    }

    public void testEnumConstraint()
    {
        TestClassWithConstraint t = new TestClassWithConstraint();
        for (int i = 0; i < 100; ++i)
        {
            Solver.randomize(t, prng);
            assertTrue(t.c == Color.BLUE || t.c == Color.RED);
        }
    }
}
