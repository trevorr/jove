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

import com.newisys.verilog.CallbackReason;
import com.newisys.verilog.VerilogCallback;
import com.newisys.verilog.VerilogCallbackHandler;
import com.newisys.verilog.VerilogSimTime;
import com.newisys.verilog.VerilogTime;

/**
 * Base implementation for registered simulation callbacks.
 * 
 * @author Scott Diesing
 */
abstract class BehavioralCallback
    implements VerilogCallback
{
    private final VerilogCallbackHandler handler;
    protected final BehavioralSimulation simulation;
    private final boolean recurring;
    private final CallbackReason reason;

    BehavioralCallback(
        BehavioralSimulation simulation,
        VerilogCallbackHandler handler,
        CallbackReason reason,
        boolean recurring)
    {
        this.handler = handler;
        this.simulation = simulation;
        this.reason = reason;
        this.recurring = recurring;
    }

    public VerilogCallbackHandler getHandler()
    {
        return handler;
    }

    public VerilogTime getTime()
    {
        return new VerilogSimTime(simulation.getSimTime());
    }

    public CallbackReason getReason()
    {
        return reason;
    }

    public boolean isRecurring()
    {
        return recurring;
    }
}
