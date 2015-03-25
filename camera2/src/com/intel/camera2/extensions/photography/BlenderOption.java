/*
 * Copyright 2015, Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intel.camera2.extensions.photography;

/**
 * the option container class for {@link MultiFrameBlender}
 */
public class BlenderOption {
    public byte[] binaryData;
    public int binarySize;
    public int target;

    public BlenderOption(int size, int target) {
        if (size != 0) {
            this.binaryData = new byte[size];
        }
        this.binarySize = size;
        if (target > CPJNI.TARGET_REF || target < CPJNI.TARGET_CPU) {
            this.target = CPJNI.TARGET_CPU;
        } else {
            this.target = target;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.hashCode() != hashCode()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
