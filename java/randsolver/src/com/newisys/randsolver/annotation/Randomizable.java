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

package com.newisys.randsolver.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that describes zero or more constraints for a class. If a class
 * is to be randomized, the class must be annotated with Randomizable. If the
 * <code>value</code> parameter is left unspecified it defaults to an empty array.
 * An empty array implies that the random variables in this class should be
 * randomized across their full range (i.e. unconstrained).
 *
 * If there is more than one {@link Constraint} in the constraints array, each
 * Constraint must have a unique name. Duplicate names will result in a
 * {@link RuntimeException} the first time an instance of the annotated class
 * is passed to the {@link com.newisys.randsolver.Solver Solver}.
 *<P>
 * This annotation should be kept for runtime use.<br>
 * This annotation should be inherited by classes derived from a class annotated
 * with Randomizable.<br>
 * This annotation applies to class types.<br>
 *
 * @author Jon Nall
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
public @interface Randomizable
{
    /**
     * An array of {@link Constraint} annotations  used to randomize the class
     *
     * @return an array of Constraint annotations associated with the class
     */
    Constraint[] value() default {};
}
