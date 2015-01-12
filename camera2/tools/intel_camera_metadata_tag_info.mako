## -*- coding: utf-8 -*-
/*
 * Copyright (C) 2014 Intel Corporation
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

/**
 * !! Do not reference this file directly !!
 *
 * It is logically a part of camera_metadata.c.  It is broken out for ease of
 * maintaining the tag info.
 *
 * Array assignments are done using specified-index syntax to keep things in
 * sync with intel_camera_metadata_tags.h
 */

/**
 * ! Do not edit this file directly !
 *
 * Generated automatically from intel_camera_metadata_tag_info.mako
 */

const char *intel_camera_metadata_section_names[INTEL_CAMERA_SECTION_COUNT] = {
  % for i in find_all_sections(metadata):
    ${"[%s]" %(path_name(i)) | csym,pad(36)} = "${path_name(i)}",
  % endfor
};

% for sec in find_all_sections(metadata):
static tag_info_t ${path_name(sec) | csyml}_tags[${path_name(sec) | csym}_END -
        ${path_name(sec) | csym}_START] = {
  % for entry in remove_synthetic(find_unique_entries(sec)):
    [ ${entry.name | csym} - ${path_name(sec) | csym}_START ] =
    { ${'"%s",' %(entry.name_short) | pad(40)} ${entry.type | ctype_enum,ljust(11)} },
  % endfor
};

% endfor


% for sec in find_all_sections(metadata):
static tag_section_t section_${path_name(sec) | csyml} = {
    "${path_name(sec)}",
    (uint32_t) ${path_name(sec) | csym}_START,
    (uint32_t) ${path_name(sec) | csym}_END,
    ${path_name(sec) | csyml}_tags
};

% endfor

tag_section_t intel_tag_sections[INTEL_CAMERA_SECTION_COUNT] = {
  % for i in find_all_sections(metadata):
    section_${path_name(i) | csyml},
  % endfor
};

int intel_camera_metadata_enum_snprint(uint32_t tag,
                                 uint32_t value,
                                 char *dst,
                                 size_t size) {
    const char *msg = "error: not an enum";
    int ret = -1;

    switch(tag) {
    % for sec in find_all_sections(metadata):
      % for idx,entry in enumerate(remove_synthetic(find_unique_entries(sec))):
        case ${entry.name | csym}: {
          % if entry.enum:
            switch (value) {
              % for val in entry.enum.values:
                case ${entry.name | csym}_${val.name}:
                    msg = "${val.name}";
                    ret = 0;
                    break;
              % endfor
                default:
                    msg = "error: enum value out of range";
            }
          % endif
            break;
        }
      % endfor

    %endfor
    }

    strncpy(dst, msg, size - 1);
    dst[size - 1] = '\0';

    return ret;
}

<%
  find_values = lambda x: isinstance(x, metadata_model.EnumValue)
  enum_values = metadata.find_all(find_values)
  enum_value_max_len = max([len(value.name) for value in enum_values]) + 1
%>
#define CAMERA_METADATA_ENUM_STRING_MAX_SIZE ${enum_value_max_len}
