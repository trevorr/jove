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
 * Implementation of an input signal.
 * 
 * @author Trevor Robinson
 */
final class InputSignalImpl
    extends BaseRealSignalImpl
    implements InputSignal
{
    private final ClockSignal clockSignal;
    private final int size;
    private final EdgeSet inputEdges;
    private final ClockedInputMonitor inputMonitor;

    public InputSignalImpl(
        DVSimulation dvSim,
        String name,
        InputMonitor inputMonitor,
        ClockSignal clockSignal,
        ClockMonitor clockMonitor,
        EdgeSet inputEdges,
        int inputSkew,
        int bufferDepth)
    {
        super(dvSim, name, clockMonitor);

        this.clockSignal = clockSignal;
        size = inputMonitor.getSignalRead().getSize();

        this.inputEdges = inputEdges;
        this.inputMonitor = new ClockedInputMonitor(inputMonitor, clockMonitor,
            inputEdges, inputSkew, bufferDepth);
    }

    public ClockSignal getClock()
    {
        return clockSignal;
    }

    public int getSize()
    {
        return size;
    }

    public EdgeSet getInputEdges()
    {
        return inputEdges;
    }

    public int getInputSkew()
    {
        return inputMonitor.getSkew();
    }

    public int getInputDepth()
    {
        return inputMonitor.getBufferDepth();
    }

    @Override
    protected BitVector doSample(int depth, boolean async)
    {
        return doSample(inputMonitor, depth, async);
    }

    @Override
    public void syncSampleDelay(int cycles)
    {
        syncClockDelay(inputEdges, cycles);
    }

    @Override
    public Event getEdgeEvent(EdgeSet edges, int bit, boolean async)
    {
        return getEdgeEvent(inputMonitor, edges, bit, async);
    }

    @Override
    public Event getChangeEvent(BitVector mask, boolean async)
    {
        return getChangeEvent(inputMonitor, mask, async);
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
