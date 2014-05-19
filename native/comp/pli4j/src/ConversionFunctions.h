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
 * Interface to conversion functions used to convert values between Java and
 * Verilog.
 *
 * \author Trevor Robinson
 */

#ifndef ConversionFunctions_h_included
#define ConversionFunctions_h_included

// component configuration header
#include "Configuration.h"

// external component headers
#include "vpi_user.h"
#include "jnicpp.h"

jobject createObjectWrapper(const jnicpp::JObject& pliObj, vpiHandle handle);

jobject getJavaTime(const jnicpp::JEnv& env, const s_vpi_time& time);
jobject getJavaTimeNoThrow(JNIEnv *penv, const s_vpi_time& time) throw();

jobject getJavaValue(const jnicpp::JEnv& env, vpiHandle obj, const s_vpi_value& value);
jobject getJavaValueNoThrow(JNIEnv *penv, vpiHandle obj, const s_vpi_value& value) throw();

jobject getJavaValueFromObject(const jnicpp::JEnv& env, vpiHandle obj);

void getVerilogValue(const jnicpp::JEnv& env, const jnicpp::JObject& obj, s_vpi_value& value);
bool getVerilogValueNoThrow(JNIEnv *penv, jobject obj, s_vpi_value& value) throw();

void freeVerilogValue(s_vpi_value& value) throw();

#endif // ConversionFunctions_h_included
