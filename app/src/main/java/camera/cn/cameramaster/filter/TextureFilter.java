package camera.cn.cameramaster.filter;

import android.content.res.Resources;
import android.graphics.SurfaceTexture;

import java.nio.ByteBuffer;

/**
 * @packageName: cn.tongue.tonguecamera.filter
 * @fileName: TextureFilter
 * @date: 2019/3/15  14:40
 * @author: ymc
 * @QQ:745612618
 */

public class TextureFilter extends  AFilter{
    private int width=0;
    private int height=0;

    private int[] fFrame = new int[1];
    private int[] fTexture = new int[1];
    private int[] mCameraTexture=new int[1];

    private SurfaceTexture mSurfaceTexture;
    private float[] mCoordOM=new float[16];
    /**
     * 获取Track数据
     */
    private ByteBuffer tBuffer;


    public TextureFilter(Resources mRes) {
        super(mRes);
    }

    @Override
    protected void onCreate() {

    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }
}
