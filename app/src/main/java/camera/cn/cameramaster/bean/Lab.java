package camera.cn.cameramaster.bean;

/**
 * lab 实体类
 *
 * @packageName: cn.tongue.tonguecamera.bean
 * @fileName: Lab
 * @date: 2019/4/3  13:37
 * @author: ymc
 * @QQ:745612618
 */

public class Lab {

    public double L;
    public double a;
    public double b;

    public double getL() {
        return L;
    }

    public void setL(double l) {
        L = l;
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    @Override
    public String toString() {
        return "Lab l:" + L + " Lab a:" + a + " Lab b:" + b;
    }
}
