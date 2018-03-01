/**
 * 
 */
package com.tisson.fingerprint.test;

/**
 * @author yihaijun
 *
 */
public class FingerprintVo {
	private byte[] data;
	private String name;
	int fid;
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
	
	
}
