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

package com.newisys.schemaprinter;

import java.io.Writer;

import com.newisys.io.IndentWriter;
import com.newisys.util.text.DefaultTokenFormatter;
import com.newisys.util.text.TokenFormatter;

/**
 * Base implementation for schema printers.
 * 
 * @author Trevor Robinson
 */
public class SchemaPrinter
{
    private int margin = 80;
    private boolean collapseBodies = false;

    public int getMargin()
    {
        return margin;
    }

    public void setMargin(int margin)
    {
        this.margin = margin;
    }

    public boolean isCollapseBodies()
    {
        return collapseBodies;
    }

    public void setCollapseBodies(boolean collapseBodies)
    {
        this.collapseBodies = collapseBodies;
    }

    protected TokenFormatter getTokenFormatter(Writer writer)
    {
        IndentWriter indentWriter = new IndentWriter(writer);
        return new DefaultTokenFormatter(indentWriter, margin);
    }
}
