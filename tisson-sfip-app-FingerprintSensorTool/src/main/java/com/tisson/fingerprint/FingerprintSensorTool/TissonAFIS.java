/**
 * 
 */
package com.tisson.fingerprint.FingerprintSensorTool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;

import lombok.extern.slf4j.Slf4j;

import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;

/**
 * @author yihaijun
 * 
 */
@Slf4j
public class TissonAFIS {
	private final static String fileSeparator = System.getProperty("file.separator");
	private static String lastErrrorDescribe; 

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
			if(log.isDebugEnabled()){
				if(candidates.size() < 100){
					log.debug("index="+index);
				}
			}
			if(candidates==null || index>candidates.size()|| index<=0){
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
			if(log.isDebugEnabled()){
				if(candidates.size() < 100){
					log.debug("index="+(index-1)+",score="+score);
				}
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
		
		matcher.index(probe);
		
		int index = 0;
		for (FingerprintVo candidate : candidates) {
			index++;
			if(candidate.getFpTemplate()==null){
				continue;
			}
			double score = matcher.match(candidate.getFpTemplate());
			if (score > threshold) {
				result.put(FingerprintVo.getLabelReference(Double.valueOf(score).intValue(),index), candidate);
			}else{
//				if(log.isDebugEnabled()){
//					log.debug("index="+index+",score="+score);
//				}
			}
		}
		return result;
	}
	
	public static int load(FingerprintVo vo) {
		// Caching fingerprint templates
		if(vo== null || vo.getImagePath()==null || vo.getImagePath().trim().equals("")){
			lastErrrorDescribe = "vo is null or vo.getImagePath() is null.";
			return -1;
		}
		String txtFilePath = "";
		byte[] image = null;
		try {
			try {
				image = Files.readAllBytes(Paths.get(vo.getImagePath()));
			} catch (IOException e) {
				lastErrrorDescribe = "Files.readAllBytes("+ vo.getImagePath()+") IOException.";
				log.warn(lastErrrorDescribe,e);
				return -2;
			}
			FingerprintTemplate template = null;
			try {
				template = new FingerprintTemplate().dpi(500)
						.create(image);
			} catch (Throwable e) {
				lastErrrorDescribe = "new FingerprintTemplate().dpi(500).create("+ vo.getImagePath()+") Throwable.";
				log.warn(lastErrrorDescribe,e);
				return -3;
			}
			
			String jsonTemplate = template.serialize();
			
			txtFilePath = ".."+fileSeparator+"external"+fileSeparator+"fingerprint"+fileSeparator+"TissonAFIS"+fileSeparator+"fingerprint-"
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
			String jsonTemplateEx = "TissonAFIS:"+0+":"+jsonTemplate;
			String jsonTemplateZip = ZipUtils.gzip(jsonTemplate);
			jsonTemplateZip = jsonTemplateZip.replaceAll("\r", "");
			jsonTemplateZip = jsonTemplateZip.replaceAll("\n", "");
			String jsonTemplateUnzip = "TissonAFIS:"+0+":"+ZipUtils.gunzip(jsonTemplateZip);
			if(jsonTemplateZip==null){
				jsonTemplateZip="";
			}
			if(jsonTemplateUnzip==null){
				jsonTemplateUnzip="";
			}
			boolean equal = jsonTemplateUnzip.equals(jsonTemplateEx);
			
			log.debug("jsonTemplate.length()="+jsonTemplate.length()+",jsonTemplateZip.length()="+jsonTemplateZip.length()+",equal="+equal);
			
			fs.write(("TissonAFIS:"+0+":"+jsonTemplateZip).getBytes());
			
			fs.close();
			return load(vo,jsonTemplateZip);
		} catch (Throwable t) {
			lastErrrorDescribe = "load("+ vo.getImagePath()+") Throwable.(txtFilePath="+txtFilePath+")";
			log.warn(lastErrrorDescribe,t);
			return -4;
		}
	}

	public static int load(FingerprintVo vo,String jsonTemplate) {
		// Caching fingerprint templates
		if(vo== null || jsonTemplate == null|| jsonTemplate.trim().equals("")){
			return -1;
		}
		try {
			vo.setFpTemplate(new FingerprintTemplate().deserialize(ZipUtils.gunzip(jsonTemplate)));
			
			vo.setType(FingerprintTypeEnum.TissonAFIS.getCode());
			
		} catch (Throwable t) {
			log.warn("load("+ vo.getImagePath()+") Throwable",t);
			return -3;
		}
		return 0;
	}
}
