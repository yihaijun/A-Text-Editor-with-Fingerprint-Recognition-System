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

	public static double match(FingerprintTemplate probe, FingerprintTemplate candidate) {
		double score = new FingerprintMatcher().index(probe).match(candidate);
		return score;
	}

	public static double match(byte[] probeImage, byte[] candidateImage) {
		FingerprintTemplate probe = new FingerprintTemplate().dpi(500).create(
				probeImage);

		FingerprintTemplate candidate = new FingerprintTemplate().dpi(500)
				.create(candidateImage);

		return match(probe,candidate);
	}



	public static Hashtable<String, FingerprintVo> find(FingerprintTemplate probe,
			java.util.ArrayList<FingerprintVo> candidates, double threshold,int beginPos,int endPos) {
		Hashtable<String, FingerprintVo> result = new Hashtable<String, FingerprintVo>();
		if(probe==null){
			return result;
		}
		FingerprintMatcher matcher = new FingerprintMatcher().index(probe);
		for (int index = beginPos;index<=endPos;) {
			if(candidates==null || index>=candidates.size()|| index<=0){
				break;
			}
			FingerprintVo candidate=candidates.get(index-1);
			index++;
			if(candidate==null || candidate.getFpTemplate()==null){
				continue;
			}
			double score = matcher.match(candidate.getFpTemplate());
			if (score > threshold) {
				result.put(FingerprintVo.getLabelReference(Double.valueOf(score).intValue(),index-1), candidate);
			}
		}
		return result;
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
			if(candidate.getFpTemplate()==null){
				continue;
			}
			double score = matcher.match(candidate.getFpTemplate());
			if (score > threshold) {
				result.put(FingerprintVo.getLabelReference(Double.valueOf(score).intValue(),index), candidate);
			}
		}
		return result;
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
		if(vo== null || jsonTemplate == null|| jsonTemplate.trim().equals("")){
			return -1;
		}
		try {
			vo.setFpTemplateJsonStr(jsonTemplate);
			vo.setFpTemplate(new FingerprintTemplate().deserialize(vo
					.getFpTemplateJsonStr()));
			
			vo.setType(FingerprintTypeEnum.SourceAFIS.getCode());
			
		} catch (Throwable t) {
			log.warn("load("+ vo.getImagePath()+") Throwable",t);
			return -3;
		}
		return 0;
	}
}
