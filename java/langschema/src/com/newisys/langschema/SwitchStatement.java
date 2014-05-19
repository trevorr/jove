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
 * Represents a 'switch' statement, which selects a set of statements to run
 * based on the value of an expression.
 * 
 * @author Trevor Robinson
 */
public interface SwitchStatement
    extends Statement
{
    /**
     * Returns the expression that is evaluated to determine which case to
     * select.
     *
     * @return Expression
     */
    Expression getSelector();

    /**
     * Returns the list of cases for this statement.
     *
     * @return List of SwitchCase
     */
    List< ? extends SwitchCase> getCases();
}
