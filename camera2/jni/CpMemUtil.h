/*
 * Copyright (C) 2015 Intel Corporation
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

#ifndef CPMEMUTIL_H
#define CPMEMUTIL_H


#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>

#include <utils/RefBase.h>
#include <binder/IMemory.h>
#include <binder/MemoryDealer.h>


/*for HDR the supported
  * max input frame is 9, so define max frame
  * number to 9*2+2 = 20
*/
#define MAX_FRAME_NUM 20

using namespace android;

class CpMemory:public RefBase{
public:
    sp<MemoryDealer> dealer;
    sp<IMemory> memory;
    int size;
};

/*
  * init memory pool
*/
void initMemPool ();

/*
 * allocate memory with memory dealer from
 * memory heap base
*/
sp<IMemory> allocateMemory(int size);

/*
 * release memory, find the right memory in memory pool
 * then release it
*/
void deallocateMemory(sp<IMemory> mem);

/*
 * clear memory pool
*/
void unInitMemPool();

#endif // CPMEMUTIL_H

