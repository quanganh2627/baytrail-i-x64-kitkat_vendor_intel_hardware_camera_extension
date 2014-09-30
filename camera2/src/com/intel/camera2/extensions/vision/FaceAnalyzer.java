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
 * This class analyzes the input image about Face. It provides some facial information
 * that the bound of face, the position of eye, smiling score, etc. Please refer {@link FaceData} for more supporting value.<p>
 * 
 * In addition to these features, it provides the face recognition APIs as below.<br>
 * - It registers the person information that was received through {@link #getRecognitionInfo()}.<br>
 * - If several person information are registered in the database, it recognizes who the person is in a portrait.<br>
 * <br>
 * 
 * This class needs some libraries what there are dependence on Intel specific platform.
 * The {@link #isSupported} method informs that your device can be supported.<br>
 */
public class FaceAnalyzer {

    private static final String TAG = "FaceAnalyzer";
    public static final int SUCCESS = 0;
    public static final int ERROR = -1;
    private static final int MAX_FACE_NUM = 30;
    private static final String KEY_SYSTEM_PROPERTY_DB_PATH = "com.intel.facedb.";

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
    private boolean mTryToGetSmileInfo;
    private boolean mTryToGetBlinkInfo;

    /**
     * It checks for the availability of the FaceAnalyzer library.
     */
    public static boolean isSupported() {
        return PVLibraryLoader.isSupported();
    }

    /**
     * It writes the database path in System property.
     * The path will read by Camera HAL3 to load the database on FaceRecognition library.
     * @Warning
     * - If user don't want to use the FR features on the camera, it is not necessary to call this method.<br>
     * - This method must be called before opening the camera.<br>
     * @param dbPath database file path.
     * @param cameraId It can receive from the getCameraIdList() of CameraManager.
     * @return If the returned value is true, writing database path in system property was success.
     */
    public static boolean setPropertyDatabasePath(String dbPath, String cameraId) {
        boolean bRet = false;
        Log.v(TAG, "cameraId("+cameraId+") dbPath("+dbPath+")");
        if (cameraId != null && !cameraId.isEmpty()) {
            String key = KEY_SYSTEM_PROPERTY_DB_PATH + cameraId;
            try {
                String oldPath = System.setProperty(key, dbPath);
                Log.v(TAG, "Key("+key+") new("+dbPath+") old("+oldPath+")");
                bRet = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bRet;
    }

    /**
     * It creates new FaceAnalyzer instance. If it's not supported, it will return null.
     * @return FaceAnalyzer instance
     */
    public static FaceAnalyzer newInstance() {
        try {
            return new FaceAnalyzer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private FaceAnalyzer() throws Exception {
        if (!isSupported()) {
            throw new Exception("PVLibrary is not supported.");
        }
    }

    /**
     * Release internal library instances and resources.
     */
    public void release() {
        releaseLibraryInstances();
        releaseValues();
    }

    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }

    /**
     * Set the face recognition database file path.<br>
     * The file should be in app directory why the file must be read by HAL.<br>
     * The database file will be created by JNI.
     * @param dbPath database file path.
     */
    public void setFaceRecognitionDataBase(String dbPath) {
        setDBPath(dbPath);
    }

    /**
     * Set the image that will be analyzed.
     * It releases the previous image and the results.
     * 
     * @param image {@link android.media.Image}: The image converts the gray image internally.
     *                                           So, the input image should be released after calling this method.
     * @param degree supported degrees: 0, 90, 180, 270.
     */
    public void setInputImage(Image image, int degree) {
        releaseValues();

        try {
            mImage = new IaFrame(image, IaFrame.PvlFormat.GRAY, degree);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the image that will be analyzed.
     * It releases the previous image and the results.
     *
     * @param bitmap {@link android.graphics.Bitmap}: The image converts the gray image internally.
     *                                                So, the input image should be released after calling this method.
     * @param degree supported degrees: 0, 90, 180, 270.
     */
    public void setInputImage(Bitmap bitmap, int degree) {
        releaseValues();

        try {
            mImage = new IaFrame(bitmap, IaFrame.PvlFormat.GRAY, degree);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * It returns the analyzed face data.
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
     * It returns the analyzed eye data.
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

    private static void printInfo(String infoTitle, Object[] info) {
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
     * It returns the analyzed face recognition data.
     *
     * @return RecognitionInfo[]
     */
    public RecognitionInfo[] getRecognitionInfo() {
        if (mImage == null) {
            Log.v(TAG, "mRecognitionInfo("+mRecognitionInfo+") mImage("+mImage+")");
            return mRecognitionInfo;
        }

        EyeInfo[] eyeInfo = getEyeInfo();
        if (eyeInfo != null) {
            long instance = getFaceRecognitionInstance();
            if (instance != 0) {
                mRecognitionInfo = FaceRecognitionWithDbJNI.runInImage(instance, mImage, eyeInfo);
                printInfo("FR", mRecognitionInfo);
            }
        }

        return mRecognitionInfo;
    }

    /**
     * It returns the analyzed data on smiling.
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
     * It returns the analyzed data on blinking.
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

    /**
     * It returns a new person id that was not registered in database.
     * @return person id
     */
    public int createPersonId() {
        long instance = getFaceRecognitionInstance();
        if (instance != 0) {
            return FaceRecognitionWithDbJNI.getNewPersonId(instance);
        } else {
            return RecognitionInfo.UNKOWN_PERSON_ID;
        }
    }

    /**
     * It returns all face data that were registered in database.
     * @return face data
     */
    public FaceData.RecognitionInfo[] getAllFaceDataFromDatabase() {
        FaceData.RecognitionInfo[] info = null;
        long instance = getFaceRecognitionInstance();
        if (instance != 0) {
            info = FaceRecognitionWithDbJNI.getFacedataInDatabase(instance);
        }

        return info;
    }

    /**
     * It registers the recognition info to database.
     * @param info: The info that was received through calling getRecognitionInfo method.
     */
    public int registerFace(FaceData.RecognitionInfo info) {
        int ret = ERROR;
        long instance = getFaceRecognitionInstance();
        if (instance != 0) {
            ret = FaceRecognitionWithDbJNI.registerFace(instance, info);
        }
        return (ret >= 0)?SUCCESS:ERROR;
    }

    /**
     * It changes the person id to what was applied with face id in the database.
     * @param faceId    It was registered in database.
     * @param personId  It would like to update in database.
     * @return success: 0, fail: minus integer
     */
    public int updatePerson(long faceId, int personId) {
        int ret = ERROR;
        long instance = getFaceRecognitionInstance();
        if (instance != 0) {
            ret = FaceRecognitionWithDbJNI.updatePerson(instance, faceId, personId);
        }
        return (ret >= 0)?SUCCESS:ERROR;
    }

    /**
     * It unregisters the recognition info that is the same with face id in database.
     * @param faceId    It was registered in database.
     * @return  success: 0, fail: minus integer
     */
    public int unregisterFace(long faceId) {
        int ret = ERROR;
        long instance = getFaceRecognitionInstance();
        if (instance != 0) {
            ret = FaceRecognitionWithDbJNI.unregisterFace(instance, faceId);
        }
        return (ret >= 0)?SUCCESS:ERROR;
    }

    /**
     * It unregisters all of the recognition info that is the same with person id in database.
     * @param personId    It was registered in database.
     * @return  success: 0, fail: minus integer
     */
    public int unregisterPerson(int personId) {
        int ret = ERROR;
        long instance = getFaceRecognitionInstance();
        if (instance != 0) {
            ret = FaceRecognitionWithDbJNI.unregisterPerson(instance, personId);
        }
        return (ret >= 0)?SUCCESS:ERROR;
    }

    /**
     * It initializes all info in database.
     */
    public void resetDataBase() {
        long instance = getFaceRecognitionInstance();
        if (instance != 0) {
            FaceRecognitionWithDbJNI.resetDatabase();
        }
    }

    private long getFaceDetectionInstance() {
        if (FaceDetectionJNI.isSupported()) {
            if (mFaceDetectionInstance == 0) {
                mFaceDetectionInstance = FaceDetectionJNI.create();

                FaceDetectionJNI.Param param = FaceDetectionJNI.getParam(mFaceDetectionInstance);
                if (param != null) {
                    param.max_num_faces = MAX_FACE_NUM;
                    FaceDetectionJNI.setParam(mFaceDetectionInstance, param);
                }
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

    private void setDBPath(String dbPath) {
        if (!FaceRecognitionWithDbJNI.isSupported()) {
            return;
        }

        boolean pathChanged = false;
        if (dbPath != null && mDbPath != null) {
            if (mDbPath.compareToIgnoreCase(dbPath) != 0) {
                pathChanged = true;
            }
        } else if (dbPath == null && mDbPath == null) {
            pathChanged = false;
        } else {
            pathChanged = true;
        }

        if (mFaceRecognitionInstance != 0 && pathChanged) {
            FaceRecognitionWithDbJNI.destroy(mFaceRecognitionInstance);
            mFaceRecognitionInstance = 0;
        }

        if (mFaceRecognitionInstance == 0) {
            mFaceRecognitionInstance = FaceRecognitionWithDbJNI.create(dbPath);
            mDbPath = dbPath;
        }
    }

    private long getFaceRecognitionInstance() {
        if (!FaceRecognitionWithDbJNI.isSupported()) {
            return 0;
        }

        if (mFaceRecognitionInstance == 0) {
            mFaceRecognitionInstance = FaceRecognitionWithDbJNI.create(mDbPath);
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
        destroyFaceDetectionInstance();
        destroyEyeDetectionInstance();
        destroyFaceRecognitionInstance();
        destroySmileDetectionInstance();
        destroyBlinkDetectionInstance();
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
        mTryToGetSmileInfo = false;
        mTryToGetBlinkInfo = false;
    }
}
