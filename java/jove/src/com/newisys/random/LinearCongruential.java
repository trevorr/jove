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

package com.newisys.random;

/**
 * A linear congruential pseudorandom number generator, as described by Donald
 * Knuth, The Art of Computer Programming, Volume 2, Section 3.2.1.
 * 
 * @author Trevor Robinson
 */
final class LinearCongruential
    implements PRNGProvider
{
    private static final long serialVersionUID = 3258688789038904624L;

    private long seed;

    private final static long multiplier = 0x5DEECE66DL;
    private final static long addend = 0xBL;
    private final static long mask = (1L << 48) - 1;

    /**
     * Constructs a new linear congruential PRNG with the given 32-bit seed. If
     * adjustSeed is true, the seed is XORed by the multiplier and masked
     * appropriately; otherwise, the seed is used as given.
     *
     * @param seed the 32-bit seed
     * @param adjustSeed indicates whether to adjust the seed
     */
    LinearCongruential(long seed, final boolean adjustSeed)
    {
        if (adjustSeed)
        {
            seed = (seed ^ multiplier) & mask;
        }
        this.seed = seed;
    }

    /*
     * (non-Javadoc)
     * @see com.newisys.random.PRNGProvider#nextBits(int)
     */
    public int nextBits(final int bits)
    {
        if (bits < 0)
        {
            throw new IllegalArgumentException(
                "Must specify a number of bits >= 0");
        }
        else if (bits == 0)
        {
            return 0;
        }

        seed = (seed * multiplier + addend) & mask;
        return (int) (seed >>> (48 - bits));
    }

    /*
     * (non-Javadoc)
     * @see com.newisys.random.PRNGProvider#nextInts(int[], int, int)
     */
    public void nextInts(final int[] array, final int start, final int count)
    {
        if (count < 0)
        {
            throw new IllegalArgumentException("Must specify a count >= 0");
        }
        for (int i = start; i < count; ++i)
        {
            array[i] = nextBits(32);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.newisys.random.PRNGProvider#clone()
     */
    @Override
    public PRNGProvider clone()
    {
        return new LinearCongruential(seed, false);
    }
}
