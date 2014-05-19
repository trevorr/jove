/*
 * Newisys-Utils - Newisys Utility Classes
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

package com.newisys.util.text;

import java.io.IOException;

/**
 * Interface for classes that format tokens.
 * 
 * @author Trevor Robinson
 */
public interface TokenFormatter
{
    void incIndent();

    void decIndent();

    int getIndent();

    void beginGroup();

    void endGroup();

    int getGroup();

    void printToken(String s);

    void printLeadingToken(String s);

    void printTrailingToken(String s);

    void printSpace();

    boolean isNewLine();

    void newLine()
        throws IOException;

    void flush()
        throws IOException;

    void close()
        throws IOException;
}
