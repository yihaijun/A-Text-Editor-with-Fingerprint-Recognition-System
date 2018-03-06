/**
 * 
 */
package com.tisson.fingerprint.test;

import java.io.File;
import java.io.FileOutputStream;
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
public class FingerprintSensorTest {

	private static FingerprintSensor fsTest = new FingerprintSensor();
	private static SimpleDateFormat df = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss:SSS");
	private static int iFid = 1;
	private static java.util.ArrayList<FingerprintVo> fingerprintArry = new java.util.ArrayList<FingerprintVo>();
	private static java.util.ArrayList<FingerprintVo> unknowFingerprintArry = new java.util.ArrayList<FingerprintVo>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int ret = 0;
//		ret = fsTest.getDeviceCount();
//		System.out.println("[" + df.format(new Date())
//				+ "]  fsTest.getDeviceCount() return " + ret);
//		if (ret < 0) {
//			System.out.println("[" + df.format(new Date())
//					+ "]  fsTest.getDeviceCount() return " + ret);
//		}
//		ret = fsTest.openDevice(0);
//		System.out.println("[" + df.format(new Date())
//				+ "]  fsTest.openDevice(0) return " + ret);
//		if (ret != FingerprintSensorErrorCode.ERROR_SUCCESS) {
//			return;
//		}
//		getParameter(0, 10000000, 1);

		long beginTime = System.currentTimeMillis();
		loadTestBase64(false);
		loadTestBmp(true, false);
		long endTime = System.currentTimeMillis();
		System.out.println("[" + df.format(new Date())
				+ "]  load duration=" + (endTime - beginTime)
				+ ",fingerprintArry.size()=" + fingerprintArry.size()
				+ ",iFid=" + iFid+",unknowFingerprintArry.size()="+unknowFingerprintArry.size());
		for (int i = 0; i < 1; i++) {
			if(!addTestBmp()){
				break;
			}
		}
		loadTestBmp(false, false);
		System.out.println("[" + df.format(new Date())
				+ "]  addTestBmp duration=" + (endTime - beginTime)
				+ ",fingerprintArry.size()=" + fingerprintArry.size()
				+ ",iFid=" + iFid+",unknowFingerprintArry.size()="+unknowFingerprintArry.size());
//		beginTime = System.currentTimeMillis();
//		identifyBmp(true);
//		endTime = System.currentTimeMillis();
//		System.out.println("[" + df.format(new Date())
//				+ "]  identifyBmp return.duration=" + (endTime - beginTime)
//				+ ",fingerprintArry.size()=" + fingerprintArry.size()
//				+ ",iFid=" + iFid+",unknowFingerprintArry.size()="+unknowFingerprintArry.size());
		beginTime = System.currentTimeMillis();
		verifyBatch(true,50);
		endTime = System.currentTimeMillis();
		System.out.println("[" + df.format(new Date())
				+ "]  verifyBatch return.duration=" + (endTime - beginTime)
				+ ",fingerprintArry.size()=" + fingerprintArry.size()
				+ ",iFid=" + iFid+",unknowFingerprintArry.size()="+unknowFingerprintArry.size());
		beginTime = System.currentTimeMillis();
		byte[] fpTemplate = new byte[2048];
		int[] sizeFPTemp = new int[1];
		String resultOfBmp = "";
		sizeFPTemp[0] = 2048;
		
		fsTest.ExtractFromImage("d:\\123.bmp", 500, fpTemplate,
				sizeFPTemp);
		beginTime = System.currentTimeMillis();
		resultOfBmp = verifyFPByID(fpTemplate,10);
		endTime = System.currentTimeMillis();
		System.out.println("[" + df.format(new Date())
				+ "]  verifyFPByID(fingerprint.bmp,10) return.duration=" + (endTime - beginTime)
				+ ",fingerprintArry.size()=" + fingerprintArry.size()
				+ ",iFid=" + iFid+",unknowFingerprintArry.size()="+unknowFingerprintArry.size()+",resultOfBmp="+resultOfBmp);
		endTime = System.currentTimeMillis();
		
		
		fsTest.ExtractFromImage("d:\\fingerprint.bmp", 500, fpTemplate,
				sizeFPTemp);
		beginTime = System.currentTimeMillis();
		resultOfBmp = verifyFPByID(fpTemplate,0);
		endTime = System.currentTimeMillis();
		System.out.println("[" + df.format(new Date())
				+ "]  verifyFPByID(123.bmp,0) return.duration=" + (endTime - beginTime)
				+ ",fingerprintArry.size()=" + fingerprintArry.size()
				+ ",iFid=" + iFid+",unknowFingerprintArry.size()="+unknowFingerprintArry.size()+",resultOfBmp="+resultOfBmp);
		endTime = System.currentTimeMillis();
	}

	private static void getParameter(int begin, int end, int step) {
		StringBuffer buf = new StringBuffer();
		int testIntParam = 0;
		String testStringParam = "";
		for (int index = begin; index < end; index += step) {
			byte paramValue[] = new byte[40];
			int paramLen[] = new int[40];
			paramLen[0] = 40;
			int ret = fsTest.GetParameter(index, paramValue, paramLen);
			if (ret != 0) {
				continue;
			}
			buf.delete(0, buf.length());
			testIntParam = ZKFPTest.byteArrayToInt(paramValue);
			testStringParam = new String(paramValue);
			System.out.println("[" + df.format(new Date()) + "] GetParameter("
					+ index + ")=" + testIntParam + ",testStringParam="
					+ testStringParam);
		}
	}

	private static boolean loadTestBase64(boolean printlog) {
		File fileSet = new File("d:\\test\\collect-base64");
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
				System.out.println("[" + df.format(new Date()) + "] path="
						+ path);
			}
			String base64 = ZKFPTest.readFileByChars(path);
			String pathTail = path.substring(path.indexOf("-DBMerge-")
					+ "-DBMerge-".length());
			String lenFlag = pathTail.substring(0, pathTail.length() - 4);
			int len = Integer.valueOf(lenFlag).intValue();
			byte[] fpTemplate = new byte[len];
			FingerprintSensorEx.Base64ToBlob(base64, fpTemplate, len);

			int[] sizeFPTemp = new int[1];
			sizeFPTemp[0] = len;
//			int ret2 = fsTest.ExtractFromImage(path, 500, fpTemplate,
//					sizeFPTemp);
			int ret2 = ZKFPService.ExtractFromImage(path, 500, fpTemplate,
			sizeFPTemp);
			int ret3 = fsTest.DBAdd(iFid, fpTemplate);
			FingerprintVo vo = new FingerprintVo();
			vo.setName(path);
			vo.setData(fpTemplate);
			vo.setFid(iFid);
			fingerprintArry.add(vo);
			if (ret3 == 0) {
				iFid++;
			} else {
				return false;
			}
			if (printlog) {
				System.out
						.println("[" + df.format(new Date()) + "] path=" + path
								+ ",ExtractFromImage=" + ret2 + ",DBAdd="
								+ ret3);
			}
		}
		return true;
	}

	private static boolean loadTestBmp(boolean normal, boolean printlog) {
		String parentPath = "d:\\test\\";
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
					System.out.println("[" + df.format(new Date()) + "] path="
							+ path);
				}
				byte[] fpTemplate = new byte[2048];
				int[] sizeFPTemp = new int[1];
				sizeFPTemp[0] = 2048;
				int ret2 = fsTest.ExtractFromImage(path, 500, fpTemplate,
						sizeFPTemp);
				if (normal) {
					ret3 = fsTest.DBAdd(iFid, fpTemplate);
					if (ret3 != 0) {
						return false;
					}
					iFid++;
					FingerprintVo vo = new FingerprintVo();
					vo.setName(path);
					vo.setData(fpTemplate);
					vo.setFid(iFid);
					fingerprintArry.add(vo);
				} else {
					FingerprintVo vo = new FingerprintVo();
					vo.setName(path);
					vo.setData(fpTemplate);
					vo.setFid(iFid);
					unknowFingerprintArry.add(vo);
				}
				if (printlog) {
					System.out.println("[" + df.format(new Date()) + "] path="
							+ path + ",ExtractFromImage=" + ret2 + ",DBAdd="
							+ ret3);
				}
			}
		}
		return true;
	}

	private static void verifyBatch(boolean printlog,int minScore) {
		for (int i = 0; i < unknowFingerprintArry.size(); i++) {
			FingerprintVo vo = unknowFingerprintArry.get(i);
			String buf = verifyFPByID(vo.getData(),minScore);
			if (printlog) {
				System.out.println("[" + df.format(new Date()) + "] path="
						+ vo.getName() + ": " + buf);
			}
		}
	}

	private static String verifyFPByID(byte[] fpBytes,int minScore) {
		int ret = 0;
		StringBuffer buf = new StringBuffer();
		buf.delete(0, buf.length());
		int[] fid = new int[10];
		int[] score = new int[10];
		ret = fsTest.IdentifyFP(fpBytes, fid, score);
		if(ret!=0 && minScore>0){
			return "";
		}
		int pos = 0;
		if(ret==0){
			pos = fid[0];
		}
		int count =0;
		for (int j = pos; j < iFid && count<10; j++) {
			ret = ZKFPService.VerifyFPByID(j, fpBytes);
			if(ret<=minScore){
				continue;
			}
			count++;
			buf.append(j + "/" + ret + ";");
		}
		return buf.toString();
	}

	private static void identifyBmp(boolean printlog) {
		int[] fid = new int[10];
		int[] score = new int[10];
		int ret = 0;
		FingerprintSensor fsTest = new FingerprintSensor();

		for (int i = 0; i < unknowFingerprintArry.size(); i++) {
			FingerprintVo vo = unknowFingerprintArry.get(i);
			ret = fsTest.IdentifyFP(vo.getData(), fid, score);
			if (printlog) {
				StringBuffer outBuf = new StringBuffer();
				outBuf.delete(0, outBuf.length());
				outBuf.append("[" + df.format(new Date()) + "] path="
						+ vo.getName() + ",DBIdentify=" + ret);
				for (int j = 0; j < fid.length; j++) {
					outBuf.append("," + fid[j] + ":" + score[j]);
				}
				System.out.println(outBuf.toString());
			}
		}
	}

	private static boolean addTestBmp() {
		int[] sizeFPTemp = new int[1];
		sizeFPTemp[0] = 2048;
		for (int i = 0; i < fingerprintArry.size(); i++) {
			FingerprintVo vo = fingerprintArry.get(i);
			int ret3 = fsTest.DBAdd(iFid, vo.getData());
			if(ret3 == 0){
				iFid++;
			}else{
				return false;
			}
		}
		return true;
	}

	private static boolean bmpToKey() {
		String parentPath = "d:\\test\\bmp";
		File fileSet = new File(parentPath);
		File[] files = null;
		if (fileSet.exists() && fileSet.isDirectory()) {
			files = fileSet.listFiles();
			int ret2=0;
			int ret3 = 0;
			String path = "";
			for (int i = 0; i < files.length; i++) {
				path = files[i].getAbsolutePath();
				byte[] fpTemplate = new byte[2048];
				int[] sizeFPTemp = new int[1];
				sizeFPTemp[0] = 2048;
				ret2 = fsTest.ExtractFromImage(path, 500, fpTemplate,
						sizeFPTemp);
               	int[] _retLen = new int[1];
                _retLen[0] = 2048;
                byte[] regTemp = new byte[_retLen[0]];
				ret3 = ZKFPService.GenRegFPTemplate(fpTemplate, fpTemplate, fpTemplate, regTemp, _retLen);
				if(ret3!=0){
					continue;
				}
         		try {
         			String fileKeyPath = "d:\\test\\bmp-key\\"+files[i].getName()+"-"+new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date())+"-DBMerge-"+_retLen[0]+".txt";
					File fileKey = new File(fileKeyPath);
					if(fileKey.exists()){
						fileKey.delete();
					}
					fileKey.createNewFile();
					FileOutputStream fs = new FileOutputStream(fileKey, true); //在该文件的末尾添加内容
					fs.write(FingerprintSensorEx.BlobToBase64(regTemp, _retLen[0]).getBytes());
				} catch (Throwable t) {
					t.printStackTrace();
				}   
			}
		}
		return true;
	}

}
