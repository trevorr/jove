/*
 * PLI4J - A Java (TM) Interface to the Verilog PLI
 * Copyright (C) 2003 Trevor A. Robinson
 * Java is a registered trademark of Sun Microsystems, Inc. in the U.S. or
 * other countries.
 *
 * Licensed under the Academic Free License version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You should
 * have received a copy of the License along with this software; if not, you
 * may obtain a copy of the License at
 *
 * http://opensource.org/licenses/afl-2.0.php
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.newisys.verilog;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Top-level interface used to interact with a Verilog simulation.
 * 
 * @author Trevor Robinson
 */
public interface VerilogSimulation
{
    // simulator information
    String getProduct();

    String getVersion();

    List<String> getArguments();

    // time access
    long getSimTime();

    double getScaledRealTime();

    // top-level objects
    Iterator<VerilogModule> getModules();

    Iterator<VerilogUdpDefn> getUdpDefns();

    Iterator<VerilogUserSystf> getUserSystfs();

    Iterator<VerilogCallback> getCallbacks();

    Iterator<VerilogTimeQueue> getTimeQueues();

    VerilogInterModPath getInterModPath(VerilogPort port1, VerilogPort port2);

    // general object access
    VerilogObject getObjectByName(String name);

    // callbacks
    VerilogCallback addSimulationStartCallback(VerilogCallbackHandler handler);

    VerilogCallback addSimulationEndCallback(VerilogCallbackHandler handler);

    VerilogCallback addStartOfSimTimeCallback(
        VerilogTime time,
        VerilogCallbackHandler handler);

    VerilogCallback addDelayCallback(
        VerilogTime delay,
        VerilogCallbackHandler handler);

    VerilogCallback addNextSimTimeCallback(VerilogCallbackHandler handler);

    VerilogCallback addReadWriteSynchCallback(VerilogCallbackHandler handler);

    VerilogCallback addReadOnlySynchCallback(VerilogCallbackHandler handler);

    // I/O
    OutputStream getLogOutputStream();

    // simulation control
    void stop();

    void finish();
}
