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
 * the option container class for HDR blending
 */
final class HdrOption {
    final public int gammaLutSize;
    final public int ctcGainLutSize;
    public char[] rGammaLut;
    public char[] gGammaLut;
    public char[] bGammaLut;
    public char[] ctcGainLut;

    public HdrOption(int gammaLutSize, int ctcGainLutSize) {
        this.gammaLutSize = gammaLutSize;
        this.ctcGainLutSize = ctcGainLutSize;
    }

    public void setGammaLut(char[] rGammaLut, char[] gGammaLut, char[] bGammaLut) {
        if (rGammaLut == null || gGammaLut == null || bGammaLut == null) {
            throw new IllegalArgumentException("lut should not be null");
        }
        if (rGammaLut.length != gammaLutSize ||
                gGammaLut.length != gammaLutSize ||
                bGammaLut.length != gammaLutSize) {
            throw new IllegalArgumentException("lut size should be " + gammaLutSize);
        }
        this.rGammaLut = rGammaLut;
        this.gGammaLut = gGammaLut;
        this.bGammaLut = bGammaLut;
    }
}
