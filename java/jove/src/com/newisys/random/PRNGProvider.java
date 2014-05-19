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

import java.io.Serializable;

/**
 * Basic interface for a pseudorandom number generator. It allows PRNG
 * implementors to focus on the core functionality (i.e. providing a
 * serializable, cloneable stream of random bits) and use PRNGWrapper to
 * generically implement the remaining PRNG methods.
 * 
 * @author Trevor Robinson
 */
public interface PRNGProvider
    extends Serializable, Cloneable
{
    /**
     * Returns an int containing the given number of random bits. Some random
     * number generators will produce a fixed number of random bits each time
     * this method is called, and discard any unneeded bits, so it may not be
     * efficient to call this method repeatedly for a small number of bits.
     *
     * @param bits the number of random bits requested, which must be greater
     *       than or equal to zero
     * @return a random int less than (1 &lt;&lt; bits)
     */
    int nextBits(int bits);

    /**
     * Fills the given array with random ints, as if nextBits(32) were called
     * for each element.
     *
     * @param array the array to fill
     * @param start the starting index to fill
     * @param count the number of elements to fill, which must be greater than
     *       or equal to zero
     */
    void nextInts(int[] array, int start, int count);

    /**
     * Returns a new instance of this generator with the same state, meaning
     * that each instance will independently generate the same sequence.
     *
     * @return a clone of this PRNGProvider
     */
    PRNGProvider clone();
}
