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
 * Enumeration of the Verilog bit values.
 * 
 * @author Trevor Robinson
 */
public final class Bit
    extends Number
    implements Serializable
{
    private static final long serialVersionUID = 3617860772106219570L;

    /**
     * Represents the binary value 0.
     */
    public static final Bit ZERO = new Bit("0", 0);

    /**
     * Represents the binary value 1.
     */
    public static final Bit ONE = new Bit("1", 1);

    /**
     * Represents the binary value Z (high impedance).
     */
    public static final Bit Z = new Bit("Z", 2);

    /**
     * Represents the binary value X (undefined).
     */
    public static final Bit X = new Bit("X", 3);

    /**
     * Value returned by ZERO.getID().
     */
    public static final int ID_ZERO = 0;

    /**
     * Value returned by ONE.getID().
     */
    public static final int ID_ONE = 1;

    /**
     * Value returned by Z.getID().
     */
    public static final int ID_Z = 2;

    /**
     * Value returned by X.getID().
     */
    public static final int ID_X = 3;

    private final String str;
    private final int id;

    private Bit(String str, int id)
    {
        this.str = str;
        this.id = id;
    }

    /**
     * Returns a numeric ID for each bit value: ZERO=0, ONE=1, Z=2, X=3.
     *
     * @return the numeric ID associated with the Bit
     */
    public int getID()
    {
        return id;
    }

    /**
     * Returns the Bit object corresponding to the given ID: 0=ZERO, 1=ONE, 2=Z,
     * else X.
     * <P>
     * Note that <code>getBitForID(-3427).getID() == 3</code>.
     *
     * @param id the numeric ID of the requested Bit
     * @return the Bit corresponding to <code>id</code>
     */
    public static Bit getBitForID(int id)
    {
        switch (id)
        {
        case ID_ZERO:
            return ZERO;
        case ID_ONE:
            return ONE;
        case ID_Z:
            return Z;
        default:
            return X;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return str;
    }

    /**
     * Returns true if this Bit is X or Z, false otherwise.
     *
     * @return whether this Bit is X or Z
     */
    public boolean isXZ()
    {
        return id > 1;
    }

    /**
     * Returns X if this Bit is Z, otherwise returns this Bit.
     *
     * @return a Bit (ZERO, ONE, or X) as described above
     */
    public Bit ztox()
    {
        return this == Z ? X : this;
    }

    /**
     * Returns the Bit corresponding to the NOT of this Bit:
     * ZERO->ONE, ONE->ZERO, X/Z->X.
     *
     * <table>
     * <tr><td><b>Bit</b></td><td><b>not</b></td></tr>
     * <tr><td>0</td><td>1</td></tr>
     * <tr><td>1</td><td>0</td></tr>
     * <tr><td>Z</td><td>X</td></tr>
     * <tr><td>X</td><td>X</td></tr>
     * </table>
     *
     * @return a Bit as described in the table above
     */
    public Bit not()
    {
        final Bit[] table = { ONE, ZERO, X, X };
        return table[getID()];
    }

    /**
     * Returns the Bit corresponding to the AND of this Bit and the given Bit:
     * <table>
     * <tr><td><b>and</b></td><td><b>0</b></td><td><b>1</b></td><td><b>Z</b></td><td><b>X</b></td></tr>
     * <tr><td><b>0</b></td><td>0</td><td>0</td><td>0</td><td>0</td></tr>
     * <tr><td><b>1</b></td><td>0</td><td>1</td><td>X</td><td>X</td></tr>
     * <tr><td><b>X</b></td><td>0</td><td>X</td><td>X</td><td>X</td></tr>
     * <tr><td><b>Z</b></td><td>0</td><td>X</td><td>X</td><td>X</td></tr>
     * </table>
     *
     * @param b a Bit to AND with this bit
     * @return a Bit representing the AND of this and <code>b</code> as described
     *      in the table above
     */
    public Bit and(Bit b)
    {
        final Bit[][] table = { { ZERO, ZERO, ZERO, ZERO },
            { ZERO, ONE, X, X }, { ZERO, X, X, X }, { ZERO, X, X, X } };
        return table[getID()][b.getID()];
    }

    /**
     * Returns the Bit corresponding to the OR of this Bit and the given Bit:
     * <table>
     * <tr><td><b>or</b></td><td><b>0</b></td><td><b>1</b></td><td><b>Z</b></td><td><b>X</b></td></tr>
     * <tr><td><b>0</b></td><td>0</td><td>1</td><td>X</td><td>X</td></tr>
     * <tr><td><b>1</b></td><td>1</td><td>1</td><td>1</td><td>1</td></tr>
     * <tr><td><b>X</b></td><td>X</td><td>1</td><td>X</td><td>X</td></tr>
     * <tr><td><b>Z</b></td><td>X</td><td>1</td><td>X</td><td>X</td></tr>
     * </table>
     *
     * @param b a Bit to OR with this bit
     * @return a Bit representing the OR of this and <code>b</code> as described
     *      in the table above
     */
    public Bit or(Bit b)
    {
        final Bit[][] table = { { ZERO, ONE, X, X }, { ONE, ONE, ONE, ONE },
            { X, ONE, X, X }, { X, ONE, X, X } };
        return table[getID()][b.getID()];
    }

    /**
     * Returns the Bit corresponding to the XOR of this Bit and the given Bit:
     * <table>
     * <tr><td><b>xor</b></td><td><b>0</b></td><td><b>1</b></td><td><b>Z</b></td><td><b>X</b></td></tr>
     * <tr><td><b>0</b></td><td>0</td><td>1</td><td>X</td><td>X</td></tr>
     * <tr><td><b>1</b></td><td>1</td><td>0</td><td>X</td><td>X</td></tr>
     * <tr><td><b>X</b></td><td>X</td><td>X</td><td>X</td><td>X</td></tr>
     * <tr><td><b>Z</b></td><td>X</td><td>X</td><td>X</td><td>X</td></tr>
     * </table>
     *
     * @param b a Bit to XOR with this bit
     * @return a Bit representing the XOR of this and <code>b</code> as described
     *      in the table above
     */
    public Bit xor(Bit b)
    {
        final Bit[][] table = { { ZERO, ONE, X, X }, { ONE, ZERO, X, X },
            { X, X, X, X }, { X, X, X, X } };
        return table[getID()][b.getID()];
    }

    /**
     * Converts this Bit to a <code>boolean</code>.
     *
     * @return this Bit converted to a <code>boolean</code>.
     * @throws XZException if this Bit is X or Z
     */
    public boolean booleanValue()
    {
        if (isXZ())
        {
            throw new XZException("X/Z value is not boolean");
        }
        return (this == Bit.ONE);
    }

    /**
     * Converts this Bit to a <code>double</code>.
     *
     * @return this Bit converted to a <code>double</code>.
     * @throws XZException if this Bit is X or Z
     */
    @Override
    public double doubleValue()
    {
        return longValue();
    }

    /**
     * Converts this Bit to a <code>float</code>.
     *
     * @return this Bit converted to a <code>float</code>.
     * @throws XZException if this Bit is X or Z
     */
    @Override
    public float floatValue()
    {
        return intValue();
    }

    /**
     * Converts this Bit to a <code>int</code>.
     *
     * @return this Bit converted to a <code>int</code>.
     * @throws XZException if this Bit is X or Z
     */
    @Override
    public int intValue()
    {
        if (this == Bit.Z || this == Bit.X)
        {
            throw new XZException("X/Z value is not numeric");
        }
        return getID();
    }

    /**
     * Converts this Bit to a <code>long</code>.
     *
     * @return this Bit converted to a <code>long</code>.
     * @throws XZException if this Bit is X or Z
     */
    @Override
    public long longValue()
    {
        return intValue();
    }
}
