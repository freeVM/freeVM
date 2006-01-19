/*
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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
* @author Boris V. Kuznetsov
* @version $Revision$
*/

package java.security.serialization;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignedObject;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;

import org.apache.harmony.security.TestKeyPair;

import com.openintel.drl.security.test.SerializationTest;

/**
 * Tests for SignedObject serialization
 * 
 */
public class SignedObjectTest extends SerializationTest {

	private Signature sig;
	private TestKeyPair tkp = null;
    private Properties prop;
    
    protected Object[] getData() {
    	try {
        	sig = Signature.getInstance("SHA1withDSA");		
    	} catch (NoSuchAlgorithmException e) {
    		fail(e.toString());
    	}
    	try {
			tkp = new TestKeyPair("DSA");
		} catch (NoSuchAlgorithmException e1) {
			fail(e1.toString());
		}
    	prop = new Properties();
    	prop.put("aaa", "bbb");
    	Object o = null;
    	try {
    		o = new SignedObject(prop, tkp.getPrivate(), sig);
    	} catch (IOException e) {
           	fail(e.toString());  
    	} catch (SignatureException e) {   
           	fail(e.toString());  
    	} catch (InvalidKeyException e) {
           	fail(e.toString());  
    	} catch (InvalidKeySpecException e) {
          	fail(e.toString());
		}
       return new Object[] { o };
    }

    protected void assertDeserialized(Object oref, Object otest) {
    	SignedObject ref = (SignedObject) oref;
    	SignedObject test = (SignedObject) otest;

    	assertEquals(test.getAlgorithm(), ref.getAlgorithm());
 
        try {
            assertEquals((Properties)test.getObject(), prop);      	
        } catch (ClassNotFoundException e) {
           	fail(e.toString());  
        } catch (IOException e) {
           	fail(e.toString());  
        }
        try {
        	if (!test.verify(tkp.getPublic(), sig)) {
            	fail("verify() failed");
            }	
        } catch (SignatureException e) {
        	fail(e.toString());      	
        } catch (InvalidKeyException e) {
           	fail(e.toString());         	
        } catch (InvalidKeySpecException e) {
           	fail(e.toString()); 
		}                     
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(DigestExceptionTest.class);
    }
}
