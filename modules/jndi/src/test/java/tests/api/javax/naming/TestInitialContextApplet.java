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

package tests.api.javax.naming;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import tests.api.javax.naming.mock.MockApplet;

import junit.framework.TestCase;

public class TestInitialContextApplet extends TestCase {
	public void testConstructor_applet() throws NamingException {
		// MockApplet applet = new MockApplet();
		// applet.setParameter(
		// "java.naming.factory.initial",
		// "dazzle.jndi.testing.spi.DazzleContextFactory");
		// applet.setParameter(
		// "java.naming.provider.url",
		// "java.naming.provider.url.applet");
		// applet.setParameter(
		// "java.naming.factory.control",
		// "java.naming.factory.control.applet");
		// applet.setParameter(
		// "java.naming.dns.url",
		// "java.naming.dns.url.applet");
		// applet.setParameter(
		// "java.naming.factory.object",
		// "java.naming.factory.object.applet");
		// applet.setParameter(
		// "java.naming.factory.state",
		// "java.naming.factory.state.applet");
		// applet.setParameter(
		// "java.naming.factory.url.pkgs",
		// "java.naming.factory.url.pkgs.applet");
		//
		// Hashtable env = new Hashtable();
		// env.put(Context.APPLET, applet);
		//        
		// InitialContext context = new InitialContext(env);
		// Hashtable props = context.getEnvironment();
		// Hashtable expected = (Hashtable) applet.getAllParams().clone();
		// expected.put(Context.APPLET, applet);
		// assertEquals(expected, props);
	}
}
