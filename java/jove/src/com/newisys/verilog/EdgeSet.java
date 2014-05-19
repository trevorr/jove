/*
 * PLI4J - A Java (TM) Interface to the Verilog PLI
 * Copyright (C) 2003 Trevor A. Robinson
 * Java is a registered trademark of Sun Microsystems, Inc. in the U.S. or
 * other countries.
 *
 * Licensed under the Academic Free License version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You should
 * have received a copy of the License along with this software; if not, you
 * may obtain a copy of the License at
 *
 * http://opensource.org/licenses/afl-2.0.php
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.newisys.verilog;

import java.io.Serializable;

import com.newisys.verilog.util.Bit;

/**
 * Represents a set of transitions between 0, 1, and X.
 * 
 * @author Trevor Robinson
 */
public final class EdgeSet
    implements Serializable
{
    private static final long serialVersionUID = 3256728385525724464L;

    public final static byte MASK_01 = 0x01;
    public final static byte MASK_0X = 0x02;
    public final static byte MASK_10 = 0x04;
    public final static byte MASK_1X = 0x08;
    public final static byte MASK_X0 = 0x10;
    public final static byte MASK_X1 = 0x20;

    public final static byte NOEDGE_MASK = 0;
    public final static byte POSEDGE_MASK = MASK_01 | MASK_0X | MASK_X1;
    public final static byte NEGEDGE_MASK = MASK_10 | MASK_1X | MASK_X0;
    public final static byte ANYEDGE_MASK = POSEDGE_MASK | NEGEDGE_MASK;

    public final static EdgeSet NO_EDGE = new EdgeSet(NOEDGE_MASK);
    public final static EdgeSet EDGE_01 = new EdgeSet(MASK_01);
    public final static EdgeSet EDGE_0X = new EdgeSet(MASK_0X);
    public final static EdgeSet EDGE_10 = new EdgeSet(MASK_10);
    public final static EdgeSet EDGE_1X = new EdgeSet(MASK_1X);
    public final static EdgeSet EDGE_X0 = new EdgeSet(MASK_X0);
    public final static EdgeSet EDGE_X1 = new EdgeSet(MASK_X1);
    public final static EdgeSet POSEDGE = new EdgeSet(POSEDGE_MASK);
    public final static EdgeSet NEGEDGE = new EdgeSet(NEGEDGE_MASK);
    public final static EdgeSet ANYEDGE = new EdgeSet(ANYEDGE_MASK);

    private final byte mask;

    public EdgeSet(int mask)
    {
        this.mask = (byte) mask;
    }

    public byte getMask()
    {
        return mask;
    }

    public boolean has01()
    {
        return (mask & MASK_01) != 0;
    }

    public boolean has0X()
    {
        return (mask & MASK_0X) != 0;
    }

    public boolean has10()
    {
        return (mask & MASK_10) != 0;
    }

    public boolean has1X()
    {
        return (mask & MASK_1X) != 0;
    }

    public boolean hasX0()
    {
        return (mask & MASK_X0) != 0;
    }

    public boolean hasX1()
    {
        return (mask & MASK_X1) != 0;
    }

    public boolean isEmpty()
    {
        return mask == NOEDGE_MASK;
    }

    public EdgeSet intersect(EdgeSet other)
    {
        return new EdgeSet(mask & other.mask);
    }

    public EdgeSet union(EdgeSet other)
    {
        return new EdgeSet(mask | other.mask);
    }

    private final static EdgeSet[][] transEdge = {
        { NO_EDGE, EDGE_01, EDGE_0X, EDGE_0X },
        { EDGE_10, NO_EDGE, EDGE_1X, EDGE_1X },
        { EDGE_X0, EDGE_X1, NO_EDGE, NO_EDGE },
        { EDGE_X0, EDGE_X1, NO_EDGE, NO_EDGE } };

    public static EdgeSet getTransitionEdge(Bit from, Bit to)
    {
        return transEdge[from.getID()][to.getID()];
    }

    private final static int[][] transMask = {
        { 0, MASK_01, MASK_0X, MASK_0X }, { MASK_10, 0, MASK_1X, MASK_1X },
        { MASK_X0, MASK_X1, 0, 0 }, { MASK_X0, MASK_X1, 0, 0 } };

    public boolean matches(Bit from, Bit to)
    {
        return (mask & transMask[from.getID()][to.getID()]) != 0;
    }

    public boolean contains(EdgeSet other)
    {
        return (mask & other.mask) == other.mask;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof EdgeSet)
        {
            EdgeSet other = (EdgeSet) obj;
            return other.mask == mask;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return mask;
    }

    @Override
    public String toString()
    {
        switch (mask)
        {
        case NOEDGE_MASK:
            return "NOEDGE";
        case POSEDGE_MASK:
            return "POSEDGE";
        case NEGEDGE_MASK:
            return "NEGEDGE";
        case ANYEDGE_MASK:
            return "ANYEDGE";
        }

        StringBuffer buf = new StringBuffer(6 * 5 + 2);
        buf.append("{");
        if (mask != 0)
        {
            if (has01()) buf.append("0->1,");
            if (has0X()) buf.append("0->X,");
            if (has10()) buf.append("1->0,");
            if (has1X()) buf.append("1->X,");
            if (hasX0()) buf.append("X->0,");
            if (hasX1()) buf.append("X->1,");
            buf.setLength(buf.length() - 1);
        }
        buf.append("}");
        return buf.toString();
    }
}
