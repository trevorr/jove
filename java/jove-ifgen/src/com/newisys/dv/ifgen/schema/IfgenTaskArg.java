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
 * Ifgen schema object for task arguments.
 * 
 * @author Trevor Robinson
 */
public final class IfgenTaskArg
    extends IfgenSchemaObject
    implements NamedObject
{
    private static final long serialVersionUID = 3257853177347978033L;

    private final IfgenName name;
    private IfgenDirection direction;
    private IfgenType type;
    private int size;

    public IfgenTaskArg(
        IfgenSchema schema,
        IfgenName name,
        IfgenDirection direction,
        IfgenType type,
        int size)
    {
        super(schema);
        this.name = name;
        this.direction = direction;
        this.type = type;
        this.size = size;
    }

    public IfgenName getName()
    {
        return name;
    }

    public IfgenDirection getDirection()
    {
        return direction;
    }

    public void setDirection(IfgenDirection direction)
    {
        this.direction = direction;
    }

    public IfgenType getType()
    {
        return type;
    }

    public void setType(IfgenType type)
    {
        this.type = type;
    }

    public int getSize()
    {
        return size;
    }

    public void setSize(int size)
    {
        this.size = size;
    }

    @Override
    public void accept(IfgenSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }
}
