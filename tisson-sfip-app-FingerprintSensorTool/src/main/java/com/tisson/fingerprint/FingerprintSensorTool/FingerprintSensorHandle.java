/**
 * 
 */
package com.tisson.fingerprint.FingerprintSensorTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jboss.resteasy.util.Base64;

import lombok.extern.slf4j.Slf4j;

import com.zkteco.biometric.FingerprintSensor;
import com.zkteco.biometric.FingerprintSensorErrorCode;
import com.zkteco.biometric.FingerprintSensorEx;
import com.zkteco.biometric.ZKFPService;

/**
 * @author yihaijun
 * 
 */
@Slf4j
public class FingerprintSensorHandle {
	private static java.util.ArrayList<FingerprintVo> fingerprintArry = new java.util.ArrayList<FingerprintVo>();
	private static java.util.ArrayList<FingerprintVo> unknowFingerprintArry = new java.util.ArrayList<FingerprintVo>();

	private static String manufacturer = "";
	private static int deviceCount = 0;

	private static String cmdPrompt = "";

	// private static FingerprintSensor fsTest = new FingerprintSensor();
	private static SimpleDateFormat df = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss:SSS");

	private static WorkThread workThread = null;
	private static boolean mbStop = true;
	private static long mhDevice = 0;
	private static long mhDB = 0;
	private static int nFakeFunOn = 1;
	private static byte[] imgbuf = null;
	private static byte[] template = new byte[2048];
	private static int[] templateLen = new int[1];
	private static boolean bCollection = false;
	private static boolean bRegister = false;
	private static boolean bIdentify = false;
	private static int enroll_idx = 0;

	private static byte[][] regtemparray = new byte[3][2048];

	// the length of lastRegTemp
	private static int cbRegTemp = 0;
	// for verify test
	private static byte[] lastRegTemp = new byte[2048];

	private static int totalFailIdentify = 0;

	// the width of fingerprint image
	private static int fpWidth = 0;
	// the height of fingerprint image
	private static int fpHeight = 0;

	private static int iFid = 1;

	private static String currentOwner="";
	private static String currentOwnerRegTempBase64="";
	
	/**
	 * @return the currentOwnerRegTempBase64
	 */
	public static String getCurrentOwnerRegTempBase64() {
		return currentOwnerRegTempBase64;
	}

	/**
	 * @return the currentOwner
	 */
	public static String getCurrentOwner() {
		return currentOwner;
	}

	/**
	 * @return the manufacturer
	 */
	public static String getManufacturer() {
		manufacturer = "";
		manufacturer += getStrParameter(1101);
		manufacturer += getStrParameter(1102);
		manufacturer += "(SN=" + getStrParameter(1103) + ")";
		return manufacturer;
	}

	public static int getDeviceCount() {
		return deviceCount;
	}

	public static int getFingerprintArryCount() {
		return fingerprintArry.size();
	}

	public static int load() {
		if (deviceCount > 0) {
			return 0;
		}
		int ret = FingerprintSensorEx.Init();
		if (FingerprintSensorErrorCode.ZKFP_ERR_OK != ret
				&& FingerprintSensorErrorCode.ZKFP_ERR_ALREADY_INIT != ret) {
			log.info("[" + df.format(new Date()) + "] Init failed!ret =" + ret);
			return 0;
		}
		deviceCount = FingerprintSensorEx.GetDeviceCount();
		if (deviceCount < 0) {
			log.info("[" + df.format(new Date()) + "] No devices connected!");
			FreeSensor();
			return 0;
		}
		if (0 == (mhDevice = FingerprintSensorEx.OpenDevice(0))) {
			log.info("[" + df.format(new Date()) + "] Open device fail, ret = "
					+ ret + "!");
			FreeSensor();
			return 0;
		}
		log.info("mhDevice="+mhDevice);
		if (0 == (mhDB = FingerprintSensorEx.DBInit())) {
			log.info("[" + df.format(new Date()) + "] Init DB fail, ret = "
					+ ret + "!");
			FreeSensor();
			return 0;
		}
		log.info("manufacturer=" + getManufacturer());

		if(!bCollection){
			loadTestBase64(false);
			loadTestBmp(true, false);
		}
		// For ISO/Ansi
		int nFmt = 0; // Ansi
		FingerprintSensorEx.DBSetParameter(mhDB, 5010, nFmt);
		// For ISO/Ansi End

		// set fakefun off
		// FingerprintSensorEx.SetParameter(mhDevice, 2002,
		// changeByte(nFakeFunOn), 4);

		byte[] paramValue = new byte[4];
		int[] size = new int[1];
		// GetFakeOn
		// size[0] = 4;
		// FingerprintSensorEx.GetParameters(mhDevice, 2002, paramValue, size);
		// nFakeFunOn = byteArrayToInt(paramValue);

		size[0] = 4;
		FingerprintSensorEx.GetParameters(mhDevice, 1, paramValue, size);
		fpWidth = byteArrayToInt(paramValue);
		size[0] = 4;
		FingerprintSensorEx.GetParameters(mhDevice, 2, paramValue, size);
		fpHeight = byteArrayToInt(paramValue);
		// width = fingerprintSensor.getImageWidth();
		// height = fingerprintSensor.getImageHeight();
		imgbuf = new byte[fpWidth * fpHeight];

		mbStop = false;
		workThread = new WorkThread();
		workThread.start();// 线程启动

		log.info("fingerprintArry.size()=" + fingerprintArry.size());
		return 0;
	}

	public static int cmdCollection() {
		load();
		bCollection = true;
		enroll_idx = 0;
		bRegister = false;
		bIdentify = false;
		cmdPrompt = "please press the same finger 3 times for the enrollment";
//		FreeSensor();
//		deviceCount=0;
//		load();
		return 0;
	}
	public static int cmdEnroll(String owner) {
		currentOwner=owner;
		enroll_idx = 0;
		bCollection = false;
		bRegister = true;
		bIdentify = false;
		cmdPrompt = "please press the same finger 3 times for the enrollment";
		return 0;
	}

	public static int cmdIdentify() {
		enroll_idx = 0;
		bCollection = false;
		bRegister = false;
		bIdentify = true;
		cmdPrompt = "";
		return 0;
	}

	public static String cmdVerify(String path) {
		enroll_idx = 0;
		bCollection = false;
		bRegister = false;
		bIdentify = false;
		cmdPrompt = "";
		return identifyBmpFile(path);
	}

	public static String cmdTxtVerify(String path) {
		enroll_idx = 0;
		bCollection = false;
		bRegister = false;
		bIdentify = false;
		cmdPrompt = "";
		return identifyText(path);
	}

	/**
	 * @return the cmdPrompt
	 */
	public static String getCmdPrompt() {
		return cmdPrompt;
	}

	private static String getStrParameter(int code) {
		byte[] paramValue = new byte[100];
		int[] paramLen = new int[1];
		paramLen[0] = 100;
		int ret = FingerprintSensorEx.GetParameters(mhDevice, code, paramValue,
				paramLen);
		if(ret!=0){
			cmdPrompt = "GetParameters("+code+") failed,ret="+ret;
			return "";
		}
		String result = "";
		for (int i = 0; i < paramLen[0]; i++) {
			result = result + (char) paramValue[i];
		}
		return result;
	}

	private static boolean loadTestBase64(boolean printlog) {
		File fileSet = new File("..\\fingerprint\\collect-base64");
		File[] files = null;
		if (!(fileSet.exists() && fileSet.isDirectory())) {
			return false;
		}
		files = fileSet.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (!files[i].isFile()) {
				continue;
			}
			String path = files[i].getAbsolutePath();
			if (printlog) {
				log.info("[" + df.format(new Date()) + "] path=" + path);
			}
			String base64 = readFileByChars(path);
			String pathTail = path.substring(path.indexOf("-DBMerge-")
					+ "-DBMerge-".length());
			String lenFlag = pathTail.substring(0, pathTail.length() - 4);
			int len = Integer.valueOf(lenFlag).intValue();
			byte[] fpTemplate = new byte[len];
			FingerprintSensorEx.Base64ToBlob(base64, fpTemplate, len);

			int[] sizeFPTemp = new int[1];
			sizeFPTemp[0] = len;
			// int ret2 = FingerprintSensorEx.ExtractFromImage(path, 500,
			// fpTemplate,
			// sizeFPTemp);
			int ret2 = ZKFPService.ExtractFromImage(path, 500, fpTemplate,
					sizeFPTemp);
			iFid= java.lang.Math.max(iFid,FingerprintSensorEx.DBCount(mhDevice)+1);
			int ret3 = FingerprintSensorEx.DBAdd(mhDevice, iFid, fpTemplate);
			if (ret3 == 0) {
				FingerprintVo vo = new FingerprintVo();
				vo.setName(path);
				vo.setData(fpTemplate);
				String preKey="..\\fingerprint\\collect-base64\\fingerprint-";
				if(path.indexOf(preKey)>=0){
					vo.setOwner(path.substring(preKey.length(),path.indexOf("-",preKey.length()))); 
				}
				vo.setFid(iFid);
				fingerprintArry.add(vo);
				iFid++;
			} else {
				return false;
			}
			if (printlog) {
				if(log.isTraceEnabled()){
					log.trace("[" + df.format(new Date()) + "] path=" + path
								+ ",ExtractFromImage=" + ret2 + ",DBAdd="
								+ ret3);
				}
			}
		}
		return true;
	}

	private static boolean loadTestBmp(boolean normal, boolean printlog) {
		String parentPath = "..\\fingerprint\\";
		if (normal) {
			parentPath = parentPath + "db";
		} else {
			parentPath = parentPath + "collect";
		}
		File fileSet = new File(parentPath);
		File[] files = null;
		if (fileSet.exists() && fileSet.isDirectory()) {
			files = fileSet.listFiles();
			int ret3 = 0;
			String path = "";
			for (int i = 0; i < files.length; i++) {
				path = files[i].getAbsolutePath();
				if (printlog) {
					log.info("[" + df.format(new Date()) + "] path=" + path);
				}
				byte[] fpTemplate = new byte[2048];
				int[] sizeFPTemp = new int[1];
				sizeFPTemp[0] = 2048;
				int ret2 = FingerprintSensorEx.ExtractFromImage(mhDevice, path,
						500, fpTemplate, sizeFPTemp);
				if (normal) {
					iFid= java.lang.Math.max(iFid,FingerprintSensorEx.DBCount(mhDevice)+1);
					ret3 = FingerprintSensorEx
							.DBAdd(mhDevice, iFid, fpTemplate);
					if (ret3 != 0) {
						return false;
					}
					FingerprintVo vo = new FingerprintVo();
					vo.setName(path);
					vo.setData(fpTemplate);
					vo.setFid(iFid);
					String preKey="..\\fingerprint\\db\\fingerprint-";
					if(path.indexOf(preKey)>=0){
						vo.setOwner(path.substring(preKey.length(),path.indexOf("-",preKey.length()))); 
					}
					fingerprintArry.add(vo);
					iFid++;
				} else {
					FingerprintVo vo = new FingerprintVo();
					vo.setName(path);
					vo.setData(fpTemplate);
					vo.setFid(iFid);
					unknowFingerprintArry.add(vo);
				}
				if (printlog) {
					log.info("[" + df.format(new Date()) + "] path=" + path
							+ ",ExtractFromImage=" + ret2 + ",DBAdd=" + ret3);
				}
			}
		}
		return true;
	}

	private static void verifyBatch(boolean printlog, int minScore) {
		for (int i = 0; i < unknowFingerprintArry.size(); i++) {
			FingerprintVo vo = unknowFingerprintArry.get(i);
			String buf = verifyFPByID(vo.getData(), minScore);
			if (printlog) {
				log.info("[" + df.format(new Date()) + "] path=" + vo.getName()
						+ ": " + buf);
			}
		}
	}

	private static String verifyFPByID(byte[] fpBytes, int minScore) {
		int ret = 0;
		StringBuffer buf = new StringBuffer();
		buf.delete(0, buf.length());
		int[] fid = new int[10];
		int[] score = new int[10];
		ret = FingerprintSensorEx.DBIdentify(mhDevice, fpBytes, fid, score);
		if (ret != 0 && minScore > 0) {
			return "";
		}
		int pos = 0;
		if (ret == 0) {
			pos = fid[0];
		}
		int count = 0;
		for (int j = pos; j < iFid && count < 10; j++) {
			ret = ZKFPService.VerifyFPByID(j, fpBytes);
			if (ret <= minScore) {
				continue;
			}
			count++;
			buf.append(j + "/" + ret + ";");
		}
		return buf.toString();
	}


	private static String identifyBmpFile(String path) {
		int[] fid = new int[10];
		int[] score = new int[10];
		int ret = 0;
		FingerprintSensor fsTest = new FingerprintSensor();

		byte[] fpTemplate = new byte[2048];
		int[] sizeFPTemp = new int[1];
		sizeFPTemp[0] = 2048;
		ret = FingerprintSensorEx.ExtractFromImage(mhDevice, path, 500,
				fpTemplate, sizeFPTemp);
        if(ret!=0){
        	return "ExtractFromImage fail,ret="+ret;
        }
		ret = FingerprintSensorEx.DBIdentify(mhDevice, fpTemplate, fid, score);
        if(ret!=0){
        	return "DBIdentify fail,ret="+ret;
        }
		StringBuffer outBuf = new StringBuffer();
		outBuf.delete(0, outBuf.length());
		outBuf.append("DBIdentify=" + ret);
		for (int j = 0; j < fid.length; j++) {
			outBuf.append("," + fid[j] + ":" + score[j]);
		}
		return outBuf.toString();
	}

	private static String identifyText(String text) {
		int[] fid = new int[10];
		int[] score = new int[10];
		int ret = 0;
		FingerprintSensor fsTest = new FingerprintSensor();

		String []arry=text.split(":");
		String lenFlag = arry[1];
		int len = Integer.valueOf(lenFlag).intValue();
		
		String base64 = text.substring(("libzkfp:"+len).length()+1);
		if(log.isTraceEnabled()){
			log.trace("ifpTemplate base64="+base64);
		}
		
		byte[] fpTemplate = new byte[len];
		FingerprintSensorEx.Base64ToBlob(base64, fpTemplate, len);

		ret = FingerprintSensorEx.DBIdentify(mhDevice, fpTemplate, fid, score);
        if(ret!=0){
        	return "DBIdentify fail,ret="+ret;
        }
        StringBuffer outBuf = new StringBuffer();
		outBuf.delete(0, outBuf.length());

		FingerprintVo vo = getFingerprintVo(fid[0]);
		outBuf.append(vo.getOwner()+ "=" + score[0] + ";");
		int pos = fid[0]+1;
		int count =0;
		for (int j = pos; j < iFid && count<10; j++) {
			ret = ZKFPService.VerifyFPByID(j, fpTemplate);
			if(ret<=30){
				continue;
			}
			vo = getFingerprintVo(j);
			outBuf.append(vo.getOwner()+ "=" + score[0] + ";");
		}
		return outBuf.toString();
	}

	private static FingerprintVo getFingerprintVo(int fid){
		for(int i=0;i<fingerprintArry.size();i++){
			if(fingerprintArry.get(i).getFid()==fid){
				return fingerprintArry.get(i);
			}
		}
		return null;
	}
	
	private static String identifyTxtFile(String path) {
		int[] fid = new int[10];
		int[] score = new int[10];
		int ret = 0;
		FingerprintSensor fsTest = new FingerprintSensor();

		String base64 = readFileByChars(path);
		String pathTail = path.substring(path.indexOf("-DBMerge-")
				+ "-DBMerge-".length());
		String lenFlag = pathTail.substring(0, pathTail.length() - 4);
		int len = Integer.valueOf(lenFlag).intValue();
		byte[] fpTemplate = new byte[len];
		FingerprintSensorEx.Base64ToBlob(base64, fpTemplate, len);

		ret = FingerprintSensorEx.DBIdentify(mhDevice, fpTemplate, fid, score);
        if(ret!=0){
        	return "DBIdentify fail,ret="+ret;
        }
		StringBuffer outBuf = new StringBuffer();
		outBuf.delete(0, outBuf.length());
		outBuf.append("DBIdentify=" + ret);
		for (int j = 0; j < fid.length; j++) {
			outBuf.append("," + fid[j] + ":" + score[j]);
		}
		return outBuf.toString();
	}

	private static void identifyBmp(boolean printlog) {
		int[] fid = new int[10];
		int[] score = new int[10];
		int ret = 0;
		FingerprintSensor fsTest = new FingerprintSensor();

		for (int i = 0; i < unknowFingerprintArry.size(); i++) {
			FingerprintVo vo = unknowFingerprintArry.get(i);
			ret = FingerprintSensorEx.DBIdentify(mhDevice, vo.getData(), fid,
					score);
			if (printlog) {
				StringBuffer outBuf = new StringBuffer();
				outBuf.delete(0, outBuf.length());
				outBuf.append("[" + df.format(new Date()) + "] path="
						+ vo.getName() + ",DBIdentify=" + ret);
				for (int j = 0; j < fid.length; j++) {
					outBuf.append("," + fid[j] + ":" + score[j]);
				}
				log.info(outBuf.toString());
			}
		}
	}

	private static boolean addTestBmp() {
		int[] sizeFPTemp = new int[1];
		sizeFPTemp[0] = 2048;
		for (int i = 0; i < fingerprintArry.size(); i++) {
			FingerprintVo vo = fingerprintArry.get(i);
			iFid= java.lang.Math.max(iFid,FingerprintSensorEx.DBCount(mhDevice)+1);
			int ret3 = FingerprintSensorEx.DBAdd(mhDevice, iFid, vo.getData());
			if (ret3 == 0) {
				iFid++;
			} else {
				return false;
			}
		}
		return true;
	}

	private static boolean bmpToKey() {
		String parentPath = "..\\fingerprint\\bmp";
		File fileSet = new File(parentPath);
		File[] files = null;
		if (fileSet.exists() && fileSet.isDirectory()) {
			files = fileSet.listFiles();
			int ret2 = 0;
			int ret3 = 0;
			String path = "";
			for (int i = 0; i < files.length; i++) {
				path = files[i].getAbsolutePath();
				byte[] fpTemplate = new byte[2048];
				int[] sizeFPTemp = new int[1];
				sizeFPTemp[0] = 2048;
				ret2 = FingerprintSensorEx.ExtractFromImage(mhDevice, path,
						500, fpTemplate, sizeFPTemp);
				int[] _retLen = new int[1];
				_retLen[0] = 2048;
				byte[] regTemp = new byte[_retLen[0]];
				ret3 = ZKFPService.GenRegFPTemplate(fpTemplate, fpTemplate,
						fpTemplate, regTemp, _retLen);
				if (ret3 != 0) {
					continue;
				}
				try {
					String fileKeyPath = "..\\fingerprint\\bmp-key\\"
							+ files[i].getName()
							+ "-"
							+ new SimpleDateFormat("yyyyMMdd-HHmmss-SSS")
									.format(new Date()) + "-DBMerge-"
							+ _retLen[0] + ".txt";
					File fileKey = new File(fileKeyPath);
					if (fileKey.exists()) {
						fileKey.delete();
					}
					fileKey.createNewFile();
					FileOutputStream fs = new FileOutputStream(fileKey, true); // 在该文件的末尾添加内容
					fs.write(FingerprintSensorEx.BlobToBase64(regTemp,
							_retLen[0]).getBytes());
				} catch (Throwable t) {
					log.error("",t);
				}
			}
		}
		return true;
	}

	public static int byteArrayToInt(byte[] bytes) {
		int number = bytes[0] & 0xFF;
		// "|="按位或赋值。
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

				if(log.isInfoEnabled()){
					log.trace("FingerprintSensorEx.AcquireFingerprint begin ...");
				}
				ret = FingerprintSensorEx.AcquireFingerprint(
						mhDevice, imgbuf, template, templateLen);
				if(log.isInfoEnabled()){
					log.trace("FingerprintSensorEx.AcquireFingerprint return "+ret);
				}

				if (0 == (ret)) {
					if (nFakeFunOn == 1) {
						byte[] paramValue = new byte[4];
						int[] size = new int[1];
						size[0] = 4;
						int nFakeStatus = 0;
						// GetFakeStatus
						ret = FingerprintSensorEx.GetParameters(mhDevice, 2004,
								paramValue, size);
						nFakeStatus = byteArrayToInt(paramValue);
						if (0 == ret && (byte) (nFakeStatus & 31) != 31) {
							cmdPrompt = "Is a fake-finer?";
							log.warn("ret = "+ ret +",nFakeStatus=" + nFakeStatus+",Is a fake-finer?");
							if(log.isInfoEnabled()){
								log.trace(cmdPrompt);
							}
							return;
						}
					}
					OnCatpureOK(imgbuf);
					OnExtractOK(template, templateLen[0]);
					log.info(cmdPrompt);
				} else {
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					log.error("",e);
				}

			}
		}

		private void runOnUiThread(Runnable runnable) {
		}
	}

	private static void OnCatpureOK(byte[] imgBuf) {
		try {
			writeBitmap(imgBuf, fpWidth, fpHeight, "..\\fingerprint\\fingerprint.bmp");
			// btnImg.setIcon(new ImageIcon(ImageIO.read(new
			// File("fingerprint.bmp"))));
		} catch (IOException e) {
			log.error("",e);
		}
	}

	private static void OnExtractOK(byte[] template, int len) {
		if (bRegister || bCollection) {
			int ret = 0;
			if(bRegister){
				int[] fid = new int[1];
				int[] score = new int[1];
				ret = FingerprintSensorEx
					.DBIdentify(mhDB, template, fid, score);
				if (ret == 0) {
					cmdPrompt = "the finger already enroll by " + fid[0]
						+ ",cancel enroll";
//				bRegister = false;
					enroll_idx = 0;
					return;
				}
			}
			if (enroll_idx > 0
					&& FingerprintSensorEx.DBMatch(mhDB,
							regtemparray[enroll_idx - 1], template) <= 0) {
				cmdPrompt = "please press the same finger 3 times for the enrollment";
				enroll_idx = 0;
				return;
			}
			System.arraycopy(template, 0, regtemparray[enroll_idx], 0, 2048);
			enroll_idx++;
			if (enroll_idx == 3) {
				int[] _retLen = new int[1];
				_retLen[0] = 2048;
				byte[] regTemp = new byte[_retLen[0]];

				if(bRegister){
					iFid= java.lang.Math.max(iFid,FingerprintSensorEx.DBCount(mhDevice)+1);
				}
				if (0 == (ret = FingerprintSensorEx.DBMerge(mhDB,
						regtemparray[0], regtemparray[1], regtemparray[2],
						regTemp, _retLen))
						&& 0 == (ret = (bCollection?0:FingerprintSensorEx.DBAdd(mhDB, iFid,
								regTemp)))) {
					if(bRegister){
						iFid++;
					}
					cbRegTemp = _retLen[0];
					System.arraycopy(regTemp, 0, lastRegTemp, 0, cbRegTemp);
					// Base64 Template
					cmdPrompt = "enroll succ";

					currentOwnerRegTempBase64="libzkfp:"+_retLen[0]+":"+FingerprintSensorEx.BlobToBase64(regTemp, _retLen[0]);

					
					if(bCollection){

					}else{ 
					if(currentOwner==null || currentOwner.trim().equals("")){
						currentOwner=""+(iFid-1);
					}
					String bmpFilePath="..\\fingerprint\\collect\\fingerprint-"+currentOwner+"-"+new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date())+".bmp";
					String txtFilePath="..\\fingerprint\\collect-base64\\fingerprint-"+currentOwner+"-"+new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date())+"-DBMerge-"+_retLen[0]+".txt";
					try {
    					writeBitmap(imgbuf, fpWidth, fpHeight, bmpFilePath);
						File fileSet = new File(txtFilePath);
						if(fileSet.exists()){
							fileSet.delete();
						}
						fileSet.createNewFile();
						FileOutputStream fs = new FileOutputStream(fileSet, true); //在该文件的末尾添加内容
						fs.write(FingerprintSensorEx.BlobToBase64(regTemp, _retLen[0]).getBytes());
					} catch (Throwable t) {
						t.printStackTrace();
					}   

					FingerprintVo vo = new FingerprintVo();
					if(log.isTraceEnabled()){
						log.trace("currentOwner="+currentOwner);
					}
					vo.setOwner(currentOwner);
					vo.setFid(iFid-1);
					vo.setName(bmpFilePath);
					vo.setData(lastRegTemp);
					fingerprintArry.add(vo);
					}
				} else {
					cmdPrompt = "enroll fail, error code=" + ret;
				}
				// bRegister = false;
			} else {
				cmdPrompt = "You need to press the " + (3 - enroll_idx)
						+ " times fingerprint";
			}
		} else {
			if (bIdentify) {
				int[] fid = new int[1];
				int[] score = new int[1];
				int ret = FingerprintSensorEx.DBIdentify(mhDB, template, fid,
						score);
				if (ret == 0) {
					cmdPrompt = "Identify succ, fid=" + fid[0] + ",score="
							+ score[0];
					for(int i=0;i<fingerprintArry.size();i++){
						if(log.isTraceEnabled()){
							log.trace("fingerprintArry.get("+i+").getFid()="+fingerprintArry.get(i).getFid()+",fid[0]="+fid[0]+",currentOwner="+currentOwner);
						}
						if(fingerprintArry.get(i).getFid()==fid[0]){
							currentOwner=fingerprintArry.get(i).getOwner();
							break;
						}
					}
				} else {
					cmdPrompt = "Identify fail, errcode=" + ret;
				}

			} else {
				if (cbRegTemp <= 0) {
					cmdPrompt = "Please register first!";
				} else {
					int ret = FingerprintSensorEx.DBMatch(mhDB, lastRegTemp,
							template);
					if (ret > 0) {
						cmdPrompt = "Verify succ, score=" + ret;
					} else {
						cmdPrompt = "Verify fail, ret=" + ret;
					}
				}
			}
		}
	}

	/**
	 * 以字符为单位读取文件，常用于读文本，数字等类型的文件
	 */
	public static String readFileByChars(String fileName) {
		StringBuffer outBuf = new StringBuffer();
		outBuf.delete(0, outBuf.length());
		File file = new File(fileName);
		Reader reader = null;
		// try {
		// // log.info("以字符为单位读取文件内容，一次读一个字节：");
		// // 一次读一个字符
		// reader = new InputStreamReader(new FileInputStream(file));
		// int tempchar;
		// while ((tempchar = reader.read()) != -1) {
		// // 对于windows下，\r\n这两个字符在一起时，表示一个换行。
		// // 但如果这两个字符分开显示时，会换两次行。
		// // 因此，屏蔽掉\r，或者屏蔽\n。否则，将会多出很多空行。
		// // if (((char) tempchar) != '\r') {
		// // System.out.print((char) tempchar);
		// // }
		// }
		// reader.close();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		try {
			// log.info("以字符为单位读取文件内容，一次读多个字节：");
			// 一次读多个字符
			char[] tempchars = new char[30];
			int charread = 0;
			reader = new InputStreamReader(new FileInputStream(fileName));
			// 读入多个字符到字符数组中，charread为一次读取字符数
			while ((charread = reader.read(tempchars)) != -1) {
				if (charread == tempchars.length) {
					outBuf.append(tempchars);
				} else {
					for (int i = 0; i < charread; i++) {
						outBuf.append(tempchars[i]);
					}
				}
				// // 同样屏蔽掉\r不显示
				// if ((charread == tempchars.length)
				// && (tempchars[tempchars.length - 1] != '\r')) {
				// System.out.print(tempchars);
				// } else {
				// for (int i = 0; i < charread; i++) {
				// if (tempchars[i] == '\r') {
				// continue;
				// } else {
				// System.out.print(tempchars[i]);
				// }
				// }
				// }
			}
			return outBuf.toString();
		} catch (Throwable t) {
			log.error("",t);
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

	public static void writeBitmap(byte[] imageBuf, int nWidth, int nHeight,
			String path) throws IOException {
		java.io.FileOutputStream fos = new java.io.FileOutputStream(path);
		java.io.DataOutputStream dos = new java.io.DataOutputStream(fos);

		int w = (((nWidth + 3) / 4) * 4);
		int bfType = 0x424d; // 位图文件类型（0—1字节）
		int bfSize = 54 + 1024 + w * nHeight;// bmp文件的大小（2—5字节）
		int bfReserved1 = 0;// 位图文件保留字，必须为0（6-7字节）
		int bfReserved2 = 0;// 位图文件保留字，必须为0（8-9字节）
		int bfOffBits = 54 + 1024;// 文件头开始到位图实际数据之间的字节的偏移量（10-13字节）

		dos.writeShort(bfType); // 输入位图文件类型'BM'
		dos.write(changeByte(bfSize), 0, 4); // 输入位图文件大小
		dos.write(changeByte(bfReserved1), 0, 2);// 输入位图文件保留字
		dos.write(changeByte(bfReserved2), 0, 2);// 输入位图文件保留字
		dos.write(changeByte(bfOffBits), 0, 4);// 输入位图文件偏移量

		int biSize = 40;// 信息头所需的字节数（14-17字节）
		int biWidth = nWidth;// 位图的宽（18-21字节）
		int biHeight = nHeight;// 位图的高（22-25字节）
		int biPlanes = 1; // 目标设备的级别，必须是1（26-27字节）
		int biBitcount = 8;// 每个像素所需的位数（28-29字节），必须是1位（双色）、4位（16色）、8位（256色）或者24位（真彩色）之一。
		int biCompression = 0;// 位图压缩类型，必须是0（不压缩）（30-33字节）、1（BI_RLEB压缩类型）或2（BI_RLE4压缩类型）之一。
		int biSizeImage = w * nHeight;// 实际位图图像的大小，即整个实际绘制的图像大小（34-37字节）
		int biXPelsPerMeter = 0;// 位图水平分辨率，每米像素数（38-41字节）这个数是系统默认值
		int biYPelsPerMeter = 0;// 位图垂直分辨率，每米像素数（42-45字节）这个数是系统默认值
		int biClrUsed = 0;// 位图实际使用的颜色表中的颜色数（46-49字节），如果为0的话，说明全部使用了
		int biClrImportant = 0;// 位图显示过程中重要的颜色数(50-53字节)，如果为0的话，说明全部重要

		dos.write(changeByte(biSize), 0, 4);// 输入信息头数据的总字节数
		dos.write(changeByte(biWidth), 0, 4);// 输入位图的宽
		dos.write(changeByte(biHeight), 0, 4);// 输入位图的高
		dos.write(changeByte(biPlanes), 0, 2);// 输入位图的目标设备级别
		dos.write(changeByte(biBitcount), 0, 2);// 输入每个像素占据的字节数
		dos.write(changeByte(biCompression), 0, 4);// 输入位图的压缩类型
		dos.write(changeByte(biSizeImage), 0, 4);// 输入位图的实际大小
		dos.write(changeByte(biXPelsPerMeter), 0, 4);// 输入位图的水平分辨率
		dos.write(changeByte(biYPelsPerMeter), 0, 4);// 输入位图的垂直分辨率
		dos.write(changeByte(biClrUsed), 0, 4);// 输入位图使用的总颜色数
		dos.write(changeByte(biClrImportant), 0, 4);// 输入位图使用过程中重要的颜色数

		for (int i = 0; i < 256; i++) {
			dos.writeByte(i);
			dos.writeByte(i);
			dos.writeByte(i);
			dos.writeByte(0);
		}

		byte[] filter = null;
		if (w > nWidth) {
			filter = new byte[w - nWidth];
		}

		for (int i = 0; i < nHeight; i++) {
			dos.write(imageBuf, (nHeight - 1 - i) * nWidth, nWidth);
			if (w > nWidth)
				dos.write(filter, 0, w - nWidth);
		}
		dos.flush();
		dos.close();
		fos.close();
	}

	public static byte[] changeByte(int data) {
		return intToByteArray(data);
	}

	public static byte[] intToByteArray(final int number) {
		byte[] abyte = new byte[4];
		// "&" 与（AND），对两个整型操作数中对应位执行布尔代数，两个位都为1时输出1，否则0。
		abyte[0] = (byte) (0xff & number);
		// ">>"右移位，若为正数则高位补0，若为负数则高位补1
		abyte[1] = (byte) ((0xff00 & number) >> 8);
		abyte[2] = (byte) ((0xff0000 & number) >> 16);
		abyte[3] = (byte) ((0xff000000 & number) >> 24);
		return abyte;
	}

	public static void FreeSensor() {
		mbStop = true;
		try { // wait for thread stopping
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			log.error("",e);
		}
		// if (0 != mhDB)
		// {
		// FingerprintSensorEx.DBFree(mhDB);
		// mhDB = 0;
		// }
		if (0 != mhDevice) {
			FingerprintSensorEx.CloseDevice(mhDevice);
			mhDevice = 0;
		}
		FingerprintSensorEx.Terminate();
		manufacturer = "";
		deviceCount = 0;
		cmdPrompt = "";
		while(fingerprintArry.size()>0){
			fingerprintArry.remove(0);
		}
	}
}
