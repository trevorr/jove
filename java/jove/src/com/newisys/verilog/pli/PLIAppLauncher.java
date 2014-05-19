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

import java.lang.reflect.Constructor;

import com.newisys.dv.DVApplication;
import com.newisys.dv.DVSimulation;
import com.newisys.eventsim.SimulationManager;
import com.newisys.ova.OVA;
import com.newisys.ova.OVAEngine;
import com.newisys.ova.OVAInterface;
import com.newisys.random.PRNGFactory;
import com.newisys.random.PRNGFactoryFactory;
import com.newisys.threadmarshal.ThreadMarshaller;
import com.newisys.verilog.VerilogApplication;
import com.newisys.verilog.VerilogSimulation;

/**
 * Used by the native PLI interface to set up the Java side of the environment
 * and launch the verification application.
 * 
 * @author Trevor Robinson
 */
final class PLIAppLauncher
{
    private static VerilogSimulation createVerilogSimulation(PLI pli)
    {
        PLIVerilogSimulation verilogSim = new PLIVerilogSimulation(pli);
        return verilogSim;
    }

    private static DVSimulation createDVSimulation(PLI pli)
    {
        final PRNGFactory rngFactory = PRNGFactoryFactory.getDefaultFactory();
        final SimulationManager simManager = new SimulationManager(
            "PLISimulation", rngFactory, rngFactory.newInstance(0));

        final ThreadMarshaller marshaller = simManager.getThreadMarshaller();
        final PLIInterface pliProxy = (PLIInterface) marshaller.getProxy(pli);
        pli.setProxyInterface(pliProxy);
        final PLIVerilogSimulation verilogSim = new PLIVerilogSimulation(pli);

        // set up OVA proxy if OVA engine is available
        OVAEngine ovaEngine = null;
        if (OVA.isSupported())
        {
            final OVAInterface ovaProxy = (OVAInterface) marshaller
                .getProxy(new OVA());
            ovaEngine = new OVAEngine(ovaProxy);
        }

        final DVSimulation dvSim = new DVSimulation(verilogSim, simManager,
            ovaEngine);
        return dvSim;
    }

    static VerilogApplication createApplication(String appClassName, PLI pli)
        throws Exception
    {
        VerilogApplication app;
        Class< ? > appClass = Class.forName(appClassName);
        if (DVApplication.class.isAssignableFrom(appClass))
        {
            Constructor< ? > appCtor = appClass
                .getConstructor(new Class[] { DVSimulation.class });
            DVSimulation sim = createDVSimulation(pli);
            app = (DVApplication) appCtor.newInstance(new Object[] { sim });
        }
        else
        {
            Constructor< ? > appCtor = appClass
                .getConstructor(new Class[] { VerilogSimulation.class });
            VerilogSimulation sim = createVerilogSimulation(pli);
            app = (VerilogApplication) appCtor
                .newInstance(new Object[] { sim });
        }
        return app;
    }
}
