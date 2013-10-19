/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org;

/**
 * @author vlads
 *
 */
public class TestStaticInitializer implements Runnable {

	private static int count = 0;
	
	static {
		count = 1;
	}
	
	
	public void run() {
		
		if (count != 1) {
			throw new RuntimeException("Same class is used again " + count);
		}
		
		if (TestMain.verbose) {
			System.out.println("Static initializer OK");
		}
		
	}

}
