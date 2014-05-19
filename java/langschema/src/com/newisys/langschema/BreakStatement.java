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

/**
 * Represents a 'break' statement, which exits the labeled or enclosing control
 * statement.
 * 
 * @author Trevor Robinson
 */
public interface BreakStatement
    extends Statement
{
    /**
     * Returns the labeled statement that is the target of this 'break
     * 'statement, or null if the 'break' targets the enclosing statement.
     *
     * @return LabeledStatement
     */
    LabeledStatement getTarget();
}
