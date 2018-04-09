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
import java.util.Hashtable;

import lombok.extern.slf4j.Slf4j;

import com.zkteco.biometric.FingerprintSensor;
import com.zkteco.biometric.FingerprintSensorErrorCode;
import com.zkteco.biometric.FingerprintSensorEx;
import com.zkteco.biometric.ZKFPService;
import com.regaltec.nma.collector.common.thread.INmaCollectorTaskInf;
import com.regaltec.nma.collector.common.thread.NmaCollectorThreadPool;
import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * @author yihaijun
 * 
 */
@Slf4j
public class FingerprintSensorHandle {
	private static FingerprintSensorHandle instance;
	private RtsEvent lockEvent;
	public static final int STATE_OK  = 0;
	public static final int STATE_NEED_PRESS_3_TIMES  = 3;
	public static final int STATE_NEED_PRESS_2_TIMES  = 2;
	public static final int STATE_NEED_PRESS_1_TIMES  = 1;
	public static final int STAT_PRESS_TOO_MUCH_TIMES  = 4;
	public static final int STATE_NO_DEVICE  = 100;
	public static final int STATE_DEVICE_ERROR  = 101;
	public static final int STATE_DEVICE_BUSY  = 102;
	
	public static final int STATE_NO_CMD  = 200;
	public static final int STATE_NOT_MATCH  = 201;
	public static final int STATE_DBMERGE_ERROR  = 202;
	public static final int STATE_UNKNOW_ERROR  = 203;
	public static final int STATE_DBADD_ERROR  = 204;
	
	private int enrollState = STATE_NO_CMD;
	
	private java.util.ArrayList<FingerprintVo> fingerprintArry;
	private java.util.ArrayList<FingerprintVo> fingerprintDbArry;
	private java.util.ArrayList<FingerprintVo> unknowFingerprintArry;

	private String manufacturer = "";
	private int deviceCount = 0;

	private String cmdPrompt = "";

	// private FingerprintSensor fsTest = new FingerprintSensor();
	private SimpleDateFormat df = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss:SSS");

	private WorkThread workThread = null;
	private boolean mbStop = true;
	private boolean mbWork = false;

	private int thPoolSize = 2;
	private int thRunSize = (thPoolSize * 50 / 100);
	private NmaCollectorThreadPool thPool;
	private int loadDbCycleTimes = 0;
	
	
	private boolean haveStopped = true; 
	private long mhDevice = 0;
	private long mhDB = 0;
	private int nFakeFunOn = 1;
	private byte[] imgbuf = null;
	private byte[] template = new byte[2048];
	private int[] templateLen = new int[1];
	private boolean bCollection = false;
	private boolean bRegister = false;
	private boolean bIdentify = false;
	private boolean bDel = false;
	
	private int enroll_idx = 0;

	private byte[][] regtemparray = new byte[3][2048];

	// the length of lastRegTemp
	private int cbRegTemp = 0;
	// for verify test
	private byte[] lastRegTemp = new byte[2048];

	private int totalFailIdentify = 0;

	// the width of fingerprint image
	private int fpWidth = 0;
	// the height of fingerprint image
	private int fpHeight = 0;

	private int iFid = 1;
	private String speciFidPreKey="iFid=";

	private String currentOwner="";
	private String currentOwnerRegTempBase64="";

	private final static String fileSeparator = System.getProperty("file.separator");
	private String fingerprintBmpPath=".."+fileSeparator+"external"+fileSeparator+"fingerprint"+fileSeparator+"fingerprint.bmp";
	private int minScore = 30;
	
	/**
	 * @return the instance
	 */
	public static FingerprintSensorHandle getInstance() {
		if(instance==null){
			instance = new FingerprintSensorHandle();
		}
		return instance;
	}
	
	private FingerprintSensorHandle(){
		String cmd="echo [%date% %time%] new FingerprintSensorHandle() java.library.path="+System.getProperty("java.library.path") + " >>.."+fileSeparator+"sfip-oam.log";
		com.tisson.sfip.module.util.SystemUtil.callcmd(new String[] {cmd}, "utf-8");
		
		fingerprintArry = new java.util.ArrayList<FingerprintVo>();
		fingerprintDbArry = new java.util.ArrayList<FingerprintVo>();
		unknowFingerprintArry = new java.util.ArrayList<FingerprintVo>();
		enrollState = STATE_NO_CMD;
		lockEvent = new RtsEvent();
		lockEvent.lockInterruptibly();
		lockEvent.setEvent();

		mbStop = false;
		mbWork = false;
		workThread = new WorkThread();
		workThread.start();// 线程启动
		
		try {
			thPoolSize=Integer.parseInt(System.getProperties().getProperty("thPoolSizeFingerprintSensorHandle"));
			thRunSize = (thPoolSize * 50 / 100);
		} catch (NumberFormatException e) {
//			e.printStackTrace();
		}
		try {
			loadDbCycleTimes=Integer.parseInt(System.getProperties().getProperty("loadDbCycleTimesFingerprintSensorHandle"));
		} catch (NumberFormatException e) {
//			e.printStackTrace();
		}
		
		thPool = new NmaCollectorThreadPool(thPoolSize);
	}


	/**
	 * @return the enrollState
	 */
	public int getEnrollState() {
		return enrollState;
	}

	/**
	 * @return the currentOwnerRegTempBase64
	 */
	public String getCurrentOwnerRegTempBase64() {
		return currentOwnerRegTempBase64;
	}

	/**
	 * @return the currentOwner
	 */
	public String getCurrentOwner() {
		return currentOwner;
	}

	/**
	 * @return the manufacturer
	 */
	public String getManufacturer() {
		int ret =lockEvent.waitEvent(8000);
		if(ret!=0){
			log.warn("lockEvent.waitEvent(8000)="+ret+"!=0");
			lockEvent.setEvent();
		}
		if (manufacturer == null
				|| manufacturer.trim().equalsIgnoreCase("")
				|| manufacturer.indexOf("(SN=)") > 0) {
			manufacturer = "";
			manufacturer += getStrParameter(1101);
			manufacturer += getStrParameter(1102);
			manufacturer += "(SN=" + getStrParameter(1103) + ")";
		}
		lockEvent.setEvent();
		return manufacturer;
	}

	public int getDeviceCount() {
		return deviceCount;
	}

	public int getFingerprintArryCount() {
		return fingerprintArry.size();
	}
	public int getFingerprintDbArryCount() {
		return fingerprintDbArry.size();
	}
	public int load(boolean needDevice) {
		int wait = lockEvent.waitEvent(5000);
		if (wait !=0){
			log.warn("lockEvent.waitEvent(5000)="+wait+"!=0");
			lockEvent.setEvent();
			if(needDevice){
				return STATE_DEVICE_BUSY;
			}
		}
		if (deviceCount > 0) {
			lockEvent.setEvent();
			return 0;
		}
		if(log.isInfoEnabled()){
			log.info("FingerprintSensorEx.Init() begin...");
		}
		int ret = FingerprintSensorEx.Init();
		if(log.isInfoEnabled()){
			log.info("FingerprintSensorEx.Init() retrun " + ret);
		}
		if (FingerprintSensorErrorCode.ZKFP_ERR_OK != ret
				&& FingerprintSensorErrorCode.ZKFP_ERR_ALREADY_INIT != ret) {
			log.warn("[" + df.format(new Date()) + "] Init failed!ret =" + ret);
			lockEvent.setEvent();
			if(needDevice){
				return STATE_DEVICE_ERROR;
			}
		}
		deviceCount = FingerprintSensorEx.GetDeviceCount();
		if (deviceCount < 0) {
			log.warn("[" + df.format(new Date()) + "] No devices connected!");
			FreeSensor();
			lockEvent.setEvent();
			if(needDevice){
				return STATE_NO_DEVICE;
			}
		}
		if(log.isInfoEnabled()){
			log.info("deviceCount="+deviceCount);
		}
		if (0 == (mhDevice = FingerprintSensorEx.OpenDevice(0))) {
			
			log.warn("[" + df.format(new Date()) + "] Open device fail, FingerprintSensorEx.OpenDevice(0) = "
					+ ret + "!");
			mhDevice = FingerprintSensorEx.OpenDevice(1);
			if(mhDevice==0){
				log.warn("[" + df.format(new Date()) + "] Open device fail, FingerprintSensorEx.OpenDevice(1) = "
						+ ret + "!");
				FreeSensor();
				lockEvent.setEvent();
				if(needDevice){
					return STATE_NO_DEVICE;
				}
			}
		}
		if(log.isInfoEnabled()){
			log.info("mhDevice="+mhDevice);
		}
		if (0 == (mhDB = FingerprintSensorEx.DBInit())) {
			log.warn("[" + df.format(new Date()) + "] Init DB fail, ret = "
					+ ret + "!");
			FreeSensor();
			lockEvent.setEvent();
			if(needDevice){
				return STATE_DEVICE_ERROR;
			}
		}
		if(log.isInfoEnabled()){
			log.info("manufacturer=" + getManufacturer());
			wait = lockEvent.waitEvent(5000);
			if (wait !=0){
				log.warn("lockEvent.waitEvent(5000)="+wait+"!=0");
				lockEvent.setEvent();
				if(needDevice){
					return STATE_DEVICE_BUSY;
				}
			}
			log.info("lockEvent.getHoldCount="+lockEvent.getHoldCount()+",lockEvent.getQueueLength="+lockEvent.getQueueLength());
		}
		
		if(log.isDebugEnabled()){
			log.debug("bCollection="+bCollection);
		}
		iFid = 1;
		fingerprintArry.clear();
		fingerprintDbArry.clear();
		unknowFingerprintArry.clear();
		if(!bCollection){
			loadTestBase64(true);
			loadTestBmp(true, true);
			int i =0;
			for(i=0;i<loadDbCycleTimes;i++){
				if(!batchAddTestBmp(needDevice)){
					if(needDevice){
						break;
					}
				}
			}
			if(log.isInfoEnabled()){
				log.info("batchAddTestBmp() total of " +i + " times,iFid="+iFid+",loadDbCycleTimes="+System.getProperties().getProperty("loadDbCycleTimesFingerprintSensorHandle")+",thPoolSizeFingerprintSensorHandle="+System.getProperties().getProperty("thPoolSizeFingerprintSensorHandle"));
			}
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

		mbStop=false;
		mbWork=true;
		
		lockEvent.setEvent();


		if(log.isInfoEnabled()){
			log.info("fingerprintArry.size()=" + fingerprintArry.size()+",iFid="+iFid+",fingerprintDbArry.size()="+fingerprintDbArry.size()+",ZKFPService.DBCount()="+ZKFPService.DBCount());
		}
		return 0;
	}

	public int cmdCollection() {
		int ret = load(true);
		if (ret != 0){
			enrollState = ret;
			return ret;
		}
		ret= lockEvent.waitEvent(8000);
		if (ret !=0){
			lockEvent.setEvent();
			return STATE_DEVICE_BUSY;
		}
		bCollection = true;
		enroll_idx = 0;
		currentOwner="";
		currentOwnerRegTempBase64="";
		bRegister = false;
		bIdentify = false;
		bDel = false;
		cmdPrompt = "please press the same finger 3 times for the enrollment";
//		FreeSensor();
//		deviceCount=0;
//		load();
		enrollState = STATE_NEED_PRESS_3_TIMES;
		lockEvent.setEvent();
		return 0;
	}
	public int cmdEnroll(String owner) {
		int ret = load(true);
		if (ret != 0){
			enrollState = ret;
			return ret;
		}
		ret = lockEvent.waitEvent(8000);
		if (ret !=0){
			lockEvent.setEvent();
			return STATE_DEVICE_BUSY;
		}
		enroll_idx = 0;
		if(owner==null||owner.trim().equals("") || owner.indexOf(":") >=0){
			owner=""+System.currentTimeMillis();
		}
		currentOwner=owner;
		currentOwnerRegTempBase64="";
		bCollection = false;
		bRegister = true;
		bIdentify = false;
		bDel = false;
		cmdPrompt = "please press the same finger 3 times for the enrollment";
		enrollState = STATE_NEED_PRESS_3_TIMES;
		lockEvent.setEvent();
		return 0;
	}

	public int cmdIdentify() {
		int ret = lockEvent.waitEvent(8000);
		if (ret !=0){
			lockEvent.setEvent();
			return STATE_DEVICE_BUSY;
		}
		enrollState = STATE_NO_CMD;
		enroll_idx = 0;
		currentOwner="";
		currentOwnerRegTempBase64="";
		bCollection = false;
		bRegister = false;
		bIdentify = true;
		bDel = false;
		cmdPrompt = "";
		lockEvent.setEvent();
		return 0;
	}

	public String cmdVerify(String path) {
//		enroll_idx = 0;
//		bCollection = false;
//		bRegister = false;
//		bIdentify = false;
//		cmdPrompt = "";
		return identifyBmpFile(path);
	}

	public String cmdTxtVerify(String path) {
//		enroll_idx = 0;
//		bCollection = false;
//		bRegister = false;
//		bIdentify = false;
//		cmdPrompt = "";
		return identifyText(path);
	}

	public int cmdDel(String owner) {
		int ret = lockEvent.waitEvent(8000);
		if (ret !=0){
			lockEvent.setEvent();
			return STATE_DEVICE_BUSY;
		}
		if(owner.startsWith(speciFidPreKey)){
			int index =0;
			try {
				index = Integer.parseInt(owner.substring(speciFidPreKey.length()));
			} catch (NumberFormatException e) {
//				e.printStackTrace();
			}
			if (index>0){
				ret = FingerprintSensorEx.DBDel(mhDB, index);
				if(ret==0){
					cmdPrompt = "Del suc";
//					fingerprintDbArry.remove(index-1);
				}else{
					cmdPrompt = "Identify fail";
				}
			}
			lockEvent.setEvent();
			return 0;
		}
		enrollState = STATE_NO_CMD;
		enroll_idx = 0;
		currentOwner=owner;
		currentOwnerRegTempBase64="";
		bCollection = false;
		bRegister = false;
		bIdentify = true;
		bDel = true;
		cmdPrompt = "";
		lockEvent.setEvent();
		return 0;
	}

	/**
	 * @return the iFid
	 */
	public int getiFid() {
		return iFid;
	}

	/**
	 * @return the cmdPrompt
	 */
	public String getCmdPrompt() {
		return cmdPrompt;
	}

	private String getStrParameter(int code) {
		if(mhDevice<=0){
			return "";
		}
		byte[] paramValue = new byte[100];
		int[] paramLen = new int[1];
		paramLen[0] = 100;
		int ret = FingerprintSensorEx.GetParameters(mhDevice, code, paramValue,
				paramLen);
		if(ret!=0){
			cmdPrompt = "GetParameters("+mhDevice+","+code+") failed,ret="+ret;
			return "";
		}
		String result = "";
		for (int i = 0; i < paramLen[0]; i++) {
			result = result + (char) paramValue[i];
		}
		return result;
	}
	
	private String getOwnerByPath(String path, int row) {
		if(path==null || path.trim().equals("")){
			return "unknow";
		}
		String owner = path;
		// vo.setName(path+"-"+row);
		String preKey = ".." + fileSeparator + "external" + fileSeparator
				+ "fingerprint" + fileSeparator + "collect-base64"
				+ fileSeparator + "fingerprint-";
		int preKeyIndex = path.lastIndexOf(preKey);
		if (preKeyIndex < 0) {
			preKey = ".." + fileSeparator + "external" + fileSeparator
					+ "fingerprint" + fileSeparator + "collect-base64"
					+ fileSeparator + "";
			preKeyIndex = path.lastIndexOf(preKey);
		}
		if (preKeyIndex < 0) {
			preKey = ".." + fileSeparator + "external" + fileSeparator
					+ "fingerprint" + fileSeparator + "db" + fileSeparator
					+ "fingerprint-";
			preKeyIndex = path.lastIndexOf(preKey);
		}
		if (preKeyIndex < 0) {
			preKey = ".." + fileSeparator + "external" + fileSeparator
					+ "fingerprint" + fileSeparator + "SourceAFIS" + fileSeparator
					+ "fingerprint-";
			preKeyIndex = path.lastIndexOf(preKey);
		}
		
		if (preKeyIndex < 0) {
			preKey=fileSeparator;
			preKeyIndex = path.lastIndexOf(fileSeparator);
		}
		if (preKeyIndex < 0) {
			preKeyIndex = path.indexOf(":");
		}
		if (preKeyIndex < 0) {
			return owner;
		}
		
		owner = path.substring(preKeyIndex + preKey.length());
		int tailKeyIndex = owner.indexOf("-");
		if (tailKeyIndex > 0) {
			owner = owner.substring(0, tailKeyIndex);
		}
		if (row > 0) {
			owner = owner + "-" + row;
		}
		return owner;
	}
	
	private boolean loadBase64Templat(String base64,int len,String path,int row,boolean printlog){
		byte[] fpTemplate = new byte[len];
		int ret = FingerprintSensorEx.Base64ToBlob(base64, fpTemplate, len);
		if(ret != len){
			log.warn("loadBase64Templat("+base64+","+ len +","+path+","+ row+ ","+printlog+") return "+ret);
		}

//		int[] sizeFPTemp = new int[1];
//		sizeFPTemp[0] = len;
		iFid= java.lang.Math.max(iFid,FingerprintSensorEx.DBCount(mhDevice)+1);
		int ret3=0;
		ret3 = FingerprintSensorEx.DBAdd(mhDevice, iFid, fpTemplate);
		if (ret3 == 0) {
			FingerprintVo vo = new FingerprintVo();
//			vo.setImagePath(imagePath);
			vo.setName(path+"-"+row);
			vo.setData(fpTemplate);
			vo.setOwner(getOwnerByPath(path,row));
			vo.setFid(iFid);
			fingerprintArry.add(vo);
			fingerprintDbArry.add(iFid-1,vo);
			iFid++;
		} else {
			log.warn("loadBase64Templat("+base64+","+ len +","+path+","+ row+ ","+printlog+") return "+ret3);
			return false;
		}
		if (printlog) {
			if(log.isDebugEnabled()){
				log.debug("[" + df.format(new Date()) + "] path=" + path
							+ "row="+row+",DBAdd="
							+ ret3);
			}
		}
		return true;
	}
	

	private boolean loadTestBase64(boolean printlog) {
		String rootPath="../external/fingerprint/collect-base64";
		File fileSet = new File(rootPath);
		File[] files = null;
		if (!(fileSet.exists() && fileSet.isDirectory())) {
			log.warn(rootPath +" not exists or is not Directory" );
			return false;
		}
		files = fileSet.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (!files[i].isFile()) {
				continue;
			}
			String path = files[i].getAbsolutePath();
			if (printlog) {
				if(log.isInfoEnabled()){
					log.info("[" + df.format(new Date()) + "] path=" + path);
				}
			}
			String base64 = readFileByChars(path);
			int len =0;
			if(path.indexOf("-DBMerge-")>0){
				String pathTail = path.substring(path.indexOf("-DBMerge-")
						+ "-DBMerge-".length());
				String lenFlag = pathTail.substring(0, pathTail.length() - 4);
				len = Integer.valueOf(lenFlag).intValue();
			}
			String []base64Arry=base64.split("\r\n");
			for(int j=0;j<base64Arry.length;j++){
				int theLen = len;
				String thePreKey="libzkfp:";
				String base64Row=base64Arry[j];
				if(base64Arry[j].startsWith(thePreKey)){
					theLen = Integer.parseInt(base64Arry[j].substring(thePreKey.length(),base64Arry[j].indexOf(":", thePreKey.length())));
					base64Row=base64Row.substring(base64Row.indexOf(":",thePreKey.length() )+1);
				}
				loadBase64Templat(base64Row,theLen,path,j,printlog);
			}
		}
		return true;
	}

	private boolean loadTestSourceAFIS(boolean printlog) {
		String rootPath="../external/fingerprint/SourceAFIS";
		File fileSet = new File(rootPath);
		File[] files = null;
		if (!(fileSet.exists() && fileSet.isDirectory())) {
			log.warn(rootPath +" not exists or is not Directory" );
			return false;
		}
		files = fileSet.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (!files[i].isFile()) {
				continue;
			}
			String path = files[i].getAbsolutePath();
			if (printlog) {
				if(log.isInfoEnabled()){
					log.info("[" + df.format(new Date()) + "] path=" + path);
				}
			}
			String text = readFileByChars(path);
			String []jsonTemplateArry=text.split("SourceAFIS:0:");
			for(int j=0;j<jsonTemplateArry.length;j++){
				FingerprintVo vo = new  FingerprintVo();
				vo.setImagePath("");
				vo.setOwner(getOwnerByPath(path,j));
				vo.setName(path+"-"+j);
				vo.setFid(iFid);
				MySourceAFIS.load(vo, jsonTemplateArry[j]);
				fingerprintArry.add(vo);
				fingerprintDbArry.add(iFid-1,vo);
				iFid++;
			}
		}
		return true;
	}

	private boolean loadTestBmp(boolean normal, boolean printlog) {
		int beginCountUnknowFingerprintArry =  unknowFingerprintArry.size();
		int beginCountFingerprintArry =  fingerprintArry.size();
		int beginCountFingerprintDbArry =  fingerprintDbArry.size();
		
		String parentPath = "../external/fingerprint/";
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
					if(log.isDebugEnabled()){
						log.debug("[" + df.format(new Date()) + "] path=" + path);
					}
				}
				FingerprintVo vo = new FingerprintVo();
				vo.setImagePath(path);
				vo.setOwner(getOwnerByPath(path, 0));
				MySourceAFIS.load(vo);
				
				byte[] fpTemplate = new byte[2048];
				int[] sizeFPTemp = new int[1];
				sizeFPTemp[0] = 2048;
				int ret2 = FingerprintSensorEx.ExtractFromImage(mhDevice, path,
						500, fpTemplate, sizeFPTemp);
				if (normal) {
					iFid= java.lang.Math.max(iFid,FingerprintSensorEx.DBCount(mhDevice)+1);
					ret3 = FingerprintSensorEx
							.DBAdd(mhDevice, iFid, fpTemplate);
//					if (ret3 != 0) {
//						return false;
//					}
					vo.setName(path);
					vo.setData(fpTemplate);
					vo.setFid(iFid);

					vo.setOwner(getOwnerByPath(path,0));
					
					fingerprintArry.add(vo);
					fingerprintDbArry.add(vo);
					iFid++;
				} else {
					vo.setName(path);
					vo.setData(fpTemplate);
					vo.setFid(iFid);
					unknowFingerprintArry.add(vo);
				}
				if (printlog) {
					if(log.isDebugEnabled()){
						log.debug("[" + df.format(new Date()) + "] path=" + path
								+ ",ExtractFromImage=" + ret2 + ",DBAdd=" + ret3);
					}
				}
			}
		}
		if (printlog) {
			if (log.isInfoEnabled()) {
				log.info("loadTestBmp:unknowFingerprintArry add "
						+ (unknowFingerprintArry.size() - beginCountUnknowFingerprintArry)
						+ ",fingerprintArry add "
						+ (fingerprintArry.size() - beginCountFingerprintArry)
						+ ",fingerprintDbArry add "
						+ (fingerprintDbArry.size() - beginCountFingerprintDbArry));
			}
		}
		return true;
	}

	private void verifyBatch(boolean printlog, int minScore) {
		for (int i = 0; i < unknowFingerprintArry.size(); i++) {
			FingerprintVo vo = unknowFingerprintArry.get(i);
			String buf = verifyFPByID(vo.getData(), minScore);
			if (printlog) {
				if(log.isInfoEnabled()){
					log.info("[" + df.format(new Date()) + "] path=" + vo.getName()
							+ ": " + buf);
				}
			}
		}
	}

	private String verifyFPByID(byte[] fpBytes, int minScore) {
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


	private String identifyBmpFile(String path) {
		int[] fid = new int[3];
		int[] score = new int[3];
		int ret = 0;
       	Hashtable<String, FingerprintVo> record = null;
		FingerprintSensor fsTest = new FingerprintSensor();

		long beginTime=System.currentTimeMillis();
		
    	FingerprintVo vo = new FingerprintVo();
    	vo.setImagePath(path);
    	vo.setOwner(getOwnerByPath(path, 0));
    	int retMySourceAFIS_load = MySourceAFIS.load(vo);
    	long duration_1 = System.currentTimeMillis()-beginTime;
    	record = MySourceAFIS.find(vo.getTemplate(), fingerprintDbArry, minScore);
    	long duration_2 = System.currentTimeMillis() - beginTime - duration_1;
		
		byte[] fpTemplate = new byte[2048];
		int[] sizeFPTemp = new int[1];
		sizeFPTemp[0] = 2048;
		ret = FingerprintSensorEx.ExtractFromImage(mhDevice, path, 500,
				fpTemplate, sizeFPTemp);
        if(ret!=0){
        	if(retMySourceAFIS_load < 0){
        		return "ExtractFromImage fail,ret="+ret+",MySourceAFIS.load(vo) return "+ retMySourceAFIS_load;
        	}
        }else{
        	ret = FingerprintSensorEx.DBIdentify(mhDevice, fpTemplate, fid, score);
        	if(ret!=0){
            	if(retMySourceAFIS_load < 0){
            		return "DBIdentify fail,ret="+ret+",MySourceAFIS.load(vo) return "+ retMySourceAFIS_load;
            	}
        	}
        }
		StringBuffer outBuf = new StringBuffer();
		outBuf.delete(0, outBuf.length());
		outBuf.append("DBIdentify=" + ret);
		for (int j = 0; j < fid.length; j++) {
			outBuf.append("," + fid[j] + ":" + score[j]);
		}
		outBuf.append(";"); 
		long durationIn=System.currentTimeMillis()-beginTime;
		int  count = record.size();
//		FingerprintSensorEx.DBAdd(mhDB, pos, fingerprintDbArry.get(pos-1).getData());
		java.util.Iterator<String> it = record.keySet().iterator();
		for(int i=0;it.hasNext() && i< count;i++ ){
			String key=it.next();
			vo = record.get(key);
//			FingerprintSensorEx.DBAdd(mhDB, record[i], fingerprintDbArry.get(record[i]-1).getData());
			outBuf.append(key+"="+vo.getOwner()+ ";");
		}
		cmdPrompt = "";
		outBuf.append("(count="+count+",duration_1="+duration_1+",duration_2="+duration_2+",duration="+(System.currentTimeMillis()-beginTime)+",DbArrySize="+fingerprintDbArry.size()+",DBCount="+ZKFPService.DBCount()+",iFid="+iFid+")");
        return outBuf.toString();
	}

	private String identifyText(String text) {
		long beginTime=System.currentTimeMillis();

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

    	if(log.isDebugEnabled()){
    		log.debug("DBIdentify begin...");
    	}
    	
//		ret = FingerprintSensorEx.DBIdentify(mhDevice, fpTemplate, fid, score);
		ret = ZKFPService.IdentifyFP(fpTemplate, fid, score);
        if(ret!=0){
        	return "DBIdentify fail,ret="+ret;
        }
    	if(log.isDebugEnabled()){
    		log.debug("FingerprintSensorEx.DBIdentify:fid[0]="+fid[0]+",score[0]="+score[0]+",fid[1]="+fid[1]+",score[1]="+score[1]);
    	}
        int dbDelRet=0;
    	if(thPoolSize<=2){
	        int pos=fid[0];
			StringBuffer outBuf=new StringBuffer();
			outBuf.delete(0, outBuf.length());
			int count=0;
			int [] record = new int[100];
			while (ret == 0) {
				count++;
				if(count<=record.length){
					record[count-1]=fid[0];
				}
				outBuf.append(fid[0] + "=" + score[0] + ";");
				dbDelRet=FingerprintSensorEx.DBDel(mhDB, fid[0]);
				
				ret = FingerprintSensorEx.DBIdentify(mhDB,
						template, fid, score);
			}
	    	if(log.isDebugEnabled()){
	    		log.debug("FingerprintSensorEx.DBIdentify return "+ ret + ":count="+count+",dbDelRet="+dbDelRet+",fid[0]="+fid[0]+",score[0]="+score[0]+",fid[1]="+fid[1]+",score[1]="+score[1]);
	    	}
			long durationIn=System.currentTimeMillis()-beginTime;
//			FingerprintSensorEx.DBAdd(mhDB, pos, fingerprintDbArry.get(pos-1).getData());
			for(int i=0;i<record.length && i < count;i++ ){
				FingerprintSensorEx.DBAdd(mhDB, record[i], fingerprintDbArry.get(record[i]-1).getData());
			}
			cmdPrompt = "";
			outBuf.append("(count="+count+",durationIn="+durationIn+",duration="+(System.currentTimeMillis()-beginTime)+",DbArrySize="+fingerprintDbArry.size()+",DBCount="+ZKFPService.DBCount()+",iFid="+iFid+")");
	        return outBuf.toString();
    	}
        
		thPool.fetchTask();
//		long beginTime = System.currentTimeMillis();
		Hashtable<String, FingerprintVo> [] result = new Hashtable[thRunSize];
		int beginPos = 1;//fid[0];
		int step = (iFid - 1 - beginPos) / thRunSize;
		if (step < 1) {
			step = 1;
		}
		int endPos = beginPos + step;
		FPMatchTask[] matchTask = new FPMatchTask[thRunSize];
		int thIndex = 0;
		if (log.isDebugEnabled()) {
			log.debug("DBIdentify=" + ret + ",beginPos=" + beginPos
					+ ",endPos=" + endPos + ",matchTask.length="
					+ matchTask.length + ",iFid=" + iFid);
		}
		for (; endPos < iFid && thIndex < matchTask.length; thIndex++) {
			result[thIndex]=new Hashtable<String, FingerprintVo>();
			matchTask[thIndex] = new FPMatchTask(fpTemplate, minScore, result[thIndex],
					beginPos, endPos);
			thPool.submit(matchTask[thIndex].getName(), matchTask[thIndex], 0);
			beginPos = endPos + 1;
			endPos = beginPos + step;
		}
		if (log.isDebugEnabled()) {
			log.debug("beginPos=" + beginPos + ",endPos=" + endPos
					+ ",matchTask.length=" + matchTask.length + ",iFid=" + iFid);
		}
		thIndex=0;
		while(thIndex!=-1){
			thIndex=-1;
			for(int tmpIndex=0;tmpIndex<matchTask.length;tmpIndex++){
				if(matchTask[tmpIndex]==null){
					continue;
				}
				if(!thPool.isDoneTask(matchTask[tmpIndex].getName())){
					thIndex=tmpIndex;
					break;
				}
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		long durationIn=System.currentTimeMillis()-beginTime;
        StringBuffer outBuf = new StringBuffer();
		outBuf.delete(0, outBuf.length());

//		FingerprintVo vo = getFingerprintVo(fid[0]);
//		outBuf.append(vo.getOwner()+ "=" + score[0] + ";");
//		int pos = fid[0]+1;
//		int count =0;
//		for (int j = pos; j < iFid && count<10; j++) {
//			ret = ZKFPService.VerifyFPByID(j, fpTemplate);
//			if(ret<=30){
//				continue;
//			}
//			vo = getFingerprintVo(j);
//			outBuf.append(vo.getOwner()+ "=" + score[0] + ";");
//		}
		Hashtable<String, FingerprintVo> resultAll=new Hashtable<String, FingerprintVo>();
		for(int tmpIndex=0;tmpIndex<matchTask.length;tmpIndex++){
			if(result[tmpIndex]==null){
				continue;
			}
			resultAll.putAll(result[tmpIndex]);
		}
		
		java.util.Iterator<String> it = resultAll.keySet().iterator();
		while(it.hasNext()){
			String key=it.next();
//			String[] keyVaueArry=key.split("_");
			FingerprintVo vo = resultAll.get(key);
			outBuf.append(key+"="+vo.getOwner()+ ";");
		}
		outBuf.append("(count="+resultAll.size()+",durationIn="+durationIn+",duration="+(System.currentTimeMillis()-beginTime)+",DbArrySize="+fingerprintDbArry.size()+",DBCount="+ZKFPService.DBCount()+",iFid="+iFid+")");
		return outBuf.toString();
	}

//	private FingerprintVo getFingerprintVo(int fid){
//		for(int i=0;i<fingerprintArry.size();i++){
//			if(fingerprintArry.get(i).getFid()==fid){
//				return fingerprintArry.get(i);
//			}
//		}
//		return null;
//	}
	
	private String identifyTxtFile(String path) {
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

	private void identifyBmpBatch(boolean printlog) {
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
				if(log.isInfoEnabled()){
					log.info(outBuf.toString());
				}
			}
		}
	}

	private boolean batchAddTestBmp(boolean needDevic) {
		int[] sizeFPTemp = new int[1];
		sizeFPTemp[0] = 2048;
		for (int i = 0; i < fingerprintArry.size(); i++) {
			FingerprintVo vo = fingerprintArry.get(i);
			iFid= java.lang.Math.max(iFid,FingerprintSensorEx.DBCount(mhDevice)+1);
			int ret3 = FingerprintSensorEx.DBAdd(mhDevice, iFid, vo.getData());
			if (ret3 == 0) {
				fingerprintDbArry.add(iFid-1,vo);
				iFid++;
			} else {
				if(!needDevic){
					fingerprintDbArry.add(iFid-1,vo);
					iFid++;
				}else{
					return false;
				}
			}
		}
		return true;
	}

	private boolean bmpToKey() {
		String parentPath = ".."+fileSeparator+"external"+fileSeparator+"fingerprint"+fileSeparator+"bmp";
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
					String fileKeyPath = ".."+fileSeparator+"external"+fileSeparator+"fingerprint"+fileSeparator+"bmp-key"+fileSeparator+""
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
					fs.write(("libzkfp:"+_retLen[0]+":"+FingerprintSensorEx.BlobToBase64(regTemp,
							_retLen[0])+"\r\n").getBytes());
					fs.close();
				} catch (Throwable t) {
					log.warn("",t);
				}
			}
		}
		return true;
	}

	private int byteArrayToInt(byte[] bytes) {
		int number = bytes[0] & 0xFF;
		// "|="按位或赋值。
		number |= ((bytes[1] << 8) & 0xFF00);
		number |= ((bytes[2] << 16) & 0xFF0000);
		number |= ((bytes[3] << 24) & 0xFF000000);
		return number;
	}

	private class WorkThread extends Thread {
		@Override
		public void run() {
			super.run();
			int ret = 0;
			haveStopped = false;
			if(log.isInfoEnabled()){
				log.info("WorkThread begin...");
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			while (!mbStop) {
				ret =lockEvent.waitEvent(1000);
				if( ret !=0){
					log.warn("lockEvent.waitEvent(1000)="+ret+"!=0,lockEvent.getHoldCount="+lockEvent.getHoldCount()+",lockEvent.getQueueLength="+lockEvent.getQueueLength()+",mbWork="+mbWork);
					lockEvent.setEvent();
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				if(!mbWork){
					lockEvent.setEvent();
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}

				templateLen[0] = 2048;

				if (log.isTraceEnabled()) {
					log.trace("FingerprintSensorEx.AcquireFingerprint begin ...");
				}
				lockEvent.setEvent();
				try {
					ret = FingerprintSensorEx.AcquireFingerprint(mhDevice,
							imgbuf, template, templateLen);
					if (log.isTraceEnabled()) {
						log.trace("FingerprintSensorEx.AcquireFingerprint return "
								+ ret + ",nFakeFunOn=" + nFakeFunOn);
					}

					if (0 == (ret)) {
						if (nFakeFunOn == 1) {
							byte[] paramValue = new byte[4];
							int[] size = new int[1];
							size[0] = 4;
							int nFakeStatus = 0;
							// GetFakeStatus
							if (log.isTraceEnabled()) {
								log.trace("GetParameters(" + mhDevice
										+ ", 2004) begin...");
							}
							ret = FingerprintSensorEx.GetParameters(mhDevice,
									2004, paramValue, size);
							if (log.isTraceEnabled()) {
								log.trace("GetParameters(" + mhDevice
										+ ", 2004) return " + ret);
							}
							nFakeStatus = byteArrayToInt(paramValue);
							if (0 == ret && (byte) (nFakeStatus & 31) != 31) {
								cmdPrompt = "Is a fake-finer?";
								log.warn("ret = " + ret + ",nFakeStatus="
										+ nFakeStatus + ",Is a fake-finer?");
								if (log.isInfoEnabled()) {
									log.info(cmdPrompt);
								}
								return;
							}
						}
						OnCatpureOK(imgbuf);
						OnExtractOK(template, templateLen[0]);
						if(log.isInfoEnabled()){
							log.info(cmdPrompt);
						}
					} else {
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						log.error("", e);
					}

				} catch (Throwable e) {
					log.error("", e);
				}finally{
					lockEvent.setEvent();
				}
				
			}
			haveStopped = true;
			if(log.isInfoEnabled()){
				log.info("WorkThread return.");
			}
		}
	}

	private void OnCatpureOK(byte[] imgBuf) {
		try {
			writeBitmap(imgBuf, fpWidth, fpHeight, fingerprintBmpPath);
			// btnImg.setIcon(new ImageIcon(ImageIO.read(new
			// File(fingerprintBmpPath))));
		} catch (Throwable e) {
			log.error("",e);
		}
	}

	private void OnExtractOK(byte[] template, int len) {
		if (bRegister || bCollection) {
			int ret = 0;
			if (bRegister) {
				int[] fid = new int[1];
				int[] score = new int[1];
				ret = FingerprintSensorEx
						.DBIdentify(mhDB, template, fid, score);
				if (ret == 0) {
					cmdPrompt = "the finger already enroll by " + fid[0]
							+ ",cancel enroll";
					// bRegister = false;
					if(lockEvent.waitEvent(1500)!=0){
						log.warn("lockEvent.waitEvent(1500)!=0");
						lockEvent.setEvent();
						return;
					}
					enroll_idx = 0;
					enrollState = this.STATE_NEED_PRESS_3_TIMES;
					lockEvent.setEvent();
					return;
				}
			}
			if(lockEvent.waitEvent(1500)!=0){
				log.warn("lockEvent.waitEvent(1500)!=0");
				lockEvent.setEvent();
				return;
			}
			if (enroll_idx > 0 && enroll_idx < regtemparray.length) {
				if (log.isTraceEnabled()) {
					log.trace("FingerprintSensorEx.DBMatch begin...");
				}
				ret = FingerprintSensorEx.DBMatch(mhDB,
						regtemparray[enroll_idx - 1], template);
				if (log.isTraceEnabled()) {
					log.trace("FingerprintSensorEx.DBMatch return " + ret);
				}
				if (ret <= 0) {
					cmdPrompt = "please press the same finger 3 times for the enrollment";
					enroll_idx = 0;
					enrollState = this.STATE_NOT_MATCH;
					lockEvent.setEvent();
					return;
				}
			}
			if (enroll_idx < regtemparray.length) {
				System.arraycopy(template, 0, regtemparray[enroll_idx], 0, 2048);
			}
			enroll_idx++;
			if (enroll_idx == 3) {
				int[] _retLen = new int[1];
				_retLen[0] = 2048;
				byte[] regTemp = new byte[_retLen[0]];

				if (bRegister) {
					iFid = java.lang.Math.max(iFid,
							FingerprintSensorEx.DBCount(mhDevice) + 1);
				}
				if (log.isTraceEnabled()) {
					log.trace("FingerprintSensorEx.DBMerge begin...");
				}
				ret = FingerprintSensorEx.DBMerge(mhDB, regtemparray[0],
						regtemparray[1], regtemparray[2], regTemp, _retLen);
				if (log.isTraceEnabled()) {
					log.trace("FingerprintSensorEx.DBMerge return " + ret);
				}
				if (0 != ret){
					enrollState = STATE_DBMERGE_ERROR ;
					lockEvent.setEvent();
					return;
				}
				if(bRegister){
					int index =0;
					if(currentOwner.startsWith(speciFidPreKey)){
						try {
							index = Integer.parseInt(currentOwner.substring(speciFidPreKey.length()));
						} catch (NumberFormatException e) {
//							e.printStackTrace();
						}
						if (index>0){
							ret = FingerprintSensorEx.DBDel(mhDB, index);
							if(log.isDebugEnabled()){
								log.debug("FingerprintSensorEx.DBDel("+mhDB+","+index+") return "+ret);
							}
							if(ret==0){
//								fingerprintDbArry.remove(index-1);
							}
//							ret = FingerprintSensorEx.DBDel(mhDB, 50000);
//							if(ret==0){
//								if(iFid==50001){
//									iFid=50000;
//								}
//							}
							if(log.isDebugEnabled()){
								log.debug("FingerprintSensorEx.DBDel("+mhDB+",50000) return "+ret);
							}
							ret = FingerprintSensorEx.DBAdd(mhDB, index, regTemp);
							if(ret==0){
								FingerprintVo vo=new FingerprintVo();
								vo.setImagePath(fingerprintBmpPath);
								vo.setOwner(getOwnerByPath(fingerprintBmpPath,0));
								MySourceAFIS.load(vo);
								
								vo.setData(regTemp);
								vo.setFid(index);
								vo.setName(currentOwner);
								vo.setOwner(currentOwner);
								fingerprintDbArry.set(index-1,vo);
							}
							if(log.isDebugEnabled()){
								log.debug("FingerprintSensorEx.DBAdd("+mhDB+","+index+",regTemp) return "+ret);
							}
							if(ret!=0){
								ret = FingerprintSensorEx.DBAdd(mhDB, iFid, regTemp);
								if(log.isDebugEnabled()){
									log.debug("FingerprintSensorEx.DBAdd("+mhDB+","+iFid+",regTemp) return "+ret);
								}
								if(ret==0){
									FingerprintVo vo=new FingerprintVo();
									vo.setImagePath(fingerprintBmpPath);
									vo.setOwner(getOwnerByPath(fingerprintBmpPath,0));
									MySourceAFIS.load(vo);
									vo.setData(regTemp);
									vo.setFid(iFid);
									vo.setName(currentOwner);
									vo.setOwner(currentOwner);
									fingerprintDbArry.add(iFid-1, vo);
									iFid++;
								}
							}
						}
					}else{
						ret = FingerprintSensorEx.DBAdd(mhDB, iFid, regTemp);
						if(log.isDebugEnabled()){
							log.debug("FingerprintSensorEx.DBAdd("+mhDB+","+iFid+",regTemp) return "+ret);
						}
						if(ret==0){
							FingerprintVo vo=new FingerprintVo();
							vo.setImagePath(fingerprintBmpPath);
							vo.setOwner(getOwnerByPath(fingerprintBmpPath,0));
							MySourceAFIS.load(vo);
							vo.setData(regTemp);
							vo.setFid(iFid);
							vo.setName(currentOwner);
							vo.setOwner(currentOwner);
							fingerprintDbArry.add(iFid-1, vo);
							iFid++;
						}
					}
					if (0 != ret){
						enrollState = STATE_DBADD_ERROR ;
						cmdPrompt = "DBAdd FAIL!ret="+ret;
						lockEvent.setEvent();
						return;
					}
					if(currentOwner.startsWith(speciFidPreKey)){
						if(index==iFid){
							iFid++;
						}
					}else{
						iFid++;
					}
				}
				enrollState = this.STATE_OK;
				cbRegTemp = _retLen[0];
				System.arraycopy(regTemp, 0, lastRegTemp, 0, cbRegTemp);
				// Base64 Template
				cmdPrompt = "enroll succ";

				currentOwnerRegTempBase64 = "libzkfp:"
						+ _retLen[0]
						+ ":"
						+ FingerprintSensorEx.BlobToBase64(regTemp,
								_retLen[0]);

				if (bCollection) {

				} else {
					if (currentOwner == null
							|| currentOwner.trim().equals("")) {
						currentOwner = "" + (iFid - 1);
					}
					String currentOwnerFileName=currentOwner;
					if(currentOwnerFileName.indexOf("=")>=0){
						currentOwnerFileName=currentOwnerFileName.substring(currentOwnerFileName.indexOf("=")+1);
					}
					String bmpFilePath = ".."+fileSeparator+"external"+fileSeparator+"fingerprint"+fileSeparator+"collect"+fileSeparator+"fingerprint-"
							+ currentOwnerFileName
							+ "-"
							+ new SimpleDateFormat("yyyyMMdd-HHmmss-SSS")
									.format(new Date()) + ".bmp";
					String txtFilePath = ".."+fileSeparator+"external"+fileSeparator+"fingerprint"+fileSeparator+"collect-base64"+fileSeparator+"fingerprint-"
							+ currentOwnerFileName
							+ "-"
							+ new SimpleDateFormat("yyyyMMdd-HHmmss-SSS")
									.format(new Date())
							+ "-DBMerge-"
							+ _retLen[0] + ".txt";
					try {
						writeBitmap(imgbuf, fpWidth, fpHeight, bmpFilePath);
						File fileSet = new File(txtFilePath);
						if (fileSet.exists()) {
							fileSet.delete();
						}
						fileSet.createNewFile();
						FileOutputStream fs = new FileOutputStream(fileSet,
								true); // 在该文件的末尾添加内容
//						fs.write(FingerprintSensorEx.BlobToBase64(regTemp,
//								_retLen[0]).getBytes());
						fs.write(("libzkfp:"+_retLen[0]+":"+FingerprintSensorEx.BlobToBase64(regTemp,
								_retLen[0])+"\r\n").getBytes());
						fs.close();
					} catch (Throwable t) {
						t.printStackTrace();
						log.warn("",t);
					}

					FingerprintVo vo = new FingerprintVo();
					if (log.isTraceEnabled()) {
						log.trace("currentOwner=" + currentOwner);
					}
					vo.setImagePath(fingerprintBmpPath);
					vo.setOwner(getOwnerByPath(fingerprintBmpPath,0));
					MySourceAFIS.load(vo);

					vo.setOwner(currentOwner);
					vo.setFid(iFid - 1);
					vo.setName(bmpFilePath);
					vo.setData(lastRegTemp);
					fingerprintArry.add(vo);
					fingerprintDbArry.add(iFid-1-1,vo);
				}
			} else {
				cmdPrompt = "You need to press the " + (3 - enroll_idx)
						+ " times fingerprint";
				if(3 - enroll_idx == 3){
					enrollState = this.STATE_NEED_PRESS_2_TIMES;
				}else if(3 - enroll_idx == 2){
					enrollState = this.STATE_NEED_PRESS_2_TIMES;
				}else if(3 - enroll_idx == 1){
					enrollState = this.STATE_NEED_PRESS_1_TIMES;
				}else{
					enrollState = STAT_PRESS_TOO_MUCH_TIMES;
				}
			}
			lockEvent.setEvent();
		} else {
			if (bIdentify) {
				int[] fid = new int[1];
				int[] score = new int[1];
				int ret = FingerprintSensorEx.DBIdentify(mhDB, template, fid,
						score);
				if (ret == 0) {
					cmdPrompt = "Identify succ, fid=" + fid[0] + ",score="
							+ score[0];
//					for (int i = 0; i < fingerprintArry.size(); i++) {
//						if (log.isTraceEnabled()) {
//							log.trace("fingerprintArry.get(" + i
//									+ ").getFid()="
//									+ fingerprintArry.get(i).getFid()
//									+ ",fid[0]=" + fid[0] + ",currentOwner="
//									+ currentOwner);
//						}
//						if (fingerprintArry.get(i).getFid() == fid[0]) {
//							currentOwner = fingerprintArry.get(i).getOwner();
//							break;
//						}
//					}
					if(fid[0]<fingerprintDbArry.size()){
						currentOwner = fingerprintDbArry.get(fid[0]).getOwner();
					}
					if (bDel) {
						long beginTime=System.currentTimeMillis();
						StringBuffer outBuf=new StringBuffer();
						outBuf.delete(0, outBuf.length());
						int count=0;
						while (ret == 0) {
							count++;
							outBuf.append(fid[0] + "=" + score[0] + ";");
							FingerprintSensorEx.DBDel(mhDB, fid[0]);
							ret = FingerprintSensorEx.DBIdentify(mhDB,
									template, fid, score);
						}
						outBuf.append("(count="+count+",duration="+(System.currentTimeMillis()-beginTime)+")");
						cmdPrompt = "Del succ:"+outBuf.toString();

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
	private String readFileByChars(String fileName) {
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

	private void writeBitmap(byte[] imageBuf, int nWidth, int nHeight,
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

	private byte[] changeByte(int data) {
		return intToByteArray(data);
	}

	private byte[] intToByteArray(final int number) {
		byte[] abyte = new byte[4];
		// "&" 与（AND），对两个整型操作数中对应位执行布尔代数，两个位都为1时输出1，否则0。
		abyte[0] = (byte) (0xff & number);
		// ">>"右移位，若为正数则高位补0，若为负数则高位补1
		abyte[1] = (byte) ((0xff00 & number) >> 8);
		abyte[2] = (byte) ((0xff0000 & number) >> 16);
		abyte[3] = (byte) ((0xff000000 & number) >> 24);
		return abyte;
	}

	public void FreeSensor() {
		int ret = lockEvent.waitEvent(8000);
		if(ret!=0){
			log.warn("lockEvent.waitEvent(8000)="+ret+"!=0");
			lockEvent.setEvent();
		}

//		mbStop = true;
		lockEvent.setEvent();
		int index = 0;
		while (!haveStopped  && index < 10){
			try { // wait for thread stopping
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				log.error("", e);
			}
			index ++;
		}
		ret = lockEvent.waitEvent(8000);
		if(ret!=0){
			log.warn("lockEvent.waitEvent(8000)="+ret+"!=0");
			lockEvent.setEvent();
		}
		if (0 != mhDB) {
			FingerprintSensorEx.DBFree(mhDB);
			mhDB = 0;
		}
		if (0 != mhDevice) {
			FingerprintSensorEx.CloseDevice(mhDevice);
			mhDevice = 0;
		}
		FingerprintSensorEx.Terminate();
		manufacturer = "";
		deviceCount = 0;
		cmdPrompt = "";
		while (fingerprintArry.size() > 0) {
			fingerprintArry.remove(0);
		};
//		mbStop=false;
		mbWork=false;
		bCollection=false;
		bRegister=false;
		bIdentify=false;
		lockEvent.setEvent();
	}
	public void stop() {
		int ret = lockEvent.waitEvent(8000);
		if(ret!=0){
			log.warn("lockEvent.waitEvent(8000)="+ret+"!=0");
			lockEvent.setEvent();
		}
		thPool.killTimeOutTask(10000);
		thPool.stop();

		mbStop=true;
		mbWork=false;
		try {
			workThread.stop();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			workThread.destroy();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		lockEvent.setEvent();
	}

	private class FPMatchTask implements INmaCollectorTaskInf{
		private byte[] fpTemplate;
		private int minScore;
		private Hashtable<String,FingerprintVo> result;
		private int beginPos;
		private int endPos;
		private String name="";

		
		public FPMatchTask(byte[] fpTemplate,int minScore,Hashtable<String,FingerprintVo> result,int beginPos,int endPos){
			this.fpTemplate=fpTemplate;
			this.minScore=minScore;
			this.result=result;
			this.beginPos=beginPos;
			this.endPos=endPos;
			this.name=Thread.currentThread().getId()+"-"+System.currentTimeMillis()+"-"+beginPos+"-"+endPos;
		}
		public Object call() {
			if(log.isDebugEnabled()){
				log.debug(name + " begin...");
			}
			try {
				if(minScore<=0){
					if(log.isDebugEnabled()){
						log.debug(name + " return null");
					}
					return null;
				}
				int ret = 0;
				for (int j = beginPos; j <= endPos ; j++) {
					ret = ZKFPService.VerifyFPByID(j, fpTemplate);
//					ret = ZKFPService.MatchFP(fingerprintDbArry.get(j-1).getData(), fpTemplate);
					if(ret<minScore){
						continue;
					}
					FingerprintVo  vo = fingerprintDbArry.get(j-1);//getFingerprintVo(j);
					String labelReference = "100_00000";
					if(ret<10){
						labelReference="00"+ret;
					}else if(ret<100){
						labelReference="0"+ret;
					}else{
						labelReference=""+ret;
					}
					labelReference=labelReference+"_";
					if(j<10){
						labelReference=labelReference+"0000"+j;
					}else if(j<100){
						labelReference=labelReference+"000"+j;
					}else if(j<1000){
						labelReference=labelReference+"00"+j;
					}else if(j<10000){
						labelReference=labelReference+"0"+j;
					}else if(j<100){
						labelReference=labelReference+""+j;
					}
					result.put(labelReference, vo);
				}
			} catch (Throwable t) {
				log.error("FPMatchTask Throwable:"+t.toString(),t);
			}
			if(log.isDebugEnabled()){
				log.debug(name + " return null,result.size()="+result.size());
			}
			return null;
		}
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

	}

//	public interface MyZKFPService extends Library {
//		MyZKFPService INSTANCE = (MyZKFPService)Native.loadLibrary(("libzkfp.dll"),	MyZKFPService.class);
//
//	    int DBIdentify(long hDBCache,  byte[] fpTemplate, int cbTemplate,int[] FID,  int[] score);
////	    int DBCount(long hDBCache, int[] fpCount);	//same as ZKFPM_GetDBCacheCount, for new version
//	    int DBCount();
//	}
//
//	public static int  DBIdentify(long hDBCache,  byte[] fpTemplate, int cbTemplate,int[] FID,  int[] score){
//		int ret=MyZKFPService.INSTANCE.DBIdentify(hDBCache,fpTemplate,cbTemplate,FID,score);
//		return ret;
//	}
	
//	int DBCount(long hDBCache, int[] fpCount){
//		int ret=MyZKFPService.INSTANCE.DBCount(hDBCache,fpCount);
//		return ret;
//	}
	public static int DBCount(){
//		int ret=MyZKFPService.INSTANCE.DBCount();
//		return ret;
		return ZKFPService.DBCount();
	}
}