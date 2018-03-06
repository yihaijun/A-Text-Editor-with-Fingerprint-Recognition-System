/**
 * 
 */
package com.tisson.fingerprint.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.zkteco.biometric.FingerprintSensor;
import com.zkteco.biometric.FingerprintSensorErrorCode;
import com.zkteco.biometric.FingerprintSensorEx;
import com.zkteco.biometric.ZKFPService;

/**
 * @author yihaijun
 *
 */
public class ZKFPTest {
	private static boolean mbStop = true;
	private static long mhDevice = 0;
	private static long mhDB = 0;
	
	private static byte[] imgbuf = null;
	private static byte[] template = new byte[2048];
	private static int[] templateLen = new int[1];

	private static WorkThread workThread = null;

	private static int nFakeFunOn = 1;

	//Register
	private static boolean bRegister = false;
	//Identify
	private static boolean bIdentify = true;
	//finger id
	private static int iFid = 1;
	
	private static int enroll_idx = 0;

	private static byte[][] regtemparray = new byte[3][2048];

	//the length of lastRegTemp
	private static int cbRegTemp = 0;
	//for verify test
	private static byte[] lastRegTemp = new byte[2048];
	
	private static int totalFailIdentify = 0;
	
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
	private static java.util.ArrayList<FingerprintVo> fingerprintArry = new java.util.ArrayList<FingerprintVo>();
	private static java.util.ArrayList<FingerprintVo> unknowFingerprintArry = new java.util.ArrayList<FingerprintVo>();
	private static int fpWidth = 0;
	private static int fpHeight = 0;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("["+df.format(new Date())+"] java.library.path="+System.getProperty("java.library.path"));
//		-Djava.library.pat=%JAVA_HOME%/bin;C:\Windows\system32;C:\Windows;%JAVA_HOME%/jre/bin/client;%JAVA_HOME%/jre/bin/;%JAVA_HOME%/jre/lib/i386;D:\after-20170828\tisson2018\FPSensor
//		-Djava.library.pat=%JAVA_HOME%/bin;%JAVA_HOME%/jre/bin/client;%JAVA_HOME%/jre/bin/;%JAVA_HOME%/jre/lib/i386;D:\after-20170828\tisson2018\FPSensor
//		-Djava.library.pat=%JAVA_HOME%/jre/bin/client;%JAVA_HOME%/jre/bin/;%JAVA_HOME%/jre/lib/i386;D:\after-20170828\tisson2018\FPSensor
//		-Djava.library.pat=%JAVA_HOME%/jre/bin/client;%JAVA_HOME%/jre/bin/;%JAVA_HOME%/jre/lib/i386;D:\after-20170828\tisson2018\zkfptest
		int ret  = FingerprintSensorEx.Init();
		if (FingerprintSensorErrorCode.ZKFP_ERR_OK != ret)
		{
			System.out.println("["+df.format(new Date())+"] Init failed!ret =" + ret);
			return;
		}
		ret = FingerprintSensorEx.GetDeviceCount();
		if (ret < 0)
		{
			System.out.println("["+df.format(new Date())+"] No devices connected!");
			FreeSensor();
			return;
		}
		if (0 == (mhDevice = FingerprintSensorEx.OpenDevice(0)))
		{
			System.out.println("["+df.format(new Date())+"] Open device fail, ret = " + ret + "!");
			FreeSensor();
			return;
		}
		if (0 == (mhDB = FingerprintSensorEx.DBInit()))
		{
			System.out.println("["+df.format(new Date())+"] Init DB fail, ret = " + ret + "!");
			FreeSensor();
			return;
		}
		
		System.out.println("["+df.format(new Date())+"] For ISO/Ansi  begin.......");
		int nFmt = 0;	//Ansi
		FingerprintSensorEx.DBSetParameter(mhDB,  5010, nFmt);				
		byte[] paramValue = new byte[4];
		int[] size = new int[1];

		size[0] = 4;
		FingerprintSensorEx.GetParameters(mhDevice, 1, paramValue, size);
		
		fpWidth = byteArrayToInt(paramValue);
		size[0] = 4;
		FingerprintSensorEx.GetParameters(mhDevice, 2, paramValue, size);
		fpHeight = byteArrayToInt(paramValue);
		System.out.println("["+df.format(new Date())+"] fpWidth="+fpWidth+",fpHeight="+fpHeight+","+imgbuf);

		imgbuf = new byte[fpWidth*fpHeight];

		long  beginTime = System.currentTimeMillis();
		loadTestBmp(true,false);
		loadTestBmp(false,false);
		loadTestBase64(false);
		long  endTime = System.currentTimeMillis();
		System.out.println("["+df.format(new Date())+"]  loadTestBmp:"+(endTime-beginTime)+"ms,fingerprintArry.size()="+fingerprintArry.size()+",iFid="+iFid);
		int cycleTimes =0;
		cycleTimes =0;
		beginTime = System.currentTimeMillis();
		for (;cycleTimes < 200;cycleTimes++){
			if(!addTestBmp()){
				break;
			}
		}
		endTime = System.currentTimeMillis();
		System.out.println("["+df.format(new Date())+"]  addTestBmp:"+(endTime-beginTime)+"ms,Cycle times:"+cycleTimes+",fingerprintArry.size()="+fingerprintArry.size()+",iFid="+iFid);

		beginTime = System.currentTimeMillis();
		for (int i =0 ;i< 1;i++){
			identifyBmp(false);
		}
		endTime = System.currentTimeMillis();
		System.out.println("["+df.format(new Date())+"]  identifyBmp:"+(endTime-beginTime)+"ms,ImageWidth="+fpWidth+",ImageHeight="+fpHeight+",fingerprintArry.size()="+fingerprintArry.size()+",iFid="+iFid);
		identifyBmp(true);

		byte[] fpTemplate = new byte[2048];
		int[] sizeFPTemp = new int[1];
		int ret2 = FingerprintSensorEx.ExtractFromImage(mhDB, "d:\\123.bmp",
				500, fpTemplate, sizeFPTemp);
		int ret3 = FingerprintSensorEx.DBAdd(mhDB, iFid, fpTemplate);
		if(ret3==0){
			iFid++;		
		}
		cycleTimes =0;
		beginTime = System.currentTimeMillis();
		for (;cycleTimes < 90;cycleTimes++){
			if(!addTestBmp()){
				break;
			}
		}
		endTime = System.currentTimeMillis();
		System.out.println("["+df.format(new Date())+"]  addTestBmp:"+(endTime-beginTime)+"ms,Cycle times:"+cycleTimes+",fingerprintArry.size()="+fingerprintArry.size()+",iFid="+iFid);
		
		int[] fid = new int[1];
		int[] score = new int[1];
		int ret4 = FingerprintSensorEx.DBIdentify(mhDB, fpTemplate, fid,
				score);
		System.out.println("["+df.format(new Date())+"] path=d:\\123.bmp,DBAdd="+ret3+",DBIdentify="+ret4+",fid=" + fid[0] + ",score=" + score[0]+",fingerprintArry.size()="+fingerprintArry.size()+",iFid="+iFid);

		mbStop = false;
		bRegister = true;
		bIdentify = false;
		
		workThread = new WorkThread();
	    workThread.start();
		
        while(workThread.isAlive() || workThread.isDaemon()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        	
        }
		System.out.println("["+df.format(new Date())+"] workThread.isAlive()=" +workThread.isAlive());

		FreeSensor();
	}
	
	private static void loadTestBase64(boolean printlog) {
		File fileSet = new File("d:\\test\\collect-base64");
		File[] files = null;
		if (fileSet.exists() && fileSet.isDirectory()) {
			files = fileSet.listFiles();
			for (int i = 0; i < files.length; i++) {
				if( !files[i].isFile()){
					continue;
				}
				String path = files[i].getAbsolutePath();
				if(printlog){
					System.out.println("["+df.format(new Date())+"] path=" + path );
				}
				String base64=readFileByChars(path);
				String pathTail = path.substring(path.indexOf("-DBMerge-")+"-DBMerge-".length());
				String lenFlag = pathTail.substring(0, pathTail.length()-4);
				int len = Integer.valueOf(lenFlag).intValue();
				byte[] fpTemplate = new byte[len];
				FingerprintSensorEx.Base64ToBlob(base64,fpTemplate,len);
				 
				int[] sizeFPTemp = new int[1];
				sizeFPTemp[0] = len;
				int ret2 = FingerprintSensorEx.ExtractFromImage(mhDB, path,
						500, fpTemplate, sizeFPTemp);
				int ret3 = FingerprintSensorEx.DBAdd(mhDB, iFid, fpTemplate);
				FingerprintVo vo = new FingerprintVo();
				vo.setName(path);
				vo.setData(fpTemplate);
				vo.setFid(iFid);
				fingerprintArry.add(vo);
				if(ret3 == 0){
					iFid++;
				}
				int[] fid = new int[1];
				int[] score = new int[1];
				int ret4 = FingerprintSensorEx.DBIdentify(mhDB, template, fid,
						score);
				if(printlog){
					System.out.println("["+df.format(new Date())+"] path=" + path + ",ExtractFromImage=" + ret2
							+ ",DBAdd=" + ret3 + ",DBIdentify=" + ret4 + ",iFid="
							+ iFid + ",fid=" + fid[0] + ",score=" + score[0]);
				}
			}
		}
	}
	
	private static void loadTestBmp(boolean normal,boolean printlog) {
		String parentPath="d:\\test\\";
		if(normal){
			parentPath=parentPath+"db";
		}else{
			parentPath=parentPath+"collect";
		}
		File fileSet = new File(parentPath);
		File[] files = null;
		if (fileSet.exists() && fileSet.isDirectory()) {
			files = fileSet.listFiles();
			int ret3 = 0;
			String path = "";
			for (int i = 0; i < files.length; i++) {
				path = files[i].getAbsolutePath();
				if(printlog){
					System.out.println("["+df.format(new Date())+"] path=" + path );
				}
				byte[] fpTemplate = new byte[2048];
				int[] sizeFPTemp = new int[1];
				sizeFPTemp[0] = 2048;
				int ret2 = FingerprintSensorEx.ExtractFromImage(mhDB, path,
						500, fpTemplate, sizeFPTemp);
				if (normal) {
					ret3 = FingerprintSensorEx
							.DBAdd(mhDB, iFid, fpTemplate);
					if (ret3 != 0) {
						continue;
					}
					iFid++;
					FingerprintVo vo = new FingerprintVo();
					vo.setName(path);
					vo.setData(fpTemplate);
					vo.setFid(iFid);
					fingerprintArry.add(vo);
				}else{
					FingerprintVo vo = new FingerprintVo();
					vo.setName(path);
					vo.setData(fpTemplate);
					vo.setFid(iFid);
					unknowFingerprintArry.add(vo);
				}
				int[] fid = new int[1];
				int[] score = new int[1];
				int ret4 = FingerprintSensorEx.DBIdentify(mhDB, template, fid,
						score);
				if(printlog){
					System.out.println("["+df.format(new Date())+"] path=" + path + ",ExtractFromImage=" + ret2
							+ ",DBAdd=" + ret3 + ",DBIdentify=" + ret4 + ",iFid="
							+ iFid + ",fid=" + fid[0] + ",score=" + score[0]);
				}
			}
		}
	}

	
	private static boolean addTestBmp() {
		int[] sizeFPTemp = new int[1];
		sizeFPTemp[0] = 2048;
		for (int i = 0; i < fingerprintArry.size(); i++) {
			FingerprintVo vo = fingerprintArry.get(i);
			int ret3 = FingerprintSensorEx.DBAdd(mhDB, iFid, vo.getData());
			if(ret3 == 0){
				iFid++;
			}else{
				return false;
			}
		}
		return true;
	}

	private static void identifyBmp(boolean printlog) {
		int[] fid = new int[10];
		int[] score = new int[10];
		int ret = 0;

		int[] fid2 = new int[10];
		int[] score2 = new int[10];
		int ret2 = 0;
		FingerprintSensor fsTest = new FingerprintSensor();

		for (int i = 0; i < unknowFingerprintArry.size(); i++) {
			FingerprintVo vo = unknowFingerprintArry.get(i);
			ret = FingerprintSensorEx.DBIdentify(mhDB, vo.getData(), fid, score);
//			fsTest.SetParameter(code, value, len)
			ret2=fsTest.IdentifyFP(vo.getData(), fid2, score2);

			if(printlog){
				StringBuffer outBuf=new StringBuffer();
				outBuf.delete(0, outBuf.length());
				outBuf.append("["+df.format(new Date())+"] path=" + vo.getName() +",DBIdentify="+ret) ;
				for(int j=0;j<fid.length;j++){
					outBuf.append(","+fid[j]+":"+score[j]);
				}
				outBuf.append(":ret2="+ret2);
				for(int j=0;j<fid2.length;j++){
					outBuf.append(","+fid2[j]+":"+score2[j]);
				}
				System.out.println(outBuf.toString());
			}
		}
	}


	private static void FreeSensor()
	{
		mbStop = true;
		try {		//wait for thread stopping
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		if (0 != mhDB)
//		{
//			FingerprintSensorEx.DBFree(mhDB);
//			mhDB = 0;
//		}
		if (0 != mhDevice)
		{
			FingerprintSensorEx.CloseDevice(mhDevice);
			mhDevice = 0;
		}
		FingerprintSensorEx.Terminate();
	}

	public static int byteArrayToInt(byte[] bytes) {
		int number = bytes[0] & 0xFF;  
	    number |= ((bytes[1] << 8) & 0xFF00);  
	    number |= ((bytes[2] << 16) & 0xFF0000);  
	    number |= ((bytes[3] << 24) & 0xFF000000);  
	    return number;  
	 }
	
	private static class WorkThread extends Thread {
        @Override
        public void run() {
            super.run();
            int ret = 0;
            while (!mbStop) {
            	templateLen[0] = 2048;
            	if (0 == (ret = FingerprintSensorEx.AcquireFingerprint(mhDevice, imgbuf, template, templateLen)))
            	{
            		if (nFakeFunOn == 1)
                	{
                		byte[] paramValue = new byte[4];
        				int[] size = new int[1];
        				size[0] = 4;
        				int nFakeStatus = 0;
        				//GetFakeStatus
        				ret = FingerprintSensorEx.GetParameters(mhDevice, 2004, paramValue, size);
        				nFakeStatus = byteArrayToInt(paramValue);
        				System.out.println("["+df.format(new Date())+"] ret = "+ ret +",nFakeStatus=" + nFakeStatus);
        				if (0 == ret && (byte)(nFakeStatus & 31) != 31)
        				{
        					System.out.println("["+df.format(new Date())+"] yhj debug=========Is a fake-finer?");
        					return;
        				}
                	}
                	OnCatpureOK(imgbuf);
                	OnExtractOK(template, templateLen[0]);
            	}
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
		private void OnCatpureOK(byte[] imgBuf)
		{
//			try {
//				writeBitmap(imgBuf, fpWidth, fpHeight, "fingerprint.bmp");
//				btnImg.setIcon(new ImageIcon(ImageIO.read(new File("fingerprint.bmp"))));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		private void OnExtractOK(byte[] template, int len)
		{
			if(bRegister)
			{
				int[] fid = new int[1];
				int[] score = new int [1];
                int ret = FingerprintSensorEx.DBIdentify(mhDB, template, fid, score);
                if (ret == 0)
                {
                    System.out.println("["+df.format(new Date())+"] yhj debug=======the finger already enroll by " + fid[0] + ",cancel enroll");
//                    bRegister = false;
                    enroll_idx = 0;
                    totalFailIdentify = 0;
                    return;
                }
                if (enroll_idx > 0 && FingerprintSensorEx.DBMatch(mhDB, regtemparray[enroll_idx-1], template) <= 0)
                {
                	System.out.println("["+df.format(new Date())+"] yhj debug=======please press the same finger 3 times for the enrollment");
                    enroll_idx = 0;
                	totalFailIdentify ++;
                	if(totalFailIdentify>=2){
	                    bRegister = false;
	                    bIdentify = true;
	                    System.out.println("["+df.format(new Date())+"] yhj debug=======Now register exit!identify begin!");
	                    totalFailIdentify = 0;
                	}
                    return;
                }
                System.arraycopy(template, 0, regtemparray[enroll_idx], 0, 2048);
                enroll_idx++;
                System.out.println("["+df.format(new Date())+"] yhj debug=======now enroll_idx="+enroll_idx);
                if (enroll_idx == 3) {
                	int[] _retLen = new int[1];
                    _retLen[0] = 2048;
                    byte[] regTemp = new byte[_retLen[0]];
                    
                    if (0 == (ret = FingerprintSensorEx.DBMerge(mhDB, regtemparray[0], regtemparray[1], regtemparray[2], regTemp, _retLen)) &&
                    		0 == (ret = FingerprintSensorEx.DBAdd(mhDB, iFid, regTemp))) {
                    	iFid++;
                    	cbRegTemp = _retLen[0];
                        System.arraycopy(regTemp, 0, lastRegTemp, 0, cbRegTemp);
                        
                        String bmpFilePath="d:\\test\\"+new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date())+".bmp";
                        System.out.println("["+df.format(new Date())+"] bmpFilePath="+bmpFilePath);
                        try {
							ZKFPDemo.writeBitmap(regTemp, fpWidth, fpHeight,bmpFilePath );
						} catch (IOException e) {
							e.printStackTrace();
						}
                        
                        //Base64 Template
                        System.out.println("["+df.format(new Date())+"] yhj debug=======enroll succ");
                    } else {
                    	System.out.println("["+df.format(new Date())+"] yhj debug=======enroll fail, error code=" + ret);
                        bRegister = false;
                        bIdentify = true;
                    }
                    enroll_idx = 0;
                    totalFailIdentify = 0;
                } else {
                	System.out.println("["+df.format(new Date())+"] yhj debug=======You need to press the " + (3 - enroll_idx) + " times fingerprint");
                }
			}
			else
			{
				if (bIdentify)
				{
					int[] fid = new int[1];
					int[] score = new int [1];
					int ret = FingerprintSensorEx.DBIdentify(mhDB, template, fid, score);
                    if (ret == 0)
                    {
                    	System.out.println("["+df.format(new Date())+"] yhj debug=======Identify succ, fid=" + fid[0] + ",score=" + score[0]);
                    	totalFailIdentify=0;
                    }
                    else
                    {
                    	totalFailIdentify ++;
                    	System.out.println("["+df.format(new Date())+"] yhj debug=======Identify fail, errcode=" + ret+ ",totalFailIdentify="+totalFailIdentify);
                    	if(totalFailIdentify>=2){
                    		mbStop = true;
                    	}
                    }
                        
				}
				else
				{
					if(cbRegTemp <= 0)
					{
						System.out.println("["+df.format(new Date())+"] yhj debug=======Please register first!");
					}
					else
					{
						int ret = FingerprintSensorEx.DBMatch(mhDB, lastRegTemp, template);
						if(ret > 0)
						{
							System.out.println("["+df.format(new Date())+"] yhj debug=======Verify succ, score=" + ret);
						}
						else
						{
							System.out.println("["+df.format(new Date())+"] yhj debug=======Verify fail, ret=" + ret);
						}
					}
				}
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
//	        try {
////	            System.out.println("以字符为单位读取文件内容，一次读一个字节：");
//	            // 一次读一个字符
//	            reader = new InputStreamReader(new FileInputStream(file));
//	            int tempchar;
//	            while ((tempchar = reader.read()) != -1) {
//	                // 对于windows下，\r\n这两个字符在一起时，表示一个换行。
//	                // 但如果这两个字符分开显示时，会换两次行。
//	                // 因此，屏蔽掉\r，或者屏蔽\n。否则，将会多出很多空行。
////	                if (((char) tempchar) != '\r') {
////	                    System.out.print((char) tempchar);
////	                }
//	            }
//	            reader.close();
//	        } catch (Exception e) {
//	            e.printStackTrace();
//	        }
	        try {
//	            System.out.println("以字符为单位读取文件内容，一次读多个字节：");
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
//	                // 同样屏蔽掉\r不显示
//	                if ((charread == tempchars.length)
//	                        && (tempchars[tempchars.length - 1] != '\r')) {
//	                    System.out.print(tempchars);
//	                } else {
//	                    for (int i = 0; i < charread; i++) {
//	                        if (tempchars[i] == '\r') {
//	                            continue;
//	                        } else {
//	                            System.out.print(tempchars[i]);
//	                        }
//	                    }
//	                }
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
}
