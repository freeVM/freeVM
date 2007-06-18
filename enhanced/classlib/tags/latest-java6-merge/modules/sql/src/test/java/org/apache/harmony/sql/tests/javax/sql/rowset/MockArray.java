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

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class MockArray implements Array {

    public Object getArray() throws SQLException {
        return new Object[0];
    }

    public Object getArray(long index, int count) throws SQLException {
        return null;
    }

    public Object getArray(long index, int count, Map<String, Class<?>> map)
            throws SQLException {
        return null;
    }

    public Object getArray(Map<String, Class<?>> map) throws SQLException {
        return new Object[0];
    }

    public int getBaseType() throws SQLException {
        return 0;
    }

    public String getBaseTypeName() throws SQLException {
        return "base";
    }

    public ResultSet getResultSet() throws SQLException {
        return null;
    }

    public ResultSet getResultSet(long index, int count)
            throws SQLException {
        return null;
    }

    public ResultSet getResultSet(long index, int count,
            Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    public ResultSet getResultSet(Map<String, Class<?>> map)
            throws SQLException {
        return null;
    }

    public void free() throws SQLException {
    }

}