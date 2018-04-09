/**
 * 
 */
package com.tisson.fingerprint.FingerprintSensorTool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import lombok.extern.slf4j.Slf4j;

import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;
import com.zkteco.biometric.FingerprintSensorEx;

/**
 * @author yihaijun
 * 
 */
@Slf4j
public class MySourceAFIS {
	private final static String fileSeparator = System.getProperty("file.separator");

	public static double match(byte[] probeImage, byte[] candidateImage) {
		FingerprintTemplate probe = new FingerprintTemplate().dpi(500).create(
				probeImage);

		FingerprintTemplate candidate = new FingerprintTemplate().dpi(500)
				.create(candidateImage);

		double score = new FingerprintMatcher().index(probe).match(candidate);
		return score;
	}

	public static Hashtable<String, FingerprintVo> find(FingerprintTemplate probe,
			Iterable<FingerprintVo> candidates, double threshold) {
		Hashtable<String, FingerprintVo> result = new Hashtable<String, FingerprintVo>();
		if(probe==null){
			return result;
		}
		FingerprintMatcher matcher = new FingerprintMatcher().index(probe);
		int index = 0;
		for (FingerprintVo candidate : candidates) {
			index++;
			if(candidate.getTemplate()==null){
				continue;
			}
			double score = matcher.match(candidate.getTemplate());
			if (score > threshold) {
				result.put(getLabelReference(Double.valueOf(score).intValue(),index), candidate);
			}
		}
		return result;
	}
	
	private static String getLabelReference(int ret,int j){
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
			labelReference=labelReference+"00000"+j;
		}else if(j<100){
			labelReference=labelReference+"0000"+j;
		}else if(j<1000){
			labelReference=labelReference+"000"+j;
		}else if(j<10000){
			labelReference=labelReference+"00"+j;
		}else if(j<100000){
			labelReference=labelReference+"0"+j;
		}else{
			labelReference=labelReference+""+j;
		}
		return labelReference;
	}

	public static int load(FingerprintVo vo) {
		// Caching fingerprint templates
		if(vo== null || vo.getImagePath()==null || vo.getImagePath().trim().equals("")){
			return -1;
		}
		String txtFilePath = "";
		byte[] image = null;
		try {
			try {
				image = Files.readAllBytes(Paths.get(vo.getImagePath()));
			} catch (IOException e) {
				log.warn("Files.readAllBytes("+ vo.getImagePath()+") IOException",e);
				return -2;
			}
			FingerprintTemplate template = null;
			try {
				template = new FingerprintTemplate().dpi(500)
						.create(image);
			} catch (Throwable e) {
				log.warn("new FingerprintTemplate().dpi(500).create("+ vo.getImagePath()+") Throwable:"+e.toString());
				return -3;
			}
			
			String jsonTemplate = template.serialize();
			
			txtFilePath = ".."+fileSeparator+"external"+fileSeparator+"fingerprint"+fileSeparator+"SourceAFIS"+fileSeparator+"fingerprint-"
			+ vo.getOwner()
//			+ "-"
//			+ new SimpleDateFormat("yyyyMMdd-HHmmss-SSS")
//					.format(new Date())
			+ ".txt";
			File txtFile = new File(txtFilePath);
			if (txtFile.exists()) {
				txtFile.delete();
			}
			txtFile.createNewFile();
			FileOutputStream fs = new FileOutputStream(txtFile,
					true); // 在该文件的末尾添加内容
			fs.write(("SourceAFIS:"+0+":"+jsonTemplate).getBytes());
			fs.close();
			return load(vo,jsonTemplate);
		} catch (Throwable t) {
			log.warn("load("+ vo.getImagePath()+") Throwable(txtFilePath="+txtFilePath+")",t);
			return -4;
		}
	}

	public static int load(FingerprintVo vo,String jsonTemplate) {
		// Caching fingerprint templates
		if(vo== null || vo.getImagePath()==null || vo.getImagePath().trim().equals("") || jsonTemplate == null|| jsonTemplate.trim().equals("")){
			return -1;
		}
		byte[] image = null;
		try {

			vo.setJsonTemplate(jsonTemplate);
			vo.setTemplate(new FingerprintTemplate().deserialize(vo
					.getJsonTemplate()));
		} catch (Throwable t) {
			log.warn("load("+ vo.getImagePath()+") Throwable",t);
			return -3;
		}
		return 0;
	}
}
