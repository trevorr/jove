/*
 * Jove Constraint-based Random Solver
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

package com.newisys.randsolver;

/**
 * An interface which provides support for generating a family of {@link RandomMapper}
 * objects based on a class hierarchy.
 * 
 * @author Jon Nall
 *
 */
public interface RandomMapperFactory
{
    /**
     * Return a RandomMapper, if one is available for the given class. If no
     * RandomMapper can be created for the class, this method returns null.
     * @param c The class for which to provide a RandomMapper
     * @return the RandomMapper to associate with class c, or null if this
     *         factory cannot create a RandomMapper for class c.
     */
    public RandomMapper getConstraintMapper(Class c);
}
