/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (c) 2015 Intel Corporation
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
#ifndef _ADD_TYPES_H_
#define _ADD_TYPES_H_

#include <stdint.h>

typedef struct {
    uint32_t acc_ctrl; /* Mandatory field for CSS runtime */
    uint32_t in_ptr_a;
    uint32_t in_ptr_b;
    uint32_t out_ptr;
    uint32_t num_values;
} add_params;

#endif /* _ADD_TYPES_H_ */
