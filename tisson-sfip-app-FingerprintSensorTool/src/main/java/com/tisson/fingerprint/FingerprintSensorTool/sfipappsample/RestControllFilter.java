/**
 * 
 */
package com.tisson.fingerprint.FingerprintSensorTool.sfipappsample;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yihaijun
 * 
 */
@Slf4j
@Priority(Priorities.USER)
public class RestControllFilter implements ContainerRequestFilter,
		ContainerResponseFilter {

	public void filter(ContainerRequestContext requestContext)
			throws IOException {
		// System.out.println("Request filter invoked");
	}

	public void filter(ContainerRequestContext containerRequestContext,
			ContainerResponseContext containerResponseContext)
			throws IOException {
//		if (log.isTraceEnabled()) {
//			log.trace("method=" + containerRequestContext.getMethod());
//		}
		if (containerRequestContext.getMethod().equals("OPTIONS")) {
			containerResponseContext.getHeaders().add(
					"Access-Control-Allow-Origin", "*");
			containerResponseContext
					.getHeaders()
					.add("Access-Control-Allow-Headers",
							"Content-Type,x-requested-with,Authorization,Access-Control-Allow-Origin");
			containerResponseContext.getHeaders().add(
					"Access-Control-Allow-Methods", "POST,GET,OPTIONS");
			containerResponseContext.getHeaders().add("Access-Control-Max-Age",
					"3600");
			containerResponseContext.getHeaders().add("Access-Control-Allow-Credentials",true);
			containerResponseContext.getHeaders().add("Access-Control-Request-Method", "POST,GET,OPTIONS");
			containerResponseContext.setStatus(200);
		}
	}
}
