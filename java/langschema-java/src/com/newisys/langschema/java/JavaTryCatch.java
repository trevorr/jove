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

import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.TryCatch;
import com.newisys.langschema.util.NameTable;

/**
 * Represents a single 'catch' block in a Java 'try' statement.
 * 
 * @author Trevor Robinson
 */
public final class JavaTryCatch
    implements TryCatch
{
    private final JavaFunctionArgument variable;
    final JavaBlock block;
    private final NameTable nameTable = new NameTable();

    public JavaTryCatch(JavaFunctionArgument variable, JavaBlock block)
    {
        this.variable = variable;
        nameTable.addObject(variable);
        this.block = block;
    }

    public JavaFunctionArgument getExceptionVariable()
    {
        return variable;
    }

    public JavaBlock getCatchBlock()
    {
        return block;
    }

    public Iterator<NamedObject> lookupObjects(String identifier, NameKind kind)
    {
        return nameTable.lookupObjects(identifier, kind);
    }
}
