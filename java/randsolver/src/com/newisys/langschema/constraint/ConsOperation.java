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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.newisys.langschema.Operation;
import com.newisys.langschema.StructuredTypeMember;
import com.newisys.langschema.java.JavaMemberVariable;

public abstract class ConsOperation
    extends ConsExpression
    implements Operation
{
    protected final ConsExpression[] operands;

    protected ConsOperation(ConsSchema schema, ConsExpression[] operands)
    {
        super(schema);
        this.operands = operands;
    }

    public List getOperands()
    {
        return Arrays.asList(operands);
    }

    public ConsExpression getOperand(int index)
    {
        return operands[index];
    }

    public Set getModifiers()
    {
        return Collections.EMPTY_SET;
    }

    protected static void checkVarRefExpr(ConsExpression expr)
    {
        if (expr instanceof ConsMemberAccess)
        {
            ConsMemberAccess memberAccess = (ConsMemberAccess) expr;
            StructuredTypeMember member = memberAccess.getMember();
            assert (member instanceof JavaMemberVariable);
        }
        else
        {
            assert (expr instanceof ConsVariableReference || expr instanceof ConsArrayAccess);
        }
    }
}
