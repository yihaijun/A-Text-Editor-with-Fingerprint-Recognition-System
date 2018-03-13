/**
 * 
 */
package com.tisson.fingerprint.FingerprintSensorTool.sfipappsample;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import com.tisson.fingerprint.FingerprintSensorTool.FingerprintSensorHandle;
import com.tisson.sfip.api.message.request.HelloWordRequest;
import com.tisson.sfip.api.message.response.HelloWordResponse;
import com.tisson.sfip.api.service.HellWordBeanInterface;
import com.tisson.sfip.module.util.SystemUtil;
import com.tisson.sfip.module.util.serviceapitest.SfipServiceResponseSample;

/**
 * @author yihaijun
 *
 */
@Slf4j
@Service
public class HellWordBeanImpl implements HellWordBeanInterface {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1067315977501626921L;

	public HelloWordResponse helloWord(HelloWordRequest msg) {
//		log.info("recive:"+msg);
		
		HelloWordResponse response = new HelloWordResponse();
		response.setResponseCode("TST00"+HelloWordResponse.KEY_STATUS_SUCCE+"000000");
		response.setResponseContent("OK");
		response.setStatusCompletion(SfipServiceResponseSample.SUCCE_COMPLETION);

		int ret = 0;
		if(msg.getBeanName().equalsIgnoreCase("load")){
			ret = FingerprintSensorHandle.load();
			response.setResult(FingerprintSensorHandle.getManufacturer() +"," + FingerprintSensorHandle.getFingerprintArryCount()+",SfipHome="+com.tisson.sfip.util.SystemUtils.getSfipHome());
		}else if(msg.getBeanName().equalsIgnoreCase("cmdEnroll")){
			ret = FingerprintSensorHandle.cmdEnroll(msg.getMsg());
			response.setResult(FingerprintSensorHandle.getManufacturer() +"," + FingerprintSensorHandle.getFingerprintArryCount());
		}else if(msg.getBeanName().equalsIgnoreCase("getCurrentOwner")){
			response.setResult(FingerprintSensorHandle.getCurrentOwner());
		}else if(msg.getBeanName().equalsIgnoreCase("getCurrentOwnerRegTempBase64")){
			response.setResult(FingerprintSensorHandle.getCurrentOwnerRegTempBase64());
		}else if(msg.getBeanName().equalsIgnoreCase("cmdIdentify")){
			ret = FingerprintSensorHandle.cmdIdentify();
			response.setResult(FingerprintSensorHandle.getManufacturer() +"," + FingerprintSensorHandle.getFingerprintArryCount());
		}else if(msg.getBeanName().equalsIgnoreCase("cmdVerify")){
			response.setResult(FingerprintSensorHandle.cmdVerify(msg.getMsg()));
		}else if(msg.getBeanName().equalsIgnoreCase("cmdTxtVerify")){
			response.setResult(FingerprintSensorHandle.cmdTxtVerify(msg.getMsg()));
		}else if(msg.getBeanName().equalsIgnoreCase("getCmdPrompt")){
			response.setResult(FingerprintSensorHandle.getCmdPrompt());
		}else{
			response.setResult(FingerprintSensorHandle.getCmdPrompt());
		}
		return response;
	}

}
