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
import com.newisys.eventsim.SimulationThread;
import com.newisys.verilog.EdgeSet;
import com.newisys.verilog.util.BitRange;
import com.newisys.verilog.util.BitVector;

/**
 * Provides a basis for easily implementing an input, output, or input/output
 * signal by implementing the bulk of InOutSignal in terms of a few abstract
 * methods.
 * 
 * @author Trevor Robinson
 */
abstract class BaseSignalImpl
    implements Signal
{
    protected final void checkIndex(int bit, int size)
    {
        if (bit < 0 || bit >= size)
        {
            throw new IllegalArgumentException("Invalid bit index for " + size
                + "-bit signal " + getName() + ": " + bit);
        }
    }

    protected final void checkRange(int highBit, int lowBit, int size)
    {
        if (lowBit < 0 || highBit >= size || lowBit > highBit)
        {
            throw new IllegalArgumentException("Invalid bit range for " + size
                + "-bit signal " + getName() + ": high=" + highBit + ", low="
                + lowBit);
        }
    }

    protected abstract BitVector doSample(int depth, boolean async);

    public final BitVector sample()
    {
        return doSample(-1, false);
    }

    public final BitVector sampleDepth(int depth)
    {
        return doSample(depth, false);
    }

    public final BitVector sampleAsync()
    {
        return doSample(-1, true);
    }

    public final BitVector sampleDepthAsync(int depth)
    {
        return doSample(depth, true);
    }

    public final void syncSample()
    {
        syncSampleDelay(0);
    }

    public abstract void syncSampleDelay(int cycles);

    public final void syncEdge(EdgeSet edges)
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.waitFor(getEdgeEvent(edges, 0, false));
    }

    public final void syncEdge(EdgeSet edges, int bit)
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.waitFor(getEdgeEvent(edges, bit, false));
    }

    public final void syncEdgeAsync(EdgeSet edges)
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.waitFor(getEdgeEvent(edges, 0, true));
    }

    public final void syncEdgeAsync(EdgeSet edges, int bit)
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.waitFor(getEdgeEvent(edges, bit, true));
    }

    public abstract Event getEdgeEvent(EdgeSet edges, int bit, boolean async);

    public final void syncChange()
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.waitFor(getChangeEvent(null, false));
    }

    public final void syncChange(BitVector mask)
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.waitFor(getChangeEvent(mask, false));
    }

    public final void syncChangeAsync()
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.waitFor(getChangeEvent(null, true));
    }

    public final void syncChangeAsync(BitVector mask)
    {
        final SimulationThread t = SimulationThread.currentThread();
        t.waitFor(getChangeEvent(mask, true));
    }

    public abstract Event getChangeEvent(BitVector mask, boolean async);

    protected abstract void doDrive(
        int cycles,
        Object value,
        boolean nb,
        boolean async,
        boolean soft);

    public final void drive(Object value)
    {
        doDrive(0, value, false, false, false);
    }

    public final void driveNB(Object value)
    {
        doDrive(0, value, true, false, false);
    }

    public final void driveDelay(int cycles, Object value)
    {
        doDrive(cycles, value, false, false, false);
    }

    public final void driveDelayNB(int cycles, Object value)
    {
        doDrive(cycles, value, true, false, false);
    }

    public final void driveAsync(Object value)
    {
        doDrive(0, value, false, true, false);
    }

    public final void driveSoft(Object value)
    {
        doDrive(0, value, false, false, true);
    }

    public final void driveSoftNB(Object value)
    {
        doDrive(0, value, true, false, true);
    }

    public final void driveDelaySoft(int cycles, Object value)
    {
        doDrive(cycles, value, false, false, true);
    }

    public final void driveDelaySoftNB(int cycles, Object value)
    {
        doDrive(cycles, value, true, false, true);
    }

    public final void driveAsyncSoft(Object value)
    {
        doDrive(0, value, false, true, true);
    }

    protected abstract void doRangeDrive(
        int cycles,
        int highBit,
        int lowBit,
        Object value,
        boolean nb,
        boolean async,
        boolean soft);

    public final void driveRange(int highBit, int lowBit, Object value)
    {
        doRangeDrive(0, highBit, lowBit, value, false, false, false);
    }

    public final void driveRange(BitRange range, Object value)
    {
        doRangeDrive(0, range.high(), range.low(), value, false, false, false);
    }

    public final void driveRangeNB(int highBit, int lowBit, Object value)
    {
        doRangeDrive(0, highBit, lowBit, value, true, false, false);
    }

    public final void driveRangeNB(BitRange range, Object value)
    {
        doRangeDrive(0, range.high(), range.low(), value, true, false, false);
    }

    public final void driveRangeDelay(
        int cycles,
        int highBit,
        int lowBit,
        Object value)
    {
        doRangeDrive(cycles, highBit, lowBit, value, false, false, false);
    }

    public final void driveRangeDelay(int cycles, BitRange range, Object value)
    {
        doRangeDrive(cycles, range.high(), range.low(), value, false, false,
            false);
    }

    public final void driveRangeDelayNB(
        int cycles,
        int highBit,
        int lowBit,
        Object value)
    {
        doRangeDrive(cycles, highBit, lowBit, value, true, false, false);
    }

    public final void driveRangeDelayNB(int cycles, BitRange range, Object value)
    {
        doRangeDrive(cycles, range.high(), range.low(), value, true, false,
            false);
    }

    public final void driveRangeAsync(int highBit, int lowBit, Object value)
    {
        doRangeDrive(0, highBit, lowBit, value, false, true, false);
    }

    public final void driveRangeAsync(BitRange range, Object value)
    {
        doRangeDrive(0, range.high(), range.low(), value, false, true, false);
    }

    public final void driveRangeSoft(int highBit, int lowBit, Object value)
    {
        doRangeDrive(0, highBit, lowBit, value, false, false, true);
    }

    public final void driveRangeSoft(BitRange range, Object value)
    {
        doRangeDrive(0, range.high(), range.low(), value, false, false, true);
    }

    public final void driveRangeSoftNB(int highBit, int lowBit, Object value)
    {
        doRangeDrive(0, highBit, lowBit, value, true, false, true);
    }

    public final void driveRangeSoftNB(BitRange range, Object value)
    {
        doRangeDrive(0, range.high(), range.low(), value, true, false, true);
    }

    public final void driveRangeDelaySoft(
        int cycles,
        int highBit,
        int lowBit,
        Object value)
    {
        doRangeDrive(cycles, highBit, lowBit, value, false, false, true);
    }

    public final void driveRangeDelaySoft(
        int cycles,
        BitRange range,
        Object value)
    {
        doRangeDrive(cycles, range.high(), range.low(), value, false, false,
            true);
    }

    public final void driveRangeDelaySoftNB(
        int cycles,
        int highBit,
        int lowBit,
        Object value)
    {
        doRangeDrive(cycles, highBit, lowBit, value, true, false, true);
    }

    public final void driveRangeDelaySoftNB(
        int cycles,
        BitRange range,
        Object value)
    {
        doRangeDrive(cycles, range.high(), range.low(), value, true, false,
            true);
    }

    public final void driveRangeAsyncSoft(int highBit, int lowBit, Object value)
    {
        doRangeDrive(0, highBit, lowBit, value, false, true, true);
    }

    public final void driveRangeAsyncSoft(BitRange range, Object value)
    {
        doRangeDrive(0, range.high(), range.low(), value, false, true, true);
    }

    public final void syncDrive()
    {
        syncDriveDelay(0);
    }

    public abstract void syncDriveDelay(int cycles);
}
