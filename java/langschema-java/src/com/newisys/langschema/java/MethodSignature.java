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
 * Represents the signature of a given method (i.e. its name and argument
 * types). Used for keying methods by signature in a map.
 * 
 * @author Trevor Robinson
 */
final class MethodSignature
{
    final JavaFunction method;

    public MethodSignature(JavaFunction method)
    {
        this.method = method;
    }

    public JavaFunction getMethod()
    {
        return method;
    }

    public boolean equals(Object obj)
    {
        if (obj instanceof MethodSignature)
        {
            JavaFunction otherMethod = ((MethodSignature) obj).getMethod();
            return method.getName().getIdentifier().equals(
                otherMethod.getName().getIdentifier())
                && method.signatureMatches(otherMethod);
        }
        return false;
    }

    public int hashCode()
    {
        int h = method.getName().getIdentifier().hashCode();
        for (final JavaFunctionArgument arg : method.getType().getArguments())
        {
            h ^= arg.getType().hashCode();
        }
        return h;
    }
}
