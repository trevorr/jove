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

package com.newisys.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * BufferedWriter class that supports automatic line indention.
 * 
 * @author Trevor Robinson
 */
public class IndentWriter
    extends BufferedWriter
{
    private static final int DEFAULT_INDENT_BUFFER_SIZE = 40;

    /**
     * Character to use for indention.
     */
    private char indentChar;

    /**
     * Number of indention characters to write for each indention level.
     */
    private int indentMultiple;

    /**
     * Current indention level.
     */
    private int indentCount;

    /**
     * Total number of indention characters (indentCount * indentMultiple).
     */
    private int indentLength;

    /**
     * Array of indention characters to write.
     */
    private char[] indentBuffer;

    /**
     * Platform line separator string.
     */
    private char[] lineSeparator;

    /**
     * Current position in matching a line separator write.
     */
    private int lineSepPos;

    /**
     * Indicates that the next write is taking place on a new line.
     */
    private boolean newLine = true;

    public IndentWriter(Writer out)
    {
        super(out);
        init(' ', 4);
    }

    public IndentWriter(Writer out, int sz)
    {
        super(out, sz);
        init(' ', 4);
    }

    public IndentWriter(Writer out, char indentChar, int indentMultiple)
    {
        super(out);
        init(indentChar, indentMultiple);
    }

    public IndentWriter(Writer out, int sz, char indentChar, int indentMultiple)
    {
        super(out, sz);
        init(indentChar, indentMultiple);
    }

    private void init(char indentChar, int indentMultiple)
    {
        this.indentChar = indentChar;
        this.indentMultiple = indentMultiple;
        this.lineSeparator = System.getProperty("line.separator").toCharArray();
    }

    public char getIndentChar()
    {
        return indentChar;
    }

    public void setIndentChar(char indentChar)
    {
        if (indentChar != this.indentChar)
        {
            this.indentChar = indentChar;
            fillIndentBuffer();
        }
    }

    public int getIndentMultiple()
    {
        return indentMultiple;
    }

    public void setIndentMultiple(int indentMultiple)
    {
        if (indentMultiple != this.indentMultiple)
        {
            this.indentMultiple = indentMultiple;
            updateIndentLength();
        }
    }

    public int getIndentCount()
    {
        return indentCount;
    }

    public void setIndentCount(int indentCount)
    {
        if (indentCount != this.indentCount)
        {
            this.indentCount = indentCount;
            updateIndentLength();
        }
    }

    private void updateIndentLength()
    {
        indentLength = indentCount * indentMultiple;
        if (indentBuffer == null || indentBuffer.length < indentLength)
        {
            int bufLen = Math.max(indentLength * 2, DEFAULT_INDENT_BUFFER_SIZE);
            indentBuffer = new char[bufLen];
            fillIndentBuffer();
        }
    }

    private void fillIndentBuffer()
    {
        int bufLen = indentBuffer.length;
        for (int i = 0; i < bufLen; ++i)
        {
            indentBuffer[i] = indentChar;
        }
    }

    public void incIndent()
    {
        setIndentCount(indentCount + 1);
    }

    public void decIndent()
    {
        if (indentCount > 0)
        {
            setIndentCount(indentCount - 1);
        }
    }

    private void printIndent()
        throws IOException
    {
        if (indentLength > 0)
        {
            super.write(indentBuffer, 0, indentLength);
        }
    }

    private void checkIndent()
        throws IOException
    {
        if (newLine)
        {
            printIndent();
            newLine = false;
        }
    }

    private static final int NO = 0;
    private static final int PARTIAL = 1;
    private static final int COMPLETE = 2;

    private int isLineSeparator(char c)
    {
        final int result;

        // does given char match next char in line separator?
        if (c == lineSeparator[lineSepPos])
        {
            // have we seen the whole line separator?
            if (++lineSepPos == lineSeparator.length)
            {
                result = COMPLETE;

                // reset matching position
                lineSepPos = 0;
            }
            else
            {
                result = PARTIAL;
            }
        }
        else
        {
            result = NO;
        }

        return result;
    }

    private void flushPartialLineSeparator()
        throws IOException
    {
        if (lineSepPos > 0)
        {
            // write out line separator characters matched so far
            super.write(lineSeparator, 0, lineSepPos);

            // reset matching position
            lineSepPos = 0;
        }
    }

    public void write(int c)
        throws IOException
    {
        switch (isLineSeparator((char) c))
        {
        case NO:
            checkIndent();
            flushPartialLineSeparator();
            super.write(c);
            break;
        case PARTIAL:
            // do nothing
            break;
        case COMPLETE:
            super.newLine();
            newLine = true;
            break;
        default:
            assert false;
        }
    }

    public void write(char[] cbuf, int off, int len)
        throws IOException
    {
        write(cbuf, null, off, len);
    }

    public void write(String s, int off, int len)
        throws IOException
    {
        write(null, s, off, len);
    }

    private void write(char[] cbuf, String s, int off, int len)
        throws IOException
    {
        while (len > 0)
        {
            // search for embedded line separator
            int pos = off;
            int end = off + len;
            int writeLen = len;
            boolean doWrite = false;
            boolean lastWasNonSep = false;
            searchLoop: while (pos < end)
            {
                // get next character
                char c = (cbuf != null) ? cbuf[pos] : s.charAt(pos);
                ++pos;

                // is next char in buffer part of a line separator?
                switch (isLineSeparator((char) c))
                {
                case NO:
                    if (!lastWasNonSep)
                    {
                        checkIndent();
                        flushPartialLineSeparator();
                        doWrite = true;
                        lastWasNonSep = true;
                    }
                    break;
                case PARTIAL:
                    lastWasNonSep = false;
                    break;
                case COMPLETE:
                    writeLen = pos - off;
                    doWrite = true;
                    newLine = true;
                    break searchLoop;
                default:
                    assert false;
                }
            }

            // write up through line separator (or whole region if none)
            if (doWrite)
            {
                if (cbuf != null)
                {
                    super.write(cbuf, off, writeLen);
                }
                else
                {
                    super.write(s, off, writeLen);
                }
            }

            // update off/len for next iteration
            off = pos;
            len -= writeLen;
        }
    }

    public void newLine()
        throws IOException
    {
        flushPartialLineSeparator();
        super.newLine();
        newLine = true;
    }
}
