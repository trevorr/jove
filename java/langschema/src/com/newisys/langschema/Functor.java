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
 * Base interface for functions and constructors.
 * 
 * @author Trevor Robinson
 */
public interface Functor
    extends SchemaObject
{
    /**
     * Returns the type of this function or constructor.
     *
     * @return FunctionType
     */
    FunctionType getType();

    /**
     * Returns the block representing the body of this function or constructor,
     * or null if this function is abstract or otherwise undefined.
     *
     * @return Block
     */
    Block getBody();
}
