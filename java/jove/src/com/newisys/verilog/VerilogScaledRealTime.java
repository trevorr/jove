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

/**
 * Represents a simulation timestamp expressed in scaled real time.
 * 
 * @author Trevor Robinson
 */
public final class VerilogScaledRealTime
    implements VerilogTime
{
    private final double scaledRealTime;

    public static final VerilogScaledRealTime TIME0 = new VerilogScaledRealTime(
        0.0);

    public VerilogScaledRealTime(double scaledRealTime)
    {
        this.scaledRealTime = scaledRealTime;
    }

    public double getScaledRealTime()
    {
        return scaledRealTime;
    }

    public int compareTo(Object obj)
    {
        VerilogScaledRealTime other = (VerilogScaledRealTime) obj;
        return Double.compare(scaledRealTime, other.scaledRealTime);
    }

    @Override
    public boolean equals(Object compareObject)
    {
        if (compareObject instanceof VerilogScaledRealTime)
        {
            VerilogScaledRealTime compareTime = (VerilogScaledRealTime) compareObject;
            if (scaledRealTime == compareTime.scaledRealTime)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        long tmp = Double.doubleToLongBits(scaledRealTime);
        return (int) (tmp ^ (tmp >>> 32));
    }

    @Override
    public String toString()
    {
        return String.valueOf(scaledRealTime);
    }
}
