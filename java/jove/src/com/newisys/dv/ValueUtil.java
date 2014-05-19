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

package com.newisys.dv;

import com.newisys.verilog.util.Bit;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.ValueConverter;

/**
 * Utility class for working with signal values.
 * 
 * @author Trevor Robinson
 */
final class ValueUtil
{
    static Bit getBit(Object value, int index)
    {
        if (value instanceof Bit)
        {
            if (index != 0)
            {
                throw new IllegalArgumentException(
                    "Non-zero index for single-bit value: " + index);
            }
            return (Bit) value;
        }
        else
        {
            BitVector bv = ValueConverter.toBitVector(value);
            return bv.getBit(index);
        }
    }
}
