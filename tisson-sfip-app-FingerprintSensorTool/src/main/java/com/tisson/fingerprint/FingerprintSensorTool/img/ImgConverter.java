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
	 * �Ѷ���jpgͼƬ�ϳ�һ��
	 * 
	 * @param pic
	 *            String[] ���jpg�ļ��� ����·��
	 * @param newPic
	 *            String ���ɵ�gif�ļ��� ����·��
	 */
	private synchronized void jpgToGif(String pic[], String newPic) {
		try {
			AnimatedGifEncoder e = new AnimatedGifEncoder();
			e.setRepeat(0);
			e.start(newPic);
			BufferedImage src[] = new BufferedImage[pic.length];
			for (int i = 0; i < src.length; i++) {
				e.setDelay(200); // ���ò��ŵ��ӳ�ʱ��
				src[i] = ImageIO.read(new File(pic[i])); // ������Ҫ���ŵ�jpg�ļ�
				e.addFrame(src[i]); // ��ӵ�֡��
			}
			e.finish();
		} catch (Exception e) {
			System.out.println("jpgToGif Failed:");
			e.printStackTrace();
		}
	}

	/**
	 * ��gifͼƬ��֡��ֳ�jpgͼƬ
	 * 
	 * @param gifName
	 *            String СgifͼƬ(·��+����)
	 * @param path
	 *            String ����СjpgͼƬ��·��
	 * @return String[] ��������СjpgͼƬ������
	 */
	// private synchronized String[] splitGif(String gifName,String path) {
	// try {
	// GifDecoder decoder = new GifDecoder();
	// decoder.read(gifName);
	// int n = decoder.getFrameCount(); //�õ�frame�ĸ���
	// String[] subPic = new String[n];
	// String tag = this.getTag();
	// for (int i = 0; i < n; i++) {
	// BufferedImage frame = decoder.getFrame(i); //�õ�֡
	// //int delay = decoder.getDelay(i); //�õ��ӳ�ʱ��
	// //����С��JPG�ļ�
	// subPic[i] = path + String.valueOf(i)+ ".jpg";
	// FileOutputStream out = new FileOutputStream(subPic[i]);
	// ImageIO.write(frame, "jpeg", out);
	// JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
	// encoder.encode(frame); //����
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