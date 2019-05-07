package camera.cn.cameramaster.ui;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;

import butterknife.BindView;
import camera.cn.cameramaster.R;
import camera.cn.cameramaster.base.BaseActivity;
import camera.cn.cameramaster.view.AutoFitTextureView;
import camera.cn.cameramaster.view.AutoLocateHorizontalView;

/**
 * 拍照 视频
 *
 * @author ymc
 * @date 2019年5月7日 13:49:17
 */

public class CameraVideoActivity extends BaseActivity implements IVideoControl.PlaySeekTimeListener,
        IVideoControl.PlayStateListener, ICamera2.TakePhotoListener,
        SensorEventListener, ICamera2.CameraReady{

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


    @Override
    protected int getLayoutId() {
        return R.layout.activity_camera_video;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }

    /**
     * 传感器继承方法 重力发生改变
     * @param event event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    /**
     * 当已注册传感器的精度发生变化时调用
     * @param sensor sensor
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

    @Override
    public void onTakePhotoFinish(File file, int photoRotation, int width, int height) {

    }

    @Override
    public void onCameraReady() {

    }
}
