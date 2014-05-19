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
 * Base class for Java binary operations.
 * 
 * @author Trevor Robinson
 */
public abstract class JavaBinaryOperation
    extends JavaOperation
{
    public JavaBinaryOperation(JavaExpression op1, JavaExpression op2)
    {
        super(op1.schema, new JavaExpression[2]);
        operands[0] = op1;
        operands[1] = op2;
    }

    public boolean isConstant()
    {
        return operands[0].isConstant() && operands[1].isConstant();
    }
}
