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
 * PLI integer constants for the PLICallbackReason enumeration.
 * 
 * @author Trevor Robinson
 */
public interface PLICallbackReasonConstants
{
    int vpiValueChange = 1;
    int vpiStmt = 2;
    int vpiForce = 3;
    int vpiRelease = 4;
    int vpiAtStartOfSimTime = 5;
    int vpiReadWriteSynch = 6;
    int vpiReadOnlySynch = 7;
    int vpiNextSimTime = 8;
    int vpiAfterDelay = 9;
    int vpiEndOfCompile = 10;
    int vpiStartOfSimulation = 11;
    int vpiEndOfSimulation = 12;
    int vpiError = 13;
    int vpiTchkViolation = 14;
    int vpiStartOfSave = 15;
    int vpiEndOfSave = 16;
    int vpiStartOfRestart = 17;
    int vpiEndOfRestart = 18;
    int vpiStartOfReset = 19;
    int vpiEndOfReset = 20;
    int vpiEnterInteractive = 21;
    int vpiExitInteractive = 22;
    int vpiInteractiveScopeChange = 23;
    int vpiUnresolvedSystf = 24;
    int vpiAssign = 25;
    int vpiDeassign = 26;
    int vpiDisable = 27;
    int vpiPLIError = 28;
    int vpiSignal = 29;
}
