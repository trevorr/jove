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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.Expression;
import com.newisys.langschema.ForStatement;
import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.Scope;
import com.newisys.langschema.Statement;
import com.newisys.langschema.util.NameTable;

/**
 * Represents a Java 'for' statement.
 * 
 * @author Trevor Robinson
 */
public final class JavaForStatement
    extends JavaStatement
    implements ForStatement, Scope
{
    private final List<JavaBlockMember> initStmtList = new LinkedList<JavaBlockMember>();
    private JavaExpression condition;
    private final List<JavaExpressionStatement> updateStmtList = new LinkedList<JavaExpressionStatement>();
    private JavaStatement statement;
    private final NameTable nameTable = new NameTable();

    public JavaForStatement(JavaSchema schema)
    {
        super(schema);
    }

    public List<JavaBlockMember> getInitStatements()
    {
        return initStmtList;
    }

    public void addInitStatement(JavaBlockMember stmt)
    {
        stmt.setContainingStatement(this);
        initStmtList.add(stmt);
        if (stmt instanceof NamedObject)
        {
            nameTable.addObject((NamedObject) stmt);
        }
    }

    public void addInitStatements(List<JavaBlockMember> stmts)
    {
        for (final JavaBlockMember stmt : stmts)
        {
            addInitStatement(stmt);
        }
    }

    public Expression getCondition()
    {
        return condition;
    }

    public void setCondition(JavaExpression condition)
    {
        assert (condition.getResultType() instanceof JavaBooleanType);
        this.condition = condition;
    }

    public List<JavaExpressionStatement> getUpdateStatements()
    {
        return updateStmtList;
    }

    public void addUpdateStatement(JavaExpressionStatement stmt)
    {
        stmt.setContainingStatement(this);
        updateStmtList.add(stmt);
    }

    public void addUpdateStatements(List<JavaExpressionStatement> stmts)
    {
        for (final JavaExpressionStatement stmt : stmts)
        {
            addUpdateStatement(stmt);
        }
    }

    public Statement getStatement()
    {
        return statement;
    }

    public void setStatement(JavaStatement stmt)
    {
        stmt.setContainingStatement(this);
        this.statement = stmt;
    }

    public Iterator<NamedObject> lookupObjects(String identifier, NameKind kind)
    {
        return nameTable.lookupObjects(identifier, kind);
    }

    public void accept(JavaStatementVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "for statement";
    }
}
