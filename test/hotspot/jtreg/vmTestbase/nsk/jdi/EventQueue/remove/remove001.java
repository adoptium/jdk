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

package nsk.jdi.EventQueue.remove;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.*;

import java.io.*;

import nsk.share.*;
import nsk.share.jpda.*;
import nsk.share.jdi.*;

/**
 * The test checks that a VMDisconnectedException thrown by
 * the JDI method <b>com.sun.jdi.request.EventQueue.remove()</b>
 * will always be preceded by a <code>VMDisconnectEvent</code>
 * when a debuggee part of the test normally exits.
 */
public class remove001 {
    public static final int PASSED = 0;
    public static final int FAILED = 2;
    public static final int JCK_STATUS_BASE = 95;
    static final String DEBUGGEE_CLASS =
        "nsk.jdi.EventQueue.remove.remove001t";
    static final String COMMAND_READY = "ready";
    static final String COMMAND_QUIT = "quit";

    private ArgumentHandler argHandler;
    private Log log;
    private Debugee debuggee;
    private int tot_res = FAILED;

    public static void main(String argv[]) {
        int result = run(argv,System.out);
        if (result != 0) {
            throw new RuntimeException("TEST FAILED with result " + result);
        }
    }

    public static int run(String argv[], PrintStream out) {
        return new remove001().runIt(argv, out);
    }

    private int runIt(String args[], PrintStream out) {
        argHandler = new ArgumentHandler(args);
        log = new Log(out, argHandler);
        Binder binder = new Binder(argHandler, log);

        debuggee = binder.bindToDebugee(DEBUGGEE_CLASS);
        debuggee.redirectStderr(log, "remove001t.err> ");
// dummy IOPipe: just to avoid:
// "Pipe server socket listening error: java.net.SocketException"
        IOPipe pipe = debuggee.createIOPipe();

// Getting JDI events
        checkEvents(debuggee.VM().eventQueue());

        debuggee.waitFor();
        int debStat = debuggee.getStatus();
        if (debStat != (JCK_STATUS_BASE + PASSED)) {
            log.complain("TEST FAILED: debuggee's process finished with status: "
                + debStat);
            tot_res = FAILED;
        } else
            log.display("Debuggee's process finished with status: "
                + debStat);

        return tot_res;
    }

    private void checkEvents(EventQueue eventQ) {
        boolean gotVMDisconnect = false; // VMDisconnectEvent is received
        boolean gotVMDeath = false; // VMDeathEvent is received
        EventSet eventSet = null;

        debuggee.resume();
        while (true) {
            try {
                eventSet = eventQ.remove();
                EventIterator eventIter = eventSet.eventIterator();
                while (eventIter.hasNext()) {
                    Event event = eventIter.nextEvent();
                    if (event instanceof VMDisconnectEvent) {
                        gotVMDisconnect = true;
                        log.display("Got expected VMDisconnectEvent");
                        break;
                    } else if (event instanceof VMStartEvent) {
                        log.display("Got VMStartEvent");
                    } else if (event instanceof VMDeathEvent) {
                        gotVMDeath = true;
                        log.display("Got VMDeathEvent");
                    }
                    if (!gotVMDisconnect && !gotVMDeath &&
                            eventSet.suspendPolicy() !=
                                EventRequest.SUSPEND_NONE) {
                        log.display("Calling EventSet.resume() ...");
                        eventSet.resume();
                    }
                }
            } catch(InterruptedException e) {
                log.complain("TEST INCOMPLETE: caught " + e);
                tot_res = FAILED;
            } catch(VMDisconnectedException e) {
                if (gotVMDisconnect) {
                    log.display("\nCHECK PASSED: caught VMDisconnectedException preceded by a VMDisconnectEvent\n");
                    tot_res = PASSED;
                } else {
                    log.complain("\nTEST FAILED: caught VMDisconnectedException without preceding VMDisconnectEvent\n");
                    e.printStackTrace();
                    tot_res = FAILED;
                }
                break;
            }
        }
        log.display("Stopped JDI events processing");
    }
}
