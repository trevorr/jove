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
 * Represents a 'try-catch-finally' statement, which attempts to handle any
 * exceptions thrown in the 'try' block using a set of 'catch' blocks, and
 * ensures that the associated 'finally' block will always be executed
 * regardless of whether an exception is thrown.
 * 
 * @author Trevor Robinson
 */
public interface TryStatement
    extends Statement
{
    /**
     * Returns the block to execute and handle exceptions from.
     *
     * @return Block
     */
    Block getTryBlock();

    /**
     * Returns a list of 'catch' blocks.
     *
     * @return List of TryCatch
     */
    List< ? extends TryCatch> getCatches();

    /**
     * Returns the block to execute after the 'try' block and any chosen 'catch'
     * block.
     *
     * @return Block
     */
    Block getFinallyBlock();
}
