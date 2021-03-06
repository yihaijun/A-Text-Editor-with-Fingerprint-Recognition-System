/**
 * 
 */
package com.tisson.fingerprint.FingerprintSensorTool;

import java.io.Serializable;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.tisson.sfip.api.message.request.HelloWordRequest;
import com.tisson.sfip.api.message.response.HelloWordResponse;

/**
 * @author yihaijun
 *
 */
@Path("tisson/FingerprintUtils")
@Consumes({"application/json;charset=UTF-8"})
@Produces({"application/json;charset=UTF-8"})
public interface FingerprintUtilsInf  extends Serializable{
    @POST
    @Path("call")
	public HelloWordResponse helloWord(HelloWordRequest msg);
    
    @OPTIONS
    @Path("call")
	public HelloWordResponse helloWordOPTIONS(HelloWordRequest msg);

    @GET
    @Path("call")
	public HelloWordResponse helloWordGET(HelloWordRequest msg);
}
