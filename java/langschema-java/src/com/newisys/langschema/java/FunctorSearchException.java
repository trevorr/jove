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

import java.util.List;

/**
 * Base class for method/constructor lookup exceptions.
 * 
 * @author Trevor Robinson
 */
class FunctorSearchException
    extends RuntimeException
{
    private static final long serialVersionUID = 3257848774939522099L;

    public FunctorSearchException()
    {
    }

    protected static String getFunctorID(JavaFunctor functor)
    {
        if (functor instanceof JavaFunction)
        {
            return ((JavaFunction) functor).getName().getIdentifier();
        }
        else
        {
            return functor.getStructuredType().getName().getIdentifier();
        }
    }

    protected static JavaType[] getFunctorArgTypes(JavaFunctor functor)
    {
        List<JavaFunctionArgument> args = functor.getType().getArguments();
        JavaType[] argTypes = new JavaType[args.size()];
        int index = 0;
        for (final JavaFunctionArgument arg : args)
        {
            argTypes[index++] = arg.getType();
        }
        return argTypes;
    }

    protected static String formatNotFoundMessage(
        String label,
        JavaStructuredType type,
        String id,
        JavaType[] argTypes,
        JavaFunctor[] considered)
    {
        StringBuffer buf = new StringBuffer();
        buf.append(label);
        buf.append(' ');
        formatSearchMethod(buf, id, argTypes);
        buf.append(" not found in type ");
        buf.append(type.toReferenceString());
        if (considered != null)
        {
            buf.append("; considered: ");
            for (int i = 0; i < considered.length; ++i)
            {
                if (i > 0) buf.append(", ");
                formatSearchMethod(buf, getFunctorID(considered[i]),
                    getFunctorArgTypes(considered[i]));
            }
        }
        return buf.toString();
    }

    protected static void formatSearchMethod(
        StringBuffer buf,
        String id,
        JavaType[] argTypes)
    {
        buf.append(id);
        buf.append('(');
        int argTypeCount = argTypes != null ? argTypes.length : 0;
        for (int i = 0; i < argTypeCount; ++i)
        {
            if (i > 0) buf.append(", ");
            buf.append(argTypes[i].toReferenceString());
        }
        buf.append(')');
    }
}
