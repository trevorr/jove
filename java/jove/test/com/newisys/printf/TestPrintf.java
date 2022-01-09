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

import java.math.BigInteger;

import junit.framework.TestCase;

import com.newisys.verilog.util.Bit;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.BitVectorBuffer;

enum Pirate
{
    Blackbeard, Bluebeard, CapnJack;
}

class ToString
{
    @Override
    public String toString()
    {
        return "Sine Your Pitty on the Runny Kine!";
    }
}

public class TestPrintf
    extends TestCase
{
    public void testNumericConversions()
    {
        boolean bool = true;
        Bit bit = Bit.X;
        byte b = 5;
        short s = 0x14;
        int i = 0x12345678;
        long l = 0x123456789012L;
        BitVectorBuffer buf = new BitVectorBuffer("23'h14452");
        BitVector vector = new BitVector("23'h14452");
        BigInteger bigInt = new BigInteger("14452", 16);
        Pirate p = Pirate.CapnJack;
        String string = "\tJK";

        // %b
        assertEquals("1", Printf.sprintf("%b", bool));
        assertEquals("x", Printf.sprintf("%b", bit));
        assertEquals("101", Printf.sprintf("%b", b));
        assertEquals("10100", Printf.sprintf("%b", s));
        assertEquals("10010001101000101011001111000", Printf.sprintf("%b", i));
        assertEquals("100100011010001010110011110001001000000010010", Printf
            .sprintf("%b", l));
        assertEquals("10100010001010010", Printf.sprintf("%b", buf));
        assertEquals("10100010001010010", Printf.sprintf("%b", vector));
        assertEquals("10100010001010010", Printf.sprintf("%b", bigInt));
        assertEquals("10", Printf.sprintf("%b", p));
        assertEquals("10010100101001001011", Printf.sprintf("%b", string));

        // %d
        assertEquals("1", Printf.sprintf("%d", bool));
        assertEquals("?", Printf.sprintf("%d", bit));
        assertEquals("5", Printf.sprintf("%d", b));
        assertEquals("20", Printf.sprintf("%d", s));
        assertEquals("305419896", Printf.sprintf("%d", i));
        assertEquals("20015998341138", Printf.sprintf("%d", l));
        assertEquals("83026", Printf.sprintf("%d", buf));
        assertEquals("83026", Printf.sprintf("%d", vector));
        assertEquals("83026", Printf.sprintf("%d", bigInt));
        assertEquals("2", Printf.sprintf("%d", p));
        assertEquals("608843", Printf.sprintf("%d", string));

        // %i
        assertEquals("1", Printf.sprintf("%i", bool));
        assertEquals("?", Printf.sprintf("%i", bit));
        assertEquals("5", Printf.sprintf("%i", b));
        assertEquals("20", Printf.sprintf("%i", s));
        assertEquals("305419896", Printf.sprintf("%i", i));
        assertEquals("20015998341138", Printf.sprintf("%i", l));
        assertEquals("83026", Printf.sprintf("%i", buf));
        assertEquals("83026", Printf.sprintf("%i", vector));
        assertEquals("83026", Printf.sprintf("%i", bigInt));
        assertEquals("2", Printf.sprintf("%i", p));
        assertEquals("608843", Printf.sprintf("%i", string));

        // %o
        assertEquals("1", Printf.sprintf("%o", bool));
        assertEquals("?", Printf.sprintf("%o", bit));
        assertEquals("5", Printf.sprintf("%o", b));
        assertEquals("24", Printf.sprintf("%o", s));
        assertEquals("2215053170", Printf.sprintf("%o", i));
        assertEquals("443212636110022", Printf.sprintf("%o", l));
        assertEquals("242122", Printf.sprintf("%o", buf));
        assertEquals("242122", Printf.sprintf("%o", vector));
        assertEquals("242122", Printf.sprintf("%o", bigInt));
        assertEquals("2", Printf.sprintf("%o", p));
        assertEquals("2245113", Printf.sprintf("%o", string));
        assertEquals("70XZ?", Printf.sprintf("%o", new BitVector(
            "15'b111_000_xxx_zzz_01x")));
        assertEquals("X0XZ?", Printf.sprintf("%o", new BitVector(
            "14'bxx_000_xxx_zzz_01x")));

        // %x
        assertEquals("1", Printf.sprintf("%x", bool));
        assertEquals("X", Printf.sprintf("%x", bit));
        assertEquals("5", Printf.sprintf("%x", b));
        assertEquals("14", Printf.sprintf("%x", s));
        assertEquals("12345678", Printf.sprintf("%x", i));
        assertEquals("123456789012", Printf.sprintf("%x", l));
        assertEquals("14452", Printf.sprintf("%x", buf));
        assertEquals("14452", Printf.sprintf("%x", vector));
        assertEquals("14452", Printf.sprintf("%x", bigInt));
        assertEquals("2", Printf.sprintf("%x", p));
        assertEquals("94a4b", Printf.sprintf("%x", string));
        assertEquals("f0XZ?", Printf.sprintf("%x", new BitVector(
            "20'b1111_0000_xxxx_zzzz_01xz")));
        assertEquals("X0XZ?", Printf.sprintf("%x", new BitVector(
            "19'bxxx_0000_xxxx_zzzz_01xz")));

        // %X
        assertEquals("94A4B", Printf.sprintf("%X", string));

    }

    public void testStringConversions()
    {
        boolean bool = true;
        Bit bit = Bit.X;
        byte b = 5;
        short s = 0x14;
        int i = 0x12345678;
        long l = 0x123456789012L;
        BitVectorBuffer buf = new BitVectorBuffer("23'h14452");
        BitVector vector = new BitVector("23'h14452");
        BigInteger bigInt = new BigInteger("14452", 16);
        Pirate p = Pirate.CapnJack;
        String string = "\tJK";

        // %c
        assertEquals("t", Printf.sprintf("%c", bool));
        assertEquals("", Printf.sprintf("%c", bit));
        assertEquals(new String(new byte[] { 5 }), Printf.sprintf("%c", b));
        assertEquals(new String(new char[] { 0x14 }), Printf.sprintf("%c", s));
        assertEquals(new String(new char[] { 0x12 }), Printf.sprintf("%c", i));
        assertEquals(new String(new char[] { 0x12 }), Printf.sprintf("%c", l));
        assertEquals(new String(new char[] { 1 }), Printf.sprintf("%c", buf));
        assertEquals(new String(new char[] { 1 }), Printf.sprintf("%c", vector));
        assertEquals(new String(new char[] { 1 }), Printf.sprintf("%c", bigInt));
        assertEquals("C", Printf.sprintf("%c", p));
        assertEquals("\t", Printf.sprintf("%c", string));

        // %s
        assertEquals("true", Printf.sprintf("%s", bool));
        assertEquals("", Printf.sprintf("%s", bit));
        assertEquals(new String(new char[] { 5 }), Printf.sprintf("%s", b));
        assertEquals(new String(new char[] { 0x14 }), Printf.sprintf("%s", s));
        assertEquals(new String(new char[] { 0x12, 0x34, 0x56, 0x78 }), Printf
            .sprintf("%s", i));
        assertEquals(new String(
            new char[] { 0x12, 0x34, 0x56, 0x78, 0x90, 0x12 }), Printf.sprintf(
            "%s", l));
        assertEquals(new String(new char[] { 1, 0x44, 0x52 }), Printf.sprintf(
            "%s", buf));
        assertEquals(new String(new char[] { 1, 0x44, 0x52 }), Printf.sprintf(
            "%s", vector));
        assertEquals(new String(new char[] { 1, 0x44, 0x52 }), Printf.sprintf(
            "%s", bigInt));
        assertEquals("CapnJack", Printf.sprintf("%s", p));
        assertEquals("\tJK", Printf.sprintf("%s", string));

        assertEquals("Sine Your Pitty on the Runny Kine!", Printf.sprintf("%s",
            new ToString()));
    }

    public void testSignedConversion()
    {
        // only %d and %i should be signed for a signed type
        Object o = Short.valueOf((short) -1);
        assertEquals("1111111111111111", Printf.sprintf("%b", o));
        assertEquals("177777", Printf.sprintf("%o", o));
        assertEquals("-1", Printf.sprintf("%d", o));
        assertEquals("-1", Printf.sprintf("%i", o));
        assertEquals("ffff", Printf.sprintf("%x", o));

        // only %d and %i should be signed for a signed type
        o = new BigInteger("-1");
        assertEquals("1", Printf.sprintf("%b", o));
        assertEquals("1", Printf.sprintf("%o", o));
        assertEquals("-1", Printf.sprintf("%d", o));
        assertEquals("-1", Printf.sprintf("%i", o));
        assertEquals("1", Printf.sprintf("%x", o));

        // for an unsigned type, no conversion spec should produce a signed
        // representation
        o = new BitVector("16'hffff");
        assertEquals("1111111111111111", Printf.sprintf("%b", o));
        assertEquals("177777", Printf.sprintf("%o", o));
        assertEquals("65535", Printf.sprintf("%d", o));
        assertEquals("65535", Printf.sprintf("%i", o));
        assertEquals("ffff", Printf.sprintf("%x", o));
    }

    public void testWidth()
    {
        assertEquals("  arrrgh!", Printf.sprintf("%9s", "arrrgh!"));
        assertEquals("        a", Printf.sprintf("%9c", "arrrgh!"));
        assertEquals("        5", Printf.sprintf("%9d", 5));
        assertEquals("000000005", Printf.sprintf("%09d", 5));
        assertEquals("5        ", Printf.sprintf("%0-9d", 5));
        assertEquals("Alignment 5   test", Printf.sprintf(
            "Alignment %-3d test", 5));
        assertEquals("Alignment two    test", Printf.sprintf(
            "Alignment %-6s test", "two"));

        try
        {
            Printf.sprintf("%09s", "arrrgh!"); // ZERO_PAD is not allowed for strings
            assertTrue(false);
        }
        catch (InvalidFormatSpecException e)
        {
            // good.
        }
    }

    public void testJustification()
    {
        assertEquals("arrrgh!  ", Printf.sprintf("%-9s", "arrrgh!"));
        assertEquals("5        ", Printf.sprintf("%-9d", 5));
    }

    public void testFlags()
    {
        assertEquals("BLAH", Printf.sprintf("%^s", "blah"));

        // currently test the '+' and ' ' flags on positive and negative values
        assertEquals("+5", Printf.sprintf("%+d", 5));
        assertEquals("+5 ", Printf.sprintf("%-+3d", 5));
        assertEquals(" 5 ", Printf.sprintf("%- 3d", 5));
        assertEquals("+5 ", Printf.sprintf("%- +3d", 5));

        assertEquals("-5", Printf.sprintf("%+d", -5));
        assertEquals("-5 ", Printf.sprintf("%-+3d", -5));
        assertEquals("-5 ", Printf.sprintf("%- 3d", -5));
        assertEquals("-5 ", Printf.sprintf("%- +3d", -5));

        assertEquals("0xfffffffd", Printf.sprintf("%#+x", -3));
        assertEquals("037777777775", Printf.sprintf("%#+o", -3));

        // '#' flag must not prepend a '0' if one already exists
        assertEquals("000", Printf.sprintf("%#03o", 0));
        assertEquals("0XFFFFFFFD", Printf.sprintf("%#X", -3));
        assertEquals("     0x1", Printf.sprintf("%#8x", 1));
        assertEquals("0x000001", Printf.sprintf("%#08x", 1));

        // '=' flag causes argument to print with a minimum width of the arg's
        // natural width
        assertEquals("0x00000001", Printf.sprintf("%#=x", 1));

        assertEquals("000101", Printf.sprintf("%=b", new BitVector(6, 5)));
        assertEquals("101", Printf.sprintf("%b", new BitVector(6, 5)));

        assertEquals("xxx101", Printf
            .sprintf("%=b", new BitVector("6'bxxx101")));
        assertEquals("x101", Printf.sprintf("%b", new BitVector("6'bxxx101")));

        assertEquals("zzz101", Printf
            .sprintf("%=b", new BitVector("6'bzzz101")));
        assertEquals("z101", Printf.sprintf("%b", new BitVector("6'bzzz101")));

        assertEquals("0", Printf.sprintf("%b", new BitVector("6'b000000")));

        assertEquals("0xXXX5", Printf.sprintf("%#06x", new BitVector(
            "6'bxxx0101")));

        // sign flag is used for decimal even if alternate flag is present
        assertEquals("+5", Printf.sprintf("%#+d", 5));

    }

    public void testMisc()
    {
        // check that %% prints a single %
        assertEquals("%", Printf.sprintf("%%"));

        // check that basic text/arg interleaving works
        assertEquals("the quick brown %% fox jumped over the lazy dog\n",
            Printf.sprintf("the%s%s%%%%%sjumped over the %s dog\n", " quick",
                " brown ", " fox ", "lazy"));

        // check that a warning is printed if all args are not given
        Printf.sprintf("%d");

        // check that a warning is printed if too many args are given
        Printf.sprintf("%d", 5, 4);
    }
}
