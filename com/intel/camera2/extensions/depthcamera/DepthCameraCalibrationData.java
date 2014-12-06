/*******************************************************************************
 * INTEL CORPORATION PROPRIETARY INFORMATION
 *
 * This software is supplied under the terms of a license agreement or nondisclosure
 * agreement with Intel Corporation and may not be copied or disclosed except in
 * accordance with the terms of that agreement
 * Copyright(c) 2014 Intel Corporation. All Rights Reserved.
 ********************************************************************************/
package com.intel.camera2.extensions.depthcamera;

import java.util.Arrays;

import android.graphics.PointF;
import android.hardware.camera2.CameraCharacteristics;
import android.util.FloatMath;

public class DepthCameraCalibrationData 
{

	/**
	 * Point3DF holds 3D float coordinates
	 */
	public class Point3DF {
	    public float x;
	    public float y;
	    public float z;
	    
	    public Point3DF() {}

	    public Point3DF(float x, float y, float z ) {
	        this.x = x;
	        this.y = y; 
	        this.z = z;
	    }
	    
	    /**
	     * Set the point's x,y,  and z coordinates
	     */
	    public final void set(float x, float y, float z) {
	        this.x = x;
	        this.y = y;
	        this.z = z;
	    }
	    
	    /**
	     * Set the point's x,y and z coordinates to the coordinates of p
	     */
	    public final void set(Point3DF p) { 
	        this.x = p.x;
	        this.y = p.y;
	        this.z = p.z;
	    }
	    
	    public final void negate() { 
	        x = -x;
	        y = -y;
	        z = -z;
	    }
	    
	    public final void offset(float dx, float dy, float dz) {
	        x += dx;
	        y += dy;
	        z += dz;
	    }
	    
	    /**
	     * Returns true if the point's coordinates equal (x,y,z)
	     */
	    public final boolean equals(float x, float y, float z) { 
	        return this.x == x && this.y == y && this.z == z; 
	    }

	    @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (o == null || getClass() != o.getClass()) return false;

	        Point3DF point3d = (Point3DF) o;

	        if (Float.compare(point3d.x, x) != 0) return false;
	        if (Float.compare(point3d.y, y) != 0) return false;
	        if (Float.compare(point3d.z, z) != 0) return false;
	        return true;
	    }

	    @Override
	    public int hashCode() {
	        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
	        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
	        result = 31 * result + (z != +0.0f ? Float.floatToIntBits(z) : 0);
	        return result;
	    }

	    @Override
	    public String toString() {
	        return "Point3DF(" + x + ", " + y  + ", " + z + ")";
	    }

	    /**
	     * Return the euclidian distance from (0,0,0) to the point
	     */
	    public final float length() { 
	    	return FloatMath.sqrt(x * x + y * y + z*z);
	    }
	}
	public class IntrinsicParams
	{
		public IntrinsicParams()
		{
			
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
			this.mDistortion = Arrays.copyOf(mDistortion, 5);;
		}
	
		private PointF mFocalLength;  
		private PointF mPrincipalPoint;
		private double[] mDistortion = new double[3];
	}	
	
	public class ExtrinsicParams
	{
		
		private Point3DF mTranslation;
		private double[][] mRotation = new double[3][3];;
		public ExtrinsicParams()
		{
		}
		public ExtrinsicParams(Point3DF p, double[][] r)
		{
			mTranslation = p;
			setRotation(r);
		}
		public Point3DF getTranslation() {
			return mTranslation;
		}
		public void setTranslation(Point3DF mTranslation) {
			this.mTranslation = mTranslation;
		}
		public double[][] getRotation() {
			return mRotation;
		}
		public void setRotation(double[][] mRotation) {
			for ( int i =0; i< 3; i++)
				this.mRotation[i] = Arrays.copyOf(mRotation[i], 3); 
		}
	}
	
	public static DepthCameraCalibrationData getCalibrationData(CameraCharacteristics characteristics, DepthCameraSetup.DepthOutputSettings settings)
	{
		return null;
	}

	public IntrinsicParams getDepthCameraIntrinsics() {
		return mDepthCameraIntrinsics;
	}
	private void setDepthCameraIntrinsics(IntrinsicParams mDepthCameraIntrinsics) {
		this.mDepthCameraIntrinsics = mDepthCameraIntrinsics;
	}

	public IntrinsicParams getColorCameraIntrinsics() {
		return mColorCameraIntrinsics;
	}

	private void setColorCameraIntrinsics(IntrinsicParams mColorCameraIntrinsics) {
		this.mColorCameraIntrinsics = mColorCameraIntrinsics;
	}
	
	public IntrinsicParams getAuxCamerasIntrinisics(int id) {
		if ( mAuxCameraIntrinisics == null || id < 0 || id >= mAuxCameraIntrinisics.length  )
			return null;
		return this.mAuxCameraIntrinisics[id];
	}

	private void setAuxCamerasIntrinisics(IntrinsicParams mAuxCamerasIntrinisics, int id) {
		if ( mAuxCameraIntrinisics == null || id < 0 || id >= mAuxCameraIntrinisics.length  )
			return;
		this.mAuxCameraIntrinisics[id] = mAuxCamerasIntrinisics;
	}

	public ExtrinsicParams getDepthToColorExtrinsics() {
		return mDepthToColorExtrinsics;
	}

	private void setDepthToColorExtrinsics(ExtrinsicParams mDepthToColorExtrinsics) {
		this.mDepthToColorExtrinsics = mDepthToColorExtrinsics;
	}

	
	public ExtrinsicParams getAuxToColorExtrinsics(int id) {
		if ( mAuxToColorExtrinsics == null || id < 0 || id >= mAuxToColorExtrinsics.length  )
			return null; //TODO throw exception
		return mAuxToColorExtrinsics[id];
	}

	private void setAuxToColorExtrinsics(ExtrinsicParams auxToColorExtrinsics, int id) {
		if ( mAuxToColorExtrinsics == null || id < 0 || id >= mAuxToColorExtrinsics.length  )
			return;
		this.mAuxToColorExtrinsics[id] = auxToColorExtrinsics;
	}

	
	public static final int IR_CAMERA_ID_LEFT = 0;
	public static final int IR_CAMERA_ID_RIGHT = 1;

	
	private IntrinsicParams mDepthCameraIntrinsics;
	private IntrinsicParams mColorCameraIntrinsics;
	private IntrinsicParams[] mAuxCameraIntrinisics;
	
	private ExtrinsicParams mDepthToColorExtrinsics;
	private ExtrinsicParams[] mAuxToColorExtrinsics;
}
