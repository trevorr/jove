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

import java.io.Serializable;
import java.util.List;

/**
 * Base interface for objects representing the members of a schema.
 * 
 * @author Trevor Robinson
 */
public interface SchemaObject
    extends Serializable
{
    /**
     * Returns the schema that contains this object.
     *
     * @return Schema
     */
    Schema getSchema();

    /**
     * Returns a list of the annotations associated with this object.
     *
     * @return List of Annotation
     */
    List< ? extends Annotation> getAnnotations();

    /**
     * Returns a brief string representation of this schema object that
     * describes important attributes such as its type and name.
     *
     * @return String
     * @see #toSourceString()
     * @see #toString()
     */
    String toDebugString();

    /**
     * Returns a string representation of the source code for this schema
     * object.
     *
     * @return String
     * @see #toDebugString()
     * @see #toString()
     */
    String toSourceString();

    /**
     * Returns either the debug or source string representation of this schema
     * object, depending on the configuration of the schema.
     *
     * @return String
     * @see #toDebugString()
     * @see #toSourceString()
     */
    String toString();
}
