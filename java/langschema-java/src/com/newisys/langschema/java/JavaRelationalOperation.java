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
 * Base class for Java relational operations.
 * 
 * @author Trevor Robinson
 */
public abstract class JavaRelationalOperation
    extends JavaBinaryOperation
{
    public JavaRelationalOperation(
        JavaSchema schema,
        JavaExpression op1,
        JavaExpression op2)
    {
        super(op1, op2);
        JavaType type1 = op1.getResultType();
        JavaType type2 = op2.getResultType();
        assert (schema.isNumeric(type1) && schema.isNumeric(type2));
    }

    public JavaType getResultType()
    {
        return schema.booleanType;
    }
}
