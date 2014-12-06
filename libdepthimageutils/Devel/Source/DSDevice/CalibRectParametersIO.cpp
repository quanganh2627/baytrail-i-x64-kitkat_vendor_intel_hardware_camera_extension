/*******************************************************************************
 * INTEL CORPORATION PROPRIETARY INFORMATION
 *
 * This software is supplied under the terms of a license agreement or nondisclosure
 * agreement with Intel Corporation and may not be copied or disclosed except in
 * accordance with the terms of that agreement
 * Copyright(c) 2014 Intel Corporation. All Rights Reserved.
 ********************************************************************************/

#include <string>
#include <stdio.h>
#include <assert.h>
#include "CalibRectParametersIO.h"

#define MaxStrLen 512

#define MAX_CALIB_REC_PARAMS_SIZE 2048
//#define TEST

using namespace std;

static void ntoh (float * x, float netX)
{
	char * source = (char *) &netX;
	char * dest = (char *) x;

	dest[0] = source[3];
	dest[1] = source[2];
	dest[2] = source[1];
	dest[3] = source[0];
}

static void ntoh (double * x, double netX)
{
	char * source = (char *) &netX;
	char * dest = (char *) x;

	dest[0] = source[7];
	dest[1] = source[6];
	dest[2] = source[5];
	dest[3] = source[4];
	dest[4] = source[3];
	dest[5] = source[2];
	dest[6] = source[1];
	dest[7] = source[0];
}


static void ntoh (uint16_t * x, uint16_t netX)
{
	*x = ((netX << 8) & 0xff00);
	*x |= ((netX >> 8) & 0xff);
}

static void ntoh (uint32_t * x, uint32_t netX)
{
	*x = (netX<<24) & 0xFF000000;
	*x |= (netX<<8) & 0x00FF0000;
	*x |= (netX>>8) & 0x0000FF00;
	*x |= (netX>>24) & 0x000000FF;
}


template <class T> static bool readFromBin (const unsigned char * & p, T &x)
{
    assert (p);
    ntoh (&x, *((T *) p));
	p += sizeof(T);
    //printf ("Read %s of %i bytes. Now at file byte %4i\n", typeid(x).name(), (int) sizeof (T), (int) ftell (fp));
    return true;
}

template <class T> static bool readFromBin (const unsigned char * & p, T *px, int n)
{
    assert (p && px);
    for (int i=0; i<n; i++)
    {
        if (! readFromBin (p, px[i]))
        {
            return false;
        }
    }
    return true;
}

template <class T> static bool readFromBin (const unsigned char * & p, T *px, int m, int n)
{
    assert (p && px);
    for (int i=0; i<m; i++)
    {
        for (int j=0; j<n; j++)
        {
            if (! readFromBin (p, px[j+n*i]))
            {
                return false;
            }
        }
    }
    return true;
}

template <class T> static bool readFromBin (const unsigned char * & p, T *px, int m, int n, int o)
{
    assert (p && px);
    for (int i=0; i<m; i++)
    {
        for (int j=0; j<n; j++)
        {
            for (int k=0; k<o; k++)
            {
                if (! readFromBin (p, px[k+o*(j+n*i)]))
                {
                    return false;
                }
            }
        }
    }
    return true;
}

static bool readFromBin (const unsigned char * & p,DSCalibIntrinsicsNonRectified &cri)
{
    assert (p);
    return true 
        && readFromBin (p, cri.fx)
        && readFromBin (p, cri.fy)
        && readFromBin (p, cri.px)
        && readFromBin (p, cri.py)
        && readFromBin (p, cri.k, 5)
        && readFromBin (p, cri.w)
        && readFromBin (p, cri.h);
}

static bool readFromBin (const unsigned char * & p, DSCalibIntrinsicsRectified &crm)
{
    assert (p);
    return true 
        && readFromBin (p, crm.rfx)
        && readFromBin (p, crm.rfy)
        && readFromBin (p, crm.rpx)
        && readFromBin (p, crm.rpy)
        && readFromBin (p, crm.rw)
        && readFromBin (p, crm.rh);
}

bool loadCalibRectParametersMem(DSCalibRectParameters & cal, const unsigned char * buffer)
{
	const int 
        mNIR = DS_MAX_NUM_INTRINSICS_RIGHT,
        mNIT = DS_MAX_NUM_INTRINSICS_THIRD,
        mNMLR = DS_MAX_NUM_RECTIFIED_MODES_LR,
        mNMT = DS_MAX_NUM_RECTIFIED_MODES_THIRD;

	const unsigned char * p = buffer;
    bool ok = true
        && readFromBin (p, cal.versionNumber)
        && readFromBin (p, cal.numIntrinsicsRight)
        && readFromBin (p, cal.numIntrinsicsThird)
        && readFromBin (p, cal.numRectifiedModesLR)
        && readFromBin (p, cal.numRectifiedModesThird)
        && readFromBin (p, cal.intrinsicsLeft)
        && readFromBin (p, cal.intrinsicsRight, mNIR)
        && readFromBin (p, cal.intrinsicsThird, mNIT)
        && readFromBin (p, &(cal.modesLR[0][0]), mNIR, mNMLR)
        && readFromBin (p, &(cal.modesThird[0][0][0]), mNIR, mNIT, mNMT)
        && readFromBin (p, &(cal.Rleft[0][0]),  mNIR, 9)
        && readFromBin (p, &(cal.Rright[0][0]), mNIR, 9)
        && readFromBin (p, &(cal.Rthird[0][0]),  mNIR, 9)
        && readFromBin (p, cal.B,  mNIR)
        && readFromBin (p, &(cal.T[0][0]),  mNIR, 3)
        && readFromBin (p, cal.Rworld,  9)
        && readFromBin (p, cal.Tworld,  3);

	return ok;
}

bool loadCalibRectParametersBin (DSCalibRectParameters &cal, const char *fileName)
{
    assert (fileName);

    memset (&cal, 0, sizeof (cal));

    FILE *fp = fopen (fileName, "rb");
    if (! fp)
    {
        printf ("saveCalibRectParametersBin(): Can't open '%s'\n", fileName);
        return false;
    }

	unsigned char buffer[MAX_CALIB_REC_PARAMS_SIZE] = {0};
	if (fread (buffer, 1, MAX_CALIB_REC_PARAMS_SIZE, fp) == 0)
	{
        printf ("saveCalibRectParametersBin(): No data read from '%s'\n", fileName);
        return false;
	}

    if (fclose (fp) < 0)
    {
        printf ("loadCalibRectParametersBin(): Can't close '%s'\n", fileName);
        return false;
    }

	return loadCalibRectParametersMem(cal, buffer);
}

static void hton (float    *netX, float    x) { ntoh(netX, x);}
static void hton (double   *netX, double   x) { ntoh(netX, x); }
static void hton (uint16_t *netX, uint16_t x) { ntoh(netX, x); }
static void hton (uint32_t *netX, uint32_t x) { ntoh(netX, x); }
#if 0

template <class T> static bool dumpAsBin (unsigned char * & p, T x)
{
    assert (p);
    // printf ("saving at file %4i %s of size %4i\n", (int) ftell (fp), typeid(x).name(), (int) sizeof (T));
    hton ((T *) p, x);
	p += sizeof(T);
    return true;
}

template <class T> static bool dumpAsBin (unsigned char * & p, const T *px, int n)
{
    assert (p && px);
    for (int i=0; i<n; i++)
    {
        if (! dumpAsBin (p, px[i]))
        {
            return false;
        }
    }
    return true;
}

template <class T> static bool dumpAsBin (unsigned char * & p, const T *px, int m, int n)
{
    assert (p && px);
    for (int i=0; i<m; i++)
    {
        for (int j=0; j<n; j++)
        {
            if (! dumpAsBin (p, px[j+n*i]))
            {
                return false;
            }
        }
    }
    return true;
}

template <class T> static bool dumpAsBin (unsigned char * & p, const T *px, int m, int n, int o)
{
    assert (p && px);
    for (int i=0; i<m; i++)
    {
        for (int j=0; j<n; j++)
        {
            for (int k=0; k<o; k++)
            {
                if (! dumpAsBin (p, px[k+o*(j+n*i)]))
                {
                    return false;
                }
            }
        }
    }
    return true;
}

static bool dumpAsBin (unsigned char * & p, const DSCalibIntrinsicsNonRectified &cri)
{
    assert (p);
    //printf ("saving at file %4i CalibRectIntrinsics of size %4i\n", (int) ftell (fp), (int) sizeof (cri));
    return true 
        && dumpAsBin (p, cri.fx)
        && dumpAsBin (p, cri.fy)
        && dumpAsBin (p, cri.px)
        && dumpAsBin (p, cri.py)
        && dumpAsBin (p, cri.k, 5)
        && dumpAsBin (p, cri.w)
        && dumpAsBin (p, cri.h);
}

static bool dumpAsBin (unsigned char * & p, const CalibRectResolutionMode &crm)
{
    assert (p);
    //printf ("saving at file %4i CalibRectResolutionMode of size %4i\n", (int) ftell (fp), (int) sizeof (crm));
    return true 
        && dumpAsBin (p, crm.rfx)
        && dumpAsBin (p, crm.rfy)
        && dumpAsBin (p, crm.rpx)
        && dumpAsBin (p, crm.rpy)
        && dumpAsBin (p, crm.rw)
        && dumpAsBin (p, crm.rh);
}

bool saveCalibRectParametersMem (const DSCalibIntrinsicsNonRectified & cal, unsigned char * buffer, int & size)
{
    const int 
        mNIR = maxNumIntrinsicsRight,
        mNIT = maxNumIntrinsicsThird,
        mNMLR = maxNumResolutionModesLR,
        mNMT = maxNumResolutionModesThird;

	unsigned char * p = buffer;
    bool ok = true
        && dumpAsBin (p, cal.versionNumber)
        && dumpAsBin (p, cal.numIntrinsicsRight)
        && dumpAsBin (p, cal.numIntrinsicsThird)
        && dumpAsBin (p, cal.numResolutionModesLR)
        && dumpAsBin (p, cal.numResolutionModesThird)
        && dumpAsBin (p, cal.intrinsicsLeft)
        && dumpAsBin (p, cal.intrinsicsRight, mNIR)
        && dumpAsBin (p, cal.intrinsicsThird, mNIT)
        && dumpAsBin (p, &(cal.modesLR[0][0]), mNIR, mNMLR)
        && dumpAsBin (p, &(cal.modesThird[0][0][0]), mNIR, mNIT, mNMT)
        && dumpAsBin (p, &(cal.Rleft[0][0]),  mNIR, 9)
        && dumpAsBin (p, &(cal.Rright[0][0]), mNIR, 9)
        && dumpAsBin (p, &(cal.Rthird[0][0]),  mNIR, 9)
        && dumpAsBin (p, cal.B,  mNIR)
        && dumpAsBin (p, &(cal.T[0][0]),  mNIR, 3)
        && dumpAsBin (p, cal.Rworld,  9)
        && dumpAsBin (p, cal.Tworld,  3);

	size = (int) (p - buffer);
	return ok;
}

bool saveCalibRectParametersBin (const DSCalibIntrinsicsNonRectified &cal, const char *fileName)
{
    assert (fileName);

    FILE *fp = fopen (fileName, "wb");
    if (! fp)
    {
        printf ("saveCalibRectParametersBin(): Can't open '%s'\n", fileName);
        return false;
    }
    
/*
# define SHOWMEMPOS(a) { printf ("Memory pos of %20s = %4i\n", #a, (int) ((char*)(&a)-(char*)&cal)); }
    SHOWMEMPOS (cal.intrinsicsLeft);
    SHOWMEMPOS (cal.intrinsicsRight);
    SHOWMEMPOS (cal.intrinsicsThird);
    SHOWMEMPOS (cal.modesLR);
    SHOWMEMPOS (cal.modesThird);
    SHOWMEMPOS (cal.Rleft);
    SHOWMEMPOS (cal.Rright);
    SHOWMEMPOS (cal.Rthird);
    SHOWMEMPOS (cal.B);
    SHOWMEMPOS (cal.T);
    SHOWMEMPOS (cal.Rworld);
    SHOWMEMPOS (cal.Tworld);
*/

	unsigned char buffer[MAX_CALIB_REC_PARAMS_SIZE] = {0};
	unsigned int size = 0;
	bool ok = saveCalibRectParametersMem(cal, buffer, (int &) size);

    if (! ok)
    {
        printf ("saveCalibRectParametersBin(): Couldn't save all fields\n");
    }

	if (fwrite(buffer, 1, size, fp) != size)
	{
        printf ("saveCalibRectParametersBin(): Writing to '%s' failed\n", fileName);
        return false;
	}

    if (fclose (fp) < 0)
    {
        printf ("saveCalibRectParametersBin(): Can't close '%s'\n", fileName);
        return false;
    }

#ifdef TEST
    DSCalibIntrinsicsNonRectified calReloaded;
    if (! loadCalibRectParametersBin (calReloaded, fileName))
    {
        printf ("saveCalibRectParametersBin(): Can't re-load saved file '%s'\n", fileName);

    } else { 
        if (memcmp (&cal, &calReloaded, sizeof (cal)))
        {
            printf ("saveCalibRectParametersBin(): saved-then-re-loaded struct differs from original\n");

        } else {
                
            printf ("saveCalibRectParametersBin(): saved-then-re-loaded struct is the same as original. OK\n");
        }
    }
#endif
	
	return ok;
}

static bool dumpAsTCSV (FILE *fp, float x, const string &name)
{
    assert (fp);
    return fprintf (fp, "%-25s, % -18.10g\n", name.c_str(), x) > 0;
}

static bool dumpAsTCSV (FILE *fp, double x, const string &name)
{
    assert (fp);
    return fprintf (fp, "%-25s, % -18.10g\n", name.c_str(), x) > 0;
}

static bool dumpAsTCSV (FILE *fp, int x, const string &name)
{
    assert (fp);
    return fprintf (fp, "%-25s, % -18i\n", name.c_str(), x) > 0;
}

static bool dumpAsCSV (FILE *fp, float x)
{
    assert (fp);
    return fprintf (fp, ", % -18.10g", x) > 0;
}

static bool dumpAsCSV (FILE *fp, double x)
{
    assert (fp);
    return fprintf (fp, ", % -18.10g", x) > 0;
}

//static bool dumpAsCSV (FILE *fp, int x)
//{
//    assert (fp);
//    return fprintf (fp, ", % -18i", x) > 0;
//}

static bool dumpAsCSV (FILE *fp, unsigned int x)
{
    assert (fp);
    return fprintf (fp, ", % -18i", x) > 0;
}

static bool dumpAsTCSV (FILE *fp, const DSCalibIntrinsicsNonRectified &cri, const string &name);
static bool dumpManyAsTCSV (FILE *fp, const DSCalibIntrinsicsNonRectified *cri, int m, int dummyStep, const string &name);

static bool dumpAsTCSV (FILE *fp, const DSCalibIntrinsicsRectified &crm, const string &name);
static bool dumpManyAsTCSV (FILE *fp, const DSCalibIntrinsicsRectified *crm, int m, int dummyStep, const string &name);

template <class T> static bool dumpManyAsTCSV (FILE *fp, const T *x, int m, int stepBytes, const string &name)
{
    assert (fp);
    if (fprintf (fp, "%-25s", name.c_str()) <= 0)
    {
        return false;
    }
    for (int i=0; i<m; i++)
    {
        if (! dumpAsCSV (fp, *((T*)(((const char*)x)+i*stepBytes))))
        {
            return false;
        }
    }
    if (fprintf (fp, "\n") <= 0)
    {
        return false;
    }
    return true;
}

template <class T> static bool dumpAsTCSV (FILE *fp, const T *x, int m, const string &name, bool compact)
{
    if (compact)
    {
        return dumpManyAsTCSV (fp, x, m, sizeof(T), name);
    }
    assert (fp);
    for (int i=0; i<m; i++)
    {
        char name_i[MaxStrLen];
        sprintf (name_i, "%s_%i", name.c_str(), i);
        if (! dumpAsTCSV (fp, x[i], name_i))
        {
            return false;
        }
    }
    return true;
}

static bool dumpAsTCSV (FILE *fp, const DSCalibIntrinsicsNonRectified &cri, const string &name)
{
    assert (fp);
    return true 
        && dumpAsTCSV (fp, cri.fx, name+"_fx")
        && dumpAsTCSV (fp, cri.fy, name+"_fy")
        && dumpAsTCSV (fp, cri.px, name+"_px")
        && dumpAsTCSV (fp, cri.py, name+"_py")
        && dumpAsTCSV (fp, &(cri.k[0]), 5, name+"_k", false)
        && dumpAsTCSV (fp, (int)cri.w, name+"_w")
        && dumpAsTCSV (fp, (int)cri.h, name+"_h");
}

static bool dumpManyAsTCSV (FILE *fp, const DSCalibIntrinsicsNonRectified *cri, int m, int /*dummyStep*/, const string &name)
{
    assert (fp);
    return true 
        && dumpManyAsTCSV (fp, &cri->fx, m, sizeof(*cri), name+"_fx")
        && dumpManyAsTCSV (fp, &cri->fy, m, sizeof(*cri), name+"_fy")
        && dumpManyAsTCSV (fp, &cri->px, m, sizeof(*cri), name+"_px")
        && dumpManyAsTCSV (fp, &cri->py, m, sizeof(*cri), name+"_py")
        && dumpManyAsTCSV (fp, cri->k+0, m, sizeof(*cri), name+"_k0")
        && dumpManyAsTCSV (fp, cri->k+1, m, sizeof(*cri), name+"_k1")
        && dumpManyAsTCSV (fp, cri->k+2, m, sizeof(*cri), name+"_k2")
        && dumpManyAsTCSV (fp, cri->k+3, m, sizeof(*cri), name+"_k3")
        && dumpManyAsTCSV (fp, cri->k+4, m, sizeof(*cri), name+"_k4")
        && dumpManyAsTCSV (fp, &cri->w, m,  sizeof(*cri), name+"_w")
        && dumpManyAsTCSV (fp, &cri->h, m,  sizeof(*cri), name+"_h");
}

static bool dumpAsTCSV (FILE *fp, const DSCalibIntrinsicsRectified &crm, const string &name)
{
    assert (fp);
    return true 
        && dumpAsTCSV (fp, crm.rfx, name+"_rfx")
        && dumpAsTCSV (fp, crm.rfy, name+"_rfy")
        && dumpAsTCSV (fp, crm.rpx, name+"_rpx")
        && dumpAsTCSV (fp, crm.rpy, name+"_rpy")
        && dumpAsTCSV (fp, (int)crm.rw, name+"_rw")
        && dumpAsTCSV (fp, (int)crm.rh, name+"_rh");
}

static bool dumpManyAsTCSV (FILE *fp, const DSCalibIntrinsicsRectified *crm, int m, int /*dummyStep*/, const string &name)
{
    assert (fp);
    return true 
        && dumpManyAsTCSV (fp, &crm->rfx, m, sizeof(*crm), name+"_rfx")
        && dumpManyAsTCSV (fp, &crm->rfy, m, sizeof(*crm), name+"_rfy")
        && dumpManyAsTCSV (fp, &crm->rpx, m, sizeof(*crm), name+"_rpx")
        && dumpManyAsTCSV (fp, &crm->rpy, m, sizeof(*crm), name+"_rpy")
        && dumpManyAsTCSV (fp, &crm->rw, m,  sizeof(*crm), name+"_rw")
        && dumpManyAsTCSV (fp, &crm->rh, m,  sizeof(*crm), name+"_rh");
}

template <class T> static bool dumpAsTCSV (FILE *fp, const T *x, int m, int n, const string &name, bool compact)
{
    assert (fp);
    for (int j=0; j<m; j++)
    {
        char name_j[MaxStrLen];
        sprintf (name_j, "%s_%i", name.c_str(), j);
        if (! dumpAsTCSV (fp, x+j*n, n, name_j, compact))
        {
            return false;
        }
    }
    return true;
}

template <class T> static bool dumpAsTCSV (FILE *fp, const T *x, int m, int n, int p, const string &name, bool compact)
{
    assert (fp);
    for (int k=0; k<m; k++)
    {
        char name_k[MaxStrLen];
        sprintf (name_k, "%s_%i", name.c_str(), k);
        if (! dumpAsTCSV (fp, x+k*n*p, n, p, name_k, compact))
        {
            return false;
        }
    }
    return true;
}

bool saveCalibRectParametersTCSV (const DSCalibRectParameters &cal, const char *fileName, bool compact)
{
    assert (fileName);
    FILE *fp = fopen (fileName, "wb");
    if (! fp)
    {
        printf ("saveCalibRectParametersTCSV(): Can't open '%s'\n", fileName);
        return false;
    }
    const int 
        mNIR = DS_MAX_NUM_INTRINSICS_RIGHT,
        mNIT = DS_MAX_NUM_INTRINSICS_THIRD,
        mNMLR = DS_MAX_NUM_RECTIFIED_MODES_LR,
        mNMT = DS_MAX_NUM_RECTIFIED_MODES_THIRD;

    bool ok = true
        && dumpAsTCSV (fp, (int)cal.versionNumber, "versionNumber")
        && dumpAsTCSV (fp, cal.numIntrinsicsRight, "numIntrinsicsRight")
        && dumpAsTCSV (fp, cal.numIntrinsicsThird, "numIntrinsicsThird")
        && dumpAsTCSV (fp, cal.numRectifiedModesLR, "numResolutionModesLR")
        && dumpAsTCSV (fp, cal.numRectifiedModesThird, "numResolutionModesThird")
        && dumpAsTCSV (fp, cal.intrinsicsLeft, "intrinsicsLeft")
        && dumpAsTCSV (fp, cal.intrinsicsRight, mNIR, "intrinsicsRight", compact)
        && dumpAsTCSV (fp, cal.intrinsicsThird, mNIT, "intrinsicsThird", compact)
        && dumpAsTCSV (fp, &(cal.modesLR[0][0]), mNIR, mNMLR, "modesLR", compact)
        && dumpAsTCSV (fp, &(cal.modesThird[0][0][0]), mNIR, mNIT, mNMT, "modesThird", compact)
        && dumpAsTCSV (fp, &(cal.Rleft[0][0]),  mNIR, 9, "Rleft", compact)
        && dumpAsTCSV (fp, &(cal.Rright[0][0]), mNIR, 9, "Rright", compact)
        && dumpAsTCSV (fp, &(cal.Rthird[0][0]),  mNIR, 9, "Rthird", compact)
        && dumpAsTCSV (fp, cal.B,  mNIR, "B", false)
        && dumpAsTCSV (fp, &(cal.T[0][0]),  mNIR, 3, "T", compact)
        && dumpAsTCSV (fp, cal.Rworld,  9, "Rworld", compact)
        && dumpAsTCSV (fp, cal.Tworld,  3, "Tworld", compact);
    
    if (fclose (fp) < 0)
    {
        printf ("saveCalibRectParametersTCSV(): Can't close '%s'\n", fileName);
        return false;
    }
    return ok;
}
#endif