/*
 * LangSchema - Generic Programming Language Modeling Interfaces
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

package com.newisys.langschema;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple base implementation of a comment annotation that represents the
 * comment as a List of Strings.
 * 
 * @author Trevor Robinson
 */
public abstract class CommentImpl
    implements Comment
{
    private static final String LINE_SEP = System.getProperty("line.separator");

    private final List<String> lines = new LinkedList<String>();
    private int textLength;
    private boolean leading;

    public CommentImpl()
    {
        this(true);
    }

    public CommentImpl(boolean leading)
    {
        this.leading = leading;
    }

    public CommentImpl(String line)
    {
        this(line, true);
    }

    public CommentImpl(String line, boolean leading)
    {
        this.leading = leading;
        addLine(line);
    }

    public boolean isLeading()
    {
        return leading;
    }

    public void setLeading(boolean leading)
    {
        this.leading = leading;
    }

    public String getText()
    {
        StringBuffer buf = new StringBuffer(textLength);
        Iterator<String> iter = lines.iterator();
        while (iter.hasNext())
        {
            String line = iter.next();
            buf.append(line);
            if (iter.hasNext()) buf.append(LINE_SEP);
        }
        return buf.toString();
    }

    public List<String> getLines()
    {
        return lines;
    }

    public void addLine(String line)
    {
        if (!lines.isEmpty()) textLength += LINE_SEP.length();
        lines.add(line);
        textLength += line.length();
    }

    public String toString()
    {
        return lines.toString();
    }
}
