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

import java.io.Serializable;
import java.math.BigInteger;

/**
 * A mutable class that implements the functions of a BitVector including
 * arithmetic and logical operations, as well as bit access.
 * 
 * @author Jon Nall
 */
// TODO: Review assertions/exceptions thrown and make sure they're correct and consistent
public final class BitVectorBuffer
    extends Number
    implements Comparable, Serializable
{
    static final long serialVersionUID = 1540610592319205698L;

    final static int BITS_PER_UNIT = 32;
    final private static BitVectorFormat myFormat = new BitVectorFormat();

    private int myLength;
    int[] myValues; // NEVER use myValues.length outside of
    // maskExtraLength/extend/truncate. always use
    // getLastEntry()
    int[] myXzMask; // FOLLOWUP: can this be null for a performance boost?

    // Constructors
    /**
     * Creates a BitVectorBuffer with the specified size, with all bits set to
     * {@link Bit#X}.
     *
     * @param length The size of the BitVectorBuffer in bits.
     */
    public BitVectorBuffer(int length)
    {
        this(length, Bit.X);
    }

    /**
     * Creates a BitVectorBuffer with a length of 1 from the given Bit
     *
     * @param bit value to use for the BitVectorBuffer
     */
    BitVectorBuffer(Bit bit)
    {
        this(1, bit);
    }

    /**
     * Creates a BitVectorBuffer with the specified size, with each bit having
     * the value bit.
     *
     * @param length The size of the BitVectorBuffer in bits.
     * @param bit The Bit to which each bit of the BitVectorBuffer should be
     *            set.
     */
    public BitVectorBuffer(int length, Bit bit)
    {
        myLength = 0;
        myValues = new int[1];
        myXzMask = new int[1];
        setLength(length, bit);
    }

    /**
     * Creates a BitVectorBuffer with the specified size, filled with the
     * initial value provided. Bits 32 and higher are sign-extended.
     *
     * @param length The size of the BitVectorBuffer in bits.
     * @param value The value with which to initialize the BitVectorBuffer.
     */
    public BitVectorBuffer(int length, int value)
    {
        this(length, value, true);
    }

    /**
     * Creates a BitVectorBuffer with the specified size, filled with the
     * initial value provided. Bits 32 and higher are either zero-filled or
     * sign-extended, depending on the signExtend parameter
     *
     * @param length The size of the BitVectorBuffer in bits.
     * @param value The value with which to initialize the BitVectorBuffer.
     * @param signExtend true if bits 32 and higher should be sign extended,
     *            false if they should be zero-extended.
     */
    public BitVectorBuffer(int length, int value, boolean signExtend)
    {
        myLength = BITS_PER_UNIT;
        myValues = new int[1];
        myXzMask = new int[1];
        myValues[0] = value;

        Bit extensionBit = (signExtend ? (value < 0) ? Bit.ONE : Bit.ZERO
            : Bit.ZERO);
        setLength(length, extensionBit);
    }

    /**
     * Creates a BitVectorBuffer with the specified size, filled with the
     * initial value provided. Bits 64 and higher are sign-extended.
     *
     * @param length The size of the BitVectorBuffer in bits.
     * @param value The value with which to initialize the BitVectorBuffer.
     */
    public BitVectorBuffer(int length, long value)
    {
        this(length, value, true);
    }

    /**
     * Creates a BitVectorBuffer with the specified size, filled with the
     * initial value provided. Bits 64 and higher are sign-extended or
     * zero-filled, depending on the signExtend parameter.
     *
     * @param length The size of the BitVectorBuffer in bits.
     * @param value The value with which to initialize the BitVectorBuffer.
     * @param signExtend true if bits 64 and higher should be sign extended,
     *            false if they should be zero-extended.
     */
    public BitVectorBuffer(int length, long value, boolean signExtend)
    {
        myLength = BITS_PER_UNIT * 2;
        myValues = new int[] { (int) (value & ~0),
            (int) (value >> BITS_PER_UNIT) };
        myXzMask = new int[] { 0, 0 };

        Bit extensionBit = (signExtend ? (value < 0) ? Bit.ONE : Bit.ZERO
            : Bit.ZERO);
        setLength(length, extensionBit);
    }

    /**
     * Creates a BitVectorBuffer from a Verilog-style String of the format
     * described in {@link BitVectorFormat BitVectorFormat}.
     *
     * @param value String with which to initialize the BitVectorBuffer.
     */
    public BitVectorBuffer(String value)
    {
        this(myFormat.parse(value));
    }

    /**
     * Creates a BitVectorBuffer from a Verilog-style String of the format
     * described in {@link BitVectorFormat BitVectorFormat}. It will have a
     * length of length, which may cause value to be either truncated or
     * zero-extended.
     *
     * @param value String with which to initialize the BitVectorBuffer.
     * @param length size of the BitVectorBuffer in bits.
     */
    public BitVectorBuffer(String value, int length)
    {
        this(myFormat.parse(value, length));
        setLength(length);
    }

    /**
     * Creates a BitVectorBuffer from the given byte array. The input array is
     * assumed to be in big-endian byte-order: the most significant byte is in
     * the zeroth element.
     *
     * @param bytes a byte array containing values used to initialize the
     *            BitVectorBuffer
     */
    public BitVectorBuffer(byte[] bytes)
    {
        // Create a BitVectorBuffer with the proper length, initialized to zero
        this(bytes.length * 8, 0);

        int bite = 0;
        int intVal = 0;
        int shiftVal = 0;

        for (bite = bytes.length - 1; bite >= 0; --bite)
        {
            int byteVal = bytes[bite] & 0x0FF;
            myValues[intVal] |= (byteVal << (shiftVal * 8));

            ++shiftVal;
            if ((shiftVal % 4) == 0)
            {
                intVal++;
                shiftVal = 4;
            }
        }
    }

    /**
     * Creates a BitVectorBuffer from a BitVector.
     *
     * @param vect BitVector with which to initialize the BitVectorBuffer.
     */
    public BitVectorBuffer(BitVector vect)
    {
        this(vect.myBuffer);
    }

    /**
     * Creates a BitVectorBuffer from a BitVectorBuffer.
     *
     * @param buf BitVectorBuffer with which to initialize the BitVectorBuffer.
     */
    public BitVectorBuffer(BitVectorBuffer buf)
    {
        this(buf.myValues, buf.myXzMask, buf.length());
    }

    /**
     * Creates a BitVectorBuffer from the given parameters
     *
     * @param values int array to use for 0/1 values
     * @param xzmask int array to use for x/z values
     * @param length size of the new BitVectorBuffer in bits
     */
    BitVectorBuffer(int[] values, int[] xzmask, int length)
    {
        assign(length, values, xzmask);
    }

    /**
     * Returns the number of bits in this BitVectorBuffer.
     *
     * @return length of the BitVectorBuffer
     */
    public int length()
    {
        return myLength;
    }

    /**
     * Assigns vect to this BitVectorBuffer, extending or truncating such that
     * the length of this BitVectorBuffer is unchanged.
     *
     * @param vect BitVector to assign to this BitVectorBuffer
     * @return this BitVectorBuffer after performing the assign operation.
     */
    public BitVectorBuffer assign(BitVector vect)
    {
        return assign(vect.myBuffer, false);
    }

    /**
     * {@link #assign(BitVector) assign(BitVector)}
     *
     * @param buf BitVectorBuffer to assign to this BitVectorBuffer
     * @param extendThis set to true if this BitVectorBuffer should be
     *            lengthened to
     *            <code>Math.max(buf.length(), this.length())</code>
     * @return this BitVectorBuffer after performing the assign operation.
     */
    private BitVectorBuffer assign(BitVectorBuffer buf, boolean extendThis)
    {
        int length = (extendThis ? Math.max(buf.length(), this.length()) : this
            .length());
        return assign(length, buf.myValues, buf.myXzMask);
    }

    private BitVectorBuffer assign(int length, int[] values, int[] xzmask)
    {
        myLength = length;

        // This routine is the base of most BitVectorBuffer and BitVector
        // constructors. Use new + System.arraycopy here as that seems to
        // be much faster than either doing a clone() or a java for loop,
        // copying one element at a time.
        //
        // Josh Bloch thinks this is the best way.
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6195753
        assert(values.length == xzmask.length);
        final int arrLength = values.length;
        myValues = new int[arrLength];
        myXzMask = new int[arrLength];
        System.arraycopy(values, 0, myValues, 0, arrLength);
        System.arraycopy(xzmask, 0, myXzMask, 0, arrLength);

        maskExtraLength();
        return this;
    }

    /**
     * Assigns vect to this BitVectorBufferdepending on mask and returns a this
     * BitVectorBuffer. Assigns vect to this if mask is a 1:
     * <P>
     * <code><pre>
     * if (mask[i] == 1)
     *     this[i] = vect[i];
     * else if (mask[i] == X || mask[i] == Z)
     *     this[i] = X;
     * else
     *     this[i] = this[i];
     * </pre></code>
     * <P>
     * mask is 0-extended if needed. vect is 0-extended if needed. the length of
     * this will remain unchanged.
     *
     * @param vect The BitVector to assign from
     * @param mask The mask to use when deciding what to assign to this
     *            BitVectorBuffer
     * @return This BitVectorBuffer with the value determined by the above
     *         algorithm.
     */
    public BitVectorBuffer assignMask(BitVector vect, BitVector mask)
    {
        return assignMask(vect.myBuffer, mask.myBuffer);
    }

    /**
     * {@link #assignMask(BitVector, BitVector) assignMask(BitVector, BitVector)}
     *
     * @param buf The BitVector to assign from
     * @param mask The mask to use when deciding what to assign to this
     *            BitVectorBuffer
     * @return This BitVectorBuffer with the value determined by the above
     *         algorithm.
     */
    private BitVectorBuffer assignMask(BitVectorBuffer buf, BitVectorBuffer mask)
    {
        int curBit = 0;
        int maskValues;
        int maskXzMask;
        int bufValues;
        int bufXzMask;
        for (int i = 0; i < getLastEntry(); i++)
        {
            curBit = (i + 1) * BITS_PER_UNIT;

            ////////////////////
            // MASK calculation
            if (mask.length() < curBit)
            {
                int bitDiff = curBit - mask.length();
                if (bitDiff > BITS_PER_UNIT)
                {
                    // need to entirely generate this value
                    // zero-extend
                    maskValues = 0;
                    maskXzMask = 0;
                }
                else
                {
                    // need to generate part of this value
                    int clearMask = ~(((1 << bitDiff) - 1) << mask.length());
                    maskValues = (mask.myValues[i] & clearMask);
                    maskXzMask = (mask.myXzMask[i] & clearMask);
                }
            }
            else
            {
                // still have a valid mask value
                maskValues = mask.myValues[i];
                maskXzMask = mask.myXzMask[i];
            }

            ////////////////////
            // BUF calculation
            if (buf.length() < curBit)
            {
                int bitDiff = curBit - buf.length();
                if (bitDiff > BITS_PER_UNIT)
                {
                    // need to entirely generate this value
                    // 0-extend
                    bufValues = 0;
                    bufXzMask = 0;
                }
                else
                {
                    // need to generate part of this value
                    int clearMask = ~(((1 << bitDiff) - 1) << buf.length());
                    bufValues = (buf.myValues[i] & clearMask);
                    bufXzMask = (buf.myXzMask[i] & clearMask);
                }
            }
            else
            {
                // still have a valid buf value
                bufValues = buf.myValues[i];
                bufXzMask = buf.myXzMask[i];
            }

            // System.err.println("mask: " + Integer.toHexString(maskValues) +
            //     "/" + Integer.toHexString(maskXzMask));
            // System.err.println("buf : " + Integer.toHexString(bufValues) +
            //     "/" + Integer.toHexString(bufXzMask));
            ////////////////////
            // do the assign

            // Force any X/Z bit in the mask to an X
            int forceX = maskXzMask;
            // Clear any 1 bit in the mask
            int clearBits = ~(maskValues & ~maskXzMask);

            myValues[i] &= clearBits;
            myXzMask[i] &= clearBits;

            myValues[i] |= (maskValues & bufValues);
            myXzMask[i] |= (maskValues & bufXzMask);

            myValues[i] |= forceX;
            myXzMask[i] |= forceX;
        }

        return this;
    }

    /**
     * Sets this BitVectorBuffer to the specified length, truncating or
     * zero-extending as needed.
     *
     * @param length the new length of the BitVectorBuffer
     * @return this BitVectorBuffer set to length
     */
    public BitVectorBuffer setLength(int length)
    {
        return setLength(length, Bit.ZERO);
    }

    /**
     * Sets this BitVectorBuffer to the specified length, truncating or
     * extending with bit as needed.
     *
     * @param length the length to which the BitVectorBuffer should be extended
     * @param bit the Bit to fill with when extending the BitVectorBuffer
     * @return this BitVectorBuffer set to _length, extended as appropriate
     */
    public BitVectorBuffer setLength(int length, Bit bit)
    {
        assert (length > 0);
        // if there's no change, just return
        if (length() == length)
        {
            return this;
        }
        else if (length < length())
        {
            return truncate(length);
        }
        else
        {
            // extend with sign-extension
            return extend(length, bit);
        }
    }

    /**
     * Set the length of this BitVectorBuffer, truncating or extending as
     * needed. If extension is needed, the most significant bit will be used to
     * extend the value.
     *
     * @param length The length to which the BitVectorBuffer should be extended
     * @return this BitVectorBuffer set to length, extended as appropriate
     */
    public BitVectorBuffer setLengthHigh(int length)
    {
        return setLength(length, getBit(length() - 1));
    }

    /**
     * Compares this BitVectorBuffer with the specified Object for order. If obj
     * is not a BitVector, a CastClassException is thrown.
     *
     * @param obj the object to compare
     * @return a negative number, zero, or a positive number as this
     *         BitVectorBuffer is numerically less than, equal to, or greater
     *         than o, which must be a BitVectorBuffer.
     * @throws ClassCastException - if the specified object's type prevents it
     *             from being compared to this Object.
     * @throws XZException - if either this BitVectorBuffer or obj
     *             contains X/Z values
     */
    public int compareTo(Object obj)
    {
        BitVectorBuffer buf = (BitVectorBuffer) obj;

        if (containsXZ() || buf.containsXZ())
        {
            throw new XZException("compareTo doesn't support X/Z values: "
                + this.toString(2));
        }

        int thisLastEntry = this.getLastEntry();
        int bufLastEntry = buf.getLastEntry();
        int maxLastEntry = Math.max(thisLastEntry, bufLastEntry);

        for (int i = maxLastEntry - 1; i >= 0; i--)
        {
            long thisInt = (i >= thisLastEntry) ? 0 : this.myValues[i];
            long thatInt = (i >= bufLastEntry) ? 0 : buf.myValues[i];
            thisInt &= ((1L << BITS_PER_UNIT) - 1);
            thatInt &= ((1L << BITS_PER_UNIT) - 1);
            if (thisInt > thatInt)
            {
                return 1;
            }
            else if (thisInt < thatInt)
            {
                return -1;
            }
        }
        for (int i = maxLastEntry; i < bufLastEntry; i++)
        {
            if (buf.myValues[i] != 0)
            {
                return -1;
            }
        }
        for (int i = maxLastEntry; i < thisLastEntry; i++)
        {
            if (this.myValues[i] != 0)
            {
                return 1;
            }
        }
        return 0;
    }

    /**
     * Compares the Object represented by obj to this BitVectorBuffer
     * (equivalent to Verilog ==).
     * <P>
     * Comparison Table: <table border=1>
     * <tr>
     * <td>==</td>
     * <td>0</td>
     * <td>1</td>
     * <td>X</td>
     * <td>Z</td>
     * </tr>
     * <P>
     * <tr>
     * <td>0</td>
     * <td>true</td>
     * <td>false</td>
     * <td>false</td>
     * <td>false</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>false</td>
     * <td>true</td>
     * <td>false</td>
     * <td>false</td>
     * </tr>
     * <tr>
     * <td>X</td>
     * <td>false</td>
     * <td>false</td>
     * <td>false</td>
     * <td>false</td>
     * </tr>
     * <tr>
     * <td>Z</td>
     * <td>false</td>
     * <td>false</td>
     * <td>false</td>
     * <td>false</td>
     * </tr>
     * </table>
     *
     * @param obj Object to compare against
     * @return true if and only if obj is a BitVectorBuffer, BitVector or a Bit
     *         and a bitwise comparison using the above table results in all
     *         true values.
     */
    @Override
    public boolean equals(Object obj)
    {
        BitVectorBuffer buf = null;
        if (this == obj)
        {
            // maybe we get lucky.
            return true;
        }
        if (!(obj instanceof BitVector || obj instanceof BitVectorBuffer || obj instanceof Bit))
        {
            return false;
        }

        if (obj instanceof BitVector)
        {
            buf = ((BitVector) obj).myBuffer;
        }
        else if (obj instanceof Bit)
        {
            buf = new BitVectorBuffer((Bit) obj);
        }
        else
        {
            buf = (BitVectorBuffer) obj;
        }

        if (this.containsXZ() || buf.containsXZ()) return false;

        int maxUnits = Math.max(this.getLastEntry(), buf.getLastEntry());

        for (int i = 0; i < maxUnits; ++i)
        {
            int val1 = (i >= this.getLastEntry()) ? 0 : myValues[i];
            int val2 = (i >= buf.getLastEntry()) ? 0 : buf.myValues[i];
            if (val1 != val2)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a BitVectorBuffer in which a given bit is 1 if, for that
     * particular bit position, equals(vect) would return true. Otherwise, the
     * bit is set to 0.
     * <P>
     * Example:
     * <code>16'b0000_1111_xxxx_zzzz.equalsMask(16'b0x00_1zz1_10xx_zz1z)</code>
     * results in a BitVectorBuffer of: <code>16'b1011100100000000</code>.
     * <P>
     * If vect.length() > this.length(), this BitVectorBuffer is zero-extended
     * to vect.length().
     *
     * @param vect the BitVector to compare against.
     * @return a BitVectorBuffer with the property described above.
     */
    public BitVectorBuffer equalsMask(BitVector vect)
    {
        return equalsMask(vect.myBuffer);
    }

    /**
     * See {@link #equalsMask(BitVector) equalsMask(BitVector)}
     *
     * @param buf the BitVectorBuffer to compare against
     * @return a BitVectorBuffer with the property described in
     *         {@link #equalsMask(BitVector) equalsMask(BitVector)}
     */
    private BitVectorBuffer equalsMask(BitVectorBuffer buf)
    {
        int maxLength = Math.max(buf.length(), this.length());

        BitVectorBuffer newbuf = new BitVectorBuffer(maxLength);
        for (int i = 0; i < newbuf.getLastEntry(); i++)
        {
            int val1 = 0, val2 = 0, xz1 = 0, xz2 = 0;
            if (i < this.getLastEntry())
            {
                val1 = this.myValues[i];
                xz1 = this.myXzMask[i];
            }
            if (i < buf.getLastEntry())
            {
                val2 = buf.myValues[i];
                xz2 = buf.myXzMask[i];
            }

            newbuf.myValues[i] = ~(val1 ^ val2) & ~(xz1 | xz2);
            newbuf.myXzMask[i] = 0;
        }

        return newbuf;
    }

    /**
     * Returns a BitVectorBuffer in which a given bit is 1 if, for that
     * particular bit position, equalsExact(vect) would return true. Otherwise,
     * the bit is set to 0.
     * <P>
     * Example:
     * <code>16'b0000_1111_xxxx_zzzz.equalsExactMask(16'b0x00_1zz1_10xx_zz1z)</code>
     * results in a BitVectorBuffer of: <code>16'b1011100100111101</code>.
     * <P>
     * If vect.length() > this.length(), this BitVectorBuffer is zero-extended
     * to vect.length().
     *
     * @param vect the BitVector to compare against.
     * @return a BitVectorBuffer with the property described above.
     */
    public BitVectorBuffer equalsExactMask(BitVector vect)
    {
        return equalsExactMask(vect.myBuffer);
    }

    /**
     * See {@link #equalsExactMask(BitVector) equalsExactMask(BitVector)}
     *
     * @param buf the BitVectorBuffer to compare against
     * @return a BitVectorBuffer with the property described in
     *         {@link #equalsExactMask(BitVector) equalsExactMask(BitVector)}
     */
    private BitVectorBuffer equalsExactMask(BitVectorBuffer buf)
    {
        int maxLength = Math.max(buf.length(), this.length());

        BitVectorBuffer newbuf = new BitVectorBuffer(maxLength);
        for (int i = 0; i < newbuf.getLastEntry(); i++)
        {
            int val1 = 0, val2 = 0, xz1 = 0, xz2 = 0;
            if (i < this.getLastEntry())
            {
                val1 = this.myValues[i];
                xz1 = this.myXzMask[i];
            }
            if (i < buf.getLastEntry())
            {
                val2 = buf.myValues[i];
                xz2 = buf.myXzMask[i];
            }
            newbuf.myValues[i] = (~(val1 ^ val2)) & (~(xz1 ^ xz2));
            newbuf.myXzMask[i] = 0;
        }

        return newbuf;
    }

    /**
     * Compares the BitVector represented by vect to this BitVectorBuffer
     * (equivalent to Verilog ===).
     * <P>
     * Comparison Table: <table border=1>
     * <tr>
     * <td>===</td>
     * <td>0</td>
     * <td>1</td>
     * <td>X</td>
     * <td>Z</td>
     * </tr>
     * <P>
     * <tr>
     * <td>0</td>
     * <td>true</td>
     * <td>false</td>
     * <td>false</td>
     * <td>false</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>false</td>
     * <td>true</td>
     * <td>false</td>
     * <td>false</td>
     * </tr>
     * <tr>
     * <td>X</td>
     * <td>false</td>
     * <td>false</td>
     * <td>true</td>
     * <td>false</td>
     * </tr>
     * <tr>
     * <td>Z</td>
     * <td>false</td>
     * <td>false</td>
     * <td>false</td>
     * <td>true</td>
     * </tr>
     * </table>
     *
     * @param vect BitVector to compare against
     * @return true if and only if a bitwise comparison of vect using the above
     *         table results results in all true values. Note that for
     *         BitVectors of differing lengths, the shorter BitVector is
     *         zero-extended.
     */
    public boolean equalsExact(BitVector vect)
    {
        return equalsExact(vect.myBuffer);
    }

    /**
     * See {@link #equalsExact(BitVector) equalsExact(BitVector)}
     *
     * @param buf the BitVectorBuffer to compare against
     * @return a BitVectorBuffer with the property described in
     *         {@link #equalsExact(BitVector) equalsExact(BitVector)}
     */
    private boolean equalsExact(BitVectorBuffer buf)
    {
        int maxUnits = Math.max(this.getLastEntry(), buf.getLastEntry());

        for (int i = 0; i < maxUnits; ++i)
        {
            int val1 = 0, val2 = 0, xz1 = 0, xz2 = 0;
            if (i < this.getLastEntry())
            {
                val1 = this.myValues[i];
                xz1 = this.myXzMask[i];
            }
            if (i < buf.getLastEntry())
            {
                val2 = buf.myValues[i];
                xz2 = buf.myXzMask[i];
            }

            if (val1 != val2 || xz1 != xz2)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares the BitVector represented by vect to this BitVectorBuffer
     * (equivalent to Verilog =?=).
     * <P>
     * Comparison Table: <table border=1>
     * <tr>
     * <td>=?=</td>
     * <td>0</td>
     * <td>1</td>
     * <td>X</td>
     * <td>Z</td>
     * </tr>
     * <P>
     * <tr>
     * <td>0</td>
     * <td>true</td>
     * <td>false</td>
     * <td>true</td>
     * <td>true</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>false</td>
     * <td>true</td>
     * <td>true</td>
     * <td>true</td>
     * </tr>
     * <tr>
     * <td>X</td>
     * <td>true</td>
     * <td>true</td>
     * <td>true</td>
     * <td>true</td>
     * </tr>
     * <tr>
     * <td>Z</td>
     * <td>true</td>
     * <td>true</td>
     * <td>true</td>
     * <td>true</td>
     * </tr>
     * </table>
     *
     * @param vect BitVector to compare against
     * @return true if and only if a bitwise comparison of vect using the above
     *         table results results in all true values. Note that for
     *         BitVectors of differing lengths, the shorter BitVector is
     *         zero-extended.
     */
    public boolean equalsWild(BitVector vect)
    {
        return equalsWild(vect.myBuffer);
    }

    /**
     * See {@link #equalsWild(BitVector) equalsWild(BitVector)}
     *
     * @param buf the BitVectorBuffer to compare against
     * @return a BitVectorBuffer with the property described in
     *         {@link #equalsWild(BitVector) equalsWild(BitVector)}
     */
    private boolean equalsWild(BitVectorBuffer buf)
    {
        int maxUnits = Math.max(this.getLastEntry(), buf.getLastEntry());
        for (int i = 0; i < maxUnits; ++i)
        {
            int val1 = 0, val2 = 0, xz1 = 0, xz2 = 0;
            if (i < this.getLastEntry())
            {
                val1 = this.myValues[i];
                xz1 = this.myXzMask[i];
            }
            if (i < buf.getLastEntry())
            {
                val2 = buf.myValues[i];
                xz2 = buf.myXzMask[i];
            }

            int tmpMask = xz1 | xz2;
            int tmpResultA = val1 | tmpMask;
            int tmpResultB = val2 | tmpMask;

            if (tmpResultA != tmpResultB)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a BitVectorBuffer in which a given bit is 1 if, for that
     * particular bit position, equalsWild(vect) would return true. Otherwise,
     * the bit is set to 0.
     * <P>
     * Example:
     * <code>16'b0000_1111_xxxx_zzzz.equalsWildMask(16'b0x00_1zz1_10xx_zz1z)</code>
     * results in a BitVectorBuffer of: <code>16'b1111111111111111</code>.
     * <P>
     * If vect.length() > this.length(), this BitVectorBuffer is zero-extended
     * to vect.length().
     *
     * @param vect the BitVector to compare against.
     * @return a BitVectorBuffer with the property described above.
     */
    public BitVectorBuffer equalsWildMask(BitVector vect)
    {
        return equalsWildMask(vect.myBuffer);
    }

    /**
     * See {@link #equalsWildMask(BitVector) equalsWildMask(BitVector)}
     *
     * @param buf the BitVectorBuffer to compare against
     * @return a BitVectorBuffer with the property described in
     *         {@link #equalsWildMask(BitVector) equalsWildMask(BitVector)}
     */
    private BitVectorBuffer equalsWildMask(BitVectorBuffer buf)
    {
        int maxLength = Math.max(buf.length(), this.length());

        BitVectorBuffer newbuf = new BitVectorBuffer(maxLength);
        for (int i = 0; i < newbuf.getLastEntry(); i++)
        {
            int val1 = 0, val2 = 0, xz1 = 0, xz2 = 0;
            if (i < this.getLastEntry())
            {
                val1 = this.myValues[i];
                xz1 = this.myXzMask[i];
            }
            if (i < buf.getLastEntry())
            {
                val2 = buf.myValues[i];
                xz2 = buf.myXzMask[i];
            }

            newbuf.myValues[i] = (~(val1 ^ val2)) | (xz1 | xz2);
            newbuf.myXzMask[i] = 0;
        }

        return newbuf;
    }

    /**
     * Returns a hash code value for the BitVectorBuffer.
     *
     * @return the hash code
     */
    @Override
    public int hashCode()
    {
        int result = 17;
        result = 37 * result + myLength;
        for (int i = 0; i < myValues.length; i++)
        {
            result = 37 * result + myValues[i];
        }
        for (int i = 0; i < myXzMask.length; i++)
        {
            result = 37 * result + myXzMask[i];
        }
        return result;
    }

    /**
     * Performs a bitwise AND function of this BitVectorBuffer and vect and
     * places the results in this BitVectorBuffer. If vect.length() >
     * this.length(), this BitVectorBuffer will be lengthened and zero-extended.
     *
     * @param vect the BitVector with which to AND.
     * @return this & _vect
     */
    public BitVectorBuffer and(BitVector vect)
    {
        return and(vect.myBuffer);
    }

    /**
     * See {@link #and(BitVector) and(BitVector)}
     *
     * @param buf the BitVectorBuffer to compare against
     * @return a BitVectorBuffer with the property described in
     *         {@link #and(BitVector) and(BitVector)}
     */
    private BitVectorBuffer and(BitVectorBuffer buf)
    {
        int i;
        int tmpResult;
        int forceZero;
        int tmpMask;

        if (buf.length() > length())
        {
            setLength(buf.length());
        }

        for (i = 0; i < getLastEntry(); i++)
        {
            int val1 = 0, val2 = 0, xz1 = 0, xz2 = 0;
            if (i < this.getLastEntry())
            {
                val1 = this.myValues[i];
                xz1 = this.myXzMask[i];
            }
            if (i < buf.getLastEntry())
            {
                val2 = buf.myValues[i];
                xz2 = buf.myXzMask[i];
            }

            forceZero = ((~val1 & ~xz1) | (~val2 & ~xz2));

            tmpResult = val1 & val2;
            tmpMask = xz1 | xz2;
            tmpResult |= tmpMask; // N & [XZ] == X
            tmpMask &= ~forceZero; // 0 & [01XZ] == 0
            tmpResult &= ~forceZero;
            myValues[i] = tmpResult;
            myXzMask[i] = tmpMask;
        }
        return this;
    }

    /**
     * Performs a bitwise AND function of this BitVectorBuffer and NOT vect and
     * places the results in this BitVectorBuffer. If vect.length() >
     * this.length(), this BitVectorBuffer will be lengthened and zero-extended.
     *
     * @param vect the BitVector to NOT and with which to AND.
     * @return this & ~vect
     */
    public BitVectorBuffer andNot(BitVector vect)
    {
        return andNot(vect.myBuffer);
    }

    /**
     * See {@link #andNot(BitVector) andNot(BitVector)}
     *
     * @param buf the BitVectorBuffer to compare against
     * @return a BitVectorBuffer with the property described in
     *         {@link #andNot(BitVector) andNot(BitVector)}
     */
    private BitVectorBuffer andNot(BitVectorBuffer buf)
    {
        int i;
        int tmpResult;
        int forceZero;
        int tmpMask;
        int notBuf;

        if (buf.length() > length())
        {
            setLength(buf.length());
        }

        for (i = 0; i < getLastEntry(); i++)
        {
            int val1 = 0, val2 = 0, xz1 = 0, xz2 = 0;
            if (i < this.getLastEntry())
            {
                val1 = this.myValues[i];
                xz1 = this.myXzMask[i];
            }
            if (i < buf.getLastEntry())
            {
                val2 = buf.myValues[i];
                xz2 = buf.myXzMask[i];
            }

            notBuf = ~val2;
            forceZero = ((~val1 & ~xz1) | (~notBuf & ~xz2));

            tmpResult = val1 & notBuf;
            tmpMask = xz1 | xz2;
            tmpResult |= tmpMask; // N & [XZ] == X
            tmpMask &= ~forceZero; // 0 & [01XZ] == 0
            tmpResult &= ~forceZero;
            myValues[i] = tmpResult;
            myXzMask[i] = tmpMask;
        }
        return this;
    }

    /**
     * Performs a bitwise OR function of this BitVectorBuffer and vect and
     * places the results in this BitVectorBuffer. If vect.length() >
     * this.length(), this BitVectorBuffer will be lengthened and zero-extended.
     *
     * @param vect the BitVectorBuffer with which to OR.
     * @return this | vect
     */
    public BitVectorBuffer or(BitVector vect)
    {
        return or(vect.myBuffer);
    }

    /**
     * See {@link #or(BitVector) or(BitVector)}
     *
     * @param buf the BitVectorBuffer to compare against
     * @return a BitVectorBuffer with the property described in
     *         {@link #or(BitVector) or(BitVector)}
     */
    private BitVectorBuffer or(BitVectorBuffer buf)
    {
        int i;
        int tmpResult;
        int forceOne;
        int tmpMask;

        if (buf.length() > length())
        {
            setLength(buf.length());
        }

        for (i = 0; i < getLastEntry(); i++)
        {
            int val1 = 0, val2 = 0, xz1 = 0, xz2 = 0;
            if (i < this.getLastEntry())
            {
                val1 = this.myValues[i];
                xz1 = this.myXzMask[i];
            }
            if (i < buf.getLastEntry())
            {
                val2 = buf.myValues[i];
                xz2 = buf.myXzMask[i];
            }

            forceOne = ((val1 & ~xz1) | (val2 & ~xz2));

            tmpResult = val1 | val2;
            tmpMask = xz1 | xz2;
            tmpResult |= tmpMask; // N | [XZ] == X
            tmpMask &= ~forceOne; // 1 | [01XZ] == 1
            myValues[i] = tmpResult;
            myXzMask[i] = tmpMask;
        }
        return this;
    }

    /**
     * Performs a bitwise XOR function of this BitVectorBuffer and vect and
     * places the results in this BitVectorBuffer. If vect.length() >
     * this.length(), this BitVectorBuffer will be lengthened and zero-extended.
     *
     * @param vect the BitVectorBuffer with which to XOR.
     * @return this ^ vect
     */
    public BitVectorBuffer xor(BitVector vect)
    {
        return xor(vect.myBuffer);
    }

    /**
     * See {@link #xor(BitVector) xor(BitVector)}
     *
     * @param buf the BitVectorBuffer to compare against
     * @return a BitVectorBuffer with the property described in
     *         {@link #xor(BitVector) xor(BitVector)}
     */
    private BitVectorBuffer xor(BitVectorBuffer buf)
    {
        int i;
        int tmpResult;
        int tmpMask;

        if (buf.length() > length())
        {
            setLength(buf.length());
        }

        for (i = 0; i < getLastEntry(); i++)
        {
            int val1 = 0, val2 = 0, xz1 = 0, xz2 = 0;
            if (i < this.getLastEntry())
            {
                val1 = this.myValues[i];
                xz1 = this.myXzMask[i];
            }
            if (i < buf.getLastEntry())
            {
                val2 = buf.myValues[i];
                xz2 = buf.myXzMask[i];
            }

            tmpResult = val1 ^ val2;
            tmpMask = xz1 | xz2;
            tmpResult |= tmpMask; // N ^ [XZ] == X
            myValues[i] = tmpResult;
            myXzMask[i] = tmpMask;
        }
        return this;
    }

    /**
     * Performs a bitwise NOT function of this BitVectorBuffer and places the
     * results in this BitVectorBuffer.
     *
     * @return ~this
     */
    public BitVectorBuffer not()
    {
        int i;
        int tmpResult;
        for (i = 0; i < getLastEntry(); i++)
        {
            tmpResult = ~myValues[i];
            tmpResult |= myXzMask[i];
            myValues[i] = tmpResult;
        }
        return this;
    }

    /**
     * Performs an addition of this BitVectorBuffer and vect and places the
     * result in this BitVectorBuffer. The resulting length of this
     * BitVectorBuffer will have a length equal to
     * <code>Math.max(this.length(), vect.length())</code>.
     *
     * @param vect the BitVector with which to add.
     * @return this + vect
     */
    public BitVectorBuffer add(BitVector vect)
    {
        return addsub(vect.myBuffer, true);
    }

    /**
     * Performs a subtraction of vect from this BitVectorBuffer and places the
     * result in this BitVectorBuffer. The resulting length of this
     * BitVectorBuffer will have a length equal to
     * <code>Math.max(this.length(), vect.length())</code>.
     *
     * @param vect the BitVector with which to add.
     * @return this - vect
     */
    public BitVectorBuffer subtract(BitVector vect)
    {
        return addsub(vect.myBuffer, false);
    }

    /**
     * See {@link #add(BitVector) add(BitVector)}
     * {@link #subtract(BitVector) subtract(BitVector)}
     *
     * @param buf the BitVectorBuffer to add/subtract
     * @param add true if this method should perform addition, false if
     *            subtraction
     * @return a BitVectorBuffer with the property described in
     *         {@link #add(BitVector) add(BitVector)}or
     *         {@link #subtract(BitVector) subtract(BitVector)}, depending on
     *         add
     */
    private BitVectorBuffer addsub(BitVectorBuffer buf, boolean add)
    {
        int carry = 0;
        long result = 0;

        if (buf.length() > length())
        {
            setLength(buf.length());
        }

        // If either addend contains X or Z, all bits are
        // set to X.
        if (this.containsXZ() || buf.containsXZ())
        {
            this.fillBits(length() - 1, 0, Bit.X);
            return this;
        }

        for (int i = 0; i < getLastEntry(); i++)
        {
            long addend0 = (i < this.getLastEntry()) ? this.myValues[i] : 0;
            long addend1 = (i < buf.getLastEntry()) ? buf.myValues[i] : 0;
            addend0 &= 0xFFFFFFFFL;
            addend1 &= 0xFFFFFFFFL;
            if (add)
            {
                result = addend0 + addend1 + carry;
            }
            else
            {
                result = addend0 - addend1 - carry;
            }
            this.myValues[i] = (int) result;
            carry = (int) ((result >> BITS_PER_UNIT) & 1);
        }

        maskExtraLength();
        return this;
    }

    /**
     * Performs a multiplication of this BitVectorBuffer with vect and places
     * the result in this BitVectorBuffer. The resulting length of this
     * BitVectorBuffer will have a length equal to
     * <code>Math.max(this.length(), vect.length())</code>.
     *
     * @param vect the BitVector with which to add.
     * @return this * vect
     */
    public BitVectorBuffer multiply(BitVector vect)
    {
        return multiply(vect.myBuffer);
    }

    /**
     * See {@link #multiply(BitVector) multiply(BitVector)}
     *
     * @param buf the BitVectorBuffer to multiply
     * @return a BitVectorBuffer with the property described in
     *         {@link #multiply(BitVector) multiply(BitVector)}
     */
    private BitVectorBuffer multiply(BitVectorBuffer buf)
    {
        /*
         * some simple cases: 0 * n == 0 x * n == x z * n == z
         */

        if (buf.length() > length())
        {
            setLength(buf.length());
        }

        // If either addend contains X, all bits are set to X.
        // Else if either addend contains an Z, all bits are set to Z.
        if (this.containsXZ() || buf.containsXZ())
        {
            Bit fillBit = Bit.X;
            if (!this.containsX() && !buf.containsX())
            {
                // z * n == z
                fillBit = Bit.Z;
            }
            this.fillBits(length() - 1, 0, fillBit);
            return this;
        }
        // If either multiplicand is zero, result is always zero.
        else if (this.isZero() || buf.isZero())
        {
            this.fillBits(length() - 1, 0, Bit.ZERO);
            return this;
        }
        if (this.length() <= 32 && buf.length() <= 32)
        {
            long mult0 = this.longValue() & ((1L << BITS_PER_UNIT) - 1);
            long mult1 = buf.longValue() & ((1L << BITS_PER_UNIT) - 1);

            long result = mult0 * mult1;

            this.fillBits(length() - 1, 0, Bit.ZERO);
            if (this.getLastEntry() > 1)
            {
                this.myValues[1] = (int) (result >>> 32);
            }
            this.myValues[0] = (int) (result & ~0);
            maskExtraLength();
            return this;
        }

        // If either operand is > 32 bits, we use BigInteger
        BigInteger mult0 = new BigInteger(this.getBytes(false, true));
        BigInteger mult1 = new BigInteger(buf.getBytes(false, true));
        BigInteger result = mult0.multiply(mult1);
        BitVectorBuffer q = new BitVectorBuffer(result.toByteArray());
        q.setLengthHigh(this.length());
        this.assign(q, false);
        maskExtraLength();
        return this;
    }

    /**
     * Calculates this BitVectorBuffer modulo vect and places the result in this
     * BitVectorBuffer. The resulting length of this BitVectorBuffer will have a
     * length equal to <code>Math.max(this.length(), vect.length())</code>.
     *
     * @param vect the BitVector with which to add.
     * @return this % vect
     */

    public BitVectorBuffer mod(BitVector vect)
    {
        return divmod(vect.myBuffer, false);
    }

    /**
     * Performs a division of vect into this BitVectorBufferand places the
     * result in this BitVectorBuffer. The resulting length of this
     * BitVectorBuffer will have a length equal to
     * <code>Math.max(this.length(), vect.length())</code>.
     *
     * @param vect the BitVector with which to add.
     * @return this / vect
     */

    public BitVectorBuffer divide(BitVector vect)
    {
        return divmod(vect.myBuffer, true);
    }

    /**
     * See {@link #divide(BitVector) divide(BitVector)}
     * {@link #mod(BitVector) modulo(BitVector)}
     *
     * @param buf the BitVectorBuffer to divide/modulo
     * @param doDiv true if this method should perform division, false if modulo
     * @return a BitVectorBuffer with the property described in
     *         {@link #divide(BitVector) divide(BitVector)}or
     *         {@link #mod(BitVector) modulo(BitVector)}, depending on doDiv
     */
    private BitVectorBuffer divmod(BitVectorBuffer buf, boolean doDiv)
    {

        if (buf.isZero())
        {
            throw new ArithmeticException("Divide by zero");
        }

        if (buf.length() > length())
        {
            setLength(buf.length());
        }

        // If either addend contains X, all bits are set to X.
        // Else if either addend contains an Z, all bits are set to Z.
        if (this.containsXZ() || buf.containsXZ())
        {
            Bit fillBit = Bit.X;
            if (!this.containsX() && !buf.containsX())
            {
                // z * n == z
                fillBit = Bit.Z;
            }
            this.fillBits(length() - 1, 0, fillBit);
            return this;
        }
        // If dividend is zero, result is always zero.
        else if (this.isZero())
        {
            this.fillBits(length() - 1, 0, Bit.ZERO);
            return this;
        }

        // Check if we can use longs to do the division (we can't optimize
        // values with lengths from 33-64 bits due to the sign bit of the long)
        if (this.length() <= 32 && buf.length() <= 32)
        {
            long dividend = this.longValue() & ((1L << BITS_PER_UNIT) - 1);
            long divisor = buf.longValue() & ((1L << BITS_PER_UNIT) - 1);

            long quotient = dividend / divisor;
            long mod = dividend % divisor;

            this.fillBits(length() - 1, 0, Bit.ZERO);
            this.myValues[0] = (int) ((doDiv ? quotient : mod) & ~0);
            maskExtraLength();

            return this;
        }

        // If either operand is > 32 bits, we use BigInteger
        BigInteger dividend = new BigInteger(this.getBytes(false, true));
        BigInteger divisor = new BigInteger(buf.getBytes(false, true));

        BigInteger[] quotientAndRemainder = dividend
            .divideAndRemainder(divisor);
        BitVectorBuffer q = new BitVectorBuffer(quotientAndRemainder[doDiv ? 0
            : 1].toByteArray());
        q.setLengthHigh(this.length());
        this.assign(q, false);
        maskExtraLength();
        return this;

    }

    /**
     * Performs an arithmetic negation of this BitVectorBuffer and places the
     * result in this BitVectorBuffer.
     *
     * @return -(this)
     */
    public BitVectorBuffer negate()
    {
        BitVectorBuffer buf;
        if (containsXZ())
        {
            this.fillBits(length() - 1, 0, Bit.X);
            return this;
        }
        for (int i = 0; i < getLastEntry(); i++)
        {
            myValues[i] = ~myValues[i];
        }

        buf = new BitVectorBuffer(length(), 1);
        this.addsub(buf, true);

        maskExtraLength();
        return this;
    }

    /**
     * Performs a bit reversal this BitVectorBuffer and places the result in
     * this BitVectorBuffer.
     *
     * @return &gt;&lt;this
     */
    public BitVectorBuffer reverse()
    {
        int toWord, toBit;
        int fromWord, fromBit;
        int topBit;
        int[] tmpVals = new int[myValues.length];
        int[] tmpXz = new int[myXzMask.length];

        topBit = length() - 1;
        for (int i = 0; i < length(); i++, topBit--)
        {
            toWord = i / BITS_PER_UNIT;
            toBit = i % BITS_PER_UNIT;
            fromWord = topBit / BITS_PER_UNIT;
            fromBit = topBit % BITS_PER_UNIT;

            tmpVals[toWord] |= ((myValues[fromWord] >> fromBit) & 1) << toBit;
            tmpXz[toWord] |= ((myXzMask[fromWord] >> fromBit) & 1) << toBit;

        }

        this.myValues = tmpVals;
        this.myXzMask = tmpXz;
        return this;
    }

    /**
     * Performs a reductive and on this BitVectorBuffer and returns the result.
     * If all bits in this BitVectorBuffer are Bit.ONE, the result will be
     * Bit.ONE, otherwise, the result will be Bit.ZERO. TODO: table of 0/1/X/Z
     * values
     *
     * @return &this
     */
    public Bit reductiveAnd()
    {
        if (containsXZ())
        {
            return Bit.X;
        }
        for (int i = 0; i < getLastEntry(); i++)
        {
            final int lengthModBPU = length() % BITS_PER_UNIT;
            if (i == getLastEntry() - 1 && lengthModBPU != 0)
            {
                // might not use all bits in the last entry
                int mask = ((1 << lengthModBPU) - 1);
                if ((myValues[i] & mask) != mask)
                {
                    return Bit.ZERO;
                }
            }
            else
            {
                if (myValues[i] != ~0)
                {
                    return Bit.ZERO;
                }
            }
        }
        return Bit.ONE;
    }

    /**
     * Performs a reductive and on this BitVectorBuffer and returns the result.
     * If any bit in this BitVectorBuffer is Bit.ONE, the result will be
     * Bit.ONE, otherwise, the result will be Bit.ZERO. TODO: table of 0/1/X/Z
     * values
     *
     * @return &this
     */
    public Bit reductiveOr()
    {
        if (containsXZ())
        {
            return Bit.X;
        }
        if (isZero())
        {
            return Bit.ZERO;
        }
        return Bit.ONE;
    }

    /**
     * Performs a reductive xor on this BitVectorBuffer and returns the result.
     * If an odd number of bits in this BitVectorBuffer are Bit.ONE, the result
     * will be Bit.ONE, otherwise, the result will be Bit.ZERO. TODO: table of
     * 0/1/X/Z values
     *
     * @return &this
     */
    public Bit reductiveXor()
    {
        int xor = 0;
        if (containsXZ())
        {
            return Bit.X;
        }

        for (int i = 0; i < getLastEntry(); i++)
        {
            for (int bit = 0; bit < BITS_PER_UNIT; bit++)
            {
                xor ^= ((myValues[i] >> bit) & 1);
                if (i == getLastEntry() - 1)
                {
                    // might not use all bits in the last entry
                    if (((i * BITS_PER_UNIT) + bit) > length())
                    {
                        break;
                    }
                }
            }
        }

        assert (xor == 1 || xor == 0);
        return (xor == 1) ? Bit.ONE : Bit.ZERO;
    }

    /**
     * Method to multiplex multiple BitVectorBuffers onto one BitVectorBuffer.
     * This is useful for BDD variable ordering.
     * <P>
     * Example: if vectors holds {X, Y, Z} and X.length() == 4, Y.length() == 6,
     * and Z.length() == 2,
     * <P>
     * the resulting BitVectorBuffer will be of the format: XXXXXYXYZXYZ
     *
     * @param bufs the vectors to zip
     * @return a new BitVectorBuffer comprising the zipped vectors
     */
    public BitVectorBuffer zip(BitVectorBuffer[] bufs)
    {
        int totalBits = 0;
        for (int i = 0; i < bufs.length; i++)
        {
            totalBits += bufs[i].length();
        }
        BitVectorBuffer result = new BitVectorBuffer(totalBits);

        // Zip it up
        // if my lengths are: { 4, 6, 2} i should be of the format
        // XXXXXYXYZXYZ
        // where X represents a bit in the [4] vector, Y in the [6] vector
        // and Z in the [2] vector
        for (int i = 0; i < bufs.length; i++)
        {
            int length = bufs[i].length();
            System.out.println("zipping " + length);
            int gblIdx = i;
            for (int j = 0; j < length; j++)
            {
                int validBufs = 0;
                result.setBit(gblIdx, bufs[i].getBit(j));
                // calculate valid bufs on idx i
                for (int k = 0; k < bufs.length; k++)
                {
                    // FIXME: insert zip algorithm
                }

                gblIdx += validBufs;
            }
        }
        return result;
    }

    /**
     * Performs the opposite of
     * {@link #zip(BitVectorBuffer[]) zip(BitVectorBuffer[])}, placing each
     * unzipped BitVectorBuffer in the resulting array.
     *
     * @param bufLengths an array of the length of each BitVectorBuffer to be
     *            unzipped
     * @return an array of unzipped BitVectorBuffers.
     */
    public BitVectorBuffer[] unzip(int[] bufLengths)
    {
        int totalBits = 0;
        for (int i = 0; i < bufLengths.length; i++)
        {
            totalBits += bufLengths[i];
        }
        if (totalBits != length())
        {
            throw new IllegalArgumentException("total number of bits in"
                + "bufLengths (" + totalBits + ") doesn't equal length() "
                + "( " + length() + ")");
        }

        BitVectorBuffer[] results = new BitVectorBuffer[bufLengths.length];

        // Make the new vectors
        for (int i = 0; i < bufLengths.length; i++)
        {
            results[i] = new BitVectorBuffer(bufLengths[i]);
        }

        // And unzipper
        // if my lengths are: { 4, 6, 2} i should be of the format
        // XXXXXYXYZXYZ
        // where X represents a bit in the [4] vector, Y in the [6] vector
        // and Z in the [2] vector
        for (int i = 0; i < bufLengths.length; i++)
        {
            int length = bufLengths[i];
            int gblIdx = i;
            for (int j = 0; j < length; j++)
            {
                results[i].setBit(j, this.getBit(gblIdx));
                gblIdx += bufLengths.length;
            }
        }
        return results;
    }

    /**
     * Performs a concatenation of vect onto the end of this BitVectorBuffer.
     * This effectively increases the length of this BitVectorBuffer by the
     * length of vect.
     *
     * @param vect The BitVector to concatenate.
     * @return {this, vect}
     */
    public BitVectorBuffer concat(BitVector vect)
    {
        return concat(vect.myBuffer);
    }

    /**
     * See {@link #concat(BitVector) concat(BitVector)}
     *
     * @param buf the BitVectorBuffer concatenate
     * @return a BitVectorBuffer with the property described in
     *         {@link #concat(BitVector) concat(BitVector)}
     */
    private BitVectorBuffer concat(BitVectorBuffer buf)
    {
        BitVectorBuffer newbuf = new BitVectorBuffer(length() + buf.length(), 0);
        newbuf.setBits(newbuf.length() - 1, buf.length(), this);
        newbuf.setBits(buf.length() - 1, 0, buf);
        this.assign(newbuf, true);
        return this;
    }

    /**
     * Returns a new BitVectorBuffer which is a concatenation of the specified
     * Objects. The types of the objects in <code>values</code> must be supported
     * by {@link ValueConverter#toBitVector(Object)}.
     *
     * @param values the Objects to concatenate, specified with the most
     *      significant bits in the first Object, and so on
     * @return a new BitVectorBuffer which is the concatenation of the specified
     *      Objects
     */
    public static BitVectorBuffer concat(Object... values)
    {
        if (values.length <= 0)
        {
            throw new IllegalArgumentException(
                "At least 1 value must be specified");
        }

        // calculate total length of result
        int total = 0;
        BitVectorBuffer[] buffers = new BitVectorBuffer[values.length];
        for (int i = 0; i < values.length; ++i)
        {
            buffers[i] = ValueConverter.toBitVector(values[i]).myBuffer;
            total += buffers[i].length();
        }

        // write individual bit vectors into buffer, starting with MSB
        final BitVectorBuffer result = new BitVectorBuffer(total);
        int pos = total;

        for (final BitVectorBuffer buf : buffers)
        {
            final int len = buf.length();
            result.setBits(pos - 1, pos - len, buf);
            pos -= len;
        }

        return result;
    }

    /**
     * Returns a new BitVectorBuffer that is the result of replicating this
     * BitVectorBuffer the specified number of times.
     *
     * @param count the number of times to replicate this BitVectorBuffer
     * @return a new BitVectorBuffer containing this BitVectorBuffer replicated
     *      the specified number of times
     */
    public BitVectorBuffer replicate(int count)
    {
        // calculate total length of result
        final int len = this.length();
        final int total = len * count;

        // replicate bit vector into buffer, starting with MSB
        final BitVectorBuffer result = new BitVectorBuffer(total);
        int pos = total;
        for (int i = 0; i < count; ++i)
        {
            result.setBits(pos - 1, pos - len, this);
            pos -= len;
        }
        return result;
    }

    /**
     * Performs a left shift of this BitVectorBuffer. This method will not
     * increase the size of the BitVectorBuffer and bits shifted out will be
     * lost.
     *
     * @param numBits Number of bits to shift to the left.
     * @return <code>this &lt;&lt; numBits</code>
     */
    public BitVectorBuffer shiftLeft(int numBits)
    {
        int bitsLeft = numBits;
        int firstIdx = getLastEntry() - 1;
        int i, dest;
        int carryBits, carryXZ;

        if (bitsLeft == 0)
        {
            return this;
        }

        int[] newInts = new int[getLastEntry()];
        int[] newXz = new int[getLastEntry()];

        // we'll effectively delete all array entries from 0 through
        // (firstIdx - 1) if we're asked to shift > BITS_PER_UNIT bits
        while (bitsLeft >= BITS_PER_UNIT)
        {
            firstIdx--;
            bitsLeft -= BITS_PER_UNIT;
        }

        for (i = firstIdx, dest = (getLastEntry() - 1); i >= 0; i--, dest--)
        {
            // these casts/masking of carry(Bits|XZ) is because the java spec
            // says that when right shifting an int value, only bits 4:0 of the
            // rhs are used. when shifting a long, bits 5:0 are used. this
            // affects
            // us in the bitsLeft == 0 case.
            carryBits = (int) ((long) myValues[i] >>> (BITS_PER_UNIT - bitsLeft));
            carryXZ = (int) ((long) myXzMask[i] >>> (BITS_PER_UNIT - bitsLeft));

            carryBits &= ((1L << bitsLeft) - 1);
            carryXZ &= ((1L << bitsLeft) - 1);

            newInts[dest] = myValues[i] << bitsLeft;
            newXz[dest] = myXzMask[i] << bitsLeft;
            if (dest != (getLastEntry() - 1))
            {
                newInts[dest + 1] |= carryBits;
                newXz[dest + 1] |= carryXZ;
            }
        }

        myValues = newInts;
        myXzMask = newXz;
        maskExtraLength();
        return this;
    }

    /**
     * Performs a right shift of this BitVectorBuffer and places the result in
     * this BitVectorBuffer. The resulting BitVector will have the same length
     * as this BitVector. Zeros will be shifted into the high bits of the
     * resulting BitVector.
     *
     * @param numBits Number of bits to shift to the right.
     * @return <code>this >>> numBits</code>
     */
    public BitVectorBuffer shiftRight(int numBits)
    {
        int bitsLeft = numBits;
        int firstIdx = 0;
        int i, dest;
        int carryBits, carryXZ;

        if (bitsLeft == 0)
        {
            return this;
        }

        int[] newInts = new int[getLastEntry()];
        int[] newXz = new int[getLastEntry()];

        // we'll effectively delete all array entries from 0 through
        // (firstIdx - 1) if we're asked to shift > BITS_PER_UNIT bits
        while (bitsLeft >= BITS_PER_UNIT)
        {
            firstIdx++;
            bitsLeft -= BITS_PER_UNIT;
        }

        for (i = firstIdx, dest = 0; i < getLastEntry(); i++, dest++)
        {
            carryBits = ((1 << bitsLeft) - 1) & myValues[i];
            carryXZ = ((1 << bitsLeft) - 1) & myXzMask[i];
            newInts[dest] = myValues[i] >>> bitsLeft;
            newXz[dest] = myXzMask[i] >>> bitsLeft;
            if (dest != 0)
            {
                assert (bitsLeft != 0 || (carryBits == 0 && carryXZ == 0));
                newInts[dest - 1] |= (carryBits << (BITS_PER_UNIT - bitsLeft));
                newXz[dest - 1] |= (carryXZ << (BITS_PER_UNIT - bitsLeft));
            }
        }

        myValues = newInts;
        myXzMask = newXz;
        return this;
    }

    /**
     * Returns the value of the bit in bit position bitPos as a {@link Bit Bit}.
     *
     * @param bitPos the bit position to access
     * @return this[bitPos]
     * @throws IllegalArgumentException bitPos is not between 0 and the size of
     *             this BitVectorBuffer
     */
    public Bit getBit(int bitPos)
    {
        int intVal = bitPos / BITS_PER_UNIT;
        int shiftVal = bitPos % BITS_PER_UNIT;
        int val = 0;

        if (bitPos < 0 || bitPos >= length())
        {
            throw new IllegalArgumentException("_bitPos [" + bitPos
                + "] is not in the range" + "from 0:" + length());
        }

        val = (myValues[intVal] >> shiftVal) & 1;
        val |= ((myXzMask[intVal] >> shiftVal) & 1) << 1;

        switch (val)
        {
        case 0:
            return Bit.ZERO;
        case 1:
            return Bit.ONE;
        case 2:
            return Bit.Z;
        case 3:
            return Bit.X;
        default:
            throw new UnknownError("Internal error");
        }
    }

    /**
     * Returns a new BitVectorBuffer comprised of the bits specified in the
     * given BitRange.
     *
     * @param range the BitRange representing the desired bitslice
     * @return a BitVector comprised of bits in <code>range</code> of this
     *      BitVectorBuffer
     * @throws IllegalArgumentException if <code>range</code> specifies bits
     *      outside of this BitVectorBuffer
     */
    public BitVectorBuffer getBits(BitRange range)
    {
        return getBits(range.high, range.low);
    }

    /**
     * Returns a new BitVectorBuffer comprised of bits [hiBit:loBit] of this
     * BitVector.
     *
     * @param hiBit the most significant bit of the desired bitslice.
     * @param loBit the least significan bit of the desired bitslice.
     * @return a BitVector comprised of bits [hiBit:loBit] of this BitVectorBuffer.
     * @throws IllegalArgumentException hiBit < loBit, or hiBit >= the length of
     *             this BitVectorBuffer
     */
    // TODO: should getBits be implemented natively? (i.e. not in terms of
    // shifts)? since buf has a larger number of ints in myValues than it needs,
    // it feels hacky. that means it probably is. FOLLOWUP: PERF
    public BitVectorBuffer getBits(int hiBit, int loBit)
    {
        if (hiBit < loBit)
        {
            throw new IllegalArgumentException("MSB < LSB: " + "getBits("
                + hiBit + ", " + loBit + ")");
        }
        else if (hiBit >= length())
        {
            throw new IllegalArgumentException(
                "Trying to access out-of-range bit [" + String.valueOf(hiBit)
                    + "] of a " + String.valueOf(length()) + "-bit field");
        }

        BitVectorBuffer buf = new BitVectorBuffer(this);
        buf.shiftRight(loBit);
        buf.setLength(hiBit - loBit + 1);
        return buf;
    }

    /**
     * Return the number of bits in this BitVectorBuffer which have value bit.
     * @param bit The value for which to check
     * @return the number of bits in this BitVectorBuffer which have value bit.
     */
    public int getBitCount(Bit bit)
    {
        return getBitCount(length() - 1, 0, bit);
    }

    /**
     * Return the number of bits in the given range of this BitVectorBuffer
     * which have the value of the specified Bit.
     *
     * @param range the BitRange in which to check
     * @param bit the value for which to check
     * @return the number of bits in the given range of this BitVectorBuffer
     *      which have value <code>bit</code>
     * @throws IllegalArgumentException if <code>range</code> specifies bits
     *      outside of this BitVectorBuffer
     */
    public int getBitCount(BitRange range, Bit bit)
    {
        return getBitCount(range.high, range.low, bit);
    }

    /**
     * Return the number of bits in the given range of this BitVectorBuffer
     * which have the value of the specified Bit.
     * @param hiBit the high bit of the range to check
     * @param loBit the low bit of the range to check
     * @param bit the value for which to check
     * @return the number of bits in the given range of this BitVectorBuffer
     * which have value <code>bit</code>.
     * @throws IllegalArgumentException <code>hiBit < loBit</code>, or
     *      <code>hiBit >= this.length()</code>
     */
    public int getBitCount(int hiBit, int loBit, Bit bit)
    {
        if (hiBit < loBit)
        {
            throw new IllegalArgumentException("MSB < LSB: " + "getBits("
                + hiBit + ", " + loBit + ")");
        }
        else if (hiBit >= length())
        {
            throw new IllegalArgumentException(
                "Trying to access out-of-range bit [" + String.valueOf(hiBit)
                    + "] of a " + String.valueOf(length()) + "-bit field");
        }

        int startIdx = loBit / BITS_PER_UNIT;
        int stopIdx = hiBit / BITS_PER_UNIT;
        int startBit = loBit % BITS_PER_UNIT;
        int stopBit = hiBit % BITS_PER_UNIT;

        int matchID = bit.getID();
        int count = 0;

        for (int i = startIdx; i <= stopIdx; ++i)
        {
            int curVal = myValues[i];
            int curXz = myXzMask[i];
            int start = (i == startIdx) ? startBit : 0;
            int stop = (i == stopIdx) ? stopBit : (BITS_PER_UNIT - 1);

            for (int j = start; j <= stop; ++j)
            {
                int curID = 0;
                curID = (((curXz >>> j) & 1) << 1);
                curID |= ((curVal >>> j) & 1);
                if (curID == matchID)
                {
                    ++count;
                }
            }
        }

        return count;
    }

    /**
     * This BitVectorBuffer is updated such that:
     * <P>
     * <code>result[i] = (vect[i] == Bit.ZERO ? result[i] : Bit.X)</code>
     * <P>
     * If vect is not of the same length as this BitVectorBuffer, it will be
     * truncated or zero-extended as needed. However, the size of this
     * BitVectorBuffer will not be changed by this operation.
     *
     * @param vect BitVector containing a mask of 1's.
     * @return this BitVector with X's in any bit position where a 1 is present
     *         in vect.
     */
    public BitVectorBuffer setX(BitVector vect)
    {
        return setX(vect.myBuffer);
    }

    /**
     * See {@link #setX(BitVector) setX(BitVector)}
     *
     * @param buf the BitVectorBuffer containing the mask
     * @return a BitVectorBuffer with the property described in
     *         {@link #setX(BitVector) setX(BitVector)}
     */
    private BitVectorBuffer setX(BitVectorBuffer buf)
    {

        for (int i = 0; i < getLastEntry(); i++)
        {
            int val2 = 0, xz2 = 0;
            if (i < buf.getLastEntry())
            {
                val2 = buf.myValues[i];
                xz2 = buf.myXzMask[i];
            }

            int genX = val2 | (~val2 & xz2);
            myValues[i] |= genX;
            myXzMask[i] |= genX;
        }
        return this;
    }

    /**
     * This BitVectorBuffer is update such that:
     * <P>
     *
     * <pre><code>
     * if (vect[i] == Bit.ZERO)
     * {
     *     this[i] = this[i];
     * }
     * else if (vect[i] == Bit.ONE)
     * {
     *     this[i] = Bit.Z;
     * }
     * else
     * {
     *     this[i] = Bit.X;
     * }
     * </pre></code>
     * <P>
     * If vect is not of the same length as this BitVectorBuffer, it will be
     * truncated or zero-extended as needed. However, the size of this
     * BitVectorBuffer will not be changed by this operation.
     *
     * @param vect BitVector containing a mask of 1's.
     * @return this BitVectorBuffer with Z's in any bit position where a 1 is
     *         present in vect and X's in any bit position where an X or Z is
     *         present in vect
     */
    public BitVectorBuffer setZ(BitVector vect)
    {
        return setZ(vect.myBuffer);
    }

    /**
     * See {@link #setZ(BitVector) setZ(BitVector)}
     *
     * @param buf the BitVectorBuffer containing the mask
     * @return a BitVectorBuffer with the property described in
     *         {@link #setZ(BitVector) setZ(BitVector)}
     */
    private BitVectorBuffer setZ(BitVectorBuffer buf)
    {
        for (int i = 0; i < getLastEntry(); i++)
        {
            int val2 = 0, xz2 = 0;
            if (i < buf.getLastEntry())
            {
                val2 = buf.myValues[i];
                xz2 = buf.myXzMask[i];
            }

            int genZ = (val2 & ~xz2);
            int genX = xz2;
            myValues[i] |= genX;
            myValues[i] &= ~genZ;
            myXzMask[i] |= (genX | genZ);
        }
        return this;
    }

    /**
     * Returns a new BitVectorBuffer with a 1 in each bit position that is
     * occupied by an X in this BitVectorBuffer and a 0 in each bit position
     * that is occupied by a 0, 1, or Z.
     *
     * @return new BitVectorBuffer representing a mask of X values in this
     *         BitVectorBuffer.
     */
    public BitVectorBuffer getXMask()
    {
        return getXZMask(true);
    }

    /**
     * Returns a new BitVectorBuffer with a 1 in each bit position that is
     * occupied by an Z in this BitVectorBuffer and a 0 in each bit position
     * that is occupied by a 0, 1, or X.
     *
     * @return new BitVectorBuffer representing a mask of Z values in this
     *         BitVectorBuffer.
     */
    public BitVectorBuffer getZMask()
    {
        return getXZMask(false);
    }

    /**
     * See {@link #setX(BitVector) setX(BitVector)}and
     * {@link #setZ(BitVector) setZ(BitVector)}
     *
     * @param getX true if an Xmask is desired, false if a Zmask is desired.
     * @return a BitVectorBuffer with the property described in
     *         {@link #setX(BitVector) setX(BitVector)}or
     *         {@link #setZ(BitVector) setZ(BitVector)}as determined by getX
     */
    private BitVectorBuffer getXZMask(boolean getX)
    {
        BitVectorBuffer buf = new BitVectorBuffer(length(), 0);
        for (int i = 0; i < getLastEntry(); i++)
        {
            int xzMask = (getX ? this.myValues[i] : ~this.myValues[i]);
            buf.myValues[i] = (this.myXzMask[i] & xzMask);
        }
        return buf;
    }

    /**
     * Sets each bit in the specified BitRange of this BitVectorBuffer to the
     * specified Bit.
     *
     * @param range the BitRange of this BitVectorBuffer, in which all bits
     *      should be set to <code>bit</code>
     * @param bit the Bit with which to fill
     * @return this BitVectorBuffer with each bit in the specified BitRange
     *      set to <code>bit</code>
     * @throws IllegalArgumentException if <code>range</code> specifies bits
     *      outside of this BitVectorBuffer
     */
    public BitVectorBuffer fillBits(BitRange range, Bit bit)
    {
        return fillBits(range.high, range.low, bit);
    }

    /**
     * Sets each bit in hiBit:loBit of this BitVectorBuffer to the specified Bit.
     *
     * @param hiBit the most significant bit of the desired bitslice.
     * @param loBit the least significan bit of the desired bitslice.
     * @param bit the Bit with which to fill
     * @return this BitVectorBuffer with hiBit:loBit set to bit.
     * @throws IllegalArgumentException hiBit < loBit, or hiBit >= the length of
     *             this BitVector
     */
    public BitVectorBuffer fillBits(int hiBit, int loBit, Bit bit)
    {
        int fillValues;
        int fillXzMask;
        int highIdx;
        int lowIdx;
        int bitDiff;
        int mask;
        int id = bit.getID();
        if (hiBit < loBit)
        {
            throw new IllegalArgumentException("MSB < LSB in parameter list");
        }
        else if (hiBit >= length())
        {
            throw new IllegalArgumentException(
                "Trying to access out-of-range bit [" + String.valueOf(hiBit)
                    + "] of a " + String.valueOf(length()) + "-bit field");
        }

        fillValues = 0;
        fillXzMask = 0;
        if ((id & 1) == 1)
        {
            fillValues = ~0;
        }
        if ((id & 2) == 2)
        {
            fillXzMask = ~0;
        }

        highIdx = hiBit / BITS_PER_UNIT;
        lowIdx = loBit / BITS_PER_UNIT;
        if (highIdx == lowIdx)
        {
            bitDiff = hiBit - loBit + 1;
            mask = (int) (((1L << bitDiff) - 1L) << (loBit % BITS_PER_UNIT));
            myValues[highIdx] &= ~mask;
            myXzMask[highIdx] &= ~mask;
            myValues[highIdx] |= (fillValues & mask);
            myXzMask[highIdx] |= (fillXzMask & mask);
        }
        else
        {
            // fill the top entry
            bitDiff = hiBit - (highIdx * BITS_PER_UNIT) + 1;
            mask = (int) ((1L << bitDiff) - 1L);
            myValues[highIdx] &= ~mask;
            myXzMask[highIdx] &= ~mask;
            myValues[highIdx] |= (fillValues & mask);
            myXzMask[highIdx] |= (fillXzMask & mask);

            // fill the bottom entry
            bitDiff = ((lowIdx + 1) * BITS_PER_UNIT) - loBit;
            assert (bitDiff >= 0);
            mask = (int) (((1L << bitDiff) - 1L) << (BITS_PER_UNIT - bitDiff));
            myValues[lowIdx] &= ~mask;
            myXzMask[lowIdx] &= ~mask;
            myValues[lowIdx] |= (fillValues & mask);
            myXzMask[lowIdx] |= (fillXzMask & mask);

            // and fill everything in the middle
            for (int i = lowIdx + 1; i < highIdx; i++)
            {
                myValues[i] = fillValues;
                myXzMask[i] = fillXzMask;
            }
        }

        return this;
    }

    /**
     * Returns this BitVectorBuffer with the bit in bitPos set to the value
     * represented by bit.
     *
     * @param bitPos the bit position to access
     * @param bit the Bit enumeration to which this[bitPos] should be set
     * @return this BitVectorBuffer with the bit in bitPos set to the value
     *         represented by bit
     * @throws IllegalArgumentException bitPos < 0, or bitPos >= the length of
     *             this BitVectorBuffer
     */
    public BitVectorBuffer setBit(int bitPos, Bit bit)
    {
        int intVal = bitPos / BITS_PER_UNIT;
        int shiftVal = bitPos % BITS_PER_UNIT;
        int val;
        int mask;
        int id;

        if (bit == null)
        {
            throw new NullPointerException("bit is null");
        }
        if (bitPos < 0 || bitPos >= length())
        {
            throw new IllegalArgumentException("bitPos (" + bitPos
                + ") is not in the range from 0:" + (length() - 1));
        }

        id = bit.getID();
        val = 0;
        mask = 0;
        if ((id & 1) == 1)
        {
            val = 1;
        }
        if ((id & 2) == 2)
        {
            mask = 1;
        }

        myValues[intVal] &= ~(1 << shiftVal);
        myXzMask[intVal] &= ~(1 << shiftVal);
        myValues[intVal] |= val << shiftVal;
        myXzMask[intVal] |= mask << shiftVal;
        return this;
    }

    /**
     * Returns this BitVectorBuffer with the specified BitRange set to the
     * specified BitVector.
     *
     * @param range the BitRange of this BitVectorBuffer to set to <code>vect</code>
     * @param vect the BitVector with which to set the bitslice
     * @return this BitVectorBuffer with the specified BitRange set to
     *      <code>vect</code>
     * @throws IllegalArgumentException if <code>range</code> specifies bits
     *      outside of this BitVectorBuffer
     */
    public BitVectorBuffer setBits(BitRange range, BitVector vect)
    {
        return setBits(range.high, range.low, vect);
    }

    /**
     * Returns this BitVectorBuffer with bits [hiBit:loBit] set to vect. If
     * hiBit - loBit + 1 is greater than 64 bits, <code>value</code> is
     * sign-extended.
     *
     * @param hiBit the most significant bit of the desired bitslice.
     * @param loBit the least significan bit of the desired bitslice.
     * @param value the value to which bits [hiBit:loBit] of this
     *            BitVectorBuffer should be set.
     * @return this BitVectorBuffer with bits [hiBit:loBit] set to buf.
     * @throws IllegalArgumentException hiBit &lt; loBit, or hiBit &gt;= the length of
     *             this BitVectorBuffer
     */
    public BitVectorBuffer setBits(int hiBit, int loBit, long value)
    {
        int sliceLength = (hiBit - loBit + 1);
        int bit, src;
        if (hiBit < loBit)
        {
            throw new IllegalArgumentException("MSB < LSB in parameter list ("
                + hiBit + " < " + loBit + ")");
        }
        else if (hiBit >= length() || loBit < 0)
        {
            throw new IllegalArgumentException(
                "Trying to access out-of-range bit [" + String.valueOf(hiBit)
                    + "] of a " + String.valueOf(length()) + "-bit field");
        }

        for (bit = loBit, src = 0; bit < (loBit + sliceLength); bit++, src++)
        {
            int intIdx = bit / BITS_PER_UNIT;
            int intShift = bit % BITS_PER_UNIT;
            int srcIdx = src / BITS_PER_UNIT;
            int srcShift = src % BITS_PER_UNIT;

            // clear whatever was there
            myValues[intIdx] &= ~(1 << intShift);
            myXzMask[intIdx] &= ~(1 << intShift);

            // and write the new value, zero-extending buf if needed
            final int bufValues;
            switch (srcIdx)
            {
            case 0:
                bufValues = (int) value;
                break;
            case 1:
                bufValues = (int) (value >> 32);
                break;
            default:
                bufValues = (value > 0) ? 0 : ~0;
            }
            myValues[intIdx] |= ((bufValues >> srcShift) & 1) << intShift;
        }

        return this;
    }

    /**
     * Returns this BitVectorBuffer with bits [hiBit:loBit] set to vect.
     *
     * @param hiBit the most significant bit of the desired bitslice.
     * @param loBit the least significan bit of the desired bitslice.
     * @param vect the BitVector to which bits [hiBit:loBit] of this
     *            BitVectorBuffer should be set.
     * @return this BitVectorBuffer with bits [hiBit:loBit] set to buf.
     * @throws IllegalArgumentException hiBit &lt; loBit, or hiBit &gt;= the length of
     *             this BitVectorBuffer
     */
    public BitVectorBuffer setBits(int hiBit, int loBit, BitVector vect)
    {
        return setBits(hiBit, loBit, vect.myBuffer);
    }

    // See {@link #setBits(int, int, BitVector) setBits(int, int, BitVector)}
    private BitVectorBuffer setBits(int hiBit, int loBit, BitVectorBuffer buf)
    {
        int sliceLength = (hiBit - loBit + 1);
        int bit, src;
        if (hiBit < loBit)
        {
            throw new IllegalArgumentException("MSB < LSB in parameter list");
        }
        else if (hiBit >= length() || loBit < 0)
        {
            throw new IllegalArgumentException(
                "Trying to access out-of-range bit [" + String.valueOf(hiBit)
                    + "] of a " + String.valueOf(length()) + "-bit field");
        }

        for (bit = loBit, src = 0; bit < (loBit + sliceLength); bit++, src++)
        {
            int intIdx = bit / BITS_PER_UNIT;
            int intShift = bit % BITS_PER_UNIT;
            int srcIdx = src / BITS_PER_UNIT;
            int srcShift = src % BITS_PER_UNIT;

            // clear whatever was there
            myValues[intIdx] &= ~(1 << intShift);
            myXzMask[intIdx] &= ~(1 << intShift);

            // and write the new value, zero-extending buf if needed
            int bufValues = (srcIdx < buf.getLastEntry() ? buf.myValues[srcIdx]
                : 0);
            int bufXzMask = (srcIdx < buf.getLastEntry() ? buf.myXzMask[srcIdx]
                : 0);
            myValues[intIdx] |= ((bufValues >> srcShift) & 1) << intShift;
            myXzMask[intIdx] |= ((bufXzMask >> srcShift) & 1) << intShift;
        }

        return this;
    }

    // Misc
    /**
     * Check if this BitVectorBuffer contains an X or Z value.
     *
     * @return true if this BitVectorBuffer contains an X or Z value, false
     *         otherwise
     */
    public boolean containsXZ()
    {
        for (int i = 0; i < getLastEntry(); i++)
        {
            if (myXzMask[i] != 0)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the BitVectorBuffer contains an X.
     *
     * @return true if this BitVectorBuffer contains an X.
     */
    private boolean containsX()
    {
        for (int i = 0; i < getLastEntry(); i++)
        {
            if ((myValues[i] & myXzMask[i]) != 0)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if this BitVectorBuffer should evaluate to false.
     *
     * @return true if every bit in this BitVectorBuffer is a zero, false
     *         otherwise
     */
    public boolean isZero()
    {
        int i;
        for (i = 0; i < getLastEntry(); i++)
        {
            if (myValues[i] != 0 || myXzMask[i] != 0)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if BitVectorBuffer[bitPos] should evaluate to false.
     *
     * @param bitPos the bit position to test
     * @return true if this[bitPos] is a zero, false otherwise
     * @throws IllegalArgumentException bitPos is outside the size of this
     *             BitVectorBuffer
     */
    public boolean isZero(int bitPos)
    {
        int intVal = bitPos / BITS_PER_UNIT;
        int shiftVal = bitPos % BITS_PER_UNIT;
        int val = (myValues[intVal] >> shiftVal) & 1;
        int mask = (myXzMask[intVal] >> shiftVal) & 1;
        if (bitPos < 0 || bitPos >= length())
        {
            throw new IllegalArgumentException("_bitPos is not in the range ["
                + length() + "-1:0]");
        }
        if (val == 0 && mask == 0)
        {
            return true;
        }
        return false;
    }

    /**
     * Check if this BitVector should evaluate to true.
     *
     * @return true if any bit in this BitVectorBuffer is a 1, false otherwise.
     */
    public boolean isNotZero()
    {
        int i;
        if (containsXZ()) return false;
        for (i = 0; i < getLastEntry(); i++)
        {
            if (myValues[i] != 0)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if BitVectorBuffer[bitPos] should evaluate to true.
     *
     * @param bitPos the bit position to test
     * @return true if this[bitPos] is a zero, false otherwise
     * @throws IllegalArgumentException bitPos is outside the size of this
     *             BitVectorBuffer
     */
    public boolean isNotZero(int bitPos)
    {
        int intVal = bitPos / BITS_PER_UNIT;
        int shiftVal = bitPos % BITS_PER_UNIT;
        int val = (myValues[intVal] >> shiftVal) & 1;
        int mask = (myXzMask[intVal] >> shiftVal) & 1;

        if (bitPos < 0 || bitPos >= length())
        {
            throw new IllegalArgumentException("bitPos is not in the range ["
                + "size-1:0]");
        }
        if (val == 1 && mask == 0)
        {
            return true;
        }
        return false;
    }

    /**
     * Converts this BitVectorBuffer to an <code>double</code>. This conversion
     * is analogous to a <a
     * href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">
     * <i>narrowing primitive conversion </i> </a> from <code>long</code> to
     * <code>double</code> as defined in the <a
     * href="http://java.sun.com/docs/books/jls/html/">Java Language
     * Specification </a>: if this BitVectorBuffer is too big to fit in an
     * <code>double</code>, only the low-order 64 bits are used. Note that
     * this conversion can lose information about the overall magnitude of the
     * BitVectorBuffer value.
     *
     * @return this BitVectorBuffer converted to an <code>double</code>.
     * @throws XZException if an X/Z value is present in the BitVectorBuffer
     */
    @Override
    public double doubleValue()
    {
        return longValue();
    }

    /**
     * Converts this BitVectorBuffer to an <code>float</code>. This conversion
     * is analogous to a <a
     * href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">
     * <i>narrowing primitive conversion </i> </a> from <code>int</code> to
     * <code>float</code> as defined in the <a
     * href="http://java.sun.com/docs/books/jls/html/">Java Language
     * Specification </a>: if this BitVectorBuffer is too big to fit in an
     * <code>float</code>, only the low-order 32 bits are used. Note that
     * this conversion can lose information about the overall magnitude of the
     * BitVectorBuffer value.
     *
     * @return this BitVectorBuffer converted to an <code>float</code>.
     * @throws XZException if an X/Z value is present in the BitVectorBuffer
     */
    @Override
    public float floatValue()
    {
        return intValue();
    }

    /**
     * Converts this BitVectorBuffer to an <code>int</code>. This conversion
     * is analogous to a <a
     * href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">
     * <i>narrowing primitive conversion </i> </a> from <code>long</code> to
     * <code>int</code> as defined in the <a
     * href="http://java.sun.com/docs/books/jls/html/">Java Language
     * Specification </a>: if this BitVectorBuffer is too big to fit in an
     * <code>int</code>, only the low-order 32 bits are returned. Note that
     * this conversion can lose information about the overall magnitude of the
     * BitVectorBuffer value.
     *
     * @return this BitVectorBuffer converted to an <code>int</code>.
     * @throws XZException if an X/Z value is present in the BitVectorBuffer
     */
    @Override
    public int intValue()
    {
        int mask = ~0;
        if (containsXZ())
        {
            throw new XZException("X/Z value is not numeric: "
                + this.toString(2));
        }
        if (length() < BITS_PER_UNIT)
        {
            mask = (1 << length()) - 1;
        }
        return (myValues[0] & mask);
    }

    /**
     * Converts this BitVectorBuffer to a <code>long</code>. This conversion
     * is analogous to a <a
     * href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">
     * <i>narrowing primitive conversion </i> </a> from <code>long</code> to
     * <code>int</code> as defined in the <a
     * href="http://java.sun.com/docs/books/jls/html/">Java Language
     * Specification </a>: if this BitVectorBuffer is too big to fit in a
     * <code>long</code>, only the low-order 64 bits are returned. Note that
     * this conversion can lose information about the overall magnitude of the
     * BitVectorBuffer value.
     *
     * @return this BitVectorBuffer converted to a <code>long</code>.
     * @throws XZException if an X/Z value is present in the BitVectorBuffer
     */
    @Override
    public long longValue()
    {
        long value = 0;
        if (containsXZ())
        {
            throw new XZException("X/Z value is not numeric: "
                + this.toString(2));
        }
        if (length() > BITS_PER_UNIT)
        {
            value = myValues[1];
            value <<= BITS_PER_UNIT;
        }
        value |= (myValues[0] & 0xFFFFFFFFL);

        return value;
    }

    /**
     * Convert this BitVectorBuffer to a String. This conversion uses the
     * default formatting values of {@link BitVectorFormat BitVectorFormat}.
     *
     * @return the String representation of this BitVectorBuffer
     */
    @Override
    public String toString()
    {
        return myFormat.format(this.toBitVector());
    }

    /**
     * Convert this BitVectorBuffer to a String with radix radix.
     *
     * @param radix Radix of the String representation.
     * @return the String representation of this BitVectorBuffer with the given
     *         radix
     */
    public String toString(int radix)
    {
        return myFormat.format(this.toBitVector(), radix);
    }

    /**
     * Converts to a BitVector representing the data in this BitVectorBuffer.
     * <P>
     * TODO: keep a cached BitVector as a private member and update it whenever
     * the buffer changes.
     *
     * @return a new BitVector representing this BitVectorBuffer
     */
    public BitVector toBitVector()
    {
        BitVector bitVector = new BitVector(this);
        return bitVector;
    }

    /**
     * Return an array of bytes representing this BitVectorBuffer. If the length
     * of this BitVectorBuffer is not a multiple of 8, the most significant byte
     * will be padded with zeros in the uppermost bits.
     * <P>
     * The array returned will be in big-endian byte-order: the most significant
     * byte is in the zeroth element.
     * <P>
     * @return a byte array of the bytes comprising this BitVectorBuffer.
     * @throws XZException BitVectorBuffer contains X/Z values
     */
    public byte[] getBytes()
    {
        return getBytes(false, false);
    }

    /**
     * Return an array of bytes representing this BitVectorBuffer. If the length
     * of this BitVectorBuffer is not a multiple of 8, the most significant byte
     * will be padded with zeros in the uppermost bits.
     * <P>
     * If minimalSize is true, the minimal number of bytes will be returned that
     * still describe the value and keep the sign bit intact.
     * <P>
     * If forceUnsigned is true, the top-most bit in byte[0] will be a zero.
     * This may cause the array returned to be one byte longer than it would be
     * otherwise, for instance <code>16'h8182</code> would be returned as
     * <code>{0x00, 0x81, 0x82}</code>
     * <P>
     * minimalSize and forceUnsigned may both be true, resulting in a minimal
     * representation that has the topmost bit in byte[0] to be a zero.
     *
     * @param minimalSize true if the array returned should be of minimal size
     *            to describe the value
     * @param forceUnsigned true if the top-most bit in the representation
     *            should be a zero
     * @return the byte array
     */
    private byte[] getBytes(boolean minimalSize, boolean forceUnsigned)
    {
        int numBytes = (length() / 8) + (((length() % 8) == 0) ? 0 : 1);

        if (containsXZ())
        {
            throw new XZException("getBytes doesn't support X/Z: "
                + this.toString(2));
        }

        // Calculate the highest byte idx that doesn't contain sign information
        int highestWordIdx = getLastEntry() - 1;
        int highestByteIdx = numBytes - 1;
        int topByteShift = (highestByteIdx % 4) * 8;
        byte topByte = (byte) (myValues[highestWordIdx] >> topByteShift);

        if (minimalSize)
        {
            // NOTE: This code yields a byte[] identical to that provided by
            // BigInteger, which contains a minimum number of bytes, while
            // preserving the sign.
            byte curByteVal = topByte;
            if (topByte == 0 || topByte == -1)
            {
                int curWordIdx = highestWordIdx;
                int curByteIdx = highestByteIdx;
                do
                {
                    int curByteShift = curByteIdx % 4;
                    curByteVal = (byte) (myValues[curWordIdx] >> curByteShift);
                    if (curByteVal != topByte)
                    {
                        if ((curByteVal & 0x80) == 0)
                        {
                            if (topByte == -1)
                            {
                                ++highestByteIdx;
                            }
                        }
                        else
                        {
                            if (topByte == 0)
                            {
                                ++highestByteIdx;
                            }
                        }
                        break;
                    }
                    else if (curWordIdx == 0 && curByteIdx == 0)
                    {
                        highestByteIdx = 0;
                        break;
                    }

                    --highestByteIdx;
                    --curByteIdx;
                    if (((curByteIdx + 1) % 4) == 0)
                    {
                        --curWordIdx;
                    }
                }
                while (curWordIdx >= 0 || curByteIdx >= 0);
            }
        }

        int intVal = highestByteIdx / 4;
        int shiftVal = highestByteIdx % 4;
        int extraEntry = (forceUnsigned && topByte != 0) ? 1 : 0;
        byte[] bytes = new byte[highestByteIdx + extraEntry + 1];

        for (int revBite = extraEntry; revBite < bytes.length; ++revBite)
        {
            bytes[revBite] = (byte) ((myValues[intVal] >> (shiftVal * 8)) & 0xff);
            --shiftVal;
            if (shiftVal == -1)
            {
                --intVal;
                shiftVal = 3;
            }
            //System.out.println("bytes[" + revBite + "]: 0x"
            //    + Integer.toHexString(bytes[revBite] & 0xFF));
        }

        return bytes;
    } /////////////////////////////////////////////

    /**
     * Mask off anything between length() and length() + (length() %
     * BITS_PER_UNIT)
     */
    private void maskExtraLength()
    {
        int maxBit = myValues.length * BITS_PER_UNIT;
        int bitDiff = maxBit - length();
        int curIdx = myValues.length - 1;

        // First zero out any entries in myValue in which no valid bits reside
        for (curIdx = (myValues.length - 1); curIdx > (length() / BITS_PER_UNIT); --curIdx)
        {
            myValues[curIdx] = 0;
            myXzMask[curIdx] = 0;
        }

        // Then zero out the top bits in the highest entry in which valid bits
        // reside.
        bitDiff = ((curIdx + 1) * BITS_PER_UNIT) - length();
        assert (bitDiff >= 0);
        int mask = (int) (((1L << bitDiff) - 1L) << (BITS_PER_UNIT - bitDiff));
        myValues[curIdx] &= ~mask;
        myXzMask[curIdx] &= ~mask;
    }

    /**
     * Guarantees this BitVectorBuffer to have the specified number of
     * bits. If <code>length &lt;= this.length()</code>, the BitVectorBuffer is
     * unchanged. Otherwise, this BitVectorBuffer is extended as necessary with
     * <code>extensionBit</code>.
     *
     * @param length the requested length
     * @param extensionBit the Bit to use when extending this BitVectorBuffer
     *      beyond its current length
     * @return this BitVectorBuffer guaranteed to have at least
     *      <code>length</code> bits
     */
    public BitVectorBuffer extend(int length, Bit extensionBit)
    {
        // TODO: a previous truncate may have left us with empty myValues entries
        // that we could use to avoid new'ing an array FOLLOWUP: PERF
        if (length <= 0)
        {
            throw new IllegalArgumentException("length (" + length
                + ") cannot be <= 0");
        }

        if (length <= myLength)
        {
            return this;
        }
        else if (length == myLength)
        {
            return this;
        }

        final int id = extensionBit.getID();
        int value = 0;
        int xzMask = 0;
        if ((id & 1) == 1)
        {
            value = ~0;
        }
        if ((id & 2) == 2)
        {
            xzMask = ~0;
        }

        // Simple Case where we don't need to actually increase our
        // arrays
        if (((myLength - 1) / BITS_PER_UNIT) == ((length - 1) / BITS_PER_UNIT))
        {
            // fill the extra bits and be done
            int curBit = myValues.length * BITS_PER_UNIT;
            int bitDiff = curBit - myLength;
            int clearMask = (int) (~(((1L << bitDiff) - 1L) << myLength));
            int maskValues = (value & ~clearMask);
            int maskXzMask = (xzMask & ~clearMask);
            myValues[myValues.length - 1] &= clearMask;
            myXzMask[myXzMask.length - 1] &= clearMask;
            myValues[myValues.length - 1] |= maskValues;
            myXzMask[myXzMask.length - 1] |= maskXzMask;
            myLength = length;
            maskExtraLength();
            return this;
        }

        // In this case we have to actually extend our arrays
        int numUnits = ((length) / BITS_PER_UNIT)
            + (((length) % BITS_PER_UNIT == 0) ? 0 : 1);
        int[] tmpValues = new int[numUnits];
        int[] tmpXzMask = new int[numUnits];
        for (int i = 0; i < tmpValues.length; i++)
        {
            if (i >= myValues.length)
            {
                tmpValues[i] = value;
                tmpXzMask[i] = xzMask;
            }
            else if (i < (myValues.length - 1))
            {
                tmpValues[i] = myValues[i];
                tmpXzMask[i] = myXzMask[i];
            }
            else
            {
                int curBit = myValues.length * BITS_PER_UNIT;
                int bitDiff = curBit - myLength;
                int clearMask = (int) (~(((1L << bitDiff) - 1L) << myLength));
                int maskValues = (value & ~clearMask);
                int maskXzMask = (xzMask & ~clearMask);
                tmpValues[i] = myValues[i];
                tmpXzMask[i] = myXzMask[i];
                tmpValues[i] &= clearMask;
                tmpXzMask[i] &= clearMask;
                tmpValues[i] |= maskValues;
                tmpXzMask[i] |= maskXzMask;
            }
        }

        this.assign(length, tmpValues, tmpXzMask);
        maskExtraLength();
        return this;
    }

    /**
     * Truncate this BitVectorBuffer.
     *
     * @param length the number of bits by which to truncate this
     *            BitVectorBuffer. If length == length(), this BitVectorBuffer
     *            is returned unchanged.
     * @return this BitVectorBuffer truncated to length bits
     * @throws IllegalArgumentException if length > length()
     */
    private BitVectorBuffer truncate(int length)
    {
        if (length > length())
        {
            throw new IllegalArgumentException(length + " > " + length());
        }
        else if (length == length())
        {
            return this;
        }

        myLength = length;
        maskExtraLength();
        return this;
    }

    /**
     * Returns the index of the last entry in myValues/myXzMask that contains
     * valid data for this BitVectorBuffer.
     *
     * @return last index of myValues/myXzMask containing valid data
     */
    private int getLastEntry()
    {
        return ((length() - 1) / BITS_PER_UNIT) + 1;
    }

}
