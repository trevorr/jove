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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.Namespace;
import com.newisys.langschema.util.NameTable;

/**
 * Ifgen schema object for packages.
 * 
 * @author Trevor Robinson
 */
public final class IfgenPackage
    extends IfgenSchemaObject
    implements Namespace, IfgenSchemaMember, IfgenPackageMember
{
    private static final long serialVersionUID = 3257850973962777143L;

    private final IfgenName name;
    private final List<IfgenPackageMember> members = new LinkedList<IfgenPackageMember>();
    private final NameTable nameTable = new NameTable();
    private IfgenPackage pkg;

    public IfgenPackage(IfgenSchema schema, IfgenName name, IfgenPackage pkg)
    {
        super(schema);
        this.name = name;
        this.pkg = pkg;
    }

    public IfgenName getName()
    {
        return name;
    }

    public List<IfgenPackageMember> getMembers()
    {
        return members;
    }

    public void addMember(IfgenPackageMember member)
    {
        member.setPackage(this);
        members.add(member);
        if (member instanceof NamedObject)
        {
            nameTable.addObject((NamedObject) member);
        }
    }

    public Iterator<NamedObject> lookupObjects(String identifier, NameKind kind)
    {
        return nameTable.lookupObjects(identifier, kind);
    }

    public IfgenPackage getPackage()
    {
        return pkg;
    }

    public void setPackage(IfgenPackage pkg)
    {
        this.pkg = pkg;
    }

    public void accept(IfgenSchemaMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(IfgenPackageMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public void accept(IfgenSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }
}
