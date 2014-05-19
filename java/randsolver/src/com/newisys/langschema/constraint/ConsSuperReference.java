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

import com.newisys.langschema.StructuredType;
import com.newisys.langschema.SuperReference;
import com.newisys.langschema.java.JavaAbstractClass;

public final class ConsSuperReference
    extends ConsExpression
    implements SuperReference
{
    private final JavaAbstractClass type;

    public ConsSuperReference(ConsSchema schema, JavaAbstractClass type)
    {
        super(schema);
        this.type = type;
    }

    public JavaAbstractClass getResultType()
    {
        return type;
    }

    public StructuredType getType()
    {
        return type;
    }

    public String toSourceString()
    {
        return "super";
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
