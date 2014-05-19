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

import com.newisys.langschema.PrimitiveType;

/**
 * Base class for Java primitive types.
 * 
 * @author Trevor Robinson
 */
public abstract class JavaPrimitiveType
    extends JavaTypeImpl
    implements PrimitiveType
{
    JavaRawClass wrapperClass;

    protected JavaPrimitiveType(JavaSchema schema)
    {
        super(schema);
    }

    public final JavaRawClass getWrapperClass()
    {
        return wrapperClass;
    }

    JavaPrimitiveType unbox(JavaType type)
    {
        final JavaPrimitiveType primType;
        if (type instanceof JavaPrimitiveType)
        {
            primType = (JavaPrimitiveType) type;
        }
        else if (type instanceof JavaRawClass)
        {
            final JavaRawClass cls = (JavaRawClass) type;
            final JavaSchema clsSchema = cls.getSchema();
            if (cls == clsSchema.booleanWrapperType)
                primType = schema.booleanType;
            else if (cls == clsSchema.byteWrapperType)
                primType = schema.byteType;
            else if (cls == clsSchema.characterWrapperType)
                primType = schema.charType;
            else if (cls == clsSchema.doubleWrapperType)
                primType = schema.doubleType;
            else if (cls == clsSchema.floatWrapperType)
                primType = schema.floatType;
            else if (cls == clsSchema.integerWrapperType)
                primType = schema.intType;
            else if (cls == clsSchema.longWrapperType)
                primType = schema.longType;
            else if (cls == clsSchema.shortWrapperType)
                primType = schema.shortType;
            else
                primType = null;
        }
        else
        {
            primType = null;
        }
        return primType;
    }

    static boolean isDoubleSubtype(JavaType type)
    {
        return type instanceof JavaDoubleType || isFloatSubtype(type);
    }

    static boolean isFloatSubtype(JavaType type)
    {
        return type instanceof JavaFloatType || isLongSubtype(type);
    }

    static boolean isLongSubtype(JavaType type)
    {
        return type instanceof JavaLongType || isIntSubtype(type);
    }

    static boolean isIntSubtype(JavaType type)
    {
        return type instanceof JavaIntType || isShortSubtype(type)
            || isCharSubtype(type);
    }

    static boolean isShortSubtype(JavaType type)
    {
        return type instanceof JavaShortType || isByteSubtype(type);
    }

    static boolean isCharSubtype(JavaType type)
    {
        return type instanceof JavaCharType;
    }

    static boolean isByteSubtype(JavaType type)
    {
        return type instanceof JavaByteType;
    }

    static boolean isBooleanSubtype(JavaType type)
    {
        return type instanceof JavaBooleanType;
    }
}
