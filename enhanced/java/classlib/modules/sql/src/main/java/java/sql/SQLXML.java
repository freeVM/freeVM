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

package java.sql;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

/**
 * TODO Javadoc
 */
public interface SQLXML {

    void free() throws SQLException;

    InputStream getBinaryStream() throws SQLException;

    OutputStream setBinaryStream() throws SQLException;

    Reader getCharacterStream() throws SQLException;

    Writer setCharacterStream() throws SQLException;

    String getString() throws SQLException;

    void setString(String value) throws SQLException;

    <T extends Source> T getSource(Class<T> sourceClass) throws SQLException;

    <T extends Result> T setResult(Class<T> resultClass) throws SQLException;
}