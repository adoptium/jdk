/*
 * Copyright (c) 2003, 2016, Oracle and/or its affiliates. All rights reserved.
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

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

/**
 * @test
 * @key sound
 * @bug 4936397
 * @summary Verify that there'll be either SimpleInputDevice OR DirectAudioDevice
 */
public class NoSimpleInputDevice {

    public static void main(String[] args) throws Exception {
        out("4936397: Verify that there'll be either SimpleInputDevice OR DirectAudioDevice");
        boolean foundSimpleInputDevice = false;
        boolean foundDirectAudioDevice = false;

        Mixer.Info[] aInfos = AudioSystem.getMixerInfo();
        for (int i = 0; i < aInfos.length; i++) {
            try {
                Mixer mixer = AudioSystem.getMixer(aInfos[i]);
                String mixerClass = mixer.getClass().toString();
                if (mixerClass.indexOf("SimpleInputDevice") >= 0) {
                    out("Found SimpleInputDevice: "+aInfos[i]);
                    foundSimpleInputDevice = true;
                }
                if (mixerClass.indexOf("DirectAudioDevice") >= 0) {
                    out("Found DirectAudioDevice: "+aInfos[i]);
                    foundDirectAudioDevice = true;
                }
            } catch (Exception e) {
                out("Unexpected exception: "+e);
            }
        }
        if (aInfos.length == 0) {
            out("[No mixers available] - cannot exercise this test.");
        } else {
            if (foundSimpleInputDevice && foundDirectAudioDevice) {
                out("Found both types of capture devices!");
                throw new Exception("Test FAILED!");
            }
            out("Did not find both types of capture devices. Test passed");
        }
    }

    static void out(String s) {
        System.out.println(s); System.out.flush();
    }
}
