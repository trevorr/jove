/*
 * LangSchema-Java - Programming Language Modeling Classes for Java (TM)
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

package com.newisys.langschema.java;

/**
 * Exception thrown when a constructor lookup fails because no applicable
 * constructor was found.
 * 
 * @author Trevor Robinson
 */
public final class ConstructorNotFoundException
    extends FunctorSearchException
{
    private static final long serialVersionUID = 3690758380025165360L;

    private final JavaStructuredType type;
    private final JavaType[] argTypes;
    private final JavaConstructor[] considered;

    public ConstructorNotFoundException(
        JavaStructuredType type,
        JavaType[] argTypes)
    {
        this(type, argTypes, null);
    }

    public ConstructorNotFoundException(
        JavaStructuredType type,
        JavaType[] argTypes,
        JavaConstructor[] considered)
    {
        this.type = type;
        this.argTypes = argTypes;
        this.considered = considered;
    }

    public JavaStructuredType getType()
    {
        return type;
    }

    public JavaType[] getArgTypes()
    {
        return argTypes;
    }

    public JavaConstructor[] getConsidered()
    {
        return considered;
    }

    public String getMessage()
    {
        String id = type.getName().getIdentifier();
        return formatNotFoundMessage("Constructor", type, id, argTypes,
            considered);
    }
}
