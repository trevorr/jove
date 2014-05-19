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

import com.newisys.langschema.ThisReference;
import com.newisys.langschema.java.JavaStructuredType;

public final class ConsThisReference
    extends ConsExpression
    implements ThisReference
{
    private final JavaStructuredType type;
    private boolean qualified;

    public ConsThisReference(ConsSchema schema, JavaStructuredType type)
    {
        super(schema);
        this.type = type;
    }

    public JavaStructuredType getResultType()
    {
        return type;
    }

    public JavaStructuredType getType()
    {
        return type;
    }

    public boolean isQualified()
    {
        return qualified;
    }

    public void setQualified(boolean qualified)
    {
        this.qualified = qualified;
    }

    public String toSourceString()
    {
        return qualified ? type + ".this" : "this";
    }

    public boolean isConstant()
    {
        return false;
    }

    public void accept(ConsConstraintExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
