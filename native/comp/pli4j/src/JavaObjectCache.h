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
 * Declares the JavaObjectCache class and singleton instance pointer.
 *
 * \author Trevor Robinson
 */

#ifndef JavaObjectCache_h_included
#define JavaObjectCache_h_included

// component configuration header
#include "Configuration.h"

// external component headers
#include "jnicpp.h"

class JavaObjectCache
{
public:
    JavaObjectCache(const jnicpp::JEnv& env);

    // java/lang/String
    jnicpp::JClass String_class;

    // java/lang/Boolean
    jnicpp::JClass Boolean_class;
    jnicpp::JMethod<jboolean> Boolean_booleanValue;

    // java/lang/Integer
    jnicpp::JClass Integer_class;
    jnicpp::JCtor Integer_ctor;
    jnicpp::JMethod<jint> Integer_intValue;

    // java/lang/Double
    jnicpp::JClass Double_class;
    jnicpp::JCtor Double_ctor;
    jnicpp::JMethod<jdouble> Double_doubleValue;

    // java/lang/Throwable
    jnicpp::JClass Throwable_class;
    jnicpp::JMethod<void> Throwable_printStackTrace;

    // java/lang/RuntimeException
    jnicpp::JClass RuntimeException_class;
    jnicpp::JCtorTmpl<jthrowable> RuntimeException_ctor;

    // java/lang/NullPointerException
    jnicpp::JClass NullPointerException_class;
    jnicpp::JCtorTmpl<jthrowable> NullPointerException_ctor;

    // java/lang/ClassCastException
    jnicpp::JClass ClassCastException_class;
    jnicpp::JCtorTmpl<jthrowable> ClassCastException_ctor;

    // com/newisys/verilog/VerilogObject
    jnicpp::JClass VerilogObject_class;

    // com/newisys/verilog/VerilogApplication
    jnicpp::JClass VerilogApplication_class;
    jnicpp::JMethod<void> VerilogApplication_registerObject;
    jnicpp::JMethod<void> VerilogApplication_registerSignal;
    jnicpp::JMethod<void> VerilogApplication_registerVerilogTask;
    jnicpp::JMethod<void> VerilogApplication_callJavaTask;
    jnicpp::JMethod<void> VerilogApplication_start;
    jnicpp::JMethod<void> VerilogApplication_finish;

    // com/newisys/verilog/util/Bit
    jnicpp::JClass Bit_class;
    jnicpp::JMethod<jint> Bit_getID;
    jnicpp::JObject Bit_ZERO;
    jnicpp::JObject Bit_ONE;
    jnicpp::JObject Bit_Z;
    jnicpp::JObject Bit_X;

    // com/newisys/verilog/util/BitVector
    jnicpp::JClass BitVector_class;
    jnicpp::JCtor BitVector_ctor;
    jnicpp::JMethod<jintArray> BitVector_values;
    jnicpp::JMethod<jintArray> BitVector_xzMask;
    jnicpp::JMethod<jint> BitVector_length;

    // com/newisys/verilog/pli/PLI
    jnicpp::JClass PLI_class;
    jnicpp::JMethod<jobject> PLI_createObject;
    jnicpp::JMethod<void> PLI_dispatchCallback;

    // com/newisys/verilog/pli/PLITime
    jnicpp::JClass PLITime_class;
    jnicpp::JCtor PLITime_ctor;

    // com/newisys/verilog/pli/PLIVerilogInfo
    jnicpp::JClass PLIVerilogInfo_class;
    jnicpp::JCtor PLIVerilogInfo_ctor;
    jnicpp::JMethod<void> PLIVerilogInfo_addArgument;

    // com/newisys/ova/OVA
    // This is required even if USE_OVA is not defined
    // since it provides OVAInterface.ovaIsSupported()
    jnicpp::JClass OVA_class;
    
#ifdef USE_OVA
    // com/newisys/ova/OVAException
    jnicpp::JClass OVAException_class;
    jnicpp::JCtor OVAException_ctor;

    // com/newisys/ova/OVAEngine
    jnicpp::JClass OVAEngine_class;
    jnicpp::JMethod<void> OVAEngine_dispatchEngineCallback;
    jnicpp::JMethod<void> OVAEngine_dispatchAssertCallback;
    
    // com/newisys/ova/OVAEngineEventType (Enumeration)
    jnicpp::JClass OVAEngineEventType_class;
    jnicpp::JStaticMethod<jobject> OVAEngineEventType_forValue;

    // com/newisys/ova/OVAAssertEventType (Enumeration)
    jnicpp::JClass OVAAssertEventType_class;
    jnicpp::JStaticMethod<jobject> OVAAssertEventType_forValue;

    // com/newisys/ova/OVAExprType (Enumeration)
    jnicpp::JClass OVAExprType_class;
    jnicpp::JStaticMethod<jobject> OVAExprType_forValue;
    
    // com/newisys/ova/OVASrcFileBlk
    jnicpp::JClass OVASourceFileInfo_class;
    jnicpp::JCtor OVASourceFileInfo_ctor;
    
    // com/newisys/ova/OVAAssertInfo
    jnicpp::JClass OVAAssertInfo_class;
    jnicpp::JCtor OVAAssertInfo_ctor;
#endif // USE_OVA
};

extern JavaObjectCache* pjoc;

#endif // JavaObjectCache_h_included
