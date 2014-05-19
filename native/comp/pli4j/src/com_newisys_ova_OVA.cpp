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
 * Implementation of native methods in com.newisys.ova.OVA.
 *
 * \author Jon Nall
 */

#ifdef __CYGWIN__
    // include the __int64 def needed by jni.h
    #include <w32api/basetyps.h>
#endif

#include "com_newisys_ova_OVA.h"
#include "Configuration.h"
#include "CallbackHandler.h"
#include "JavaObjectCache.h"
#include "Utilities.h"
#include "FinishSupport.h"
#include "jnicpp.h"
#include "OVAApi.h"

#include <map>
#include <sstream>

using namespace jnicpp;
using std::exception;

#ifdef USE_OVA
typedef std::map<jlong, jlong*> OVACountMap;
OVACountMap ovaSuccessCountMap;
OVACountMap ovaFailureCountMap;
#endif // USE_OVA

/*
 * Class:     com_newisys_ova_OVA
 * Method:    isSupported
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_newisys_ova_OVA_isSupported
  (JNIEnv *penv)
{
    // This is not an actual OVA API. Rather, it's always compiled
    // in and available to Jove

#ifdef OVA_DEBUG
    cout << "ovaIsSupported()";
#endif // OVA_DEBUG
    
    bool status = false;
#ifdef USE_OVA
    status = true;
#endif
    
#ifdef OVA_DEBUG
    cout << ", status = " << (status ? "OK" : "ERROR") << endl;
#endif // OVA_DEBUG
    return (status) ? JNI_TRUE : JNI_FALSE;
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    getApiVersion
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_newisys_ova_OVA_getApiVersion
  (JNIEnv *penv, jobject ths)
{
#ifdef USE_OVA

#ifdef OVA_DEBUG
    cout << "ovaGetApiVersion()";
#endif // OVA_DEBUG
    Ova_String ovaVersion = ovaGetApiVersion();
#ifdef OVA_DEBUG
    cout << ", version = " << ovaVersion << endl;
#endif // OVA_DEBUG
    return JString::newString(penv, ovaVersion);
    
#else
    return NULL;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    registerClient
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_newisys_ova_OVA_registerClient
  (JNIEnv *penv, jobject ths)
{
#ifdef USE_OVA

#ifdef OVA_DEBUG
    cout << "ovaRegisterClient()";
#endif // OVA_DEBUG
    Ova_ClientID id = ovaRegisterClient();
#ifdef OVA_DEBUG
    cout << ", clientID = " << id << endl;
#endif
    return (jlong)id;

#else // OVA_DEBUG
    return 0;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    setConfigSwitch
 * Signature: (JIZ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_newisys_ova_OVA_setConfigSwitch
  (JNIEnv *penv, jobject ths, jlong clientID, jint confSwitch, jboolean enable)
{
#ifdef USE_OVA

#ifdef OVA_DEBUG
    cout << "ovaSetConfigSwitch(" << clientID << ", " << confSwitch << ",  "
        << (enable == JNI_TRUE ? "true" : "false") << ")";
#endif // OVA_DEBUG
    Ova_Bool ovaEnable = (enable == JNI_TRUE) ? OVA_TRUE : OVA_FALSE;
    Ova_Bool status = ovaSetConfigSwitch(clientID, (Ova_ConfigSwitch)confSwitch, ovaEnable);
#ifdef OVA_DEBUG
    cout << ", status = " << (status ? "OK" : "ERROR") << endl;
#endif // OVA_DEBUG
    return (status == OVA_TRUE) ? JNI_TRUE : JNI_FALSE;

#else
    return JNI_FALSE;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    doAction
 * Signature: (JIJ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_newisys_ova_OVA_doAction
  (JNIEnv *penv, jobject ths, jlong clientID, jint eventID, jlong udRef)
{
#ifdef USE_OVA

#ifdef OVA_DEBUG
    cout << "ovaDoAction(" << clientID << ", " << eventID << ", " << udRef << ")";
#endif // OVA_DEBUG
    Ova_Bool status = ovaDoAction(clientID, (Ova_EngAction)eventID, (const void*)udRef);
#ifdef OVA_DEBUG
    cout << ", status = " << (status ? "OK" : "ERROR") << endl;
#endif // OVA_DEBUG
    return (status == OVA_TRUE) ? JNI_TRUE : JNI_FALSE;

#else
    return JNI_FALSE;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    firstAssert
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_newisys_ova_OVA_firstAssert
  (JNIEnv *penv, jobject ths, jlong clientID)
{
#ifdef USE_OVA

#ifdef OVA_DEBUG
    cout << "ovaFirstAssert(" << clientID << ")";
#endif // OVA_DEBUG
    Ova_AssertID id = ovaFirstAssert(clientID);
#ifdef OVA_DEBUG
    cout << ", id = " << id << endl;
#endif // OVA_DEBUG
    return (jlong)id;

#else
    return 0;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    nextAssert
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_newisys_ova_OVA_nextAssert
  (JNIEnv *penv, jobject ths, jlong clientID)
{
#ifdef USE_OVA
#ifdef OVA_DEBUG
    cout << "ovaNextAssert(" << clientID << ")";
#endif // OVA_DEBUG
    Ova_AssertID id = ovaNextAssert(clientID);
#ifdef OVA_DEBUG
    cout << ", id = " << id << endl;
#endif // OVA_DEBUG
    return (jlong)id;

#else
    return 0;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    assertDoAction
 * Signature: (JIJJJ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_newisys_ova_OVA_assertDoAction
  (JNIEnv *penv, jobject ths, jlong clientID, jint eventID, jlong assertionID, 
   jlong attemptID, jlong udRef)
{
#ifdef USE_OVA
#ifdef OVA_DEBUG
    cout << "ovaAssertDoAction(" << clientID << ", " << eventID << ", " << 
        assertionID << ", " << attemptID << ", " << udRef << ")";
#endif // OVA_DEBUG
    Ova_Bool status = ovaAssertDoAction(clientID, (Ova_AssertAction)eventID,
            assertionID, attemptID, (const void*)udRef);
#ifdef OVA_DEBUG
    cout << ", status = " << (status ? "OK" : "ERROR") << endl;
#endif // OVA_DEBUG
    return (status == OVA_TRUE) ? JNI_TRUE : JNI_FALSE;

#else
    return JNI_FALSE;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    hasAssertInfo
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_newisys_ova_OVA_hasAssertInfo
  (JNIEnv *penv, jobject ths, jlong clientID)
{
#ifdef USE_OVA

#ifdef OVA_DEBUG
    cout << "ovaHasSyntaxInfo(" << clientID << ")";
#endif // OVA_DEBUG
    Ova_Bool status = ovaHasSyntaxInfo(clientID);
#ifdef OVA_DEBUG
    cout << ", status = " << (status ? "OK" : "ERROR") << endl;
#endif // OVA_DEBUG
    return (status == OVA_TRUE) ? JNI_TRUE : JNI_FALSE;

#else
    return JNI_FALSE;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    getAssertInfo
 * Signature: (JJ)Lcom/newisys/ova/OVAAssertInfo;
 */
JNIEXPORT jobject JNICALL Java_com_newisys_ova_OVA_getAssertInfo
  (JNIEnv *penv, jobject ths, jlong clientID, jlong assertID)
{
#ifdef USE_OVA

#ifdef OVA_DEBUG
    cout << "ovaGetAssertSyntaxInfo(" << clientID << ", " << assertID << ")";
#endif // OVA_DEBUG
    if(ovaHasSyntaxInfo(clientID) == OVA_FALSE)
    {
        die("Simulator doesn't support querying OVA assert syntax info");
    }

    Ova_AssertSyntaxInfo info = ovaGetAssertSyntaxInfo(clientID, assertID);
    assert(info != NULL);
#ifdef OVA_DEBUG
    cout << ", name = " << info->name << ", exprType: " << info->exprType <<
        ", severity: " << info->severity << ", category: " << info->category <<
        ", scopeName: " << info->scopeName << ", userMsg: " <<
        ((info->userMsg == NULL) ? "(NULL)" : info->userMsg) << endl;
#endif // OVA_DEBUG

    try
    {
        // Create a new OVAAssertSyntaxInfo object
        // 1. Create the ExprType
        enterJava();
        jobject exprType = pjoc->OVAExprType_forValue.call(JArguments() << (jint)info->exprType);
        exitJava();
        assert(exprType != NULL);

        // 2. Create the SrcFileBlk
        assert(info->srcBlock != NULL);
        enterJava();
        jobject srcFileBlk = pjoc->OVASourceFileInfo_ctor.create(JArguments()
                << JString::newString(penv, info->srcBlock->fileName)
                << (jint)info->srcBlock->startRow
                << (jint)info->srcBlock->startColumn
                << (jint)info->srcBlock->endRow
                << (jint)info->srcBlock->endColumn);
        exitJava();
        // 3. Create the AssertSyntaxInfo object
        enterJava();
        jobject syntaxInfo = pjoc->OVAAssertInfo_ctor.create(JArguments()
                << JString::newString(penv, info->name)
                << exprType
                << srcFileBlk
                << (jint)info->severity
                << (jint)info->category
                << JString::newString(penv, info->scopeName)
                << JString::newString(penv, info->userMsg));
        exitJava();
        return syntaxInfo;
    }
    catch(JVMException& e)
    {
        die(e);
    } 

#else
    return NULL;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    addEngineListener
 * Signature: (JI)Z
 */
JNIEXPORT jboolean JNICALL Java_com_newisys_ova_OVA_addEngineListener
  (JNIEnv *penv, jobject ths, jlong clientID, jint eventID)
{
#ifdef USE_OVA

#ifdef OVA_DEBUG
    cout << "ovaAddEngListener(" << clientID << ", " << eventID << ")";
#endif // OVA_DEBUG
    Ova_Bool status = ovaAddEngListener(clientID, (Ova_EngEvent)eventID, pli4j_ovaeng_cb_rtn, NULL);
#ifdef OVA_DEBUG
    cout << ", status = " << (status ? "OK" : "ERROR") << endl;
#endif // OVA_DEBUG
    return (status == OVA_TRUE) ? JNI_TRUE : JNI_FALSE;

#else
    return JNI_FALSE;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    addAssertListener
 * Signature: (JIJ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_newisys_ova_OVA_addAssertListener
  (JNIEnv *penv, jobject ths, jlong clientID, jint eventID, jlong assertID)
{
#ifdef USE_OVA

#ifdef OVA_DEBUG
    cout << "ovaAddAssertListener(" << clientID << ", " << eventID << 
        ", " << assertID << ")";
#endif // OVA_DEBUG
    Ova_Bool status = ovaAddAssertListener(clientID, (Ova_AssertEvent)eventID,
            (Ova_AssertID)assertID, pli4j_ovaassert_cb_rtn, NULL);
#ifdef OVA_DEBUG
    cout << ", status = " << (status ? "OK" : "ERROR") << endl;
#endif // OVA_DEBUG
    return (status == OVA_TRUE) ? JNI_TRUE : JNI_FALSE;

#else
    return JNI_FALSE;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    removeEngineListener
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_newisys_ova_OVA_removeEngineListener
  (JNIEnv *penv, jobject ths, jint eventID)
{
#ifdef USE_OVA

#ifdef OVA_DEBUG
    cout << "ovaRemoveEngListener(" << eventID << ")";
#endif // OVA_DEBUG
    Ova_Bool status = ovaRemoveEngListener((Ova_EngEvent)eventID, NULL);
#ifdef OVA_DEBUG
    cout << ", status = " << (status ? "OK" : "ERROR") << endl;
#endif // OVA_DEBUG
    return (status == OVA_TRUE) ? JNI_TRUE : JNI_FALSE;

#else
    return JNI_FALSE;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    removeAssertListener
 * Signature: (IJ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_newisys_ova_OVA_removeAssertListener
  (JNIEnv *penv, jobject ths, jint eventID, jlong assertID)
{
#ifdef USE_OVA

#ifdef OVA_DEBUG
    cout << "ovaRemoveAssertListener(" << eventID << ", " << assertID << ")";
#endif // OVA_DEBUG
    Ova_Bool status = ovaRemoveAssertListener((Ova_AssertEvent)eventID,
            (Ova_AssertID)assertID, NULL);
#ifdef OVA_DEBUG
    cout << ", status = " << (status ? "OK" : "ERROR") << endl;
#endif // OVA_DEBUG
    return (status == OVA_TRUE) ? JNI_TRUE : JNI_FALSE;

#else
    return JNI_FALSE;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    enableAssertCount
 * Signature: (JJI)Z
 */
JNIEXPORT jboolean JNICALL Java_com_newisys_ova_OVA_enableAssertCount
  (JNIEnv *env, jobject ths, jlong clientID, jlong assertID, jint eventID)
{
#ifdef USE_OVA

#ifdef OVA_DEBUG
    cout << "ovaEnableAssertCount(" << clientID << ", " << assertID << 
        ", " << eventID << ")";
#endif // OVA_DEBUG
    assert(eventID == OvaAttemptSuccessAssertE || eventID == OvaAttemptFailureAssertE);
    jboolean returnVal = JNI_FALSE;
    
    // register a callback on the event type (if one is not already registered).
    if(eventID == OvaAttemptSuccessAssertE)
    {
        if(ovaSuccessCountMap[assertID] == NULL)
        {
            jlong* count = new jlong(0);
            if(count == NULL)
            {
                returnVal = JNI_FALSE;
            }
            else
            {
                *count = 0;
                ovaSuccessCountMap[assertID] = count;
                returnVal |= ovaAddAssertListener(clientID, (Ova_AssertEvent)eventID, assertID,
                        pli4j_ovaassert_count_cb_rtn, count);
            }    
        }
        else
        {
            *(ovaSuccessCountMap[assertID]) = 0;
            returnVal = JNI_TRUE;
        }
    }
    else{
        if(ovaFailureCountMap[assertID] == NULL)
        {
            jlong* count = new jlong(0);
            if(count == NULL)
            {
                returnVal = JNI_FALSE;
            }
            else
            {
                *count = 0;
                ovaSuccessCountMap[assertID] = count;
                returnVal |= ovaAddAssertListener(clientID, (Ova_AssertEvent)eventID, assertID,
                        pli4j_ovaassert_count_cb_rtn, count);
            }    
        }
        else
        {
            *(ovaFailureCountMap[assertID]) = 0;
            returnVal = JNI_TRUE;
        }
    }

#ifdef OVA_DEBUG
    cout << ", status = " << ((returnVal == JNI_TRUE) ? "OK" : "ERROR") << endl;
#endif // OVA_DEBUG
    return returnVal;

#else
    return JNI_FALSE;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    disableAssertCount
 * Signature: (JJI)Z
 */
JNIEXPORT jboolean JNICALL Java_com_newisys_ova_OVA_disableAssertCount
  (JNIEnv *env, jobject ths, jlong clientID, jlong assertID, jint eventID)
{
#ifdef USE_OVA

#ifdef OVA_DEBUG
    cout << "ovaDisableAssertCount(" << clientID << ", " << assertID << 
        ", " << eventID << ")";
#endif // OVA_DEBUG
    assert(eventID == OvaAttemptSuccessAssertE || eventID == OvaAttemptFailureAssertE);
    jboolean returnVal = JNI_FALSE;
    
    // remove the callback on the event type
    if(eventID == OvaAttemptSuccessAssertE)
    {
        if(ovaSuccessCountMap[assertID] != NULL)
        {
            returnVal |= ovaRemoveAssertListener((Ova_AssertEvent)eventID, assertID, NULL);
        }
    }
    else{
        if(ovaFailureCountMap[assertID] != NULL)
        {
            returnVal |= ovaRemoveAssertListener((Ova_AssertEvent)eventID, assertID, NULL);
        }
    }
    
#ifdef OVA_DEBUG
    cout << ", status = " << ((returnVal == JNI_TRUE) ? "OK" : "ERROR") << endl;
#endif // OVA_DEBUG
    return returnVal;

#else
    return JNI_FALSE;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    getAssertCount
 * Signature: (JJI)J
 */
JNIEXPORT jlong JNICALL Java_com_newisys_ova_OVA_getAssertCount
  (JNIEnv *penv, jobject ths, jlong clientID, jlong assertID, jint eventID)
{
#ifdef USE_OVA
#ifdef OVA_DEBUG
    cout << "ovaGetAssertCount(" << clientID << ", " << assertID << 
        ", " << eventID << ")";
#endif // OVA_DEBUG
    assert(eventID == OvaAttemptSuccessAssertE || eventID == OvaAttemptFailureAssertE);
    jlong count = 0;
    bool throwOvaException = true;
    
    if(eventID == OvaAttemptSuccessAssertE)
    {
        if(ovaSuccessCountMap[assertID] != NULL)
        {
            count = *(ovaSuccessCountMap[assertID]);
            throwOvaException = false;
        }
    }
    else
    {
        if(ovaFailureCountMap[assertID] != NULL)
        {
            count = *(ovaFailureCountMap[assertID]);
            throwOvaException = false;
        }
    }

    if(throwOvaException)
    {
#ifdef OVA_DEBUG
        cout << ", [counting not enabled. throwing exception]" << endl;
#endif // OVA_DEBUG
        std::ostringstream msg;
        msg << "Counting is not enabled for assertID: " << assertID <<
            ", eventID: " << eventID;
        jstring str = JString::newString(penv, msg.str().c_str());
        jthrowable ovaException = (jthrowable)(pjoc->OVAException_ctor.create(JArguments() << str));
        JEnv env(penv);
        env.throwException(ovaException);
    }
#ifdef OVA_DEBUG
    cout << ", count = " << count << endl;
#endif // OVA_DEBUG
    return count;

#else
    return 0;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    setAssertSeverity
 * Signature: (JJI)Z
 */
JNIEXPORT jboolean JNICALL Java_com_newisys_ova_OVA_setAssertSeverity
  (JNIEnv *penv, jobject ths, jlong clientID, jlong assertID, jint severity)
{
#ifdef USE_OVA

#ifdef OVA_DEBUG
    cout << "ovaSetAssertSeverity(" << clientID << ", " << assertID << 
        ", " << severity << ")";
#endif // OVA_DEBUG
    Ova_Bool status = ovaSetAssertSeverity(clientID, assertID, severity);
#ifdef OVA_DEBUG
    cout << ", status = " << (status ? "OK" : "ERROR") << endl;
#endif // OVA_DEBUG
    return (status == OVA_TRUE) ? JNI_TRUE : JNI_FALSE;

#else
    return JNI_FALSE;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    getAssertSeverity
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL Java_com_newisys_ova_OVA_getAssertSeverity
  (JNIEnv *penv, jobject ths, jlong clientID, jlong assertID)
{
#ifdef USE_OVA

#ifdef OVA_DEBUG
    cout << "ovaGetAssertSeverity(" << clientID << ", " << assertID << ")";
#endif // OVA_DEBUG
    jint severity = (jint)ovaGetAssertSeverity(clientID, assertID);
#ifdef OVA_DEBUG
    cout << ", severity = " << severity << endl;
#endif // OVA_DEBUG
    return severity;

#else
    return 0;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    setAssertCategory
 * Signature: (JJI)Z
 */
JNIEXPORT jboolean JNICALL Java_com_newisys_ova_OVA_setAssertCategory
  (JNIEnv *penv, jobject ths, jlong clientID, jlong assertID, jint category)
{
#ifdef USE_OVA

#ifdef OVA_DEBUG
    cout << "ovaSetAssertCategory(" << clientID << ", " << assertID << 
        ", " << category << ")";
#endif // OVA_DEBUG
    Ova_Bool status = ovaSetAssertCategory(clientID, assertID, category);
#ifdef OVA_DEBUG
    cout << ", status = " << (status ? "OK" : "ERROR") << endl;
#endif // OVA_DEBUG
    return (status == OVA_TRUE) ? JNI_TRUE : JNI_FALSE;

#else
    return JNI_FALSE;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    getAssertCategory
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL Java_com_newisys_ova_OVA_getAssertCategory
  (JNIEnv *penv, jobject ths, jlong clientID, jlong assertID)
{
#ifdef USE_OVA

#ifdef OVA_DEBUG
    cout << "ovaGetAssertCategory(" << clientID << ", " << assertID << ")";
#endif // OVA_DEBUG
    jint category = (jint)ovaGetAssertCategory(clientID, assertID);
#ifdef OVA_DEBUG
    cout << ", category = " << category << endl;
#endif // OVA_DEBUG
    return category;

#else
    return 0;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    setAssertUserMessage
 * Signature: (JJLjava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_newisys_ova_OVA_setAssertUserMessage
  (JNIEnv *penv, jobject ths, jlong clientID, jlong assertID, jstring msgString)
{
#ifdef USE_OVA

#ifdef OVA_DEBUG
    cout << "ovaSetAssertUserMessage(" << clientID << ", " << assertID << 
        ", " << msgString << ")";
#endif // OVA_DEBUG
    char* msg = JString::convertToCharArray(penv, msgString);
    Ova_Bool status = ovaSetAssertUserMessage(clientID, assertID, msg);
    free(msg);
#ifdef OVA_DEBUG
    cout << ", status = " << (status ? "OK" : "ERROR") << endl;
#endif // OVA_DEBUG
    return (status == OVA_TRUE) ? JNI_TRUE : JNI_FALSE;

#else
    return JNI_FALSE;
#endif // USE_OVA
}

/*
 * Class:     com_newisys_ova_OVA
 * Method:    getAssertUserMessage
 * Signature: (JJ)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_newisys_ova_OVA_getAssertUserMessage
  (JNIEnv *penv, jobject ths, jlong clientID, jlong assertID)
{
#ifdef USE_OVA

#ifdef OVA_DEBUG
    cout << "ovaGetAssertUserMessage(" << clientID << ", " << assertID << ")";
#endif // OVA_DEBUG
    char* msg = ovaGetAssertUserMessage(clientID, assertID);
#ifdef OVA_DEBUG
    cout << ", msg = " << msg << endl;
#endif // OVA_DEBUG
    jstring msgString= JString::newString(penv, msg);
    assert(msgString != NULL);
    return msgString;

#else
    return NULL;
#endif // USE_OVA
}
