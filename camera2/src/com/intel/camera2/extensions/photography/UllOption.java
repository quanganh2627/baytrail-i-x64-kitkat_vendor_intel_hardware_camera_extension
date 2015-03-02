package com.intel.camera2.extensions.photography;

/**
 * the option container class for ULL blending
 */
public class UllOption {
    final public float analogGain;
    final public float aperture;
    final public float digitalGain;
    final public int exposureTime;
    final public int iso;
    final public int totalExposure;
    final public boolean enabledNdFilter;
    final public int zoomFactor;

    public UllOption(float analogGain, 
            float digitalGain, 
            float aperture, 
            int exposureTimeUs, 
            int iso, 
            int totalExposure, 
            int zoomFactor,
            boolean enabledNdFilter) {
        this.analogGain = analogGain;
        this.digitalGain = digitalGain;
        this.aperture = aperture;
        this.exposureTime = exposureTimeUs;
        this.iso = iso;
        this.totalExposure = totalExposure;
        this.enabledNdFilter = enabledNdFilter;
        this.zoomFactor = zoomFactor;
    }
}
