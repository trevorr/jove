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
import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.util.NameTable;

/**
 * Represents a Java statement block.
 * 
 * @author Trevor Robinson
 */
public class JavaBlock
    extends JavaStatement
    implements Block
{
    private final List<JavaBlockMember> members = new LinkedList<JavaBlockMember>();
    private final NameTable nameTable = new NameTable();

    public JavaBlock(JavaSchema schema)
    {
        super(schema);
    }

    public List<JavaBlockMember> getMembers()
    {
        return members;
    }

    public void addMember(JavaBlockMember member)
    {
        addMemberAt(member, members.size());
    }

    public void addMemberAt(JavaBlockMember member, int index)
    {
        member.setContainingStatement(this);
        members.add(index, member);
        if (member instanceof NamedObject)
        {
            nameTable.addObject((NamedObject) member);
        }
    }

    public void addMembers(List members)
    {
        Iterator iter = members.iterator();
        while (iter.hasNext())
        {
            addMember((JavaBlockMember) iter.next());
        }
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
        return "block (" + members.size() + " members)";
    }
}
