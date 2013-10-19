/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 *  This code is a bit modified source file taken from the AXIS2 repository.
 *  Origin can be found at 
 *   http://svn.apache.org/viewvc/webservices/axis2/trunk/java/modules/kernel/src/org/apache/axis2/transport/SimpleAxis2Server.java?revision=490434&view=markup
 */

package org.apache.axis2.transport;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.axis2.util.CommandLineOption;
import org.apache.axis2.util.CommandLineOptionParser;
import org.apache.axis2.util.OptionsValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SimpleAxis2Server {

    private static final Log log = LogFactory.getLog(SimpleHTTPServer.class);

    int port = -1;

    public static int DEFAULT_PORT = 8080;
    

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
		String repoLocation = null;
		String confLocation = null;

		CommandLineOptionParser optionsParser = new CommandLineOptionParser(args);
		List invalidOptionsList = optionsParser.getInvalidOptions(new OptionsValidator() {
			public boolean isInvalid(CommandLineOption option) {
				String optionType = option.getOptionType();
				return !("repo".equalsIgnoreCase(optionType) || "conf"
						.equalsIgnoreCase(optionType));
			}
		});
		
		if ((invalidOptionsList.size()>0)||(args.length>4))
		{
			printUsage();
			return;
		}
		
		Map optionsMap = optionsParser.getAllOptions();

		CommandLineOption repoOption = (CommandLineOption) optionsMap
				.get("repo");
		CommandLineOption confOption = (CommandLineOption) optionsMap
				.get("conf");

		log.info("[SimpleAxisServer] Starting");
		if (repoOption != null) {
			repoLocation = repoOption.getOptionValue();
			log.info("[SimpleAxisServer] Using the Axis2 Repository"
					+ new File(repoLocation).getAbsolutePath());
		}
		if (confOption != null) {
			confLocation = confOption.getOptionValue();
			System.out
					.println("[SimpleAxisServer] Using the Axis2 Configuration File"
							+ new File(confLocation).getAbsolutePath());
		}

		ListenerManager listenerManager = null;
		try {
			ConfigurationContext configctx = ConfigurationContextFactory
					.createConfigurationContextFromFileSystem(repoLocation,
							confLocation);
			listenerManager =  new ListenerManager();
			listenerManager.init(configctx);
			listenerManager.start();
			Thread.sleep(2000);
		} catch (Throwable t) {
            log.fatal("[SimpleAxisServer] Shutting down. Error starting SimpleAxisServer", t);
            System.exit(10);
        }
		log.info("[SimpleAxisServer] Started");

		boolean run = true;
		while (run) {
			Thread.sleep(2000);
			//final String sep = File.separator; confLocation +sep+ ".." +sep+ 
			File stop = new File("stopServer");
			if (stop.exists()) {
				log.info("[SimpleAxisServer] Shutting down...");
				listenerManager.stop();
				run = false;
			}
		}
    }
    
    public static void printUsage() {
        System.out.println("Usage: SimpleAxisServer -repo <repository>  -conf <axis2 configuration file>");
        System.out.println();
        System.exit(1);
    }
}
