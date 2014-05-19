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

/*
 * Adapted from mt19937ar.c:
 *
 * A C-program for MT19937, with initialization improved 2002/1/26.
 * Coded by Takuji Nishimura and Makoto Matsumoto.
 *
 * Before using, initialize the state by using init_genrand(seed)
 * or init_by_array(init_key, key_length).
 *
 * Copyright (C) 1997 - 2002, Makoto Matsumoto and Takuji Nishimura,
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The names of its contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 * Any feedback is very welcome.
 * http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/emt.html
 * email: m-mat @ math.sci.hiroshima-u.ac.jp (remove space)
 */

package com.newisys.random;

/**
 * A pseudorandom number generator based on the Mersenne Twister algorithm.
 * 
 * @author Makoto Matsumoto and Takuji Nishimura (original C version)
 * @author Trevor Robinson (Java translation)
 */
final class MersenneTwister
    implements PRNGProvider
{
    private static final long serialVersionUID = 3256726177879109939L;

    public static final int N = 624; // size of the state vector

    private int mt[]; // array for the state vector
    private int mti; // next entry to use is mt[mti]

    /**
     * Constructs a new MersenneTwister with the given 32-bit seed.
     *
     * @param seed a 32-bit seed
     */
    MersenneTwister(final int seed)
    {
        setSeed(seed);
    }

    /**
     * Constructs a new MersenneTwister with the given 624-int seed.
     *
     * @param seed a 624-int seed
     */
    MersenneTwister(final int[] seed)
    {
        setSeed(seed);
    }

    private MersenneTwister(final MersenneTwister other)
    {
        mt = other.mt.clone();
        mti = other.mti;
    }

    private void setSeed(final int seed)
    {
        mt = new int[N];
        mt[0] = seed;
        for (mti = 1; mti < N; ++mti)
        {
            mt[mti] = (1812433253 * (mt[mti - 1] ^ (mt[mti - 1] >>> 30)) + mti);
            // See Knuth TAOCP Vol2. 3rd Ed. P.106 for multiplier.
            // In the previous versions, MSBs of the seed affect
            // only MSBs of the array mt[].
            // 2002/01/09 modified by Makoto Matsumoto
        }
    }

    private void setSeed(final int[] seed)
    {
        setSeed(19650218);
        int i = 1;
        int j = 0;
        int k = (N > seed.length ? N : seed.length);
        for (; k != 0; --k)
        {
            // non-linear
            mt[i] = (mt[i] ^ ((mt[i - 1] ^ (mt[i - 1] >>> 30)) * 1664525))
                + seed[j] + j;
            ++i;
            ++j;
            if (i >= N)
            {
                mt[0] = mt[N - 1];
                i = 1;
            }
            if (j >= seed.length) j = 0;
        }
        for (k = N - 1; k != 0; --k)
        {
            // non-linear
            mt[i] = (mt[i] ^ ((mt[i - 1] ^ (mt[i - 1] >>> 30)) * 1566083941))
                - i;
            ++i;
            if (i >= N)
            {
                mt[0] = mt[N - 1];
                i = 1;
            }
        }
        // MSB is 1; assuring non-zero initial array
        mt[0] = 0x80000000;
    }

    private int nextInt()
    {
        // generate N words at one time
        if (mti >= N)
        {
            final int[] mt1 = this.mt; // cache this locally
            final int M = 397;
            final int MATRIX_A = 0x9908b0df; // constant vector a
            final int UPPER_MASK = 0x80000000; // most significant w-r bits
            final int LOWER_MASK = 0x7fffffff; // least significant r bits
            int kk;

            for (kk = 0; kk < N - M; ++kk)
            {
                int y3 = (mt1[kk] & UPPER_MASK) | (mt1[kk + 1] & LOWER_MASK);
                mt1[kk] = mt1[kk + M] ^ (y3 >>> 1)
                    ^ ((y3 & 1) != 0 ? MATRIX_A : 0);
            }
            for (; kk < N - 1; ++kk)
            {
                int y1 = (mt1[kk] & UPPER_MASK) | (mt1[kk + 1] & LOWER_MASK);
                mt1[kk] = mt1[kk + (M - N)] ^ (y1 >>> 1)
                    ^ ((y1 & 1) != 0 ? MATRIX_A : 0);
            }
            {
                int y2 = (mt1[N - 1] & UPPER_MASK) | (mt1[0] & LOWER_MASK);
                mt1[N - 1] = mt1[M - 1] ^ (y2 >>> 1)
                    ^ ((y2 & 1) != 0 ? MATRIX_A : 0);
            }
            mti = 0;
        }

        int y = mt[mti++];

        // tempering
        y ^= y >>> 11;
        y ^= (y << 7) & 0x9d2c5680;
        y ^= (y << 15) & 0xefc60000;
        y ^= (y >>> 18);

        return y;
    }

    /*
     * (non-Javadoc)
     * @see com.newisys.rng.PRNGProvider#nextBits(int)
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

        return nextInt() >>> (32 - bits);
    }

    /*
     * (non-Javadoc)
     * @see com.newisys.rng.PRNGProvider#nextInts(int[], int, int)
     */
    public void nextInts(final int[] array, final int start, final int count)
    {
        if (count < 0)
        {
            throw new IllegalArgumentException("Must specify a count >= 0");
        }
        for (int i = start; i < count; ++i)
        {
            array[i] = nextInt();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.newisys.rng.PRNGProvider#clone()
     */
    @Override
    public PRNGProvider clone()
    {
        return new MersenneTwister(this);
    }
}
