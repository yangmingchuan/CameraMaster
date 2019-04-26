package camera.cn.cameramaster.ui.look;

/**
 * 帧数据 回调接口
 *
 * @packageName: cn.tongue.tonguecamera.ui.look
 * @fileName: FrameCallback
 * @date: 2019/3/15  13:33
 * @author: ymc
 * @QQ:745612618
 */

public interface FrameCallback {

    void onFrame(byte[] bytes, long time);

}
