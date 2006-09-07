/* 
*  Copyright 2005 The Apache Software Foundation or its licensors, as applicable. 
* 
*  Licensed under the Apache License, Version 2.0 (the "License"); 
*  you may not use this file except in compliance with the License. 
*  You may obtain a copy of the License at 
* 
*    http://www.apache.org/licenses/LICENSE-2.0 
* 
*  Unless required by applicable law or agreed to in writing, software 
*  distributed under the License is distributed on an "AS IS" BASIS, 
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
*  See the License for the specific language governing permissions and 
*  limitations under the License. 
*/
package java.rmi;

/**
 * @ar.org.fitc.spec_ref
 * 
 */
public class ConnectIOException extends RemoteException {

    /**
     * 
     */
    private static final long serialVersionUID = -8087809532704668744L;

    /**
     * @ar.org.fitc.spec_ref
     * 
     */
    public ConnectIOException(String s) {
        super(s);
    }

    /**
     * @ar.org.fitc.spec_ref
     * 
     */
    public ConnectIOException(String s, Exception ex) {
        super(s, ex);
    }
}
