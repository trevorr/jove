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
import com.newisys.verilog.util.Bit;

/**
 * Tracks value changes on a clock signal.
 * 
 * @author Trevor Robinson
 */
final class ClockMonitor
    extends BaseMonitor
{
    // some members have default access for efficient access by inner class
    final DVEventManager dvEventManager;
    final InputMonitor inputMonitor;
    final int bit;
    final BackRefBuffer buffer;
    Bit currentValue;
    EdgeSet lastEdge = null;
    long lastEdgeTime = -1;
    int posEdgeCount = 0;

    public ClockMonitor(
        DVEventManager dvEventManager,
        InputMonitor inputMonitor,
        int bit,
        int bufferDepth)
    {
        super(inputMonitor);

        // check arguments
        assert (dvEventManager != null);
        assert (inputMonitor != null);
        assert (bufferDepth >= 1);

        // store attributes from constructor arguments
        this.dvEventManager = dvEventManager;
        this.inputMonitor = inputMonitor;
        this.bit = bit;

        // create the cycle back-reference buffer
        buffer = new BackRefBuffer(bufferDepth, Bit.X);

        // get initial value
        currentValue = ValueUtil.getBit(inputMonitor.getCurrentValue(), bit);

        // set up handler for clock edge callbacks
        inputMonitor.addListener(new ClockEdgeCallback());
    }

    public InputMonitor getInputMonitor()
    {
        return inputMonitor;
    }

    public int getBit()
    {
        return bit;
    }

    public int getBufferDepth()
    {
        return buffer.getDepth();
    }

    public void setBufferDepth(int newDepth)
    {
        buffer.setDepth(newDepth);
    }

    public Bit getValue(int depth)
    {
        Object fullValue = buffer.getValue(depth);
        return ValueUtil.getBit(fullValue, bit);
    }

    public Bit getValueAsync()
    {
        return currentValue;
    }

    public EdgeSet getLastEdge()
    {
        return lastEdge;
    }

    public long getLastEdgeTime()
    {
        return lastEdgeTime;
    }

    public int getPosEdgeCount()
    {
        return posEdgeCount;
    }

    public EdgeSet getCurrentEdge(long simTime)
    {
        return (simTime == lastEdgeTime) ? lastEdge : null;
    }

    public boolean atEdge(long simTime, EdgeSet edges)
    {
        EdgeSet curEdge = getCurrentEdge(simTime);
        return curEdge != null && edges.contains(curEdge);
    }

    @Override
    public String toString()
    {
        return "ClockMonitor{" + inputMonitor + "}";
    }

    private final class ClockEdgeCallback
        implements EdgeListener
    {
        public EdgeSet getEdges()
        {
            return EdgeSet.ANYEDGE;
        }

        public int getBit()
        {
            return bit;
        }

        public void notifyEdge(final VerilogSimTime time, final EdgeSet edge)
        {
            final long simTime = time.getSimTime();
            final Bit oldValue = currentValue;
            final Bit newValue = ValueUtil.getBit(inputMonitor
                .getCurrentValue(), bit);

            dvEventManager.registerSynchCallback(new SynchListener()
            {
                public void notifySynch()
                {
                    if (Debug.enabled)
                    {
                        Debug.out.println(ClockMonitor.this
                            + ".ClockEdgeCallback: " + oldValue + " -> "
                            + newValue + " @ " + simTime);
                    }

                    // shift new value into buffer
                    buffer.pushValue(newValue);

                    // update current value, last edge, last edge time
                    currentValue = newValue;
                    lastEdge = edge;
                    lastEdgeTime = simTime;

                    // count rising edges
                    if (EdgeSet.POSEDGE.contains(edge))
                    {
                        ++posEdgeCount;
                    }

                    // notify listeners
                    notifyListeners(time, oldValue, newValue);
                }
            });
        }
    }
}
