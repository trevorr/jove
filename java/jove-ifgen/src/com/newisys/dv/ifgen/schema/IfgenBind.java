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
import com.newisys.langschema.util.NameTable;

/**
 * Ifgen schema object for binds.
 * 
 * @author Trevor Robinson
 * @author Jon Nall
 */
public final class IfgenBind
    extends IfgenNamedTestbenchMember
    implements IfgenSchemaMember, IfgenPackageMember, IfgenTemplate
{
    private static final long serialVersionUID = 3616443514435745593L;

    private IfgenUnresolvedName portName;
    private IfgenPort port;
    private final List<IfgenBindMember> members = new LinkedList<IfgenBindMember>();
    private final List<IfgenVariableDecl> parameters;
    private final NameTable nameTable = new NameTable();
    private IfgenPackage pkg;

    public IfgenBind(IfgenSchema schema, IfgenName name, IfgenPort port)
    {
        this(schema, name, null, emptyVarDeclList);
        setPort(port);
    }

    public IfgenBind(
        IfgenSchema schema,
        IfgenName name,
        IfgenUnresolvedName portName,
        List<IfgenVariableDecl> parameters)
    {
        super(schema, name);
        this.portName = portName;
        this.parameters = parameters;

        for (final IfgenVariableDecl p : parameters)
        {
            if (nameTable.lookupObjects(p.getName().getIdentifier(),
                IfgenNameKind.EXPRESSION).hasNext())
            {
                throw new Error("In bind '" + this.getName()
                    + "', parameter: '" + p.getName()
                    + "' has the same name as a previous parameter");
            }

            nameTable.addObject(p);
        }
    }

    public List<IfgenVariableDecl> getParameters()
    {
        return this.parameters;
    }

    public IfgenUnresolvedName getPortName()
    {
        return portName;
    }

    public void setPortName(IfgenUnresolvedName portName)
    {
        this.portName = portName;
    }

    public IfgenPort getPort()
    {
        return port;
    }

    public void setPort(IfgenPort port)
    {
        this.port = port;
    }

    public List<IfgenBindMember> getMembers()
    {
        return members;
    }

    public void addMember(IfgenBindMember member)
    {
        members.add(member);
    }

    public void addMember(int index, IfgenBindMember member)
    {
        members.add(index, member);
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

    public Iterator< ? extends NamedObject> lookupObjects(
        String identifier,
        NameKind kind)
    {
        return nameTable.lookupObjects(identifier, kind);
    }

    public boolean hasParameters()
    {
        return parameters.size() > 0;
    }
}
