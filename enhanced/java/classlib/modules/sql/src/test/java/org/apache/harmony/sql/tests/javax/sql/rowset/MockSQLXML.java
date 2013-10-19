/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.sql.tests.javax.sql.rowset;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.SQLXML;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

public class MockSQLXML implements SQLXML {
    private String str = "sqlXML";

    public void free() throws SQLException {
        str = null;
    }

    public InputStream getBinaryStream() throws SQLException {
        return null;
    }

    public Reader getCharacterStream() throws SQLException {
        return null;
    }

    public <T extends Source> T getSource(Class<T> sourceClass)
            throws SQLException {
        return null;
    }

    public String getString() throws SQLException {
        return str;
    }

    public OutputStream setBinaryStream() throws SQLException {
        return null;
    }

    public Writer setCharacterStream() throws SQLException {
        return null;
    }

    public <T extends Result> T setResult(Class<T> resultClass)
            throws SQLException {
        return null;
    }

    public void setString(String value) throws SQLException {
        str = value;
    }
}
