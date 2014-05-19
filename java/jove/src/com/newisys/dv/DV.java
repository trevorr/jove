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

/**
 * Contains convenience fields and methods for static simulations.
 * Specifically, the class contains the static <code>simulation</code> field
 * used by the static signal interface pattern:
 * <p><blockquote><pre>
 * public final class MyInterface
 * {
 *     public final static ClockSignal clk = DV.simulation.getClockSignal(
 *         "clk", 1);
 *     public final static InOutSignal x = DV.simulation.getInOutSignal(
 *         "x", clk, EdgeSet.POSEDGE, 0, EdgeSet.POSEDGE, 1, 1);
 * }
 * </pre></blockquote><p>
 * Note: This class is provided as a convenience to the end user and should
 * <b>NOT</b> be used by any code in the <code>com.newisys.dv</code> package.
 * 
 * @author Trevor Robinson
 */
public final class DV
{
    /**
     * <code>DVSimulation</code> object representing the simulation running in
     * this VM/class loader. This field is initialized by (the first execution
     * of) {@link DVApplication#DVApplication(DVSimulation)}, and should
     * therefore be available to any signal interface classes referenced by
     * derived application classes.
     */
    public static DVSimulation simulation;

    private DV()
    {
        // prevent instantiation
    }
}
