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

package com.newisys.behsim;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import com.newisys.dv.DVApplication;
import com.newisys.dv.DVSimulation;
import com.newisys.eventsim.SimulationManager;
import com.newisys.eventsim.SimulationThread;
import com.newisys.ova.OVAEngine;
import com.newisys.random.PRNGFactory;
import com.newisys.random.PRNGFactoryFactory;

/**
 * Provides the main() method used to launch behavioral simulations.
 * 
 * @author Trevor Robinson
 * @author Jon Nall
 */
public final class BehavioralLauncher
{
    /**
     * Launches a behavioral simulation.
     * <P>
     * usage: BehavioralLauncher &lt;appclass&gt; [plus args]
     *
     * @param args an array of arguments to pass to the behavioral simulation
     */
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.err
                .println("Syntax: BehavioralLauncher <appclass> [plus args]");
            System.exit(1);
        }

        SimulationThread behClkThread = null;
        DVApplication app = null;
        try
        {
            // get the DVApplication class
            final Class< ? > appCls = Class.forName(args[0]);
            assert (DVApplication.class.isAssignableFrom(appCls));

            // create the simulation objects
            final BehavioralSimulation sim = new BehavioralSimulation(Arrays
                .asList(args));
            final PRNGFactory rngFactory = PRNGFactoryFactory
                .getDefaultFactory();
            final SimulationManager simManager = new SimulationManager(
                "BehavioralSimulation", rngFactory, rngFactory.newInstance(0));
            final OVAEngine ovaEngine = null;
            final DVSimulation dvSim = new DVSimulation(sim, simManager,
                ovaEngine);

            // create the default/system clock
            final String clockName = "DefaultClock";
            sim.createRegister(clockName, 1);
            behClkThread = dvSim.fork(clockName, new BehavioralClockGenerator(
                dvSim, clockName, 100));

            // create the DVApplication object
            final Constructor< ? > appCtor = appCls
                .getConstructor(DVSimulation.class);
            app = (DVApplication) appCtor.newInstance(dvSim);

            // start the DVApplication
            app.start();

            // start the behavioral simulator
            sim.run();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (behClkThread != null)
            {
                // TODO: do we need to worry about this thread having children?
                behClkThread.terminate();
            }

            if (app != null)
            {
                app.finish();
            }
        }
    }
}
