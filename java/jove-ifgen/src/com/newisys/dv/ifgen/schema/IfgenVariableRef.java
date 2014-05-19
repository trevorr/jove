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
 * Ifgen schema object for variable references.
 * 
 * @author Jon Nall
 */
public final class IfgenVariableRef
    extends IfgenExpression
{
    private static final long serialVersionUID = 1318387509893533941L;

    private IfgenVariableDecl decl;
    private IfgenUnresolvedName unresolvedName;

    public IfgenVariableRef(IfgenSchema schema, IfgenUnresolvedName name)
    {
        super(schema);
        this.unresolvedName = name;
    }

    public void setDecl(IfgenVariableDecl decl)
    {
        this.decl = decl;
    }

    public IfgenVariableDecl getDecl()
    {
        return this.decl;
    }

    public IfgenName getName()
    {
        assert (decl != null);
        return decl.getName();
    }

    public IfgenUnresolvedName getUnresolvedName()
    {
        assert (unresolvedName != null);
        return unresolvedName;
    }

    @Override
    public IfgenType getType()
    {
        return (decl == null) ? null : decl.getType();
    }

    @Override
    public void accept(IfgenSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public String toDebugString()
    {
        return (decl == null) ? unresolvedName.toString() : decl
            .toDebugString();
    }

}
