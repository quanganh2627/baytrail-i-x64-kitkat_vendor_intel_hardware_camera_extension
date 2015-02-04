package com.intel.camera2.extensions.depthcamera;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.ArrayList;

import android.graphics.PointF;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.util.FloatMath;
import android.util.Size;
import android.util.Pair;
import android.util.Log;
import android.view.Surface;


/**
* class DepthCameraCalibrationDataMap holds all calibration data that is recieved in the static metadata
* For a certain configuration (resolution, rectification mode), user can query the calibration data of the camera.
* Calibration data will include intrinsics of each node and extrinsics between the nodes.
*/
public class DepthCameraCalibrationDataMap
{
	public class IntrinsicParams
	{
		public IntrinsicParams(PointF focal, PointF principalP, double[] distortion, Size resolution, int cameraId)
		{
			mFocalLength.set(focal.x, focal.y);
			mPrincipalPoint.set(principalP.x, principalP.y) ;
			if ( distortion != null )
			{
				mDistortion = new double[5];
				mDistortion = Arrays.copyOf(distortion, 5);
			}
			setResolution(resolution);
			mCameraId = cameraId;
			
		}
		public IntrinsicParams(float focalx, float focaly, float principalPx, float principalPy, double[] distortion, int width, int height, int cameraId)
		{   
			mFocalLength.set(focalx, focaly);
			mPrincipalPoint.set(principalPx, principalPy) ;
			if ( distortion != null )
			{
				mDistortion = new double[5];
				mDistortion = Arrays.copyOf(distortion, 5);
			}
			mResolution = new Size(width , height);
			mCameraId = cameraId;
			
		}
		public PointF getFocalLength() {
			return mFocalLength;
		}
	
		public void setFocalLength(PointF mFocalLength) {
			this.mFocalLength = mFocalLength;
		}
	
		public PointF getPrincipalPoint() {
			return mPrincipalPoint;
		}
	
		public void setPrincipalPoint(PointF mPrincipalPoint) {
			this.mPrincipalPoint = mPrincipalPoint;
		}
	
		public double[] getDistortion() {
			return mDistortion;
		}
	
		public void setDistortion(double[] mDistortion) {
			if ( this.mDistortion == null )
				mDistortion = new double[5];
			this.mDistortion = Arrays.copyOf(mDistortion, 5);
		}
	
		public Size getResolution()
		{
			return mResolution;
		}
		public void setResolution(Size resolution)
		{
			
			mResolution = new Size(resolution.getWidth() , resolution.getHeight());
		}

		public boolean isRectified()
		{
			return (mDistortion == null);
		}
		@Override 
		public IntrinsicParams clone()
		{
			return new IntrinsicParams(mFocalLength, mPrincipalPoint, mDistortion, mResolution, mCameraId);
		}
		public int getCameraId() {
			return mCameraId;
		}
		private void setCameraId( int id )
		{
			mCameraId = id;
		}
		@Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (o == null || getClass() != o.getClass()) return false;

	        IntrinsicParams intrP = (IntrinsicParams) o;

	        if ( mFocalLength.equals(intrP.getFocalLength()) &&
	        	mPrincipalPoint.equals(intrP.getPrincipalPoint()) &&
	        	mResolution.equals(intrP.getResolution()) && 
	        	Arrays.equals(mDistortion, intrP.getDistortion() ) &&
	        	mCameraId == intrP.getCameraId()
	        		)
	        	return true;
	        return false;
	    }

        @Override
        public String toString()
        {   
            String res = "Resolution  = " + mResolution.toString() + "\n";
            res += "camera Id " + mCameraId + "\n";
            res += "FocalLength  = " + mFocalLength.x + "," + mFocalLength.y +"\n";
            res += "PrincipalPoint  = " + mPrincipalPoint.x + "," + mPrincipalPoint.y +"\n";
            res += "Distortion \n";
            if ( mDistortion != null )
            {
                for ( int i =0; i<5; i++ )
                {
                    res += mDistortion[i] + " ";
                }
            }
            else
                res += "=null";
            return res;
        }

		private PointF mFocalLength = new PointF();  
		private PointF mPrincipalPoint = new PointF();
		private double[] mDistortion = null ; //might be null if rectified
		private Size mResolution;

		private int mCameraId;
	}	
	
	public class ExtrinsicParams
	{
		
		public ExtrinsicParams()
		{
		}
		public ExtrinsicParams(float[] p, double[][] r)
		{
			setTranslation(p);
			setRotation(r);
		}
		public ExtrinsicParams(float[] p, double[] r)
		{
            if ( p != null )
    			setTranslation(p);
			if ( r != null )
				for ( int i =0; i< 3; i++)
					for ( int j =0; j< 3; j++)
						mRotation[i][j] = r[i*3+j];
				
		}
		public float[] getTranslation() {
			return mTranslation;
		}
		public void setTranslation(float[] mTranslation) {
			this.mTranslation = Arrays.copyOf(mTranslation, 3);
		}
		public double[][] getRotation() {
			return mRotation;
		}
		public void setRotation(double[][] mRotation) {
			for ( int i =0; i< 3; i++)
				this.mRotation[i] = Arrays.copyOf(mRotation[i], 3); 
		}
		@Override 
		public ExtrinsicParams clone()
		{
            return new ExtrinsicParams(mTranslation, mRotation);
		}
		
		@Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (o == null || getClass() != o.getClass()) return false;

	        ExtrinsicParams intrP = (ExtrinsicParams) o;

	        if ( !Arrays.equals(mTranslation, intrP.getTranslation()))
	        	return false;
	        double[][] oRotation = intrP.getRotation();
	        for ( int i =0; i< 3; i++)
	        	if ( !Arrays.equals(mRotation[i], oRotation[i]))
	        		return false;
	        return true;
	    }
        @Override
        public String toString()
        {   
            String res = "translation  = ";
            for ( int i =0; i<3; i++ )
            {
                res += mTranslation[i] + " ";
            }
            res += "\n rotation = \n";
            for ( int i =0; i<3; i++ )
            {
                for ( int j =0; j<3; j++ )
                    res += mRotation[i][j] + " ";
                res += "\n";
            }
            return res;
        }
	    private float[] mTranslation = new float[3]; //for rectified case will be [x 0 0 ]
		private double[][] mRotation = new double[3][3];;

	}
	
	public class DepthCameraCalibrationData
	{
		public DepthCameraCalibrationData(float baseline)
		{
			mBaseLine = baseline;
		}
		/** get intrensic/extrinsic for the specifc instance */
		public IntrinsicParams getDepthCameraIntrinsics() {
			return mDepthCameraIntrinsics;
		}
		private void setDepthCameraIntrinsics(IntrinsicParams mDepthCameraIntrinsics) {
			this.mDepthCameraIntrinsics = mDepthCameraIntrinsics.clone();
		}

		public IntrinsicParams getColorCameraIntrinsics() {
			return mColorCameraIntrinsics;
		}

		private void setColorCameraIntrinsics(IntrinsicParams mColorCameraIntrinsics) {
			this.mColorCameraIntrinsics = mColorCameraIntrinsics.clone();
		}
		
		/**
		* aux camera Ids are as returned in DepthCameraMetadata
		*  {@link com.intel.camera.extensions.depthcamera.DepthCameraMetadata.DEPTHCOMMON_AVAILABLE_NODES_LEFT}
		*  {@link com.intel.camera.extensions.depthcamera.DepthCameraMetadata.DEPTHCOMMON_AVAILABLE_NODES_RIGHT}
		*/
		public IntrinsicParams getAuxCamerasIntrinisics(int id) {
            return mAuxCameraIntrinsics.get(id);
		}

		private void setAuxCamerasIntrinisics(IntrinsicParams auxCameraIntrinsics, int id) {
            mAuxCameraIntrinsics.put(id, auxCameraIntrinsics);
		}

		public ExtrinsicParams getDepthToColorExtrinsics() {
			return mDepthToColorExtrinsics;
		}

		private void setDepthToColorExtrinsics(ExtrinsicParams mDepthToColorExtrinsics) {
			this.mDepthToColorExtrinsics = mDepthToColorExtrinsics.clone();
		}

		public ExtrinsicParams getDepthToWorldExtrinsics() {
			return mDepthToWorldExtrinsics;
		}

		private void setDepthToWorldExtrinsics(ExtrinsicParams mDepthToWorldExtrinsics) {
			this.mDepthToWorldExtrinsics = mDepthToWorldExtrinsics.clone();
		}
		public float getBaseLine() { return mBaseLine; }
        @Override 
        public String toString()
        {
            String res = "\nDepth\n" + mDepthCameraIntrinsics.toString();
            res+= "\nColor\n" + mColorCameraIntrinsics.toString();
            res+= "\nDepthToColor\n" + mDepthToColorExtrinsics.toString();
            res+= "\nDepthToWorld\n" + mDepthToWorldExtrinsics.toString();
            Set<Integer> keys = mAuxCameraIntrinsics.keySet();
            for ( Integer k : keys )
            { 
                IntrinsicParams tmp = mAuxCameraIntrinsics.get(k);
                res+= "\nAux " + k.toString() + "\n" +  tmp.toString();
            }
            res+= "\nBaseLine\n" + mBaseLine;
            return res;
        }
		private IntrinsicParams mDepthCameraIntrinsics; //Always rectified
		private IntrinsicParams mColorCameraIntrinsics;
		private ExtrinsicParams mDepthToWorldExtrinsics;
		private ExtrinsicParams mDepthToColorExtrinsics; //color might be rectified or not rectified
		private HashMap<Integer, IntrinsicParams> mAuxCameraIntrinsics = new HashMap<Integer, IntrinsicParams>();
		private float mBaseLine;
	}
	
	/** constructor 
	 * @return **/
	public DepthCameraCalibrationDataMap(CameraCharacteristics c)
	{
		try
		{
			mCalibrationDataArray = c.get(DepthCameraCharacteristics.DEPTHCOMMON_CALIBRATION_DATA);
			nativeCalibrationDataMapClassInit(mCalibrationDataArray);
		}
		catch (Exception e)
		{
			mCalibrationDataArray = null;
		}
	}
	/** 
	* get calibration data class specific to configuration 
	*/
	public DepthCameraCalibrationData getCalibrationData(Size colorResolution, Size depthResolution, boolean isRectified, int colorCameraID /* integrated or DS4 */)
	{
		if ( mCalibrationDataArray == null )
		{
			Log.w(TAG,"Calibration data are not present in Camera Characteristics!!" );
			return null;
		}

		
		DepthCameraCalibrationData res = getCalibrationDataFromMap(colorResolution, depthResolution, isRectified, colorCameraID);
		if (res == null )
		{
			//translate to setting and use getCalibrationData(setting)
			res = nativeGetCalibrationData(colorResolution.getWidth(), colorResolution.getHeight(),
					depthResolution.getWidth(), depthResolution.getHeight(), isRectified, colorCameraID);
			if ( res != null )
				addCalibrationDataToMap(colorResolution, depthResolution, res );	
		}
		//Add to Map
		return res;
	}
	private void addCalibrationDataToMap(Size colorResolution, Size depthResolution, DepthCameraCalibrationData newEntry)
	{
		ArrayList<DepthCameraCalibrationData> calibDataList = calibrationDataMap.get(new Pair<Size, Size>(colorResolution, depthResolution));
		if ( calibDataList == null )
		{
			calibDataList = new ArrayList<DepthCameraCalibrationData>();
			calibDataList.add(newEntry);
			calibrationDataMap.put(new Pair<Size,Size>(colorResolution, depthResolution), calibDataList);
		}
		else
			calibDataList.add(newEntry);	

	}
	private DepthCameraCalibrationData getCalibrationDataFromMap(Size colorResolution, Size depthResolution, boolean isRectified, int cameraId)
	{

		ArrayList<DepthCameraCalibrationData> calibDataList = calibrationDataMap.get(new Pair<Size, Size>(colorResolution, depthResolution));
		
		if ( calibDataList == null )
			return null;
		for (DepthCameraCalibrationData s : calibDataList)
		{
			if (s.getColorCameraIntrinsics().isRectified() == isRectified &&
				s.getColorCameraIntrinsics().getCameraId() == cameraId)
				return s;
		}
		return null;
	}
	/** 
	* get calibration data class specific to configuration 
	*/
	public DepthCameraCalibrationData getCalibrationData(Surface colorSurface, Surface depthSurface, CaptureRequest request, int colorCameraID /* integrated or DS4 */)
	{
		if ( mCalibrationDataArray == null )
		{
			Log.w(TAG,"Calibration data are not present in Camera Characteristics!!" );
			return null;
		}

		boolean isRectified;
		try {
			// Check if we already have this set

			isRectified = (request.get(DepthCaptureRequest.R200_COLOR_RECTIFICATION_MODE) == DepthCameraMetadata.R200_COLOR_RECTIFICATION_MODE_ON );
		}
		catch(Exception e)
		{
			isRectified = false;
		}

		Size colorResolution = new Size(nativeGetSurfaceWidth(colorSurface), nativeGetSurfaceHeight(colorSurface));
		Size depthResolution = new Size(nativeGetSurfaceWidth(depthSurface), nativeGetSurfaceHeight(depthSurface));
		return getCalibrationData( colorResolution, depthResolution, isRectified, colorCameraID);
	}


	//data will be saved in native code using data structure of DS4
	//on query translation to relevant values will be done, and saved in a Map for further reference 
	private HashMap< Pair<Size, Size> , ArrayList<DepthCameraCalibrationData> > calibrationDataMap = new HashMap< Pair<Size, Size> , ArrayList<DepthCameraCalibrationData> >();
	private byte[] mCalibrationDataArray;
	private static final String TAG = "DepthCameraCalibrationDataMap";

	
	private synchronized native DepthCameraCalibrationData nativeGetCalibrationData( int colorWidth, int colorHeight, int depthWidth, int depthHeight, boolean isRectified, int cameraId);
	private synchronized native void nativeCalibrationDataMapClassInit(byte[] calibrationData);
	private synchronized native int nativeGetSurfaceWidth(Surface s);
	private synchronized native int nativeGetSurfaceHeight(Surface s);
	/**
	* We use a class initializer to allow the native code to cache some
	* field offsets.
	*/
	private static native void nativeClassInit();

	/**
	* This fields are used by native code, do not access or modify.
	*/
	private long mNativeContext;

    static {
        	System.loadLibrary("inteldepthcamera_jni");
        	nativeClassInit();
    }
};
