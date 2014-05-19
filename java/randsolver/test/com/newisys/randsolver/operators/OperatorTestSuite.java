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

import junit.framework.Test;
import junit.framework.TestSuite;

public class OperatorTestSuite
{

    public static Test suite()
    {
        TestSuite suite = new TestSuite(
            "Test for com.newisys.randsolver.operators");
        //$JUnit-BEGIN$
        suite.addTestSuite(Relational.class);
        suite.addTestSuite(AdditionSubtraction.class);
        suite.addTestSuite(Shifts.class);

        // these last two are likely to run out of heap
        suite.addTestSuite(Division.class);
        suite.addTestSuite(Multiplication.class);
        //$JUnit-END$
        return suite;
    }

}
