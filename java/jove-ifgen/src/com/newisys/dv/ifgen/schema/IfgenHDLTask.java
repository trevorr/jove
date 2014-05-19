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
import java.util.Iterator;
import java.util.List;

import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.Namespace;
import com.newisys.langschema.SchemaObject;
import com.newisys.langschema.util.NameTable;

/**
 * Ifgen schema object for HDL tasks.
 * 
 * @author Trevor Robinson
 * @author Jon Nall
 */
public final class IfgenHDLTask
    extends IfgenTask
    implements IfgenTemplate, Namespace
{
    private static final long serialVersionUID = 3257290248785834801L;
    private static final List<SchemaObject> emptySchemaObjectList = Collections
        .emptyList();

    private IfgenModuleDef instancePath;
    private final List<IfgenVariableDecl> parameters;
    private final NameTable nameTable = new NameTable();

    public IfgenHDLTask(IfgenSchema schema, IfgenName name)
    {
        this(schema, name, emptyVarDeclList);
    }

    public IfgenHDLTask(
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
                throw new Error("In HDL task '" + this.getName()
                    + "', parameter: '" + p.getName()
                    + "' has the same name as a previous parameter");
            }

            nameTable.addObject(p);
        }
    }

    public IfgenModuleDef getInstancePath()
    {
        return instancePath;
    }

    public void setInstancePath(IfgenModuleDef instancePath)
    {
        this.instancePath = instancePath;
    }

    public List<IfgenVariableDecl> getParameters()
    {
        return this.parameters;
    }

    @Override
    public void addArgument(IfgenTaskArg argument)
    {
        if (nameTable.lookupObjects(argument.getName().getIdentifier(),
            IfgenNameKind.EXPRESSION).hasNext())
        {
            throw new Error("In HDL task '" + this.getName() + "', argument: '"
                + argument.getName()
                + "' has the same name as a previous parameter or argument");
        }
        super.addArgument(argument);
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

    public List< ? extends SchemaObject> getMembers()
    {
        return emptySchemaObjectList;
    }
}
