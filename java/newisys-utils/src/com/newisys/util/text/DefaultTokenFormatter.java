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

package com.newisys.util.text;

import java.io.IOException;

import com.newisys.io.IndentWriter;

/**
 * TokenFormatter implementation that writes to an underlying IndentWriter.
 * 
 * @author Trevor Robinson
 */
public final class DefaultTokenFormatter
    implements TokenFormatter
{
    private final static int INITIAL_LENGTH = 64;

    final static int KIND_NORMAL = 0;
    final static int KIND_LEADING = 1;
    final static int KIND_TRAILING = 2;
    final static int KIND_SPACE = 3;

    private final IndentWriter writer;
    private final int margin;

    private String[] tokens;
    private int[] kinds;
    private int[] groups;
    private int tokenCount = 0;
    private int curGroup = 0;
    private int initIndent;

    public DefaultTokenFormatter(IndentWriter writer, int margin)
    {
        this.writer = writer;
        this.margin = margin;
        reallocBuffers(INITIAL_LENGTH);
    }

    private void reallocBuffers(int length)
    {
        String[] newTokens = new String[length];
        int[] newKinds = new int[length];
        int[] newGroups = new int[length];
        if (tokens != null)
        {
            System.arraycopy(tokens, 0, newTokens, 0, tokens.length);
            System.arraycopy(kinds, 0, newKinds, 0, kinds.length);
            System.arraycopy(groups, 0, newGroups, 0, groups.length);
        }
        tokens = newTokens;
        kinds = newKinds;
        groups = newGroups;
    }

    private void checkBuffers(int neededLength)
    {
        int curLength = tokens.length;
        if (curLength < neededLength)
        {
            while (curLength < neededLength)
                curLength *= 2;
            reallocBuffers(curLength);
        }
    }

    public void incIndent()
    {
        writer.incIndent();
    }

    public void decIndent()
    {
        writer.decIndent();
    }

    public int getIndent()
    {
        return writer.getIndentCount();
    }

    public void beginGroup()
    {
        ++curGroup;
    }

    public void endGroup()
    {
        --curGroup;
        assert (curGroup >= 0);
    }

    public int getGroup()
    {
        return curGroup;
    }

    void printToken(String s, int kind)
    {
        assert (s != null);
        if (tokenCount == 0)
        {
            initIndent = writer.getIndentCount();
        }
        checkBuffers(tokenCount + 1);
        tokens[tokenCount] = s;
        kinds[tokenCount] = kind;
        groups[tokenCount] = curGroup;
        ++tokenCount;
    }

    public void printToken(String s)
    {
        printToken(s, KIND_NORMAL);
    }

    public void printLeadingToken(String s)
    {
        printToken(s, KIND_LEADING);
    }

    public void printTrailingToken(String s)
    {
        printToken(s, KIND_TRAILING);
    }

    public void printSpace()
    {
        // suppress leading or adjacent spaces
        if (tokenCount > 0 && kinds[tokenCount - 1] != KIND_SPACE)
        {
            printToken(" ", KIND_SPACE);
        }
    }

    public boolean isNewLine()
    {
        return tokenCount == 0;
    }

    public void newLine()
        throws IOException
    {
        // suppress trailing spaces
        while (tokenCount > 0 && kinds[tokenCount - 1] == KIND_SPACE)
        {
            --tokenCount;
        }

        // optimize blank lines
        if (tokenCount == 0)
        {
            writer.newLine();
            return;
        }

        int start = 0;
        int end = tokenCount;
        int startGroup = 0;
        int indentedGroup = -1;
        int saveIndent = writer.getIndentCount();
        int indent = initIndent;
        boolean indented = false;
        LineInfo queue = null;

        while (true)
        {
            // get starting column based on indent level
            int colsPerChar = writer.getIndentChar() == '\t' ? 8 : 1;
            int col = indent * colsPerChar * writer.getIndentMultiple();

            // always write first normal token and its trailing tokens
            boolean gotNormal = false;
            int pos = start;
            while (pos < end)
            {
                int kind = kinds[pos];
                if (gotNormal && kind != KIND_TRAILING) break;
                if (kind == KIND_NORMAL) gotNormal = true;

                String token = tokens[pos];
                col += token.length();
                ++pos;
            }
            int writeLast = pos - 1;

            // find next normal token beyond margin
            boolean lastWasNormalOrTrailing = false;
            int wrapGroup = 0;
            while (pos < end)
            {
                String token = tokens[pos];
                col += token.length();
                int kind = kinds[pos];
                if (kind == KIND_NORMAL)
                {
                    if (col > margin)
                    {
                        // assign group of wrapped tokens as minimum group of
                        // leading boundaries
                        wrapGroup = groups[pos];
                        while (--pos >= writeLast)
                        {
                            int group = groups[pos];
                            if (group < wrapGroup) wrapGroup = group;
                        }
                        break;
                    }
                    writeLast = pos;
                    lastWasNormalOrTrailing = true;
                }
                else if (kind == KIND_TRAILING && lastWasNormalOrTrailing)
                {
                    writeLast = pos;
                }
                else
                {
                    lastWasNormalOrTrailing = false;
                }
                ++pos;
            }
            if (pos == end) writeLast = pos - 1;

            // write accepted tokens
            writer.setIndentCount(indent);
            //writer.write(String.valueOf(startGroup) + ": ");
            while (start <= writeLast)
            {
                String token = tokens[start++];
                writer.write(token);
            }
            writer.newLine();

            // split at entry into lower groups
            for (int group = startGroup; group < wrapGroup; ++group)
            {
                int prevGroup = wrapGroup;
                for (pos = start; pos < end; ++pos)
                {
                    int nextGroup = groups[pos];
                    if (nextGroup > prevGroup && prevGroup == group)
                    {
                        // back up to first leading token
                        while (pos > start && leadingOrSpace(kinds[pos - 1]))
                            --pos;
                        while (pos < end && kinds[pos] == KIND_SPACE)
                            ++pos;
                        if (!indented)
                            ++indent;
                        else
                            indented = false;
                        queue = new LineInfo(pos, end, group, indent, true,
                            queue);
                        end = pos;
                        break;
                    }
                    prevGroup = nextGroup;
                }
            }

            // split remainder (after skipping any leading spaces)
            pos = start;
            while (pos < end && kinds[pos] == KIND_SPACE)
                ++pos;
            if (pos < end)
            {
                if (indentedGroup < wrapGroup && !indented)
                {
                    ++indent;
                    indented = true;
                }
                queue = new LineInfo(pos, end, wrapGroup, indent, indented,
                    queue);
            }
            indentedGroup = wrapGroup;

            if (queue == null) break;

            start = queue.start;
            end = queue.end;
            startGroup = queue.group;
            indent = queue.indent;
            indented = queue.indented;
            queue = queue.next;
        }

        writer.setIndentCount(saveIndent);
        tokenCount = 0;
    }

    private static boolean leadingOrSpace(int kind)
    {
        return kind == KIND_LEADING || kind == KIND_SPACE;
    }

    private static class LineInfo
    {
        final int start, end, group, indent;
        final boolean indented;
        final LineInfo next;

        public LineInfo(
            int start,
            int end,
            int group,
            int indent,
            boolean indented,
            LineInfo next)
        {
            this.start = start;
            this.end = end;
            this.group = group;
            this.indent = indent;
            this.indented = indented;
            this.next = next;
        }
    }

    public void flush()
        throws IOException
    {
        if (tokenCount > 0) newLine();
        writer.flush();
    }

    public void close()
        throws IOException
    {
        if (tokenCount > 0) newLine();
        writer.close();
    }
}
