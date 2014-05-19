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
 * A factory implementation for the linear congruential pseudorandom number
 * generator.
 * 
 * @author Trevor Robinson
 */
public final class LinearCongruentialFactory
    implements PRNGFactory
{
    /**
     * A singleton instance of LinearCongruentialFactory.
     */
    public static final LinearCongruentialFactory INSTANCE = new LinearCongruentialFactory();

    /*
     * (non-Javadoc)
     * @see com.newisys.rng.PRNGFactory#createRNG()
     */
    public PRNG newInstance()
    {
        int seed = (int) System.currentTimeMillis();
        return new PRNGWrapper(new LinearCongruential(seed, true));
    }

    /*
     * (non-Javadoc)
     * @see com.newisys.rng.PRNGFactory#createRNG(long)
     */
    public PRNG newInstance(long seed)
    {
        return new PRNGWrapper(new LinearCongruential((int) seed, true));
    }

    /*
     * (non-Javadoc)
     * @see com.newisys.rng.PRNGFactory#createRNG(com.newisys.rng.PRNG)
     */
    public PRNG newInstance(PRNG seedSource)
    {
        int seed = seedSource.nextBits(32);
        return new PRNGWrapper(new LinearCongruential(seed, true));
    }
}
