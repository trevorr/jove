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
 * Represents a Java type member variable.
 * 
 * @author Trevor Robinson
 */
public final class JavaMemberVariable
    extends JavaVariable
    implements JavaClassMember, JavaInterfaceMember
{
    private JavaVisibility visibility;
    private JavaStructuredType container;

    public JavaMemberVariable(String id, JavaType type)
    {
        super(id, type);
        visibility = JavaVisibility.DEFAULT;
    }

    final void copyFrom(JavaMemberVariable other)
    {
        super.copyFrom(other);
        visibility = other.visibility;
    }

    public JavaMemberVariable clone()
    {
        final JavaMemberVariable clone = new JavaMemberVariable(name
            .getIdentifier(), type);
        clone.copyFrom(this);
        return clone;
    }

    public JavaVisibility getVisibility()
    {
        return visibility;
    }

    public void setVisibility(JavaVisibility visibility)
    {
        this.visibility = visibility;
    }

    public boolean isAccessible(JavaStructuredType< ? > fromType)
    {
        return JavaStructuredTypeImpl.isAccessible(this, fromType);
    }

    public JavaStructuredType< ? > getStructuredType()
    {
        return container;
    }

    public void setStructuredType(JavaStructuredType< ? > container)
    {
        this.container = container;
        name.setNamespace(container);
    }

    public void accept(JavaSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(JavaStructuredTypeMemberVisitor visitor)
    {
        visitor.visit(this);
    }
}
