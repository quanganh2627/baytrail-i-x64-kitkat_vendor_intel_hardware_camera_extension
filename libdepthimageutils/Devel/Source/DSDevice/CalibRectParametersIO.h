/*******************************************************************************
 * INTEL CORPORATION PROPRIETARY INFORMATION
 *
 * This software is supplied under the terms of a license agreement or nondisclosure
 * agreement with Intel Corporation and may not be copied or disclosed except in
 * accordance with the terms of that agreement
 * Copyright(c) 2014 Intel Corporation. All Rights Reserved.
 ********************************************************************************/

# pragma once

# include "DSCalibRectParameters.h"

/* 
   Functions to load or save a CalibRectParameters to a binary or transposed CSV file.
*/
bool loadCalibRectParametersMem  (DSCalibRectParameters & cal, const unsigned char * buffer);
bool loadCalibRectParametersBin  (DSCalibRectParameters &cal, const char *fileName);

#if 0
bool saveCalibRectParametersMem  (const DSCalibRectParameters & cal, unsigned char * buffer, int & size);
bool saveCalibRectParametersBin  (const DSCalibRectParameters &cal, const char *fileName);

bool saveCalibRectParametersTCSV (const DSCalibRectParameters &cal, const char *fileName, bool compact);
#endif
