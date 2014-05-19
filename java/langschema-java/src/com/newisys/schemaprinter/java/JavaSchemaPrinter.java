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
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import com.newisys.langschema.java.JavaPackage;
import com.newisys.langschema.java.JavaPackageMember;
import com.newisys.langschema.java.JavaSchema;
import com.newisys.langschema.java.JavaSchemaObject;
import com.newisys.langschema.java.JavaStructuredType;
import com.newisys.schemaprinter.SchemaPrinter;
import com.newisys.schemaprinter.WrappedIOException;
import com.newisys.util.text.TokenFormatter;

/**
 * Generates Java source text corresponding to a schema.
 * 
 * @author Trevor Robinson
 */
public class JavaSchemaPrinter
    extends SchemaPrinter
{
    protected BasePrinter getBasePrinter(
        TokenFormatter fmt,
        ImportManager importMgr)
    {
        return new BasePrinter(fmt, this, importMgr);
    }

    protected SchemaObjectPrinter getSchemaObjectPrinter(BasePrinter basePrinter)
    {
        return new SchemaObjectPrinter(basePrinter);
    }

    public void print(JavaSchema schema, File sourceRoot)
        throws IOException
    {
        try
        {
            FilePackageMemberPrinter visitor = new FilePackageMemberPrinter(
                this, sourceRoot);
            visitor.printSchemaMembers(schema.getMembers());
        }
        catch (WrappedIOException e)
        {
            throw e.getIOException();
        }
    }

    public void print(JavaSchema schema, Writer writer)
        throws IOException
    {
        try
        {
            TokenFormatter fmt = getTokenFormatter(writer);
            StreamPackageMemberPrinter visitor = new StreamPackageMemberPrinter(
                getBasePrinter(fmt, new ImportManager()));
            visitor.printSchemaMembers(schema.getMembers());
            fmt.flush();
        }
        catch (WrappedIOException e)
        {
            throw e.getIOException();
        }
    }

    public static File getPackageRoot(JavaPackage pkg, File sourceRoot)
    {
        if (pkg != null)
        {
            String pkgPath = pkg.getName().getCanonicalName();
            pkgPath = pkgPath.replace('.', File.separatorChar);
            File pkgRoot = new File(sourceRoot, pkgPath);
            return pkgRoot;
        }
        else
        {
            return sourceRoot;
        }
    }

    public File getTypeFile(JavaStructuredType type, File sourceRoot)
    {
        JavaPackage pkg = type.getPackage();
        File pkgRoot = getPackageRoot(pkg, sourceRoot);
        while (true)
        {
            JavaStructuredType outerType = type.getStructuredType();
            if (outerType == null) break;
            type = outerType;
        }
        return FilePrinter.getTypeFile(type, pkgRoot);
    }

    public void print(JavaPackage pkg, File sourceRoot)
        throws IOException
    {
        try
        {
            FilePackageMemberPrinter visitor = new FilePackageMemberPrinter(
                this, getPackageRoot(pkg, sourceRoot));
            visitor.printPackageMembers(pkg.getMembers());
        }
        catch (WrappedIOException e)
        {
            throw e.getIOException();
        }
    }

    public void print(JavaPackageMember member, File sourceRoot)
        throws IOException
    {
        try
        {
            JavaPackage pkg = member.getPackage();
            FilePackageMemberPrinter visitor = new FilePackageMemberPrinter(
                this, getPackageRoot(pkg, sourceRoot));
            member.accept(visitor);
        }
        catch (WrappedIOException e)
        {
            throw e.getIOException();
        }
    }

    public void print(JavaSchemaObject obj, Writer writer)
        throws IOException
    {
        try
        {
            TokenFormatter fmt = getTokenFormatter(writer);
            SchemaObjectPrinter smp = getSchemaObjectPrinter(getBasePrinter(
                fmt, new ImportManager()));
            obj.accept(smp);
            fmt.flush();
        }
        catch (WrappedIOException e)
        {
            throw e.getIOException();
        }
    }

    public String toString(JavaSchemaObject obj)
    {
        StringWriter writer = new StringWriter(200);
        try
        {
            print(obj, writer);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return writer.toString();
    }
}
