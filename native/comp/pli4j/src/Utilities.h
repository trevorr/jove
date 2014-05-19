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
 * Declares some general utility functions.
 *
 * \author Trevor Robinson
 */

#ifndef Utilities_h_included
#define Utilities_h_included

// component configuration header
#include "Configuration.h"

// external component headers
#include "jnicpp.h"

// system headers
#include <exception>

jthrowable throwException(const jnicpp::JEnv& env,
    const jnicpp::JCtorTmpl<jthrowable>& ctor, const char* msg);

void die(const char *message);
void die(const jnicpp::JVMException& e);

#define NO_THROW_PROLOG() \
{ \
    try { \
        jnicpp::JEnv env(penv); \
        try {

#define NO_THROW_EPILOG(failure_return) \
        } \
        catch (jnicpp::JVMException& e) { \
            /* rethrow Java exception */ \
            env.throwException(e.getThrowable()); \
        } \
        catch (std::exception& e) { \
            /* throw RuntimeException for C++ exception */ \
            throwException(env, pjoc->RuntimeException_ctor, e.what()); \
        } \
    } \
    catch (std::exception& e) { \
        /* die on exception while throwing exception */ \
        die(e.what()); \
    } \
    return failure_return; \
}

#endif // Utilities_h_included
