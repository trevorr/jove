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

import java.util.Set;

/**
 * Exception thrown when a constructor lookup fails because multiple
 * applicable constructors were found.
 * 
 * @author Trevor Robinson
 */
public final class AmbiguousConstructorException
    extends FunctorSearchException
{
    private static final long serialVersionUID = 3690760600590563124L;

    private final JavaStructuredType type;
    private final JavaType[] argTypes;
    private final Set<JavaConstructor> ctors;

    public AmbiguousConstructorException(
        JavaStructuredType type,
        JavaType[] argTypes,
        Set<JavaConstructor> ctors)
    {
        this.type = type;
        this.argTypes = argTypes;
        this.ctors = ctors;
    }

    public JavaStructuredType getType()
    {
        return type;
    }

    public JavaType[] getArgTypes()
    {
        return argTypes;
    }

    public Set<JavaConstructor> getConstructors()
    {
        return ctors;
    }

    public String getMessage()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("Ambiguous constructor invocation '");
        String id = type.getName().getIdentifier();
        formatSearchMethod(buf, id, argTypes);
        buf.append("' in type ");
        buf.append(type.toReferenceString());
        if (ctors != null)
        {
            buf.append("; found constructors: ");
            boolean first = true;
            for (JavaConstructor ctor : ctors)
            {
                if (!first) buf.append(", ");
                buf.append(ctor);
                first = false;
            }
        }
        return buf.toString();
    }
}
