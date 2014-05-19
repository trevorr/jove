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

import static com.newisys.verilog.pli.PLIObjectTypeConstants.*;

import com.newisys.verilog.ObjectType;

/**
 * PLI version of the ObjectType enumeration.
 * 
 * @author Trevor Robinson
 */
public enum PLIObjectType implements PLIVerilogEnum<ObjectType>
{
    ALWAYS(ObjectType.ALWAYS, vpiAlways),
    ASSIGN_STMT(ObjectType.ASSIGN_STMT, vpiAssignStmt),
    ASSIGNMENT(ObjectType.ASSIGNMENT, vpiAssignment),
    BEGIN(ObjectType.BEGIN, vpiBegin),
    CASE(ObjectType.CASE, vpiCase),
    CASE_ITEM(ObjectType.CASE_ITEM, vpiCaseItem),
    CONSTANT(ObjectType.CONSTANT, vpiConstant),
    CONT_ASSIGN(ObjectType.CONT_ASSIGN, vpiContAssign),
    DEASSIGN(ObjectType.DEASSIGN, vpiDeassign),
    DEF_PARAM(ObjectType.DEF_PARAM, vpiDefParam),
    DELAY_CONTROL(ObjectType.DELAY_CONTROL, vpiDelayControl),
    DISABLE(ObjectType.DISABLE, vpiDisable),
    EVENT_CONTROL(ObjectType.EVENT_CONTROL, vpiEventControl),
    EVENT_STMT(ObjectType.EVENT_STMT, vpiEventStmt),
    FOR(ObjectType.FOR, vpiFor),
    FORCE(ObjectType.FORCE, vpiForce),
    FOREVER(ObjectType.FOREVER, vpiForever),
    FORK(ObjectType.FORK, vpiFork),
    FUNC_CALL(ObjectType.FUNC_CALL, vpiFuncCall),
    FUNCTION(ObjectType.FUNCTION, vpiFunction),
    GATE(ObjectType.GATE, vpiGate),
    IF(ObjectType.IF, vpiIf),
    IF_ELSE(ObjectType.IF_ELSE, vpiIfElse),
    INITIAL(ObjectType.INITIAL, vpiInitial),
    INTEGER_VAR(ObjectType.INTEGER_VAR, vpiIntegerVar),
    INTER_MOD_PATH(ObjectType.INTER_MOD_PATH, vpiInterModPath),
    ITERATOR(ObjectType.ITERATOR, vpiIterator),
    IO_DECL(ObjectType.IO_DECL, vpiIODecl),
    MEMORY(ObjectType.MEMORY, vpiMemory),
    MEMORY_WORD(ObjectType.MEMORY_WORD, vpiMemoryWord),
    MOD_PATH(ObjectType.MOD_PATH, vpiModPath),
    MODULE(ObjectType.MODULE, vpiModule),
    NAMED_BEGIN(ObjectType.NAMED_BEGIN, vpiNamedBegin),
    NAMED_EVENT(ObjectType.NAMED_EVENT, vpiNamedEvent),
    NAMED_FORK(ObjectType.NAMED_FORK, vpiNamedFork),
    NET(ObjectType.NET, vpiNet),
    NET_BIT(ObjectType.NET_BIT, vpiNetBit),
    NULL_STMT(ObjectType.NULL_STMT, vpiNullStmt),
    OPERATION(ObjectType.OPERATION, vpiOperation),
    PARAM_ASSIGN(ObjectType.PARAM_ASSIGN, vpiParamAssign),
    PARAMETER(ObjectType.PARAMETER, vpiParameter),
    PART_SELECT(ObjectType.PART_SELECT, vpiPartSelect),
    PATH_TERM(ObjectType.PATH_TERM, vpiPathTerm),
    PORT(ObjectType.PORT, vpiPort),
    PORT_BIT(ObjectType.PORT_BIT, vpiPortBit),
    PRIM_TERM(ObjectType.PRIM_TERM, vpiPrimTerm),
    REAL_VAR(ObjectType.REAL_VAR, vpiRealVar),
    REG(ObjectType.REG, vpiReg),
    REG_BIT(ObjectType.REG_BIT, vpiRegBit),
    RELEASE(ObjectType.RELEASE, vpiRelease),
    REPEAT(ObjectType.REPEAT, vpiRepeat),
    REPEAT_CONTROL(ObjectType.REPEAT_CONTROL, vpiRepeatControl),
    SCHED_EVENT(ObjectType.SCHED_EVENT, vpiSchedEvent),
    SPEC_PARAM(ObjectType.SPEC_PARAM, vpiSpecParam),
    SWITCH(ObjectType.SWITCH, vpiSwitch),
    SYS_FUNC_CALL(ObjectType.SYS_FUNC_CALL, vpiSysFuncCall),
    SYS_TASK_CALL(ObjectType.SYS_TASK_CALL, vpiSysTaskCall),
    TABLE_ENTRY(ObjectType.TABLE_ENTRY, vpiTableEntry),
    TASK(ObjectType.TASK, vpiTask),
    TASK_CALL(ObjectType.TASK_CALL, vpiTaskCall),
    TCHK(ObjectType.TCHK, vpiTchk),
    TCHK_TERM(ObjectType.TCHK_TERM, vpiTchkTerm),
    TIME_VAR(ObjectType.TIME_VAR, vpiTimeVar),
    TIME_QUEUE(ObjectType.TIME_QUEUE, vpiTimeQueue),
    UDP(ObjectType.UDP, vpiUdp),
    UDP_DEFN(ObjectType.UDP_DEFN, vpiUdpDefn),
    USER_SYSTF(ObjectType.USER_SYSTF, vpiUserSystf),
    VAR_SELECT(ObjectType.VAR_SELECT, vpiVarSelect),
    WAIT(ObjectType.WAIT, vpiWait),
    WHILE(ObjectType.WHILE, vpiWhile),
    ATTRIBUTE(ObjectType.ATTRIBUTE, vpiAttribute),
    BIT_SELECT(ObjectType.BIT_SELECT, vpiBitSelect),
    CALLBACK(ObjectType.CALLBACK, vpiCallback),
    DELAY_TERM(ObjectType.DELAY_TERM, vpiDelayTerm),
    DELAY_DEVICE(ObjectType.DELAY_DEVICE, vpiDelayDevice),
    FRAME(ObjectType.FRAME, vpiFrame),
    GATE_ARRAY(ObjectType.GATE_ARRAY, vpiGateArray),
    MODULE_ARRAY(ObjectType.MODULE_ARRAY, vpiModuleArray),
    PRIMITIVE_ARRAY(ObjectType.PRIMITIVE_ARRAY, vpiPrimitiveArray),
    NET_ARRAY(ObjectType.NET_ARRAY, vpiNetArray),
    RANGE(ObjectType.RANGE, vpiRange),
    REG_ARRAY(ObjectType.REG_ARRAY, vpiRegArray),
    SWITCH_ARRAY(ObjectType.SWITCH_ARRAY, vpiSwitchArray),
    UDP_ARRAY(ObjectType.UDP_ARRAY, vpiUdpArray),
    CONT_ASSIGN_BIT(ObjectType.CONT_ASSIGN_BIT, vpiContAssignBit),
    NAMED_EVENT_ARRAY(ObjectType.NAMED_EVENT_ARRAY, vpiNamedEventArray);

    private final ObjectType baseEnum;
    private final int value;

    PLIObjectType(ObjectType baseEnum, int value)
    {
        this.baseEnum = baseEnum;
        this.value = value;
    }

    public ObjectType getVerilogEnum()
    {
        return baseEnum;
    }

    public int getValue()
    {
        return value;
    }

    public static PLIObjectType getObjectType(ObjectType baseEnum)
    {
        for (PLIObjectType v : values())
        {
            if (v.getVerilogEnum() == baseEnum) return v;
        }
        throw new IllegalArgumentException("Enum object mapping not found: "
            + baseEnum);
    }

    public static PLIObjectType getObjectType(int value)
    {
        for (PLIObjectType v : values())
        {
            if (v.getValue() == value) return v;
        }
        throw new IllegalArgumentException("Enum value mapping not found: "
            + value);
    }
}
