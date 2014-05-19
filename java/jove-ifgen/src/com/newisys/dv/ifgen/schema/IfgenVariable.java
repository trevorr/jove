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
 * Ifgen schema object for variables.
 * 
 * @author Jon Nall
 */
public class IfgenVariable
{
    private IfgenType type;
    private final IfgenName name;

    public IfgenVariable(IfgenName name, IfgenType type)
    {
        this.name = name;
        this.type = type;
    }

    public IfgenName getName()
    {
        return name;
    }

    public IfgenType getType()
    {
        return type;
    }

    public void setType(IfgenType type)
    {
        this.type = type;
    }
}
