/**
 * 
 */


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * @author yihaijun
 *
 */
public class FGTFPTest {
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

	public interface fpLibrary extends Library {
		fpLibrary INSTANCE = (fpLibrary)Native.loadLibrary((".\\fpengine.dll"),	fpLibrary.class);

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
		String base64_b1 = readFileByChars("D:\\test\\fgt\\1.txt");
		String base64_b2 = readFileByChars("D:\\test\\fgt\\2.txt");
		String base64_1 = readFileByChars("D:\\test\\fgt\\testiso-11.txt");
		String base64_2 = readFileByChars("D:\\test\\fgt\\testiso-22.txt");
		String base64_3 = readFileByChars("D:\\test\\fgt\\testiso-3.txt");

		StringBuffer buf = new StringBuffer();
		buf.delete(0, buf.length());
		for(int i=0;i<3;i++){
			buf.delete(0, buf.length());
			buf.append("[" + df.format(new Date())+"] UMatchTemplateEx:");
			ret = fpLibrary.INSTANCE.UMatchTemplateEx(base64_b1.toCharArray(),base64_b1.toCharArray());		
			buf.append("b1:b1="+ ret+";");
			ret = fpLibrary.INSTANCE.UMatchTemplateEx(base64_b1.toCharArray(),base64_b2.toCharArray());		
			buf.append("b1:b2="+ ret+";");

			ret = fpLibrary.INSTANCE.UMatchTemplateEx(base64_1.toCharArray(),base64_1.toCharArray());		
			buf.append("1:1="+ ret+";");
			ret = fpLibrary.INSTANCE.UMatchTemplateEx(base64_1.toCharArray(),base64_2.toCharArray());		
			buf.append("1:2="+ ret+";");
			ret = fpLibrary.INSTANCE.UMatchTemplateEx(base64_1.toCharArray(),base64_3.toCharArray());		
			buf.append("1:3="+ ret+";");
			buf.append(" [" + df.format(new Date())+"]");
			System.out.println(buf.toString());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
    }
}
