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
package com.intel.camera2.extensions.photography;

import com.intel.camera2.extensions.IaFrame;
import com.intel.camera2.extensions.photography.CPLibraryLoader;

final class CPJNI extends CPLibraryLoader {
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
