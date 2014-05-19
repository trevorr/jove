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

package com.newisys.randsolver.mappers;

import com.newisys.randsolver.Constraint;
import com.newisys.randsolver.ConstraintCompiler;

public final class TwoStateBitMapper
    extends BitMapper
{
    private boolean bit;
    private static Constraint constraint = constraint = ConstraintCompiler
        .compile(TwoStateBitMapper.class, "bit in {0:1};");

    public Constraint getConstraint()
    {
        return constraint;
    }
}