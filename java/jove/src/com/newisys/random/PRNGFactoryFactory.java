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
 * Factory for pseudorandom number generator factories. Although a factory-
 * factory seems like a confusing concept, it simply refers to the fact that
 * this class provides PRNGFactory objects according to a policy or
 * configuration, while those factories are used to create PRNG objects
 * (i.e. independent random number streams) of a particular kind.
 * 
 * @author Trevor Robinson
 */
public class PRNGFactoryFactory
{
    /**
     * Returns the factory for the default pseudorandom number generator.
     *
     * @return an instance of the default PRNGFactory
     */
    public static PRNGFactory getDefaultFactory()
    {
        return MersenneTwisterFactory.INSTANCE;
    }
}
