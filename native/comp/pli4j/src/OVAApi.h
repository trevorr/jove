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
 * Interface to the OVA API and associated callback functions.
 *
 * \author Jon Nall
 */

#ifndef OVAApi_h_included
#define OVAApi_h_included

// All OVA API weirdness is handled in this file to keep it in one
// place. 

#ifdef USE_OVA
extern "C"
{
// acc_user.h is included first because the version in $VCS_HOME does
// braindead stuff like redefining bool to int. The #ifdef ACC_USER_H
// guard at the top of the file prevents the VCS version from being used
#include "acc_user.h"

// should be found in $VCS_HOME/include
#include "ovaApiIncludeC.h"

// callback routines defined in CallbackHandler.cpp
void pli4j_ovaassert_count_cb_rtn(Ova_AssertEvent event_type, Ova_Time sim_time_unused,
        Ova_AssertID assert_id, Ova_AssertAttemptID attempt_id,
        Ova_UserData user_data);
void pli4j_ovaeng_cb_rtn(Ova_EngEvent event_type, Ova_Time sim_time,
        Ova_UserData user_data);
void pli4j_ovaassert_cb_rtn(Ova_AssertEvent event_type, Ova_Time sim_time,
        Ova_AssertID assert_id, Ova_AssertAttemptID attempt_id,
        Ova_UserData user_data);

// these methods are not available in ovaApiIncludeC.h, but are used by the
// JNI layer
extern Ova_Bool ovaRemoveEngListener(Ova_EngEvent eventID, Ova_UserData udRef); 
extern Ova_Bool ovaRemoveAssertListener(Ova_AssertEvent eventID,
        Ova_AssertID assertID, Ova_UserData udRef); 
}

#endif // USE_OVA

#endif // OVAApi_h_included
