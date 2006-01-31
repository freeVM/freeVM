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

package java.security;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.harmony.security.SpiEngUtils;
import junit.framework.TestCase;
import org.apache.harmony.security.test.TestUtils;


/**
 * Tests for <code>Provider</code> constructor and methods
 * 
 */
public class ProviderTest extends TestCase {

    Provider p;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        p = new MyProvider();
    }
    
    /*
     * Class under test for void Provider()
     */
    public final void testProvider() {
        if (!p.getProperty("Provider.id name").equals(String.valueOf(p.getName()))) {
            fail("Incorrect \"Provider.id name\" value");    
        }
        if (!p.getProperty("Provider.id version").equals(String.valueOf(p.getVersion()))) {
            fail("Incorrect \"Provider.id version\" value");    
        }
        if (!p.getProperty("Provider.id info").equals(String.valueOf(p.getInfo()))) {
            fail("Incorrect \"Provider.id info\" value");    
        }
        if (!p.getProperty("Provider.id className").equals(p.getClass().getName())) {
            fail("Incorrect \"Provider.id className\" value");    
        }
    }

    public final void testClear() {
        p.clear();
        if (p.getProperty("MessageDigest.SHA-1") != null) {
            fail("Provider contains properties");
        }
    }

    /*
     * Class under test for void Provider(String, double, String)
     */
    public final void testProviderStringdoubleString() {
        Provider p = new MyProvider("Provider name", 123.456, "Provider info");
        if (!p.getName().equals("Provider name") ||
            p.getVersion() != 123.456 ||
            !p.getInfo().equals("Provider info")) {
            fail("Incorrect values");
        }
    }

    public final void testGetName() {
        if (!p.getName().equals("MyProvider")) {
            fail("Incorrect provider name");
        }
    }

    public final void testGetVersion() {
        if (p.getVersion() != 1.0) {
            fail("Incorrect provider version");
        }
    }

    public final void testGetInfo() {
        if (!p.getInfo().equals("Provider for testing")) {
            fail("Incorrect provider info");
        }
    }

    /*
     * Class under test for String toString()
     */
    public final void testToString() {
        if (!"MyProvider provider, Ver. 1.0 Provider for testing".equals(p.toString())) {
            fail("Incorrect provider.toString()");
        }
    }

    /*
     * Class under test for void load(InputStream)
     */
    public final void testLoadInputStream() {
        FileInputStream fis = null;
        String fileName = SpiEngUtils.getFileName(TestUtils.TEST_ROOT,
                "java/security/Provider.prop.dat");
        try {
            fis = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            fail(e.toString());
        }
        try {
            p.load(fis);    
        } catch (IOException e) {
            fail(e.toString());
        }

        if (!"value 1".equals(p.getProperty("Property 1").trim()) ||
                !"className".equals(p.getProperty("serviceName.algName").trim()) ||    
                !"attrValue".equals(p.getProperty("serviceName.algName attrName").trim()) ||
                !"stanbdardName".equals(p.getProperty("Alg.Alias.engineClassName.aliasName").trim()) ||
                !String.valueOf(p.getName()).equals(p.getProperty("Provider.id name").trim()) ||
                !String.valueOf(p.getVersion()).equals(p.getProperty("Provider.id version").trim()) ||
                !String.valueOf(p.getInfo()).equals(p.getProperty("Provider.id info").trim()) ||
                !p.getClass().getName().equals(p.getProperty("Provider.id className").trim()) ||
                !"SomeClassName".equals(p.getProperty("MessageDigest.SHA-1").trim()) ) {
            fail("Incorrect property value");
        }
    }

    /*
     * Class under test for void putAll(Map)
     */
    public final void testPutAllMap() {
        HashMap hm = new HashMap();
        hm.put("MessageDigest.SHA-1", "aaa.bbb.ccc.ddd");
        hm.put("Property 1", "value 1");
        hm.put("serviceName.algName attrName", "attrValue");
        hm.put("Alg.Alias.engineClassName.aliasName", "stanbdardName");
        p.putAll(hm);
        if (!"value 1".equals(p.getProperty("Property 1").trim()) ||
                !"attrValue".equals(p.getProperty("serviceName.algName attrName").trim()) ||
                !"stanbdardName".equals(p.getProperty("Alg.Alias.engineClassName.aliasName").trim()) ||
                !"aaa.bbb.ccc.ddd".equals(p.getProperty("MessageDigest.SHA-1").trim()) ) {
            fail("Incorrect property value");
        }
    }

    /*
     * Class under test for Set entrySet()
     */
    public final void testEntrySet() {
        p.put("MessageDigest.SHA-256", "aaa.bbb.ccc.ddd");
        
        Set s = p.entrySet();
        try {
            s.clear();
        } catch (UnsupportedOperationException e) {
        }
        Set s1 = p.entrySet();
        if ((s == s1) || s1.isEmpty() ) {
            fail("Must return unmodifiable set");
        }
        if (s1.size() != 8) {    
            fail("Incorrect set size");
        }
        for (Iterator it = s1.iterator(); it.hasNext();) {
            Entry e = (Entry)it.next();
            String key = (String)e.getKey();
            String val = (String)e.getValue();
            if (key.equals("MessageDigest.SHA-1") && val.equals("SomeClassName")) {
                continue;
            }
            if (key.equals("Alg.Alias.MessageDigest.SHA1") && val.equals("SHA-1")) {
                continue;
            }
            if (key.equals("MessageDigest.abc") && val.equals("SomeClassName")) {
                continue;
            }
            if (key.equals("Provider.id className") && val.equals(p.getClass().getName())) {
                continue;
            }
            if (key.equals("Provider.id name") && val.equals("MyProvider")) {
                continue;
            }
            if (key.equals("MessageDigest.SHA-256") && val.equals("aaa.bbb.ccc.ddd")) {
                continue;
            }
            if (key.equals("Provider.id version") && val.equals("1.0")) {
                continue;
            }
            if (key.equals("Provider.id info") && val.equals("Provider for testing")) {
                continue;
            }
            fail("Incorrect set");
        }        
    }

    /*
     * Class under test for Set keySet()
     */
    public final void testKeySet() {
        p.put("MessageDigest.SHA-256", "aaa.bbb.ccc.ddd");
        
        Set s = p.keySet();
        try {
            s.clear();
        } catch (UnsupportedOperationException e) {
        }
        Set s1 = p.keySet();
        if ((s == s1) || s1.isEmpty() ) {
            fail("Must return unmodifiable set");
        }
        if (s1.size() != 8) {    
            fail("Incorrect set size");
        }
        if (!s1.contains("MessageDigest.SHA-256") ||
                !s1.contains("MessageDigest.SHA-1") ||
                !s1.contains("Alg.Alias.MessageDigest.SHA1") ||
                !s1.contains("MessageDigest.abc") ||
                !s1.contains("Provider.id info") ||
                !s1.contains("Provider.id className") ||
                !s1.contains("Provider.id version") ||
                !s1.contains("Provider.id name")) {
            fail("Incorrect set");
        }
    }

    /*
     * Class under test for Collection values()
     */
    public final void testValues() {
        p.put("MessageDigest.SHA-256", "aaa.bbb.ccc.ddd");
        
        Collection c = p.values();
        try {
            c.clear();
        } catch (UnsupportedOperationException e) {
        }
        Collection c1 = p.values();
        if ((c == c1) || c1.isEmpty() ) {
            fail("Must return unmodifiable set");
        }
        if (c1.size() != 8) {    
            fail("Incorrect set size " + c1.size());
        }    
        if (!c1.contains("MyProvider") ||
                !c1.contains("aaa.bbb.ccc.ddd") ||
                !c1.contains("Provider for testing") ||
                !c1.contains("1.0") ||
                !c1.contains("SomeClassName") ||
                !c1.contains("SHA-1") ||
                !c1.contains(p.getClass().getName())) {
            fail("Incorrect set");
        }
    }

    /*
     * Class under test for Object put(Object, Object)
     */
    public final void testPutObjectObject() {
        p.put("MessageDigest.SHA-1", "aaa.bbb.ccc.ddd");
        p.put("Type.Algorithm", "className");
        if (!"aaa.bbb.ccc.ddd".equals(p.getProperty("MessageDigest.SHA-1").trim()) ) {
            fail("Incorrect property value");
        }
        
        Set services = p.getServices();
        if (services.size() != 3) {
            fail("incorrect size");
        }
        for (Iterator it = services.iterator(); it.hasNext();) {
            Provider.Service s = (Provider.Service)it.next();
            if ("Type".equals(s.getType()) &&
                    "Algorithm".equals(s.getAlgorithm()) &&
                    "className".equals(s.getClassName())) {
                continue;
            }
            if ("MessageDigest".equals(s.getType()) &&
                    "SHA-1".equals(s.getAlgorithm()) &&
                    "aaa.bbb.ccc.ddd".equals(s.getClassName())) {
                continue;
            }
            if ("MessageDigest".equals(s.getType()) &&
                    "abc".equals(s.getAlgorithm()) &&
                    "SomeClassName".equals(s.getClassName())) {
                continue;
            }
            fail("Incorrect service");
        }
    }

    /*
     * Class under test for Object remove(Object)
     */
    public final void testRemoveObject() {
        Object o = p.remove("MessageDigest.SHA-1");
        if (!"SomeClassName".equals(o)) {
            fail("Incorrect return value");
        }
        if (p.getProperty("MessageDigest.SHA-1") != null) {
            fail("Provider contains properties");
        }
        if (p.getServices().size() != 1){
            fail("Service not removed");
        }
    }

    public final void testImplementsAlg() {
        HashMap hm = new HashMap();
        hm.put("KeySize", "1024");
        hm.put("AAA", "BBB");
        Provider.Service s = new Provider.Service(p, "Type", "Algorithm",
                "className", null, hm);
        p.putService(s);
        if (!p.implementsAlg("Type", "Algorithm", null, null) ||
                !p.implementsAlg("MessageDigest", "SHA-1", null, null)) {
            fail("Case 1. implementsAlg failed");
        }
        if (!p.implementsAlg("Type", "Algorithm", "KeySize", "512")) {
            fail("Case 2. implementsAlg failed");
        }
        if (p.implementsAlg("Type", "Algorithm", "KeySize", "1025")) {
            fail("Case 3. implementsAlg failed");
        }
        if (!p.implementsAlg("Type", "Algorithm", "AAA", "BBB")) {
            fail("Case 3. implementsAlg failed");
        }    
    }

    public final void testSetProviderNumber() {
        p.setProviderNumber(100);
        if (p.getProviderNumber() != 100) {
            fail("Incorrect ProviderNumber");        
        }
    }

    public final void testGetProviderNumber() {
        if (p.getProviderNumber() != -1) {
            fail("Case 1. Incorrect ProviderNumber");        
        }
        
        int i = Security.addProvider(p);
        if (p.getProviderNumber() != i) {
            fail("Case 2. Incorrect ProviderNumber");        
        }
        Security.removeProvider(p.getName());    // clean up
    }

    public final void testGetService() {
        try { 
            p.getService(null, "algorithm");
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try { 
            p.getService("type", null);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        
        Provider.Service s = new Provider.Service(p, "Type", "Algorithm",
                "className", null, null);
        p.putService(s);
        
        if (p.getService("Type", "AlgoRithM") != s) {
            fail("Case 1. getService() failed");
        }
        
        Provider.Service s1 = p.getService("MessageDigest", "AbC");
        if (s1 == null) {
            fail("Case 2. getService() failed");            
        }
        
        s = new Provider.Service(p, "MessageDigest", "SHA-1",
                "className", null, null);
        p.putService(s);
        if (s1 == p.getService("MessageDigest", "SHA-1")) {
            fail("Case 3. getService() failed");
        }
        
        if (p.getService("MessageDigest", "SHA1") == null) {
            fail("Case 4. getService() failed");
        }
    }

    public final void testGetServices() {
        Provider.Service s = new Provider.Service(p, "Type", "Algorithm",
                "className", null, null);

        // incomplete services should be removed
        p.put("serv.alg", "aaaaaaaaaaaaa");
        p.put("serv.alg KeySize", "11111");
        p.put("serv1.alg1 KeySize", "222222");
        p.remove("serv.alg");
        
        p.putService(s);
        Set services = p.getServices();
        if (services.size() != 3) {
            fail("incorrect size");
        }
        for (Iterator it = services.iterator(); it.hasNext();) {
            s = (Provider.Service)it.next();
            if ("Type".equals(s.getType()) &&
                    "Algorithm".equals(s.getAlgorithm()) &&
                    "className".equals(s.getClassName())) {
                continue;
            }
            if ("MessageDigest".equals(s.getType()) &&
                    "SHA-1".equals(s.getAlgorithm()) &&
                    "SomeClassName".equals(s.getClassName())) {
                continue;
            }
            if ("MessageDigest".equals(s.getType()) &&
                    "abc".equals(s.getAlgorithm()) &&
                    "SomeClassName".equals(s.getClassName())) {
                continue;
            }    
            fail("Incorrect service");
        }
    }

    public final void testPutService() {
        HashMap hm = new HashMap();
        hm.put("KeySize", "1024");
        hm.put("AAA", "BBB");
        Provider.Service s = new Provider.Service(p, "Type", "Algorithm",
                "className", null, hm);
        p.putService(s);
        if (s != p.getService("Type", "Algorithm")){
            fail("putService failed");
        }
        if (!"className".equals(p.getProperty("Type.Algorithm"))) {
            fail("incorrect className");
        }
        if (!"1024".equals(p.getProperty("Type.Algorithm KeySize"))) {
            fail("incorrect attribute");
        }    
    }

    public final void testRemoveService() {
        Provider.Service s = new Provider.Service(p, "Type", "Algorithm",
                "className", null, null);
        p.putService(s);
        p.removeService(s);
        Set services = p.getServices();
        if (services.size() != 2) {
            fail("incorrect size");
        }
        
        for (Iterator it = services.iterator(); it.hasNext();) {
            s = (Provider.Service)it.next();
            if ("MessageDigest".equals(s.getType()) &&
                    "SHA-1".equals(s.getAlgorithm()) &&
                    "SomeClassName".equals(s.getClassName())) {
                continue;
            }
            if ("MessageDigest".equals(s.getType()) &&
                    "abc".equals(s.getAlgorithm()) &&
                    "SomeClassName".equals(s.getClassName())) {
                continue;
            }
            fail("Incorrect service");
        }
        
        if (p.getProperty("Type.Algorithm") != null) {
            fail("incorrect property");
        }    
    }
    
    public final void testService1() {
        p.put("MessageDigest.SHA-1", "AnotherClassName");
        Provider.Service s = p.getService("MessageDigest", "SHA-1");
        if (!"AnotherClassName".equals(s.getClassName())) {
            fail("Incorrect class name "+ s.getClassName());
        }
    }
    
    public final void testService2() {
        Provider[] pp = Security.getProviders("MessageDigest.SHA-1");
        if (pp == null) {
            return;
        }
        p = pp[0];
        p.put("MessageDigest.SHA-1", "AnotherClassName");
        Provider.Service s = p.getService("MessageDigest", "SHA-1");
        if (!"AnotherClassName".equals(s.getClassName())) {
            fail("Incorrect class name "+ s.getClassName());
        }
        try {
            s.newInstance(null);
            fail("No expected NoSuchAlgorithmException");
        } catch (NoSuchAlgorithmException e) {    
        }
    }

    class MyProvider extends Provider {
        MyProvider() {
            super("MyProvider", 1.0, "Provider for testing");
            put("MessageDigest.SHA-1", "SomeClassName");
            put("MessageDigest.abc", "SomeClassName");
            put("Alg.Alias.MessageDigest.SHA1", "SHA-1");
        }
        
        MyProvider(String name, double version, String info) {
            super(name, version, info);
        }
    }
}
