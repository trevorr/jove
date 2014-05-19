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

import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;

/**
 * Base class for printf-like implementations. The class exposes methods that
 * can be overridden by derived classes to extend or modify the formatting
 * behavior.
 * <P>
 * The base implementation supports the following:<br>
 * <u><b>Flags</b></u><br>
 * '#': Alternate format<br>
 *      For hexadecimal conversions the string "0x" or "0X" is prefixed to the
 *      result, depending on whether uppercase characters are being forced. For
 *      octal conversions, the string "0" is prefixed to the result unless the
 *      initial chracter of the result is already a "0".<P>
 * '0': Zero padding<br>
 *      Zeros, rather than spaces, should be used to pad the formatted string.
 *      See "-".<P>
 * '-': Left justification<br>
 *      The formatted string is left justified. If both "-" and "0" are
 *      specified, "-" has precedence.<P>
 * '+': Force sign<br>
 *      For signed conversions, a "+" is prefixed to positive values. If both
 *      "+" and " " are specified, "+" has precedence.<P>
 * ' ': Space before positive values<br>
 *      For signed conversions, a " " is prefixed to positive values. See "+".
 *      <P>
 * '^': Force uppercase<br>
 *      All letters in the formatted string are printed as uppercase.<P>
 * '=': Force natural length<br>
 *      The value being formatted will be padded out to the natural width
 *      of its type. This is related to, but separate from, the width
 *      specification.<P>
 *
 * <u><b>Width</b></u>
 * This specifies the minimum length of the formatted string. If the formatted
 * string is naturally longer than this value, this value has no effect.
 * However if the formatted string is shorted than this value, it is padded with
 * zero or spaces as determined by the flags and conversion specifier.
 * <P>
 * <u><b>Precision</b></u>
 * Precision is not supported in the base implementation and will result in an
 * {@link InvalidFormatSpecException} being thrown.
 * <P>
 * <u><b>Conversion specifier</b><br></u>
 * %: This specifier does not consume an argument and prints "%".<P>
 * b: Takes a type supported for numeric conversions (see below) and prints the
 *      value as binary. X/Z values are printed in lowercase by default.
 * c: Takes any type and converts it to a string. Then the first chracter is
 *      printed. Width is respected, but only one character will be printed,
 *      so sprintf("%3c", "foo") will print "  f".<P>
 * d, i: Takes a type supported for numeric conversions (see below) and prints
 *      the value as a signed, decimal value. If the argument contains X/Z
 *      values, a "?" is printed.<P>
 * o: Takes a type supported for numeric conversions (see below) and prints the
 *      value as octal. If, a 3-bit "nibble" contains a combination of X, Z, and
 *      numeric values, that nibble is printed as "?". If such a nibble contains
 *      only X values or only Z values, an "X" or "Z" is printed as appropriate.
 *      If the top nibble of the value is not a full 3 bits, it is not
 *      zero-padded. For example ("%o", 2'bxx) prints "X", not "?" (which would
 *      be the case if it was zero-padded to 3'b0xx).<P>
 * s: Takes any type and prints it as a string. Width is respected and should
 *      padding be needed, spaces are always used.<P>
 * x,X: Takes a type supported for numeric conversions (see below) and prints
 *      the value as hexadecimal. X/Z values are printed in uppercase. If "X" is
 *      used, any letters printed (a-f) will be printed as uppercase characters.
 *      If the top nibble of the value is not a full 4 bits, it is not
 *      zero-padded (see %o description for an example of how this affects
 *      formatting).<P>
 *
 * <u><b>Numeric Conversions</b></u><br>
 * Types supported for numeric conversions include:<br>
 * Boolean: <code>true</code> is converted to
 *      {@link com.newisys.verilog.util.Bit#ONE} and <code>false</code> is
 *      converted to {@link com.newisys.verilog.util.Bit#ZERO}.<P>
 * Enum: the {@link Enum#ordinal} method is used to convert Enum objects to
 *      integers.<P>
 * CharSequence: CharSequence objects are converted to BitVectors where the most
 *      significant 8 bits of the BitVector holding the ASCII value of the first
 *      character in the sequence and so on with the least most significant 8
 *      bits of the BitVector holding the ASCII value of the last character in
 *      the sequence.<P>
 * Number: Numbers use their innate numeric values.<P>
 *
 *
 * <u><b>String Conversions</b></u><br>
 * Any type may be printed as a string. There are some special cases:<br>
 * Number: Numbers are zero-padded such that their length is a multiple of 8
 *      bits. The most significant 8 bits are then interpreted as an ASCII
 *      value and that character is printed, and so on. Should one of the bytes
 *      have a value of zero, that byte is ignored, and the next byte is
 *      processed. This implies that printing the value 0 as a string results
 *      in the empty string. Further, if the argument contains X/Z values, the
 *      empty string is printed.<P>
 * All other types: All other types are printed using their
 *      {@link Object#toString} methods.
 * <P>
 * <u><b>Notes for derived classes</b></u><br>
 * Derived classes must support format specifications with the following
 * format: <code>%[flags][width][.[precicion]]&lt;conversionSpecifier&gt;</code>, where
 * <code>flags</code> consists of zero or more non-letter characters,
 * <code>width</code> consists of zero or more numbers, <code>precision</code>
 * consists of a period followed by zero or more numbers, and
 * <code>conversionSpecfier</code> is a letter. Methods are exposed to allow
 * derived classes to define the valid flags and valid conversion specifiers.
 * 
 * @author Jon Nall
 */
public class PrintfFormatter
{
    // base formatters
    private ConversionFormatter sFormatter = new StringFormatter();
    private ConversionFormatter nFormatter = new NumericFormatter();
    private ConversionFormatter noArgFormatter = new NoArgFormatter();

    // Each character can have its own ConversionFormatter
    // We allocate an array of 128 (the number of basic ASCII chars) even
    // though we won't use them all. it just makes it easier to index via
    // the conversion spec character.
    private ConversionFormatter[] formatters = new ConversionFormatter[128];

    /**
     * Sole constructor.
     */
    protected PrintfFormatter()
    {
        // set up conversion formatters
        for (char c = 0; c < 128; ++c)
        {
            formatters[c] = getFormatterForSpec(c);
        }
    }

    /**
     * Formats the specified arguments as described in the specified format
     * string and prints the result to {@link System#out}.
     * <P>
     * This is equivalent to:<br>
     * <code>System.out.println(sprintf(fmt, args));</code>
     *
     * @param fmt the format to use for printing as described in {@link #sprintf}
     * @param args the arguments to be formatted
     */
    public void printf(CharSequence fmt, Object... args)
    {
        System.out.println(sprintf(fmt, args));
    }

    /**
     * Formats the specified arguments as described in the specified format
     * string and prints the result to the specified OutputStream.
     *
     * @param stream the OutputStream to which the formatted String will be printed
     * @param fmt the format to use for printing as described in {@link #sprintf}
     * @param args the arguments to be formatted
     */
    public void fprintf(OutputStream stream, CharSequence fmt, Object... args)
    {
        try
        {
            stream.write(sprintf(fmt, args).getBytes());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Formats <code>args</code> according the the specified format and returns
     * the resulting string.
     *
     * @param fmt the format string
     * @param args the arguments to be formatted
     * @return the formatted string
     */
    public final synchronized String sprintf(CharSequence fmt, Object... args)
    {
        int curArgIdx = 0;
        int curSpecIdx = 0;
        boolean processingPercent = false;

        EnumSet<PrintfFlag> flags = null;
        int widthSpec = 0;
        int precisionSpec = 0;

        boolean widthWasSpecified = false;
        boolean precisionWasSpecified = false;

        boolean processingFlags = false;
        boolean processingWidth = false;
        boolean processingPrecision = false;
        boolean sawPrecisionDot = false;

        PrintfSpec[] printSpecs = null;

        int startIdx = -1;
        int bufWidth = 0;

        final int length = fmt.length();
        for (int i = 0; i < length; ++i)
        {
            char c = fmt.charAt(i);
            if (c == '%')
            {
                if (!processingPercent)
                {
                    // lazy initialization/growing of printSpecs
                    if (printSpecs == null)
                    {
                        // who really calls printf with > 10 args?
                        printSpecs = new PrintfSpec[10];
                    }
                    else if (curArgIdx == printSpecs.length)
                    {
                        // if we run out of array entries, double it.
                        PrintfSpec[] tmpSpecArr = new PrintfSpec[printSpecs.length * 2];
                        System.arraycopy(printSpecs, 0, tmpSpecArr, 0,
                            printSpecs.length);
                        printSpecs = tmpSpecArr;
                    }

                    startIdx = i;
                    processingPercent = true;
                    widthSpec = 0;
                    widthWasSpecified = false;
                    precisionSpec = 0;
                    precisionWasSpecified = false;
                    flags = EnumSet.noneOf(PrintfFlag.class);
                    processingFlags = true;
                    continue;
                }
            }

            if (processingPercent)
            {
                if (processingFlags)
                {
                    assert (!processingWidth && !processingPrecision);
                    PrintfFlag flag = getFlag(c);

                    if (flag != null)
                    {
                        flags.add(flag);
                        continue;
                    }
                    else
                    {
                        processingFlags = false;
                        processingWidth = true;
                    }
                }
                if (processingWidth)
                {
                    assert (!processingFlags && !processingPrecision);
                    if (Character.isDigit(c))
                    {
                        widthWasSpecified = true;
                        widthSpec *= 10;
                        widthSpec += Character.getNumericValue(c);
                        continue;
                    }
                    else
                    {
                        processingWidth = false;
                        processingPrecision = true;
                    }
                }
                if (processingPrecision)
                {
                    assert (!processingFlags && !processingWidth);
                    if (!sawPrecisionDot)
                    {
                        if (c != '.')
                        {
                            processingPrecision = false;
                        }
                        else
                        {
                            precisionWasSpecified = true;
                            precisionSpec = 0;
                            sawPrecisionDot = true;
                            continue;
                        }
                    }
                    else if (Character.isDigit(c))
                    {
                        precisionSpec *= 10;
                        precisionSpec += Character.getNumericValue(c);
                        continue;
                    }
                    else
                    {
                        processingPrecision = false;
                    }
                }

                assert (!processingFlags && !processingWidth && !processingPrecision);

                final Object curArg = curArgIdx < args.length ? args[curArgIdx]
                    : null;
                final PrintfSpec spec = new PrintfSpec(startIdx, i, c, flags,
                    widthSpec, widthWasSpecified, precisionSpec,
                    precisionWasSpecified, curArg);

                ConversionFormatter formatter = getFormatter(c);
                if (formatter == null)
                {
                    throw new InvalidFormatSpecException(
                        "Unsupported conversion specifier: " + c);
                }

                bufWidth += formatter.getMaximumLength(spec);
                if (formatter.consumesArg(spec))
                {
                    ++curArgIdx;
                }
                printSpecs[curSpecIdx++] = spec;

                // append to the buffer and mark ourselves as no longer
                // processing a format specification
                startIdx = -1;
                processingPercent = false;
            }
            else
            {
                ++bufWidth;
            }
        }

        // we can return right now if there were no PrintfSpecs found
        if (printSpecs == null)
        {
            return postProcess(fmt).toString();
        }

        // check that all specs that consume an argument were given an argument
        if (curArgIdx != args.length)
        {
            warnAboutMissingArgs(args.length, curArgIdx);
        }

        // pass 2
        // iterate over the formatters to build the final string
        StringBuilder fmtBuf = new StringBuilder(bufWidth);
        int lastIdx = 0;
        for (final PrintfSpec spec : printSpecs)
        {
            if (spec == null) continue;

            fmtBuf.append(fmt.subSequence(lastIdx, spec.startIdx));
            ConversionFormatter formatter = getFormatter(spec.conversionSpec);
            assert (formatter != null);
            formatter.format(spec, fmtBuf);
            lastIdx = spec.endIdx + 1;
        }
        fmtBuf.append(fmt.subSequence(lastIdx, fmt.length()));

        //                System.out.println("fmt: " + fmt + ", obj: "
        //                    + ((args.length > 0) ? args[0] : null) + ", type: "
        //                    + ((args.length > 0 && args[0] != null) ?
        //                        args[0].getClass() : null));
        //                System.out.println("result[" + fmtBuf + "]");
        //                System.out.println("estimated: " + bufWidth);
        //                System.out.println("actual: " + fmtBuf.length());

        // assertion to check that we allocated properly. This is really just
        // a performance check
        assert (fmtBuf.length() <= bufWidth);

        return postProcess(fmtBuf).toString();
    }

    /**
     * Called after all formatting is complete. This method allows derived
     * classes a chance to operate on the entire formatted string. This
     * implementation just returns <code>buf</code>.
     *
     * @param buf the formatted string
     * @return the formatted string with any desired post-processing operations
     *      performed
     */
    protected CharSequence postProcess(CharSequence buf)
    {
        return buf;
    }

    /**
     * Returns the appropriate {@link ConversionFormatter} for the specified
     * conversion specifier.
     *
     * @param conversionSpec the conversion specifier for which to return
     *      a ConversionFormatter
     * @return a ConversionFormatter for <code>conversionSpec</code>
     */
    protected ConversionFormatter getFormatter(char conversionSpec)
    {
        return formatters[conversionSpec];
    }

    /**
     * Returns the {@link PrintfFlag} associated with the specified flag or
     * <code>null</code> if <code>flag</code> does not correspond to a
     * supported printf flag.
     *
     * @param flag the flag to check
     * @return the PrintfFlag corresponding to <code>flag</code> or
     *      <code>null</code> if there is no such PrintfFlag
     */
    protected PrintfFlag getFlag(char flag)
    {
        switch (flag)
        {
        case '#':
            return PrintfFlag.ALTERNATE_FORM;
        case '0':
            return PrintfFlag.ZERO_PAD;
        case '-':
            return PrintfFlag.LEFT_JUSTIFY;
        case ' ':
            return PrintfFlag.SPACE_BEFORE_POSITIVE_VALUE;
        case '+':
            return PrintfFlag.PRINT_SIGN;
        case '^':
            return PrintfFlag.UPPERCASE;
        case '=':
            return PrintfFlag.USE_NATURAL_WIDTH;
        default:
            return null;
        }
    }

    /**
     * Returns whether or not the specified conversion is a numeric conversion.
     *
     * @param conversionSpec the conversion specifier to check
     * @return <code>true</code> if the conversion specifier is a numeric
     *      conversion, <code>false</code> otherwise
     */
    private boolean isNumericConversion(char conversionSpec)
    {
        switch (conversionSpec)
        {
        case 'b':
        case 'd':
        case 'i':
        case 'o':
        case 'x':
        case 'X':
            return true;
        default:
            return false;
        }
    }

    /**
     * Returns whether or not the specified conversion is a string conversion.
     *
     * @param conversionSpec the conversion specifier to check
     * @return <code>true</code> if the conversion specifier is a string
     *      conversion, <code>false</code> otherwise
     */
    private boolean isStringConversion(char conversionSpec)
    {
        switch (conversionSpec)
        {
        case 'c':
        case 's':
            return true;
        default:
            return false;
        }
    }

    /**
     * Returns whether or not the specified conversion is a valid conversion.
     * <P>
     * Note that the range of values for which this method returns
     * <code>true</code> should be a superset of the union of
     * {@link #isNumericConversion} and {@link #isStringConversion}.
     *
     * @param conversionSpec the conversion specifier to check
     * @return <code>true</code> if the conversion specifier is a valid
     *      conversion, <code>false</code> otherwise
     */
    private boolean isValidConversion(char conversionSpec)
    {
        if (isNumericConversion(conversionSpec)
            || isStringConversion(conversionSpec))
        {
            return true;
        }

        switch (conversionSpec)
        {
        case '%':
            return true;
        default:
            return false;
        }
    }

    /**
     * Returns the {@link ConversionFormatter} for the specified conversion
     * specifier, or <code>null</code> if no ConversionFormatter exists for
     * the specifier.
     *
     * @param conversionSpec the conversion specifier to check
     * @return a ConversionFormatter for <code>conversionSpec</code> or
     *      <code>null</code> if no such ConversionFormatter exists
     */
    private ConversionFormatter getFormatterForSpec(char conversionSpec)
    {
        if (isNumericConversion(conversionSpec))
        {
            return nFormatter;
        }
        else if (isStringConversion(conversionSpec))
        {
            return sFormatter;
        }
        else if (isValidConversion(conversionSpec))
        {
            return noArgFormatter;
        }
        else
        {
            return null;
        }
    }

    /**
     * Warn the user that more arguments were consumed than were provided. Dump
     * a stack trace to help the user.
     *
     * @param observedArgs the number of arguments specified by the user
     * @param expectedArgs the number of arguments expected by sprintf. This
     *      value is exactly the number of conversion specifiers in the format
     *      that consume an argument.
     */
    private void warnAboutMissingArgs(int observedArgs, int expectedArgs)
    {
        // Give the user a pointer so they can fix their code
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        String psprintfCaller = null;
        StringBuilder buf = new StringBuilder(512);
        boolean foundSprintfCall = false;
        for (int i = 0; i < stack.length; ++i)
        {
            StackTraceElement level = stack[i];
            if (level.getClassName().equals(PrintfFormatter.class.getName()))
            {
                foundSprintfCall = true;
                continue;
            }

            else if (foundSprintfCall)
            {
                // assume the caller is the first non com.newisys.printf
                // method in the stack
                if (psprintfCaller == null
                    && !level.getClassName().startsWith("com.newisys.printf"))
                {
                    psprintfCaller = level.toString();
                }

                buf.append("\t");
                buf.append(level.toString());
                buf.append("\n");
            }
        }

        if (psprintfCaller == null)
        {
            psprintfCaller = "[UNKNOWN SOURCE]";
        }

        if (observedArgs < expectedArgs)
        {
            System.err.println("WARNING: psprintf() saw format specifier"
                + " without corresponding argument. Source: " + psprintfCaller);
        }
        else
        {
            System.err.println("WARNING: psprintf() saw argument"
                + " without corresponding format specifier. Source: "
                + psprintfCaller);
        }
        System.err.println(buf);
    }
}
