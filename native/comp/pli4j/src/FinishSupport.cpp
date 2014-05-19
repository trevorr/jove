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
 * Contains functions used to handle calling $finish from Java.
 * Calling $finish with Java on the call stack can cause the simulation to
 * crash. This module handles that by maintaining a Java entry count, and if
 * $finish is called while it is greater than zero, the PLI finish call is
 * deferred until it becomes zero.
 *
 * \author Trevor Robinson
 */

// module header
#include "FinishSupport.h"

// external component headers
#include "vpi_user.h"
#include "veriuser.h"

// system headers
#include <assert.h>

// private data
static int javaEntryCount = 0;
static bool finishOnJavaExit = false;

static void do_finish()
{
#ifdef NO_VPI_CONTROL
    #ifdef PLI_DEBUG
    cout << "tf_dofinish" << endl;
    #endif
    tf_dofinish();
#else
    #ifdef PLI_DEBUG
    cout << "vpi_control(vpiFinish)" << endl;
    #endif
    vpi_control(vpiFinish, 1);
#endif
}

void enterJava()
{
    ++javaEntryCount;
}

void exitJava()
{
    --javaEntryCount;
    assert(javaEntryCount >= 0);
    if (javaEntryCount == 0 && finishOnJavaExit) {
#ifdef JVM_DEBUG
        cout << "Returned from JVM; executing PLI.finish()" << endl;
#endif
        finishOnJavaExit = false;
        do_finish();
    }
}

void finishCalled()
{
    if (javaEntryCount == 0) {
        do_finish();
    } else {
#ifdef JVM_DEBUG
        cout << "PLI.finish() called from JVM; flagging for execution on return" << endl;
#endif
        finishOnJavaExit = true;
    }
}
