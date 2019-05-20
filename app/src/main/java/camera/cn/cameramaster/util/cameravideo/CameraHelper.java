package camera.cn.cameramaster.util.cameravideo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import camera.cn.cameramaster.R;
import camera.cn.cameramaster.view.AutoFitTextureView;
import camera.cn.cameramaster.view.AwbSeekBar;


/**
 * 摄像头整合帮助类
 *
 * @date 2019年5月9日 17:08:21
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraHelper implements ICamera2, AwbSeekBar.OnAwbSeekBarChangeListener {

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * 淡入 淡出 动画
     */
    private final Animation mAlphaInAnimation;
    private final Animation mAlphaOutAnimation;

    /**
     * 设备旋转方向
     */
    private int mDeviceRotation;

    private int mPhotoRotation;

    /**
     * 光强
     */
    private float mLight;

    private AtomicBoolean mIsCameraOpen;

    private CameraManager mCameraManager;

    private TakePhotoListener mTakePhotoListener;

    private CameraReady mCameraReady;

    /**
     * 摄像头的id集合
     */
    private String[] mCameraIds;

    /**
     * 摄像头支持的最大size
     */
    private Size mLargest;

    /**
     * 可缩放区域
     */
    private Size mZoomSize;

    private Size mVideoSize;

    private Context mContext;

    /**
     * 相机 曝光 范围
     */
    private Range<Integer> range1;

    /**
     * 需要打开的摄像头id
     */
    private String mCameraId;

    private MediaRecorder mMediaRecorder;

    private CaptureRequest.Builder mPreviewBuilder;

    private CameraDevice mCameraDevice;

    private CameraCaptureSession mPreviewSession;

    private TextureView mTextureView;
    /**
     * 后台线程
     */
    private HandlerThread mBackgroundThread;

    /**
     * 后台handle
     */
    private Handler mBackgroundHandler;

    private AtomicBoolean mIsRecordVideo = new AtomicBoolean();

    private CameraType mNowCameraType;

    /**
     * 拍照的图片读取类
     */
    private ImageReader mImageReader;

    /**
     * 是否支持闪光灯
     */
    private boolean mFlashSupported;

    /**
     * 图片的路径
     */
    private String mPhotoPath;

    /**
     * Orientation of the camera sensor
     */
    private int mSensorOrientation;

    /**
     * 最大的放大倍数
     */
    private float mMaxZoom = 0;

    /**
     * 放大的矩阵，拍照使用
     */
    private Rect mZoomRect;

    /**
     * 摄像头支持的分辨率流集合
     */
    private StreamConfigurationMap mMap;

    private FlashState mNowFlashState = FlashState.CLOSE;

    private boolean mIsCapture = false;

    private CameraCharacteristics mCharacteristics;

    private boolean mNoAFRun = false;

    private boolean mIsAFRequest = false;

    private CameraMode CAMERA_STATE = CameraMode.TAKE_PHOTO;

    private Surface mPreViewSurface;

    private Surface mRecordSurface;

    private CoordinateTransformer mCoordinateTransformer;

    private Rect mPreviewRect;
    private Rect mFocusRect;

    /**
     * 根据摄像头管理器获取一个帮助类
     *
     * @param context 实体类
     */
    public CameraHelper(Context context) {
        this.mContext = context;
        mIsCameraOpen = new AtomicBoolean(false);
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mCameraManager = cameraManager;
        try {
            mCameraIds = mCameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mFocusRect = new Rect();

        mAlphaInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.alpha_in);
        mAlphaOutAnimation = AnimationUtils.loadAnimation(mContext, R.anim.alpha_out);
    }

    private TextView mTextView;

    /**
     * 状态文字 赋值
     * @param mTextView tv
     */
    public void setShowTextView(TextView mTextView){
        this.mTextView = mTextView;
    }


    @Override
    public void cameraZoom(float scale) {
        if (scale < 1.0f) {
            scale = 1.0f;
        }
        if (scale <= mMaxZoom) {
            int cropW = (int) ((mZoomSize.getWidth() / (mMaxZoom * 2.6)) * scale);
            int cropH = (int) ((mZoomSize.getHeight() / (mMaxZoom * 2.6)) * scale);

            Rect zoom = new Rect(cropW, cropH,
                    mZoomSize.getWidth() - cropW,
                    mZoomSize.getHeight() - cropH);
            mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
            mZoomRect = zoom;
            updatePreview();   //重复更新预览请求
        }
    }

    /**
     * 获取最大zoom
     *
     * @return 放大数值
     */
    public float getMaxZoom() {
        return mMaxZoom;
    }

    /**
     * 初始化拍照的图片读取类
     */
    private void initImageReader() {
        //取最大的分辨率
        Size largest = Collections.max(Arrays.asList(mMap.getOutputSizes(ImageFormat.JPEG)),
                new CompareSizesByArea());
        mZoomSize = largest;
        //实例化拍照用的图片读取类
        if (mImageReader != null) {
            mImageReader.close();
        }
        mImageReader = ImageReader.newInstance(largest.getWidth(),
                largest.getHeight(), ImageFormat.JPEG, 2);
    }

    /**
     * 初始化一个适合的预览尺寸
     */
    private void initSize() {
        Size largest = Collections.max(
                Arrays.asList(mMap.getOutputSizes(ImageFormat.JPEG)),
                new CompareSizesByArea());

        Point displaySize = new Point();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getSize(displaySize);

        mLargest = chooseOptimalSize(mMap.getOutputSizes(SurfaceTexture.class),
                this.mTextureView.getWidth(),
                this.mTextureView.getHeight(),
                displaySize.x,
                displaySize.y,
                largest
        );
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean openCamera(CameraType cameraType) {

        if (mIsCameraOpen.get()) {
            return true;
        }
        mIsCameraOpen.set(true);
        mZoomRect = null;
        this.mNowCameraType = cameraType;
        int cameraTypeId;
        switch (cameraType) {
            default:
            case BACK:
                cameraTypeId = CameraCharacteristics.LENS_FACING_BACK;
                break;
            case FRONT:
                cameraTypeId = CameraCharacteristics.LENS_FACING_FRONT;
                break;
            case USB:
                cameraTypeId = CameraCharacteristics.LENS_FACING_EXTERNAL;
                break;
        }

        try {
            for (String cameraId : mCameraIds) {
                mCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);
                Integer facing = mCharacteristics.get(CameraCharacteristics.LENS_FACING);
                // 曝光增益 范围
                range1 = mCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
                //获取曝光时间
                etr = mCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
                if (facing != null && facing != cameraTypeId) {
                    continue;
                }

                Float maxZoom = mCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
                if (maxZoom != null) {
                    mMaxZoom = maxZoom;
                }

                //获取摄像头支持的流配置信息
                mMap = mCharacteristics
                        .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (mMap == null) {
                    return false;
                }
                //初始化拍照的图片读取类
                initImageReader();
                //初始化尺寸
                initSize();

                //获取摄像头角度
                mSensorOrientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

                mVideoSize = chooseVideoSize(mMap.getOutputSizes(MediaRecorder.class));
                if (mTextureView != null) {
                    ((AutoFitTextureView) mTextureView).setAspectRatio(mLargest.getHeight(), mLargest.getWidth());
                }

                //检查是否这个摄像头是否支持闪光灯，拍照模式的时候使用
                Boolean available = mCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                this.mCameraId = cameraId;
                mPreviewRect = new Rect(0, 0, mTextureView.getWidth(), mTextureView.getHeight());
                mCoordinateTransformer = new CoordinateTransformer(mCharacteristics, new RectF(mPreviewRect));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return openCamera(mCameraId);
    }

    @SuppressLint("MissingPermission")
    private boolean openCamera(String cameraId) {
        try {
            mCameraManager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void closeCamera() {
        Log.e("camera", "关闭摄像头");
        mIsCameraOpen.set(false);

        closePreviewSession();
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    @Override
    public boolean switchCamera(CameraType cameraType) {
        closeCamera();
        return openCamera(cameraType);
    }

    @Override
    public boolean startPreview() {
        if (mBackgroundHandler == null) {
            return false;
        }
        try {

            //初始化预览的尺寸
            initSize();

            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mLargest.getWidth(), mLargest.getHeight());
            mPreViewSurface = new Surface(surfaceTexture);
            //创建一个预览请求
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //添加预览输出目标画面
            mPreviewBuilder.addTarget(mPreViewSurface);

            if (mZoomRect != null) {
                //放大的矩阵
                mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, mZoomRect);
            }
            //当前线程创建一个预览请求
            mCameraDevice.createCaptureSession(Arrays.asList(mPreViewSurface,
                    mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    mPreviewSession = session;
                    setup3AControlsLocked(mPreviewBuilder);
                    //重复更新预览请求
                    updatePreview();
                    if (mCameraReady != null) {
                        mCameraReady.onCameraReady();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 更新预览界面
     */
    private void updatePreview() {
        if (mCameraDevice == null) {
            return;
        }
        try {
            //  mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            mPreviewSession.setRepeatingRequest(
                    mPreviewBuilder.build(),
                    null,
                    mBackgroundHandler
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void resumePreview() {
        try {
            if (!mNoAFRun) {
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_IDLE);
            }
            if (!isLegacyLocked()) {
                // Tell the camera to lock focus.
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                        CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_CANCEL);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                        CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE);
            }
            mIsAFRequest = false;
            mCameraState = 0;
            mPreviewSession.capture(mPreviewBuilder.build(), null,
                    mBackgroundHandler);
            updatePreview();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean startVideoRecord(String path, int mediaType) {
        if (mIsRecordVideo.get()){
            new Throwable("video record is recording");
        }
        if (path == null){
            new Throwable("path can not null");
        }
        if (mediaType != MediaRecorder.OutputFormat.MPEG_4){
            new Throwable("this mediaType can not support");
        }
        if (!setVideoRecordParam(path)){
            return false;
        }
        startRecordVideo();
        return true;
    }

    /**
     * 设置录像的参数
     *
     * @param path
     * @return
     */
    private boolean setVideoRecordParam(String path) {
        mMediaRecorder = new MediaRecorder();

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(path);

        int bitRate = mVideoSize.getWidth() * mVideoSize.getHeight();
        bitRate = mVideoSize.getWidth() < 1080 ? bitRate * 2 : bitRate;

        mMediaRecorder.setVideoEncodingBitRate(bitRate);
        mMediaRecorder.setVideoFrameRate(15);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        mMediaRecorder.setAudioEncodingBitRate(8000);
        mMediaRecorder.setAudioChannels(1);
        mMediaRecorder.setAudioSamplingRate(8000);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        if (mNowCameraType == CameraType.BACK) {
            //后置摄像头图像要旋转90度
            mMediaRecorder.setOrientationHint(90);
        } else {
            //前置摄像头图像要旋转270度
            mMediaRecorder.setOrientationHint(270);
        }
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void stopVideoRecord() {
        if (mIsRecordVideo.get()) {
            mIsRecordVideo.set(false);
        } else {
            return;
        }
        mMediaRecorder.setOnErrorListener(null);
        mMediaRecorder.setOnInfoListener(null);
        mMediaRecorder.setPreviewDisplay(null);
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        // startPreview();
    }

    @Override
    public boolean takePhone(String path, MediaType mediaType) {
        this.mPhotoPath = path;
        setTakePhotoFlashMode(mPreviewBuilder);
        updatePreview();
        // lockFocus();

        if (!mNoAFRun) {
            if (mIsAFRequest) {
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, AFRegions);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, AERegions);
            }
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
        }
        if (!isLegacyLocked()) {
            // Tell the camera to lock focus.
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
        }
        mCameraState = WAITING_LOCK;
        if (!mFlashSupported) {
            capturePhoto();
        } else {
            switch (mNowFlashState) {
                case CLOSE:
                    capturePhoto();
                    break;
                case OPEN:
                case AUTO:
                    mBackgroundHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mPreviewSession.capture(mPreviewBuilder.build(), mCaptureCallback,
                                        mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 800);
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    private boolean isLegacyLocked() {
        return mCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) ==
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;
    }

    private void setup3AControlsLocked(CaptureRequest.Builder builder) {
        // Enable auto-magical 3A run by camera device
        builder.set(CaptureRequest.CONTROL_MODE,
                CaptureRequest.CONTROL_MODE_AUTO);

        Float minFocusDist =
                mCharacteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);

        // If MINIMUM_FOCUS_DISTANCE is 0, lens is fixed-focus and we need to skip the AF run.
        mNoAFRun = (minFocusDist == null || minFocusDist == 0);

        if (!mNoAFRun) {
            // If there is a "continuous picture" mode available, use it, otherwise default to AUTO.
            if (contains(mCharacteristics.get(
                    CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES),
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)) {
                builder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            } else {
                builder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_AUTO);
            }
        }

        // If there is an auto-magical flash control mode available, use it, otherwise default to
        // the "on" mode, which is guaranteed to always be available.
        if (mNowFlashState != FlashState.CLOSE) {
            if (contains(mCharacteristics.get(
                    CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES),
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)) {
                builder.set(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            } else {
                builder.set(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON);
            }
        }

        // If there is an auto-magical white balance control mode available, use it.
        if (contains(mCharacteristics.get(
                CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES),
                CaptureRequest.CONTROL_AWB_MODE_AUTO)) {
            // Allow AWB to run auto-magically if this device supports this
            builder.set(CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_AUTO);
        }
    }

    /**
     * 真正拍照
     */
    private void capturePhoto() {
        mIsCapture = true;
        final CaptureRequest.Builder captureBuilder;
        try {
            //设置拍照后的回调监听
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
            captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());
            //设置自动对焦
            /*captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);*/
            // Use the same AE and AF modes as the preview.
      /*      if(mNowFlashState != FlashState.CLOSE) {
                if(mFlashSupported)
                    setup3AControlsLocked(captureBuilder);
            }*/

            mPhotoRotation = getOrientation(mDeviceRotation);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, mPhotoRotation);

            //放大的矩阵
            if (mZoomRect != null) {
                captureBuilder.set(CaptureRequest.SCALER_CROP_REGION, mZoomRect);
            }
            setTakePhotoFlashMode(captureBuilder);
            captureBuilder.setTag(1);
            mPreviewSession.stopRepeating();
            mPreviewSession.abortCaptures();
            mBackgroundHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        mPreviewSession.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                            }

                            @Override
                            public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {

                            }


                        }, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }, 200);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Size getPreViewSize() {
        return mLargest;
    }

    @Override
    public void setSurface(Surface surface) {
        //this.mSurface = surface;
    }

    /**
     * 如果设置了textureView则不用设置Surface
     *
     * @param textureView textureView
     */
    @Override
    public void setTextureView(TextureView textureView) {
        this.mTextureView = textureView;
    }

    @Override
    public void setTakePhotoListener(TakePhotoListener mTakePhotoListener) {
        this.mTakePhotoListener = mTakePhotoListener;
    }

    @Override
    public void setCameraReady(CameraReady cameraReady) {
        this.mCameraReady = cameraReady;
    }

    @Override
    public void flashSwitchState(FlashState mFlashState) {
        mNowFlashState = mFlashState;
        if (CAMERA_STATE == CameraMode.TAKE_PHOTO) {
            setTakePhotoFlashMode(mPreviewBuilder);
            updatePreview();
        }
    }

    @Override
    public void setCameraState(CameraMode cameraMode) {
        CAMERA_STATE = cameraMode;
        if (CAMERA_STATE == CameraMode.TAKE_PHOTO) {
            setTakePhotoFlashMode(mPreviewBuilder);
            updatePreview();
        }
    }

    private void toFocusRect(RectF rectF) {
        mFocusRect.left = Math.round(rectF.left);
        mFocusRect.top = Math.round(rectF.top);
        mFocusRect.right = Math.round(rectF.right);
        mFocusRect.bottom = Math.round(rectF.bottom);
    }

    private MeteringRectangle calcTapAreaForCamera2(int areaSize, int weight, float x, float y) {
        int left = clamp((int) x - areaSize / 2,
                mPreviewRect.left, mPreviewRect.right - areaSize);
        int top = clamp((int) y - areaSize / 2,
                mPreviewRect.top, mPreviewRect.bottom - areaSize);
        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        toFocusRect(mCoordinateTransformer.toCameraSpace(rectF));
        return new MeteringRectangle(mFocusRect, weight);
    }

    private MeteringRectangle[] AFRegions;
    private MeteringRectangle[] AERegions;

    @Override
    public void requestFocus(float x, float y) {
        mIsAFRequest = true;
        MeteringRectangle rect = calcTapAreaForCamera2(
                mTextureView.getWidth() / 5,
                1000, x, y);

        AFRegions = new MeteringRectangle[]{rect};
        AERegions = new MeteringRectangle[]{rect};

        Log.e("AFRegions", "AFRegions:" + AFRegions[0].toString());

        try {
            final CaptureRequest.Builder mFocusBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            mFocusBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
            mFocusBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, AFRegions);
            mFocusBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, AERegions);
            if (mZoomRect != null) {
                mFocusBuilder.set(CaptureRequest.SCALER_CROP_REGION, mZoomRect);
            }

            mFocusBuilder.addTarget(mPreViewSurface);

            if (CAMERA_STATE == CameraMode.RECORD_VIDEO) {
                if (mRecordSurface != null) {
                    mFocusBuilder.addTarget(mRecordSurface);
                    setRecordVideoFlashMode(mFocusBuilder);
                }
            }

            mPreviewSession.setRepeatingRequest(mFocusBuilder.build(),
                    null, mBackgroundHandler);

            //      mFocusBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            mFocusBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

            mPreviewSession.capture(mFocusBuilder.build(),
                    null, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开启后台线程
     */
    public void startBackgroundThread() {
        mBackgroundThread = new HandlerThread(CameraHelper.class.getSimpleName());
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * 停止后台进程
     */
    public void stopBackgroundThread() {
        if (mBackgroundThread != null){
            mBackgroundThread.quitSafely();
        }
        try {
            if (mBackgroundThread != null){
                mBackgroundThread.join();
            }
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setTakePhotoFlashMode(CaptureRequest.Builder builder) {
        if (!mFlashSupported){
            return;
        }
        switch (mNowFlashState) {
            case CLOSE:
                builder.set(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON);
                builder.set(CaptureRequest.FLASH_MODE,
                        CaptureRequest.FLASH_MODE_OFF);
                break;
            case OPEN:
                builder.set(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                break;
            case AUTO:
                builder.set(CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                Log.e("mode", "自动闪光灯");
                break;
            default:
                break;
        }
    }

    /**
     * 设置 录像闪光灯
     * @param builder
     */
    private void setRecordVideoFlashMode(CaptureRequest.Builder builder) {
        if (!mFlashSupported){
            return;
        }
        switch (mNowFlashState) {
            case CLOSE:
                builder.set(CaptureRequest.FLASH_MODE,
                        CaptureRequest.FLASH_MODE_OFF);
                break;
            case OPEN:
                builder.set(CaptureRequest.FLASH_MODE,
                        CaptureRequest.FLASH_MODE_TORCH);
                break;
            case AUTO:
                if (mLight < 10.0f) {
                    builder.set(CaptureRequest.FLASH_MODE,
                            CaptureRequest.FLASH_MODE_TORCH);
                }
                break;
        }
    }

    /**
     * 设置光线强度
     */
    public void setLight(float light) {
        this.mLight = light;
    }

    /**
     * 开始录像
     */
    private void startRecordVideo() {
        try {
            closePreviewSession();

            //录像的时候 取最大的分辨率
            mLargest = Collections.max(Arrays.asList(mMap.getOutputSizes(SurfaceTexture.class)),
                    new CompareSizesByArea());

            if (mCameraDevice == null) {
                return;
            }

            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);

            setRecordVideoFlashMode(mPreviewBuilder);

            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mLargest.getWidth(), mLargest.getHeight());
           /* if(mSurface != null)
                mSurface.release();*/
            mPreViewSurface = new Surface(surfaceTexture);

            mPreviewBuilder.addTarget(mPreViewSurface);
            mRecordSurface = mMediaRecorder.getSurface();
            mPreviewBuilder.addTarget(mRecordSurface);
            List<Surface> surfaceList = new ArrayList<>();
            surfaceList.add(mPreViewSurface);
            surfaceList.add(mRecordSurface);
            if (mZoomRect != null){
                //放大的矩阵
                mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, mZoomRect);
            }
            mCameraDevice.createCaptureSession(surfaceList, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    mPreviewSession = session;
                    updatePreview();
                    mIsRecordVideo.set(true);
                    mMediaRecorder.start();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    /**
     * 释放资源
     */
    public void destroy() {
        //mSurface.release();
    }

    private int getOrientation(int rotation) {
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    /**
     * 设置当前相机位置
     *
     * @param rotation 角度
     */
    public void setDeviceRotation(int rotation) {
        this.mDeviceRotation = rotation;
    }


    /**
     * seekBar 滑动监听事件
     */
    @Override
    public void doInProgress1() {
        mTextView.setText("自动");
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_AUTO);
        updatePreview();
    }

    @Override
    public void doInProgress2() {
        mTextView.setText("多云");
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT);
        updatePreview();
    }

    @Override
    public void doInProgress3() {
        mTextView.setText("白天");
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT);
        updatePreview();
    }

    @Override
    public void doInProgress4() {
        mTextView.setText("日光灯");
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT);
        updatePreview();
    }

    @Override
    public void doInProgress5() {
        mTextView.setText("白炽灯");
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT);
        updatePreview();
    }

    @Override
    public void doInProgress6() {
        mTextView.setText("阴影");
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_SHADE);
        updatePreview();
    }

    @Override
    public void doInProgress7() {
        mTextView.setText("黄昏");
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_TWILIGHT);
        updatePreview();
    }

    @Override
    public void doInProgress8() {
        mTextView.setText("暖光");
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT);
        updatePreview();
    }

    @Override
    public void onStopTrackingTouch(int num) {
        switch (num) {
            case 0:
                mTextView.setText("自动");
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_AUTO);
                break;
            case 10:
                mTextView.setText("多云");
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT);
                break;
            case 20:
                mTextView.setText("白天");
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT);
                break;
            case 30:
                mTextView.setText("日光灯");
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT);
                break;
            case 40:
                mTextView.setText("白炽灯");
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT);
                break;
            case 50:
                mTextView.setText("阴影");
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_SHADE);
                break;
            case 60:
                mTextView.setText("黄昏");
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_TWILIGHT);
                break;
            case 70:
                mTextView.setText("暖光");
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT);
                break;
        }
        updatePreview();
        mTextView.startAnimation(mAlphaOutAnimation);
        mTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mTextView.setVisibility(View.VISIBLE);
        mTextView.startAnimation(mAlphaInAnimation);
    }



    /**
     * 异步保存照片
     */
    private class PhotoSaver implements Runnable {

        /**
         * 图片文件
         */
        private File mFile;

        /**
         * 拍照的图片
         */
        private Image mImage;

        public PhotoSaver(Image image, File file) {
            this.mImage = image;
            this.mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
            byte[] buffer = new byte[byteBuffer.remaining()];
            byteBuffer.get(buffer);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(mFile);
                fileOutputStream.write(buffer);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                byteBuffer.clear();
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                resumePreview();
                if (mTakePhotoListener != null){
                    mTakePhotoListener.onTakePhotoFinish(mFile, mPhotoRotation, 0, 0);
                }
            }
        }
    }


    private static final int WAITING_LOCK = 1;
    private int mCameraState = 0;

    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            process(result);
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            process(partialResult);
        }

        private void process(CaptureResult result) {
            switch (mCameraState) {
                case WAITING_LOCK:
                    boolean readyToCapture = true;
                    if (!mNoAFRun) {
                        Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                        if (afState == null) {
                            break;
                        }

                        // If auto-focus has reached locked state, we are ready to capture
                        readyToCapture =
                                (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                                        afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED);
                    }

                    // If we are running on an non-legacy device, we should also wait until
                    // auto-exposure and auto-white-balance have converged as well before
                    // taking a picture.
                    if (!isLegacyLocked()) {
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        Integer awbState = result.get(CaptureResult.CONTROL_AWB_STATE);
                        if (aeState == null || awbState == null) {
                            break;
                        }

                        readyToCapture = readyToCapture &&
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED &&
                                awbState == CaptureResult.CONTROL_AWB_STATE_CONVERGED;
                    }

                    // If we haven't finished the pre-capture sequence but have hit our maximum
                    // wait timeout, too bad! Begin capture anyway.
                    if (!readyToCapture) {
                        readyToCapture = true;
                    }

                    if (readyToCapture) {
                        // Capture once for each user tap of the "Picture" button.
                        capturePhoto();
                        // After this, the camera will go back to the normal state of preview.
                        mCameraState = 0;
                    }
            }
        }
    };

    /**
     * 拍照的有效回调
     */
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            if (mIsCapture) {
                Image image = reader.acquireNextImage();
                new Thread(new PhotoSaver(image, new File(mPhotoPath))).start();
                mIsCapture = false;
            }
        }
    };

    /**
     * 打开摄像头状态回调
     */
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };

    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        return choices[choices.length - 1];
    }


    /**
     * 选择一个适合的预览尺寸，不然有一些机型不支持
     *
     * @param choices
     * @param textureViewWidth
     * @param textureViewHeight
     * @param maxWidth
     * @param maxHeight
     * @param aspectRatio
     * @return
     */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            return choices[0];
        }
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * Return true if the given array contains the given integer.
     *
     * @param modes array to check.
     * @param mode  integer to get for.
     * @return true if the array contains the given integer, otherwise false.
     */
    private static boolean contains(int[] modes, int mode) {
        if (modes == null) {
            return false;
        }
        for (int i : modes) {
            if (i == mode) {
                return true;
            }
        }
        return false;
    }

    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }


    /**
     * 视频录像保存的路径
     *
     * @return
     */
    public String getVideoFilePath() {
//        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsoluteFile();
//        return (dir == null ? "" : (dir.getAbsolutePath() + "/"))
//                + System.currentTimeMillis() + ".mp4";
        return String.valueOf(new File(mContext.getExternalFilesDir(null), System.currentTimeMillis() + ".mp4"));
    }

    /**
     * 图片拍照的路径
     *
     * @return 图片路径
     */
    public String getPhotoFilePath() {
//        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsoluteFile();
//        return (dir == null ? "" : (dir.getAbsolutePath() + "/")) + System.currentTimeMillis() + ".jpeg";
        return String.valueOf(new File(mContext.getExternalFilesDir(null), System.currentTimeMillis() + ".jpg"));
    }

    /**
     * dip 转 px
     * @param context 上下文
     * @param dipValue dip
     * @return px
     */
    public int dip2px(Context context, float dipValue) {
        return (int) (dipValue * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    /**
     * 整数s转 xx:xx:xx
     *
     * @param time time
     * @return time
     */
    public String secToTime(int time) {
        String timeStr;
        int hour;
        int minute;
        int second;
        if (time <= 0){
            return "00:00";
        }
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99){
                    return "99:59:59";
                }
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    /**
     * 格式化 时间
     * @param i 时间
     * @return 格式化时间
     */
    private String unitFormat(int i) {
        String retStr;
        if (i >= 0 && i < 10){
            retStr = "0" + Integer.toString(i);
        }else{
            retStr = "" + i;
        }
        return retStr;
    }

    /**
     * 根据 文件获取uri
     * @param context 上下文对象
     * @param file 文件
     * @return uri
     */
    public Uri getUriFromFile(Context context, File file) {
        if (Build.VERSION.SDK_INT >= 24) {
            return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        } else {
            return Uri.fromFile(file);
        }
    }

    public Range<Integer> getRange1 (){
        return range1;
    }

    /**
     * 设置ae 属性
     */
    public void setAERegions(int ae){
        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, ae);
        updatePreview();
    }

    /**
     * 曝光时间
     */
    private Range<Long> etr;

    public Range<Long> getEtr(){
        return etr;
    }

    /**
     * 设置ae 属性
     */
    public void setAeTime(long aeTime){
        mPreviewBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, aeTime);
        updatePreview();
    }

    /**
     * 设置 mPreviewBuilder 种类
     * @param key CaptureRequest
     * @param value v
     * @param <T>
     */
    public <T> void setCameraBuilerMode(CaptureRequest.Key<T> key, T value) {
        mPreviewBuilder.set(key, value);
        updatePreview();
    }

}
