package camera.cn.cameramaster.util;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Size;

import java.util.Comparator;

/**
 * 比较 工具
 *
 * @packageName: cn.tongue.tonguecamera.util
 * @fileName: CompareSizesByArea
 * @date: 2019/4/16  13:42
 * @author: ymc
 * @QQ:745612618
 */

public class CompareSizesByArea implements Comparator<Size> {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int compare(Size lhs, Size rhs) {
        // 在这里投射以确保乘法不会溢出
        return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                (long) rhs.getWidth() * rhs.getHeight());
    }
}