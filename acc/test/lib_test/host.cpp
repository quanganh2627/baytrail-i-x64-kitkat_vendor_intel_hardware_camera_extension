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
#include <stdint.h>
#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include "add_types.h"
#include "acc.h"

#define NUM_VALUES 256
#define DMEM0_INDEX 0x01
#define FW_PATH "/sdcard/isp/add.bin"

namespace android {

extern "C" {

static int
run_add(AccControl* in_acc_control, uint16_t *in_a, uint16_t *in_b, uint16_t *out, int num_values)
{
    void *fw;
    int result = 0;
    unsigned fw_size,
             acc_handle,
             plane_size = num_values*sizeof(uint16_t),
             prm_size = sizeof(add_params);
    acc_buf *in_buf_a,
               *in_buf_b,
               *out_buf,
               *prm_buf;
    add_params *params;

    printf ("At %s(%d)\n", __FUNCTION__, __LINE__);
    /* Initialize acceleration API */
    result = in_acc_control->acc_init();
    if (result < 0) {
        printf("%s: Failed to initialize ACC, quitting with err = %d\n", __func__,
                result);
        return result;
    }

    fw = in_acc_control->acc_open_fw(FW_PATH, &fw_size);
    if (fw == NULL) {
        printf("%s: Unable to open firmware: %s\n", __func__, FW_PATH);
        return -1;
    }

    result = in_acc_control->acc_load_fw(fw, fw_size, &acc_handle);
    if (result < 0) {
        printf("%s: Failed to load firmware, quitting with err = %d\n", __func__,
                result);
        return result;
    }

    params = (add_params*)in_acc_control->acc_alloc(prm_size);
    in_buf_a = in_acc_control->acc_buf_create(in_a, plane_size);
    in_buf_b = in_acc_control->acc_buf_create(in_b, plane_size);
    out_buf  = in_acc_control->acc_buf_create(out,  plane_size);
    prm_buf  = in_acc_control->acc_buf_create(params, prm_size);

    /* Initialize parameters */
    params->acc_ctrl   = 0;
    params->num_values = num_values;
    params->in_ptr_a   = in_buf_a->css_ptr;
    params->in_ptr_b   = in_buf_b->css_ptr;
    params->out_ptr    = out_buf->css_ptr;
    in_acc_control->acc_buf_sync_to_css(in_buf_a);
    in_acc_control->acc_buf_sync_to_css(in_buf_b);
    in_acc_control->acc_buf_sync_to_css(prm_buf);

    printf ("At %s(%d)\n", __FUNCTION__, __LINE__);
    in_acc_control->acc_set_mapped_arg(acc_handle, DMEM0_INDEX, prm_buf->css_ptr, prm_size);

    /* Run binary and wait for it to finish */
    printf ("At %s(%d)\n", __FUNCTION__, __LINE__);
    result = in_acc_control->acc_start_fw(acc_handle);
    printf ("At %s(%d) result %d\n", __FUNCTION__, __LINE__, result);
    if (result < 0) {
        printf("%s: Failed to start firmware, quitting with err = %d\n", __func__,
                result);
        in_acc_control->acc_unload_fw(acc_handle);
        return result;
    }

    printf ("At %s(%d)\n", __FUNCTION__, __LINE__);
    result = in_acc_control->acc_wait_fw(acc_handle);
    printf ("At %s(%d)\n", __FUNCTION__, __LINE__);
    if (result < 0) {
        printf("%s: Failed during wait for firmware, quitting with err = %d\n",
                __func__, result);
        in_acc_control->acc_unload_fw(acc_handle);
        return result;
    }

    printf ("At %s(%d)\n", __FUNCTION__, __LINE__);
    in_acc_control->acc_buf_sync_to_cpu(out_buf);
    in_acc_control->acc_buf_free(in_buf_a);
    in_acc_control->acc_buf_free(in_buf_b);
    in_acc_control->acc_buf_free(out_buf);
    in_acc_control->acc_buf_free(prm_buf);

    printf ("At %s(%d)\n", __FUNCTION__, __LINE__);
    in_acc_control->acc_free(params);

    /* Cleanup acceleration API */
    printf ("At %s(%d)\n", __FUNCTION__, __LINE__);
    in_acc_control->acc_unload_fw(acc_handle);
    free(fw);
    /* acc_close(); */
    return 0;
}

int main()
{
    int errors = 0, i;
    uint16_t *in_a,
             *in_b,
             *out;
    // Need aligned mallocs
    char accDevicePath[] = "/dev/video4";
    AccControl* acc_control = new AccControl(accDevicePath);
    in_a = (uint16_t*)acc_control->acc_alloc(sizeof(uint16_t)*NUM_VALUES);
    in_b = (uint16_t*)acc_control->acc_alloc(sizeof(uint16_t)*NUM_VALUES);
    out  = (uint16_t*)acc_control->acc_alloc(sizeof(uint16_t)*NUM_VALUES);
    printf ("At %s(%d)\n", __FUNCTION__, __LINE__);

    for (i=0; i<NUM_VALUES; i++) {
        in_a[i] = i;
        in_b[i] = i;
        out[i] = -1;
    }

    printf ("At %s(%d)\n", __FUNCTION__, __LINE__);
    errors = run_add(acc_control, in_a, in_b, out, NUM_VALUES);
    printf ("At %s(%d)\n", __FUNCTION__, __LINE__);
    if (errors != 0)
      return errors != 0;

    for (i=0; i<NUM_VALUES; i++) {
        if (out[i] != 2*i) {
            printf("error in output at index %d (expected %d, got %d)\n",
                i, 2*i, out[i]);
            errors++;
        }
    }

    printf ("At %s(%d)\n", __FUNCTION__, __LINE__);

    acc_control->acc_free(in_a);
    acc_control->acc_free(in_b);
    acc_control->acc_free(out);

    if (errors == 0)
        printf ("Add Test successful!\n");
    else
        printf ("Add Test failed with %d errors!\n", errors);

    if (acc_control) {
        delete acc_control;
    }

    return (errors != 0);
}

}

}
