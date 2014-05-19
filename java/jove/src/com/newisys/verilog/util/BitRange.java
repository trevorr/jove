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

package com.newisys.verilog.util;

/**
 * An immutable class representing a bit range. It is guaranteed that
 * <code>high()</code> will always return a value greater than or equal to
 * <code>low()</code>.
 * 
 * @author Jon Nall
 */
public final class BitRange
{
    final int high;
    final int low;

    /**
     * Creates a new BitRange with the specified values. The <code>high</code>
     * and <code>low</code> values specified are inclusive.
     *
     * @param high the high bit of the range (inclusive)
     * @param low the low bit of the range (inclusive)
     * @throws IllegalArgumentException if <code>high &lt; low</code>
     */
    public BitRange(int high, int low)
    {
        if (high < low)
        {
            throw new IllegalArgumentException(
                "Illegal BitRange (high < low) high: " + high + "low: " + low);
        }
        this.high = high;
        this.low = low;
    }

    /**
     * Returns the high bit of this BitRange.
     *
     * @return the high bit of this BitRange
     */
    public int high()
    {
        return high;
    }

    /**
     * Returns the low bit of this BitRange.
     *
     * @return the low bit of this BitRange
     */
    public int low()
    {
        return low;
    }

    /**
     * Returns the number of bits in this BitRange
     * (i.e. <code>high - low + 1</code>).
     *
     * @return the number of bits in this BitRange
     */
    public int length()
    {
        return high - low + 1;
    }

    /**
     * Returns a String representation of this BitRange.
     *
     * @return a String representation of this BitRange
     * @see Object#toString
     */
    @Override
    public String toString()
    {
        return "[" + high + ":" + low + "]";
    }

}
