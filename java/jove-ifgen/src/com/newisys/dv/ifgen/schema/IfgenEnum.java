/*
 * Jove - The Open Verification Environment for the Java (TM) Platform
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

package com.newisys.dv.ifgen.schema;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.Namespace;
import com.newisys.langschema.SchemaObject;
import com.newisys.langschema.util.NameTable;

/**
 * Ifgen schema object for enumerations.
 * 
 * @author Jon Nall
 */
public final class IfgenEnum
    extends IfgenSchemaObject
    implements Namespace, IfgenSchemaMember, IfgenPackageMember
{
    private static final long serialVersionUID = -6968919470634786502L;
    private final IfgenEnumType type;
    private final List<IfgenEnumElement> elements = new LinkedList<IfgenEnumElement>();
    private final NameTable nameTable = new NameTable();
    private IfgenPackage pkg;

    public IfgenEnum(IfgenSchema schema, IfgenEnumType type)
    {
        super(schema);
        this.type = type;
    }

    @Override
    public void accept(IfgenSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    public IfgenName getName()
    {
        return type.getName();
    }

    public void accept(IfgenSchemaMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    public IfgenEnumType getType()
    {
        return type;
    }

    public IfgenPackage getPackage()
    {
        return pkg;
    }

    public void setPackage(IfgenPackage pkg)
    {
        this.pkg = pkg;
    }

    public void addElement(IfgenEnumElement element)
    {
        assert (!elements.contains(element));
        elements.add(element);
        nameTable.addObject(element);
    }

    public IfgenEnumElement getElement(String id)
    {
        Iterator< ? extends NamedObject> iter = nameTable.lookupObjects(id,
            IfgenNameKind.EXPRESSION);

        assert (iter.hasNext());
        IfgenEnumElement element = (IfgenEnumElement) iter.next();
        assert (!iter.hasNext());

        return element;
    }

    public List<IfgenEnumElement> getElements()
    {
        return new LinkedList<IfgenEnumElement>(elements);
    }

    public void accept(IfgenPackageMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    public List<IfgenEnumElement> getRange(
        IfgenEnumElement from,
        IfgenEnumElement to)
    {
        assert (elements.contains(from));
        assert (elements.contains(to));

        int fromIdx = elements.indexOf(from);
        int toIdx = elements.indexOf(to);
        int minIdx = Math.min(fromIdx, toIdx);
        int maxIdx = Math.max(fromIdx, toIdx);

        return elements.subList(minIdx, maxIdx + 1);
    }

    @Override
    public String toDebugString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append("enum: ");
        buf.append(type.getName().getIdentifier());
        buf.append("{ ");

        Iterator<IfgenEnumElement> iter = elements.iterator();
        while (iter.hasNext())
        {
            buf.append(iter.next());
            if (iter.hasNext())
            {
                buf.append(", ");
            }
        }

        buf.append(" }");
        return buf.toString();
    }

    public List< ? extends SchemaObject> getMembers()
    {
        return new LinkedList<IfgenEnumElement>(elements);
    }

    public Iterator< ? extends NamedObject> lookupObjects(
        String identifier,
        NameKind kind)
    {
        return nameTable.lookupObjects(identifier, kind);
    }

}
