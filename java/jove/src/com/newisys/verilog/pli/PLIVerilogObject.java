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

import com.newisys.verilog.ObjectType;
import com.newisys.verilog.TimeType;
import com.newisys.verilog.ValueType;
import com.newisys.verilog.VerilogCallback;
import com.newisys.verilog.VerilogCallbackHandler;
import com.newisys.verilog.VerilogObject;
import com.newisys.verilog.VerilogRuntimeException;

/**
 * PLI implementation of VerilogObject.
 * 
 * @author Trevor Robinson
 */
public abstract class PLIVerilogObject
    implements VerilogObject
{
    public static final long NULL_HANDLE = 0;

    protected final PLIInterface pliIntf;
    private final PLIObjectType type;
    protected long handle;
    private boolean needFree;

    PLIVerilogObject(PLIInterface pliIntf, PLIObjectType type)
    {
        this.pliIntf = pliIntf;
        this.type = type;
        this.handle = NULL_HANDLE;
        this.needFree = false;
    }

    PLIVerilogObject(
        PLIInterface pliIntf,
        PLIObjectType type,
        long handle,
        boolean needFree)
    {
        this.pliIntf = pliIntf;
        this.type = type;
        this.handle = handle;
        this.needFree = needFree;
    }

    protected final void checkValid()
    {
        if (handle == NULL_HANDLE)
        {
            throw new VerilogRuntimeException(
                "Object handle is no longer valid");
        }
    }

    protected final void invalidate()
    {
        handle = NULL_HANDLE;
        needFree = false;
    }

    protected final void free()
    {
        if (needFree)
        {
            pliIntf.freeObject(handle);
        }
        invalidate();
    }

    @Override
    protected void finalize()
        throws Throwable
    {
        free();
    }

    public final long getHandle()
    {
        return handle;
    }

    public static long getObjectHandle(PLIVerilogObject obj)
    {
        return (obj != null) ? obj.getHandle() : NULL_HANDLE;
    }

    final void setHandle(long handle, boolean needFree)
    {
        if (this.handle != NULL_HANDLE)
        {
            throw new VerilogRuntimeException("Object already has a handle");
        }

        this.handle = handle;
        this.needFree = needFree;
    }

    public final ObjectType getType()
    {
        return type.getVerilogEnum();
    }

    protected final PLIVerilogObject getRelatedObject(int type)
    {
        checkValid();
        return pliIntf.getObject(type, handle);
    }

    protected final PLIVerilogObject getChildObject(int index)
    {
        checkValid();
        return pliIntf.getObjectByIndex(handle, index);
    }

    protected final VerilogCallback addValueChangeCallbackImpl(
        VerilogCallbackHandler handler)
    {
        try
        {
            return pliIntf.registerCallback(PLICallbackReason.VALUE_CHANGE,
                PLITime.SIM_TIME0, this, PLIValueType.OBJ_TYPE, handler);
        }
        catch (VerilogRuntimeException e)
        {
            throw new VerilogRuntimeException(
                "Unable to register callback for value change of " + this, e);
        }
    }

    protected final VerilogCallback addValueChangeCallbackImpl(
        TimeType timeType,
        ValueType valueType,
        VerilogCallbackHandler handler)
    {
        try
        {
            return pliIntf.registerCallback(PLICallbackReason.VALUE_CHANGE,
                PLITime.getPLITime(PLITimeType.getTimeType(timeType)), this,
                PLIValueType.getValueType(valueType), handler);
        }
        catch (VerilogRuntimeException e)
        {
            throw new VerilogRuntimeException(
                "Unable to register callback for value change of " + this, e);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof PLIVerilogObject)
        {
            PLIVerilogObject other = (PLIVerilogObject) obj;
            checkValid();
            other.checkValid();
            return pliIntf.compareObjects(handle, other.handle);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        // TODO: implement better hash code
        return type.getValue();
    }

    @Override
    public String toString()
    {
        return getClass().getName() + "@" + Long.toHexString(handle);
    }
}
