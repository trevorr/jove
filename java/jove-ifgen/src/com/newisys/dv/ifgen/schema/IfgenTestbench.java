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

import com.newisys.langschema.Name;
import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.Namespace;
import com.newisys.langschema.util.NameTable;

/**
 * Ifgen schema object for testbench definitions.
 * 
 * @author Jon Nall
 */
public final class IfgenTestbench
    extends IfgenSchemaObject
    implements Namespace, IfgenSchemaMember, IfgenPackageMember,
    IfgenTestbenchMemberContainer, IfgenTemplate
{
    private static final long serialVersionUID = -6181316866280187211L;
    private final IfgenName name;
    private IfgenPackage pkg;
    private List<IfgenTestbenchMember> members = new LinkedList<IfgenTestbenchMember>();
    private List<IfgenWildname> imports = new LinkedList<IfgenWildname>();
    private final List<IfgenVariableDecl> parameters;
    private final NameTable nameTable = new NameTable();

    public IfgenTestbench(
        IfgenSchema schema,
        IfgenName name,
        List<IfgenVariableDecl> parameters)
    {
        super(schema);
        this.name = name;
        this.parameters = parameters;

        for (final IfgenVariableDecl decl : parameters)
        {
            nameTable.addObject(decl);
        }
    }

    @Override
    public void accept(IfgenSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    public Name getName()
    {
        return name;
    }

    public void addImportDecl(IfgenWildname wildname)
    {
        imports.add(wildname);
    }

    public List<IfgenWildname> getImports()
    {
        return imports;
    }

    public List<IfgenVariableDecl> getParameters()
    {
        return parameters;
    }

    public void addMember(IfgenTestbenchMember member)
    {
        members.add(member);
    }

    public void addMember(IfgenNamedTestbenchMember member)
    {
        members.add(member);
        nameTable.addObject(member);
    }

    public List< ? extends IfgenTestbenchMember> getMembers()
    {
        return new LinkedList<IfgenTestbenchMember>(members);
    }

    public Iterator< ? extends NamedObject> lookupObjects(
        String identifier,
        NameKind kind)
    {
        return nameTable.lookupObjects(identifier, kind);
    }

    public void accept(IfgenSchemaMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    public IfgenPackage getPackage()
    {
        return pkg;
    }

    public void setPackage(IfgenPackage pkg)
    {
        this.pkg = pkg;
    }

    public void accept(IfgenPackageMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    public boolean hasParameters()
    {
        return parameters.size() > 0;
    }
}
