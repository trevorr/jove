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

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the definitions in a particular Ifgen source file.
 * 
 * @author Trevor Robinson
 */
public final class IfgenFile
{
    private File sourceFile;
    private IfgenPackage packageDecl;
    private final List<IfgenWildname> importDecls = new LinkedList<IfgenWildname>();
    private final List<IfgenSchemaMember> definitions = new LinkedList<IfgenSchemaMember>();

    public File getSourceFile()
    {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile)
    {
        this.sourceFile = sourceFile;
    }

    public IfgenPackage getPackageDecl()
    {
        return packageDecl;
    }

    public void setPackageDecl(IfgenPackage packageDecl)
    {
        this.packageDecl = packageDecl;
    }

    public List<IfgenWildname> getImportDecls()
    {
        return importDecls;
    }

    public void addImportDecl(IfgenWildname importDecl)
    {
        importDecls.add(importDecl);
    }

    public List<IfgenSchemaMember> getDefinitions()
    {
        return definitions;
    }

    public void addDefinition(IfgenSchemaMember definition)
    {
        definitions.add(definition);
    }
}
