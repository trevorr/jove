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

import com.newisys.verilog.VerilogSimTime;
import com.newisys.verilog.util.BitVector;

/**
 * Event triggered when a signal value change occurs asynchronously.
 * 
 * @author Trevor Robinson
 */
final class AsyncValueChangeEvent
    extends ValueChangeEvent
{
    private final DVEventManager dvEventManager;
    private final int delay;

    public AsyncValueChangeEvent(
        DVEventManager dvEventManager,
        ValueChangeMonitor monitor,
        BitVector mask,
        int delay)
    {
        super("AsyncValueChangeEvent", monitor, mask);
        this.dvEventManager = dvEventManager;
        this.delay = delay;
    }

    @Override
    public void notifyChange(VerilogSimTime time, Object value)
    {
        super.notifyChange(time, value);

        if (delay > 0)
        {
            VerilogSimTime t = new VerilogSimTime(time.getSimTime() + delay);
            dvEventManager.registerSimTimeCallback(t, this);
        }
        else
        {
            dvEventManager.registerSynchCallback(this);
        }
    }
}
