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

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a type variable for a Java raw type.
 * 
 * @author Trevor Robinson
 */
public class JavaTypeVariable
    extends JavaBoundedType
    implements JavaTypeBound
{
    private final JavaName name;

    public JavaTypeVariable(JavaSchema schema, String id)
    {
        this(schema, id, null, null);
    }

    public JavaTypeVariable(
        JavaSchema schema,
        String id,
        List<JavaTypeBound> upperBounds,
        List<JavaTypeBound> lowerBounds)
    {
        super(schema, upperBounds, lowerBounds);
        this.name = new JavaName(id, JavaNameKind.TYPE);
    }

    public JavaName getName()
    {
        return name;
    }

    public JavaWildcardType getWildcard()
    {
        return new JavaWildcardType(schema, upperBounds, lowerBounds);
    }

    public boolean hasTypeVariables()
    {
        return true;
    }

    public boolean isSelfReferential()
    {
        return referencesThis(this);
    }

    private boolean referencesThis(JavaType type)
    {
        if (type == this) return true;

        if (type instanceof JavaParameterizedType)
        {
            return referencesThis((JavaParameterizedType< ? >) type);
        }
        else if (type instanceof JavaBoundedType)
        {
            return referencesThis((JavaBoundedType) type);
        }
        return false;
    }

    private boolean referencesThis(JavaParameterizedType< ? > type)
    {
        final List<JavaReferenceType> typeArgs = type.getActualTypeArguments();
        for (final JavaReferenceType typeArg : typeArgs)
        {
            if (referencesThis(typeArg)) return true;
        }
        return false;
    }

    private boolean referencesThis(JavaBoundedType type)
    {
        for (final JavaTypeBound bound : type.upperBounds)
        {
            if (referencesThis(bound)) return true;
        }
        for (final JavaTypeBound bound : type.lowerBounds)
        {
            if (referencesThis(bound)) return true;
        }
        return false;
    }

    public JavaBoundedType parameterize(JavaReferenceType type)
    {
        final JavaWildcardType result = new JavaWildcardType(schema);
        final Map<JavaTypeVariable, JavaReferenceType> typeVarMap = Collections
            .singletonMap(this, type);
        for (final JavaTypeBound bound : upperBounds)
        {
            final JavaTypeBound resolvedBound = (JavaTypeBound) resolveType(
                bound, typeVarMap);
            result.addUpperBound(resolvedBound);
        }
        for (final JavaTypeBound bound : lowerBounds)
        {
            final JavaTypeBound resolvedBound = (JavaTypeBound) resolveType(
                bound, typeVarMap);
            result.addLowerBound(resolvedBound);
        }
        return result;
    }

    public boolean contains(JavaType type)
    {
        if (type instanceof JavaReferenceType)
        {
            if (isSelfReferential())
            {
                final JavaType substType = parameterize((JavaReferenceType) type);
                return substType.contains(type);
            }
            else
            {
                return super.contains(type);
            }
        }
        return false;
    }

    public void accept(JavaSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(JavaTypeVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "type variable " + name;
    }

    public String toReferenceString()
    {
        return name.getCanonicalName();
    }

    public String toInternalName()
    {
        return getErasure().toInternalName();
    }
}
