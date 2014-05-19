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

package com.newisys.dv;

import junit.framework.TestCase;

import com.newisys.eventsim.SimulationManager;

/**
 * Test the basic calls to the Mailbox class that can be tested in a single
 * thread.
 */
public class MailboxTest
    extends TestCase
{
    private Mailbox<String> m1;
    private SimulationManager simManager;

    static String s[] = { "When", "in", "the", "course", "of", "human",
        "events", "..." };
    static final int SIZE = 8;

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(MailboxTest.class);
    }

    public MailboxTest(String arg0)
    {
        super(arg0);
    }

    final public void testMailbox()
    {
        simManager = new SimulationManager();
        m1 = new Mailbox<String>(simManager, "m1");

        doPut();
        doPeekWait(0);
        doPeekNoWait(0);
        doGetWait(0);
        doPeekWait(1);
        doPeekNoWait(1);
        doGetNoWait(1);
        doSize(0);
        doPut();
        doSize(SIZE);
        doPut();
        doSize(SIZE * 2);
        doPeekWait(0);
        doPeekNoWait(0);
        doSize(SIZE * 2);

        doToString(m1.getClass().getName() + "{m1}");
    }

    public void doPut()
    {
        for (int i = 0; i < SIZE; i++)
        {
            m1.put(s[i]);
        }
    }

    public void doGetWait(int index)
    {
        String getValue = m1.getWait();
        assertEquals(s[index], getValue);
    }

    public void doGetNoWait(int index)
    {
        String getValue;

        for (int i = index; i < SIZE; i++)
        {
            getValue = m1.getNoWait();
            assertEquals(s[i], getValue);
        }
    }

    public void doPeekWait(int index)
    {
        String peekValue;

        for (int i = 0; i < (SIZE * 10); i++)
        {
            peekValue = m1.peekWait();
            assertEquals(s[index], peekValue);
        }
    }

    public void doPeekNoWait(int index)
    {
        String peekValue;

        for (int i = 0; i < (SIZE * 10); i++)
        {
            peekValue = m1.peekNoWait();
            assertEquals(s[index], peekValue);
        }
    }

    public void doToString(String expected)
    {
        assertEquals(m1.toString(), expected);
    }

    public void doSize(int expected)
    {
        int actual = m1.size();
        assertEquals(actual, expected);
    }
}
