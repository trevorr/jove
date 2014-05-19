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

import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.NamedObject;

/**
 * Ifgen schema object for tasks.
 * 
 * @author Trevor Robinson
 */
public abstract class IfgenTask
    extends IfgenNamedTestbenchMember
    implements NamedObject, IfgenSchemaMember, IfgenPackageMember
{
    private final List<IfgenTaskArg> arguments = new LinkedList<IfgenTaskArg>();
    private IfgenPackage pkg;

    public IfgenTask(IfgenSchema schema, IfgenName name)
    {
        super(schema, name);
    }

    public List<IfgenTaskArg> getArguments()
    {
        return arguments;
    }

    public void addArgument(IfgenTaskArg argument)
    {
        arguments.add(argument);
    }

    public IfgenPackage getPackage()
    {
        return pkg;
    }

    public void setPackage(IfgenPackage pkg)
    {
        this.pkg = pkg;
    }
}
