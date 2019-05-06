package camera.cn.cameramaster.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.widget.TextView;

/**
 * 文字显示 一秒后消失
 */

@SuppressLint("AppCompatCustomView")
public class AnimationTextView extends TextView {
    private Handler mMainHandler;
    private Animation mAnimation;
    /**
     * 防止又换了个text，但是上次哪个还没有消失即将小时就把新的text的给消失了
     */
    public int mTimes = 0;

    public AnimationTextView(Context context) {
        super(context);
    }

    public AnimationTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimationTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AnimationTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setmMainHandler(Handler mMainHandler) {
        this.mMainHandler = mMainHandler;
    }

    public void setmAnimation(Animation mAnimation) {
        this.mAnimation = mAnimation;
    }

    public void start(String text, int message) {
        if (mAnimation == null || mMainHandler == null) {
            return;
        }
        this.setVisibility(VISIBLE);
        mTimes++;
        this.setText(text);
        this.startAnimation(mAnimation);
        new Thread(new SleepThread(mMainHandler, message, 1000, Integer.valueOf(mTimes))).start();
    }

    public void stop() {
        this.setVisibility(GONE);
    }
}
