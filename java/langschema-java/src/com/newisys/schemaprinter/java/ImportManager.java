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

package com.newisys.schemaprinter.java;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.newisys.langschema.Name;
import com.newisys.langschema.Namespace;

/**
 * Manages the set of import declarations for a Java compilation unit.
 * 
 * @author Trevor Robinson
 */
public class ImportManager
{
    private final Map<String, Name> importMap = new LinkedHashMap<String, Name>();
    private final Set<String> noImportSet = new HashSet<String>();

    public ImportManager()
    {
    }

    public void addNoImport(String id)
    {
        noImportSet.add(id);
    }

    public boolean isImported(Name name)
    {
        String id = name.getIdentifier();
        if (noImportSet.contains(id))
        {
            // name cannot be imported, generally because the compilation unit
            // declares a type with the same simple name
            return false;
        }
        Name importedName = importMap.get(id);
        if (importedName == null)
        {
            // no name has been imported for the given ID;
            // add an import for this name
            importMap.put(id, name);
            return true;
        }
        else if (importedName.equals(name))
        {
            // this name has already been imported
            return true;
        }
        else
        {
            // another name with the same ID has already been imported
            return false;
        }
    }

    public void printImports(BufferedWriter writer)
        throws IOException
    {
        boolean gotImport = false;
        Collection<Name> importNames = sortNames(importMap.values());
        for (final Name name : importNames)
        {
            // omit import for java.lang types, which are only in the
            // import map as placeholders
            String pkgName = name.getNamespace().getName().getCanonicalName();
            if (!pkgName.equals("java.lang"))
            {
                writer.write("import ");
                writer.write(name.getCanonicalName());
                writer.write(";");
                writer.newLine();
                gotImport = true;
            }
        }
        if (gotImport) writer.newLine();
    }

    private Collection<Name> sortNames(Collection<Name> collection)
    {
        Name[] names = new Name[collection.size()];
        collection.toArray(names);
        Arrays.sort(names, new Comparator<Name>()
        {
            public int compare(Name name0, Name name1)
            {
                int rank0 = getNameRank(name0);
                int rank1 = getNameRank(name1);
                return (rank0 != rank1) ? rank0 - rank1 : name0
                    .getCanonicalName().compareTo(name1.getCanonicalName());
            }

            private int getNameRank(Name n)
            {
                String id = getTopID(n);
                if (id.equals("java")) return 0;
                if (id.equals("javax")) return 1;
                return 2;
            }

            private String getTopID(Name n)
            {
                while (true)
                {
                    Namespace ns = n.getNamespace();
                    if (ns == null) break;
                    n = ns.getName();
                }
                return n.getIdentifier();
            }
        });
        return Arrays.asList(names);
    }
}
