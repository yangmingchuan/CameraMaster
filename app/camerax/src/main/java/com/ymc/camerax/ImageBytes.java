package com.ymc.camerax;

/**
 * Created by ymc on 12/8/20.
 *
 * @Description
 */
class ImageBytes {

    public byte[] bytes;
    public int width;
    public int height;

    public ImageBytes(byte[] bytes, int width, int height) {
        this.bytes = bytes;
        this.width = width;
        this.height = height;
    }

}
