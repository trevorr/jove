/*
 * Jove - The Open Verification Environment for the Java (TM) Platform
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

package com.newisys.dv.ifgen.schema;

import com.newisys.langschema.NamedObject;

/**
 * Ifgen schema object for enumeration elements.
 * 
 * @author Jon Nall
 */
public class IfgenEnumElement
    extends IfgenSchemaObject
    implements NamedObject
{
    private static final long serialVersionUID = -8905369291273390165L;
    private final IfgenName name;
    private IfgenEnum enumType;

    public IfgenEnumElement(IfgenSchema schema, IfgenName name)
    {
        super(schema);
        this.name = name;
        this.enumType = null;
    }

    public IfgenEnumElement(
        IfgenSchema schema,
        IfgenName name,
        IfgenEnum enumType)
    {
        super(schema);
        this.name = name;
        this.enumType = enumType;
    }

    public IfgenName getName()
    {
        return name;
    }

    public void setEnum(IfgenEnum enumType)
    {
        this.enumType = enumType;
    }

    public IfgenEnumType getEnumType()
    {
        return (enumType == null) ? null : enumType.getType();
    }

    @Override
    public String toDebugString()
    {
        return name.toString();
    }

    @Override
    public void accept(IfgenSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }
}
