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
 * Ifgen schema object for interface signals.
 * 
 * @author Trevor Robinson
 */
public final class IfgenInterfaceSignal
    extends IfgenSchemaObject
    implements IfgenInterfaceMember, NamedObject
{
    private static final long serialVersionUID = 3976733688010586164L;

    private final IfgenInterface intf;
    private final IfgenName name;
    private IfgenSignalType type;
    private int size;
    private IfgenSampleDef sample;
    private IfgenDriveDef drive;
    private int sampleDepth;
    private IfgenModuleDef module;
    private IfgenSignalRef hdlNode;
    private IfgenSignalRef effectiveHDLNode;

    public IfgenInterfaceSignal(
        IfgenInterface intf,
        String id,
        IfgenSignalType type,
        int size)
    {
        super(intf.getSchema());
        this.intf = intf;
        this.name = new IfgenName(id, IfgenNameKind.EXPRESSION, intf);
        this.type = type;
        this.size = size;
        sampleDepth = 1;
    }

    public IfgenInterfaceSignal makeCopy(IfgenInterface newIntf)
    {
        IfgenInterfaceSignal copy = new IfgenInterfaceSignal(newIntf, name
            .getIdentifier(), type, size);
        copy.sample = this.sample;
        copy.drive = this.drive;
        copy.sampleDepth = this.sampleDepth;
        copy.module = this.module;
        copy.hdlNode = this.hdlNode;
        copy.effectiveHDLNode = this.effectiveHDLNode;
        return copy;
    }

    public IfgenInterface getInterface()
    {
        return intf;
    }

    public IfgenName getName()
    {
        return name;
    }

    public IfgenSignalType getType()
    {
        return type;
    }

    public void setType(IfgenSignalType type)
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

    public IfgenSampleDef getSample()
    {
        return sample;
    }

    public void setSample(IfgenSampleDef sample)
    {
        this.sample = sample;
    }

    public IfgenDriveDef getDrive()
    {
        return drive;
    }

    public void setDrive(IfgenDriveDef drive)
    {
        this.drive = drive;
    }

    public int getSampleDepth()
    {
        return sampleDepth;
    }

    public void setSampleDepth(int sampleDepth)
    {
        this.sampleDepth = sampleDepth;
    }

    public IfgenModuleDef getModule()
    {
        return module;
    }

    public void setModule(IfgenModuleDef module)
    {
        this.module = module;
    }

    public IfgenSignalRef getHDLNode()
    {
        return hdlNode;
    }

    public void setHDLNode(IfgenSignalRef hdlNode)
    {
        this.hdlNode = hdlNode;
    }

    public IfgenSignalRef getEffectiveHDLNode()
    {
        return effectiveHDLNode;
    }

    public void setEffectiveHDLNode(IfgenSignalRef effectiveHDLNode)
    {
        this.effectiveHDLNode = effectiveHDLNode;
    }

    public void accept(IfgenInterfaceMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public void accept(IfgenSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }
}
