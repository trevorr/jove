/*
 * LangSchema-Jove - Programming Language Modeling Classes for Jove
 * Copyright (C) 2005 Newisys, Inc. or its licensors, as applicable.
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

package com.newisys.langschema.jove;

import com.newisys.langschema.Name;
import com.newisys.langschema.java.JavaClass;
import com.newisys.langschema.java.JavaConstructor;
import com.newisys.langschema.java.JavaRawAbstractClass;
import com.newisys.langschema.java.JavaStructuredType;
import com.newisys.langschema.java.JavaType;
import com.newisys.langschema.java.JavaTypeVisitor;

/**
 * Represents an associative array of a particular element type.
 * 
 * @author Trevor Robinson
 */
public final class JoveAssocArrayType
    extends JavaRawAbstractClass
    implements JavaClass
{
    private final JavaType elementType;

    public JoveAssocArrayType(JavaClass baseClass, JavaType elementType)
    {
        super(baseClass.getSchema(), generateID(baseClass.getName(),
            elementType), baseClass.getPackage(), null);
        this.baseClass = baseClass;
        this.elementType = elementType;
    }

    private static String generateID(Name name, JavaType elementType)
    {
        return name.getIdentifier() + "<" + elementType.toReferenceString()
            + ">";
    }

    public JavaType getElementType()
    {
        return elementType;
    }

    public JavaConstructor getConstructor(
        JavaType[] argTypes,
        JavaStructuredType typeContext)
    {
        return getBaseClass().getConstructor(argTypes, typeContext);
    }

    public boolean equals(Object obj)
    {
        return obj instanceof JoveAssocArrayType
            && ((JoveAssocArrayType) obj).elementType.equals(elementType);
    }

    public int hashCode()
    {
        return getClass().hashCode() ^ elementType.hashCode();
    }

    public void accept(JavaTypeVisitor visitor)
    {
        ((JoveTypeVisitor) visitor).visit(this);
    }
}
