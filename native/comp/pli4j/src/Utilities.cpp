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
 * Defines some general utility functions.
 *
 * \author Trevor Robinson
 */

// module header
#include "Utilities.h"

// component headers
#include "JavaObjectCache.h"

// external component headers
#include "veriuser.h"

// using declarations
using namespace jnicpp;

jthrowable throwException(const jnicpp::JEnv& env,
    const jnicpp::JCtorTmpl<jthrowable>& ctor, const char* msg)
{
    jstring msgStr = JString::newString(env, msg);
    jthrowable exception = ctor.create(JArguments() << msgStr);
    env.throwException(exception);
    return exception;
}

void die(const char *message)
{
    tf_error("PLI4J: %s\n", message);

    exit(1);
}

void die(const jnicpp::JVMException& e)
{
    tf_error("PLI4J: %s\n", e.what());

    // our JavaObjectCache may not have been initialized; for example, its
    // constructor may have thrown an exception trying to load a class;
    // however, if this is the case, there is no need to print a stack
    // trace because it would be empty anyway
    if (pjoc != NULL) {
        JThrowable throwable = e.getThrowable();
        pjoc->Throwable_printStackTrace.call(throwable);
    }

    exit(1);
}
