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

import java.util.Arrays;

import com.newisys.langschema.ArrayType;

/**
 * Represents a Java array type.
 * 
 * @author Trevor Robinson
 */
public class JavaArrayType
    extends JavaRawAbstractClass
    implements ArrayType
{
    private final JavaType elementType;
    private final JavaType[] indexTypes;

    protected JavaArrayType(String id, JavaType elementType, int dimensions)
    {
        super(elementType.getSchema(), id, null, null);
        assert (dimensions > 0);
        assert (!(elementType instanceof JavaArrayType));

        this.elementType = elementType;
        this.indexTypes = new JavaType[dimensions];
        Arrays.fill(indexTypes, schema.intType);

        baseClass = schema.getObjectType();
        addBaseInterface(schema.getCloneableType());
        addBaseInterface(schema.getSerializableType());

        JavaMemberVariable length = newField("length", schema.intType);
        length.setVisibility(JavaVisibility.PUBLIC);
        length.addModifier(JavaVariableModifier.FINAL);

        JavaFunction clone = newMethod("clone", this);
        clone.setVisibility(JavaVisibility.PUBLIC);
    }

    public JavaArrayType(JavaType elementType, int dimensions)
    {
        this(generateID(elementType, dimensions), elementType, dimensions);
    }

    static String generateID(JavaType elementType, int dimensions)
    {
        return stringOfChar('[', dimensions) + elementType.toInternalName();
    }

    private static String stringOfChar(char c, int len)
    {
        StringBuffer buf = new StringBuffer(len);
        for (int i = 0; i < len; ++i)
        {
            buf.append(c);
        }
        return buf.toString();
    }

    public JavaType getElementType()
    {
        return elementType;
    }

    public JavaType[] getIndexTypes()
    {
        return indexTypes;
    }

    public JavaType getAccessType(int indexCount)
    {
        int dimCount = indexTypes.length;
        assert (indexCount >= 0 && indexCount <= dimCount);
        if (indexCount == 0)
        {
            return this;
        }
        else if (indexCount == dimCount)
        {
            return elementType;
        }
        else
        {
            return new JavaArrayType(elementType, dimCount - indexCount);
        }
    }

    public boolean equals(Object obj)
    {
        if (obj instanceof JavaArrayType)
        {
            JavaArrayType other = (JavaArrayType) obj;
            return elementType.equals(other.elementType)
                && indexTypes.length == other.indexTypes.length;
        }
        return false;
    }

    public int hashCode()
    {
        int h = getClass().hashCode() ^ elementType.hashCode();
        for (int i = 0; i < indexTypes.length; ++i)
        {
            h = (h * 37) ^ indexTypes[i].hashCode();
        }
        return h;
    }

    public boolean isSubtype(JavaType type)
    {
        return super.isSubtype(type)
            || (type instanceof JavaArrayType && isSubtype((JavaArrayType) type));
    }

    private boolean isSubtype(JavaArrayType type)
    {
        if (elementType instanceof JavaPrimitiveType)
        {
            return elementType == type.elementType
                && indexTypes.length == type.indexTypes.length;
        }
        else if (indexTypes.length == type.indexTypes.length)
        {
            return elementType.isSubtype(type.elementType);
        }
        else
        {
            return indexTypes.length < type.indexTypes.length
                && elementType.isSubtype(type);
        }
    }

    public String toInternalName()
    {
        return getName().getIdentifier();
    }

    public String toReferenceString()
    {
        String etn = elementType.toReferenceString();
        int dimensions = indexTypes.length;
        StringBuffer buf = new StringBuffer(etn.length() + dimensions * 2);
        buf.append(etn);
        for (int i = 0; i < dimensions; ++i)
        {
            buf.append("[]");
        }
        return buf.toString();
    }

    public void accept(JavaTypeVisitor visitor)
    {
        visitor.visit(this);
    }
}
