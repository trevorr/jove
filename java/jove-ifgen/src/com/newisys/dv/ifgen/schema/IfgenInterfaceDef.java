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

import java.util.Collections;
import java.util.List;

/**
 * Ifgen schema object for default interface declarations.
 * 
 * @author Trevor Robinson
 * @author Jon Nall
 */
public final class IfgenInterfaceDef
    implements IfgenBindMember
{
    private final static List<IfgenExpression> emptyList = Collections
        .emptyList();

    private IfgenUnresolvedName intfName;
    private IfgenInterface intf;
    private final List<IfgenExpression> args;

    public IfgenInterfaceDef(IfgenUnresolvedName intfName)
    {
        this(intfName, emptyList);
    }

    public IfgenInterfaceDef(
        IfgenUnresolvedName intfName,
        List<IfgenExpression> args)
    {
        this.intfName = intfName;
        this.args = args;
    }

    public IfgenInterfaceDef(IfgenInterface intf)
    {
        this(null, emptyList);
        setIntf(intf);
    }

    public IfgenUnresolvedName getIntfName()
    {
        return intfName;
    }

    public void setIntfName(IfgenUnresolvedName intfName)
    {
        this.intfName = intfName;
    }

    public IfgenInterface getIntf()
    {
        return intf;
    }

    public void setIntf(IfgenInterface intf)
    {
        this.intf = intf;
    }

    public List<IfgenExpression> getArgs()
    {
        return this.args;
    }

    public void accept(IfgenBindMemberVisitor visitor)
    {
        visitor.visit(this);
    }
}
