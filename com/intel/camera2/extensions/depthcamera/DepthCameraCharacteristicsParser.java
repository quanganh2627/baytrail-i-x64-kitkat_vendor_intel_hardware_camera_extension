/*******************************************************************************
 * INTEL CORPORATION PROPRIETARY INFORMATION
 *
 * This software is supplied under the terms of a license agreement or nondisclosure
 * agreement with Intel Corporation and may not be copied or disclosed except in
 * accordance with the terms of that agreement
 * Copyright(c) 2014 Intel Corporation. All Rights Reserved.
 ********************************************************************************/
package com.intel.camera2.extensions.depthcamera;

import java.util.ArrayList;
import java.util.List;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraCharacteristics.Key;
import android.hardware.camera2.CameraMetadata;
import android.util.Size;

/*
	Set of static helper functions to access depth camera charecteristics 
*/	
public class DepthCameraCharacteristicsParser
{
	
	//// Auxiliary streams Description strings
	public final static String AUX_STREAM_LEFT_DESC = "Left Camera Stream in a stereo system (Color or IR, " +
			"check supported formats for more info)";
	public final static String AUX_STREAM_RIGHT_DESC = "Right Camera Stream in a stereo System (Color or IR, " +
			"check supported formats for more info)";
	public final static String AUX_STREAM_LEFT_RIGHT_DESC = "Left and Right Camera Streams in a stereo System (Color or IR, " +
			"check supported formats for more info)";
	public final static String AUX_STREAM_CENTER_DESC = "Main Infra Red Emitter image";
	
	//// Depth Camera Module
	public final static int DEPTH_CAMERA_DS_MODULE_ID = 0;
	public final static int DEPTH_CAMERA_IVCAM_MODULE_ID = 1;
	
	public final static String DEPTH_CAMERA_DS_MODULE_NAME = "DS";
	public final static String DEPTH_CAMERA_IVCAM_MODULE_NAME = "IVCAM";

	//// Depth=>Color Mapping Methods
	public final static int DEPTHCOLOR_UVMAPPING_IMAGE = 0;
	public final static int DEPTHCOLOR_MAPPED_COLOR_IMAGE = 1;
	
    /**
    * returns node index array of the nodeId in the available nodes characteristics
    * if not available null is returned
    */
    private static ArrayList<Integer> getNodeIndex(CameraCharacteristics c,int nodeId)
    {
        try
        {
            int[] nodes = c.get(DepthCameraCharacteristics.DEPTHCOMMON_AVAILABLE_NODES);
            ArrayList<Integer> res = new ArrayList<Integer>();
            for ( int i=0; i< nodes.length; i++)
                if ( nodes[i] == nodeId )
                    res.add(i);
            return res.size() > 0 ? res : null;
        }
        catch ( IllegalArgumentException e)
        {
            return null;
        }
    }
    /**
    *  returns supported formats for the node id
    *  null if none are available
    */
    public static Size[] getSupportedNodeSizes(CameraCharacteristics c, int nodeId) {

        ArrayList<Integer> nodeIdx = getNodeIndex(c,nodeId);

        try
        {
            ArrayList<Size> res = new ArrayList<Size>();

            Size[] sizes = c.get(CameraCharacteristics.SCALER_AVAILABLE_PROCESSED_SIZES);
            byte[] mappings = c.get(DepthCameraCharacteristics.DEPTHCOMMON_SIZE_NODES_MAPPING);

            if ( sizes.length != mappings.length )
                throw new IllegalStateException(" availableSizes array size doesn't match the nodesMapping array size!!!");
            for ( int n = 0; n < nodeIdx.size(); n++ )
            {            
            	
                int nodeMask = 1 << nodeIdx.get(n).intValue();; //assume only one
                
                for ( int i=0; i<sizes.length;i++)
                {
                    if ( (mappings[i] & nodeMask) != 0)
                        res.add(sizes[i]);
                }
            }
            if ( res.size() != 0 )
                return res.toArray(new Size[res.size()]);
        }
        catch ( IllegalArgumentException e)
        {
            return null;
        }
        return null;
    }
    private static int[] convertArrayListToInt(ArrayList<Integer> in)
    {
    	if (in == null || in.size() == 0)
    		return null;
    	int[] res = new int[in.size()];
    	for ( int i=0; i<in.size();i++)
    	{
    		res[i] = in.get(i).intValue();
    	}
    	return res;
    }
    /**
    *  returns supported formats for the node id
    *  null if none are available
    */
    public static int[] getSupportedNodeFormats(CameraCharacteristics c, int nodeId)
    {
        ArrayList<Integer> nodeIdx = getNodeIndex(c,nodeId);
        try
        {
            ArrayList<Integer> res = new ArrayList<Integer>();

            int[] formats = c.get(CameraCharacteristics.SCALER_AVAILABLE_FORMATS);
            byte[] mappings = c.get(DepthCameraCharacteristics.DEPTHCOMMON_FORMAT_NODES_MAPPING);

            if ( formats.length != mappings.length )
                throw new IllegalStateException(" availableFormats array size doesn't match the nodesMapping array size!!!");
            for ( int n = 0; n < nodeIdx.size(); n++ )
            {            
            	 int nodeMask = 1 << nodeIdx.get(n).intValue();; //assume only one //assume only one
                
                for ( int i=0; i<formats.length;i++)
                {
                    if ( (mappings[i] & nodeMask ) != 0)
                        res.add(formats[i]);
                }
            }
            if ( res.size() != 0 )
                return convertArrayListToInt(res);
        }
        catch ( IllegalArgumentException e)
        {
            return null;
        }
        return null;
    }
	/** 
	 * returns whether the Camera supports depth stream or not
	 * @param c
	 * @return
	 */
	public static boolean isDepthCamera(CameraCharacteristics c) {
        if ( getNodeIndex(c,DepthCameraMetadata.DEPTHCOMMON_AVAILABLE_NODES_DEPTH) != null )
            return true;
		return false;
	}
	
	/**
	 * returns the name of the camera module 
	 * @param c
	 * @return
	 */
	public static String getDepthCameraModuleName(CameraCharacteristics c)
	{
        try
        {
            int mid = c.get(DepthCameraCharacteristics.DEPTHCOMMON_MODULE_ID);
            switch ( mid )
            {
                case DepthCameraMetadata.DEPTHCOMMON_MODULE_ID_IVCAM: 
                    return DEPTH_CAMERA_IVCAM_MODULE_NAME;
                case DepthCameraMetadata.DEPTHCOMMON_MODULE_ID_DS:
                    return DEPTH_CAMERA_DS_MODULE_NAME;
                default:
                    return null;
            }
        }
        catch ( IllegalArgumentException e)
        {
            return null;
        }

        
	}
	/**
	 * Returns supported Pixal formats for Depth Stream
	 * @param c
	 * @return
	 */
    public static int[] getSupportedDepthFormats(CameraCharacteristics c) {
        return  getSupportedNodeFormats(c, DepthCameraMetadata.DEPTHCOMMON_AVAILABLE_NODES_DEPTH);
	}
    
    /**
     * Returns supported depth units for the depth stream values
     * @param c
     * @return
     */
    public static byte[] getSupportedDepthUnits(CameraCharacteristics c)
    {
        try {
            return c.get(DepthCameraCharacteristics.DEPTHCOMMON_AVAILABLE_DEPTH_UNITS);
        }
        catch ( IllegalArgumentException e)
        {
            return null;
        }
    }
    /**
     * Returns list of supported auxiliary stream ids
     * @param c
     * @return
     */
    public static int[] getSupportedAuxiliaryStreams(CameraCharacteristics c)
    {
    	return null;
    }
   
    /**
     * Returns IDs of supported Depth To Color mapping modes
     * @param c
     * @return
     */
    public static int[] getSupportedDepthColorMapping(CameraCharacteristics c)
    {
    	return null;
    }

    /**
     * Returns a string describing the Auxiliary stream
     * @param auxId
     * @return
     */
    public static String getAuxDescription(int auxId)
    {
        switch ( auxId )
        {
           case DepthCameraMetadata.DEPTHCOMMON_AVAILABLE_NODES_LEFT_RIGHT:
            	return AUX_STREAM_LEFT_RIGHT_DESC;
            case DepthCameraMetadata.DEPTHCOMMON_AVAILABLE_NODES_CENTER:
                return AUX_STREAM_CENTER_DESC;
            default:
                return "Unknown";
        }
    }
    
    /**
     * Returns for a given auxiliary stream id (auxId), the supported formats
     * @param c
     * @param auxId
     * @return
     */
    public static int[] getSupportedAuxFormats(CameraCharacteristics c, int auxId) {
		return  getSupportedNodeFormats(c, auxId); //TODO auxId is ok?
	}
    
    /**
     * Returns supported color stream formats
     * @param c
     * @return
     */
    public static int[] getSupportedColorFormats(CameraCharacteristics c) {
		return  getSupportedNodeFormats(c, DepthCameraMetadata.DEPTHCOMMON_AVAILABLE_NODES_COLOR);
	}
    
    /**
     * Returns supported stream sizes for Depth and Auxiliary streams
     * @param c
     * @return
     */
    public static Size[] getSupportedDepthAuxSizes(CameraCharacteristics c) {
		return getSupportedNodeSizes(c, DepthCameraMetadata.DEPTHCOMMON_AVAILABLE_NODES_DEPTH);
	} 
    /**
     * Returns List of the supported Color Stream sizes
     * @param c
     * @return
     */
    public static Size[] getSupportedColorSizes(CameraCharacteristics c) {
		return getSupportedNodeSizes(c, DepthCameraMetadata.DEPTHCOMMON_AVAILABLE_NODES_COLOR);
	} 
    
    /**
     * Returns For a given resolution, the list of frame rates supported for the depth stream
     * 
     * @param c
     * @param resolution
     * @return
     */
    public static int[] getSupportedDepthFrameRates(CameraCharacteristics c, Size resolution) {
		return null;
	}
    
    /**
     * Returns For a given resolution, the list of frame rates supported for the color stream
     * @param c
     * @param resolution
     * @return
     */
    public static int[] getSupportedColorFrameRates(CameraCharacteristics c, Size resolution) {
		return null;
	}
    
    
	//Get calibration data for specific output setting mode
    /**
     * Returns Calibration Data for a given output settings (given resolution)
     * @param c
     * @param s
     * @return
     */
    
    //TODO change to return the struct
	public static byte[] getCalibrationData(CameraCharacteristics c, DepthCameraSetup.DepthOutputSettings s)
    {
		try {
            return c.get(DepthCameraCharacteristics.DEPTHCOMMON_CALIBRATION_DATA);
        }
        catch ( IllegalArgumentException e)
        {
            return null;
        }
    }
	
}
