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
 * Represents an unresolved name that can possible end with a wildcard.
 * Used for import directives.
 * 
 * @author Trevor Robinson
 */
public class IfgenWildname
{
    private IfgenUnresolvedName packageOrTypeName;
    private boolean importMembers;

    public IfgenWildname(IfgenUnresolvedName packageOrTypeName)
    {
        this.packageOrTypeName = packageOrTypeName;
    }

    public IfgenWildname(
        IfgenUnresolvedName packageOrTypeName,
        boolean importMembers)
    {
        this.packageOrTypeName = packageOrTypeName;
        this.importMembers = importMembers;
    }

    public IfgenUnresolvedName getPackageOrTypeName()
    {
        return packageOrTypeName;
    }

    public void setPackageOrTypeName(IfgenUnresolvedName packageOrTypeName)
    {
        this.packageOrTypeName = packageOrTypeName;
    }

    public boolean isImportMembers()
    {
        return importMembers;
    }

    public void setImportMembers(boolean importMembers)
    {
        this.importMembers = importMembers;
    }
}
