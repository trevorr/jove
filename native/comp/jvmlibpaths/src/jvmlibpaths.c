/*
 * jvmlibpaths - Utility to generate Java (TM) VM library search path list
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

#include <stdlib.h>
#include <stdio.h>
#include <limits.h>
#include <string.h>

#include "jvminvoke.h"

/*
 * Generates LD_LIBRARY_PATH with required JVM paths prepended
 * (assuming they are not present already):
 *
 *   $JVMDIR ($JREDIR/lib/$ARCH/$JVMTYPE)
 *   $JVMDIR/.. ($JREDIR/lib/$ARCH)
 *   $JVMDIR/../../../../lib/$ARCH ($JREDIR/../lib/$ARCH)
 */

#ifdef WIN32

    #include <windows.h>
    #define snprintf _snprintf

    #define ENV_SEARCH_PATH "PATH"
    #define FILE_SEPARATOR "\\"
    #define FILE_SEPARATOR_CHAR '\\'
    #define PATH_SEPARATOR ";"
    #define MAXPATHLEN MAX_PATH

    #define PATH_PARTS 4

#else

    #define ENV_SEARCH_PATH "LD_LIBRARY_PATH"
    #define FILE_SEPARATOR "/"
    #define FILE_SEPARATOR_CHAR '/'
    #define PATH_SEPARATOR ":"
    #define MAXPATHLEN PATH_MAX

    #define HAS_ARCH 1
    #define PATH_PARTS 5

#endif

int main(int argc, char **argv)
{
    char *oldpath;
    char *newpath;
    char jvmpath[MAXPATHLEN];

    oldpath = getenv(ENV_SEARCH_PATH);
    newpath = oldpath;

    if (getJVMPath("client", jvmpath, sizeof(jvmpath)) == JNI_OK) {
        char *pathparts[PATH_PARTS];
        int pathpart;

        for (pathpart = 0; pathpart < PATH_PARTS; ++pathpart) {
            char *lastslash;

            lastslash = (char *)strrchr(jvmpath, FILE_SEPARATOR_CHAR);
            if (lastslash) {
                *lastslash = 0;
                pathparts[pathpart] = lastslash + 1;
            } else {
                break;
            }
        }

        if (pathpart == PATH_PARTS) {
            char *jvmtype;
            char *libarch;
            char *jre;
            int newpathsize = 1;
            int needjvmdir = 0;
            int needjrelibarch = 0;
            int needjdklibarch = 0;
            char jvmdir[MAXPATHLEN];
            char jrelibarch[MAXPATHLEN];
            char jdklibarch[MAXPATHLEN];
            char *tail;

            jvmtype = pathparts[1];
#ifdef HAS_ARCH
            *(pathparts[2] - 1) = FILE_SEPARATOR_CHAR;
            libarch = pathparts[3];
            jre = pathparts[4];
#else
            libarch = pathparts[2];
            jre = pathparts[3];
#endif

            snprintf(jvmdir, sizeof(jvmdir),
                "%s" FILE_SEPARATOR
                "%s" FILE_SEPARATOR
                "%s" FILE_SEPARATOR
                "%s",
                jvmpath, jre, libarch, jvmtype);
            if (!strstr(oldpath, jvmdir)) {
                needjvmdir = 1;
                newpathsize += strlen(jvmdir) + 1;

            }

            snprintf(jrelibarch, sizeof(jrelibarch),
                "%s" FILE_SEPARATOR
                "%s" FILE_SEPARATOR
                "%s",
                jvmpath, jre, libarch);
            if (!strstr(oldpath, jrelibarch)) {
                needjrelibarch = 1;
                newpathsize += strlen(jrelibarch) + 1;

            }

            snprintf(jdklibarch, sizeof(jdklibarch),
                "%s" FILE_SEPARATOR
                "%s",
                jvmpath, libarch);
            if (!strstr(oldpath, jdklibarch)) {
                needjdklibarch = 1;
                newpathsize += strlen(jdklibarch) + 1;

            }

            if (oldpath) {
                newpathsize += strlen(oldpath) + 1;
            }

            newpath = malloc(newpathsize);
            tail = newpath;
            if (needjvmdir) {
                tail += sprintf(tail, "%s" PATH_SEPARATOR, jvmdir);
            }
            if (needjrelibarch) {
                tail += sprintf(tail, "%s" PATH_SEPARATOR, jrelibarch);
            }
            if (needjdklibarch) {
                tail += sprintf(tail, "%s" PATH_SEPARATOR, jdklibarch);
            }
            if (oldpath) {
                tail += sprintf(tail, "%s" PATH_SEPARATOR, oldpath);
            }
            if (tail != newpath) {
                --tail;
            }
            *tail = 0;
        }
    }

    if (newpath) {
        printf("%s\n", newpath);
    }
}
