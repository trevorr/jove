/*
 * LangSchema-Java - Programming Language Modeling Classes for Java (TM)
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

package com.newisys.langschema.java;

/**
 * Exception thrown when a field lookup fails because the field was not found.
 * 
 * @author Trevor Robinson
 */
public final class FieldNotFoundException
    extends RuntimeException
{
    private static final long serialVersionUID = 3760847852528022838L;

    private final JavaStructuredType type;
    private final String id;

    public FieldNotFoundException(JavaStructuredType type, String id)
    {
        this.type = type;
        this.id = id;
    }

    public String getMessage()
    {
        return "Field " + id + " not found in type " + type.toReferenceString();
    }
}
