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
import com.newisys.verilog.util.BitRange;
import com.newisys.verilog.util.BitVector;

/**
 * A wrapper that allows InputSignals and OutputSignals to be referenced as
 * InOutSignals. This class is used especially for port classes, which, when
 * not directionally constrained, are made up entirely of InOutSignal members.
 * <p>
 * While this class allows input or output signals to be referenced as in/outs,
 * it is still a runtime error to drive an input signal or sample an output
 * signal. Specifically, if InputSignal methods are called on a wrapped
 * OutputSignal, or vice versa, a {@link DVRuntimeException} is thrown.
 * 
 * @author Trevor Robinson
 */
public final class PortSignalWrapper
    implements InOutSignal, ClockSignal
{
    final Signal signal;
    private final InputSignal in;
    private final OutputSignal out;

    /**
     * Creates a new PortSignalWrapper with the specified InputSignal.
     *
     * @param signal the InputSignal to wrap
     */
    public PortSignalWrapper(InputSignal signal)
    {
        this.signal = signal;
        this.in = signal;
        this.out = null;
    }

    /**
     * Creates a new PortSignalWrapper with the specified OutputSignal.
     *
     * @param signal the OutputSignal to wrap
     */
    public PortSignalWrapper(OutputSignal signal)
    {
        this.signal = signal;
        this.in = null;
        this.out = signal;
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return signal.getName();
    }

    /**
     * {@inheritDoc}
     */
    public ClockSignal getClock()
    {
        return signal.getClock();
    }

    /**
     * {@inheritDoc}
     */
    public int getSize()
    {
        return signal.getSize();
    }

    /**
     * Returns whether or not this signal wrapped by this PortSignalWrapper
     * is an {@link InputSignal}.
     *
     * @return <code>true</code> if the wrapped signal is an InputSignal,
     *      <code>false</code> otherwise
     */
    public boolean isInput()
    {
        return in != null;
    }

    /**
     * Returns whether or not this signal wrapped by this PortSignalWrapper
     * is an {@link OutputSignal}.
     *
     * @return <code>true</code> if the wrapped signal is an OutputSignal,
     *      <code>false</code> otherwise
     */
    public boolean isOutput()
    {
        return out != null;
    }

    /**
     * Returns whether or not this signal wrapped by this PortSignalWrapper
     * is a {@link ClockSignal}.
     *
     * @return <code>true</code> if the wrapped signal is a ClockSignal,
     *      <code>false</code> otherwise
     */
    public boolean isClock()
    {
        return signal instanceof ClockSignal;
    }

    private void checkInput()
    {
        if (in == null)
        {
            throw new DVRuntimeException("Signal is not an input ["
                + signal.getName() + "]");
        }
    }

    /**
     * {@inheritDoc}
     */
    public EdgeSet getInputEdges()
    {
        checkInput();
        return in.getInputEdges();
    }

    /**
     * {@inheritDoc}
     */
    public int getInputSkew()
    {
        checkInput();
        return in.getInputSkew();
    }

    /**
     * {@inheritDoc}
     */
    public int getInputDepth()
    {
        checkInput();
        return in.getInputDepth();
    }

    /**
     * {@inheritDoc}
     */
    public BitVector sample()
    {
        checkInput();
        return in.sample();
    }

    /**
     * {@inheritDoc}
     */
    public BitVector sampleDepth(int depth)
    {
        checkInput();
        return in.sampleDepth(depth);
    }

    /**
     * {@inheritDoc}
     */
    public BitVector sampleAsync()
    {
        checkInput();
        return in.sampleAsync();
    }

    /**
     * {@inheritDoc}
     */
    public BitVector sampleDepthAsync(int depth)
    {
        checkInput();
        return in.sampleDepthAsync(depth);
    }

    /**
     * {@inheritDoc}
     */
    public void syncSample()
    {
        checkInput();
        in.syncSample();
    }

    /**
     * {@inheritDoc}
     */
    public void syncSampleDelay(int cycles)
    {
        checkInput();
        in.syncSampleDelay(cycles);
    }

    /**
     * {@inheritDoc}
     */
    public void syncEdge(EdgeSet edges)
    {
        checkInput();
        in.syncEdge(edges);
    }

    /**
     * {@inheritDoc}
     */
    public void syncEdge(EdgeSet edges, int bit)
    {
        checkInput();
        in.syncEdge(edges, bit);
    }

    /**
     * {@inheritDoc}
     */
    public void syncEdgeAsync(EdgeSet edges)
    {
        checkInput();
        in.syncEdgeAsync(edges);
    }

    /**
     * {@inheritDoc}
     */
    public void syncEdgeAsync(EdgeSet edges, int bit)
    {
        checkInput();
        in.syncEdgeAsync(edges, bit);
    }

    /**
     * {@inheritDoc}
     */
    public Event getEdgeEvent(EdgeSet edges, int bit, boolean async)
    {
        checkInput();
        return in.getEdgeEvent(edges, bit, async);
    }

    /**
     * {@inheritDoc}
     */
    public void syncChange()
    {
        checkInput();
        in.syncChange();
    }

    /**
     * {@inheritDoc}
     */
    public void syncChange(BitVector mask)
    {
        checkInput();
        in.syncChange(mask);
    }

    /**
     * {@inheritDoc}
     */
    public void syncChangeAsync()
    {
        checkInput();
        in.syncChangeAsync();
    }

    /**
     * {@inheritDoc}
     */
    public void syncChangeAsync(BitVector mask)
    {
        checkInput();
        in.syncChangeAsync(mask);
    }

    /**
     * {@inheritDoc}
     */
    public Event getChangeEvent(BitVector mask, boolean async)
    {
        checkInput();
        return in.getChangeEvent(mask, async);
    }

    private void checkOutput()
    {
        if (out == null)
        {
            throw new DVRuntimeException("Signal is not an output ["
                + signal.getName() + "]");
        }
    }

    /**
     * {@inheritDoc}
     */
    public EdgeSet getOutputEdges()
    {
        checkOutput();
        return out.getOutputEdges();
    }

    /**
     * {@inheritDoc}
     */
    public int getOutputSkew()
    {
        checkOutput();
        return out.getOutputSkew();
    }

    /**
     * {@inheritDoc}
     */
    public void drive(Object value)
    {
        checkOutput();
        out.drive(value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveNB(Object value)
    {
        checkOutput();
        out.driveNB(value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveDelay(int cycles, Object value)
    {
        checkOutput();
        out.driveDelay(cycles, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveDelayNB(int cycles, Object value)
    {
        checkOutput();
        out.driveDelayNB(cycles, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveAsync(Object value)
    {
        checkOutput();
        out.driveAsync(value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveSoft(Object value)
    {
        checkOutput();
        out.driveSoft(value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveSoftNB(Object value)
    {
        checkOutput();
        out.driveSoftNB(value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveDelaySoft(int cycles, Object value)
    {
        checkOutput();
        out.driveDelaySoft(cycles, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveDelaySoftNB(int cycles, Object value)
    {
        checkOutput();
        out.driveDelaySoftNB(cycles, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveAsyncSoft(Object value)
    {
        checkOutput();
        out.driveAsyncSoft(value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveRange(int highBit, int lowBit, Object value)
    {
        checkOutput();
        out.driveRange(highBit, lowBit, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveRange(BitRange range, Object value)
    {
        checkOutput();
        out.driveRange(range, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveRangeNB(int highBit, int lowBit, Object value)
    {
        checkOutput();
        out.driveRangeNB(highBit, lowBit, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveRangeNB(BitRange range, Object value)
    {
        checkOutput();
        out.driveRangeNB(range, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveRangeDelay(
        int cycles,
        int highBit,
        int lowBit,
        Object value)
    {
        checkOutput();
        out.driveRangeDelay(cycles, highBit, lowBit, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveRangeDelay(int cycles, BitRange range, Object value)
    {
        checkOutput();
        out.driveRangeDelay(cycles, range, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveRangeDelayNB(
        int cycles,
        int highBit,
        int lowBit,
        Object value)
    {
        checkOutput();
        out.driveRangeDelayNB(cycles, highBit, lowBit, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveRangeDelayNB(int cycles, BitRange range, Object value)
    {
        checkOutput();
        out.driveRangeDelayNB(cycles, range, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveRangeAsync(int highBit, int lowBit, Object value)
    {
        checkOutput();
        out.driveRangeAsync(highBit, lowBit, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveRangeAsync(BitRange range, Object value)
    {
        checkOutput();
        out.driveRangeAsync(range, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveRangeSoft(int highBit, int lowBit, Object value)
    {
        checkOutput();
        out.driveRangeSoft(highBit, lowBit, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveRangeSoft(BitRange range, Object value)
    {
        checkOutput();
        out.driveRangeSoft(range, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveRangeSoftNB(int highBit, int lowBit, Object value)
    {
        checkOutput();
        out.driveRangeSoftNB(highBit, lowBit, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveRangeSoftNB(BitRange range, Object value)
    {
        checkOutput();
        out.driveRangeSoftNB(range, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveRangeDelaySoft(
        int cycles,
        int highBit,
        int lowBit,
        Object value)
    {
        checkOutput();
        out.driveRangeDelaySoft(cycles, highBit, lowBit, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveRangeDelaySoft(int cycles, BitRange range, Object value)
    {
        checkOutput();
        out.driveRangeDelaySoft(cycles, range, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveRangeDelaySoftNB(
        int cycles,
        int highBit,
        int lowBit,
        Object value)
    {
        checkOutput();
        out.driveRangeDelaySoftNB(cycles, highBit, lowBit, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveRangeDelaySoftNB(int cycles, BitRange range, Object value)
    {
        checkOutput();
        out.driveRangeDelaySoftNB(cycles, range, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveRangeAsyncSoft(int highBit, int lowBit, Object value)
    {
        checkOutput();
        out.driveRangeAsyncSoft(highBit, lowBit, value);
    }

    /**
     * {@inheritDoc}
     */
    public void driveRangeAsyncSoft(BitRange range, Object value)
    {
        checkOutput();
        out.driveRangeAsyncSoft(range, value);
    }

    /**
     * {@inheritDoc}
     */
    public void syncDrive()
    {
        checkOutput();
        out.syncDrive();
    }

    /**
     * {@inheritDoc}
     */
    public void syncDriveDelay(int cycles)
    {
        checkOutput();
        out.syncDriveDelay(cycles);
    }

    /**
     * {@inheritDoc}
     */
    public int getCycleCount()
    {
        return signal.getClock().getCycleCount();
    }
}
