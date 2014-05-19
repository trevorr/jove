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

package com.newisys.langschema.java.util;

import java.util.List;

import com.newisys.langschema.java.*;

/**
 * Helper methods for building common expressions.
 * 
 * @author Trevor Robinson
 */
public final class ExpressionBuilder
{
    private ExpressionBuilder()
    {
        // prevent instantiation
    }

    public static JavaFunctionInvocation memberCall(
        JavaExpression obj,
        String methodID)
    {
        return memberCall(obj, methodID, (JavaExpression[]) null, null);
    }

    public static JavaFunctionInvocation memberCall(
        JavaExpression obj,
        String methodID,
        JavaExpression arg1)
    {
        return memberCall(obj, methodID, new JavaExpression[] { arg1 }, null);
    }

    public static JavaFunctionInvocation memberCall(
        JavaExpression obj,
        String methodID,
        JavaExpression arg1,
        JavaExpression arg2)
    {
        return memberCall(obj, methodID, new JavaExpression[] { arg1, arg2 },
            null);
    }

    public static JavaFunctionInvocation memberCall(
        JavaExpression obj,
        String methodID,
        JavaExpression[] args,
        JavaStructuredType typeContext)
    {
        return memberCall(obj, methodID, args, getArgTypes(args), typeContext);
    }

    public static JavaFunctionInvocation memberCall(
        JavaExpression obj,
        String methodID,
        JavaExpression[] args,
        JavaType[] argTypes,
        JavaStructuredType typeContext)
    {
        JavaStructuredType objType = (JavaStructuredType) obj.getResultType();
        JavaFunction method = objType
            .getMethod(methodID, argTypes, typeContext);
        JavaFunctionInvocation callExpr = new JavaFunctionInvocation(
            new JavaMemberAccess(obj, method));
        if (args != null)
        {
            for (int i = 0; i < args.length; ++i)
            {
                callExpr.addArgument(args[i]);
            }
        }
        return callExpr;
    }

    public static JavaFunctionInvocation staticCall(
        JavaAbstractClass cls,
        String methodID)
    {
        return staticCall(cls, methodID, (JavaExpression[]) null, null);
    }

    public static JavaFunctionInvocation staticCall(
        JavaAbstractClass cls,
        String methodID,
        JavaExpression arg1)
    {
        return staticCall(cls, methodID, new JavaExpression[] { arg1 }, null);
    }

    public static JavaFunctionInvocation staticCall(
        JavaAbstractClass cls,
        String methodID,
        JavaExpression arg1,
        JavaExpression arg2)
    {
        return staticCall(cls, methodID, new JavaExpression[] { arg1, arg2 },
            null);
    }

    public static JavaFunctionInvocation staticCall(
        JavaAbstractClass cls,
        String methodID,
        JavaExpression[] args,
        JavaStructuredType typeContext)
    {
        return staticCall(cls, methodID, args, getArgTypes(args), typeContext);
    }

    public static JavaFunctionInvocation staticCall(
        JavaAbstractClass cls,
        String methodID,
        JavaExpression[] args,
        JavaType[] argTypes,
        JavaStructuredType typeContext)
    {
        JavaFunction method = cls.getMethod(methodID, argTypes, typeContext);
        JavaFunctionInvocation callExpr = new JavaFunctionInvocation(
            new JavaFunctionReference(method));
        if (args != null)
        {
            for (int i = 0; i < args.length; ++i)
            {
                callExpr.addArgument(args[i]);
            }
        }
        return callExpr;
    }

    public static JavaInstanceCreation newInstance(JavaAbstractClass cls)
    {
        return newInstance(cls, (JavaExpression[]) null, null);
    }

    public static JavaInstanceCreation newInstance(
        JavaAbstractClass cls,
        JavaStructuredType typeContext)
    {
        return newInstance(cls, (JavaExpression[]) null, typeContext);
    }

    public static JavaInstanceCreation newInstance(
        JavaAbstractClass cls,
        JavaExpression arg1)
    {
        return newInstance(cls, new JavaExpression[] { arg1 }, null);
    }

    public static JavaInstanceCreation newInstance(
        JavaAbstractClass cls,
        JavaExpression arg1,
        JavaStructuredType typeContext)
    {
        return newInstance(cls, new JavaExpression[] { arg1 }, typeContext);
    }

    public static JavaInstanceCreation newInstance(
        JavaAbstractClass cls,
        JavaExpression arg1,
        JavaExpression arg2)
    {
        return newInstance(cls, new JavaExpression[] { arg1, arg2 }, null);
    }

    public static JavaInstanceCreation newInstance(
        JavaAbstractClass cls,
        JavaExpression arg1,
        JavaExpression arg2,
        JavaStructuredType typeContext)
    {
        return newInstance(cls, new JavaExpression[] { arg1, arg2 },
            typeContext);
    }

    public static JavaInstanceCreation newInstance(
        JavaAbstractClass cls,
        JavaExpression[] args,
        JavaStructuredType typeContext)
    {
        JavaType[] argTypes = getArgTypes(args);
        JavaConstructor ctor = cls.getConstructor(argTypes, typeContext);
        JavaInstanceCreation newExpr = new JavaInstanceCreation(cls, ctor);
        if (args != null)
        {
            for (int i = 0; i < args.length; ++i)
            {
                newExpr.addArgument(args[i]);
            }
        }
        return newExpr;
    }

    public static JavaType[] getArgTypes(JavaExpression[] args)
    {
        JavaType[] argTypes = null;
        if (args != null)
        {
            int argCount = args.length;
            argTypes = new JavaType[argCount];
            for (int i = 0; i < argCount; ++i)
            {
                argTypes[i] = args[i].getResultType();
            }
        }
        return argTypes;
    }

    public static JavaType[] getArgTypes(List<JavaExpression> args)
    {
        JavaType[] argTypes = null;
        if (args != null)
        {
            JavaExpression[] argArray = new JavaExpression[args.size()];
            args.toArray(argArray);
            argTypes = getArgTypes(argArray);
        }
        return argTypes;
    }

    public static JavaExpression checkDowncast(
        JavaExpression expr,
        JavaType type)
    {
        if (!expr.getResultType().equals(type))
        {
            expr = new JavaCastExpression(type, expr);
        }
        return expr;
    }
}
