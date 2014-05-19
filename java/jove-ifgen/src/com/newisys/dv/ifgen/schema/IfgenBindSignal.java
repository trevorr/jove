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

/**
 * Ifgen schema object for bind signals.
 * 
 * @author Trevor Robinson
 */
public final class IfgenBindSignal
    extends IfgenSchemaObject
    implements IfgenBindMember
{
    private static final long serialVersionUID = 3977578117185288500L;

    private final IfgenBind bind;
    private String portSignalID;
    private IfgenPortSignal portSignal;
    private IfgenSignalRef intfSignal;

    public IfgenBindSignal(
        IfgenBind bind,
        String portSignalID,
        IfgenSignalRef intfSignal)
    {
        super(bind.getSchema());
        this.bind = bind;
        this.portSignalID = portSignalID;
        this.intfSignal = intfSignal;
    }

    public IfgenBindSignal(
        IfgenBind bind,
        IfgenPortSignal portSignal,
        IfgenSignalRef intfSignal)
    {
        super(bind.getSchema());
        this.bind = bind;
        this.portSignal = portSignal;
        this.intfSignal = intfSignal;
    }

    public IfgenBind getBind()
    {
        return bind;
    }

    public String getPortSignalID()
    {
        return portSignalID;
    }

    public void setPortSignalID(String portSignalID)
    {
        this.portSignalID = portSignalID;
    }

    public IfgenPortSignal getPortSignal()
    {
        return portSignal;
    }

    public void setPortSignal(IfgenPortSignal portSignal)
    {
        this.portSignal = portSignal;
    }

    public IfgenSignalRef getIntfSignal()
    {
        return intfSignal;
    }

    public void setIntfSignal(IfgenSignalRef intfSignal)
    {
        this.intfSignal = intfSignal;
    }

    public void accept(IfgenBindMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public void accept(IfgenSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }
}
