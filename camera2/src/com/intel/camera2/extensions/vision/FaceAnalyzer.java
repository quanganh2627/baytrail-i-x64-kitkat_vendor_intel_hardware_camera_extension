package com.intel.camera2.extensions.vision;

import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Bitmap.Config;
import android.media.Image;
import android.util.Log;

import com.intel.camera2.extensions.IaFrame;
import com.intel.camera2.extensions.vision.FaceData.BlinkInfo;
import com.intel.camera2.extensions.vision.FaceData.EyeInfo;
import com.intel.camera2.extensions.vision.FaceData.FaceInfo;
import com.intel.camera2.extensions.vision.FaceData.RecognitionInfo;
import com.intel.camera2.extensions.vision.FaceData.SmileInfo;

/**
 * It analyzes the image about Face.<br>
 */
public class FaceAnalyzer {

    private static final String TAG = "FaceAnalyzer";
    private static FaceAnalyzer mFaceAnalyzer;

    private long mFaceDetectionInstance;
    private long mEyeDetectionInstance;
    private long mFaceRecognitionInstance;
    private long mSmileDetectionInstance;
    private long mBlinkDetectionInstance;

    private IaFrame mImage;
    private FaceInfo[] mFaceInfo;
    private EyeInfo[] mEyeInfo;
    private RecognitionInfo[] mRecognitionInfo;
    private SmileInfo[] mSmileInfo;
    private BlinkInfo[] mBlinkInfo;

    /**
     * Get FaceAnalyzer instance.
     * @return FaceAnalyzer singleton instance. If it is not supported, it will return NULL.
     */
    public static FaceAnalyzer getInstance() {
        if (mFaceAnalyzer == null && PVLibraryLoader.isSupported()) {
            mFaceAnalyzer = new FaceAnalyzer();
        }
        return mFaceAnalyzer;
    }

    /**
     * Release internal library instances and resources.
     */
    public static void release() {
        if (mFaceAnalyzer != null) {
            mFaceAnalyzer.releaseLibraryInstances();
            mFaceAnalyzer.releaseValues();
            mFaceAnalyzer = null;
        }
    }

    private FaceAnalyzer() {
    }

    /**
     * Set the image that is analyzed.
     * It is released the previous image and the results.
     * 
     * @param image {@link android.media.Image}: The image converts the gray image internally.
     *                                           So, the input image is able to released after calling this method.
     * @param degree supported degrees: 0, 90, 180, 270.
     */
    public void setImage(Image image, int degree) {
        releaseValues();

        if (image.getFormat() == ImageFormat.YUV_420_888) {
            byte[] imageData = IaFrame.getGrayImageData(image);
            if (imageData != null) {
                int stride = image.getPlanes()[0].getRowStride();
                int width = image.getWidth();
                int height = image.getHeight();
                int format = 2;
                mImage = new IaFrame(imageData, stride, width, height, format, degree);
            }
        } else if (image.getFormat() == ImageFormat.JPEG) {
            ByteBuffer jpegBuffer = image.getPlanes()[0].getBuffer();
            byte[] jpegData = new byte[jpegBuffer.capacity()];
            jpegBuffer.get(jpegData);

            Bitmap bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
            if (bitmap != null) {
                setImage(bitmap, degree);
            }
        }
    }

    /**
     * Set the image that is analyzed.
     * It is released the previous image and the results.
     *
     * @param bitmap {@link android.graphics.Bitmap}: The image converts the gray image internally.
     *                                                So, the input image is able to released after calling this method.
     * @param degree supported degrees: 0, 90, 180, 270.
     */
    public void setImage(Bitmap bitmap, int degree) {
        releaseValues();

        if (bitmap == null || bitmap.getConfig() != Config.ARGB_8888) {
            String reason1 = "bitmap("+bitmap+")";
            String reason2 = (bitmap == null)?"":bitmap.getConfig().toString();
            Log.e(TAG, reason1 + reason2);
            return;
        }

        byte[] imageData = convertToGray(bitmap);
        if (imageData != null) {
            int stride = bitmap.getRowBytes() / 4;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int format = 2;
            mImage = new IaFrame(imageData, stride, width, height, format, degree);
        } else {
            Log.e(TAG, "imageData == null");
        }
    }

    /**
     * Set the image that is analyzed.
     * It is released the previous image and the results.
     *
     * @param frame {@link com.intel.camera2.extensions.IaFrame}:
     *                  The frame instance will be used until to release FaceAnalyzer or to set new image.
     */
    public void setImage(IaFrame frame) {
        releaseValues();
        mImage = frame;
    }

    private boolean checkImage() {
        return (mImage != null);
    }

    /**
     * It returns the FaceInfo array.
     * If there are no the FaceInfo, it analyzes the image.
     *
     * @return FaceInfo[]
     */
    public FaceInfo[] getFaceInfo() {
        if (!checkImage()) return null;

        if (mFaceInfo == null) {
            long instance = getFaceDetectionInstance();
            if (instance != 0) {
                mFaceInfo = FaceDetectionJNI.runInImage(instance, mImage);
            }
        }

        return mFaceInfo;
    }

    /**
     * It returns the EyeInfo array.
     * If there are no the EyeInfo, it analyzes the image.
     *
     * @return EyeInfo[]
     */
    public EyeInfo[] getEyeInfo() {
        if (!checkImage()) return null;

        getFaceInfo();

        if (mEyeInfo == null) {
            long instance = getEyeDetectionInstance();
            if (instance != 0) {
                mEyeInfo = EyeDetectionJNI.runInImage(instance, mImage, mFaceInfo);
            }
        }

        return mEyeInfo;
    }

    /**
     * It returns the RecognitionInfo array.
     * If there are no the RecognitionInfo, it analyzes the image.
     *
     * @return RecognitionInfo[]
     */
    public RecognitionInfo[] getRecognitionInfo() {
        if (!checkImage()) return null;

        getEyeInfo();

        if (mRecognitionInfo == null) {
            long instance = getFaceRecognitionInstance();
            if (instance != 0) {
                mRecognitionInfo = FaceRecognitionJNI.runInImage(instance, mImage, mEyeInfo);
            }
        }

        return mRecognitionInfo;
    }

    /**
     * It returns the SmileInfo array.
     * If there are no the SmileInfo, it analyzes the image.
     *
     * @return SmileInfo[]
     */
    public SmileInfo[] getSmileInfo() {
        if (!checkImage()) return null;

        getEyeInfo();

        if (mSmileInfo == null) {
            long instance = getSmileDetectionInstance();
            if (instance != 0) {
                mSmileInfo = SmileDetectionJNI.runInImage(instance, mImage, mEyeInfo);
            }
        }

        return mSmileInfo;
    }

    /**
     * It returns the BlinkInfo array.
     * If there are no the BlinkInfo, it analyzes the image.
     *
     * @return BlinkInfo[]
     */
    public BlinkInfo[] getBlinkInfo() {
        if (!checkImage()) return null;

        getEyeInfo();

        if (mBlinkInfo == null) {
            long instance = getBlinkDetectionInstance();
            if (instance != 0) {
                mBlinkInfo = BlinkDetectionJNI.runInImage(instance, mImage, mEyeInfo);
            }
        }

        return mBlinkInfo;
    }

    private long getFaceDetectionInstance() {
        if (FaceDetectionJNI.isSupported()) {
            if (mFaceDetectionInstance == 0) {
                mFaceDetectionInstance = FaceDetectionJNI.create();
            }
        }
        return mFaceDetectionInstance;
    }

    private void destroyFaceDetectionInstance() {
        if (mFaceDetectionInstance != 0) {
            FaceDetectionJNI.destroy(mFaceDetectionInstance);
            mFaceDetectionInstance = 0;
        }
    }

    private long getEyeDetectionInstance() {
        if (EyeDetectionJNI.isSupported()) {
            if (mEyeDetectionInstance == 0) {
                mEyeDetectionInstance = EyeDetectionJNI.create();
            }
        }
        return mEyeDetectionInstance;
    }

    private void destroyEyeDetectionInstance() {
        if (mEyeDetectionInstance != 0) {
            EyeDetectionJNI.destroy(mEyeDetectionInstance);
            mEyeDetectionInstance = 0;
        }
    }

    private long getFaceRecognitionInstance() {
        if (FaceRecognitionJNI.isSupported()) {
            if (mFaceRecognitionInstance == 0) {
                mFaceRecognitionInstance = FaceRecognitionJNI.create();
            }
        }
        return mFaceRecognitionInstance;
    }

    private void destroyFaceRecognitionInstance() {
        if (mFaceRecognitionInstance != 0) {
            FaceRecognitionJNI.destroy(mFaceRecognitionInstance);
            mFaceRecognitionInstance = 0;
        }
    }

    private long getSmileDetectionInstance() {
        if (SmileDetectionJNI.isSupported()) {
            if (mSmileDetectionInstance == 0) {
                mSmileDetectionInstance = SmileDetectionJNI.create();
            }
        }
        return mSmileDetectionInstance;
    }

    private void destroySmileDetectionInstance() {
        if (mSmileDetectionInstance != 0) {
            SmileDetectionJNI.destroy(mSmileDetectionInstance);
            mSmileDetectionInstance = 0;
        }
    }

    private long getBlinkDetectionInstance() {
        if (BlinkDetectionJNI.isSupported()) {
            if (mBlinkDetectionInstance == 0) {
                mBlinkDetectionInstance = BlinkDetectionJNI.create();
            }
        }
        return mBlinkDetectionInstance;
    }

    private void destroyBlinkDetectionInstance() {
        if (mBlinkDetectionInstance != 0) {
            BlinkDetectionJNI.destroy(mBlinkDetectionInstance);
            mBlinkDetectionInstance = 0;
        }
    }

    private void releaseLibraryInstances() {
        if (mFaceAnalyzer != null) {
            destroyFaceDetectionInstance();
            destroyEyeDetectionInstance();
            destroyFaceRecognitionInstance();
            destroySmileDetectionInstance();
            destroyBlinkDetectionInstance();
        }
    }

    private void releaseValues() {
        mImage = null;
        mFaceInfo = null;
        mEyeInfo = null;
        mRecognitionInfo = null;
        mSmileInfo = null;
        mBlinkInfo = null;
    }

    private static byte[] convertToGray(Bitmap bitmap) {
        if (FaceDetectionJNI.isSupported()) {
            return FaceDetectionJNI.convertToGray(bitmap);
        } else {
            return null;
        }
    }
}
