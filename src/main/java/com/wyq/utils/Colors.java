package com.wyq.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public final class Colors {

    /**
     * @see #as(String)
     * 
     * @deprecated
     */
    @Deprecated
    public static Color fromString(String str) {
        return as(str);
    }

    /**
     * 将字符串变成颜色值
     * 
     * @param str
     *            颜色字符串，详细，请参看本类的总体描述，如果为空，则表示黑色
     * @return 颜色对象
     */
    public static Color as(String str) {
        if (null == str){
            return Color.BLACK;
        }
        // 整理一下字符串以便后面匹配分析
        str = StringVerification.trim(str.toUpperCase());

        if (str.startsWith("#")) {
            str = str.substring(1);
        }

        if (str.endsWith(";")) {
            str = str.substring(0, str.length() - 1);
        }

        // RGB: #FFF
        String reg1 = "^([0-9A-F])([0-9A-F])([0-9A-F])$";
        Pattern p = Pattern.compile(reg1);
        Matcher m = p.matcher(str);
        if (m.find()) {
            return new Color(parseInt(StringVerification.dup(m.group(1), 2), 16),
                             parseInt(StringVerification.dup(m.group(2), 2), 16),
                             parseInt(StringVerification.dup(m.group(3), 2), 16));
        }

        // RRGGBB: #F0F0F0
        String reg2 = "^([0-9A-F]{2})([0-9A-F]{2})([0-9A-F]{2})$";
        p = Pattern.compile(reg2);
        m = p.matcher(str);
        if (m.find()) {
            return new Color(parseInt(m.group(1), 16),
                             parseInt(m.group(2), 16),
                             parseInt(m.group(3), 16));
        }

        // ARGB: #9FE5
        String reg3 = "^([0-9A-F])([0-9A-F])([0-9A-F])([0-9A-F])$";
        p = Pattern.compile(reg3);
        m = p.matcher(str);
        if (m.find()) {
            return new Color(parseInt(StringVerification.dup(m.group(2), 2), 16),
                             parseInt(StringVerification.dup(m.group(3), 2), 16),
                             parseInt(StringVerification.dup(m.group(4), 2), 16),
                             parseInt(StringVerification.dup(m.group(1), 2), 16));
        }

        // AARRGGBB: #88FF8899
        String reg4 = "^([0-9A-F]{2})([0-9A-F]{2})([0-9A-F]{2})([0-9A-F]{2})$";
        p = Pattern.compile(reg4);
        m = p.matcher(str);
        if (m.find()) {
            return new Color(parseInt(m.group(2), 16),
                             parseInt(m.group(3), 16),
                             parseInt(m.group(4), 16),
                             parseInt(m.group(1), 16));
        }

        // RGB值: rgb(255,33,89)
        String reg5 = "^RGB\\s*[(]\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*[)]$";
        p = Pattern.compile(reg5);
        m = p.matcher(str);
        if (m.find()) {
            return new Color(parseInt(m.group(1), 10),
                             parseInt(m.group(2), 10),
                             parseInt(m.group(3), 10));
        }

        // // RGBA值: rgba(6,6,6,255)
        // p =
        // Pattern.compile("^RGBA\\s*[(]\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*[)]$");
        // m = p.matcher(str);
        // if (m.find()) {
        // return new Color(parseInt(m.group(1), 10),
        // parseInt(m.group(2), 10),
        // parseInt(m.group(3), 10),
        // parseInt(m.group(4), 10));
        // }

        // RGBA值: rgba(6,6,6,0.9)
        String reg6 = "^RGBA\\s*[(]\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*((\\d[.])?\\d+)\\s*[)]$";
        p = Pattern.compile(reg6);
        m = p.matcher(str);
        if (m.find()) {
            float alpha = Float.parseFloat(m.group(4));

            return new Color(parseInt(m.group(1), 10),
                             parseInt(m.group(2), 10),
                             parseInt(m.group(3), 10),
                             (int) (255.0f * alpha));
        }

        // 全都匹配不上，返回黑色
        return Color.BLACK;
    }

    /**
     * Color转换为 “rgb(12, 25, 33)” 格式字符串
     * 
     * @param color
     *            颜色
     * @return 文字格式
     */
    public static String toRGB(Color color) {
        return String.format("rgb(%d, %d, %d)", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * 获取一个制定范围内的颜色
     * 
     * @param min
     *            最小取值，范围0-255
     * @param max
     *            最大取值，范围0-255
     * @return 颜色
     */
    public static Color randomColor(int min, int max) {
        if (min > 255) {
            min = 255;
        }
        if (min < 0) {
            min = 0;
        }
        if (max > 255) {
            max = 255;
        }
        if (max < 0) {
            max = 0;
        }
        return new Color(min + R.random(0, max - min),
                         min + R.random(0, max - min),
                         min + R.random(0, max - min));
    }

    /**
     * 获取一个随机颜色
     * 
     * @return 颜色
     */
    public static Color randomColor() {
        return randomColor(0, 255);
    }

    /**
     * 获取图片指定像素点的RGB值
     * 
     * @param srcIm
     *            源图片
     * @param x
     *            横坐标
     * @param y
     *            纵坐标
     * @return RGB值数组
     */
    public static int[] getRGB(BufferedImage srcIm, int x, int y) {
        int pixel = srcIm.getRGB(x, y);
        return getRGB(pixel);
    }

    /**
     * 获取像素点的RGB值(三元素数组)）
     * 
     * @param pixel
     *            像素RGB值
     * @return RGB值数组
     */
    public static int[] getRGB(int pixel) {
        int r = (pixel >> 16) & 0xff;
        int g = (pixel >> 8) & 0xff;
        int b = pixel & 0xff;
        return new int[]{r, g, b};
    }

    public static int getAlpha(int pixel) {
        ColorModel cm = ColorModel.getRGBdefault();
        return cm.getAlpha(pixel);
    }

    /**
     * 获取图片指定像素点的亮度值(YUV中的Y)
     * 
     * @param srcIm
     *            源图片
     * @param x
     *            横坐标
     * @param y
     *            纵坐标
     * @return 亮度值
     */
    public static int getLuminance(BufferedImage srcIm, int x, int y) {
        int[] rgb = getRGB(srcIm, x, y);
        return (int) (0.3 * rgb[0] + 0.59 * rgb[1] + 0.11 * rgb[2]); // 加权法
    }

    /**
     * 获取图片指定像素点的亮度值(YUV中的Y) 类型为double
     * 
     * @param srcIm
     *            源图片
     * @param x
     *            横坐标
     * @param y
     *            纵坐标
     * @return 亮度值
     */
    public static double getLuminanceDouble(BufferedImage srcIm, int x, int y) {
        int[] rgb = getRGB(srcIm, x, y);
        return 0.3 * rgb[0] + 0.59 * rgb[1] + 0.11 * rgb[2]; // 加权法
    }

    /**
     * 获取图片指定像素点的灰度值
     * 
     * @param srcIm
     *            源图片
     * @param x
     *            横坐标
     * @param y
     *            纵坐标
     * @return 灰度值
     */
    public static int getGray(BufferedImage srcIm, int x, int y) {
        int grayValue = getLuminance(srcIm, x, y);
        int newPixel = 0;
        newPixel = (grayValue << 16) & 0x00ff0000 | (newPixel & 0xff00ffff);
        newPixel = (grayValue << 8) & 0x0000ff00 | (newPixel & 0xffff00ff);
        newPixel = (grayValue) & 0x000000ff | (newPixel & 0xffffff00);
        return newPixel;
    }

    /**
     * 获取两个像素点正片叠底后的像素值
     * 
     * @param pixel1
     *            像素点1
     * @param pixel2
     *            像素点1
     * @return 新像素点值
     */
    public static int getMultiply(int pixel1, int pixel2) {
        int[] rgb1 = getRGB(pixel1);
        int[] rgb2 = getRGB(pixel2);
        int r = rgb1[0] * rgb2[0] / 255;
        int g = rgb1[1] * rgb2[1] / 255;
        int b = rgb1[2] * rgb2[2] / 255;
        return new Color(r, g, b).getRGB();
    }

    private Colors() {}
}
