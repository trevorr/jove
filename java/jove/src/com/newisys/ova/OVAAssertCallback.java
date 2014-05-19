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

package com.newisys.ova;

import com.newisys.verilog.VerilogTime;

/**
 * Class providing basic callback functionality for {@link OVAAssert} objects.
 * Classes deriving from this class override {@link #run}.
 * 
 * @author Jon Nall
 */
public abstract class OVAAssertCallback
{
    /**
     * The OVAAssert with which this callback is associated.
     */
    private final OVAAssert ovaAssert;
    private final boolean enableForAllAsserts;

    /**
     * Constructs an OVAAssertCallback that is called when any OVAAssert is
     * the target of the event.
     */
    protected OVAAssertCallback()
    {
        this.ovaAssert = null;
        this.enableForAllAsserts = true;
    }

    /**
     * Constructs an OVAAssertCallback that is called only when the specified
     * assertion is the target of the event.
     *
     * @param ovaAssert the assert for which this callback will occur.
     */
    protected OVAAssertCallback(OVAAssert ovaAssert)
    {
        this.ovaAssert = ovaAssert;
        this.enableForAllAsserts = false;
    }

    /**
     * Contains the actual callback handler code. This handler is registered
     * with an OVAEvent via
     * {@link OVAAssert#addListener(OVAAssertEventType, OVAAssertCallback)}.
     * Once registered, this method will be called each time the specified
     * event type occurs for that OVAAssert. This handler may be registered
     * with multiple event types and will be called when each type occurs.
     *
     * @param ovaAssert the OVAAssert associated with this callback.
     * @param eventType the event type that occurred for <code>ovaAssert</code>
     * @param simTime the time at which the event occurred
     * @param attemptID the attempt identifier associated with the event
     */
    public abstract void run(
        OVAAssert ovaAssert,
        OVAAssertEventType eventType,
        VerilogTime simTime,
        long attemptID);

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode()
    {
        return (int) (System.identityHashCode(this) ^ ((ovaAssert == null)
            ? 0xA5A5A5A5 : ovaAssert.assertID));
    }

    /**
     * Determines if this callback is enabled for the specified assertion.
     * The {@link #run} method should not be called unless this method returns
     * <code>true</code>.
     *
     * @param ovaAssert the assert to check
     * @return true if this callback should be run for <code>ovaAssert</code>,
     *      false otherwise
     */
    final boolean enabledForAssert(OVAAssert ovaAssert)
    {
        return (enableForAllAsserts || ovaAssert == this.ovaAssert);
    }
}
