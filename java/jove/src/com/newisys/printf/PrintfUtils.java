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

package com.newisys.printf;

import java.math.BigInteger;

import com.newisys.verilog.util.Bit;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.BitVectorBuffer;

/**
 * Static methods which are useful for a variety of {@link ConversionFormatter}
 * objects.
 * 
 * @author Jon Nall
 */
final class PrintfUtils
{
    /**
     * Returns the width in bits of the specified Object. Supported types
     * include {@link Boolean} and most classes implementing {@link Number}.
     *
     * @param arg the object to check
     * @return the number of bits required by the type of <code>arg</code>
     * @throws InvalidFormatSpecException if <code>arg</code> is not a
     *      supported type
     */
    public static final int getWidth(Object arg)
    {
        Class c = arg.getClass();
        if (c == Bit.class || c == Boolean.class)
            return 1;
        else if (c == Byte.class)
            return 8;
        else if (c == Short.class)
            return 16;
        else if (c == Integer.class || arg instanceof Enum< ? >)
            return 32;
        else if (c == Long.class)
            return 64;
        else if (c == BitVectorBuffer.class)
            return ((BitVectorBuffer) arg).length();
        else if (c == BitVector.class)
            return ((BitVector) arg).length();
        else if (c == BigInteger.class)
        {
            BigInteger bigInt = (BigInteger) arg;
            boolean isNegative = bigInt.signum() < 0;
            return bigInt.bitLength() + (isNegative ? 1 : 0);
        }
        else
            throw new InvalidFormatSpecException("Unsupported numeric type: "
                + c);
    }
}
