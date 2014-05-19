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

import com.newisys.langschema.Name;
import com.newisys.langschema.Namespace;

/**
 * Represents the name of an Ifgen schema object.
 * 
 * @author Trevor Robinson
 */
public final class IfgenName
    implements Name
{
    private static final long serialVersionUID = 3689910656577122869L;

    private final String identifier;
    private final IfgenNameKind kind;
    private Namespace namespace;

    public IfgenName(String identifier, IfgenNameKind kind, Namespace namespace)
    {
        this.identifier = identifier;
        this.kind = kind;
        this.namespace = namespace;
    }

    public IfgenName(String identifier, IfgenNameKind kind)
    {
        this.identifier = identifier;
        this.kind = kind;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public IfgenNameKind getKind()
    {
        return kind;
    }

    public Namespace getNamespace()
    {
        return namespace;
    }

    public void setNamespace(Namespace namespace)
    {
        this.namespace = namespace;
    }

    public String getCanonicalName()
    {
        return (namespace != null) ? namespace.getName().getCanonicalName()
            + "." + identifier : identifier;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof IfgenName)
        {
            IfgenName other = (IfgenName) obj;
            return identifier.equals(other.identifier) && kind == other.kind
                && namespace == other.namespace;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return identifier.hashCode() ^ kind.hashCode() ^ namespace.hashCode();
    }

    @Override
    public String toString()
    {
        return getCanonicalName();
    }
}
