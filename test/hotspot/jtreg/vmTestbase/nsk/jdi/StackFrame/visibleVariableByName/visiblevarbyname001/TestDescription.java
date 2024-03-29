/*
 * Copyright (c) 2018, 2024, Oracle and/or its affiliates. All rights reserved.
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


/*
 * @test
 *
 * @summary converted from VM Testbase nsk/jdi/StackFrame/visibleVariableByName/visiblevarbyname001.
 * VM Testbase keywords: [quick, jpda, jdi]
 * VM Testbase readme:
 * DESCRIPTION:
 *     The test for the implementation of an object of the type
 *     StackFrame.
 *     The test checks up that a result of the method
 *     com.sun.jdi.StackFrame.visibleVariableByName()
 *     complies with its spec:
 *     public LocalVariable visibleVariableByName(java.lang.String name)
 *                                     throws AbsentInformationException
 *     Finds a LocalVariable that matches the given name and is visible
 *     at the current frame location.
 *     See visibleVariables() for more information on visibility.
 *     Parameters: name - the variable name to find
 *     Returns:    the matching LocalVariable, or
 *                 null if there is no visible variable with the given name.
 *     Throws: AbsentInformationException -
 *             if there is no line number information for this method.
 *             InvalidStackFrameException -
 *             if this stack frame has become invalid.
 *             Once the frame's thread is resumed,
 *             the stack frame is no longer valid.
 *     when a tested program is prepared with full information (see COMMENTS),
 *     hence, AbsentInformationException is not expected to happen.
 *     The test works as follows:
 *     The debugger program - nsk.jdi.StackFrame.visibleVariableByName.visiblevarbyname001;
 *     the debuggee program - nsk.jdi.StackFrame.visibleVariableByName.visiblevarbyname001a.
 *     Using nsk.jdi.share classes,
 *     the debugger gets the debuggee running on another JavaVM,
 *     creates the object debuggee.VM,
 *     establishes a pipe with the debuggee program, and then
 *     send to the programm commands, to which the debuggee replies
 *     via the pipe. Upon getting reply,
 *     the debugger calls corresponding debuggee.VM methods to get
 *     needed data and compares the data got to the data expected.
 *     In case of error the test produces the return value 97 and
 *     a corresponding error message(s).
 *     Otherwise, the test is passed and produces
 *     the return value 95 and no message.
 * COMMENTS:
 *     This test is option depended,
 *     that is its .cfg file contains the option
 *         JAVAC_OPTS=-g
 *     because at the date of preparing the test
 *     javac prepared full information for the test only
 *     been invoked with the option.
 *
 * @library /vmTestbase
 *          /test/lib
 * @build nsk.jdi.StackFrame.visibleVariableByName.visiblevarbyname001
 *        nsk.jdi.StackFrame.visibleVariableByName.visiblevarbyname001a
 *
 * @comment make sure visiblevarbyname001a is compiled with full debug info
 * @clean nsk.jdi.StackFrame.visibleVariableByName.visiblevarbyname001a
 * @compile -g:lines,source,vars ../visiblevarbyname001a.java
 *
 * @run driver
 *      nsk.jdi.StackFrame.visibleVariableByName.visiblevarbyname001
 *      -verbose
 *      -arch=${os.family}-${os.simpleArch}
 *      -waittime=5
 *      -debugee.vmkind=java
 *      -transport.address=dynamic
 *      -debugee.vmkeys="${test.vm.opts} ${test.java.opts}"
 */

