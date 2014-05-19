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
 * Exception thrown when a method lookup fails because multiple applicable
 * methods were found.
 * 
 * @author Trevor Robinson
 */
public final class AmbiguousMethodException
    extends FunctorSearchException
{
    private static final long serialVersionUID = 3905799760396235827L;

    private final JavaStructuredType type;
    private final String id;
    private final JavaType[] argTypes;
    private final Set<JavaFunction> methods;

    public AmbiguousMethodException(
        JavaStructuredType type,
        String id,
        JavaType[] argTypes,
        Set<JavaFunction> methods)
    {
        this.type = type;
        this.id = id;
        this.argTypes = argTypes;
        this.methods = methods;
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

    public Set<JavaFunction> getMethods()
    {
        return methods;
    }

    public String getMessage()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("Ambiguous method invocation '");
        formatSearchMethod(buf, id, argTypes);
        buf.append("' in type ");
        buf.append(type.toReferenceString());
        if (methods != null)
        {
            buf.append("; found methods: ");
            boolean first = true;
            for (JavaFunction method : methods)
            {
                if (!first) buf.append(", ");
                buf.append(method);
                first = false;
            }
        }
        return buf.toString();
    }
}
