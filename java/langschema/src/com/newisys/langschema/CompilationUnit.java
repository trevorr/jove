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
 * Represents an independent compilation unit from the source code.
 * 
 * While compilation units are not conceptually part of the schema, it is
 * necessary to segregate schema objects from different compilation units in the
 * processing of languages that use compilation units for namespace management
 * and access control.
 * 
 * @author Trevor Robinson
 */
public interface CompilationUnit
    extends NamedObject, Container, Scope
{
    /**
     * Returns the canonical path to the source file for this compilation unit.
     *
     * @return String
     */
    String getSourcePath();

    /**
     * Returns a list of all the members in this compilation unit.
     *
     * @return List of CompilationUnitMember
     * @see com.newisys.langschema.Container#getMembers()
     */
    List< ? extends CompilationUnitMember> getMembers();
}
