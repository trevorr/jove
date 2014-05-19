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

import java.util.List;
import java.util.Map;

import com.newisys.random.PRNG;

abstract class ExprSolver
{

    String mName; // String representation
    protected RandVarSet mVars; // Set of variables this solver solves for

    protected abstract boolean needsReevaluation(RandVarSet varSet, Object obj);

    // returns a list of RandomVariables which had their cyclic constraints
    // reset
    protected abstract List solve(
        Constraint constraint,
        RandVarSet constrainedVars,
        RandVarSet unconstrainedVars);

    // returns a map of RandomVariable->BDD mappings for cyclic constraints
    // that were added
    protected abstract Map commit(Object obj, PRNG rng)
        throws IllegalAccessException;

    public abstract boolean isComplex();

    protected ExprSolver(String name)
    {
        mName = name;
    }

    @Override
    public String toString()
    {
        return mName + "@" + hashCode();
    }

}
