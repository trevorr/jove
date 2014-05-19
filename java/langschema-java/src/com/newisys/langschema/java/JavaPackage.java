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

import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.Namespace;
import com.newisys.langschema.util.NameTable;

/**
 * Represents a Java package.
 * 
 * @author Trevor Robinson
 */
public final class JavaPackage
    extends JavaSchemaObjectImpl
    implements Namespace, JavaSchemaMember, JavaPackageMember
{
    private final JavaName name;
    private JavaPackage pkg;
    private final List<JavaPackageMember> members = new LinkedList<JavaPackageMember>();
    private final NameTable nameTable = new NameTable();

    public JavaPackage(JavaSchema schema, String id, JavaPackage pkg)
    {
        super(schema);
        this.name = new JavaName(id, JavaNameKind.PACKAGE, pkg);
        this.pkg = pkg;
    }

    public JavaName getName()
    {
        return name;
    }

    public JavaPackage getPackage()
    {
        return pkg;
    }

    public void setPackage(JavaPackage pkg)
    {
        this.pkg = pkg;
        name.setNamespace(pkg);
    }

    public void addMember(JavaPackageMember member)
    {
        member.setPackage(this);
        members.add(member);
        nameTable.addObject(member);
    }

    public List<JavaPackageMember> getMembers()
    {
        return members;
    }

    public Iterator<NamedObject> lookupObjects(String identifier, NameKind kind)
    {
        return nameTable.lookupObjects(identifier, kind);
    }

    public void accept(JavaSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(JavaSchemaMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(JavaPackageMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "package " + name;
    }
}
