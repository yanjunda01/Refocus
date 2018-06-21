package com.arcsoft.refocus.utils;

import java.io.ByteArrayOutputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import com.arcsoft.jpeg.Encoder;
import com.arcsoft.jpeg.Encoder.ColorFormat;

public class YuvUtil {

	private static final int DEFAULT_QUALITY = 90;
	
	public static int yuvToABGR(int y, int u, int v) {
		int r, g, b;

		r = y + (int) 1.402f * v;
		g = y - (int) (0.344f * u + 0.714f * v);
		b = y + (int) 1.772f * u;
		r = r > 255 ? 255 : r < 0 ? 0 : r;
		g = g > 255 ? 255 : g < 0 ? 0 : g;
		b = b > 255 ? 255 : b < 0 ? 0 : b;
		return 0xff000000 | (b << 16) | (g << 8) | r;
	}

	public static Bitmap nv21ToBitmap(byte[] nv21, int width, int height) {
		Bitmap bmp = null;
		try {
			int[] strides = {width, width};//{(width+63)/64*64, (width+63)/64*64};
			YuvImage image = new YuvImage(nv21, ImageFormat.NV21, width, height, strides);
			if (image != null) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				image.compressToJpeg(new Rect(0, 0, width, height), 90, out);
				bmp = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());
				out.close();
			}
			image = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bmp;
	}

	public static byte[] encodeNv21(byte[] nv21, int width, int height) {
		try {
			YuvImage image = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
			if (image != null) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				image.compressToJpeg(new Rect(0, 0, width, height), 90, out);
				byte[] jpeg = out.toByteArray();
				out.close();
				return jpeg;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public byte[] ABGRToYuv420sp(int[] abgr, int width, int height) {
		int len = width * height;
		// yuv格式数组大小，y亮度占len长度，u,v各占len/4长度。
		byte[] yuv = new byte[len * 3 / 2];
		int y, u, v;
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				// 屏蔽ARGB的透明度值
				int rgb = abgr[row * width + col] & 0x00FFFFFF;
				// 像素的颜色顺序为bgr，移位运算。
				int r = rgb & 0xFF;
				int g = (rgb >> 8) & 0xFF;
				int b = (rgb >> 16) & 0xFF;
				// 套用公式
				y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
				u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
				v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;
				// 调整
				y = y < 16 ? 16 : (y > 255 ? 255 : y);
				u = u < 0 ? 0 : (u > 255 ? 255 : u);
				v = v < 0 ? 0 : (v > 255 ? 255 : v);
				// 赋值
				yuv[row * width + col] = (byte) y;
				yuv[len + (row >> 1) * width + (col & ~1) + 0] = (byte) u;
				yuv[len + (row >> 1) * width + (col & ~1) + 1] = (byte) v;
			}
		}
		return yuv;
	}

	public static Bitmap yuv420spToBitmap(byte[] yuv420sp, int width, int height) {
		int frameSize = width * height;
		int[] abgr = new int[frameSize];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int y = (0xff & (yuv420sp[i * width + j]));
				int u = (0xff & (yuv420sp[frameSize + (i >> 1) * width + (j & ~1) + 0]));
				int v = (0xff & (yuv420sp[frameSize + (i >> 1) * width + (j & ~1) + 1]));
				y = y < 16 ? 16 : y;
				int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
				int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
				int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));
				r = r < 0 ? 0 : (r > 255 ? 255 : r);
				g = g < 0 ? 0 : (g > 255 ? 255 : g);
				b = b < 0 ? 0 : (b > 255 ? 255 : b);
				abgr[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
			}
		}
		Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bmp.setPixels(abgr, 0, width, 0, 0, width, height);
		return bmp;
	}

	public static int[] yuv420spToARGB(byte[] yuv420sp, int width, int height) {
		int frameSize = width * height;
		int[] argb = new int[width * height * 4];
		for (int j = 0, yp = 0; j < height; j++) {
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & (yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0) {
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}

				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);

				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;

				argb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
			}
		}
		return argb;
	}

	public static int[] yuv420spToABGR1(byte[] yuv420sp, int width, int height) {
		int size = width * height;
		int offset = size;
		int[] abgr = new int[size];
		int u, v, y1, y2, y3, y4;

		for (int i = 0, k = 0; i < size; i += 2, k += 2) {
			y1 = yuv420sp[i] & 0xff;
			y2 = yuv420sp[i + 1] & 0xff;
			y3 = yuv420sp[width + i] & 0xff;
			y4 = yuv420sp[width + i + 1] & 0xff;

			u = yuv420sp[offset + k] & 0xff;
			v = yuv420sp[offset + k + 1] & 0xff;
			u = u - 128;
			v = v - 128;

			abgr[i] = yuvToABGR(y1, u, v);
			abgr[i + 1] = yuvToABGR(y2, u, v);
			abgr[width + i] = yuvToABGR(y3, u, v);
			abgr[width + i + 1] = yuvToABGR(y4, u, v);

			if (i != 0 && (i + 2) % width == 0)
				i += width;
		}

		return abgr;
	}

	public int[] yuv420spToABGR2(byte[] yuv420sp, int width, int height) {
		int frameSize = width * height;
		int[] abgr = new int[frameSize];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int y = (0xff & (yuv420sp[i * width + j]));
				int u = (0xff & (yuv420sp[frameSize + (i >> 1) * width + (j & ~1) + 0]));
				int v = (0xff & (yuv420sp[frameSize + (i >> 1) * width + (j & ~1) + 1]));
				y = y < 16 ? 16 : y;
				int r = Math.round(1.166f * (y - 16) + 1.596f * (v - 128));
				int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
				int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));
				r = r < 0 ? 0 : (r > 255 ? 255 : r);
				g = g < 0 ? 0 : (g > 255 ? 255 : g);
				b = b < 0 ? 0 : (b > 255 ? 255 : b);
				abgr[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
			}
		}
		return abgr;
	}

	/**
	 * 获取指定点（dstWidth,dstHeight）的RBG值
	 * 
	 * @param yuv420sp
	 * @param width
	 * @param height
	 * @param dstWidth
	 * @param dstHeight
	 * @return
	 */
	public static int getARGBFromYuv420sp(byte[] yuv420sp, int width, int height, int dstWidth, int dstHeight) {
		// Y矩阵长度frameSize , V和U矩阵第一位即frameSize
		final int frameSize = width * height;
		// yp为Y在矩阵中的位置，yph为所需要点的高mHeight-1，ypw为所需要点的宽mWidth
		int yp, yph = dstHeight - 1, ypw = dstWidth;
		yp = width * yph + ypw;
		// uvp为
		// uv在数组中的位置，V和U矩阵第一位即frameSize，yph>>1取值范围（0，0，1，1，2，2...）yph从0开始，即UV数组为Y数组的1/2.
		int uvp = frameSize + (yph >> 1) * width, u = 0, v = 0;
		// 获取Y的数值
		int y = (0xff & (yuv420sp[yp])) - 16;
		if (y < 0)
			y = 0;
		if ((ypw & 1) == 0) {
			v = (0xff & yuv420sp[uvp++]) - 128;
			u = (0xff & yuv420sp[uvp]) - 128;
		} else {
			u = (0xff & yuv420sp[uvp--]) - 128;
			v = (0xff & yuv420sp[uvp]) - 128;
		}
		int y1192 = 1192 * y;
		int r = (y1192 + 1634 * v);
		int g = (y1192 - 833 * v - 400 * u);
		int b = (y1192 + 2066 * u);
		if (r < 0)
			r = 0;
		else if (r > 262143)
			r = 262143;
		if (g < 0)
			g = 0;
		else if (g > 262143)
			g = 262143;
		if (b < 0)
			b = 0;
		else if (b > 262143)
			b = 262143;
		return (0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff));
	}

	public static int yuv422spToARGB(int y, int u, int v) {
		int r = (int) (y + 1.370705 * (v - 128));
		int g = (int) (y - 0.698001 * (v - 128) + 0.337633 * (u - 128));
		int b = (int) (y + 1.732446 * (u - 128));
		if (r > 255)
			r = 255;
		if (g > 255)
			g = 255;
		if (b > 255)
			b = 255;
		if (r < 0)
			r = 0;
		if (g < 0)
			g = 0;
		if (b < 0)
			b = 0;
		return (0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff));
	}

	public static byte[] yuv422spToBGR(byte[] yuv422sp, int width, int height) {
		int index = 0;
		byte[] bgr = new byte[3 * width * height];

		for (int i = 0; i < width * height * 2; i += 4) {

			int pixel_16 = yuv422sp[i + 3] << 24 | yuv422sp[i + 2] << 16 | yuv422sp[i + 1] << 8 | yuv422sp[i + 0];
			int y0 = (pixel_16 & 0x000000ff);
			int u = (pixel_16 & 0x0000ff00) >> 8;
			int y1 = (pixel_16 & 0x00ff0000) >> 16;
			int v = (pixel_16 & 0xff000000) >> 24;

			int argb = yuv422spToARGB(y0, u, v);
			bgr[index++] = (byte) (argb & 0x000000ff); // b
			bgr[index++] = (byte) ((argb & 0x0000ff00) >> 8);// g
			bgr[index++] = (byte) ((argb & 0x00ff0000) >> 16);// r

			argb = yuv422spToARGB(y1, u, v);
			bgr[index++] = (byte) (argb & 0x000000ff);
			bgr[index++] = (byte) ((argb & 0x0000ff00) >> 8);
			bgr[index++] = (byte) ((argb & 0x00ff0000) >> 16);
		}
		return bgr;
	}

	public static byte[] getYuvData(byte[] dataAligned, int width, int height, int nBytesAlign) {
		int yLen = width * height;
		int uvLen = yLen / 2;

		boolean bWidthNeedPadding = (width % nBytesAlign != 0);

		byte[] yuvData = new byte[yLen + uvLen];

		if (bWidthNeedPadding) {
			// padding
			int widthPadding = (nBytesAlign - width % nBytesAlign) % nBytesAlign;
			// Y
			int rowBytes = width + widthPadding;
			for (int row = 0; row < height; row++) {
				System.arraycopy(dataAligned, rowBytes * row, yuvData, row * width, width);
			}
			// UV
			int uvRows = height / 2;
			int uvOffsetSrc = rowBytes * height;
			int uvOffsetDst = width * height;
			for (int row = 0; row < uvRows; row++) {
				System.arraycopy(dataAligned, uvOffsetSrc + rowBytes * row, yuvData, uvOffsetDst + row * width, width);
			}
		} else {
			// Y
			System.arraycopy(dataAligned, 0, yuvData, 0, yLen);
			// UV
			System.arraycopy(dataAligned, yLen, yuvData, yLen, uvLen);
		}

		return yuvData;
	}

	public static byte[] cropYuv420sp(byte[] srcData, int srcWidth, int srcHeight, Rect cropRect) {
		int dstWidth = cropRect.width();
		int dstHeight = cropRect.height();
		byte[] dstData = new byte[dstWidth * dstHeight * 3 / 2];

		// Y
		int yRows = dstHeight;
		int srcOffset = srcWidth * cropRect.top + cropRect.left;
		int dstOffset = 0;
		for (int row = 0; row < yRows; row++) {
			System.arraycopy(srcData, srcOffset, dstData, dstOffset, dstWidth);
			srcOffset += srcWidth;
			dstOffset += dstWidth;
		}

		// UV
		int uvRows = dstHeight / 2;
		srcOffset = srcWidth * srcHeight + srcWidth * cropRect.top / 2 + cropRect.left;
		dstOffset = dstWidth * dstHeight;
		for (int row = 0; row < uvRows; row++) {
			System.arraycopy(srcData, srcOffset, dstData, dstOffset, dstWidth);
			srcOffset += srcWidth;
			dstOffset += dstWidth;
		}

		return dstData;
	}

	public static void swapUV(byte[] data, int width, int height) {
		int len = data.length;
		int yLen = width * height;
		byte temp;
		for (int i = yLen; i < len - 1; i += 2) {
			temp = data[i];
			data[i] = data[i + 1];
			data[i + 1] = temp;
		}
	}
	
	public static byte[] encodeJpeg(byte[] srcData,int width,int height,Encoder.ColorFormat format){
		
		int[] pitches = null;
		if(ColorFormat.PLANAR_YV12.equals(format) || ColorFormat.PLANAR_YU12.equals(format)){
			pitches = new int[]{width,width/2,width/2};
		}else if(ColorFormat.PLANAR_NV21.equals(format) || ColorFormat.PLANAR_NV12.equals(format)){
			pitches = new int[]{width,width,width};
		}else{
			pitches = new int[]{width,width,width};
		}
		byte[] result = Encoder.encode(srcData, pitches, width, height, format, DEFAULT_QUALITY);
		return result;
	}
}

