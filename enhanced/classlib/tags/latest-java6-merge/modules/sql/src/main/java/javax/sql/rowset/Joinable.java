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

package javax.sql.rowset;

import java.sql.SQLException;

public interface Joinable {
    int[] getMatchColumnIndexes() throws SQLException;

    String[] getMatchColumnNames() throws SQLException;

    void setMatchColumn(int columnIdx) throws SQLException;

    void setMatchColumn(int[] columnIdxes) throws SQLException;

    void setMatchColumn(String columnName) throws SQLException;

    void setMatchColumn(String[] columnNames) throws SQLException;

    void unsetMatchColumn(int columnIdx) throws SQLException;

    void unsetMatchColumn(int[] columnIdxes) throws SQLException;

    void unsetMatchColumn(String columnName) throws SQLException;

    void unsetMatchColumn(String[] columnName) throws SQLException;
}
