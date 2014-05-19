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

import com.newisys.langschema.Literal;
import com.newisys.langschema.java.JavaType;
import com.newisys.langschema.jove.JoveBitVectorType;
import com.newisys.verilog.util.BitVector;

public class ConsBitVectorLiteral
    extends ConsExpression
    implements Literal
{
    private final BitVector value;
    private JavaType resultType;

    public ConsBitVectorLiteral(ConsSchema schema, BitVector value)
    {
        super(schema);
        this.resultType = new JoveBitVectorType(schema.bitVectorType, value
            .length());
        this.value = value;
    }

    public JavaType getResultType()
    {
        return resultType;
    }

    public BitVector getValue()
    {
        return value;
    }

    public boolean isConstant()
    {
        return true;
    }

    public Object evaluateConstant()
    {
        return value;
    }

    public String toSourceString()
    {
        return value.toString();
    }

    public void accept(ConsConstraintExpressionVisitor visitor)
    {
        ((ConsConstraintExpressionVisitor) visitor).visit(this);
    }
}
