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

package com.newisys.verilog.pli;

import com.newisys.verilog.VerilogCallbackHandler;
import com.newisys.verilog.VerilogObject;
import com.newisys.verilog.VerilogRuntimeException;
import com.newisys.verilog.VerilogScaledRealTime;
import com.newisys.verilog.VerilogSimTime;
import com.newisys.verilog.VerilogTime;

/**
 * Implementation of the low-level PLI native interface.
 * 
 * @author Trevor Robinson
 */
public final class PLI
    implements PLIInterface
{
    /**
     * PLI interface to provide to newly created objects, in case a thread
     * marshalling proxy is in use.
     */
    private PLIInterface pliProxy;

    long cachedSimTime = PLITime.INVALID_SIM_TIME;
    double cachedScaledRealTime = PLITime.INVALID_SCALED_REAL_TIME;

    public PLI()
    {
        pliProxy = this;
    }

    public PLIInterface getProxyInterface()
    {
        return pliProxy;
    }

    public void setProxyInterface(PLIInterface pliProxy)
    {
        this.pliProxy = pliProxy;
    }

    // simulation information

    public native PLIVerilogInfo getInfo();

    // callbacks

    private native long registerCallback0(
        PLIVerilogCallback callback,
        int reason,
        int timeType,
        long longTime,
        double doubleTime,
        long handle,
        int valueType);

    public PLIVerilogCallback registerCallback(
        PLICallbackReason reason,
        VerilogCallbackHandler handler)
    {
        return registerCallback(reason, PLITime.SIM_TIME0, null,
            PLIValueType.SUPPRESS, handler);
    }

    public PLIVerilogCallback registerCallback(
        PLICallbackReason reason,
        PLITime time,
        VerilogCallbackHandler handler)
    {
        return registerCallback(reason, time, null, PLIValueType.SUPPRESS,
            handler);
    }

    public PLIVerilogCallback registerCallback(
        PLICallbackReason reason,
        PLITime time,
        PLIVerilogObject obj,
        PLIValueType valueType,
        VerilogCallbackHandler handler)
    {
        assert (time != null);

        PLIVerilogCallback callback = new PLIVerilogCallback(pliProxy, reason,
            reason.isRecurring(), handler, time, obj, valueType);

        long handle = registerCallback0(callback, reason.getValue(), time
            .getTimeType().getValue(), time.getSimTime(), time
            .getScaledRealTime(), PLIVerilogObject.getObjectHandle(obj),
            valueType.getValue());
        if (handle != PLIVerilogObject.NULL_HANDLE)
        {
            callback.setHandle(handle, true);
            return callback;
        }
        else
        {
            throw getErrorRuntimeException();
        }
    }

    // called from native code to execute the Java callback handler
    void dispatchCallback(
        PLIVerilogCallback callback,
        int timeType,
        long longTime,
        double doubleTime,
        Object value,
        int index)
    {
        callback.markOccurred();

        try
        {
            final VerilogTime time;
            if (timeType == PLITimeTypeConstants.vpiSim)
            {
                time = new VerilogSimTime(longTime);
                cachedSimTime = longTime;
                cachedScaledRealTime = PLITime.INVALID_SCALED_REAL_TIME;
            }
            else if (timeType == PLITimeTypeConstants.vpiScaledReal)
            {
                time = new VerilogScaledRealTime(doubleTime);
                cachedSimTime = PLITime.INVALID_SIM_TIME;
                cachedScaledRealTime = doubleTime;
            }
            else
            {
                time = null;
                cachedSimTime = PLITime.INVALID_SIM_TIME;
                cachedScaledRealTime = PLITime.INVALID_SCALED_REAL_TIME;
            }

            PLICallbackData data = new PLICallbackData(time, callback
                .getObject(), value, index);

            VerilogCallbackHandler handler = callback.getHandler();
            handler.run(callback, data);
        }
        finally
        {
            if (!callback.isRecurring() && !callback.isReleased())
            {
                callback.release();
            }
        }
    }

    private native boolean cancelCallback0(long handle);

    public void cancelCallback(long handle)
    {
        if (!cancelCallback0(handle))
        {
            throw getErrorRuntimeException();
        }
    }

    public native void releaseCallback(long handle);

    // handles

    private native long getHandle0(int type, long refHandle);

    public PLIVerilogObject getObject(int type, long refHandle)
    {
        long handle = getHandle0(type, refHandle);
        if (handle != PLIVerilogObject.NULL_HANDLE)
        {
            return createObject(handle);
        }
        else
        {
            throw getErrorRuntimeException();
        }
    }

    private native long getHandleByName0(String name, long scopeHandle);

    public PLIVerilogObject getObjectByName(String name, long scopeHandle)
    {
        long handle = getHandleByName0(name, scopeHandle);
        if (handle != PLIVerilogObject.NULL_HANDLE)
        {
            return createObject(handle);
        }
        else
        {
            throw getErrorRuntimeException();
        }
    }

    private native long getHandleMulti0(int type, long handle1, long handle2);

    public PLIVerilogObject getObjectMulti(int type, long handle1, long handle2)
    {
        long handle = getHandleMulti0(type, handle1, handle2);
        if (handle != PLIVerilogObject.NULL_HANDLE)
        {
            return createObject(handle);
        }
        else
        {
            throw getErrorRuntimeException();
        }
    }

    private native long getHandleByIndex0(long parentHandle, int index);

    public PLIVerilogObject getObjectByIndex(long parentHandle, int index)
    {
        long handle = getHandleByIndex0(parentHandle, index);
        if (handle != PLIVerilogObject.NULL_HANDLE)
        {
            return createObject(handle);
        }
        else
        {
            throw getErrorRuntimeException();
        }
    }

    private native long getHandleByMultiIndex0(long parentHandle, int[] indices);

    public PLIVerilogObject getObjectByMultiIndex(
        long parentHandle,
        int[] indices)
    {
        long handle = getHandleByMultiIndex0(parentHandle, indices);
        if (handle != PLIVerilogObject.NULL_HANDLE)
        {
            return createObject(handle);
        }
        else
        {
            throw getErrorRuntimeException();
        }
    }

    private native long iterate0(int type, long refHandle);

    public <T extends VerilogObject> PLIIterator<T> iterate(
        int type,
        long refHandle)
    {
        return new PLIIterator<T>(pliProxy, iterate0(type, refHandle));
    }

    private native long scan0(long iterHandle);

    public PLIVerilogObject scan(long iterHandle)
    {
        long handle = scan0(iterHandle);
        if (handle != PLIVerilogObject.NULL_HANDLE)
        {
            return createObject(handle);
        }
        else
        {
            return null;
        }
    }

    public native boolean compareObjects(long handle1, long handle2);

    public native void freeObject(long handle);

    // called from both Java and native code
    private PLIVerilogObject createObject(long handle)
    {
        int type = getPropInt(PLIPropertyTypeConstants.vpiType, handle);
        switch (type)
        {
        case PLIObjectTypeConstants.vpiConstant:
            return new PLIVerilogConstant(pliProxy, handle);
        case PLIObjectTypeConstants.vpiModule:
            return new PLIVerilogModule(pliProxy, handle);
        case PLIObjectTypeConstants.vpiNet:
            return new PLIVerilogNet(pliProxy, handle);
        case PLIObjectTypeConstants.vpiNetBit:
            return new PLIVerilogNetBit(pliProxy, handle);
        case PLIObjectTypeConstants.vpiPort:
            return new PLIVerilogPort(pliProxy, handle);
        case PLIObjectTypeConstants.vpiPortBit:
            return new PLIVerilogPortBit(pliProxy, handle);
        case PLIObjectTypeConstants.vpiReg:
            return new PLIVerilogReg(pliProxy, handle);
        case PLIObjectTypeConstants.vpiRegBit:
            return new PLIVerilogRegBit(pliProxy, handle);
        case PLIObjectTypeConstants.vpiSchedEvent:
            return new PLIVerilogSchedEvent(pliProxy, handle);
        case PLIObjectTypeConstants.vpiVarSelect:
            return new PLIVerilogVarArraySelect(pliProxy, handle);
        case PLIObjectTypeConstants.vpiIntegerVar:
        case PLIObjectTypeConstants.vpiRealVar:
        case PLIObjectTypeConstants.vpiTimeVar:
            return new PLIVerilogVariable(pliProxy, PLIObjectType
                .getObjectType(type), handle);
        default:
            throw new VerilogRuntimeException("Unknown object type: " + type);
        }
    }

    // properties

    public native int getPropInt(int prop, long handle);

    public native String getPropStr(int prop, long handle);

    public boolean getPropBool(int prop, long handle)
    {
        return getPropInt(prop, handle) != 0;
    }

    // values

    private native Object getValue0(long handle, int format);

    public Object getValue(long handle, int format)
    {
        Object value = getValue0(handle, format);
        if (value == null)
        {
            throw getErrorRuntimeException();
        }
        return value;
    }

    private native long putValue0(
        long handle,
        Object value,
        int timeType,
        long longTime,
        double doubleTime,
        int flags);

    public void putValue(long handle, Object value)
    {
        final long eventHandle = putValue0(handle, value,
            PLITimeTypeConstants.vpiSuppress, 0, 0,
            PLIDriveDelayModeConstants.vpiNoDelay);

        if (eventHandle == ~0L)
        {
            checkErrorRuntimeException();
        }
    }

    private PLIVerilogSchedEvent putValueDelayInternal(
        long handle,
        Object value,
        VerilogTime time,
        PLIDriveDelayMode mode,
        boolean returnEvent)
    {
        PLITime pliTime = PLITime.getPLITime(time);
        int flags = mode.getValue();

        // request a return event if the user requested it and mode != NO_DELAY
        returnEvent &= mode != PLIDriveDelayMode.NO_DELAY;
        if (returnEvent)
        {
            final int vpiReturnEvent = 0x1000;
            flags |= vpiReturnEvent;
        }
        long eventHandle = putValue0(handle, value, pliTime.getTimeType()
            .getValue(), pliTime.getSimTime(), pliTime.getScaledRealTime(),
            flags);

        // If we request an event handle, a value of NULL denotes an error
        // condition. However, if we do not request an event handle, a value
        // of ~0L denotes an error condition.
        if (returnEvent)
        {
            if (handle == PLIVerilogObject.NULL_HANDLE)
            {
                checkErrorRuntimeException();
                return null;
            }

            return new PLIVerilogSchedEvent(pliProxy, eventHandle);
        }
        else
        {
            if (eventHandle == ~0L)
            {
                checkErrorRuntimeException();
            }

            return null;
        }
    }

    public void putValueDelay(
        long handle,
        Object value,
        VerilogTime time,
        PLIDriveDelayMode mode)
    {
        PLIVerilogSchedEvent event = putValueDelayInternal(handle, value, time,
            mode, false);
        assert (event == null);
    }

    public PLIVerilogSchedEvent putValueDelayNotify(
        long handle,
        Object value,
        VerilogTime time,
        PLIDriveDelayMode mode)
    {
        return putValueDelayInternal(handle, value, time, mode, true);
    }

    public void cancelEvent(long handle)
    {
        final int vpiCancelEvent = 7;
        final long eventHandle = putValue0(handle, null,
            PLITimeTypeConstants.vpiSuppress, 0, 0, vpiCancelEvent);

        if (eventHandle == ~0L)
        {
            checkErrorRuntimeException();
        }
    }

    public void forceValue(long handle, Object value)
    {
        final int vpiForceFlag = 5;
        final long eventHandle = putValue0(handle, value,
            PLITimeTypeConstants.vpiSuppress, 0, 0, vpiForceFlag);

        if (eventHandle == ~0L)
        {
            checkErrorRuntimeException();
        }
    }

    private native Object releaseForce0(long handle);

    public Object releaseForce(long handle)
    {
        Object value = releaseForce0(handle);
        if (value == null)
        {
            throw getErrorRuntimeException();
        }
        return value;
    }

    // time

    public native PLITime getTime(int timeType, long handle);

    public PLITime getTime(int timeType)
    {
        PLITime pliTime = getTime(timeType, PLIVerilogObject.NULL_HANDLE);
        if (timeType == PLITimeTypeConstants.vpiSim)
        {
            cachedSimTime = pliTime.getSimTime();
        }
        else if (timeType == PLITimeTypeConstants.vpiScaledReal)
        {
            cachedScaledRealTime = pliTime.getScaledRealTime();
        }
        return pliTime;
    }

    // logging

    private native boolean print0(byte[] msg, int off, int len);

    public void print(byte[] msg, int off, int len)
    {
        if (!print0(msg, off, len))
        {
            throw getErrorRuntimeException();
        }
    }

    private native boolean flush0();

    public void flush()
    {
        // the return value of vpi_flush doesn't seem to be reliable
        flush0();
    }

    // simulation control

    public native void stop();

    public native void finish();

    // errors

    // error states
    private final static int vpiCompile = 1;
    //private final static int vpiPLI = 2;
    private final static int vpiRun = 3;

    private final static String[] errorStates = { "Compile", "PLI", "Run" };

    // error levels
    private final static int vpiNotice = 1;
    //private final static int vpiWarning = 2;
    //private final static int vpiError = 3;
    //private final static int vpiSystem = 4;
    private final static int vpiInternal = 5;

    private final static String[] errorLevels = { "Notice", "Warning", "Error",
        "System", "Internal" };

    private final static class ErrorInfo
    {
        final int state;
        final int level;
        final String message;
        final String product;
        final String code;
        final String file;
        final int line;

        ErrorInfo(
            int state,
            int level,
            String message,
            String product,
            String code,
            String file,
            int line)
        {
            this.state = state;
            this.level = level;
            this.message = message;
            this.product = product;
            this.code = code;
            this.file = file;
            this.line = line;
        }
    }

    private native ErrorInfo getError();

    private String getExceptionMessage(ErrorInfo info)
    {
        String msg;
        if (info != null)
        {
            StringBuffer buf = new StringBuffer();

            if (info.state >= vpiCompile && info.state <= vpiRun)
            {
                buf.append(errorStates[info.state - 1]);
                buf.append(": ");
            }

            if (info.level >= vpiNotice && info.level <= vpiInternal)
            {
                buf.append(errorLevels[info.level - 1]);
                buf.append(": ");
            }

            if (info.message != null)
            {
                buf.append(info.message);
            }
            else
            {
                buf.append("<no message>");
            }

            if (info.file != null)
            {
                buf.append(" at ");
                buf.append(info.file);
                buf.append(", line ");
                buf.append(info.line);
            }

            msg = buf.toString();
        }
        else
        {
            msg = "Unknown failure";
        }
        return msg;
    }

    private VerilogRuntimeException getErrorRuntimeException()
    {
        return new VerilogRuntimeException(getExceptionMessage(getError()));
    }

    private void checkErrorRuntimeException()
    {
        ErrorInfo info = getError();
        if (info != null)
        {
            throw new VerilogRuntimeException(getExceptionMessage(info));
        }
    }
}
