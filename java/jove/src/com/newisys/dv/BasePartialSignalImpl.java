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

/**
 * Base implementation for partial signals.
 * 
 * @author Trevor Robinson
 */
abstract class BasePartialSignalImpl
    extends BaseSignalImpl
{
    private final Signal baseSignal;
    protected final int partHigh;
    protected final int partLow;
    private final String name;

    public BasePartialSignalImpl(Signal baseSignal, int partHigh, int partLow)
    {
        this.baseSignal = baseSignal;
        name = baseSignal.getName() + "[" + partHigh + ":" + partLow + "]";
        this.partHigh = partHigh;
        this.partLow = partLow;
    }

    public final String getName()
    {
        return name;
    }

    public final ClockSignal getClock()
    {
        return baseSignal.getClock();
    }

    public final int getSize()
    {
        return partHigh - partLow + 1;
    }

    protected final BitVector doSample(
        final InputSignal in,
        final int depth,
        final boolean async)
    {
        final BitVector fullValue;
        if (depth < 0)
        {
            if (!async)
            {
                fullValue = in.sample();
            }
            else
            {
                fullValue = in.sampleAsync();
            }
        }
        else
        {
            if (!async)
            {
                fullValue = in.sampleDepth(depth);
            }
            else
            {
                fullValue = in.sampleDepthAsync(depth);
            }
        }
        return fullValue.getBits(partHigh, partLow);
    }

    protected final Event getEdgeEvent(
        final InputSignal in,
        final EdgeSet edges,
        final int bit,
        final boolean async)
    {
        final int partSize = partHigh - partLow + 1;
        checkIndex(bit, partSize);

        return in.getEdgeEvent(edges, bit + partLow, async);
    }

    protected final Event getChangeEvent(
        final InputSignal in,
        final BitVector mask,
        final boolean async)
    {
        // extend mask to be relative to full signal
        final BitVectorBuffer buf = new BitVectorBuffer(in.getSize(), Bit.ZERO);
        if (mask != null)
        {
            buf.setBits(partHigh, partLow, mask);
        }
        else
        {
            buf.fillBits(partHigh, partLow, Bit.ONE);
        }
        final BitVector fullMask = buf.toBitVector();

        return in.getChangeEvent(fullMask, async);
    }

    private static void decodeDrive(
        final OutputSignal out,
        final int cycles,
        final int highBit,
        final int lowBit,
        final Object value,
        final boolean nb,
        final boolean async,
        final boolean soft)
    {
        if (!async)
        {
            if (!nb)
            {
                if (!soft)
                {
                    out.driveRangeDelay(cycles, highBit, lowBit, value);
                }
                else
                {
                    out.driveRangeDelaySoft(cycles, highBit, lowBit, value);
                }
            }
            else
            {
                if (!soft)
                {
                    out.driveRangeDelayNB(cycles, highBit, lowBit, value);
                }
                else
                {
                    out.driveRangeDelaySoftNB(cycles, highBit, lowBit, value);
                }
            }
        }
        else
        {
            // async drive with delay would be nonsense
            assert (cycles == 0);

            if (!soft)
            {
                out.driveRangeAsync(highBit, lowBit, value);
            }
            else
            {
                out.driveRangeAsyncSoft(highBit, lowBit, value);
            }
        }
    }

    protected final void doDrive(
        final OutputSignal out,
        final int cycles,
        final Object value,
        final boolean nb,
        final boolean async,
        final boolean soft)
    {
        decodeDrive(out, cycles, partHigh, partLow, value, nb, async, soft);
    }

    protected final void doRangeDrive(
        final OutputSignal out,
        final int cycles,
        final int highBit,
        final int lowBit,
        final Object value,
        final boolean nb,
        final boolean async,
        final boolean soft)
    {
        final int partSize = partHigh - partLow + 1;
        checkRange(highBit, lowBit, partSize);

        decodeDrive(out, cycles, highBit + partLow, lowBit + partLow, value,
            nb, async, soft);
    }
}
