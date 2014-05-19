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

import static com.newisys.verilog.pli.PLIPropertyTypeConstants.*;

/**
 * Enumeration of PLI property types.
 * 
 * @author Trevor Robinson
 */
public enum PLIPropertyType implements PLIEnum
{
    TYPE(vpiType),
    NAME(vpiName),
    FULL_NAME(vpiFullName),
    SIZE(vpiSize),
    FILE(vpiFile),
    LINE_NO(vpiLineNo),
    TOP_MODULE(vpiTopModule),
    CELL_INSTANCE(vpiCellInstance),
    DEF_NAME(vpiDefName),
    PROTECTED(vpiProtected),
    TIME_UNIT(vpiTimeUnit),
    TIME_PRECISION(vpiTimePrecision),
    DEF_NET_TYPE(vpiDefNetType),
    UNCONN_DRIVE(vpiUnconnDrive),
    DEF_FILE(vpiDefFile),
    DEF_LINE_NO(vpiDefLineNo),
    DEF_DELAY_MODE(vpiDefDelayMode),
    DEF_DECAY_TIME(vpiDefDecayTime),
    SCALAR(vpiScalar),
    VECTOR(vpiVector),
    EXPLICIT_NAME(vpiExplicitName),
    DIRECTION(vpiDirection),
    CONN_BY_NAME(vpiConnByName),
    NET_TYPE(vpiNetType),
    EXPLICIT_SCALARED(vpiExplicitScalared),
    EXPLICIT_VECTORED(vpiExplicitVectored),
    EXPANDED(vpiExpanded),
    IMPLICIT_DECL(vpiImplicitDecl),
    CHARGE_STRENGTH(vpiChargeStrength),
    ARRAY(vpiArray),
    PORT_INDEX(vpiPortIndex),
    TERM_INDEX(vpiTermIndex),
    STRENGTH0(vpiStrength0),
    STRENGTH1(vpiStrength1),
    PRIM_TYPE(vpiPrimType),
    POLARITY(vpiPolarity),
    DATA_POLARITY(vpiDataPolarity),
    EDGE(vpiEdge),
    PATH_TYPE(vpiPathType),
    TCHK_TYPE(vpiTchkType),
    OP_TYPE(vpiOpType),
    CONST_TYPE(vpiConstType),
    BLOCKING(vpiBlocking),
    CASE_TYPE(vpiCaseType),
    NET_DECL_ASSIGN(vpiNetDeclAssign),
    FUNC_TYPE(vpiFuncType),
    USER_DEFN(vpiUserDefn),
    SCHEDULED(vpiScheduled),
    ACTIVE(vpiActive),
    AUTOMATIC(vpiAutomatic),
    CELL(vpiCell),
    CONFIG(vpiConfig),
    CONSTANT_SELECT(vpiConstantSelect),
    DECOMPILE(vpiDecompile),
    DEF_ATTRIBUTE(vpiDefAttribute),
    DELAY_TYPE(vpiDelayType),
    ITERATOR_TYPE(vpiIteratorType),
    LIBRARY(vpiLibrary),
    MULTI_ARRAY(vpiMultiArray),
    OFFSET(vpiOffset),
    RESOLVED_NET_TYPE(vpiResolvedNetType),
    SAVE_RESTART_ID(vpiSaveRestartID),
    SAVE_RESTART_LOCATION(vpiSaveRestartLocation),
    VALID(vpiValid),
    SIGNED(vpiSigned),
    LOCAL_PARAM(vpiLocalParam),
    MOD_PATH_HAS_IF_NONE(vpiModPathHasIfNone);

    private final int value;

    PLIPropertyType(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }
}
