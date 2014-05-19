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
 * Ifgen schema object for port signals.
 * 
 * @author Trevor Robinson
 */
public final class IfgenPortSignal
    extends IfgenSchemaObject
    implements NamedObject
{
    private static final long serialVersionUID = 3257284712572989753L;

    private final IfgenPort port;
    private final IfgenDirection direction;
    private final IfgenName name;
    private final boolean fixedDir;

    public IfgenPortSignal(
        IfgenPort port,
        String id,
        IfgenDirection direction,
        boolean fixedDir)
    {
        super(port.getSchema());
        this.port = port;
        this.name = new IfgenName(id, IfgenNameKind.EXPRESSION, port);
        this.direction = direction;
        this.fixedDir = fixedDir;
    }

    public IfgenPort getPort()
    {
        return port;
    }

    public IfgenName getName()
    {
        return name;
    }

    public IfgenDirection getDirection()
    {
        return direction;
    }

    public boolean isDirectionFixed()
    {
        return fixedDir;
    }

    @Override
    public void accept(IfgenSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }
}
