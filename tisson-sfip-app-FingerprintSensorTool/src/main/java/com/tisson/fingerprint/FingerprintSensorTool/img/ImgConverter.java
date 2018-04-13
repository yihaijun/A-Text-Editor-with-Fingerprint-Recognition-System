/**
 * 
 */
package com.tisson.fingerprint.FingerprintSensorTool.img;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/** 
 *  
 */

/** 
 * Created on 2011-5-24 Discription:[convert GIF->JPG GIF->PNG PNG->GIF(X) 
 * PNG->JPG ] 
 *  
 * @param source 
 * @param formatName 
 * @param result 
 * @author:dx[hzdengxu@163.com] 
 */
/**
 * @author yihaijun
 * 
 */
public class ImgConverter {
	private String[] args;

	public static void convert(String source, String formatName, String result) {
		try {
			File f = new File(source);
			f.canRead();
			BufferedImage src = ImageIO.read(f);
			ImageIO.write(src, formatName, new File(result));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ImgConverter(String[] args) {
		// TODO Auto-generated constructor stub
		this.args = args;
	}

	public void run() {
		if (this.args.length > 2) {
			convert(this.args[0], this.args[1], this.args[2]);
		}
	}

	/**
	 * 把多张jpg图片合成一张
	 * 
	 * @param pic
	 *            String[] 多个jpg文件名 包含路径
	 * @param newPic
	 *            String 生成的gif文件名 包含路径
	 */
	private synchronized void jpgToGif(String pic[], String newPic) {
		try {
			AnimatedGifEncoder e = new AnimatedGifEncoder();
			e.setRepeat(0);
			e.start(newPic);
			BufferedImage src[] = new BufferedImage[pic.length];
			for (int i = 0; i < src.length; i++) {
				e.setDelay(200); // 设置播放的延迟时间
				src[i] = ImageIO.read(new File(pic[i])); // 读入需要播放的jpg文件
				e.addFrame(src[i]); // 添加到帧中
			}
			e.finish();
		} catch (Exception e) {
			System.out.println("jpgToGif Failed:");
			e.printStackTrace();
		}
	}

	/**
	 * 把gif图片按帧拆分成jpg图片
	 * 
	 * @param gifName
	 *            String 小gif图片(路径+名称)
	 * @param path
	 *            String 生成小jpg图片的路径
	 * @return String[] 返回生成小jpg图片的名称
	 */
	// private synchronized String[] splitGif(String gifName,String path) {
	// try {
	// GifDecoder decoder = new GifDecoder();
	// decoder.read(gifName);
	// int n = decoder.getFrameCount(); //得到frame的个数
	// String[] subPic = new String[n];
	// String tag = this.getTag();
	// for (int i = 0; i < n; i++) {
	// BufferedImage frame = decoder.getFrame(i); //得到帧
	// //int delay = decoder.getDelay(i); //得到延迟时间
	// //生成小的JPG文件
	// subPic[i] = path + String.valueOf(i)+ ".jpg";
	// FileOutputStream out = new FileOutputStream(subPic[i]);
	// ImageIO.write(frame, "jpeg", out);
	// JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
	// encoder.encode(frame); //存盘
	// out.flush();
	// out.close();
	// }
	// return subPic;
	// } catch (Exception e) {
	// System.out.println( "splitGif Failed!");
	// e.printStackTrace();
	// return null;
	// }
	// }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ImgConverter(args).run();
	}
}