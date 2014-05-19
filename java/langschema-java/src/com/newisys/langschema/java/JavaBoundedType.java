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

import java.util.LinkedList;
import java.util.List;

/**
 * Base class for Java bounded types, such as type variables and wildcard
 * types.
 * 
 * @author Trevor Robinson
 */
public abstract class JavaBoundedType
    extends JavaReferenceTypeImpl
{
    protected final List<JavaTypeBound> upperBounds = new LinkedList<JavaTypeBound>();
    protected final List<JavaTypeBound> lowerBounds = new LinkedList<JavaTypeBound>();

    public JavaBoundedType(
        JavaSchema schema,
        List<JavaTypeBound> upperBounds,
        List<JavaTypeBound> lowerBounds)
    {
        super(schema);
        if (upperBounds != null) addUpperBounds(upperBounds);
        if (lowerBounds != null) addLowerBounds(lowerBounds);
    }

    public List<JavaTypeBound> getUpperBounds()
    {
        return upperBounds;
    }

    public void addUpperBound(JavaTypeBound bound)
    {
        addBound(upperBounds, bound);
    }

    public void addUpperBounds(List<JavaTypeBound> bounds)
    {
        for (final JavaTypeBound bound : bounds)
        {
            addBound(upperBounds, bound);
        }
    }

    public List<JavaTypeBound> getLowerBounds()
    {
        return lowerBounds;
    }

    public void addLowerBound(JavaTypeBound bound)
    {
        addBound(lowerBounds, bound);
    }

    public void addLowerBounds(List<JavaTypeBound> bounds)
    {
        for (final JavaTypeBound bound : bounds)
        {
            addBound(lowerBounds, bound);
        }
    }

    private static void addBound(List<JavaTypeBound> list, JavaTypeBound bound)
    {
        assert (bound instanceof JavaAbstractInterface || list.isEmpty());
        list.add(bound);
    }

    public boolean hasTypeVariables()
    {
        for (final JavaTypeBound bound : upperBounds)
        {
            if (bound.hasTypeVariables()) return true;
        }
        for (final JavaTypeBound bound : lowerBounds)
        {
            if (bound.hasTypeVariables()) return true;
        }
        return false;
    }

    public JavaType getErasure()
    {
        return !upperBounds.isEmpty() ? upperBounds.get(0).getErasure()
            : schema.getObjectType();
    }

    public boolean isSubtype(JavaType type)
    {
        return type == this || type instanceof JavaNullType || contains(type);
    }

    public boolean contains(JavaType type)
    {
        if (type instanceof JavaBoundedType)
        {
            return contains((JavaBoundedType) type);
        }
        else if (type instanceof JavaStructuredType)
        {
            return contains((JavaStructuredType) type);
        }
        return false;
    }

    private boolean contains(JavaBoundedType type)
    {
        // check that each upper bound of this type is a supertype of at least
        // one upper bound of the given type
        upperCheck: for (final JavaTypeBound bound : upperBounds)
        {
            for (final JavaTypeBound otherBound : type.upperBounds)
            {
                if (bound.isSubtype(otherBound)) continue upperCheck;
            }
            return false;
        }
        // check that each lower bound of this type is a subtype of at least
        // one lower bound of the given type
        lowerCheck: for (final JavaTypeBound bound : lowerBounds)
        {
            for (final JavaTypeBound otherBound : type.lowerBounds)
            {
                if (otherBound.isSubtype(bound)) continue lowerCheck;
            }
            return false;
        }
        return true;
    }

    private boolean contains(JavaStructuredType type)
    {
        // check that given type is subtype of all upper bounds
        for (final JavaTypeBound bound : upperBounds)
        {
            if (!bound.isSubtype(type)) return false;
        }
        // check that given type is supertype of all lower bounds
        for (final JavaTypeBound bound : lowerBounds)
        {
            if (!type.isSubtype(bound)) return false;
        }
        return true;
    }
}
