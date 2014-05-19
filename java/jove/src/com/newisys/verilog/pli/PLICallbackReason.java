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

import static com.newisys.verilog.pli.PLICallbackReasonConstants.*;

import com.newisys.verilog.CallbackReason;

/**
 * PLI version of the CallbackReason enumeration.
 * 
 * @author Trevor Robinson
 */
public enum PLICallbackReason implements PLIVerilogEnum<CallbackReason>
{
    VALUE_CHANGE(CallbackReason.VALUE_CHANGE, vpiValueChange, true),
    STMT(CallbackReason.STMT, vpiStmt, true),
    FORCE(CallbackReason.FORCE, vpiForce, true),
    RELEASE(CallbackReason.RELEASE, vpiRelease, true),
    AT_START_OF_SIM_TIME(CallbackReason.AT_START_OF_SIM_TIME,
        vpiAtStartOfSimTime, false),
    READ_WRITE_SYNCH(CallbackReason.READ_WRITE_SYNCH, vpiReadWriteSynch, false),
    READ_ONLY_SYNCH(CallbackReason.READ_ONLY_SYNCH, vpiReadOnlySynch, false),
    NEXT_SIM_TIME(CallbackReason.NEXT_SIM_TIME, vpiNextSimTime, false),
    AFTER_DELAY(CallbackReason.AFTER_DELAY, vpiAfterDelay, false),
    END_OF_COMPILE(CallbackReason.END_OF_COMPILE, vpiEndOfCompile, false),
    START_OF_SIMULATION(CallbackReason.START_OF_SIMULATION,
        vpiStartOfSimulation, false),
    END_OF_SIMULATION(CallbackReason.END_OF_SIMULATION, vpiEndOfSimulation,
        false),
    ERROR(CallbackReason.ERROR, vpiError, true),
    TCHK_VIOLATION(CallbackReason.TCHK_VIOLATION, vpiTchkViolation, true),
    START_OF_SAVE(CallbackReason.START_OF_SAVE, vpiStartOfSave, true),
    END_OF_SAVE(CallbackReason.END_OF_SAVE, vpiEndOfSave, true),
    START_OF_RESTART(CallbackReason.START_OF_RESTART, vpiStartOfRestart, true),
    END_OF_RESTART(CallbackReason.END_OF_RESTART, vpiEndOfRestart, true),
    START_OF_RESET(CallbackReason.START_OF_RESET, vpiStartOfReset, true),
    END_OF_RESET(CallbackReason.END_OF_RESET, vpiEndOfReset, true),
    ENTER_INTERACTIVE(CallbackReason.ENTER_INTERACTIVE, vpiEnterInteractive,
        true),
    EXIT_INTERACTIVE(CallbackReason.EXIT_INTERACTIVE, vpiExitInteractive, true),
    INTERACTIVE_SCOPE_CHANGE(CallbackReason.INTERACTIVE_SCOPE_CHANGE,
        vpiInteractiveScopeChange, true),
    UNRESOLVED_SYSTF(CallbackReason.UNRESOLVED_SYSTF, vpiUnresolvedSystf, true),
    ASSIGN(CallbackReason.ASSIGN, vpiAssign, true),
    DEASSIGN(CallbackReason.DEASSIGN, vpiDeassign, true),
    DISABLE(CallbackReason.DISABLE, vpiDisable, true),
    PLI_ERROR(CallbackReason.PLI_ERROR, vpiPLIError, true),
    SIGNAL(CallbackReason.SIGNAL, vpiSignal, true);

    private final CallbackReason baseEnum;
    private final int value;
    private final boolean recurring;

    PLICallbackReason(CallbackReason baseEnum, int value, boolean recurring)
    {
        this.baseEnum = baseEnum;
        this.value = value;
        this.recurring = recurring;
    }

    public CallbackReason getVerilogEnum()
    {
        return baseEnum;
    }

    public int getValue()
    {
        return value;
    }

    public boolean isRecurring()
    {
        return recurring;
    }

    public static PLICallbackReason getCallbackReason(CallbackReason baseEnum)
    {
        for (PLICallbackReason v : values())
        {
            if (v.getVerilogEnum() == baseEnum) return v;
        }
        throw new IllegalArgumentException("Enum object mapping not found: "
            + baseEnum);
    }

    public static PLICallbackReason getCallbackReason(int value)
    {
        for (PLICallbackReason v : values())
        {
            if (v.getValue() == value) return v;
        }
        throw new IllegalArgumentException("Enum value mapping not found: "
            + value);
    }
}
