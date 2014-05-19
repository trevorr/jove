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

//TODO: add checking with JUnit
/**
 * @author jon.nall
 *
 * TestPrePost
 */

@Randomizable(@Constraint(expr = "bar > 3'h2 && bar < 5; bar != constant;"))
class PrePostTestClass
    implements RandomHooks
{
    @Rand
    @Length(4)
    BitVector bar;
    int constant;

    public PrePostTestClass()
    {
        constant = 3;
        bar = new BitVector(4);
    }

    public void preRandomize()
    {
        ++constant;
    }

    public void postRandomize()
    {
        bar = bar.add(new BitVector(1, 1));
    }
}

public class TestPrePost
    extends TestCase
{
    public void testPrePost()
    {

        PrePostTestClass test = new PrePostTestClass();
        PRNG prng = PRNGFactoryFactory.getDefaultFactory().newInstance();
        for (int i = 0; i < 50; ++i)
        {
            Solver.randomize(test, prng);
            assertTrue((test.bar.intValue() - 1) > 2);
            assertTrue((test.bar.intValue() - 1) < 5);
            assertTrue((test.bar.intValue() - 1) != test.constant);
        }
    }
}
