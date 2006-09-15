/* Copyright 2004 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.sql.tests.javax.sql;

import javax.sql.PooledConnection;

import junit.framework.TestCase;

/**
 * JUnit Testcase for the javax.sql.PooledConnection class
 * 
 */

public class PooledConnectionTest extends TestCase {

	public void testPooledConnection() {

		PooledConnection theConnection = new Impl_PooledConnection();

	} // end method testPooledConnection()

} // end class PooledConnectionTest
