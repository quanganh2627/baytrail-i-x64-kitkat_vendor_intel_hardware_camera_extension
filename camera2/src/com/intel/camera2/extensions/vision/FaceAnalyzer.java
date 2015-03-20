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
package com.intel.camera2.extensions.vision;

import android.graphics.Bitmap;
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

    private String mDbPath;

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

    private boolean mTryToGetFaceInfo;
    private boolean mTryToGetEyeInfo;
    private boolean mTryToGetRecognitionInfo;
    private boolean mTryToGetSmileInfo;
    private boolean mTryToGetBlinkInfo;

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

    public void loadFaceRecognitionDatabase(String dbPath) {
        getFaceRecognitionInstance(dbPath);
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

        try {
            mImage = new IaFrame(image, IaFrame.PvlFormat.GRAY, degree);
        } catch (Exception e) {
            e.printStackTrace();
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

        try {
            mImage = new IaFrame(bitmap, IaFrame.PvlFormat.GRAY, degree);
        } catch (Exception e) {
            e.printStackTrace();
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

    /**
     * It returns the FaceInfo array.
     * If there are no the FaceInfo, it analyzes the image.
     *
     * @return FaceInfo[]
     */
    public FaceInfo[] getFaceInfo() {
        if (mTryToGetFaceInfo || mImage == null) {
            Log.v(TAG, "mFaceInfo("+mFaceInfo+") mTryToGetFaceInfo("+mTryToGetFaceInfo+") mImage("+mImage+")");
            return mFaceInfo;
        }

        long instance = getFaceDetectionInstance();
        if (instance != 0) {
            mFaceInfo = FaceDetectionJNI.runInImage(instance, mImage);
            mTryToGetFaceInfo = true;
            printInfo("FD", mFaceInfo);
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
        if (mTryToGetEyeInfo || mImage == null) {
            Log.v(TAG, "mEyeInfo("+mEyeInfo+") mTryToGetEyeInfo("+mTryToGetEyeInfo+") mImage("+mImage+")");
            return mEyeInfo;
        }

        FaceInfo[] faceInfo = getFaceInfo();
        if (faceInfo != null) {
            long instance = getEyeDetectionInstance();
            if (instance != 0) {
                mEyeInfo = EyeDetectionJNI.runInImage(instance, mImage, faceInfo);
                mTryToGetEyeInfo = true;
                printInfo("ED", mEyeInfo);
            }
        }

        return mEyeInfo;
    }

    public static void printInfo(String infoTitle, Object[] info) {
        if (info != null && info.length > 0) {
            Log.i(TAG, infoTitle + " result count: " + info.length);
            for (int i = 0; i < info.length; i++) {
                Log.i(TAG, "\t\t["+i+"]" + info[i].toString());
            }
        } else {
            Log.i(TAG, infoTitle + " result: null");
        }
    }

    /**
     * It returns the RecognitionInfo array.
     * If there are no the RecognitionInfo, it analyzes the image.
     *
     * @return RecognitionInfo[]
     */
    public RecognitionInfo[] getRecognitionInfo() {
        if (mTryToGetRecognitionInfo || mImage == null) {
            Log.v(TAG, "mRecognitionInfo("+mRecognitionInfo+") mTryToGetRecognitionInfo("+mTryToGetRecognitionInfo+") mImage("+mImage+")");
            return mRecognitionInfo;
        }

        EyeInfo[] eyeInfo = getEyeInfo();
        if (eyeInfo != null) {
            long instance = getFaceRecognitionInstance(mDbPath);
            if (instance != 0) {
                mRecognitionInfo = FaceRecognitionWithDbJNI.runInImage(instance, mImage, eyeInfo);
                mTryToGetRecognitionInfo = true;
                printInfo("FR", mRecognitionInfo);
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
        if (mTryToGetSmileInfo || mImage == null) {
            Log.v(TAG, "mSmileInfo("+mSmileInfo+") mTryToGetSmileInfo("+mTryToGetSmileInfo+") mImage("+mImage+")");
            return mSmileInfo;
        }

        EyeInfo[] eyeInfo = getEyeInfo();
        if (eyeInfo != null) {
            long instance = getSmileDetectionInstance();
            if (instance != 0) {
                mSmileInfo = SmileDetectionJNI.runInImage(instance, mImage, eyeInfo);
                mTryToGetSmileInfo = true;
                printInfo("SD", mSmileInfo);
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
        if (mTryToGetBlinkInfo || mImage == null) {
            Log.v(TAG, "mBlinkInfo("+mBlinkInfo+") mTryToGetBlinkInfo("+mTryToGetBlinkInfo+") mImage("+mImage+")");
            return mBlinkInfo;
        }

        EyeInfo[] eyeInfo = getEyeInfo();
        if (eyeInfo != null) {
            long instance = getBlinkDetectionInstance();
            if (instance != 0) {
                mBlinkInfo = BlinkDetectionJNI.runInImage(instance, mImage, eyeInfo);
                mTryToGetBlinkInfo = true;
                printInfo("BD", mBlinkInfo);
            }
        }

        return mBlinkInfo;
    }

    public int getNewPersonId() {
        long instance = getFaceRecognitionInstance(mDbPath);
        if (instance != 0) {
            return FaceRecognitionWithDbJNI.getNewPersonId(instance);
        } else {
            return RecognitionInfo.UNKOWN_PERSON_ID;
        }
    }

    public void registerFace(RecognitionInfo info) {
        long instance = getFaceRecognitionInstance(mDbPath);
        if (instance != 0) {
            FaceRecognitionWithDbJNI.registerFace(instance, info);
        }
    }

    public void registerPerson(long faceId, int personId) {
        long instance = getFaceRecognitionInstance(mDbPath);
        if (instance != 0) {
            FaceRecognitionWithDbJNI.updatePerson(instance, faceId, personId);
        }
    }

    public void unregisterFace(long faceId) {
        long instance = getFaceRecognitionInstance(mDbPath);
        if (instance != 0) {
            FaceRecognitionWithDbJNI.unregisterFace(instance, faceId);
        }
    }

    public void unregisterPerson(int personId) {
        long instance = getFaceRecognitionInstance(mDbPath);
        if (instance != 0) {
            FaceRecognitionWithDbJNI.unregisterPerson(instance, personId);
        }
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

    private long getFaceRecognitionInstance(String dbPath) {
        if (FaceRecognitionWithDbJNI.isSupported()) {
            if (mDbPath != null && mDbPath.compareToIgnoreCase(dbPath) != 0) {
                if (mFaceRecognitionInstance != 0) {
                    FaceRecognitionWithDbJNI.destroy(mFaceRecognitionInstance);
                    mFaceRecognitionInstance = 0;
                }
            }

            if (mFaceRecognitionInstance == 0 && dbPath != null && !dbPath.isEmpty()) {
                mFaceRecognitionInstance = FaceRecognitionWithDbJNI.create(dbPath);
                mDbPath = dbPath;
            }
        }
        return mFaceRecognitionInstance;
    }

    private void destroyFaceRecognitionInstance() {
        if (mFaceRecognitionInstance != 0) {
            FaceRecognitionWithDbJNI.destroy(mFaceRecognitionInstance);
            mFaceRecognitionInstance = 0;
            mDbPath = null;
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
        mTryToGetFaceInfo = false;
        mTryToGetEyeInfo = false;
        mTryToGetRecognitionInfo = false;
        mTryToGetSmileInfo = false;
        mTryToGetBlinkInfo = false;
    }
}
