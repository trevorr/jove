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
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.newisys.io.IndentWriter;
import com.newisys.langschema.java.JavaAnnotationType;
import com.newisys.langschema.java.JavaClass;
import com.newisys.langschema.java.JavaEnum;
import com.newisys.langschema.java.JavaInterface;
import com.newisys.langschema.java.JavaPackage;
import com.newisys.langschema.java.JavaStructuredType;
import com.newisys.schemaprinter.WrappedIOException;
import com.newisys.util.text.DefaultTokenFormatter;
import com.newisys.util.text.TokenFormatter;

/**
 * Used to print Java source files.
 * 
 * @author Trevor Robinson
 */
public class FilePrinter
{
    private final BufferedWriter fileBufWriter;
    private final CharArrayWriter bufWriter;
    private final TokenFormatter fmt;
    private final ImportManager importMgr;
    private final BasePrinter basePrinter;

    public FilePrinter(
        JavaSchemaPrinter schemaPrinter,
        JavaStructuredType type,
        File pkgRoot)
    {
        // determine the file to write to
        File typeFile = getTypeFile(type, pkgRoot);
        System.out.println("Writing Java source: " + typeFile.getPath());

        try
        {
            // create and open the file
            typeFile.getParentFile().mkdirs();
            FileWriter fileWriter = new FileWriter(typeFile);
            fileBufWriter = new BufferedWriter(fileWriter);

            // print the package declaration, if any
            JavaPackage pkg = type.getPackage();
            if (pkg != null)
            {
                fileBufWriter.write("package ");
                fileBufWriter.write(pkg.getName().getCanonicalName());
                fileBufWriter.write(";");
                fileBufWriter.newLine();
                fileBufWriter.newLine();
            }
        }
        catch (IOException e)
        {
            throw new WrappedIOException(e);
        }

        // create a buffer and formatter
        bufWriter = new CharArrayWriter(8192);
        IndentWriter indentWriter = new IndentWriter(bufWriter);
        fmt = new DefaultTokenFormatter(indentWriter, schemaPrinter.getMargin());
        importMgr = new ImportManager();
        basePrinter = schemaPrinter.getBasePrinter(fmt, importMgr);
    }

    public static File getTypeFile(JavaStructuredType type, File pkgRoot)
    {
        String typeID = type.getName().getIdentifier();
        File typeFile = new File(pkgRoot, typeID + ".java");
        return typeFile;
    }

    public void printAnnotationType(JavaAnnotationType obj)
    {
        importMgr.addNoImport(obj.getName().getIdentifier());
        basePrinter.printAnnotationType(obj);
    }

    public void printClass(JavaClass obj)
    {
        importMgr.addNoImport(obj.getName().getIdentifier());
        basePrinter.printClass(obj);
    }

    public void printEnum(JavaEnum obj)
    {
        importMgr.addNoImport(obj.getName().getIdentifier());
        basePrinter.printEnum(obj);
    }

    public void printInterface(JavaInterface obj)
    {
        importMgr.addNoImport(obj.getName().getIdentifier());
        basePrinter.printInterface(obj);
    }

    public void close()
    {
        try
        {
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
        catch (IOException e)
        {
            throw new WrappedIOException(e);
        }
    }
}
