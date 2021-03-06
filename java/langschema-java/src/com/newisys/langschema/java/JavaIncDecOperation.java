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
 * Base class for Java increment/decrement operations.
 * 
 * @author Trevor Robinson
 */
public abstract class JavaIncDecOperation
    extends JavaUnaryOperation
{
    public JavaIncDecOperation(JavaSchema schema, JavaExpression op1)
    {
        super(op1);
        checkVarRefExpr(op1);
        assert (schema.isNumeric(op1.getResultType()));
    }

    public JavaType getResultType()
    {
        return operands[0].getResultType();
    }

    public boolean isConstant()
    {
        return false;
    }
}
