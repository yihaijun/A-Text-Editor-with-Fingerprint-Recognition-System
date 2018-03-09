package com.tisson.fingerprint.FingerprintSensorTool.sfipappsample;


import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tisson.sfip.module.util.serviceapitest.LoadTestUseCasesArray;
import com.tisson.sfip.module.util.serviceapitest.SfipServiceResponseSample;
import com.tisson.sfip.module.util.serviceapitest.TestResult;
import com.tisson.sfip.module.util.serviceapitest.TestUseCases;

import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:spring/tisson-sfip-app-FingerprintSensorTool-spring-context.xml"})
public class HellWordBeanImplTest  implements ApplicationContextAware {
	protected ApplicationContext applicationContext;
	@Autowired
	protected LoadTestUseCasesArray loadTestUseCasesArray;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;

	}

	protected void test(String path) {
		TestUseCases testUseCases = null;
		TestResult testResult = null;
		java.util.ArrayList<TestUseCases> testUseCasesArry = null;
		try {
			loadTestUseCasesArray.load(path, applicationContext);
		} catch (Throwable e) {
			log.error("loadTestUseCasesArray.load("+path+") Exception",e);
			Assert.fail();
		}
		testUseCasesArry = loadTestUseCasesArray.getTestUseCasesArry();
		testUseCases = testUseCasesArry.get(0);
		testResult = testUseCases.execute(applicationContext);
		Assert.assertEquals(testResult.getResponseStatus(),
				SfipServiceResponseSample.SUCCE_COMPLETION);

	}

	@Test
	public void testHelloWord() {
		System.out.println("kkkkkk");
		try {
			test("src/test/resources/testUseCases/hellWordBeanSfip-helloWord-junit-TST0000000000.txt");
		} catch (Throwable e1) {
			e1.printStackTrace();
		}
		try {
			Thread.currentThread().sleep(1000L*1000L);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
		/**
     * Rigourous Test :-)
     */
	@Test
    public void testApp()
    {
    }
}
