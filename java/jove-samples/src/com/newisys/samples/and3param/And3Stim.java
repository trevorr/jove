/*
 * Jove - The Open Verification Environment for the Java (TM) Platform
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

package com.newisys.samples.and3param;

import com.newisys.randsolver.annotation.Constraint;
import com.newisys.randsolver.annotation.Rand;
import com.newisys.randsolver.annotation.Randomizable;

/**
 * This class is little more than a stimulus generator for the testbench.
 * Further, its constraint is meant more as an example than anything. There is
 * one constraint, named "cc1", which is responsible for constraining
 * <code>value</code> to a 3 bit value.
 */
@Randomizable(@Constraint(name = "cc1", expr = "value in {min:max};"))
public final class And3Stim
{

    /**
     * The value to be randomized. It is annotated with {@link Rand} to denote
     * it should be randomized. Further it is private; access modifiers have no
     * bearing on whether or not a variable can be randomized (assuming the
     * default java security policy is in effect).
     */
    @Rand
    private int value;

    /**
     * These are variables to be used by the constraint. For performance reasons
     * it is always wise to make state variables (i.e. variables used in
     * constraints, but not being randomized) <code>static</code> and
     * <code>final</code>.
     */
    @SuppressWarnings("unused")
    private static final int max = 7;
    @SuppressWarnings("unused")
    private static final int min = 0;

    /**
     * Returns the value for this stimulus.
     *
     * @return the current value of <code>value</code>
     */
    public int getValue()
    {
        return value;
    }
}
