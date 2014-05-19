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

import java.util.Collections;
import java.util.Set;

import com.newisys.langschema.Constructor;
import com.newisys.langschema.ConstructorModifier;

/**
 * Represents a Java constructor.
 * 
 * @author Trevor Robinson
 */
public final class JavaConstructor
    extends JavaFunctor
    implements Constructor, JavaClassMember
{
    public JavaConstructor(JavaSchema schema)
    {
        this(new JavaFunctionType(schema.voidType));
    }

    public JavaConstructor(JavaFunctionType funcType)
    {
        super(funcType);
    }

    public JavaConstructor clone()
    {
        final JavaConstructor clone = new JavaConstructor(funcType);
        clone.copyFrom(this);
        return clone;
    }

    public Set< ? extends ConstructorModifier> getModifiers()
    {
        return Collections.emptySet();
    }

    public void accept(JavaSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(JavaStructuredTypeMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "constructor " + container.getName();
    }
}
