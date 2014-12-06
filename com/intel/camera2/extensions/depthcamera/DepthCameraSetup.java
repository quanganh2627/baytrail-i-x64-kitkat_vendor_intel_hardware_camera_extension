/*******************************************************************************
 * INTEL CORPORATION PROPRIETARY INFORMATION
 *
 * This software is supplied under the terms of a license agreement or nondisclosure
 * agreement with Intel Corporation and may not be copied or disclosed except in
 * accordance with the terms of that agreement
 * Copyright(c) 2014 Intel Corporation. All Rights Reserved.
 ********************************************************************************/

package com.intel.camera2.extensions.depthcamera;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.Size;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;


public  class DepthCameraSetup
{ 
	static public class DepthFrameReader implements AutoCloseable 
	{
		public static final String TAG = "DepthFrameReader";
		private class FrameReaderDepthImage extends DepthImage
		{
			private final int DEPTH_IMAGE_DEPTH_PLANE_IDX = 0;
			private final int DEPTH_IMAGE_UVMAP_PLANE_IDX = 1;
			//will be using the SurfaceImage implemented below, to avoid duplication of JNI interface
			protected FrameReaderDepthImage(SurfaceImage si) {
				mDepthImage = si;
			}

			@Override
			public Point projectWorldToImageCoordinates(int x, int y, int z) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int[] projectImageToWorldCoordinates(int u, int v) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Point[] mapDepthToColorCoordinates(Point[] depthCoordinates) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Point[] mapDepthToColorCoordinates(Point origin, int width,
					int hight) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int getZ(int x, int y) {
				if ( mDepthImage.isImageValid()){
					Plane[] planes = mDepthImage.getPlanes();
					if ( planes != null )
					{
						Plane depthPlane = planes[DEPTH_IMAGE_DEPTH_PLANE_IDX];
						if ( depthPlane == null )
							throw new IllegalStateException("Depth plane is null");
						return depthPlane.getBuffer().getInt(y*depthPlane.getRowStride() + x*depthPlane.getPixelStride());
					}
					else
						throw new IllegalStateException("Depth plane is null");
						
				} else {
	                throw new IllegalStateException("getZ - Image is already released");
	            }
			}
			@Override
			public Plane getUVMapPlane() {
				if ( mDepthImage.isImageValid() && DepthFrameReader.this.mFrameRequestConfig.getUVMapEnabledMode()){
					Plane[] planes = mDepthImage.getPlanes();
					if ( planes != null && planes.length > DEPTH_IMAGE_UVMAP_PLANE_IDX)
					{
						if ( planes[DEPTH_IMAGE_UVMAP_PLANE_IDX] == null )
							throw new IllegalStateException("UV plane is null");
						return planes[DEPTH_IMAGE_UVMAP_PLANE_IDX];
					}
					else
						throw new IllegalStateException("UV plane is null");
						
				} else {
	                throw new IllegalStateException("getUVMapPlane - Image is already released");
	            }
			}
			@Override
			public void close() {
				if ( mDepthImage.isImageValid())
					DepthFrameReader.this.releaseDepthImage(this);
			}

			@Override
			public int getFormat() {
				return mDepthImage.getFormat();
			}

			@Override
			public int getHeight() {
				return mDepthImage.getHeight();
			}

			@Override
			public Plane[] getPlanes() {
				return mDepthImage.getPlanes();
			}

			@Override
			public long getTimestamp() {
				return mDepthImage.getTimestamp();
			}

			@Override
			public int getWidth() {
				return mDepthImage.getWidth();
			}		
			public DepthFrameReader getReader() {
				return DepthFrameReader.this;
			}

			public void clearSurfacePlanes() {
				mDepthImage.clearSurfacePlanes();
			}

			public void setImageValid(boolean b) {
				mDepthImage.setImageValid(b);
			}

			@Override
			public void convertZ16ToRGB(ByteBuffer destBuffer)
		    {
				if ( destBuffer == null)
					return;
				if ( mDepthImage == null )
					return;
				Plane[] planes= mDepthImage.getPlanes();
				if ( planes == null || planes.length == 0 )
					return;
				
				ByteBuffer depthBuff = planes[0].getBuffer(); //assume first plane
				nativeConvertBuffToRGBFormat(depthBuff, destBuffer, depthBuff.capacity());
			}

			@Override
			public void convertUVMapToRGB(ByteBuffer src, ByteBuffer rgbPixels, ByteBuffer dest, int colorWidth, int colorHeight)
			{
				if ( src == null )
					return;
				if ( rgbPixels == null )
					return;
				if ( dest == null )
					return;
				nativeConvertUVMapBuffToRGBFormat(rgbPixels, src, dest, mDepthImage.getWidth(), mDepthImage.getHeight(), colorWidth, colorHeight);
				
			}
			private SurfaceImage mDepthImage;
			
			private synchronized native void nativeConvertBuffToRGBFormat(ByteBuffer src, ByteBuffer dest, int depthsize); //allocated in java updated in c++ 
			private synchronized native void nativeConvertUVMapBuffToRGBFormat(ByteBuffer src, ByteBuffer colorPixels, ByteBuffer dest, int dWidth, int dHeight, int cWidth, int cHeight); //allocated in java updated in c++
		}
		
		
		public class DepthFrame   
		{
			//data on each resolution/format can be retrieved from the image itself
			private DepthFrame(DepthImage di, Image ci, Image irL, Image irR)
			{
				setColorImage(ci);
				setDepthImage(di);
				if ( irL != null || irR != null )
					mIRImage = new Image[MAX_NUM_OF_IRIMAGES];
				setIRImage(irL, 0);
				setIRImage(irR, 1);
				setFrameValid(true);
			}
			private void verifyFrameValidThrowESE()
			{
				if (!mIsFrameValid)
					throw new IllegalStateException("Frame is already released");
					
			}
			public DepthImage getDepthImage() 
			{
				verifyFrameValidThrowESE();
				if ( DepthFrameReader.this.mFrameRequestConfig.getDepthEnabled() )
				{
					return mDepthImage;
				}
				else
				{
					throw new IllegalStateException("Depth stream is not enabled");
				}
			}

			private void setDepthImage(DepthImage mDepthImage) {
				this.mDepthImage = mDepthImage;
			}

			public Image getColorImage() {
				verifyFrameValidThrowESE();
				if ( DepthFrameReader.this.mFrameRequestConfig.getColorEnabled() )
				{
					if ( DepthFrameReader.this.mFrameRequestConfig.getColorPreviewSurface() == null)
						return mColorImage;
					else 
						throw new IllegalStateException("Preview Surface is supplied, cannot get color image");
				}
				else
				{
					throw new IllegalStateException("Color stream is not enabled");
				}
			}

			private void setColorImage(Image mColorImage) {
				this.mColorImage = mColorImage;
			}

			public Image getIRImage(int id) {
				verifyFrameValidThrowESE();
				
				if (DepthFrameReader.this.mFrameRequestConfig.getIREnabled() )
				{	
					
					if ( id == IMAGE_TYPE_IR_LEFT || id == IMAGE_TYPE_IR_RIGHT )
						if ( mIRImage == null )
							return null;
						else
							return mIRImage[id];
					else
						throw new IllegalStateException("Wrong IR Image ID!");
				}
				else
				{
					throw new IllegalStateException("IR stream is not enabled");
				}
			}

			private void setIRImage(Image image, int id) {
				if ( mIRImage != null && mIRImage.length > id )
					this.mIRImage[id] = image;
			}

			private DepthFrameReader getReader() 
			{
		       return DepthFrameReader.this;
		    }
			
			//release all images and return them to the pool, 
			public void close() {
				if ( mIsFrameValid) {
					DepthFrameReader.this.releaseDepthFrame(this);
				}
			}
			
			private void releaseAllImages() {
				if ( mDepthImage != null )
					DepthFrameReader.this.releaseDepthImage(mDepthImage);
				if ( mColorImage != null )
					DepthFrameReader.this.releaseSurfaceImage(mColorImage);
	            if ( mIRImage != null && mIRImage.length >0 && mIRImage[0] != null)
	            	DepthFrameReader.this.releaseSurfaceImage(mIRImage[0]);
	            if ( mIRImage != null && mIRImage.length >1 && mIRImage[1] != null)
		            DepthFrameReader.this.releaseSurfaceImage(mIRImage[1]);
	        }
			public void setFrameValid(boolean b) {
				mIsFrameValid = b;
			}
			private boolean isFrameValid()
			{
				return mIsFrameValid;
			}
			private DepthImage mDepthImage;
			private Image mColorImage;
			private Image[] mIRImage;
			private boolean mIsFrameValid;	
		}
		
		/**
		 * Used for acquiring the different surface/images types, same values on native side
		 */
		
		private static final int IMAGE_TYPE_IR_LEFT = 0;
		private static final int IMAGE_TYPE_IR_RIGHT = 1;
		private static final int IMAGE_TYPE_COLOR = 2;
		private static final int IMAGE_TYPE_DEPTH = 3;
		private static final int MAX_NUM_OF_SURFACES = IMAGE_TYPE_DEPTH + 1;
		private static final int MAX_NUM_OF_IMAGES = IMAGE_TYPE_DEPTH +1;
		private static final int MAX_NUM_OF_IRIMAGES = IMAGE_TYPE_IR_RIGHT + 1;
		
	    /**
	     * Returned by nativeImageSetup when acquiring the image was successful.
	     */
	    private static final int ACQUIRE_SUCCESS = 0;
	    /**
	     * Returned by nativeImageSetup when we couldn't acquire the buffer,
	     * because there were no buffers available to acquire.
	     */
	    private static final int ACQUIRE_NO_BUFS = 1;
	    /**
	     * Returned by nativeImageSetup when we couldn't acquire the buffer
	     * because the consumer has already acquired {@maxImages} and cannot
	     * acquire more than that.
	     */
	    private static final int ACQUIRE_MAX_IMAGES = 2;

	    /**
	     * <p>Create a new reader for depth images only of the desired size and format. </p>
	     *
	     * <p>The {@code config} parameter has all the configurations for the output, which 
	     * images should be creates, which format and size, and the size of the Depth Frame Queue (maxFrames). 
	     * The {@code chars} parameter has the camera characteristics, and has all supported formats/sizes </p>
	     *
	     * @param config
	     *            defines width, height and format of the required outputs (depth, color, ir, uvmap). 
	     *            Supported formats are listed In DepthCameraCharacteristics
		 * @param chars 	
		 *            defines characteristics of the camera
	     */
	    private static DepthFrameReader newInstance(DepthOutputSettings config, CameraCharacteristics chars) {
	       return new DepthFrameReader(config,chars);
	    }
	    
	  
		/**
	     * protected constructor 
	     */
	    protected DepthFrameReader(DepthOutputSettings config, CameraCharacteristics chars) 
	    {
	    	 if ( config == null || chars == null )
	    		 throw new IllegalArgumentException(
		                 "Depth output configuration and CameraCharacterisitics cannot be null");
	    	    
	    	 mFrameRequestConfig = config;
	    	 validateConfiguration();
	    	 
	    	//extract the surface details in case of color preview
	     	Surface previewSurface =  mFrameRequestConfig.getColorPreviewSurface();
	     	if ( previewSurface != null )
	     	{
	     		int format = nativeGetSurfaceFormat(previewSurface);
	     		int width = nativeGetSurfaceWidth(previewSurface);
	     		int height = nativeGetSurfaceHeight( previewSurface);
	     		
	     		mFrameRequestConfig.setColorImageFormat(format);
	     		mFrameRequestConfig.setColorSize(new Size(width,height));
	     	}
	    	mCalibrationData = DepthCameraCharacteristics.getCalibrationData(chars, mFrameRequestConfig);
	    	mMaxFrames = config.getMaxFrames();
	    	if (mMaxFrames < 1) {
	             throw new IllegalArgumentException(
	                 "Maximum outstanding frame count must be at least 1");
	        }
	    	 
	    	 nativeInitAllImages();
	    	 
	    	 mSurfaces = new Surface[MAX_NUM_OF_SURFACES];
	    	 mNumPlanes = new int[MAX_NUM_OF_IMAGES];
	    	 mImageEnabled = new boolean[MAX_NUM_OF_IMAGES];
	    	 //init values
	    	 if ( mFrameRequestConfig.getDepthEnabled())
	    	 {
	    		 mSurfaces[IMAGE_TYPE_DEPTH] = nativeGetSurface(IMAGE_TYPE_DEPTH);
	    		 mNumPlanes[IMAGE_TYPE_DEPTH] = calcNumPlanes(IMAGE_TYPE_DEPTH);
	    	 }
	    	 else
	    	 {
	    		 mSurfaces[IMAGE_TYPE_DEPTH] = null;
	    		 mNumPlanes[IMAGE_TYPE_DEPTH] = 0;
	    	 }
	    	 
	    	 if ( mFrameRequestConfig.getColorEnabled() )
	    	 {
	    		 if ( mFrameRequestConfig.getColorPreviewSurface() == null ) //if we don't already have a preview surface
	    			 mSurfaces[IMAGE_TYPE_COLOR] = nativeGetSurface(IMAGE_TYPE_COLOR);
	    		 else 
	    			 mSurfaces[IMAGE_TYPE_COLOR] = config.getColorPreviewSurface();
	    		 mNumPlanes[IMAGE_TYPE_COLOR] = calcNumPlanes(IMAGE_TYPE_COLOR);
	    	 }
	    	 else
	    	 {
	    		 mSurfaces[IMAGE_TYPE_COLOR] = null;
	    		 mNumPlanes[IMAGE_TYPE_COLOR] = 0;
	    	 }
	    		 
	    	 if ( mFrameRequestConfig.getIREnabled())
	    	 { 
	    		//same surface for left and right, then we split it to 2 images
	    		mSurfaces[IMAGE_TYPE_IR_RIGHT] = mSurfaces[IMAGE_TYPE_IR_LEFT] = nativeGetSurface(IMAGE_TYPE_IR_LEFT);
	    		mNumPlanes[IMAGE_TYPE_IR_LEFT] = calcNumPlanes(IMAGE_TYPE_IR_LEFT);
	    		mNumPlanes[IMAGE_TYPE_IR_RIGHT] = calcNumPlanes(IMAGE_TYPE_IR_RIGHT);
	    	 }
	    	 else
	    	 {
	    		mSurfaces[IMAGE_TYPE_IR_RIGHT] = mSurfaces[IMAGE_TYPE_IR_LEFT] = null;
	    		mNumPlanes[IMAGE_TYPE_IR_LEFT] = 0;
	    		mNumPlanes[IMAGE_TYPE_IR_RIGHT] = 0;
	    	 }
	    	
	    	for ( int i = 0 ; i<MAX_NUM_OF_IMAGES; i++)
	    		mImageEnabled[i] = (mNumPlanes[i] > 0);
	    }


		private void nativeInitAllImages()
	    {
			nativeFrameSyncInit(new WeakReference<DepthFrameReader>(this));
	    	if ( mFrameRequestConfig.getColorEnabled() && mFrameRequestConfig.getColorPreviewSurface() == null)
	    		nativeInit(mFrameRequestConfig.getColorSize().getWidth(),
	    				mFrameRequestConfig.getColorSize().getHeight(),
	    				mFrameRequestConfig.getColorImageFormat(),
	    				IMAGE_TYPE_COLOR, 
	    				mFrameRequestConfig.getColorRectificationMode(),
	    				getCalibrationData(),
	    				mFrameRequestConfig.getMaxFrames());
	    	if ( mFrameRequestConfig.getDepthEnabled() )
	    		nativeInit(mFrameRequestConfig.getDepthIRSize().getWidth(),
	    				mFrameRequestConfig.getDepthIRSize().getHeight(),
	    				mFrameRequestConfig.getDepthImageFormat(),
	    				IMAGE_TYPE_DEPTH, 
	    				mFrameRequestConfig.getUVMapEnabledMode(),
	    				getCalibrationData(),
	    				mFrameRequestConfig.getMaxFrames());
	    	
	    	if ( mFrameRequestConfig.getIREnabled() )
	    	{
	    		nativeInit(mFrameRequestConfig.getDepthIRSize().getWidth(),
	    				mFrameRequestConfig.getDepthIRSize().getHeight(),
	    				mFrameRequestConfig.getIRImageFormat(),
	    				IMAGE_TYPE_IR_LEFT, 
	    				false,//TODO add support for LEFT or LEFT_AND_RIGHT
	    				getCalibrationData(),
	    				mFrameRequestConfig.getMaxFrames());
	    	}
	    }

		/**
	     * The Size of each Depth {@link Image}, in pixels.
	     *
	     * <p>DepthFrameReader guarantees that all DepthImages acquired from DepthFrameReader (for example, with
	     * {@link #acquireNextFrame}) will have the same dimensions as specified in
	     * {@link #newInstance}.</p>
	     *
	     * @return the Size of a depth Image
	     */
	    public Size getDepthSize() 
	    {
	    	if ( mFrameRequestConfig.getDepthEnabled() )
	    		return mFrameRequestConfig.getDepthIRSize();
	    	return null;
	    }
	    public boolean getColorRectificationMode()
	    {
	    	return mFrameRequestConfig.getColorRectificationMode();
	    }
	    /**
	     * The Size of each Color {@link Image}, in pixels, if configured, otherwise null
	     *
	     * <p>DepthFrameReader guarantees that all color Images acquired from it (for example, with
	     * {@link #acquireNextFrame}) will have the same dimensions as specified in
	     * {@link #newInstance}.</p>
	     *
	     * @return the size of an color Image
	     */
	    public Size getColorSize() {
	        return mFrameRequestConfig.getColorSize();
	    }

	    /**
	     * The Size of each IR {@link Image}, in pixels, if configured, otherwise null
	     *
	     * <p>DepthFrameReader guarantees that all IR Images acquired from DepthFrameReader (for example, with
	     * {@link #acquireNextFrame}) will have the same dimensions as specified in
	     * {@link #newInstance}.</p>
	     *
	     * @return the size of an IR Image
	     */
	    public Size getIRSize() {
	    	if ( mFrameRequestConfig.getIREnabled() )
	    		return mFrameRequestConfig.getDepthIRSize();
	    	return null;
	    }

	    /**
	     * The {@link ImageFormat image format} of each Image.
	     *
	     * <p>DepthFrameReader guarantees that all {@link DepthImage depth Images} acquired from DepthFrameReader
	     *  (for example, with {@link #acquireNextFrame}) will have the same format as specified in
	     * {@link #newInstance}.</p>
	     *
	     * @return the format of Depth Image
	     *
	     * @see ImageFormat
	     */
	    public int getDepthImageFormat() {
	    	if ( mFrameRequestConfig.getDepthEnabled() )
	    		return mFrameRequestConfig.getDepthImageFormat();
	    	return ImageFormat.UNKNOWN;
	    }

	    /**
	     * The {@link ImageFormat image format} of each color Image, or unknown if none is configured
	     *
	     * <p>DepthFrameReader guarantees that all {@link Image color Images} acquired from DepthFrameReader
	     *  (for example, with {@link #acquireNextFrame}) will have the same format as specified in
	     * {@link #newInstance}.</p>
	     *
	     * @return the format of color Image
	     *
	     * @see ImageFormat
	     */
	    public int getColorImageFormat() {
	    	if ( mFrameRequestConfig.getColorEnabled())
	    		return mFrameRequestConfig.getColorImageFormat();
	    	return ImageFormat.UNKNOWN;
	    }
	    
	    /**
	     * The {@link ImageFormat image format} of each IR Image, or unknown if none is configured
	     *
	     * <p>DepthFrameReader guarantees that all {@link Image IR Images} acquired from DepthFrameReader
	     *  (for example, with {@link #acquireNextFrame}) will have the same format as specified in
	     * {@link #newInstance}.</p>
	     *
	     * @return the format of ir Image
	     *
	     * @see ImageFormat
	     */
	    public int getIRImageFormat() {
	    	if ( mFrameRequestConfig.getIREnabled() )
	    		return mFrameRequestConfig.getIRImageFormat();
	    	return ImageFormat.UNKNOWN;
	    }
	    /**
	     * Maximum number of DepthFrames that can be acquired from the DepthFrameReader by any time (for example,
	     * with {@link #acquireNextFrame}).
	     *
	     * <p>A frame is considered acquired after it's returned by a function from DepthFrameReader, and
	     * until the frame is {@link DepthFrame#close closed} to release the frame back to the DepthFrameReader.
	     * </p>
	     *
	     * <p>Attempting to acquire more than {@code maxImages} concurrently will result in the
	     * acquire function throwing a {@link IllegalStateException}. Furthermore,
	     * while the max number of images have been acquired by the DepthFrameReader user, the producer
	     * enqueueing additional images may stall until at least one image has been released. </p>
	     *
	     * @return Maximum number of frames for this DepthFrameReader.
	     *
	     * @see Image#close
	     */
	    public int getMaxImages() {
	        return mMaxFrames;
	    }
	    
	    public Surface getDepthSurface() {
	        return mSurfaces[IMAGE_TYPE_DEPTH];
	    }
	    
	    public Surface getColorSurface() {
	    	return mSurfaces[IMAGE_TYPE_COLOR];
	    }

	    public Surface getIRSurface(int id) 
	    {
	    	if ( id == IMAGE_TYPE_IR_LEFT || id == IMAGE_TYPE_IR_RIGHT )
	    		return mSurfaces[id];
	        return null;
	    }
	    
	    
	    /**
	     * <p>
	     * Acquire the latest {@link DepthFrame }s array from the DepthFrameReader's queue, dropping older
	     * {@link DepthFrame frames}. The returned frame will include the images which were enabled for output, other images 
	     *  will be null. Returns {@code null} if no new frame is available.
	     * </p>
	     * <p>
	     * This operation will acquire all the frames possible from the DepthFrameReader,
	     * but {@link #close} all frames that aren't the latest. This function is
	     * recommended to use over {@link #acquireNextFrame} for most use-cases, as it's
	     * more suited for real-time processing.
	     * </p>
	     * <p>
	     * Note that {@link #getMaxFrames maxFrames} should be at least 2 for
	     * {@link #acquireLatestFrame} to be any different than {@link #acquireNextFrame} -
	     * discarding all-but-the-newest {@link Image} requires temporarily acquiring two
	     * {@link Image Images} at once. 
	     * </p>
	     * <p>
	     * This operation will fail by throwing an {@link IllegalStateException} if
	     * {@code maxFrames} have been acquired with {@link #acquireLatestFrame} or
	     * {@link #acquireNextImage}. In particular a sequence of {@link #acquireLatestFrame}
	     * calls greater than {@link #getMaxFrames} without calling {@link Frame#close} in-between
	     * will exhaust the underlying queue. At such a time, {@link IllegalStateException}
	     * will be thrown until more frames are
	     * released with {@link Frame#close}.
	     * </p>
	     *
	     * @return latest frame of image data, or {@code null} if no image data is available.
	     * @throws IllegalStateException if too many frames are currently acquired
	     */
	    public DepthFrame acquireLatestDepthFrame() {
	    	
	    	return acquireDepthFrame(true);
	    }
	   
	    		


		/**
	     * <p>
	     * Acquire the next Frame from the DepthFrameReader's queue. Returns {@code null} if
	     * no new Frame is available.
	     * </p>
	     *
	     * <p><i>Warning:</i> Consider using {@link #acquireLatestFrame()} instead, as it will
	     * automatically release older frames, and allow slower-running processing routines to catch
	     * up to the newest frame. Usage of {@link #acquireNextFrame} is recommended for
	     * batch/background processing. Incorrectly using this function can cause frames to appear
	     * with an ever-increasing delay, followed by a complete stall where no new frames seem to
	     * appear.
	     * </p>
	     *
	     * <p>
	     * This operation will fail by throwing an {@link IllegalStateException} if
	     * {@code maxImages} have been acquired with {@link #acquireNextFrame} or
	     * {@link #acquireLatestFrame}. In particular a sequence of {@link #acquireNextImage} or
	     * {@link #acquireLatestFrame} calls greater than {@link #getMaxImages maxImages} without
	     * calling {@link DepthFrame#close} in-between will exhaust the underlying queue. At such a time,
	     * {@link IllegalStateException} will be thrown until more frames are released with
	     * {@link DepthFrame#close}.
	     * </p>
	     *
	     * @return a new frame of image data, or {@code null} if no image data is available.
	     * @throws IllegalStateException if {@code maxImages} frames are currently acquired
	     * @see #acquireLatestFrame
	     */
	    public DepthFrame acquireNextDepthFrame() {
	    	return acquireDepthFrame(false);
	    }

	    /**
	     * Register a listener to be invoked when a new image becomes available
	     * from the DepthFrameReader.
	     *
	     * @param listener
	     *            The listener that will be run.
	     * @param handler
	     *            The handler on which the listener should be invoked, or null
	     *            if the listener should be invoked on the calling thread's looper.
	     * @throws IllegalArgumentException
	     *            If no handler specified and the calling thread has no looper.
	     */
	    public void setOnDepthFrameAvailableListener(OnDepthFrameAvailableListener listener, Handler handler) {
	        synchronized (mListenerLock) {
	            if (listener != null) {
	                Looper looper = handler != null ? handler.getLooper() : Looper.myLooper();
	                if (looper == null) {
	                    throw new IllegalArgumentException(
	                            "handler is null but the current thread is not a looper");
	                }
	                if (mListenerHandler == null || mListenerHandler.getLooper() != looper) {
	                    mListenerHandler = new ListenerHandler(looper);
	                }
	                mListener = listener;
	            } else {
	                mListener = null;
	                mListenerHandler = null;
	            }
	        }
	    }

	    /**
	     * Callback interface for being notified that a new image is available.
	     *
	     * <p>
	     * The onImageAvailable is called per image basis, that is, callback fires for every new frame
	     * available from DepthFrameReader.
	     * </p>
	     */
	    public interface OnDepthFrameAvailableListener {
	        /**
	         * Callback that is called when a new image is available from DepthFrameReader.
	         *
	         * @param reader the DepthFrameReader the callback is associated with.
	         * @see DepthFrameReader
	         * @see Image
	         */
	        void onDepthFrameAvailable(DepthFrameReader reader);
	    }

	    /**
	     * Free up all the resources associated with this DepthFrameReader.
	     *
	     * <p>
	     * After calling this method, this DepthFrameReader can not be used. Calling
	     * any methods on this DepthFrameReader and frames previously provided by
	     * {@link #acquireNextFrame} or {@link #acquireLatestFrame}
	     * will result in an {@link IllegalStateException}, and attempting to read from
	     * {@link ByteBuffer ByteBuffers} returned by an earlier
	     * {@link Image.Plane#getBuffer Plane#getBuffer} call will
	     * have undefined behavior.
	     * </p>
	     */
	    @Override
	    public void close() {
	    	setOnDepthFrameAvailableListener(null, null);
	    	if ( mFrameRequestConfig.getDepthEnabled() )
	    		nativeClose(IMAGE_TYPE_DEPTH);
	    	if ( mFrameRequestConfig.getColorEnabled() && mFrameRequestConfig.getColorPreviewSurface() == null )
	    		nativeClose(IMAGE_TYPE_COLOR);
	    	if ( mFrameRequestConfig.getIREnabled() )
	    		nativeClose(IMAGE_TYPE_IR_LEFT);
	    	nativeFrameSyncClose();
	    }

	  //return only non-null surfaces
	    public ArrayList<Surface> getSurfaces() {
	    	ArrayList<Surface> res = new ArrayList<Surface>();
			for ( int i=0; i< MAX_NUM_OF_SURFACES; i++)
				if ( mSurfaces[i] != null && i != IMAGE_TYPE_IR_RIGHT) //its the same surface for left and right, add it once
					res.add(mSurfaces[i]);
			return  res;
		}

	    
	    // returns calibration data for current mode selected
	    public byte[] getCalibrationData() 
	    {
	    	//TODO change to return DepthCameraCalibrationData
	    	return mCalibrationData;
	    }

	    @Override
	    protected void finalize() throws Throwable {
	        try {
	            close();
	        } finally {
	            super.finalize();
	        }
	    }
	    

	    ////////////////////
	    //     Private    //
	    ////////////////////
		private int calcNumPlanes(int type) 
		{
			switch ( type)
			{
			case IMAGE_TYPE_IR_LEFT:
			case IMAGE_TYPE_IR_RIGHT:
				if (mFrameRequestConfig.getIREnabled())
					return getNumPlanesFromFormat(mFrameRequestConfig.getIRImageFormat());
				break;
			case IMAGE_TYPE_COLOR:
				if ( mFrameRequestConfig.getColorEnabled() && mFrameRequestConfig.getColorPreviewSurface() == null )
					return getNumPlanesFromFormat(mFrameRequestConfig.getColorImageFormat());
				break;
			case IMAGE_TYPE_DEPTH:
				if ( mFrameRequestConfig.getDepthEnabled())
				{
					int num = getNumPlanesFromFormat(mFrameRequestConfig.getDepthImageFormat());
					if ( mFrameRequestConfig.getUVMapEnabledMode() ) //additional plane for uv map
						num++;
					return num;
				}
				break;
			}
			return 0;
		}

		private void validateConfiguration() 
		{
			//validate user didn't set color format or size while providing preview surface
			if  (mFrameRequestConfig.getColorPreviewSurface() != null )
			{
				if ( mFrameRequestConfig.getColorImageFormat() != ImageFormat.UNKNOWN ||
						mFrameRequestConfig.getColorSize() != null )
				{
					Log.w(TAG,"Color preview Surface is provided, settings of Color Image " +
							"Format or Size might be overriden by the surface's actual settings");
				}
			}
			
		}
		
		private DepthFrame acquireDepthFrame(boolean isLatest) {
	    	
	    	DepthImage di = null;
	    	SurfaceImage[] surfaceImages = { null, null, null, null};
	    	int status = ACQUIRE_SUCCESS;
	    	
	    	for ( int i=0; i< MAX_NUM_OF_IMAGES; i++ )
	    	{
	    		if ( mImageEnabled[i])//stream enabled
	    		{
	    			surfaceImages[i] = new SurfaceImage(i);
	    			if (isLatest)
	    				status = acquireLatestSurfaceImage(surfaceImages[i], i);
	    			else
	    				status = acquireNextSurfaceImage(surfaceImages[i], i);
		    
		    		if ( status != ACQUIRE_SUCCESS )
		    			break;
		    		if ( i == IMAGE_TYPE_DEPTH )
		    		{
		    			di = new FrameReaderDepthImage(surfaceImages[i]);
		    		}
	    		}
	    	}
	    	if ( status != ACQUIRE_SUCCESS )
	    	{
	    		if ( status == ACQUIRE_NO_BUFS )
	    			return null;
	    		if ( status == ACQUIRE_MAX_IMAGES )
	    			 throw new IllegalStateException(
	                         String.format(
	                                 "maxFrames (%d) has already been acquired, " +
	                                 "call #close before acquiring more.", mMaxFrames));
	    			
	    	}
	    	return new DepthFrame(di,surfaceImages[IMAGE_TYPE_COLOR],surfaceImages[IMAGE_TYPE_IR_LEFT], surfaceImages[IMAGE_TYPE_IR_RIGHT]);
	    }

	    private int acquireLatestSurfaceImage(SurfaceImage image, int imageType) {
	    	int status = acquireNextSurfaceImage(image, imageType);
	        
	    	if ( status != ACQUIRE_SUCCESS)
	    		return status;
	    	SurfaceImage last = image;
	    	int resStatus = status;
	        try {
	            for (;;) {
	            	status = acquireNextSurfaceImage(image, imageType);
	            	if ( status != ACQUIRE_SUCCESS)
	            	{
	            		image = last;
	            	    last = null;
	                    return resStatus;
	                }
	                last.close();
	                last = image;
	                resStatus = status;
	            }
	        } finally {
            	if (last != null) {
	                last.close();
	            }
	        }
		}

	
	    /** 
	     * Attempts to acquire the next frame from the underlying native implementation.
	     *
	     * <p>
	     * Note that unexpected failures will throw at the JNI level.
	     * </p>
	     *
	     * @param si A blank SurfaceImage.
	     * @param id of surface image (color, ir left or ir right)
	     * @return One of the {@code ACQUIRE_*} codes that determine success or failure.
	     *
	     * @see #ACQUIRE_MAX_IMAGES
	     * @see #ACQUIRE_NO_BUFS
	     * @see #ACQUIRE_SUCCESS
	     */
	    private int acquireNextSurfaceImage(SurfaceImage si, int imageId) {
	    	int status = nativeImageSetup(si, imageId);

	    	//TODO in case of different frame rate, the color might not be available always,
	    	// a new status type might be needed here to indicate that and return null
	        switch (status) {
	            case ACQUIRE_SUCCESS:
	                si.createSurfacePlanes();
	                si.setImageValid(true);
	            case ACQUIRE_NO_BUFS:
	            case ACQUIRE_MAX_IMAGES:
	                break;
	            default:
	                throw new AssertionError("Unknown nativeImageSetup return code " + status);
	        }

	        return status;
	    }

	 
	    /**
	     * <p>Return the frame to the DepthFrameReader for reuse.</p>
	     */
	    private void releaseDepthFrame(DepthFrame df) {
	        if (df.getReader() != this) {
	            throw new IllegalArgumentException(
	                "This frame was not produced by this DepthFrameReader");
	        }
	        df.releaseAllImages();
	        df.setFrameValid(false);
	    }

	
	    /**
	     * Only a subset of the formats defined in
	     * {@link android.graphics.ImageFormat ImageFormat} and
	     * {@link android.graphics.PixelFormat PixelFormat} are supported by
	     * DepthFrameReader. When reading RGB data from a surface, the formats defined in
	     * {@link android.graphics.PixelFormat PixelFormat} can be used, when
	     * reading YUV, JPEG or raw sensor data (for example, from camera or video
	     * decoder), formats from {@link android.graphics.ImageFormat ImageFormat}
	     * are used.
	     */
	    private int getNumPlanesFromFormat(int format) {
	    	switch (format) {
	            case ImageFormat.YV12:
	            case ImageFormat.YUV_420_888:
	            case ImageFormat.NV21:
	                return 3;
	            case ImageFormat.NV16:
	                return 2;
	            case PixelFormat.RGB_565:
	            case PixelFormat.RGBA_8888:
	            case PixelFormat.RGBX_8888:
	            case PixelFormat.RGB_888:
	            case ImageFormat.JPEG:
	            case ImageFormat.YUY2:
	            case ImageFormat.Y8:
	            case ImageFormat.Y16:
	            case ImageFormat.RAW_SENSOR:
	            case DepthImageFormat.Z16:
	                return 1;
	            default:
	                throw new UnsupportedOperationException(
	                        String.format("Invalid format specified %d", format));
	        }
	        
	    }

	    /**
	     * Called from Native code when an Event happens.
	     *
	     * This may be called from an arbitrary Binder thread, so access to the DepthFrameReader must be
	     * synchronized appropriately.
	     */
	    private static void postEventFromNative(Object selfRef) {
	        @SuppressWarnings("unchecked")
	        WeakReference<DepthFrameReader> weakSelf = (WeakReference<DepthFrameReader>)selfRef;
	        final DepthFrameReader dr = weakSelf.get();
	        if (dr == null) {
	            return;
	        }

	        final Handler handler;
	        synchronized (dr.mListenerLock) {
	            handler = dr.mListenerHandler;
	        }
	        if (handler != null) {
	            handler.sendEmptyMessage(0);
	        }
	    }

	    
	    private final byte[] mCalibrationData; 
	    private final DepthOutputSettings mFrameRequestConfig;
	    private final int mMaxFrames;
	    private final int[] mNumPlanes;
	    private final boolean[] mImageEnabled;
	    private final Surface[] mSurfaces;

	    private final Object mListenerLock = new Object();
	    private OnDepthFrameAvailableListener mListener;
	    private ListenerHandler mListenerHandler;

	    /**
	     * This fields are used by native code, do not access or modify.
	     */
	    private long mColorNativeContext;
	    private long mDepthNativeContext;
	    private long mIRNativeContext;
	    
	    private long mFrameSynchronizer;

	    /**
	     * This custom handler runs asynchronously so callbacks don't get queued behind UI messages.
	     */
	    private final class ListenerHandler extends Handler {
	        public ListenerHandler(Looper looper) {
	            super(looper, null, true /*async*/);
	        }

	        @Override
	        public void handleMessage(Message msg) {
	        	OnDepthFrameAvailableListener listener;
	            synchronized (mListenerLock) {
	                listener = mListener;
	            }
	            if (listener != null) {
	                listener.onDepthFrameAvailable(DepthFrameReader.this);
	            }
	        }
	    }

	    
	    private class SurfaceImage extends android.media.Image {
	        public SurfaceImage(int type) {
	        	mType = type;
	            mIsImageValid = false;
	        }
	        public int getType()
	        {
	        	return mType;
	        }
	        @Override
	        public void close() {
	            if (mIsImageValid) {
	                DepthFrameReader.this.releaseSurfaceImage(this);
	            }
	        }

	        public DepthFrameReader getReader() {
	            return DepthFrameReader.this;
	        }

	        @Override
	        public int getFormat() {
	            if (mIsImageValid) {
	                return DepthFrameReader.this.getImageFormat(mType);
	            } else {
	                throw new IllegalStateException("getFormat Image is already released");
	            }
	        	
	        }

	        @Override
	        public int getWidth() {
	        	if (mIsImageValid) {
	        		Size size = DepthFrameReader.this.getImageSize(mType);
	        		
	        		if (size != null )
	        			return size.getWidth();
	        		else
	        			throw new IllegalStateException("Size is null!");
	            } else {
	                throw new IllegalStateException("getWidth Image is already released");
	            }
	        }

	        @Override
	        public int getHeight() {
	        	if (mIsImageValid) {
	        		Size size = DepthFrameReader.this.getImageSize(mType);
	        		if (size != null )
	        			return size.getHeight();
	        		else
	        			throw new IllegalStateException("Size is null!");
	            } else {
	                throw new IllegalStateException("getheight- Image is already released");
	            }
	        }

	        @Override
	        public long getTimestamp() {
	            if (mIsImageValid) {
	                return mTimestamp;
	            } else {
	                throw new IllegalStateException("getTimestamp Image is already released");
	            }
	        }

	        @Override
	        public Plane[] getPlanes() {
	            if (mIsImageValid) {
	                // Shallow copy is fine.
	                return mPlanes.clone();
	            } else {
	                throw new IllegalStateException("getPlanes Image is already released");
	            }
	        }

	        @Override
	        protected final void finalize() throws Throwable {
	            try {
	                close();
	            } finally {
	                super.finalize();
	            }
	        }

	        private void setImageValid(boolean isValid) {
	            mIsImageValid = isValid;
	        }

	        private boolean isImageValid() {
	            return mIsImageValid;
	        }

	        private void clearSurfacePlanes() {
	            if (mIsImageValid) {
	                for (int i = 0; i < mPlanes.length; i++) {
	                    if (mPlanes[i] != null) {
	                        mPlanes[i].clearBuffer();
	                        mPlanes[i] = null;
	                    }
	                }
	            }
	        }

	        private void createSurfacePlanes() {
	            mPlanes = new SurfacePlane[DepthFrameReader.this.mNumPlanes[mType] ];
	            for (int i = 0; i < DepthFrameReader.this.mNumPlanes[mType]; i++) {
	                mPlanes[i] = nativeCreatePlane(i);
	            }
	        	return ;
	        }
	        private class SurfacePlane extends android.media.Image.Plane {
	            // SurfacePlane instance is created by native code when a new SurfaceImage is created
	            private SurfacePlane(int index, int rowStride, int pixelStride) {
	                mIndex = index;
	                mRowStride = rowStride;
	                mPixelStride = pixelStride;
	            }

	            @Override
	            public ByteBuffer getBuffer() {
	                if (SurfaceImage.this.isImageValid() == false) {
	                    throw new IllegalStateException("Image is already released");
	                }
	                if (mBuffer != null) {
	                    return mBuffer;
	                } else {
	                	if ( SurfaceImage.this.getType() == IMAGE_TYPE_DEPTH && mIndex == 1 ) //UVMAP
	                	{
	                		Size size = DepthFrameReader.this.getDepthSize();
	                		Size colorSize = DepthFrameReader.this.getColorSize();
	                		
							mBuffer =SurfaceImage.this.nativeImageGetUVMapBuffer(DepthFrameReader.this, DepthFrameReader.this.getColorRectificationMode(),
								DepthFrameReader.this.getColorImageFormat(),
								colorSize.getWidth(), 
								colorSize.getHeight()
								);
	                	}
	                	else
	                	{
	                		mBuffer = SurfaceImage.this.nativeImageGetBuffer(mIndex);
	                    }
	                    // Set the byteBuffer order according to host endianness (native order),
	                    // otherwise, the byteBuffer order defaults to ByteOrder.BIG_ENDIAN.
	                    return mBuffer.order(ByteOrder.nativeOrder());
	                }
	            }

	            @Override
	            public int getPixelStride() {
	                if (SurfaceImage.this.isImageValid()) {
	                    return mPixelStride;
	                } else {
	                    throw new IllegalStateException("Image is already released");
	                }
	            }

	            @Override
	            public int getRowStride() {
	                if (SurfaceImage.this.isImageValid()) {
	                    return mRowStride;
	                } else {
	                    throw new IllegalStateException("Image is already released");
	                }
	            }

	            private void clearBuffer() {
	                mBuffer = null;
	            }

	            final private int mIndex;
	            final private int mPixelStride;
	            final private int mRowStride;

	            private ByteBuffer mBuffer;
	        }

			/**
	         * This field is used to keep track of native object and used by native code only.
	         * Don't modify.
	         */
	        private long mUVMapBuffer;

	        /**
	         * This field is used to keep track of native object and used by native code only.
	         * Don't modify.
	         */
	        private long mLockedBuffer;

	        /**
	         * This field is set by native code during nativeImageSetup().
	         */
	        private long mTimestamp;

	        private SurfacePlane[] mPlanes;
	        private boolean mIsImageValid;
	        private int mType;
	        private synchronized native ByteBuffer nativeImageGetUVMapBuffer(DepthFrameReader obj, boolean rectMode, int colorWidth, int colorHeight, int colorFormat);
	        private synchronized native ByteBuffer nativeImageGetBuffer(int idx);
	        private synchronized native SurfacePlane nativeCreatePlane(int idx);
	    }

	    
		private void releaseSurfaceImage(Image i) 
		{
	        if (! (i instanceof SurfaceImage) ) { 
	            throw new IllegalArgumentException(
	                "This image was not produced by a DepthFrameReader");
	        }
	        SurfaceImage si = (SurfaceImage) i;
	        if (si.getReader() != DepthFrameReader.this) {
	            throw new IllegalArgumentException(
	                "This image was not produced by this DepthFrameReader");
	        }
	        si.clearSurfacePlanes();
	        nativeReleaseImage(i, si.mType);
	        si.setImageValid(false);
	    }
		
		public Size getImageSize(int mType) {
			switch (  mType ){
			case IMAGE_TYPE_IR_LEFT:
			case IMAGE_TYPE_IR_RIGHT:
				return getIRSize();
			case IMAGE_TYPE_COLOR:
				return getColorSize();
			case IMAGE_TYPE_DEPTH:
				return getDepthSize();
			}
		    throw new IllegalArgumentException(
	                "Wrong image type");
		}


		private int getImageFormat(int mType) {
			switch (  mType ){
			case IMAGE_TYPE_IR_LEFT:
			case IMAGE_TYPE_IR_RIGHT:
				return getIRImageFormat();
			case IMAGE_TYPE_COLOR:
				return getColorImageFormat();
			case IMAGE_TYPE_DEPTH:
				return getDepthImageFormat();
			}
		    throw new IllegalArgumentException(
	                "Wrong image type");
		}


		private void releaseDepthImage(Image i) 
		{
	        if (! (i instanceof FrameReaderDepthImage) ) {
	            throw new IllegalArgumentException(
	                "This image was not produced by a DepthFrameReader");
	        }
	        FrameReaderDepthImage si = (FrameReaderDepthImage) i;
	        if (si.getReader() != DepthFrameReader.this) {
	            throw new IllegalArgumentException(
	                "This image was not produced by this DepthFrameReader");
	        }

	        si.clearSurfacePlanes();
	        nativeReleaseImage(si.mDepthImage, IMAGE_TYPE_DEPTH);
	        si.setImageValid(false);
	    }
		
		////Native interface
	    private synchronized native  int nativeGetSurfaceHeight(Surface previewSurface);
		private synchronized native  int nativeGetSurfaceWidth(Surface previewSurface);
		private synchronized native  int nativeGetSurfaceFormat(Surface previewSurface);

		private synchronized native void nativeInit(int w, int h, int fmt, int imgType,boolean imgParam,byte[] calibdata, int maxImgs); 
	    
	    private synchronized native void nativeClose(int imgType);
	    private synchronized native void nativeReleaseImage(Image i, int imgType);
	    private synchronized native Surface nativeGetSurface( int imgType);

	    private synchronized native void nativeFrameSyncClose();
	    private synchronized native void nativeFrameSyncInit(Object weakSelf);
	    
	    /**
	     * @param imageId - type of the image 
	     * @return A return code {@code ACQUIRE_*}
	     *
	     * @see #ACQUIRE_SUCCESS
	     * @see #ACQUIRE_NO_BUFS
	     * @see #ACQUIRE_MAX_IMAGES
	     */
	    private synchronized native int nativeImageSetup(Image i, int imageId);

	    /**
	     * We use a class initializer to allow the native code to cache some
	     * field offsets.
	     */
	    private static native void nativeClassInit();
	    static {
	        System.loadLibrary("inteldepthcamera_jni"); 
	        nativeClassInit();
	    }
	}

	static public class DepthOutputSettings
	{
		private static DepthOutputSettings newInstance() 
		{
		       return new DepthOutputSettings();
		}
		protected DepthOutputSettings()
		{
			mColorPreviewSurface = null;
			mMaxFrames = 3;//greater than zero - minimum number necessary
			
			mIsColorEnabled = false;
			mIsDepthEnabled = false;
			mIsIREnabled = false;
			mIsColorRectificationModeEnabled = false;
			mIsUVMapEnabled = false;
			
			mDepthIRSize = null;
			mColorSize = null;
			
			mDepthFormat = ImageFormat.UNKNOWN;
			mColorFormat = ImageFormat.UNKNOWN;
			mIRFormat = ImageFormat.UNKNOWN;
		}
		
		public int getMaxFrames() //will configure the max frames for the frame reader
		{
			return mMaxFrames;
		}
		public void setMaxFrames(int x )
		{
			mMaxFrames = x;
		}
		//helper class for configuring output based on template
		public  Size getDepthIRSize() {
			return mDepthIRSize;
		}
		public  void setDepthIRSize(Size resolution) {
			mDepthIRSize = resolution;
		}
		
		public  int getDepthImageFormat() {
			return mDepthFormat;
		}
		public  void setDepthImageFormat(int format) {
			mDepthFormat = format;
		}

		public  int getIRImageFormat() {
			return mIRFormat;
		}
		public  void setIRImageFormat(int format) {
			mIRFormat = format;
		}
	
		public  int getColorImageFormat() {
			return mColorFormat;
		}
		public  void setColorImageFormat(int format) {
			mColorFormat = format;
		}
		
		public  Size getColorSize() {
			return mColorSize;
		}
		public  void setColorSize(Size resolution) {
			mColorSize = resolution;
		} 
		
		public  boolean getColorRectificationMode() {
			return mIsColorRectificationModeEnabled;
		} 
		public  void setColorRectificationMode(boolean toggle){
			mIsColorRectificationModeEnabled = toggle;
		}
	
		public  boolean getUVMapEnabledMode() {
			return mIsUVMapEnabled;
		}
		public  void setUVMapEnabledMode(boolean mode) {
			mIsUVMapEnabled = mode;
		}     	

		public Surface getColorPreviewSurface()
		{
			return mColorPreviewSurface;
		}
		
		public void setColorPreviewSurface(Surface s)
		{
			mColorPreviewSurface = s;
		}

		public void setDepthEnabled(boolean mode) {
			mIsDepthEnabled = mode;
		}
		public void setIREnabled(boolean mode){
			mIsIREnabled = mode;
		}
		public void setColorEnabled(boolean mode){
			mIsColorEnabled = mode;
		}
		public boolean getDepthEnabled() { 
			return mIsDepthEnabled;
		}
		public boolean getIREnabled() { 
			return mIsIREnabled;
		}
		public boolean getColorEnabled() { 
			return mIsColorEnabled;
		}
		
		private Surface mColorPreviewSurface;
		private int mMaxFrames;
		
		private Size mDepthIRSize;
		private int mDepthFormat;
		
		private int mIRFormat;
		
		private Size mColorSize;
		private int mColorFormat;
		
		private boolean mIsColorEnabled;
		private boolean mIsDepthEnabled;
		private boolean mIsIREnabled;
		private boolean mIsColorRectificationModeEnabled;
		private boolean mIsUVMapEnabled;
	}
	
	/////////////////////////////////////////////////////////////
	/// 		Public Static Functions 					  ///
	/////////////////////////////////////////////////////////////
	
	// creates reader based on depth output settings, configure outputs and returns the reader
    public static DepthFrameReader configureOutputs(CameraDevice device, 
    		DepthOutputSettings outputSettings, CameraCharacteristics cameraChar ) throws CameraAccessException
	{
    	if (device == null)
    		throw new IllegalArgumentException(
	                "CameraDevice argument cannot be null");
    	if ( outputSettings == null)
    		throw new IllegalArgumentException(
	                "DepthOutputSettings argument cannot be null");
    	if ( cameraChar == null )
    		throw new IllegalArgumentException(
                "CameraCharacteristics argument cannot be null");
    	
    	//Frame reader will validate the configurations and throw an exception in case of a mismatch
    	DepthFrameReader newReader = DepthFrameReader.newInstance(outputSettings, cameraChar);
    	device.configureOutputs(newReader.getSurfaces());
		return newReader;
	}
             
    
	//add target output surfaces
    public  static void addTargets(CaptureRequest.Builder req, DepthFrameReader reader) 
    {
    	if ( req == null )
    		throw new IllegalArgumentException(
	                "CaptureRequest.Builder argument cannot be null");
    	if ( reader == null )
    		throw new IllegalArgumentException(
	                "DepthFrameReader argument cannot be null");

    	List<Surface> targets = reader.getSurfaces();
    	for ( int i=0; i<targets.size(); i++)
    		req.addTarget(targets.get(i));
    	return;
	}

	public static DepthOutputSettings createDepthOutputSettings() {
		return DepthOutputSettings.newInstance();
	}
}
