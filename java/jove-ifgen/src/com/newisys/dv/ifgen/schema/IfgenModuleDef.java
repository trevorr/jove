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
 * Ifgen schema object for default module declarations.
 * 
 * @author Trevor Robinson
 * @author Jon Nall
 */
public final class IfgenModuleDef
    extends IfgenSchemaObject
    implements IfgenInterfaceMember
{
    private static final long serialVersionUID = 3256443616292582711L;

    private final IfgenUnresolvedName unresolvedName;
    private final boolean containsVarRef;
    private final IfgenComplexVariableRef complexRef;

    public IfgenModuleDef(
        IfgenSchema schema,
        IfgenUnresolvedName moduleName,
        boolean containsVarRef)
    {
        super(schema);
        this.unresolvedName = moduleName;
        this.containsVarRef = containsVarRef;

        if (this.containsVarRef)
        {
            complexRef = new IfgenComplexVariableRef(schema,
                this.unresolvedName);
        }
        else
        {
            complexRef = null;
        }
    }

    public boolean containsVarRef()
    {
        return this.containsVarRef;
    }

    public IfgenUnresolvedName getName()
    {
        return this.unresolvedName;
    }

    public IfgenComplexVariableRef getComplexRef()
    {
        return this.complexRef;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof IfgenModuleDef)
        {
            IfgenModuleDef other = (IfgenModuleDef) obj;
            return unresolvedName.equals(other.unresolvedName)
                && this.containsVarRef == other.containsVarRef;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return unresolvedName.hashCode() | (containsVarRef ? 1 : 0);
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
