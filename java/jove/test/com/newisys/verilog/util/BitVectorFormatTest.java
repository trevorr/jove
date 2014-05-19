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

import junit.framework.TestCase;

/**
 * @author jon.nall
 *
 * BitVectorFormatTest
 * TODO: write test for format width
 */
public class BitVectorFormatTest
    extends TestCase
{
    private BitVectorFormat myFmt;

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        myFmt = new BitVectorFormat();
    }

    public void testParse()
    {
        // array layout
        // string    width   radix  value   xzmask
        String validStrings[][] = { { "3'b101", "3", "2", "0101", "0" },
            { "10'o77", "10", "8", "77", "0" },
            { "23'd1234", "23", "10", "1234", "0" },
            { "32'h1234abcd", "32", "16", "1234abcd", "0" },
            { "32'hAbCdEf", "32", "16", "abcdef", "0" },
            { "1'b0", "1", "2", "0", "0" }, { "1'b1", "1", "2", "1", "0" },
            { "1'bx", "1", "2", "1", "1" }, { "1'bz", "1", "2", "0", "1" },
            { "8'hX", "8", "16", "ff", "255" }, // X-entension
            { "8'hZ", "8", "16", "0", "255" }, // Z-entension
            { "8'b0x", "8", "2", "00000001", "1" }, // 0-extension
            { "2'b1_x", "2", "2", "11", "1" } }; //underscore

        BitVector vector;
        int i;
        String str;
        int value;
        int mask;
        int length;
        int radix;

        for (i = 0; i < validStrings.length; i++)
        {
            str = validStrings[i][0];
            //System.out.println("Testing [" + str + "]");
            length = Integer.parseInt(validStrings[i][1]);
            radix = Integer.parseInt(validStrings[i][2]);
            value = Integer.parseInt(validStrings[i][3], radix);
            mask = Integer.parseInt(validStrings[i][4]);
            vector = myFmt.parse(str);
            assertEquals(length, vector.length());
            assertEquals(value, vector.myBuffer.myValues[0]);
            assertEquals(mask, vector.myBuffer.myXzMask[0]);
            assertEquals((mask != 0), vector.containsXZ());
        }

        vector = myFmt.parse("'h14");
        assertEquals(32, vector.length());

        vector = myFmt.parse("44'h1234567890a", 32);
        assertEquals("32'h4567890a", vector.toString());

        vector = myFmt.parse("'d015");
        assertEquals(32, vector.length());

        vector = myFmt.parse("8'bx101_0011", 16);
        assertEquals("16'hX?3", vector.toString());
    }

    /*
     * Class to test for String format(BitVectorBuffer)
     */
    public void testFormatBitVectorBuffer()
    {
        BitVector vector;
        vector = new BitVector("16'b00001111xxxxzzzz");
        assertEquals("16'hfXZ", myFmt.format(vector));

        // Test setting different radices
        // System.out.println("Testing setRadix()");
        myFmt.setRadix(2);
        assertEquals("16'b1111xxxxzzzz", myFmt.format(vector));
        myFmt.setRadix(8);
        assertEquals("16'o07??Z", myFmt.format(vector));
        myFmt.setRadix(16);
        assertEquals("16'hfXZ", myFmt.format(vector));

        // Test Underscore freq
        // System.out.println("Testing setUnderscoreFreq()");
        myFmt.setUnderscoreFreq(4);
        myFmt.setRadix(2);
        assertEquals("16'b1111_xxxx_zzzz", myFmt.format(vector));
        myFmt.setRadix(8);
        assertEquals("16'o0_7??Z", myFmt.format(vector));
        myFmt.setUnderscoreFreq(1);
        myFmt.setRadix(16);
        assertEquals("16'hf_X_Z", myFmt.format(vector));

        vector = new BitVector("32'hffff_fffa");
        assertEquals("32'd4294967290", vector.toString(10));
        vector = new BitVector("63'h7fff_ffff_ffff_fffa");
        assertEquals("63'd9223372036854775802", vector.toString(10));
        vector = new BitVector("64'hffff_ffff_ffff_fffa");
        assertEquals("64'd18446744073709551610", vector.toString(10));
    }

    /*
     * Class to test for String format(BitVectorBuffer, int)
     */
    public void testFormatBitVectorBufferint()
    {
        String[][] validStrings = {
            { "16'b00001111xxxxzzzz", "2", "16'b1111xxxxzzzz" },
            { "16'b00001111xxxxzzzz", "8", "16'o07??Z" },
            { "16'b00001111xxxxzzzz", "10", "16'd?" },
            { "16'b00001111xxxxzzzz", "16", "16'hfXZ" },
            { "16'b0x001z11xzxxzx01", "16", "16'h????" },
            { "16'bxxxxzzzz", "16", "16'hXZ" }, { "4'hx", "2", "4'bx" },
            { "4'bx", "2", "4'bx" } };
        int i;
        String str = "";
        int radix;
        String expectedStr;

        for (i = 0; i < validStrings.length; i++)
        {
            str = validStrings[i][0];
            radix = Integer.parseInt(validStrings[i][1]);
            //System.out.println("Testing [" + str + "] -> base " + radix);
            expectedStr = validStrings[i][2];
            BitVector vector = new BitVector(str);
            assertEquals(expectedStr, myFmt.format(vector, radix));
        }
    }

    public void testSetRadix()
    {
        myFmt.setRadix(8);
        BitVector bv = myFmt.parse("77");
        assertEquals("32'h3f", bv.toString());
        try
        {
            bv = myFmt.parse("af");
            fail("BitVectorFormat with a radix of 8 parsed a hex value");
        }
        catch (IllegalArgumentException e)
        {
            // do nothing
        }
        assertEquals("32'o77", myFmt.format(bv));

        myFmt.setRadix(16);
        bv = myFmt.parse("1af");
        assertEquals("32'h1af", bv.toString());

        try
        {
            myFmt.setRadix(16);
            bv = myFmt.parse("12'af");
            fail("BitVectorFormat parsed 12'af");
        }
        catch (IllegalArgumentException e)
        {
            // do nothing
        }
    }

    public void testPrintRadix()
    {
        myFmt.setPrintRadix(true);
        myFmt.setRadix(16);
        BitVector bv;

        bv = new BitVector("16'h1234");
        assertEquals("16'h1234", myFmt.format(bv));
        myFmt.setPrintLength(false);
        assertEquals("'h1234", myFmt.format(bv));
        myFmt.setPrintLength(true);
        assertEquals("16'h1234", myFmt.format(bv));
        myFmt.setPrintRadix(false);
        assertEquals("1234", myFmt.format(bv));
    }

    public void testCapitalization()
    {
        myFmt.setRadix(16);
        BitVector bv = myFmt.parse("32'habcdef01");
        assertEquals("32'habcdef01", myFmt.format(bv));
        myFmt.setCapitalization(true);
        assertEquals("32'hABCDEF01", myFmt.format(bv));
    }
}
