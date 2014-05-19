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
import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.java.JavaType;

public abstract class ConsSetOperation
    extends ConsExpression
{
    private final ConsExpression expr;
    private final List members = new LinkedList(); // List<ConsSetMember>
    protected boolean printWeight;

    public ConsSetOperation(ConsExpression expr)
    {
        super(expr.schema);
        this.expr = expr;
    }

    abstract public String getOperatorString();

    public JavaType getResultType()
    {
        return schema.booleanType;
    }

    public boolean isConstant()
    {
        // treat set operations as non-constant, because a) they are only usable
        // in random constraint blocks and b) it's easier
        return false;
    }

    public ConsExpression getExpr()
    {
        return expr;
    }

    public List getMembers()
    {
        return members;
    }

    public void addMember(ConsSetMember expr)
    {
        members.add(expr);
    }

    public String toSourceString()
    {
        StringBuffer members = new StringBuffer();
        Iterator iter = getMembers().iterator();
        boolean insertSpace = false;

        while (iter.hasNext())
        {
            ConsSetMember member = (ConsSetMember) iter.next();
            members.append(((insertSpace) ? ", " : "") + member);
            insertSpace = true;
        }

        return getExpr() + " " + getOperatorString() + " { " + members + " }";
    }
}
