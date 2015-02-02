package com.intel.camera2.extensions.depthcamera;
import android.media.Image;
import android.graphics.PointF;

public abstract class UVMAPImage extends Image {

	protected UVMAPImage() {
		/* Empty */
	}

	public abstract PointF getUV(int x,int y);
}
