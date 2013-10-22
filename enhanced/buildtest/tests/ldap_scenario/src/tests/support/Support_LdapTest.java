/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package tests.support;

import java.io.File;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import junit.framework.TestCase;

import org.apache.ldap.server.configuration.MutableServerStartupConfiguration;
import org.apache.ldap.server.configuration.ShutdownConfiguration;
import org.apache.ldap.server.jndi.ServerContextFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.mina.util.AvailablePortFinder;

/**
 * This class should be extended when test case need to communicate with ldap
 * server. We use Apache Directory Server as embeded ldap server, it will be
 * started before each test case and shutdown when the test case is done. We do
 * starting and shutdowning server at method <code>setUp()</code> and
 * <code>tearDown()</code> respectively, so if sub class override this two
 * methods, must call supper's implementations to start and shutdown server
 * properly.
 */
public class Support_LdapTest extends TestCase {

    static {
        System.setProperty("log4j.defaultInitOverride", "true");
        ConsoleAppender appender = new ConsoleAppender(new PatternLayout());
        // FIXME: use not deprecated method to initial log4j
        appender.setThreshold(Priority.DEBUG);
        BasicConfigurator.configure(appender);
    }

    /** the context root for the rootDSE */
    protected LdapContext rootDSE;

    /** flag whether to delete database files for each test or not */
    protected boolean isCleanWorkingDir = true;

    protected MutableServerStartupConfiguration configuration;

    /**
     * listening port of the server
     */
    protected int port;

    /**
     * all necessary environment properties to create a ldap server
     */
    protected Hashtable<String, Object> serverEnv = new Hashtable<String, Object>();

    private static String DEFAULT_PRINCIPAL = "uid=admin,ou=system";

    private static String DEFAULT_CREDENTIALS = "secret";

    private static String DEFAULT_AUTHENTICATION = "simple";

    protected String workingDir = "ldap-temp";

    protected void setUp() throws Exception {
        super.setUp();
        configuration = new MutableServerStartupConfiguration();
        configuration.setWorkingDirectory(new File(workingDir));
        cleanWorkingDir(configuration.getWorkingDirectory());
        port = AvailablePortFinder.getNextAvailable(1024);
        configuration.setLdapPort(port);
        // configuration.setShutdownHookEnabled(false);
        serverEnv = new Hashtable<String, Object>(configuration
                .toJndiEnvironment());

        initialAuth();

        serverEnv.put(Context.INITIAL_CONTEXT_FACTORY,
                ServerContextFactory.class.getName());

        serverEnv.put(Context.PROVIDER_URL, "");
        rootDSE = new InitialLdapContext(serverEnv, null);

    }

    /**
     * If subclass want to different authentication setting for Ldap server,
     * should override this method, and set <code>serverEnt</code>.
     * 
     */
    protected void initialAuth() {
        serverEnv.put(Context.SECURITY_PRINCIPAL, DEFAULT_PRINCIPAL);
        serverEnv.put(Context.SECURITY_CREDENTIALS, DEFAULT_CREDENTIALS);
        serverEnv.put(Context.SECURITY_AUTHENTICATION, DEFAULT_AUTHENTICATION);

    }

    protected void tearDown() throws Exception {
        super.tearDown();
        serverEnv.putAll(new ShutdownConfiguration().toJndiEnvironment());
        new InitialContext(serverEnv);

        cleanWorkingDir(configuration.getWorkingDirectory());
        configuration = new MutableServerStartupConfiguration();
        serverEnv.clear();
    }

    private void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                deleteFile(f);
            }
            file.delete();
        } else {
            file.delete();
        }
    }

    public void test_misc() {
	// Do nothing
    }

    /**
     * delete the whole working directory for Ldap server
     * 
     * @param workingDirectory
     *            directory stored data for Ldap server
     */
    private void cleanWorkingDir(File workingDirectory) {
        if (!isCleanWorkingDir || !workingDirectory.exists()
                || !workingDirectory.isDirectory()) {
            return;
        }

        File[] files = workingDirectory.listFiles();
        for (File file : files) {
            deleteFile(file);
        }
    }
}
