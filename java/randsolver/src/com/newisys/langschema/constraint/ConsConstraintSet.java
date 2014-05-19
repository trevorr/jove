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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.java.JavaType;

public class ConsConstraintSet
    extends ConsExpression
{
    private final List exprs = new LinkedList(); // List<ConsExpression>
    private boolean constant = true;

    public ConsConstraintSet(ConsSchema schema)
    {
        super(schema);
    }

    public JavaType getResultType()
    {
        return schema.booleanType;
    }

    public final List getExprs()
    {
        return exprs;
    }

    public final void addExpr(ConsExpression expr)
    {
        //assert (expr.getResultType().isIntegralConvertible());
        exprs.add(expr);
        if (!expr.isConstant()) constant = false;
    }

    public boolean isConstant()
    {
        return constant;
    }

    public void accept(ConsConstraintExpressionVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toSourceString()
    {
        StringBuffer buf = new StringBuffer(200);
        printExprSet(buf, exprs);
        return buf.toString();
    }

    static void printExprSet(StringBuffer buf, Collection exprs)
    {
        buf.append("{\n");
        Iterator iter = exprs.iterator();
        while (iter.hasNext())
        {
            ConsExpression expr = (ConsExpression) iter.next();
            buf.append("    ");
            buf.append(expr.toSourceString());
            buf.append(";\n");
        }
        buf.append("}\n");
    }
}
