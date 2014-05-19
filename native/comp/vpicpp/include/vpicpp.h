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

#ifndef vpicpp_h_included
#define vpicpp_h_included

#include <exception>
#include <string>

#include "vpi_user.h"

namespace vpicpp {

//////////////////////////////////////////////////////////////////////

vpiHandle getHandle(int type, vpiHandle refHandle = NULL);

int getIntProperty(int propType, vpiHandle obj);
std::string getStringProperty(int propType, vpiHandle obj);

int getIntValue(vpiHandle obj);
std::string getStringValue(vpiHandle obj);

//////////////////////////////////////////////////////////////////////

class VPIException : public std::exception
{
public:
    VPIException(const std::string& msg) throw();
    VPIException(const VPIException& rhs) throw();

    virtual ~VPIException() throw();

    virtual const char* what() const throw();

private:
    const std::string m_msg;
};

//////////////////////////////////////////////////////////////////////

class VPIHandle
{
public:
    VPIHandle(int type, vpiHandle refHandle = NULL);

private:
    // copy constructor not supported
    VPIHandle(const VPIHandle& that);

    // assignment operator not supported
    VPIHandle& operator=(const VPIHandle& that);

public:
    virtual ~VPIHandle() throw();

    // automatic conversion to contained jobject
    operator vpiHandle() const throw();

    int getIntProperty(int propType);
    std::string getStringProperty(int propType);

private:
    vpiHandle m_handle;
};

//////////////////////////////////////////////////////////////////////

class VPIIterator
{
public:
    VPIIterator(int type, vpiHandle refHandle = NULL);

private:
    // copy constructor not supported
    VPIIterator(const VPIIterator& that);

    // assignment operator not supported
    VPIIterator& operator=(const VPIIterator& that);

public:
    virtual ~VPIIterator() throw();

    vpiHandle next();

private:
    vpiHandle m_handle;
};

int getIteratorCount(int type, vpiHandle refHandle = NULL);

//////////////////////////////////////////////////////////////////////

}

#include "vpicpp.inl"

#endif // vpicpp_h_included
