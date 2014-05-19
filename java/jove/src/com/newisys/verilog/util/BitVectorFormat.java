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

package com.newisys.verilog.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class converts Strings to BitVectorBuffer objects and vice versa.
 * Strings are of the format:
 * <P>
 * w'bn where:
 * <ul>
 * <li>w represents a length in decimal from 1 to 65535.
 * <li>b represents a base (one of b, o, d, or h).
 * <li>n represents a String in base b with a maximum length of w.
 * </ul>
 * <P>
 * Note that:
 * <ul>
 * <li>Alphabetic characters are case-insensitive.
 * <li>Underscores are allowed and are ignored.
 * <li>A leading '-' denotes a negative and is only valid for decimal values.
 * <li>The values "X" (Unknown) and "Z" (High-Impedance) are allowed in
 * binary, octal, and hexadecimal values only.
 * <li>If width is specified, base must also be specified. If not specified,
 * width defaults to 32 and base defaults to d.
 * </ul>
 * <P>
 * This class provides static methods to convert BitVectors to Strings and vice-
 * versa. A BitVectorFormat object also holds some state about how BitVectors
 * should be converted to Strings including whether or not to print radix and
 * length, if and when to insert underscores into formatted strings, whether or
 * not to use capitalization, etc.
 * 
 * @author Jon Nall
 */
public class BitVectorFormat
{
    private int radix;
    private int underscoreFreq;
    private boolean printLengthEnabled;
    private boolean printRadixEnabled;
    private boolean useCapitals;
    private boolean useXzCompression;
    private Integer formatWidth;
    private static Pattern binaryPattern = Pattern.compile("[^01XxZz]");
    private static Pattern octalPattern = Pattern.compile("[^0-7XxZz]");
    private static Pattern decimalPattern = Pattern.compile("[^0-9XxZz]");
    private static Pattern hexPattern = Pattern.compile("[^0-9A-Fa-fXxZz]");
    private static Pattern numPattern = Pattern
        .compile("-?(?:([0-9]+)?'([bodh]))?([0-9A-Fa-fXxZz]+)");
    private static Pattern leadingZeroPattern = Pattern
        .compile("^0+([0-9A-fa-f]+)");
    private static Pattern xzPattern = Pattern.compile("[XxZz]");

    private final static class DecimalHelper
    {
        private final static char[][] multTable = {
            { '0', '2', '4', '6', '8', '0', '2', '4', '6', '8' },
            { '1', '3', '5', '7', '9', '1', '3', '5', '7', '9' } };
        private final static char[][] multCarryTable = {
            { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 }, { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 } };

        private final static char[][] addTable = {
            { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' },
            { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' },
            { '2', '3', '4', '5', '6', '7', '8', '9', '0', '1' },
            { '3', '4', '5', '6', '7', '8', '9', '0', '1', '2' },
            { '4', '5', '6', '7', '8', '9', '0', '1', '2', '3' },
            { '5', '6', '7', '8', '9', '0', '1', '2', '3', '4' },
            { '6', '7', '8', '9', '0', '1', '2', '3', '4', '5' },
            { '7', '8', '9', '0', '1', '2', '3', '4', '5', '6' },
            { '8', '9', '0', '1', '2', '3', '4', '5', '6', '7' },
            { '9', '0', '1', '2', '3', '4', '5', '6', '7', '8' },
            { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' } };
        private final static char[][] addCarryTable = {
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
            { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 1, 1, 1 },
            { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 }, { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 },
            { 0, 0, 0, 0, 1, 1, 1, 1, 1, 1 }, { 0, 0, 0, 1, 1, 1, 1, 1, 1, 1 },
            { 0, 0, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 } };

        private final static char[] dtocTable = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9' };

        private static StringBuffer sbMul2(StringBuffer m0)
        {
            final int length = m0.length();
            final StringBuffer sbuf = new StringBuffer(length + 2);

            int carry = 0;
            for (int i = (length - 1); i >= 0; --i)
            {
                final int idx = Character.getNumericValue(m0.charAt(i));
                assert (idx >= 0);

                sbuf.append(multTable[carry][idx]);
                carry = multCarryTable[carry][idx];
            }

            if (carry == 1)
            {
                sbuf.append("1");
            }

            return sbuf.reverse();
        }

        private static StringBuffer sbAdd(StringBuffer a0, StringBuffer a1)
        {
            final int a0length = a0.length();
            final int a1length = a1.length();
            final int maxLength = Math.max(a0length, a1length);
            final StringBuffer sbuf = new StringBuffer(maxLength + 2);
            final StringBuffer a0rev = new StringBuffer(a0).reverse();
            final StringBuffer a1rev = new StringBuffer(a1).reverse();

            char tmp0 = '0';
            char tmp1 = '0';
            int carry = 0;
            for (int i = 0; i < maxLength; ++i)
            {
                tmp0 = (i >= a0length) ? '0' : a0rev.charAt(i);
                tmp1 = (i >= a1length) ? '0' : a1rev.charAt(i);
                final int idx0 = Character.getNumericValue(tmp0);
                final int idx1 = Character.getNumericValue(tmp1);
                assert (idx0 >= 0 && idx1 >= 0);

                sbuf.append(addTable[idx0 + carry][idx1]);
                carry = addCarryTable[idx0 + carry][idx1];
                assert (carry == 0 || carry == 1);
            }

            if (carry == 1)
            {
                sbuf.append("1");
            }

            return sbuf.reverse();
        }

        private static StringBuffer sbDiv2(StringBuffer d0)
        {
            final int length = d0.length();
            final StringBuffer sbuf = new StringBuffer(length + 1);
            final char[] tmp = { '0', '0' };

            boolean nonZeroValueSeen = false;
            for (int i = 0; i < length; ++i)
            {
                tmp[1] = d0.charAt(i);
                int idx = -1;

                if (tmp[0] == '0')
                {
                    idx = Character.getNumericValue(tmp[1]);
                }
                else
                {
                    idx = (Character.getNumericValue(tmp[0]) * 10)
                        + Character.getNumericValue(tmp[1]);
                }
                assert (idx >= 0);

                nonZeroValueSeen |= dtocTable[idx >> 1] != '0';
                if (nonZeroValueSeen)
                {
                    // don't append leading zeros
                    sbuf.append(dtocTable[idx >> 1]);
                }
                tmp[0] = (idx % 2 == 0) ? '0' : '1';
            }

            return sbuf;
        }

        private static String sbMod2(StringBuffer m0)
        {
            final int value = Character.getNumericValue(m0
                .charAt(m0.length() - 1));
            return (value % 2 == 0) ? "0" : "1";
        }

        static String decToBinStr(String s)
        {
            final int length = s.length();
            StringBuffer sbuf = new StringBuffer(length << 2);
            StringBuffer num = new StringBuffer(s);

            boolean done = false;
            while (!done)
            {
                sbuf.append(sbMod2(num));
                num = sbDiv2(num);
                for (int i = 0; i <= num.length(); ++i)
                {
                    if (i == num.length())
                    {
                        done = true;
                        break;
                    }
                    else if (num.charAt(i) != '0')
                    {
                        break;
                    }
                }
            }

            return sbuf.reverse().toString();
        }

        static String bitsToDecStr(BitVectorBuffer buffer)
        {
            final int length = buffer.length();
            final int numWords = ((length / BitVectorBuffer.BITS_PER_UNIT) + ((length
                % BitVectorBuffer.BITS_PER_UNIT == 0) ? 0 : 1));

            StringBuffer sbuf = new StringBuffer(length / 3).append("0");
            StringBuffer current = new StringBuffer(length / 3).append("1");

            for (int i = 0; i < numWords; ++i)
            {
                final int curWord = buffer.myValues[i];
                final int numBits = (i <= (numWords - 1))
                    ? BitVectorBuffer.BITS_PER_UNIT
                    : (length % BitVectorBuffer.BITS_PER_UNIT);

                for (int bit = 0; bit < numBits; ++bit)
                {
                    final int curBit = (curWord >> bit) & 1;
                    if (curBit == 1)
                    {
                        StringBuffer sbuf2 = sbAdd(sbuf, current);
                        sbuf = sbuf2;
                    }
                    current = sbMul2(current);
                }
            }

            return sbuf.toString();
        }
    }

    /**
     * Creates a BitVectorFormat with default values. The defaults are:
     * <table>
     * <tr><td>radix</td><td>16</td></tr>
     * <tr><td>underscore frequency</td><td>0 (no underscores)</td></tr>
     * <tr><td>format width</td><td>-1 (use BitVector's natural width)</td></tr>
     * <tr><td>print length</td><td>true</td></tr>
     * <tr><td>print radix</td><td>true</td></tr>
     * <tr><td>use capital letters</td><td>false</td></tr>
     * </table>
     */
    public BitVectorFormat()
    {
        radix = 16;
        underscoreFreq = 0;
        formatWidth = 0;
        printLengthEnabled = true;
        printRadixEnabled = true;
        useCapitals = false;
        useXzCompression = true;
    }

    /**
     * Converts a String to a BitVector.
     *
     *
     * @param value a String containing the BitVector representation to be parsed
     * @return a new BitVector with the value represented by <code>value</code>
     */
    public BitVector parse(String value)
    {
        return (new BitVector(parseString(value, -1)));
    }

    /**
     * Converts a String to a BitVector with the specified length. If there is
     * a length specifier in <code>value</code>, it is overriden by
     * <code>length</code>.
     *
     * @param value a String containing the BitVector representation to be parsed
     * @param length the length of the new BitVector, which must be greater than
     *      zero
     * @return a new BitVector with the value specified by <code>value</code>
     *      and a length specified by <code>length</code>
     */
    public BitVector parse(String value, int length)
    {
        if (length <= 0)
        {
            throw new IllegalArgumentException("length (" + length
                + ") is <= 0");
        }
        return (new BitVector(parseString(value, length)));
    }

    /**
     * Converts a BitVector to a String using the current radix setting of
     * this BitVectorFormat.
     *
     * @param vector a BitVector to be converted to a String
     * @return a new String with the given radix representing <code>vector</code>
     */
    public String format(BitVector vector)
    {
        return (format(vector, radix));
    }

    /**
     * Converts a BitVector to a String with the provided radix.
     *
     * @param vector a BitVector to be converted to a String
     * @param radix the radix to use in the String representation
     * @return a new String with the given radix representing <code>vector</code>
     */
    public String format(BitVector vector, int radix)
    {
        return format(vector.myBuffer, radix);
    }

    /**
     * Converts a BitVectorBuffer to a String with the provided radix.
     *
     * @param buffer a BitVectorBuffer to be converted to a String
     * @param radix the radix to use in the String representation
     * @return a new String with the given radix representing <code>buffer</code>
     */
    public String format(BitVectorBuffer buffer, int radix)
    {
        if (buffer.length() == 0)
        {
            return "<initializing>";
        }

        StringBuffer strBuf = new StringBuffer();
        if (printRadixEnabled)
        {
            if (printLengthEnabled)
            {
                strBuf.append(String.valueOf(buffer.length()));
            }
            strBuf.append("'" + getRadixString(radix));
        }

        String valueStr = bitsToString(buffer, radix);

        if (useCapitals)
        {
            valueStr = valueStr.toUpperCase();
        }

        int naturalWidth = valueStr.length();
        if (formatWidth != null)
        {
            Matcher m = leadingZeroPattern.matcher(valueStr);
            if (m.matches())
            {
                valueStr = m.group(1);
            }

            if (formatWidth > valueStr.length())
            {
                for (int i = 0; i < (formatWidth - valueStr.length()); ++i)
                {
                    strBuf.append((i > naturalWidth) ? " " : "0");
                }
            }
        }

        strBuf.append(valueStr);
        return (strBuf.toString());
    }

    /////////////////////////////////////////////
    /////////////////////////////////////////////
    // Private Methods start here
    /////////////////////////////////////////////
    /////////////////////////////////////////////

    /**
     * Takes a String and returns a BitVectorBuffer. It validates that the value
     * string is legal for the given radix and populates the BitVectorBuffer
     * with the appropriate values.
     *
     * @param value the String to parse
     * @param length the maximum length of the resulting BitVectorBuffer or -1
     *            if the length should be determined from value
     * @return a BitVectorBuffer having the value of value
     * @throws IllegalArgumentException value doesn't conform to a String
     *             representing a valid Verilog Number
     * @throws NumberFormatException the length is zero or > 65335, or a
     *             negative prefix is used with a radix other than 10, or an X
     *             or Z value appears in a String with a radix of 10.
     */
    private BitVectorBuffer parseString(String value, int length)
    {
        int realLength = 0;
        int bvLength = 0;
        int radix = 0;
        value = value.replaceAll("_", "").toLowerCase();

        Matcher matcher = numPattern.matcher(value);
        if (!matcher.matches())
        {
            throw new IllegalArgumentException("Invalid number: " + value);
        }

        String sizeStr = matcher.group(1);
        String radixStr = matcher.group(2);
        String numStr = matcher.group(3);
        /////////////////////
        // radix calculations
        if (radixStr != null)
        {
            switch (radixStr.charAt(0))
            {
            case 'b':
                radix = 2;
                break;
            case 'o':
                radix = 8;
                break;
            case 'd':
                radix = 10;
                break;
            case 'h':
                radix = 16;
                break;
            default:
                throw new NumberFormatException("Unsupported radix: "
                    + radixStr.charAt(0));
            }
        }
        else
        {
            radix = this.radix;
        }
        validateRadix(numStr, radix);

        ////////////////////
        // length calculation
        if (length != -1)
        {
            // use the supplied length if it's valid
            realLength = length;
        }
        else if (sizeStr != null)
        {
            // if supplied length is invalid, check for valid length specifier
            // in string
            realLength = Integer.parseInt(sizeStr);
        }
        else
        {
            // calculate the minimum number of bits needed to contain the value
            realLength = (int) Math.ceil(numStr.length() * Math.log(radix)
                / Math.log(2));

            // print a warning if we're going to overflow, then force 32 bits
            if (realLength > 32)
            {
                System.err
                    .println("WARNING: Data overflow. Truncating value to 32 bits. ["
                        + value + "]");
            }
            realLength = 32;
        }

        bvLength = realLength;

        /////////////////////
        // value calculations
        boolean negative = value.startsWith("-");
        if (radix == 10)
        {
            if (xzPattern.matcher(numStr).find() == true)
            {
                throw new NumberFormatException(
                    "X/Z Values are not allowed in decimal values");
            }

            // stringToBitVectorBuffer works on radices which are a power
            // of two. so if the radix we get is 10, convert it to something
            // else (say, binary). do this conversion after validateRadix() since
            // if that fails the user should get a meaningful Exception message.

            // OK to use BigInteger since there are no X/Z values
            numStr = DecimalHelper.decToBinStr(numStr);
            radix = 2;
        }

        return (stringToBitVectorBuffer(negative, realLength, bvLength, radix,
            numStr));
    }

    /**
     * Validates that value is a legal numeric sequence for the given radix.
     *
     * @param value String containing characters to check
     * @param radix Radix to use when checking
     * @throws NumberFormatException value contains a character which isn't a
     *             valid digit of base radix
     */
    private void validateRadix(String value, int radix)
    {
        switch (radix)
        {
        case 2:
        {
            if (binaryPattern.matcher(value).find() == true)
            {
                throw new NumberFormatException("Value not binary: " + value);
            }
        }
        case 8:
        {
            if (octalPattern.matcher(value).find() == true)
            {
                throw new NumberFormatException("Value not octal: " + value);
            }
        }
        case 10:
        {
            if (decimalPattern.matcher(value).find() == true)
            {
                throw new NumberFormatException("Value not decimal: " + value);
            }
        }
        case 16:
        {
            if (hexPattern.matcher(value).find() == true)
            {
                throw new NumberFormatException("Value not hexadecimal: "
                    + value);
            }
        }
        }
    }

    /**
     * Return a String representation of radix.
     *
     * @param radix the radix to convert
     * @return a String representation of the provided radix
     * @throws NumberFormatException the radix is unsupported
     */
    private String getRadixString(int radix)
    {
        switch (radix)
        {
        case 2:
            return (new String("b"));
        case 8:
            return (new String("o"));
        case 10:
            return (new String("d"));
        case 16:
            return (new String("h"));
        default:
            throw new NumberFormatException("Invalid radix: "
                + String.valueOf(radix));
        }
    }

    char[][] charTable = new char[][] {
        // log2=0 (invalid)
        {},
        // log2=1 (binary)
        { '0', '1' },
        // log2=2 (invalid)
        {},
        // log2=3 (octal)
        { '0', '1', '2', '3', '4', '5', '6', '7' },
        // log2=4 (hexadecimal)
        { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f' } };

    /**
     * Returns the String representation for the specified BitVectorBuffer with
     * the specified radix. If <code>buffer</code> includes an X or Z, that will
     * be shown on a per-character basis, depending on the radix. For radices
     * greater than 2, if any bit in a value is X or Z, the character will be
     * filled by X, Z, or ?. If a combination of 0, 1, X, and Z are present in a
     * character, a ? will be inserted.
     *
     * @param buffer the BitVectorBuffer to convert to a String
     * @param radix the radix to use in the String representation
     * @return a String representation of <code>buffer</code> with the
     *      specified radix
     */
    private String bitsToString(BitVectorBuffer buffer, int radix)
    {
        final int length = buffer.length();
        final int log2 = Integer.numberOfTrailingZeros(radix);

        // assert radix is a real power of 2 and is not zero
        assert (radix == 10 || Integer.highestOneBit(radix) == Integer
            .lowestOneBit(radix));
        assert (log2 != 32);

        final StringBuffer sbuf = new StringBuffer(length / log2);
        int count = 0;
        int curWord = 0;
        int curBit = 0;

        if (radix == 10)
        {
            // any X/Z value results in a "?" string
            if (buffer.containsXZ())
            {
                return "?";
            }
            else if (length < 64)
            {
                long longValue = buffer.longValue();
                return Long.toString(longValue);
            }
            else
            {
                return DecimalHelper.bitsToDecStr(buffer);
            }
        }

        do
        {
            // create a mask with a width of either log2(radix) or the number of
            // bits left (in the case where the number of bits left < log2(radix))
            final int mask = (count + log2 < length) ? (1 << log2) - 1
                : (1 << (length - count)) - 1;
            int idx = (buffer.myValues[curWord] >> curBit) & mask;
            idx |= ((buffer.myXzMask[curWord] >> curBit) & mask) << log2;

            if (underscoreFreq != 0 && count != 0
                && (count % underscoreFreq) == 0)
            {
                sbuf.append("_");
            }

            if (idx == (mask << log2))
            {
                // 3'bz
                sbuf.append((radix == 2) ? "z" : "Z");
            }
            else if (idx == ((mask << log2) | mask))
            {
                // 3'bx
                sbuf.append((radix == 2) ? "x" : "X");
            }
            else if ((idx & (mask << log2)) != 0)
            {
                // mix of x/z/0/1 values
                sbuf.append("?");
            }
            else
            {
                // no xz value
                sbuf.append(charTable[log2][idx]);
            }
            count += log2;
            curBit += log2;
            if (curBit >= BitVectorBuffer.BITS_PER_UNIT)
            {
                curBit = 0;
                curWord++;
            }
        }
        while (count < length);

        // we built up the string backwards (sbuf[0] is the LSB), so reverse it.
        sbuf.reverse();

        if (!useXzCompression)
        {
            return sbuf.toString();
        }

        // compress X/Z/0 values
        char initialChar = sbuf.charAt(0);
        if (initialChar == 'x' || initialChar == 'X' || initialChar == 'z'
            || initialChar == 'Z' || initialChar == '0')
        {
            int lastZero = 0;
            int lastNonUnderScore = 0;
            int endOfDelete = 0;
            final int sLength = sbuf.length();

            for (int i = 1; i < sLength; ++i)
            {
                final char curChar = sbuf.charAt(i);
                if (curChar == '_') continue;

                if (curChar == '0')
                {
                    lastZero = i;
                }

                if (initialChar != curChar)
                {
                    final boolean preserveZero = (initialChar == '0' && (radix == 8))
                        || buffer.isZero();
                    endOfDelete = (preserveZero) ? lastZero
                        : (initialChar == '0') ? i : lastNonUnderScore;

                    if (initialChar == '0'
                        && !preserveZero
                        && (curChar == 'x' || curChar == 'X' || curChar == 'z' || curChar == 'Z'))
                    {
                        // sbuf could be of the form 000xx123, so we have to reduce
                        // the xx
                        initialChar = curChar;
                    }
                    else
                    {
                        break;
                    }
                }
                else if ((i + 1) == sLength)
                {
                    endOfDelete = i;
                }

                lastNonUnderScore = i;
            }

            if (endOfDelete != 0)
            {
                sbuf.delete(0, endOfDelete);
            }
        }

        return sbuf.toString();
    }

    /**
     * Creates a new BitVectorBuffer object with the specified length and
     * populates it with the data represented in <code>value</code>, which is in
     * base <code>radix</code>.
     *
     * @param negative true if this is a negative value, false if a positive value
     * @param length the length of the value passed in
     * @param finalLength the length of the BitVectorBuffer to be returned
     * @param radix the base in which <code>value</code> is formatted
     * @param value String to convert to a BitVectorBuffer
     * @return a BitVectorBuffer based on <code>length</code>, <code>finalLength</code>,
     *      <code>radix</code>, and <code>value</code>
     * @throws IllegalArgumentException the length of the String is to small to
     *             contain the value
     */
    private BitVectorBuffer stringToBitVectorBuffer(
        boolean negative,
        int length,
        int finalLength,
        int radix,
        String value)
    {
        int bitIdx = 0;
        int charIdx;
        int logRadix;
        int msbValue = -1;
        int msbMask = -1;
        StringBuffer strBuf = new StringBuffer(value);
        BitVectorBuffer buf = new BitVectorBuffer(finalLength, 0);
        logRadix = Integer.numberOfTrailingZeros(radix);

        // assert radix is a real power of 2 and is not zero
        assert (radix == 10 || Integer.highestOneBit(radix) == Integer
            .lowestOneBit(radix));
        assert (logRadix != 32);

        strBuf.reverse();

        // iterate through _value, populating myValues
        // appropriately, depending on radix
        for (charIdx = 0; charIdx < strBuf.length(); charIdx++)
        {
            char curChar = strBuf.charAt(charIdx);
            int bitShiftVal = 0;
            for (bitIdx = (charIdx * logRadix), bitShiftVal = 0; bitIdx < ((charIdx * logRadix) + logRadix); bitIdx++, bitShiftVal++)
            {
                int intIdx = bitIdx / BitVectorBuffer.BITS_PER_UNIT;
                int intShiftVal = bitIdx % BitVectorBuffer.BITS_PER_UNIT;
                boolean charIsX = Character.toLowerCase(curChar) == 'x';
                boolean charIsZ = Character.toLowerCase(curChar) == 'z';
                int curNum = 0;

                if (bitIdx >= finalLength) break;

                if (!(charIsX || charIsZ))
                {
                    curNum = Character.digit(curChar, radix);

                    if (bitIdx >= length && (curNum & (1 << bitShiftVal)) != 0)
                    {
                        throw new IllegalArgumentException("Length ("
                            + String.valueOf(length)
                            + ") is too small for value (" + strBuf + ")");
                    }
                }
                if (charIsX)
                {
                    msbValue = 1;
                    msbMask = 1;
                }
                else if (charIsZ)
                {
                    msbValue = 0;
                    msbMask = 1;
                }
                else
                {
                    // OK to use BigInteger as this isn't an X/Z value
                    msbValue = (curNum >> bitShiftVal);
                    msbMask = 0;
                }

                buf.myValues[intIdx] |= (msbValue << intShiftVal);
                buf.myXzMask[intIdx] |= (msbMask << intShiftVal);
            }
        }

        // Now check that length bits were actually specified.
        // If not, perform the extension.
        assert (msbValue != -1 && msbMask != -1);
        if (msbMask == 0) msbValue = 0; // non-X/Z values are extended with 0's
        while (bitIdx < finalLength)
        {
            int intIdx = bitIdx / BitVectorBuffer.BITS_PER_UNIT;
            int intShiftVal = bitIdx % BitVectorBuffer.BITS_PER_UNIT;
            buf.myValues[intIdx] |= (msbValue << intShiftVal);
            buf.myXzMask[intIdx] |= (msbMask << intShiftVal);
            bitIdx++;
        }
        if (negative)
        {
            buf.negate();
        }
        return (buf);
    }

    /**
     * Returns the radix used when formatting.
     *
     * @return Returns the radix.
     */
    public int getRadix()
    {
        return radix;
    }

    /**
     * Sets the radix to use when formatting.
     *
     * @param radix The radix to use when formatting.
     */
    public void setRadix(int radix)
    {
        this.radix = radix;
    }

    /**
     * Returns the width used when formatting.
     *
     * @return Returns the format width.
     */
    public int getFormatWidth()
    {
        return formatWidth;
    }

    /**
     * Sets the width to use when formatting. The formatter will use the greater
     * of a BitVector's natural width and this value when printing. If padding is
     * required, spaces are used. <code>width</code> must be >= 0.
     *
     * A width of 0 will avoid printing leading zeros. A <code>null</code>
     * width means to use the values natural width.
     *
     * @param width The width to use when formatting.
     */
    public void setFormatWidth(Integer width)
    {
        if (width != null && width < 0)
        {
            throw new IllegalArgumentException("width must be >= 0");
        }

        this.formatWidth = width;
    }

    /**
     * Returns the underscore frequency used when formatting.
     *
     * @return Returns the underscore frequency.
     */
    public int getUnderscoreFreq()
    {
        return underscoreFreq;
    }

    /**
     * Sets the underscore frequency to use when formatting. A frequency of X
     * will print an underscore after every X characters.
     *
     * @param frequency The underscore frequency to set.
     */
    public void setUnderscoreFreq(int frequency)
    {
        this.underscoreFreq = frequency;
    }

    /**
     * Returns whether or not lengths are currently a part of formatted strings.
     *
     * @return Returns true if lengths are a part of formatted strings.
     */
    public boolean getPrintLength()
    {
        return printLengthEnabled;
    }

    /**
     * Sets whether or not the length should be a part of formatted strings.
     *
     * @param enable true if lengths should be a part of formatted strings.
     */
    public void setPrintLength(boolean enable)
    {
        this.printLengthEnabled = enable;
    }

    /**
     * Returns whether or not radices are should be a part of formatted strings.
     *
     * @return Returns true if radices are a part of formatted strings.
     */
    public boolean getPrintRadix()
    {
        return printRadixEnabled;
    }

    /**
     * Sets whether or not the radix should be a part of formatted strings. If
     * this setting is changed to false, the width will also not be printed in
     * generated Strings.
     *
     * @param enable true if radices should be a part of generated strings.
     */
    public void setPrintRadix(boolean enable)
    {
        this.printRadixEnabled = enable;
    }

    /**
     * Returns whether or not hex digits are printed in capital letters in
     * formatted strings.
     *
     * @return Returns true if formatter is configured to print in capital
     *         letters.
     */
    public boolean getCapitalization()
    {
        return useCapitals;
    }

    /**
     * Sets whether or not hex digits should be capitalized in formatted strings.
     *
     * @param enable true if hex digits should be printed in capital letters
     */
    public void setCapitalization(boolean enable)
    {
        this.useCapitals = enable;
    }

    /**
     * Sets whether or not leading X/Z values should be compressed down to one
     * X/Z. For instance <code>16'hx4</code> will be printed as <code>16'hX4</code>
     * rather than <code>16'hXXX4</code>.
     *
     * @param enable true if leading X/Z values should be compressed
     */
    public void setXzCompression(boolean enable)
    {
        this.useXzCompression = enable;
    }

    /**
     * Returns whether or not leading X/Z values should be compressed down to one
     * X/Z.
     *
     * @return true if leading X/Z compression is enabled, false otherwise
     */
    public boolean getXzCompression()
    {
        return this.useXzCompression;
    }

}
