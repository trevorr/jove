/*
 * LangSchema - Generic Programming Language Modeling Interfaces
 * Copyright (C) 2005 Newisys, Inc. or its licensors, as applicable.
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

package com.newisys.langschema;

import java.util.List;

/**
 * Represents an interface type, that is, a structured type that can inherit
 * definitions only from other interfaces.
 * 
 * @author Trevor Robinson
 */
public interface Interface
    extends StructuredType
{
    /**
     * Returns a list of the base interfaces this interface extends.
     *
     * @return List of Interface
     */
    List< ? extends Interface> getBaseInterfaces();

    /**
     * Returns a list of the members of this interface.
     *
     * @return List of InterfaceMember
     * @see com.newisys.langschema.Container#getMembers()
     */
    List< ? extends InterfaceMember> getMembers();
}
