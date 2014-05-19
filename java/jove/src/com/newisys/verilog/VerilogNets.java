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

import java.util.Iterator;

/**
 * Represents the common features of a net or net bit.
 * 
 * @author Trevor Robinson
 */
public interface VerilogNets
    extends VerilogAbsVar, VerilogReadValue, VerilogWriteValueDelay,
    VerilogWriteValueForce
{
    // properties
    NetType getNetType();

    boolean isScalar();

    boolean isVector();

    boolean isExplicitScalared();

    boolean isExplicitVectored();

    boolean isExpanded();

    boolean isNetDeclAssign();

    boolean isImplicitDecl();

    int getSize();

    int getStrength0();

    int getStrength1();

    int getChargeStrength();

    // objects
    VerilogExpr getLeftRange();

    VerilogExpr getRightRange();

    Iterator<VerilogPort> getPorts();

    Iterator<VerilogPort> getPortInsts();

    Iterator<VerilogContAssign> getContAssigns();

    Iterator<VerilogPrimTerm> getPrimTerms();

    Iterator<VerilogPathTerm> getPathTerms();

    Iterator<VerilogTchkTerm> getTchkTerms();

    Iterator<VerilogNetDriver> getDrivers();

    Iterator<VerilogNetLoad> getLoads();
}
