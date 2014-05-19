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
 * Base interface for schema objects that contain other schema objects.
 * 
 * Note that this interface does not imply any naming relationship between the
 * objects that implement it and the objects contained by those objects. In
 * fact, there is no requirement that the objects have names at all.
 * 
 * @author Trevor Robinson
 */
public interface Container
{
    /**
     * Returns a list of all the schema objects in this container.
     *
     * Although the general contract of this method is that it may return a list
     * of any objects derived from SchemaObject, derived classes may further
     * restrict the types of objects returned.
     *
     * @return List of SchemaObject
     */
    List< ? extends SchemaObject> getMembers();
}
