package camera.cn.cameramaster.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
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
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.yanzhenjie.loading.dialog.LoadingDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import camera.cn.cameramaster.R;
import camera.cn.cameramaster.adapter.EffectAdapter;
import camera.cn.cameramaster.adapter.SenseAdapter;
import camera.cn.cameramaster.base.BaseActivity;
import camera.cn.cameramaster.server.AnyEventType;
import camera.cn.cameramaster.listener.AwbSeekBarChangeListener;
import camera.cn.cameramaster.server.ServerManager;
import camera.cn.cameramaster.util.CompareSizesByArea;
import camera.cn.cameramaster.util.Utils;
import camera.cn.cameramaster.view.AutoFitTextureView;
import camera.cn.cameramaster.view.ShowSurfaceView;
import camera.cn.cameramaster.view.AnimationTextView;
import camera.cn.cameramaster.view.AwbSeekBar;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
import static camera.cn.cameramaster.util.AppConstant.SHOW_AE;
import static camera.cn.cameramaster.util.AppConstant.SHOW_AWB;
import static camera.cn.cameramaster.util.AppConstant.SHOW_EFFECT;
import static camera.cn.cameramaster.util.AppConstant.SHOW_ISO;
import static camera.cn.cameramaster.util.AppConstant.SHOW_SENSE;
import static camera.cn.cameramaster.util.AppConstant.SHOW_ZOOM;

/**
 * google 拍照 demo
 *
 * @author ymc
 * @date 2019年1月28日 17:32:45
 * @url https://github.com/googlesamples/android-Camera2Basic
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class GoogleCameraActivity extends BaseActivity {
    private static final String TAG = "GoogleCameraActivity";
    /**
     * 这里为了测试 相机将画布设置为 1像素，如果 想要看原版效果，
     * 则隐藏 ShowSurfaceView ，将AutoFitTextureView设置充满
     */
    @BindView(R.id.textureView_g)
    AutoFitTextureView mTextureView;
    @BindView(R.id.surfaceView2)
    ShowSurfaceView svShow;
    /**
     * 闪光灯
     */
    @BindView(R.id.iv_flash)
    ImageView ivFlash;
    /**
     * 曝光
     */
    @BindView(R.id.iv_ae)
    ImageView ivAW;
    /**
     * 白平衡
     */
    @BindView(R.id.iv_awb)
    ImageView ivAWB;
    /**
     * 光感度
     */
    @BindView(R.id.iv_iso)
    ImageView ivISO;
    /**
     * 放大倍数
     */
    @BindView(R.id.iv_zoom)
    ImageView ivZoom;
    /**
     * 影响
     */
    @BindView(R.id.iv_effect)
    ImageView ivEffect;
    /**
     * 感应
     */
    @BindView(R.id.iv_sense)
    ImageView ivSense;
    /**
     * 底部切换布局
     */
    @BindView(R.id.layout_bottom)
    RelativeLayout mLayoutBottom;
    /**
     * 拍照布局
     */
    @BindView(R.id.homecamera_bottom_relative2)
    RelativeLayout mLayoutCapture;
    @BindView(R.id.img_camera_g)
    ImageView ivCamereVideo;
    @BindView(R.id.switch_ae)
    Switch switchAe;
    @BindView(R.id.sb_ae)
    SeekBar sbAe;
    @BindView(R.id.layout_ae)
    LinearLayout layoutAe;
    @BindView(R.id.sb_zoom)
    SeekBar sbZoom;
    @BindView(R.id.layout_zoom)
    LinearLayout layoutZoom;
    @BindView(R.id.sb_awb)
    AwbSeekBar sbAwb;
    @BindView(R.id.layout_awb)
    LinearLayout layoutAwb;
    @BindView(R.id.switch_iso)
    Switch switchIso;
    @BindView(R.id.sb_iso)
    SeekBar sbIso;
    @BindView(R.id.layout_iso)
    LinearLayout layoutIso;
    @BindView(R.id.txt_window_txt)
    AnimationTextView txtWindowTxt;
    @BindView(R.id.txt_sb_txt)
    TextView tvSbTxt;
    @BindView(R.id.layout_effect)
    LinearLayout llEffect;
    @BindView(R.id.rv_effect_list)
    RecyclerView evEffectList;
    @BindView(R.id.layout_sense)
    LinearLayout llSense;
    @BindView(R.id.rv_sense_list)
    RecyclerView evSenseList;

    /**
     * visible与invisible之间切换的动画
     */
    private TranslateAnimation mShowAction;

    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    /**
     * 相机预览状态
     */
    private static final int STATE_PREVIEW = 0;
    /**
     * 等待相机锁定
     */
    private static final int STATE_WAITING_LOCK = 1;
    /**
     * 相机状态：等待曝光处于预捕获状态。
     */
    private static final int STATE_WAITING_PRECAPTURE = 2;
    /**
     * 相机状态：等待曝光状态不是预捕获。
     */
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;
    /**
     * 相机状态：已拍摄照片。
     */
    private static final int STATE_PICTURE_TAKEN = 4;
    /**
     * Camera2 API保证的最大预览宽度
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;
    /**
     * Camera2 API保证的最大预览高度
     */
    private static final int MAX_PREVIEW_HEIGHT = 1280;
    /**
     * 当前拍照id
     */
    private String mCameraId;
    /**
     * 相机预览
     */
    private CameraCaptureSession mCaptureSession;
    private CameraDevice mCameraDevice;
    /**
     * 预览
     */
    private Size mPreviewSize;
    /**
     * 相机预览
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    /**
     * 视频对象
     */
    private MediaRecorder mMediaRecorder;
    /**
     * 当前相机状态.
     */
    private int mState = STATE_PREVIEW;

    /**
     * 在关闭相机之前阻止应用程序退出
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * 当前相机设备是否支持Flash
     */
    private boolean mFlashSupported;

    /**
     * 相机传感器的方向
     */
    private int mSensorOrientation;

    private HandlerThread mBackgroundThread;

    private Handler mBackgroundHandler;

    private ImageReader mImageReader;

    private File mFile;

    private File mVideoPath;

    /**
     * true ： 正在录制  /  false ：反之
     */
    private boolean hasVideoOn = false;

    /**
     * 闪光灯类型 0 ：关闭   1： 打开   2：自动
     */
    private int flishType = 0;

    /**
     * 是否显示底部 布局的按钮
     */
    private boolean showAeFlag = false;
    /**
     * 底部 布局集合
     */
    private List<View> mLayoutList = new LinkedList<>();
    /**
     * 文字动画
     */
    private ScaleAnimation mScaleWindowAnimation;
    /**
     * 淡入动画
     */
    private AlphaAnimation mAlphaInAnimation;
    /**
     * 淡出动画
     */
    private AlphaAnimation mAlphaOutAnimation;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * 加载动画
     */
    private LoadingDialog mDialog;

    /**
     * url
     */
    private String mRootUrl;

    /**
     * 服务管理器
     */
    private ServerManager mServerManager;
    /**
     * 是否显示Awb的按钮
     */
    private boolean showAwbFlag = false;
    /**
     * 是否显示 iso 的按钮
     */
    private boolean showIsoFlag = false;
    /**
     * 是否显示 effect 的按钮
     */
    private boolean showEffectFlag = false;
    /**
     * 是否显示 iso 的按钮
     */
    private boolean showSenseFlag = false;
    /**
     * 相机配置
     */
    private CameraCharacteristics characteristics;
    /**
     * 相机 曝光 范围
     */
    private Range<Integer> range1;
    /**
     * 曝光时间
     */
    private Range<Long> etr;
    /**
     * 相机 iso 范围
     */
    private Range<Integer> isoRange;
    /**
     * zoom 是否显示标识
     */
    private boolean showZoomFlag = false;
    /**
     * 相机 管理类
     */
    private CameraManager manager;

    private SenseAdapter sAdapter;
    private EffectAdapter effectAdapter;
    /**
     * 图片保存名称
     */
    private String fileName;
    /**
     * 静态大小
     */
    private Size largest;
    private Surface surface;
    /**
     * 是否正在录制中
     */
    private boolean isRecording = false;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_google_camera;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initView() {
        mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        mShowAction.setDuration(300);

        mScaleWindowAnimation = new ScaleAnimation(2.0f, 1.0f, 2.0f,
                1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mScaleWindowAnimation.setDuration(300);

        mAlphaInAnimation = new AlphaAnimation(0.0f, 1.0f);
        mAlphaInAnimation.setDuration(500);
        mAlphaOutAnimation = new AlphaAnimation(1.0f, 0.0f);
        mAlphaOutAnimation.setDuration(500);
        mMediaRecorder = new MediaRecorder();
        txtWindowTxt.setmAnimation(mScaleWindowAnimation);
        sbAe.setOnSeekBarChangeListener(new CameraSeekBarListener());
        sbZoom.setOnSeekBarChangeListener(new CameraSeekBarListener());
        sbIso.setOnSeekBarChangeListener(new CameraSeekBarListener());

        LinearLayoutManager ms = new LinearLayoutManager(this);
        ms.setOrientation(LinearLayoutManager.HORIZONTAL);
        LinearLayoutManager ms1 = new LinearLayoutManager(this);
        ms1.setOrientation(LinearLayoutManager.HORIZONTAL);
        evSenseList.setLayoutManager(ms);
        evEffectList.setLayoutManager(ms1);
        sAdapter = new SenseAdapter(this);
        effectAdapter = new EffectAdapter(this);
        evSenseList.setAdapter(sAdapter);
        evEffectList.setAdapter(effectAdapter);
        //注册 eventbus
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onEvent(AnyEventType event) {
        lockFocus();
    }

    @Override
    protected void initData() {

        fileName = System.currentTimeMillis() + ".jpg";
        mFile = new File(getExternalFilesDir(null), fileName);
        // 将底部布局 依次添加到 列表中
        mLayoutList.clear();
        mLayoutList.add(mLayoutBottom);
        mLayoutList.add(layoutAe);
        mLayoutList.add(layoutAwb);
        mLayoutList.add(layoutIso);
        mLayoutList.add(layoutZoom);
        mLayoutList.add(llEffect);
        mLayoutList.add(llSense);

        // AndServer run in the service.
        mServerManager = new ServerManager(this);
        mServerManager.register();
        mServerManager.startServer();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        // 存在关联则打开相机，没有则绑定事件
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }


    /**
     * SurfaceTextureListener  监听事件
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    };

    /**
     * CameraDevice 改变状态时候 调用
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            //打开相机时会调用此方法。 我们在这里开始相机预览。
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            finish();
        }

    };

    /**
     * ImageReader 的回调对象。 静止图像已准备好保存。
     */
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image mImage = reader.acquireNextImage();
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            // 其次把文件插入到系统图库
            try {
                MediaStore.Images.Media.insertImage(getContentResolver(),
                        mFile.getAbsolutePath(), fileName, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            // 最后通知图库更新
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(mFile.getPath())));
            Toast.makeText(GoogleCameraActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
//            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
//            Image image = reader.acquireLatestImage();
//            if (image != null) {
//                int imageWidth = image.getWidth();
//                int imageHeight = image.getHeight();
//                byte[] data68 = Camera2Util.getBytesFromImageAsType(image, 2);
//                int rgb[] = Camera2Util.decodeYUV420SP(data68, imageWidth, imageHeight);
//                Bitmap bitmap2 = Bitmap.createBitmap(rgb, 0, imageWidth,
//                        imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
//                Bitmap d65bitmap = BitmapUtils.rotateMyBitmap(BitmapUtils.ImgaeToNegative(bitmap2));
//                svShow.setBitmap(d65bitmap);
//                image.close();
//            }
//            setResult(AppConstant.RESULT_CODE.RESULT_OK);
//            finish();
        }
    };

    /**
     * 处理与jpg文件捕捉的事件监听(预览)
     */
    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    // 预览正常
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
                default:
                    break;
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }

    };

    /**
     * 给定摄像机支持的尺寸 否则选择最小的一个尺寸
     *
     * @param choices           相机支持预期输出的尺寸列表
     * @param textureViewWidth  纹理视图相对于传感器坐标的宽度
     * @param textureViewHeight 纹理视图相对于传感器坐标的高度
     * @param maxWidth          最大宽度
     * @param maxHeight         最大高度
     * @param aspectRatio       纵横比
     * @return size
     */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // 收集至少与预览Surface一样大的支持的分辨率
        List<Size> bigEnough = new ArrayList<>();
        // 收集小于预览Surface的支持的分辨率
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
        // 挑选适合的尺寸
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    /**
     * 设置与摄像头相关的成员变量。
     *
     * @param width  摄像机预览的可用大小宽度
     * @param height 相机预览的可用尺寸高度
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private void setUpCameraOutputs(int width, int height) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                characteristics = manager.getCameraCharacteristics(cameraId);
                // 仅适用后置摄像头
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                //得到相机支持的流配置(包括支持的图片分辨率等),不支持就返回
                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }
                // 曝光增益 范围
                range1 = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
                //获取支持的iso范围
                isoRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
                // 曝光时长
                int[] avails = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
                // 白平衡
                int[] aa = characteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES);
                // 最大白平衡数
                Integer maxAwb = characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AWB);
                //获取曝光时间
                etr = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);

                // 静态图像捕获，选择最大可用大小。
                largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                // w: 720  h :960
//                Size largest = Collections.max(
//                        Arrays.asList(map.getOutputSizes(ImageFormat.YUV_420_888)),
//                        new CompareSizesByArea());
//                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
//                        ImageFormat.YUV_420_888, 2);
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, 1);
                mImageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener, mBackgroundHandler);

                //了解我们是否需要交换尺寸以获得相对于传感器的预览尺寸
                int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }
                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;
                //如果需要颠倒方向
                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }
                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);

                // 将TextureView的宽高比与我们选择的预览大小相匹配。
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                // 检查 远光灯
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException ignored) {
        }
    }

    /**
     * 打开相机
     *
     * @param width  宽度
     * @param height 长度
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2300, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * 打开 BackgroundThread
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * 关闭 BackgroundThread
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建一个新的相机预览
     */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            //将默认缓冲区的大小配置为相机预览的大小。
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            surface = new Surface(texture);
            //使用Surface设置CaptureRequest.Builder
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            //添加这句话 可以在 mImageReader 监听回调中持续获取 预览图片
//            mPreviewRequestBuilder.addTarget(mImageReader.getSurface());
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            if (null == mCameraDevice) {
                                return;
                            }
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // 自动变焦是连续的
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                setAutoFlash(mPreviewRequestBuilder);
                                //禁用所有自动设置
//                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF);
                                //而 ISO 和 Exposure Time 与之相反，仅在 aeMode 关闭时才起作用
                                //只是禁用自动曝光，白平衡继续开启,自己设置iso等值，必须禁用曝光
//                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                                //设置曝光时间 ms单位
                                mPreviewRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, 1000L);
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 1);
                                // 设置 iso 灵敏度
                                mPreviewRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, 200);
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
                                //设置曝光补偿
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 10);
                                // 设置帧 持续时长
                                mPreviewRequestBuilder.set(CaptureRequest.SENSOR_FRAME_DURATION, 1000L);
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mBackgroundHandler);
                                SetListener();
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Matrix 转换配置为 mTextureView
     *
     * @param viewWidth  mTextureView 宽度
     * @param viewHeight mTextureView 高度
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize) {
            return;
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    /**
     * 锁定焦点设置
     */
    public void lockFocus() {
        try {
            // 相机锁定的方法
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            // mCaptureCallback 等待锁定
            mState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.e(TAG, "lockFocus: " + e.getMessage());
        }
    }

    /**
     * 运行预捕获序列以捕获静止图像。 在调用此方法时调用
     */
    private void runPrecaptureSequence() {
        try {
            // 相机触发的方法
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // mCaptureCallback等待设置预捕获序列。
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍摄静止图片。 当我们得到响应时，应该调用此方法
     */
    private void captureStillPicture() {
        try {
            if (null == activity || null == mCameraDevice) {
                return;
            }
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // 使用与预览相同的AE和AF模式。
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            setAutoFlash(captureBuilder);

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            //对于方向为90的设备，我们只需从ORIENTATIONS返回我们的映射
            //对于方向为270的设备，我们需要将JPEG旋转180度
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360);

            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    Log.e(TAG, mFile.toString());
                    unlockFocus();
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解锁焦点 在静止图像捕获序列时调用此方法
     */
    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            setAutoFlash(mPreviewRequestBuilder);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新 Preview
     */
    private void updatePreview() {
        try {
            mPreviewRequest = mPreviewRequestBuilder.build();
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    @OnClick({R.id.iv_back_g, R.id.iv_flash, R.id.iv_ae, R.id.iv_awb, R.id.iv_iso,R.id.img_camera_g,
            R.id.iv_zoom, R.id.iv_change_camera, R.id.iv_effect, R.id.iv_sense, R.id.iv_images})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_camera_g: {
                lockFocus();
                break;
            }
            case R.id.iv_back_g: {
                finish();
                break;
            }
            case R.id.iv_images: {
                // 挑选 相册信息  https://www.jianshu.com/p/498c9d06c193
                break;
            }
            case R.id.iv_zoom: {
                showZoomFlag = !showZoomFlag;
                showLayout(SHOW_ZOOM, showZoomFlag);
                break;
            }
            case R.id.iv_ae: {
                showAeFlag = !showAeFlag;
                showLayout(SHOW_AE, showAeFlag);
                break;
            }
            case R.id.iv_awb:
                showAwbFlag = !showAwbFlag;
                showLayout(SHOW_AWB, showAwbFlag);
                break;
            case R.id.iv_iso:
                showIsoFlag = !showIsoFlag;
                showLayout(SHOW_ISO, showIsoFlag);
                break;
            case R.id.iv_effect:
                showEffectFlag = !showEffectFlag;
                showLayout(SHOW_EFFECT, showEffectFlag);
                break;
            case R.id.iv_sense:
                showSenseFlag = !showSenseFlag;
                showLayout(SHOW_SENSE, showSenseFlag);
                break;
            case R.id.iv_change_camera: {
                if ("1".equals(mCameraId)) {
                    mCameraId = "0";
                } else if ("0".equals(mCameraId)) {
                    mCameraId = "1";
                } else {
                    mCameraId = "0";
                }
                //关闭相机再开启另外个摄像头
                if (mCaptureSession != null) {
                    mCaptureSession.close();
                    mCaptureSession = null;
                }
                if (mCameraDevice != null) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                }
                try {
                    manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.iv_flash: {
                if (!mFlashSupported) {
                    Log.e(TAG, "该设备暂不支持 闪光灯");
                    return;
                }
                switch (flishType) {
                    case 0:
                        // 从关闭切换到打开
                        flishType = 1;
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                CameraMetadata.CONTROL_AE_MODE_ON);
                        mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE,
                                CameraMetadata.FLASH_MODE_SINGLE);
                        ivFlash.setImageResource(R.mipmap.btn_flash_on_normal);
                        break;
                    case 1:
                        //从打开切换到 自动
                        flishType = 2;
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        ivFlash.setImageResource(R.mipmap.btn_flash_auto_normal);
                        break;
                    case 2:
                        //自动切换到关闭
                        flishType = 0;
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                CameraMetadata.CONTROL_AE_MODE_ON);
                        mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE,
                                CameraMetadata.FLASH_MODE_OFF);
                        ivFlash.setImageResource(R.mipmap.btn_flash_off_normal);
                        break;
                    default:
                        break;
                }
                updatePreview();
            }
            default:
                break;
        }
    }

    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (mFlashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
    }

    /**
     * 显示和隐藏控件
     *
     * @param showWhat
     * @param showOrNot
     */
    private void showLayout(int showWhat, boolean showOrNot) {
        View v = mLayoutList.get(showWhat);
        if (showOrNot) {
            //全部隐藏但是AF/AE的显示出来
            for (int i = 0; i < mLayoutBottom.getChildCount(); i++) {
                if (mLayoutBottom.getChildAt(i).getVisibility() == View.VISIBLE) {
                    mLayoutBottom.getChildAt(i).setVisibility(View.INVISIBLE);
                }
            }
            v.startAnimation(mShowAction);
            v.setVisibility(View.VISIBLE);
        } else {
            //全部隐藏但是capture的显示出来
            for (int i = 0; i < mLayoutBottom.getChildCount(); i++) {
                if (mLayoutBottom.getChildAt(i).getVisibility() == View.VISIBLE) {
                    mLayoutBottom.getChildAt(i).setVisibility(View.INVISIBLE);
                }
            }
            mLayoutCapture.startAnimation(mShowAction);
            mLayoutCapture.setVisibility(View.VISIBLE);
        }
    }

    /**
     * switch 修改事件
     *
     * @param buttonView view
     * @param isChecked  boolean
     */
    @OnCheckedChanged({R.id.switch_ae, R.id.switch_iso})
    public void CameraOnCheckChangedListener(CompoundButton buttonView, boolean isChecked) {
        //翻转的时候mPreviewRequestBuilder变为了null,会挂掉
        if (mPreviewRequestBuilder == null) {
            return;
        }
        switch (buttonView.getId()) {
            case R.id.switch_ae: {
                switchIso.setChecked(isChecked);
                if (isChecked) {
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                    layoutIso.getChildAt(1).setEnabled(false);
                } else {
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
                    layoutIso.getChildAt(1).setEnabled(true);
                }
                break;
            }
            case R.id.switch_iso: {
                switchAe.setChecked(isChecked);
                if (isChecked) {
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                    layoutIso.getChildAt(1).setEnabled(false);
                } else {
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
                    layoutIso.getChildAt(1).setEnabled(true);
                }
                break;
            }
            default:
                break;
        }
        updatePreview();
    }

    /**
     * seekbar 滑动监听事件
     */
    private class CameraSeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            switch (seekBar.getId()) {
                case R.id.sb_ae: {
                    if (switchAe.isChecked()) {
                        // 曝光增益
                        if (range1 == null) {
                            break;
                        }
                        Log.e(TAG, "曝光增益范围：" + range1.toString());
                        int maxmax = range1.getUpper();
                        int minmin = range1.getLower();
                        int all = maxmax - minmin;
                        int time = 100 / all;
                        int ae = ((progress / time) - maxmax) > maxmax ? maxmax :
                                ((progress / time) - maxmax) < minmin ? minmin : ((progress / time) - maxmax);
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, ae);
                        tvSbTxt.setText("曝光增益：" + ae);
                    } else {
                        // 曝光时间
                        if (etr == null) {
                            tvSbTxt.setText("获取曝光时间失败");
                            break;
                        }
                        Log.e(TAG, "曝光时间范围：" + etr.toString());
                        long max = etr.getUpper();
                        long min = etr.getLower();
                        long ae = ((progress * (max - min)) / 100 + min);
                        mPreviewRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, ae);
                        tvSbTxt.setText("曝光时间：" + ae);
                    }
                    break;
                }
                case R.id.sb_iso: {
                    if (isoRange == null) {
                        tvSbTxt.setText("获取iso失败");
                        break;
                    }
                    int max1 = isoRange.getUpper();
                    int min1 = isoRange.getLower();
                    int iso = ((progress * (max1 - min1)) / 100 + min1);
                    mPreviewRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, iso);
                    tvSbTxt.setText("灵敏度：" + iso);
                    break;
                }
                case R.id.sb_zoom: {
                    Rect rect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                    int radio = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM).intValue() / 2;
                    int realRadio = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM).intValue();
                    int centerX = rect.centerX();
                    int centerY = rect.centerY();
                    int minMidth = (rect.right - ((progress * centerX) / 100 / radio) - 1) - ((progress * centerX / radio) / 100 + 8);
                    int minHeight = (rect.bottom - ((progress * centerY) / 100 / radio) - 1) - ((progress * centerY / radio) / 100 + 16);
                    if (minMidth < rect.right / realRadio || minHeight < rect.bottom / realRadio) {
                        Log.e("sb_zoom", "sb_zoomsb_zoomsb_zoom");
                        return;
                    }
//                    Rect newRect = new Rect(20, 20, rect.right - ((i * centerX) / 100 / radio) - 1, rect.bottom - ((i * centerY) / 100 / radio) - 1);
                    Rect newRect = new Rect((progress * centerX / radio) / 100 + 40,
                            (progress * centerY / radio) / 100 + 40,
                            rect.right - ((progress * centerX) / 100 / radio) - 1,
                            rect.bottom - ((progress * centerY) / 100 / radio) - 1);
                    Log.i("sb_zoom", "left--->" + ((progress * centerX / radio) / 100 + 8)
                            + ",,,top--->" + ((progress * centerY / radio) / 100 + 16) + ",,,right--->"
                            + (rect.right - ((progress * centerX) / 100 / radio) - 1) + ",,,bottom--->"
                            + (rect.bottom - ((progress * centerY) / 100 / radio) - 1));
                    mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, newRect);
                    tvSbTxt.setText("放大：" + progress + "%");
                    break;
                }
                default:
                    break;
            }
            updatePreview();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            tvSbTxt.setVisibility(View.VISIBLE);
            tvSbTxt.startAnimation(mAlphaInAnimation);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            tvSbTxt.startAnimation(mAlphaOutAnimation);
            tvSbTxt.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 在系统相机 配置分配完整后 添加绑定事件
     */
    private void SetListener() {
        // 配置 白平衡滑动事件
        sbAwb.setmOnAwbSeekBarChangeListener(new AwbSeekBarChangeListener(GoogleCameraActivity.this,
                tvSbTxt, mPreviewRequestBuilder, mCaptureSession, mBackgroundHandler, mCaptureCallback));

        sAdapter.setSenseOnItemClickListener(new SenseAdapter.SenseOnItemClickListener() {
            @Override
            public void itemOnClick(int position) {
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_USE_SCENE_MODE);
                switch (position) {
                    case 0:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_DISABLED);
                        break;
                    case 1:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_FACE_PRIORITY);
                        break;
                    case 2:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_ACTION);
                        break;
                    case 3:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_PORTRAIT);
                        break;
                    case 4:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_LANDSCAPE);
                        break;
                    case 5:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_NIGHT);
                        break;
                    case 6:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_NIGHT_PORTRAIT);
                        break;
                    case 7:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_THEATRE);
                        break;
                    case 8:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_BEACH);
                        break;
                    case 9:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_SNOW);
                        break;
                    case 10:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_SUNSET);
                        break;
                    case 11:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_STEADYPHOTO);
                        break;
                    case 12:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_FIREWORKS);
                        break;
                    case 13:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_SPORTS);
                        break;
                    case 14:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_PARTY);
                        break;
                    case 15:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_CANDLELIGHT);
                        break;
                    case 16:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_BARCODE);
                        break;
                    default:
                        break;
                }
                updatePreview();
            }
        });

        effectAdapter.setEffectOnItemClickListener(new EffectAdapter.EffectOnItemClickListener() {
            @Override
            public void itemOnClick(int position) {
                switch (position) {
                    case 0:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_AQUA);
                        break;
                    case 1:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_BLACKBOARD);
                        break;
                    case 2:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_MONO);
                        break;
                    case 3:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_NEGATIVE);
                        break;
                    case 4:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_POSTERIZE);
                        break;
                    case 5:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_SEPIA);
                        break;
                    case 6:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_SOLARIZE);
                        break;
                    case 7:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_WHITEBOARD);
                        break;
                    case 8:
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_OFF);
                        break;
                    default:
                        break;
                }
                updatePreview();
            }
        });
    }

    /**
     * 录制预览
     */
    private void startRecordingVideo() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            setUpMediaRecorder();
            SurfaceTexture mtexture = mTextureView.getSurfaceTexture();
            assert mtexture != null;
            mtexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            Surface previewSurface = new Surface(mtexture);
            surfaces.add(previewSurface);
            mPreviewRequestBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewRequestBuilder.addTarget(recorderSurface);

            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //设置反复捕获数据的请求，这样预览界面就会一直有数据显示
                    mCaptureSession = cameraCaptureSession;
                    updatePreview();
                    Log.e(TAG, "onConfigured: " + Thread.currentThread().getName());
                    // Start recording
                    mMediaRecorder.start();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 录制结束
     */
    private void stopRecordingVideo() {
        // Stop recording
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        Toast.makeText(this, "Video saved: " + mVideoPath.getAbsolutePath(),
                Toast.LENGTH_SHORT).show();
        createCameraPreviewSession();
    }

    /**
     * 设置 MediaRecorder
     *
     * @throws IOException
     */
    private void setUpMediaRecorder() throws IOException {
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mVideoPath = Utils.getOutputMediaFile(this,MEDIA_TYPE_VIDEO);
        mMediaRecorder.setOutputFile(mVideoPath.getAbsolutePath());
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(largest.getWidth(), largest.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (mSensorOrientation) {
            case 90:
                mMediaRecorder.setOrientationHint(ORIENTATIONS.get(rotation));
                break;
            case 270:
                mMediaRecorder.setOrientationHint(ORIENTATIONS.get(rotation));
                break;
            default:
                break;
        }
        mMediaRecorder.prepare();
    }


    /**
     * Start notify.
     */
    public void onServerStart(String ip) {
        closeDialog();
        if (!TextUtils.isEmpty(ip)) {
            List<String> addressList = new LinkedList<>();
            mRootUrl = "http://" + ip + ":8080/";
            addressList.add(mRootUrl);
            addressList.add("http://" + ip + ":8080/login.html");
            Log.e(TAG, "onServerStart: " + TextUtils.join("\n", addressList));
        } else {
            mRootUrl = null;
            Log.e(TAG, "onServerStart: " + getString(R.string.server_ip_error));
        }
    }

    /**
     * Error notify.
     */
    public void onServerError(String message) {
        closeDialog();
        mRootUrl = null;
        Log.e(TAG, "onServerError: " + message);
    }

    /**
     * Stop notify.
     */
    public void onServerStop() {
        closeDialog();
        mRootUrl = null;
    }

    private void showDialog() {
        if (mDialog == null) {
            mDialog = new LoadingDialog(this);
        }
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    private void closeDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        mServerManager.unRegister();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private static final String LOGIN_ATTRIBUTE = "USER.LOGIN.SIGN";

    public CaptureRequest.Builder getBuilder() {
        return mPreviewRequestBuilder;
    }

    public CameraCaptureSession getSession() {
        return mCaptureSession;
    }

}
