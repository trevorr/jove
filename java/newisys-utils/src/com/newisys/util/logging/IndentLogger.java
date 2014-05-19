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

package com.newisys.util.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import com.newisys.io.IndentWriter;

/**
 * Logging implementation that supports automatic line indention.
 *
 * @author Trevor Robinson
 */
public final class IndentLogger
{
    private final IndentWriter indentWriter;
    private final PrintWriter printWriter;

    public IndentLogger(Writer out, char indentChar, int indentMultiple)
    {
        indentWriter = new IndentWriter(out, indentChar, indentMultiple);
        printWriter = new PrintWriter(indentWriter, true);
    }

    public IndentLogger(Writer out)
    {
        this(out, ' ', 2);
    }

    public IndentLogger(OutputStream out)
    {
        this(new OutputStreamWriter(out));
    }

    public IndentLogger(String fileName) throws IOException
    {
        this(new FileWriter(fileName));
    }

    public IndentLogger(File file) throws IOException
    {
        this(new FileWriter(file));
    }

    public char getIndentChar()
    {
        return indentWriter.getIndentChar();
    }

    public void setIndentChar(char indentChar)
    {
        indentWriter.setIndentChar(indentChar);
    }

    public int getIndentMultiple()
    {
        return indentWriter.getIndentMultiple();
    }

    public void setIndentMultiple(int indentMultiple)
    {
        indentWriter.setIndentMultiple(indentMultiple);
    }

    public int getIndentCount()
    {
        return indentWriter.getIndentCount();
    }

    public void setIndentCount(int indentCount)
    {
        indentWriter.setIndentCount(indentCount);
    }

    public void incIndent()
    {
        indentWriter.incIndent();
    }

    public void decIndent()
    {
        indentWriter.decIndent();
    }

    public boolean checkError()
    {
        return printWriter.checkError();
    }

    public void close()
        throws IOException
    {
        printWriter.close();
    }

    public void flush()
        throws IOException
    {
        printWriter.flush();
    }

    public void print(boolean b)
    {
        printWriter.print(b);
    }

    public void print(char c)
    {
        printWriter.print(c);
    }

    public void print(char[] s)
    {
        printWriter.print(s);
    }

    public void print(double d)
    {
        printWriter.print(d);
    }

    public void print(float f)
    {
        printWriter.print(f);
    }

    public void print(int i)
    {
        printWriter.print(i);
    }

    public void print(Object obj)
    {
        printWriter.print(obj);
    }

    public void print(String s)
    {
        printWriter.print(s);
    }

    public void print(long l)
    {
        printWriter.print(l);
    }

    public void println()
    {
        printWriter.println();
    }

    public void println(boolean x)
    {
        printWriter.println(x);
    }

    public void println(char x)
    {
        printWriter.println(x);
    }

    public void println(char[] x)
    {
        printWriter.println(x);
    }

    public void println(double x)
    {
        printWriter.println(x);
    }

    public void println(float x)
    {
        printWriter.println(x);
    }

    public void println(int x)
    {
        printWriter.println(x);
    }

    public void println(Object x)
    {
        printWriter.println(x);
    }

    public void println(String x)
    {
        printWriter.println(x);
    }

    public void println(long x)
    {
        printWriter.println(x);
    }
}
