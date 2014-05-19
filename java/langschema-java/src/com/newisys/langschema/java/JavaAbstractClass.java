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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.newisys.langschema.Class;

/**
 * Represents a Java class or similar construct, such as an array or enum.
 * 
 * @author Trevor Robinson
 */
public interface JavaAbstractClass
    extends Class, JavaStructuredType<JavaClassMember>
{
    JavaAbstractClass getBaseClass();

    List<JavaAbstractClass> getBaseClasses();

    boolean isSuperclassOf(JavaAbstractClass other);

    List<JavaClassMember> getMembers();

    Collection<JavaFunction> getNonOverriddenMethods();

    Set<JavaConstructor> getConstructors(JavaStructuredType typeContext);

    Set<JavaConstructor> getConstructors(
        JavaType[] argTypes,
        JavaStructuredType typeContext);

    Set<JavaConstructor> getConstructors(
        JavaType[] argTypes,
        boolean allowConversion,
        boolean allowVarArgs,
        JavaStructuredType typeContext);

    JavaConstructor getConstructor(
        JavaType[] argTypes,
        JavaStructuredType typeContext)
        throws ConstructorNotFoundException, AmbiguousConstructorException;

    JavaConstructor getConstructor(JavaType... argTypes)
        throws ConstructorNotFoundException, AmbiguousConstructorException;
}
