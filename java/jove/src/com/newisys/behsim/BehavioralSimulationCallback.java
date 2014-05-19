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
import com.newisys.verilog.ObjectType;
import com.newisys.verilog.ValueType;
import com.newisys.verilog.VerilogCallbackHandler;
import com.newisys.verilog.VerilogObject;

/**
 * Represents a callback requested on the simulation state.
 * 
 * @author Scott Diesing
 */
public class BehavioralSimulationCallback
    extends BehavioralCallback
{
    public BehavioralSimulationCallback(
        BehavioralSimulation simulation,
        VerilogCallbackHandler handler,
        CallbackReason reason)
    {
        super(simulation, handler, reason, false);
    }

    public void cancel()
    {
        simulation.cancelCallback(this);
    }

    public void remove()
    {
        simulation.removeCallback(this);
    }

    public VerilogObject getObject()
    {
        return null;
    }

    public ValueType getValueType()
    {
        return null;
    }

    public ObjectType getType()
    {
        return null;
    }
}
