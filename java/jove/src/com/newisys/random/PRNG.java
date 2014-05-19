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

/**
 * Pseudorandom number generator interface.
 * 
 * @author Trevor Robinson
 */
public interface PRNG
    extends PRNGProvider
{
    public PRNGProvider getPRNGProvider();

    /*
     * (non-Javadoc)
     * @see com.newisys.prng.PRNGProvider#nextBits(int)
     */
    public int nextBits(final int bits);

    /*
     * (non-Javadoc)
     * @see com.newisys.prng.PRNGProvider#nextInts(int[], int, int)
     */
    public void nextInts(final int[] array, final int start, final int count);

    /**
     * Returns the next pseudorandom, uniformly distributed <code>boolean</code>
     * value from the generator's sequence.
     *
     * @return a boolean
     */
    public boolean nextBoolean();

    /**
     * Returns a pseudorandom <code>boolean</code> value from the generator's
     * sequence with the given probability that it will be <code>true</code>.
     *
     * @param probability a <code>float</code> between 0 and 1 (inclusive)
     * @return a boolean
     */
    public boolean nextBoolean(final float probability);

    /**
     * Returns a pseudorandom <code>boolean</code> value from the generator's
     * sequence with the given probability that it will be <code>true</code>.
     *
     * @param probability a <code>double</code> between 0 and 1 (inclusive)
     * @return a boolean
     */
    public boolean nextBoolean(final double probability);

    /**
     * Returns the next pseudorandom, uniformly distributed <code>int</code>
     * value from the generator's sequence.
     *
     * @return an int
     */
    public int nextInt();

    /**
     * Returns the next pseudorandom, uniformly distributed, positive
     * <code>int</code> value between <code>0</code> (inclusive) and
     * <code>limit</code> (exclusive) from the generator's sequence.
     *
     * @param limit an <code>int</code> greater than 0 that the generated
     *            <code>int</code> must be less than
     * @return an int between <code>0</code> (inclusive) and
     *         <code>limit</code> (exclusive)
     */
    public int nextInt(final int limit);

    /**
     * Returns the next pseudorandom, uniformly distributed <code>int</code>
     * value between <code>min</code> and <code>max</code> (inclusive) from
     * the generator's sequence.
     *
     * @param min the minimum value to return
     * @param max the maximum value to return
     * @return an int between <code>min</code> and <code>max</code>
     *         (inclusive)
     */
    public int nextInt(final int min, final int max);

    /**
     * Returns the next pseudorandom, uniformly distributed <code>long</code>
     * value from the generator's sequence.
     *
     * @return a long
     */
    public long nextLong();

    /**
     * Returns the next pseudorandom, uniformly distributed <code>float</code>
     * value between <code>0.0</code> and <code>1.0</code> from the
     * generator's sequence.
     *
     * @return a float
     */
    public float nextFloat();

    /**
     * Returns the next pseudorandom, uniformly distributed <code>double</code>
     * value between <code>0.0</code> and <code>1.0</code> from the
     * generator's sequence.
     *
     * @return a double
     */
    public double nextDouble();

    /**
     * Returns the next pseudorandom, uniformly distributed
     * <code>BitVector</code> value between 0 and
     * 2<sup><code>numBits</code></sup>.
     *
     * @param numBits the length of the BitVector to be returned, which must be
     *      greater than 0
     * @return a BitVector
     * @throws IllegalArgumentException if <code>numBits &lt;= 0</code>.
     */
    public BitVector nextBitVector(int numBits);

    /**
     * Returns a new instance of this PRNG with the same state, meaning
     * that each instance will independently generate the same sequence.
     *
     * @return a clone of this PRNG
     */
    public PRNG clone();
}
