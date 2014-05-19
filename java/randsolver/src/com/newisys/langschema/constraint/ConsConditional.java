/*
 * Jove Constraint-based Random Solver
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

package com.newisys.langschema.constraint;

import com.newisys.langschema.java.JavaStructuredType;
import com.newisys.langschema.java.JavaType;

public final class ConsConditional
    extends ConsOperation
{
    private final JavaType resultType;

    public ConsConditional(
        ConsExpression op1,
        ConsExpression op2,
        ConsExpression op3)
    {
        super(op1.schema, new ConsExpression[3]);

        if (!schema.isDVIntegral(op1.getResultType()))
        {
            throw new IllegalArgumentException(
                "First operand of conditional must be integral");
        }

        JavaType type2 = op2.getResultType();
        JavaType type3 = op3.getResultType();
        if (type2.equals(type3))
        {
            resultType = type2;
        }
        else if (schema.isDVNumeric(type2) && schema.isDVNumeric(type3))
        {
            resultType = schema.promote(type2, type3);
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

    public String toSourceString()
    {
        return operands[0] + " ? " + operands[1] + " : " + operands[2];
    }

    public boolean isConstant()
    {
        return operands[0].isConstant() && operands[1].isConstant()
            && operands[2].isConstant();
    }

    public void accept(ConsConstraintExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
