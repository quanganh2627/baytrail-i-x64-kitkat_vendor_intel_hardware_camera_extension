package com.intel.camera2.extensions.depthcamera;

import android.graphics.PointF;
import android.media.Image;

public abstract class UVMAPImage extends Image {

    protected UVMAPImage() {
		/* Empty */
    }

    public abstract PointF getUV(int x,int y);
}
