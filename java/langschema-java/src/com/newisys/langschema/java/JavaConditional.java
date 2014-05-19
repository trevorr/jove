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
 * Represents a Java conditional expression.
 * 
 * @author Trevor Robinson
 */
public final class JavaConditional
    extends JavaOperation
{
    private final JavaType resultType;

    public JavaConditional(
        JavaExpression op1,
        JavaExpression op2,
        JavaExpression op3)
    {
        super(op1.schema, new JavaExpression[3]);

        if (!schema.isBoolean(op1.getResultType()))
        {
            throw new IllegalArgumentException(
                "First operand of conditional must be boolean");
        }

        JavaType type2 = op2.getResultType();
        JavaType type3 = op3.getResultType();
        if (type2.equals(type3))
        {
            resultType = type2;
        }
        else if (schema.isNumeric(type2) && schema.isNumeric(type3))
        {
            // NOTE: This code does not implement the following cases from the
            // Java Language Spec, Section 15.25:
            // 1) byte : short -> short
            // 2) T={byte, short, char} : constant int representable in T -> T
            resultType = promote(type2, type3);
        }
        else if (type2 instanceof JavaStructuredType
            && type2.isAssignableFrom(type3))
        {
            resultType = type2;
        }
        else if (type3 instanceof JavaStructuredType
            && type3.isAssignableFrom(type2))
        {
            resultType = type3;
        }
        else
        {
            throw new IllegalArgumentException(
                "Invalid types for conditional choices");
        }

        operands[0] = op1;
        operands[1] = op2;
        operands[2] = op3;
    }

    public JavaType getResultType()
    {
        return resultType;
    }

    public boolean isConstant()
    {
        return operands[0].isConstant() && operands[1].isConstant()
            && operands[2].isConstant();
    }

    public void accept(JavaExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
