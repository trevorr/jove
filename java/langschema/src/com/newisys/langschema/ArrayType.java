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

/**
 * Represents a type that is an array of the given element type and the given
 * index type(s).
 * 
 * @author Trevor Robinson
 */
public interface ArrayType
    extends Type
{
    /**
     * Returns the type of the elements in this array type.
     *
     * @return Type
     */
    Type getElementType();

    /**
     * Returns an array containing the index types of this array.
     *
     * @return int[] with an element for each dimension of the array type
     */
    Type[] getIndexTypes();
}
