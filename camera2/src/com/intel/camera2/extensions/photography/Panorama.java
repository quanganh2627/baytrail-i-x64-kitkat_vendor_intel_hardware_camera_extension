package com.intel.camera2.extensions.photography;

import com.intel.camera2.extensions.IaFrame;

public class Panorama {
    private static final String TAG = "Panorama";
    private static Panorama mPanorama;
    private long mJNIInstance;
    private int mPicIndex;
    private IaFrame mTempImage;

    private Panorama(long instance) {
        mJNIInstance = instance;
    }

    public static Panorama getInstance() {
        if (mPanorama == null && PanoramaJNI.isSupported()) {
            long jniInstance = PanoramaJNI.create();
            if (jniInstance != 0) {
                mPanorama = new Panorama(jniInstance);
            }
        }
        return mPanorama;
    }

    public static void release() {
        if (mPanorama != null) {
            mPanorama.releaseLibraryInstances();
            mPanorama = null;
        }
    }

    private void releaseLibraryInstances() {
        if (mJNIInstance != 0) {
            PanoramaJNI.destroy(mJNIInstance);
            mJNIInstance = 0;
        }
    }

    public void initialize() {
        mPicIndex = 0;
        mTempImage = null;
        PanoramaJNI.reset(mJNIInstance);
    }

    public void setParam(int direction) {
        PanoramaJNI.setParam(mJNIInstance, direction);
    }

    public void addImage(IaFrame image) {
        if (false) {
        if (mPicIndex == 0) {
            mTempImage = image;
        } else if (mPicIndex == 1) {
            int direction = getDirection(mTempImage, image);

            PanoramaJNI.reset(mJNIInstance);
            PanoramaJNI.setParam(mJNIInstance, direction);
            PanoramaJNI.stitch(mJNIInstance, image, mPicIndex);
            PanoramaJNI.stitch(mJNIInstance, image, mPicIndex);
        } else {
            PanoramaJNI.stitch(mJNIInstance, image, mPicIndex);
        }
        }
        PanoramaJNI.stitch(mJNIInstance, image, mPicIndex);
        mPicIndex++;
    }

    public IaFrame getStitchedImage() {
        if (mPicIndex > 0) {
            return PanoramaJNI.run(mJNIInstance);
        }
        return null;
    }

    public void setDebug(int debug) {
        PanoramaJNI.setDebug(mJNIInstance, debug);
    }

    private int getDirection(IaFrame first, IaFrame second) {
        PanoramaJNI.reset(mJNIInstance);
        PanoramaJNI.setParam(mJNIInstance, PanoramaJNI.DIRECTION_PREVIEW_AUTO);
        PanoramaJNI.stitch(mJNIInstance, first, 0);
        PanoramaJNI.stitch(mJNIInstance, second, 0);
        PanoramaJNI.Param param = PanoramaJNI.getParam(mJNIInstance);
        if (param != null) {
            return param.direction;
        } else {
            return PanoramaJNI.DIRECTION_PREVIEW_AUTO;
        }
    }
}
