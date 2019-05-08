package camera.cn.cameramaster.util;


import java.util.Arrays;

/**
 * rgb 转 lab 工具类
 *
 * @fileName: LabUtil
 * @date: 2019/4/3  13:38
 * @author: ymc
 * @QQ:745612618
 */

public class LabUtil {

    public static void main(String[] args) {
        for (int i = 0; i < 24; i++) {
            double[] a = AppConstant.rgbmap[i];
            int fx = 0;
            int fx1 = -30;
            double rrr = a[0] + fx > 255 ? 255 :a[0] + fx;
            double ggg = a[1] + fx > 255 ? 255 :a[1] + fx;
            double bbb = a[2] + fx1 > 255 ? 255 :a[2] + fx1;
            double[] xyz = LabUtil.sRGB2XYZ(new double[]{rrr,ggg,bbb});
            double[] lab = LabUtil.XYZ2Lab(xyz);
            double cha = Math.sqrt(Math.pow((lab[0] - AppConstant.labmap[i][0]), 2)
                    + Math.pow((lab[1] - AppConstant.labmap[i][1]), 2)
                    + Math.pow((lab[2] - AppConstant.labmap[i][2]), 2));
            System.out.println(Arrays.toString(new double[]{rrr,ggg,bbb}) + "   lab[]:"+ Arrays.toString(lab) + "  色差:"+cha);
            System.out.println("lab误差 l: "+(lab[0] - AppConstant.labmap[i][0]) +
                    "  a: "+(lab[1] - AppConstant.labmap[i][1])+"  b: "+(lab[2] - AppConstant.labmap[i][2]));
//            Log.e("", a.toString() + "   lab[]:"+lab.toString() + "  色差:"+cha);
        }
    }

    /**
     * D65  or  D50
     */
    private static boolean hasD50 = false;

    /**
     * lab 转 xyz
     *
     * @param Lab
     * @return
     */
    public static double[] Lab2XYZ(double[] Lab) {
        double[] XYZ = new double[3];
        double L, a, b;
        double fx, fy, fz;
        double Xn, Yn, Zn;
        // D50
        if (hasD50) {
            Xn = 96.42;
            Yn = 100;
            Zn = 82.51;
        } else {
            // D65
            Xn = 95.04;
            Yn = 100;
            Zn = 108.89;
        }
        L = Lab[0];
        a = Lab[1];
        b = Lab[2];

        fy = (L + 16) / 116;
        fx = a / 500 + fy;
        fz = fy - b / 200;

        if (fx > 0.2069) {
            XYZ[0] = Xn * Math.pow(fx, 3);
        } else {
            XYZ[0] = Xn * (fx - 0.1379) * 0.1284;
        }

        if ((fy > 0.2069) || (L > 8)) {
            XYZ[1] = Yn * Math.pow(fy, 3);
        } else {
            XYZ[1] = Yn * (fy - 0.1379) * 0.1284;
        }

        if (fz > 0.2069) {
            XYZ[2] = Zn * Math.pow(fz, 3);
        } else {
            XYZ[2] = Zn * (fz - 0.1379) * 0.1284;
        }

        return XYZ;
    }

    /**
     * xyz 转为 lab
     *
     * @param XYZ
     * @return
     */
    public static double[] XYZ2Lab(double[] XYZ) {
        double[] Lab = new double[3];
        double X, Y, Z;
        X = XYZ[0];
        Y = XYZ[1];
        Z = XYZ[2];
        double Xn, Yn, Zn;
        if (hasD50) {
            // D50
            Xn = 96.42;
            Yn = 100;
            Zn = 82.51;
        } else {
            // D65
            Xn = 95.04;
            Yn = 100;
            Zn = 108.89;
        }
        double XXn, YYn, ZZn;
        XXn = X / Xn;
        YYn = Y / Yn;
        ZZn = Z / Zn;

        double fx, fy, fz;
        if (XXn > 0.008856) {
            fx = Math.pow(XXn, 0.333333);
        } else {
            fx = 7.787 * XXn + 0.137931;
        }
        if (YYn > 0.008856) {
            fy = Math.pow(YYn, 0.333333);
        } else {
            fy = 7.787 * YYn + 0.137931;
        }
        if (ZZn > 0.008856) {
            fz = Math.pow(ZZn, 0.333333);
        } else {
            fz = 7.787 * ZZn + 0.137931;
        }
        Lab[0] = 116 * fy - 16;
        Lab[1] = 500 * (fx - fy);
        Lab[2] = 200 * (fy - fz);

        return Lab;
    }

    /**
     * rgb 转 xyz
     *
     * @return double[]
     */
    public static double[] sRGB2XYZ(double[] RGB) {
        double[] XYZ = new double[3];
        double sR, sG, sB;
        sR = RGB[0];
        sG = RGB[1];
        sB = RGB[2];
        sR /= 255;
        sG /= 255;
        sB /= 255;

        if (sR <= 0.04045) {
            sR = sR / 12.92;
        } else {
            sR = Math.pow(((sR + 0.055) / 1.055), 2.4);
        }

        if (sG <= 0.04045) {
            sG = sG / 12.92;
        } else {
            sG = Math.pow(((sG + 0.055) / 1.055), 2.4);
        }

        if (sB <= 0.04045) {
            sB = sB / 12.92;
        } else {
            sB = Math.pow(((sB + 0.055) / 1.055), 2.4);
        }
        if (hasD50) {
            // D50
            XYZ[0] = 43.6052025 * sR + 38.5081593 * sG + 14.3087414 * sB;
            XYZ[1] = 22.2491598 * sR + 71.6886060 * sG + 6.0621486 * sB;
            XYZ[2] = 1.3929122 * sR + 9.7097002 * sG + 71.4185470 * sB;
        } else {
            // D65
            XYZ[0] = 41.24 * sR + 35.76 * sG + 18.05 * sB;
            XYZ[1] = 21.26 * sR + 71.52 * sG + 7.22 * sB;
            XYZ[2] = 1.93 * sR + 11.92 * sG + 95.05 * sB;
        }
        return XYZ;
    }

    /**
     * xyz 转 rgb
     *
     * @param XYZ double[]
     * @return double[]
     */
    public static double[] XYZ2sRGB(double[] XYZ) {
        double[] sRGB = new double[3];
        double X, Y, Z;
        double dr = 0, dg = 0, db = 0;
        X = XYZ[0];
        Y = XYZ[1];
        Z = XYZ[2];

        if (hasD50) {
            // TODO: 2019/4/3 D50格式暂时没有找到 D50格式
        } else {
            dr = 0.032406 * X - 0.015371 * Y - 0.0049895 * Z;
            dg = -0.0096891 * X + 0.018757 * Y + 0.00041914 * Z;
            db = 0.00055708 * X - 0.0020401 * Y + 0.01057 * Z;
        }

        if (dr <= 0.00313) {
            dr = dr * 12.92;
        } else {
            dr = Math.exp(Math.log(dr) / 2.4) * 1.055 - 0.055;
        }

        if (dg <= 0.00313) {
            dg = dg * 12.92;
        } else {
            dg = Math.exp(Math.log(dg) / 2.4) * 1.055 - 0.055;
        }

        if (db <= 0.00313) {
            db = db * 12.92;
        } else {
            db = Math.exp(Math.log(db) / 2.4) * 1.055 - 0.055;
        }

        dr = dr * 255;
        dg = dg * 255;
        db = db * 255;

        dr = Math.min(255, dr);
        dg = Math.min(255, dg);
        db = Math.min(255, db);

        sRGB[0] = dr + 0.5;
        sRGB[1] = dg + 0.5;
        sRGB[2] = db + 0.5;

        return sRGB;
    }

}
