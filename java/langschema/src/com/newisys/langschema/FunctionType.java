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

import java.util.List;
import java.util.Set;

/**
 * Represents the type of a function.
 * 
 * @author Trevor Robinson
 */
public interface FunctionType
    extends Type, Scope
{
    /**
     * Returns a list of arguments to this function.
     *
     * @return List of FunctionArgument
     */
    List< ? extends FunctionArgument> getArguments();

    /**
     * Returns whether this function takes a variable argument list following
     * the declared arguments.
     *
     * @return boolean
     */
    boolean isVarArgs();

    /**
     * Returns the result type of this function. For constructors, the return
     * type should always be (an implementation of) VoidType.
     *
     * @return Type
     */
    Type getReturnType();

    /**
     * Returns the types of exceptions thrown by this function.
     *
     * @return Set of Type
     */
    Set< ? extends Type> getExceptionTypes();
}
