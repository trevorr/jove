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

import com.newisys.verilog.CallbackReason;
import com.newisys.verilog.ValueType;
import com.newisys.verilog.VerilogCallback;
import com.newisys.verilog.VerilogCallbackHandler;
import com.newisys.verilog.VerilogObject;
import com.newisys.verilog.VerilogRuntimeException;
import com.newisys.verilog.VerilogTime;

/**
 * PLI implementation of VerilogCallback.
 * 
 * @author Trevor Robinson
 */
public final class PLIVerilogCallback
    extends PLIVerilogObject
    implements VerilogCallback
{
    private final PLICallbackReason reason;
    private final boolean recurring;
    private final VerilogCallbackHandler handler;
    private final PLITime time;
    private final PLIVerilogObject obj;
    private final PLIValueType valueType;

    private boolean occurred;
    private boolean released;

    public PLIVerilogCallback(
        PLIInterface pliIntf,
        PLICallbackReason reason,
        boolean recurring,
        VerilogCallbackHandler handler,
        PLITime time,
        PLIVerilogObject obj,
        PLIValueType valueType)
    {
        super(pliIntf, PLIObjectType.CALLBACK);

        this.reason = reason;
        this.recurring = recurring;
        this.handler = handler;
        this.time = time;
        this.obj = obj;
        this.valueType = valueType;

        occurred = false;
        released = false;
    }

    public CallbackReason getReason()
    {
        return reason.getVerilogEnum();
    }

    public boolean isRecurring()
    {
        return recurring;
    }

    public VerilogCallbackHandler getHandler()
    {
        return handler;
    }

    public VerilogTime getTime()
    {
        return time.getVerilogTime();
    }

    public VerilogObject getObject()
    {
        return obj;
    }

    public ValueType getValueType()
    {
        return valueType.getVerilogEnum();
    }

    public synchronized void cancel()
    {
        checkValid();
        checkNotReleased();
        if (recurring || !occurred)
        {
            pliIntf.cancelCallback(handle);
        }
        else
        {
            pliIntf.releaseCallback(handle);
        }
        invalidate();
        released = true;
    }

    void markOccurred()
    {
        occurred = true;
    }

    boolean isReleased()
    {
        return released;
    }

    synchronized void release()
    {
        checkValid();
        checkNotReleased();
        pliIntf.releaseCallback(handle);
        invalidate();
        released = true;
    }

    private void checkNotReleased()
    {
        if (released)
        {
            throw new VerilogRuntimeException(
                "Callback has already been released");
        }
    }
}
