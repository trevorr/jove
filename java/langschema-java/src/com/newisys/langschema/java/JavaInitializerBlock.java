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
 * Represents a Java class initializer block.
 * 
 * @author Trevor Robinson
 */
public final class JavaInitializerBlock
    extends JavaBlock
    implements JavaClassMember
{
    private boolean _static;
    private JavaStructuredType container;

    public JavaInitializerBlock(JavaSchema schema, boolean _static)
    {
        super(schema);
        this._static = _static;
    }

    public JavaInitializerBlock(JavaSchema schema)
    {
        this(schema, false);
    }

    public JavaInitializerBlock clone()
    {
        throw new UnsupportedOperationException("Clone not supported");
    }

    public boolean isStatic()
    {
        return _static;
    }

    public void setStatic(boolean _static)
    {
        this._static = _static;
    }

    public JavaVisibility getVisibility()
    {
        return JavaVisibility.PRIVATE;
    }

    public boolean isAccessible(JavaStructuredType< ? > fromType)
    {
        return JavaStructuredTypeImpl.isAccessible(this, fromType);
    }

    public JavaStructuredType< ? > getStructuredType()
    {
        return container;
    }

    public void setStructuredType(JavaStructuredType< ? > container)
    {
        this.container = container;
    }

    public void accept(JavaStructuredTypeMemberVisitor visitor)
    {
        visitor.visit(this);
    }
}
