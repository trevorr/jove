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

import java.util.Map;

/**
 * Represents a raw Java class (as opposed to a parameterized class).
 * 
 * @author Trevor Robinson
 */
public class JavaRawClass
    extends JavaRawAbstractClass
    implements JavaClass, JavaClassMember, JavaInterfaceMember, JavaBlockMember
{
    private JavaStatement containingStatement;

    /**
     * General class constructor.
     *
     * @param schema the schema this class will be part of
     * @param id the identifier for this class
     * @param pkg the package containing this class, or null for the default
     *      package
     * @param outerType the type containing this class, or null for a top-level
     *      class
     */
    protected JavaRawClass(
        JavaSchema schema,
        String id,
        JavaPackage pkg,
        JavaStructuredType< ? > outerType)
    {
        super(schema, id, pkg, outerType);
    }

    /**
     * Default package, top-level class constructor.
     *
     * @param schema the schema this class will be part of
     * @param id the identifier for this class
     */
    public JavaRawClass(JavaSchema schema, String id)
    {
        this(schema, id, null, null);
    }

    /**
     * Top-level class constructor.
     *
     * @param schema the schema this class will be part of
     * @param id the identifier for this class
     * @param pkg the package containing this class, or null for the default
     *      package
     */
    public JavaRawClass(JavaSchema schema, String id, JavaPackage pkg)
    {
        this(schema, id, pkg, null);
    }

    /**
     * Nested class constructor.
     *
     * @param schema the schema this class will be part of
     * @param id the identifier for this class
     * @param outerType the type containing this class (cannot be null)
     */
    public JavaRawClass(
        JavaSchema schema,
        String id,
        JavaStructuredType< ? > outerType)
    {
        this(schema, id, outerType.getPackage(), outerType);
    }

    /**
     * Anonymous class constructor.
     *
     * @param outerClass the class containing this anonymous class
     */
    public JavaRawClass(JavaRawAbstractClass outerClass)
    {
        this(outerClass.schema, "<anonymous>", outerClass);
    }

    public JavaRawClass clone()
    {
        final JavaRawClass clone = new JavaRawClass(schema, name
            .getIdentifier());
        clone.copyFrom(this);
        return clone;
    }

    public void addTypeVariable(JavaTypeVariable var)
    {
        typeVariables.add(var);
    }

    protected JavaParameterizedClass createParameterization(
        Map<JavaTypeVariable, JavaReferenceType> typeVarMap)
    {
        return new JavaParameterizedClass(this, typeVarMap);
    }

    protected void resolveParameterization(
        JavaParameterizedType<JavaClassMember> paramType,
        Map<JavaTypeVariable, JavaReferenceType> fullTypeVarMap)
    {
        JavaParameterizedClass paramCls = (JavaParameterizedClass) paramType;
        paramCls.resolveBaseClass();
        paramCls.resolveBaseInterfaces();
        paramCls.resolveMembers(fullTypeVarMap);
    }

    public JavaParameterizedClass parameterize(JavaReferenceType... args)
    {
        return (JavaParameterizedClass) super.parameterize(args);
    }

    public JavaParameterizedClass parameterize(
        Map<JavaTypeVariable, JavaReferenceType> typeVarMap)
    {
        return (JavaParameterizedClass) super.parameterize(typeVarMap);
    }

    public void setBaseClass(JavaClass baseClass)
    {
        this.baseClass = baseClass;
    }

    public JavaStatement getContainingStatement()
    {
        return containingStatement;
    }

    public void setContainingStatement(JavaStatement stmt)
    {
        this.containingStatement = stmt;
    }

    public boolean isSubtype(JavaType type)
    {
        return super.isSubtype(type)
            || (type instanceof JavaPrimitiveType && isSubtype(((JavaPrimitiveType) type)
                .getWrapperClass()))
            || (type instanceof JavaStructuredType && name.getCanonicalName()
                .equals("java.lang.Object"));
    }

    public String toDebugString()
    {
        return "class " + name;
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

    public void accept(JavaBlockMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(JavaTypeVisitor visitor)
    {
        visitor.visit(this);
    }
}
