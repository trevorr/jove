/*
 * jvmtest - Java (TM) VM Invocation Test Program
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

int main(int argc, char **argv)
{
    char jvmpath[255];

    if (getJVMPath("client", jvmpath, sizeof(jvmpath)) == JNI_OK) {
        CreateJavaVMPtr pfcvm;

        printf("Found JVM library: %s\n", jvmpath);

        if (loadJVMLibrary(jvmpath, &pfcvm) == JNI_OK) {
            JavaVMOption options[1];
            JavaVMInitArgs args;
            JavaVM *pvm;
            JNIEnv *penv;

            printf("JVM library loaded (create function @ %p)\n", pfcvm);

            options[0].optionString = "-Djava.class.path=.";

            args.version = JNI_VERSION_1_2;
            args.nOptions = 1;
            args.options = options;
            args.ignoreUnrecognized = JNI_FALSE;

            if (createJVM(pfcvm, &args, &pvm, &penv) == JNI_OK) {
                const char* args[1];

                printf("JVM created\n");

                args[0] = "Java";
                executeClass(penv, "JVMTest", 1, args);
                destroyJVM(pvm);
            }
        }
    }
}
