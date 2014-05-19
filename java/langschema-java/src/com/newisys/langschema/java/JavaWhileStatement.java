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

import com.newisys.langschema.WhileStatement;

/**
 * Represents a Java 'while' statement.
 * 
 * @author Trevor Robinson
 */
public final class JavaWhileStatement
    extends JavaStatement
    implements WhileStatement
{
    private final JavaExpression condition;
    private final JavaStatement statement;

    public JavaWhileStatement(JavaExpression condition, JavaStatement statement)
    {
        super(condition.schema);
        assert (condition.getResultType() instanceof JavaBooleanType);
        this.condition = condition;
        statement.setContainingStatement(this);
        this.statement = statement;
    }

    public JavaExpression getCondition()
    {
        return condition;
    }

    public JavaStatement getStatement()
    {
        return statement;
    }

    public void accept(JavaStatementVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "while statement";
    }
}
