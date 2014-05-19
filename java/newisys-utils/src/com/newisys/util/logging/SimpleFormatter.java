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

package com.newisys.util.logging;

import java.text.MessageFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Simple Formatter class based on Java's built-in logging infrastructure.
 * 
 * @author Trevor Robinson
 */
public class SimpleFormatter
    extends Formatter
{
    private String lineSeparator = System.getProperty("line.separator");

    private boolean printLogger;
    private boolean printLevel;

    public SimpleFormatter()
    {
        this(true, true);
    }

    public SimpleFormatter(boolean printLogger, boolean printLevel)
    {
        this.printLogger = printLogger;
        this.printLevel = printLevel;
    }

    public String format(LogRecord record)
    {
        final StringBuffer sb = new StringBuffer(80);

        if (printLogger)
        {
            sb.append(record.getLoggerName());
            if (printLevel)
            {
                sb.append('[');
                sb.append(record.getLevel().getLocalizedName());
                sb.append(']');
            }
            sb.append(": ");
        }
        else if (printLevel)
        {
            sb.append(record.getLevel().getLocalizedName());
            sb.append(": ");
        }

        final String method = record.getSourceMethodName();
        if (method != null)
        {
            sb.append(method);
            sb.append(": ");
        }

        sb.append(MessageFormat.format(record.getMessage(), record
            .getParameters()));

        sb.append(lineSeparator);
        return sb.toString();
    }
}
