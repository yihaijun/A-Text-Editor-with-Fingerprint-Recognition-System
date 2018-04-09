/**
 * 
 */
package com.tisson.fingerprint.FingerprintSensorTool;

import com.machinezoo.sourceafis.FingerprintTemplate;

/**
 * @author yihaijun
 *
 */
public class FingerprintVo {
	private byte[] data;
	private String name;
	private String owner;

	private String imagePath;
	private FingerprintTemplate template;
	private String jsonTemplate;
	
	private int fid=-1;
	
	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}
	/**
	 * @param data the data to set
	 */
	public void setData(byte[] data) {
		this.data = data;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the fid
	 */
	public int getFid() {
		return fid;
	}
	/**
	 * @param fid the fid to set
	 */
	public void setFid(int fid) {
		this.fid = fid;
	}
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
	 * @return the template
	 */
	public FingerprintTemplate getTemplate() {
		return template;
	}
	/**
	 * @param template the template to set
	 */
	public void setTemplate(FingerprintTemplate template) {
		this.template = template;
	}
	/**
	 * @return the jsonTemplate
	 */
	public String getJsonTemplate() {
		return jsonTemplate;
	}
	/**
	 * @param jsonTemplate the jsonTemplate to set
	 */
	public void setJsonTemplate(String jsonTemplate) {
		this.jsonTemplate = jsonTemplate;
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
}
