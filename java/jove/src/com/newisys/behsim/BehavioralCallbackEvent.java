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

package com.newisys.behsim;

import com.newisys.eventsched.SimulationEvent;
import com.newisys.verilog.VerilogCallback;
import com.newisys.verilog.VerilogCallbackData;
import com.newisys.verilog.VerilogCallbackHandler;

/**
 * Provides a way for the event scheduler to call the run method on an callback
 * handler.
 * 
 * @author Scott Diesing
 */
public class BehavioralCallbackEvent
    implements SimulationEvent
{
    private final BehavioralSimulationCallback callback;
    private final BehavioralCallbackData data;

    BehavioralCallbackEvent(
        BehavioralSimulationCallback cb,
        BehavioralCallbackData data)
    {
        this.callback = cb;
        this.data = data;
    }

    public void execute()
    {
        callback.remove();
        VerilogCallbackHandler handler = callback.getHandler();
        handler.run(callback, data);
    }

    public VerilogCallback getCallback()
    {
        return callback;
    }

    public VerilogCallbackData getCallbackData()
    {
        return data;
    }
}
