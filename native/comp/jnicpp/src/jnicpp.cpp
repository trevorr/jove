/*
 * jnicpp - Lightweight C++ Wrapper Classes for the Java (TM) Native Interface
 * Copyright (C) 2003 Trevor A. Robinson
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

#include "jnicpp.h"

#ifndef _WIN32
#include <dlfcn.h>
#else
#include <windows.h>
#endif

namespace jnicpp {

//////////////////////////////////////////////////////////////////////

void checkException(JNIEnv* penv) throw(JException)
{
    jthrowable obj = penv->ExceptionOccurred();
    if (obj != NULL) {
        penv->ExceptionClear();
        throw JVMException(penv, obj);
    }
}

void convertException(JNIEnv* penv, const std::string& msg) throw(JException)
{
    jthrowable obj = penv->ExceptionOccurred();
    if (obj != NULL) {
        penv->ExceptionClear();
        throw JVMException(penv, obj);
    } else {
        throw JException(msg);
    }
}

const char* getJNIResultMessage(jint result) throw()
{
    switch (result) {
        case JNI_OK:
            return "Success";
        case JNI_EDETACHED:
            return "Thread detached from the VM";
        case JNI_EVERSION:
            return "JNI version error";
        case JNI_ENOMEM:
            return "Not enough memory";
        case JNI_EEXIST:
            return "VM already created";
        case JNI_EINVAL:
            return "Invalid arguments";
        case JNI_ERR:
        default:
            return "Unknown error";
    }
}

//////////////////////////////////////////////////////////////////////

void JClass::registerNativeMethod(const char* name, const char* signature, void* fnptr)
{
    JNINativeMethod method;
    method.name = const_cast<char*>(name);
    method.signature = const_cast<char*>(signature);
    method.fnPtr = fnptr;
    if (m_penv->RegisterNatives(m_obj, &method, 1) != JNI_OK) {
        std::string msg("Unable to register native method: ");
        msg += name;
        msg += signature;
        convertException(m_penv, msg);
    }
}

void JClass::unregisterNativeMethods()
{
    if (m_penv->UnregisterNatives(m_obj) != JNI_OK) {
        convertException(m_penv, "Unable to unregister native methods");
    }
}

jclass JClass::findClass(JNIEnv* penv, const char* name)
{
    jclass cls = penv->FindClass(name);
#ifdef JNICPP_DEBUG_REF
    printf("FindClass: penv=%p, name=%s, cls=%p\n", penv, name, cls);
#endif
    if (cls == NULL) {
        std::string msg("Unable to find class: ");
        msg += name;
        convertException(penv, msg);
    }
    return cls;
}

//////////////////////////////////////////////////////////////////////

jmethodID JAbstractMethod::getMethodID(JNIEnv* penv, jclass cls,
    const char* name, const char* signature)
{
    jmethodID method = penv->GetMethodID(cls, name, signature);
#ifdef JNICPP_DEBUG_REF
    printf("GetMethodID: penv=%p, cls=%p, name=%s, method=%p\n", penv, cls, name, method);
#endif
    if (method == NULL) {
        std::string msg("Unable to find instance method: ");
        msg += name;
        msg += signature;
        convertException(penv, msg);
    }
    return method;
}

jmethodID JAbstractMethod::getStaticMethodID(JNIEnv* penv, jclass cls,
    const char* name, const char* signature)
{
    jmethodID method = penv->GetStaticMethodID(cls, name, signature);
#ifdef JNICPP_DEBUG_REF
    printf("GetStaticMethodID: penv=%p, cls=%p, name=%s, method=%p\n", penv, cls, name, method);
#endif
    if (method == NULL) {
        std::string msg("Unable to find static method: ");
        msg += name;
        msg += signature;
        convertException(penv, msg);
    }
    return method;
}

//////////////////////////////////////////////////////////////////////

jfieldID JAbstractField::getFieldID(JNIEnv* penv, jclass cls,
    const char* name, const char* signature)
{
    jfieldID field = penv->GetFieldID(cls, name, signature);
#ifdef JNICPP_DEBUG_REF
    printf("GetFieldID: penv=%p, cls=%p, name=%s, field=%p\n", penv, cls, name, field);
#endif
    if (field == NULL) {
        std::string msg("Unable to find instance field: ");
        msg += name;
        msg += signature;
        convertException(penv, msg);
    }
    return field;
}

jfieldID JAbstractField::getStaticFieldID(JNIEnv* penv, jclass cls,
    const char* name, const char* signature)
{
    jfieldID field = penv->GetStaticFieldID(cls, name, signature);
#ifdef JNICPP_DEBUG_REF
    printf("GetStaticFieldID: penv=%p, cls=%p, name=%s, field=%p\n", penv, cls, name, field);
#endif
    if (field == NULL) {
        std::string msg("Unable to find static field: ");
        msg += name;
        msg += signature;
        convertException(penv, msg);
    }
    return field;
}

//////////////////////////////////////////////////////////////////////

std::string JVMException::getMessage(JNIEnv* penv, jthrowable obj) throw()
{
    try {
        JClass cls(penv, "java/lang/Throwable");
        JMethod<jstring> method(cls, "toString", "()Ljava/lang/String;");
        jstring msg = method.call(obj);
        if (msg != NULL) {
            return JString(penv, msg, true).toString();
        } else {
            return "<null>";
        }
    }
    catch (...) {
        return "<Unable to get exception message>";
    }
}

//////////////////////////////////////////////////////////////////////

JVMOption::JVMOption(const std::string& optionString, void* extraInfo) :
    m_optionString(optionString),
    m_extraInfo(extraInfo)
{
    // done
}

void JVMOption::initJavaVMOption(JavaVMOption& opt) const
{
    opt.optionString = const_cast<char*>(m_optionString.c_str());
    opt.extraInfo = m_extraInfo;
}

//////////////////////////////////////////////////////////////////////

void JVMOptions::addOption(const std::string& str, void* info)
{
    m_opts.push_back(JVMOption(str, info));
}

void JVMOptions::defineProperty(const std::string& name, const std::string& value)
{
    addOption("-D" + name + "=" + value);
}

void JVMOptions::addClassPath(const std::string& path)
{
    addOption("-Djava.class.path=" + path);
}

int JVMOptions::getOptionCount() const
{
    return m_opts.size();
}

JavaVMOption* JVMOptions::newOptionArray() const
{
    JavaVMOption* arr = new JavaVMOption[getOptionCount()];

    JVMOptionList::const_iterator it = m_opts.begin();
    JVMOptionList::const_iterator end_it = m_opts.end();
    JavaVMOption* curOpt = arr;

    for (; it != end_it; ++it, ++curOpt) {
        (*it).initJavaVMOption(*curOpt);
    }

    return arr;
}

void JVMOptions::deleteOptionArray(JavaVMOption* opts) const
{
    delete[] opts;
}

//////////////////////////////////////////////////////////////////////

JVM::JVM() :
    m_pvm(createJVM(getDefaultJVMPath().c_str(), JVMOptions()))
{
    // done
}

JVM::JVM(const char* path) :
    m_pvm(createJVM(path, JVMOptions()))
{
    // done
}

JVM::JVM(const JVMOptions& opts) :
    m_pvm(createJVM(getDefaultJVMPath().c_str(), opts))
{
    // done
}

JVM::JVM(const char* path, const JVMOptions& opts) :
    m_pvm(createJVM(path, opts))
{
    // done
}

JVM::~JVM()
{
    m_pvm->DestroyJavaVM();
}

JVM::operator JavaVM*() const throw()
{
    return m_pvm;
}

std::string JVM::getDefaultJVMPath()
{
    // use JVM specified by JAVA_HOME, HOSTTYPE, and (optionally) JVMTYPE
    const char *javahome = getenv("JAVA_HOME");
    if (javahome == NULL) {
        throw JException("JAVA_HOME not found in environment");
    }

#ifndef _WIN32
    #ifndef _DARWIN
        const char *hosttype = getenv("HOSTTYPE");
        if (hosttype == NULL) {
            throw JException("HOSTTYPE not found in environment");
        }
    #endif
#endif

    const char *jvmtype = getenv("JVMTYPE");
    if (jvmtype == NULL) {
        jvmtype = "client";
    }

    std::string path("");
#ifdef _DARWIN
    path += "/System/Library/Frameworks/JavaVM.Framework/Versions/CurrentJDK/Libraries/libjvm_compat.dylib";
#else
    path += javahome;
    #ifndef _WIN32
        path += "/jre/lib/";
        path += hosttype;
        path += "/";
        path += jvmtype;
        path += "/libjvm.so";
    #else
        path += "\\jre\\bin\\";
        path += jvmtype;
        path += "\\jvm.dll";
    #endif
#endif  
    return path;
}

#ifdef _WIN32

static void appendErrorMessage(std::string& msg, DWORD errorCode)
{
    LPCTSTR lpMsgBuf;
    if (FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER |
                      FORMAT_MESSAGE_FROM_SYSTEM |
                      FORMAT_MESSAGE_IGNORE_INSERTS,
                      NULL, // ignored, source is system
                      errorCode, // message ID
                      0, // use default language
                      (LPTSTR)&lpMsgBuf, // store allocated buffer pointer
                      0, // no minimum buffer size
                      NULL // no message arguments
                      ) > 0) {
        msg += ": ";
        msg += lpMsgBuf;
        LocalFree((LPVOID)lpMsgBuf);
    }
}

#endif

JavaVM* JVM::createJVM(const char* path, const JVMOptions& opts)
{
    const char *funcName = "JNI_CreateJavaVM";
    typedef jint (JNICALL *CreateJavaVMPtr)(JavaVM **ppvm, void **ppenv, void *pargs);
    CreateJavaVMPtr pfcvm;

#ifndef _WIN32
    void *libjvm;

    // load JVM shared object
    libjvm = dlopen(path, RTLD_NOW | RTLD_GLOBAL);
    if (libjvm == NULL) {
        throw JException(dlerror());
    }

    // get JNI_CreateJavaVM entry point
    pfcvm = (CreateJavaVMPtr)dlsym(libjvm, funcName);
    if (pfcvm == NULL) {
        throw JException(dlerror());
    }
#else
    HINSTANCE handle;

    // load JVM DLL
    if ((handle = LoadLibrary(path)) == 0) {
        DWORD errorCode = GetLastError();
        std::string msg("LoadLibrary failed to load ");
        msg += path;
        appendErrorMessage(msg, errorCode);
        throw JException(msg);
    }

    // get JNI_CreateJavaVM entry point
    pfcvm = (CreateJavaVMPtr)GetProcAddress(handle, funcName);
    if (pfcvm == NULL) {
        DWORD errorCode = GetLastError();
        std::string msg("GetProcAddress failed for ");
        msg += funcName;
        appendErrorMessage(msg, errorCode);
        throw JException(msg);
    }
#endif

    JavaVMInitArgs args;
    args.version = JNI_VERSION_1_2;
    args.nOptions = opts.getOptionCount();
    args.options = opts.newOptionArray();
    args.ignoreUnrecognized = JNI_FALSE;

    JavaVM *pvm;
    JNIEnv *penv;
    jint res = pfcvm(&pvm, (void **)&penv, &args);

    opts.deleteOptionArray(args.options);

    if (res != JNI_OK) {
        std::string msg("JNI_CreateJavaVM failed: ");
        msg += getJNIResultMessage(res);
        throw JException(msg);
    }

    return pvm;
}

//////////////////////////////////////////////////////////////////////

}
