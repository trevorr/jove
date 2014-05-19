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

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import com.newisys.langschema.java.JavaAnnotationType;
import com.newisys.langschema.java.JavaClass;
import com.newisys.langschema.java.JavaEnum;
import com.newisys.langschema.java.JavaInterface;
import com.newisys.langschema.java.JavaPackage;
import com.newisys.langschema.java.JavaPackageMember;
import com.newisys.langschema.java.JavaPackageMemberVisitor;
import com.newisys.langschema.java.JavaSchemaMember;
import com.newisys.langschema.java.JavaSchemaMemberVisitor;

/**
 * Used to print Java package members to individual source files.
 * 
 * @author Trevor Robinson
 */
public class FilePackageMemberPrinter
    implements JavaSchemaMemberVisitor, JavaPackageMemberVisitor
{
    private final JavaSchemaPrinter schemaPrinter;
    private final File pkgRoot;

    public FilePackageMemberPrinter(
        JavaSchemaPrinter schemaPrinter,
        File pkgRoot)
    {
        this.schemaPrinter = schemaPrinter;
        this.pkgRoot = pkgRoot;
    }

    public void printSchemaMembers(Collection members)
    {
        Iterator iter = members.iterator();
        while (iter.hasNext())
        {
            JavaSchemaMember member = (JavaSchemaMember) iter.next();
            member.accept(this);
        }
    }

    public void printPackageMembers(Collection members)
    {
        Iterator iter = members.iterator();
        while (iter.hasNext())
        {
            JavaPackageMember member = (JavaPackageMember) iter.next();
            member.accept(this);
        }
    }

    public void visit(JavaPackage obj)
    {
        File newPkgRoot = new File(pkgRoot, obj.getName().getIdentifier());
        FilePackageMemberPrinter visitor = new FilePackageMemberPrinter(
            schemaPrinter, newPkgRoot);
        visitor.printPackageMembers(obj.getMembers());
    }

    public void visit(JavaAnnotationType obj)
    {
        FilePrinter fp = new FilePrinter(schemaPrinter, obj, pkgRoot);
        fp.printAnnotationType(obj);
        fp.close();
    }

    public void visit(JavaClass obj)
    {
        FilePrinter fp = new FilePrinter(schemaPrinter, obj, pkgRoot);
        fp.printClass(obj);
        fp.close();
    }

    public void visit(JavaEnum obj)
    {
        FilePrinter fp = new FilePrinter(schemaPrinter, obj, pkgRoot);
        fp.printEnum(obj);
        fp.close();
    }

    public void visit(JavaInterface obj)
    {
        FilePrinter fp = new FilePrinter(schemaPrinter, obj, pkgRoot);
        fp.printInterface(obj);
        fp.close();
    }
}
