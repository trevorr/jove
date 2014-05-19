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

import com.newisys.eventsim.PulseEvent;
import com.newisys.verilog.EdgeSet;
import com.newisys.verilog.VerilogSimTime;

/**
 * Base class for events corresponding to a signal edge.
 * 
 * @author Trevor Robinson
 */
abstract class EdgeEvent
    extends PulseEvent
    implements EdgeListener
{
    protected final EdgeMonitor monitor;
    protected final EdgeSet edges;
    protected final int bit;

    public EdgeEvent(
        String baseName,
        EdgeMonitor monitor,
        EdgeSet edges,
        int bit)
    {
        super(getEventName(baseName, monitor, edges, bit));
        this.monitor = monitor;
        this.edges = edges;
        this.bit = bit;
    }

    private static String getEventName(
        String baseName,
        EdgeMonitor monitor,
        EdgeSet edges,
        int bit)
    {
        return baseName + "{" + edges + " " + monitor.getSignalName() + ":"
            + bit + "}";
    }

    @Override
    protected void preWait()
    {
        monitor.addListener(this);
    }

    @Override
    protected void postWait()
    {
        monitor.removeListener(this);
    }

    public EdgeSet getEdges()
    {
        return edges;
    }

    public int getBit()
    {
        return bit;
    }

    public void notifyEdge(VerilogSimTime time, EdgeSet edge)
    {
        if (Debug.enabled)
        {
            Debug.out.println(this + ": " + edge + " @ " + time);
        }
    }
}
