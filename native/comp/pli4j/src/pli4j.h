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
 * Declares the pli4j library entrypoints.
 *
 * \author Trevor Robinson
 */

#ifndef pli4j_h_included
#define pli4j_h_included

// component configuration header
#include "Configuration.h"

extern "C" {

int pli4j_init(char *user_data);
int pli4j_register_object(char *user_data);
int pli4j_register_signal(char *user_data);
int pli4j_register_verilog_task(char *user_data);
int pli4j_call(char *user_data);
int pli4j_start(char *user_data);
int pli4j_destroy(char *user_data);

void pli4j_register_vpi_tasks(void);

} // extern "C"

#endif // pli4j_h_included
