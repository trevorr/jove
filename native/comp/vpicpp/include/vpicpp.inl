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

namespace vpicpp {

//////////////////////////////////////////////////////////////////////

inline int getIntProperty(int propType, vpiHandle obj)
{
    return vpi_get(propType, obj);
}

inline std::string getStringProperty(int propType, vpiHandle obj)
{
    return std::string(vpi_get_str(propType, obj));
}

inline int getIntValue(vpiHandle obj)
{
    s_vpi_value value;
    value.format = vpiIntVal;
    vpi_get_value(obj, &value);
    return value.value.integer;
}

inline std::string getStringValue(vpiHandle obj)
{
    s_vpi_value value;
    value.format = vpiStringVal;
    vpi_get_value(obj, &value);
    return std::string(value.value.str);
}

//////////////////////////////////////////////////////////////////////

void checkException() throw(VPIException);
void convertException(const std::string& msg) throw(VPIException);

//////////////////////////////////////////////////////////////////////

inline VPIException::VPIException(const std::string& msg) throw() :
    m_msg(msg)
{
    // done
}

inline VPIException::VPIException(const VPIException& rhs) throw() :
    m_msg(rhs.m_msg)
{
    // done
}

inline VPIException::~VPIException() throw()
{
    // done
}

inline const char* VPIException::what() const throw()
{
    return m_msg.c_str();
}

//////////////////////////////////////////////////////////////////////

inline VPIHandle::VPIHandle(int type, vpiHandle refHandle) :
    m_handle(getHandle(type, refHandle))
{
    // done
}

inline VPIHandle::~VPIHandle() throw()
{
    vpi_free_object(m_handle);
}

inline VPIHandle::operator vpiHandle() const throw()
{
    return m_handle;
}

inline int VPIHandle::getIntProperty(int propType)
{
    return ::vpicpp::getIntProperty(propType, m_handle);
}

inline std::string VPIHandle::getStringProperty(int propType)
{
    return ::vpicpp::getStringProperty(propType, m_handle);
}

//////////////////////////////////////////////////////////////////////

inline VPIIterator::VPIIterator(int type, vpiHandle refHandle) :
    m_handle(vpi_iterate(type, refHandle))
{
    checkException();
}

inline VPIIterator::~VPIIterator() throw()
{
    if (m_handle != NULL) {
        vpi_free_object(m_handle);
    }
}

inline vpiHandle VPIIterator::next()
{
    if (m_handle != NULL) {
        vpiHandle result = vpi_scan(m_handle);
        if (result == NULL) {
            m_handle = NULL;
        }
        return result;
    } else {
        return NULL;
    }
}

//////////////////////////////////////////////////////////////////////

}
