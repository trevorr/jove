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
 * Contains the CallbackInfo class.
 *
 * \author Trevor Robinson
 */

#ifndef CallbackInfo_h_included
#define CallbackInfo_h_included

// component configuration header
#include "Configuration.h"

// external component headers
#include "vpi_user.h"
#include "jnicpp.h"

class CallbackInfo
{
public:
    CallbackInfo(JNIEnv* penv, jobject callback, jint reason) :
        m_callback(penv, callback, false),
        m_reason(reason),
        m_handle(NULL),
        m_valid(true)
    {
        // done
    }

    ~CallbackInfo()
    {
        m_valid = false;
    }

    void assertValid() const
    {
        assert(m_valid);
    }

    jobject getCallback() const
    {
        return m_callback;
    }

    void release()
    {
        m_callback.release();
    }

    jint getReason() const
    {
        return m_reason;
    }

    vpiHandle getHandle() const
    {
        return m_handle;
    }

    void setHandle(vpiHandle handle)
    {
        m_handle = handle;
    }

private:
    jnicpp::JObject m_callback;
    const jint m_reason;
    vpiHandle m_handle;
    bool m_valid;
};

#endif // CallbackInfo_h_included
