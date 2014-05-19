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

public final class ConsIntLiteral
    extends ConsExpression
    implements Literal
{
    private final int value;
    private int radix;

    public ConsIntLiteral(ConsSchema schema, int value)
    {
        super(schema);
        this.value = value;
        this.radix = 10;
    }

    public JavaType getResultType()
    {
        return schema.intType;
    }

    public int getValue()
    {
        return value;
    }

    public int getRadix()
    {
        return radix;
    }

    public void setRadix(int radix)
    {
        this.radix = radix;
    }

    public String toSourceString()
    {
        String prefix;
        switch (radix)
        {
        case 16:
            prefix = "0x";
            break;
        case 8:
            prefix = "0";
            break;
        default:
            assert (radix == 10);
            prefix = "";
        }
        return prefix + Integer.toString(value, radix);
    }

    public boolean isConstant()
    {
        return true;
    }

    public void accept(ConsConstraintExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
