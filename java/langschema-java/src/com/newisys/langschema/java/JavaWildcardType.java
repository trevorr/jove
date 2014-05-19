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
 * Represents a Java wildcard type.
 * 
 * @author Trevor Robinson
 */
public final class JavaWildcardType
    extends JavaBoundedType
{
    public JavaWildcardType(JavaSchema schema)
    {
        super(schema, null, null);
    }

    public JavaWildcardType(
        JavaSchema schema,
        List<JavaTypeBound> upperBounds,
        List<JavaTypeBound> lowerBounds)
    {
        super(schema, upperBounds, lowerBounds);
    }

    public boolean equals(Object obj)
    {
        if (obj instanceof JavaWildcardType)
        {
            JavaWildcardType other = (JavaWildcardType) obj;
            return upperBounds.equals(other.upperBounds)
                && lowerBounds.equals(other.lowerBounds);
        }
        return false;
    }

    public int hashCode()
    {
        return 73 * upperBounds.hashCode() + lowerBounds.hashCode();
    }

    public void accept(JavaTypeVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "wildcard type";
    }

    public String toReferenceString()
    {
        return "?"
            + (!lowerBounds.isEmpty() ? " extends "
                + lowerBounds.get(0).toReferenceString() : (!upperBounds
                .isEmpty() ? " super " + upperBounds.get(0).toReferenceString()
                : ""));
    }

    public String toInternalName()
    {
        return getErasure().toInternalName();
    }
}
