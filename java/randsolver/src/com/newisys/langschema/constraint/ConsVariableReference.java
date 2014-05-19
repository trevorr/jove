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

import com.newisys.langschema.Expression;
import com.newisys.langschema.VariableReference;
import com.newisys.langschema.java.JavaType;
import com.newisys.langschema.java.JavaVariable;
import com.newisys.langschema.java.JavaVariableModifier;

public final class ConsVariableReference
    extends ConsExpression
    implements VariableReference
{
    private final JavaVariable variable;

    public ConsVariableReference(ConsSchema schema, JavaVariable variable)
    {
        super(schema);
        this.variable = variable;
    }

    public JavaType getResultType()
    {
        return variable.getType();
    }

    public JavaVariable getVariable()
    {
        return variable;
    }

    public String toSourceString()
    {
        return variable.getName().getCanonicalName();
    }

    public boolean isConstant()
    {
        if (variable.hasModifier(JavaVariableModifier.FINAL))
        {
            Expression initExpr = variable.getInitializer();
            return initExpr != null && initExpr.isConstant();
        }
        return false;
    }

    public void accept(ConsConstraintExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
