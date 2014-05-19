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
 * PLI integer constants for the PLINetType enumeration.
 * 
 * @author Trevor Robinson
 */
public interface PLINetTypeConstants
{
    int vpiWire = 1;
    int vpiWand = 2;
    int vpiWor = 3;
    int vpiTri = 4;
    int vpiTri0 = 5;
    int vpiTri1 = 6;
    int vpiTriReg = 7;
    int vpiTriAnd = 8;
    int vpiTriOr = 9;
    int vpiSupply1 = 10;
    int vpiSupply0 = 11;
    int vpiNone = 12;
}
