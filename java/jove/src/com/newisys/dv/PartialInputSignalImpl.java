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
import com.newisys.verilog.util.BitVector;

/**
 * Implementation of a partial input signal.
 * 
 * @author Trevor Robinson
 */
final class PartialInputSignalImpl
    extends BasePartialSignalImpl
    implements InputSignal
{
    private final InputSignal in;

    public PartialInputSignalImpl(InputSignal in, int partHigh, int partLow)
    {
        super(in, partHigh, partLow);
        this.in = in;
    }

    public int getInputDepth()
    {
        return in.getInputDepth();
    }

    public EdgeSet getInputEdges()
    {
        return in.getInputEdges();
    }

    public int getInputSkew()
    {
        return in.getInputSkew();
    }

    @Override
    protected BitVector doSample(int depth, boolean async)
    {
        return doSample(in, depth, async);
    }

    @Override
    public Event getChangeEvent(BitVector mask, boolean async)
    {
        return getChangeEvent(in, mask, async);
    }

    @Override
    public Event getEdgeEvent(EdgeSet edges, int bit, boolean async)
    {
        return getEdgeEvent(in, edges, bit, async);
    }

    @Override
    public void syncSampleDelay(int cycles)
    {
        in.syncSampleDelay(cycles);
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
}
