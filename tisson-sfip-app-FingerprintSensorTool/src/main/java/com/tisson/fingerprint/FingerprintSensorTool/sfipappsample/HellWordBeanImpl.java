/**
 * 
 */
package com.tisson.fingerprint.FingerprintSensorTool.sfipappsample;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.protocol.rest.support.ContentType;
import com.tisson.fingerprint.FingerprintSensorTool.FingerprintSensorHandle;
import com.tisson.sfip.api.message.request.HelloWordRequest;
import com.tisson.sfip.api.message.response.HelloWordResponse;
import com.tisson.sfip.module.util.SystemUtil;
import com.tisson.sfip.module.util.serviceapitest.SfipServiceResponseSample;

/**
 * @author yihaijun
 *
 */
@Slf4j
@Service
@Consumes({"application/json;charset=UTF-8"})
@Produces({"application/json;charset=UTF-8"})
public class HellWordBeanImpl implements HellWordBeanInterface {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1067315977501626921L;

	@POST
	@Path("helloWord")
	public HelloWordResponse helloWord(HelloWordRequest msg) {
		HelloWordResponse response = new HelloWordResponse();
		response.setResponseCode("TST00"+HelloWordResponse.KEY_STATUS_SUCCE+"000000");
		response.setResponseContent("OK");
		String caller = "";
		String called = "";
		try {
			caller = com.tisson.sfip.extensiblexml.customtag.CallOtherAppBeanContext
					.getCallOtherAppBeanContext().getCallerContext()
					.getContextName();
			called = com.tisson.sfip.extensiblexml.customtag.CallOtherAppBeanContext
					.getCallOtherAppBeanContext().getCalledContext()
					.getContextName();
		} catch (Exception e) {
		}
		response.setResult("[" +called+"] receive[" + msg.getMsg()
		                                    					+ "] from [" +caller+"].("
		                                    					+ Thread.currentThread().getContextClassLoader() + ":"+this.getClass().getName() + ")("+SystemUtil.getLocalHostAddress()+":"+Thread.currentThread().getName()+")");
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
