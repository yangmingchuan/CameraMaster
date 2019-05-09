package camera.cn.cameramaster.util.cameravideo;

import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.io.File;

/**
 * 相机 接口类
 */

public interface ICamera2 {

    /**
     * 缩放
     * @param zoom 缩放比例
     */
    void cameraZoom(float zoom);

    /**
     * 打开摄像头
     */
    boolean openCamera(CameraType cameraType);

    /**
     * 关闭摄像头
     */
    void closeCamera();

    /**
     * 切换摄像头
     * @param cameraType 切换摄像头类型
     * @return boolean 是否切换成功
     */
    boolean switchCamera(CameraType cameraType);

    /**
     * 开启相机的预览模式
     */
    boolean startPreview();

    /**
     * 停止预览
     */
    void resumePreview();

    /**
     * 开始视频的录制
     * @param path  存储路径
     * @param mediaType  文件类型
     */
    boolean startVideoRecord(String path, int mediaType);

    /**
     * 停止视频录制
     */
    void stopVideoRecord();

    /**
     * 拍照
     * @param path 存储路径
     * @param mediaType  文件类型
     */
    boolean takePhone(String path, MediaType mediaType);

    /**
     * 获取预览大小
     * @return
     */
    Size getPreViewSize();

    void setSurface(Surface surface);

    void setTextureView(TextureView textureView);

    void setTakePhotoListener(TakePhotoListener mTakePhotoListener);

    void setCameraReady(CameraReady mCameraReady);

    void flashSwitchState(FlashState mFlashState);

    void setCameraState(CameraMode cameraMode);

    /**
     * 手动请求对焦
     * @param x
     * @param y
     */
    void requestFocus(float x, float y);

    /**
     * 摄像头模式类型
     */
    enum CameraMode
    {
        RECORD_VIDEO,
        TAKE_PHOTO
    }

    /**
     * 当前只有mp4类型
     */
    enum MediaType
    {
        MP4,
        JPEG,
    }

    /**
     * 摄像头类型
     */
    enum CameraType
    {
        FRONT,
        BACK,
        USB
    }

    /**
     * 灯光状态
     */
    enum FlashState
    {
        CLOSE,
        AUTO,
        OPEN
    }

    interface TakePhotoListener{
        void onTakePhotoFinish(File file, int photoRotation, int width, int height);
    }

    interface CameraReady
    {
        void onCameraReady();
    }
}
