/*
 * vpicpp - Lightweight C++ Wrapper Classes for the Verilog Procedural
 * Interface (VPI)
 * Copyright (C) 2003 Trevor A. Robinson
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

#include "vpicpp.h"

#include <sstream>

namespace vpicpp {

//////////////////////////////////////////////////////////////////////

vpiHandle getHandle(int type, vpiHandle refHandle)
{
    vpiHandle result = vpi_handle(type, refHandle);
    if (result == NULL) {
        std::ostringstream msg;
        msg << "Unable to retrieve handle of type " << type;
        if (refHandle != NULL) {
            msg << " for handle " << refHandle;
        }
        convertException(msg.str());
    }
    return result;
}

//////////////////////////////////////////////////////////////////////

void checkException() throw(VPIException)
{
    s_vpi_error_info info;
    if (vpi_chk_error(&info)) {
        std::ostringstream msg;
        switch (info.state) {
            case vpiCompile:
                msg << "Compile ";
                break;
            case vpiPLI:
                msg << "PLI ";
                break;
            case vpiRun:
                msg << "Runtime ";
                break;
            default:
                msg << "Other ";
                break;
        }
        switch (info.level) {
            case vpiNotice:
                msg << "notice: ";
                break;
            case vpiWarning:
                msg << "warning: ";
                break;
            case vpiError:
                msg << "error: ";
                break;
            case vpiSystem:
                msg << "system error: ";
                break;
            default:
                msg << "internal error: ";
                break;
        }
        if (info.product != NULL) {
            msg << info.product << ": ";
        }
        if (info.message != NULL) {
            msg << info.message;
        }
        if (info.code != NULL) {
            msg << " (" << info.code << ")";
        }
        if (info.file != NULL) {
            msg << " at " << info.file << ", line " << info.line;
        }
        throw VPIException(msg.str());
    }
}

void convertException(const std::string& msg) throw(VPIException)
{
    checkException();
    throw VPIException(msg);
}

//////////////////////////////////////////////////////////////////////

int getIteratorCount(int type, vpiHandle refHandle)
{
    VPIIterator iter(type, refHandle);
    int count = 0;
    while (iter.next() != NULL) {
        ++count;
    }
    return count;
}

//////////////////////////////////////////////////////////////////////

}
