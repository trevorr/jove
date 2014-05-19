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

import com.newisys.langschema.ArrayInitializer;

/**
 * Represents a Java array initializer expression.
 * 
 * @author Trevor Robinson
 */
public final class JavaArrayInitializer
    extends JavaExpression
    implements ArrayInitializer
{
    private final JavaArrayType type;
    private final List<JavaExpression> elements = new LinkedList<JavaExpression>();

    public JavaArrayInitializer(JavaArrayType type)
    {
        super(type.schema);
        this.type = type;
    }

    public JavaArrayType getResultType()
    {
        return type;
    }

    public List<JavaExpression> getElements()
    {
        return elements;
    }

    public void addElement(JavaExpression elem)
    {
        assert (type.getAccessType(1).isAssignableFrom(elem.getResultType()));
        elements.add(elem);
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
