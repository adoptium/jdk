/*
 * Copyright (c) 2001, 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package nsk.jdi.BreakpointRequest.addThreadFilter;

import nsk.share.*;
import nsk.share.jpda.*;
import nsk.share.jdi.*;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

import java.util.*;
import java.io.*;

/**
 * The test for the implementation of an object of the type
 * BreakpointRequest.
 *
 * The test checks that results of the method
 * <code>com.sun.jdi.BreakpointRequest.addThreadFilter()</code>
 * complies with its spec.
 *
 * The test checks up on the following assertion:
 *     Restricts the events generated by this request
 *     to those in the given thread.
 * The cases to test include re-invocations of the method
 * addThreadFilter() on the same BreakpointRequest object.
 * There are two BreakpointRequests to check as follows:
 * (1) For BreakpointRequest2, both invocations are with different
 * ThreadReferences restricting one Breakpoint event to two threads.
 * The test expects no Breakpoint event will be received.
 * (2) For BreakpointRequest1, both invocations are with the same
 * ThreadReference restricting one Breakpoint event to one thread.
 * The test expects this Breakpoint event will be received.
 *
 * The test works as follows.
 * - The debugger resumes the debuggee and waits for the BreakpointEvent.
 * - The debuggee creates two threads, thread1 and thread2, and invokes
 *   the methodForCommunication to be suspended and
 *   to inform the debugger with the event.
 * - Upon getting the BreakpointEvent, the debugger
 *   - sets up BreakpointRequests 1&2 within the method
 *     in the class TestClass which will be calling by both threads,
 *   - restricts the BreakpointRequest1 only to thread1,
 *   - restricts the BreakpointRequest2 to both thread1 and thread2,
 *   - resumes debuggee's main thread, and
 *   - waits for the event.
 * - Debuggee's main thread starts both threads.
 * - Upon getting the event, the debugger performs the checks required.
 */

public class threadfilter004 extends TestDebuggerType1 {

    public static void main (String argv[]) {
        int result = run(argv,System.out);
        if (result != 0) {
            throw new RuntimeException("TEST FAILED with result " + result);
        }
    }

    public static int run (String argv[], PrintStream out) {
        debuggeeName = "nsk.jdi.BreakpointRequest.addThreadFilter.threadfilter004a";
        return new threadfilter004().runThis(argv, out);
    }

    private String testedClassName =
      "nsk.jdi.BreakpointRequest.addThreadFilter.threadfilter004aTestClass";

    protected void testRun() {

        EventRequest eventRequest1 = null;
        EventRequest eventRequest2 = null;

        ThreadReference thread1 = null;
        ThreadReference thread2 = null;

        String thread1Name = "thread1";
        String thread2Name = "thread2";

        String property1 = "BreakpointRequest1";
        String property2 = "BreakpointRequest2";

        String methodName = "method";
        String bpLineName = "breakpointLine";

        ReferenceType testClassReference = null;

        for (int i = 0; ; i++) {

            if (!shouldRunAfterBreakpoint()) {
                vm.resume();
                break;
            }

            display(":::::: case: # " + i);

            switch (i) {

                case 0:
                testClassReference =
                     (ReferenceType) vm.classesByName(testedClassName).get(0);

                thread1 = (ThreadReference) debuggeeClass.getValue(
                                            debuggeeClass.fieldByName(thread1Name));
                thread2 = (ThreadReference) debuggeeClass.getValue(
                                            debuggeeClass.fieldByName(thread2Name));

                eventRequest1 = setting2BreakpointRequest (thread1,
                                       testClassReference, methodName, bpLineName,
                                       EventRequest.SUSPEND_ALL, property1);
                eventRequest2 = setting2BreakpointRequest (thread1,
                                       testClassReference, methodName, bpLineName,
                                       EventRequest.SUSPEND_ALL, property2);

                ((BreakpointRequest) eventRequest1).addThreadFilter(thread1);
                ((BreakpointRequest) eventRequest2).addThreadFilter(thread2);

                display("......waiting for BreakpointEvent in tested thread1");
                Event newEvent = eventHandler.waitForRequestedEvent(new EventRequest[]{eventRequest1, eventRequest2}, waitTime, true);

                if ( !(newEvent instanceof BreakpointEvent)) {
                    setFailedStatus("ERROR: new event is not BreakpointEvent");
                } else {
                    String property = (String) newEvent.request().getProperty("number");
                    display("       got new BreakpointEvent with property 'number' == " + property);

                    if ( !property.equals(property1) ) {
                        setFailedStatus("ERROR: property is not : " + property1);
                    }

                    EventRequest newEventRequest = newEvent.request();
                    if (!newEventRequest.equals(eventRequest1) ) {
                        setFailedStatus("The BreakpointEvent occured not for eventRequest1");
                    }

                    ThreadReference thr = ((BreakpointEvent)newEvent).thread();
                    if (!thr.equals(thread1)) {
                        setFailedStatus("The BreakpointEvent occured in unexpected thread: " + thr);
                    }
                }
                vm.resume();
                break;

                default:
                throw new Failure("** default case 2 **");
            }
        }
        return;
    }

    private BreakpointRequest setting2BreakpointRequest ( ThreadReference thread,
                                                          ReferenceType   testedClass,
                                                          String          methodName,
                                                          String          bpLine,
                                                          int             suspendPolicy,
                                                          String          property        ) {
        try {
            display("......setting up a breakpoint:");
            display("       thread: " + thread + "; class: " + testedClass + "; method: " + methodName + "; line: " + bpLine);

            int n = ( (IntegerValue) testedClass.getValue(testedClass.fieldByName(bpLine) ) ).value();
            Location loc = (Location) ((Method) testedClass.methodsByName(methodName).get(0)).allLineLocations().get(n);

            BreakpointRequest
            bpr = eventRManager.createBreakpointRequest(loc);
            bpr.putProperty("number", property);
            if (thread != null)
                bpr.addThreadFilter(thread);
            bpr.setSuspendPolicy(suspendPolicy);

            display("      a breakpoint has been set up");
            return bpr;
        } catch ( Exception e ) {
            throw new Failure("** FAILURE to set up BreakpointRequest **");
        }
    }
}
