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
 * Enumeration of operator types.
 * 
 * @author Trevor Robinson
 */
public enum OpType
{
    MINUS,
    PLUS,
    NOT,
    BIT_NEG,
    UNARY_AND,
    UNARY_NAND,
    UNARY_OR,
    UNARY_NOR,
    UNARY_XOR,
    UNARY_XNOR,
    SUB,
    DIV,
    MOD,
    EQ,
    NEQ,
    CASE_EQ,
    CASE_NEQ,
    GT,
    GE,
    LT,
    LE,
    LSHIFT,
    RSHIFT,
    ADD,
    MULT,
    LOG_AND,
    LOG_OR,
    BIT_AND,
    BIT_OR,
    BIT_XOR,
    BIT_XNOR,
    CONDITION,
    CONCAT,
    MULTI_CONCAT,
    EVENT_OR,
    NULL,
    LIST,
    MIN_TYP_MAX,
    POSEDGE,
    NEGEDGE,
    ARITH_LSHIFT,
    ARITH_RSHIFT,
    POWER;
}
