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
 * PLI integer constants for the PLIValueType enumeration.
 * 
 * @author Trevor Robinson
 */
public interface PLIValueTypeConstants
{
    int vpiBinStr = 1;
    int vpiOctStr = 2;
    int vpiDecStr = 3;
    int vpiHexStr = 4;
    int vpiScalar = 5;
    int vpiInt = 6;
    int vpiReal = 7;
    int vpiString = 8;
    int vpiVector = 9;
    int vpiStrength = 10;
    int vpiTime = 11;
    int vpiObjType = 12;
    int vpiSuppress = 13;
}
