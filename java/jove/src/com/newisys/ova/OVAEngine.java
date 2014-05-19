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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.newisys.verilog.VerilogSimTime;
import com.newisys.verilog.VerilogTime;

/**
 * Class representing the underlying OVA engine. Contains methods for
 * configuring and acting on the OVA engine. Also contains methods for adding
 * callback listeners on OVA engine events.
 * 
 * @author Jon Nall
 */
public final class OVAEngine
{
    // OVA constants
    static final int OVA_CLIENTID_NULL = 0;
    static final int OVA_ASSERTID_NULL = 0;

    // Instance methods/members
    private final Map<Long, OVAAssert> assertIdMap = new HashMap<Long, OVAAssert>();
    private final boolean ovaHasSyntaxInfo;
    private final long clientID;
    private final String versionString;
    private final OVAInterface ovaIntf;

    // default access for efficient access by AssertListener
    private final Map<OVAAssertEventType, List<OVAAssertCallback>> assertCallbackMap = new EnumMap<OVAAssertEventType, List<OVAAssertCallback>>(
        OVAAssertEventType.class);
    private final Map<OVAEngineEventType, List<OVAEngineCallback>> engineCallbackMap = new EnumMap<OVAEngineEventType, List<OVAEngineCallback>>(
        OVAEngineEventType.class);
    private final Map<String, OVAAssert> assertNameMap = new HashMap<String, OVAAssert>();
    private final List<OVAAssert> assertList = new LinkedList<OVAAssert>();

    /**
     * Sole constructor. Creates an OVAEngine which is registered with the
     * underlying OVA infrastructure.
     *
     * @param ovaIntf the OVAInterface to be used by this OVAEngine
     */
    public OVAEngine(OVAInterface ovaIntf)
    {
        this.ovaIntf = ovaIntf;

        clientID = ovaIntf.registerClient();

        // ugh. i'd like to assert that clientID is not OVA_CLIENTID_NULL, but
        // it seems OVA passes back a clientID of OVA_CLIENTID_NULL if there
        // are no OVA assertions in the design. instead, just warn the user in
        // case they think they should have assertions in their design
        if (clientID == OVA_CLIENTID_NULL)
        {
            System.err.println("WARNING: OVA is enabled, but there are no OVA"
                + " assertions in the design");
        }

        versionString = ovaIntf.getApiVersion();
        ovaHasSyntaxInfo = ovaIntf.hasAssertInfo(clientID);

        // cache all asserts
        // this makes the assumption that OVA asserts cannot be added at runtime
        OVAAssert curAssert = findAssert(ovaIntf.firstAssert(clientID));
        while (curAssert != null)
        {
            assertList.add(curAssert);
            assertNameMap.put(curAssert.getName(), curAssert);
            curAssert = findAssert(ovaIntf.nextAssert(clientID));
        }

        // add a listener that handles errors internal to the underlying OVA
        // implementation (so they don't get dropped on the floor).
        addListener(OVAEngineEventType.EngineError, new OVAEngineCallback()
        {
            public void run(OVAEngineEventType eventType, VerilogTime simTime)
            {
                assert (eventType == OVAEngineEventType.EngineError);
                throw new OVAException("Internal OVA error at time: " + simTime);
            }
        });
    }

    /**
     * Returns the OVA API version string from the underlying OVA implementation.
     *
     * @return the OVA API version string
     */
    public String getApiVersion()
    {
        return versionString;
    }

    /**
     * Configures the OVAEngine, enabling or disabling certain functionality.
     *
     * @param configSwitch the configuration switch to be set
     * @param enable true if <code>configSwitch</code> should be enabled,
     *      false otherwise
     */
    public void configure(OVAConfigSwitch configSwitch, boolean enable)
    {
        ovaIntf.setConfigSwitch(clientID, configSwitch.getValue(), enable);
    }

    /**
     * Perform the specified action on the OVAEngine.
     *
     * @param action the action to be performed on the OVAEngine
     */
    public void doAction(OVAEngineAction action)
    {
        ovaIntf.doAction(clientID, action.getValue(), 0);
    }

    /**
     * Returns the list of OVAAsserts registered with this OVAEngine.
     *
     * @return a List of the OVAAsserts registered with this OVAEngine
     */
    public List<OVAAssert> getAsserts()
    {
        List<OVAAssert> newList = new ArrayList<OVAAssert>(assertList.size());
        newList.addAll(assertList);
        return newList;
    }

    /**
     * Returns the OVAAssert having the specified name.
     *
     * @param name the name of the requested assert
     * @return the OVAAssert with the specified name, or <code>null</code> if
     *      no such OVAAssert is registered with this OVAEngine
     */
    public OVAAssert getAssert(String name)
    {
        // OVA names for OVAExprType.Check assertions are suffixed with ".cb"
        String ovaName = name + ".cb";
        if (assertNameMap.containsKey(ovaName))
        {
            return assertNameMap.get(ovaName);
        }
        return null;
    }

    // @see OVAAssert#addListener
    void addAssertListener(
        OVAAssert ovaAssert,
        OVAAssertEventType eventType,
        OVAAssertCallback handler)
    {
        // If this listener wants to listen to All events, add them to
        // each event type (except OVAAssertEventType.All)
        if (eventType == OVAAssertEventType.All)
        {
            for (final OVAAssertEventType type : OVAAssertEventType.values())
            {
                if (type == OVAAssertEventType.All) continue;

                addAssertListener(ovaAssert, type, handler);
            }
            return;
        }

        List<OVAAssertCallback> callbackList = assertCallbackMap.get(eventType);

        if (callbackList != null)
        {
            if (!callbackList.contains(handler))
            {
                // a given handler can only exist once for a given eventType
                callbackList.add(handler);
            }
        }
        else
        {
            callbackList = new LinkedList<OVAAssertCallback>();
            callbackList.add(handler);
            assertCallbackMap.put(eventType, callbackList);

            // and register with OVA
            ovaIntf.addAssertListener(clientID, eventType.getValue(),
                ovaAssert.assertID);
        }
    }

    // @see OVAAssert#removeListener
    void removeAssertListener(
        OVAAssert ovaAssert,
        OVAAssertEventType eventType,
        OVAAssertCallback handler)
    {
        // If this listener wants to ignore All events, remove them from
        // each event type (except OVAAssertEventType.All)
        if (eventType == OVAAssertEventType.All)
        {
            for (final OVAAssertEventType type : OVAAssertEventType.values())
            {
                if (type == OVAAssertEventType.All) continue;
                List<OVAAssertCallback> callbackList = assertCallbackMap
                    .get(type);
                if (callbackList != null && callbackList.contains(handler))
                {
                    removeAssertListener(ovaAssert, type, handler);
                }
            }
            return;
        }

        List<OVAAssertCallback> callbackList = assertCallbackMap.get(eventType);
        assert (callbackList != null && callbackList.contains(handler));

        callbackList.remove(handler);

        // remove the callback entirely if the list is empty
        if (callbackList.isEmpty())
        {
            ovaIntf.removeAssertListener(eventType.getValue(),
                ovaAssert.assertID);
            assertCallbackMap.put(eventType, null);
        }
    }

    /**
     * Adds a callback handler for the specified event type. This callback will
     * be run whenever an event of the specified type occurs.
     *
     * @param eventType the event type which will trigger <code>handler</code>
     *      to be run
     * @param handler the callback handler to be run when an event of
     *      <code>eventType</code> occurs.
     */
    public void addListener(
        OVAEngineEventType eventType,
        OVAEngineCallback handler)
    {
        // If this listener wants to listen to All events, add them to
        // each event type (except OVAEngineEventType.All)
        if (eventType == OVAEngineEventType.All)
        {
            for (final OVAEngineEventType type : OVAEngineEventType.values())
            {
                if (type == OVAEngineEventType.All) continue;

                addListener(type, handler);
            }
            return;
        }

        List<OVAEngineCallback> callbackList = engineCallbackMap.get(eventType);

        if (callbackList != null)
        {
            // a given handler can only exist once for a given eventType
            if (!callbackList.contains(handler))
            {
                callbackList.add(handler);
            }
        }
        else
        {
            callbackList = new LinkedList<OVAEngineCallback>();
            callbackList.add(handler);
            engineCallbackMap.put(eventType, callbackList);

            // and register with OVA
            ovaIntf.addEngineListener(clientID, eventType.getValue());
        }
    }

    /**
     * Adds a callback handler for the specified Set of event types. This
     * callback will be run whenever an event of any of the specified types
     * occurs.
     *
     * @param eventTypeSet the set of event types which will trigger
     *      <code>handler</code> to be run
     * @param handler the callback handler to be run when an event matching any
     *      of the types in <code>eventTypeSet</code> occurs.
     */
    public void addListener(
        Set<OVAEngineEventType> eventTypeSet,
        OVAEngineCallback handler)
    {
        for (final OVAEngineEventType type : eventTypeSet)
        {
            addListener(type, handler);
        }
    }

    /**
     * Removes the specified handler associated with the specified event type.
     * This handler will no longer be run when events of type
     * <code>eventType</code> occur. However, if the handler is registered to
     * be run when other types of events occur, it will still be run. Also,
     * if multiple handlers are associated with the specified event type, they
     * will not be affected by this call.
     *
     * @param eventType the event type from which to remove this handler
     * @param handler the handler to remove
     */
    public void removeListener(
        OVAEngineEventType eventType,
        OVAEngineCallback handler)
    {
        // If this listener wants to ignore All events, remove them from
        // each event type (except OVAEngineEventType.All)
        if (eventType == OVAEngineEventType.All)
        {
            for (final OVAEngineEventType type : OVAEngineEventType.values())
            {
                if (type == OVAEngineEventType.All) continue;
                List<OVAEngineCallback> callbackList = engineCallbackMap
                    .get(type);
                if (callbackList != null && callbackList.contains(handler))
                {
                    removeListener(type, handler);
                }
            }
            return;
        }

        List<OVAEngineCallback> callbackList = engineCallbackMap.get(eventType);
        assert (callbackList != null && callbackList.contains(handler));

        callbackList.remove(handler);

        // remove the callback entirely if the list is empty
        if (callbackList.isEmpty())
        {
            ovaIntf.removeEngineListener(eventType.getValue());
            engineCallbackMap.put(eventType, null);
        }
    }

    // OVAAssert helpers
    // These keep OVAAssert from knowing about ovaIntf or clientID
    // @see OVAAssert#doAction
    void doAssertAction(
        OVAAssert ovaAssert,
        OVAAssertAction action,
        long attemptID)
    {
        final long userData = 0;
        ovaIntf.assertDoAction(clientID, action.getValue(), ovaAssert.assertID,
            attemptID, userData);
    }

    // @see OVAAssert#enableCount
    void enableAssertCount(OVAAssert ovaAssert, OVAAssertEventType eventType)
    {
        boolean status = ovaIntf.enableAssertCount(clientID,
            ovaAssert.assertID, eventType.getValue());
        assert (status);
    }

    // @see OVAAssert#disableCount
    void disableAssertCount(OVAAssert ovaAssert, OVAAssertEventType eventType)
    {
        boolean status = ovaIntf.disableAssertCount(clientID,
            ovaAssert.assertID, eventType.getValue());
        assert (status);
    }

    // @see OVAAssert#getCount
    long getAssertCount(OVAAssert ovaAssert, OVAAssertEventType eventType)
    {
        return ovaIntf.getAssertCount(clientID, ovaAssert.assertID, eventType
            .getValue());
    }

    // @see OVAAssert#setSeverity
    void setAssertSeverity(OVAAssert ovaAssert, int severity)
    {
        boolean status = ovaIntf.setAssertSeverity(clientID,
            ovaAssert.assertID, severity);
        assert (status);

        // update the assert's info
        OVAAssertInfo oldInfo = ovaAssert.getInfo();
        ovaAssert.updateAssertInfo(new OVAAssertInfo(oldInfo.name,
            oldInfo.exprType, oldInfo.srcFileBlk, severity, oldInfo.category,
            oldInfo.scopeName, oldInfo.userMsg));
    }

    // @see OVAAssert#setCategory
    void setAssertCategory(OVAAssert ovaAssert, int category)
    {
        boolean status = ovaIntf.setAssertCategory(clientID,
            ovaAssert.assertID, category);
        assert (status);

        // update the assert's info
        OVAAssertInfo oldInfo = ovaAssert.getInfo();
        ovaAssert.updateAssertInfo(new OVAAssertInfo(oldInfo.name,
            oldInfo.exprType, oldInfo.srcFileBlk, oldInfo.severity, category,
            oldInfo.scopeName, oldInfo.userMsg));

    }

    // @see OVAAssert#setUserMessage
    void setAssertUserMessage(OVAAssert ovaAssert, String msg)
    {
        boolean status = ovaIntf.setAssertUserMessage(clientID,
            ovaAssert.assertID, msg);
        assert (status);

        // update the assert's info
        OVAAssertInfo oldInfo = ovaAssert.getInfo();
        ovaAssert.updateAssertInfo(new OVAAssertInfo(oldInfo.name,
            oldInfo.exprType, oldInfo.srcFileBlk, oldInfo.severity,
            oldInfo.category, oldInfo.scopeName, msg));
    }

    // called from native code to dispatch engine callbacks
    void dispatchAssertCallback(
        OVAAssertEventType eventType,
        long simTime,
        long assertID,
        long attemptID)
    {
        // list of callbacks that should be run
        List<OVAAssertCallback> callbackList = new LinkedList<OVAAssertCallback>();

        // All listeners get notified on All events
        if (eventType == OVAAssertEventType.All)
        {
            for (final OVAAssertEventType type : assertCallbackMap.keySet())
            {
                assert (type != OVAAssertEventType.All);
                List<OVAAssertCallback> list = assertCallbackMap.get(type);
                if (list != null)
                {
                    callbackList.addAll(list);
                }
            }
        }
        else
        {
            List<OVAAssertCallback> list = assertCallbackMap.get(eventType);
            // if callbackList is null, we shouldn't be receiving callbacks
            assert (list != null);
            callbackList.addAll(list);
        }
        assert (!callbackList.isEmpty());

        final OVAAssert ovaAssert = findAssert(assertID);
        assert (ovaAssert != null);

        for (final OVAAssertCallback callback : callbackList)
        {
            if (callback.enabledForAssert(ovaAssert))
            {
                callback.run(ovaAssert, eventType, new VerilogSimTime(simTime),
                    attemptID);
            }
        }
    }

    // called from native code to dispatch assert callbacks
    void dispatchEngineCallback(OVAEngineEventType eventType, long simTime)
    {
        List<OVAEngineCallback> callbackList = new LinkedList<OVAEngineCallback>();

        // All listeners get notified on All events
        if (eventType == OVAEngineEventType.All)
        {
            for (final OVAEngineEventType type : engineCallbackMap.keySet())
            {
                assert (type != OVAEngineEventType.All);
                List<OVAEngineCallback> list = engineCallbackMap.get(type);
                if (list != null)
                {
                    callbackList.addAll(list);
                }
            }
        }
        else
        {
            List<OVAEngineCallback> list = engineCallbackMap.get(eventType);
            // if callbackList is null, we shouldn't be receiving callbacks
            assert (list != null);
            callbackList.addAll(list);
        }
        assert (!callbackList.isEmpty());

        for (final OVAEngineCallback callback : callbackList)
        {
            callback.run(eventType, new VerilogSimTime(simTime));
        }
    }

    private OVAAssert findAssert(long assertID)
    {
        if (assertID == OVA_ASSERTID_NULL)
        {
            return null;
        }
        else
        {
            OVAAssert ovaAssert = null;
            if (!assertIdMap.containsKey(assertID))
            {
                assert (ovaHasSyntaxInfo);
                ovaAssert = new OVAAssert(assertID, this, ovaIntf
                    .getAssertInfo(clientID, assertID));
                assertIdMap.put(assertID, ovaAssert);
            }
            else
            {
                ovaAssert = assertIdMap.get(assertID);
            }
            return ovaAssert;
        }
    }
}
