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
 * Base interface for raw Java types (as opposed to parameterized types).
 * 
 * @author Trevor Robinson
 */
public interface JavaRawStructuredType<M extends JavaStructuredTypeMember>
    extends JavaStructuredType<M>, JavaSchemaMember, JavaPackageMember
{
    void setStructuredType(JavaStructuredType< ? > container);

    void setVisibility(JavaVisibility visibility);

    void addModifier(JavaTypeModifier modifier);

    void addBaseInterface(JavaAbstractInterface baseInterface);

    void addMember(M member);

    void addMemberBefore(M member, JavaStructuredTypeMember other);

    void addMemberAfter(M member, JavaStructuredTypeMember other);

    JavaMemberVariable newField(String id, JavaType type);

    JavaFunction newMethod(String id, JavaType returnType);
}
