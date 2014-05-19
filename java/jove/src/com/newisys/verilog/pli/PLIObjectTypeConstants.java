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

/**
 * PLI integer constants for the PLIObjectType enumeration.
 * 
 * @author Trevor Robinson
 */
public interface PLIObjectTypeConstants
{
    // 1364-1995 object types
    public static final int vpiAlways = 1;
    public static final int vpiAssignStmt = 2;
    public static final int vpiAssignment = 3;
    public static final int vpiBegin = 4;
    public static final int vpiCase = 5;
    public static final int vpiCaseItem = 6;
    public static final int vpiConstant = 7;
    public static final int vpiContAssign = 8;
    public static final int vpiDeassign = 9;
    public static final int vpiDefParam = 10;
    public static final int vpiDelayControl = 11;
    public static final int vpiDisable = 12;
    public static final int vpiEventControl = 13;
    public static final int vpiEventStmt = 14;
    public static final int vpiFor = 15;
    public static final int vpiForce = 16;
    public static final int vpiForever = 17;
    public static final int vpiFork = 18;
    public static final int vpiFuncCall = 19;
    public static final int vpiFunction = 20;
    public static final int vpiGate = 21;
    public static final int vpiIf = 22;
    public static final int vpiIfElse = 23;
    public static final int vpiInitial = 24;
    public static final int vpiIntegerVar = 25;
    public static final int vpiInterModPath = 26;
    public static final int vpiIterator = 27;
    public static final int vpiIODecl = 28;
    public static final int vpiMemory = 29;
    public static final int vpiMemoryWord = 30;
    public static final int vpiModPath = 31;
    public static final int vpiModule = 32;
    public static final int vpiNamedBegin = 33;
    public static final int vpiNamedEvent = 34;
    public static final int vpiNamedFork = 35;
    public static final int vpiNet = 36;
    public static final int vpiNetBit = 37;
    public static final int vpiNullStmt = 38;
    public static final int vpiOperation = 39;
    public static final int vpiParamAssign = 40;
    public static final int vpiParameter = 41;
    public static final int vpiPartSelect = 42;
    public static final int vpiPathTerm = 43;
    public static final int vpiPort = 44;
    public static final int vpiPortBit = 45;
    public static final int vpiPrimTerm = 46;
    public static final int vpiRealVar = 47;
    public static final int vpiReg = 48;
    public static final int vpiRegBit = 49;
    public static final int vpiRelease = 50;
    public static final int vpiRepeat = 51;
    public static final int vpiRepeatControl = 52;
    public static final int vpiSchedEvent = 53;
    public static final int vpiSpecParam = 54;
    public static final int vpiSwitch = 55;
    public static final int vpiSysFuncCall = 56;
    public static final int vpiSysTaskCall = 57;
    public static final int vpiTableEntry = 58;
    public static final int vpiTask = 59;
    public static final int vpiTaskCall = 60;
    public static final int vpiTchk = 61;
    public static final int vpiTchkTerm = 62;
    public static final int vpiTimeVar = 63;
    public static final int vpiTimeQueue = 64;
    public static final int vpiUdp = 65;
    public static final int vpiUdpDefn = 66;
    public static final int vpiUserSystf = 67;
    public static final int vpiVarSelect = 68;
    public static final int vpiWait = 69;
    public static final int vpiWhile = 70;

    // 1364-2000 object types
    public static final int vpiAttribute = 105;
    public static final int vpiBitSelect = 106;
    public static final int vpiCallback = 107;
    public static final int vpiDelayTerm = 108;
    public static final int vpiDelayDevice = 109;
    public static final int vpiFrame = 110;
    public static final int vpiGateArray = 111;
    public static final int vpiModuleArray = 112;
    public static final int vpiPrimitiveArray = 113;
    public static final int vpiNetArray = 114;
    public static final int vpiRange = 115;
    public static final int vpiRegArray = 116;
    public static final int vpiSwitchArray = 117;
    public static final int vpiUdpArray = 118;
    public static final int vpiContAssignBit = 128;
    public static final int vpiNamedEventArray = 129;
}
