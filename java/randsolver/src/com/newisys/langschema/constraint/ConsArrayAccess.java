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

import com.newisys.langschema.ArrayAccess;
import com.newisys.langschema.Expression;
import com.newisys.langschema.java.JavaArrayType;
import com.newisys.langschema.java.JavaIntType;
import com.newisys.langschema.java.JavaType;

public final class ConsArrayAccess
    extends ConsExpression
    implements ArrayAccess
{
    private final ConsExpression array;
    private final List indices = new LinkedList(); // List<ConsExpression>

    public ConsArrayAccess(ConsExpression array)
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

    public Expression getArray()
    {
        return array;
    }

    public List getIndices()
    {
        return indices;
    }

    public void addIndex(ConsExpression expr)
    {
        assert (expr.getResultType() instanceof JavaIntType);
        indices.add(expr);
    }

    public String toSourceString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append(array);
        Iterator iter = indices.iterator();
        while (iter.hasNext())
        {
            buf.append('[');
            buf.append(iter.next());
            buf.append(']');
        }
        return buf.toString();
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
