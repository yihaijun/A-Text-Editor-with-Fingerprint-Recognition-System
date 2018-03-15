/**
 * 
 */
package com.tisson.fingerprint.FingerprintSensorTool;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
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
 *
 */
@Slf4j
@Service
@Path("tisson/FingerprintUtilsInf")
@Consumes({"application/json;charset=UTF-8"})
@Produces({"application/json;charset=UTF-8"})
public class FingerprintUtilsImpl implements FingerprintUtilsInf{
	/**
	 * 
	 */
	private static final long serialVersionUID = 9172334635209012977L;

    @POST
    @Path("helloWord")
	public HelloWordResponse helloWord(HelloWordRequest msg) {
	       if(log.isTraceEnabled()){
	    	   log.trace(msg.toString());
	       }
			HelloWordResponse response = new HelloWordResponse();
			response.setResponseCode("TST00"+HelloWordResponse.KEY_STATUS_SUCCE+"000000");
			response.setResponseContent("OK");
			response.setStatusCompletion(SfipServiceResponseSample.SUCCE_COMPLETION);

			int ret = 0;
			if(msg.getAppName() != null && !msg.getAppName().trim().equals("")){
				//调用其它辅助应用的bean,以后增加加,现暂时直接返回当前操作状态
				response.setResult(FingerprintSensorHandle.getCmdPrompt());
			}else if(msg.getBeanName().equalsIgnoreCase("load")){
				ret = FingerprintSensorHandle.load();
				response.setResult(FingerprintSensorHandle.getManufacturer() +"," + FingerprintSensorHandle.getFingerprintArryCount()+",SfipHome="+com.tisson.sfip.util.SystemUtils.getSfipHome());
			}else if(msg.getBeanName().equalsIgnoreCase("cmdFreeSensor")){
				FingerprintSensorHandle.FreeSensor();
				response.setResult("SfipHome="+com.tisson.sfip.util.SystemUtils.getSfipHome());
			}else if(msg.getBeanName().equalsIgnoreCase("getFingerprintSensorInfo")){
				response.setResult(FingerprintSensorHandle. getManufacturer());
			}else if(msg.getBeanName().equalsIgnoreCase("cmdCollection")){
				ret = FingerprintSensorHandle.cmdCollection();
				response.setResult(FingerprintSensorHandle.getManufacturer() +"," + FingerprintSensorHandle.getFingerprintArryCount());
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
			try {
				RpcContext.getContext().getResponse(HttpServletResponse.class).addHeader("Access-Control-Allow-Origin","*");
			} catch (Throwable t) {
				if(log.isTraceEnabled()){
					log.trace("",t);
				}
			}
			return response;
	}

    @OPTIONS
    @Path("helloWord")
	public HelloWordResponse helloWordOPTIONS(HelloWordRequest msg) {
	       if(log.isTraceEnabled()){
	    	   log.trace(msg.toString());
	       }
		return helloWord(msg);
	}

}
