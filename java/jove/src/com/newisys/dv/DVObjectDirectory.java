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

import java.util.LinkedHashMap;
import java.util.Map;

import com.newisys.verilog.VerilogObject;

/**
 * Mapping of names to known simulation objects.
 * 
 * @author Trevor Robinson
 */
final class DVObjectDirectory
{
    private final Map<String, VerilogObject> objectMap = new LinkedHashMap<String, VerilogObject>();
    private final Map<String, SignalInfo> signalMap = new LinkedHashMap<String, SignalInfo>();

    public DVObjectDirectory()
    {
        super();
    }

    public synchronized void registerObject(String name, VerilogObject obj)
    {
        assert (obj != null);
        if (objectMap.containsKey(name))
        {
            throw new DVRuntimeException(
                "Duplicate Verilog object registration for: " + name);
        }
        objectMap.put(name, obj);
    }

    public synchronized VerilogObject lookupObject(String name)
    {
        return objectMap.get(name);
    }

    public synchronized void registerSignal(String name, SignalInfo info)
    {
        assert (info != null);
        if (signalMap.containsKey(name))
        {
            throw new DVRuntimeException(
                "Duplicate Verilog signal registration for: " + name);
        }
        signalMap.put(name, info);
    }

    public synchronized SignalInfo lookupSignal(String name)
    {
        return signalMap.get(name);
    }
}
