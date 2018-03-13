/**
 * 
 */
package com.tisson.fingerprint.FingerprintSensorTool.sfipappsample;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

/**
 * @author yihaijun
 * 
 */
public class RestControllFilter implements ContainerRequestFilter,
		ContainerResponseFilter {

	public void filter(ContainerRequestContext requestContext)
			throws IOException {
//		System.out.println("Request filter invoked");
	}

	public void filter(ContainerRequestContext containerRequestContext,
			ContainerResponseContext containerResponseContext)
			throws IOException {
		containerResponseContext.getHeaders().add(
				"Access-Control-Allow-Origin", "*");
		//		System.out.println("Response filter invoked");
//		if (containerRequestContext.getMethod().equals("OPTIONS")) {
//			containerResponseContext.getHeaders().add(
//					"Access-Control-Allow-Origin", "*");
//			containerResponseContext
//					.getHeaders()
//					.add("Access-Control-Allow-Headers",
//							"Content-Type,x-requested-with,Authorization,Access-Control-Allow-Origin");
//			containerResponseContext.getHeaders().add(
//					"Access-Control-Allow-Methods", "POST, GET, OPTIONS");
//			containerResponseContext.getHeaders().add("Access-Control-Max-Age",
//					"360");
//		}
	}
}
