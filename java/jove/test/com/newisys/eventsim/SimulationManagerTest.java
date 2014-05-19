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

package com.newisys.eventsim;

import junit.framework.TestCase;

import com.newisys.random.PRNG;
import com.newisys.random.PRNGFactory;
import com.newisys.random.PRNGFactoryFactory;
import com.newisys.threadmarshal.ThreadMarshaller;

public class SimulationManagerTest
    extends TestCase
{
    static final PRNGFactory rngFactory = PRNGFactoryFactory
        .getDefaultFactory();

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(SimulationManagerTest.class);
    }

    private PRNG rng;

    @Override
    public void setUp()
    {
        rng = rngFactory.newInstance(0);
    }

    public final void testBasic()
    {
        final SimulationManager sim = new SimulationManager("BasicSimMgr",
            rngFactory, rng);
        assertTrue("!hasActiveThreads after creation", !sim.hasActiveThreads());

        final Event e = new PulseEvent("Basic:e");
        final SimulationThread t = sim.fork("Basic:t", new Runnable()
        {
            public void run()
            {
                SimulationThread.currentThread().waitFor(e);
            }
        });
        assertNotNull("fork returns non-null", t);
        assertTrue("forked thread pending", t.getState() == ThreadState.PENDING);

        assertTrue("hasActiveThreads after fork", sim.hasActiveThreads());
        sim.executeThreads();
        assertTrue("forked thread blocked", t.getState() == ThreadState.BLOCKED);
        assertTrue("hasActiveThreads after first execute", sim
            .hasActiveThreads());

        sim.notifyOf(e);
        sim.executeThreads();
        assertTrue("forked thread terminated",
            t.getState() == ThreadState.TERMINATED);
        assertTrue("!hasActiveThreads after last execute", !sim
            .hasActiveThreads());
    }

    int flag;

    public final void testTerminate()
    {
        final SimulationManager sim = new SimulationManager("TerminateSimMgr",
            rngFactory, rng);
        assertTrue("!hasActiveThreads after creation", !sim.hasActiveThreads());

        flag = 0;
        final Event e1 = new PulseEvent("Terminate:e1");
        final SimulationThread t1 = sim.fork("Terminate:t1", new Runnable()
        {
            public void run()
            {
                final Event e2 = new PulseEvent("Terminate:e2");
                final SimulationThread t2 = sim.fork("Terminate:t2",
                    new Runnable()
                    {
                        public void run()
                        {
                            ++flag;
                            SimulationThread.currentThread().waitFor(e2);
                            flag = -1;
                        }
                    });
                assertNotNull("fork returns non-null", t2);
                assertTrue("t2 pending", t2.getState() == ThreadState.PENDING);

                ++flag;
                SimulationThread.currentThread().waitFor(e1);

                t2.terminate();
                t2.join();
                ++flag;
            }
        });
        assertNotNull("fork returns non-null", t1);
        assertTrue("t1 pending", t1.getState() == ThreadState.PENDING);
        assertTrue("flag == 0", flag == 0);

        assertTrue("hasActiveThreads after fork", sim.hasActiveThreads());
        sim.executeThreads();
        assertTrue("hasActiveThreads after first execute", sim
            .hasActiveThreads());
        assertTrue("flag == 2", flag == 2);

        sim.notifyOf(e1);
        sim.executeThreads();
        assertTrue("!hasActiveThreads after last execute", !sim
            .hasActiveThreads());
        assertTrue("flag == 3", flag == 3);
    }

    public final void testTerminateImmediate()
    {
        final SimulationManager sim = new SimulationManager(
            "TerminateImmediateSimMgr", rngFactory, rng);
        assertTrue("!hasActiveThreads after creation", !sim.hasActiveThreads());

        flag = 0;
        final SimulationThread t1 = sim.fork("TerminateImmediate:t1",
            new Runnable()
            {
                public void run()
                {
                    final SimulationThread t2 = sim.fork(
                        "TerminateImmediate:t2", new Runnable()
                        {
                            public void run()
                            {
                                flag = -1;
                            }
                        });
                    assertNotNull("fork returns non-null", t2);
                    assertTrue("t2 pending",
                        t2.getState() == ThreadState.PENDING);

                    t2.terminate();
                    t2.join();
                    flag = 1;
                }
            });
        assertNotNull("fork returns non-null", t1);
        assertTrue("t1 pending", t1.getState() == ThreadState.PENDING);
        assertTrue("flag == 0", flag == 0);

        assertTrue("hasActiveThreads after fork", sim.hasActiveThreads());
        sim.executeThreads();
        assertTrue("!hasActiveThreads after first execute", !sim
            .hasActiveThreads());
        assertTrue("flag == 1", flag == 1);
    }

    public final void testTerminateJoin()
    {
        final SimulationManager sim = new SimulationManager(
            "TerminateJoinSimMgr", rngFactory, rng);
        assertTrue("!hasActiveThreads after creation", !sim.hasActiveThreads());

        final SimulationThread t1 = sim.fork("TerminateJoin:t1", new Runnable()
        {
            public void run()
            {
                for (int i = 0; i < 100; ++i)
                {
                    final SimulationThread t2 = sim.fork("TerminateJoin:t2",
                        new Runnable()
                        {
                            public void run()
                            {
                                SimulationThread.currentThread().yield();
                            }
                        });
                    assertNotNull("fork returns non-null", t2);
                    assertTrue("t2 pending",
                        t2.getState() == ThreadState.PENDING);

                    SimulationThread.currentThread().yield();
                    t2.terminate();
                    t2.join();
                }
            }
        });
        assertNotNull("fork returns non-null", t1);
        assertTrue("t1 pending", t1.getState() == ThreadState.PENDING);

        assertTrue("hasActiveThreads after fork", sim.hasActiveThreads());
        sim.executeThreads();
        assertTrue("!hasActiveThreads after first execute", !sim
            .hasActiveThreads());
    }

    public final void testNotifyMany()
    {
        final SimulationManager sim = new SimulationManager("NotifyManySimMgr",
            rngFactory, rng);
        assertTrue("!hasActiveThreads after creation", !sim.hasActiveThreads());

        final Event e = new PulseEvent("NotifyMany:e");
        final int THREAD_COUNT = 100;
        final SimulationThread[] ta = new SimulationThread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; ++i)
        {
            ta[i] = sim.fork("NotifyMany:Thread-" + i, new Runnable()
            {
                public void run()
                {
                    SimulationThread.currentThread().waitFor(e);
                }
            });
        }
        assertTrue(THREAD_COUNT + " threads created",
            sim.activeThreads.size() == THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; ++i)
        {
            assertTrue("forked thread " + i + " pending",
                ta[i].getState() == ThreadState.PENDING);
        }

        assertTrue("hasActiveThreads after fork", sim.hasActiveThreads());
        sim.executeThreads();
        for (int i = 0; i < THREAD_COUNT; ++i)
        {
            assertTrue("forked thread " + i + " blocked",
                ta[i].getState() == ThreadState.BLOCKED);
        }
        assertTrue("hasActiveThreads after first execute", sim
            .hasActiveThreads());

        sim.notifyOf(e);
        for (int i = 0; i < THREAD_COUNT; ++i)
        {
            assertTrue("forked thread " + i + " pending",
                ta[i].getState() == ThreadState.PENDING);
        }
        sim.executeThreads();
        for (int i = 0; i < THREAD_COUNT; ++i)
        {
            assertTrue("forked thread " + i + " terminated",
                ta[i].getState() == ThreadState.TERMINATED);
        }
        assertTrue("!hasActiveThreads after last execute", !sim
            .hasActiveThreads());
    }

    public final void testTerminateThreads()
    {
        final SimulationManager sim = new SimulationManager(
            "TerminateThreadsSimMgr", rngFactory, rng);
        assertTrue("!hasActiveThreads after creation", !sim.hasActiveThreads());

        final int THREAD_COUNT = 100;
        final SimulationThread[] ta = new SimulationThread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; ++i)
        {
            final Event e = new PulseEvent("TerminateThreads:Event-" + i);
            ta[i] = sim.fork("TerminateThreads:Thread-" + i, new Runnable()
            {
                public void run()
                {
                    SimulationThread.currentThread().waitFor(e);
                }
            });
        }
        assertTrue(THREAD_COUNT + " threads created",
            sim.activeThreads.size() == THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; ++i)
        {
            assertTrue("forked thread " + i + " pending",
                ta[i].getState() == ThreadState.PENDING);
        }

        assertTrue("hasActiveThreads after fork", sim.hasActiveThreads());
        sim.executeThreads();
        for (int i = 0; i < THREAD_COUNT; ++i)
        {
            assertTrue("forked thread " + i + " blocked",
                ta[i].getState() == ThreadState.BLOCKED);
        }
        assertTrue("hasActiveThreads after first execute", sim
            .hasActiveThreads());

        sim.terminateThreads();
        assertTrue("!hasActiveThreads after terminateThreads", !sim
            .hasActiveThreads());
    }

    private static class SimMgrRunnable
        implements Runnable
    {
        private final int id;
        private final PRNG rng;
        int availThreads;

        public SimMgrRunnable(int id, PRNG rng)
        {
            this.id = id;
            this.rng = rng;
        }

        synchronized int getForkCount(PRNG rand, int min, int max)
        {
            int forkCount = availThreads > min ? rand.nextInt(min, Math.min(
                max, availThreads)) : availThreads;
            availThreads -= forkCount;
            return forkCount;
        }

        public void run()
        {
            final PRNG rnd = rngFactory.newInstance(rng);

            // run multiple complete simulations
            final int REP_COUNT = 10;
            for (int i = 0; i < REP_COUNT; ++i)
            {
                availThreads = 200;

                final SimulationManager sim = new SimulationManager(
                    "StressSimMgr-" + id, rngFactory, rng);

                final int forkCount = getForkCount(rnd, 1, 5);
                for (int j = 0; j < forkCount; ++j)
                {
                    sim.fork("Stress:SimThread-" + j, new SimThreadRunnable(1));
                }

                sim.executeThreads();

                assertTrue(!sim.hasActiveThreads());
            }
        }

        private class SimThreadRunnable
            implements Runnable
        {
            final int depth;

            public SimThreadRunnable(int depth)
            {
                this.depth = depth;
            }

            public void run()
            {
                final SimulationThread t = SimulationThread.currentThread();
                final SimulationManager sim = t.getManager();
                final PRNG rnd = rngFactory.newInstance(t.getRandom());

                final int MAX_FORK_DEPTH = 5;
                if (depth < MAX_FORK_DEPTH)
                {
                    final int forkCount = getForkCount(rnd, 0, 3);
                    if (forkCount > 0)
                    {
                        final SimulationThread[] ta = new SimulationThread[forkCount];
                        for (int i = 0; i < forkCount; ++i)
                        {
                            ta[i] = sim.fork(t.getName() + "-" + i,
                                new SimThreadRunnable(depth + 1));
                        }
                        switch (rnd.nextInt(3))
                        {
                        case 0:
                            t.joinAll(ta);
                            break;
                        case 1:
                            t.joinAny(ta);
                            break;
                        default:
                        // join none
                        }
                    }
                }
            }
        }
    }

    public final void testRandomForkJoin()
    {
        // create some independent simulations running in different threads
        final int SIM_COUNT = 1;
        final Thread[] ta = new Thread[SIM_COUNT];
        for (int i = 0; i < SIM_COUNT; ++i)
        {
            final SimMgrRunnable r = new SimMgrRunnable(i, rngFactory
                .newInstance(rng));
            ta[i] = new Thread(r, "Stress:Thread-" + i);
            ta[i].start();
        }

        // wait for all the simulations to complete
        for (int i = 0; i < SIM_COUNT; ++i)
        {
            try
            {
                ta[i].join();
            }
            catch (InterruptedException e)
            {
                throw new Error(e);
            }
        }
    }

    public void testAbandonedChild()
    {
        final SimulationManager sim = new SimulationManager(
            "AbandonedChildSimMgr", rngFactory, rng);
        assertTrue("!hasActiveThreads after creation", !sim.hasActiveThreads());

        SimulationThread topThread = sim.fork("AbandonedChild:topThread",
            new Runnable()
            {
                public void run()
                {
                    SimulationThread parent = sim.fork("AbandonedChild:parent",
                        new Runnable()
                        {
                            public void run()
                            {
                                sim.fork("AbandonedChild:child", new Runnable()
                                {
                                    public void run()
                                    {
                                        Event e = new PulseEvent(
                                            "AbandonedChild:e");
                                        SimulationThread.currentThread()
                                            .waitFor(e);
                                    }
                                });
                                // no join; abandon child
                            }
                        });
                    parent.join();

                    SimulationThread t = SimulationThread.currentThread();
                    t.terminateChildren();
                    t.joinChildren();
                }
            });

        sim.executeThreads();
        topThread.terminate();

        assertTrue("!hasActiveThreads after terminateThreads", !sim
            .hasActiveThreads());
    }

    public void testChildTerminateThreads()
    {
        final SimulationManager sim = new SimulationManager(
            "ChildTerminateThreadsSimMgr", rngFactory, rng);
        assertTrue("!hasActiveThreads after creation", !sim.hasActiveThreads());

        sim.fork("ChildTerminateThreads:t1", new Runnable()
        {
            public void run()
            {
                SimulationThread t2 = sim.fork("ChildTerminateThreads:t2",
                    new Runnable()
                    {
                        public void run()
                        {
                            SimulationThread t3 = sim.fork(
                                "ChildTerminateThreads:t3", new Runnable()
                                {
                                    public void run()
                                    {
                                        Event e = new PulseEvent(
                                            "ChildTerminateThreads:e");
                                        SimulationThread.currentThread()
                                            .waitFor(e);
                                    }
                                });
                            sim.terminateThreads();
                            t3.join();
                        }
                    });
                t2.join();
            }
        });

        sim.executeThreads();

        assertTrue("!hasActiveThreads after terminateThreads", !sim
            .hasActiveThreads());
    }

    private static interface MarshalTestInterface
    {
        Thread getCurrentThread();
    }

    private static class MarshalTest
        implements MarshalTestInterface
    {
        public Thread getCurrentThread()
        {
            return Thread.currentThread();
        }
    }

    public final void testThreadMarshal()
    {
        final SimulationManager sim = new SimulationManager(
            "ThreadMarshalSimMgr", rngFactory, rng);
        assertTrue("!hasActiveThreads after creation", !sim.hasActiveThreads());

        final ThreadMarshaller marshaller = sim.getThreadMarshaller();
        final MarshalTest testObject = new MarshalTest();
        final MarshalTestInterface testProxy = (MarshalTestInterface) marshaller
            .getProxy(testObject);
        final Thread mainThread = Thread.currentThread();
        final Event e = new PulseEvent("ThreadMarshal:e");
        final SimulationThread t = sim.fork("ThreadMarshal:t", new Runnable()
        {
            public void run()
            {
                Thread proxyThread = testProxy.getCurrentThread();
                assertTrue("call marshalled", proxyThread == mainThread);
                SimulationThread.currentThread().waitFor(e);
            }
        });
        assertNotNull("fork returns non-null", t);
        assertTrue("forked thread pending", t.getState() == ThreadState.PENDING);

        assertTrue("hasActiveThreads after fork", sim.hasActiveThreads());
        sim.executeThreads();
        assertTrue("forked thread blocked", t.getState() == ThreadState.BLOCKED);
        assertTrue("hasActiveThreads after first execute", sim
            .hasActiveThreads());

        sim.notifyOf(e);
        sim.executeThreads();
        assertTrue("forked thread terminated",
            t.getState() == ThreadState.TERMINATED);
        assertTrue("!hasActiveThreads after last execute", !sim
            .hasActiveThreads());
    }

    public final void testUnhandledException()
    {
        final SimulationManager sim = new SimulationManager(
            "UnhandledExceptionSimMgr", rngFactory, rng);
        assertTrue("!hasActiveThreads after creation", !sim.hasActiveThreads());

        final String msg = "BOOM!";
        final SimulationThread t = sim.fork("UnhandledException:t",
            new Runnable()
            {
                public void run()
                {
                    throw new RuntimeException(msg);
                }
            });
        assertNotNull("fork returns non-null", t);
        assertTrue("forked thread pending", t.getState() == ThreadState.PENDING);
        assertTrue("hasActiveThreads after fork", sim.hasActiveThreads());

        try
        {
            sim.executeThreads();
            assertTrue("executeThreads threw exception", false);
        }
        catch (UnhandledExceptionException e)
        {
            assertTrue("unhandled exception thrown by t", e.getThread() == t);
            assertTrue("correct unhandled exception message", e.getCause()
                .getMessage() == msg);
        }

        assertTrue("forked thread terminated",
            t.getState() == ThreadState.TERMINATED);
        assertTrue("!hasActiveThreads after last execute", !sim
            .hasActiveThreads());
    }
}
