package com.intel.camera2.extensions.photography;

import com.intel.camera2.extensions.IaFrame;
import com.intel.camera2.extensions.photography.CPLibraryLoader;

public class CPJNI extends CPLibraryLoader {
    // CP blending process target.
    // Those definitions should be synced up with enum 'ia_cp_target' in 'ia_cp_types.h'
    public static final int TARGET_CPU = 0; //Intel Architecture (IA) host
    public static final int TARGET_IPU = 1; //Image Processing Unit
    public static final int TARGET_GPU = 2; //Graphics Processing Unit
    public static final int TARGET_ATE = 3; //ATE C bitexact reference model
    public static final int TARGET_REF = 4; //Generic C reference model

    public static final int RET_ERROR = -1;

    public native static long init();
    public native static void uninit(long instance);

    public native static int hdrInit(long instance, int width, int height, BlenderOption opt);
    public native static IaFrame hdrCompose(long instance, IaFrame[] inputs, HdrOption cfg);
    public native static int hdrAbort(long instance);
    public native static int hdrUninit(long instance);

    public native static int ullInit(long instance, int width, int height, BlenderOption opt);
    public native static IaFrame ullCompose(long instance, IaFrame[] inputs, UllOption cfg);
    public native static int ullAbort(long instance);
    public native static int ullUninit(long instance);

    // for testing
    public native static IaFrame debugFrameConvert(long instance, IaFrame in);
}
