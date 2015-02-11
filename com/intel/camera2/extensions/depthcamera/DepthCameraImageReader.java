package com.intel.camera2.extensions.depthcamera;

import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.media.Image;
import android.media.Image.Plane;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


//TODO update documentation
/**
 * <p>The ImageReader class allows direct application access to image data
 * rendered into a {@link android.view.Surface}</p>
 *
 * <p>Several Android media API classes accept Surface objects as targets to
 * render to, including {@link MediaPlayer}, {@link MediaCodec},
 * {@link android.hardware.camera2.CameraDevice}, and
 * {@link android.renderscript.Allocation RenderScript Allocations}. The image
 * sizes and formats that can be used with each source vary, and should be
 * checked in the documentation for the specific API.</p>
 *
 * <p>The image data is encapsulated in {@link Image} objects, and multiple such
 * objects can be accessed at the same time, up to the number specified by the
 * {@code maxImages} constructor parameter. New images sent to an ImageReader
 * through its {@link Surface} are queued until accessed through the {@link #acquireLatestImage}
 * or {@link #acquireNextImage} call. Due to memory limits, an image source will
 * eventually stall or drop Images in trying to render to the Surface if the
 * ImageReader does not obtain and release Images at a rate equal to the
 * production rate.</p>
 */
public class DepthCameraImageReader implements AutoCloseable {

    /**
     * Returned by nativeImageSetup when acquiring the image was successful.
     */
    private static final int ACQUIRE_SUCCESS = 0;
    /**
     * Returned by nativeImageSetup when we couldn't acquire the buffer,
     * because there were no buffers available to acquire.
     */
    private static final int ACQUIRE_NO_BUFS = 1;
    /**
     * Returned by nativeImageSetup when we couldn't acquire the buffer
     * because the consumer has already acquired {@maxImages} and cannot
     * acquire more than that.
     */
    private static final int ACQUIRE_MAX_IMAGES = 2;

    /**
     * <p>Create a new reader for images of the desired size and format.</p>
     *
     * <p>The {@code maxImages} parameter determines the maximum number of {@link Image}
     * objects that can be be acquired from the {@code ImageReader}
     * simultaneously. Requesting more buffers will use up more memory, so it is
     * important to use only the minimum number necessary for the use case.</p>
     *
     * <p>The valid sizes and formats depend on the source of the image
     * data.</p>
     *
     * @param width
     *            The default width in pixels of the Images that this reader will produce.
     * @param height
     *            The default height in pixels of the Images that this reader will produce.
     * @param format
     *            The format of the Image that this reader will produce. This
     *            must be one of the {@link android.graphics.ImageFormat} or
     *            {@link android.graphics.PixelFormat} constants. Note that
     *            not all formats is supported, like ImageFormat.NV21.
     * @param maxImages
     *            The maximum number of images the user will want to
     *            access simultaneously. This should be as small as possible to limit
     *            memory use. Once maxImages Images are obtained by the user, one of them
     *            has to be released before a new Image will become available for access
     *            through {@link #acquireLatestImage()} or {@link #acquireNextImage()}.
     *            Must be greater than 0.
     *
     * @see Image
     */
    public static DepthCameraImageReader newInstance(int width, int height, int format, int maxImages) {
        return new DepthCameraImageReader(width, height, format, maxImages);
    }

    /**
     * @hide
     */
    protected DepthCameraImageReader(int width, int height, int format, int maxImages) {
        mWidth = width;
        mHeight = height;
        mFormat = format;
        mMaxImages = maxImages;

        if (width < 1 || height < 1) {
            throw new IllegalArgumentException(
                    "The image dimensions must be positive");
        }
        if (mMaxImages < 1) {
            throw new IllegalArgumentException(
                    "Maximum outstanding image count must be at least 1");
        }

        if (!DepthImageFormat.isPublicFormat(format)) {
            throw new IllegalArgumentException(
                    "format is not supported since it is not one of the formats defined in DepthImageFormat");
        }

        mNumPlanes = getNumPlanesFromFormat();

        nativeInit(new WeakReference<DepthCameraImageReader>(this), width, height, format, maxImages);

        mSurface = nativeGetSurface();
    }

    /**
     * The default width of {@link Image Images}, in pixels.
     *
     * <p>The width may be overridden by the producer sending buffers to this
     * ImageReader's Surface. If so, the actual width of the images can be
     * found using {@link Image#getWidth}.</p>
     *
     * @return the expected width of an Image
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * The default height of {@link Image Images}, in pixels.
     *
     * <p>The height may be overridden by the producer sending buffers to this
     * ImageReader's Surface. If so, the actual height of the images can be
     * found using {@link Image#getHeight}.</p>
     *
     * @return the expected height of an Image
     */
    public int getHeight() {
        return mHeight;
    }

    /**
     * The default {@link ImageFormat image format} of {@link Image Images}.
     *
     * <p>Some color formats may be overridden by the producer sending buffers to
     * this ImageReader's Surface if the default color format allows. ImageReader
     * guarantees that all {@link Image Images} acquired from ImageReader
     * (for example, with {@link #acquireNextImage}) will have a "compatible"
     * format to what was specified in {@link #newInstance}.
     * As of now, each format is only compatible to itself.
     * The actual format of the images can be found using {@link Image#getFormat}.</p>
     *
     * @return the expected format of an Image
     *
     * @see ImageFormat
     */
    public int getImageFormat() {
        return mFormat;
    }

    /**
     * Maximum number of images that can be acquired from the ImageReader by any time (for example,
     * with {@link #acquireNextImage}).
     *
     * <p>An image is considered acquired after it's returned by a function from ImageReader, and
     * until the Image is {@link Image#close closed} to release the image back to the ImageReader.
     * </p>
     *
     * <p>Attempting to acquire more than {@code maxImages} concurrently will result in the
     * acquire function throwing a {@link IllegalStateException}. Furthermore,
     * while the max number of images have been acquired by the ImageReader user, the producer
     * enqueueing additional images may stall until at least one image has been released. </p>
     *
     * @return Maximum number of images for this ImageReader.
     *
     * @see Image#close
     */
    public int getMaxImages() {
        return mMaxImages;
    }

    /**
     * <p>Get a {@link Surface} that can be used to produce {@link Image Images} for this
     * {@code ImageReader}.</p>
     *
     * <p>Until valid image data is rendered into this {@link Surface}, the
     * {@link #acquireNextImage} method will return {@code null}. Only one source
     * can be producing data into this Surface at the same time, although the
     * same {@link Surface} can be reused with a different API once the first source is
     * disconnected from the {@link Surface}.</p>
     *
     * @return A {@link Surface} to use for a drawing target for various APIs.
     */
    public Surface getSurface() {
        return mSurface;
    }
    private Image convertSurfaceImageToDepthCameraImage(SurfaceImage si)
    {
        if (si.getFormat() == DepthImageFormat.Z16)
            return new DepthImageImpl(si);
        return new  UVMAPImageImpl(si);
    }

    private Image acquireNextImageNoConvert() {
        SurfaceImage si = new SurfaceImage();
        int status = acquireNextSurfaceImage(si);

        switch (status) {
            case ACQUIRE_SUCCESS:
                return si;
            case ACQUIRE_NO_BUFS:
                return null;
            case ACQUIRE_MAX_IMAGES:
                throw new IllegalStateException(
                        String.format(
                                "maxImages (%d) has already been acquired, " +
                                        "call #close before acquiring more.", mMaxImages));
            default:
                throw new AssertionError("Unknown nativeImageSetup return code " + status);
        }
    }

    /**
     * <p>
     * Acquire the latest {@link Image} from the ImageReader's queue, dropping older
     * {@link Image images}. Returns {@code null} if no new image is available.
     * </p>
     * <p>
     * This operation will acquire all the images possible from the ImageReader,
     * but {@link #close} all images that aren't the latest. This function is
     * recommended to use over {@link #acquireNextImage} for most use-cases, as it's
     * more suited for real-time processing.
     * </p>
     * <p>
     * Note that {@link #getMaxImages maxImages} should be at least 2 for
     * {@link #acquireLatestImage} to be any different than {@link #acquireNextImage} -
     * discarding all-but-the-newest {@link Image} requires temporarily acquiring two
     * {@link Image Images} at once. Or more generally, calling {@link #acquireLatestImage}
     * with less than two images of margin, that is
     * {@code (maxImages - currentAcquiredImages < 2)} will not discard as expected.
     * </p>
     * <p>
     * This operation will fail by throwing an {@link IllegalStateException} if
     * {@code maxImages} have been acquired with {@link #acquireLatestImage} or
     * {@link #acquireNextImage}. In particular a sequence of {@link #acquireLatestImage}
     * calls greater than {@link #getMaxImages} without calling {@link Image#close} in-between
     * will exhaust the underlying queue. At such a time, {@link IllegalStateException}
     * will be thrown until more images are
     * released with {@link Image#close}.
     * </p>
     *
     * @return latest frame of image data, or {@code null} if no image data is available.
     * @throws IllegalStateException if too many images are currently acquired
     */

    public Image acquireLatestImage() {
        Image image = acquireNextImageNoConvert();
        if (image == null) {
            return null;
        }
        try {
            for (;;) {
                Image next = acquireNextImageNoThrowISE();
                if (next == null) {
                    Image result = image;
                    image = null;
                    return convertSurfaceImageToDepthCameraImage((SurfaceImage)result);
                }
                image.close();
                image = next;
            }
        } finally {
            if (image != null) {
                image.close();
            }
        }
    }

    /**
     * Don't throw IllegalStateException if there are too many images acquired.
     *
     * @return Image if acquiring succeeded, or null otherwise.
     *
     * @hide
     */
    public Image acquireNextImageNoThrowISE() {
        SurfaceImage si = new SurfaceImage();
        return acquireNextSurfaceImage(si) == ACQUIRE_SUCCESS ? si : null;
    }

    /**
     * Attempts to acquire the next image from the underlying native implementation.
     *
     * <p>
     * Note that unexpected failures will throw at the JNI level.
     * </p>
     *
     * @param si A blank SurfaceImage.
     * @return One of the {@code ACQUIRE_*} codes that determine success or failure.
     *
     * @see #ACQUIRE_MAX_IMAGES
     * @see #ACQUIRE_NO_BUFS
     * @see #ACQUIRE_SUCCESS
     */
    private int acquireNextSurfaceImage(SurfaceImage si) {

        int status = nativeImageSetup(si);

        switch (status) {
            case ACQUIRE_SUCCESS:
                si.createSurfacePlanes();
                si.setImageValid(true);
            case ACQUIRE_NO_BUFS:
            case ACQUIRE_MAX_IMAGES:
                break;
            default:
                throw new AssertionError("Unknown nativeImageSetup return code " + status);
        }

        return status;
    }

    /**
     * <p>
     * Acquire the next Image from the ImageReader's queue. Returns {@code null} if
     * no new image is available.
     * </p>
     *
     * <p><i>Warning:</i> Consider using {@link #acquireLatestImage()} instead, as it will
     * automatically release older images, and allow slower-running processing routines to catch
     * up to the newest frame. Usage of {@link #acquireNextImage} is recommended for
     * batch/background processing. Incorrectly using this function can cause images to appear
     * with an ever-increasing delay, followed by a complete stall where no new images seem to
     * appear.
     * </p>
     *
     * <p>
     * This operation will fail by throwing an {@link IllegalStateException} if
     * {@code maxImages} have been acquired with {@link #acquireNextImage} or
     * {@link #acquireLatestImage}. In particular a sequence of {@link #acquireNextImage} or
     * {@link #acquireLatestImage} calls greater than {@link #getMaxImages maxImages} without
     * calling {@link Image#close} in-between will exhaust the underlying queue. At such a time,
     * {@link IllegalStateException} will be thrown until more images are released with
     * {@link Image#close}.
     * </p>
     *
     * @return a new frame of image data, or {@code null} if no image data is available.
     * @throws IllegalStateException if {@code maxImages} images are currently acquired
     * @see #acquireLatestImage
     */
    public Image acquireNextImage() {
        SurfaceImage si = new SurfaceImage();
        int status = acquireNextSurfaceImage(si);

        switch (status) {
            case ACQUIRE_SUCCESS:
                return convertSurfaceImageToDepthCameraImage(si);
            case ACQUIRE_NO_BUFS:
                return null;
            case ACQUIRE_MAX_IMAGES:
                throw new IllegalStateException(
                        String.format(
                                "maxImages (%d) has already been acquired, " +
                                        "call #close before acquiring more.", mMaxImages));
            default:
                throw new AssertionError("Unknown nativeImageSetup return code " + status);
        }
    }

    /**
     * <p>Return the frame to the ImageReader for reuse.</p>
     */
    private void releaseImage(Image i) {
        if (! (i instanceof SurfaceImage)) {
            throw new IllegalArgumentException(
                    "This image was not produced by an ImageReader");
        }
        SurfaceImage si = (SurfaceImage) i;
        if (si.getReader() != this) {
            throw new IllegalArgumentException(
                    "This image was not produced by this ImageReader");
        }

        si.clearSurfacePlanes();
        nativeReleaseImage(i);
        si.setImageValid(false);
    }

    /**
     * Register a listener to be invoked when a new image becomes available
     * from the ImageReader.
     *
     * @param listener
     *            The listener that will be run.
     * @param handler
     *            The handler on which the listener should be invoked, or null
     *            if the listener should be invoked on the calling thread's looper.
     * @throws IllegalArgumentException
     *            If no handler specified and the calling thread has no looper.
     */
    public void setOnImageAvailableListener(OnDepthCameraImageAvailableListener listener, Handler handler) {
        synchronized (mListenerLock) {
            if (listener != null) {
                Looper looper = handler != null ? handler.getLooper() : Looper.myLooper();
                if (looper == null) {
                    throw new IllegalArgumentException(
                            "handler is null but the current thread is not a looper");
                }
                if (mListenerHandler == null || mListenerHandler.getLooper() != looper) {
                    mListenerHandler = new ListenerHandler(looper);
                }
                mListener = listener;
            } else {
                mListener = null;
                mListenerHandler = null;
            }
        }
    }

    /**
     * Callback interface for being notified that a new image is available.
     *
     * <p>
     * The onImageAvailable is called per image basis, that is, callback fires for every new frame
     * available from ImageReader.
     * </p>
     */
    public interface OnDepthCameraImageAvailableListener {
        /**
         * Callback that is called when a new image is available from ImageReader.
         *
         * @param reader the ImageReader the callback is associated with.
         * @see ImageReader
         * @see Image
         */
        void onDepthCameraImageAvailable(DepthCameraImageReader reader);
    }

    /**
     * Free up all the resources associated with this ImageReader.
     *
     * <p>
     * After calling this method, this ImageReader can not be used. Calling
     * any methods on this ImageReader and Images previously provided by
     * {@link #acquireNextImage} or {@link #acquireLatestImage}
     * will result in an {@link IllegalStateException}, and attempting to read from
     * {@link ByteBuffer ByteBuffers} returned by an earlier
     * {@link Image.Plane#getBuffer Plane#getBuffer} call will
     * have undefined behavior.
     * </p>
     */
    @Override
    public void close() {
        setOnImageAvailableListener(null, null);
        nativeClose();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    /**
     * Only a subset of the formats defined in
     * {@link android.graphics.ImageFormat ImageFormat} and
     * {@link android.graphics.PixelFormat PixelFormat} are supported by
     * ImageReader. When reading RGB data from a surface, the formats defined in
     * {@link android.graphics.PixelFormat PixelFormat} can be used, when
     * reading YUV, JPEG or raw sensor data (for example, from camera or video
     * decoder), formats from {@link android.graphics.ImageFormat ImageFormat}
     * are used.
     */
    private int getNumPlanesFromFormat() {
        switch (mFormat) {
            case DepthImageFormat.Z16:
            case DepthImageFormat.UVMAP:
                return 1;
            default:
                throw new UnsupportedOperationException(
                        String.format("Invalid depth format specified %d", mFormat));
        }
    }

    /**
     * Called from Native code when an Event happens.
     *
     * This may be called from an arbitrary Binder thread, so access to the ImageReader must be
     * synchronized appropriately.
     */
    private static void postEventFromNative(Object selfRef) {
        @SuppressWarnings("unchecked")
        WeakReference<DepthCameraImageReader> weakSelf = (WeakReference<DepthCameraImageReader>)selfRef;
        final DepthCameraImageReader ir = weakSelf.get();
        if (ir == null) {
            return;
        }

        final Handler handler;
        synchronized (ir.mListenerLock) {
            handler = ir.mListenerHandler;
        }
        if (handler != null) {
            handler.sendEmptyMessage(0);
        }
    }


    private final int mWidth;
    private final int mHeight;
    private final int mFormat;
    private final int mMaxImages;
    private final int mNumPlanes;
    private final Surface mSurface;

    private final Object mListenerLock = new Object();
    private OnDepthCameraImageAvailableListener mListener;
    private ListenerHandler mListenerHandler;

    /**
     * This field is used by native code, do not access or modify.
     */
    private long mNativeContext;

    /**
     * This custom handler runs asynchronously so callbacks don't get queued behind UI messages.
     */
    private final class ListenerHandler extends Handler {
        public ListenerHandler(Looper looper) {
            super(looper, null, true /*async*/);
        }

        @Override
        public void handleMessage(Message msg) {
            OnDepthCameraImageAvailableListener listener;
            synchronized (mListenerLock) {
                listener = mListener;
            }
            if (listener != null) {
                listener.onDepthCameraImageAvailable(DepthCameraImageReader.this);
            }
        }
    }

    private class SurfaceImage extends Image {
        public SurfaceImage() {
            mIsImageValid = false;
        }

        @Override
        public void close() {
            if (mIsImageValid) {
                DepthCameraImageReader.this.releaseImage(this);
            }
        }

        public DepthCameraImageReader getReader() {
            return DepthCameraImageReader.this;
        }

        @Override
        public int getFormat() {
            if (mIsImageValid) {
                return DepthCameraImageReader.this.mFormat;
            } else {
                throw new IllegalStateException("Image is already released");
            }
        }

        @Override
        public int getWidth() {
            if (mIsImageValid) {
                return DepthCameraImageReader.this.mWidth;
            } else {
                throw new IllegalStateException("Image is already released");
            }
        }

        @Override
        public int getHeight() {
            if (mIsImageValid) {
                return DepthCameraImageReader.this.mHeight;
            } else {
                throw new IllegalStateException("Image is already released");
            }
        }

        @Override
        public long getTimestamp() {
            if (mIsImageValid) {
                return mTimestamp;
            } else {
                throw new IllegalStateException("Image is already released");
            }
        }

        @Override
        public Plane[] getPlanes() {
            if (mIsImageValid) {
                // Shallow copy is fine.
                return mPlanes.clone();
            } else {
                throw new IllegalStateException("Image is already released");
            }
        }

        @Override
        protected final void finalize() throws Throwable {
            try {
                close();
            } finally {
                super.finalize();
            }
        }

        private void setImageValid(boolean isValid) {
            mIsImageValid = isValid;
        }

        private boolean isImageValid() {
            return mIsImageValid;
        }

        private void clearSurfacePlanes() {
            if (mIsImageValid) {
                for (int i = 0; i < mPlanes.length; i++) {
                    if (mPlanes[i] != null) {
                        mPlanes[i].clearBuffer();
                        mPlanes[i] = null;
                    }
                }
            }
        }

        private void createSurfacePlanes() {
            mPlanes = new SurfacePlane[DepthCameraImageReader.this.mNumPlanes];
            for (int i = 0; i < DepthCameraImageReader.this.mNumPlanes; i++) {
                mPlanes[i] = nativeCreatePlane(i, DepthCameraImageReader.this.mFormat);
            }
        }
        private class SurfacePlane extends android.media.Image.Plane {
            // SurfacePlane instance is created by native code when a new SurfaceImage is created
            private SurfacePlane(int index, int rowStride, int pixelStride) {
                mIndex = index;
                mRowStride = rowStride;
                mPixelStride = pixelStride;
            }

            @Override
            public ByteBuffer getBuffer() {
                if (SurfaceImage.this.isImageValid() == false) {
                    throw new IllegalStateException("Image is already released");
                }
                if (mBuffer != null) {
                    return mBuffer;
                } else {
                    mBuffer = SurfaceImage.this.nativeImageGetBuffer(mIndex,
                            DepthCameraImageReader.this.mFormat);
                    // Set the byteBuffer order according to host endianness (native order),
                    // otherwise, the byteBuffer order defaults to ByteOrder.BIG_ENDIAN.
                    return mBuffer.order(ByteOrder.nativeOrder());
                }
            }

            @Override
            public int getPixelStride() {
                if (SurfaceImage.this.isImageValid()) {
                    return mPixelStride;
                } else {
                    throw new IllegalStateException("Image is already released");
                }
            }

            @Override
            public int getRowStride() {
                if (SurfaceImage.this.isImageValid()) {
                    return mRowStride;
                } else {
                    throw new IllegalStateException("Image is already released");
                }
            }

            private void clearBuffer() {
                mBuffer = null;
            }

            final private int mIndex;
            final private int mPixelStride;
            final private int mRowStride;

            private ByteBuffer mBuffer;
        }


        /**
         * This field is used to keep track of native object and used by native code only.
         * Don't modify.
         */
        private long mLockedBuffer;

        /**
         * This field is set by native code during nativeImageSetup().
         */
        private long mTimestamp;

        private SurfacePlane[] mPlanes;
        private boolean mIsImageValid;

        private synchronized native ByteBuffer nativeImageGetBuffer(int idx, int readerFormat);
        private synchronized native SurfacePlane nativeCreatePlane(int idx, int readerFormat);

        public synchronized native void nativeCalcUVMapVal(double[] rotation, float[] translation, float depthfx, float depthfy, float deothpx, float depthpy, float colorfx, float colorfy,
                                                           float colorpx, float colorpy, double[] distortion, int colorWidth, int colorHeight, boolean rectified, int x, int y, ByteBuffer res, int depth
                                                           , int width, int height);
        public synchronized native void nativeCalcUVMapValForRegion(double[] rotation, float[] translation, float depthfx, float depthfy, float deothpx, float depthpy, float colorfx, float colorfy,
                                                                    float colorpx, float colorpy, double[] distortion, int colorWidth, int colorHeight, boolean rectified, int originx, int originy,
                                                                    int widht, int height, ByteBuffer res);
    }
    private class DepthImageImpl extends DepthImage
    {
        public DepthImageImpl(SurfaceImage image)
        {
            if (image == null)
                throw new IllegalArgumentException(
                        "image cannot be null");
            mImage = image;
        }
        @Override
        public void close() {
            mImage.close();
        }

        public DepthCameraImageReader getReader() {
            return mImage.getReader();
        }

        @Override
        public int getFormat() {
            return mImage.getFormat();
        }

        @Override
        public int getWidth() {
            return mImage.getWidth();
        }

        @Override
        public int getHeight() {
            return mImage.getHeight();
        }

        @Override
        public long getTimestamp() {
            return mImage.getTimestamp();
        }

        @Override
        public Plane[] getPlanes() {
            return mImage.getPlanes();
        }

        // row , column , depth => point cloud in world coordinates (camera coordinates)
        @Override
        public Point3DF projectImageToWorldCoordinates(DepthCameraCalibrationDataMap.IntrinsicParams zIntrinsics ,Point pos2d)
        {
            if (pos2d.x < 0 || pos2d.y < 0)
                throw new IllegalArgumentException("origin x,y cannot be negative!");
            if (pos2d.y >= getHeight() || pos2d.x  >= getWidth())
                throw new IllegalArgumentException("point exceeds the depth image limits! "  + pos2d.x + " " + pos2d.y);

            float z = getZ(pos2d.x, pos2d.y);
            PointF principalP = zIntrinsics.getPrincipalPoint();
            PointF focalL = zIntrinsics.getFocalLength();
            float x = 0;
            if (focalL.x != 0)
                x = z * (pos2d.x - principalP.x) / focalL.x;
            float y = 0;
            if (focalL.y != 0)
                y =z * (pos2d.y - principalP.y) / focalL.y;
            return new Point3DF(x,y,z);
        }

        //wether the color is rectified or not can be "understood" from the parameters (distortion is null	)
        @Override
        public PointF mapDepthToColorCoordinates(DepthCameraCalibrationDataMap.DepthCameraCalibrationData calibrationData, Point p)
        {
            DepthCameraCalibrationDataMap.ExtrinsicParams extrinsics = calibrationData.getDepthToColorExtrinsics();
            DepthCameraCalibrationDataMap.IntrinsicParams colorIntr = calibrationData.getColorCameraIntrinsics();
            DepthCameraCalibrationDataMap.IntrinsicParams depthIntr = calibrationData.getDepthCameraIntrinsics();
            if (p.x < 0 || p.y < 0)
                throw new IllegalArgumentException("origin x,y cannot be negative!");
            if (p.y >= getHeight() || p.x  >= getWidth())
                throw new IllegalArgumentException("point exceeds the depth image limits! "  + p.x + " " + p.y);

            double[][] rotation = extrinsics.getRotation();
            double[] rotation1D = new double[9];
            for (int i=0; i <3; i++)
                for (int j=0; j<3; j++)
                    rotation1D[i*3+j] = rotation[i][j];

            ByteBuffer resByteBuffer = ByteBuffer.allocateDirect(4*2); //uvmap is 2 floats = float = 4bytes
            resByteBuffer.order(ByteOrder.nativeOrder());
            mImage.nativeCalcUVMapVal(rotation1D,
                    extrinsics.getTranslation(),
                    depthIntr.getFocalLength().x, depthIntr.getFocalLength().y, //depth focal
                    depthIntr.getPrincipalPoint().x, depthIntr.getPrincipalPoint().y, //depth principal
                    colorIntr.getFocalLength().x, colorIntr.getFocalLength().y, //color focal
                    colorIntr.getPrincipalPoint().x, colorIntr.getPrincipalPoint().y, //color principal
                    colorIntr.getDistortion(),
                    colorIntr.getResolution().getWidth(), colorIntr.getResolution().getHeight(),
                    colorIntr.isRectified(),
                    p.x , p.y, resByteBuffer, getZ(p.x,p.y), getWidth(), getHeight() ); //depth coordinatesvalue

            return new PointF(resByteBuffer.getFloat(), resByteBuffer.getFloat());
        }
        @Override
        public PointF[][]  mapDepthToColorCoordinates(DepthCameraCalibrationDataMap.DepthCameraCalibrationData calibrationData, Point origin, int width, int height)
        {
            if (origin.x < 0 || origin.y < 0)
                throw new IllegalArgumentException("origin x,y cannot be negative!");
            if (origin.x + width > getWidth() || origin.y + height > getHeight())
                throw new IllegalArgumentException("rectangle area is exceeds the depth image limits " + origin.x + " " + origin.y);

            ByteBuffer resByteBuffer = ByteBuffer.allocateDirect(width*height*4*2); //uvmap is 2 floats = float = 4bytes
            resByteBuffer.order(ByteOrder.nativeOrder());

            DepthCameraCalibrationDataMap.ExtrinsicParams extrinsics = calibrationData.getDepthToColorExtrinsics();
            DepthCameraCalibrationDataMap.IntrinsicParams colorIntr = calibrationData.getColorCameraIntrinsics();
            DepthCameraCalibrationDataMap.IntrinsicParams depthIntr = calibrationData.getDepthCameraIntrinsics();

            double[][] rotation = extrinsics.getRotation();
            double[] rotation1D = new double[9];
            for (int i=0; i <3; i++)
                for (int j=0; j<3; j++)
                    rotation1D[i*3+j] = rotation[i][j];

            mImage.nativeCalcUVMapValForRegion(rotation1D,
                    extrinsics.getTranslation(),
                    depthIntr.getFocalLength().x, depthIntr.getFocalLength().y, //depth focal
                    depthIntr.getPrincipalPoint().x, depthIntr.getPrincipalPoint().y, //depth principal
                    colorIntr.getFocalLength().x, colorIntr.getFocalLength().y, //color focal
                    colorIntr.getPrincipalPoint().x, colorIntr.getPrincipalPoint().y, //color principal
                    colorIntr.getDistortion(),
                    colorIntr.getResolution().getWidth(), colorIntr.getResolution().getHeight(),
                    colorIntr.isRectified(),
                    origin.x, origin.y , width, height, resByteBuffer);

            PointF[][] res = new PointF[height][width];
            for (int i=0; i< height; i++)
                for (int j=0; j< width; j++)
                {
                    res[i][j] = new PointF();
                    res[i][j].x = resByteBuffer.getFloat(); //gets next float and increases position by 4
                    res[i][j].y = resByteBuffer.getFloat();
                }

            return res;
        }
        @Override
        public int getZ(int x, int y)
        {
            if (mImage.isImageValid())
            {
                Plane[] planes = mImage.getPlanes();
                if (planes != null)
                {
                    Plane depthPlane = planes[0];
                    if (depthPlane == null)
                        throw new IllegalStateException("Depth plane is null");

                    int zPos = y*depthPlane.getRowStride() + x*depthPlane.getPixelStride();
                    if (zPos + depthPlane.getPixelStride() > depthPlane.getBuffer().capacity())
                    {
                        throw new IllegalArgumentException(
                                "x,y indexes are out of boundaries of the depth buffer zpos " + zPos + " capacity " + depthPlane.getBuffer().capacity());
                    }
                    return depthPlane.getBuffer().getChar(zPos);
                }
                else
                    throw new IllegalStateException("Depth plane is null");

            }
            throw new IllegalStateException("getZ - Image is already released");
        }

        //Surface Image
        private SurfaceImage mImage;
    }

    private class UVMAPImageImpl extends UVMAPImage
    {
        public UVMAPImageImpl(SurfaceImage image)
        {
            if (image == null)
                throw new IllegalArgumentException(
                        "image cannot be null");
            mImage = image;
        }
        @Override
        public void close() {
            mImage.close();
        }

        public DepthCameraImageReader getReader() {
            return mImage.getReader();
        }

        @Override
        public int getFormat() {
            return mImage.getFormat();
        }

        @Override
        public int getWidth() {
            return mImage.getWidth();
        }

        @Override
        public int getHeight() {
            return mImage.getHeight();
        }

        @Override
        public long getTimestamp() {
            return mImage.getTimestamp();
        }

        @Override
        public Plane[] getPlanes() {
            return mImage.getPlanes();
        }

        @Override
        public PointF getUV(int x,int y)
        {
            if (mImage.isImageValid())
            {
                Plane[] p = mImage.getPlanes();
                if (p == null)
                    throw new IllegalStateException("UVMAPImage plane is null!!");

                Plane uvmapPlane = p[0];
                ByteBuffer uvMapBuffer = uvmapPlane.getBuffer();
                int uPos = y * uvmapPlane.getRowStride() + x * uvmapPlane.getPixelStride();
                if (uPos + uvmapPlane.getPixelStride() > uvMapBuffer.capacity())
                {
                    throw new IllegalArgumentException(
                            "uvMap x,y indexes are out of boundaries of the uvmap buffer");
                }
                return new PointF(uvMapBuffer.getFloat(uPos) , uvMapBuffer.getFloat(uPos + 4));
            }
            throw new IllegalStateException("getUV - Image is already released");
        }
        //Surface Image
        private SurfaceImage mImage;
    }

    private synchronized native void nativeInit(Object weakSelf, int w, int h,
                                                int fmt, int maxImgs);
    private synchronized native void nativeClose();
    private synchronized native void nativeReleaseImage(Image i);
    private synchronized native Surface nativeGetSurface();

    /**
     * @return A return code {@code ACQUIRE_*}
     *
     * @see #ACQUIRE_SUCCESS
     * @see #ACQUIRE_NO_BUFS
     * @see #ACQUIRE_MAX_IMAGES
     */
    private synchronized native int nativeImageSetup(Image i);

    /**
     * We use a class initializer to allow the native code to cache some
     * field offsets.
     */
    private static native void nativeClassInit();
    static {
        System.loadLibrary("inteldepthcamera_jni");
        nativeClassInit();
    }
}
