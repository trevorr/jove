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
 * Contains callback functions called by the simulator that in turn dispatch
 * the callback to Java.
 *
 * \author Trevor Robinson
 * \author Jon Nall (OVA support)
 */

// module header
#include "CallbackHandler.h"

// component headers
#include "Utilities.h"
#include "JavaObjectCache.h"
#include "ConversionFunctions.h"
#include "FinishSupport.h"

// external component headers
#include "veriuser.h"

// using declarations
using namespace jnicpp;
using std::exception;

// shared data
#ifdef USE_TF_SYNCHRONIZE
char* misctfInstance = NULL;
CallbackInfoList rwSynchCallbacks;
#endif

#ifdef USE_OVA
#include "OVAApi.h"

static jnicpp::JObject* pOvaEngineObj = NULL;
#endif // USE_OVA

static void dispatch_cb(
    CallbackInfo* info_ptr,
    vpiHandle obj = NULL,
    p_vpi_time time = NULL,
    p_vpi_value value = NULL,
    PLI_INT32 index = 0)
{
    assert(info_ptr != NULL);
    info_ptr->assertValid();

    if (pvm != NULL) {
        try {
            JEnv env(*pvm);

            jint timeType = vpiSuppressTime;
            jlong longTime = 0;
            jdouble doubleTime = 0;
            if (time != NULL) {
                timeType = time->type;
                longTime = (static_cast<jlong>(time->high) << 32) | time->low;
                doubleTime = time->real;
            }

            jobject valueObj = NULL;
            if (obj != NULL && value != NULL) {
                valueObj = getJavaValue(env, obj, *value);
            }
            JLocalRef<> valueRef(env, valueObj);
            enterJava();
            pjoc->PLI_dispatchCallback.call(*pPliObj, JArguments()
                << info_ptr->getCallback()
                << timeType
                << longTime
                << doubleTime
                << valueObj
                << (jint) index);
            exitJava();
        }
        catch (JVMException& e) {
            die(e);
        }
        catch (exception& e) {
            die(e.what());
        }
    } else {
        // JVM has already been destroyed, presumably due to an error
#ifdef PLI_DEBUG
        cout << "JVM destroyed; ignoring Java callback" << endl;
#endif
    }
}

PLI_INT32 pli4j_cb_rtn(p_cb_data cb_data_p)
{
#ifdef PLI_DEBUG
    cout << "pli4j_cb_rtn: " << cb_data_p << endl;
#endif

    assert(cb_data_p != NULL);

    CallbackInfo* info_ptr = reinterpret_cast<CallbackInfo*>(cb_data_p->user_data);
    dispatch_cb(info_ptr, cb_data_p->obj, cb_data_p->time,
        cb_data_p->value, cb_data_p->index);

    return 0;
}

#ifdef USE_OVA
static JObject* getOVAEngine()
{
    if(pOvaEngineObj == NULL)
    {
        // call DV.simulation.getOVAEngine()
        JEnv env(*pvm);
        jnicpp::JClass DV_class(env, "com/newisys/dv/DV");
        JStaticField<jobject> DV_simulation_field(DV_class, "simulation", "Lcom/newisys/dv/DVSimulation;");

        jnicpp::JClass DVSimulation_class(env, "com/newisys/dv/DVSimulation");
        jnicpp::JMethod<jobject> getOVAEngine(DVSimulation_class, "getOVAEngine", "()Lcom/newisys/ova/OVAEngine;");
        jobject ovaEngine = getOVAEngine.call(DV_simulation_field.value());
        pOvaEngineObj = new JObject(env, ovaEngine, true);
    }

    return pOvaEngineObj;
}
#endif // USE_OVA

#ifdef USE_OVA
void pli4j_ovaassert_count_cb_rtn(Ova_AssertEvent event_type, Ova_Time sim_time_unused,
        Ova_AssertID assert_id, Ova_AssertAttemptID attempt_id,
        Ova_UserData user_data)
{
    assert(event_type == OvaAttemptSuccessAssertE || event_type == OvaAttemptFailureAssertE);
    assert(user_data != NULL);

    // user_data is a pointer to a jlong. it gets set up in 
    // com_newisys_ova_OVA_enableAssertCount
    jlong* count = (jlong*)user_data;
    ++(*count); 
}
#endif // USE_OVA

#ifdef USE_OVA
void pli4j_ovaeng_cb_rtn(Ova_EngEvent event_type, Ova_Time sim_time_unused,
        Ova_UserData user_data)
{
        // according to the OVA API spec the Ova_Time is not used and
        // clients should call ovaGetCurrentTime/ovaGetCurrentTimeL
        // we'll use ovaGetCurrentTimeL since numeric types are easier
        // to deal with
        jlong simTime = (jlong)ovaGetCurrentTimeL();
#ifdef OVA_DEBUG
    cout << "pli4j_ovaeng_cb_rtn: event_type: " << event_type <<
        ", sim_time: " << simTime << ", user_data: " << user_data << endl;
#endif // OVA_DEBUG

    if (pvm != NULL) {
        try {
            JEnv env(*pvm);

            enterJava();
            jobject eventType = pjoc->OVAEngineEventType_forValue.call(
                    JArguments()
                    << (jint)event_type);
            pjoc->OVAEngine_dispatchEngineCallback.call(*getOVAEngine(), JArguments()
                    << eventType
                    << simTime);
            exitJava();
        }
        catch (JVMException& e) {
            die(e);
        }
        catch (exception& e) {
            die(e.what());
        }
    } else {
        // JVM has already been destroyed, presumably due to an error
#ifdef OVA_DEBUG
        cout << "JVM destroyed; ignoring Java OVA Engine callback" << endl;
#endif // OVA_DEBUG
    }
}
#endif // USE_OVA

#ifdef USE_OVA
void pli4j_ovaassert_cb_rtn(Ova_AssertEvent event_type, Ova_Time sim_time_unused,
        Ova_AssertID assert_id, Ova_AssertAttemptID attempt_id,
        Ova_UserData user_data)
{
        // according to the OVA API spec the Ova_Time is not used and
        // clients should call ovaGetCurrentTime/ovaGetCurrentTimeL
        // we'll use ovaGetCurrentTimeL since numeric types are easier
        // to deal with
        jlong simTime = (jlong)ovaGetCurrentTimeL();

#ifdef OVA_DEBUG
    cout << "pli4j_ovaassert_cb_rtn: event_type: " << event_type <<
        ", time: " << simTime << ", assert_id: " << assert_id <<
        ", attempt_id: " << attempt_id << ", userData: " << user_data << endl;
#endif // OVA_DEBUG

    if (pvm != NULL) {
        try {
            JEnv env(*pvm);

            jlong assertID = (jlong)assert_id;
            jlong attemptID = (jlong)attempt_id;

            enterJava();
            jobject eventType = pjoc->OVAAssertEventType_forValue.call(
                    JArguments()
                    << (jint)event_type);
            pjoc->OVAEngine_dispatchAssertCallback.call(*getOVAEngine(), JArguments()
                    << eventType 
                    << simTime 
                    << assertID
                    << attemptID);
            exitJava();
        }
        catch (JVMException& e) {
            die(e);
        }
        catch (exception& e) {
            die(e.what());
        }
    } else {
        // JVM has already been destroyed, presumably due to an error
#ifdef OVA_DEBUG
        cout << "JVM destroyed; ignoring Java OVA Assert callback" << endl;
#endif // OVA_DEBUG
    }
}
#endif // USE_OVA

int pli4j_misctf(char *user_data, int reason)
{
#ifdef PLI_DEBUG
    cout << "pli4j_misctf: " << user_data << ", " << reason << endl;
#endif

#ifdef USE_TF_SYNCHRONIZE
    // handle R/W synch callback
    if (reason == REASON_SYNCH) {

        // normally there should be at least one callback in the queue;
        // however, it could be empty if all callbacks have been cancelled
        if (!rwSynchCallbacks.empty()) {

            // pop the callback from the head of the queue
            CallbackInfo* info_ptr = rwSynchCallbacks.front();
            rwSynchCallbacks.pop_front();

            // if the queue contains more callbacks, schedule another synch
            // (this is done before dispatching the callback, since the Java
            // application may schedule additional callbacks, and we would
            // not know whether we need to schedule the synch or if it was
            // already done by PLI.registerCallback0)
            if (!rwSynchCallbacks.empty()) {
                int result = tf_isynchronize(misctfInstance);
                // we should be able to assume success here, since the only
                // reason for tf_isynchronize to fail is being in R/O synch,
                // and we know we are in R/W synch
                assert (result == 0);
            }

            // get current simulation time (as done by VPI R/W callback)
            s_vpi_time time;
            time.type = vpiSimTime;
            vpi_get_time(NULL, &time);

            // dispatch the callback to Java
            dispatch_cb(info_ptr, NULL, &time);
        }
    }
#endif

    return 0;
}
