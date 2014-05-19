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

import java.util.Random;

import junit.framework.TestCase;

import com.newisys.random.PRNG;
import com.newisys.random.PRNGFactoryFactory;

// TODO testcase for: modulo (<=64 bits, > 64bits, X, Z, 0)
// TODO testcase for: BitVector.multiply( <=64 bits, > 64bits, X, Z, 0)
// TODO testcase for: byte[] constructor bug (signed bytes fixed in BitVectorBuffer.java 1.32)

public class BitVectorTest
    extends TestCase
{
    private BitVector bv;
    private BitVectorFormat myFmt;

    @Override
    public void setUp()
    {
        myFmt = new BitVectorFormat();
        myFmt.setRadix(2);
        myFmt.setUnderscoreFreq(4);
    }

    public void testBitRange()
    {
        BitRange range = new BitRange(0, 0);
        assertEquals(0, range.high());
        assertEquals(0, range.low());

        range = new BitRange(44, 13);
        assertEquals(44, range.high());
        assertEquals(13, range.low());

        try
        {
            range = new BitRange(0, 4);
            assertTrue(false);
        }
        catch (IllegalArgumentException e)
        {
            // good.
        }
    }

    public void testBitVectorInt()
    {
        bv = new BitVector(33);
        assertEquals(true, bv.equalsExact(new BitVector(
            "33'bx_xxxx_xxxx_xxxx_xxxx")));
    }

    public void testBitVectorIntInt()
    {
        BitVector vector = new BitVector(8, 0xffffff98);
        assertTrue(vector.equalsExact(new BitVector("8'h98")));
        vector = new BitVector(12, -3);
        assertTrue(vector.equalsExact(new BitVector("12'hffd")));

        vector = new BitVector("40'hfcffffffff").add(new BitVector(32, 1));
        assertTrue(vector.equalsExact(new BitVector("40'hfd00000000")));

        vector = new BitVector(32, -1);
        assertTrue(vector.equalsExact(new BitVector("32'hffffffff")));
    }

    public void testBitVectorIntIntBoolean()
    {
        BitVector vector = new BitVector(33, 0xffffff98);
        assertTrue(vector.equalsExact(new BitVector("33'h1ffffff98")));
        vector = new BitVector(33, 0xffffff98, false);
        assertTrue(vector.equalsExact(new BitVector("33'hffffff98")));
        vector = new BitVector(33, 0xffffff98, true);
        assertTrue(vector.equalsExact(new BitVector("33'h1ffffff98")));
    }

    public void testBitVectorIntLong()
    {
        BitVector vector = new BitVector(8, 0xffffffffffffff98L);
        assertTrue(vector.equalsExact(new BitVector("8'h98")));

        vector = new BitVector(40, 0xffffffffffffff98L);
        assertTrue(vector.equalsExact(new BitVector("40'hffffffff98")));

        vector = new BitVector(64, -1);
        assertTrue(vector.equalsExact(new BitVector("64'hffffffffffffffff")));
    }

    public void testBitVectorIntLongBoolean()
    {
        BitVector vector = new BitVector(96, 0xffffff98);
        assertTrue(vector.equalsExact(new BitVector(
            "96'hffffffffffffffffffffff98")));
        vector = new BitVector(96, 0xffffff98, false);
        assertTrue(vector.equalsExact(new BitVector("96'hffffff98")));
        vector = new BitVector(96, 0xffffff98, true);
        assertTrue(vector.equalsExact(new BitVector(
            "96'hffffffffffffffffffffff98")));
    }

    public void testBitVectorStringInt()
    {
        BitVector vector;

        vector = new BitVector("'h14");
        assertEquals(32, vector.length());

        vector = new BitVector("44'h1234567890a", 32);
        assertEquals("32'h4567890a", vector.toString());

        vector = new BitVector("'d015");
        assertEquals(32, vector.length());

        // this should print a warning to stderr
        vector = new BitVector("'h123456789");
        assertEquals("32'h23456789", vector.toString());
    }

    public void testBitVectorIntArrIntArrInt()
    {
        int[] values = { 0x0F0F0F0F, 0x0F0F0F0F, 0x0F0F0F0F };
        int[] xzmask = { 0xA5A5A5A5, 0xA5A5A5A5, 0xA5A5A5A5 };
        bv = new BitVector(values, xzmask, 38);
        assertEquals("38'bz01x1xz0z01x1xz0z01x1xz0z01x1xz0z01x1x", bv
            .toString(2));
    }

    public void testBitVectorByteArr()
    {
        BitVector bv;
        bv = new BitVector(new byte[] { 0x45, 0x30, 0x15, 0x20, 0x33 });
        assertEquals("40'h4530152033", bv.toString());
    }

    public void testBitVectorBit()
    {
        BitVector bv;
        bv = new BitVector(Bit.ONE);
        assertEquals("1'b1", bv.toString(2));
        bv = new BitVector(Bit.ZERO);
        assertEquals("1'b0", bv.toString(2));
        bv = new BitVector(Bit.X);
        assertEquals("1'bx", bv.toString(2));
        bv = new BitVector(Bit.Z);
        assertEquals("1'bz", bv.toString(2));
    }

    public void testBitVectorIntBit()
    {
        BitVector bv;
        bv = new BitVector(1, Bit.ONE);
        assertEquals("1'b1", bv.toString(2));
        bv = new BitVector(32, Bit.ZERO);
        assertEquals("32'h0", bv.toString(16));
        bv = new BitVector(16, Bit.X);
        assertEquals("16'hX", bv.toString(16));
        bv = new BitVector(36, Bit.Z);
        assertEquals("36'hZ", bv.toString(16));
    }

    public void testLength()
    {
        int lengths[] = { 1, 32, 33, 64, 128, 129 };
        int i;

        for (i = 0; i < lengths.length; i++)
        {
            BitVectorBuffer bvf;

            // BitVector(int)
            bv = new BitVector(lengths[i]);
            assertEquals(lengths[i], bv.length());

            // BitVector(int, int)
            bv = new BitVector(lengths[i], 1);
            assertEquals(lengths[i], bv.length());

            // BitVector(int, long);
            bv = new BitVector(lengths[i], (long) 1);
            assertEquals(lengths[i], bv.length());

            // BitVector(String)
            bv = new BitVector(lengths[i] + "'h1");
            assertEquals(lengths[i], bv.length());

            // BitVector(BitVectorBuffer)
            bvf = new BitVectorBuffer(lengths[i]);
            bv = new BitVector(bvf);
            assertEquals(lengths[i], bv.length());
        }
    }

    public void testEquals()
    {
        bv = new BitVector("16'b0000_1111_xxxx_zzzz");
        BitVector bv2 = new BitVector("16'b0000_1111_xxxx_zzzz");
        BitVector bv3 = new BitVector("16'b0x00_1zz1_10xx_zz1z");
        BitVector bv4 = new BitVector("8'hff");
        BitVector bv5 = new BitVector("4'hf");
        BitVector bv6 = new BitVector("8'b1111_1111");
        String str = new String("kaboom");

        assertEquals(false, bv.equals(bv2));
        assertEquals(false, bv.equals(bv3));
        assertEquals(false, bv4.equals(bv5));
        assertEquals(true, bv4.equals(bv6));
        assertEquals(false, bv.equals(str));

        bv = new BitVector(64, 2);
        bv2 = new BitVector(32, 2);
        assertEquals(true, bv.equals(bv2));
        assertEquals(true, bv2.equals(bv));
    }

    public void testEqualsMask()
    {
        bv = new BitVector("16'b0000_1111_xxxx_zzzz");
        BitVector bv2 = new BitVector("16'b0000_1111_xxxx_zzzz");
        BitVector bv3 = new BitVector("16'b0x00_1zz1_10xx_zz1z");
        BitVector bv4 = new BitVector("8'hff");
        BitVector bv5 = new BitVector("4'hf");
        BitVector bv6 = new BitVector("8'b1111_1111");

        assertEquals("16'b1111111100000000", bv.equalsMask(bv2).toString(2));
        assertEquals("16'b1011100100000000", bv.equalsMask(bv3).toString(2));
        assertEquals("16'b1111000000000000", bv.equalsMask(bv4).toString(2));
        assertEquals("16'b1111000000000000", bv.equalsMask(bv5).toString(2));
        assertEquals("16'b1111000000000000", bv.equalsMask(bv6).toString(2));
    }

    public void testEqualsExact()
    {
        bv = new BitVector("16'b0000_1111_xxxx_zzzz");
        BitVector bv2 = new BitVector("16'b0000_1111_xxxx_zzzz");
        BitVector bv3 = new BitVector("16'b0x00_1zz1_10xx_zz1z");
        BitVector bv4 = new BitVector("8'hff");
        BitVector bv5 = new BitVector("4'hf");
        BitVector bv6 = new BitVector("8'b1111_1111");
        BitVector bv7 = new BitVector("16'hff");

        assertEquals(true, bv.equalsExact(bv2));
        assertEquals(false, bv.equalsExact(bv3));
        assertEquals(false, bv4.equalsExact(bv5));
        assertEquals(true, bv4.equalsExact(bv6));
        assertEquals(true, bv6.equalsExact(bv7));
    }

    public void testEqualsExactMask()
    {
        bv = new BitVector("16'b0000_1111_xxxx_zzzz");
        BitVector bv2 = new BitVector("16'b0000_1111_xxxx_zzzz");
        BitVector bv3 = new BitVector("16'b0x00_1zz1_10xx_zz1z");
        BitVector bv4 = new BitVector("16'hff");
        BitVector bv5 = new BitVector("16'hf");
        BitVector bv6 = new BitVector("16'b1111_1111");

        assertEquals("16'b1111111111111111", bv.equalsExactMask(bv2)
            .toString(2));
        assertEquals("16'b1011100100111101", bv.equalsExactMask(bv3)
            .toString(2));
        assertEquals("16'b1111000000000000", bv.equalsExactMask(bv4)
            .toString(2));
        assertEquals("16'b1111000000000000", bv.equalsExactMask(bv5)
            .toString(2));
        assertEquals("16'b1111000000000000", bv.equalsExactMask(bv6)
            .toString(2));
    }

    public void testEqualsWild()
    {
        bv = new BitVector("16'b0000_1111_xxxx_zzzz");
        BitVector bv2 = new BitVector("16'b0000_1111_xxxx_zzzz");
        BitVector bv3 = new BitVector("16'b0x00_1zz1_10xx_zz1z");
        BitVector bv4 = new BitVector("8'hff");
        BitVector bv5 = new BitVector("4'hf");
        BitVector bv6 = new BitVector("8'b1111_1111");

        assertEquals(true, bv.equalsWild(bv2));
        assertEquals(true, bv.equalsWild(bv3));
        assertEquals(false, bv4.equalsWild(bv5));
        assertEquals(true, bv4.equals(bv6));
    }

    public void testEqualsWildMask()
    {
        bv = new BitVector("16'b0000_1111_xxxx_zzzz");
        BitVector bv2 = new BitVector("16'b0000_1111_xxxx_zzzz");
        BitVector bv3 = new BitVector("16'b0x00_1zz1_10xx_zz1z");
        BitVector bv4 = new BitVector("8'hff");
        BitVector bv5 = new BitVector("4'hf");
        BitVector bv6 = new BitVector("8'b1111_1111");

        assertEquals("16'b1111111111111111", bv.equalsWildMask(bv2).toString(2));
        assertEquals("16'b1111111111111111", bv.equalsWildMask(bv3).toString(2));
        assertEquals("16'b1111000011111111", bv.equalsWildMask(bv4).toString(2));
        assertEquals("16'b1111000011111111", bv.equalsWildMask(bv5).toString(2));
        assertEquals("16'b1111000011111111", bv.equalsWildMask(bv6).toString(2));

    }

    public void testAnd()
    {
        bv = new BitVector("16'b0000_1111_xxxx_zzzz");
        BitVector bv2 = new BitVector("16'b01xz_01xz_01xz_01xz");
        myFmt.setRadix(2);
        myFmt.setUnderscoreFreq(4);
        assertEquals("16'b1xx_0xxx_0xxx", myFmt.format(bv.and(bv2)));
    }

    public void testAndNot()
    {
        bv = new BitVector("16'b0000_1111_xxxx_zzzz");
        BitVector bv2 = new BitVector("16'b01xz_01xz_01xz_01xz");
        myFmt.setRadix(2);
        myFmt.setUnderscoreFreq(4);
        assertEquals("16'b10xx_x0xx_x0xx", myFmt.format(bv.andNot(bv2)));
        assertEquals(bv.andNot(bv2).toString(), bv.and(bv2.not()).toString());
        assertEquals("16'b1xx_0000_0xxx_0xxx", myFmt.format(bv2.andNot(bv)));
        assertEquals(bv2.andNot(bv).toString(), bv2.and(bv.not()).toString());

        BitVectorBuffer bvb = new BitVectorBuffer("64'h12345678");
        bv = new BitVector("36'hfffffffff");
        bvb.setLength(16);
        assertEquals("36'h0", bvb.andNot(bv).toString());

    }

    public void testOr()
    {
        bv = new BitVector("16'b0000_1111_xxxx_zzzz");
        BitVector bv2 = new BitVector("16'b01xz_01xz_01xz_01xz");
        myFmt.setRadix(2);
        myFmt.setUnderscoreFreq(4);
        assertEquals("16'b1xx_1111_x1xx_x1xx", myFmt.format(bv.or(bv2)));
    }

    public void testXor()
    {
        bv = new BitVector("16'b0000_1111_xxxx_zzzz");
        BitVector bv2 = new BitVector("16'b01xz_01xz_01xz_01xz");
        myFmt.setRadix(2);
        myFmt.setUnderscoreFreq(4);
        assertEquals("16'b1xx_10xx_xxxx_xxxx", myFmt.format(bv.xor(bv2)));
    }

    public void testNot()
    {
        bv = new BitVector("4'b01xz");
        myFmt.setRadix(2);
        myFmt.setUnderscoreFreq(4);
        assertEquals("4'b10xx", myFmt.format(bv.not()));
    }

    public void testConcat()
    {
        bv = new BitVector("33'h123456789");
        BitVector bv2 = new BitVector("32'habcdef01");
        BitVector bv3 = bv.concat(bv2);
        assertEquals(bv.length() + bv2.length(), bv3.length());
        assertEquals("65'h123456789abcdef01", bv3.toString(16));

        bv = BitVector.concat(new BitVector(1, 1), new BitVector(5, 0));
        assertEquals("6'h20", bv.toString());

        bv = BitVector.concat(new BitVector("30'h20000000"),
            new BitVector(5, 0));
        assertEquals("35'h400000000", bv.toString());

        bv = BitVector.concat(new BitVector("36'h100000000"), new BitVector(
            "36'h100000000"));
        assertEquals("72'h100000000100000000", bv.toString());

        try
        {
            bv = BitVector.concat();
            assertFalse(true);
        }
        catch (IllegalArgumentException e)
        {
            // good dog.
        }

        byte b = 3;
        short s = 5;
        bv = BitVector.concat(s, 6, 0x100000000L, Bit.X, b);
        assertEquals("121'ha0000000c000000020000000?03", bv.toString());
    }

    public void testReplicate()
    {
        bv = new BitVector(1, 1);
        BitVector bv2 = bv.replicate(67);
        assertEquals("67'h7ffffffffffffffff", bv2.toString());

        bv = new BitVector("36'hfedcba987");
        bv2 = bv.replicate(3);
        assertEquals("108'hfedcba987fedcba987fedcba987", bv2.toString());
    }

    public void testShiftLeft()
    {
        bv = new BitVector(1, 1);
        assertEquals(0, bv.shiftLeft(1).intValue());

        bv = new BitVector(33, 0xa5a5a5a5);
        assertEquals(0x14b4b4b4aL, bv.shiftLeft(1).longValue());

        bv = new BitVector("5'b10xz1");
        assertEquals("5'h1?", bv.shiftLeft(0).toString(16));

        bv = new BitVector(65, 1);
        assertEquals("65'h10000000000000000", bv.shiftLeft(64).toString(16));

        bv = new BitVector("78'h0000fa5c623f8c205dde");
        bv = bv.getBits(63, 26).setLength(40, Bit.ZERO);

        assertEquals("40'h3e97188fe3", bv.toString());
        assertEquals("40'hfa5c623f8c", bv.shiftLeft(2).toString());

        bv = new BitVector(40, 0x40);
        assertEquals("40'h40", bv.shiftLeft(0).toString());

        bv = new BitVector("576'h19_b4de_85b4_6756_c11b");
        bv = bv.shiftLeft(288);
        assertTrue(bv.getBits(575, 360).isZero());
        assertEquals("72'h19b4de85b46756c11b", bv.getBits(359, 288).toString());
        assertTrue(bv.getBits(287, 0).isZero());
    }

    public void testShiftRight()
    {
        bv = new BitVector(1, 1);
        assertEquals(0, bv.shiftRight(1).intValue());

        bv = new BitVector(33, 0x14b4b4b4aL);
        assertEquals(0xa5a5a5a5L, bv.shiftRight(1).longValue());

        bv = new BitVector("5'b10xz1");
        assertEquals("5'h1?", bv.shiftRight(0).toString(16));

        bv = new BitVector("65'h10000000000000000");
        assertEquals("65'h1", bv.shiftRight(64).toString(16));

        bv = new BitVector(40, 0x40);
        assertEquals("40'h40", bv.shiftRight(0).toString());

        bv = new BitVector(1152, 0);
        bv = bv.setBits(647, 576, new BitVector("72'h19b4de85b46756c11b"));
        bv = bv.shiftRight(576);
        assertTrue(bv.getBits(1151, 72).isZero());
        assertEquals("72'h19b4de85b46756c11b", bv.getBits(71, 0).toString());
    }

    public void testGetBits()
    {
        bv = new BitVector("65'h10123456789abcdef");

        // straddle an int boundary
        assertEquals(2, bv.getBits(64, 63).intValue());
        // straddle 2 int boundaries
        assertEquals(0x202468acfL, bv.getBits(64, 31).longValue());
        // no shift
        assertEquals("65'h10123456789abcdef", bv.getBits(64, 0).toString(16));

        // straddle an int boundary
        assertEquals(2, bv.getBits(new BitRange(64, 63)).intValue());
        // straddle 2 int boundaries
        assertEquals(0x202468acfL, bv.getBits(new BitRange(64, 31)).longValue());
        // no shift
        assertEquals("65'h10123456789abcdef", bv.getBits(new BitRange(64, 0))
            .toString(16));

    }

    public void testGetBit()
    {
        bv = new BitVector(65);
        assertEquals(Bit.X, bv.getBit(63));

        bv = new BitVector(65, 1);
        bv = bv.shiftLeft(64);
        assertEquals(Bit.ONE, bv.getBit(64));
        assertEquals(Bit.ZERO, bv.getBit(0));
    }

    public void testGetBitCount()
    {
        assertEquals(65, new BitVector(65).getBitCount(Bit.X));
        assertEquals(0, new BitVector(65, 0).getBitCount(Bit.X));
        assertEquals(6, new BitVector(65).getBitCount(34, 29, Bit.X));
        assertEquals(6, new BitVector(65).getBitCount(64, 59, Bit.X));
        assertEquals(6, new BitVector(65).getBitCount(5, 0, Bit.X));
        assertEquals(6, new BitVector(65)
            .getBitCount(new BitRange(5, 0), Bit.X));

        assertEquals(65, new BitVector(65, Bit.Z).getBitCount(Bit.Z));
        assertEquals(0, new BitVector(65, 0).getBitCount(Bit.Z));
        assertEquals(6, new BitVector(65, Bit.Z).getBitCount(34, 29, Bit.Z));
        assertEquals(6, new BitVector(65, Bit.Z).getBitCount(64, 59, Bit.Z));
        assertEquals(6, new BitVector(6, Bit.Z).getBitCount(5, 0, Bit.Z));
        assertEquals(6, new BitVector(6, Bit.Z).getBitCount(new BitRange(5, 0),
            Bit.Z));

        assertEquals(65, new BitVector(65, Bit.ONE).getBitCount(Bit.ONE));
        assertEquals(0, new BitVector(65, 0).getBitCount(Bit.ONE));
        assertEquals(6, new BitVector(65, Bit.ONE).getBitCount(34, 29, Bit.ONE));
        assertEquals(6, new BitVector(65, Bit.ONE).getBitCount(64, 59, Bit.ONE));
        assertEquals(6, new BitVector(6, Bit.ONE).getBitCount(5, 0, Bit.ONE));
        assertEquals(6, new BitVector(6, Bit.ONE).getBitCount(
            new BitRange(5, 0), Bit.ONE));

        assertEquals(65, new BitVector(65, Bit.ZERO).getBitCount(Bit.ZERO));
        assertEquals(0, new BitVector(65, Bit.X).getBitCount(Bit.ZERO));
        assertEquals(6, new BitVector(65, Bit.ZERO).getBitCount(34, 29,
            Bit.ZERO));
        assertEquals(6, new BitVector(65, Bit.ZERO).getBitCount(64, 59,
            Bit.ZERO));
        assertEquals(6, new BitVector(6, Bit.ZERO).getBitCount(5, 0, Bit.ZERO));
        assertEquals(6, new BitVector(6, Bit.ZERO).getBitCount(new BitRange(5,
            0), Bit.ZERO));

        assertEquals(4, new BitVector("16'b1010_1x0x_1z0z_zxzx")
            .getBitCount(Bit.ZERO));
        assertEquals(4, new BitVector("16'b1010_1x0x_1z0z_zxzx")
            .getBitCount(Bit.ONE));
        assertEquals(4, new BitVector("16'b1010_1x0x_1z0z_zxzx")
            .getBitCount(Bit.Z));
        assertEquals(4, new BitVector("16'b1010_1x0x_1z0z_zxzx")
            .getBitCount(Bit.X));

    }

    public void testSetBits()
    {
        bv = new BitVector("65'h00123456789abcdef");

        // straddle an int boundary
        assertEquals("65'h18123456789abcdef", bv.setBits(64, 63,
            new BitVector(2, 3)).toString(16));
        // straddle 2 int boundaries
        assertEquals("65'h1abcdef0129abcdef", bv.setBits(64, 28,
            new BitVector("37'h1abcdef012")).toString(16));
        // no shift
        assertEquals("65'h555533330000XXXX", bv.setBits(64, 0,
            new BitVector("65'h0555533330000xxxx")).toString(16));

        // straddle an int boundary
        assertEquals("65'h18123456789abcdef", bv.setBits(new BitRange(64, 63),
            new BitVector(2, 3)).toString(16));
        // straddle 2 int boundaries
        assertEquals("65'h1abcdef0129abcdef", bv.setBits(new BitRange(64, 28),
            new BitVector("37'h1abcdef012")).toString(16));
        // no shift
        assertEquals("65'h555533330000XXXX", bv.setBits(new BitRange(64, 0),
            new BitVector("65'h0555533330000xxxx")).toString(16));

        // straddle an int boundary
        assertEquals("65'h18123456789abcdef", bv.setBits(64, 63, 3)
            .toString(16));
        // straddle 2 int boundaries
        assertEquals("65'h1abcdef0129abcdef", bv.setBits(64, 28, 0x1abcdef012L)
            .toString(16));
        // sign-extend
        assertEquals("65'h1ffffffffffffffff", bv.setBits(64, 0, ~0)
            .toString(16));

    }

    public void testSetBit()
    {
        bv = new BitVector(65);
        bv = bv.setBit(63, Bit.Z);
        assertEquals(Bit.Z, bv.getBit(63));

        bv = bv.setBit(64, Bit.ONE);
        assertEquals(Bit.Z, bv.getBit(63));
        assertEquals(Bit.ONE, bv.getBit(64));

        bv = bv.setBit(0, Bit.ZERO);
        assertEquals(Bit.ZERO, bv.getBit(0));
        assertEquals(Bit.Z, bv.getBit(63));
        assertEquals(Bit.ONE, bv.getBit(64));

        BitVectorBuffer bvb = new BitVectorBuffer(8, 0);
        bvb.setBit(1, Bit.ONE);
        bvb.setBit(3, Bit.ONE);
        assertEquals(0xa, bvb.intValue());
    }

    public void testContainsXZ()
    {
        bv = new BitVector(1);
        assertEquals(true, bv.containsXZ());
        bv = bv.setBits(0, 0, new BitVector("1'b1"));
        assertEquals(false, bv.containsXZ());
    }

    public void testIsZero()
    {
        assertEquals(true, new BitVector("2'b00").isZero());
        assertEquals(false, new BitVector("2'b10").isZero());
        assertEquals(false, new BitVector("2'bx0").isZero());
        assertEquals(false, new BitVector("2'bz0").isZero());

        assertEquals(true, new BitVector("2'b00").isZero(1));
        assertEquals(false, new BitVector("2'b10").isZero(1));
        assertEquals(false, new BitVector("2'bx0").isZero(1));
        assertEquals(false, new BitVector("2'bz0").isZero(1));

        assertEquals(true, new BitVector(
            "33'b0_0000_0000_0000_0000_0000_0000_0000_0000").isZero(32));
        assertEquals(false, new BitVector(
            "33'b1_0000_0000_0000_0000_0000_0000_0000_0000").isZero(32));
        assertEquals(false, new BitVector(
            "33'bx_0000_0000_0000_0000_0000_0000_0000_0000").isZero(32));
        assertEquals(false, new BitVector(
            "33'bz_0000_0000_0000_0000_0000_0000_0000_0000").isZero(32));
    }

    public void testTest()
    {
        new BitVector("33'b0_0000_0000_0000_0000_0000_0000_0000_0000");
    }

    public void testIsNotZero()
    {
        assertEquals(false, new BitVector("2'b00").isNotZero());
        assertEquals(true, new BitVector("2'b10").isNotZero());
        assertEquals(false, new BitVector("2'bx0").isNotZero());
        assertEquals(false, new BitVector("2'bz0").isNotZero());

        assertEquals(false, new BitVector("2'b00").isNotZero(1));
        assertEquals(true, new BitVector("2'b10").isNotZero(1));
        assertEquals(false, new BitVector("2'bx0").isNotZero(1));
        assertEquals(false, new BitVector("2'bz0").isNotZero(1));

        assertEquals(false, new BitVector(
            "33'b0_0000_0000_0000_0000_0000_0000_0000_0000").isNotZero(32));
        assertEquals(true, new BitVector(
            "33'b1_0000_0000_0000_0000_0000_0000_0000_0000").isNotZero(32));
        assertEquals(false, new BitVector(
            "33'bx_0000_0000_0000_0000_0000_0000_0000_0000").isNotZero(32));
        assertEquals(false, new BitVector(
            "33'bz_0000_0000_0000_0000_0000_0000_0000_0000").isNotZero(32));
    }

    public void testIntValue()
    {
        bv = new BitVector(34, 0x301020304L);
        assertEquals(0x1020304, bv.intValue());
    }

    public void testLongValue()
    {
        bv = new BitVector(34, 0x301020304L);
        assertEquals(0x301020304L, bv.longValue());

        bv = new BitVector(1, 1);
        assertEquals(1, bv.longValue());
    }

    public void testToString()
    {
        bv = new BitVector("8'b01xz_0101");
        assertEquals("8'h?5", bv.toString());
    }

    public void testToStringInt()
    {
        bv = new BitVector("8'b01xz_0101");
        assertEquals("8'o1?5", bv.toString(8));
    }

    public void testExtend()
    {
        bv = new BitVector("31'h12345678");
        BitVector tmp = bv.extend(8, Bit.ZERO);
        assertEquals("31'h12345678", tmp.toString());

        tmp = bv.extend(31, Bit.ONE);
        assertEquals("31'h12345678", tmp.toString());

        tmp = bv.extend(32, Bit.X);
        assertEquals("32'h?2345678", tmp.toString());
        assertEquals("4'bx001", tmp.getBits(31, 28).toString(2));

        tmp = bv.extend(65, Bit.ZERO);
        assertEquals("65'h12345678", tmp.toString());
        assertEquals("33'h0", tmp.getBits(64, 32).toString());

        BitVectorBuffer buf = new BitVectorBuffer("31'h12345678");
        buf.extend(8, Bit.ZERO);
        assertEquals("31'h12345678", buf.toString());

        buf.extend(31, Bit.ONE);
        assertEquals("31'h12345678", buf.toString());

        buf.extend(32, Bit.X);
        assertEquals("32'h?2345678", buf.toString());
        assertEquals("4'bx001", buf.getBits(31, 28).toString(2));

        buf.extend(65, Bit.ZERO);
        assertEquals("65'h?2345678", buf.toString());
        assertEquals("33'h0", buf.getBits(64, 32).toString());
    }

    public void testSetLength()
    {
        bv = new BitVector("8'ha5");
        bv = bv.setLength(6);
        assertEquals("6'h25", bv.toString());

        bv = new BitVector("8'bx101_0000");
        bv = bv.setLength(16);
        assertEquals("16'h?0", bv.toString());

        bv = new BitVector("8'b0101_0000");
        bv = bv.setLength(16);
        assertEquals("16'h50", bv.toString());

        bv = new BitVector("7'b101_0000");
        bv = bv.setLength(9, Bit.ZERO);
        assertEquals("9'b1010000", bv.toString(2));

        bv = new BitVector("7'b101_0000");
        bv = bv.setLength(9, Bit.ONE);
        assertEquals("9'b111010000", bv.toString(2));

        bv = new BitVector("7'b101_0000");
        bv = bv.setLength(9, Bit.Z);
        assertEquals("9'bz1010000", bv.toString(2));

        bv = new BitVector("7'b101_0000");
        bv = bv.setLength(9, Bit.X);
        assertEquals("9'bx1010000", bv.toString(2));

        bv = new BitVector("7'b101_0000");
        bv = bv.setLength(33, Bit.ZERO);
        assertEquals("33'h50", bv.toString());

        bv = new BitVector("7'b101_0000");
        bv = bv.setLength(33, Bit.ONE);
        assertEquals("33'h1ffffffd0", bv.toString());

        bv = new BitVector("7'b101_0000");
        bv = bv.setLength(33, Bit.Z);
        assertEquals("33'hZ?0", bv.toString());

        bv = new BitVector("7'b101_0000");
        bv = bv.setLength(33, Bit.X);
        assertEquals("33'hX?0", bv.toString());
    }

    public void testSetLengthHigh()
    {
        bv = new BitVector("8'b0101_1111");
        bv = bv.setLengthHigh(32);
        assertEquals("32'h5f", bv.toString());

        bv = new BitVector("8'b0101_1111");
        bv = bv.setLengthHigh(64);
        assertEquals("64'h5f", bv.toString());

        bv = new BitVector("8'b1101_1111");
        bv = bv.setLengthHigh(32);
        assertEquals("32'hffffffdf", bv.toString());

        bv = new BitVector("8'b1101_1111");
        bv = bv.setLengthHigh(64);
        assertEquals("64'hffffffffffffffdf", bv.toString());

        bv = new BitVector("8'bz101_1111");
        bv = bv.setLengthHigh(32);
        assertEquals("32'hZ?f", bv.toString());

        bv = new BitVector("8'bz101_1111");
        bv = bv.setLengthHigh(64);
        assertEquals("64'hZ?f", bv.toString());

        bv = new BitVector("8'bx101_1111");
        bv = bv.setLengthHigh(32);
        assertEquals("32'hX?f", bv.toString());

        bv = new BitVector("8'bx101_1111");
        bv = bv.setLengthHigh(64);
        assertEquals("64'hX?f", bv.toString());
    }

    public void testByteAccessors()
    {
        StringBuffer sb = new StringBuffer("This is a test.");
        byte[] bytes;

        bv = new BitVector(sb.toString().getBytes());

        assertEquals("This is a test.", new String(bv.getBytes()));
        bv = new BitVector("1'bx");
        try
        {
            bytes = bv.getBytes();
            fail("Exception not thrown for getBytes() on a BitVector w/ X/Z");
        }
        catch (XZException e)
        {
            // do nothing
        }

        bv = new BitVector("13'b1_1010_1011_1100");
        bytes = bv.getBytes();
        assertEquals(2, bytes.length);
        assertEquals((byte) 0x1A, bytes[0]);
        assertEquals((byte) 0xBC, bytes[1]);

        bv = new BitVector(32, -1);
        assertEquals(4, bv.getBytes().length);

        bv = new BitVector(32, 1);
        assertEquals(4, bv.getBytes().length);

        bv = new BitVector("64'hffff_ffff_ffff_ffff");
        assertEquals(8, bv.getBytes().length);
    }

    public void testSetXSetZ()
    {
        BitVector a;
        BitVector b;
        BitVector c;

        // TEST: setX() normal values
        a = new BitVector("6'b001101");
        b = new BitVector("6'b010101");
        c = a.setX(a.xor(b));
        assertEquals("6'bx101", c.toString(2));

        // TEST: setX() X/Z mask
        a = new BitVector("6'b001101");
        b = new BitVector("6'b0010xz");
        c = a.setX(b);
        assertEquals("6'bx1xx", c.toString(2));

        // TEST: setZ() normal values
        a = new BitVector("6'b001101");
        b = new BitVector("6'b010101");
        c = a.setZ(a.xor(b));
        assertEquals("6'bz101", c.toString(2));

        // TEST: setX() X/Z mask
        a = new BitVector("6'b001101");
        b = new BitVector("6'b0010xz");
        c = a.setZ(b);
        assertEquals("6'bz1xx", c.toString(2));

    }

    public void testgetXMaskGetZMask()
    {
        BitVector a = new BitVector("4'b0000");
        BitVector b = new BitVector("4'b1111");
        BitVector c = new BitVector("4'bxxxx");
        BitVector d = new BitVector("4'bzzzz");

        assertEquals("4'b0", a.getXMask().toString(2));
        assertEquals("4'b0", a.getZMask().toString(2));
        assertEquals("4'b0", b.getXMask().toString(2));
        assertEquals("4'b0", b.getZMask().toString(2));
        assertEquals("4'b1111", c.getXMask().toString(2));
        assertEquals("4'b0", c.getZMask().toString(2));
        assertEquals("4'b0", d.getXMask().toString(2));
        assertEquals("4'b1111", d.getZMask().toString(2));
    }

    public void testFillBits()
    {
        BitVector a = new BitVector(64, 0);
        BitVector r;
        BitVectorFormat fmt = new BitVectorFormat();
        fmt.setUnderscoreFreq(4);
        fmt.setRadix(2);

        r = a.fillBits(7, 0, Bit.ONE);
        assertEquals("64'b1111_1111", fmt.format(r));

        r = a.fillBits(7, 0, Bit.Z);
        assertEquals("64'bz", fmt.format(r));

        r = a.fillBits(7, 0, Bit.X);
        assertEquals("64'bx", fmt.format(r));

        r = a.fillBits(47, 16, Bit.X);
        assertEquals("64'bx_" + "0000_0000_0000_0000", fmt.format(r));

        r = r.fillBits(32, 16, Bit.ZERO);
        assertEquals(
            "64'bx0_" + "0000_0000_0000_0000_" + "0000_0000_0000_0000", fmt
                .format(r));

        BitVectorBuffer b = new BitVectorBuffer(65, 0);
        b.fillBits(64, 0, Bit.X);
        assertEquals("65'bx", fmt.format(b.toBitVector()));

        BitVector bv = null;

        bv = new BitVector(45, 0);
        bv = bv.fillBits(new BitRange(39, 36), Bit.ONE);
        assertEquals("45'hf000000000", bv.toString());

        bv = new BitVector(45, 0);
        bv = bv.fillBits(new BitRange(39, 32), Bit.ONE);
        assertEquals("45'hff00000000", bv.toString());

        bv = new BitVector(45, 0);
        bv = bv.fillBits(new BitRange(41, 33), Bit.ONE);
        assertEquals("45'h3fe00000000", bv.toString());

        bv = new BitVector(45, 0);
        bv = bv.fillBits(new BitRange(42, 31), Bit.ONE);
        assertEquals("45'h7ff80000000", bv.toString());

        bv = new BitVector(96, 0);
        bv = bv.fillBits(new BitRange(77, 26), Bit.ONE);
        assertEquals("96'h3ffffffffffffc000000", bv.toString());
    }

    public void testAssign()
    {
        BitVector a = new BitVector("6'b101010");
        assertEquals("6'b1111", a.assign(new BitVector("4'b1111")).toString(2));
        assertEquals("6'bxz1", a.assign(new BitVector("3'bxz1")).toString(2));
        assertEquals("6'b111000", a.assign(new BitVector("8'bzx111000"))
            .toString(2));
    }

    public void testAssignMaskBitVectorBuffer()
    {
        BitVectorBuffer a;
        BitVector mask;
        BitVector value;
        BitVectorBuffer result;

        // TEST: all length equal
        a = new BitVectorBuffer("8'b01zx01zx");
        mask = new BitVector("8'b11001100");
        value = new BitVector("8'bxz10xz10");
        result = a.assignMask(value, mask);
        assertEquals("8'bxzzxxzzx", result.toString(2));

        // TEST: lengths: buf > mask > val
        a = new BitVectorBuffer("8'b01zx_01zx");
        mask = new BitVector("6'b10_1100");
        value = new BitVector("5'b0_xz10");
        result = a.assignMask(value, mask);
        assertEquals("8'b10xxzzx", result.toString(2));

        // TEST: lengths: mask > buf > val
        a = new BitVectorBuffer("5'bx_01zx");
        mask = new BitVector("8'b1110_1100");
        value = new BitVector("4'bxz10");
        result = a.assignMask(value, mask);
        assertEquals("5'bxzzx", result.toString(2));

        // TEST: lengths: val > buf > mask
        a = new BitVectorBuffer("5'bx_01zx");
        mask = new BitVector("2'b11");
        value = new BitVector("8'b1010_xz10");
        result = a.assignMask(value, mask);
        assertEquals("5'bx0110", result.toString(2));

        // TEST: lengths: buf > mask > val (span an int)
        a = new BitVectorBuffer("80'h1234_5678_90AB_CDEF_XZ12");
        mask = new BitVector("31'h70F0_FF00");
        value = new BitVector("16'b0101_01xz_xzxz_0101");
        result = a.assignMask(value, mask);
        assertEquals("80'h1234567890ab8d0f5?12", result.toString(16));

    }

    public void testAssignMaskBitVector()
    {
        BitVector a;
        BitVector mask;
        BitVector value;
        BitVector result;

        // TEST: all length equal
        a = new BitVector("8'b01zx01zx");
        mask = new BitVector("8'b11001100");
        value = new BitVector("8'bxz10xz10");
        result = a.assignMask(value, mask);
        assertEquals("8'bxzzxxzzx", result.toString(2));

        // TEST: lengths: buf > mask > val
        a = new BitVector("8'b01zx_01zx");
        mask = new BitVector("6'b10_1100");
        value = new BitVector("5'b0_xz10");
        result = a.assignMask(value, mask);
        assertEquals("8'b10xxzzx", result.toString(2));

        // TEST: lengths: mask > buf > val
        a = new BitVector("5'bx_01zx");
        mask = new BitVector("8'b1110_1100");
        value = new BitVector("4'bxz10");
        result = a.assignMask(value, mask);
        assertEquals("5'bxzzx", result.toString(2));

        // TEST: lengths: val > buf > mask
        a = new BitVector("5'bx_01zx");
        mask = new BitVector("2'b11");
        value = new BitVector("8'b1010_xz10");
        result = a.assignMask(value, mask);
        assertEquals("5'bx0110", result.toString(2));

        // TEST: lengths: buf > mask > val (span an int)
        a = new BitVector("80'h1234_5678_90AB_CDEF_XZ12");
        mask = new BitVector("31'h70F0_FF00");
        value = new BitVector("16'b0101_01xz_xzxz_0101");
        result = a.assignMask(value, mask);
        assertEquals("80'h1234567890ab8d0f5?12", result.toString(16));

    }

    public void testLengths()
    {
        BitVectorBuffer bvb;
        BitVector bv;
        BitVector bv2;
        BitVector result;

        // TEST BVB And/Extend
        bvb = new BitVectorBuffer("64'h1234567890abcdef");
        bv = new BitVector("12'hA5A");
        bvb.and(bv);
        assertEquals("64'h84a", bvb.toString());
        // TEST BVB And/Truncate
        bvb = new BitVectorBuffer("12'hA5A");
        bv = new BitVector("64'h1234567890abcdef");
        bvb.and(bv);
        assertEquals("64'h84a", bvb.toString());

        // TEST BVB Or/Extend
        bvb = new BitVectorBuffer("64'h1234567890abcdef");
        bv = new BitVector("12'hA5A");
        bvb.or(bv);
        assertEquals("64'h1234567890abcfff", bvb.toString());
        // TEST BVB Or/Truncate
        bvb = new BitVectorBuffer("12'hA5A");
        bv = new BitVector("64'h1234567890abcdef");
        bvb.or(bv);
        assertEquals("64'h1234567890abcfff", bvb.toString());

        // TEST BV Or/1
        bv = new BitVector("64'h1234567890abcdef");
        bv2 = new BitVector("12'hA5A");
        result = bv.or(bv2);
        assertEquals("64'h1234567890abcfff", result.toString());
        // TEST BV Or/2
        bv = new BitVector("12'hA5A");
        bv2 = new BitVector("64'h1234567890abcdef");
        result = bv2.or(bv);
        assertEquals("64'h1234567890abcfff", result.toString());

        // TEST BVB Xor/Extend
        bvb = new BitVectorBuffer("64'h1234567890abcdef");
        bv = new BitVector("12'hA5A");
        bvb.xor(bv);
        assertEquals("64'h1234567890abc7b5", bvb.toString());
        // TEST BVB Xor/Truncate
        bvb = new BitVectorBuffer("12'hA5A");
        bv = new BitVector("64'h1234567890abcdef");
        bvb.xor(bv);
        assertEquals("64'h1234567890abc7b5", bvb.toString());

        // TEST BV Xor/1
        bv = new BitVector("64'h1234567890abcdef");
        bv2 = new BitVector("12'hA5A");
        result = bv.xor(bv2);
        assertEquals("64'h1234567890abc7b5", result.toString());
        // TEST BV Xor/2
        bv = new BitVector("12'hA5A");
        bv2 = new BitVector("64'h1234567890abcdef");
        result = bv2.xor(bv);
        assertEquals("64'h1234567890abc7b5", result.toString());

        // TEST BVB AndNot/Extend
        bvb = new BitVectorBuffer("64'h1234567890abcdef");
        bv = new BitVector("12'hA5A");
        bvb.andNot(bv);
        assertEquals("64'h1234567890abc5a5", bvb.toString());
        // TEST BVB AndNot/Truncate
        bvb = new BitVectorBuffer("12'hA5A");
        bv = new BitVector("64'h1234567890abcdef");
        bvb.andNot(bv);
        assertEquals("64'h210", bvb.toString());

        // TEST BV AndNot/1
        bv = new BitVector("64'h1234567890abcdef");
        bv2 = new BitVector("12'hA5A");
        result = bv.andNot(bv2);
        assertEquals("64'h1234567890abc5a5", result.toString());
        // TEST BV AndNot/2
        bv = new BitVector("12'hA5A");
        bv2 = new BitVector("64'h1234567890abcdef");
        result = bv2.andNot(bv);
        assertEquals("64'h1234567890abc5a5", result.toString());

        // TEST BVB setX/Extend
        bvb = new BitVectorBuffer("64'h1234567890abcdef");
        bv = new BitVector("12'hA5A");
        bvb.setX(bv);
        assertEquals("64'h1234567890abc???", bvb.toString());
        // TEST BVB setX/Truncate
        bvb = new BitVectorBuffer("12'hA5A");
        bv = new BitVector("64'h1234567890abcdef");
        bvb.setX(bv);
        assertEquals("12'bx1xxxx1xxxx", bvb.toString(2));

        // TEST BV setX/1
        bv = new BitVector("64'h1234567890abcdef");
        bv2 = new BitVector("12'hA5A");
        result = bv.setX(bv2);
        assertEquals("64'h1234567890abc???", result.toString());
        // TEST BV setX/2
        bv = new BitVector("12'hA5A");
        bv2 = new BitVector("64'h1234567890abcdef");
        result = bv2.setX(bv);
        assertEquals("64'h1234567890abc???", result.toString());

        // TEST BVB setZ/Extend
        bvb = new BitVectorBuffer("64'h1234567890abcdef");
        bv = new BitVector("12'hA5A");
        bvb.setZ(bv);
        assertEquals("64'h1234567890abc???", bvb.toString());
        // TEST BVB setZ/Truncate
        bvb = new BitVectorBuffer("12'hA5A");
        bv = new BitVector("64'h1234567890abcdef");
        bvb.setZ(bv);
        assertEquals("12'bz1zzzz1zzzz", bvb.toString(2));

        // TEST BV setZ/1
        bv = new BitVector("64'h1234567890abcdef");
        bv2 = new BitVector("12'hA5A");
        result = bv.setZ(bv2);
        assertEquals("64'h1234567890abc???", result.toString());
        // TEST BV setZ/2
        bv = new BitVector("12'hA5A");
        bv2 = new BitVector("64'h1234567890abcdef");
        result = bv2.setZ(bv);
        assertEquals("64'h1234567890abc???", result.toString());

        /*
         * // FIXME: add tests for equals*Mask // TEST BVB equalsMask/Extend bvb =
         * new BitVectorBuffer("64'h1234567890abcdef"); bv = new
         * BitVector("12'hdeA"); bvb = bvb.equalsMask(bv);
         * assertEquals("64'h000000000000ffa", bvb.toString()); // TEST BVB
         * equalsMask/Truncate bvb = new BitVectorBuffer("12'hdeA"); bv = new
         * BitVector("64'h1234567890abcdef"); bvb = bvb.equalsMask(bv);
         * assertEquals("12'hFF0", bvb.toString()); // TEST BV equalsMask/1 bv =
         * new BitVector("64'h1234567890abcdef"); bv2 = new
         * BitVector("12'hdeA"); result = bv.equalsMask(bv2);
         * assertEquals("64'h0000000000000FF0", result.toString()); // TEST BV
         * equalsMask/2 bv = new BitVector("12'hdeA"); bv2 = new
         * BitVector("64'h1234567890abcdef"); result = bv2.equalsMask(bv);
         * assertEquals("64'h0000000000000FF0", result.toString());
         */

    }

    public void testAdd()
    {
        BitVector a1;
        BitVector a2;

        a1 = new BitVector("2'b00");
        assertEquals("2'b0", a1.add(a1).toString(2));

        a1 = new BitVector("2'b01");
        assertEquals("2'b10", a1.add(a1).toString(2));

        a1 = new BitVector("2'b10");
        assertEquals("2'b0", a1.add(a1).toString(2));

        a2 = new BitVector("3'b10");
        assertEquals("3'b100", a1.add(a2).toString(2));
        assertEquals("3'b100", a2.add(a1).toString(2));

        a1 = new BitVector("32'h8000_0000");
        a2 = new BitVector("33'h8000_0000");
        assertEquals("33'h100000000", a1.add(a2).toString());
        assertEquals("33'h100000000", a2.add(a1).toString());

        PRNG rng = PRNGFactoryFactory.getDefaultFactory().newInstance();
        for (int i = 0; i < 100; i++)
        {
            long a = rng.nextLong();
            long b = rng.nextLong();
            StringBuffer longResult = new StringBuffer(Long.toHexString(a + b));
            while (longResult.length() < 16)
            {
                longResult.insert(0, 0);
            }
            longResult.insert(0, "64'h");

            BitVector l1 = new BitVector("64'h" + Long.toHexString(a));
            BitVector l2 = new BitVector("64'h" + Long.toHexString(b));
            assertEquals(longResult.toString().replaceFirst("^64'h0+", "64'h"),
                l1.add(l2).toString());
        }

        a1 = new BitVector("64'hffff_ffff_ffff_ffff");
        a2 = new BitVector("65'h1");
        assertEquals("65'h10000000000000000", a1.add(a2).toString());
        assertEquals("65'h10000000000000000", a2.add(a1).toString());

        a1 = new BitVector("64'hffff_ffff_ffff_ffff");
        a2 = new BitVector("65'b0x");
        assertEquals("65'hX", a1.add(a2).toString());
        assertEquals("65'hX", a2.add(a1).toString());

        a1 = new BitVector("64'hffff_ffff_ffff_ffff");
        a2 = new BitVector("65'b0z");
        assertEquals("65'hX", a1.add(a2).toString());
        assertEquals("65'hX", a2.add(a1).toString());

        a1 = new BitVector(Bit.ONE);
        a2 = new BitVector(64, Bit.ONE);
        assertEquals("64'h0", a1.add(a2).toString());
        assertEquals("64'h0", a2.add(a1).toString());
    }

    public void testSubtract()
    {
        BitVector a1;
        BitVector a2;

        a1 = new BitVector("2'b00");
        assertEquals("2'b0", a1.subtract(a1).toString(2));

        a1 = new BitVector("2'b01");
        assertEquals("2'b0", a1.subtract(a1).toString(2));

        a1 = new BitVector("2'b00");
        a2 = new BitVector("2'b01");
        assertEquals("2'b11", a1.subtract(a2).toString(2));

        a1 = new BitVector("32'h0");
        a2 = new BitVector("33'h8000_0000");
        assertEquals("33'h180000000", a1.subtract(a2).toString());
        assertEquals("33'h80000000", a2.subtract(a1).toString());

        Random rng = new Random();
        for (int i = 0; i < 100; i++)
        {
            long a = rng.nextLong();
            long b = rng.nextLong();
            StringBuffer longResult = new StringBuffer(Long.toHexString(a - b));
            while (longResult.length() < 16)
            {
                longResult.insert(0, 0);
            }
            longResult.insert(0, "64'h");

            BitVector l1 = new BitVector("64'h" + Long.toHexString(a));
            BitVector l2 = new BitVector("64'h" + Long.toHexString(b));
            assertEquals(longResult.toString().replaceFirst("^64'h0+", "64'h"),
                l1.subtract(l2).toString());
        }
    }

    public void testMultiply()
    {
        BitVector m1 = null;
        BitVector m2 = null;

        m1 = new BitVector(4, 15);
        m2 = new BitVector(4, 15);
        assertEquals("4'h1", m1.multiply(m2).toString());
        assertEquals("4'h1", m2.multiply(m1).toString());

        m1 = new BitVector(4, 15);
        m2 = new BitVector(5, 15);
        assertEquals("5'h1", m1.multiply(m2).toString());
        assertEquals("5'h1", m2.multiply(m1).toString());

        m1 = new BitVector(4, 15);
        m2 = new BitVector(8, 15);
        assertEquals("8'he1", m1.multiply(m2).toString());
        assertEquals("8'he1", m2.multiply(m1).toString());

        // check overflow
        m1 = new BitVector(64, 0x8000000000000000L);
        m2 = new BitVector(2, 2);
        assertEquals("64'h0", m1.multiply(m2).toString());
        assertEquals("64'h0", m2.multiply(m1).toString());

        // check overflow
        m1 = new BitVector(64, 0xffffffffffffffffL);
        m2 = new BitVector(2, 2);
        assertEquals("64'hfffffffffffffffe", m1.multiply(m2).toString());
        assertEquals("64'hfffffffffffffffe", m2.multiply(m1).toString());

        // test multiply by a valus with X's and Z's
        m1 = new BitVector(64, 0xffffffffffffffffL);
        m2 = new BitVector("4'b01xz");
        assertEquals("64'hX", m1.multiply(m2).toString());
        assertEquals("64'hX", m1.multiply(m2).toString());

        // test multiply by a value with Z's but not X's
        m1 = new BitVector(64, 0xffffffffffffffffL);
        m2 = new BitVector("4'b01zz");
        assertEquals("64'hZ", m1.multiply(m2).toString());
        assertEquals("64'hZ", m1.multiply(m2).toString());

        // test multiply by zero
        m1 = new BitVector(64, 0xffffffffffffffffL);
        m2 = new BitVector(Bit.ZERO);
        assertEquals("64'h0", m1.multiply(m2).toString());
        assertEquals("64'h0", m1.multiply(m2).toString());

    }

    public void testDivide()
    {
        BitVector divisor = null;
        BitVector dividend = null;

        try
        {
            divisor = new BitVector(1, 0);
            dividend = new BitVector(32, 4);
            dividend.divide(divisor);
            assertTrue(false);
        }
        catch (ArithmeticException e)
        {
            // good. no dividing by zero here, thanks.
        }

        divisor = new BitVector(32, 4);
        dividend = new BitVector(33, 0);
        assertEquals("33'h0", dividend.divide(divisor).toString());

        // check 32-bit division
        divisor = new BitVector(32, 0x07000000);
        dividend = new BitVector(32, 0x10000000);
        assertEquals("32'h2", dividend.divide(divisor).toString());

        // check > 32-bit division
        divisor = new BitVector(64, 0x07000000);
        dividend = new BitVector(32, 0x10000000);
        assertEquals("64'h2", dividend.divide(divisor).toString());

        // check > 32-bit division with vector having topmost bit set
        divisor = new BitVector(32, 0x10000000);
        dividend = new BitVector(64, 0xffffffffffffffffL);
        assertEquals("64'hfffffffff", dividend.divide(divisor).toString());
    }

    public void testMod()
    {
        BitVector bv = null;
        BitVector result = null;
        bv = new BitVector(32, 3);
        result = bv.mod(new BitVector(32, 2));
        assertEquals("32'h3", bv.toString());
        assertEquals("32'h1", result.toString());

        bv = new BitVector(32, 0xe);
        result = bv.mod(new BitVector(32, 4));
        assertEquals("32'h2", result.toString());

        bv = new BitVector("34'h820de");
        result = bv.mod(new BitVector(32, 4));
        assertEquals("34'h2", result.toString());

        bv = new BitVector("40'hd01ba72f70");
        result = bv.mod(new BitVector(32, 64));
        assertEquals("40'h30", result.toString());
    }

    public void testZipUnzip()
    {
        /*
         * BitVector zipped; BitVector[] unzipped; BitVector[] vects = new
         * BitVector[3];
         *
         * vects[0] = new BitVector("16'h1234"); vects[1] = new
         * BitVector("16'h5678"); vects[2] = new BitVector("16'h90ab"); zipped =
         * vects[0].zip(vects); unzipped = zipped.unzip(new int[] {16, 16, 16});
         * assertEquals("16'h1234", unzipped[0].toString());
         * assertEquals("16'h5678", unzipped[1].toString());
         * assertEquals("16'h90ab", unzipped[2].toString());
         *
         * vects[0] = new BitVector("32'h12344321"); vects[1] = new
         * BitVector("1'h1"); vects[2] = new BitVector("7'h76"); zipped =
         * vects[0].zip(vects); unzipped = zipped.unzip(new int[] {32, 1, 7});
         * assertEquals("32'h12344321", unzipped[0].toString());
         * assertEquals("1'h1", unzipped[1].toString()); assertEquals("7'h76",
         * unzipped[2].toString());
         */
    }

    public void testReduction()
    {
        BitVector bv;

        bv = new BitVector("64'hA5A5A5A5_A5A5A5A5");
        assertEquals(Bit.ZERO, bv.reductiveAnd());
        assertEquals(Bit.ONE, bv.reductiveOr());
        assertEquals(Bit.ZERO, bv.reductiveXor());

        bv = new BitVector("64'hE5A5A5A5_A5A5A5A5");
        assertEquals(Bit.ZERO, bv.reductiveAnd());
        assertEquals(Bit.ONE, bv.reductiveOr());
        assertEquals(Bit.ONE, bv.reductiveXor());

        bv = new BitVector("33'h0");
        assertEquals(Bit.ZERO, bv.reductiveAnd());
        assertEquals(Bit.ZERO, bv.reductiveOr());
        assertEquals(Bit.ZERO, bv.reductiveXor());

        bv = new BitVector("33'h1FFFFFFFF");
        assertEquals(Bit.ONE, bv.reductiveAnd());
        assertEquals(Bit.ONE, bv.reductiveOr());
        assertEquals(Bit.ONE, bv.reductiveXor());

        bv = new BitVector("33'bx_xxxxxxxx_xxxxxxxx_xxxxxxxx_xxxxxxxx");
        assertEquals(Bit.X, bv.reductiveAnd());
        assertEquals(Bit.X, bv.reductiveOr());
        assertEquals(Bit.X, bv.reductiveXor());

        bv = new BitVector("33'bz_zzzzzzzz_zzzzzzzz_zzzzzzzz_zzzzzzzz");
        assertEquals(Bit.X, bv.reductiveAnd());
        assertEquals(Bit.X, bv.reductiveOr());
        assertEquals(Bit.X, bv.reductiveXor());

        bv = new BitVector("33'h0A5A5A5A5");
        assertEquals(Bit.ZERO, bv.reductiveAnd());
        assertEquals(Bit.ONE, bv.reductiveOr());
        assertEquals(Bit.ZERO, bv.reductiveXor());

        bv = new BitVector("33'h1A5A5A5A5");
        assertEquals(Bit.ZERO, bv.reductiveAnd());
        assertEquals(Bit.ONE, bv.reductiveOr());
        assertEquals(Bit.ONE, bv.reductiveXor());

        bv = new BitVector("33'h1A5A5A5A5");
        bv = bv.setBit(0, Bit.X);
        assertEquals(Bit.X, bv.reductiveAnd());
        assertEquals(Bit.X, bv.reductiveOr());
        assertEquals(Bit.X, bv.reductiveXor());

        bv = new BitVector("33'h1A5A5A5A5");
        bv = bv.setBit(0, Bit.Z);
        assertEquals(Bit.X, bv.reductiveAnd());
        assertEquals(Bit.X, bv.reductiveOr());
        assertEquals(Bit.X, bv.reductiveXor());

        bv = new BitVector("32'hffff_fffe");
        assertEquals(Bit.ZERO, bv.reductiveAnd());
        assertEquals(Bit.ONE, bv.reductiveOr());
        assertEquals(Bit.ONE, bv.reductiveXor());
    }

    public void testReverse()
    {
        BitVector bv;

        bv = new BitVector("1'b1");
        assertEquals("1'h1", bv.reverse().toString());

        bv = new BitVector("8'b01010101");
        assertEquals("8'haa", bv.reverse().toString());

        bv = new BitVector("8'b01x10z01");
        assertEquals("8'b10z01x10", bv.reverse().toString(2));

        bv = new BitVector("32'hA5A5A5A5");
        assertEquals("32'ha5a5a5a5", bv.reverse().toString());

        bv = new BitVector("64'hF5A5A5A5_A5A5A5AD");
        assertEquals("64'hb5a5a5a5a5a5a5af", bv.reverse().toString());

    }

    public void testNegate()
    {
        BitVector bv;

        bv = new BitVector("64'h0");
        assertEquals("64'h0", bv.negate().toString());

        bv = new BitVector("64'h1");
        assertEquals("64'hffffffffffffffff", bv.negate().toString());

        bv = new BitVector("8'b110x1110");
        assertEquals("8'hX", bv.negate().toString());

        bv = new BitVector(32, 0);
        assertEquals("32'h0", bv.negate().toString());

        bv = new BitVector(16, 1);
        assertEquals("16'hffff", bv.negate().toString());
        // make sure we're masking off bits properly.
        assertEquals(0, bv.myBuffer.myValues[0] >>> 16);
    }

    public void testCompare()
    {
        BitVector bv1;
        BitVector bv2;

        bv1 = new BitVector("1'bx");
        bv2 = new BitVector("32'b0");
        try
        {
            bv1.compareTo(bv2);
        }
        catch (XZException e)
        {
            // do nothing
        }

        bv1 = new BitVector("33'h0FFFFFFFF");
        bv2 = new BitVector("32'hFFFFFFFF");
        assertEquals(0, bv1.compareTo(bv2));

        bv1 = new BitVector("33'h1FFFFFFFF");
        bv2 = new BitVector("32'hFFFFFFFF");
        assertEquals(1, bv1.compareTo(bv2));

        bv1 = new BitVector("32'hFFFFFFFF");
        bv2 = new BitVector("33'h1FFFFFFFF");
        assertEquals(-1, bv1.compareTo(bv2));
    }

    public void testNumbers()
    {
        BitVector f = new BitVector(32, 0xfffffffd);
        assertEquals((float) -3.0, f.floatValue());

        BitVector d = new BitVector(64, 0xfffffffffffffffdL);
        assertEquals(-3.0, d.doubleValue());

        assertEquals(1.0, Bit.ONE.doubleValue());
        assertEquals(0.0, Bit.ZERO.doubleValue());
        assertEquals((float) 1.0, Bit.ONE.floatValue());
        assertEquals((float) 0.0, Bit.ZERO.floatValue());

        assertEquals(1.0, new BitVector("96'h123456780000000000000001")
            .doubleValue());
        assertEquals(0.0, new BitVector("96'h123456780000000000000000")
            .doubleValue());
        assertEquals((float) 1.0, new BitVector("96'h123456781234567800000001")
            .floatValue());
        assertEquals((float) 0.0, new BitVector("96'h123456781234567800000000")
            .floatValue());

        try
        {
            Bit.X.longValue();
            assertTrue(false);
        }
        catch (XZException e)
        {
            // exception as expected -- good
        }

        try
        {
            Bit.Z.longValue();
            assertTrue(false);
        }
        catch (XZException e)
        {
            // exception as expected -- good
        }

        try
        {
            new BitVector(Bit.X).intValue();
            assertTrue(false);
        }
        catch (XZException e)
        {
            // exception as expected -- good
        }

        try
        {
            new BitVector(Bit.Z).longValue();
            assertTrue(false);
        }
        catch (XZException e)
        {
            // exception as expected -- good
        }

        // test Bit booleanValue() functionality
        assertTrue(Bit.ONE.booleanValue());
        assertFalse(Bit.ZERO.booleanValue());
        try
        {
            Bit.X.booleanValue();
            assertTrue(false);
        }
        catch (XZException e)
        {
            // exception as expected -- good
        }
        try
        {
            Bit.Z.booleanValue();
            assertTrue(false);
        }
        catch (XZException e)
        {
            // exception as expected -- good
        }

    }

}
