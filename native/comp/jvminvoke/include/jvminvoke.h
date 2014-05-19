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

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef jint (JNICALL *CreateJavaVMPtr)(JavaVM **ppvm, void **ppenv, void *pargs);

int getJVMPath(const char *jvmtype, char *jvmpath, int maxlen);
int loadJVMLibrary(const char *jvmpath, CreateJavaVMPtr *ppfcvm);
int createJVM(CreateJavaVMPtr pfcvm, JavaVMInitArgs *pargs,
    JavaVM **ppvm, JNIEnv **ppenv);
int executeClass(JNIEnv *penv, const char *classname, int argc, const char **argv);
int destroyJVM(JavaVM *pvm);

#ifdef __cplusplus
} /* extern "C" */
#endif
