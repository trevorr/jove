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
import com.newisys.verilog.VerilogSimTime;
import com.newisys.verilog.util.BitVector;

/**
 * Event triggered when a single changes value.
 * 
 * @author Trevor Robinson
 */
abstract class ValueChangeEvent
    extends PulseEvent
    implements ValueChangeListener
{
    protected final ValueChangeMonitor monitor;
    protected final BitVector mask;

    public ValueChangeEvent(
        String baseName,
        ValueChangeMonitor monitor,
        BitVector mask)
    {
        super(getEventName(baseName, monitor, mask));
        this.monitor = monitor;
        this.mask = mask;
    }

    private static String getEventName(
        String baseName,
        ValueChangeMonitor monitor,
        BitVector mask)
    {
        final StringBuffer buf = new StringBuffer();
        buf.append(baseName);
        buf.append('{');
        buf.append(monitor.getSignalName());
        if (mask != null)
        {
            buf.append(':');
            buf.append(mask);
        }
        buf.append('}');
        return buf.toString();
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

    public BitVector getMask()
    {
        return mask;
    }

    public void notifyChange(VerilogSimTime time, Object value)
    {
        if (Debug.enabled)
        {
            Debug.out.println(this + ": " + value + " @ " + time);
        }
    }
}
