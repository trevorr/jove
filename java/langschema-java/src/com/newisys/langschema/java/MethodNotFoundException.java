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
 * Exception thrown when a method lookup fails because no applicable method
 * was found.
 * 
 * @author Trevor Robinson
 */
public final class MethodNotFoundException
    extends FunctorSearchException
{
    private final JavaStructuredType type;
    private final String id;
    private final JavaType[] argTypes;
    private final JavaFunction[] considered;

    public MethodNotFoundException(
        JavaStructuredType type,
        String id,
        JavaType[] argTypes)
    {
        this(type, id, argTypes, null);
    }

    public MethodNotFoundException(
        JavaStructuredType type,
        String id,
        JavaType[] argTypes,
        JavaFunction[] considered)
    {
        this.type = type;
        this.id = id;
        this.argTypes = argTypes;
        this.considered = considered;
    }

    public JavaStructuredType getType()
    {
        return type;
    }

    public String getIdentifier()
    {
        return id;
    }

    public JavaType[] getArgTypes()
    {
        return argTypes;
    }

    public JavaFunction[] getConsidered()
    {
        return considered;
    }

    public String getMessage()
    {
        return formatNotFoundMessage("Method", type, id, argTypes, considered);
    }
}
