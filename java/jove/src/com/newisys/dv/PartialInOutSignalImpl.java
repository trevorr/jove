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
 * Implementation of a partial inout signal.
 * 
 * @author Trevor Robinson
 */
final class PartialInOutSignalImpl
    extends BasePartialSignalImpl
    implements InOutSignal
{
    private final InOutSignal inOut;

    public PartialInOutSignalImpl(InOutSignal inOut, int partHigh, int partLow)
    {
        super(inOut, partHigh, partLow);
        this.inOut = inOut;
    }

    public int getInputDepth()
    {
        return inOut.getInputDepth();
    }

    public EdgeSet getInputEdges()
    {
        return inOut.getInputEdges();
    }

    public int getInputSkew()
    {
        return inOut.getInputSkew();
    }

    @Override
    protected BitVector doSample(int depth, boolean async)
    {
        return doSample(inOut, depth, async);
    }

    @Override
    public Event getChangeEvent(BitVector mask, boolean async)
    {
        return getChangeEvent(inOut, mask, async);
    }

    @Override
    public Event getEdgeEvent(EdgeSet edges, int bit, boolean async)
    {
        return getEdgeEvent(inOut, edges, bit, async);
    }

    @Override
    public void syncSampleDelay(int cycles)
    {
        inOut.syncSampleDelay(cycles);
    }

    public EdgeSet getOutputEdges()
    {
        return inOut.getOutputEdges();
    }

    public int getOutputSkew()
    {
        return inOut.getOutputSkew();
    }

    @Override
    protected void doDrive(
        int cycles,
        Object value,
        boolean nb,
        boolean async,
        boolean soft)
    {
        doDrive(inOut, cycles, value, nb, async, soft);
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
        doRangeDrive(inOut, cycles, highBit, lowBit, value, nb, async, soft);
    }

    @Override
    public void syncDriveDelay(int cycles)
    {
        inOut.syncDriveDelay(cycles);
    }
}
