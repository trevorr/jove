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
 * PLI integer constants for the PLIRelationType enumeration.
 * 
 * @author Trevor Robinson
 */
public interface PLIRelationTypeConstants
{
    // methods used to traverse 1 to 1 relationships
    int vpiCondition = 71;
    int vpiDelay = 72;
    int vpiElseStmt = 73;
    int vpiForIncStmt = 74;
    int vpiForInitStmt = 75;
    int vpiHighConn = 76;
    int vpiLhs = 77;
    int vpiIndex = 78;
    int vpiLeftRange = 79;
    int vpiLowConn = 80;
    int vpiParent = 81;
    int vpiRhs = 82;
    int vpiRightRange = 83;
    int vpiScope = 84;
    int vpiSysTfCall = 85;
    int vpiTchkDataTerm = 86;
    int vpiTchkNotifier = 87;
    int vpiTchkRefTerm = 88;

    // methods used to traverse 1 to many relationships
    int vpiArgument = 89;
    int vpiBit = 90;
    int vpiDriver = 91;
    int vpiInternalScope = 92;
    int vpiLoad = 93;
    int vpiModDataPathIn = 94;
    int vpiModPathIn = 95;
    int vpiModPathOut = 96;
    int vpiOperand = 97;
    int vpiPortInst = 98;
    int vpiProcess = 99;
    int vpiVariables = 100;
    int vpiUse = 101;

    // methods which can traverse 1 to 1, or 1 to many relationships
    int vpiExpr = 102;
    int vpiPrimitive = 103;
    int vpiStmt = 104;

    // methods added with 1364-2000
    int vpiActiveTimeFormat = 119;
    int vpiInTerm = 120;
    int vpiInstanceArray = 121;
    int vpiLocalDriver = 122;
    int vpiLocalLoad = 123;
    int vpiOutTerm = 124;
    int vpiPorts = 125;
    int vpiSimNet = 126;
    int vpiTaskFunc = 127;
}
