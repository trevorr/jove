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
import com.newisys.util.text.TextUtil;

/**
 * Specifies an enumeration argument to a command line option.
 * 
 * @author Trevor Robinson
 */
public class EnumArgDef
    extends StringArgDef
{
    private final List<String> values;
    private final List<String> valueDescriptions;

    public EnumArgDef(String name)
    {
        this(name, null, 1, 1);
    }

    public EnumArgDef(String name, String description)
    {
        this(name, description, 1, 1);
    }

    public EnumArgDef(
        String name,
        String description,
        int minOccurs,
        int maxOccurs)
    {
        super(name, description, minOccurs, maxOccurs);
        values = new LinkedList<String>();
        valueDescriptions = new LinkedList<String>();
    }

    public void addValue(String value, String description)
    {
        values.add(value);
        valueDescriptions.add(description);
    }

    public List<String> getValues()
    {
        return values;
    }

    public List<String> getValueDescriptions()
    {
        return valueDescriptions;
    }

    public boolean contains(String value)
    {
        return values.contains(value);
    }

    public void validateValue(String value)
        throws ValidationException
    {
        if (!contains(value))
        {
            throw new ValidationException("Value for " + getName()
                + " must be one of: " + values);
        }
    }

    protected boolean hasDumpContent()
    {
        return super.hasDumpContent() || !values.isEmpty();
    }

    public void dumpArgs(IndentPrintWriter ipw)
    {
        super.dumpArgs(ipw);
        ipw.incIndent();
        final Iterator<String> valueIter = values.iterator();
        final Iterator<String> descIter = valueDescriptions.iterator();
        while (valueIter.hasNext())
        {
            String value = valueIter.next();
            String description = descIter.next();
            ipw.print(TextUtil.padTrailing(value, DESCRIPTION_COLUMN
                - ipw.getIndentTotal()));
            if (description != null)
            {
                ipw.print(description);
            }
            if (value.equals(defValue))
            {
                ipw.print(" (default)");
            }
            ipw.println();
        }
        ipw.decIndent();
    }
}
