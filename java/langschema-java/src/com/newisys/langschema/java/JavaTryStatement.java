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

import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.TryStatement;

/**
 * Represents a Java 'try' statement.
 * 
 * @author Trevor Robinson
 */
public final class JavaTryStatement
    extends JavaStatement
    implements TryStatement
{
    private final JavaBlock tryBlock;
    private final List<JavaTryCatch> catches = new LinkedList<JavaTryCatch>();
    private JavaBlock finallyBlock;

    public JavaTryStatement(JavaBlock tryBlock)
    {
        super(tryBlock.schema);
        tryBlock.setContainingStatement(this);
        this.tryBlock = tryBlock;
    }

    public JavaBlock getTryBlock()
    {
        return tryBlock;
    }

    public List<JavaTryCatch> getCatches()
    {
        return catches;
    }

    public void addCatch(JavaTryCatch tryCatch)
    {
        tryCatch.block.setContainingStatement(this);
        catches.add(tryCatch);
    }

    public JavaBlock getFinallyBlock()
    {
        return finallyBlock;
    }

    public void setFinallyBlock(JavaBlock finallyBlock)
    {
        if (finallyBlock != null)
        {
            finallyBlock.setContainingStatement(this);
        }
        this.finallyBlock = finallyBlock;
    }

    public void accept(JavaStatementVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "try statement";
    }
}
