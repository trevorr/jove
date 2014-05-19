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

import com.newisys.verilog.EdgeSet;
import com.newisys.verilog.VerilogSimTime;
import com.newisys.verilog.util.BitVector;

/**
 * Tracks value changes on a clock-relative input signal.
 * 
 * @author Trevor Robinson
 */
final class ClockedInputMonitor
    extends BaseMonitor
{
    // some members have default access for efficient access by inner class
    final InputMonitor inputMonitor;
    private final ClockMonitor clockMonitor;
    final EdgeSet clockEdges;
    final int skew;
    final BackRefBuffer buffer;
    long lastCycleTime;

    public ClockedInputMonitor(
        InputMonitor inputMonitor,
        ClockMonitor clockMonitor,
        EdgeSet clockEdges,
        int skew,
        int bufferDepth)
    {
        super(inputMonitor);

        // check arguments
        assert (inputMonitor != null);
        assert (clockMonitor != null);
        assert (clockEdges != null && !clockEdges.isEmpty());
        assert (skew <= 0);
        assert (bufferDepth >= 1);

        // store attributes from constructor arguments
        this.inputMonitor = inputMonitor;
        this.clockMonitor = clockMonitor;
        this.clockEdges = clockEdges;
        this.skew = skew;

        // ensure adequate aging in input monitor
        if (inputMonitor.getMaxAge() < -skew)
        {
            inputMonitor.setMaxAge(-skew);
        }

        // create the cycle back-reference buffer
        final int size = inputMonitor.getSignalRead().getSize();
        final BitVector xValue = new BitVector(size);
        buffer = new BackRefBuffer(bufferDepth, xValue);

        // set up handler for clock edge callbacks
        clockMonitor.addListener(new ClockEdgeCallback());
    }

    public InputMonitor getInputMonitor()
    {
        return inputMonitor;
    }

    public ClockMonitor getClockMonitor()
    {
        return clockMonitor;
    }

    public EdgeSet getClockEdges()
    {
        return clockEdges;
    }

    public int getSkew()
    {
        return skew;
    }

    public int getBufferDepth()
    {
        return buffer.getDepth();
    }

    public void setBufferDepth(int newDepth)
    {
        buffer.setDepth(newDepth);
    }

    public Object getValue(int depth)
    {
        return buffer.getValue(depth);
    }

    public Object getValueAsync(long simTime)
    {
        return inputMonitor.getAgedValue(simTime, -skew);
    }

    public long getLastChangeTime()
    {
        return inputMonitor.getLastChangeTime();
    }

    public long getLastCycleTime()
    {
        return lastCycleTime;
    }

    public EdgeSet getPreviousEdgeInCycle(int bit)
    {
        if (lastCycleTime < inputMonitor.getLastChangeTime())
        {
            return inputMonitor.getPreviousEdge(bit);
        }
        else
        {
            return EdgeSet.NO_EDGE;
        }
    }

    public boolean isChangedInCycle(BitVector mask)
    {
        if (lastCycleTime < inputMonitor.getLastChangeTime())
        {
            return inputMonitor.isChangedFromPrevious(mask);
        }
        else
        {
            return false;
        }
    }

    @Override
    public String toString()
    {
        return "ClockedInputMonitor{" + inputMonitor + ", " + clockMonitor
            + "}";
    }

    private final class ClockEdgeCallback
        implements EdgeListener
    {
        public EdgeSet getEdges()
        {
            return clockEdges;
        }

        public int getBit()
        {
            return 0;
        }

        public void notifyEdge(VerilogSimTime time, EdgeSet edge)
        {
            long simTime = time.getSimTime();
            Object oldValue = buffer.getValue(0);
            Object newValue = inputMonitor.getAgedValue(simTime, -skew);

            if (Debug.enabled)
            {
                Debug.out.println(ClockedInputMonitor.this
                    + ".ClockEdgeCallback: " + oldValue + " -> " + newValue
                    + " @ " + simTime + " (skew " + skew + ")");
            }

            // shift new value into buffer
            buffer.pushValue(newValue);

            // track time of last clock edge
            lastCycleTime = simTime;

            // notify listeners
            notifyListeners(time, oldValue, newValue);
        }
    }
}
