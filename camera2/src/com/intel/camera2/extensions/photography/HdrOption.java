package com.intel.camera2.extensions.photography;

/**
 * the option container class for HDR blending
 */
public class HdrOption {
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
