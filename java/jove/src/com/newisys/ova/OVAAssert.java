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

import java.util.Set;

/**
 * Class representing an OVA assert. Includes querying the assert for various
 * properties (name, etc) and performing various actions on the the assert.
 * 
 * @author Jon Nall
 */
public final class OVAAssert
{
    final long assertID;
    private final OVAEngine engine;

    // This object is not available to the user code via a getter since the
    // user might try to cache a copy and end up with a stale structure. Instead,
    // getters are available from this class for most of the fields in info.
    // Others can be exposed if need be.
    private OVAAssertInfo info;

    /**
     * Sole constructor. Only the OVAEngine class is allowed to create new
     * OVAAssert objects.
     *
     * @param assertID this assert's unique identifier
     * @param engine the OVAEngine with which this OVAAssert is associated
     * @param info the OVAAssertInfo for this assert
     */
    OVAAssert(long assertID, OVAEngine engine, OVAAssertInfo info)
    {
        this.assertID = assertID;
        this.engine = engine;
        this.info = info;

    }

    /**
     * Adds a handler that will be called when the specified event type occurs
     * for this assert.
     *
     * @param eventType the OVA event type which will trigger <code>handler</code>
     *      to be called
     * @param handler the callback handler to be run when the specified event
     *      type occurs for this assert
     */
    public void addListener(
        OVAAssertEventType eventType,
        OVAAssertCallback handler)
    {
        engine.addAssertListener(this, eventType, handler);
    }

    /**
     * Adds a handler that will be called when any of the event types in
     * <code>eventTypeSet</code> occur for this assert.
     *
     * @param eventTypeSet a <code>Set</code> of <code>OVAAssertEventType</code>
     *      objects which will trigger <code>handler</code> to be called
     * @param handler the callback handler to be run when any of the specified
     *      event types occur for this assert
     */
    public void addListener(
        Set<OVAAssertEventType> eventTypeSet,
        OVAAssertCallback handler)
    {
        for (final OVAAssertEventType type : eventTypeSet)
        {
            addListener(type, handler);
        }
    }

    /**
     * Removes the specified callback handler for the specified event type. If
     * there are other handlers associated with <code>eventType</code>, they
     * will not be affected. Likewise, if <code>handler</code> is associated
     * with event types other than <code>eventType</code> those callbacks will
     * not be affected.
     *
     * @param eventType the type of event on which to stop receiving callbacks
     * @param handler the callback handler that should be removed
     */
    public void removeListener(
        OVAAssertEventType eventType,
        OVAAssertCallback handler)
    {
        engine.removeAssertListener(this, eventType, handler);
    }

    /**
     * Enables counting of the specified event for this assert. The count can
     * be accessed by calling {@link #getCount}. This method resets the count to
     * zero before enabling counting.
     * <P>
     * Only {@link OVAAssertEventType#AttemptSuccess} and
     * {@link OVAAssertEventType#AttemptFailure} have counting support.
     * <P>
     * If counting is already enabled on this OVAAssert for the specified
     * event type, the count is reset, but stays enabled.
     *
     * @param eventType the type of event being counted
     * @throws OVAException if eventType is not AttemptSucces or AttemptFailure
     */
    public void enableCount(OVAAssertEventType eventType)
    {
        if (eventType != OVAAssertEventType.AttemptSuccess
            && eventType != OVAAssertEventType.AttemptFailure)
        {
            throw new OVAException(
                "Only Success and Failure events are supported in enableCount");
        }

        engine.enableAssertCount(this, eventType);
    }

    /**
     * Disables counting of the specified event for this assert. The count
     * will be available via {@link #getCount}.
     * <P>
     * Only {@link OVAAssertEventType#AttemptSuccess} and
     * {@link OVAAssertEventType#AttemptFailure} have counting support.
     *
     * @param eventType the type of event being counted
     * @throws OVAException if eventType is not AttemptSucces or AttemptFailure
     */
    public void disableCount(OVAAssertEventType eventType)
    {
        if (eventType != OVAAssertEventType.AttemptSuccess
            && eventType != OVAAssertEventType.AttemptFailure)
        {
            throw new OVAException(
                "Only Success and Failure events are supported in disableCount");
        }

        engine.disableAssertCount(this, eventType);
    }

    /**
     * Returns the number of times the specified event type has occurred for
     * this assert. If counting has not been enabled
     * (via {@link #enableCount}), this method will throw an
     * OVAException.
     * <P>
     * Only {@link OVAAssertEventType#AttemptSuccess} and
     * {@link OVAAssertEventType#AttemptFailure} have counting support.
     *
     * @param eventType the type of event being counted
     * @return the number of times an event of type <code>eventType</code> has
     *      occurred for this assert since enableCount was called
     * @throws OVAException if counting has not been enabled or if eventType is
     *      not AttemptSucces or AttemptFailure
     */
    public long getCount(OVAAssertEventType eventType)
    {
        if (eventType != OVAAssertEventType.AttemptSuccess
            && eventType != OVAAssertEventType.AttemptFailure)
        {
            throw new OVAException(
                "Only Success and Failure events are supported in getCount");
        }

        try
        {
            return engine.getAssertCount(this, eventType);
        }
        catch (OVAException e)
        {
            throw new OVAException(
                "Tried to getCount for assertion that did not "
                    + "have counting enabled. Assertion: " + this
                    + ", EventType: " + eventType);
        }
    }

    /**
     * Performs the requested action on this assert. Some actions require an
     * attempt identifier, in which case <code>attemptID</code> should be the
     * appropriate identifier. For actions that do not require an attempt
     * identifier, <code>attemptID</code> should be zero.
     *
     * @param action the action to be performed on this assert
     * @param attemptID the attempt identifier if <code>action</code> requires
     *      one, or zero if it does not
     */
    public void doAction(OVAAssertAction action, long attemptID)
    {
        engine.doAssertAction(this, action, attemptID);
    }

    /**
     * Returns the name of this assert. For example, the assert<br>
     * <code>main.foo.bar.baz.assert1.cb</code><br>
     * has a name of <code>"assert1.cb"</code>.
     *
     * @return the name of this assert
     */
    public String getName()
    {
        return info.name;
    }

    /**
     * Returns the scope name of this assert. For example, the assert<br>
     * <code>main.foo.bar.baz.assert1.cb</code><br>
     * has a scope name of <code>"main.foo.bar.baz"</code>.
     *
     * @return the scope name of this assert
     */
    public String getScopeName()
    {
        return info.scopeName;
    }

    /**
     * Returns the expression type of this assert.
     *
     * @return the expression type of this assert
     */
    public OVAExprType getExprType()
    {
        return info.exprType;
    }

    /**
     * Returns the severity of this assert. Severity is a value from 0 to 0xFF.
     * If no severity has been specified, 0 is returned.
     *
     * @return the severity of this assert
     */
    public int getSeverity()
    {
        return info.severity;
    }

    /**
     * Returns the category of this assert. Category is a value from 0 to
     * 0xFFFFFF. If no category has been specified, 0 is returned.
     *
     * @return the category of this assert
     */
    public int getCategory()
    {
        return info.category;
    }

    /**
     * Returns the user message of this assert. If no user message has been
     * specified, the empty string is returned.
     *
     * @return the user message of this assert
     */
    public String getUserMessage()
    {
        return info.userMsg;
    }

    /**
     * Sets the severity of this assert. Severity is a value from 0 to 0xFF.
     *
     * @param severity the severity of this assert
     * @throws IllegalArgumentException if <code>severity</code> is outside of
     *      the range 0 to 0xFF
     */
    public void setSeverity(int severity)
    {
        if ((severity & ~0x000000FF) != 0)
        {
            throw new IllegalArgumentException("Severity [0x"
                + Integer.toHexString(severity)
                + "] out of legal range. Max value is 0x000000FF");
        }
        engine.setAssertSeverity(this, severity);
    }

    /**
     * Sets the category of this assert. Category is a value from 0 to 0xFFFFFF.
     *
     * @param category the category of this assert
     * @throws IllegalArgumentException if <code>category</code> is outside of
     *      the range 0 to 0xFFFFFF
     */
    public void setCategory(int category)
    {
        if ((category & ~0x00FFFFFF) != 0)
        {
            throw new IllegalArgumentException("Category [0x"
                + Integer.toHexString(category)
                + "] out of legal range. Max value is 0x00FFFFFF");
        }
        engine.setAssertCategory(this, category);
    }

    /**
     * Sets the user message of this assert. <code>null</code> messages are
     * not allowed.
     *
     * @param msg the message to associate with this assert
     * @throws IllegalArgumentException if <code>msg</code> is null
     */
    public void setUserMessage(String msg)
    {
        if (msg == null)
        {
            throw new IllegalArgumentException(
                "Cannot set OVAAssert user message to a null string");
        }
        engine.setAssertUserMessage(this, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return (int) ((assertID >> 32) ^ (assertID & ~0));
    }

    // Called by OVAEngine whenever the OVAAssertInfo for this OVAAssert changes
    /**
     * Returns the OVAAssertInfo for this assert
     *
     * @return the OVAAssertInfo for this assert
     */
    OVAAssertInfo getInfo()
    {
        return info;
    }

    /**
     * Updates the OVAAssertInfo for this assert, replacing the
     * existing OVAAssertInfo with <code>info</code>.
     *
     * @param info the OVAAssertInfo for this assert
     */
    void updateAssertInfo(OVAAssertInfo info)
    {
        this.info = info;
    }
}
