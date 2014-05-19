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

import com.newisys.langschema.java.JavaArrayType;
import com.newisys.langschema.java.JavaPackage;
import com.newisys.langschema.java.JavaType;
import com.newisys.langschema.jove.JoveAssocArrayType;
import com.newisys.langschema.jove.JoveBitVectorType;
import com.newisys.langschema.jove.JoveFixedArrayType;
import com.newisys.schemaprinter.java.BasePrinter;
import com.newisys.schemaprinter.java.ImportManager;
import com.newisys.util.text.TokenFormatter;

/**
 * Java base printer with support for Jove extended types.
 * 
 * @author Trevor Robinson
 */
public class JoveBasePrinter
    extends BasePrinter
{
    public JoveBasePrinter(
        TokenFormatter fmt,
        JoveSchemaPrinter config,
        ImportManager importMgr)
    {
        super(fmt, config, importMgr);
    }

    public void printType(JavaType type, JavaPackage pkgContext)
    {
        if (type instanceof JavaArrayType)
        {
            JavaArrayType arrayType = (JavaArrayType) type;
            printType(arrayType.getElementType(), pkgContext);

            int[] dims;
            int dimCount;
            if (type instanceof JoveFixedArrayType)
            {
                dims = ((JoveFixedArrayType) type).getDimensions();
                dimCount = dims.length;
            }
            else
            {
                dims = null;
                dimCount = arrayType.getIndexTypes().length;
            }

            for (int i = 0; i < dimCount; ++i)
            {
                fmt.printTrailingToken("[");
                if (dims != null)
                {
                    fmt.printTrailingToken("/*" + dims[i] + "*/");
                }
                fmt.printTrailingToken("]");
            }
        }
        else if (type instanceof JoveAssocArrayType)
        {
            JoveAssocArrayType assocType = (JoveAssocArrayType) type;
            printName(assocType.getBaseClass().getName(), pkgContext);
            String elemName = assocType.getElementType().toReferenceString();
            fmt.printTrailingToken("/*" + elemName + "*/");
        }
        else if (type instanceof JoveBitVectorType)
        {
            JoveBitVectorType bvType = (JoveBitVectorType) type;
            printName(bvType.getBaseClass().getName(), pkgContext);
            String desc = bvType.getDescription();
            int size = bvType.getSize();
            desc = (desc != null) ? desc + ":" + size : String.valueOf(size);
            fmt.printTrailingToken("/*" + desc + "*/");
        }
        else
        {
            super.printType(type, pkgContext);
        }
    }
}
