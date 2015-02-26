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
