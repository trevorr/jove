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

/**
 * Represents a Java addition-assignment operation.
 * 
 * @author Trevor Robinson
 */
public final class JavaAssignAdd
    extends JavaAssignmentOperation
{
    public JavaAssignAdd(
        JavaSchema schema,
        JavaExpression op1,
        JavaExpression op2)
    {
        super(schema, op1, op2);
        JavaType type1 = op1.getResultType();
        JavaType type2 = op2.getResultType();
        if (!(schema.isNumeric(type1) && schema.isNumeric(type2)))
        {
            JavaType stringType = schema
                .getTypeForSystemClass("java.lang.String");
            assert (type1.equals(stringType));
        }
    }

    public void accept(JavaExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
