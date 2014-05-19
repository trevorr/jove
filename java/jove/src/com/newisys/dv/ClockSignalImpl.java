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

package com.newisys.dv;

import com.newisys.eventsim.Event;
import com.newisys.verilog.EdgeSet;
import com.newisys.verilog.util.Bit;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.ValueConverter;

/**
 * Implementation of a clock signal.
 * 
 * @author Trevor Robinson
 */
final class ClockSignalImpl
    extends BaseRealSignalImpl
    implements ClockSignal
{
    public ClockSignalImpl(
        DVSimulation dvSim,
        String name,
        ClockMonitor clockMonitor)
    {
        super(dvSim, name, clockMonitor);
    }

    public ClockSignal getClock()
    {
        return this;
    }

    public int getSize()
    {
        return 1;
    }

    public EdgeSet getInputEdges()
    {
        return EdgeSet.ANYEDGE;
    }

    public int getInputSkew()
    {
        return 0;
    }

    public int getInputDepth()
    {
        return 1;
    }

    @Override
    protected BitVector doSample(int depth, boolean async)
    {
        final Object value;
        if (!async)
        {
            syncSample();
            if (depth < 0) depth = 0;
            value = clockMonitor.getValue(depth);
        }
        else
        {
            if (depth < 0)
            {
                value = clockMonitor.getValueAsync();
            }
            else
            {
                value = clockMonitor.getValue(depth);
            }
        }
        return ValueConverter.toBitVector(value);
    }

    @Override
    public void syncSampleDelay(int cycles)
    {
        syncClockDelay(EdgeSet.ANYEDGE, cycles);
    }

    @Override
    public Event getEdgeEvent(EdgeSet edges, int bit, boolean async)
    {
        if (bit != 0)
        {
            throw new IllegalArgumentException(
                "Invalid edge bit index for clock signal: " + bit);
        }

        return getClockEdgeEvent(edges);
    }

    @Override
    public Event getChangeEvent(BitVector mask, boolean async)
    {
        if (mask != null && mask.getBit(0) != Bit.ONE)
        {
            throw new IllegalArgumentException(
                "Invalid change mask for clock signal: " + mask);
        }

        // FIXME: since we return an EdgeEvent, X/Z transitions are not reported
        return getClockEdgeEvent(EdgeSet.ANYEDGE);
    }

    @Override
    protected void doDrive(
        int cycles,
        Object value,
        boolean nb,
        boolean async,
        boolean soft)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doRangeDrive(
        int cycles,
        int highBit,
        int lowBit,
        Object value,
        boolean nb,
        boolean async,
        boolean soft)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void syncDriveDelay(int cycles)
    {
        throw new UnsupportedOperationException();
    }

    public int getCycleCount()
    {
        return clockMonitor.getPosEdgeCount();
    }
}
