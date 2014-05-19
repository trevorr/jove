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

package com.newisys.verilog.pli;

import com.newisys.verilog.VerilogRuntimeException;
import com.newisys.verilog.VerilogScaledRealTime;
import com.newisys.verilog.VerilogSimTime;
import com.newisys.verilog.VerilogTime;

/**
 * Represents a simulator timestamp, as either simulator ticks or scaled real
 * time.
 * 
 * @author Trevor Robinson
 */
public final class PLITime
{
    public final static long INVALID_SIM_TIME = -1;
    public final static double INVALID_SCALED_REAL_TIME = -1.0;

    public final static PLITime SIM_TIME0 = new PLITime(0);
    public final static PLITime SCALED_REAL_TIME0 = new PLITime(0.0);
    public final static PLITime SUPPRESS = new PLITime();

    private final PLITimeType timeType;
    private final long simTime;
    private final double scaledRealTime;

    public PLITime(PLITimeType timeType, long simTime, double scaledRealTime)
    {
        this.timeType = timeType;
        this.simTime = simTime;
        this.scaledRealTime = scaledRealTime;
    }

    // called from native code
    PLITime(int timeType, long simTime, double scaledRealTime)
    {
        this(PLITimeType.getTimeType(timeType), simTime, scaledRealTime);
    }

    public PLITime()
    {
        this.timeType = PLITimeType.SUPPRESS;
        this.simTime = INVALID_SIM_TIME;
        this.scaledRealTime = INVALID_SCALED_REAL_TIME;
    }

    public PLITime(long simTime)
    {
        this.timeType = PLITimeType.SIM;
        this.simTime = simTime;
        this.scaledRealTime = INVALID_SCALED_REAL_TIME;
    }

    public PLITime(double scaledRealTime)
    {
        this.timeType = PLITimeType.SCALED_REAL;
        this.simTime = INVALID_SIM_TIME;
        this.scaledRealTime = scaledRealTime;
    }

    public PLITimeType getTimeType()
    {
        return timeType;
    }

    public long getSimTime()
    {
        return simTime;
    }

    public double getScaledRealTime()
    {
        return scaledRealTime;
    }

    public VerilogTime getVerilogTime()
    {
        if (timeType == PLITimeType.SIM)
        {
            return new VerilogSimTime(simTime);
        }
        else if (timeType == PLITimeType.SCALED_REAL)
        {
            return new VerilogScaledRealTime(scaledRealTime);
        }
        else if (timeType == PLITimeType.SUPPRESS)
        {
            return null;
        }
        else
        {
            throw new VerilogRuntimeException("Unknown time type: " + timeType);
        }
    }

    public static PLITime getPLITime(VerilogTime time)
    {
        if (time == null)
        {
            return SUPPRESS;
        }
        else if (time instanceof VerilogSimTime)
        {
            return new PLITime(((VerilogSimTime) time).getSimTime());
        }
        else if (time instanceof VerilogScaledRealTime)
        {
            return new PLITime(((VerilogScaledRealTime) time)
                .getScaledRealTime());
        }
        else
        {
            throw new ClassCastException("Unknown time class: "
                + time.getClass());
        }
    }

    public static PLITime getPLITime(PLITimeType type)
    {
        switch (type.getValue())
        {
        case PLITimeTypeConstants.vpiScaledReal:
            return SCALED_REAL_TIME0;
        case PLITimeTypeConstants.vpiSim:
            return SIM_TIME0;
        case PLITimeTypeConstants.vpiSuppress:
            return SUPPRESS;
        default:
            throw new VerilogRuntimeException("Unknown time type: " + type);
        }
    }
}
