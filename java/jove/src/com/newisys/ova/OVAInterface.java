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
 * Interface to the OVA API.
 * 
 * @author Jon Nall
 */
public interface OVAInterface
{
    /**
     * Returns a string representing the OVA API version.
     *
     * @return the API version string
     */
    public String getApiVersion();

    /**
     * Registers a client with OVA. A client identifier is returned. This
     * identifier can be zero in the following cases:<br>
     * <ul>
     * <li>There is no OVA support in the simulator</li>
     * <li>There are no OVA assertions present in the design being simulated</li>
     * </ul>
     *
     * @return the client identifier to be used in future calls into the API, or
     *      zero as described above
     */
    public long registerClient();

    /**
     * Sets the specified OVA option.
     *
     * @param clientID the client identifier
     * @param confSwitch the configuration switch to be set
     * @param enable true if <code>confSwitch<code> should be enabled, false
     *      otherwise
     * @return true if the operation is successful, false otherwise
     * @see OVAConfigSwitch
     */
    public boolean setConfigSwitch(long clientID, int confSwitch, boolean enable);

    /**
     * Performs the specified action on the OVA Engine.
     *
     * @param clientID the client identifier
     * @param actionID the type of action to be performed on the OVA engine
     * @param userData user defined data
     * @return true if the operation is successful, false otherwise
     * @see OVAEngineAction
     */
    public boolean doAction(long clientID, int actionID, long userData);

    /**
     * Returns the first assert from the engine, or <code>null</code> if no
     * asserts are present in the design.
     *
     * @param clientID the client identifier
     * @return the assert identifier of the first assert or <code>null</code>
     *      if no asserts are present in the design
     * @see OVAAssert
     */
    public long firstAssert(long clientID);

    /**
     * Returns the next assert from the engine, or <code>null</code> if no
     * more asserts are available. It is expected that {@link #firstAssert} has
     * been called before calling this method.
     *
     * @param clientID the client identifier
     * @return the assert identifier of the next assert or <code>null</code>
     *      if no more asserts are available
     * @see OVAAssert
     */
    public long nextAssert(long clientID);

    /**
     * Performs the specified action on the specified {@link OVAAssert}. Some
     * actions require an attempt identifier. For those that do not,
     * <code>attemptID</code> should be zero.
     *
     * @param clientID the client identifier
     * @param actionID the type of action to be performed on the assert
     * @param assertID the assert identifier
     * @param attemptID the attempt identifier
     * @param userData user defined data
     * @return true if the operation is successful, false otherwise
     * @see OVAAssert
     * @see OVAAssertAction
     */
    public boolean assertDoAction(
        long clientID,
        int actionID,
        long assertID,
        long attemptID,
        long userData);

    /**
     * Returns whether information about OVA asserts is available from OVA.
     *
     * @param clientID the client identifier
     * @return true if assert information is available, false otherwise
     */
    public boolean hasAssertInfo(long clientID);

    /**
     * Returns the information for the specified assert. It is expected that
     * {@link #hasAssertInfo} will be called and return <code>true</code> before
     * calling this method.
     *
     * @param clientID the client identifier
     * @param assertID the assert identifier
     * @return the OVAAssertInfo corresponding to the assert referenced by
     *      <code>assertID</code>
     * @see OVAAssert
     */
    public OVAAssertInfo getAssertInfo(long clientID, long assertID);

    /**
     * Adds a callback for the specified engine event type.
     *
     * @param clientID the client identifier
     * @param eventID the event type
     * @return true if the operation is successful, false otherwise
     * @see OVAEngineEventType
     */
    public boolean addEngineListener(long clientID, int eventID);

    /**
     * Adds a callback for a specified assert and event type.
     *
     * @param clientID the client identifier
     * @param eventID the event type
     * @param assertID the assert identifier
     * @return true if the operation is successful, false otherwise
     * @see OVAAssert
     * @see OVAAssertEventType
     */
    public boolean addAssertListener(long clientID, int eventID, long assertID);

    /**
     * Removes a callback for a specified assert and event type.
     *
     * @param eventID the event type
     * @param assertID the assert identifier
     * @return true if the operation is successful, false otherwise
     * @see OVAAssert
     * @see OVAEngineEventType
     */
    public boolean removeAssertListener(int eventID, long assertID);

    /**
     * Removes a callback for the specified engine event type.
     *
     * @param eventID the event type
     * @return true if the operation is successful, false otherwise
     * @see OVAEngineEventType
     */
    public boolean removeEngineListener(int eventID);

    /**
     * Sets the severity level of the specified assert. The severity should
     * be a value from 0 to 0xFF.
     *
     * @param clientID the client identifier
     * @param assertID the assert identifier
     * @param severity the severity level
     * @return true if the operation is successful, false otherwise
     * @see OVAAssert
     */
    public boolean setAssertSeverity(long clientID, long assertID, int severity);

    /**
     * Returns the current severity level for the specified assert. The
     * severity is a value from 0 to 0xFF.
     *
     * @param clientID the client identifier
     * @param assertID the assert identifier
     * @return the severity level for the assert referenced by
     *      <code>assertID</code>
     * @see OVAAssert
     */
    public int getAssertSeverity(long clientID, long assertID);

    /**
     * Sets the category of the specified assert. The category should
     * be a value from 0 to 0xFFFFFF.
     *
     * @param clientID the client identifier
     * @param assertID the assert identifier
     * @param category the category
     * @return true if the operation is successful, false otherwise
     * @see OVAAssert
     */
    public boolean setAssertCategory(long clientID, long assertID, int category);

    /**
     * Returns the current category for the specified assert. The category is a
     * value from 0 to 0xFFFFFF.
     *
     * @param clientID the client identifier
     * @param assertID the assert identifier
     * @return the category for the assert referenced by <code>assertID</code>
     * @see OVAAssert
     */
    public int getAssertCategory(long clientID, long assertID);

    /**
     * Sets the user message of the specified assert. The user message should
     * be any non-null string.
     *
     * @param clientID the client identifier
     * @param assertID the assert identifier
     * @param msg the user message
     * @return true if the operation is successful, false otherwise
     * @see OVAAssert
     */
    public boolean setAssertUserMessage(long clientID, long assertID, String msg);

    /**
     * Returns the current user message for the specified assert. The user
     * message is a non-null string.
     *
     * @param clientID the client identifier
     * @param assertID the assert identifier
     * @return theuser message for the assert referenced by <code>assertID</code>
     * @see OVAAssert
     */
    public String getAssertUserMessage(long clientID, long assertID);

    /**
     * Enables counting the specified event type for the specified assert. This
     * method is separate from the generic {@link #addAssertListener} method in
     * order to improve performance. This method resets the count to zero before
     * enabling counting.
     *
     * @param clientID the client identifier
     * @param assertID the assert identifier
     * @param eventID the event type being counted
     * @return true if the operation is successful, false otherwise
     * @see OVAAssert
     * @see OVAAssertEventType
     */
    public boolean enableAssertCount(long clientID, long assertID, int eventID);

    /**
     * Disables counting the specified event type for the specified assert. This
     * method is a counterpart to {@link #enableAssertCount}. The event count
     * obtained via {@link #getAssertCount} will continue to be valid after this
     * method is called (unless there is a subsequent call to enableAssertCount).
     *
     * @param clientID the client identifier
     * @param assertID the assert identifier
     * @param eventID the event type being counted
     * @return true if the operation is successful, false otherwise
     * @see OVAAssert
     * @see OVAAssertEventType
     */
    public boolean disableAssertCount(long clientID, long assertID, int eventID);

    /**
     * Returns the number of times the specified event type has occurred for the
     * specified assert since counting was enabled. If counting is not enabled,
     * an OVAException is thrown.
     *
     * @param clientID the client identifier
     * @param assertID the assert identifier
     * @param eventID the event type being counted
     * @return the number of times an event of type <code>eventID</code>
     *      occurred for the assert referenced by <code>assertID</code>
     * @throws OVAException if counting has not been enabled
     * @see OVAAssert
     * @see OVAAssertEventType
     */
    public long getAssertCount(long clientID, long assertID, int eventID);
}
