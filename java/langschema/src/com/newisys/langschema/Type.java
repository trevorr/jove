/*
 * LangSchema - Generic Programming Language Modeling Interfaces
 * Copyright (C) 2005 Newisys, Inc. or its licensors, as applicable.
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

package com.newisys.langschema;

import java.util.Set;

/**
 * Base interface for all data/object types.
 * 
 * @author Trevor Robinson
 */
public interface Type
    extends SchemaObject
{
    /**
     * Returns a collection of any language-specific modifiers for this type.
     *
     * @return Set of TypeModifier
     */
    Set< ? extends TypeModifier> getModifiers();

    /**
     * Returns whether a variable of this type is assignable from an object of
     * the given type.
     *
     * @param other the source type of the assignment
     * @return boolean
     */
    boolean isAssignableFrom(Type other);

    /**
     * Returns whether this type intrinsically represents some range of
     * integers.
     *
     * @return boolean
     */
    boolean isStrictIntegral();

    /**
     * Returns whether this type represents values that can be converted to
     * integers.
     *
     * @return boolean
     */
    boolean isIntegralConvertible();
}
