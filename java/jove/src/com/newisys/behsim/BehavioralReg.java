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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.newisys.verilog.*;
import com.newisys.verilog.util.Bit;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.BitVectorBuffer;

/**
 * This supports a subset of the VerilogReg contract. Unsupported methods throw
 * UnsupportedOperationException.
 * 
 * @author Scott Diesing
 */
public class BehavioralReg
    implements VerilogReg
{
    /**
     * Used internally when value change occurs. It allows the event scheduler
     * to control when within the time step the callbacks occur.
     */
    private class ValueChangeHandler
        implements VerilogCallbackHandler
    {
        private final BehavioralReg reg;
        private final BehavioralRegCallback callback;

        public ValueChangeHandler(
            BehavioralReg reg,
            BehavioralRegCallback callback)
        {
            this.reg = reg;
            this.callback = callback;
        }

        public void run(VerilogCallback cb, VerilogCallbackData data)
        {
            callback.getHandler().run(
                callback,
                new BehavioralRegCallbackData(simulation, reg, callback
                    .getValueType(), callback.getTimeType()));
        }

    }

    /**
     * Used internally when a put value delay is called. It allows the event
     * scheduler to schedule the value change in the future.
     */
    private class PutValueCallbackHandler
        implements VerilogCallbackHandler
    {
        private final VerilogReg verilogReg;
        private final Object value;

        public PutValueCallbackHandler(VerilogReg verilogReg, Object value)
        {
            this.verilogReg = verilogReg;
            this.value = value;
        }

        public void run(VerilogCallback cb, VerilogCallbackData data)
        {
            verilogReg.putValue(value);
        }
    }

    private final String name;
    private BitVectorBuffer buffer;
    private final int size;
    private final List<BehavioralRegCallback> valueChangeCallbackList = Collections
        .synchronizedList(new LinkedList<BehavioralRegCallback>());
    final BehavioralSimulation simulation;

    /**
     * Construct a behavioral register.
     * @param simulation the simulation containing this register
     * @param name the name of this register
     * @param size the width in bits of this register
     */
    BehavioralReg(BehavioralSimulation simulation, String name, int size)
    {
        this.simulation = simulation;
        this.name = name;
        this.size = size;
        assert (size > 0);
        // The initialization value for reg, time, and integer data types shall
        // be the unknown value, x. (IEEE Verilog Standard)
        buffer = new BitVectorBuffer(size);
    }

    public String getName()
    {
        return name;
    }

    /**
     * Returns a Bit if size equals 1. Otherwise it always returns a BitVector.
     * @return a Bit or BitVector representing the current register value
     */
    public Object getValue()
    {
        Object returnValue;
        if (size == 1)
        {
            returnValue = getValue(ValueType.SCALAR);
        }
        else
        {
            returnValue = getValue(ValueType.VECTOR);
        }
        return returnValue;
    }

    /**
     * Supports SCALAR, VECTOR, and INT
     *
     * SCALAR returns bit zero even if size is greater than zero.
     * @param type the desired result type
     * @return an Object representing the current register value
     */
    public synchronized Object getValue(ValueType type)
    {
        if (type == ValueType.OBJ_TYPE)
        {
            if (size == 1)
            {
                type = ValueType.SCALAR;
            }
            else
            {
                type = ValueType.VECTOR;
            }
        }
        if (type == ValueType.SCALAR)
        {
            return (buffer.getBit(0));
        }
        else if (type == ValueType.VECTOR)
        {
            return buffer.toBitVector();
        }
        else if (type == ValueType.INT)
        {
            return new Integer(buffer.intValue());
        }
        else if (type == ValueType.SUPPRESS)
        {
            return null;
        }
        else
        {
            throw new IllegalArgumentException(type
                + " not a supported type for BehavioralReg.getValue()");
        }
    }

    /**
     * Supports Bit, BitVector, BitVectorBuffer, Integer, Long
     * @param value the new register value
     */
    public synchronized void putValue(Object value)
    {
        BitVectorBuffer localBuffer = objectToBitVectorBuffer(value);

        if (buffer.equalsExact(localBuffer.toBitVector()) == false)
        {
            buffer = localBuffer;
            addValueChangeCallbacksToEventScheduler();
        }
    }

    /**
     * Converts the value to a BitVectorBuffer.
     * @param value the value to convert
     * @return a new BitVectorBuffer containing the value
     */
    private BitVectorBuffer objectToBitVectorBuffer(Object value)
    {
        BitVectorBuffer localBuffer;
        if (value instanceof Bit)
        {
            Bit newValue = (Bit) value;
            assert (size == 1);
            localBuffer = new BitVectorBuffer(1);
            localBuffer.setBit(0, newValue);
        }
        else if (value instanceof BitVector)
        {
            BitVector newValue = (BitVector) value;
            assert (size == newValue.length());
            localBuffer = new BitVectorBuffer(newValue);
        }
        else if (value instanceof BitVectorBuffer)
        {
            BitVectorBuffer newValue = (BitVectorBuffer) value;
            assert (size == newValue.length());
            localBuffer = new BitVectorBuffer(newValue);
        }
        else if (value instanceof Integer)
        {
            Integer newValue = (Integer) value;
            assert (size <= 32);
            localBuffer = new BitVectorBuffer(size, newValue.intValue());
        }
        else if (value instanceof Long)
        {
            Long newValue = (Long) value;
            assert (size <= 64);
            localBuffer = new BitVectorBuffer(size, newValue.longValue());
        }
        else
        {
            throw new IllegalArgumentException(value.getClass()
                + " not a supported type for BehavioralReg.putValue()");
        }
        return localBuffer;
    }

    public String getFullName()
    {
        return name;
    }

    public ObjectType getType()
    {
        return ObjectType.REG;
    }

    public boolean isScalar()
    {
        return (size == 1);
    }

    public boolean isVector()
    {
        return (size > 1);
    }

    public int getSize()
    {
        return size;
    }

    /**
     * Add a callback handler to the callback list. Each callback handler in
     * the list will have its run method invoked when this reg changes value.
     * The callback handler, along with a reference to this register is wrapped
     * in a callback object that is returned to the caller.
     * @param handler the value change callback handler
     * @return a callback object
     */
    public VerilogCallback addValueChangeCallback(VerilogCallbackHandler handler)
    {
        // Create a callback object.
        BehavioralRegCallback valueChangeCallback = new BehavioralRegCallback(
            simulation, handler, this, TimeType.SIM, ValueType.OBJ_TYPE);

        // Add the callback object to the internal list.
        valueChangeCallbackList.add(valueChangeCallback);

        // Return the callback object to the caller.
        return valueChangeCallback;
    }

    public VerilogCallback addValueChangeCallback(
        TimeType timeType,
        ValueType valueType,
        VerilogCallbackHandler handler)
    {
        BehavioralRegCallback valueChangeCallback = new BehavioralRegCallback(
            simulation, handler, this, timeType, valueType);

        valueChangeCallbackList.add(valueChangeCallback);

        return valueChangeCallback;

    }

    //    * vpiInertialDelay All scheduled events on the object shall be removed
    //      before this event is scheduled.
    //
    //  * vpiTransportDelay All events on the object scheduled for times later
    //    than this event shall be removed (modified transport delay).
    //
    //  * vpiPureTransportDelay No events on the object shall be removed
    //    (transport delay).
    //
    //  * vpiNoDelay The object shall be set to the passed value with no delay.
    //    Argument time_p shall be ignored and can be set to NULL.

    /**
     * Causes the value of this register to change at some time in the future.
     * It only supports no delay and pure transport delay.
     *
     * @param value the new register value
     * @param delay the delay time
     * @param mode the delay mode
     */
    public void putValueDelay(
        Object value,
        VerilogTime delay,
        DriveDelayMode mode)
    {
        // identical to putValueDelayNotify. just ignore the return value
        putValueDelayNotify(value, delay, mode);
    }

    /**
     * Causes the value of this register to change at some time in the future.
     * It only supports no delay and pure transport delay.
     *
     * @param value the new register value
     * @param delay the delay time
     * @param mode the delay mode
     * @return an event object used to monitor the delayed assignment
     */
    public VerilogSchedEvent putValueDelayNotify(
        Object value,
        VerilogTime delay,
        DriveDelayMode mode)
    {
        VerilogSchedEvent schedEvent = null;
        if (mode == DriveDelayMode.NO_DELAY)
        {
            putValue(value);
            schedEvent = new BehavioralSchedEvent(this, null, false);
        }
        else if (mode == DriveDelayMode.PURE_TRANSPORT_DELAY)
        {

            VerilogCallbackHandler handler = new PutValueCallbackHandler(this,
                value);
            VerilogCallback callback = simulation.addDelayCallback(delay,
                handler);
            schedEvent = new BehavioralSchedEvent(this, callback, true);
        }
        else
        {
            // Behavioral will not support transport delay or inertial delay.
            throw new UnsupportedOperationException();
        }

        return schedEvent;
    }

    /**
     * Removes this particular callback from the list of callbacks registered
     * on this object.
     * @param callBackToCancel the callback to cancel
     */
    void cancelCallback(BehavioralRegCallback callBackToCancel)
    {
        valueChangeCallbackList.remove(callBackToCancel);
    }

    /**
     * Copy all the value change callbacks to the eventscheduler via the
     * simulation to cause the actual callback to occur on this time step. This
     * allows the event scheduler to control the exact point within the time
     * step to make the callbacks.
     */
    private void addValueChangeCallbacksToEventScheduler()
    {
        for (final BehavioralRegCallback valueChangeCallback : valueChangeCallbackList)
        {
            ValueChangeHandler surrogateHandler = new ValueChangeHandler(this,
                valueChangeCallback);

            simulation.addReadWriteSynchCallback(surrogateHandler);
        }
    }

    /*
     * The following portion of the class is not implemented.
     */

    public VerilogModule getModule()
    {
        throw new UnsupportedOperationException();
    }

    public VerilogRegBit getBit(int index)
    {
        throw new UnsupportedOperationException();
    }

    public Iterator<VerilogRegBit> getBits()
    {
        throw new UnsupportedOperationException();
    }

    public VerilogScope getScope()
    {
        throw new UnsupportedOperationException();
    }

    public VerilogExpr getLeftRange()
    {
        throw new UnsupportedOperationException();
    }

    public VerilogExpr getRightRange()
    {
        throw new UnsupportedOperationException();
    }

    public Iterator<VerilogPort> getPorts()
    {
        throw new UnsupportedOperationException();
    }

    public Iterator<VerilogPort> getPortInsts()
    {
        throw new UnsupportedOperationException();
    }

    public Iterator<VerilogContAssign> getContAssigns()
    {
        throw new UnsupportedOperationException();
    }

    public Iterator<VerilogPrimTerm> getPrimTerms()
    {
        throw new UnsupportedOperationException();
    }

    public Iterator<VerilogTchkTerm> getTchkTerms()
    {
        throw new UnsupportedOperationException();
    }

    public Iterator<VerilogRegDriver> getDrivers()
    {
        throw new UnsupportedOperationException();
    }

    public Iterator<VerilogRegLoad> getLoads()
    {
        throw new UnsupportedOperationException();
    }

    public Iterator<VerilogDeclObject> getUses()
    {
        throw new UnsupportedOperationException();
    }

    public void forceValue(Object value)
    {
        throw new UnsupportedOperationException();
    }

    public Object releaseForce()
    {
        throw new UnsupportedOperationException();
    }

    public String getFile()
    {
        throw new UnsupportedOperationException();
    }

    public int getLineNo()
    {
        throw new UnsupportedOperationException();
    }
}
