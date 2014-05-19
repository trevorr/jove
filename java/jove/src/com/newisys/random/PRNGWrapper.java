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

import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.BitVectorBuffer;

/**
 * Convenience class for generating pseudorandom numbers. The actual random data
 * generation is performed by an underlying PRNG instance.
 * 
 * @author Trevor Robinson
 */
final class PRNGWrapper
    implements PRNG
{
    private static final long serialVersionUID = 3905242333854511412L;

    private final PRNGProvider prng;

    /**
     * Constructs a new PRNGWrapper use the PRNG returned by
     * <code>PRNGProvider.getDefaultFactory().newInstance()</code>.
     */
    public PRNGWrapper()
    {
        this(PRNGFactoryFactory.getDefaultFactory().newInstance());
    }

    /**
     * Constructs a new PRNGWrapper use the PRNG returned by
     * <code>PRNGProvider.getDefaultFactory().newInstance(seed)</code>.
     *
     * @param seed the seed for the PRNG
     */
    public PRNGWrapper(final long seed)
    {
        this(PRNGFactoryFactory.getDefaultFactory().newInstance(seed));
    }

    /**
     * Constructs a new PRNGWrapper using the given PRNG.
     *
     * @param prng the PRNG to use
     */
    public PRNGWrapper(final PRNGProvider prng)
    {
        this.prng = prng;
    }

    public PRNGProvider getPRNGProvider()
    {
        return prng;
    }

    /*
     * (non-Javadoc)
     * @see com.newisys.prng.PRNG#nextBits(int)
     */
    /* (non-Javadoc)
     * @see com.newisys.random.PRNG#nextBits(int)
     */
    public int nextBits(final int bits)
    {
        return prng.nextBits(bits);
    }

    /*
     * (non-Javadoc)
     * @see com.newisys.prng.PRNG#nextInts(int[], int, int)
     */
    /* (non-Javadoc)
     * @see com.newisys.random.PRNG#nextInts(int[], int, int)
     */
    public void nextInts(final int[] array, final int start, final int count)
    {
        prng.nextInts(array, start, count);
    }

    /* (non-Javadoc)
     * @see com.newisys.random.PRNG#nextBoolean()
     */
    public boolean nextBoolean()
    {
        return prng.nextBits(1) != 0;
    }

    /* (non-Javadoc)
     * @see com.newisys.random.PRNG#nextBoolean(float)
     */
    public boolean nextBoolean(final float probability)
    {
        if (probability > 0.0f && probability < 1.0f)
        {
            return nextFloat() < probability;
        }
        else if (probability == 0.0f)
        {
            return false;
        }
        else if (probability == 1.0f)
        {
            return true;
        }

        throw new IllegalArgumentException(
            "Probability must be between 0 and 1 (inclusive)");
    }

    /* (non-Javadoc)
     * @see com.newisys.random.PRNG#nextBoolean(double)
     */
    public boolean nextBoolean(final double probability)
    {
        if (probability > 0.0 && probability < 1.0)
        {
            return nextDouble() < probability;
        }
        else if (probability == 0.0)
        {
            return false;
        }
        else if (probability == 1.0)
        {
            return true;
        }

        throw new IllegalArgumentException(
            "Probability must be between 0 and 1 (inclusive)");
    }

    /* (non-Javadoc)
     * @see com.newisys.random.PRNG#nextInt()
     */
    public int nextInt()
    {
        return prng.nextBits(32);
    }

    /* (non-Javadoc)
     * @see com.newisys.random.PRNG#nextInt(int)
     */
    public int nextInt(final int limit)
    {
        if (limit <= 0)
        {
            throw new IllegalArgumentException("Limit must be greater than 0");
        }

        // optimize power-of-2 limit case
        if ((limit & -limit) == limit)
        {
            // use high-order bits to avoid short period in low-order bits of
            // linear congruential generators
            return (int) ((limit * (long) prng.nextBits(31)) >> 31);
        }

        // reject values of 'bits' that result in an uneven distribution
        //
        // Given:
        //   val = bits % limit
        //     -> bits = val + k * limit, k >= 0
        // Reject 'bits' if:
        //   max(val) + k * limit > max(bits)
        //     -> (limit - 1) + k * limit > 2^31 - 1
        //     -> (limit - 1) + (bits - val) < 0
        int bits, val;
        do
        {
            bits = prng.nextBits(31);
            val = bits % limit;
        }
        while ((limit - 1) + (bits - val) < 0);
        return val;
    }

    /* (non-Javadoc)
     * @see com.newisys.random.PRNG#nextInt(int, int)
     */
    public int nextInt(final int min, final int max)
    {
        if (min > max)
        {
            throw new IllegalArgumentException("Minimum must be <= maximum");
        }

        return nextInt(max - min + 1) + min;
    }

    /* (non-Javadoc)
     * @see com.newisys.random.PRNG#nextLong()
     */
    public long nextLong()
    {
        return ((long) prng.nextBits(32) << 32) + prng.nextBits(32);
    }

    /* (non-Javadoc)
     * @see com.newisys.random.PRNG#nextFloat()
     */
    public float nextFloat()
    {
        int i = prng.nextBits(24);
        return i / ((float) (1 << 24));
    }

    /* (non-Javadoc)
     * @see com.newisys.random.PRNG#nextDouble()
     */
    public double nextDouble()
    {
        long l = ((long) (prng.nextBits(26)) << 27) + prng.nextBits(27);
        return l / (double) (1L << 53);
    }

    /* (non-Javadoc)
     * @see com.newisys.random.PRNG#nextBitVector(int)
     */
    public BitVector nextBitVector(int numBits)
    {
        if (numBits <= 0)
        {
            throw new IllegalArgumentException("numBits <= 0. numBits: "
                + numBits);
        }

        BitVectorBuffer buf = new BitVectorBuffer(numBits);

        final int numInts = (numBits / 32) + ((numBits % 32 == 0) ? 0 : 1);
        for (int i = 0; i < numInts; ++i)
        {
            final int curLoBit = i * 32;
            if (numBits >= 32)
            {
                buf.setBits(curLoBit + 31, curLoBit, prng.nextBits(32));
                numBits -= 32;
            }
            else
            {
                assert (i == numInts - 1);
                buf.setBits(curLoBit + numBits - 1, curLoBit, prng
                    .nextBits(numBits));
            }
        }

        return buf.toBitVector();
    }

    /*
     * (non-Javadoc)
     * @see com.newisys.prng.PRNG#clone()
     */
    @Override
    public PRNG clone()
    {
        return new PRNGWrapper(prng.clone());
    }
}
