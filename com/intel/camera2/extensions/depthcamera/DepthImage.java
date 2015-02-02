package com.intel.camera2.extensions.depthcamera;
import android.graphics.Point;
import android.graphics.PointF;
import android.media.Image;

public abstract class DepthImage extends Image {

	protected DepthImage() {
		/* Empty */
	}
	// Projection for color image doesn't make sense... it needs z value which we don't have for color image - only for depth image
	// the only reason to use it is in the uvmapping process - which we provide anyways.

	// From point cloud in world coordinates to depth image coordinates (row, column, depth (get from image))
	public abstract Point projectWorldToImageCoordinates(DepthCameraCalibrationDataMap.IntrinsicParams zIntrincs,DepthCameraCalibrationDataMap.Point3DF pos3d);

	// Row , column , depth (get from image) => point cloud in world coordinates (camera coordinates)
	public abstract DepthCameraCalibrationDataMap.Point3DF projectImageToWorldCoordinates(DepthCameraCalibrationDataMap.IntrinsicParams zIntrincs ,Point pos2d);

	// Whether the color is rectified or not can be retrieved from calibration data
	public abstract PointF mapDepthToColorCoordinates(DepthCameraCalibrationDataMap.DepthCameraCalibrationData calibrationData, Point depthCoordinates );

	// Map part of the depth image (region)- might be used for optimization
	public abstract PointF[][]  mapDepthToColorCoordinates(DepthCameraCalibrationDataMap.DepthCameraCalibrationData calibrationData, Point origin, int width, int height ); 

	// Get z value at coordinate
	public abstract int getZ(int x, int y);
}
