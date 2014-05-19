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

/**
 * Enumeration of primitive types.
 * 
 * @author Trevor Robinson
 */
public enum PrimitiveType
{
    AND,
    NAND,
    NOR,
    OR,
    XOR,
    XNOR,
    BUF,
    NOT,
    BUFIF0,
    BUFIF1,
    NOTIF0,
    NOTIF1,
    NMOS,
    PMOS,
    CMOS,
    RNMOS,
    RPMOS,
    RCMOS,
    RTRAN,
    RTRANIF0,
    RTRANIF1,
    TRAN,
    TRANIF0,
    TRANIF1,
    PULLUP,
    PULLDOWN,
    SEQ,
    COMB;
}
