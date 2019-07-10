package com.codingapi.txlcn.tm;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationTest extends BaseTest {

	Logger logger = LoggerFactory.getLogger(ApplicationTest.class);
	
	@Test
	public void test() {
		synchronized (this) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
	}
}

