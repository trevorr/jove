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

import java.util.HashSet;
import java.util.Set;

import com.newisys.langschema.Variable;

/**
 * Base class for Java variables.
 * 
 * @author Trevor Robinson
 */
public abstract class JavaVariable
    extends JavaSchemaObjectImpl
    implements Variable
{
    protected final JavaName name;
    protected JavaType type;
    private final Set<JavaVariableModifier> modifiers = new HashSet<JavaVariableModifier>();
    private JavaExpression initializer;

    public JavaVariable(String id, JavaType type)
    {
        super(type.getSchema());
        // type cannot be null, void, etc.
        assert (type instanceof JavaPrimitiveType
            || type instanceof JavaStructuredType || type instanceof JavaBoundedType);
        this.name = new JavaName(id, JavaNameKind.EXPRESSION);
        this.type = type;
    }

    final void copyFrom(JavaVariable other)
    {
        modifiers.addAll(other.modifiers);
        initializer = other.initializer;
    }

    public JavaName getName()
    {
        return name;
    }

    public JavaType getType()
    {
        return type;
    }

    public void setType(JavaType type)
    {
        this.type = type;
    }

    public Set<JavaVariableModifier> getModifiers()
    {
        return modifiers;
    }

    public boolean hasModifier(JavaVariableModifier mod)
    {
        return modifiers.contains(mod);
    }

    public void addModifier(JavaVariableModifier mod)
    {
        modifiers.add(mod);
    }

    public void addModifiers(Set<JavaVariableModifier> mods)
    {
        modifiers.addAll(mods);
    }

    public JavaExpression getInitializer()
    {
        return initializer;
    }

    public void setInitializer(JavaExpression initializer)
    {
        assert (initializer == null || type.isAssignableFrom(initializer
            .getResultType()));
        this.initializer = initializer;
    }

    public String toDebugString()
    {
        return "variable " + name;
    }
}
