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

package com.newisys.behsim;

import com.newisys.dv.DVSimulation;
import com.newisys.verilog.VerilogReg;
import com.newisys.verilog.util.Bit;

/**
 * Generates a clock pattern on a signal given the clocking parameters.
 * 
 * @author Scott Diesing
 */
public class BehavioralClockGenerator
    implements Runnable
{
    private final DVSimulation dvSim;
    private final String clockName;
    private final int period;
    private final int firstHalfPeriod;
    private final int initialDelay;
    private final Bit initialValue;
    private final boolean firstEdgeRising;

    public BehavioralClockGenerator(
        DVSimulation dvSim,
        String clockName,
        int period)
    {
        this(dvSim, clockName, period, period / 2, 0, Bit.ZERO, false);
    }

    public BehavioralClockGenerator(
        DVSimulation dvSim,
        String clockName,
        int period,
        int initialDelay)
    {
        this(dvSim, clockName, period, period / 2, initialDelay, Bit.ZERO,
            false);
    }

    public BehavioralClockGenerator(
        DVSimulation dvSim,
        String clockName,
        int period,
        int firstHalfPeriod,
        int initalDelay,
        Bit initialValue,
        boolean firstEdgeRising)
    {
        this.dvSim = dvSim;
        this.clockName = clockName;
        this.period = period;
        this.firstHalfPeriod = firstHalfPeriod;
        this.initialDelay = initalDelay;
        this.initialValue = initialValue;
        this.firstEdgeRising = firstEdgeRising;
    }

    public void run()
    {
        VerilogReg clockDriver = (VerilogReg) dvSim.getObjectByName(clockName);
        Bit curValue[] = { Bit.ZERO, Bit.ONE };
        clockDriver.putValue(initialValue);
        dvSim.delay(initialDelay);

        int lastHalfPeriod = period - firstHalfPeriod;
        if (lastHalfPeriod <= 0)
        {
            throw new RuntimeException("firstHalfPeriod(" + firstHalfPeriod
                + ") greater than period(" + period + ")");
        }

        int i;

        if (firstEdgeRising)
        {
            i = 1;
        }
        else
        {
            i = 0;
        }

        while (true)
        {
            clockDriver.putValue(curValue[i++ % 2]);
            dvSim.delay(lastHalfPeriod);
            clockDriver.putValue(curValue[i++ % 2]);
            dvSim.delay(firstHalfPeriod);
        }
    }
}
