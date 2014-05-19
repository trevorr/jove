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
 * Class providing basic callback functionality for an {@link OVAEngine}.
 * 
 * @author Jon Nall
 */
public interface OVAEngineCallback
{
    /**
     * Contains the callback handler code. This callback is registered via
     * {@link OVAEngine#addListener(OVAEngineEventType, OVAEngineCallback)} and
     * will be called whenever events of the specified OVAEngineEventType
     * occur. This handler may be registered with more than one
     * OVAEngineEventType, in which case it will be called when each type of
     * event occurs.
     *
     * @param eventType the event type that occurred
     * @param simTime the time at which the event occurred
     */
    public void run(OVAEngineEventType eventType, VerilogTime simTime);
}
