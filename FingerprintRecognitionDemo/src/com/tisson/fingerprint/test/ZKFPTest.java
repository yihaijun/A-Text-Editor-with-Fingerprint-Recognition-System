/**
 * 
 */
package com.tisson.fingerprint.test;

import com.zkteco.biometric.FingerprintSensorErrorCode;
import com.zkteco.biometric.FingerprintSensorEx;

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
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("java.library.pat="+System.getProperty("java.library.path"));
//		-Djava.library.pat=%JAVA_HOME%/bin;C:\Windows\system32;C:\Windows;%JAVA_HOME%/jre/bin/client;%JAVA_HOME%/jre/bin/;%JAVA_HOME%/jre/lib/i386;D:\after-20170828\tisson2018\FPSensor
//		-Djava.library.pat=%JAVA_HOME%/bin;%JAVA_HOME%/jre/bin/client;%JAVA_HOME%/jre/bin/;%JAVA_HOME%/jre/lib/i386;D:\after-20170828\tisson2018\FPSensor
//		-Djava.library.pat=%JAVA_HOME%/jre/bin/client;%JAVA_HOME%/jre/bin/;%JAVA_HOME%/jre/lib/i386;D:\after-20170828\tisson2018\FPSensor
//		-Djava.library.pat=%JAVA_HOME%/jre/bin/client;%JAVA_HOME%/jre/bin/;%JAVA_HOME%/jre/lib/i386;D:\after-20170828\tisson2018\zkfptest
		int ret  = FingerprintSensorEx.Init();
		if (FingerprintSensorErrorCode.ZKFP_ERR_OK != ret)
		{
			System.out.println("Init failed!ret =" + ret);
			return;
		}
		ret = FingerprintSensorEx.GetDeviceCount();
		if (ret < 0)
		{
			System.out.println("No devices connected!");
			FreeSensor();
			return;
		}
		if (0 == (mhDevice = FingerprintSensorEx.OpenDevice(0)))
		{
			System.out.println("Open device fail, ret = " + ret + "!");
			FreeSensor();
			return;
		}
		if (0 == (mhDB = FingerprintSensorEx.DBInit()))
		{
			System.out.println("Init DB fail, ret = " + ret + "!");
			FreeSensor();
			return;
		}
		System.out.println("yhj debug=========For ISO/Ansi  begin.......");

		int nFmt = 0;	//Ansi
		FingerprintSensorEx.DBSetParameter(mhDB,  5010, nFmt);				
		byte[] paramValue = new byte[4];
		int[] size = new int[1];

		size[0] = 4;
		FingerprintSensorEx.GetParameters(mhDevice, 1, paramValue, size);
		int fpWidth = 0;
		//the height of fingerprint image
		int fpHeight = 0;
		
		fpWidth = byteArrayToInt(paramValue);
		size[0] = 4;
		FingerprintSensorEx.GetParameters(mhDevice, 2, paramValue, size);
		fpHeight = byteArrayToInt(paramValue);
		System.out.println("yhj debug=========fpWidth="+fpWidth+",fpHeight="+fpHeight);
		//width = fingerprintSensor.getImageWidth();
		//height = fingerprintSensor.getImageHeight();
		imgbuf = new byte[fpWidth*fpHeight];
//		btnImg.resize(fpWidth, fpHeight);

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
		System.out.println("yhj debug=========workThread.isAlive()=" +workThread.isAlive());

		FreeSensor();
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
            	//�ɼ�ָ��ͼ��ָ��ģ��
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
        				System.out.println("ret = "+ ret +",nFakeStatus=" + nFakeStatus);
        				if (0 == ret && (byte)(nFakeStatus & 31) != 31)
        				{
        					System.out.println("yhj debug=========Is a fake-finer?");
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
                    System.out.println("yhj debug=======the finger already enroll by " + fid[0] + ",cancel enroll");
//                    bRegister = false;
                    enroll_idx = 0;
                    totalFailIdentify = 0;
                    return;
                }
                if (enroll_idx > 0 && FingerprintSensorEx.DBMatch(mhDB, regtemparray[enroll_idx-1], template) <= 0)
                {
                	System.out.println("yhj debug=======please press the same finger 3 times for the enrollment");
                    enroll_idx = 0;
                	totalFailIdentify ++;
                	if(totalFailIdentify>=2){
	                    bRegister = false;
	                    bIdentify = true;
	                    System.out.println("yhj debug=======Now register exit!identify begin!");
	                    totalFailIdentify = 0;
                	}
                    return;
                }
                System.arraycopy(template, 0, regtemparray[enroll_idx], 0, 2048);
                enroll_idx++;
                System.out.println("yhj debug=======now enroll_idx="+enroll_idx);
                if (enroll_idx == 3) {
                	int[] _retLen = new int[1];
                    _retLen[0] = 2048;
                    byte[] regTemp = new byte[_retLen[0]];
                    
                    if (0 == (ret = FingerprintSensorEx.DBMerge(mhDB, regtemparray[0], regtemparray[1], regtemparray[2], regTemp, _retLen)) &&
                    		0 == (ret = FingerprintSensorEx.DBAdd(mhDB, iFid, regTemp))) {
                    	iFid++;
                    	cbRegTemp = _retLen[0];
                        System.arraycopy(regTemp, 0, lastRegTemp, 0, cbRegTemp);
                        //Base64 Template
                        System.out.println("yhj debug=======enroll succ");
                    } else {
                    	System.out.println("yhj debug=======enroll fail, error code=" + ret);
                        bRegister = false;
                        bIdentify = true;
                    }
                    enroll_idx = 0;
                    totalFailIdentify = 0;
                } else {
                	System.out.println("yhj debug=======You need to press the " + (3 - enroll_idx) + " times fingerprint");
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
                    	System.out.println("yhj debug=======Identify succ, fid=" + fid[0] + ",score=" + score[0]);
                    	totalFailIdentify=0;
                    }
                    else
                    {
                    	totalFailIdentify ++;
                    	System.out.println("yhj debug=======Identify fail, errcode=" + ret+ ",totalFailIdentify="+totalFailIdentify);
                    	if(totalFailIdentify>=2){
                    		mbStop = true;
                    	}
                    }
                        
				}
				else
				{
					if(cbRegTemp <= 0)
					{
						System.out.println("yhj debug=======Please register first!");
					}
					else
					{
						int ret = FingerprintSensorEx.DBMatch(mhDB, lastRegTemp, template);
						if(ret > 0)
						{
							System.out.println("yhj debug=======Verify succ, score=" + ret);
						}
						else
						{
							System.out.println("yhj debug=======Verify fail, ret=" + ret);
						}
					}
				}
			}
		}
	}
}
