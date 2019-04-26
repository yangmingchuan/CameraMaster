package camera.cn.cameramaster.base;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import camera.cn.cameramaster.R;

/**
 *  基础Activity
 *
 * @packageName: cn.ymc.vip.suntimejava.base
 * @fileName: BaseActivity
 * @date: 2019/1/8  17:35
 * @author: ymc
 * @QQ:745612618
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected BaseActivity activity;
    private Unbinder bun;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(getLayoutId());
//        getSupportActionBar().hide();
        bun = ButterKnife.bind(this);
        activity = this;
        initStatusColor();
        initView();
        initData();
    }

    /**
     * 设置透明状态栏,这样才能让 ContentView 向上  6.0小米手机设置 tootlbar 会被挤上去
     * window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
     */
    private void initStatusColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置状态栏颜色
            window.setStatusBarColor(getColor(R.color.theme));

            ViewGroup mContentView = activity.findViewById(Window.ID_ANDROID_CONTENT);
            View mChildView = mContentView.getChildAt(0);
            if (mChildView != null) {
                //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View . 使其不为系统 View 预留空间.
                ViewCompat.setFitsSystemWindows(mChildView, false);
            }
        }
    }

    protected abstract int getLayoutId();
    protected abstract void initView();
    protected abstract void initData();

    @Override
    protected void onDestroy() {
        bun.unbind();
        super.onDestroy();
    }
}
