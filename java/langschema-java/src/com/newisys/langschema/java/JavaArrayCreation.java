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

import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.ArrayCreation;

/**
 * Represents a Java array creation expression.
 * 
 * @author Trevor Robinson
 */
public final class JavaArrayCreation
    extends JavaExpression
    implements ArrayCreation
{
    private final JavaArrayType type;
    private final List<JavaExpression> dimensions = new LinkedList<JavaExpression>();
    private JavaArrayInitializer initializer;

    public JavaArrayCreation(JavaArrayType type)
    {
        super(type.schema);
        this.type = type;
    }

    public JavaArrayType getResultType()
    {
        return type;
    }

    public JavaArrayType getType()
    {
        return type;
    }

    public List<JavaExpression> getDimensions()
    {
        return dimensions;
    }

    public void addDimension(JavaExpression expr)
    {
        assert (schema.intType.isAssignableFrom(expr.getResultType()));
        dimensions.add(expr);
    }

    public JavaArrayInitializer getInitializer()
    {
        return initializer;
    }

    public void setInitializer(JavaArrayInitializer initializer)
    {
        assert (initializer.getResultType().equals(type));
        this.initializer = initializer;
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
