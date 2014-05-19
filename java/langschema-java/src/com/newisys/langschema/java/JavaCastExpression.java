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

import com.newisys.langschema.CastExpression;

/**
 * Represents a Java cast expression.
 * 
 * @author Trevor Robinson
 */
public final class JavaCastExpression
    extends JavaExpression
    implements CastExpression
{
    private final JavaType type;
    private final JavaExpression expr;

    public JavaCastExpression(JavaType type, JavaExpression expr)
    {
        super(type.getSchema());
        this.type = type;
        this.expr = expr;
    }

    public JavaType getResultType()
    {
        return type;
    }

    public JavaType getType()
    {
        return type;
    }

    public JavaExpression getExpression()
    {
        return expr;
    }

    public boolean isConstant()
    {
        return (type instanceof JavaPrimitiveType || isStringType(type))
            && expr.isConstant();
    }

    private static boolean isStringType(JavaType type)
    {
        if (type instanceof JavaClass)
        {
            JavaClass cls = (JavaClass) type;
            String name = cls.getName().getCanonicalName();
            return name.equals("java.lang.String");
        }
        return false;
    }

    public void accept(JavaExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
