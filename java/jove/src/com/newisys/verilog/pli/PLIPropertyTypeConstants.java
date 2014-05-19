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
 * PLI integer constants for the PLIPropertyType enumeration.
 * 
 * @author Trevor Robinson
 */
public interface PLIPropertyTypeConstants
{
    // generic properties
    int vpiType = 1;
    int vpiName = 2;
    int vpiFullName = 3;
    int vpiSize = 4;
    int vpiFile = 5;
    int vpiLineNo = 6;

    // module properties
    int vpiTopModule = 7;
    int vpiCellInstance = 8;
    int vpiDefName = 9;
    int vpiProtected = 10;
    int vpiTimeUnit = 11;
    int vpiTimePrecision = 12;
    int vpiDefNetType = 13;
    int vpiUnconnDrive = 14;
    int vpiDefFile = 15;
    int vpiDefLineNo = 16;
    int vpiDefDelayMode = 47;
    int vpiDefDecayTime = 48;

    // port and net properties
    int vpiScalar = 17;
    int vpiVector = 18;
    int vpiExplicitName = 19;
    int vpiDirection = 20;
    int vpiConnByName = 21;
    int vpiNetType = 22;
    int vpiExplicitScalared = 23;
    int vpiExplicitVectored = 24;
    int vpiExpanded = 25;
    int vpiImplicitDecl = 26;
    int vpiChargeStrength = 27;
    int vpiArray = 28;
    int vpiPortIndex = 29;

    // gate and terminal properties
    int vpiTermIndex = 30;
    int vpiStrength0 = 31;
    int vpiStrength1 = 32;
    int vpiPrimType = 33;

    // path, path terminal, timing check properties
    int vpiPolarity = 34;
    int vpiDataPolarity = 35;
    int vpiEdge = 36;
    int vpiPathType = 37;
    int vpiTchkType = 38;

    // expression properties
    int vpiOpType = 39;
    int vpiConstType = 40;
    int vpiBlocking = 41;
    int vpiCaseType = 42;
    int vpiNetDeclAssign = 43;

    // task/function properties
    int vpiFuncType = 44;
    int vpiUserDefn = 45;
    int vpiScheduled = 46;

    // 1364-2000 properties
    int vpiActive = 49;
    int vpiAutomatic = 50;
    int vpiCell = 51;
    int vpiConfig = 52;
    int vpiConstantSelect = 53;
    int vpiDecompile = 54;
    int vpiDefAttribute = 55;
    int vpiDelayType = 56;
    int vpiIteratorType = 57;
    int vpiLibrary = 58;
    int vpiMultiArray = 59;
    int vpiOffset = 60;
    int vpiResolvedNetType = 61;
    int vpiSaveRestartID = 62;
    int vpiSaveRestartLocation = 63;
    int vpiValid = 64;
    int vpiSigned = 65;
    int vpiLocalParam = 70;
    int vpiModPathHasIfNone = 71;
}
