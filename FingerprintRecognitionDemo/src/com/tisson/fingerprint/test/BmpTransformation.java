/**
 * 
 */
package com.tisson.fingerprint.test;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author yihaijun
 * 
 */
public class BmpTransformation {

	public static Image loadAnyMemoryImageSource(int nwidth, int nheight,int[] ndata,byte [] data) {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image image = kit.createImage(new MyMemoryImageSource(nwidth, nheight,
				ndata, 0, nwidth,data));
		return image;
	}
	
	/**
	 * 读取任意图片文件
	 * 
	 * @param file
	 * @return
	 */
	public static MyMemoryImageSource loadAnyImage(String file) {
		InputStream fs = null;
		try {
			fs = new BufferedInputStream(new FileInputStream(file));
			int bflen = 14;
			byte[] bf = new byte[bflen];
			fs.read(bf, 0, bflen); // 读取14字节BMP文件头
			int bilen = 40;
			byte[] bi = new byte[bilen];
			fs.read(bi, 0, bilen); // 读取40字节BMP信息头
			// 获取一些重要数据
			int nwidth = (((int) bi[7] & 0xff) << 24) // 源图宽度
					| (((int) bi[6] & 0xff) << 16)
					| (((int) bi[5] & 0xff) << 8) | (int) bi[4] & 0xff;
			int nheight = (((int) bi[11] & 0xff) << 24) // 源图高度
					| (((int) bi[10] & 0xff) << 16)
					| (((int) bi[9] & 0xff) << 8) | (int) bi[8] & 0xff;
			// 位数
			int nbitcount = (((int) bi[15] & 0xff) << 8) | (int) bi[14] & 0xff;
			// 源图大小
			int nsizeimage = (((int) bi[23] & 0xff) << 24)
					| (((int) bi[22] & 0xff) << 16)
					| (((int) bi[21] & 0xff) << 8) | (int) bi[20] & 0xff;
			if(nsizeimage == 0){
				nsizeimage=nwidth*nheight;
			}
			
			int nclrused = (((int)bi[35]&0xff)<<24)  
			       | (((int)bi[34]&0xff)<<16)  
			       | (((int)bi[33]&0xff)<<8)  
			       | (int)bi[32]&0xff;  
			
			// 对24位BMP进行解析
			if (nbitcount == 24) {
				int npad = (nsizeimage / nheight) - nwidth * 3;
				int[] ndata = new int[nheight * nwidth];
				byte[] brgb = new byte[(nwidth + npad) * 3 * nheight];
				fs.read(brgb, 0, (nwidth + npad) * 3 * nheight);
				int nindex = 0;
				for (int j = 0; j < nheight; j++) {
					for (int i = 0; i < nwidth; i++) {
						ndata[nwidth * (nheight - j - 1) + i] = (255 & 0xff) << 24
								| (((int) brgb[nindex + 2] & 0xff) << 16)
								| (((int) brgb[nindex + 1] & 0xff) << 8)
								| (int) brgb[nindex] & 0xff;
						nindex += 3;
					}
					nindex += npad;
				}
				Toolkit kit = Toolkit.getDefaultToolkit();
				return new MyMemoryImageSource(nwidth, nheight,
						ndata, 0, nwidth,brgb);
			}else if (nbitcount == 8){// 8位BMP或其他图片格式
				return  readDataFromBit8(nwidth ,nheight,nclrused,nbitcount,nsizeimage,fs );
			}else{
				return  readDataFromBit8(nwidth ,nheight,nclrused,nbitcount,nsizeimage,fs );
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != fs) {
					fs.close();
					fs = null;
				}
			} catch (IOException e) {
			}
		}
		return null;
	}


    
	private static MyMemoryImageSource readDataFromBit8(int nwidth ,int nheight,int nclrused,int nbitcount,int nsizeimage,InputStream fs ) throws IOException{
	       // Have to determine the number of colors, the clrsused  
	       // parameter is dominant if it is greater than zero.  If  
	       // zero, calculate colors based on bitsperpixel.  
	       int nNumColors = 0;  
		    int  ndata8[] = null;
	       if (nclrused > 0)  
	           {  
	           nNumColors = nclrused;  
	           }  
	       else  
	           {  
	           nNumColors = (1&0xff)<<nbitcount;  
	           }  
	       System.out.println("The number of Colors is "+nNumColors);  
	       // Some bitmaps do not have the sizeimage field calculated  
	       // Ferret out these cases and fix 'em.  
	       if (nsizeimage == 0)  
	           {  
	           nsizeimage = ((((nwidth*nbitcount)+31) & 31 ) >> 3);  
	           nsizeimage *= nheight;  
	           System.out.println("nsizeimage (backup) is "+nsizeimage);  
	           }  
	       // Read the palatte colors.  
	       int  npalette[] = new int [nNumColors];  
	       byte bpalette[] = new byte [nNumColors*4];  
	       fs.read(bpalette,0,nNumColors*4);  
	       int nindex8 = 0;  
	       for (int n = 0; n < nNumColors; n++)  
	           {  
	           npalette[n] = (255&0xff)<<24  
	           | (((int)bpalette[nindex8+2]&0xff)<<16)  
	           | (((int)bpalette[nindex8+1]&0xff)<<8)  
	           | (int)bpalette[nindex8]&0xff;  
//	           // System.out.println ("Palette Color "+n  
//	           +" is:"+npalette[n]+" (res,R,G,B)= ("  
//	           +((int)(bpalette[nindex8+3]) & 0xff)+","  
//	           +((int)(bpalette[nindex8+2]) & 0xff)+","  
//	           +((int)bpalette[nindex8+1]&0xff)+","  
//	           +((int)bpalette[nindex8]&0xff)+")");  
	           nindex8 += 4;  
	           }  
	       // Read the image data (actually indices into the palette)  
	       // Scan lines are still padded out to even 4-byte  
	       // boundaries.  
	       int npad8 = (nsizeimage / nheight) - nwidth;  
	       System.out.println("nPad is:"+npad8);  
	       ndata8 = new int [nwidth*nheight];  
	       byte bdata[] = new byte [(nwidth+npad8)*nheight];  
	       fs.read (bdata, 0, (nwidth+npad8)*nheight);  
	       nindex8 = 0;  
	       for (int j8 = 0; j8 < nheight; j8++)  
	           {  
	           for (int i8 = 0; i8 < nwidth; i8++)  
	           {  
	           ndata8 [nwidth*(nheight-j8-1)+i8] =  
	               npalette [((int)bdata[nindex8]&0xff)];  
	           nindex8++;  
	           }  
	           nindex8 += npad8;
	           }  
//	       byte []data=new byte[nNumColors*4+(nwidth+npad8)*nheight];
//	       for(int i=0;i<nNumColors*4;i++){
//	    	   data[i]=bpalette[i];
//	       }
//	       for(int i=nNumColors*4;i<nNumColors*4+(nwidth+npad8)*nheight;i++){
//	    	   data[i]=bdata[i-nNumColors*4];
//	       }
	       byte []data=bdata;
			return new MyMemoryImageSource(nwidth, nheight,
				    ndata8, 0, nwidth,data);
//	       Image image = createImage  
//	           ( new MyMemoryImageSource (nwidth, nheight,  
//	                        ndata8, 0, nwidth));  
	}
}