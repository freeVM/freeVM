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
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package javax.security.sasl;

import com.openintel.drl.security.test.PerformanceTest;

import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.util.Map;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.harmony.security.SpiEngUtils;

/**
 * Test for Sasl class
 * 
 */

public class SaslTest4 extends PerformanceTest {
    private static final String SRVSSRV = "SaslServerFactory.";

    private static final String fServerClass = "javax.security.sasl.mySaslServerFactory";

    private Provider [] provs;
    private boolean initProvs = false;

    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(SaslTest4.class);
    }

    /**
     * Constructor for SaslTest2.
     * 
     * @param arg0
     */
    public SaslTest4(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        super.setUp();
        if (!initProvs) {
            provs = Security.getProviders();
            initProvs = true;
        }
        if (provs != null) {
            for (int i = 0; i < provs.length; i++) {
                Security.removeProvider(provs[i].getName());
            }
        }
    }

    protected Provider[] mProv;

    private void addProviders() {
        for (int i = 0; i < mProv.length; i++) {
            Security.insertProviderAt(mProv[i], 1);
        }
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        if (mProv != null) {
            for (int i = 0; i < mProv.length; i++) {
                Security.removeProvider(mProv[i].getName());
            }
        }
        if (provs != null) {
            for (int i = 0; i < provs.length; i++) {
                Security.insertProviderAt(provs[i], (i+1));
            }
        }    }

    /**
     * Test for <code>createSaslServer(String mechanism, 
     *      String protocol, String serverName,
     *      Map prop, CallbackHandler cbh))</code> method 
     * Assertions:
     * throws NullPointerException when mechanism is null;
     * throws SaslException when parameters (protocol, cbh,
     * mechanism) are wrong.
     * 
     * All providers are previously removed and 
     * 2 new providers were added.
     */
    public void testCreateServer01() throws SaslException {
        mProv = new Provider[] {
                (new SpiEngUtils()).new MyProvider("MySaslServerProvider1",
                        "Testing provider SaslServerFactory - 1", SRVSSRV
                                .concat("MECH-1"), fServerClass),
                (new SpiEngUtils()).new MyProvider("MySaslServerProvider2",
                        "Testing provider SaslServerFactory - 2", SRVSSRV
                                .concat("MECH-2"), fServerClass) };
        addProviders();

        CallbackHandler cbH = new cbHand();
        try {
            Sasl.createSaslServer(null, null, null, null, cbH);
            fail("NullPointerException should be thrown when mechanisms is null");
        } catch (NullPointerException e) {
        }
        try {
            Sasl.createSaslServer("MECH-2", "protocol", null, null, cbH);
            fail("SaslException should be thrown when CallbackHandler is wrong");
        } catch (SaslException e) {
        }
        cbH = new cbHandN();
        try {
            Sasl.createSaslServer("MECH-1", "protocol", null, null, cbH);
            fail("SaslException should be thrown when mechamisms is wrong");
        } catch (SaslException e) {
        }
        try {
            Sasl.createSaslServer("MECH-2", null, null, null, cbH);
            fail("SaslException should be thrown when protocol is null");
        } catch (SaslException e) {
        }
    }

    /**
     * Test for <code>createSaslServer(String mechanism, 
     *      String protocol, String serverName,
     *      Map prop, CallbackHandler cbh))</code>
     * method Assertions: throws NullPointerException when mechanisms is null;
     * returns null SaslServer.
     * 
     * All providers are previously removed.
     */
    public void testCreateServer02() throws SaslException {
        try {
            Sasl.createSaslServer(null, null, null, null, null);
            fail("NullPointerException should be thrown when mechanisms is null");
        } catch (NullPointerException e) {
        }
        assertNull("Not null result", Sasl.createSaslServer("MECH-999", null,
                null, null, null));
    }
    
    /**
     * Test for <code>createSaslServer(String mechanism, 
     *      String protocol, String serverName,
     *      Map prop, CallbackHandler cbh))</code> method
     *  
     * Assertions: 
     * returns SaslServer;
     * throws SaslServer for MECH-1 mechanism
     * 
     * All providers are previously removed and 
     * 2 new providers were added.
     */
    public void testCreateServer03() throws SaslException {
        mProv = new Provider[] {
                (new SpiEngUtils()).new MyProvider("MySaslServerProvider1",
                        "Testing provider SaslServerFactory - 1", SRVSSRV
                                .concat("MECH-1"), fServerClass),
                (new SpiEngUtils()).new MyProvider("MySaslServerProvider2",
                        "Testing provider SaslServerFactory - 2", SRVSSRV
                                .concat("MECH-2"), fServerClass) };
        addProviders();

        CallbackHandler cbH = new cbHandN();
        SaslServer saslS = Sasl.createSaslServer("MECH-2", "protocol", null,
                null, cbH);
        assertNotNull("Null result", saslS);
        assertTrue("Not SaslServer", saslS instanceof SaslServer);
        try {
            saslS.unwrap(null, 1, 1);
            fail("SaslException sould be thrown");
        } catch (SaslException e) {
        }
        assertFalse("Incorrect isComplete() result", saslS.isComplete());
        // try to create Server for wrong mechanism
        try {
            saslS = Sasl
                    .createSaslServer("MECH-1", "protocol", null, null, cbH);
            fail("SaslException sould be thrown");
        } catch (SaslException e) {
        }
    }
    
    /**
     * Test for <code>createSaslServer(String mechanism, 
     *      String protocol, String serverName,
     *      Map prop, CallbackHandler cbh))</code> method
     *  
     * Assertions: 
     * returns SaslServer;
     * throws SaslServer for MECH-1 mechanism
     * 
     * All providers are previously removed and 
     * 1 new provider was added.
     */
    public void testCreateServer04() throws SaslException {
        mProv = new Provider[] { (new SpiEngUtils()).new MyProvider(
                "MySaslServerProvider1",
                "Testing provider SaslServerFactory - 1", SRVSSRV
                        .concat("MECH-1"), fServerClass) };
        mProv[0].put(SRVSSRV.concat("MECH-2"), fServerClass);
        addProviders();
        CallbackHandler cbH = new cbHandN();
        SaslServer saslS = Sasl.createSaslServer("MECH-2", "protocol", null,
                null, cbH);
        assertNotNull("Null result for MECH-2", saslS);
        assertTrue("Not SaslServer", saslS instanceof SaslServer);
        assertFalse("Incorrect isComplete() result", saslS.isComplete());
        // try to create Server for wrong mechanism
        try {
            saslS = Sasl
                    .createSaslServer("MECH-1", "protocol", null, null, cbH);
            fail("SaslException sould be thrown");
        } catch (SaslException e) {
        }
    }

    /**
     * Test for <code>createSaslServer(String mechanism, 
     *      String protocol, String serverName,
     *      Map prop, CallbackHandler cbh))</code> method
     *  
     * Assertions: 
     * return null Server when there is no provider supported some mechanism
     * returns SaslServer when incorrect mechanism is used
     * 
     * All providers are previously removed and 
     * 2 new providers were added.
     */
    public void testCreateServer05() throws SaslException {
        mProv = new Provider[] {
                (new SpiEngUtils()).new MyProvider("MySaslServerProvider1",
                        "Testing provider SaslServerFactory - 1", SRVSSRV
                                .concat("MECH-2"), fServerClass.concat("Ext")),
                (new SpiEngUtils()).new MyProvider("MySaslServerProvider2",
                        "Testing provider SaslServerFactory - 2", SRVSSRV
                                .concat("MECH-1"), fServerClass),
                (new SpiEngUtils()).new MyProvider("MySaslServerProvider3",
                        "Testing provider SaslServerFactory - 3", SRVSSRV
                                .concat("MECH-6"), fServerClass) };
        mProv[2].put(SRVSSRV.concat("MECH-5"), fServerClass);
        addProviders();

        CallbackHandler cbH = new cbHandN();

        SaslServer saslS;
        // try to create SaslServer for wrong mechanism
        // there is no provider supported MECH-77, MECH-66 mechanisms

        assertNull("Not null object was created for wrong mechanism", Sasl
                .createSaslServer("MECH-77", "protocol", null, null, cbH));

        saslS = Sasl.createSaslServer("MECH-2", "protocol", null, null, cbH);
        assertNotNull("Null result for MECH-2", saslS);
        assertTrue("Incorrect object", saslS instanceof SaslServer);
        try {
            saslS.unwrap(null, 1, 1);
            fail("SaslException sould be thrown");
        } catch (SaslException e) {
        }
        assertFalse("Incorrect isComplete() result", saslS.isComplete());
        // MECH-1 was defined in some provider but it is supported in another
        // provider
        try {
            Sasl.createSaslServer("MECH-1", "protocol", null, null, cbH);
            fail("SaslException sould be thrown");
        } catch (SaslException e) {
        }
        // MECH-6 and MECH-5 were defined in one provider but they are supported
        // in another provider
        saslS = Sasl.createSaslServer("MECH-6", "protocol", null, null, cbH);
        assertNotNull("Null result for MECH-6", saslS);
        assertTrue("Incorrect object", saslS instanceof SaslServer);
        saslS = Sasl.createSaslServer("MECH-5", "protocol", null, null, cbH);
        assertNotNull("Null result for MECH-5", saslS);
        assertTrue("Incorrect object", saslS instanceof SaslServer);
    }
}

/*
 * Additional class for creating SaslServer object
 */

class mySaslServerFactory implements SaslServerFactory {
    public mySaslServerFactory() {
        super();
    }

    public String[] getMechanismNames(Map prop) {
        return new String[] { "MECH-1", "MECH-2", "MECH-3", "MECH-4" };
    }

    public SaslServer createSaslServer(String mech, String protocol,
            String srvName, Map prop, CallbackHandler hnd) throws SaslException {
        if (mech == null) {
            throw new SaslException();
        }
        if ("MECH-1".equals(mech)) {
            throw new SaslException("Incorrect mechanisms");
        }
        if (protocol == null) {
            throw new SaslException("Protocol is null");
        }
        TextOutputCallback[] cb = { new TextOutputCallback(
                TextOutputCallback.INFORMATION, "Information") };
        try {
            hnd.handle(cb);
        } catch (UnsupportedCallbackException e) {
            throw new SaslException("Incorrect callback handlere", e);
        } catch (IOException e) {
            throw new SaslException("Incorrect callback handlere", e);
        }
        return new mySaslServer();
    }

    public class mySaslServer implements SaslServer {
        public mySaslServer() {
            super();
        }

        public void dispose() throws SaslException {
        }

        public byte[] evaluateResponse(byte[] challenge) throws SaslException {
            return new byte[0];
        }

        public String getMechanismName() {
            return "Server Proba";
        }

        public Object getNegotiatedProperty(String s) {
            return "";
        }

        public String getAuthorizationID() {
            return "";
        }

        public boolean isComplete() {
            return false;
        }

        public byte[] unwrap(byte[] incoming, int offset, int len)
                throws SaslException {
            throw new SaslException();
        }

        public byte[] wrap(byte[] outgoing, int offset, int len)
                throws SaslException {
            return new byte[0];
        }
    }
}

class mySaslServerFactoryExt extends mySaslServerFactory {
    public String[] getMechanismNames(Map prop) {
        return new String[] { "MECH-5", "MECH-6" };
    }

    public SaslServer createSaslServer(String mech, String protocol,
            String srvName, Map prop, CallbackHandler hnd) throws SaslException {
        if (mech == null) {
            throw new SaslException();
        }
        return new mySaslServer();
    }
}