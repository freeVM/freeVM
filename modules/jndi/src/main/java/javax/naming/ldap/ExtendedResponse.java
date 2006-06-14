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


package javax.naming.ldap;

import java.io.Serializable;

/**
 * See RFC2251 for the definition of an <code>ExtendedResponse</code>.
 * 
 * 
 */
public interface ExtendedResponse extends Serializable {

    /*
     * -------------------------------------------------------------------
     * Methods
     * -------------------------------------------------------------------
     */

    /**
     * Gets the object ID assigned to this response.
     * (see RFC2251)
     * 
     * @return          the object ID assigned to the response
     */
    String getID();

    /**
     * Gets the response encoded using ASN.1 Basic Encoding Rules (BER).
     * 
     * @return          the response encoded using ASN.1 BER
     */
    byte[] getEncodedValue();

}


