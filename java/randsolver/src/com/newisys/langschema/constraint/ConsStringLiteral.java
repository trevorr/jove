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

public final class ConsStringLiteral
    extends ConsExpression
    implements Literal
{
    private final String value;

    public ConsStringLiteral(ConsSchema schema, String value)
    {
        super(schema);
        this.value = value;
    }

    public JavaType getResultType()
    {
        return schema.getTypeForSystemClass("java.lang.String");
    }

    public String getValue()
    {
        return value;
    }

    public String toSourceString()
    {
        return '"' + escape(value) + '"';
    }

    public static String escape(String src)
    {
        StringBuffer buf = new StringBuffer();
        int end = src.length();
        for (int i = 0; i < end; ++i)
        {
            char c = src.charAt(i);
            switch (c)
            {
            case 0x0A:
                buf.append("\\n");
                break;
            case 0x09:
                buf.append("\\t");
                break;
            case '"':
                buf.append("\\\"");
                break;
            case '\\':
                buf.append("\\\\");
                break;
            default:
                buf.append(c);
            }
        }
        return buf.toString();
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
