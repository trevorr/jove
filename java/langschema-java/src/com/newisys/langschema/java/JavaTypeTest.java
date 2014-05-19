/*
 * LangSchema-Java - Programming Language Modeling Classes for Java (TM)
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

package com.newisys.langschema.java;

import com.newisys.langschema.TypeTest;

/**
 * Represents a Java instanceof expression.
 * 
 * @author Trevor Robinson
 */
public final class JavaTypeTest
    extends JavaExpression
    implements TypeTest
{
    private final JavaExpression expr;
    private final JavaType type;

    public JavaTypeTest(JavaExpression expr, JavaType type)
    {
        super(expr.schema);
        this.expr = expr;
        this.type = type;
    }

    public JavaType getResultType()
    {
        return schema.booleanType;
    }

    public JavaExpression getExpression()
    {
        return expr;
    }

    public JavaType getType()
    {
        return type;
    }

    public boolean isConstant()
    {
        return false;
    }

    public void accept(JavaExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
