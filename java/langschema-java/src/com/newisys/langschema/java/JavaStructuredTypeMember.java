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

import com.newisys.langschema.StructuredTypeMember;

/**
 * Interface implemented by Java constructs that may be members of a structured
 * type.
 * 
 * @author Trevor Robinson
 */
public interface JavaStructuredTypeMember
    extends StructuredTypeMember, JavaSchemaObject
{
    JavaStructuredType< ? > getStructuredType();

    void setStructuredType(JavaStructuredType< ? > container);

    JavaVisibility getVisibility();

    boolean isAccessible(JavaStructuredType< ? > fromType);

    JavaStructuredTypeMember clone();

    void accept(JavaStructuredTypeMemberVisitor visitor);
}
