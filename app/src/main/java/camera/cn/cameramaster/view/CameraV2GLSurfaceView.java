package camera.cn.cameramaster.view;

import android.content.Context;
import android.opengl.GLSurfaceView;

import camera.cn.cameramaster.render.CameraV2Renderer;
import camera.cn.cameramaster.util.CameraV2;


/**
 * CameraV2 GLSurfaceView
 * 参考url ： [https://blog.csdn.net/lb377463323/article/details/78054892]
 *
 * @date 2019年2月12日 13:41:16
 * @author ymc
 */

public class CameraV2GLSurfaceView extends GLSurfaceView {
    public static boolean shouldTakePic = false;

    public void init(CameraV2 camera, boolean isPreviewStarted, Context context) {
        setEGLContextClientVersion(2);
        CameraV2Renderer mCameraV2Renderer = new CameraV2Renderer();
        mCameraV2Renderer.init(this, camera, isPreviewStarted, context);
        setRenderer(mCameraV2Renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public CameraV2GLSurfaceView(Context context) {
        super(context);
    }
}
