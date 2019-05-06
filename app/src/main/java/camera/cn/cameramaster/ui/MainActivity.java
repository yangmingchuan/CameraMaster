package camera.cn.cameramaster.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import camera.cn.cameramaster.R;
import camera.cn.cameramaster.base.BaseActivity;
import camera.cn.cameramaster.util.AppConstant;

/**
 * 首页
 *
 * @author ymc
 */

public class MainActivity extends BaseActivity {
    private final static String TAG = "MainActivity";
    @BindView(R.id.btn_camera)
    public Button btn;
    @BindView(R.id.tv_message)
    TextView tvMsg;

    private File mD65File;
    private File mFile;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        mD65File= new File(getExternalFilesDir(null), "picD65.jpg");
        mFile = new File(getExternalFilesDir(null), "pic.jpg");

    }

    @Override
    protected void initData() {
        requestPermission();
    }

    @OnClick({R.id.btn_camera, R.id.btn_camera2, R.id.btn_filter_camera2})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_camera:
                Intent intent = new Intent(this, CameraActivity.class);
                startActivityForResult(intent, 0);
                break;
            case R.id.btn_camera2:
                Intent intent2 = new Intent(this, GoogleCameraActivity.class);
                startActivityForResult(intent2, 1);
                break;
            case R.id.btn_filter_camera2:
                Intent intentFilter2 = new Intent(this, CameraSurfaceViewActivity.class);
                startActivityForResult(intentFilter2, 0);
                break;
            default:
                break;
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != AppConstant.RESULT_CODE.RESULT_OK) {
            return;
        }
        if (requestCode == 0) {
            String imgPath = data.getStringExtra(AppConstant.KEY.IMG_PATH);
            int picWidth = data.getIntExtra(AppConstant.KEY.PIC_WIDTH, 0);
            int picHeight = data.getIntExtra(AppConstant.KEY.PIC_HEIGHT, 0);
            Intent intent = new Intent(activity, ShowPicActivity.class);
            intent.putExtra(AppConstant.KEY.PIC_WIDTH, picWidth);
            intent.putExtra(AppConstant.KEY.PIC_HEIGHT, picHeight);
            intent.putExtra(AppConstant.KEY.IMG_PATH, imgPath);
            startActivity(intent);
        }else if(requestCode == 1){
//            tvMsg.setText("图片 D65 光源处理中 请勿退出....");
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    final long starttime = System.currentTimeMillis();
//                    FileInputStream fis = null;
//                    FileOutputStream output = null;
//                    try {
//                        fis = new FileInputStream(mFile);
//                        Bitmap bitmap = BitmapFactory.decodeStream(fis);
//                        Bitmap d65bitmap = BitmapUtils.ImgaeToNegative(bitmap);
//                        output = new FileOutputStream(mD65File);
//                        d65bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                tvMsg.setText("处理完毕.耗时："+ (System.currentTimeMillis() - starttime)+ " ms");
//                            }
//                        });
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
        }
    }

    /**
     * 动态申请  (电话/位置/存储)
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void requestPermission() {
        AndPermission.with(this)
                .permission(Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO)
                .rationale(new Rationale() {
                    @Override
                    public void showRationale(Context context, List<String> permissions, RequestExecutor executor) {
                        executor.execute();
                    }
                })
                .onGranted(new Action() {
                    @Override
                    public void onAction(List<String> permissions) {
                        Log.e(TAG, "用户给权限");
                    }
                })
                .onDenied(new Action() {
                    @Override
                    public void onAction(List<String> permissions) {
                        if (AndPermission.hasAlwaysDeniedPermission(MainActivity.this, permissions)) {
                            // 打开权限设置页
                            AndPermission.permissionSetting(MainActivity.this).execute();
                            return;
                        }
                        Log.e(TAG, "用户拒绝权限");
                    }
                })
                .start();
    }
}
