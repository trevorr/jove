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
 * Ifgen schema object for drive definitions.
 * 
 * @author Trevor Robinson
 * @author Jon Nall
 */
public final class IfgenDriveDef
    extends IfgenSchemaObject
    implements IfgenEdgeDef, IfgenInterfaceMember
{
    private static final long serialVersionUID = 3689070625498872114L;

    private IfgenEdge edge;
    private int skew;

    public IfgenDriveDef(IfgenSchema schema, IfgenEdge edge, int skew)
    {
        super(schema);
        this.edge = edge;
        this.skew = skew;
    }

    public IfgenEdge getEdge()
    {
        return edge;
    }

    public void setEdge(IfgenEdge edge)
    {
        this.edge = edge;
    }

    public int getSkew()
    {
        return skew;
    }

    public void setSkew(int skew)
    {
        this.skew = skew;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof IfgenDriveDef)
        {
            IfgenDriveDef other = (IfgenDriveDef) obj;
            return edge == other.edge && skew == other.skew;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return edge.hashCode() ^ skew;
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
