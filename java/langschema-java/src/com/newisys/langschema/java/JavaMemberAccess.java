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

import com.newisys.langschema.MemberAccess;

/**
 * Represents a Java member access expression.
 * 
 * @author Trevor Robinson
 */
public final class JavaMemberAccess
    extends JavaExpression
    implements MemberAccess
{
    private final JavaExpression object;
    private final JavaStructuredTypeMember member;

    public JavaMemberAccess(
        JavaExpression object,
        JavaStructuredTypeMember member)
    {
        super(object.schema);
        assert (object.getResultType() instanceof JavaStructuredType);
        assert (member instanceof JavaMemberVariable || member instanceof JavaFunction);
        this.object = object;
        this.member = member;
    }

    public JavaType getResultType()
    {
        if (member instanceof JavaMemberVariable)
        {
            return ((JavaMemberVariable) member).getType();
        }
        else
        {
            assert (member instanceof JavaFunction);
            return ((JavaFunction) member).getType();
        }
    }

    public JavaExpression getObject()
    {
        return object;
    }

    public JavaStructuredTypeMember getMember()
    {
        return member;
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
