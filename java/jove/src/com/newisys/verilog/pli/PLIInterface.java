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
import com.newisys.verilog.VerilogTime;

/**
 * Low-level interface to native PLI functionality. This interface is used to
 * communicate with the real native implementation through a thread marshalling
 * proxy, since most PLI implementations do not support multithreaded access.
 * 
 * @author Trevor Robinson
 */
public interface PLIInterface
{
    PLIVerilogInfo getInfo();

    PLIVerilogCallback registerCallback(
        PLICallbackReason reason,
        VerilogCallbackHandler handler);

    PLIVerilogCallback registerCallback(
        PLICallbackReason reason,
        PLITime time,
        VerilogCallbackHandler handler);

    PLIVerilogCallback registerCallback(
        PLICallbackReason reason,
        PLITime time,
        PLIVerilogObject obj,
        PLIValueType valueType,
        VerilogCallbackHandler handler);

    void cancelCallback(long handle);

    void releaseCallback(long handle);

    PLIVerilogObject getObject(int type, long refHandle);

    PLIVerilogObject getObjectByName(String name, long scopeHandle);

    PLIVerilogObject getObjectMulti(int type, long handle1, long handle2);

    PLIVerilogObject getObjectByIndex(long parentHandle, int index);

    PLIVerilogObject getObjectByMultiIndex(long parentHandle, int[] indices);

    <T extends VerilogObject> PLIIterator<T> iterate(int type, long refHandle);

    PLIVerilogObject scan(long iterHandle);

    boolean compareObjects(long handle1, long handle2);

    void freeObject(long handle);

    int getPropInt(int prop, long handle);

    String getPropStr(int prop, long handle);

    boolean getPropBool(int prop, long handle);

    Object getValue(long handle, int format);

    void putValue(long handle, Object value);

    void putValueDelay(
        long handle,
        Object value,
        VerilogTime time,
        PLIDriveDelayMode mode);

    PLIVerilogSchedEvent putValueDelayNotify(
        long handle,
        Object value,
        VerilogTime time,
        PLIDriveDelayMode mode);

    void cancelEvent(long handle);

    void forceValue(long handle, Object value);

    Object releaseForce(long handle);

    PLITime getTime(int timeType, long handle);

    PLITime getTime(int timeType);

    void print(byte[] msg, int off, int len);

    void flush();

    void stop();

    void finish();
}
