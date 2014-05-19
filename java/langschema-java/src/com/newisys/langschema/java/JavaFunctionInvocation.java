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

import com.newisys.langschema.FunctionInvocation;

/**
 * Represents a Java method invocation expression.
 * 
 * @author Trevor Robinson
 */
public final class JavaFunctionInvocation
    extends JavaExpression
    implements FunctionInvocation
{
    private final JavaExpression function;
    private JavaType resultType;
    private final List<JavaExpression> arguments = new LinkedList<JavaExpression>();

    public JavaFunctionInvocation(JavaExpression function)
    {
        super(function.schema);
        this.function = function;
        assert (function.getResultType() instanceof JavaFunctionType);
        JavaFunctionType funcType = (JavaFunctionType) function.getResultType();
        resultType = funcType.getReturnType();
    }

    public JavaType getResultType()
    {
        return resultType;
    }

    public void setResultType(JavaType resultType)
    {
        this.resultType = resultType;
    }

    public JavaExpression getFunction()
    {
        return function;
    }

    public List<JavaExpression> getArguments()
    {
        return arguments;
    }

    public void addArgument(JavaExpression expr)
    {
        arguments.add(expr);
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
