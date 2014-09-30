/*
 * Copyright (c) 2015 Intel Corporation.
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
#include <utils/Vector.h>
#include "CpMemUtil.h"
#include "JNIUtil.h"

using namespace android;

Vector <sp<CpMemory> > mCpMem;

void initMemPool ()
{
    mCpMem.clear();
    mCpMem.setCapacity(MAX_FRAME_NUM);
}

sp<IMemory> allocateMemory(int size)
{
    sp<MemoryDealer> dealer;
    sp<IMemory> memory;
    dealer = new MemoryDealer(size, "Acc");
    memory = dealer->allocate(size);
    sp<CpMemory> mem = new CpMemory();
    mem->dealer =  dealer;
    mem->memory = memory;
    mem->size = size;
    if (mCpMem.size() == MAX_FRAME_NUM)
        LOGE("the pool is full");
    else
        mCpMem.push_back(mem);
    return memory;
}

void deallocateMemory(sp<IMemory> mem)
{
    for (int i = 0; i < MAX_FRAME_NUM; i++) {
         if (mCpMem[i]->memory == mem) {
             sp<CpMemory> mem = mCpMem[i];
             mem.clear();
             mCpMem.removeAt(i);
             break;
         }
    }
}

void unInitMemPool()
{
    mCpMem.clear();
}

