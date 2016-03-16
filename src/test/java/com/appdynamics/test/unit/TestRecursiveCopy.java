package com.appdynamics.test.unit;

import org.testng.annotations.Test;

import com.appdynamics.utils.FileUtils;

public class TestRecursiveCopy {

	@Test
	public void testRecursiveJarCopy() throws Exception {
		FileUtils utils = new FileUtils();
		
		String resourceName = "./target/test-classes/bootstrap-admin-template";

		utils.recursivelyCopy(resourceName, null);
	}

}
