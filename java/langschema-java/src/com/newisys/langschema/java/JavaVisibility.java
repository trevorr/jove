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

import com.newisys.langschema.Visibility;

/**
 * Enumeration of Java visibility modifiers.
 * 
 * @author Trevor Robinson
 */
public enum JavaVisibility implements Visibility
{
    PUBLIC(3), PROTECTED(2), DEFAULT(1), PRIVATE(0);

    private final int level;

    private JavaVisibility(int level)
    {
        this.level = level;
    }

    public boolean isLessVisible(JavaVisibility other)
    {
        return level < other.level;
    }

    public String toString()
    {
        return name().toLowerCase();
    }
}
