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

import com.newisys.verilog.VerilogCallbackData;
import com.newisys.verilog.VerilogObject;
import com.newisys.verilog.VerilogTime;

/**
 * An implementation of VerilogCallbackData for PLI callbacks.
 * 
 * @author Trevor Robinson
 */
public final class PLICallbackData
    implements VerilogCallbackData
{
    private final VerilogTime time;
    private final VerilogObject obj;
    private final Object value;
    private final int index;

    public PLICallbackData(
        VerilogTime time,
        VerilogObject obj,
        Object value,
        int index)
    {
        this.time = time;
        this.obj = obj;
        this.value = value;
        this.index = index;
    }

    public VerilogTime getTime()
    {
        return time;
    }

    public VerilogObject getObject()
    {
        return obj;
    }

    public Object getValue()
    {
        return value;
    }

    public int getIndex()
    {
        return index;
    }
}
