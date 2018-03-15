package com.tisson.fingerprint.FingerprintSensorTool.sfipappsample;

import java.io.Serializable;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.alibaba.dubbo.rpc.protocol.rest.support.ContentType;
import com.tisson.sfip.api.message.request.HelloWordRequest;
import com.tisson.sfip.api.message.response.HelloWordResponse;

@Path("sfip-management/HellWordBeanSfipDubboInterface")
@Consumes({"application/json;charset=UTF-8"})
@Produces({"application/json;charset=UTF-8"})
public interface HellWordBeanInterface extends Serializable{
    @POST
    @Path("helloWord")
	public HelloWordResponse helloWord(HelloWordRequest msg);
    
    @OPTIONS
    @Path("helloWord")
	public HelloWordResponse helloWordOPTIONS(HelloWordRequest msg);
}
