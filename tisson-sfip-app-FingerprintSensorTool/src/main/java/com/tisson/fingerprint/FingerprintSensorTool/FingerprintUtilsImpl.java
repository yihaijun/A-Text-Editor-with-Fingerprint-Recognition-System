/**
 * 
 */
package com.tisson.fingerprint.FingerprintSensorTool;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.rpc.RpcContext;
import com.tisson.fingerprint.FingerprintSensorTool.sfipappsample.HellWordBeanImpl;
import com.tisson.sfip.api.message.request.HelloWordRequest;
import com.tisson.sfip.api.message.response.HelloWordResponse;
import com.tisson.sfip.module.util.serviceapitest.SfipServiceResponseSample;

/**
 * @author yihaijun
 */
@Slf4j
@Service
@Path("tisson/FingerprintUtils")
@Consumes({"application/json;charset=UTF-8"})
@Produces({"application/json;charset=UTF-8"})
public class FingerprintUtilsImpl implements FingerprintUtilsInf{
	/**
	 * 
	 */
	private static final long serialVersionUID = 9172334635209012977L;

	private String getResponseCode(int ret){
		return getResponseCode("01",ret);
	}
	
	private String getResponseCode(String method,int ret){
		if (ret == FingerprintSensorHandle.STATE_OK || ret == FingerprintSensorHandle.STAT_PRESS_TOO_MUCH_TIMES){
			return "FPU"+method+HelloWordResponse.KEY_STATUS_SUCCE+"00000" + ret;
		}
		if (ret == FingerprintSensorHandle.STATE_NEED_PRESS_3_TIMES || ret == FingerprintSensorHandle.STATE_NEED_PRESS_2_TIMES || ret == FingerprintSensorHandle.STATE_NEED_PRESS_1_TIMES){
			return "FPU"+method+HelloWordResponse.KEY_STATUS_UNKNOW+"00000" + ret;
		}
		if (ret == 0){
			return "FPU"+method+"00"+"000000";
		}
		String retCode=""+ret;
		if(retCode.length()>6){
			retCode = retCode.substring(retCode.length()-6);
		}
		while(retCode.length()<6){
			retCode="0"+retCode;
		}
		return "FPU"+method+HelloWordResponse.KEY_STATUS_FAIL+retCode;
	}

    @POST
    @Path("call")
	public HelloWordResponse helloWord(HelloWordRequest msg) {
		if (log.isTraceEnabled()) {
			log.trace(msg.toString());
		} else if (log.isDebugEnabled()) {
			if (!msg.getBeanName().equalsIgnoreCase("getCmdPrompt")) {
				log.debug(msg.toString());
			}
		}
		HelloWordResponse response = new HelloWordResponse();
		response.setResponseCode("FPU01"+HelloWordResponse.KEY_STATUS_SUCCE+"000000");
		response.setResponseContent("");
		response.setStatusCompletion(SfipServiceResponseSample.SUCCE_COMPLETION);

		try{
			int ret = 0;
			if(msg.getAppName() != null && !msg.getAppName().trim().equals("")){
				//调用其它辅助应用的bean,以后增加加,现暂时直接返回当前操作状态
				response.setResult(FingerprintSensorHandle.getInstance().getInstance().getCmdPrompt());
			}else if(msg.getBeanName().equalsIgnoreCase("load")){
				boolean needDevice = true;
				if(msg.getMsg() !=null && msg.getMsg().equalsIgnoreCase("false")){
					needDevice = false;
				}
				ret = FingerprintSensorHandle.getInstance().load(needDevice);
				response.setResult(FingerprintSensorHandle.getInstance().getManufacturer() +"," + FingerprintSensorHandle.getInstance().getFingerprintArryCount()+"/"+FingerprintSensorHandle.getInstance().getFingerprintDbArryCount()+"/"+FingerprintSensorHandle.getInstance().DBCount()+"/"+FingerprintSensorHandle.getInstance().getiFid()+",SfipHome="+com.tisson.sfip.util.SystemUtils.getSfipHome());
			}else if(msg.getBeanName().equalsIgnoreCase("cmdFreeSensor")){
				FingerprintSensorHandle.getInstance().FreeSensor();
				response.setResult("SfipHome="+com.tisson.sfip.util.SystemUtils.getSfipHome());
			}else if(msg.getBeanName().equalsIgnoreCase("getFingerprintSensorInfo")){
				response.setResponseCode("FPU00"+HelloWordResponse.KEY_STATUS_SUCCE+"000000");
				response.setResult(FingerprintSensorHandle.getInstance(). getManufacturer());
			}else if(msg.getBeanName().equalsIgnoreCase("cmdCollection")){
				ret = FingerprintSensorHandle.getInstance().cmdCollection();
				response.setResponseCode(getResponseCode("01",ret));
				response.setResult(FingerprintSensorHandle.getInstance().getManufacturer() +"," + FingerprintSensorHandle.getInstance().getFingerprintArryCount());
			}else if(msg.getBeanName().equalsIgnoreCase("cmdEnroll")){
				ret = FingerprintSensorHandle.getInstance().cmdEnroll(msg.getMsg());
				response.setResponseCode(getResponseCode("01",ret));
				response.setResult(FingerprintSensorHandle.getInstance().getManufacturer() +"," + FingerprintSensorHandle.getInstance().getFingerprintArryCount());
			}else if(msg.getBeanName().equalsIgnoreCase("getCurrentOwner")){
				response.setResponseCode("FPU00"+HelloWordResponse.KEY_STATUS_SUCCE+"000000");
				response.setResult(FingerprintSensorHandle.getInstance().getCurrentOwner());
			}else if(msg.getBeanName().equalsIgnoreCase("getCurrentOwnerRegTempBase64")){
				response.setResponseCode(getResponseCode("00",FingerprintSensorHandle.getInstance().getEnrollState()));
				String cmdErrorCode = getResponseCode("00",FingerprintSensorHandle.getInstance().getEnrollState());
            	if(cmdErrorCode.equals("FPU0100000000") || cmdErrorCode.equals("FPU0100000004") || cmdErrorCode.equals("FPU0000000000") || cmdErrorCode.equals("FPU0000000004")){
					response.setResult(FingerprintSensorHandle.getInstance().getCurrentOwnerRegTempBase64());
            	}else{
            		response.setResult(FingerprintSensorHandle.getInstance().getCmdPrompt());
				}
			}else if(msg.getBeanName().equalsIgnoreCase("cmdIdentify")){
				ret = FingerprintSensorHandle.getInstance().cmdIdentify();
				response.setResponseCode(getResponseCode("01",ret));
				response.setResult(FingerprintSensorHandle.getInstance().getManufacturer() +"," + FingerprintSensorHandle.getInstance().getFingerprintArryCount());
			}else if(msg.getBeanName().equalsIgnoreCase("cmdVerify")){
				response.setResult(FingerprintSensorHandle.getInstance().cmdVerify(msg.getMsg()));
			}else if(msg.getBeanName().equalsIgnoreCase("cmdTxtVerify")){
				response.setResult(FingerprintSensorHandle.getInstance().cmdTxtVerify(msg.getMsg()));
			}else if(msg.getBeanName().equalsIgnoreCase("cmdDel")){
				ret = FingerprintSensorHandle.getInstance().cmdDel(msg.getMsg());
				response.setResponseCode(getResponseCode("01",ret));
				response.setResult(FingerprintSensorHandle.getInstance().getManufacturer() +"," + FingerprintSensorHandle.getInstance().getFingerprintArryCount());
			}else if(msg.getBeanName().equalsIgnoreCase("getCmdPrompt")){
				response.setResponseCode("FPU00"+HelloWordResponse.KEY_STATUS_SUCCE+"000000");
				response.setResult(FingerprintSensorHandle.getInstance().getCmdPrompt() +"("+ FingerprintSensorHandle.getInstance().getFingerprintArryCount()+"/"+FingerprintSensorHandle.getInstance().getFingerprintDbArryCount()+"/"+FingerprintSensorHandle.getInstance().DBCount()+"/"+FingerprintSensorHandle.getInstance().getiFid()+")");
			}else{
				response.setResponseCode(getResponseCode("02",1));
				response.setResult(FingerprintSensorHandle.getInstance().getCmdPrompt());
			}
			
			RpcContext.getContext().getResponse(HttpServletResponse.class).addHeader("Access-Control-Allow-Origin","*");
		} catch (Throwable t) {
			log.warn("",t);
			response.setResponseCode("FPU02"+HelloWordResponse.KEY_STATUS_FAIL+"000"+ FingerprintSensorHandle.STATE_UNKNOW_ERROR);
		}
		if (log.isTraceEnabled()) {
			log.trace(response.toString());
		} else if (log.isDebugEnabled()) {
			if (!msg.getBeanName().equalsIgnoreCase("getCmdPrompt")) {
				log.debug(response.toString());
			}
		}
		return response;
	}

	@OPTIONS
	@Path("call")
	public HelloWordResponse helloWordOPTIONS(HelloWordRequest msg) {
		if (log.isTraceEnabled()) {
			log.trace(msg.toString());
		}
		return helloWord(msg);
	}

	public void init(){
		FingerprintSensorHandle.getInstance().load(true);
	}
	
	public void onExit() {
		FingerprintSensorHandle.getInstance().FreeSensor();
		FingerprintSensorHandle.getInstance().stop();
	}

    @GET
    @Path("call")
	public HelloWordResponse helloWordGET(HelloWordRequest msg) {
		if (log.isTraceEnabled()) {
			log.trace(msg.toString());
		}
		return helloWord(msg);
	}
}
