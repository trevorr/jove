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

package com.newisys.eventsched;

public class SimpleUpdateEvent
    implements UpdateEvent
{

    final int x;
    int y = -1;
    int queue;
    SimulationTimeStep step = null;
    SimpleEvaluationEvent evaluationEvent;

    public SimpleUpdateEvent(int x, SimulationTimeStep step, int queue)
    {
        this.x = x;
        this.step = step;
        this.queue = queue;
    }

    public SimpleUpdateEvent(int x)
    {
        this.x = x;
    }

    public void execute()
    {
        y = x * 2;
        if (step != null)
        {
            y = y * 2;
            evaluationEvent = new SimpleEvaluationEvent(y);
            switch (queue % 4)
            {
            case 0:
                step.addEventToActiveQueue(evaluationEvent);
                break;
            case 1:
                step.addEventToInactiveQueue(evaluationEvent);
                break;
            case 2:
                step.addEventToMonitorQueue(evaluationEvent);
                break;
            case 3:
            // fall through on purpose
            default:
                step.addEventToNonblockingAssignUpdateQueue(evaluationEvent);
            }
        }
    }

}
