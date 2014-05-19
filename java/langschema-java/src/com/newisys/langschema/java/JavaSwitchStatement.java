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

import com.newisys.langschema.Block;
import com.newisys.langschema.Expression;
import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.SwitchStatement;
import com.newisys.langschema.util.NameTable;

/**
 * Represents a Java switch statement.
 * 
 * @author Trevor Robinson
 */
public final class JavaSwitchStatement
    extends JavaStatement
    implements SwitchStatement, Block
{
    private final JavaExpression selector;
    private final List<JavaBlockMember> members = new LinkedList<JavaBlockMember>();
    private final NameTable nameTable = new NameTable();
    private final List<JavaSwitchCase> cases = new LinkedList<JavaSwitchCase>();
    private boolean gotDefaultCase;

    public JavaSwitchStatement(JavaExpression selector)
    {
        super(selector.schema);
        this.selector = selector;
    }

    public Expression getSelector()
    {
        return selector;
    }

    public List<JavaBlockMember> getMembers()
    {
        return members;
    }

    void addMember(JavaBlockMember member)
    {
        member.setContainingStatement(this);
        members.add(member);
        if (member instanceof NamedObject)
        {
            nameTable.addObject((NamedObject) member);
        }
    }

    public Iterator<NamedObject> lookupObjects(String identifier, NameKind kind)
    {
        return nameTable.lookupObjects(identifier, kind);
    }

    public List<JavaSwitchCase> getCases()
    {
        return cases;
    }

    public JavaSwitchValueCase newValueCase()
    {
        JavaSwitchValueCase _case = new JavaSwitchValueCase(this);
        cases.add(_case);
        return _case;
    }

    public JavaSwitchDefaultCase newDefaultCase()
    {
        assert (!gotDefaultCase);
        JavaSwitchDefaultCase _case = new JavaSwitchDefaultCase(this);
        cases.add(_case);
        gotDefaultCase = true;
        return _case;
    }

    public void accept(JavaStatementVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "switch statement";
    }
}
