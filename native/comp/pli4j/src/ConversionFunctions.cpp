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
 * Contains conversion functions used to convert values between Java and
 * Verilog.
 *
 * \author Trevor Robinson
 */

// module header
#include "ConversionFunctions.h"

// component headers
#include "Utilities.h"
#include "JavaObjectCache.h"

// external component headers
#include "vpicpp.h"

// system headers
#include <sstream>

// using declarations
using namespace jnicpp;
using namespace vpicpp;

jobject createObjectWrapper(const jnicpp::JObject& pliObj, vpiHandle handle)
{
    return pjoc->PLI_createObject.call(pliObj, JArguments()
        << reinterpret_cast<jlong>(handle));
}

jobject getJavaTime(const jnicpp::JEnv& env, const s_vpi_time& time)
{
    jint timeType = time.type;
    jlong simTime = (static_cast<jlong>(time.high) << 32) | time.low;
    jdouble scaledRealTime = time.real;

    return pjoc->PLITime_ctor.create(JArguments()
        << timeType
        << simTime
        << scaledRealTime);
}

jobject getJavaTimeNoThrow(JNIEnv *penv, const s_vpi_time& time) throw()
NO_THROW_PROLOG()
    return getJavaTime(env, time);
NO_THROW_EPILOG(NULL)

jobject getJavaValue(const jnicpp::JEnv& env, vpiHandle obj, const s_vpi_value& value)
{
    switch (value.format) {
        case vpiIntVal:
        {
            return pjoc->Integer_ctor.create(JArguments() << (jint) value.value.integer);
        }
        case vpiRealVal:
        {
            return pjoc->Double_ctor.create(JArguments() << value.value.real);
        }
        case vpiBinStrVal:
        case vpiOctStrVal:
        case vpiDecStrVal:
        case vpiHexStrVal:
        case vpiStringVal:
        {
            return JString::newString(env, value.value.str);
        }
        case vpiScalarVal:
        {
            jobject bit;
            switch (value.value.scalar) {
                case vpi0: bit = pjoc->Bit_ZERO; break;
                case vpi1: bit = pjoc->Bit_ONE; break;
                case vpiZ: bit = pjoc->Bit_Z; break;
                default: bit = pjoc->Bit_X; break;
            }
            return JLocalRef<>::newLocalRef(env, bit);
        }
        case vpiVectorVal:
        {
            jint size = getIntProperty(vpiSize, obj);
            if (size <= 0 || size > 0x4000) {
                std::ostringstream msg;
                msg << "Invalid bit vector size: " << size;
                throw JException(msg.str());
            }

            int count = (size - 1) / 32 + 1;
            JIntArray aval(env, count), bval(env, count);
            {
                JIntArray::elements_type avalElems(aval), bvalElems(bval);
                p_vpi_vecval pvecval = value.value.vector;
                for (jint i = 0; i < count; ++i) {
                    avalElems[i] = pvecval->aval;
                    bvalElems[i] = pvecval->bval;
                    ++pvecval;
                }
            }

            return pjoc->BitVector_ctor.create(JArguments() << aval << bval << size);
        }
        case vpiStrengthVal:
            // TODO
        case vpiTimeVal:
            // TODO
        default:
        {
            std::ostringstream msg;
            msg << "Unsupported value type: " << value.format;
            throw JException(msg.str());
        }
    }
}

jobject getJavaValueNoThrow(JNIEnv *penv, vpiHandle obj, const s_vpi_value& value) throw()
NO_THROW_PROLOG()
    return getJavaValue(env, obj, value);
NO_THROW_EPILOG(NULL)

jobject getJavaValueFromObject(const jnicpp::JEnv& env, vpiHandle obj)
{
    s_vpi_value value;
    value.format = vpiObjTypeVal;
    vpi_get_value(obj, &value);

    return getJavaValue(env, obj, value);
}

void getVerilogValue(const jnicpp::JEnv& env, const jnicpp::JObject& obj, s_vpi_value& value)
{
    value.format = vpiSuppressVal;
    value.value.str = NULL;

    if (obj == NULL) {
        jthrowable exception = throwException(env, pjoc->NullPointerException_ctor,
            "Cannot convert null to a PLI value");
        throw JVMException(env, exception);
    }

    if (obj.isInstanceOf(pjoc->BitVector_class)) {
        JIntArray aval(env, pjoc->BitVector_values.call(obj), true);
        JIntArray bval(env, pjoc->BitVector_xzMask.call(obj), true);
        jint size = pjoc->BitVector_length.call(obj);
        int count = (size - 1) / 32 + 1;

        assert(aval.length() >= count);
        assert(bval.length() >= count);

        p_vpi_vecval pvecval = static_cast<p_vpi_vecval>(
            malloc(sizeof(s_vpi_vecval) * count));

        value.format = vpiVectorVal;
        value.value.vector = pvecval;
        {
            JIntArray::elements_type avalElems(aval), bvalElems(bval);
            for (jint i = 0; i < count; ++i) {
                pvecval->aval = avalElems[i];
                pvecval->bval = bvalElems[i];
                ++pvecval;
            }
        }
        return;
    }

    if (obj.isInstanceOf(pjoc->Bit_class)) {
        int id = pjoc->Bit_getID.call(obj);
        value.format = vpiScalarVal;
        switch (id) {
            case 0: value.value.scalar = vpi0; break;
            case 1: value.value.scalar = vpi1; break;
            case 2: value.value.scalar = vpiZ; break;
            default: value.value.scalar = vpiX; break;
        }
        return;
    }

    if (obj.isInstanceOf(pjoc->Boolean_class)) {
        jboolean boolValue = pjoc->Boolean_booleanValue.call(obj);
        value.format = vpiScalarVal;
        value.value.scalar = boolValue ? vpi1 : vpi0;
        return;
    }

    if (obj.isInstanceOf(pjoc->Integer_class)) {
        value.format = vpiIntVal;
        value.value.integer = pjoc->Integer_intValue.call(obj);
        return;
    }

    if (obj.isInstanceOf(pjoc->Double_class)) {
        value.format = vpiRealVal;
        value.value.real = pjoc->Double_doubleValue.call(obj);
        return;
    }

    if (obj.isInstanceOf(pjoc->String_class)) {
        jstring str = static_cast<jstring>(static_cast<jobject>(obj));
        value.format = vpiStringVal;
        value.value.str = JString::convertToCharArray(env, str);
        return;
    }

    jthrowable exception = throwException(env, pjoc->ClassCastException_ctor,
        "Not a PLI value type");
    throw JVMException(env, exception);
}

bool getVerilogValueNoThrow(JNIEnv *penv, jobject obj, s_vpi_value& value) throw()
NO_THROW_PROLOG()
    JObject object(env, obj, false);
    getVerilogValue(env, object, value);
    return true;
NO_THROW_EPILOG(false)

void freeVerilogValue(s_vpi_value& value) throw()
{
    switch (value.format) {
        case vpiStringVal:
        {
            free(value.value.str);
            value.value.str = NULL;
        }
        case vpiVectorVal:
        {
            free(value.value.vector);
            value.value.vector = NULL;
        }
    }

    value.format = vpiSuppressVal;
}
