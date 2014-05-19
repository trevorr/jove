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

package com.newisys.verilog.pli;

import java.util.LinkedList;
import java.util.List;

/**
 * Encapsulates simulator information obtained from PLIInterface.getInfo().
 * 
 * @author Trevor Robinson
 */
public final class PLIVerilogInfo
{
    private final String product;
    private final String version;
    private final LinkedList<String> arguments = new LinkedList<String>();

    PLIVerilogInfo(String product, String version)
    {
        this.product = product;
        this.version = version;
    }

    void addArgument(String argument)
    {
        arguments.add(argument);
    }

    public List<String> getArguments()
    {
        return arguments;
    }

    public String getProduct()
    {
        return product;
    }

    public String getVersion()
    {
        return version;
    }
}
