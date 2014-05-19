/*
 * jvminvoke - A Java (TM) VM Invocation Library for C
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

#include "jvminvoke.h"

#include <stdlib.h>
#include <stdio.h>
#include <limits.h>
#include <sys/stat.h>

#ifdef WIN32

    #include <windows.h>
    #define snprintf _snprintf

    #define FILE_SEPARATOR "\\"
    #define MAXPATHLEN MAX_PATH
    #define JRE_JVM_SUBDIR "\\jre\\bin\\"
    #define JVM_DLL "jvm.dll"

#else

    #include <dlfcn.h>

    #if defined(i386)
        #define ARCH "i386"
    #elif defined(ia64)
        #define ARCH "ia64"
    #else
        #define ARCH "sparcv9"
    #endif

    #define FILE_SEPARATOR "/"
    #define MAXPATHLEN PATH_MAX
    #define JRE_JVM_SUBDIR "/jre/lib/" ARCH "/"
    #define JVM_DLL "libjvm.so"

#endif

#ifdef WIN32

static int getStringFromRegistry(HKEY key, const char *name, char *buf, int maxlen)
{
    DWORD type, size;

    if (RegQueryValueEx(key, name, 0, &type, 0, &size) == 0
        && type == REG_SZ
        && (size < (unsigned int)maxlen)) {
        if (RegQueryValueEx(key, name, 0, 0, buf, &size) == 0) {
            return JNI_TRUE;
        }
    }
    return JNI_FALSE;
}

static int getJVMPathFromRegistry(const char *jvmtype, char *jvmpath, int maxlen)
{
    const char *errormsg;
    HKEY key, subkey;
    char version[MAXPATHLEN];
    char javahome[MAXPATHLEN];
    struct stat s;

    /* Find the current version of the JRE */
    if (RegOpenKeyEx(HKEY_LOCAL_MACHINE,
        "Software\\JavaSoft\\Java Runtime Environment", 0, KEY_READ, &key) != 0) {
        errormsg = "Error opening JRE registry key";
        goto error;
    }

    if (!getStringFromRegistry(key, "CurrentVersion", version, sizeof(version))) {
        errormsg = "Failed reading value of CurrentVersion";
        RegCloseKey(key);
        goto error;
    }

    /* Open registry key for current version */
    if (RegOpenKeyEx(key, version, 0, KEY_READ, &subkey) != 0) {
        errormsg = "Error opening JRE current version registry key";
        RegCloseKey(key);
        goto error;
    }

    /* First attempt to get JVM of desired type based on JavaHome value */
    if (getStringFromRegistry(subkey, "JavaHome", javahome, sizeof(javahome))) {
        snprintf(jvmpath, maxlen, "%s\\bin\\%s\\" JVM_DLL, javahome, jvmtype);
        if (stat(jvmpath, &s) == 0) {
            goto success;
        }
    }

    /* Finally attempt to get default JVM from RuntimeLib value */
    if (getStringFromRegistry(subkey, "RuntimeLib", jvmpath, maxlen)) {
        if (stat(jvmpath, &s) == 0) {
            goto success;
        }
    }

    RegCloseKey(key);
    RegCloseKey(subkey);

error:
    fprintf(stderr, "Error getting Java home from registry: %s\n", errormsg);
    return JNI_ERR;

success:
    RegCloseKey(key);
    RegCloseKey(subkey);
    return JNI_OK;
}

#endif

int getJVMPath(const char *jvmtype, char *jvmpath, int maxlen)
{
    const char *javahome;
    struct stat s;

    javahome = getenv("JAVA_HOME");
    if (javahome != NULL) {
        snprintf(jvmpath, maxlen,
            "%s" JRE_JVM_SUBDIR "%s" FILE_SEPARATOR JVM_DLL,
            javahome, jvmtype);
        if (stat(jvmpath, &s) == 0) {
            return JNI_OK;
        }
    }

#ifdef WIN32
    return getJVMPathFromRegistry(jvmtype, jvmpath, maxlen);
#else
    return JNI_ERR;
#endif
}

int loadJVMLibrary(const char *jvmpath, CreateJavaVMPtr *ppfcvm)
{
    const char *errormsg;

#ifdef WIN32
    HINSTANCE handle;

    /* Load JVM DLL */
    if ((handle = LoadLibrary(jvmpath)) == 0) {
        errormsg = "LoadLibrary failed";
        goto error;
    }

    /* Get JNI_CreateJavaVM entry point */
    *ppfcvm = (CreateJavaVMPtr)GetProcAddress(handle, "JNI_CreateJavaVM");
    if (*ppfcvm == NULL) {
        errormsg = "GetProcAddress for JNI_CreateJavaVM failed";
        goto error;
    }
#else
    void *libjvm;

    /* Load JVM shared object */
    libjvm = dlopen(jvmpath, RTLD_NOW | RTLD_GLOBAL);
    if (libjvm == NULL) {
        errormsg = dlerror();
        goto error;
    }

    /* Get JNI_CreateJavaVM entry point */
    *ppfcvm = (CreateJavaVMPtr)dlsym(libjvm, "JNI_CreateJavaVM");
    if (*ppfcvm == NULL) {
        errormsg = dlerror();
        goto error;
    }
#endif

    return JNI_OK;

error:
    fprintf(stderr, "Error loading Java VM (%s): %s\n", jvmpath, errormsg);
    return JNI_ERR;
}

int createJVM(CreateJavaVMPtr pfcvm, JavaVMInitArgs *pargs,
    JavaVM **ppvm, JNIEnv **ppenv)
{
    jint res;

    res = pfcvm(ppvm, (void **)ppenv, pargs);
    return res;
}

static jobjectArray buildStringArray(JNIEnv *penv, int count, const char **strs)
{
    jclass stringClass;
    jarray stringArray;
    int i;

    stringClass = (*penv)->FindClass(penv, "java/lang/String");
    if (stringClass == NULL) return NULL;

    stringArray = (*penv)->NewObjectArray(penv, count, stringClass, NULL);
    if (stringArray == NULL) return NULL;

    for (i = 0; i < count; i++) {
        jstring str = (*penv)->NewStringUTF(penv, *strs++);
        if (str == NULL) return NULL;
        (*penv)->SetObjectArrayElement(penv, stringArray, i, str);
        (*penv)->DeleteLocalRef(penv, str);
    }

    return stringArray;
}

int executeClass(JNIEnv *penv, const char *classname, int argc, const char **argv)
{
    const char *errormsg;
    jclass mainClass;
    jmethodID mainID;
    jobjectArray mainArgs;

    /* Load class */
    mainClass = (*penv)->FindClass(penv, classname);
    if (mainClass == NULL) {
        errormsg = "FindClass failed";
        goto java_error;
    }

    /* Get main method */
    mainID = (*penv)->GetStaticMethodID(penv, mainClass, "main",
        "([Ljava/lang/String;)V");
    if (mainID == NULL) {
        errormsg = "GetStaticMethodID for main failed";
        goto java_error;
    }

    /* Build argument array */
    mainArgs = buildStringArray(penv, argc, argv);
    if (mainArgs == NULL) {
        errormsg = "Building argument array failed";
        goto java_error;
    }

    /* Invoke main method. */
    (*penv)->CallStaticVoidMethod(penv, mainClass, mainID, mainArgs);
    if ((*penv)->ExceptionOccurred(penv)) {
        errormsg = "Calling main failed";
        goto java_error;
    }

    return JNI_OK;

java_error:
    if ((*penv)->ExceptionOccurred(penv)) {
        (*penv)->ExceptionDescribe(penv);
    } else {
        fprintf(stderr, "Error executing class (%s): %s\n", classname, errormsg);
    }
    return JNI_ERR;
}

int destroyJVM(JavaVM *pvm)
{
    jint res;

    /*
     * "Detach the current thread so that it appears to
     * have exited when the application's main method exits."
     */
    if ((res = (*pvm)->DetachCurrentThread(pvm)) != JNI_OK) {
        fprintf(stderr, "Could not detach main thread\n");
        return res;
    }

    /*
     * "JDK 1.2 still does not support VM unloading.
     * DestroyJavaVM always returns an error code."
     */
    (*pvm)->DestroyJavaVM(pvm);
    return JNI_OK;
}

