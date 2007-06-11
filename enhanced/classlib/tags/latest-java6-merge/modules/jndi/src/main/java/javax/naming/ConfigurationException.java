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


package javax.naming;

/**
 * This is the <code>NamingException</code> used when there is a problem
 * encountered with the configuration.
 * <p>
 * Multithreaded access to an instance is only safe when client code locks the
 * object first.</p>
 * 
 */
public class ConfigurationException extends NamingException{

    /*
     * This constant is used during deserialization to check the J2SE version
     * which created the serialized object.
     */
	static final long serialVersionUID = -2535156726228855704L; // J2SE 1.4.2
	
    /**
     * Constructs a <code>ConfigurationException</code> instance 
     * with all data initialized to null.
     */
    public ConfigurationException() {
        super();
    }
    
    /**
     * Constructs a <code>ConfigurationException</code> instance 
     * with the specified message.
     * 
     * @param s The detail message for the exception. It may be null.
     */
    public ConfigurationException(String s){
    	super(s);
    }

}


