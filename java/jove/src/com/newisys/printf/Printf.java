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

package com.newisys.printf;

import java.io.OutputStream;

/**
 * Provides static access to the base printf implementation.
 * 
 * @author Jon Nall
 */
public class Printf
{
    private static final PrintfFormatter formatter = new PrintfFormatter();

    /**
     * Formats the specified arguments as described in the specified format
     * string and prints the result to {@link System#out}.<P>
     * This is equivalent to:<br>
     * <code>System.out.println(Printf.sprintf(fmt, args));</code>
     *
     * @param fmt the format to use for printing as described in {@link #sprintf}
     * @param args the arguments to be formatted
     */
    public static void printf(CharSequence fmt, Object... args)
    {
        formatter.printf(fmt, args);
    }

    /**
     * Formats the specified arguments as described in the specified format
     * string and prints the result to the specified OutputStream.<P>
     *
     * @param stream the OutputStream to which the formatted String will be printed
     * @param fmt the format to use for printing as described in {@link #sprintf}
     * @param args the arguments to be formatted
     */
    public static void fprintf(
        OutputStream stream,
        CharSequence fmt,
        Object... args)
    {
        formatter.sprintf(fmt, args);
    }

    /**
     * Formats the specified arguments as described in the specified format and
     * returns the result as a String.
     * <P>
     * A format specifier is of the form: %[flags][width][.[precision]]&lt;conversion specicifer&gt;
     * <br>
     * Where flags is one of the following:<br>
     * <ul>
     * <li><code>#</code> This uses an "alternate format" when formatting the
     * argument. For octal conversions, a "0" is prepended to the
     * value if the leading character is not already "0". For
     * hexadecimal conversions, the string "0x" is prepended to the value (or
     * "0X" if "X" is the conversion specifier). The <code>#</code> flag does
     * not affect any other conversion specifiers.
     * <li><code>0</code> For numeric conversions, this flag causes values to
     * be zero-padded rather than space-padded.
     * </ul>
     *
     * @param fmt the format to use for printing as described above
     * @param args the arguments to be formatted
     * @return the formatted string
     */
    public static String sprintf(CharSequence fmt, Object... args)
    {
        return formatter.sprintf(fmt, args);
    }

    private Printf()
    {
        // no one can instantiate a Printf
    }
}
