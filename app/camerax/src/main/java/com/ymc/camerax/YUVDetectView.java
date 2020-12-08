package com.ymc.camerax;

/**
 * Created by ymc on 12/8/20.
 *
 * @Description
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.ImageReader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class YUVDetectView extends FrameLayout {

    ImageView[] ivs;
    CheckBox cb;
    boolean isFlip = false;
    boolean isShowing = false;
    int rotation = 0;
    byte[] buf;

    public YUVDetectView(@NonNull Context context) {
        this(context, null);
    }

    public YUVDetectView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YUVDetectView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.view_yuv_detect, this);

        ivs = new ImageView[]{
                findViewById(R.id.iv1), // I420
                findViewById(R.id.iv2), // YV12
                findViewById(R.id.iv3), // NV12
                findViewById(R.id.iv4), // NV21
        };
        cb = findViewById(R.id.cb);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isFlip = isChecked;
            }
        });

        View btn = findViewById(R.id.btn);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                rotation = (rotation + 90) % 360;
            }
        });
    }

    public void input(final ImageReader imageReader) {
        final ImageBytes imageBytes = YUVTools.getBytesFromImageReader(imageReader);
        if(imageBytes != null) {
            final int w = isFlip ? imageBytes.height : imageBytes.width;
            final int h = isFlip ? imageBytes.width : imageBytes.height;
            displayImage(imageBytes.bytes, w, h);
        }
    }

    public void input(final Image image) {
        final ImageBytes imageBytes = YUVTools.getBytesFromImage(image);
        if(imageBytes != null) {
            final int w = isFlip ? imageBytes.height : imageBytes.width;
            final int h = isFlip ? imageBytes.width : imageBytes.height;
            displayImage(imageBytes.bytes, w, h);
        }
    }

    public void inputAsync(final byte[] data, int width, int height) {
        final int w = isFlip ? height : width;
        final int h = isFlip ? width : height;

        if (isShowing) return;
        isShowing = true;
        new Thread() {
            @Override
            public void run() {
                displayImage(data, w, h);
                isShowing = false;
            }
        }.start();
    }

    private void displayImage(byte[] data, int w, int h) {
        long time = System.currentTimeMillis();

        if(buf == null) {
            buf = new byte[data.length];
        }
        int rw = rotation % 180 == 0 ? w : h, rh = rotation % 180 == 0 ? h : w; // rotated

        YUVTools.rotateP(data, buf, w, h, rotation);
        final Bitmap b0 = YUVTools.i420ToBitmap(buf, rw, rh);

        YUVTools.rotateP(data, buf, w, h, rotation);
        final Bitmap b1 = YUVTools.yv12ToBitmap(buf, rw, rh);

        YUVTools.rotateSP(data, buf, w, h, rotation);
        final Bitmap b2 = YUVTools.nv12ToBitmap(buf, rw, rh);

        YUVTools.rotateSP(data, buf, w, h, rotation);
        final Bitmap b3 = YUVTools.nv21ToBitmap(buf, rw, rh);

        time = System.currentTimeMillis() - time;
        Log.d("YUVDetectView", "convert time: " + time);
        post(new Runnable() {
            @Override
            public void run() {
                if (b0 != null) ivs[0].setImageBitmap(b0);
                if (b1 != null) ivs[1].setImageBitmap(b1);
                if (b2 != null) ivs[2].setImageBitmap(b2);
                if (b3 != null) ivs[3].setImageBitmap(b3);
            }
        });
    }

}

