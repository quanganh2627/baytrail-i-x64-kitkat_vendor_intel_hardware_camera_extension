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
#include "pvl_face_recognition_with_db.h"
#include "PvlUtil.h"
#include <stdlib.h>

pvl_err pvl_face_recognition_with_db_get_default_config(pvl_config *config)
{
    return pvl_success;
}

pvl_err pvl_face_recognition_with_db_create(const pvl_config *config, const char *db_path, pvl_face_recognition_with_db **fr)
{
    return pvl_success;
}

void pvl_face_recognition_with_db_destroy(pvl_face_recognition_with_db *fr)
{
}

pvl_err pvl_face_recognition_with_db_reset(pvl_face_recognition_with_db *fr)
{
    return pvl_success;
}

pvl_err pvl_face_recognition_with_db_set_parameters(pvl_face_recognition_with_db *fr, const pvl_face_recognition_with_db_parameters *params)
{
    return pvl_success;
}

pvl_err pvl_face_recognition_with_db_get_parameters(pvl_face_recognition_with_db *fr, pvl_face_recognition_with_db_parameters *params)
{
    return pvl_success;
}

pvl_err pvl_face_recognition_with_db_run_in_image(pvl_face_recognition_with_db *fr, const pvl_image *image, int32_t num_faces,
                const pvl_point *left_eyes, const pvl_point *right_eyes,
                        pvl_face_recognition_with_db_result *results)
{
    return pvl_success;
}

pvl_err pvl_face_recognition_with_db_run_in_preview(pvl_face_recognition_with_db *fr, const pvl_image *image, int32_t num_faces,
                const pvl_point *left_eyes, const pvl_point *right_eyes, const int32_t *tracking_ids,
                        pvl_face_recognition_with_db_result *results)
{
    return pvl_success;
}

pvl_err pvl_face_recognition_with_db_register_facedata(pvl_face_recognition_with_db *fr, const pvl_face_recognition_facedata *facedata)
{
    return pvl_success;
}

pvl_err pvl_face_recognition_with_db_unregister_facedata(pvl_face_recognition_with_db *fr, uint64_t face_id)
{
    return pvl_success;
}

pvl_err pvl_face_recognition_with_db_unregister_person(pvl_face_recognition_with_db *fr, int32_t person_id)
{
    return pvl_success;
}

pvl_err pvl_face_recognition_with_db_update_person(pvl_face_recognition_with_db *fr, uint64_t face_id, int32_t new_person_id)
{
    return pvl_success;
}

int32_t pvl_face_recognition_with_db_get_num_faces_in_database(pvl_face_recognition_with_db *fr)
{
    return 0;
}

pvl_err pvl_face_recognition_with_db_create_result_buffer(pvl_face_recognition_with_db *fr, int32_t max_faces, pvl_face_recognition_with_db_result **fr_results)
{
    *fr_results = (pvl_face_recognition_with_db_result*)malloc(sizeof(pvl_face_recognition_with_db_result) * max_faces);
    for (int i = 0; i < max_faces; i++) {
        fr_results[i]->facedata.data = (uint8_t*)malloc(fr->facedata_size);
    }
    return pvl_success;
}

void pvl_face_recognition_with_db_destroy_result_buffer(pvl_face_recognition_with_db_result *fr_results)
{
    int length = (int)(sizeof(fr_results) / sizeof(fr_results[0]));
    LOGE("[%s:%d] length: %d", __FUNCTION__, __LINE__, length);
    for (int i = 0; i < length; i++) {
        free(fr_results[i].facedata.data);
    }
    free(fr_results);
}

int32_t pvl_face_recognition_with_db_get_new_person_id(const pvl_face_recognition_with_db* fr)
{
    return 0;
}


