/*
 * LangSchema-Jove - Programming Language Modeling Classes for Jove
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

package com.newisys.langschema.jove;

import com.newisys.langschema.FixedArrayType;
import com.newisys.langschema.java.JavaArrayType;
import com.newisys.langschema.java.JavaType;

/**
 * Represents an array type with fixed dimensions.
 * 
 * @author Trevor Robinson
 */
public final class JoveFixedArrayType
    extends JavaArrayType
    implements FixedArrayType
{
    private final int[] dimensions;

    public JoveFixedArrayType(JavaType elementType, int[] dimensions)
    {
        super(elementType, dimensions.length);
        this.dimensions = dimensions;
    }

    public int[] getDimensions()
    {
        return dimensions;
    }

    public int[] getLowBounds()
    {
        int[] lowBounds = new int[dimensions.length];
        return lowBounds;
    }

    public int[] getHighBounds()
    {
        int[] highBounds = new int[dimensions.length];
        for (int i = 0; i < dimensions.length; ++i)
        {
            highBounds[i] = dimensions[i] - 1;
        }
        return highBounds;
    }

    public boolean equals(Object obj)
    {
        if (obj instanceof JoveFixedArrayType && super.equals(obj))
        {
            JoveFixedArrayType other = (JoveFixedArrayType) obj;
            for (int i = 0; i < dimensions.length; ++i)
            {
                if (other.dimensions[i] != dimensions[i]) return false;
            }
            return true;
        }
        return false;
    }

    public int hashCode()
    {
        int h = super.hashCode();
        for (int i = 0; i < dimensions.length; ++i)
        {
            h = (h * 37) ^ dimensions[i];
        }
        return h;
    }

    public String toReferenceString()
    {
        String etn = getElementType().toReferenceString();
        int dimCount = dimensions.length;
        StringBuffer buf = new StringBuffer(etn.length() + dimCount * 8);
        buf.append(etn);
        for (int i = 0; i < dimCount; ++i)
        {
            buf.append('[');
            buf.append(dimensions[i]);
            buf.append(']');
        }
        return buf.toString();
    }
}
