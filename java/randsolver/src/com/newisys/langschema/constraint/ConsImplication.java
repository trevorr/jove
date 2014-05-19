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

import java.util.Iterator;

import com.newisys.langschema.java.JavaType;

public class ConsImplication
    extends ConsBinaryOperation
{
    public ConsImplication(ConsExpression predicate, ConsExpression constraint)
    {
        super(predicate, constraint);
    }

    public JavaType getResultType()
    {
        return schema.booleanType;
    }

    public String toSourceString()
    {
        StringBuffer cSetBuf = new StringBuffer();

        if (getOperand(1) instanceof ConsConstraintSet)
        {
            ConsConstraintSet cSet = (ConsConstraintSet) getOperand(1);
            cSetBuf.append("{ ");
            Iterator iter = cSet.getExprs().iterator();
            while (iter.hasNext())
            {
                cSetBuf.append(iter.next());
            }
            cSetBuf.append(" }");
        }
        else
        {
            cSetBuf.append(getOperand(1));
        }

        return "( " + getOperand(0) + " => " + cSetBuf + " )";
    }

    public void accept(ConsConstraintExpressionVisitor visitor)
    {
        ((ConsConstraintExpressionVisitor) visitor).visit(this);
    }

    public boolean isConstant()
    {
        boolean isConstant = true;
        for (int i = 0; i < operands.length; ++i)
        {
            isConstant &= operands[i].isConstant();
        }
        return isConstant;
    }
}
