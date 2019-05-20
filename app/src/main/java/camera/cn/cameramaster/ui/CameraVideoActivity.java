package camera.cn.cameramaster.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import camera.cn.cameramaster.R;
import camera.cn.cameramaster.adapter.EffectAdapter;
import camera.cn.cameramaster.adapter.MenuAdapter;
import camera.cn.cameramaster.adapter.SenseAdapter;
import camera.cn.cameramaster.base.BaseActivity;
import camera.cn.cameramaster.view.AwbSeekBarChangeListener;
import camera.cn.cameramaster.util.AppConstant;
import camera.cn.cameramaster.util.cameravideo.CameraHelper;
import camera.cn.cameramaster.util.cameravideo.ICamera2;
import camera.cn.cameramaster.util.cameravideo.IVideoControl;
import camera.cn.cameramaster.util.cameravideo.VideoPlayer;
import camera.cn.cameramaster.view.AutoFitTextureView;
import camera.cn.cameramaster.view.AutoLocateHorizontalView;
import camera.cn.cameramaster.view.AwbSeekBar;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import static camera.cn.cameramaster.util.AppConstant.SHOW_AE;
import static camera.cn.cameramaster.util.AppConstant.SHOW_AWB;
import static camera.cn.cameramaster.util.AppConstant.SHOW_EFFECT;
import static camera.cn.cameramaster.util.AppConstant.SHOW_SENSE;

/**
 * 拍照 视频
 *
 * @author ymc
 * @date 2019年5月7日 13:49:17
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraVideoActivity extends BaseActivity implements IVideoControl.PlaySeekTimeListener,
        IVideoControl.PlayStateListener, ICamera2.TakePhotoListener,
        SensorEventListener, ICamera2.CameraReady, AutoLocateHorizontalView.OnSelectedPositionChangedListener {

    private static final String TAG = "CameraVideoActivity";

    /**
     * 当前的显示面板状态
     */
    public int TEXTURE_STATE = AppConstant.TEXTURE_PREVIEW_STATE;

    @BindView(R.id.video_photo)
    ImageView videoPhoto;
    @BindView(R.id.video_texture)
    AutoFitTextureView videoTexture;
    @BindView(R.id.video_record_seek_bar)
    SeekBar videoRecordSeekBar;
    @BindView(R.id.video_time)
    TextView videoTime;
    @BindView(R.id.video_switch_flash)
    ImageView videoSwitchFlash;
    @BindView(R.id.video_switch_camera)
    ImageView videoSwitchCamera;
    @BindView(R.id.video_play)
    ImageButton videoPlay;
    @BindView(R.id.video_delete)
    ImageButton videoDelete;
    @BindView(R.id.video_menu)
    AutoLocateHorizontalView videoMenu;
    @BindView(R.id.video_record)
    ImageButton videoRecord;
    @BindView(R.id.video_save)
    ImageButton videoSave;
    @BindView(R.id.video_mine_play)
    ImageButton videoMinePlay;
    @BindView(R.id.video_seek_bar)
    SeekBar videoSeekBar;
    @BindView(R.id.video_seek_time)
    TextView videoSeekTime;
    @BindView(R.id.video_hint_text)
    TextView videoHintText;
    /**
     * 焦点框
     */
    @BindView(R.id.video_fouces)
    ImageView videoFouces;
    /**
     * zoom 缩小
     */
    @BindView(R.id.video_minus)
    ImageView videoMinus;
    /**
     * scale zoom 条
     */
    @BindView(R.id.video_scale)
    SeekBar videoScale;
    /**
     *  zoom 放大
     */
    @BindView(R.id.video_add)
    ImageView videoAdd;
    @BindView(R.id.video_scale_bar_layout)
    RelativeLayout videoScaleBarLayout;
    /**
     * 底部切换布局
     */
    @BindView(R.id.layout_bottom)
    RelativeLayout mLayoutBottom;

    /**
     * ae 修改布局
     */
    @BindView(R.id.layout_ae)
    LinearLayout layoutAe;
    /**
     * 特效
     */
    @BindView(R.id.layout_effect)
    LinearLayout llEffect;
    @BindView(R.id.rv_effect_list)
    RecyclerView evEffectList;
    @BindView(R.id.layout_sense)
    LinearLayout llSense;
    @BindView(R.id.rv_sense_list)
    RecyclerView evSenseList;
    @BindView(R.id.sb_ae)
    SeekBar sbAe;

    @BindView(R.id.sb_awb)
    AwbSeekBar sbAwb;

    /**
     * awb
     */
    @BindView(R.id.layout_awb)
    LinearLayout layoutAwb;

    @BindView(R.id.rl_camera)
    RelativeLayout rlCamera;

    @BindView(R.id.switch_ae)
    Switch switchAe;

    @BindView(R.id.txt_sb_txt)
    TextView tvSbTxt;

    /**
     * 视频播放器
     */
    private VideoPlayer mVideoPlayer;
    /**
     * 相机模式
     */
    private int MODE;
    /**
     * 视频保存路径
     */
    private String mVideoPath;
    /**
     * 拍照工具类
     */
    private CameraHelper cameraHelper;
    /**
     * 菜单适配器
     */
    private MenuAdapter mMenuAdapter;
    /**
     * 当前拍照模式
     */
    private int NOW_MODE;
    /**
     * 触摸事件处理类
     */
    private CameraTouch mCameraTouch;
    /**
     * 放大缩小seekBar 是否可以隐藏
     */
    private boolean isCanHind;
    /**
     * 手动对焦 动画
     */
    private FoucesAnimation mFoucesAnimation;
    /**
     * 前 后 摄像头标识
     */
    private ICamera2.CameraType mNowCameraType = ICamera2.CameraType.BACK;
    /**
     * 单点标识
     */
    private boolean hasRecordClick = false;
    /**
     * 是否在 录制中
     */
    private boolean hasRecording = false;
    /**
     * 图片路径
     */
    private String mCameraPath;
    /**
     * 倒计时
     */
    private Disposable mDisposable;
    /**
     * 是否正在播放 标识
     */
    private boolean hasPlaying = false;
    /**
     * 是否有拍照权限
     */
    private boolean isNoPremissionPause;

    /**
     * 定义文字动画
     */
    private AlphaAnimation mAlphaInAnimation;
    private AlphaAnimation mAlphaOutAnimation;
    private SenseAdapter sAdapter;
    private EffectAdapter effectAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_camera_video;
    }

    @Override
    protected void initView() {
        // 将底部布局 依次添加到 列表中
        mLayoutList.clear();
        mLayoutList.add(mLayoutBottom);
        mLayoutList.add(layoutAe);
        mLayoutList.add(layoutAwb);
        mLayoutList.add(llEffect);
        mLayoutList.add(llSense);
        // 初始化 切换动画
        mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        mShowAction.setDuration(100);

        mVideoPlayer = new VideoPlayer();
        //设置时间戳回调
        mVideoPlayer.setPlaySeekTimeListener(this);
        MODE = getIntent().getIntExtra("mode", AppConstant.CAMERA_MODE);

        if (MODE == AppConstant.CAMERA_MODE) {
            //摄像头模式
            initCameraMode();
        } else if (MODE == AppConstant.VIDEO_MODE) {
            //视频播放模式
            mVideoPath = getIntent().getStringExtra("videoPath");
            initVideoMode();
        }
        mFoucesAnimation = new FoucesAnimation();
        // 淡入动画
        mAlphaInAnimation = new AlphaAnimation(0.0f, 1.0f);
        mAlphaInAnimation.setDuration(500);
        // 淡出动画
        mAlphaOutAnimation = new AlphaAnimation(1.0f, 0.0f);
        mAlphaOutAnimation.setDuration(500);

        sbAwb.setmOnAwbSeekBarChangeListener(cameraHelper);

        LinearLayoutManager ms = new LinearLayoutManager(this);
        ms.setOrientation(LinearLayoutManager.HORIZONTAL);
        LinearLayoutManager ms1 = new LinearLayoutManager(this);
        ms1.setOrientation(LinearLayoutManager.HORIZONTAL);
        evSenseList.setLayoutManager(ms);
        evEffectList.setLayoutManager(ms1);
        sAdapter = new SenseAdapter(this, AppConstant.senseArr);
        effectAdapter = new EffectAdapter(this,AppConstant.effectArr);
        evSenseList.setAdapter(sAdapter);
        evEffectList.setAdapter(effectAdapter);
        // rv 点击事件
        initListener();
    }

    @Override
    protected void initData() {
        mCameraPath = cameraHelper.getPhotoFilePath();
        mVideoPath = cameraHelper.getVideoFilePath();

        sbAe.setOnSeekBarChangeListener(new CameraSeekBarListener());
    }

    /**
     * 初始化 录像
     */
    private void initVideoMode() {
        hindMenu();
        hindSwitchCamera();
        hindVideoRecordSeekBar();
        mVideoPlayer.setPlayStateListener(this);
        videoRecord.setVisibility(View.GONE);
        videoHintText.setVisibility(View.GONE);
        videoTexture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {     //单机屏幕显示出控件
                if (videoMinePlay.getVisibility() == View.VISIBLE) {
                    hindPlayView();
                } else {
                    showPlayView();
                    videoTexture.postDelayed(mHindViewRunnable, 3000);
                }
            }
        });

        videoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int progress;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    this.progress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //触摸进度条取消几秒后隐藏的事件
                videoTexture.removeCallbacks(mHindViewRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mVideoPlayer.seekTo(progress);
                videoTexture.postDelayed(mHindViewRunnable, 3000);
            }
        });
    }

    /**
     * 初始化 拍照
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initCameraMode() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                        PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            isNoPremissionPause = true;
        }
        initCamera(mNowCameraType);
        cameraHelper = new CameraHelper(this);
        cameraHelper.setTakePhotoListener(this);
        cameraHelper.setCameraReady(this);
        cameraHelper.setShowTextView(tvSbTxt);
        mVideoPlayer.setLoopPlay(true);
        List<String> menus = new ArrayList<>();
        menus.add("拍照");
        menus.add("录像");
        menus.add("曝光");
        menus.add("白平衡");
        menus.add("效果");
        menus.add("感觉");

        mMenuAdapter = new MenuAdapter(this, menus, videoMenu);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        videoMenu.setLayoutManager(linearLayoutManager);
        videoMenu.setAdapter(mMenuAdapter);
        videoMenu.setOnSelectedPositionChangedListener(this);

        mCameraTouch = new CameraTouch();

        videoMenu.setOnTouchListener(new HorizontalViewTouchListener());
        registerSensor();
        initScaleSeekbar();
    }

    /**
     * 初始化摄像头
     *
     * @param cameraType
     */
    private void initCamera(ICamera2.CameraType cameraType) {
        if (cameraHelper == null) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        cameraHelper.setTextureView(videoTexture);
        cameraHelper.openCamera(cameraType);
    }


    /**
     * 初始化 scale seekBar
     */
    private void initScaleSeekbar() {
        videoScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float scale = (float) progress / (float) seekBar.getMax() * cameraHelper.getMaxZoom();
                    cameraHelper.cameraZoom(scale);
                    mCameraTouch.setScale(scale);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                removeSeekBarRunnable();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBarDelayedHind();
            }
        });
    }

    /**
     * 横向列表 touch事件 (拍照预览 缩放)
     */
    private class HorizontalViewTouchListener implements View.OnTouchListener {

        private long mClickOn;
        private float mLastX;
        private float mLastY;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (motionEvent.getPointerCount() == 1) {
                        mClickOn = System.currentTimeMillis();
                        mLastX = motionEvent.getX();
                        mLastY = motionEvent.getY();
                    }
                    break;
                // 用户两指按下事件
                case MotionEvent.ACTION_POINTER_DOWN:
                    mCameraTouch.onScaleStart(motionEvent);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (motionEvent.getPointerCount() == 2) {
                        mCameraTouch.onScale(motionEvent);
                        return true;
                    } else {
                        float x = motionEvent.getX() - mLastX;
                        float y = motionEvent.getY() - mLastY;
                        if (Math.abs(x) >= 10 || Math.abs(y) >= 10) {
                            mClickOn = 0;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (motionEvent.getPointerCount() == 1) {
                        if ((System.currentTimeMillis() - mClickOn) < 500) {
                            moveFouces((int) motionEvent.getX(), (int) motionEvent.getY());
                        }
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mCameraTouch.onScaleEnd(motionEvent);
                    return true;
                default:
                    break;
            }
            return false;
        }
    }

    /**
     * 点击事件
     *
     * @param view view
     */
    @OnClick({R.id.video_switch_camera, R.id.video_switch_flash})
    public void cameraOnClickListener(View view) {
        switch (view.getId()) {
            // 切换摄像头状态
            case R.id.video_switch_camera:
                if (mNowCameraType == ICamera2.CameraType.FRONT) {
                    cameraHelper.switchCamera(ICamera2.CameraType.BACK);
                    mNowCameraType = ICamera2.CameraType.BACK;
                } else {
                    cameraHelper.switchCamera(ICamera2.CameraType.FRONT);
                    mNowCameraType = ICamera2.CameraType.FRONT;
                }
                mCameraTouch.resetScale();
                break;
            case R.id.video_switch_flash:
                Object o = videoSwitchFlash.getTag();
                if (o == null || ((int) o) == 0) {
                    videoSwitchFlash.setBackgroundResource(R.mipmap.flash_auto);
                    videoSwitchFlash.setTag(1);
                    cameraHelper.flashSwitchState(ICamera2.FlashState.AUTO);
                } else if (((int) o) == 1) {
                    videoSwitchFlash.setBackgroundResource(R.mipmap.flash_open);
                    videoSwitchFlash.setTag(2);
                    cameraHelper.flashSwitchState(ICamera2.FlashState.OPEN);
                } else {
                    videoSwitchFlash.setBackgroundResource(R.mipmap.flash_close);
                    videoSwitchFlash.setTag(0);
                    cameraHelper.flashSwitchState(ICamera2.FlashState.CLOSE);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 传感器继承方法 重力发生改变
     * 根据重力方向 动态旋转拍照图片角度(暂时关闭该方法)
     *
     * 使用以下方法
     * int rotation = getWindowManager().getDefaultDisplay().getRotation();
     * @param event event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
//            Log.e(TAG, "onSensorChanged: x: " + x +"   y: "+y +"  z : "+z);
            if (z > 55.0f) {
                //向左横屏
            } else if (z < -55.0f) {
                //向右横屏
            } else if (y > 60.0f) {
                //是倒竖屏
            } else {
                //正竖屏
            }
        }
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float light = event.values[0];
            cameraHelper.setLight(light);
        }
    }

    /**
     * 注册陀螺仪传感器
     */
    private void registerSensor() {
        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        Sensor mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (mSensor == null) {
            return;
        }
        mSensorManager.registerListener(this, mSensor, Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this, mLightSensor, Sensor.TYPE_LIGHT);
    }

    /**
     * 当已注册传感器的精度发生变化时调用
     *
     * @param sensor   sensor
     * @param accuracy 传感器的新精度
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSeekTime(int allTime, final int time) {
        if (videoSeekBar ==null || videoSeekBar.getVisibility() != View.VISIBLE){
            return;
        }
        if (videoSeekBar.getMax() != allTime){
            videoSeekBar.setMax(allTime);
        }
        videoSeekBar.setProgress(time);
        videoSeekTime.post(new Runnable() {
            @Override
            public void run() {
                float t = (float) time / 1000.0f;
                videoSeekTime.setText(cameraHelper.secToTime(Math.round(t)));
            }
        });
    }

    @Override
    public void onStartListener(int width, int height) {
        videoTexture.setVideoAspectRatio(width, height);
        videoMinePlay.setImageResource(R.mipmap.ic_pause);
        videoPlay.setImageResource(R.mipmap.ic_pause);
    }

    @Override
    public void onCompletionListener() {
        hasPlaying = false;
        videoMinePlay.setImageResource(R.mipmap.ic_play);
        videoPlay.setImageResource(R.mipmap.ic_play);
        videoPlay.setVisibility(View.VISIBLE);
    }

    /**
     * 拍照完成回调
     *
     * @param file          文件
     * @param photoRotation 角度
     * @param width         宽度
     * @param height        高度
     */
    @Override
    public void onTakePhotoFinish(final File file, int photoRotation, int width, int height) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hindSwitchCamera();
                hindMenu();
                showRecordEndView();
                videoSwitchFlash.setVisibility(View.GONE);
                videoRecord.setVisibility(View.GONE);
                videoHintText.setVisibility(View.GONE);
                TEXTURE_STATE = AppConstant.TEXTURE_PHOTO_STATE;
                videoTexture.setVisibility(View.GONE);
                videoPhoto.setImageURI(cameraHelper.getUriFromFile(CameraVideoActivity.this, file));
                videoPhoto.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * 相机准备完毕
     */
    @Override
    public void onCameraReady() {
        videoRecord.setClickable(true);
    }

    /**
     * 横向菜单列表 修改点击事件
     *
     * @param pos
     */
    @Override
    public void selectedPositionChanged(int pos) {
        Log.e(TAG, "selectedPositionChanged: "+pos);
        switch (pos){
            case 0:{
                showLayout(0, false);
                NOW_MODE = AppConstant.VIDEO_TAKE_PHOTO;
                cameraHelper.setCameraState(ICamera2.CameraMode.TAKE_PHOTO);
                videoHintText.setText("点击拍照");
                break;
            }
            case 1:{
                showLayout(0, false);
                NOW_MODE = AppConstant.VIDEO_RECORD_MODE;
                cameraHelper.setCameraState(ICamera2.CameraMode.RECORD_VIDEO);
                videoHintText.setText("点击录像");
                break;
            }
            // 调整 曝光
            case 2:{
                showLayout(SHOW_AE, true);
                break;
            }
            // 调整 白平衡
            case 3:{
                showLayout(SHOW_AWB, true);
                break;
            }
            // 调整 效果
            case 4:{
                showLayout(3, true);
                break;
            }
            // 调整 感觉
            case 5:{
                showLayout(4, true);
                break;
            }
        }
    }

    /**
     * 录像时长倒计时
     */
    @SuppressLint("SetTextI18n")
    private void recordCountDown() {
        videoTime.setVisibility(View.VISIBLE);
        videoRecordSeekBar.setVisibility(View.VISIBLE);
        final int count = 10;
        mDisposable = Observable.interval(1, 1, TimeUnit.SECONDS)
                .take(count + 1)
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(Long aLong) {
                        return count - aLong;
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) {
                        long time = 11 - aLong;
                        if (time < 10) {
                            videoTime.setText("0:0" + String.valueOf(time));
                        } else {
                            videoTime.setText("0:" + String.valueOf(time));
                        }
                        videoRecordSeekBar.setProgress((int) time);
                        if (time == AppConstant.VIDEO_MAX_TIME) {
                            videoTime.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    recordVideoOrTakePhoto();
                                    hindVideoRecordSeekBar();
                                }
                            }, 300);
                        }
                    }
                });
    }

    /**
     * 拍照或者录像
     */
    @OnClick(R.id.video_record)
    public void recordVideoOrTakePhoto() {
        if (hasRecordClick) {
            return;
        }
        hasRecordClick = true;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission
                (this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "cameraOnClickListener: 动态权限获取失败...");
            return;
        }
        //拍照
        if (NOW_MODE == AppConstant.VIDEO_TAKE_PHOTO && mCameraPath!=null) {
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            cameraHelper.setDeviceRotation(rotation);
            cameraHelper.takePhone(mCameraPath, ICamera2.MediaType.JPEG);
        }
        //录制视频
        if (NOW_MODE == AppConstant.VIDEO_RECORD_MODE) {
            if (!hasRecording) {
                // 暂停录像
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission
                        (this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                mVideoPath = cameraHelper.getVideoFilePath();
                hasRecording = cameraHelper.startVideoRecord(mVideoPath, MediaRecorder.OutputFormat.MPEG_4);
                if (hasRecording) {
                    videoRecord.setImageResource(R.mipmap.ic_recording);
                    hindSwitchCamera();
                    recordCountDown();
                    hindMenu();
                    //      mVideoHintText.setVisibility(View.GONE);
                    videoHintText.setText("点击停止");
                    videoSwitchFlash.setVisibility(View.GONE);
                    TEXTURE_STATE = AppConstant.TEXTURE_RECORD_STATE;
                }
            } else {
                // 开始录像
                if (mDisposable != null && !mDisposable.isDisposed()){
                    mDisposable.dispose();
                }
                mDisposable = null;
                videoSeekTime.setVisibility(View.GONE);
                hasRecording = false;
                cameraHelper.stopVideoRecord();

                videoRecord.setImageResource(R.drawable.ic_camera);
                videoRecord.setVisibility(View.GONE);
                videoHintText.setVisibility(View.GONE);
                showRecordEndView();
                hindVideoRecordSeekBar();
                playVideo();
            }
        }
        hasRecordClick = false;
    }

    /**
     * 返回 取消拍照或者 录像
     */
    @OnClick(R.id.video_delete)
    public void deleteVideoOrPicture() {
        if (TEXTURE_STATE == AppConstant.TEXTURE_PLAY_STATE) {
            mVideoPlayer.stop();
            cameraHelper.startBackgroundThread();
            cameraHelper.openCamera(mNowCameraType);
            mCameraTouch.resetScale();  //重新打开摄像头重置一下放大倍数
            File file = new File(mVideoPath);
            if (file.exists()){
                file.delete();
            }
            videoHintText.setText("点击录像");
        } else if (TEXTURE_STATE == AppConstant.TEXTURE_PHOTO_STATE) {
            File file = new File(mCameraPath);
            if (file.exists()){
                file.delete();
            }
            cameraHelper.resumePreview();
            videoTexture.setVisibility(View.VISIBLE);
            videoPhoto.setVisibility(View.GONE);
            videoHintText.setText("点击拍照");
        }
        initData();
        TEXTURE_STATE = AppConstant.TEXTURE_PREVIEW_STATE;
        hindRecordEndView();
        videoSwitchCamera.setVisibility(View.VISIBLE);
        videoMenu.setVisibility(View.VISIBLE);
        videoRecord.setVisibility(View.VISIBLE);
        videoTime.setVisibility(View.GONE);
        videoTime.setText("0:00");
        videoHintText.setVisibility(View.VISIBLE);
        videoSwitchFlash.setVisibility(View.VISIBLE);
    }

    /**
     * 发送视频或者图片
     */
    @OnClick(R.id.video_save)
    public void saveVideoOrPhoto() {
        final Intent data;
        data = new Intent();
        if (NOW_MODE == AppConstant.VIDEO_TAKE_PHOTO){
            data.putExtra("path", mCameraPath);
            data.putExtra("mediaType", "image");
            saveMedia(new File(mCameraPath));
        }
        else if (NOW_MODE == AppConstant.VIDEO_RECORD_MODE) {
            data.putExtra("path", mVideoPath);
            data.putExtra("mediaType", "video");
            saveMedia(new File(mVideoPath));
        }
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * TextureView 触摸方法
     */
    private class CameraTouch {
        private float mOldScale = 1.0f;
        private float mScale;
        private float mSpan = 0;
        private float mOldSpan;
        private float mFirstDistance = 0;

        public void onScale(MotionEvent event) {
            if (event.getPointerCount() == 2) {
                if (mFirstDistance == 0) {
                    mFirstDistance = distance(event);
                }

                float distance = distance(event);
                float scale;
                if (distance > mFirstDistance) {
                    scale = (distance - mFirstDistance) / 80;
                    scale = scale + mSpan;
                    mOldSpan = scale;
                    mScale = scale;
                } else if (distance < mFirstDistance) {
                    scale = distance / mFirstDistance;
                    mOldSpan = scale;
                    mScale = scale * mOldScale;
                } else {
                    return;
                }

                cameraHelper.cameraZoom(mScale);
                videoScale.setProgress((int) ((mScale / cameraHelper.getMaxZoom()) * videoScale.getMax()));
                if (mScale < 1.0f) {
                    videoScale.setProgress(0);
                }
            }
        }

        /**
         * scale 开始
         *
         * @param event
         */
        public void onScaleStart(MotionEvent event) {
            mFirstDistance = 0;
            setScaleMax((int) cameraHelper.getMaxZoom());
            videoScaleBarLayout.setVisibility(View.VISIBLE);
            removeSeekBarRunnable();
        }

        /**
         * scale 结束
         *
         * @param event MotionEvent
         */
        private void onScaleEnd(MotionEvent event) {
            if (mScale < 1.0f) {
                mOldScale = 1.0f;
            } else if (mScale > cameraHelper.getMaxZoom()) {
                mOldScale = cameraHelper.getMaxZoom();
            } else {
                mOldScale = mScale;
            }
            mSpan = mOldSpan;

            if (event != null) {
                seekBarDelayedHind();
            }
        }

        /**
         * 重置 缩放
         */
        public void resetScale() {
            mOldScale = 1.0f;
            mSpan = 0f;
            mFirstDistance = 0f;
            videoScale.setProgress(0);
        }

        public void setScale(float scale) {
            mScale = scale;
            mOldSpan = scale;
            onScaleEnd(null);
        }

        /**
         * 计算两个手指间的距离
         *
         * @param event MotionEvent
         * @return 距离
         */
        private float distance(MotionEvent event) {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            // 使用勾股定理返回两点之间的距离
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

        private void setScaleMax(int max) {
            videoScale.setMax(max * 100);
        }
    }

    /**
     * camera 点击对焦动画
     */
    private class FoucesAnimation extends Animation {

        private int width = cameraHelper.dip2px(CameraVideoActivity.this, 150);
        private int W = cameraHelper.dip2px(CameraVideoActivity.this, 65);

        private int oldMarginLeft;
        private int oldMarginTop;

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {

            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams) videoFouces.getLayoutParams();
            int w = (int) (width * (1 - interpolatedTime));
            if (w < W) {
                w = W;
            }
            layoutParams.width = w;
            layoutParams.height = w;
            if (w == W) {
                videoFouces.setLayoutParams(layoutParams);
                return;
            }
            layoutParams.leftMargin = oldMarginLeft - (w / 2);
            layoutParams.topMargin = oldMarginTop + (w / 8);
            videoFouces.setLayoutParams(layoutParams);
        }

        public void setOldMargin(int oldMarginLeft, int oldMarginTop) {
            this.oldMarginLeft = oldMarginLeft;
            this.oldMarginTop = oldMarginTop;
            removeImageFoucesRunnable();
            imageFoucesDelayedHind();
        }
    }

    /**
     * 移除对焦 消失任务
     */
    private void removeImageFoucesRunnable() {
        videoFouces.removeCallbacks(mImageFoucesRunnable);
    }

    /**
     * 添加 延时消失任务
     */
    private void imageFoucesDelayedHind() {
        videoFouces.postDelayed(mImageFoucesRunnable, 500);
    }

    /**
     * seekBar 添加延时消失任务
     */
    private void seekBarDelayedHind() {
        if (isCanHind) {
            videoScaleBarLayout.postDelayed(SeekBarLayoutRunnalbe, 2000);
        }
        isCanHind = false;
    }

    private Runnable mImageFoucesRunnable = new Runnable() {
        @Override
        public void run() {
            videoFouces.setVisibility(View.GONE);
        }
    };

    /**
     * 移除隐藏 seekBar消失的任务
     */
    private void removeSeekBarRunnable() {
        isCanHind = true;
        videoScale.removeCallbacks(SeekBarLayoutRunnalbe);
    }

    /**
     * 3s后隐藏的runnable
     */
    private Runnable SeekBarLayoutRunnalbe = new Runnable() {
        @Override
        public void run() {
            videoScaleBarLayout.setVisibility(View.GONE);
        }
    };

    /**
     * 显示播放视频 的 确认和删除按钮
     */
    private void showRecordEndView() {
        videoSave.setVisibility(View.VISIBLE);
        videoDelete.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏视频录像的进度条
     */
    private void hindVideoRecordSeekBar() {
        videoRecordSeekBar.setVisibility(View.GONE);
        videoRecordSeekBar.setProgress(0);
    }

    /**
     * 关闭摄像头
     */
    private void closeCamera() {
        videoRecord.setClickable(false);
        cameraHelper.closeCamera();
        cameraHelper.stopBackgroundThread();
    }

    /**
     * 播放视频
     */
    private void playVideo() {
        closeCamera();
        if (mVideoPath != null && mVideoPlayer != null) {
            mVideoPlayer.setDataSourceAndPlay(mVideoPath);
            hasPlaying = true;
            //视频播放状态
            TEXTURE_STATE = AppConstant.TEXTURE_PLAY_STATE;
        }
    }

    /**
     * 移动焦点图标
     *
     * @param x x坐标
     * @param y y坐标
     */
    private void moveFouces(int x, int y) {
        videoFouces.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams layoutParams
                = (RelativeLayout.LayoutParams) videoFouces.getLayoutParams();
        videoFouces.setLayoutParams(layoutParams);
        mFoucesAnimation.setDuration(500);
        mFoucesAnimation.setRepeatCount(0);
        mFoucesAnimation.setOldMargin(x, y);
        videoFouces.startAnimation(mFoucesAnimation);
        cameraHelper.requestFocus(x, y);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraHelper != null) {
            cameraHelper.startBackgroundThread();
        }

        if (videoTexture.isAvailable()) {
            if (MODE == AppConstant.CAMERA_MODE) {
                if (TEXTURE_STATE == AppConstant.TEXTURE_PREVIEW_STATE) {
                    //预览状态
                    initCamera(mNowCameraType);
                } else if (TEXTURE_STATE == AppConstant.TEXTURE_PLAY_STATE) {
                    //视频播放状态
                    mVideoPlayer.play();
                }
                mVideoPlayer.setVideoPlayWindow(new Surface(videoTexture.getSurfaceTexture()));
            }
        } else {
            videoTexture.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    if (MODE == AppConstant.CAMERA_MODE) {
                        if (TEXTURE_STATE == AppConstant.TEXTURE_PREVIEW_STATE) {
                            //预览状态
                            initCamera(mNowCameraType);
                        } else if (TEXTURE_STATE == AppConstant.TEXTURE_PLAY_STATE) {
                            //视频播放状态
                            mVideoPlayer.play();
                        }
                        mVideoPlayer.setVideoPlayWindow(new Surface(videoTexture.getSurfaceTexture()));
                    } else if (MODE == AppConstant.VIDEO_MODE) {
                        mVideoPlayer.setVideoPlayWindow(new Surface(videoTexture.getSurfaceTexture()));
                        Log.e("videoPath", "path:" + mVideoPath);
                        mVideoPlayer.setDataSourceAndPlay(mVideoPath);
                        hasPlaying = true;
                        //视频播放状态
                        TEXTURE_STATE = AppConstant.TEXTURE_PLAY_STATE;
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isNoPremissionPause) {
            isNoPremissionPause = false;
            return;
        }
        Log.e("camera", "mode:" + MODE);
        if (MODE == AppConstant.CAMERA_MODE) {
            if (TEXTURE_STATE == AppConstant.TEXTURE_PREVIEW_STATE) {
                cameraHelper.closeCamera();
                cameraHelper.stopBackgroundThread();
            } else if (TEXTURE_STATE == AppConstant.TEXTURE_PLAY_STATE) {
                mVideoPlayer.pause();
            }
        }
    }

    /**
     * 隐藏切换摄像头按钮
     */
    private void hindSwitchCamera() {
        videoSwitchCamera.setVisibility(View.GONE);
    }

    /**
     * 隐藏切换菜单
     */
    private void hindMenu() {
        videoMenu.setVisibility(View.GONE);
    }

    /**
     * 刷新相册
     *
     * @param mediaFile 文件
     */
    private void saveMedia(File mediaFile) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(mediaFile);
        intent.setData(uri);
        sendBroadcast(intent);
    }

    /**
     * 隐藏录像完成后底部两个按钮
     */
    private void hindRecordEndView() {
        videoSave.setVisibility(View.GONE);
        videoDelete.setVisibility(View.GONE);
    }

    /**
     * 隐藏播放界面的控件出来
     */
    private void hindPlayView() {
        videoSeekBar.setVisibility(View.GONE);
        videoMinePlay.setVisibility(View.GONE);
        videoPlay.setVisibility(View.GONE);
        videoSeekTime.setVisibility(View.GONE);
    }

    /**
     * 视频播放模式控件隐藏
     */
    private Runnable mHindViewRunnable = new Runnable() {
        @Override
        public void run() {
            hindPlayView();
        }
    };

    /**
     * 显示播放界面的控件出来
     */
    private void showPlayView() {
        videoSeekBar.setVisibility(View.VISIBLE);
        videoMinePlay.setVisibility(View.VISIBLE);
        videoPlay.setVisibility(View.VISIBLE);
        videoSeekTime.setVisibility(View.VISIBLE);
    }

    /**
     * 底部 布局集合
     */
    private List<View> mLayoutList = new LinkedList<>();
    /**
     * visible与invisible之间切换的动画
     */
    private TranslateAnimation mShowAction;

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
                    mLayoutBottom.getChildAt(i).setVisibility(View.GONE);
                }
            }
            v.startAnimation(mShowAction);
            v.setVisibility(View.VISIBLE);
        } else {
            //全部隐藏但是capture的显示出来
            for (int i = 0; i < mLayoutBottom.getChildCount(); i++) {
                if (mLayoutBottom.getChildAt(i).getVisibility() == View.VISIBLE) {
                    mLayoutBottom.getChildAt(i).setVisibility(View.GONE);
                }
            }
            rlCamera.startAnimation(mShowAction);
            rlCamera.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 曝光 ae 滑动监听事件
     */
    private class CameraSeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            switch (seekBar.getId()){
                case R.id.sb_ae: {
                    if (switchAe.isChecked()) {
                        // 曝光增益
                        if (cameraHelper.getRange1() == null) {
                            break;
                        }
                        Log.e(TAG, "曝光增益范围：" + cameraHelper.getRange1().toString());
                        int maxmax = cameraHelper.getRange1().getUpper();
                        int minmin = cameraHelper.getRange1().getLower();
                        int all = maxmax - minmin;
                        int time = 100 / all;
                        int ae = ((progress / time) - maxmax) > maxmax ? maxmax :
                                ((progress / time) - maxmax) < minmin ? minmin : ((progress / time) - maxmax);
                        cameraHelper.setAERegions(ae);
                        tvSbTxt.setText("曝光增益：" + ae);
                    } else {
                        // 曝光时间
                        if (cameraHelper.getEtr() == null) {
                            tvSbTxt.setText("获取曝光时间失败");
                            break;
                        }
                        Log.e(TAG, "曝光时间范围：" + cameraHelper.getEtr().toString());
                        long max = cameraHelper.getEtr().getUpper();
                        long min = cameraHelper.getEtr().getLower();
                        long ae = ((progress * (max - min)) / 100 + min);
                        cameraHelper.setAeTime(ae);
                        tvSbTxt.setText("曝光时间：" + ae);
                    }
                    break;
                }
            }
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
     * rv点击事件 初始化
     */
    private void initListener() {
        sAdapter.setSenseOnItemClickListener(new SenseAdapter.SenseOnItemClickListener() {
            @Override
            public void itemOnClick(int position) {
                cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_USE_SCENE_MODE);
                switch (position) {
                    case 0:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_DISABLED);
                        break;
                    case 1:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_FACE_PRIORITY);
                        break;
                    case 2:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_ACTION);
                        break;
                    case 3:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_PORTRAIT);
                        break;
                    case 4:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_LANDSCAPE);
                        break;
                    case 5:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_NIGHT);
                        break;
                    case 6:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_NIGHT_PORTRAIT);
                        break;
                    case 7:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_THEATRE);
                        break;
                    case 8:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_BEACH);
                        break;
                    case 9:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_SNOW);
                        break;
                    case 10:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_SUNSET);
                        break;
                    case 11:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_STEADYPHOTO);
                        break;
                    case 12:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_FIREWORKS);
                        break;
                    case 13:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_SPORTS);
                        break;
                    case 14:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_PARTY);
                        break;
                    case 15:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_CANDLELIGHT);
                        break;
                    case 16:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_BARCODE);
                        break;
                    default:
                        break;
                }
            }
        });

        effectAdapter.setEffectOnItemClickListener(new EffectAdapter.EffectOnItemClickListener() {
            @Override
            public void itemOnClick(int position) {
                switch (position) {
                    case 0:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_AQUA);
                        break;
                    case 1:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_BLACKBOARD);
                        break;
                    case 2:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_MONO);
                        break;
                    case 3:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_NEGATIVE);
                        break;
                    case 4:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_POSTERIZE);
                        break;
                    case 5:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_SEPIA);
                        break;
                    case 6:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_SOLARIZE);
                        break;
                    case 7:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_WHITEBOARD);
                        break;
                    case 8:
                        cameraHelper.setCameraBuilerMode(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_OFF);
                        break;
                    default:
                        break;
                }
            }
        });
    }
}
