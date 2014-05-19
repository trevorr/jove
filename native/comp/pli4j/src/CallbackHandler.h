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
 * Interface to the callback dispatch functions.
 *
 * \author Trevor Robinson
 * \author Jon Nall (tf_isynchronize workaround)
 */

#ifndef CallbackHandler_h_included
#define CallbackHandler_h_included

// component configuration header
#include "Configuration.h"

// component headers
#include "CallbackInfo.h"

// external component headers
#include "vpi_user.h"
#include "jnicpp.h"

// system headers
#include <list>

extern jnicpp::JVM* pvm;
extern jnicpp::JObject* pPliObj;

#ifdef USE_TF_SYNCHRONIZE
extern char* misctfInstance;

typedef std::list<CallbackInfo*> CallbackInfoList;
extern CallbackInfoList rwSynchCallbacks;
#endif

extern "C" {

PLI_INT32 pli4j_cb_rtn(p_cb_data cb_data_p);
int pli4j_misctf(char *user_data, int reason);

} // extern "C"

#endif // CallbackHandler_h_included
