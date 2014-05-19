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

package com.newisys.dv.ifgen.schemaprinter;

import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

import com.newisys.dv.ifgen.schema.IfgenNameKind;
import com.newisys.dv.ifgen.schema.IfgenPackage;
import com.newisys.dv.ifgen.schema.IfgenPort;
import com.newisys.dv.ifgen.schema.IfgenSchemaMember;
import com.newisys.dv.ifgen.schema.IfgenSchemaObject;
import com.newisys.io.IndentWriter;
import com.newisys.langschema.Name;
import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.schemaprinter.SchemaPrinter;
import com.newisys.schemaprinter.WrappedIOException;
import com.newisys.schemaprinter.java.ImportManager;
import com.newisys.util.text.DefaultTokenFormatter;
import com.newisys.util.text.TokenFormatter;

/**
 * Generates Ifgen source text corresponding to a schema.
 * 
 * @author Trevor Robinson
 */
public class IfgenSchemaPrinter
    extends SchemaPrinter
{
    public void print(IfgenSchemaObject obj, Writer writer)
        throws IOException
    {
        try
        {
            TokenFormatter fmt = getTokenFormatter(writer);
            SchemaObjectPrinter smp = new SchemaObjectPrinter(new BasePrinter(
                fmt, new ImportManager()));
            obj.accept(smp);
            fmt.flush();
        }
        catch (WrappedIOException e)
        {
            throw e.getIOException();
        }
    }

    public String toString(IfgenSchemaObject obj)
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

    public static File getPackageRoot(IfgenPackage pkg, File sourceRoot)
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

    public void printFile(
        Collection< ? extends IfgenSchemaMember> members,
        IfgenPackage pkg,
        File file)
        throws IOException
    {
        System.out.println("Writing Ifgen source: " + file.getPath());

        // create and open the file
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter fileBufWriter = new BufferedWriter(fileWriter);

        // print the package declaration, if any
        if (pkg != null)
        {
            fileBufWriter.write("package ");
            fileBufWriter.write(pkg.getName().getCanonicalName());
            fileBufWriter.write(";");
            fileBufWriter.newLine();
            fileBufWriter.newLine();
        }

        // create a buffer and formatter
        CharArrayWriter bufWriter = new CharArrayWriter(8192);
        IndentWriter indentWriter = new IndentWriter(bufWriter);
        TokenFormatter fmt = new DefaultTokenFormatter(indentWriter,
            getMargin());
        ImportManager importMgr = new ImportManager();
        SchemaObjectPrinter printer = new SchemaObjectPrinter(new BasePrinter(
            fmt, importMgr));

        // exclude declared type identifiers from imports
        for (IfgenSchemaMember member : members)
        {
            if (member instanceof NamedObject)
            {
                NamedObject namedMember = (NamedObject) member;
                Name name = namedMember.getName();
                NameKind nameKind = name.getKind();
                if (nameKind == IfgenNameKind.TYPE)
                {
                    importMgr.addNoImport(name.getIdentifier());
                }
            }
        }

        // print all members
        boolean first = true;
        for (IfgenSchemaMember member : members)
        {
            // suppress reverse ports
            if (member instanceof IfgenPort)
            {
                IfgenPort port = (IfgenPort) member;
                if (port.isReverse()) continue;
            }

            if (!first) fmt.newLine();
            member.accept(printer);
            first = false;
        }

        // flush the formatter and indent writer to the buffer
        fmt.flush();

        // print the import directives
        importMgr.printImports(fileBufWriter);

        // write the contents of the buffer to the file
        bufWriter.writeTo(fileBufWriter);

        // close the writers
        fmt.close();
        fileBufWriter.close();
    }
}
