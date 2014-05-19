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

/** \file
 * Defines the JavaObjectCache class and singleton instance pointer.
 *
 * \author Trevor Robinson
 */

// module header
#include "JavaObjectCache.h"

// using declarations
using namespace jnicpp;

// shared data
JavaObjectCache* pjoc = NULL;

static jobject getFieldObject(
    const JClass& cls, const char* fieldName, const char* signature)
{
    JStaticField<jobject> field(cls, fieldName, signature);
    return field.value();
}

JavaObjectCache::JavaObjectCache(const JEnv& env) :
    // java/lang/String
    String_class(env, "java/lang/String"),

    // java/lang/Boolean
    Boolean_class(env, "java/lang/Boolean"),
    Boolean_booleanValue(Boolean_class, "booleanValue", "()Z"),

    // java/lang/Integer
    Integer_class(env, "java/lang/Integer"),
    Integer_ctor(Integer_class, "(I)V"),
    Integer_intValue(Integer_class, "intValue", "()I"),

    // java/lang/Double
    Double_class(env, "java/lang/Double"),
    Double_ctor(Double_class, "(D)V"),
    Double_doubleValue(Double_class, "doubleValue", "()D"),

    // java/lang/Throwable
    Throwable_class(env, "java/lang/Throwable"),
    Throwable_printStackTrace(Throwable_class, "printStackTrace", "()V"),

    // java/lang/RuntimeException
    RuntimeException_class(env, "java/lang/RuntimeException"),
    RuntimeException_ctor(RuntimeException_class, "(Ljava/lang/String;)V"),

    // java/lang/NullPointerException
    NullPointerException_class(env, "java/lang/NullPointerException"),
    NullPointerException_ctor(NullPointerException_class, "(Ljava/lang/String;)V"),

    // java/lang/ClassCastException
    ClassCastException_class(env, "java/lang/ClassCastException"),
    ClassCastException_ctor(ClassCastException_class, "(Ljava/lang/String;)V"),

    // com/newisys/verilog/VerilogObject
    VerilogObject_class(env, "com/newisys/verilog/VerilogObject"),

    // com/newisys/verilog/VerilogApplication
    VerilogApplication_class(env, "com/newisys/verilog/VerilogApplication"),
    VerilogApplication_registerObject(VerilogApplication_class, "registerObject",
        "(Ljava/lang/String;Lcom/newisys/verilog/VerilogObject;)V"),
    VerilogApplication_registerSignal(VerilogApplication_class, "registerSignal",
        "(Ljava/lang/String;Lcom/newisys/verilog/VerilogObject;Lcom/newisys/verilog/VerilogObject;)V"),
    VerilogApplication_registerVerilogTask(VerilogApplication_class, "registerVerilogTask",
        "(Ljava/lang/String;Lcom/newisys/verilog/VerilogReg;Lcom/newisys/verilog/VerilogReg;[Lcom/newisys/verilog/VerilogObject;)V"),
    VerilogApplication_callJavaTask(VerilogApplication_class, "callJavaTask",
        "(Ljava/lang/String;Lcom/newisys/verilog/VerilogReg;[Lcom/newisys/verilog/VerilogObject;)V"),
    VerilogApplication_start(VerilogApplication_class, "start", "()V"),
    VerilogApplication_finish(VerilogApplication_class, "finish", "()V"),

    // com/newisys/verilog/util/Bit
    Bit_class(env, "com/newisys/verilog/util/Bit"),
    Bit_getID(Bit_class, "getID", "()I"),
    Bit_ZERO(env, getFieldObject(Bit_class, "ZERO", "Lcom/newisys/verilog/util/Bit;"), true),
    Bit_ONE(env, getFieldObject(Bit_class, "ONE", "Lcom/newisys/verilog/util/Bit;"), true),
    Bit_Z(env, getFieldObject(Bit_class, "Z", "Lcom/newisys/verilog/util/Bit;"), true),
    Bit_X(env, getFieldObject(Bit_class, "X", "Lcom/newisys/verilog/util/Bit;"), true),

    // com/newisys/verilog/util/BitVector
    BitVector_class(env, "com/newisys/verilog/util/BitVector"),
    BitVector_ctor(BitVector_class, "([I[II)V"),
    BitVector_values(BitVector_class, "values", "()[I"),
    BitVector_xzMask(BitVector_class, "xzMask", "()[I"),
    BitVector_length(BitVector_class, "length", "()I"),

    // com/newisys/verilog/pli/PLI
    PLI_class(env, "com/newisys/verilog/pli/PLI"),
    PLI_createObject(PLI_class, "createObject",
        "(J)Lcom/newisys/verilog/pli/PLIVerilogObject;"),
    PLI_dispatchCallback(PLI_class, "dispatchCallback",
        "(Lcom/newisys/verilog/pli/PLIVerilogCallback;IJDLjava/lang/Object;I)V"),

    // com/newisys/verilog/pli/PLITime
    PLITime_class(env, "com/newisys/verilog/pli/PLITime"),
    PLITime_ctor(PLITime_class, "(IJD)V"),

    // com/newisys/verilog/pli/PLIVerilogInfo
    PLIVerilogInfo_class(env, "com/newisys/verilog/pli/PLIVerilogInfo"),
    PLIVerilogInfo_ctor(PLIVerilogInfo_class, "(Ljava/lang/String;Ljava/lang/String;)V"),
    PLIVerilogInfo_addArgument(PLIVerilogInfo_class, "addArgument",
        "(Ljava/lang/String;)V"),

    // com/newisys/ova/OVA
    // This is instantiated even when USE_OVA is undefined since
    // it provides OVAInterface.ovaIsSupported()
    OVA_class(env, "com/newisys/ova/OVA")
#ifdef USE_OVA
    ,
    // com/newisys/ova/OVAException
    OVAException_class(env, "com/newisys/ova/OVAException"),
    OVAException_ctor(OVAException_class, "(Ljava/lang/String;)V"),

    // com/newisys/ova/OVAEngine
    OVAEngine_class(env, "com/newisys/ova/OVAEngine"),
    OVAEngine_dispatchEngineCallback(OVAEngine_class, "dispatchEngineCallback",
            "(Lcom/newisys/ova/OVAEngineEventType;J)V"),
    OVAEngine_dispatchAssertCallback(OVAEngine_class, "dispatchAssertCallback",
            "(Lcom/newisys/ova/OVAAssertEventType;JJJ)V"),
    // com/newisys/ova/OVAEngineEventType
    OVAEngineEventType_class(env, "com/newisys/ova/OVAEngineEventType"),
    OVAEngineEventType_forValue(OVAEngineEventType_class, "forValue", "(I)Lcom/newisys/ova/OVAEngineEventType;"),

    // com/newisys/ova/OVAAssertEventType
    OVAAssertEventType_class(env, "com/newisys/ova/OVAAssertEventType"),
    OVAAssertEventType_forValue(OVAAssertEventType_class, "forValue", "(I)Lcom/newisys/ova/OVAAssertEventType;"),

    
    // com/newisys/ova/OVAExprType
    OVAExprType_class(env, "com/newisys/ova/OVAExprType"),
    OVAExprType_forValue(OVAExprType_class, "forValue", "(I)Lcom/newisys/ova/OVAExprType;"),

    // com/newisys/ova/OVASourceFileInfo
    OVASourceFileInfo_class(env, "com/newisys/ova/OVASourceFileInfo"),
    OVASourceFileInfo_ctor(OVASourceFileInfo_class, "(Ljava/lang/String;IIII)V"),

    // com/newisys/ova/OVAAssertInfo
    OVAAssertInfo_class(env, "com/newisys/ova/OVAAssertInfo"),
    OVAAssertInfo_ctor(OVAAssertInfo_class,
            "(Ljava/lang/String;Lcom/newisys/ova/OVAExprType;Lcom/newisys/ova/OVASourceFileInfo;IILjava/lang/String;Ljava/lang/String;)V")
#endif // USE_OVA
{
}

