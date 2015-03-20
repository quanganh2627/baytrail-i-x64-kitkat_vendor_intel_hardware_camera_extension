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
