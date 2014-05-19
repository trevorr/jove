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

import java.util.LinkedList;
import java.util.List;

import com.newisys.eventsim.AnyEvent;
import com.newisys.eventsim.Event;
import com.newisys.verilog.EdgeSet;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.BitVectorBuffer;
import com.newisys.verilog.util.ValueConverter;

/**
 * Base implementation for concatenated signals.
 * 
 * @author Trevor Robinson
 */
abstract class BaseConcatSignalImpl
    extends BaseSignalImpl
{
    private final Signal[] signals;
    private final int[] sizes;
    private final int totalSize;

    private final InputSignal refIn;
    private final int maxDepth;
    private final OutputSignal refOut;

    public BaseConcatSignalImpl(Signal[] signals)
    {
        final int count = signals.length;
        if (count < 1)
        {
            throw new IllegalArgumentException(
                "Concatenated signal must contain at least one member signal");
        }

        this.signals = signals;
        this.sizes = new int[count];
        int totalSize = 0;
        for (int i = 0; i < count; ++i)
        {
            sizes[i] = signals[i].getSize();
            totalSize += sizes[i];
        }
        this.totalSize = totalSize;

        final Signal refSignal = signals[1];
        if (refSignal instanceof InputSignal)
        {
            refIn = (InputSignal) refSignal;
            int maxDepth = 0;
            for (int i = 0; i < count; ++i)
            {
                int depth = ((InputSignal) signals[i]).getInputDepth();
                if (depth > maxDepth) maxDepth = depth;
            }
            this.maxDepth = maxDepth;
        }
        else
        {
            refIn = null;
            maxDepth = 0;
        }
        if (refSignal instanceof OutputSignal)
        {
            refOut = (OutputSignal) refSignal;
        }
        else
        {
            refOut = null;
        }
    }

    public String getName()
    {
        final StringBuffer buf = new StringBuffer();
        buf.append('{');
        for (int i = 0; i < signals.length; ++i)
        {
            if (i > 0) buf.append(", ");
            buf.append(signals[i].getName());
        }
        buf.append('}');
        return buf.toString();
    }

    public ClockSignal getClock()
    {
        return refIn != null ? refIn.getClock() : refOut.getClock();
    }

    public int getSize()
    {
        return totalSize;
    }

    public EdgeSet getInputEdges()
    {
        return refIn.getInputEdges();
    }

    public int getInputSkew()
    {
        return refIn.getInputSkew();
    }

    public int getInputDepth()
    {
        return maxDepth;
    }

    @Override
    protected BitVector doSample(int depth, boolean async)
    {
        if (depth > maxDepth)
        {
            throw new IllegalArgumentException("Sample depth " + depth
                + " exceeds maximum depth " + maxDepth
                + " of concatenated signal");
        }

        if (!async)
        {
            syncSample();
        }

        final BitVectorBuffer buffer = new BitVectorBuffer(totalSize);
        int ofs = totalSize;
        for (int i = 0; i < signals.length; ++i)
        {
            final InputSignal signal = (InputSignal) signals[i];
            final BitVector value = (depth < 0) ? signal.sampleAsync() : signal
                .sampleDepthAsync(depth);
            final int highIndex = ofs - 1;
            final int lowIndex = ofs - sizes[i];
            ofs = lowIndex;
            buffer.setBits(highIndex, lowIndex, value);
        }
        return buffer.toBitVector();
    }

    @Override
    public void syncSampleDelay(int cycles)
    {
        refIn.syncSampleDelay(cycles);
    }

    @Override
    public Event getEdgeEvent(EdgeSet edges, int bit, boolean async)
    {
        // check for valid bit
        if (bit < 0 || bit >= totalSize)
        {
            throw new IllegalArgumentException("Invalid edge bit index for "
                + totalSize + "-bit signal: " + bit);
        }

        // build event(s)
        Event event = null;
        int ofs = totalSize;
        for (int i = 0; i < signals.length; ++i)
        {
            final InputSignal signal = (InputSignal) signals[i];
            final int highIndex = ofs - 1;
            final int lowIndex = ofs - sizes[i];
            if (bit >= lowIndex && bit <= highIndex)
            {
                event = signal.getEdgeEvent(edges, bit - lowIndex, async);
                break;
            }
            ofs = lowIndex;
        }
        assert (event != null);
        return event;
    }

    @Override
    public Event getChangeEvent(BitVector mask, boolean async)
    {
        final BitVector effMask;
        if (mask != null)
        {
            // adjust length of mask if necessary
            effMask = mask.setLength(totalSize);

            // check for valid mask
            if (!effMask.isNotZero())
            {
                throw new IllegalArgumentException("Invalid change mask for "
                    + totalSize + "-bit signal: " + mask);
            }
        }
        else
        {
            // not using mask
            effMask = null;
        }

        // build event(s)
        final List<Event> events = new LinkedList<Event>();
        int ofs = totalSize;
        for (int i = 0; i < signals.length; ++i)
        {
            final InputSignal signal = (InputSignal) signals[i];
            if (effMask != null)
            {
                final int highIndex = ofs - 1;
                final int lowIndex = ofs - sizes[i];
                final BitVector signalMask = effMask.getBits(highIndex,
                    lowIndex);
                if (signalMask.isNotZero())
                {
                    events.add(signal.getChangeEvent(signalMask, async));
                }
                ofs = lowIndex;
            }
            else
            {
                events.add(signal.getChangeEvent(null, async));
            }
        }
        assert (!events.isEmpty());
        return new AnyEvent(events, true);
    }

    public EdgeSet getOutputEdges()
    {
        return refOut.getOutputEdges();
    }

    public int getOutputSkew()
    {
        return refOut.getOutputSkew();
    }

    @Override
    protected void doDrive(
        int cycles,
        Object value,
        boolean nb,
        boolean async,
        boolean soft)
    {
        // async drive with delay would be nonsense
        assert (!async || cycles == 0);

        if (!nb && !async)
        {
            syncDriveDelay(cycles);
        }

        BitVector bv = ValueConverter.toBitVector(value);

        // zero-extend value if necessary
        if (bv.length() < totalSize) bv = bv.setLength(totalSize);

        int ofs = totalSize;
        for (int i = 0; i < signals.length; ++i)
        {
            final OutputSignal signal = (OutputSignal) signals[i];
            final int highIndex = ofs - 1;
            final int lowIndex = ofs - sizes[i];
            ofs = lowIndex;
            driveSignal(signal, cycles, bv.getBits(highIndex, lowIndex), nb,
                soft);
        }
    }

    private static void driveSignal(
        OutputSignal signal,
        int cycles,
        Object value,
        boolean nb,
        boolean soft)
    {
        if (nb)
        {
            if (soft)
            {
                signal.driveDelaySoftNB(cycles, value);
            }
            else
            {
                signal.driveDelayNB(cycles, value);
            }
        }
        else
        {
            if (soft)
            {
                signal.driveAsyncSoft(value);
            }
            else
            {
                signal.driveAsync(value);
            }
        }
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
        // async drive with delay would be nonsense
        assert (!async || cycles == 0);

        // check range
        if (lowBit < 0 || highBit >= totalSize || lowBit > highBit)
        {
            throw new IllegalArgumentException("Invalid bit range for "
                + totalSize + "-bit signal: high=" + highBit + ", low="
                + lowBit);
        }

        if (!nb && !async)
        {
            syncDriveDelay(cycles);
        }

        BitVector bv = ValueConverter.toBitVector(value);

        // zero-extend value if necessary
        final int valueSize = highBit - lowBit + 1;
        if (bv.length() < valueSize) bv = bv.setLength(valueSize);

        int signalOfs = totalSize;
        int valueOfs = valueSize;
        for (int i = 0; i < signals.length; ++i)
        {
            final OutputSignal signal = (OutputSignal) signals[i];

            // determine high/low indices of this signal relative to the
            // entire concatenation
            final int signalSize = sizes[i];
            final int signalHigh = signalOfs - 1;
            final int signalLow = signalOfs - signalSize;
            assert (signalLow >= 0);

            // always extract signal values from high end of value vector
            final int valueHigh = valueOfs - 1;

            // writing all of the current signal?
            if ((signalHigh <= highBit && signalHigh >= lowBit)
                && (signalLow <= highBit && signalLow >= lowBit))
            {
                // determine the low index to extract from the value vector
                // and update the value offset
                final int valueLow = valueOfs - signalSize;
                assert (valueLow >= 0);
                valueOfs = valueLow;

                driveSignal(signal, cycles, bv.getBits(valueHigh, valueLow),
                    nb, soft);
            }
            // writing any (but not all) of the current signal?
            else if ((highBit <= signalHigh && highBit >= signalLow)
                || (lowBit <= signalHigh && lowBit >= signalLow))
            {
                // determine the number of bits excluded from each end of this
                // signal due to the range
                int highExclude = signalHigh - highBit;
                if (highExclude < 0) highExclude = 0;
                int lowExclude = lowBit - signalLow;
                if (lowExclude < 0) lowExclude = 0;

                // determine which bits to drive based on the exclusions
                final int driveHigh = signalSize - highExclude - 1;
                final int driveLow = lowExclude;
                final int driveSize = driveHigh - driveLow + 1;
                assert (driveSize >= 1);

                // determine the low index to extract from the value vector
                // and update the value offset
                final int valueLow = valueOfs - driveSize;
                assert (valueLow >= 0);
                valueOfs = valueLow;

                driveSignalRange(signal, cycles, driveHigh, driveLow, bv
                    .getBits(valueHigh, valueLow), nb, soft);
            }

            signalOfs = signalLow;
        }
    }

    private static void driveSignalRange(
        OutputSignal signal,
        int cycles,
        int highBit,
        int lowBit,
        Object value,
        boolean nb,
        boolean soft)
    {
        if (nb)
        {
            if (soft)
            {
                signal.driveRangeDelaySoftNB(cycles, highBit, lowBit, value);
            }
            else
            {
                signal.driveRangeDelayNB(cycles, highBit, lowBit, value);
            }
        }
        else
        {
            if (soft)
            {
                signal.driveRangeAsyncSoft(highBit, lowBit, value);
            }
            else
            {
                signal.driveRangeAsync(highBit, lowBit, value);
            }
        }
    }

    @Override
    public void syncDriveDelay(int cycles)
    {
        refOut.syncDriveDelay(cycles);
    }

    @Override
    public String toString()
    {
        return getClass().getName() + getName();
    }
}
