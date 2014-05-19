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
 * A factory for creating pseudorandom number generators of a particular kind.
 * 
 * @author Trevor Robinson
 */
public interface PRNGFactory
{
    /**
     * Creates a new pseudorandom number generator with an arbitrarily chosen
     * seed. Generally, this seed will be a non-constant value, such as the
     * current system time, so this method is not suitable for cases where
     * reproducibility is desired.
     *
     * @return a new PRNG instance
     */
    PRNG newInstance();

    /**
     * Creates a new pseudorandom number generator with the given seed. For a
     * given factory, if two generators are created with the same seed, they
     * will produce the same sequence of values.
     *
     * @param seed the value used to seed the new generator
     * @return a new PRNG instance
     */
    PRNG newInstance(long seed);

    /**
     * Creates a new pseudorandom number generator with a seed generated using
     * the given generator. This method is useful for creating families of
     * independent random number generators all seeded from the same source.
     *
     * @param seedSource the source of random data used to seed the new
     *            generator
     * @return a new PRNG instance
     */
    PRNG newInstance(PRNG seedSource);
}
