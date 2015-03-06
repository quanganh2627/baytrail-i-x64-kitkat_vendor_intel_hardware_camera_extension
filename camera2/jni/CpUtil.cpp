#include "CpUtil.h"
#include <stdio.h>

void convert(JNIEnv* env, ia_frame* dst, jobject jIaFrameSrc) {
	create_ia_frame(dst, ia_frame_format_nv12, getValueInt(env, jIaFrameSrc, "stride"), getValueInt(env, jIaFrameSrc, "width"), getValueInt(env, jIaFrameSrc, "height"), getValueInt(env, jIaFrameSrc, "degree"));
	copyValueByteArray(env, (unsigned char*)dst->data, jIaFrameSrc, "imageData");
	LOGD("converted frame :");
	printFrameInfo(dst);
}

void create_ia_frame(ia_frame* pFrame, ia_frame_format format, int stride, int width, int height, int rotation) {
	if (format == ia_frame_format_nv12) {
		pFrame->width = width;
		pFrame->height = height;
		pFrame->stride = stride;
		pFrame->size = pFrame->stride * height * 3 / 2;
		pFrame->rotation = rotation;
		pFrame->format = format;
		pFrame->data = (unsigned char*) malloc(pFrame->size);
	} else {
		LOGE("[%s] not supported frame format", __FUNCTION__);
	}
}

void destroy_ia_frame(ia_frame* pFrame) {
	if (pFrame->data != NULL) {
		free((unsigned char*) pFrame->data);
	}
}

#define BIT_SHIFT 10
void downscaleFrame(ia_frame* src, ia_frame* dest) {
	unsigned char* pSrc;
	unsigned char* pDest;
	unsigned char* pSrcY = (unsigned char*)src->data;
	unsigned char* pDestY = (unsigned char*)dest->data;
	unsigned char* pSrcUV = pSrcY + src->stride * src->height;
	unsigned char* pDestUV = pDestY + dest->stride * dest->height;
	int dest_w = dest->width;
	int dest_h = dest->height;
	int ratio_w = (src->width << BIT_SHIFT) / dest->width;
	int ratio_h = (src->height << BIT_SHIFT) / dest->height;
	int x, y;
	int index = 0;
	LOGD("src frame");
	printFrameInfo(src);
	LOGD("dest frame");
	printFrameInfo(dest);
	for (y = 0 ; y < dest_h ; y++) {
		pSrc = &pSrcY[((ratio_h * y) >> BIT_SHIFT) * src->stride];
		pDest = &pDestY[y * dest->stride];

		*pDest++ = pSrc[0];
		for (x = 1 ; x < dest_w ; x++) {
			*pDest++ = pSrc[(ratio_w * x) >> BIT_SHIFT];
		}
	}
	dest_w = (dest->width + 1) / 2;
	dest_h = (dest->height + 1) / 2;
	for (y = 0 ; y < dest_h ; y++) {
		pSrc = &pSrcUV[((ratio_h * y) >> BIT_SHIFT) * src->stride];
		pDest = &pDestUV[y * dest->stride];
		pDest[0] = pSrc[0];
		pDest[1] = pSrc[1];
		pDest += 2;
		for (x = 1 ; x < dest_w ; x++) {
			index = ((ratio_w * x) >> BIT_SHIFT) * 2;
			pDest[0] = pSrc[index];
			pDest[1] = pSrc[index + 1];
			pDest += 2;
		}
	}
}

void printFrameInfo(ia_frame* pFrame) {
	LOGD("frame info : data = %p, w = %d, h = %d, stride = %d, size = %d, format = %d, rotation = %d",
			pFrame->data, pFrame->width, pFrame->height, pFrame->stride, pFrame->size, pFrame->format, pFrame->rotation);

}

void debugDumpData(const char* filename, unsigned char* data, int bytes) {
	LOGD("dumping data %p -> %s", data, filename);
	FILE *fp;
	size_t ret;
	fp = fopen(filename, "w+");
//	LOGD("fp = 0x%p", fp);
	if (fp != NULL && data != NULL && bytes > 0) {
		ret = fwrite(data, sizeof(unsigned char), bytes, fp);
//		LOGD("written %d bytes to file", ret);
	}
	fclose(fp);
}


jobject createIaFrame(JNIEnv* env, ia_frame* src)
{
    jclass cls = env->FindClass(CLASS_IAFRAME);
    jmethodID constructor = env->GetMethodID(cls, "<init>", "([BIIIII)V");

    jbyteArray imageData = env->NewByteArray(src->size);
    env->SetByteArrayRegion(imageData, 0, src->size, (jbyte*)src->data);
    jobject iaFrame = env->NewObject(cls, constructor, imageData, src->stride, src->width, src->height, 17, src->rotation);
    return iaFrame;
}

