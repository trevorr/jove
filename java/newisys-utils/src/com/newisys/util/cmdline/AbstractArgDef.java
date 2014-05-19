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

import com.newisys.io.IndentPrintWriter;
import com.newisys.util.text.TextUtil;

/**
 * Describes a command line option argument specification.
 * 
 * @author Trevor Robinson
 */
public abstract class AbstractArgDef
{
    public static final int UNBOUNDED = -1;

    protected static final int DESCRIPTION_COLUMN = 30;

    protected final String name;
    protected final String description;
    protected final int minOccurs;
    protected final int maxOccurs;

    public AbstractArgDef(
        String name,
        String description,
        int minOccurs,
        int maxOccurs)
    {
        if (minOccurs < 0)
        {
            throw new IllegalArgumentException("minOccurs must be >= 0");
        }
        if ((maxOccurs < minOccurs || maxOccurs <= 0) && maxOccurs != UNBOUNDED)
        {
            throw new IllegalArgumentException(
                "maxOccurs must be >= minOccurs and > 0, or UNBOUNDED");
        }

        this.name = name;
        this.description = description;
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
    }

    public final String getName()
    {
        return name;
    }

    public final String getDescription()
    {
        return description;
    }

    public final int getMinOccurs()
    {
        return minOccurs;
    }

    public final int getMaxOccurs()
    {
        return maxOccurs;
    }

    public final boolean isOptional()
    {
        return minOccurs == 0;
    }

    public final boolean isUnbounded()
    {
        return maxOccurs == UNBOUNDED;
    }

    public String getSyntaxString()
    {
        StringBuffer buf = new StringBuffer();
        if (isOptional())
        {
            buf.append('[');
            buf.append(name);
            buf.append(']');
        }
        else
        {
            buf.append('<');
            buf.append(name);
            buf.append('>');
        }
        if (minOccurs > 1 || maxOccurs > 1)
        {
            buf.append('{');
            buf.append(minOccurs);
            buf.append(',');
            buf.append(maxOccurs);
            buf.append('}');
        }
        else if (isUnbounded())
        {
            buf.append("...");
        }
        return buf.toString();
    }

    protected boolean hasDumpContent()
    {
        return description != null;
    }

    public void dumpArgs(IndentPrintWriter ipw)
    {
        if (hasDumpContent())
        {
            String syntax = getSyntaxString();
            ipw.print(syntax);

            if (description != null)
            {
                String[] descLines = description.split("\n");

                int syntaxLen = syntax.length();
                int startCol = ipw.getIndentTotal();
                if (startCol + syntaxLen >= DESCRIPTION_COLUMN)
                {
                    ipw.println();
                }
                else
                {
                    startCol += syntaxLen;
                }

                for (int i = 0; i < descLines.length; ++i)
                {
                    if (i > 0)
                    {
                        ipw.println();
                        startCol = ipw.getIndentTotal();
                    }
                    ipw.print(TextUtil.replicate(' ', DESCRIPTION_COLUMN
                        - startCol));
                    ipw.print(descLines[i]);
                }
            }

            ipw.println();
        }
    }

    public String toString()
    {
        return name;
    }
}
