package camera.cn.cameramaster.ui.look;

import android.opengl.GLSurfaceView;
import android.os.Build;
import android.view.SurfaceView;
import android.widget.ImageButton;

import butterknife.BindView;
import camera.cn.cameramaster.R;
import camera.cn.cameramaster.base.BaseActivity;

/**
 * 美颜界面
 */

public class LookCameraActivity extends BaseActivity {
    @BindView(R.id.mSurface)
    SurfaceView mSurface;
    @BindView(R.id.mShutter)
    ImageButton mShutter;
    private GLSurfaceView.Renderer mRenderer;
    private int cameraId = 1;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_look_camera;
    }

    @Override
    protected void initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            mRenderer = new Camera2Renderer();
        }else{
//            mRenderer = new Camera1Renderer();
        }
    }

    @Override
    protected void initData() {

    }

}
