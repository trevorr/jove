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

import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import com.newisys.verilog.*;

/**
 * PLI implementation of VerilogSimulation.
 * 
 * @author Trevor Robinson
 */
public final class PLIVerilogSimulation
    implements VerilogSimulation
{
    private final PLI pliNonProxied;
    private final PLIInterface pliProxy;
    private final PLIVerilogInfo pliCachedInfo;

    public PLIVerilogSimulation(PLI pli)
    {
        pliNonProxied = pli;
        pliProxy = pliNonProxied.getProxyInterface();
        pliCachedInfo = pliProxy.getInfo();
    }

    public String getProduct()
    {
        return pliCachedInfo.getProduct();
    }

    public String getVersion()
    {
        return pliCachedInfo.getVersion();
    }

    public List<String> getArguments()
    {
        return pliCachedInfo.getArguments();
    }

    public long getSimTime()
    {
        if (pliNonProxied.cachedSimTime != PLITime.INVALID_SIM_TIME)
        {
            return pliNonProxied.cachedSimTime;
        }
        return pliProxy.getTime(PLITimeTypeConstants.vpiSim).getSimTime();
    }

    public double getScaledRealTime()
    {
        if (pliNonProxied.cachedScaledRealTime != PLITime.INVALID_SCALED_REAL_TIME)
        {
            return pliNonProxied.cachedScaledRealTime;
        }
        return pliProxy.getTime(PLITimeTypeConstants.vpiScaledReal)
            .getScaledRealTime();
    }

    private <T extends VerilogObject> Iterator<T> getTopLevelObjects(int type)
    {
        return pliProxy.iterate(type, PLIVerilogObject.NULL_HANDLE);
    }

    public Iterator<VerilogModule> getModules()
    {
        return getTopLevelObjects(PLIObjectTypeConstants.vpiModule);
    }

    public Iterator<VerilogUdpDefn> getUdpDefns()
    {
        return getTopLevelObjects(PLIObjectTypeConstants.vpiUdpDefn);
    }

    public Iterator<VerilogUserSystf> getUserSystfs()
    {
        return getTopLevelObjects(PLIObjectTypeConstants.vpiUserSystf);
    }

    public Iterator<VerilogCallback> getCallbacks()
    {
        return getTopLevelObjects(PLIObjectTypeConstants.vpiCallback);
    }

    public Iterator<VerilogTimeQueue> getTimeQueues()
    {
        return getTopLevelObjects(PLIObjectTypeConstants.vpiTimeQueue);
    }

    public VerilogInterModPath getInterModPath(
        VerilogPort port1,
        VerilogPort port2)
    {
        return (VerilogInterModPath) pliProxy.getObjectMulti(
            PLIObjectTypeConstants.vpiInterModPath, ((PLIVerilogObject) port1)
                .getHandle(), ((PLIVerilogObject) port2).getHandle());
    }

    public VerilogObject getObjectByName(String name)
    {
        return pliProxy.getObjectByName(name, PLIVerilogObject.NULL_HANDLE);
    }

    public VerilogCallback addSimulationStartCallback(
        VerilogCallbackHandler handler)
    {
        try
        {
            return pliProxy.registerCallback(
                PLICallbackReason.START_OF_SIMULATION, handler);
        }
        catch (VerilogRuntimeException e)
        {
            throw new VerilogRuntimeException(
                "Unable to register callback for start of simulation", e);
        }
    }

    public VerilogCallback addSimulationEndCallback(
        VerilogCallbackHandler handler)
    {
        try
        {
            return pliProxy.registerCallback(
                PLICallbackReason.END_OF_SIMULATION, handler);
        }
        catch (VerilogRuntimeException e)
        {
            throw new VerilogRuntimeException(
                "Unable to register callback for end of simulation", e);
        }
    }

    public VerilogCallback addStartOfSimTimeCallback(
        VerilogTime time,
        VerilogCallbackHandler handler)
    {
        try
        {
            return pliProxy.registerCallback(
                PLICallbackReason.AT_START_OF_SIM_TIME, PLITime
                    .getPLITime(time), handler);
        }
        catch (VerilogRuntimeException e)
        {
            throw new VerilogRuntimeException(
                "Unable to register callback for start of simulation time "
                    + time, e);
        }
    }

    public VerilogCallback addDelayCallback(
        VerilogTime delay,
        VerilogCallbackHandler handler)
    {
        try
        {
            return pliProxy.registerCallback(PLICallbackReason.AFTER_DELAY,
                PLITime.getPLITime(delay), handler);
        }
        catch (VerilogRuntimeException e)
        {
            throw new VerilogRuntimeException(
                "Unable to register callback for delay " + delay, e);
        }
    }

    public VerilogCallback addNextSimTimeCallback(VerilogCallbackHandler handler)
    {
        try
        {
            return pliProxy.registerCallback(PLICallbackReason.NEXT_SIM_TIME,
                handler);
        }
        catch (VerilogRuntimeException e)
        {
            throw new VerilogRuntimeException(
                "Unable to register callback for next simulation time", e);
        }
    }

    public VerilogCallback addReadWriteSynchCallback(
        VerilogCallbackHandler handler)
    {
        try
        {
            return pliProxy.registerCallback(
                PLICallbackReason.READ_WRITE_SYNCH, handler);
        }
        catch (VerilogRuntimeException e)
        {
            throw new VerilogRuntimeException(
                "Unable to register callback for read/write synch", e);
        }
    }

    public VerilogCallback addReadOnlySynchCallback(
        VerilogCallbackHandler handler)
    {
        try
        {
            return pliProxy.registerCallback(PLICallbackReason.READ_ONLY_SYNCH,
                handler);
        }
        catch (VerilogRuntimeException e)
        {
            throw new VerilogRuntimeException(
                "Unable to register callback for read-only synch", e);
        }
    }

    public OutputStream getLogOutputStream()
    {
        return new PLILogOutputStream(pliProxy);
    }

    public void stop()
    {
        pliProxy.stop();
    }

    public void finish()
    {
        pliProxy.finish();
    }
}
