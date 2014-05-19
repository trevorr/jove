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
 * Ifgen schema object for interfaces.
 * 
 * @author Trevor Robinson
 * @author Jon Nall
 */
public final class IfgenInterface
    extends IfgenNamedTestbenchMember
    implements Namespace, IfgenSchemaMember, IfgenPackageMember, IfgenTemplate
{
    private static final long serialVersionUID = 4049637875903379252L;

    private final List<IfgenInterfaceMember> members = new LinkedList<IfgenInterfaceMember>();
    private final List<IfgenVariableDecl> parameters;
    private final NameTable nameTable = new NameTable();
    private IfgenPackage pkg;

    public IfgenInterface(IfgenSchema schema, IfgenName name)
    {
        this(schema, name, emptyVarDeclList);
    }

    public IfgenInterface(
        IfgenSchema schema,
        IfgenName name,
        List<IfgenVariableDecl> parameters)
    {
        super(schema, name);
        this.parameters = parameters;

        for (final IfgenVariableDecl p : parameters)
        {
            if (nameTable.lookupObjects(p.getName().getIdentifier(),
                IfgenNameKind.EXPRESSION).hasNext())
            {
                throw new Error("In interface '" + this.getName()
                    + "', parameter: '" + p.getName()
                    + "' has the same name as a previous parameter");
            }

            nameTable.addObject(p);
        }
    }

    public List<IfgenInterfaceMember> getMembers()
    {
        return members;
    }

    public List<IfgenVariableDecl> getParameters()
    {
        return this.parameters;
    }

    public void addMember(IfgenInterfaceMember member)
    {
        members.add(member);
        if (member instanceof NamedObject)
        {
            nameTable.addObject((NamedObject) member);
        }
    }

    public void addMember(int index, IfgenInterfaceMember member)
    {
        members.add(index, member);
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

    public boolean hasParameters()
    {
        return parameters.size() > 0;
    }
}
