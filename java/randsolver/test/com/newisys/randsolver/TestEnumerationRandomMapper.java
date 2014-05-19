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

import com.newisys.verilog.util.BitVector;

/**
 * @author jon.nall
 *
 * TestEnumerationRandomMapper
 */
public class TestEnumerationRandomMapper
    implements RandomMapper
{
    @SuppressWarnings("unused")
    private BitVector randomBit = new BitVector(4);
    private Constraint constraint = null;

    public Constraint getConstraint()
    {
        if (constraint == null)
        {
            constraint = ConstraintCompiler
                .compile(getClass(),
                    "randomBit in {0:2, 6, 10}; randomBit != 6; (randomBit & 1) in {0:1};");
        }

        return constraint;
    }

    public int getID(Object o)
    {
        if (o == InternalEnum.ZERO)
            return 0;
        else if (o == InternalEnum.ONE)
            return 1;
        else if (o == InternalEnum.TWO)
            return 2;
        else if (o == InternalEnum.TEN)
            return 10;
        else
            throw new NoSuchElementException("Unknown TestEnumeration: " + o);
    }

    public Object getObject(int id)
    {
        switch (id)
        {
        case 0:
            return InternalEnum.ZERO;
        case 1:
            return InternalEnum.ONE;
        case 2:
            return InternalEnum.TWO;
        case 10:
            return InternalEnum.TEN;
        default:
            return null;
        }
    }

}
