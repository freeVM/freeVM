/*
 * Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable
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
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Alexei Y. Zakharov
 * @version $Revision: 1.1.2.4 $
 */

package org.apache.harmony.jndi.provider.dns;


/**
 * Represents domain protocol Question Record
 * 
 * @author Alexei Zakharov
 * @version $Revision: 1.1.2.4 $
 * @see RFC 1035
 */
public class QuestionRecord {

    /** domain name */
    private String qName;

    /** type of query */
    private int qType;

    /** class of query */
    private int qClass;
    
    /** Empty constructor */
    public QuestionRecord() {}

    /**
     * Constructs new question record with given parameters
     * 
     * @param qName domain name
     * @param qType question record type
     * @param qClass question record class
     */
    public QuestionRecord(
            String qName,
            int qType,
            int qClass)
    {
        this.qName = qName;
        this.qType = qType;
        this.qClass = qClass;
    }

   
    /**
     * Creates a sequence of bytes that represents the current question record.
     *  
     * @param buffer the buffer in which the result byte sequence will be
     *  written
     * @param startIdx the index in the buffer to start at
     * @return updated index of the buffer array
     */
    public int writeBytes(byte[] buffer, int startIdx)
        throws DomainProtocolException
    {
        int idx = startIdx;

        // basic checkings
        if (buffer == null) {
            throw new DomainProtocolException("buffer is null");
        }
        if (startIdx >= buffer.length || startIdx < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        // write the name
        idx = ProviderMgr.writeName(qName, buffer, idx);
        // QTYPE
        idx = ProviderMgr.write16Int(qType, buffer, idx);
        // QCLASS
        idx = ProviderMgr.write16Int(qClass, buffer, idx);
        return idx;
    }
    // getters and setters methods


    /**
     * Parses given sequence of bytes and constructs a question record from it.
     * 
     * @param mesBytes the byte array that should be parsed
     * @param startIdx an index of <code>mesBytes</code> array to start the
     *  parsing at
     * @param resultQR an object the result of the operation will be stored into
     * @return updated index of <code>mesBytes</code> array
     * @throws DomainProtocolException if something went wrong
     */
    public static int parseRecord(byte[] mesBytes, int startIdx,
            QuestionRecord resultQR) throws DomainProtocolException
    {
        int idx = startIdx;
        StringBuffer nameSB = new StringBuffer();

        if (resultQR == null) {
            throw new NullPointerException("Given resultQR is null");
        }
        // name
        idx = ProviderMgr.parseName(mesBytes, idx , nameSB);
        resultQR.setQName(nameSB.toString());
        // QTYPE
        resultQR.setQType(ProviderMgr.parse16Int(mesBytes, idx));
        idx += 2;
        // QCLASS
        resultQR.setQClass(ProviderMgr.parse16Int(mesBytes, idx));
        idx += 2;
        return idx;
    }
   
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String qClassStr, qTypeStr;

        if (qType > 0 && qType < 256) {
            qTypeStr = ProviderConstants.rrTypeNames[qType];
        } else {
            qTypeStr = String.valueOf(qType);
        }
        if (qClass > 0 && qClass < 256) {
            qClassStr = ProviderConstants.rrClassNames[qClass];
        } else {
            qClassStr = String.valueOf(qClass);
        }
        sb.append(qClassStr);
        sb.append(" ");
        sb.append(qTypeStr);
        sb.append(" ");
        sb.append(qName);
        return sb.toString();
    }

    /**
     * @return Returns the qClass.
     */
    public int getQClass() {
        return qClass;
    }
    /**
     * @param class1 The qClass to set.
     */
    public void setQClass(int class1) {
        qClass = class1;
    }
    /**
     * @return Returns the qName.
     */
    public String getQName() {
        return qName;
    }
    /**
     * @param name The qName to set.
     */
    public void setQName(String name) {
        qName = name;
    }
    /**
     * @return Returns the qType.
     */
    public int getQType() {
        return qType;
    }
    /**
     * @param type The qType to set.
     */
    public void setQType(int type) {
        qType = type;
    }
}
