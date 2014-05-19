/*
 * LangSchema-Jove - Programming Language Modeling Classes for Jove
 * Copyright (C) 2005 Newisys, Inc. or its licensors, as applicable.
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

package com.newisys.schemaprinter.jove;

import com.newisys.langschema.java.JavaType;
import com.newisys.langschema.jove.JoveAssocArrayType;
import com.newisys.langschema.jove.JoveBitVectorType;
import com.newisys.langschema.jove.JoveTypeVisitor;
import com.newisys.langschema.jove.JoveFixedArrayType;
import com.newisys.schemaprinter.java.BasePrinter;
import com.newisys.schemaprinter.java.SchemaObjectPrinter;

/**
 * Java schema object printer with support for Jove extended types.
 * 
 * @author Trevor Robinson
 */
public class JoveSchemaObjectPrinter
    extends SchemaObjectPrinter
    implements JoveTypeVisitor
{
    public JoveSchemaObjectPrinter(BasePrinter basePrinter)
    {
        super(basePrinter);
    }

    public void visit(JoveAssocArrayType obj)
    {
        print((JavaType) obj);
    }

    public void visit(JoveBitVectorType obj)
    {
        print((JavaType) obj);
    }

    public void visit(JoveFixedArrayType obj)
    {
        print((JavaType) obj);
    }
}
