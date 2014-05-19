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

import com.newisys.langschema.Type;

/**
 * Base interface for all Java types.
 * 
 * @author Trevor Robinson
 */
public interface JavaType
    extends Type, JavaSchemaObject
{
    /**
     * Returns whether this type is a type variable or contains references to
     * type variables.
     *
     * @return true iff this type contains type variables
     */
    boolean hasTypeVariables();

    /**
     * Returns the erasure of this type.
     *
     * @return the erasure of this type
     */
    JavaType getErasure();

    /**
     * Return whether the given type is a subtype of this type.
     *
     * @param type the type to test
     * @return true iff the given type is a subtype of this type
     */
    boolean isSubtype(JavaType type);

    /**
     * Return whether this type contains the given type.
     *
     * @param type the type to test
     * @return true iff this type contains the given type
     */
    boolean contains(JavaType type);

    /**
     * Returns the binary/VM name of this type.
     *
     * @return the binary name of this type
     */
    String toInternalName();

    /**
     * Returns the Java syntax used to reference this type.
     *
     * @return a textual reference to this type
     */
    String toReferenceString();

    void accept(JavaTypeVisitor visitor);
}
