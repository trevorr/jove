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

/**
 * Provides static methods to convert between various types.
 * 
 * @author Trevor Robinson
 */
public final class ValueConverter
{
    private static final BitVector BIT_VECTOR_ZERO = new BitVector(1, Bit.ZERO);
    private static final BitVector BIT_VECTOR_ONE = new BitVector(1, Bit.ONE);
    private static final BitVector BIT_VECTOR_Z = new BitVector(1, Bit.Z);
    private static final BitVector BIT_VECTOR_X = new BitVector(1, Bit.X);

    /**
     * Converts a {@link Boolean} to a {@link Bit}.
     *
     * @param b the Boolean to convert
     * @return {@link Bit#ONE} if b is true, or {@link Bit#ZERO} if b is false
     */
    public static Bit booleanToBit(Boolean b)
    {
        return b.booleanValue() ? Bit.ONE : Bit.ZERO;
    }

    /**
     * Converts a {@link Boolean} to a {@link BitVector}.
     *
     * @param b the Boolean to convert
     * @return a BitVector with the value 1'b1 if b is true, or 1'b0 if b is false
     */
    public static BitVector booleanToBitVector(Boolean b)
    {
        return b.booleanValue() ? BIT_VECTOR_ONE : BIT_VECTOR_ZERO;
    }

    /**
     * Converts a {@link Bit} to a {@link BitVector}.
     *
     * @param b the Bit to convert
     * @return a BitVector of length 1 identical in value to b
     */
    public static BitVector bitToBitVector(Bit b)
    {
        switch (b.getID())
        {
        case Bit.ID_ZERO:
            return BIT_VECTOR_ZERO;
        case Bit.ID_ONE:
            return BIT_VECTOR_ONE;
        case Bit.ID_Z:
            return BIT_VECTOR_Z;
        default:
            return BIT_VECTOR_X;
        }
    }

    /**
     * Converts an {@link Object} to a {@link BitVector}. The following types
     * are valid for value: {@link BitVector}, {@link Bit}, {@link Boolean},
     * {@link Integer}, {@link Long}, {@link Double}, and {@link String}.
     * <P>
     * Converting from a Bit or Boolean results in a BitVector of width 1.<br>
     * Converting from an Integer results in a BitVector of width 32.<br>
     * Converting from a Long or Double results in a BitVector of width 64.<br>
     * Converting from a String results in a BitVector of width
     * <code>((String)value).length() * 8</code>.<br>
     *
     * @param value the Object to convert
     * @return a BitVector with width and value determined by the type and value
     * of <code>value</code>
     * @throws ClassCastException if the type of value is not supported
     */
    public static BitVector toBitVector(Object value)
    {
        if (value instanceof BitVector)
        {
            return (BitVector) value;
        }
        else if (value instanceof Bit)
        {
            return bitToBitVector((Bit) value);
        }
        else if (value instanceof Boolean)
        {
            return booleanToBitVector((Boolean) value);
        }
        else if (value instanceof Byte)
        {
            return new BitVector(8, ((Byte) value).byteValue());
        }
        else if (value instanceof Short)
        {
            return new BitVector(16, ((Short) value).shortValue());
        }
        else if (value instanceof Integer)
        {
            return new BitVector(32, ((Integer) value).intValue());
        }
        else if (value instanceof Long)
        {
            return new BitVector(64, ((Long) value).longValue());
        }
        else if (value instanceof Double)
        {
            return new BitVector(64, ((Double) value).longValue());
        }
        else if (value instanceof String)
        {
            return new BitVector(((String) value).getBytes());
        }
        else
        {
            throw new ClassCastException("Not a Verilog value type: "
                + describeObject(value));
        }
    }

    /**
     * Converts an {@link Object} to a {@link Bit}. The following types
     * are valid for value: {@link BitVector}, {@link Bit}, {@link Boolean},
     * {@link Integer}, {@link Long}, {@link Double}, and {@link String}.
     * <P>
     * In cases where the width of value is greater than 1, the lowest bit in
     * value is used as the value of the returned Bit.
     * <P>
     * If value is a Double, it is rounded before taking the lowest bit.<br>
     * If value is a String, the lowest bit of the first character is used.
     *
     * @param value the Object to convert
     * @return a Bit with value determined by the type and value of <code>value</code>
     * @throws ClassCastException if the type of value is not supported
     */
    public static Bit toBit(Object value)
    {
        if (value instanceof BitVector)
        {
            return ((BitVector) value).getBit(0);
        }
        else if (value instanceof Bit)
        {
            return (Bit) value;
        }
        else if (value instanceof Boolean)
        {
            return booleanToBit((Boolean) value);
        }
        else if (value instanceof Byte)
        {
            return Bit.getBitForID(((Byte) value).byteValue() & 1);
        }
        else if (value instanceof Short)
        {
            return Bit.getBitForID(((Short) value).shortValue() & 1);
        }
        else if (value instanceof Integer)
        {
            return Bit.getBitForID(((Integer) value).intValue() & 1);
        }
        else if (value instanceof Long)
        {
            return Bit.getBitForID(((Long) value).intValue() & 1);
        }
        else if (value instanceof Double)
        {
            double d = ((Double) value).doubleValue();
            return Bit.getBitForID((int) Math.round(d) & 1);
        }
        else if (value instanceof String)
        {
            return Bit.getBitForID(((String) value).charAt(0) & 1);
        }
        else
        {
            throw new ClassCastException("Not a Verilog value type: "
                + describeObject(value));
        }
    }

    /**
     * Converts an {@link Object} to an int. The following types
     * are valid for value: {@link BitVector}, {@link Bit}, {@link Boolean},
     * {@link Number}, and {@link String}.
     * <P>
     * If value is a Bit or BitVector which contains X/Z values, 0 is returned.
     *
     * @param value the Object to convert
     * @return an int with value determined by the type and value of <code>value</code>
     * @throws ClassCastException if the type of value is not supported
     */
    public static int toInt(Object value)
    {
        if (value instanceof BitVector)
        {
            // convert X/Z to 0, like vpi_get_value
            BitVector bv = (BitVector) value;
            return !bv.containsXZ() ? bv.intValue() : 0;
        }
        else if (value instanceof Bit)
        {
            // convert X/Z to 0, like vpi_get_value
            int id = ((Bit) value).getID();
            return (id <= 1) ? id : 0;
        }
        else if (value instanceof Boolean)
        {
            return ((Boolean) value).booleanValue() ? 1 : 0;
        }
        else if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        else if (value instanceof String)
        {
            return stringToInt((String) value);
        }
        else
        {
            throw new ClassCastException("Not a Verilog value type: "
                + describeObject(value));
        }
    }

    /**
     * Converts an {@link Object} to a long. The following types
     * are valid for value: {@link BitVector}, {@link Bit}, {@link Boolean},
     * {@link Number}, and {@link String}.
     * <P>
     * If value is a Bit or BitVector which contains X/Z values, 0 is returned.
     *
     * @param value the Object to convert
     * @return a long  with value determined by the type and value of <code>value</code>
     * @throws ClassCastException if the type of value is not supported
     */
    public static long toLong(Object value)
    {
        if (value instanceof BitVector)
        {
            // convert X/Z to 0, like vpi_get_value
            BitVector bv = (BitVector) value;
            return !bv.containsXZ() ? bv.longValue() : 0;
        }
        else if (value instanceof Bit)
        {
            // convert X/Z to 0, like vpi_get_value
            int id = ((Bit) value).getID();
            return (id <= 1) ? id : 0;
        }
        else if (value instanceof Boolean)
        {
            return ((Boolean) value).booleanValue() ? 1L : 0;
        }
        else if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        else if (value instanceof String)
        {
            return stringToLong((String) value);
        }
        else
        {
            throw new ClassCastException("Not a Verilog value type: "
                + describeObject(value));
        }
    }

    /**
     * Converts an {@link Object} to a double. The following types
     * are valid for value: {@link BitVector}, {@link Bit}, {@link Boolean},
     * {@link Number}, and {@link String}.
     * <P>
     * If value is a Bit or BitVector which contains X/Z values, {@link Double#NaN}
     * is returned.
     *
     * @param value the Object to convert
     * @return a double with value determined by the type and value of <code>value</code>
     * @throws ClassCastException if the type of value is not supported
     */
    public static double toDouble(Object value)
    {
        if (value instanceof BitVector)
        {
            BitVector bv = (BitVector) value;
            return !bv.containsXZ() ? Double.longBitsToDouble(bv.longValue())
                : Double.NaN;
        }
        else if (value instanceof Bit)
        {
            int id = ((Bit) value).getID();
            return (id <= 1) ? id : Double.NaN;
        }
        else if (value instanceof Boolean)
        {
            return ((Boolean) value).booleanValue() ? 1.0 : 0.0;
        }
        else if (value instanceof Number)
        {
            return ((Number) value).doubleValue();
        }
        else if (value instanceof String)
        {
            return stringToLong((String) value);
        }
        else
        {
            throw new ClassCastException("Not a Verilog value type: "
                + describeObject(value));
        }
    }

    private static int stringToInt(String s)
    {
        char[] chars = new char[4];
        s.getChars(0, Math.min(4, s.length()), chars, 0);
        return (chars[0] & 0xFF) | ((chars[1] & 0xFF) << 8)
            | ((chars[2] & 0xFF) << 16) | ((chars[3] & 0xFF) << 24);
    }

    private static long stringToLong(String s)
    {
        char[] chars = new char[8];
        s.getChars(0, Math.min(8, s.length()), chars, 0);
        return (chars[0] & 0xFF) | ((long) (chars[1] & 0xFF) << 8)
            | ((long) (chars[2] & 0xFF) << 16)
            | ((long) (chars[3] & 0xFF) << 24)
            | ((long) (chars[4] & 0xFF) << 32)
            | ((long) (chars[5] & 0xFF) << 40)
            | ((long) (chars[6] & 0xFF) << 48)
            | ((long) (chars[7] & 0xFF) << 56);
    }

    private static String describeObject(Object o)
    {
        return o != null ? o + " [" + o.getClass().getName() + "]" : "null";
    }
}
