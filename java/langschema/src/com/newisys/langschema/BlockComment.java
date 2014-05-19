/*
 * LangSchema - Generic Programming Language Modeling Interfaces
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

package com.newisys.langschema;

/**
 * Simple implementation of a block comment annotation.
 * 
 * @author Trevor Robinson
 */
public class BlockComment
    extends CommentImpl
{
    private static final long serialVersionUID = 3616444588211253814L;

    public BlockComment()
    {
    }

    public BlockComment(boolean leading)
    {
        super(leading);
    }

    public BlockComment(String line)
    {
        super(line);
    }

    public BlockComment(String line, boolean leading)
    {
        super(line, leading);
    }
}
