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
 * Represents a logical signal (or multiple-bit signal bus) that is typically
 * accessed relative to a clock signal. This interface is the abstract base
 * interface for the four concrete signal types: input, output, input/output,
 * and clock.
 * 
 * @author Trevor Robinson
 */
public interface Signal
{
    /**
     * Returns the full name of this signal, as specified to the appropriate
     * getSignal() method of DVSimulation.
     *
     * @return the full name of this signal
     */
    String getName();

    /**
     * Returns the clock signal that this signal is sampled/driven relative to.
     * For clock signals, this method returns <code>this</code>.
     *
     * @return the clock signal for this signal
     */
    ClockSignal getClock();

    /**
     * Returns the width of this signal in bits. For clock signals, the width is
     * always 1.
     *
     * @return the width of this signal
     */
    int getSize();
}
