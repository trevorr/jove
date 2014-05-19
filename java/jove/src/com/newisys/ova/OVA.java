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

/**
 * The native OVA interface. This class implements
 * {@link com.newisys.ova.OVAInterface} by providing native implementations for
 * each interface method.
 * 
 * @author Jon Nall
 */
public final class OVA
    implements OVAInterface
{
    /**
     * Returns whether native OVA support is available.
     *
     * @return true if native OVA support is available, false otherwise
     */
    public static native boolean isSupported();

    /**
     * Sole constructor. This is not intended to be invoked by user code.
     *
     * @throws IllegalStateException if the simulator does not support OVA
     */
    public OVA()
    {
        // do nothing
        if (!isSupported())
        {
            throw new IllegalStateException(
                "Trying to invoke an instance of OVA, "
                    + "but the simulator does not support it");
        }
    }

    /**
     * {@inheritDoc}
     */
    public native String getApiVersion();

    /**
     * {@inheritDoc}
     */
    public native long registerClient();

    /**
     * {@inheritDoc}
     */
    public native boolean setConfigSwitch(
        long clientID,
        int confSwitch,
        boolean enable);

    /**
     * {@inheritDoc}
     */
    public native boolean doAction(long clientID, int eventID, long userData);

    /**
     * {@inheritDoc}
     */
    public native long firstAssert(long clientID);

    /**
     * {@inheritDoc}
     */
    public native long nextAssert(long clientID);

    /**
     * {@inheritDoc}
     */
    public native boolean assertDoAction(
        long clientID,
        int eventID,
        long assertID,
        long attemptID,
        long userData);

    /**
     * {@inheritDoc}
     */
    public native boolean hasAssertInfo(long clientID);

    /**
     * {@inheritDoc}
     */
    public native OVAAssertInfo getAssertInfo(long clientID, long id);

    /**
     * {@inheritDoc}
     */
    public native boolean addEngineListener(long clientID, int eventID);

    /**
     * {@inheritDoc}
     */
    public native boolean addAssertListener(
        long clientID,
        int eventID,
        long assertID);

    /**
     * {@inheritDoc}
     */
    public native boolean removeAssertListener(int eventID, long assertID);

    /**
     * {@inheritDoc}
     */
    public native boolean removeEngineListener(int eventID);

    /**
     * {@inheritDoc}
     */
    public native boolean setAssertSeverity(
        long clientID,
        long assertID,
        int severity);

    /**
     * {@inheritDoc}
     */
    public native int getAssertSeverity(long clientID, long assertID);

    /**
     * {@inheritDoc}
     */
    public native boolean setAssertCategory(
        long clientID,
        long assertID,
        int category);

    /**
     * {@inheritDoc}
     */
    public native int getAssertCategory(long clientID, long assertID);

    /**
     * {@inheritDoc}
     */
    public native boolean setAssertUserMessage(
        long clientID,
        long assertID,
        String msg);

    /**
     * {@inheritDoc}
     */
    public native String getAssertUserMessage(long clientID, long assertID);

    /**
     * {@inheritDoc}
     */
    public native boolean enableAssertCount(
        long clientID,
        long assertID,
        int eventID);

    /**
     * {@inheritDoc}
     */
    public native boolean disableAssertCount(
        long clientID,
        long assertID,
        int eventID);

    /**
     * {@inheritDoc}
     */
    public native long getAssertCount(long clientID, long assertID, int eventID);
}
