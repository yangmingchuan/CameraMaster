package camera.cn.cameramaster.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Bitmap处理工具
 *
 * @date 2019年1月25日 14:27:33
 */
public class BitmapUtils {
    private BitmapUtils() {
    }

    /**
     * 800*480
     */
    private static final int CONFIG_480P = 1;
    /**
     * 1280*720
     */
    private static final int CONFIG_720P = 2;
    /**
     * 1920*1080
     */
    private static final int CONFIG_1080P = 3;
    /**
     * 2560*1440
     */
    private static final int CONFIG_2K = 4;

    private static int getSize(int config) {
        int size = 0;
        switch (config) {
            case CONFIG_480P:
                size = 480;
                break;
            case CONFIG_720P:
                size = 720;
                break;
            case CONFIG_1080P:
                size = 1080;
                break;
            case CONFIG_2K:
                size = 1440;
                break;
            default:
                break;
        }
        return size;
    }

    /**
     * 返回适应屏幕尺寸的位图
     *
     * @param bit    bit
     * @param config config
     */
    private static Bitmap getRightSzieBitmap(Bitmap bit, int config) {
        // 得到理想宽度
        int ww = getSize(config);
        // 获取图片宽度
        int w = bit.getWidth();
        // 计算缩放率
        float rate = 1f;
        if (w > ww) {
            rate = (float) ww / (float) w;
        }
        // 重新绘图
        Bitmap bitmap = Bitmap.createBitmap((int) (bit.getWidth() * rate),
                (int) (bit.getHeight() * rate), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Rect rect = new Rect(0, 0, (int) (bit.getWidth() * rate),
                (int) (bit.getHeight() * rate));
        canvas.drawBitmap(bit, null, rect, null);
        return bitmap;
    }

    /**
     * 返回适应屏幕的位图 更节省内存
     *
     * @param fileName file name
     * @param config   config
     * @return Bitmap
     */
    public static Bitmap getRightSzieBitmap(String fileName, int config) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName, options);
        options.inJustDecodeBounds = false;
        int w = options.outWidth;
        int ww = getSize(config);
        if ((ww * 2) < w) {
            options.inSampleSize = 2;
        }
        // 重新绘图
        Bitmap bitmap = BitmapFactory.decodeFile(fileName, options);
        return getRightSzieBitmap(bitmap, config);
    }


    /**
     * 图片去色,返回灰度图片
     *
     * @param bmpOriginal 传入的图片
     * @return 去色后的图片
     */
    private static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
                Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public static Bitmap replaceBitmapColor(Bitmap oldBitmap, int oldColor, int newColor) {
        Bitmap mBitmap = oldBitmap.copy(Config.ARGB_8888, true);
        int mBitmapWidth = mBitmap.getWidth();
        int mBitmapHeight = mBitmap.getHeight();
        for (int i = 0; i < mBitmapHeight; i++) {
            for (int j = 0; j < mBitmapWidth; j++) {
                int color = mBitmap.getPixel(j, i);
                if (color == oldColor) {
                    //将被替换色替换为需要替换成的颜色附近的值，都替换为相同的颜色略显单调
                    mBitmap.setPixel(j, i, (int) (newColor + Math.random() * 100000));
                }
            }
        }
        return mBitmap;
    }

    /**
     * 将图片 D65 转换 为位图
     *
     * @param bitmap 原来图片
     * @return 新图片
     */
    public static Bitmap ImgaeToNegative(Bitmap bitmap) {
        //其实我们获得宽和高就是图片像素的宽和高
        //它们的乘积就是总共一张图片拥有的像素点数
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap bmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        //用来存储旧的色素点的数组
        int[] oldPx = new int[width * height];
        //用来存储新的像素点的数组
        int[] newPx = new int[width * height];
        int color;//用来存储原来颜色值
        int r, g, b, a;//存储颜色的四个分量：红，绿，蓝，透明度

        //该方法用来将图片的像素写入到oldPx中，我们这样子设置，就会获取全部的像素点
        //第一个参数为写入的数组，第二个参数为读取第一个的像素点的偏移量，一般设置为0
        //第三个参数为写入时，多少个像素点作为一行,第三个和第四个参数为读取的起点坐标
        //第五个参数表示读取的长度，第六个表示读取的高度
        bitmap.getPixels(oldPx, 0, width, 0, 0, width, height);
        // 存放 rgb
        double[] rgbmap = new double[3];
        //下面用循环来处理每一个像素点
        long startTime = System.currentTimeMillis();
        int index = 0;
        for (int i = 0; i < width * height; i++) {
            //获取一个原来的像素点
            color = oldPx[i];
            r = Color.red(color);
            g = Color.green(color);
            b = Color.blue(color);
            a = Color.alpha(color);
            rgbmap[0] = r;
            rgbmap[1] = g;
            rgbmap[2] = b;
            // D65 光源 换算
            double[] xyz = LabUtil.sRGB2XYZ(rgbmap);
            double[] lab = LabUtil.XYZ2Lab(xyz);

            double[] xyz2 = LabUtil.Lab2XYZ(lab);
            double[] rgb = LabUtil.XYZ2sRGB(xyz2);

            //下面计算生成新的颜色分量
            r = (int) rgb[0];
            g = (int) rgb[1];
            b = (int) rgb[2];

            if(rgbmap[0]!=r || rgbmap[1]!=g || rgbmap[2]!=b){
                index ++;
            }

            //下面主要保证r g b 的值都必须在0~255之内
            if (r > 255) {
                r = 255;
            } else if (r < 0) {
                r = 0;
            }
            if (g > 255) {
                g = 255;
            } else if (g < 0) {
                g = 0;
            }
            if (b > 255) {
                b = 255;
            } else if (b < 0) {
                b = 0;
            }
            //下面合成新的像素点，并添加到newPx中
            color = Color.argb(a, r, g, b);
            newPx[i] = color;
        }
        //然后重要的一步，为bmp设置新颜色了,该方法中的参数意义与getPixels中的一样
        //无非是将newPx写入到bmp中
        bmp.setPixels(newPx, 0, width, 0, 0, width, height);
        Log.e("camera2", "图片转换需要时间 ："+ (System.currentTimeMillis()-startTime)
                +" 修改次数："+ index);
        return bmp;
    }

    /**
     * 去色同时加圆角
     *
     * @param bmpOriginal 原图
     * @param pixels      圆角弧度
     * @return 修改后的图片
     */
    public static Bitmap toGrayscale(Bitmap bmpOriginal, int pixels) {
        return toRoundCorner(toGrayscale(bmpOriginal), pixels);
    }

    /**
     * 把图片变成圆角
     *
     * @param bitmap 需要修改的图片
     * @param pixels 圆角的弧度
     * @return 圆角图片
     */
    private static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    /**
     * 使圆角功能支持BitampDrawable
     *
     * @param bitmapDrawable bitmapDrawable
     * @param pixels         pixels
     * @return BitmapDrawable
     */
    @SuppressWarnings("deprecation")
    public static BitmapDrawable toRoundCorner(BitmapDrawable bitmapDrawable,
                                               int pixels) {
        Bitmap bitmap = bitmapDrawable.getBitmap();
        bitmapDrawable = new BitmapDrawable(toRoundCorner(bitmap, pixels));
        return bitmapDrawable;
    }

    /**
     * 读取路径中的图片，然后将其转化为缩放后的bitmap返回
     *
     * @param path path
     */
    public static Bitmap saveBefore(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false;
        // 计算缩放比
        int be = (int) (options.outHeight / (float) 200);
        if (be <= 0)
            be = 1;
        options.inSampleSize = 2; // 图片长宽各缩小二分之一
        // 重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false哦
        bitmap = BitmapFactory.decodeFile(path, options);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        System.out.println(w + "   " + h);
        // savePNG_After(bitmap,path);
        saveJPGE_After(bitmap, path, 90);
        return bitmap;
    }

    /**
     * 将Bitmap转换成指定大小
     *
     * @param bitmap
     * @param width
     * @param height
     * @return
     */
    public static Bitmap createBitmapBySize(Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    // 图片按比例大小压缩方法
    public static Bitmap getImageFromPath(String srcPath, float maxWidth, float maxHeight) {
        /*if (!isFileAtPath(srcPath)) {
            return null;
        }*/
        try {
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
            newOpts.inJustDecodeBounds = true;
            Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空
            newOpts.inJustDecodeBounds = false;
            int w = newOpts.outWidth;
            int h = newOpts.outHeight;
            Log.d("getImageFromPath", "bSize:newOpts.out.w=" + w + " h=" + h);

            float aBili = (float) maxHeight / (float) maxWidth;
            float bBili = (float) h / (float) w;
            // be=1表示不缩放，be=2代表大小变成原来的1/2，注意be只能是2的次幂，即使算出的不是2的次幂，使用时也会自动转换成2的次幂
            int be = 1;
            if (aBili > bBili) {
                if (w > maxWidth) {
                    be = (int) (w / maxWidth);
                }
            } else {
                if (h > maxHeight) {
                    be = (int) (h / maxHeight);
                }
            }
            if (be <= 1) {//如果是放大，则不放大
                be = 1;
            }
            newOpts.inSampleSize = be;// 设置缩放比例
            bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

            int degree = readPictureDegree(srcPath);
            if (degree != 0) {
                bitmap = rotaingImageView(degree, bitmap);
            }
            if (bitmap == null) {
                return null;
            }
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap decodeBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //可以只获取宽高而不加载
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        //计算压缩比例
        options = calculateInSampleSize(options, reqWidth, reqHeight);
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * 图片压缩处理（使用Options的方法）
     *
     * @param reqWidth  目标宽度
     * @param reqHeight 目标高度
     * @使用方法 首先你要将Options的inJustDecodeBounds属性设置为true，BitmapFactory.decode一次图片。
     * 然后将Options连同期望的宽度和高度一起传递到到本方法中。
     * 之后再使用本方法的返回值做参数调用BitmapFactory.decode创建图片。
     * @explain BitmapFactory创建bitmap会尝试为已经构建的bitmap分配内存
     * ，这时就会很容易导致OOM出现。为此每一种创建方法都提供了一个可选的Options参数
     * ，将这个参数的inJustDecodeBounds属性设置为true就可以让解析方法禁止为bitmap分配内存
     * ，返回值也不再是一个Bitmap对象， 而是null。虽然Bitmap是null了，但是Options的outWidth、
     * outHeight和outMimeType属性都会被赋值。
     */
    public static BitmapFactory.Options calculateInSampleSize(
            final BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        // 设置压缩比例
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return options;
    }
/*

    public static BitmapFactory.Options createBitmap(BitmapFactory.Options options ,int bwidth, int bheight, int reqWidth, int reqHeight){
        int inSampleSize = 1;
        if (bheight > reqHeight || bwidth > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) bheight
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) bwidth / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        // 设置压缩比例
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return options;
    }
*/

    //提取图像Alpha位图
    public static Bitmap getAlphaBitmap(Bitmap mBitmap, int mColor) {
        //BitmapDrawable的getIntrinsicWidth（）方法，Bitmap的getWidth（）方法
        //注意这两个方法的区别
        //Bitmap mAlphaBitmap = Bitmap.createBitmap(mBitmapDrawable.getIntrinsicWidth(), mBitmapDrawable.getIntrinsicHeight(), Config.ARGB_8888);
        Bitmap mAlphaBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Config.ARGB_8888);

        Canvas mCanvas = new Canvas(mAlphaBitmap);
        Paint mPaint = new Paint();

        mPaint.setColor(mColor);
        //从原位图中提取只包含alpha的位图
        Bitmap alphaBitmap = mBitmap.extractAlpha();
        //在画布上（mAlphaBitmap）绘制alpha位图
        mCanvas.drawBitmap(alphaBitmap, 0, 0, mPaint);

        return mAlphaBitmap;
    }


    /**
     * 旋转 bitmap
     *
     * @param bmp bitmap
     * @return 旋转后的 bitmap
     */
    public static Bitmap rotateMyBitmap(Bitmap bmp) {
        //*****旋转一下
        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        Bitmap bitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Config.ARGB_8888);

        Bitmap nbmp2 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        return nbmp2;
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }


    /**
     * 保存图片为PNG
     *
     * @param bitmap
     * @param name
     */
    public static void savePNG_After(Bitmap bitmap, String name) {
        File file = new File(name);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存图片为JPEG
     *
     * @param bitmap
     * @param path
     */
    public static boolean saveJPGE_After(Bitmap bitmap, String path, int quality) {
        File file = new File(path);
        makeDir(file);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
                out.flush();
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 保存图片为JPEG
     *
     * @param bitmap
     * @param path
     */
    public static void saveJPGE_After(Context context, Bitmap bitmap, String path, int quality) {
        File file = new File(path);
        makeDir(file);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
                out.flush();
                out.close();
            }
            updateResources(context, file.getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存图片为PNG
     *
     * @param bitmap
     * @param path
     */
    public static void saveJPGE_After_PNG(Context context, Bitmap bitmap, String path, int quality) {
        File file = new File(path);
        makeDir(file);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, quality, out)) {
                out.flush();
                out.close();
            }
            updateResources(context, file.getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存图片为PNG
     *
     * @param bitmap
     * @param path
     */
    public static void saveJPGE_After_WebP(Context context, Bitmap bitmap, String path, int quality) {
        File file = new File(path);
        makeDir(file);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.WEBP, quality, out)) {
                out.flush();
                out.close();
            }
            updateResources(context, file.getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void makeDir(File file) {
        File tempPath = new File(file.getParent());
        if (!tempPath.exists()) {
            tempPath.mkdirs();
        }
    }

    /**
     * 图片合成
     *
     * @param src
     * @param watermark
     * @return
     */
    public static Bitmap createBitmap(Bitmap src, Bitmap watermark) {
        if (src == null) {
            return null;
        }
        int w = src.getWidth();
        int h = src.getHeight();
        int ww = watermark.getWidth();
        int wh = watermark.getHeight();
        // create the new blank bitmap
        Bitmap newb = Bitmap.createBitmap(w, h, Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
        Canvas cv = new Canvas(newb);
        // draw src into
        cv.drawBitmap(src, 0, 0, null);// 在 0，0坐标开始画入src
        // draw watermark into
        cv.drawBitmap(watermark, w - ww + 5, h - wh + 5, null);// 在src的右下角画入水印
        // save all clip
        cv.save(Canvas.ALL_SAVE_FLAG);// 保存
        // store
        cv.restore();// 存储
        return newb;
    }

    /**
     * Bitmap 转 Drawable
     *
     * @param bitmap
     * @return
     */
    public static Drawable bitmapToDrawableByBD(Bitmap bitmap) {
        @SuppressWarnings("deprecation")
        Drawable drawable = new BitmapDrawable(bitmap);
        return drawable;
    }

    /**
     * 将图片转换成byte[]以便能将其存到数据库
     */
    public static byte[] getByteFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            // Log.e(TAG, "transform byte exception");
        }
        return out.toByteArray();
    }

    /**
     * 将数据库中的二进制图片转换成位图
     *
     * @param temp
     * @return
     */
    public static Bitmap getBitmapFromByte(byte[] temp) {
        if (temp != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(temp, 0, temp.length);
            return bitmap;
        } else {
            // Bitmap bitmap=BitmapFactory.decodeResource(getResources(),
            // R.drawable.contact_add_icon);
            return null;
        }
    }

    /**
     * 将手机中的文件转换为Bitmap类型
     *
     * @param f
     * @return
     */
    public static Bitmap getBitemapFromFile(File f) {
        if (!f.exists())
            return null;
        try {
            return BitmapFactory.decodeFile(f.getAbsolutePath());
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 镜像水平翻转
     *
     * @param bmp
     * @return
     */
    public Bitmap convertMirrorBmp(Bitmap bmp) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();

        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1); // 镜像水平翻转
        Bitmap convertBmp = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true);

        return convertBmp;
    }

    /**
     * 垂直翻转
     *
     * @param bmp
     * @return
     */
    public static Bitmap convertVertical(Bitmap bmp) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();

        Matrix matrix = new Matrix();
        matrix.postScale(1, -1); // 镜像垂直翻转
        Bitmap convertBmp = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true);

        return convertBmp;
    }

    /**
     * 将手机中的文件转换为Bitmap类型
     *
     * @param path 期望宽高
     * @return
     */
    public static Bitmap decodeFile(String path, int screenWidth, int screenHeight) {
        Bitmap bm = null;
        BitmapFactory.Options opt = new BitmapFactory.Options();
        //这个isjustdecodebounds很重要
        opt.inJustDecodeBounds = true;
        bm = BitmapFactory.decodeFile(path, opt);

        //获取到这个图片的原始宽度和高度
        int picWidth = opt.outWidth;
        int picHeight = opt.outHeight;

        //isSampleSize是表示对图片的缩放程度，比如值为2图片的宽度和高度都变为以前的1/2
        opt.inSampleSize = 1;
        //根据屏的大小和图片大小计算出缩放比例
        if (picWidth > picHeight) {
            if (picWidth > screenWidth) {
                opt.inSampleSize = picWidth / screenWidth;
            }
        } else {
            if (picHeight > screenHeight) {
                opt.inSampleSize = picHeight / screenHeight;
            }
        }

        //这次再真正地生成一个有像素的，经过缩放了的bitmap
        opt.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(path, opt);
        return bm;
    }

    /**
     * 把资源图片转换成Bitmap
     *
     * @param drawable 资源图片
     * @return 位图
     */
    public static Bitmap getBitmapFromDrawable(Drawable drawable) {
        int width = drawable.getBounds().width();
        int height = drawable.getBounds().height();
        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
                .getOpacity() != PixelFormat.OPAQUE ? Config.ARGB_8888
                : Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }


    /**
     * 把被系统旋转了的图片，转正
     *
     * @param angle 旋转角度
     * @return bitmap 图片
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    /**
     * <<<<<<< HEAD
     * 翻转
     *
     * @param bitmap
     * @return
     */
    public static Bitmap flip(Bitmap bitmap) {
        // 点中了翻转
        Matrix m = new Matrix();
        m.postScale(-1, 1);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        return bitmap;
    }

    /**
     * view turnto bitmap
     * =======
     * 将View转为Bitmap
     * >>>>>>> master
     *
     * @param view
     * @return
     */
    public static Bitmap getViewBitmap(View view) {
        view.clearFocus(); // 清除视图焦点
        view.setPressed(false);// 将视图设为不可点击

        boolean willNotCache = view.willNotCacheDrawing();// 返回视图是否可以保存他的画图缓存
        view.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation //将视图在此操作时置为透明
        int color = view.getDrawingCacheBackgroundColor();// 获得绘制缓存位图的背景颜色
        view.setDrawingCacheBackgroundColor(0);// 设置绘图背景颜色

        if (color != 0) {// 如果获得的背景不是黑色的则释放以前的绘图缓存
            view.destroyDrawingCache();// 释放绘图资源所使用的缓存
        }
        view.buildDrawingCache();// 重新创建绘图缓存，此时的背景色是黑色
        Bitmap cacheBitmap = view.getDrawingCache();// 将绘图缓存得到的,注意这里得到的只是一个图像的引用
        if (cacheBitmap == null) {
            return null;
        }

        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createBitmap(cacheBitmap);// 将位图实例化

        } catch (OutOfMemoryError e) {
            while (bitmap == null) {
                System.gc();
                System.runFinalization();
                bitmap = Bitmap.createBitmap(cacheBitmap);// 将位图实例化
            }
        }


        view.destroyDrawingCache();// Restore the view //恢复视图
        view.setWillNotCacheDrawing(willNotCache);// 返回以前缓存设置
        view.setDrawingCacheBackgroundColor(color);// 返回以前的缓存颜色设置

        return bitmap;
    }


    /**
     * 将View转为Bitmap
     *
     * @param view
     * @return
     */
    public static Bitmap convertViewToBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    /**
     * 将View转为Bitmap
     *
     * @param view
     * @return
     */
    public static Bitmap getBitmapFromView(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.draw(canvas);
        return bitmap;
    }

    public static void updateResources(Context context, String path) {
        MediaScannerConnection.scanFile(context, new String[]{path}, null, null);
    }

    public static Bitmap returnSaturationBitmap(Context context, Bitmap bitmap, int screenWidth, int screenHeight) {
        Bitmap bmp = null;
/*
        int maxWidth = MyApplication.getInstance().getScreenWidth() - SystemUtils.dp2px(context, 20);
        int maxHeight = maxWidth * 4 / 3;*/

        //  - SystemUtils.dp2px(context, 20)
        int reqWidth = screenWidth;
        int reqHeight = reqWidth * 4 / 3;

        bmp = createBitmap(bitmap, reqWidth, reqHeight);

        ColorMatrix cMatrix = new ColorMatrix();
        // 设置饱和度
        cMatrix.setSaturation(0.0f);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));

        Canvas canvas = new Canvas(bmp);
        // 在Canvas上绘制一个已经存在的Bitmap。这样，dstBitmap就和srcBitmap一摸一样了
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return bmp;
    }

    /**
     * 创建期望大小的bitmap
     *
     * @param bitmap
     * @param reqWidth
     * @return
     */
    public static Bitmap createBitmap(Bitmap bitmap, int reqWidth, int reqHeight) {
        Bitmap bmp = null;
        int inSampleSize = 0;

        int bWidth = bitmap.getWidth();
        int bHeight = bitmap.getHeight();

        if (bHeight > reqHeight || bWidth > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) bHeight
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) bWidth / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
        }
        try {
            if (inSampleSize != 0) {
                bmp = Bitmap.createBitmap(bWidth / inSampleSize, bHeight / inSampleSize, Config.ARGB_8888);
            } else {
                bmp = Bitmap.createBitmap(bWidth, bHeight, Config.ARGB_8888);
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            while (bmp == null) {
                System.gc();
                System.runFinalization();
                if (inSampleSize != 0) {
                    bmp = Bitmap.createBitmap(bWidth / inSampleSize, bHeight / inSampleSize, Config.ARGB_8888);
                } else {
                    bmp = Bitmap.createBitmap(bWidth, bHeight, Config.ARGB_8888);
                }
            }
        }

        return bmp;
    }

    /**
     * 圆形Bitmap
     *
     * @param bitmap
     * @return
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(outBitmap);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPX = bitmap.getWidth() / 2;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPX, roundPX, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return outBitmap;
    }

    /**
     * 改变bitmap 对比度
     *
     * @param bitmap
     * @param progress
     * @return
     */
    public static Bitmap returnContrastBitmap(Bitmap bitmap, int progress) {
        //曝光度
        Bitmap contrast_bmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Config.ARGB_8888);
        // int brightness = progress - 127;
        float contrast = (float) ((progress + 64) / 128.0);
        ColorMatrix contrast_cMatrix = new ColorMatrix();
        contrast_cMatrix.set(new float[]{contrast, 0, 0, 0, 0, 0,
                contrast, 0, 0, 0,// 改变对比度
                0, 0, contrast, 0, 0, 0, 0, 0, 1, 0});

        Paint contrast_paint = new Paint();
        contrast_paint.setColorFilter(new ColorMatrixColorFilter(contrast_cMatrix));

        Canvas contrast_canvas = new Canvas(contrast_bmp);
        // 在Canvas上绘制一个已经存在的Bitmap。这样，dstBitmap就和srcBitmap一摸一样了
        contrast_canvas.drawBitmap(bitmap, 0, 0, contrast_paint);

        return contrast_bmp;
    }

}