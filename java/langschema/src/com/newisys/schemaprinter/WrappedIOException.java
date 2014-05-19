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

package com.newisys.schemaprinter;

import java.io.IOException;

/**
 * RuntimeException wrapper for IOExceptions used to allow IOExceptions to
 * propagate as unchecked exceptions through Visitor interfaces.
 * 
 * @author Trevor Robinson
 */
public final class WrappedIOException
    extends RuntimeException
{
    private static final long serialVersionUID = 3258415049248027448L;

    public WrappedIOException(IOException cause)
    {
        super(cause);
    }

    public IOException getIOException()
    {
        return (IOException) getCause();
    }
}
