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
import java.util.Map;

import com.newisys.langschema.java.util.ExpressionBuilder;

/**
 * Represents a Java enum type.
 * 
 * @author Trevor Robinson
 */
public final class JavaEnum
    extends JavaRawAbstractClass
    implements JavaClassMember, JavaInterfaceMember
{
    private final List<JavaMemberVariable> values;

    /**
     * General enum constructor.
     *
     * @param schema the schema this enum will be part of
     * @param id the identifier for this enum
     * @param outerType the type containing this enum, or null for a top-level
     *      enum
     * @param pkg the package containing this enum, or null for the default
     *      package
     */
    public JavaEnum(
        JavaSchema schema,
        String id,
        JavaPackage pkg,
        JavaStructuredType< ? > outerType)
    {
        super(schema, id, pkg, outerType);
        baseClass = schema.getEnumType().parameterize(this);
        this.values = new LinkedList<JavaMemberVariable>();
    }

    /**
     * Default package, top-level enum constructor.
     *
     * @param schema the schema this enum will be part of
     * @param id the identifier for this enum
     */
    public JavaEnum(JavaSchema schema, String id)
    {
        this(schema, id, null, null);
    }

    /**
     * Top-level enum constructor.
     *
     * @param schema the schema this enum will be part of
     * @param id the identifier for this enum
     * @param pkg the package containing this enum, or null for the default
     *      package
     */
    public JavaEnum(JavaSchema schema, String id, JavaPackage pkg)
    {
        this(schema, id, pkg, null);
    }

    /**
     * Nested enum constructor.
     *
     * @param schema the schema this enum will be part of
     * @param id the identifier for this enum
     * @param outerType the type containing this enum (cannot be null)
     */
    public JavaEnum(
        JavaSchema schema,
        String id,
        JavaStructuredType< ? > outerType)
    {
        this(schema, id, outerType.getPackage(), outerType);
    }

    public JavaEnum clone()
    {
        final JavaEnum clone = new JavaEnum(schema, name.getIdentifier());
        clone.copyFrom(this);
        return clone;
    }

    public void addTypeVariable(JavaTypeVariable var)
    {
        throw new UnsupportedOperationException(
            "Enums cannot have type variables");
    }

    protected JavaParameterizedType<JavaClassMember> createParameterization(
        Map<JavaTypeVariable, JavaReferenceType> typeVarMap,
        Map<JavaTypeVariable, JavaReferenceType> fullTypeVarMap)
    {
        throw new UnsupportedOperationException("Enums cannot be parameterized");
    }

    public List<JavaMemberVariable> getValues()
    {
        return values;
    }

    public JavaMemberVariable newValue(
        String id,
        boolean wantAnonClass,
        JavaExpression... args)
    {
        JavaRawAbstractClass type = this;
        if (wantAnonClass)
        {
            final JavaRawClass anonClass = new JavaRawClass(type);
            anonClass.addModifier(JavaTypeModifier.STATIC);
            type = anonClass;
        }
        final JavaMemberVariable var = new JavaMemberVariable(id, type);
        var.setVisibility(JavaVisibility.PUBLIC);
        var.addModifier(JavaVariableModifier.STATIC);
        var.addModifier(JavaVariableModifier.FINAL);
        if (args != null && args.length > 0)
        {
            var.setInitializer(ExpressionBuilder.newInstance(this, args, this));
        }
        values.add(var);
        addMember(var);
        return var;
    }

    public String toDebugString()
    {
        return "enum " + name;
    }

    public void accept(JavaSchemaMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(JavaPackageMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(JavaStructuredTypeMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(JavaTypeVisitor visitor)
    {
        visitor.visit(this);
    }
}
