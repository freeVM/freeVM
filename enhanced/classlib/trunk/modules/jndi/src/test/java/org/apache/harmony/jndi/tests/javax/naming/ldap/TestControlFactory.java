/* 
 *  Licensed to the Apache Software Foundation (ASF) under one or more 
 *  contributor license agreements.  See the NOTICE file distributed with 
 *  this work for additional information regarding copyright ownership. 
 *  The ASF licenses this file to You under the Apache License, Version 2.0 
 *  (the "License"); you may not use this file except in compliance with 
 *  the License.  You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License. 
 */ 
/**
 * @author Hugo Beilis
 * @author Leonardo Soler
 * @author Gabriel Miretti
 * @version 1.0
 */
package org.apache.harmony.jndi.tests.javax.naming.ldap;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.*;
import org.apache.harmony.jndi.tests.javax.naming.spi.mock.ldap.MockControl;
import junit.framework.TestCase;

/**
 * <p>This Test class is testing the class ControlFactory in the javax.naming.ldap package.</p>
 * <p>Here in the next tables we are gonna find all methods to be tested:</p>
 * <table>
 	<tbody><th>Method Summary:</th>
	<tr><TD>Return</TD><TD>Method</TD></tr>
	<tr>
		<td class="c0" id="c00"><input class="a0" size="30" name="sas9nt11" readonly="readonly" value="static Control" id="f00"></TD>
		<td class="c0" id="c10"><input class="a0" size="65" name="sas9nt21" readonly="readonly" value="getControlInstance(Control ctl, Context ctx, Hashtable env)" id="f10"></td>
		
	</tr>
	</tbody>
	</table>
 */
public class TestControlFactory extends TestCase {

	/**
	 * <p>This method is not implemted.</p>
	 * @param args Possible parameter to help us initiate all tests.
	 */
	public static void main(String[] args) {
	}


	/**
	 * <p>Constructor method of the test class.</p>
	 * <p>Here in this case we do not do anything else of initiate the inherited constructor.</p>
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * <p>Test method for 'javax.naming.ldap.ControlFactory.getControlInstance(Control, Context, Hashtable<?, ?>)'</p>
	 * <p>Here we are testing the static method of the class ControlFactory</p>
	 * <p>The expected result is null.</p> 
	 */
	public void testGetControlInstanceControlContextHashtableOfQQ001() {
		
		try {
			
			assertNull(ControlFactory.getControlInstance(null,null,null));
			
		} catch (NamingException e) {
			
		}
	}
	/**
	 * <p>Test method for 'javax.naming.ldap.ControlFactory.getControlInstance(Control, Context, Hashtable<?, ?>)'</p>
	 * <p>Here we are testing the static method of the class ControlFactory</p>
	 * <p>The expected result is the control sended.</p> 
	 */
	public void testGetControlInstanceControlContextHashtableOfQQ002() {
		
		try {
			MockControl cs =  new MockControl("c1", false, new byte[] { 1, 2, 3, 4 }); 
					
			assertEquals(cs,ControlFactory.getControlInstance(cs,null,null));
		} catch (NamingException e) {
			
		}
	}

	/**
	 * <p>Test method for 'javax.naming.ldap.ControlFactory.getControlInstance(Control, Context, Hashtable<?, ?>)'</p>
	 * <p>Here we are testing the static method of the class ControlFactory</p>
	 * <p>The expected result is the control sended.</p> 
	 */
	public void testGetControlInstanceControlContextHashtableOfQQ003() {
		
		try {
			Hashtable env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY,"org.apache.harmony.jndi.tests.javax.naming.spi.mock.ldap.MockContextFactory");
			MockControl[] cs = { new MockControl("c1", false, new byte[] { 1, 2, 3, 4 }), 
					new MockControl("c1", true, new byte[] { 'a', 'b', 'c', 'd' }), };
			MockControl cs2 =  new MockControl("c1", false, new byte[] { 1, 2, 3, 4 }); 
			InitialLdapContext ilc=new InitialLdapContext(env, cs);
			assertEquals(cs2,ControlFactory.getControlInstance(cs2,ilc,env));
		} catch (NamingException e) {
			
		}
	}
	
	/**
	 * <p>Test method for 'javax.naming.ldap.ControlFactory.getControlInstance(Control, Context, Hashtable<?, ?>)'</p>
	 * <p>Here we are testing the static method of the class ControlFactory</p>
	 * <p>The expected result is the control sended.</p> 
	 */
	public void testGetControlInstanceControlContextHashtableOfQQ004() {
		
		try {
			Hashtable env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY,"org.apache.harmony.jndi.tests.javax.naming.spi.mock.ldap.MockContextFactory");
			env.put(LdapContext.CONTROL_FACTORIES,"org.apache.harmony.jndi.tests.javax.naming.spi.mock.ldap.MockControlFactory");
			MockControl[] cs = { new MockControl("c1", false, new byte[] { 1, 2, 3, 4 }),new MockControl("c1", true, new byte[] { 'a', 'b', 'c', 'd' }), };
			MockControl cs2 =  new MockControl("c1", false, new byte[] { 1, 2, 3, 4 });
			InitialLdapContext ilc=new InitialLdapContext(env, cs);
			
			assertEquals(cs2,ControlFactory.getControlInstance(cs2,ilc,env));
		} catch (NamingException e) {
			
		}
	}
	
}
