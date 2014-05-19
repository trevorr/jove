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

import com.newisys.langschema.Expression;
import com.newisys.langschema.MemberAccess;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.StructuredTypeMember;
import com.newisys.langschema.java.JavaMemberVariable;
import com.newisys.langschema.java.JavaStructuredType;
import com.newisys.langschema.java.JavaStructuredTypeMember;
import com.newisys.langschema.java.JavaType;

public final class ConsMemberAccess
    extends ConsExpression
    implements MemberAccess
{
    private final ConsExpression object;
    private final JavaStructuredTypeMember member;

    public ConsMemberAccess(
        ConsExpression object,
        JavaStructuredTypeMember member)
    {
        super(object.schema);
        assert (object.getResultType() instanceof JavaStructuredType);
        assert (member instanceof JavaMemberVariable);
        this.object = object;
        this.member = member;
    }

    public JavaType getResultType()
    {
        assert (member instanceof JavaMemberVariable);
        return ((JavaMemberVariable) member).getType();
    }

    public Expression getObject()
    {
        return object;
    }

    public StructuredTypeMember getMember()
    {
        return member;
    }

    public String toSourceString()
    {
        String memberID = ((NamedObject) member).getName().getIdentifier();
        return object + "." + memberID;
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
