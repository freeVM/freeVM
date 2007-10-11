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
package org.apache.harmony.sql.internal.rowset;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.rowset.WebRowSet;
import javax.sql.rowset.spi.SyncFactoryException;

import org.apache.harmony.luni.util.NotImplementedException;

public class WebRowSetImpl extends CachedRowSetImpl implements WebRowSet {

    public WebRowSetImpl(String providerID) throws SyncFactoryException {
        super(providerID);
    }

    public void readXml(Reader reader) throws SQLException {
        throw new NotImplementedException();
    }

    public void readXml(InputStream iStream) throws SQLException, IOException {
        throw new NotImplementedException();
    }

    public void writeXml(ResultSet rs, Writer writer) throws SQLException {
        throw new NotImplementedException();
    }

    public void writeXml(ResultSet rs, OutputStream oStream)
            throws SQLException, IOException {
        throw new NotImplementedException();
    }

    public void writeXml(Writer writer) throws SQLException {
        throw new NotImplementedException();
    }

    public void writeXml(OutputStream oStream) throws SQLException, IOException {
        throw new NotImplementedException();
    }

}
