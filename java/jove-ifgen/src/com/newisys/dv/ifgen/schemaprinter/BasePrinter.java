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

import com.newisys.dv.ifgen.parser.IfgenParser;
import com.newisys.dv.ifgen.schema.IfgenDirection;
import com.newisys.dv.ifgen.schema.IfgenEdge;
import com.newisys.dv.ifgen.schema.IfgenKeywords;
import com.newisys.dv.ifgen.schema.IfgenPackage;
import com.newisys.dv.ifgen.schema.IfgenSchema;
import com.newisys.dv.ifgen.schema.IfgenSignalType;
import com.newisys.dv.ifgen.schema.IfgenType;
import com.newisys.dv.ifgen.schema.IfgenUnresolvedName;
import com.newisys.dv.ifgen.schema.IfgenWildname;
import com.newisys.langschema.Annotation;
import com.newisys.langschema.BlankLine;
import com.newisys.langschema.BlockComment;
import com.newisys.langschema.InlineComment;
import com.newisys.langschema.Name;
import com.newisys.langschema.Namespace;
import com.newisys.langschema.SchemaObject;
import com.newisys.schemaprinter.SchemaPrinterModule;
import com.newisys.schemaprinter.java.ImportManager;
import com.newisys.util.text.TokenFormatter;

/**
 * Base printer module used to print objects common to all modules.
 * 
 * @author Trevor Robinson
 */
class BasePrinter
    extends SchemaPrinterModule
{
    private final ImportManager importMgr;

    public BasePrinter(TokenFormatter fmt, ImportManager importMgr)
    {
        super(fmt);
        this.importMgr = importMgr;
    }

    public TokenFormatter getFormatter()
    {
        return fmt;
    }

    protected void printComments(SchemaObject obj, boolean leading)
    {
        printComments(obj, leading, false);
    }

    protected void printComments(
        SchemaObject obj,
        boolean leading,
        boolean allowLeadingBlank)
    {
        boolean prevBlank = !allowLeadingBlank;
        for (Annotation ann : obj.getAnnotations())
        {
            if (ann.isLeading() != leading) continue;

            if (ann instanceof BlockComment)
            {
                BlockComment comment = (BlockComment) ann;
                for (String line : comment.getLines())
                {
                    if (!leading) fmt.printSpace();
                    fmt.printToken("//" + line);
                    printNewLine();
                }
                prevBlank = false;
            }
            else if (ann instanceof InlineComment)
            {
                InlineComment comment = (InlineComment) ann;
                if (!leading) fmt.printSpace();
                fmt.printToken("/*" + comment.getText() + "*/");
                if (leading) fmt.printSpace();
                prevBlank = false;
            }
            else if (ann instanceof BlankLine)
            {
                if (!prevBlank) printNewLine();
                prevBlank = true;
            }
        }
    }

    protected void printID(Name name)
    {
        printID(name.getIdentifier());
    }

    protected void printID(String id)
    {
        if (IfgenKeywords.isKeyword(id)) id = "\"" + id + "\"";
        fmt.printToken(id);
    }

    protected void printName(Name name, IfgenPackage pkgContext)
    {
        String id = name.getIdentifier();
        Namespace namespace = name.getNamespace();
        if (namespace != null && namespace != pkgContext)
        {
            // do not attempt to import names defined in the current package
            if ((pkgContext != null && pkgContext.lookupObjects(id,
                name.getKind()).hasNext())
                || !importMgr.isImported(name))
            {
                printNamespace(namespace);
            }
        }
        fmt.printToken(id);
    }

    private void printNamespace(Namespace namespace)
    {
        printNameNoImport(namespace.getName());
        fmt.printLeadingToken(".");
    }

    private void printNameNoImport(Name name)
    {
        Namespace namespace = name.getNamespace();
        if (namespace != null)
        {
            printNamespace(namespace);
        }
        fmt.printToken(name.getIdentifier());
    }

    protected void printQname(IfgenUnresolvedName qname)
    {
        printQname(qname, false);
    }

    private void printQname(IfgenUnresolvedName qname, boolean dotStar)
    {
        if (qname.isValidIdentifiers())
        {
            boolean first = true;
            for (String id : qname.getIdentifiers())
            {
                if (!first) fmt.printLeadingToken(".");
                fmt.printToken(id);
                first = false;
            }
            if (dotStar)
            {
                fmt.printTrailingToken(".");
                fmt.printTrailingToken("*");
            }
        }
        else
        {
            fmt.printToken("\"" + IfgenParser.escape(qname.toString())
                + (dotStar ? ".*" : "") + "\"");
        }
    }

    protected void printWildname(IfgenWildname wildname)
    {
        printQname(wildname.getPackageOrTypeName(), wildname.isImportMembers());
    }

    protected void printSignalType(IfgenSignalType type)
    {
        switch (type)
        {
        case CLOCK:
            fmt.printToken("clock");
            break;
        case INPUT:
            fmt.printToken("input");
            break;
        case OUTPUT:
            fmt.printToken("output");
            break;
        case INOUT:
            fmt.printToken("inout");
            break;
        }
    }

    protected void printDirection(IfgenDirection direction)
    {
        switch (direction)
        {
        case INPUT:
            fmt.printToken("input");
            break;
        case OUTPUT:
            fmt.printToken("output");
            break;
        case INOUT:
            fmt.printToken("inout");
            break;
        }
    }

    protected void printEdge(IfgenEdge edge)
    {
        switch (edge)
        {
        case POSEDGE:
            fmt.printToken("posedge");
            break;
        case NEGEDGE:
            fmt.printToken("negedge");
            break;
        case ANYEDGE:
            fmt.printToken("anyedge");
            break;
        }
    }

    protected void printType(IfgenSchema schema, IfgenType type)
    {
        if (type == schema.BIT_TYPE)
        {
            fmt.printToken("bit");
        }
        else if (type == schema.INTEGER_TYPE)
        {
            fmt.printToken("integer");
        }
        else if (type == schema.STRING_TYPE)
        {
            fmt.printToken("string");
        }
    }

    protected void printVector(int size)
    {
        if (size > 1) printRange(size - 1, 0);
    }

    protected void printRange(int from, int to)
    {
        fmt.printLeadingToken("[");
        fmt.printToken(String.valueOf(from));
        fmt.printTrailingToken(":");
        fmt.printToken(String.valueOf(to));
        fmt.printTrailingToken("]");
    }
}
