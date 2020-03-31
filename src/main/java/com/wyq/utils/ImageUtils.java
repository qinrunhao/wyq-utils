package com.wyq.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.util.Base64;
import java.util.Iterator;

/**
 * 对图像操作的简化 API
 * 
 */
@Slf4j
public class ImageUtils {

    public static byte[] compressWidth(byte[] photo, int maxWidth) throws IOException {
        return compress(photo, maxWidth, null);
    }

    public static byte[] compressHeight(byte[] photo, int maxHeight) throws IOException {
        return compress(photo, null, maxHeight);
    }

    /**
     * 图片等比缩放
     */
    private static byte[] compress(byte[] photo, Integer maxWidth, Integer maxHeignt) throws IOException{
        ByteArrayInputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            in = new ByteArrayInputStream(photo);
            BufferedImage image = ImageIO.read(in);
            int srcWidth = image.getWidth();
            int srcHeight = image.getHeight();
            if (maxWidth != null && maxWidth >= srcWidth) {
                return photo;
            }
            if (maxHeignt != null && maxHeignt >= srcHeight) {
                return photo;
            }

            double scale = 0.0;
            if (maxWidth != null && image.getWidth() > maxWidth) {
                scale = (double) maxWidth / image.getWidth();
            }
            if (maxHeignt != null && image.getHeight() > maxHeignt) {
                scale = (double) maxHeignt / image.getHeight();
            }
            int height = (int) (srcHeight * scale);
            int width = (int) (srcWidth * scale);
            AffineTransform scaleTransform = AffineTransform.getScaleInstance(scale, scale);
            AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR);
            BufferedImage image1 = bilinearScaleOp.filter(image, new BufferedImage(width, height, image.getType()));
            out = new ByteArrayOutputStream();
            boolean flag = ImageIO.write(image1, "png", out);
            if(!flag){
                log.error("压缩图片失败");
            }
            photo = out.toByteArray();
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null){
                out.close();
            }
        }
        return photo;
    }

	/**
	 * 根据指定文字内容，生成验证码，字体颜色随机变化。
	 * 
	 * @param content
	 *            文字内容
	 * @return 图像
	 */
	public static BufferedImage createCaptcha(String content, String bgColor) {
		return createCaptcha(content, 0, 0, null, bgColor, null);
	}

	/**
	 * 根据指定文字内容，生成验证码
	 * 
	 * @param content
	 *            文字内容
	 * @param width
	 *            图片宽度
	 * @param height
	 *            图片高度
	 * @param fontColor
	 *            文字颜色 默认黑色
	 * @param bgColor
	 *            背景颜色 默认白色
	 * @return 图像
	 */
	public static BufferedImage createCaptcha(String content, int width, int height, String fontColor, String bgColor,
			String fontName) {
		// 处理下参数
		if (StringUtils.isEmpty(content)) {
			return null;
		}
		boolean isChinese = StringVerification.isChineseCharacter(content.charAt(0));
		if (width <= 0) {
			// 中文字体的话，间距需要多一些
			width = content.length() * (isChinese ? 25 : 20) + 20;
		}
		if (height <= 0) {
			height = 30;
		}
		Color userColor = StringUtils.isEmpty(fontColor) ? null : Colors.as(fontColor);
		Color colorBg = StringUtils.isEmpty(bgColor) ? Colors.randomColor() : Colors.as(bgColor);

		// 生成背景
		BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D gc = im.createGraphics();
		gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gc.setBackground(colorBg);
		gc.clearRect(0, 0, width, height);

		// 加入干扰线
		for (int i = 0; i < 7; i++) {
			gc.setColor(userColor == null ? Colors.randomColor(5, 250) : userColor);
			int x = R.random(0, width);
			int y = R.random(0, height);
			int x1 = R.random(0, width);
			int y1 = R.random(0, height);
			gc.drawLine(x, y, x1, y1);
		}

		// 写入文字
		int rx = 10;
		int ry = isChinese ? height - 8 : height - 10;
		for (int i = 0; i < content.length(); i++) {
			int fontStyle = R.random(0, 3);
			int fontSize = R.random(height - 10, height - 5);
			Font textFont = StringUtils.isEmpty(fontName) ? Fonts.random(fontStyle, fontSize)
					: Fonts.get(fontName, fontStyle, fontSize);
			gc.setColor(userColor == null ? Colors.randomColor(10, 250) : userColor);
			gc.setFont(textFont);
			// 设置字体旋转角度
			int degree = R.random(0, 64) % 30;
			// 正向角度
			gc.rotate(degree * Math.PI / 180, rx, ry);
			gc.drawString(content.charAt(i) + "", rx, ry);
			// 反向角度
			gc.rotate(-degree * Math.PI / 180, rx, ry);
			rx += (isChinese ? 5 : 0) + width / (content.length() + 2);
		}

		// 图像扭曲
		im = twist(im, 1, bgColor);
		return im;
	}

	/**
	 * 扭曲图片
	 * 
	 * @param srcIm
	 *            源图片
	 * @param twistRank
	 *            扭曲程度，默认为1，数值越大扭曲程度越高
	 * @param bgColor
	 *            扭曲后露出的底图填充色，一般选择要源图片的背景色
	 * @return 被扭曲后的图片
	 */
	public static BufferedImage twist(Object srcIm, double twistRank, String bgColor) {
		if (twistRank <= 0) {
			twistRank = 1;
		}
		BufferedImage bufImg = read(srcIm);
		double period = R.random(0, 7) + 3;// 波形的幅度倍数，越大扭曲的程序越高，一般为3
		double phase = R.random(0, 6);// 波形的起始相位，取值区间（0-2＊PI）
		int width = bufImg.getWidth();
		int height = bufImg.getHeight();

		BufferedImage tarIm = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D gc = tarIm.createGraphics();
		gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gc.setBackground(StringUtils.isEmpty(bgColor) ? Colors.randomColor() : Colors.as(bgColor));
		gc.clearRect(0, 0, width, height);

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int nX = pos4twist(twistRank, phase, period, height, i, j);
				int nY = j;
				if (nX >= 0 && nX < width && nY >= 0 && nY < height) {
					tarIm.setRGB(nX, nY, bufImg.getRGB(i, j));
				}
			}
		}
		return tarIm;
	}

	private static int pos4twist(double rank, double phase, double period, int hOrW, int xOrY, int yOrX) {
		double dyOrX = Math.PI * rank * yOrX / hOrW + phase;
		double dxOrY = Math.sin(dyOrX);
		return xOrY + (int) (dxOrY * period);
	}
	
	 /**
     * 将一个图片文件读入内存
     * 
     * @param img
     *            图片文件
     * @return 图片对象
     */
    public static BufferedImage read(Object img) {
        try {
            if (img instanceof BufferedImage) {
                return (BufferedImage) img;
            }
            if (img instanceof CharSequence) {
                return ImageIO.read(FileUtils.checkFile(img.toString()));
            }
            if (img instanceof File) {
                return ImageIO.read((File) img);
            }

            if (img instanceof URL) {
                img = ((URL) img).openStream();
            }

            if (img instanceof InputStream) {
                File tmp = File.createTempFile("tmp_img", ".jpg");
                FileUtils.write(tmp, img);
                try {
                    return read(tmp);
                }
                finally {
                    tmp.delete();
                }
            }
            throw Lang.makeThrow("Unkown img info!! --> " + img);
        }
        catch (IOException e) {
            try {
                InputStream in = null;
                if (img instanceof File) {
                    in = new FileInputStream((File) img);
                }
                else if (img instanceof URL) {
                    in = ((URL) img).openStream();
                }
                else if (img instanceof InputStream) {
                    in = (InputStream) img;
                }
                if (in != null) {
                    return readJpeg(in);
                }
            }
            catch (IOException e2) {
                e2.fillInStackTrace();
            }
            return null;
            // throw Lang.wrapThrow(e);
        }
    }
    
    /**
     * 尝试读取JPEG文件的高级方法,可读取32位的jpeg文件
     * <p/>
     * 来自:
     * http://stackoverflow.com/questions/2408613/problem-reading-jpeg-image-
     * using-imageio-readfile-file
     * 
     */
    private static BufferedImage readJpeg(InputStream in) throws IOException {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("JPEG");
        ImageReader reader = null;
        while (readers.hasNext()) {
            reader = readers.next();
            if (reader.canReadRaster()) {
                break;
            }
        }
        if (reader == null) {
            return null;
        }
        try {
            ImageInputStream input = ImageIO.createImageInputStream(in);
            reader.setInput(input);
            // Read the image raster
            Raster raster = reader.readRaster(0, null);
            BufferedImage image = createJPEG4(raster);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writeJpeg(image, out, 1);
            out.flush();
            return read(new ByteArrayInputStream(out.toByteArray()));
        }
        finally {
            try {
                reader.dispose();
            }
            catch (Throwable e) {}
        }
    }
    
    private static BufferedImage createJPEG4(Raster raster) {
        int w = raster.getWidth();
        int h = raster.getHeight();
        byte[] rgb = new byte[w * h * 3];

        float[] Y = raster.getSamples(0, 0, w, h, 0, (float[]) null);
        float[] Cb = raster.getSamples(0, 0, w, h, 1, (float[]) null);
        float[] Cr = raster.getSamples(0, 0, w, h, 2, (float[]) null);
        float[] K = raster.getSamples(0, 0, w, h, 3, (float[]) null);

        for (int i = 0, imax = Y.length, base = 0; i < imax; i++, base += 3) {
            float k = 220 - K[i], y = 255 - Y[i], cb = 255 - Cb[i], cr = 255 - Cr[i];

            double val = y + 1.402 * (cr - 128) - k;
            val = (val - 128) * .65f + 128;
            rgb[base] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff : (byte) (val + 0.5);

            val = y - 0.34414 * (cb - 128) - 0.71414 * (cr - 128) - k;
            val = (val - 128) * .65f + 128;
            rgb[base + 1] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff : (byte) (val + 0.5);

            val = y + 1.772 * (cb - 128) - k;
            val = (val - 128) * .65f + 128;
            rgb[base + 2] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff : (byte) (val + 0.5);
        }

        raster = Raster.createInterleavedRaster(new DataBufferByte(rgb, rgb.length),
                                                w,
                                                h,
                                                w * 3,
                                                3,
                                                new int[]{0, 1, 2},
                                                null);

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel cm = new ComponentColorModel(cs,
                                                false,
                                                true,
                                                Transparency.OPAQUE,
                                                DataBuffer.TYPE_BYTE);
        return new BufferedImage(cm, (WritableRaster) raster, true, null);
    }
    
    /**
     * 写入一个 JPG 图像
     * 
     * @param im
     *            图像对象
     * @param targetJpg
     *            目标输出 JPG 图像文件
     * @param quality
     *            质量 0.1f ~ 1.0f
     */
    public static void writeJpeg(RenderedImage im, Object targetJpg, float quality) {
        ImageWriter writer = null;
        try {
            writer = ImageIO.getImageWritersBySuffix("jpg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            ImageOutputStream os = ImageIO.createImageOutputStream(targetJpg);
            writer.setOutput(os);
            writer.write((IIOMetadata) null, new IIOImage(im, null, null), param);
            os.flush();
            os.close();
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            if (writer != null) {
                try {
                    writer.dispose();
                }
                catch (Throwable e) {}
            }
        }
    }
    
    /**
     * 将内存中一个图片写入目标文件
     * 
     * @param im
     *            图片对象
     * @param targetFile
     *            目标文件，根据其后缀，来决定写入何种图片格式
     */
    public static void write(RenderedImage im, File targetFile) {
        try {
            ImageIO.write(im, FileUtils.getSuffixName(targetFile), targetFile);
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }
    
    public static String writeBase64String(BufferedImage img, String type) {
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	try {
            ImageIO.write(img, type, bos);
            byte[] imageBytes = bos.toByteArray();
            @SuppressWarnings("static-access")
			String imageString = Base64.getEncoder().encodeToString(imageBytes);
            bos.close();
            return "data:image/" + type + ";base64," + imageString;
        } catch (IOException e) {
        	throw Lang.wrapThrow(e);
        }
    	
    }
    
    public static String writeBase64String(BufferedImage img) {
    	return writeBase64String(img, "png");
    }
}