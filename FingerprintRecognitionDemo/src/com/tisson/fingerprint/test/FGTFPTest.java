/**
 * 
 */
package com.tisson.fingerprint.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.tisson.fingerprint.test.FGTFPDemo.fpLibrary;

/**
 * @author yihaijun
 *
 */
public class FGTFPTest {
	public interface fpLibrary extends Library {
		fpLibrary INSTANCE = (fpLibrary)Native.loadLibrary(("fpengine.dll"),	fpLibrary.class);

		int OpenDevice(int comnum,int nbaud,int style);
		int LinkDevice();
		int CloseDevice();
		
		int DevicePrint(byte[] buffer,int size);
		int GetImage(byte[] imagedata,int[] size);
		void GenFpChar();
		void EnrolFpChar();
		int GetWorkMsg();
		int GetRetMsg();
		
		int GetFpCharByGen(byte[] tpbuf,int[] tpsize);
		int GetFpCharByEnl(byte[] fpbuf,int[] fpsize);
		
		int ChangeTemplateType(int type,byte[] input,byte[] output);
		
		int MatchTemplateOne(byte[] pSrcData,byte[] pDstData,int nDstSize);
		int MatchTemplate(byte[] pSrcData,byte[] pDstData);


		int CreateTemplate(byte[] pFingerData,byte[] pTemplate);	
		void CharByteToStr(byte[] buf,int size,char[] str);
		void CharByteToUStr(byte[] buf,int size,char[] str);
		int MatchTemplateEx(char[] pSrcData,char[] pDstData);
		int UMatchTemplateEx(char[] puSrcData,char[] puDstData);

	}
	
	public static int  MatchTemplateOne(byte[] pSrcData,byte[] pDstData,int nDstSize){
		int ret=fpLibrary.INSTANCE.MatchTemplateOne(pSrcData,pDstData,nDstSize);
		return ret;
	}

	public static int CreateTemplate(byte[] pFingerData,byte[] pTemplate){
		int ret=fpLibrary.INSTANCE.CreateTemplate(pFingerData,pTemplate);
		return ret;
	}
	public static void CharByteToStr(byte[] buf,int size,char[] str){
		fpLibrary.INSTANCE.CharByteToStr(buf,size,str);
	}
	public static void CharByteToUStr(byte[] buf,int size,char[] str){
		fpLibrary.INSTANCE.CharByteToUStr(buf,size,str);
	}
	public static int MatchTemplateEx(char[] pSrcData,char[] pDstData){
		int ret=fpLibrary.INSTANCE.MatchTemplateEx(pSrcData,pDstData);
		return ret;
	}
	public static int UMatchTemplateEx(char[] puSrcData,char[] puDstData){
		int ret=fpLibrary.INSTANCE.UMatchTemplateEx(puSrcData,puDstData);
		return ret;
	}


	 /** 
     * NIO way 
     *  
     * @param filename 
     * @return 
     * @throws IOException 
     */  
    public static byte[] toByteArray2(String filename) throws IOException {  
  
        File f = new File(filename);  
        if (!f.exists()) {  
            throw new FileNotFoundException(filename);  
        }  
  
        FileChannel channel = null;  
        FileInputStream fs = null;  
        try {  
            fs = new FileInputStream(f);  
            channel = fs.getChannel();  
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());  
            while ((channel.read(byteBuffer)) > 0) {  
                // do nothing  
                // System.out.println("reading");  
            }  
            return byteBuffer.array();  
        } catch (IOException e) {  
            e.printStackTrace();  
            throw e;  
        } finally {  
            try {  
                channel.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
            try {  
                fs.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
    }  
	 /**
     * 以字符为单位读取文件，常用于读文本，数字等类型的文件
     */
    public static String readFileByChars(String fileName){
    	StringBuffer outBuf = new StringBuffer();
    	outBuf.delete(0, outBuf.length());
        File file = new File(fileName);
        Reader reader = null;
        try {
//            System.out.println("以字符为单位读取文件内容，一次读多个字节：");
            // 一次读多个字符
            char[] tempchars = new char[30];
            int charread = 0;
            reader = new InputStreamReader(new FileInputStream(fileName));
            // 读入多个字符到字符数组中，charread为一次读取字符数
            while ((charread = reader.read(tempchars)) != -1) {
            	if(charread == tempchars.length){
            		outBuf.append(tempchars);
            	}else{
                    for (int i = 0; i < charread; i++) {
                    	outBuf.append(tempchars[i]);
                    }
            	}
//                // 同样屏蔽掉\r不显示
//                if ((charread == tempchars.length)
//                        && (tempchars[tempchars.length - 1] != '\r')) {
//                    System.out.print(tempchars);
//                } else {
//                    for (int i = 0; i < charread; i++) {
//                        if (tempchars[i] == '\r') {
//                            continue;
//                        } else {
//                            System.out.print(tempchars[i]);
//                        }
//                    }
//                }
            }
            return outBuf.toString();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
    }

    public static void main(String[] args) {
		int ret = 0;
		byte[] imageByte = null;
		byte[] imageByteData = null;
		byte[] pFingerData = new  byte[2048];
		char[]  charByteToStr = new char[2048];
		char[]  charByteToUStr = new char[2048];
		try {
			imageByte = toByteArray2("D:\\test\\fgt\\1.bmp");
			imageByteData = new byte[imageByte.length-54];
			for(int i=0;i<imageByteData.length;i++){
				imageByteData[i]=imageByte[i+54];
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		ret = FGTFPTest.CreateTemplate(pFingerData, imageByteData);
//		System.out.println("FGTFPTest.CreateTemplate return "+ ret);
//		FGTFPTest.CharByteToStr(pFingerData,256,charByteToStr);
//		System.out.println("FGTFPTest.CharByteToStr return ["+ String.valueOf(charByteToStr)+"]");
//		FGTFPTest.CharByteToUStr(pFingerData,256,charByteToUStr);
//		System.out.println("CharByteToUStr return ["+ String.valueOf(charByteToUStr)+"]");
		
		String base64 = readFileByChars("D:\\test\\fgt\\1.txt");
		String base64_2 = readFileByChars("D:\\test\\fgt\\2.txt");
		String base64_3 = readFileByChars("D:\\test\\fgt\\3.txt");
		String base64_4 = readFileByChars("D:\\test\\fgt\\4.txt");
		String base64_5 = readFileByChars("D:\\test\\fgt\\5.txt");
		ret = FGTFPTest.MatchTemplateEx(base64.toCharArray(),base64.toCharArray());		
		System.out.println("FGTFPTest.MatchTemplateEx(1,1) return "+ ret);
		ret = FGTFPTest.MatchTemplateEx(base64.toCharArray(),base64.toCharArray());		
		System.out.println("FGTFPTest.MatchTemplateEx(1,) return "+ ret);
		ret = FGTFPTest.MatchTemplateEx(base64.toCharArray(),base64_2.toCharArray());		
		System.out.println("FGTFPTest.MatchTemplateEx(1,2) return "+ ret);
		ret = FGTFPTest.UMatchTemplateEx(base64.toCharArray(),base64_2.toCharArray());		
		System.out.println("FGTFPTest.UMatchTemplateEx(1,2) return "+ ret);
		ret = FGTFPTest.UMatchTemplateEx(base64.toCharArray(),base64_3.toCharArray());		
		System.out.println("FGTFPTest.UMatchTemplateEx(1,3) return "+ ret);
		ret = FGTFPTest.UMatchTemplateEx(base64.toCharArray(),base64_4.toCharArray());		
		System.out.println("FGTFPTest.UMatchTemplateEx(1,4) return "+ ret);
		ret = FGTFPTest.UMatchTemplateEx(base64.toCharArray(),base64_5.toCharArray());		
		System.out.println("FGTFPTest.UMatchTemplateEx(1,5) return "+ ret);
	}
}
