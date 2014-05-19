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

/**
 * An immutable class that implements the functions of a BitVector including
 * arithmetic and logical operations, as well as bit access.
 * 
 * @author Jon Nall
 */
public final class BitVector
    extends Number
    implements Comparable, Serializable
{
    static final long serialVersionUID = 3246007612484334862L;

    final BitVectorBuffer myBuffer;

    // Constructors
    /**
     * Creates a BitVector with the specified size, with all bits set to
     * {@link Bit#X Bit.X}.
     *
     * @param length The size of the BitVector in bits.
     */
    public BitVector(int length)
    {
        myBuffer = new BitVectorBuffer(length);
    }

    /**
     * Creates a BitVector with the specified size, with each bit having the
     * value bit.
     *
     * @param length The size of the BitVector in bits.
     * @param bit The Bit to which each bit of the BitVector should be set.
     */
    public BitVector(int length, Bit bit)
    {
        myBuffer = new BitVectorBuffer(length, bit);
    }

    /**
     * Creates a BitVector with the specified size, filled with the initial
     * value provided. Bits 32 and higher are sign-extended.
     *
     * @param length The size of the BitVector in bits.
     * @param value The value with which to initialize the BitVector.
     */
    public BitVector(int length, int value)
    {
        myBuffer = new BitVectorBuffer(length, value, true);
    }

    /**
     * Creates a BitVector with the specified size, filled with the initial
     * value provided. Bits 32 and higher are either zero-filled or
     * sign-extended, depending on the signExtend parameter
     *
     * @param length The size of the BitVector in bits.
     * @param value The value with which to initialize the BitVector.
     * @param signExtend true if bits 32 and higher should be sign extended,
     *            false if they should be zero-extended.
     */
    public BitVector(int length, int value, boolean signExtend)
    {
        myBuffer = new BitVectorBuffer(length, value, signExtend);
    }

    /**
     * Creates a BitVector with the specified size, filled with the initial
     * value provided. Bits 64 and higher are sign-extended.
     *
     * @param length The size of the BitVector in bits.
     * @param value The value with which to initialize the BitVector.
     */
    public BitVector(int length, long value)
    {
        myBuffer = new BitVectorBuffer(length, value, true);
    }

    /**
     * Creates a BitVector with the specified size, filled with the initial
     * value provided. Bits 64 and higher are sign-extended or zero-filled,
     * depending on the signExtend parameter.
     *
     * @param length The size of the BitVector in bits.
     * @param value The value with which to initialize the BitVector.
     * @param signExtend true if bits 64 and higher should be sign extended,
     *            false if they should be zero-extended.
     */
    public BitVector(int length, long value, boolean signExtend)
    {
        myBuffer = new BitVectorBuffer(length, value, signExtend);
    }

    /**
     * Creates a BitVector from a Verilog-style String of the format described
     * in {@link BitVectorFormat BitVectorFormat}.
     *
     * @param value String with which to initialize the BitVector.
     */
    public BitVector(String value)
    {
        myBuffer = new BitVectorBuffer(value);
    }

    /**
     * Creates a BitVector from a Verilog-style String of the format described in
     * {@link BitVectorFormat BitVectorFormat}. It will have a length of
     * length, which may cause value to be either truncated or zero-extended.
     *
     * @param value String with which to initialize the BitVector.
     * @param length size of the BitVector in bits.
     */
    public BitVector(String value, int length)
    {
        myBuffer = new BitVectorBuffer(value, length);
    }

    /**
     * Creates a BitVector from a BitVectorBuffer.
     *
     * @param buffer BitVectorBuffer with which to initialize the BitVector.
     */
    BitVector(BitVectorBuffer buffer)
    {
        myBuffer = new BitVectorBuffer(buffer);
    }

    /**
     * Creates a BitVector from the given byte array. The input array is assumed
     * to be in big-endian byte-order: the most significant byte is in the
     * zeroth element.
     *
     * @param bytes a byte array containing values used to initialize the
     *            BitVector
     */
    public BitVector(byte[] bytes)
    {
        myBuffer = new BitVectorBuffer(bytes);
    }

    /**
     * Creates a BitVector with a length of 1 from the given Bit
     *
     * @param bit value to use for the BitVector
     */
    BitVector(Bit bit)
    {
        myBuffer = new BitVectorBuffer(bit);
    }

    /**
     * Creates a BitVector from the given parameters
     *
     * @param values int array to use for 0/1 values
     * @param xzmask int array to use for x/z values
     * @param length size of the new BitVector in bits
     */
    BitVector(int[] values, int[] xzmask, int length)
    {
        myBuffer = new BitVectorBuffer(values, xzmask, length);
    }

    /**
     * Returns the 0/1 values for this vector.
     *
     * @return an int[] of 0/1 values
     */
    int[] values()
    {
        return myBuffer.myValues;
    }

    /**
     * Returns the x/z values for this vector.
     *
     * @return an int[] of x/z values
     */
    int[] xzMask()
    {
        return myBuffer.myXzMask;
    }

    /**
     * Returns the number of bits in this BitVector.
     *
     * @return length of the BitVector
     */
    public int length()
    {
        return myBuffer.length();
    }

    /**
     * Creates a new BitVector from vect extending or truncating such that the
     * length of the new BitVector is identical to this BitVector's length
     *
     * @param vect BitVector to assign to the new BitVector
     * @return a new BitVector after performing the assign operation.
     */
    public BitVector assign(BitVector vect)
    {
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.assign(vect));
    }

    /**
     * Create a new BitVector equal in value to this BitVector except
     * vect is assigned to the new BitVector's value depending on mask according
     * to this algorithm:
     * <P>
     * <code><pre>
     * if (mask[i] == 1)
     *     newBitVector[i] = vect[i];
     * else if (mask[i] == X || mask[i] == Z)
     *     newBitVector[i] = X;
     * else
     *     newBitVector[i] = this[i];
     * </pre></code>
     * <P>
     * mask is 0-extended if needed. vect is 0-extended if needed. the length of
     * the returned BitVector will be identical to this BitVector's length.
     *
     * @param vect The BitVector to assign from
     * @param mask The mask to use when deciding what to assign to this
     *            BitVector
     * @return A new BitVector with the value determined by the above algorithm.
     */
    public BitVector assignMask(BitVector vect, BitVector mask)
    {
        BitVectorBuffer buf = new BitVectorBuffer(this.myBuffer);
        return new BitVector(buf.assignMask(vect, mask));
    }

    /**
     * Returns a BitVector which is guaranteed to have the specified number of
     * bits. If <code>length &lt;= this.length()</code>, the BitVector is
     * returned unchanged. Otherwise, a new BitVector is created with the
     * specified length and zero-extended.
     *
     * @param length the requested length
     * @return a BitVector guaranteed to have at least <code>length</code> bits
     */
    public BitVector extend(int length)
    {
        return extend(length, Bit.ZERO);
    }

    /**
     * Returns a BitVector which is guaranteed to have the specified number of
     * bits. If <code>length &lt;= this.length()</code>, the BitVector is
     * returned unchanged. Otherwise, a new BitVector is created with the
     * specified length and extended with <code>extensionBit</code>.
     *
     * @param length the requested length
     * @param extensionBit the Bit to use when extending this BitVector beyond
     *      its current length
     * @return a BitVector guaranteed to have at least <code>length</code> bits
     */
    public BitVector extend(int length, Bit extensionBit)
    {
        if (length <= this.length())
        {
            return this;
        }
        return setLength(length, extensionBit);
    }

    /**
     * Returns a BitVector which is an exact copy of this BitVector except
     * its length is truncated or zero-extended as needed. If
     * <code>length == this.length()</code>, this BitVector is returned.
     * Otherwise a new BitVector is returned.
     *
     * @param length the new length of the BitVector
     * @return new BitVector identical to this BitVector with its length set to
     *         the requested length
     */
    public BitVector setLength(int length)
    {
        return setLength(length, Bit.ZERO);
    }

    /**
     * Returns a BitVector which is an exact copy of this BitVector except
     * its length is truncated or extended as needed. If it needs to be
     * extended, bit will be used as the value for the extension. If
     * <code>length == this.length()</code>, this BitVector is returned.
     * Otherwise a new BitVector is returned.
     *
     * @param length the new length of the BitVector
     * @param bit the Bit to use if we need to extend this BitVector
     * @return a new BitVector identical to this BitVector with its length set
     *         to the requested length
     */
    public BitVector setLength(int length, Bit bit)
    {
        if (this.length() == length)
        {
            return this;
        }
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.setLength(length, bit));
    }

    /**
     * Returns a BitVector which is an exact copy of this BitVector except
     * its length is truncated or sign-extended as needed. If
     * <code>length == this.length()</code>, this BitVector is returned.
     * Otherwise a new BitVector is returned.
     *
     * @param length the new length of the BitVector
     * @return a new BitVector identical to this BitVector with its length set
     *         to the requested length
     */
    public BitVector setLengthHigh(int length)
    {
        if (this.length() == length)
        {
            return this;
        }
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.setLengthHigh(length));
    }

    /**
     * Compares this BitVector with the specified Object. If obj is not a
     * BitVector, a CastClassException is thrown.
     *
     * @param obj the object to compare
     * @return a negative number, zero, or a positive number as this BitVector
     *         is numerically less than, equal to, or greater than o, which must
     *         be a BitVector.
     * @throws ClassCastException obj is not a BitVector
     * @throws XZException obj is a BitVector containing X/Z values
     */
    public int compareTo(Object obj)
    {
        BitVector vect = (BitVector) obj;
        return myBuffer.compareTo(vect.myBuffer);
    }

    /**
     * Compares the Object represented by obj to this BitVector (equivalent to
     * Verilog ==).
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
     * @return true if and only if obj is a BitVector, BitVector, or Bit which
     *         when compared with this BitVector on a bit by bit basis is true
     *         for each bit according to the table above. Note that for vectors
     *         of differing lengths, the shorter vector is zero-extended.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof BitVector || obj instanceof BitVectorBuffer || obj instanceof Bit))
        {
            return false;
        }

        return myBuffer.equals(obj);
    }

    /**
     * Returns a BitVector in which a given bit is 1 if, for that particular bit
     * position, equals(vect) would return true. Otherwise, the bit is set to 0.
     * <P>
     * Example:
     * <code>16'b0000_1111_xxxx_zzzz.equalsMask(16'b0x00_1zz1_10xx_zz1z)</code>
     * results in a BitVector of: <code>16'b1011100100000000</code>.
     * <P>
     * If vect.length() > this.length(), this BitVector is zero-extended to
     * vect.length().
     *
     * @param vect the BitVector to compare against.
     * @return a new BitVector with the property described above.
     */
    public BitVector equalsMask(BitVector vect)
    {
        return new BitVector(myBuffer.equalsMask(vect));
    }

    /**
     * Compares the BitVector represented by vect to this BitVector (equivalent
     * to Verilog ===).
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
     * @return true if and only if vect is a BitVector which when compared with
     *         this BitVector on a bit by bit basis is true for each bit
     *         according to the table above. Note that for vectors of differing
     *         lengths, the shorter vector is zero-extended.
     */
    public boolean equalsExact(BitVector vect)
    {
        return myBuffer.equalsExact(vect);
    }

    /**
     * Returns a BitVector in which a given bit is 1 if, for that particular bit
     * position, equalsExact(vect) would return true. Otherwise, the bit is set
     * to 0.
     * <P>
     * Example:
     * <code>16'b0000_1111_xxxx_zzzz.equalsExactMask(16'b0x00_1zz1_10xx_zz1z)</code>
     * results in a BitVector of: <code>16'b1011100100111101</code>.
     * <P>
     * If vect.length() > this.length(), this BitVector is zero-extended to
     * vect.length().
     *
     * @param vect the BitVector to compare against.
     * @return a BitVector with the property described above.
     */
    public BitVector equalsExactMask(BitVector vect)
    {
        return new BitVector(myBuffer.equalsExactMask(vect));
    }

    /**
     * Compares the BitVector represented by vect to this BitVector (equivalent
     * to Verilog =?=).
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
        return myBuffer.equalsWild(vect);
    }

    /**
     * Returns a BitVector in which a given bit is 1 if, for that particular bit
     * position, equalsWild(vect) would return true. Otherwise, the bit is set
     * to 0.
     * <P>
     * Example:
     * <code>16'b0000_1111_xxxx_zzzz.equalsWildMask(16'b0x00_1zz1_10xx_zz1z)</code>
     * results in a BitVector of: <code>16'b1111111111111111</code>.
     * <P>
     * If vect.length() > this.length(), this BitVector is zero-extended to
     * vect.length().
     *
     * @param vect the BitVector to compare against.
     * @return a BitVector with the property described above.
     */
    public BitVector equalsWildMask(BitVector vect)
    {
        return new BitVector(myBuffer.equalsWildMask(vect));
    }

    /**
     * Return a HashCode for this BitVector.
     * @return the hash code
     */
    @Override
    public int hashCode()
    {
        // let BitVectorBuffer do the work, but try to
        // be a little unique (uses an arbitrarily picked prime number)
        return myBuffer.hashCode() ^ 0x77402731;
    }

    /**
     * Performs a bitwise AND function of this BitVector and vect and returns a
     * new BitVector holding the result. The result will have a length equal to
     * <code>Math.max(this.length(), vect.length())</code>.
     *
     * @param vect the BitVector with which to AND.
     * @return this & vect
     */
    public BitVector and(BitVector vect)
    {
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.and(vect));
    }

    /**
     * Performs a bitwise AND function of this BitVector and NOT vect and
     * returns a new BitVector holding the result. The result will have a length
     * equal to <code>Math.max(this.length(), vect.length())</code>.
     *
     * @param vect the BitVector to NOT and with which to AND.
     * @return this & ~vect
     */
    public BitVector andNot(BitVector vect)
    {
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.andNot(vect));
    }

    /**
     * Performs a bitwise OR function of this BitVector and vect and returns a
     * new BitVector holding the result. The result will have a length equal to
     * <code>Math.max(this.length(), vect.length())</code>.
     *
     * @param vect the BitVector with which to OR.
     * @return this | vect
     */
    public BitVector or(BitVector vect)
    {
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.or(vect));
    }

    /**
     * Performs a bitwise XOR function of this BitVector and vect and returns a
     * new BitVector holding the result. The result will have a length equal to
     * <code>Math.max(this.length(), vect.length())</code>.
     *
     * @param vect the BitVector with which to XOR.
     * @return this ^ vect
     */
    public BitVector xor(BitVector vect)
    {
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.xor(vect));
    }

    /**
     * Performs a bitwise NOT function of this BitVector and vect and returns a
     * new BitVector holding the result.
     *
     * @return ~this
     */
    public BitVector not()
    {
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.not());
    }

    /**
     * Performs an addition of this BitVector and vect and returns a new
     * BitVector holding the result. The result will have a length equal to
     * <code>Math.max(this.length(), vect.length())</code>.
     *
     * @param vect the BitVector with which to add.
     * @return this + vect
     */
    public BitVector add(BitVector vect)
    {
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.add(vect));
    }

    /**
     * Performs a subtraction of vect from this BitVector and returns a new
     * BitVector holding the result. The result will have a length equal to
     * <code>Math.max(this.length(), vect.length())</code>.
     *
     * @param vect the BitVector to subtract from this BitVector.
     * @return this - vect
     */
    public BitVector subtract(BitVector vect)
    {
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.subtract(vect));
    }

    /**
     * Performs a multiplication of this BitVector and vect and returns a new
     * BitVector holding the result. The result will have a length equal to
     * <code>Math.max(this.length(), vect.length())</code>.
     *
     * @param vect the BitVector to multiply with this BitVector.
     * @return this * vect
     */
    public BitVector multiply(BitVector vect)
    {
        BitVectorBuffer buf = new BitVectorBuffer(this.myBuffer);
        return new BitVector(buf.multiply(vect));
    }

    /**
     * Performs a division of vect into this BitVector and returns a new
     * BitVector holding the result. The result will have a length equal to
     * <code>Math.max(this.length(), vect.length())</code>.
     *
     * @param vect the BitVector to divide into this BitVector.
     * @return this / vect
     */
    public BitVector divide(BitVector vect)
    {
        BitVectorBuffer buf = new BitVectorBuffer(this.myBuffer);
        return new BitVector(buf.divide(vect));
    }

    /**
     * Calculates this BitVector modulo vect and returns a new BitVector holding
     * the result. The result will have a length equal to
     * <code>Math.max(this.length(), vect.length())</code>.
     *
     * @param vect the BitVector by which to modulo this BitVector.
     * @return this % vect
     */
    public BitVector mod(BitVector vect)
    {
        BitVectorBuffer buf = new BitVectorBuffer(this.myBuffer);
        return new BitVector(buf.mod(vect));
    }

    /**
     * Performs an arithmetic negation of this BitVector and returns a new
     * BitVector holding the result.
     *
     * @return -(this)
     */
    public BitVector negate()
    {
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.negate());
    }

    /**
     * Performs a bit reversal this BitVector and returns a new BitVector
     * holding the result.
     *
     * @return &gt;&lt;this
     */
    public BitVector reverse()
    {
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.reverse());
    }

    /**
     * Performs a reductive and on this BitVector and returns the result. If all
     * bits in this BitVector are Bit.ONE, the result will be Bit.ONE,
     * otherwise, the result will be Bit.ZERO. TODO: table of 0/1/X/Z values
     *
     * @return &this
     */
    public Bit reductiveAnd()
    {
        return (myBuffer.reductiveAnd());
    }

    /**
     * Performs a reductive or on this BitVector and returns the result. If any
     * bit in this BitVector is Bit.ONE, the result will be Bit.ONE, otherwise,
     * the result will be Bit.ZERO. TODO: table of 0/1/X/Z values
     *
     * @return |this
     */
    public Bit reductiveOr()
    {
        return (myBuffer.reductiveOr());
    }

    /**
     * Performs a reductive xor on this BitVector and returns the result. If an
     * odd number of bits in this BitVector are Bit.ONE, the result will be
     * Bit.ONE, otherwise, the result will be Bit.ZERO. TODO: table of 0/1/X/Z
     * values
     *
     * @return ^this
     */
    public Bit reductiveXor()
    {
        return (myBuffer.reductiveXor());
    }

    /**
     * Method to multiplex multiple BitVectors onto one BitVector. This is
     * useful for BDD variable ordering.
     * <P>
     * Example: if vectors holds {X, Y, Z} and X.length() == 4, Y.length() == 6,
     * and Z.length() == 2,
     * <P>
     * the resulting BitVector will be of the format: XXXXXYXYZXYZ
     *
     * @param vectors the vectors to zip
     * @return a new BitVector comprising the zipped vectors
     */
    public BitVector zip(BitVector[] vectors)
    {
        BitVectorBuffer[] bufs = new BitVectorBuffer[vectors.length];
        for (int i = 0; i < vectors.length; i++)
        {
            bufs[i] = vectors[i].myBuffer;
        }
        return new BitVector(myBuffer.zip(bufs));
    }

    /**
     * Performs the opposite of {@link #zip(BitVector[]) zip(BitVector[])},
     * placing each unzipped BitVector in the resulting array.
     *
     * @param vectLengths an array of the length of each BitVector to be
     *            unzipped
     * @return an array of unzipped BitVectors.
     */
    public BitVector[] unzip(int[] vectLengths)
    {
        BitVectorBuffer[] bufs = myBuffer.unzip(vectLengths);
        BitVector[] results = new BitVector[vectLengths.length];
        for (int i = 0; i < vectLengths.length; i++)
        {
            results[i] = new BitVector(bufs[i]);
        }
        return results;
    }

    /**
     * Performs a concatenation of vector onto the end of this BitVector and
     * returns a new BitVector holding the result.
     *
     * @param vect the BitVector to concatenate.
     * @return {this, vect}
     */
    public BitVector concat(BitVector vect)
    {
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.concat(vect));
    }

    /**
     * Returns a new BitVector which is a concatenation of the specified
     * Objects. The types of the objects in <code>values</code> must be supported
     * by {@link ValueConverter#toBitVector(Object)}.
     *
     * @param values the Objects to concatenate, specified with the most
     *      significant bits in the first Object, and so on
     * @return a new BitVector which is the concatenation of the specified
     *      Objects
     */
    public static BitVector concat(Object... values)
    {
        return new BitVector(BitVectorBuffer.concat(values));
    }

    /**
     * Returns a new BitVector that is the result of replicating this
     * BitVector the specified number of times.
     *
     * @param count the number of times to replicate this BitVector
     * @return a new BitVector containing this BitVector replicated the
     *      specified number of times
     */
    public BitVector replicate(int count)
    {
        return new BitVector(myBuffer.replicate(count));
    }

    /**
     * Performs a left shift of this BitVector and returns a new BitVector
     * holding the result. The resulting BitVector will have the same length as
     * this BitVector.
     *
     * @param numBits Number of bits to shift to the left.
     * @return this &lt;&lt; numBits
     */
    public BitVector shiftLeft(int numBits)
    {
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.shiftLeft(numBits));
    }

    /**
     * Performs a right shift of this BitVector and returns a new BitVector
     * holding the result. The resulting BitVector will have the same length as
     * this BitVector. Zeros will be shifted into the high bits of the resulting
     * BitVector.
     *
     * @param numBits Number of bits to shift to the left.
     * @return this >>> numBits
     */
    public BitVector shiftRight(int numBits)
    {
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.shiftRight(numBits));
    }

    /**
     * Returns the value of the bit in bit position bitPos as a {@link Bit Bit}.
     *
     * @param bitPos the bit position to access
     * @return this[bitPos]
     * @throws IllegalArgumentException bitPos is not between 0 and the size of
     *             this BitVector
     */
    public Bit getBit(int bitPos)
    {
        return myBuffer.getBit(bitPos);
    }

    /**
     * Returns a new BitVector comprised of bits specified by the given BitRange.
     *
     * @param range a BitRange describing the bitslice
     * @return a BitVector comprised of bits [hiBit:loBit] of this BitVector.
     * @throws IllegalArgumentException if <code>range</code> specifies bits
     *      outside of this BitVector
     */
    public BitVector getBits(BitRange range)
    {
        return getBits(range.high, range.low);
    }

    /**
     * Returns a new BitVector comprised of bits [hiBit:loBit] of this
     * BitVector.
     *
     * @param hiBit the most significant bit of the desired bitslice.
     * @param loBit the least significan bit of the desired bitslice.
     * @return a BitVector comprised of bits [hiBit:loBit] of this BitVector.
     * @throws IllegalArgumentException hiBit < loBit, or hiBit >= the length of
     *             this BitVector
     */
    public BitVector getBits(int hiBit, int loBit)
    {
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.getBits(hiBit, loBit));
    }

    /**
     * Return the number of bits in this BitVector which have value bit.
     * @param bit The value for which to check
     * @return the number of bits in this BitVector which have value bit.
     */
    public int getBitCount(Bit bit)
    {
        return getBitCount(length() - 1, 0, bit);
    }

    /**
     * Return the number of bits in the given range of this BitVector
     * which have value bit.
     * @param range the BitRange to check
     * @param bit The value for which to check
     * @return the number of bits in the given range of this BitVector
     *      which have value bit.
     * @throws IllegalArgumentException if <code>range</code> specifies bits
     *      outside of this BitVector
     */
    public int getBitCount(BitRange range, Bit bit)
    {
        return myBuffer.getBitCount(range.high, range.low, bit);
    }

    /**
     * Return the number of bits in the given range of this BitVector
     * which have value bit.
     * @param hiBit The high bit of the range to check
     * @param loBit The low bit of the range to check
     * @param bit The value for which to check
     * @return the number of bits in the given range of this BitVector
     *      which have value bit.
     * @throws IllegalArgumentException hiBit < loBit, or hiBit >= the length of
     *             this BitVector
     */
    public int getBitCount(int hiBit, int loBit, Bit bit)
    {
        return myBuffer.getBitCount(hiBit, loBit, bit);
    }

    /**
     * Returns a new BitVector which is a mask of 1's corresponding to X's in
     * this BitVector. The new BitVector, result, has:
     * <P>
     * <code>result[i] = (vect[i] == Bit.ZERO ? result[i] : Bit.X)</code>
     * <P>
     * If vect is not of the same length as this BitVector, it will be truncated
     * or zero-extended as needed. However, this operation will not result in a
     * BitVector with a greater length than this BitVector.
     *
     * @param vect BitVector containing a mask of 1's.
     * @return this BitVector with X's in any bit position where a 1 is present
     *         in vect.
     */
    public BitVector setX(BitVector vect)
    {
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.setX(vect));
    }

    /**
     * Returns a new BitVector which is a mask of 1's corresponding to Z's in
     * this BitVector. The new BitVector, result, has:
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
     * If vect is not of the same length as this BitVector, it will be truncated
     * or zero-extended as needed. However, this operation will not result in a
     * BitVector with a greater length than this BitVector.
     *
     * @param vect BitVector containing a mask of 1's.
     * @return this BitVector with Z's in any bit position where a 1 is present
     *         in vect.
     */
    public BitVector setZ(BitVector vect)
    {
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.setZ(vect));
    }

    /**
     * Returns a new BitVector with a 1 in each bit position that is occupied by
     * an X in this BitVector and a 0 in each bit position that is occupied by a
     * 0, 1, or Z.
     *
     * @return new BitVector representing a mask of X values in this BitVector.
     */
    public BitVector getXMask()
    {
        return new BitVector(myBuffer.getXMask());
    }

    /**
     * Returns a new BitVector with a 1 in each bit position that is occupied by
     * an Z in this BitVector and a 0 in each bit position that is occupied by a
     * 0, 1, or X.
     *
     * @return new BitVector representing a mask of Z values in this BitVector.
     */
    public BitVector getZMask()
    {
        return new BitVector(myBuffer.getZMask());
    }

    /**
     * Returns a new BitVector which is identical to this BitVector except bits
     * in the specified BitRange are set to the value represented by bit.
     *
     * @param range the BitRange in which each bit should be set to <code>bit</code>
     * @param bit the Bit to fill with
     * @return a new BitVector with each bit in the specified BitRange set to bit.
     * @throws IllegalArgumentException if <code>range</code> specifies bits
     *      outside of this BitVector
     */
    public BitVector fillBits(BitRange range, Bit bit)
    {
        return fillBits(range.high, range.low, bit);
    }

    /**
     * Returns a new BitVector which is identical to this BitVector except bits
     * hiBit:loBit are set to the value represented by bit.
     *
     * @param hiBit the most significant bit of the desired bitslice.
     * @param loBit the least significan bit of the desired bitslice.
     * @param bit the Bit to fill with
     * @return a new BitVector with hiBit:loBit set to bit.
     * @throws IllegalArgumentException hiBit < loBit, or hiBit >= the length of
     *             this BitVector
     */
    public BitVector fillBits(int hiBit, int loBit, Bit bit)
    {
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.fillBits(hiBit, loBit, bit));
    }

    /**
     * Returns a new BitVector which is identical to this one except the bit in
     * bitPos is set to the value represented by bit.
     *
     * @param bitPos the bit position to access
     * @param bit the Bit enumeration to which this[bitPos] should be set
     * @return a BitVector identical to this one with this[bitPos] set to the
     *         value represented by bit
     * @throws IllegalArgumentException bitPos < 0, or bitPos >= the length of
     *             this BitVectorBuffer
     */
    public BitVector setBit(int bitPos, Bit bit)
    {
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.setBit(bitPos, bit));
    }

    /**
     * Returns a new BitVector which is identical to this BitVector except bits
     * in the specified BitRange are set to the value represented by <code>vector</code>.
     * <code>vector</code> is truncated or zero-extended as appropriate to set
     * its length to the length of <code>range</code>.
     *
     * @param range a BitRange describing the desired bitslice
     * @param vector the BitVector to fill with
     * @return a new BitVector with hiBit:loBit set to vector.
     * @throws IllegalArgumentException if <code>range</code> specifies bits
     *      outside of this BitVector
     */
    public BitVector setBits(BitRange range, BitVector vector)
    {
        return setBits(range.high, range.low, vector);
    }

    /**
     * Returns a new BitVector which is identical to this BitVector except bits
     * hiBit:loBit are set to the value represented by <code>value</code>.
     * <code>value</code>is truncated or sign-extended as appropriate to set its
     * length to (hiBit - loBit + 1).
     *
     * @param hiBit the most significant bit of the desired bitslice.
     * @param loBit the least significan bit of the desired bitslice.
     * @param value the value to fill with
     * @return a new BitVector with hiBit:loBit set to vector.
     * @throws IllegalArgumentException hiBit &lt; loBit, or hiBit &gt;= the length of
     *             this BitVector
     */

    public BitVector setBits(int hiBit, int loBit, long value)
    {
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.setBits(hiBit, loBit, value));
    }

    /**
     * Returns a new BitVector which is identical to this BitVector except bits
     * hiBit:loBit are set to the value represented by vector. vector is
     * truncated or zero-extended as appropriate to set its length to (hiBit -
     * loBit + 1).
     *
     * @param hiBit the most significant bit of the desired bitslice.
     * @param loBit the least significan bit of the desired bitslice.
     * @param vector the BitVector to fill with
     * @return a new BitVector with hiBit:loBit set to vector.
     * @throws IllegalArgumentException hiBit &lt; loBit, or hiBit &gt;= the length of
     *             this BitVector
     */

    public BitVector setBits(int hiBit, int loBit, BitVector vector)
    {
        BitVectorBuffer buf = new BitVectorBuffer(myBuffer);
        return new BitVector(buf.setBits(hiBit, loBit, vector));
    }

    /**
     * Check if this BitVector contains an X or Z value.
     *
     * @return true if this BitVector contains an X or Z value, false otherwise
     */
    public boolean containsXZ()
    {
        return myBuffer.containsXZ();
    }

    /**
     * Check if this BitVector should evaluate to false.
     *
     * @return true if every bit in this BitVector is a zero, false otherwise
     */
    public boolean isZero()
    {
        return myBuffer.isZero();
    }

    /**
     * Check if BitVector[bitPos] should evaluate to false.
     *
     * @param bitPos the bit position to test
     * @return true if every this[bitPos] is zero, false otherwise
     * @throws IllegalArgumentException bitPos is outside the size of this
     *             BitVector
     */
    public boolean isZero(int bitPos)
    {
        return myBuffer.isZero(bitPos);
    }

    /**
     * Check if this BitVector should evaluate to true.
     *
     * @return true if any bit in this BitVector is a 1, false otherwise.
     */
    public boolean isNotZero()
    {
        return myBuffer.isNotZero();
    }

    /**
     * Check if BitVector[bitPos] should evaluate to true.
     *
     * @param bitPos the bit position to test
     * @return true this[bitPos] is a 1, false otherwise.
     * @throws IllegalArgumentException bitPos is outside the size of this
     *             BitVector
     */
    public boolean isNotZero(int bitPos)
    {
        return myBuffer.isNotZero(bitPos);
    }

    /**
     * Converts this BitVector to an <code>double</code>. This conversion
     * is analogous to a <a
     * href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">
     * <i>narrowing primitive conversion </i> </a> from <code>long</code> to
     * <code>double</code> as defined in the <a
     * href="http://java.sun.com/docs/books/jls/html/">Java Language
     * Specification </a>: if this BitVector is too big to fit in an
     * <code>double</code>, only the low-order 64 bits are used. Note that
     * this conversion can lose information about the overall magnitude of the
     * BitVector value.
     *
     * @return this BitVectorBuffer converted to an <code>double</code>.
     * @throws XZException if an X/Z value is present in the BitVector
     */
    @Override
    public double doubleValue()
    {
        return myBuffer.doubleValue();
    }

    /**
     * Converts this BitVector to an <code>float</code>. This conversion
     * is analogous to a <a
     * href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">
     * <i>narrowing primitive conversion </i> </a> from <code>int</code> to
     * <code>float</code> as defined in the <a
     * href="http://java.sun.com/docs/books/jls/html/">Java Language
     * Specification </a>: if this BitVector is too big to fit in an
     * <code>float</code>, only the low-order 32 bits are used. Note that
     * this conversion can lose information about the overall magnitude of the
     * BitVector value.
     *
     * @return this BitVector converted to an <code>float</code>.
     * @throws XZException if an X/Z value is present in the BitVector
     */
    @Override
    public float floatValue()
    {
        return myBuffer.floatValue();
    }

    /**
     * Converts this BitVector to an <code>int</code>. This conversion is
     * analogous to a <a
     * href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">
     * <i>narrowing primitive conversion </i> </a> from <code>long</code> to
     * <code>int</code> as defined in the <a
     * href="http://java.sun.com/docs/books/jls/html/">Java Language
     * Specification </a>: if this BitVector is too big to fit in an
     * <code>int</code>, only the low-order 32 bits are returned. Note that
     * this conversion can lose information about the overall magnitude of the
     * BitVector value.
     *
     * @return this BitVector converted to an <code>int</code>.
     * @throws XZException if an X/Z value is present in the BitVector
     */
    @Override
    public int intValue()
    {
        return myBuffer.intValue();
    }

    /**
     * Converts this BitVector to a <code>long</code>. This conversion is
     * analogous to a <a
     * href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">
     * <i>narrowing primitive conversion </i> </a> from <code>long</code> to
     * <code>int</code> as defined in the <a
     * href="http://java.sun.com/docs/books/jls/html/">Java Language
     * Specification </a>: if this BitVector is too big to fit in a
     * <code>long</code>, only the low-order 64 bits are returned. Note that
     * this conversion can lose information about the overall magnitude of the
     * BitVector value.
     *
     * @return this BitVector converted to a <code>long</code>.
     * @throws XZException if an X/Z value is present in the BitVector
     */
    @Override
    public long longValue()
    {
        return myBuffer.longValue();
    }

    /**
     * Convert this BitVector to a String. This conversion uses the default
     * formatting values of {@link BitVectorFormat BitVectorFormat}.
     *
     * @return the String representation of this BitVector
     */
    @Override
    public String toString()
    {
        return myBuffer.toString();
    }

    /**
     * Convert this BitVector to a String with radix radix.
     *
     * @param radix Radix of the String representation.
     * @return the String representation of this BitVector with the given radix
     */
    public String toString(int radix)
    {
        return myBuffer.toString(radix);
    }

    /**
     * Return an array of bytes representing this BitVector. If the length of
     * this BitVector is not a multiple of 8, the most significant byte will be
     * padded with zeros in the uppermost bits.
     * <P>
     * The array returned will be in big-endian byte-order: the most significant
     * byte is in the zeroth element.
     * <P>
     * @return a byte array of the bytes comprising this BitVector.
     * @throws UnsupportedOperationException BitVector contains X/Z values
     */
    public byte[] getBytes()
    {
        return myBuffer.getBytes();
    }
}
