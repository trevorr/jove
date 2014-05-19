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

import com.newisys.langschema.LabeledStatement;

/**
 * Represents a Java labeled statement.
 * 
 * @author Trevor Robinson
 */
public final class JavaLabeledStatement
    extends JavaStatement
    implements LabeledStatement
{
    private final JavaName name;
    private JavaStatement statement;

    public JavaLabeledStatement(
        JavaSchema schema,
        String id,
        JavaStatement statement)
    {
        super(schema);
        this.name = new JavaName(id, JavaNameKind.LABEL, null);
        if (statement != null)
        {
            statement.setContainingStatement(this);
        }
        this.statement = statement;
    }

    public JavaLabeledStatement(JavaSchema schema, String id)
    {
        this(schema, id, null);
    }

    public JavaName getName()
    {
        return name;
    }

    public JavaStatement getStatement()
    {
        return statement;
    }

    public void setStatement(JavaStatement statement)
    {
        if (statement != null)
        {
            statement.setContainingStatement(this);
        }
        this.statement = statement;
    }

    public void accept(JavaStatementVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "labeled statement " + name;
    }
}
