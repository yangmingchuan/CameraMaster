package camera.cn.cameramaster.ui.look.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * 自定义GLSurfaceView，暴露出onAttachedToWindow
 * 方法及onDetachedFromWindow方法，取消holder的默认监听
 * onAttachedToWindow及onDetachedFromWindow必须保证view
 * 存在Parent
 *
 * @packageName: cn.tongue.tonguecamera.ui.look.view
 * @fileName: GLView
 * @date: 2019/3/15  13:56
 * @author: ymc
 * @QQ:745612618
 */

public class GLView extends GLSurfaceView {

    private Object mSurface;
    private Renderer mRenderer;

    public GLView(Context context, Object surface, Renderer renderer) {
        super(context);
        this.mSurface = surface;
        this.mRenderer = renderer;
        // 取消 holder 默认监听
        getHolder().addCallback(null);
        setEGLWindowSurfaceFactory(new EGLWindowSurfaceFactory() {
            @Override
            public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig
                    config, Object window) {
                return egl.eglCreateWindowSurface(display, config, mSurface, null);
            }

            @Override
            public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
                egl.eglDestroySurface(display, surface);
            }
        });
        setEGLContextClientVersion(2);
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        setPreserveEGLContextOnPause(true);
    }

    public GLView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void attachedToWindow() {
        super.onAttachedToWindow();
    }

    public void detachedFromWindow() {
        super.onDetachedFromWindow();
    }

}
