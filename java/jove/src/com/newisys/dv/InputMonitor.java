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
import com.newisys.verilog.TimeType;
import com.newisys.verilog.ValueType;
import com.newisys.verilog.VerilogCallback;
import com.newisys.verilog.VerilogCallbackData;
import com.newisys.verilog.VerilogCallbackHandler;
import com.newisys.verilog.VerilogReadValue;
import com.newisys.verilog.VerilogSimTime;
import com.newisys.verilog.util.BitVector;

/**
 * Tracks value changes asynchronously on an input signal.
 * 
 * @author Trevor Robinson
 */
final class InputMonitor
    extends BaseMonitor
{
    // some members have default access for efficient access by inner class
    final DVEventManager dvEventManager;
    Object currentValue;
    Object previousValue;
    long lastChangeTime = -1;
    final ValueTracker valueTracker;

    public InputMonitor(
        DVEventManager dvEventManager,
        String signalName,
        VerilogReadValue signalRead,
        ValueType valueType,
        long maxAge)
    {
        super(signalName, signalRead);

        // check arguments
        assert (maxAge >= 0);

        // store attributes from constructor arguments
        this.dvEventManager = dvEventManager;

        // get initial value (used to indicate value change edges)
        currentValue = signalRead.getValue();
        previousValue = currentValue;

        // initialize the value tracker, which keeps a limited history of
        // signal values to implement negative input skew
        valueTracker = new ValueTracker(maxAge);
        valueTracker.trackChange(Long.MIN_VALUE, currentValue);

        // set up handler for value change callbacks
        signalRead.addValueChangeCallback(TimeType.SIM, valueType,
            new ValueChangeCallback());
    }

    public Object getCurrentValue()
    {
        return currentValue;
    }

    public Object getPreviousValue()
    {
        return previousValue;
    }

    public long getLastChangeTime()
    {
        return lastChangeTime;
    }

    public EdgeSet getPreviousEdge(int bit)
    {
        return getTransitionEdge(previousValue, currentValue, bit);
    }

    public boolean isChangedFromPrevious(BitVector mask)
    {
        return isChangedInMask(previousValue, currentValue, mask);
    }

    public long getMaxAge()
    {
        return valueTracker.getMaxAge();
    }

    public void setMaxAge(long skew)
    {
        valueTracker.setMaxAge(skew);
    }

    public Object getAgedValue(long simTime, long age)
    {
        return valueTracker.getValue(simTime, age);
    }

    @Override
    public String toString()
    {
        return "InputMonitor{" + signalName + "}";
    }

    private final class ValueChangeCallback
        implements VerilogCallbackHandler
    {
        public void run(VerilogCallback cb, VerilogCallbackData data)
        {
            VerilogSimTime simTimeObj = (VerilogSimTime) data.getTime();
            long simTime = simTimeObj.getSimTime();
            Object oldValue = currentValue;
            Object newValue = data.getValue();

            if (Debug.enabled)
            {
                Debug.out.println(InputMonitor.this + ".ValueChangeCallback: "
                    + oldValue + " -> " + newValue + " @ " + simTime);
            }

            // update previous value, current value, last change time
            previousValue = oldValue;
            currentValue = newValue;
            lastChangeTime = simTime;

            // update value tracker
            valueTracker.trackChange(simTime, newValue);

            // notify listeners
            notifyListeners(simTimeObj, oldValue, newValue);

            // execute pending threads before returning to simulator
            dvEventManager.executeThreads();
        }
    }
}
