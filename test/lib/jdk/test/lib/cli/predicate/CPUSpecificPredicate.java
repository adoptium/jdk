/*
 * Copyright (c) 2014, 2022, Oracle and/or its affiliates. All rights reserved.
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

package jdk.test.lib.cli.predicate;

import jdk.test.lib.Platform;
import jdk.test.whitebox.cpuinfo.CPUInfo;

import java.util.function.BooleanSupplier;

public class CPUSpecificPredicate implements BooleanSupplier {
    private final String cpuArchPattern;
    private final String supportedCPUFeatures[];
    private final String unsupportedCPUFeatures[];

    public CPUSpecificPredicate(String cpuArchPattern,
            String supportedCPUFeatures[],
            String unsupportedCPUFeatures[]) {
        this.cpuArchPattern = cpuArchPattern;
        this.supportedCPUFeatures = supportedCPUFeatures;
        this.unsupportedCPUFeatures = unsupportedCPUFeatures;
    }

    @Override
    public boolean getAsBoolean() {
        if (!Platform.getOsArch().matches(cpuArchPattern)) {
            System.out.println("CPU arch " + Platform.getOsArch() + " does not match " + cpuArchPattern);
            return false;
        }

        if (supportedCPUFeatures != null) {
            for (String feature : supportedCPUFeatures) {
                if (!CPUInfo.hasFeature(feature)) {
                    System.out.println("CPU does not support " + feature
                            + " feature");
                    return false;
                }
            }
        }

        if (unsupportedCPUFeatures != null) {
            for (String feature : unsupportedCPUFeatures) {
                if (CPUInfo.hasFeature(feature)) {
                    System.out.println("CPU support " + feature + " feature");
                    return false;
                }
            }
        }
        return true;
    }
}
