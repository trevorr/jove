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

import com.newisys.langschema.SynchronizedStatement;

/**
 * Represents a Java 'synchronized' statement.
 * 
 * @author Trevor Robinson
 */
public final class JavaSynchronizedStatement
    extends JavaStatement
    implements SynchronizedStatement
{
    private final JavaExpression lock;
    private final JavaBlock block;

    public JavaSynchronizedStatement(JavaExpression lock, JavaBlock block)
    {
        super(lock.schema);
        this.lock = lock;
        block.setContainingStatement(this);
        this.block = block;
    }

    public JavaExpression getLock()
    {
        return lock;
    }

    public JavaBlock getBlock()
    {
        return block;
    }

    public void accept(JavaStatementVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "synchronized statement";
    }
}
