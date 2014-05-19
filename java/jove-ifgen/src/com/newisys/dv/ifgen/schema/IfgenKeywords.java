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

package com.newisys.dv.ifgen.schema;

import java.util.HashSet;
import java.util.Set;

/**
 * Tests whether a given string represents an Ifgen language keyword.
 * 
 * @author Trevor Robinson
 */
public final class IfgenKeywords
{
    private static final Set<String> keywords = new HashSet<String>();

    static
    {
        keywords.add("anyedge");
        keywords.add("bind");
        keywords.add("bit");
        keywords.add("clock");
        keywords.add("default");
        keywords.add("depth");
        keywords.add("drive");
        keywords.add("hdl_node");
        keywords.add("hdl_task");
        keywords.add("hvl_task");
        keywords.add("import");
        keywords.add("inout");
        keywords.add("input");
        keywords.add("integer");
        keywords.add("interface");
        keywords.add("is");
        keywords.add("module");
        keywords.add("negedge");
        keywords.add("output");
        keywords.add("package");
        keywords.add("port");
        keywords.add("posedge");
        keywords.add("sample");
        keywords.add("testbench");
    }

    private IfgenKeywords()
    {
        // prevent instantiation
    }

    public static boolean isKeyword(String id)
    {
        return keywords.contains(id);
    }
}
