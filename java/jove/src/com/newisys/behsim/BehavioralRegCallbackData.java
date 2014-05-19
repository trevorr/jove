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

import com.newisys.verilog.TimeType;
import com.newisys.verilog.ValueType;
import com.newisys.verilog.VerilogObject;
import com.newisys.verilog.VerilogScaledRealTime;
import com.newisys.verilog.VerilogSimTime;
import com.newisys.verilog.VerilogTime;

/**
 * Represents the state of a register during a callback.
 * 
 * @author Scott Diesing
 */
public class BehavioralRegCallbackData
    extends BehavioralCallbackData
{
    private final BehavioralReg behavioralReg;
    private final ValueType valueType;
    private final TimeType timeType;

    BehavioralRegCallbackData(
        BehavioralSimulation simulation,
        BehavioralReg data,
        ValueType valueType,
        TimeType timeType)
    {
        super(simulation);
        this.behavioralReg = data;
        this.valueType = valueType;
        this.timeType = timeType;
    }

    @Override
    public VerilogObject getObject()
    {
        return behavioralReg;
    }

    /**
     * Return the correct object based on the ValueType.
     * @return an Object representing the current register value
     */
    @Override
    public Object getValue()
    {
        // Let BehavioralReg translate itself into the correct type of object.
        // If it does not know how to handle a ValueType it will throw an
        // exception.
        return behavioralReg.getValue(valueType);
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
