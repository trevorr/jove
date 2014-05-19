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
 * Enumeration of callback reasons.
 * 
 * @author Trevor Robinson
 */
public enum CallbackReason
{
    VALUE_CHANGE,
    STMT,
    FORCE,
    RELEASE,
    AT_START_OF_SIM_TIME,
    READ_WRITE_SYNCH,
    READ_ONLY_SYNCH,
    NEXT_SIM_TIME,
    AFTER_DELAY,
    END_OF_COMPILE,
    START_OF_SIMULATION,
    END_OF_SIMULATION,
    ERROR,
    TCHK_VIOLATION,
    START_OF_SAVE,
    END_OF_SAVE,
    START_OF_RESTART,
    END_OF_RESTART,
    START_OF_RESET,
    END_OF_RESET,
    ENTER_INTERACTIVE,
    EXIT_INTERACTIVE,
    INTERACTIVE_SCOPE_CHANGE,
    UNRESOLVED_SYSTF,
    ASSIGN,
    DEASSIGN,
    DISABLE,
    PLI_ERROR,
    SIGNAL;
}
