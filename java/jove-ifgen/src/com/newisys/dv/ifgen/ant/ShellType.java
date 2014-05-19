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

package com.newisys.dv.ifgen.ant;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.types.DataType;

/**
 * DataType for nested shell elements.
 * 
 * @author Jon Nall
 */
public class ShellType
    extends DataType
{
    private String testbench;
    private String shellname;
    private List<ArgType> parameters = new LinkedList<ArgType>();

    public void setTestbench(String testbench)
    {
        this.testbench = testbench;
    }

    public void setShellname(String shellname)
    {
        this.shellname = shellname;
    }

    public void addConfiguredArg(ArgType parameter)
    {
        parameters.add(parameter);
    }

    public String getTestbench()
    {
        return this.testbench;
    }

    public String getShellname()
    {
        return this.shellname;
    }

    public List< ? extends ArgType> getParameters()
    {
        return parameters;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder(64);
        buf.append(testbench);
        buf.append(" <");
        Iterator<ArgType> iter = parameters.iterator();
        while (iter.hasNext())
        {
            final ArgType def = iter.next();
            buf.append(def.getName());
            buf.append(" = ");
            buf.append(def.getValue());
            if (iter.hasNext())
            {
                buf.append(", ");
            }
        }

        buf.append("> ");
        buf.append(shellname);
        return buf.toString();
    }
}
