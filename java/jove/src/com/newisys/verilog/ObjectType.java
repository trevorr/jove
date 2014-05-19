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
 * Enumeration of PLI object types.
 * 
 * @author Trevor Robinson
 */
public enum ObjectType
{
    ALWAYS,
    ASSIGN_STMT,
    ASSIGNMENT,
    BEGIN,
    CASE,
    CASE_ITEM,
    CONSTANT,
    CONT_ASSIGN,
    DEASSIGN,
    DEF_PARAM,
    DELAY_CONTROL,
    DISABLE,
    EVENT_CONTROL,
    EVENT_STMT,
    FOR,
    FORCE,
    FOREVER,
    FORK,
    FUNC_CALL,
    FUNCTION,
    GATE,
    IF,
    IF_ELSE,
    INITIAL,
    INTEGER_VAR,
    INTER_MOD_PATH,
    ITERATOR,
    IO_DECL,
    MEMORY,
    MEMORY_WORD,
    MOD_PATH,
    MODULE,
    NAMED_BEGIN,
    NAMED_EVENT,
    NAMED_FORK,
    NET,
    NET_BIT,
    NULL_STMT,
    OPERATION,
    PARAM_ASSIGN,
    PARAMETER,
    PART_SELECT,
    PATH_TERM,
    PORT,
    PORT_BIT,
    PRIM_TERM,
    REAL_VAR,
    REG,
    REG_BIT,
    RELEASE,
    REPEAT,
    REPEAT_CONTROL,
    SCHED_EVENT,
    SPEC_PARAM,
    SWITCH,
    SYS_FUNC_CALL,
    SYS_TASK_CALL,
    TABLE_ENTRY,
    TASK,
    TASK_CALL,
    TCHK,
    TCHK_TERM,
    TIME_VAR,
    TIME_QUEUE,
    UDP,
    UDP_DEFN,
    USER_SYSTF,
    VAR_SELECT,
    WAIT,
    WHILE,
    ATTRIBUTE,
    BIT_SELECT,
    CALLBACK,
    DELAY_TERM,
    DELAY_DEVICE,
    FRAME,
    GATE_ARRAY,
    MODULE_ARRAY,
    PRIMITIVE_ARRAY,
    NET_ARRAY,
    RANGE,
    REG_ARRAY,
    SWITCH_ARRAY,
    UDP_ARRAY,
    CONT_ASSIGN_BIT,
    NAMED_EVENT_ARRAY;
}
