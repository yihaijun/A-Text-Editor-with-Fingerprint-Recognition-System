/**
 * 
 */
package com.tisson.fingerprint.FingerprintSensorTool;

import com.machinezoo.sourceafis.FingerprintTemplate;
import com.tisson.sfip.module.util.SystemUtil;

/**
 * @author yihaijun
 *
 */
public class FingerprintVo {
	private int type=FingerprintTypeEnum.ZKLIB.getCode();
	
	private String owner;

	private String imagePath;

	private byte[] fpTemplateByteArry;

	private FingerprintTemplate fpTemplate;
	private String fpTemplateJsonStr;

	private String collector=SystemUtil.getLocalHostAddress();
	
//	private FingerprintVo(){
//		
//	}
	
	/**
	 * @return the fpTemplateByteArry
	 */
	public byte[] getFpTemplateByteArry() {
		return fpTemplateByteArry;
	}
	/**
	 * @param fpTemplateByteArry the fpTemplateByteArry to set
	 */
	public void setFpTemplateByteArry(byte[] fpTemplateByteArry) {
		this.fpTemplateByteArry = fpTemplateByteArry;
	}
	/**
	 * @return the name
	 */
	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}
	/**
	 * @param owner the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}
	/**
	 * @return the fpTemplate
	 */
	public FingerprintTemplate getFpTemplate() {
		return fpTemplate;
	}
	/**
	 * @param fpTemplate the fpTemplate to set
	 */
	public void setFpTemplate(FingerprintTemplate fpTemplate) {
		this.fpTemplate = fpTemplate;
	}
	/**
	 * @return the fpTemplateJsonStr
	 */
	public String getFpTemplateJsonStr() {
		return fpTemplateJsonStr;
	}
	/**
	 * @param fpTemplateJsonStr the fpTemplateJsonStr to set
	 */
	public void setFpTemplateJsonStr(String fpTemplateJsonStr) {
		this.fpTemplateJsonStr = fpTemplateJsonStr;
	}
	/**
	 * @return the imagePath
	 */
	public String getImagePath() {
		return imagePath;
	}
	/**
	 * @param imagePath the imagePath to set
	 */
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}	

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}
	/**
	 * @return the collector
	 */
	public String getCollector() {
		return collector;
	}
	public static String getLabelReference(int ret,int j){
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
}
