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
 * Represents a simulation timestamp expressed in simulator ticks.
 * 
 * @author Trevor Robinson
 */
public final class VerilogSimTime
    implements VerilogTime
{
    private final long simTime;

    public static final VerilogSimTime TIME0 = new VerilogSimTime(0);

    public VerilogSimTime(long simTime)
    {
        this.simTime = simTime;
    }

    public long getSimTime()
    {
        return simTime;
    }

    public int compareTo(Object obj)
    {
        VerilogSimTime other = (VerilogSimTime) obj;
        long diff = simTime - other.simTime;
        return diff > 0 ? 1 : diff < 0 ? -1 : 0;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof VerilogSimTime)
        {
            VerilogSimTime other = (VerilogSimTime) obj;
            if (simTime == other.simTime)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return (int) (simTime ^ (simTime >>> 32));
    }

    @Override
    public String toString()
    {
        return String.valueOf(simTime);
    }
}
