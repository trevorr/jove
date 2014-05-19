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
 * Represents a raw Java interface (as opposed to a parameterized interface).
 * 
 * @author Trevor Robinson
 */
public final class JavaRawInterface
    extends JavaRawAbstractInterface
    implements JavaInterface
{
    /**
     * General interface constructor.
     *
     * @param schema the schema this interface will be part of
     * @param id the identifier for this interface
     * @param pkg the package containing this interface, or null for the
     *      default package
     * @param outerType the type containing this interface, or null for a
     *      top-level interface
     */
    protected JavaRawInterface(
        JavaSchema schema,
        String id,
        JavaPackage pkg,
        JavaStructuredType< ? > outerType)
    {
        super(schema, id, pkg, outerType);
    }

    /**
     * Default package, top-level interface constructor.
     *
     * @param schema the schema this interface will be part of
     * @param id the identifier for this interface
     */
    public JavaRawInterface(JavaSchema schema, String id)
    {
        this(schema, id, null, null);
    }

    /**
     * Top-level interface constructor.
     *
     * @param schema the schema this interface will be part of
     * @param id the identifier for this interface
     * @param pkg the package containing this interface, or null for the
     *      default package
     */
    public JavaRawInterface(JavaSchema schema, String id, JavaPackage pkg)
    {
        this(schema, id, pkg, null);
    }

    /**
     * Nested interface constructor.
     *
     * @param schema the schema this interface will be part of
     * @param id the identifier for this interface
     * @param outerType the type containing this interface (cannot be null)
     */
    public JavaRawInterface(
        JavaSchema schema,
        String id,
        JavaStructuredType< ? > outerType)
    {
        this(schema, id, outerType.getPackage(), outerType);
    }

    public JavaRawInterface clone()
    {
        final JavaRawInterface clone = new JavaRawInterface(schema, name
            .getIdentifier());
        clone.copyFrom(this);
        return clone;
    }

    public void addTypeVariable(JavaTypeVariable var)
    {
        typeVariables.add(var);
    }

    protected JavaParameterizedInterface createParameterization(
        Map<JavaTypeVariable, JavaReferenceType> typeVarMap)
    {
        return new JavaParameterizedInterface(this, typeVarMap);
    }

    protected void resolveParameterization(
        JavaParameterizedType<JavaInterfaceMember> paramType,
        Map<JavaTypeVariable, JavaReferenceType> fullTypeVarMap)
    {
        JavaParameterizedInterface paramIntf = (JavaParameterizedInterface) paramType;
        paramIntf.resolveBaseInterfaces();
        paramIntf.resolveMembers(fullTypeVarMap);
    }

    public JavaParameterizedInterface parameterize(JavaReferenceType... args)
    {
        return (JavaParameterizedInterface) super.parameterize(args);
    }

    public JavaParameterizedInterface parameterize(
        Map<JavaTypeVariable, JavaReferenceType> typeVarMap)
    {
        return (JavaParameterizedInterface) super.parameterize(typeVarMap);
    }

    public String toDebugString()
    {
        return "interface " + name;
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
