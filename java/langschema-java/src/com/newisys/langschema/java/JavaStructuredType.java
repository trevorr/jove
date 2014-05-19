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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.newisys.langschema.StructuredType;

/**
 * Base interface for Java structured types, such as classes and interfaces.
 * 
 * @author Trevor Robinson
 */
public interface JavaStructuredType<M extends JavaStructuredTypeMember>
    extends StructuredType, JavaType, JavaTypeBound, JavaGenericDeclaration,
    JavaStructuredTypeMember
{
    JavaName getName();

    JavaPackage getPackage();

    JavaStructuredType< ? > getStructuredType();

    boolean isContainingTypeOf(JavaStructuredTypeMember other);

    JavaVisibility getVisibility();

    Set<JavaTypeModifier> getModifiers();

    JavaRawStructuredType<M> getRawType();

    List<JavaTypeVariable> getFullTypeVariables();

    JavaParameterizedType<M> parameterize(JavaReferenceType... args);

    JavaParameterizedType<M> parameterize(
        Map<JavaTypeVariable, JavaReferenceType> fullTypeVarMap);

    List<JavaAbstractInterface> getBaseInterfaces();

    boolean implementsInterface(JavaAbstractInterface intf);

    List<M> getMembers();

    JavaMemberVariable getField(String id, JavaStructuredType typeContext)
        throws FieldNotFoundException;

    JavaMemberVariable getField(String id)
        throws FieldNotFoundException;

    Set<JavaFunction> getMethods(String id, JavaStructuredType typeContext);

    Set<JavaFunction> getMethods(
        String id,
        JavaType[] argTypes,
        JavaStructuredType typeContext);

    Set<JavaFunction> getMethods(
        String id,
        JavaType[] argTypes,
        boolean allowConversion,
        boolean allowVarArgs,
        JavaStructuredType typeContext);

    JavaFunction getMethod(
        String id,
        JavaType[] argTypes,
        JavaStructuredType typeContext)
        throws MethodNotFoundException, AmbiguousMethodException;

    JavaFunction getMethod(String id, JavaType... argTypes)
        throws MethodNotFoundException, AmbiguousMethodException;

    String toReferenceString();

    String toInternalName();
}
