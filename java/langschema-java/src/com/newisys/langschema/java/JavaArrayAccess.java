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

import com.newisys.langschema.ArrayAccess;

/**
 * Represents a Java array access.
 * 
 * @author Trevor Robinson
 */
public final class JavaArrayAccess
    extends JavaExpression
    implements ArrayAccess
{
    private final JavaExpression array;
    private final List<JavaExpression> indices = new LinkedList<JavaExpression>();

    public JavaArrayAccess(JavaExpression array)
    {
        super(array.schema);
        assert (array.getResultType() instanceof JavaArrayType);
        this.array = array;
    }

    public JavaType getResultType()
    {
        JavaArrayType arrayType = (JavaArrayType) array.getResultType();
        JavaType elementType = arrayType.getElementType();
        int dimensions = arrayType.getIndexTypes().length;
        int indexCount = indices.size();
        if (indexCount == dimensions)
        {
            // full element access
            return elementType;
        }
        else
        {
            // nested array access
            assert (indexCount > 0 && indexCount <= dimensions);
            return schema.getArrayType(elementType, dimensions - indexCount);
        }
    }

    public JavaExpression getArray()
    {
        return array;
    }

    public List<JavaExpression> getIndices()
    {
        return indices;
    }

    public void addIndex(JavaExpression expr)
    {
        assert (schema.intType.isAssignableFrom(expr.getResultType()));
        indices.add(expr);
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
