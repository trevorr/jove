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
 * Compile-time configuration defines.
 *
 * \author Trevor Robinson
 */

#ifndef Configuration_h_included
#define Configuration_h_included

//#define PLI_DEBUG
//#define JVM_DEBUG
//#define OVA_DEBUG

#define SIMULATOR_VCS

#ifdef SIMULATOR_VCS
    #define NO_VPI_CONTROL
    #define USE_TF_SYNCHRONIZE
    #define USE_OVA
#else
    #define REGISTER_VPI_TASKS
#endif

// iostream is used for debugging
#if defined(PLI_DEBUG) || defined(JVM_DEBUG) || defined(OVA_DEBUG)
#include <iostream>
using std::cout;
using std::cerr;
using std::endl;
#endif

#endif // Configuration_h_included
