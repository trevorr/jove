/*
 * Newisys-Utils - Newisys Utility Classes
 * Copyright (C) 2005 Newisys, Inc. or its licensors, as applicable.
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

package com.newisys.util.cmdline;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.newisys.io.IndentPrintWriter;

/**
 * Describes a command line option specification.
 * 
 * @author Trevor Robinson
 */
public class OptionArgDef
    extends AbstractArgDef
{
    private final List<StringArgDef> argDefs;

    public OptionArgDef(String name)
    {
        this(name, null, 0, UNBOUNDED);
    }

    public OptionArgDef(String name, String description)
    {
        this(name, description, 0, UNBOUNDED);
    }

    public OptionArgDef(
        String name,
        String description,
        int minOccurs,
        int maxOccurs)
    {
        super(name, description, minOccurs, maxOccurs);
        argDefs = new LinkedList<StringArgDef>();
    }

    public void addArgDef(StringArgDef argDef)
    {
        argDefs.add(argDef);
    }

    public List<StringArgDef> getArgDefs()
    {
        return argDefs;
    }

    public String getSyntaxString()
    {
        StringBuffer buf = new StringBuffer();

        if (minOccurs == 0)
        {
            buf.append('[');
        }

        buf.append('-');
        buf.append(name);

        Iterator iter = argDefs.iterator();
        while (iter.hasNext())
        {
            buf.append(' ');
            StringArgDef argDef = (StringArgDef) iter.next();
            buf.append(argDef.getSyntaxString());
        }

        if (minOccurs == 0)
        {
            buf.append(']');
        }

        return buf.toString();
    }

    protected boolean hasDumpContent()
    {
        return super.hasDumpContent() || !argDefs.isEmpty();
    }

    public void dumpArgs(IndentPrintWriter ipw)
    {
        super.dumpArgs(ipw);
        ipw.incIndent();
        for (StringArgDef argDef : argDefs)
        {
            argDef.dumpArgs(ipw);
        }
        ipw.decIndent();
    }

    public String toString()
    {
        return "-" + name;
    }
}
