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

import java.util.Set;

import com.newisys.langschema.java.JavaClass;
import com.newisys.langschema.java.JavaRawClass;
import com.newisys.langschema.java.JavaSchema;
import com.newisys.langschema.java.JavaType;
import com.newisys.schemaprinter.java.JavaSchemaPrinter;
import com.newisys.schemaprinter.jove.JoveSchemaPrinter;

/**
 * Represents a Java schema extended with extra Jove type information.
 * 
 * @author Trevor Robinson
 */
public class JoveSchema
    extends JavaSchema
{
    private static final String BIT_CLASS = "com.newisys.verilog.util.Bit";
    public final JavaClass bitType = (JavaClass) getTypeForSystemClass(BIT_CLASS);

    private static final String BIT_VECTOR_CLASS = "com.newisys.verilog.util.BitVector";
    public final JavaRawClass bitVectorType = (JavaRawClass) getTypeForSystemClass(BIT_VECTOR_CLASS);

    protected JavaSchemaPrinter createDefaultPrinter()
    {
        return new JoveSchemaPrinter();
    }

    public final boolean isBit(JavaType type)
    {
        return isStructuredType(type, BIT_CLASS);
    }

    public final boolean isBitVector(JavaType type)
    {
        return isPrimitiveType(type, JoveBitVectorType.class, BIT_VECTOR_CLASS);
    }

    public final int getBitVectorSize(JavaType type)
    {
        return (type instanceof JoveBitVectorType) ? ((JoveBitVectorType) type)
            .getSize() : -1;
    }

    public final JavaClass getBitVectorType(int size)
    {
        return getBitVectorType(size, null);
    }

    public final JavaClass getBitVectorType(int size, String desc)
    {
        return (size > 0) ? new JoveBitVectorType(bitVectorType, size, desc)
            : bitVectorType;
    }

    protected Set<String> builtIntegralTypeSet()
    {
        Set<String> integralTypes = super.builtIntegralTypeSet();
        integralTypes.add(BIT_CLASS);
        integralTypes.add(BIT_VECTOR_CLASS);
        return integralTypes;
    }

    public boolean isDVIntegral(JavaType type)
    {
        if (isBoolean(type) || isBitVector(type))
        {
            return true;
        }
        return isIntegral(type);
    }

    public boolean isDVNumeric(JavaType type)
    {
        if (isBoolean(type) || isBitVector(type))
        {
            return true;
        }
        return isNumeric(type);
    }

    private static final IntegralTypeInfo BIT_INFO = new IntegralTypeInfo(1,
        false, true);

    protected IntegralTypeInfo getIntegralTypeInfo(JavaType type)
    {
        if (isBitVector(type))
        {
            return new IntegralTypeInfo(getBitVectorSize(type), false, true);
        }
        else if (isBit(type))
        {
            return BIT_INFO;
        }
        return super.getIntegralTypeInfo(type);
    }

    protected JavaType getIntegralType(IntegralTypeInfo info)
    {
        if (!info.vector)
        {
            return super.getIntegralType(info);
        }
        else if (info.bits == 1)
        {
            return bitType;
        }
        else
        {
            // TODO: preserve signed attribute?
            return getBitVectorType(info.bits);
        }
    }
}
