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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import com.newisys.util.text.TextUtil;

/**
 * PrintWriter class that supports automatic line indention.
 * 
 * @author Trevor Robinson
 */
public class IndentPrintWriter
    extends PrintWriter
{
    private boolean newLine;
    private char indentChar;
    private int indentMultiple;
    private int indentCount;
    private String indentString;

    /**
     * Create a new IndentPrintWriter, with automatic line flushing.
     *
     * @param out A character-output stream
     */
    public IndentPrintWriter(Writer out)
    {
        this(out, true);
    }

    /**
     * Create a new IndentPrintWriter.
     *
     * @param out A character-output stream
     * @param autoFlush A boolean; if true, the println() methods will flush the
     *            output buffer
     */
    public IndentPrintWriter(Writer out, boolean autoFlush)
    {
        super(out, autoFlush);

        newLine = true;
        indentChar = ' ';
        indentMultiple = 2;
        indentCount = 0;
    }

    /**
     * Create a new IndentPrintWriter, with automatic line flushing, from an
     * existing OutputStream. This convenience constructor creates the necessary
     * intermediate OutputStreamWriter, which will convert characters into bytes
     * using the default character encoding.
     *
     * @param out An output stream
     *
     * @see java.io.OutputStreamWriter#OutputStreamWriter(java.io.OutputStream)
     */
    public IndentPrintWriter(OutputStream out)
    {
        this(out, true);
    }

    /**
     * Create a new IndentPrintWriter from an existing OutputStream. This
     * convenience constructor creates the necessary intermediate
     * OutputStreamWriter, which will convert characters into bytes using the
     * default character encoding.
     *
     * @param out An output stream
     * @param autoFlush A boolean; if true, the println() methods will flush the
     *            output buffer
     *
     * @see java.io.OutputStreamWriter#OutputStreamWriter(java.io.OutputStream)
     */
    public IndentPrintWriter(OutputStream out, boolean autoFlush)
    {
        this(new BufferedWriter(new OutputStreamWriter(out)), autoFlush);
    }

    /**
     * Write a single character.
     */
    private void checkIndent()
    {
        if (newLine)
        {
            newLine = false;
            if (indentString != null)
            {
                write(indentString);
            }
        }
    }

    /* (non-Javadoc)
     * @see java.io.Writer#write(int)
     */
    public void write(int c)
    {
        synchronized (lock)
        {
            checkIndent();
            super.write(c);
        }
    }

    /* (non-Javadoc)
     * @see java.io.Writer#write(char[], int, int)
     */
    public void write(char buf[], int off, int len)
    {
        synchronized (lock)
        {
            checkIndent();
            super.write(buf, off, len);
        }
    }

    /* (non-Javadoc)
     * @see java.io.Writer#write(java.lang.String, int, int)
     */
    public void write(String s, int off, int len)
    {
        synchronized (lock)
        {
            checkIndent();
            super.write(s, off, len);
        }
    }

    /**
     * Finish the line.
     */
    protected void newLine()
    {
        synchronized (lock)
        {
            super.println();
            newLine = true;
        }
    }

    /**
     * Generate indent string.
     */
    private void genIndent()
    {
        indentString = TextUtil.replicate(indentChar, indentCount
            * indentMultiple);
    }

    /**
     * Get the character used for indention.
     *
     * @return the character used for indention
     */
    public char getIndentChar()
    {
        return indentChar;
    }

    /**
     * Set the character used for indention.
     *
     * @param c the character used for indention
     */
    public void setIndentChar(char c)
    {
        if (indentChar != c)
        {
            indentChar = c;
            genIndent();
        }
    }

    /**
     * Get the indention multiple.
     *
     * @return the indention multiple
     */
    public int getIndentMultiple()
    {
        return indentMultiple;
    }

    /**
     * Set the indention multiple.
     *
     * @param n the indention multiple
     */
    public void setIndentMultiple(int n)
    {
        if (n < 0)
        {
            throw new IllegalArgumentException("Indent multiple must be >= 0");
        }

        if (indentMultiple != n)
        {
            indentMultiple = n;
            genIndent();
        }
    }

    /**
     * Get the indention count.
     *
     * @return the indention count
     */
    public int getIndentCount()
    {
        return indentCount;
    }

    /**
     * Set the indention count.
     *
     * @param n the indention count
     */
    public void setIndentCount(int n)
    {
        if (n < 0)
        {
            throw new IllegalArgumentException("Indent count must be >= 0");
        }

        if (indentCount != n)
        {
            indentCount = n;
            genIndent();
        }
    }

    /**
     * Returns the total number of indention characters, which is the indention
     * count multiplied by the indention multiple.
     *
     * @return the total number of indention characters
     */
    public int getIndentTotal()
    {
        return indentCount * indentMultiple;
    }

    /**
     * Increment the current indention count by 1.
     */
    public void incIndent()
    {
        setIndentCount(indentCount + 1);
    }

    /**
     * Decrement the current indention count by 1.
     */
    public void decIndent()
    {
        setIndentCount(indentCount - 1);
    }

    /**
     * Increment the current indention count by n.
     *
     * @param n the amount to increment the indention count
     */
    public void incIndent(int n)
    {
        setIndentCount(indentCount + n);
    }

    /**
     * Decrement the current indention count by n.
     *
     * @param n the amount to decrement the indention count
     */
    public void decIndent(int n)
    {
        setIndentCount(indentCount - n);
    }

    /*
     * (non-Javadoc)
     * @see java.io.PrintWriter#println()
     */
    public void println()
    {
        synchronized (lock)
        {
            newLine();
        }
    }

    /*
     * (non-Javadoc)
     * @see java.io.PrintWriter#println(boolean)
     */
    public void println(boolean x)
    {
        synchronized (lock)
        {
            print(x);
            newLine();
        }
    }

    /*
     * (non-Javadoc)
     * @see java.io.PrintWriter#println(char)
     */
    public void println(char x)
    {
        synchronized (lock)
        {
            print(x);
            newLine();
        }
    }

    /*
     * (non-Javadoc)
     * @see java.io.PrintWriter#println(int)
     */
    public void println(int x)
    {
        synchronized (lock)
        {
            print(x);
            newLine();
        }
    }

    /*
     * (non-Javadoc)
     * @see java.io.PrintWriter#println(long)
     */
    public void println(long x)
    {
        synchronized (lock)
        {
            print(x);
            newLine();
        }
    }

    /*
     * (non-Javadoc)
     * @see java.io.PrintWriter#println(float)
     */
    public void println(float x)
    {
        synchronized (lock)
        {
            print(x);
            newLine();
        }
    }

    /*
     * (non-Javadoc)
     * @see java.io.PrintWriter#println(double)
     */
    public void println(double x)
    {
        synchronized (lock)
        {
            print(x);
            newLine();
        }
    }

    /*
     * (non-Javadoc)
     * @see java.io.PrintWriter#println(char[])
     */
    public void println(char x[])
    {
        synchronized (lock)
        {
            print(x);
            newLine();
        }
    }

    /*
     * (non-Javadoc)
     * @see java.io.PrintWriter#println(java.lang.String)
     */
    public void println(String x)
    {
        synchronized (lock)
        {
            print(x);
            newLine();
        }
    }

    /*
     * (non-Javadoc)
     * @see java.io.PrintWriter#println(java.lang.Object)
     */
    public void println(Object x)
    {
        synchronized (lock)
        {
            print(x);
            newLine();
        }
    }
}
