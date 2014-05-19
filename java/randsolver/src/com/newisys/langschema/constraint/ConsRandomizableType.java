/*
 * Jove Constraint-based Random Solver
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

package com.newisys.langschema.constraint;

import com.newisys.langschema.Type;
import com.newisys.langschema.java.JavaConstructor;
import com.newisys.langschema.java.JavaRawAbstractClass;
import com.newisys.langschema.java.JavaStructuredType;
import com.newisys.langschema.java.JavaType;
import com.newisys.langschema.java.JavaTypeVisitor;
import com.newisys.langschema.jove.JoveBitVectorType;

public final class ConsRandomizableType
    extends JavaRawAbstractClass
{
    // This type is used for classes that have a ConstraintProvider, such
    // as enums.

    private static final long serialVersionUID = 3834305133491335472L;
    private Class enumClass;
    private final static int size = 32;

    public ConsRandomizableType(ConsSchema schema, Class enumClass, int size)
    {
        super(schema, schema.randomizableType.getName().getIdentifier(),
            schema.randomizableType.getPackage(), null);
        this.baseClass = schema.getObjectType();
        this.enumClass = enumClass;
    }

    public Class getEnumClass()
    {
        return enumClass;
    }

    public boolean isAssignableFrom(Type other)
    {
        return other instanceof JoveBitVectorType || other == getBaseClass()
            || super.isAssignableFrom(other);
    }

    public boolean isStrictIntegral()
    {
        return true;
    }

    public JavaConstructor getConstructor(
        JavaType[] argTypes,
        JavaStructuredType typeContext)
    {
        return getBaseClass().getConstructor(argTypes, typeContext);
    }

    public boolean equals(Object obj)
    {
        return obj instanceof ConsRandomizableType
            && ((ConsRandomizableType) obj).enumClass == enumClass;
    }

    public int hashCode()
    {
        return getClass().hashCode() ^ enumClass.hashCode() ^ (size * 43);
    }

    public void accept(JavaTypeVisitor visitor)
    {
        // ignored
    }
}
