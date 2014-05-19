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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that describes a list of constraints to be used when randomizing
 * a class. The <code>expr</code> parameter is not optional. The <code>name</code>
 * parameter is optional and defaults to the empty string.
 *
 *@author Jon Nall
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Constraint
{
    /**
     * The name of the constraint. <code>name</code> is optional and defaults to
     * the empty string.
     * @return the name of this constraint or the empty string if it is an unnamed
     *      constraint
     */
    String name() default "";

    /**
     * The constraint expression(s). This is a String containing a list of
     * valid constraint expressions.
     * @return a string of constraint expressions
     */
    String expr();
}
