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
import com.newisys.verilog.TimeType;
import com.newisys.verilog.ValueType;
import com.newisys.verilog.VerilogCallbackHandler;
import com.newisys.verilog.VerilogObject;
import com.newisys.verilog.VerilogScaledRealTime;
import com.newisys.verilog.VerilogSimTime;
import com.newisys.verilog.VerilogTime;

/**
 * Represents a callback requested on a register.
 * 
 * @author Scott Diesing
 */
public class BehavioralRegCallback
    extends BehavioralCallback
{
    private final BehavioralReg behavioralReg;
    private final TimeType timeType;
    private final ValueType valueType;

    BehavioralRegCallback(
        BehavioralSimulation simulation,
        VerilogCallbackHandler handler,
        BehavioralReg behavioralReg,
        TimeType timeType,
        ValueType valueType)
    {
        super(simulation, handler, CallbackReason.VALUE_CHANGE, true);
        this.behavioralReg = behavioralReg;
        this.timeType = timeType;
        this.valueType = valueType;
    }

    public TimeType getTimeType()
    {
        return timeType;
    }

    public VerilogObject getObject()
    {
        return behavioralReg;
    }

    public void cancel()
    {
        behavioralReg.cancelCallback(this);
    }

    public ValueType getValueType()
    {
        return valueType;
    }

    public ObjectType getType()
    {
        return ObjectType.REG;
    }

    @Override
    public VerilogTime getTime()
    {
        if (timeType == TimeType.SIM)
        {
            return new VerilogSimTime(simulation.getSimTime());
        }
        else if (timeType == TimeType.SCALED_REAL)
        {
            return new VerilogScaledRealTime(simulation.getScaledRealTime());
        }
        else
        {
            return null;
        }
    }
}
