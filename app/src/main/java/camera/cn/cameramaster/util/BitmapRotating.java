package camera.cn.cameramaster.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * bitmap 工具类
 *
 * @packageName: cn.tongue.tonguecamera.util
 * @fileName: BitmapRotating
 * @date: 2019/2/14  14:26
 * @author: ymc
 * @QQ:745612618
 */

public class BitmapRotating {

    /**
     * 通过资源id转化成Bitmap
     *
     * @param context 对象
     * @param resId   资源id
     * @return bitmap
     */
    public static Bitmap ReadBitmapById(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }


    /**
     * Bitmap --> byte[]
     *
     * @param bmp Bitmap
     * @return byte[]
     */
    public static byte[] readBitmap(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        try {
            baos.flush();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }


    /**
     * 读取图片旋转的角度
     *
     * @param filename
     * @return
     */
    public static int readPictureDegree(String filename) {
        int rotate = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(filename);
            int result = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch (result) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rotate;
    }

    /**
     * 旋转图片
     *
     * @param angle
     * @param bitmap
     * @return
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (resizedBitmap != bitmap && bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }

        return resizedBitmap;
    }

    /**
     * 读取图片旋转的角度
     *
     * @param filename
     * @return
     */
    public static void setPictureDegree(String filename, int degree) {
        try {
            if (degree == 0) {
                return;
            }

            int rotate = ExifInterface.ORIENTATION_UNDEFINED;
            switch (degree) {
                case 90:
                    rotate = ExifInterface.ORIENTATION_ROTATE_90;
                    break;
                case 180:
                    rotate = ExifInterface.ORIENTATION_ROTATE_180;
                    break;
                case 270:
                    rotate = ExifInterface.ORIENTATION_ROTATE_270;
                    break;
                default:
                    break;
            }

            ExifInterface exifInterface = new ExifInterface(filename);
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION,
                    String.valueOf(rotate));
            exifInterface.saveAttributes();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
