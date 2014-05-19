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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.newisys.dv.ifgen.schemaprinter.IfgenSchemaPrinter;
import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.Schema;
import com.newisys.langschema.Scope;
import com.newisys.langschema.util.NameTable;

/**
 * Represents an Ifgen schema.
 * 
 * @author Trevor Robinson
 * @author Jon Nall
 */
public final class IfgenSchema
    implements Schema
{
    private static final long serialVersionUID = 3257562914868114992L;

    private final List<IfgenSchemaMember> members = new LinkedList<IfgenSchemaMember>();
    private final NameTable nameTable = new NameTable();
    private boolean useSourceString = true;
    private IfgenSchemaPrinter defaultPrinter;

    // Static types
    public final IfgenBitType BIT_TYPE = new IfgenBitType(this);
    public final IfgenIntegerType INTEGER_TYPE = new IfgenIntegerType(this);
    public final IfgenStringType STRING_TYPE = new IfgenStringType(this);

    // Map to keep track of dynamic types
    private final Map<IfgenType, IfgenSetType> setTypes = new HashMap<IfgenType, IfgenSetType>();
    private final Map<IfgenName, IfgenEnumType> enumTypes = new HashMap<IfgenName, IfgenEnumType>();

    public IfgenSchema()
    {
        addSetType(BIT_TYPE);
        addSetType(INTEGER_TYPE);
        addSetType(STRING_TYPE);
    }

    public List<IfgenSchemaMember> getMembers()
    {
        return members;
    }

    public void addMember(IfgenSchemaMember member)
    {
        members.add(member);
        if (member instanceof NamedObject)
        {
            nameTable.addObject((NamedObject) member);
        }
    }

    public IfgenSetType addSetType(IfgenType type)
    {
        if (type == null)
        {
            return null;
        }

        if (!setTypes.containsKey(type))
        {
            setTypes.put(type, new IfgenSetType(this, type));
        }

        return setTypes.get(type);
    }

    public IfgenSetType getSetType(IfgenType type)
    {
        return setTypes.get(type);
    }

    public IfgenEnumType addEnumType(IfgenName name)
    {
        if (name == null)
        {
            return null;
        }

        if (!enumTypes.containsKey(name))
        {
            enumTypes.put(name, new IfgenEnumType(this, name));
        }

        return enumTypes.get(name);
    }

    public IfgenEnumType getEnumType(IfgenName name)
    {
        return enumTypes.get(name);
    }

    public Iterator<NamedObject> lookupObjects(String identifier, NameKind kind)
    {
        return nameTable.lookupObjects(identifier, kind);
    }

    public boolean isUseSourceString()
    {
        return useSourceString;
    }

    public void setUseSourceString(boolean useSourceString)
    {
        this.useSourceString = useSourceString;
    }

    protected IfgenSchemaPrinter createDefaultPrinter()
    {
        return new IfgenSchemaPrinter();
    }

    public IfgenSchemaPrinter getDefaultPrinter()
    {
        if (defaultPrinter == null)
        {
            defaultPrinter = createDefaultPrinter();
            defaultPrinter.setCollapseBodies(true);
        }
        return defaultPrinter;
    }

    public void setDefaultPrinter(IfgenSchemaPrinter printer)
    {
        defaultPrinter = printer;
    }

    public IfgenPackage getPackage(String qname, boolean create)
    {
        IfgenPackage pkg = null;
        IfgenPackage outerPkg = null;
        Scope scope = this;
        String[] names = qname.split("\\.");
        for (int i = 0; i < names.length; ++i)
        {
            String name = names[i];
            Iterator iter = scope.lookupObjects(name, IfgenNameKind.PACKAGE);
            if (iter.hasNext())
            {
                pkg = (IfgenPackage) iter.next();
                assert (!iter.hasNext());
            }
            else if (create)
            {
                IfgenName jname = new IfgenName(name, IfgenNameKind.PACKAGE,
                    outerPkg);
                pkg = new IfgenPackage(this, jname, outerPkg);
                if (outerPkg != null)
                {
                    outerPkg.addMember(pkg);
                }
                else
                {
                    addMember(pkg);
                }
            }
            else
            {
                // package not found and not okay to create
                return null;
            }
            scope = outerPkg = pkg;
        }
        return pkg;
    }
}
