package camera.cn.cameramaster.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import butterknife.BindView;
import butterknife.OnClick;
import camera.cn.cameramaster.R;
import camera.cn.cameramaster.adapter.MenuAdapter;
import camera.cn.cameramaster.base.BaseActivity;
import camera.cn.cameramaster.util.AppConstant;
import camera.cn.cameramaster.util.cameravideo.CameraHelper;
import camera.cn.cameramaster.util.cameravideo.VideoPlayer;
import camera.cn.cameramaster.view.AutoFitTextureView;
import camera.cn.cameramaster.view.AutoLocateHorizontalView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

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

    @BindView(R.id.video_photo)
    ImageView videoPhoto;
    @BindView(R.id.video_texture)
    AutoFitTextureView videoTexture;
    @BindView(R.id.video_record_seek_bar)
    SeekBar videoRecordSeekBar;
    @BindView(R.id.video_close)
    ImageButton videoClose;
    @BindView(R.id.video_time)
    TextView videoTime;
    @BindView(R.id.video_switch_flash)
    ImageButton videoSwitchFlash;
    @BindView(R.id.video_switch_camera)
    ImageButton videoSwitchCamera;
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
    @BindView(R.id.video_fouces)
    ImageView videoFouces;
    @BindView(R.id.video_minus)
    ImageView videoMinus;
    @BindView(R.id.video_scale)
    SeekBar videoScale;
    @BindView(R.id.video_add)
    ImageView videoAdd;
    @BindView(R.id.video_scale_bar_layout)
    RelativeLayout videoScaleBarLayout;
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


    @Override
    protected int getLayoutId() {
        return R.layout.activity_camera_video;
    }

    @Override
    protected void initView() {
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
    }

    @Override
    protected void initData() {
        mCameraPath = cameraHelper.getPhotoFilePath();
        mVideoPath = cameraHelper.getVideoFilePath();
    }

    /**
     * 初始化 录像
     */
    private void initVideoMode() {

    }

    /**
     * 初始化 拍照
     */
    private void initCameraMode() {
        cameraHelper = new CameraHelper(this);
        cameraHelper.setTakePhotoListener(this);
        cameraHelper.setCameraReady(this);
        mVideoPlayer.setLoopPlay(true);
        List<String> menus = new ArrayList<>();
        menus.add("拍照");
        menus.add("录像");

        mMenuAdapter = new MenuAdapter(this, menus, videoMenu);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        videoMenu.setLayoutManager(linearLayoutManager);
        videoMenu.setAdapter(mMenuAdapter);
        videoMenu.setOnSelectedPositionChangedListener(this);

        mCameraTouch = new CameraTouch();
    }

    /**
     * 点击事件
     *
     * @param view view
     */
    @OnClick({R.id.video_record, R.id.video_switch_camera, R.id.video_switch_flash})
    public void cameraOnClickListener(View view) {
        switch (view.getId()) {
            // 点击拍照或者 视频按钮
            case R.id.video_record:
                if (!hasRecordClick) {
                    return;
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission
                        (this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "cameraOnClickListener: 动态权限获取失败...");
                    return;
                }
                hasRecordClick = true;
                //拍照
                if (NOW_MODE == AppConstant.VIDEO_TAKE_PHOTO) {
                    cameraHelper.takePhone(mCameraPath, ICamera2.MediaType.JPEG);
                }
                //录制视频
                if (NOW_MODE == AppConstant.VIDEO_RECORD_MODE) {
                    if (hasRecording) {
                        // 暂停录像
                        hasRecording = false;
                        if (mDisposable != null && !mDisposable.isDisposed()){
                            mDisposable.dispose();
                        }
                        mDisposable = null;
                        videoSeekTime.setVisibility(View.GONE);
                        cameraHelper.stopVideoRecord();

                        videoRecord.setImageResource(R.mipmap.ic_record);
                        videoRecord.setVisibility(View.GONE);
                        videoClose.setVisibility(View.VISIBLE);
                        videoHintText.setVisibility(View.GONE);
//                        showRecordEndView();
//                        hindVideoRecordSeekBar();
//                        playVideo();
                    } else {
                        // 开始录像

                    }
                }
                hasRecordClick = false;
                break;
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
                    videoSwitchFlash.setImageResource(R.mipmap.flash_auto);
                    videoSwitchFlash.setTag(1);
                    cameraHelper.flashSwitchState(ICamera2.FlashState.AUTO);
                } else if (((int) o) == 1) {
                    videoSwitchFlash.setImageResource(R.mipmap.flash_open);
                    videoSwitchFlash.setTag(2);
                    cameraHelper.flashSwitchState(ICamera2.FlashState.OPEN);
                } else {
                    videoSwitchFlash.setImageResource(R.mipmap.flash_close);
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
     *
     * @param event event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

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
    public void onSeekTime(int allTime, int time) {

    }

    @Override
    public void onStartListener(int width, int height) {

    }

    @Override
    public void onCompletionListener() {

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
    public void onTakePhotoFinish(File file, int photoRotation, int width, int height) {

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
        if (pos == 0) {
            NOW_MODE = AppConstant.VIDEO_TAKE_PHOTO;
            cameraHelper.setCameraState(ICamera2.CameraMode.TAKE_PHOTO);
            videoHintText.setText("点击拍照");
        }
        if (pos == 1) {
            NOW_MODE = AppConstant.VIDEO_RECORD_MODE;
            cameraHelper.setCameraState(ICamera2.CameraMode.RECORD_VIDEO);
            videoHintText.setText("点击录像");
        }
    }

    /**
     * 录像时长倒计时
     */
    @SuppressLint("SetTextI18n")
    private void recordCountDown() {
        videoTime.setVisibility(View.VISIBLE);
        videoRecordSeekBar.setVisibility(View.VISIBLE);
        final int count = 15;
//        mDisposable = Observable.interval(1, 1, TimeUnit.SECONDS)
//                .take(count + 1)
//                .map(new Function<Long, Long>() {
//                    @Override
//                    public Long apply(Long aLong) {
//                        return count - aLong;
//                    }
//                }).observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<Long>() {
//                    @Override
//                    public void accept(Long aLong) {
//                        long time = 16 - aLong;
//                        if (time < 10){
//                            videoTime.setText("0:0" + String.valueOf(time));
//                        }else{
//                            videoTime.setText("0:" + String.valueOf(time));
//                        }
//                        videoRecordSeekBar.setProgress((int) time);
//                        if (time == AppConstant.VIDEO_MAX_TIME) {
//                            videoTime.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    recordVideoOrTakePhoto();
//                                    hindVideoRecordSeekBar();
//                                }
//                            }, 300);
//
//                        }
//                    }
//                });
    }

    private void hindVideoRecordSeekBar() {

    }

    private void recordVideoOrTakePhoto() {

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

            FrameLayout.LayoutParams layoutParams =
                    (FrameLayout.LayoutParams) videoFouces.getLayoutParams();
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
        videoFouces.postDelayed(mImageFoucesRunnable, 1000);
    }

    /**
     * seekBar 添加延时消失任务
     */
    private void seekBarDelayedHind() {
        if (isCanHind) {
            videoScaleBarLayout.postDelayed(SeekBarLayoutRunnalbe, 3000);
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
}
