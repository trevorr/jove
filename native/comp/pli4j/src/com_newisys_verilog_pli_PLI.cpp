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
 * Implementation of native methods in com.newisys.verilog.pli.PLI.
 *
 * \author Trevor Robinson
 * \author Jon Nall (tf_isynchronize workaround)
 */

#ifdef __CYGWIN__
    // include the __int64 def needed by jni.h
    #include <w32api/basetyps.h>
#endif

// component configuration header
#include "Configuration.h"

// module header
#include "com_newisys_verilog_pli_PLI.h"

// component headers
#include "JavaObjectCache.h"
#include "ConversionFunctions.h"
#include "CallbackHandler.h"
#include "FinishSupport.h"

// external component headers
#include "veriuser.h"
#include "acc_user.h"
#include "jnicpp.h"

// system headers
#include <string>
#include <sstream>

#include <string.h>

// using declarations
using namespace jnicpp;
using std::exception;
using std::string;

// private data
static bool synthError;
static string synthMessage;

static void processArguments(const JEnv& env, const jobject infoObj,
    int argc, char** argv)
{
    while (argc != 0 && *argv != NULL) {
#ifdef VPI_NESTED_ARGV
        if (strcmp(*argv, "-f") != 0) {
#endif
            // normal argument
            jstring arg = JString::newString(env, *argv);
            pjoc->PLIVerilogInfo_addArgument.call(infoObj, JArguments() << arg);
#ifdef VPI_NESTED_ARGV
        } else {
            // -f argument: next argument is pointer to null-terminated
            // char* array containing filename followed by more arguments
            if (argc > 0) --argc; ++argv;
            if (argc != 0 && *argv != NULL) {
                char** nestedptr = reinterpret_cast<char**>(*argv);
                processArguments(env, infoObj, -1, nestedptr + 1);
            }
        }
#endif
        if (argc > 0) --argc; ++argv;
    }
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    getInfo
 * Signature: ()Lcom/newisys/verilog/pli/PLIVerilogInfo;
 */
JNIEXPORT jobject JNICALL Java_com_newisys_verilog_pli_PLI_getInfo
  (JNIEnv *penv, jobject ths)
{
    // clear synthetic error flag
    synthError = false;

    // get simulator info
    s_vpi_vlog_info info;
    if (!vpi_get_vlog_info(&info)) {
        return NULL;
    }

    JEnv env(penv);

    // build Java strings for product and version
    jstring product = JString::newString(env, info.product);
    jstring version = JString::newString(env, info.version);

    // construct a PLIVerilogInfo object
    jobject infoObj = pjoc->PLIVerilogInfo_ctor.create(JArguments()
        << product
        << version);

    // add command line arguments to info object
    processArguments(env, infoObj, info.argc, info.argv);

    return infoObj;
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    registerCallback0
 * Signature: (Lcom/newisys/verilog/pli/PLIVerilogCallback;IIJDJI)J
 */
JNIEXPORT jlong JNICALL Java_com_newisys_verilog_pli_PLI_registerCallback0
  (JNIEnv *penv, jobject ths, jobject callback, jint reason,
  jint timeType, jlong longTime, jdouble doubleTime, jlong lhandleObj, jint valueType)
{
    // clear synthetic error flag
    synthError = false;

    // allocate callback info object whose address will serve as the user
    // callback handle
    CallbackInfo* info_ptr = new CallbackInfo(penv, callback, reason);

    bool success;
#ifdef USE_TF_SYNCHRONIZE
    if (reason == cbReadWriteSynch) {
        // WORKAROUND: VCS (7.1.1R21) seems to schedule VPI R/W synch
        // callbacks after non-blocking assignment updates instead of
        // before, as it does for the TF R/W synch

        // schedule a synch callback if we are the first in the queue
        if (rwSynchCallbacks.empty()) {
            int result = tf_isynchronize(misctfInstance);
            success = (result == 0);
            if (!success) {
                synthError = true;
                std::ostringstream msg;
                msg << "tf_isynchronize failed with result " << result;
                synthMessage = msg.str();
            }
        } else {
            // we should be able to assume success here, since the
            // presence of queued callbacks indicates we are in the
            // proper state to schedule more (i.e. not in R/O synch)
            success = true;
        }

        if (success) {
            // add callback info to synch queue
            rwSynchCallbacks.push_back(info_ptr);
        }
    } else
#endif
    {
        s_vpi_time time;
        time.type = timeType;
        time.high = longTime >> 32;
        time.low = longTime;
        time.real = doubleTime;

        s_vpi_value value;
        value.format = valueType;
        value.value.str = NULL;

        s_cb_data cb_data;
        cb_data.reason = reason;
        cb_data.cb_rtn = pli4j_cb_rtn;
        cb_data.obj = reinterpret_cast<vpiHandle>(lhandleObj);
        cb_data.time = &time;
        cb_data.value = &value;
        cb_data.index = 0;
        cb_data.user_data = reinterpret_cast<PLI_BYTE8*>(info_ptr);

#ifdef PLI_DEBUG
        cout << "vpi_register_cb: reason=" << reason;
#endif
        vpiHandle cb_handle = vpi_register_cb(&cb_data);
#ifdef PLI_DEBUG
        cout << ", result=" << cb_handle << endl;
#endif

        if (cb_handle != NULL) {
            info_ptr->setHandle(cb_handle);
            success = true;
        } else {
            success = false;
        }
    }

    // if not successful, delete the info object and return null
    if (!success) {
        delete info_ptr;
        info_ptr = NULL;
    }

    return reinterpret_cast<jlong>(info_ptr);
}

static CallbackInfo* getCallbackInfo(jlong info_handle)
{
    CallbackInfo* info_ptr = reinterpret_cast<CallbackInfo*>(info_handle);
    assert(info_ptr != NULL);
    info_ptr->assertValid();
    return info_ptr;
}

static void deleteCallbackInfo(CallbackInfo* info_ptr)
{
#ifdef PLI_DEBUG
    cout << "deleteCallbackInfo: " << info_ptr->getHandle() << endl;
#endif
    delete info_ptr;
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    cancelCallback0
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_newisys_verilog_pli_PLI_cancelCallback0
  (JNIEnv *penv, jobject ths, jlong info_handle)
{
    // clear synthetic error flag
    synthError = false;

    CallbackInfo* info_ptr = getCallbackInfo(info_handle);

    jboolean result;
#ifdef USE_TF_SYNCHRONIZE
    if (info_ptr->getReason() == cbReadWriteSynch) {
        rwSynchCallbacks.remove(info_ptr);
        result = JNI_TRUE;
    } else
#endif
    {
        vpiHandle cb_handle = info_ptr->getHandle();
        assert(cb_handle != NULL);
#ifdef PLI_DEBUG
        cout << "vpi_remove_cb: " << cb_handle << endl;
#endif
        result = vpi_remove_cb(cb_handle) ? JNI_TRUE : JNI_FALSE;
    }

    deleteCallbackInfo(info_ptr);

    return result;
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    releaseCallback
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_newisys_verilog_pli_PLI_releaseCallback
  (JNIEnv *penv, jobject ths, jlong info_handle)
{
    // clear synthetic error flag
    synthError = false;

    CallbackInfo* info_ptr = getCallbackInfo(info_handle);

#ifdef USE_TF_SYNCHRONIZE
    if (info_ptr->getReason() == cbReadWriteSynch) {
        rwSynchCallbacks.remove(info_ptr);
    } else
#endif
    {
        vpiHandle cb_handle = info_ptr->getHandle();
        assert(cb_handle != NULL);
#ifdef PLI_DEBUG
        cout << "vpi_free_object: handle=" << cb_handle;
#endif
        int result = vpi_free_object(cb_handle);
#ifdef PLI_DEBUG
        cout << ", result=" << result << endl;
#endif
    }

    deleteCallbackInfo(info_ptr);
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    getHandle0
 * Signature: (IJ)J
 */
JNIEXPORT jlong JNICALL Java_com_newisys_verilog_pli_PLI_getHandle0
  (JNIEnv *penv, jobject ths, jint type, jlong lhandleRef)
{
    // clear synthetic error flag
    synthError = false;

    vpiHandle ref_handle = reinterpret_cast<vpiHandle>(lhandleRef);
#ifdef PLI_DEBUG
    cout << "vpi_handle: ref_handle=" << ref_handle;
#endif
    vpiHandle handle = vpi_handle(type, ref_handle);
#ifdef PLI_DEBUG
    cout << ", result=" << ref_handle << endl;
#endif
    return reinterpret_cast<jlong>(handle);
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    getHandleByName0
 * Signature: (Ljava/lang/String;J)J
 */
JNIEXPORT jlong JNICALL Java_com_newisys_verilog_pli_PLI_getHandleByName0
  (JNIEnv *penv, jobject ths, jstring name, jlong lhandleScope)
{
    // clear synthetic error flag
    synthError = false;

    std::string nameStr(JString::convertToString(penv, name));
    const char* pname = nameStr.c_str();
    vpiHandle scope_handle = reinterpret_cast<vpiHandle>(lhandleScope);
#ifdef PLI_DEBUG
    cout << "vpi_handle_by_name: name=" << pname << ", scope_handle=" << scope_handle;
#endif
    vpiHandle handle = vpi_handle_by_name(const_cast<char*>(pname), scope_handle);
#ifdef PLI_DEBUG
    cout << ", result=" << handle << endl;
#endif
    return reinterpret_cast<jlong>(handle);
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    getHandleMulti0
 * Signature: (IJJ)J
 */
JNIEXPORT jlong JNICALL Java_com_newisys_verilog_pli_PLI_getHandleMulti0
  (JNIEnv *penv, jobject ths, jint type, jlong lhandle1, jlong lhandle2)
{
    // clear synthetic error flag
    synthError = false;

    vpiHandle handle1 = reinterpret_cast<vpiHandle>(lhandle1);
    vpiHandle handle2 = reinterpret_cast<vpiHandle>(lhandle2);
#ifdef PLI_DEBUG
    cout << "vpi_handle_multi: " << handle1 << ", " << handle2;
#endif
    vpiHandle handle = vpi_handle_multi(type, handle1, handle2);
#ifdef PLI_DEBUG
    cout << ", result=" << handle << endl;
#endif
    return reinterpret_cast<jlong>(handle);
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    getHandleByIndex0
 * Signature: (JI)J
 */
JNIEXPORT jlong JNICALL Java_com_newisys_verilog_pli_PLI_getHandleByIndex0
  (JNIEnv *penv, jobject ths, jlong lhandleParent, jint index)
{
    // clear synthetic error flag
    synthError = false;

    vpiHandle parent_handle = reinterpret_cast<vpiHandle>(lhandleParent);
#ifdef PLI_DEBUG
    cout << "vpi_handle_by_index: parent_handle=" << parent_handle << ", index=" << index;
#endif
    vpiHandle handle = vpi_handle_by_index(parent_handle, index);
#ifdef PLI_DEBUG
    cout << ", result=" << handle << endl;
#endif
    return reinterpret_cast<jlong>(handle);
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    getHandleByMultiIndex0
 * Signature: (J[I)J
 */
JNIEXPORT jlong JNICALL Java_com_newisys_verilog_pli_PLI_getHandleByMultiIndex0
  (JNIEnv *penv, jobject ths, jlong lhandleParent, jintArray indices)
{
    // clear synthetic error flag
    synthError = false;

    JIntArray indicesArray(penv, indices, false);
    JIntArray::elements_type indicesElements(indicesArray);
    vpiHandle parent_handle = reinterpret_cast<vpiHandle>(lhandleParent);
#ifdef PLI_DEBUG
    cout << "vpi_handle_by_multi_index: parent_handle=" << parent_handle;
#endif

    // jint's should always be the same size as PLI_INT32, but
    // some JVM's (OSX) declare them as longs, so we'll explcitly
    // pass a PLI_INT32* of the values. This array is allocated on
    // the stack under the assumption that the simulator will be done
    // with it once vpi_handle_by_multi_index returns
    PLI_INT32 pliIndices[indicesArray.length()];
    for(jint i = 0; i < indicesArray.length(); ++i)
    {
        pliIndices[i] = (PLI_INT32) indicesElements[i];
    }

    vpiHandle handle = vpi_handle_by_multi_index(
        parent_handle,
        indicesArray.length(),
        pliIndices);
#ifdef PLI_DEBUG
    cout << ", result=" << handle << endl;
#endif
    return reinterpret_cast<jlong>(handle);
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    iterate0
 * Signature: (IJ)J
 */
JNIEXPORT jlong JNICALL Java_com_newisys_verilog_pli_PLI_iterate0
  (JNIEnv *penv, jobject ths, jint type, jlong lhandleRef)
{
    // clear synthetic error flag
    synthError = false;

    vpiHandle ref_handle = reinterpret_cast<vpiHandle>(lhandleRef);
#ifdef PLI_DEBUG
    cout << "vpi_iterate: ref_handle=" << ref_handle;
#endif
    vpiHandle handle = vpi_iterate(type, ref_handle);
#ifdef PLI_DEBUG
    cout << ", result=" << handle << endl;
#endif
    return reinterpret_cast<jlong>(handle);
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    scan0
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_newisys_verilog_pli_PLI_scan0
  (JNIEnv *penv, jobject ths, jlong lhandleIter)
{
    // clear synthetic error flag
    synthError = false;

    vpiHandle iter_handle = reinterpret_cast<vpiHandle>(lhandleIter);
#ifdef PLI_DEBUG
    cout << "vpi_scan: iter_handle=" << iter_handle;
#endif
    vpiHandle handle = vpi_scan(iter_handle);
#ifdef PLI_DEBUG
    cout << ", result=" << handle << endl;
#endif
    return reinterpret_cast<jlong>(handle);
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    compareObjects
 * Signature: (JJ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_newisys_verilog_pli_PLI_compareObjects
  (JNIEnv *penv, jobject ths, jlong lhandle1, jlong lhandle2)
{
    // clear synthetic error flag
    synthError = false;

    vpiHandle handle1 = reinterpret_cast<vpiHandle>(lhandle1);
    vpiHandle handle2 = reinterpret_cast<vpiHandle>(lhandle2);
#ifdef PLI_DEBUG
    cout << "vpi_compare_objects: handle1=" << handle1 << ", handle2=" << handle2;
#endif
    jboolean result = vpi_compare_objects(handle1, handle2);
#ifdef PLI_DEBUG
    cout << ", result=" << result << endl;
#endif
    return result;
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    freeObject
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_newisys_verilog_pli_PLI_freeObject
  (JNIEnv *penv, jobject ths, jlong lhandle)
{
    // clear synthetic error flag
    synthError = false;

    vpiHandle handle = reinterpret_cast<vpiHandle>(lhandle);
#ifdef PLI_DEBUG
    cout << "vpi_free_object: handle=" << handle;
#endif
    int result = vpi_free_object(handle);
#ifdef PLI_DEBUG
    cout << ", result=" << result << endl;
#endif
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    getPropInt
 * Signature: (IJ)I
 */
JNIEXPORT jint JNICALL Java_com_newisys_verilog_pli_PLI_getPropInt
  (JNIEnv *penv, jobject ths, jint prop, jlong lhandle)
{
    // clear synthetic error flag
    synthError = false;

    vpiHandle handle = reinterpret_cast<vpiHandle>(lhandle);
#ifdef PLI_DEBUG
    cout << "vpi_get: prop=" << prop << ", handle=" << handle;
#endif
    jint result = vpi_get(prop, handle);
#ifdef PLI_DEBUG
    cout << ", result=" << result << endl;
#endif
    return result;
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    getPropStr
 * Signature: (IJ)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_newisys_verilog_pli_PLI_getPropStr
  (JNIEnv *penv, jobject ths, jint prop, jlong lhandle)
{
    // clear synthetic error flag
    synthError = false;

    vpiHandle handle = reinterpret_cast<vpiHandle>(lhandle);
#ifdef PLI_DEBUG
    cout << "vpi_get_str: prop=" << prop << ", handle=" << handle;
#endif
    const char* str = vpi_get_str(prop, handle);
#ifdef PLI_DEBUG
    cout << ", result=" << str << endl;
#endif
    return JString::newString(penv, str);
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    getValue0
 * Signature: (JI)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_com_newisys_verilog_pli_PLI_getValue0
  (JNIEnv *penv, jobject ths, jlong lhandle, jint format)
{
    // clear synthetic error flag
    synthError = false;

    vpiHandle handle = reinterpret_cast<vpiHandle>(lhandle);
    s_vpi_value value;
    value.format = format;
    value.value.str = NULL;
#ifdef PLI_DEBUG
    cout << "vpi_get_value: handle=" << handle << ", format=" << format << endl;
#endif
    vpi_get_value(handle, &value);
    if (!vpi_chk_error(NULL)) {
        return getJavaValueNoThrow(penv, handle, value);
    }
    return NULL;
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    putValue0
 * Signature: (JLjava/lang/Object;IJDI)J
 */
JNIEXPORT jlong JNICALL Java_com_newisys_verilog_pli_PLI_putValue0
  (JNIEnv *penv, jobject ths, jlong lhandle, jobject valueObj,
  jint timeType, jlong longTime, jdouble doubleTime, jint flags)
{
    // clear synthetic error flag
    synthError = false;

    jlong returnVal;

    s_vpi_value value;
    p_vpi_value pvalue = NULL;
    if (valueObj != NULL) {
        if (!getVerilogValueNoThrow(penv, valueObj, value)) {
            freeVerilogValue(value);
            return 0;
        }
        pvalue = &value;
    }

    s_vpi_time time;
    time.type = timeType;
    time.high = longTime >> 32;
    time.low = longTime;
    time.real = doubleTime;

    vpiHandle handle = reinterpret_cast<vpiHandle>(lhandle);
#ifdef PLI_DEBUG
    cout << "vpi_put_value: handle=" << handle << endl;
#endif
    vpiHandle event_handle = vpi_put_value(handle, pvalue, &time, flags);

    if (valueObj != NULL) {
        freeVerilogValue(value);
    }

    // If a return event is requested, a NULL value means an error
    // occurred. However, if no return event is requested, the error
    // status must be queried explicitly. Do that query here to avoid
    // an extra call into JNI from the java code.
    if ((flags & vpiReturnEvent) == 0)
    {
        // check if an error occurred. if so, return ~0L
        // if not, return whatever event_handle VCS gave us
        // (should be null)
        s_vpi_error_info info;
        if (vpi_chk_error(&info))
        {
            returnVal = ~0;
        }
        else
        {
            returnVal = reinterpret_cast<jlong>(event_handle);
        }
    }
    else
    {
        returnVal = reinterpret_cast<jlong>(event_handle);
    }

#ifdef PLI_DEBUG
    cout << "  vpi_put_value result=" << returnVal << endl;
#endif

    return returnVal;
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    releaseForce0
 * Signature: (J)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_com_newisys_verilog_pli_PLI_releaseForce0
  (JNIEnv *penv, jobject ths, jlong lhandle)
{
    // clear synthetic error flag
    synthError = false;

    vpiHandle handle = reinterpret_cast<vpiHandle>(lhandle);
    s_vpi_value value;
    value.format = vpiObjTypeVal;
    value.value.str = NULL;
#ifdef PLI_DEBUG
    cout << "vpi_put_value(release): handle=" << handle << endl;
#endif
    vpi_put_value(handle, &value, NULL, vpiReleaseFlag);
    if (!vpi_chk_error(NULL)) {
        return getJavaValueNoThrow(penv, handle, value);
    }
    return NULL;
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    getTime
 * Signature: (IJ)Lcom/newisys/verilog/pli/PLITime;
 */
JNIEXPORT jobject JNICALL Java_com_newisys_verilog_pli_PLI_getTime
  (JNIEnv *penv, jobject ths, jint timeType, jlong lhandleScope)
{
    // clear synthetic error flag
    synthError = false;

    vpiHandle scope_handle = reinterpret_cast<vpiHandle>(lhandleScope);
    s_vpi_time time;
    time.type = timeType;
#ifdef PLI_DEBUG
    cout << "vpi_get_time: scope_handle=" << scope_handle << endl;
#endif
    vpi_get_time(scope_handle, &time);
    return getJavaTimeNoThrow(penv, time);
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    print0
 * Signature: ([BII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_newisys_verilog_pli_PLI_print0
  (JNIEnv *penv, jobject ths, jbyteArray b, jint off, jint len)
{
    // clear synthetic error flag
    synthError = false;

    char* buf = NULL;
    try {
        JEnv env(penv);
        JByteArray arr(env, b, true);
        JByteArray::elements_type arrElems(arr);

        buf = static_cast<char*>(malloc(len + 1));
        memcpy(buf, &arrElems[off], len);
        buf[len] = 0;

#ifdef PLI_DEBUG
        jboolean result = vpi_printf("|%d,%d|%s", off, len, buf) != EOF;
#else
        jboolean result = vpi_printf("%s", buf) != EOF;
#endif

        free(buf);
        return result;
    }
    catch (...) {
        if (buf != NULL) {
            free(buf);
        }
        return JNI_FALSE;
    }
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    flush0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_newisys_verilog_pli_PLI_flush0
  (JNIEnv *penv, jobject ths)
{
    // clear synthetic error flag
    synthError = false;

    int result = vpi_flush();
#ifdef PLI_DEBUG
    cout << "vpi_flush: result=" << result << endl;
#endif
    return result == 0;
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    stop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_newisys_verilog_pli_PLI_stop
  (JNIEnv *penv, jobject ths)
{
    // clear synthetic error flag
    synthError = false;

#ifdef NO_VPI_CONTROL
    #ifdef PLI_DEBUG
    cout << "tf_dostop" << endl;
    #endif
    tf_dostop();
#else
    #ifdef PLI_DEBUG
    cout << "vpi_control(vpiStop)" << endl;
    #endif
    vpi_control(vpiStop, 1);
#endif
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    finish
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_newisys_verilog_pli_PLI_finish
  (JNIEnv *penv, jobject ths)
{
    // clear synthetic error flag
    synthError = false;

    // some simulators (e.g. VCS) do not return from vpi_control(vpiFinish)
    // or tf_dofinish(), and simply call any end of simulation callbacks and
    // exit; however, this is a problem for us because we want to destroy
    // the JVM at the end of the simulation, which is impossible to do now
    // since we were called from JVM code; as a workaround, we flag that
    // finish has been called, but wait to act on it until all JVM calls
    // have returned
    finishCalled();
}

/*
 * Class:     com_newisys_verilog_pli_PLI
 * Method:    getError
 * Signature: ()Lcom/newisys/verilog/pli/PLI$ErrorInfo;
 */
JNIEXPORT jobject JNICALL Java_com_newisys_verilog_pli_PLI_getError
  (JNIEnv *penv, jobject ths)
{
    s_vpi_error_info info;

    // get synthetic error info
    if (synthError) {
        info.state = vpiRun;
        info.level = vpiError;
        info.message = const_cast<char*>(synthMessage.c_str());
        info.product = acc_product_version();
        info.code = NULL;
        info.file = NULL;
        info.line = 0;
    }
    // get VPI error info
    else if (!vpi_chk_error(&info)) {
        return NULL;
    }

    JEnv env(penv);

    // build Java strings for each error info string
    jstring message = JString::newString(env, info.message);
    jstring product = JString::newString(env, info.product);
    jstring code = JString::newString(env, info.code);
    jstring file = JString::newString(env, info.file);

    // construct an ErrorInfo object
    JClass infoClass(env, "com/newisys/verilog/pli/PLI$ErrorInfo");
    JCtor infoCtor(infoClass, "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V");
    jobject infoObj = infoCtor.create(JArguments()
        << (jint) info.state
        << (jint) info.level
        << message
        << product
        << code
        << file
        << (jint) info.line);

    return infoObj;
}

