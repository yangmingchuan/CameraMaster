package camera.cn.cameramaster.ui.look;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.view.View;
import android.view.ViewGroup;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import camera.cn.cameramaster.ui.look.view.GLView;


/**
 * 借助GLSurfaceView创建的GL环境，做渲染工作。不将内容渲染到GLSurfaceView
 * 的Surface上，而是将内容绘制到外部提供的Surface、SurfaceHolder或者SurfaceTexture上。
 *
 * @packageName: cn.tongue.tonguecamera.ui.look
 * @fileName: TextureController
 * @date: 2019/3/15  13:38
 * @author: ymc
 * @QQ:745612618
 */

public class TextureController implements GLSurfaceView.Renderer {
    private GLView mGLView;
    private Object surface;
    private Context mContext;
    /**
     * 输出视图的大小
     */
    private Point mWindowSize;
    /**
     * 用于绘制回调缩放的矩阵
     */
    private float[] callbackOM=new float[16];
    /**
     *  数据的大小
     */
    private Point mDataSize;
    public TextureController(Context context) {
        this.mContext=context;
        mGLView=new GLView(mContext,surface,this);
        //避免GLView的attachToWindow和detachFromWindow崩溃
        ViewGroup v=new ViewGroup(mContext) {
            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {

            }
        };
        v.addView(mGLView);
        v.setVisibility(View.GONE);
//        mEffectFilter=new TextureFilter(mContext.getResources());
//        mShowFilter=new NoFilter(mContext.getResources());
//        mGroupFilter=new GroupFilter(mContext.getResources());

        //设置默认的DateSize，DataSize由AiyaProvider根据数据源的图像宽高进行设置
        mDataSize=new Point(720,1280);

        mWindowSize=new Point(720,1280);
    }

    public void surfaceCreated(Object nativeWindow){
        this.surface=nativeWindow;
        mGLView.surfaceCreated(null);
    }

    public void surfaceChanged(int width,int height){
        this.mWindowSize.x=width;
        this.mWindowSize.y=height;
        mGLView.surfaceChanged(null,0,width,height);
    }

    public void surfaceDestroyed(){
        mGLView.surfaceDestroyed(null);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }
}
