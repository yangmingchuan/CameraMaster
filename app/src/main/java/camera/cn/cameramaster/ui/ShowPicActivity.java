package camera.cn.cameramaster.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.widget.ImageView;
import android.widget.SeekBar;

import butterknife.BindView;
import camera.cn.cameramaster.R;
import camera.cn.cameramaster.base.BaseActivity;
import camera.cn.cameramaster.util.AppConstant;

/**
 * 显示照片界面
 */
public class ShowPicActivity extends BaseActivity {
    @BindView(R.id.img)
    ImageView iv;
    @BindView(R.id.seekBar1)
    SeekBar seekBarRed;
    @BindView(R.id.seekBar2)
    SeekBar seekBarGreen;
    @BindView(R.id.seekBar3)
    SeekBar seekBarB;
    @BindView(R.id.seekBar4)
    SeekBar seekBarH;

    private Canvas canvas;
    //颜色矩阵
    private ColorMatrix colorMatrix;
    private Paint paint;
    private int picWidth;
    private int picHeight;
    private Bitmap bitmap;
    private Bitmap alterBitemp;

    private int redProgress = 128;
    private int greenProgress = 128;
    private int blueProgress = 128;
    private int aplaraProgress = 128;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_show_pic;
    }

    @Override
    protected void initView() {
        seekBarRed.setOnSeekBarChangeListener(new seekBar1Listen());
        seekBarGreen.setOnSeekBarChangeListener(new seekBar2Listen());
        seekBarB.setOnSeekBarChangeListener(new seekBar3Listen());
        seekBarH.setOnSeekBarChangeListener(new seekBar4Listen());
    }

    @Override
    protected void initData() {
        picWidth = getIntent().getIntExtra(AppConstant.KEY.PIC_WIDTH, 0);
        picHeight = getIntent().getIntExtra(AppConstant.KEY.PIC_HEIGHT, 0);
//        iv.setImageURI(Uri.parse(getIntent().getStringExtra(AppConstant.KEY.IMG_PATH)));
//        iv.setLayoutParams(new RelativeLayout.LayoutParams(picWidth, picHeight));
        canvasBitmap(getIntent().getStringExtra(AppConstant.KEY.IMG_PATH));
        colorMatrix.set(new float[]{
                -1,0,0,0,255,
                0,-1,0,0,255,
                0,0,-1,0,255,
                0,0,0,aplaraProgress/128.0f,0,
        });
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(bitmap, new Matrix(), paint);
        iv.setImageBitmap(alterBitemp);
    }

    /**
     *  画布-修改图片
     */
    private void canvasBitmap(String path) {
        //返回图像bitmap对象
        bitmap = BitmapFactory.decodeFile(path, null);
        //可修改olterbitmap  属性与 bitmap一致
        alterBitemp = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),bitmap.getConfig());
        System.out.println(bitmap.getWidth()+" ："+bitmap.getHeight());

        //画布
        canvas = new Canvas(alterBitemp);
        //canvas.drawBitmap()画笔-合成模式
        colorMatrix = new ColorMatrix();
        paint = new Paint();
        paint.setColor(Color.BLACK);
        //设置画笔过滤器-new()的颜色矩阵
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        paint.setAntiAlias(true);
        canvas.drawBitmap(bitmap, new Matrix(), paint);
        //显示imageView
        iv.setImageBitmap(alterBitemp);

    }

    /**
     * Red 修改监听事件
     */
    class seekBar1Listen implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
            redProgress = progress;
            colorMatrix.set(new float[]{
                    redProgress/128.0f,0,0,0,0,
                    0,greenProgress/128.0f,0,0,0,
                    0,0,blueProgress/128.0f,0,0,
                    0,0,0,aplaraProgress/128.0f,0,
            });
            paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            canvas.drawBitmap(bitmap, new Matrix(), paint);
            iv.setImageBitmap(alterBitemp);
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }

    /**
     * green 修改监听事件
     */
    class seekBar2Listen implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
            greenProgress = progress;
            colorMatrix.set(new float[]{
                    redProgress/128.0f,0,0,0,0,
                    0,greenProgress/128.0f,0,0,0,
                    0,0,blueProgress/128.0f,0,0,
                    0,0,0,aplaraProgress/128.0f,0,
            });
            paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            canvas.drawBitmap(bitmap, new Matrix(), paint);
            iv.setImageBitmap(alterBitemp);
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }

    /**
     * blue 修改监听事件
     */
    class seekBar3Listen implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
            blueProgress = progress;
            colorMatrix.set(new float[]{
                    redProgress/128.0f,0,0,0,0,
                    0,greenProgress/128.0f,0,0,0,
                    0,0,blueProgress/128.0f,0,0,
                    0,0,0,aplaraProgress/128.0f,0,
            });
            paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            canvas.drawBitmap(bitmap, new Matrix(), paint);
            iv.setImageBitmap(alterBitemp);
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }

    /**
     * height 修改监听事件
     */
    class seekBar4Listen implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
            aplaraProgress = progress;
            colorMatrix.set(new float[]{
                    redProgress/128.0f,0,0,0,0,
                    0,greenProgress/128.0f,0,0,0,
                    0,0,blueProgress/128.0f,0,0,
                    0,0,0,aplaraProgress/128.0f,0,
            });
            paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            canvas.drawBitmap(bitmap, new Matrix(), paint);
            iv.setImageBitmap(alterBitemp);
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }


}
