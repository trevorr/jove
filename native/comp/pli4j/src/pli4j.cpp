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
 * Defines the pli4j library entrypoints.
 *
 * \author Trevor Robinson
 * \author Jon Nall (OVA support, REGISTER_VPI_TASKS)
 */

#ifdef __CYGWIN__
    // include the __int64 def needed by jni.h
    #include <w32api/basetyps.h>
#endif

// module header
#include "pli4j.h"

// component headers
#include "Utilities.h"
#include "JavaObjectCache.h"
#include "ConversionFunctions.h"
#include "FinishSupport.h"
#include "CallbackHandler.h"
#include "com_newisys_verilog_pli_PLI.h"
#include "com_newisys_ova_OVA.h"

// external component headers
#include "vpi_user.h"
#include "veriuser.h"
#include "jnicpp.h"
#include "vpicpp.h"

// system headers
#include <exception>
#include <string>
#include <iostream>

#include <assert.h>
#include <signal.h>

// using declarations
using namespace jnicpp;
using namespace vpicpp;
using std::exception;
using std::string;
using std::cout;
using std::cerr;
using std::endl;

// shared data
JVM* pvm = NULL;
JObject* pPliObj = NULL;

// private data
static bool initTried = false;
static JObject* pAppObj = NULL;
static vpiHandle eosCallback = NULL;

static const char *getPropStr(const char *name, int argc, char** argv)
{
    int cmplen, i;
    cmplen = strlen(name);
    for (i = 0; i < argc; ++i) {
        if (strncmp(name, *argv, cmplen) == 0) {
            return (*argv) + cmplen;
        }
        ++argv;
    }
    return NULL;
}

static PLI_INT32 pli4j_eos_rtn(p_cb_data cb_data_p)
{
    pli4j_destroy(cb_data_p->user_data);
    return 0;
}

static void checkInit()
{
    if (pvm == NULL) {

        // give up after the first try, rather than spewing errors for each call
        if (initTried) {
            throw JException("JVM not available");
        }
        initTried = true;

#ifdef USE_TF_SYNCHRONIZE
        cout << "Using tf_synchronize workaround" << endl;
#endif

        // get the command line arguments
        s_vpi_vlog_info vlog_info;
        vpi_get_vlog_info(&vlog_info);

        // get the JVM class path from the command line
        const char *classpath = getPropStr("+javaclasspath=", vlog_info.argc, vlog_info.argv);
        if (classpath == NULL) {
            classpath = ".";
        }
        cout << "Using Java class path: " << classpath << endl;

        // get the VerilogApplication class from the command line
        const char *classname = getPropStr("+javaclass=", vlog_info.argc, vlog_info.argv);
        if (classname == NULL) {
            throw JException("+javaclass not specified");
        }
        cout << "Using Java application class: " << classname << endl;

        // scan command line arguments for JVM options
        JVMOptions opts;
        opts.addClassPath(classpath);
        {
            const char* javaopt_prefix = "+javaopt=";
            int cmplen = strlen(javaopt_prefix);
            int argc = vlog_info.argc;
            char** argv = vlog_info.argv;
            for (int i = 0; i < argc; ++i) {
                if (strncmp(javaopt_prefix, *argv, cmplen) == 0) {
                    const char* option = (*argv) + cmplen;
                    cout << "Using Java option: " << option << endl;
                    opts.addOption(option);
                }
                ++argv;
            }
        }

        // register an end of simulation callback so we can clean up;
        // this is important for tools like profilers that hook DestroyJVM
        s_cb_data cb_data = { cbEndOfSimulation, pli4j_eos_rtn };
        eosCallback = vpi_register_cb(&cb_data);
        if (eosCallback == NULL) {
            cerr << "Warning: Unable to register cbEndSimulation callback" << endl;
        }

        // create the JVM;
        // keep the original simulator handler for SIGINT
#ifdef JVM_DEBUG
        cout << "Creating JVM" << endl;
#endif
        struct sigaction sigint_act;
        int result;
        result = sigaction(SIGINT, NULL, &sigint_act);
        assert(result == 0);
        pvm = new JVM(opts);
        result = sigaction(SIGINT, &sigint_act, NULL);
        assert(result == 0);

        JEnv env(*pvm);

        // create the Java object cache
#ifdef JVM_DEBUG
        cout << "Creating Java object cache" << endl;
#endif
        pjoc = new JavaObjectCache(env);

        // register the native methods in the PLI class
#ifdef JVM_DEBUG
        cout << "Registering PLI native methods" << endl;
#endif
        pjoc->PLI_class.registerNativeMethod("getInfo",
            "()Lcom/newisys/verilog/pli/PLIVerilogInfo;",
            (void*)Java_com_newisys_verilog_pli_PLI_getInfo);
        pjoc->PLI_class.registerNativeMethod("registerCallback0",
            "(Lcom/newisys/verilog/pli/PLIVerilogCallback;IIJDJI)J",
            (void*)Java_com_newisys_verilog_pli_PLI_registerCallback0);
        pjoc->PLI_class.registerNativeMethod("cancelCallback0",
            "(J)Z",
            (void*)Java_com_newisys_verilog_pli_PLI_cancelCallback0);
        pjoc->PLI_class.registerNativeMethod("releaseCallback",
            "(J)V",
            (void*)Java_com_newisys_verilog_pli_PLI_releaseCallback);
        pjoc->PLI_class.registerNativeMethod("getHandle0",
            "(IJ)J",
            (void*)Java_com_newisys_verilog_pli_PLI_getHandle0);
        pjoc->PLI_class.registerNativeMethod("getHandleByName0",
            "(Ljava/lang/String;J)J",
            (void*)Java_com_newisys_verilog_pli_PLI_getHandleByName0);
        pjoc->PLI_class.registerNativeMethod("getHandleMulti0",
            "(IJJ)J",
            (void*)Java_com_newisys_verilog_pli_PLI_getHandleMulti0);
        pjoc->PLI_class.registerNativeMethod("getHandleByIndex0",
            "(JI)J",
            (void*)Java_com_newisys_verilog_pli_PLI_getHandleByIndex0);
        pjoc->PLI_class.registerNativeMethod("getHandleByMultiIndex0",
            "(J[I)J",
            (void*)Java_com_newisys_verilog_pli_PLI_getHandleByMultiIndex0);
        pjoc->PLI_class.registerNativeMethod("iterate0",
            "(IJ)J",
            (void*)Java_com_newisys_verilog_pli_PLI_iterate0);
        pjoc->PLI_class.registerNativeMethod("scan0",
            "(J)J",
            (void*)Java_com_newisys_verilog_pli_PLI_scan0);
        pjoc->PLI_class.registerNativeMethod("compareObjects",
            "(JJ)Z",
            (void*)Java_com_newisys_verilog_pli_PLI_compareObjects);
        pjoc->PLI_class.registerNativeMethod("freeObject",
            "(J)V",
            (void*)Java_com_newisys_verilog_pli_PLI_freeObject);
        pjoc->PLI_class.registerNativeMethod("getPropInt",
            "(IJ)I",
            (void*)Java_com_newisys_verilog_pli_PLI_getPropInt);
        pjoc->PLI_class.registerNativeMethod("getPropStr",
            "(IJ)Ljava/lang/String;",
            (void*)Java_com_newisys_verilog_pli_PLI_getPropStr);
        pjoc->PLI_class.registerNativeMethod("getValue0",
            "(JI)Ljava/lang/Object;",
            (void*)Java_com_newisys_verilog_pli_PLI_getValue0);
        pjoc->PLI_class.registerNativeMethod("putValue0",
            "(JLjava/lang/Object;IJDI)J",
            (void*)Java_com_newisys_verilog_pli_PLI_putValue0);
        pjoc->PLI_class.registerNativeMethod("releaseForce0",
            "(J)Ljava/lang/Object;",
            (void*)Java_com_newisys_verilog_pli_PLI_releaseForce0);
        pjoc->PLI_class.registerNativeMethod("getTime",
            "(IJ)Lcom/newisys/verilog/pli/PLITime;",
            (void*)Java_com_newisys_verilog_pli_PLI_getTime);
        pjoc->PLI_class.registerNativeMethod("print0",
            "([BII)Z",
            (void*)Java_com_newisys_verilog_pli_PLI_print0);
        pjoc->PLI_class.registerNativeMethod("flush0",
            "()Z",
            (void*)Java_com_newisys_verilog_pli_PLI_flush0);
        pjoc->PLI_class.registerNativeMethod("stop",
            "()V",
            (void*)Java_com_newisys_verilog_pli_PLI_stop);
        pjoc->PLI_class.registerNativeMethod("finish",
            "()V",
            (void*)Java_com_newisys_verilog_pli_PLI_finish);
        pjoc->PLI_class.registerNativeMethod("getError",
            "()Lcom/newisys/verilog/pli/PLI$ErrorInfo;",
            (void*)Java_com_newisys_verilog_pli_PLI_getError);

        // This OVA method is always available
        pjoc->OVA_class.registerNativeMethod("isSupported",
                "()Z",
                (void*)Java_com_newisys_ova_OVA_isSupported);
#ifdef USE_OVA
#ifdef OVA_DEBUG
        cout << "Registering OVA methods" << endl;
#endif
        pjoc->OVA_class.registerNativeMethod("getApiVersion",
                "()Ljava/lang/String;",
                (void*)Java_com_newisys_ova_OVA_getApiVersion);
        pjoc->OVA_class.registerNativeMethod("registerClient",
                "()J",
                (void*)Java_com_newisys_ova_OVA_registerClient);
        pjoc->OVA_class.registerNativeMethod("setConfigSwitch",
                "(JIZ)Z",
                (void*)Java_com_newisys_ova_OVA_setConfigSwitch);
        pjoc->OVA_class.registerNativeMethod("doAction",
                "(JIJ)Z",
                (void*)Java_com_newisys_ova_OVA_doAction);
        pjoc->OVA_class.registerNativeMethod("firstAssert",
                "(J)J",
                (void*)Java_com_newisys_ova_OVA_firstAssert);
        pjoc->OVA_class.registerNativeMethod("nextAssert",
                "(J)J",
                (void*)Java_com_newisys_ova_OVA_nextAssert);
        pjoc->OVA_class.registerNativeMethod("assertDoAction",
                "(JIJJJ)Z",
                (void*)Java_com_newisys_ova_OVA_assertDoAction);
        pjoc->OVA_class.registerNativeMethod("hasAssertInfo",
                "(J)Z",
                (void*)Java_com_newisys_ova_OVA_hasAssertInfo);
        pjoc->OVA_class.registerNativeMethod("getAssertInfo",
                "(JJ)Lcom/newisys/ova/OVAAssertInfo;",
                (void*)Java_com_newisys_ova_OVA_getAssertInfo);
        pjoc->OVA_class.registerNativeMethod("addEngineListener",
                "(JI)Z",
                (void*)Java_com_newisys_ova_OVA_addEngineListener);
        pjoc->OVA_class.registerNativeMethod("addAssertListener",
                "(JIJ)Z",
                (void*)Java_com_newisys_ova_OVA_addAssertListener);
        pjoc->OVA_class.registerNativeMethod("removeEngineListener",
                "(I)Z",
                (void*)Java_com_newisys_ova_OVA_removeEngineListener);
        pjoc->OVA_class.registerNativeMethod("removeAssertListener",
                "(IJ)Z",
                (void*)Java_com_newisys_ova_OVA_removeAssertListener);
        pjoc->OVA_class.registerNativeMethod("enableAssertCount",
                "(JJI)Z",
                (void*)Java_com_newisys_ova_OVA_enableAssertCount);
        pjoc->OVA_class.registerNativeMethod("disableAssertCount",
                "(JJI)Z",
                (void*)Java_com_newisys_ova_OVA_disableAssertCount);
        pjoc->OVA_class.registerNativeMethod("getAssertCount",
                "(JJI)J",
                (void*)Java_com_newisys_ova_OVA_getAssertCount);
        pjoc->OVA_class.registerNativeMethod("setAssertSeverity",
                "(JJI)Z",
                (void*)Java_com_newisys_ova_OVA_setAssertSeverity);
        pjoc->OVA_class.registerNativeMethod("getAssertSeverity",
                "(JJ)I",
                (void*)Java_com_newisys_ova_OVA_getAssertSeverity);
        pjoc->OVA_class.registerNativeMethod("setAssertCategory",
                "(JJI)Z",
                (void*)Java_com_newisys_ova_OVA_setAssertCategory);
        pjoc->OVA_class.registerNativeMethod("getAssertCategory",
                "(JJ)I",
                (void*)Java_com_newisys_ova_OVA_getAssertCategory);
        pjoc->OVA_class.registerNativeMethod("setAssertUserMessage",
                "(JJLjava/lang/String;)Z",
                (void*)Java_com_newisys_ova_OVA_setAssertUserMessage);
        pjoc->OVA_class.registerNativeMethod("getAssertUserMessage",
                "(JJ)Ljava/lang/String;",
                (void*)Java_com_newisys_ova_OVA_getAssertUserMessage);
#endif

        // create the PLI interface object
#ifdef JVM_DEBUG
        cout << "Creating PLI interface object" << endl;
#endif
        JCtor pliCtor(pjoc->PLI_class, "()V");
        pPliObj = new JObject(pliCtor.createObject());

        // create the VerilogApplication object
#ifdef JVM_DEBUG
        cout << "Creating VerilogApplication object" << endl;
#endif
        JClass launcherClass(env, "com/newisys/verilog/pli/PLIAppLauncher");
        JStaticMethod<jobject> createAppMethod(launcherClass, "createApplication",
            "(Ljava/lang/String;Lcom/newisys/verilog/pli/PLI;)Lcom/newisys/verilog/VerilogApplication;");
        JString appClassName(env, classname);
        enterJava();
        pAppObj = new JObject(env,
            createAppMethod.call(JArguments() << appClassName << *pPliObj),
            true);
        exitJava();
    }
}

int pli4j_init(char *user_data)
{
    try {
        checkInit();
    }
    catch (JVMException& e) {
        die(e);
    }
    catch (exception& e) {
        die(e.what());
    }
    return 0;
}

int pli4j_register_object(char *user_data)
{
    try {
        checkInit();

        JEnv env(*pvm);

        vpiHandle tfCall = getHandle(vpiSysTfCall);
        int argCount = getIteratorCount(vpiArgument, tfCall);
        if (argCount != 2) {
            throw JException("pli4j_register_object expects 2 arguments");
        }

        // get arguments
        string name;
        vpiHandle obj_handle;
        {
            VPIIterator argIter(vpiArgument, tfCall);
            vpiHandle arg;
            int i = 0;
            while ((arg = argIter.next()) != NULL) {
                int type = getIntProperty(vpiType, arg);
                switch (i) {
                    case 0:
                    {
                        int constType = getIntProperty(vpiConstType, arg);
                        if (type != vpiConstant || constType != vpiStringConst) {
                            throw JException("pli4j_register_object argument 1 must be a literal string");
                        }
                        name = getStringValue(arg);
                        break;
                    }
                    case 1:
                    {
                        obj_handle = arg;
                        break;
                    }
                }
                ++i;
            }
        }

        // instantiate wrapper for object
        jobject obj = createObjectWrapper(*pPliObj, obj_handle);
        JLocalRef<> objRef(env, obj);

        // call Java method
        enterJava();
        pjoc->VerilogApplication_registerObject.call(*pAppObj, JArguments()
            << JString(env, name.c_str())
            << obj);
        exitJava();
    }
    catch (JVMException& e) {
        die(e);
    }
    catch (exception& e) {
        die(e.what());
    }
    return 0;
}

int pli4j_register_signal(char *user_data)
{
    try {
        checkInit();

        JEnv env(*pvm);

        vpiHandle tfCall = getHandle(vpiSysTfCall);
        int argCount = getIteratorCount(vpiArgument, tfCall);
        if (argCount != 3) {
            throw JException("pli4j_register_signal expects 3 arguments");
        }

        // get arguments
        string name;
        vpiHandle sampleObj_handle;
        vpiHandle driveObj_handle;
        {
            VPIIterator argIter(vpiArgument, tfCall);
            vpiHandle arg;
            int i = 0;
            while ((arg = argIter.next()) != NULL) {
                int type = getIntProperty(vpiType, arg);
                switch (i) {
                    case 0:
                    {
                        int constType = getIntProperty(vpiConstType, arg);
                        if (type != vpiConstant || constType != vpiStringConst) {
                            throw JException("pli4j_register_signal argument 1 must be a literal string");
                        }
                        name = getStringValue(arg);
                        break;
                    }
                    case 1:
                    {
                        sampleObj_handle = arg;
                        break;
                    }
                    case 2:
                    {
                        driveObj_handle = arg;
                        break;
                    }
                }
                ++i;
            }
        }

        // instantiate wrappers for objects
        jobject sampleObj = createObjectWrapper(*pPliObj, sampleObj_handle);
        JLocalRef<> sampleObjRef(env, sampleObj);
        jobject driveObj = createObjectWrapper(*pPliObj, driveObj_handle);
        JLocalRef<> driveObjRef(env, driveObj);

        // call Java method
        enterJava();
        pjoc->VerilogApplication_registerSignal.call(*pAppObj, JArguments()
            << JString(env, name.c_str())
            << sampleObj
            << driveObj);
        exitJava();
    }
    catch (JVMException& e) {
        die(e);
    }
    catch (exception& e) {
        die(e.what());
    }
    return 0;
}

int pli4j_register_verilog_task(char *user_data)
{
    try {
        checkInit();

        JEnv env(*pvm);

        vpiHandle tfCall = getHandle(vpiSysTfCall);
        int argCount = getIteratorCount(vpiArgument, tfCall);
        if (argCount < 3) {
            throw JException("pli4j_register_verilog_task expects at least 3 arguments");
        }
        int javaArgCount = argCount - 3;

        // get arguments
        string name;
        vpiHandle start_handle;
        vpiHandle done_handle;
        JObjectArray argArray(env, javaArgCount, pjoc->VerilogObject_class);
        {
            VPIIterator argIter(vpiArgument, tfCall);
            vpiHandle arg;
            jint i = -3;
            while ((arg = argIter.next()) != NULL) {
                int type = getIntProperty(vpiType, arg);
                switch (i) {
                    case -3:
                    {
                        int constType = getIntProperty(vpiConstType, arg);
                        if (type != vpiConstant || constType != vpiStringConst) {
                            throw JException("pli4j_register_verilog_task argument 1 must be a literal string");
                        }
                        name = getStringValue(arg);
                        break;
                    }
                    case -2:
                    {
                        if (type != vpiReg) {
                            throw JException("pli4j_register_verilog_task argument 2 must be a reg");
                        }
                        start_handle = arg;
                        break;
                    }
                    case -1:
                    {
                        if (type != vpiReg) {
                            throw JException("pli4j_register_verilog_task argument 3 must be a reg");
                        }
                        done_handle = arg;
                        break;
                    }
                    default:
                    {
                        JLocalRef<> argRef(env, createObjectWrapper(*pPliObj, arg));
                        argArray[i] = argRef;
                        break;
                    }
                }
                ++i;
            }
        }

        // instantiate wrappers for start and done registers
        jobject startReg = createObjectWrapper(*pPliObj, start_handle);
        JLocalRef<> startRegRef(env, startReg);
        jobject doneReg = createObjectWrapper(*pPliObj, done_handle);
        JLocalRef<> doneRegRef(env, doneReg);

        // call Java method
        enterJava();
        pjoc->VerilogApplication_registerVerilogTask.call(*pAppObj, JArguments()
            << JString(env, name.c_str())
            << startReg
            << doneReg
            << argArray);
        exitJava();
    }
    catch (JVMException& e) {
        die(e);
    }
    catch (exception& e) {
        die(e.what());
    }
    return 0;
}

int pli4j_call(char *user_data)
{
    try {
        checkInit();

        JEnv env(*pvm);

        vpiHandle tfCall = getHandle(vpiSysTfCall);
        int argCount = getIteratorCount(vpiArgument, tfCall);
        if (argCount < 2) {
            throw JException("pli4j_call expects at least 2 arguments");
        }
        int javaArgCount = argCount - 2;

        // get arguments
        string taskName;
        vpiHandle done_handle;
        JObjectArray argArray(env, javaArgCount, pjoc->VerilogObject_class);
        {
            VPIIterator argIter(vpiArgument, tfCall);
            vpiHandle arg;
            jint i = -2;
            while ((arg = argIter.next()) != NULL) {
                int type = getIntProperty(vpiType, arg);
                switch (i) {
                    case -2:
                    {
                        int constType = getIntProperty(vpiConstType, arg);
                        if (type != vpiConstant || constType != vpiStringConst) {
                            throw JException("pli4j_call argument 1 must be a literal string");
                        }
                        taskName = getStringValue(arg);
                        break;
                    }
                    case -1:
                    {
                        if (type != vpiReg) {
                            throw JException("pli4j_call argument 2 must be a reg");
                        }
                        done_handle = arg;
                        break;
                    }
                    default:
                    {
                        JLocalRef<> argRef(env, createObjectWrapper(*pPliObj, arg));
                        argArray[i] = argRef;
                        break;
                    }
                }
                ++i;
            }
        }

        // instantiate wrapper for done register
        jobject doneReg = createObjectWrapper(*pPliObj, done_handle);
        JLocalRef<> doneRegRef(env, doneReg);

        // call Java method
        enterJava();
        pjoc->VerilogApplication_callJavaTask.call(*pAppObj, JArguments()
            << JString(env, taskName.c_str())
            << doneReg
            << argArray);
        exitJava();
    }
    catch (JVMException& e) {
        die(e);
    }
    catch (exception& e) {
        die(e.what());
    }
    return 0;
}

int pli4j_start(char *user_data)
{
    try {
        checkInit();

#ifdef USE_TF_SYNCHRONIZE
        misctfInstance = tf_getinstance();
#endif

        enterJava();
        pjoc->VerilogApplication_start.call(*pAppObj);
        exitJava();
    }
    catch (JVMException& e) {
        die(e);
    }
    catch (exception& e) {
        die(e.what());
    }
    return 0;
}

int pli4j_destroy(char *user_data)
{
    if (pvm != NULL) {
        if (pAppObj != NULL) {
#ifdef JVM_DEBUG
            cout << "Calling VerilogApplication.finish()" << endl;
#endif
            enterJava();
            pjoc->VerilogApplication_finish.call(*pAppObj);
            exitJava();

#ifdef JVM_DEBUG
            cout << "Destroying VerilogApplication object" << endl;
#endif
            delete pAppObj;
            pAppObj = NULL;
        }

#ifdef JVM_DEBUG
        cout << "Destroying PLI interface object" << endl;
#endif
        delete pPliObj;
        pPliObj = NULL;

#ifdef JVM_DEBUG
        cout << "Destroying Java object cache" << endl;
#endif
        delete pjoc;
        pjoc = NULL;

#ifdef JVM_DEBUG
        cout << "Destroying JVM" << endl;
#endif
        delete pvm;
        pvm = NULL;

        initTried = false;
    }
    return 0;
}

/* This function is passed via +load=libpli4j:pli4j_register_vpi_tasks
 * or +loadvpi=libpli4j:pli4j_register_vpi_tasks, depending on the
 * simulator.
 */
void pli4j_register_vpi_tasks(void)
{
#ifdef REGISTER_VPI_TASKS
    static s_vpi_systf_data systf_data_list[] = {
        { vpiSysTask, 0, "$pli4j_init", pli4j_init, NULL, NULL, NULL },
        { vpiSysTask, 0, "$pli4j_register_object", pli4j_register_object, NULL, NULL, NULL },
        { vpiSysTask, 0, "$pli4j_register_signal", pli4j_register_signal, NULL, NULL, NULL },
        { vpiSysTask, 0, "$pli4j_register_verilog_task", pli4j_register_verilog_task, NULL, NULL, NULL },
        { vpiSysTask, 0, "$pli4j_call", pli4j_call, NULL, NULL, NULL },
        { vpiSysTask, 0, "$pli4j_start", pli4j_start, NULL, NULL, NULL },
        { vpiSysTask, 0, "$pli4j_destroy", pli4j_destroy, NULL, NULL, NULL },
        { 0, 0, NULL, NULL, NULL, NULL, NULL }
    };

    p_vpi_systf_data systf_data_p = &(systf_data_list[0]);
    while (systf_data_p->type != 0)
    {
        vpi_register_systf(systf_data_p++);
    }
#endif
}
