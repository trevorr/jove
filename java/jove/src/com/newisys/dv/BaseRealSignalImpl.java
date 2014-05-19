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
import com.newisys.verilog.util.BitVectorBuffer;
import com.newisys.verilog.util.ValueConverter;

/**
 * Abstract base class for the core signal implementations. Provides protected
 * final helper methods that implement the functionality in the abstract methods
 * of BaseSignalImpl.
 * 
 * @author Trevor Robinson
 */
abstract class BaseRealSignalImpl
    extends BaseSignalImpl
{
    protected final DVSimulation dvSim;
    protected final String name;
    protected final ClockMonitor clockMonitor;

    public BaseRealSignalImpl(
        DVSimulation dvSim,
        String name,
        ClockMonitor clockMonitor)
    {
        this.dvSim = dvSim;
        this.name = name;
        this.clockMonitor = clockMonitor;
    }

    public final String getName()
    {
        return name;
    }

    protected static String getPartialName(
        String signalName,
        int partHigh,
        int partLow)
    {
        return signalName + "[" + partHigh + ":" + partLow + "]";
    }

    protected final BitVector doSample(
        final ClockedInputMonitor clkInputMonitor,
        int depth,
        final boolean async)
    {
        final Object value;
        if (!async)
        {
            syncSample();
            if (depth < 0) depth = 0;
            value = clkInputMonitor.getValue(depth);
        }
        else
        {
            if (depth < 0)
            {
                long simTime = dvSim.verilogSim.getSimTime();
                value = clkInputMonitor.getValueAsync(simTime);
            }
            else
            {
                value = clkInputMonitor.getValue(depth);
            }
        }
        return ValueConverter.toBitVector(value);
    }

    protected final BitVector doPartialSample(
        final ClockedInputMonitor clkInputMonitor,
        final int partHigh,
        final int partLow,
        int depth,
        final boolean async)
    {
        final BitVector fullValue = doSample(clkInputMonitor, depth, async);
        return fullValue.getBits(partHigh, partLow);
    }

    protected final Event getEdgeEvent(
        final ClockedInputMonitor clkInputMonitor,
        final EdgeSet edges,
        final int bit,
        final boolean async)
    {
        // check bit index against signal limits
        checkIndex(bit, clkInputMonitor.getSignalSize());

        // build event
        final Event e;
        if (async)
        {
            e = new AsyncEdgeEvent(dvSim.dvEventManager, clkInputMonitor
                .getInputMonitor(), edges, bit);
        }
        else
        {
            final EdgeSet prevEdge = clkInputMonitor
                .getPreviousEdgeInCycle(bit);
            if (!prevEdge.isEmpty() && edges.contains(prevEdge))
            {
                e = getClockEdgeEvent(clkInputMonitor.getClockEdges());
            }
            else
            {
                e = new SyncEdgeEvent(dvSim.dvEventManager, clkInputMonitor,
                    edges, bit);
            }
        }
        return e;
    }

    protected final Event getPartialEdgeEvent(
        final ClockedInputMonitor clkInputMonitor,
        final int partHigh,
        final int partLow,
        final EdgeSet edges,
        final int bit,
        final boolean async)
    {
        final int partSize = partHigh - partLow + 1;

        // check bit index against partial limits
        checkIndex(bit, partSize);

        return getEdgeEvent(clkInputMonitor, edges, bit + partLow, async);
    }

    protected final Event getChangeEvent(
        final ClockedInputMonitor clkInputMonitor,
        final BitVector mask,
        final boolean async)
    {
        final BitVector effMask;
        if (mask != null)
        {
            final int size = clkInputMonitor.getSignalSize();

            // adjust length of mask if necessary
            effMask = mask.setLength(size);

            // check for valid mask
            if (!effMask.isNotZero())
            {
                throw new IllegalArgumentException("Invalid change mask for "
                    + size + "-bit signal: " + mask);
            }
        }
        else
        {
            // not using mask
            effMask = null;
        }

        // build event
        final Event e;
        if (async)
        {
            e = new AsyncValueChangeEvent(dvSim.dvEventManager, clkInputMonitor
                .getInputMonitor(), effMask, -clkInputMonitor.getSkew());
        }
        else
        {
            if (clkInputMonitor.isChangedInCycle(effMask))
            {
                e = getClockEdgeEvent(clkInputMonitor.getClockEdges());
            }
            else
            {
                e = new SyncValueChangeEvent(dvSim.dvEventManager,
                    clkInputMonitor, effMask);
            }
        }
        return e;
    }

    protected final Event getPartialChangeEvent(
        final ClockedInputMonitor clkInputMonitor,
        final int partHigh,
        final int partLow,
        final BitVector mask,
        final boolean async)
    {
        // extend mask to be relative to full signal
        final int size = clkInputMonitor.getSignalSize();
        final BitVectorBuffer buf = new BitVectorBuffer(size, Bit.ZERO);
        buf.setBits(partHigh, partLow, mask);
        final BitVector fullMask = buf.toBitVector();

        return getChangeEvent(clkInputMonitor, fullMask, async);
    }

    protected final void doDrive(
        final OutputScheduler outputScheduler,
        final EdgeSet outputEdges,
        final int outputSkew,
        final int size,
        final BitVector mask0,
        final BitVector mask1,
        int cycles,
        final Object value,
        final boolean nb,
        final boolean async,
        final boolean soft)
    {
        // check masks
        assert (mask0.length() == size);
        assert (mask1.length() == size);

        // async drive with delay would be nonsense
        assert (!async || cycles == 0);

        if (nb)
        {
            if (!isSynced(outputEdges))
            {
                ++cycles;
            }
        }
        else
        {
            if (!async)
            {
                syncDriveDelay(cycles);
            }
            cycles = 0;
        }

        BitVector writeMask = mask1;
        BitVector bvValue = ValueConverter.toBitVector(value).setLength(size);
        BitVector strongMask = soft ? mask0 : writeMask.andNot(bvValue
            .getZMask());
        outputScheduler.drive(cycles, outputSkew, bvValue, writeMask,
            strongMask);
    }

    protected final void doPartialDrive(
        final OutputScheduler outputScheduler,
        final EdgeSet outputEdges,
        final int outputSkew,
        final int partHigh,
        final int partLow,
        final int size,
        final BitVector mask0,
        final BitVector mask1,
        int cycles,
        final Object value,
        final boolean nb,
        final boolean async,
        final boolean soft)
    {
        doRangeDrive(outputScheduler, outputEdges, outputSkew, size, mask0,
            mask1, cycles, partHigh, partLow, value, nb, async, soft);
    }

    protected final void doRangeDrive(
        final OutputScheduler outputScheduler,
        final EdgeSet outputEdges,
        final int outputSkew,
        final int size,
        final BitVector mask0,
        final BitVector mask1,
        int cycles,
        int highBit,
        int lowBit,
        Object value,
        boolean nb,
        boolean async,
        boolean soft)
    {
        // check masks
        assert (mask0.length() == size);
        assert (mask1.length() == size);

        // async drive with delay would be nonsense
        assert (!async || cycles == 0);

        // check range against signal limits
        checkRange(highBit, lowBit, size);

        if (nb)
        {
            if (!isSynced(outputEdges))
            {
                ++cycles;
            }
        }
        else
        {
            if (!async)
            {
                syncDriveDelay(cycles);
            }
            cycles = 0;
        }

        BitVector writeMask = mask0.fillBits(highBit, lowBit, Bit.ONE);
        BitVector bvValue = ValueConverter.toBitVector(value).setLength(size);
        if (lowBit > 0)
        {
            bvValue = bvValue.shiftLeft(lowBit);
        }
        bvValue = bvValue.assignMask(mask0, writeMask.not());
        BitVector strongMask = soft ? mask0 : writeMask.andNot(bvValue
            .getZMask());
        outputScheduler.drive(cycles, outputSkew, bvValue, writeMask,
            strongMask);
    }

    protected final void doPartialRangeDrive(
        final OutputScheduler outputScheduler,
        final EdgeSet outputEdges,
        final int outputSkew,
        final int partHigh,
        final int partLow,
        final int size,
        final BitVector mask0,
        final BitVector mask1,
        int cycles,
        int highBit,
        int lowBit,
        Object value,
        boolean nb,
        boolean async,
        boolean soft)
    {
        final int partSize = partHigh - partLow + 1;

        // check range against partial limits
        checkRange(highBit, lowBit, partSize);

        doRangeDrive(outputScheduler, outputEdges, outputSkew, size, mask0,
            mask1, cycles, highBit + partLow, lowBit + partLow, value, nb,
            async, soft);
    }

    protected final boolean isSynced(EdgeSet edges)
    {
        final long simTime = dvSim.verilogSim.getSimTime();
        return clockMonitor.atEdge(simTime, edges);
    }

    protected final Event getClockEdgeEvent(EdgeSet edges)
    {
        return new SyncEdgeEvent(dvSim.dvEventManager, clockMonitor, edges, 0);
    }

    protected final void syncClockDelay(EdgeSet edges, int cycles)
    {
        assert (cycles >= 0);
        if (cycles > 0 || !isSynced(edges))
        {
            final Event e = getClockEdgeEvent(edges);
            do
            {
                dvSim.waitFor(e);
            }
            while (--cycles > 0);
        }
    }

    @Override
    public String toString()
    {
        return getClass().getName() + "{" + name + "}";
    }
}
