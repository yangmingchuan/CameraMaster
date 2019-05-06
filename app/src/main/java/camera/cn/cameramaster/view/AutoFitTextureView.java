package camera.cn.cameramaster.view;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * 自定义 TextureView
 * 重新计算预览宽高
 *
 * @fileName: AutoFitTextureView
 * @date: 2019/1/28  17:00
 * @author: ymc
 * @QQ:745612618
 */

public class AutoFitTextureView extends TextureView {

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    public AutoFitTextureView(Context context) {
        this(context, null);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 设置此视图的纵横比。将根据比率测量视图的大小 根据参数计算。注意，参数的实际大小无关紧要
     * 调用setAspectRatio（2,3）和setAspectRatio（4,6）会产生相同的结果。
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    /**
     * 视频宽度适配
     *
     * @param width 宽度
     * @param height 长度
     */
    public void setVideoAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        //算出相机的缩放比例
        float mRatio = (float) mRatioWidth / (float) mRatioHeight;
        if (mRatio < 1.0) {
            setAspectRatio(width, height);
        } else {
            float h = getWidth() / mRatio;
            float scale;
            if (h > getHeight()){
                scale = (float) getHeight() / h;
            }else{
                scale = h / (float) getHeight();
            }
            Matrix matrix = new Matrix();
            matrix.postScale(1, scale, getWidth() / 2, getHeight() / 2);
            setTransform(matrix);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }

}