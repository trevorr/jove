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

import java.util.Iterator;

/**
 * An arbitrary collection of named objects that can be looked up by name.
 * 
 * @author Trevor Robinson
 */
public interface Scope
{
    /**
     * Returns an iterator over the objects in this scope with the given name
     * and kind.
     *
     * @param identifier the identifier to search for
     * @param kind the kind of name to search for
     * @return Iterator of NamedObject
     */
    Iterator< ? extends NamedObject> lookupObjects(
        String identifier,
        NameKind kind);
}
