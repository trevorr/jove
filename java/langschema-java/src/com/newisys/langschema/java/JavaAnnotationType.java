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

/**
 * Represents a Java annotation type.
 * 
 * @author Trevor Robinson
 */
public final class JavaAnnotationType
    extends JavaRawAbstractInterface
{
    /**
     * General annotation type constructor.
     *
     * @param schema the schema this annotation type will be part of
     * @param id the identifier for this annotation type
     * @param pkg the package containing this annotation type, or null for the
     *      default package
     * @param outerType the type containing this annotation type, or null for a
     *      top-level annotation type
     */
    protected JavaAnnotationType(
        JavaSchema schema,
        String id,
        JavaPackage pkg,
        JavaStructuredType< ? > outerType)
    {
        super(schema, id, pkg, outerType);
        super.addBaseInterface(schema.getAnnotationType());
    }

    /**
     * Default package, top-level annotation type constructor.
     *
     * @param schema the schema this annotation type will be part of
     * @param id the identifier for this annotation type
     */
    public JavaAnnotationType(JavaSchema schema, String id)
    {
        this(schema, id, null, null);
    }

    /**
     * Top-level annotation type constructor.
     *
     * @param schema the schema this annotation type will be part of
     * @param id the identifier for this annotation type
     * @param pkg the package containing this annotation type, or null for the
     *      default package
     */
    public JavaAnnotationType(JavaSchema schema, String id, JavaPackage pkg)
    {
        this(schema, id, pkg, null);
    }

    /**
     * Nested annotation type constructor.
     *
     * @param schema the schema this annotation type will be part of
     * @param id the identifier for this annotation type
     * @param outerType the type containing this annotation type (cannot be null)
     */
    public JavaAnnotationType(
        JavaSchema schema,
        String id,
        JavaStructuredType< ? > outerType)
    {
        this(schema, id, outerType.getPackage(), outerType);
    }

    public JavaAnnotationType clone()
    {
        final JavaAnnotationType clone = new JavaAnnotationType(schema, name
            .getIdentifier());
        clone.copyFrom(this);
        return clone;
    }

    public void addTypeVariable(JavaTypeVariable var)
    {
        throw new UnsupportedOperationException(
            "Annotation types cannot have type variables");
    }

    protected JavaParameterizedType<JavaInterfaceMember> createParameterization(
        Map<JavaTypeVariable, JavaReferenceType> typeVarMap,
        Map<JavaTypeVariable, JavaReferenceType> fullTypeVarMap)
    {
        throw new UnsupportedOperationException(
            "Annotation types cannot be parameterized");
    }

    public void addBaseInterface(JavaAbstractInterface baseInterface)
    {
        throw new UnsupportedOperationException(
            "Annotation types cannot explicitly extend any interfaces");
    }

    public final List<JavaFunction> getElements()
    {
        final LinkedList<JavaFunction> result = new LinkedList<JavaFunction>();
        for (final JavaInterfaceMember member : getMembers())
        {
            if (member instanceof JavaFunction)
            {
                result.add((JavaFunction) member);
            }
        }
        return result;
    }

    public final JavaFunction newElement(String id, JavaType returnType)
    {
        return newMethod(id, returnType);
    }

    public final JavaFunction newElement(
        String id,
        JavaType returnType,
        JavaAnnotationElementValue defaultValue)
    {
        final JavaFunction func = newMethod(id, returnType);
        func.setDefaultValue(defaultValue);
        return func;
    }

    public String toDebugString()
    {
        return "annotation type " + name;
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
