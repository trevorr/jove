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

package com.newisys.langschema.util;

/**
 * Exception thrown when a language semantic error is detected.
 * 
 * @author Trevor Robinson
 */
public class SemanticException
    extends RuntimeException
{
    private static final long serialVersionUID = 3545520586265801779L;

    public SemanticException()
    {
        super();
    }

    public SemanticException(String message)
    {
        super(message);
    }

    public SemanticException(Throwable cause)
    {
        super(cause);
    }

    public SemanticException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
