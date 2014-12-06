/*******************************************************************************
 * INTEL CORPORATION PROPRIETARY INFORMATION
 *
 * This software is supplied under the terms of a license agreement or nondisclosure
 * agreement with Intel Corporation and may not be copied or disclosed except in
 * accordance with the terms of that agreement
 * Copyright(c) 2014 Intel Corporation. All Rights Reserved.
 ********************************************************************************/

#pragma once
#include <cstdint>

template <class T>
struct vec2
{
    T x, y;
    vec2() : x(), y() {}
    vec2(T x, T y) : x(x), y(y) {}

    bool operator==(const vec2& v) const { return x == v.x && y == v.y; }
    vec2 operator+(const vec2& v) const { return vec2(x + v.x, y + v.y); }
    vec2 operator-(const vec2& v) const { return vec2(x - v.x, y - v.y); }

    void writeTo(T& _x, T& _y) const // Helper function to assist with ds.getSomething(x, y); style functions
    {
        _x = x;
        _y = y;
    }
};
typedef vec2<int> int2;
typedef vec2<uint32_t> uint2;
typedef vec2<float> float2;

template<class T> inline void DSMul_3x3_3x1(T Y[3], const T A[9], const T X[3])
{
    Y[0] = A[0]*X[0] + A[1]*X[1] + A[2]*X[2];
    Y[1] = A[3]*X[0] + A[4]*X[1] + A[5]*X[2];
    Y[2] = A[6]*X[0] + A[7]*X[1] + A[8]*X[2];
}

template<class T> inline void DSMul_3x3t_3x1(T Y[3], const T A[9], const T X[3])
{
    Y[0] = A[0]*X[0] + A[3]*X[1] + A[6]*X[2];
    Y[1] = A[1]*X[0] + A[4]*X[1] + A[7]*X[2];
    Y[2] = A[2]*X[0] + A[5]*X[1] + A[8]*X[2];
}

template<class T> inline void DSMul_3x3_3x3(T Y[9], const T A[9], const T B[9])
{
    Y[0] = A[0]*B[0] + A[1]*B[3] + A[2]*B[6];
    Y[1] = A[0]*B[1] + A[1]*B[4] + A[2]*B[7];
    Y[2] = A[0]*B[2] + A[1]*B[5] + A[2]*B[8];

    Y[3] = A[3]*B[0] + A[4]*B[3] + A[5]*B[6];
    Y[4] = A[3]*B[1] + A[4]*B[4] + A[5]*B[7];
    Y[5] = A[3]*B[2] + A[4]*B[5] + A[5]*B[8];

    Y[6] = A[6]*B[0] + A[7]*B[3] + A[8]*B[6];
    Y[7] = A[6]*B[1] + A[7]*B[4] + A[8]*B[7];
    Y[8] = A[6]*B[2] + A[7]*B[5] + A[8]*B[8];
}

template<class T> inline void DSMul_3x3_3x3t(T Y[9], const T A[9], const T B[9])
{
    Y[0] = A[0]*B[0] + A[1]*B[1] + A[2]*B[2];
    Y[1] = A[0]*B[3] + A[1]*B[4] + A[2]*B[5];
    Y[2] = A[0]*B[6] + A[1]*B[7] + A[2]*B[8];

    Y[3] = A[3]*B[0] + A[4]*B[1] + A[5]*B[2];
    Y[4] = A[3]*B[3] + A[4]*B[4] + A[5]*B[5];
    Y[5] = A[3]*B[6] + A[4]*B[7] + A[5]*B[8];

    Y[6] = A[6]*B[0] + A[7]*B[1] + A[8]*B[2];
    Y[7] = A[6]*B[3] + A[7]*B[4] + A[8]*B[5];
    Y[8] = A[6]*B[6] + A[7]*B[7] + A[8]*B[8];
}

template<class T> inline void DSMul_3x3t_3x3(T Y[9], const T A[9], const T B[9])
{
    Y[0] = A[0]*B[0] + A[3]*B[3] + A[6]*B[6];
    Y[1] = A[0]*B[1] + A[3]*B[4] + A[6]*B[7];
    Y[2] = A[0]*B[2] + A[3]*B[5] + A[6]*B[8];

    Y[3] = A[1]*B[0] + A[4]*B[3] + A[7]*B[6];
    Y[4] = A[1]*B[1] + A[4]*B[4] + A[7]*B[7];
    Y[5] = A[1]*B[2] + A[4]*B[5] + A[7]*B[8];

    Y[6] = A[2]*B[0] + A[5]*B[3] + A[8]*B[6];
    Y[7] = A[2]*B[1] + A[5]*B[4] + A[8]*B[7];
    Y[8] = A[2]*B[2] + A[5]*B[5] + A[8]*B[8];
}
