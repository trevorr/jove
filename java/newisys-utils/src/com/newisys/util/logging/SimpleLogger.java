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

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logger factory methods based on Java's built-in logging infrastructure.
 * 
 * @author Trevor Robinson
 */
public final class SimpleLogger
{
    public static Logger getLogger(String name)
    {
        final Logger logger = Logger.getLogger(name);

        // set up logger
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);

        // set up log file handler
        final Handler handler;
        try
        {
            handler = new FileHandler(logger.getName() + ".log");
        }
        catch (IOException e)
        {
            throw new Error(e);
        }
        handler.setFormatter(new SimpleFormatter(false, false));
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);

        return logger;
    }

    public static Logger getClassLogger(Class cls)
    {
        return getLogger(cls.getName());
    }

    public static Logger getPackageLogger(Class cls)
    {
        return getLogger(cls.getPackage().getName());
    }
}
