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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.newisys.langschema.Operation;
import com.newisys.langschema.OperationModifier;
import com.newisys.langschema.StructuredTypeMember;

/**
 * Base class for Java operation expressions.
 * 
 * @author Trevor Robinson
 */
public abstract class JavaOperation
    extends JavaExpression
    implements Operation
{
    protected final JavaExpression[] operands;

    protected JavaOperation(JavaSchema schema, JavaExpression[] operands)
    {
        super(schema);
        this.operands = operands;
    }

    public List<JavaExpression> getOperands()
    {
        return Arrays.asList(operands);
    }

    public JavaExpression getOperand(int index)
    {
        return operands[index];
    }

    public Set< ? extends OperationModifier> getModifiers()
    {
        return Collections.emptySet();
    }

    protected JavaType promote(JavaType type)
    {
        return (type instanceof JavaByteType || type instanceof JavaShortType || type instanceof JavaCharType)
            ? schema.intType : type;
    }

    protected JavaType promote(JavaType type1, JavaType type2)
    {
        if (type1 instanceof JavaDoubleType || type2 instanceof JavaDoubleType)
        {
            return schema.doubleType;
        }
        if (type1 instanceof JavaFloatType || type2 instanceof JavaFloatType)
        {
            return schema.floatType;
        }
        if (type1 instanceof JavaLongType || type2 instanceof JavaLongType)
        {
            return schema.longType;
        }
        return schema.intType;
    }

    protected static void checkVarRefExpr(JavaExpression expr)
    {
        if (expr instanceof JavaMemberAccess)
        {
            JavaMemberAccess memberAccess = (JavaMemberAccess) expr;
            StructuredTypeMember member = memberAccess.getMember();
            assert (member instanceof JavaMemberVariable);
        }
        else
        {
            assert (expr instanceof JavaVariableReference
                || expr instanceof JavaArrayAccess || expr instanceof JavaAssignmentOperation);
        }
    }
}
