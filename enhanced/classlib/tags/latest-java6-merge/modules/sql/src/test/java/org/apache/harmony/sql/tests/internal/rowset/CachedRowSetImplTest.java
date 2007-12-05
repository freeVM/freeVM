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

package org.apache.harmony.sql.tests.internal.rowset;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;

import javax.sql.RowSetEvent;
import javax.sql.RowSetListener;
import javax.sql.RowSetMetaData;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.spi.SyncProviderException;

public class CachedRowSetImplTest extends CachedRowSetTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetOriginalRow() throws Exception {
        try {
            crset.getOriginalRow();
            fail("should throw SQLException");
        } catch (SQLException e) {
            // expected: spec throw SQLException
        } catch (ArrayIndexOutOfBoundsException e) {
            // RI throw ArrayIndexOutOfBoundsException
        }

        assertTrue(crset.absolute(3));
        assertNotSame(crset.getOriginalRow(), crset.getOriginalRow());

        crset.updateString(2, "update3");
        ResultSet originalRow = crset.getOriginalRow();
        assertTrue(originalRow.next());
        assertEquals("test3", originalRow.getString(2));

        // after call acceptChanges()
        crset.updateRow();
        crset.acceptChanges();
        assertTrue(crset.absolute(3));
        assertEquals("update3", crset.getString(2));
        originalRow = crset.getOriginalRow();
        assertTrue(originalRow.next());
        // TODO uncomment it after implement Writer
        // assertEquals("update3", originalRow.getString(2));
    }

    public void testSetSyncProvider() throws Exception {
        if (System.getProperty("Testing Harmony") == "true") {
            String mySyncProvider = "org.apache.harmony.sql.internal.rowset.HYOptimisticProvider";
            crset.setSyncProvider(mySyncProvider);
            assertEquals(crset.getSyncProvider().getClass().getCanonicalName(),
                    mySyncProvider);
        }
    }

    public void testColumnUpdatedInt() throws SQLException {
        crset.first();
        // try {
        // assertFalse(crset.columnUpdated(1));
        // fail("should throw SQLException");
        // } catch (SQLException e) {
        // // expected;
        // }
        crset.next();
        try {
            crset.columnUpdated(-1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected;
        }
        try {
            crset.columnUpdated(0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected;
        }
        assertFalse(crset.columnUpdated(1));
    }

    public void testColumnUpdatedString() throws SQLException {
        crset.first();
        // try {
        // assertFalse(crset.columnUpdated("ID"));
        // fail("should throw SQLException");
        // } catch (SQLException e) {
        // // expected;
        // }
        crset.next();
        try {
            assertFalse(crset.columnUpdated("Incorrect"));
            fail("should throw SQLException");
        } catch (SQLException e) {
            // expected;
        }
        assertFalse(crset.columnUpdated("NAME"));
    }

    public void testGetPageSize() throws SQLException {
        assertEquals(0, crset.getPageSize());
        crset.setPageSize(1);
        assertEquals(1, crset.getPageSize());
    }

    public void testSetPageSize() throws SQLException {
        try {
            crset.setPageSize(-1);
            fail("should throw SQLException");
        } catch (SQLException e) {
            // expected;
        }
        crset.setPageSize(0);
        crset.setPageSize(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, crset.getPageSize());
    }

    public void testGetTableName() throws SQLException {

        crset.setTableName("USER");
        assertEquals("USER", crset.getTableName());
    }

    public void testSetTableName() throws SQLException {
        try {
            crset.setTableName(null);
            fail("should throw SQLException");
        } catch (SQLException e) {
            // expected;
        }
    }

    public void testSize() throws Exception {
        assertEquals(DEFAULT_ROW_COUNT, crset.size());
        // before populate should return 0
        assertEquals(0, noInitialCrset.size());
    }

    public void testDeleteRow() throws SQLException {
        crset.first();
        // try {
        // crset.deleteRow();
        // fail("should throw SQLException");
        // } catch (SQLException e) {
        // // expected;
        // }
        crset.next();
        assertFalse(crset.rowDeleted());
        crset.deleteRow();
        assertEquals(DEFAULT_ROW_COUNT, crset.size());
        assertTrue(crset.rowDeleted());
    }

    public void testRowDeleted() throws SQLException {
        // try {
        // crset.rowDeleted();
        // fail("should throw SQLException");
        // } catch (SQLException e) {
        // // expected;
        // }
    }

    public void testInsertRow() throws SQLException {
        crset.first();
        try {
            crset.insertRow();
            fail("should throw SQLException");
        } catch (SQLException e) {
            // expected;
        }
        crset.next();
        try {
            crset.insertRow();
            fail("should throw SQLException");
        } catch (SQLException e) {
            // expected;
        }
        crset.moveToInsertRow();
        crset.updateString("Name", "TonyWu");
        crset.updateInt("ID", 3);
        crset.insertRow();
        assertEquals("TonyWu", crset.getString(2));
        assertEquals("TonyWu", crset.getString("Name"));
        assertEquals(3, crset.getInt(1));
        assertEquals(3, crset.getInt("ID"));

        crset.moveToCurrentRow();
        assertFalse(crset.rowInserted());

    }

    public void testAcceptChanges() throws SQLException {
        crset.setTableName("USER_INFO");
        // FIXME: if the value of column is null, it would go wrong when
        // call acceptChanges(). And if one method in TestCase throws
        // SQLException, the following method will be affected.
        rs.next();
        assertEquals(1, rs.getInt(1));
        assertEquals("hermit", rs.getString(2));

        crset.absolute(3);
        assertEquals(3, crset.getInt(1));
        assertEquals("test3", crset.getString(2));
        crset.updateString(2, "HarmonY");

        crset.moveToInsertRow();
        crset.updateInt(1, 16);
        crset.updateString(2, "Apache");
        crset.insertRow();
        crset.moveToCurrentRow();

        crset.deleteRow();
        /*
         * RI will print the change result
         */
        crset.acceptChanges();

        rs = st.executeQuery("select * from USER_INFO where NAME = 'hermit'");
        rs.next();
        assertEquals("hermit", rs.getString(2));
        rs = st.executeQuery("select * from USER_INFO where NAME = 'test4'");
        rs.next();
        assertEquals("test4", rs.getString(2));

    }

    public void testExecute() throws SQLException {
        crset.setCommand("Update User_INFO set Name= ? Where ID= ? ");
        crset.setString(1, "executed!");
        crset.setInt(2, 1);
        crset.execute();

        crset.setCommand("SELECT ID, NAME FROM USER_INFO" + " WHERE ID = ? ");
        crset.setInt(1, 1);
        crset.execute();

        crset.first();
        assertEquals("executed!", crset.getString(2));

        crset.setCommand("Update User_INFO set Name= ? Where ID= ? ");
        crset.setString(1, "executed!");
        crset.setInt(2, 1);
        crset.execute(DriverManager.getConnection(DERBY_URL));

        crset.setCommand("SELECT ID, NAME FROM USER_INFO" + " WHERE ID = ? ");
        crset.setInt(1, 1);
        crset.execute(DriverManager.getConnection(DERBY_URL));
    }

    public void testExecute2() throws Exception {
        crset.setCommand("SELECT ID, NAME FROM USER_INFO" + " WHERE ID = ? ");
        crset.setInt(1, 1);
        crset.execute();

        crset.first();
        assertEquals("hermit", crset.getString(2));
    }

    public void testExecute3() throws Exception {
        // insert 15 more rows for test
        insertMoreData(15);

        rs = st.executeQuery("select * from USER_INFO");
        noInitialCrset.setUrl(DERBY_URL);
        noInitialCrset.setPageSize(5);
        noInitialCrset.setCommand("select * from USER_INFO");
        noInitialCrset.execute();
        rs = st.executeQuery("select * from USER_INFO");
        int cursorIndex = 0;
        while (noInitialCrset.next() && rs.next()) {
            cursorIndex++;
            for (int i = 1; i <= DEFAULT_COLUMN_COUNT; i++) {
                assertEquals(rs.getObject(i), noInitialCrset.getObject(i));
            }
        }
        // The pageSize works here. CachedRowSet only get 5 rows from ResultSet.
        assertEquals(5, cursorIndex);

        // change a command
        noInitialCrset = newNoInitialInstance();
        noInitialCrset.setUrl(null);
        // The pageSize still work here
        noInitialCrset.setPageSize(5);
        assertEquals(5, noInitialCrset.getPageSize());
        noInitialCrset.setCommand("select * from USER_INFO where NAME like ?");
        noInitialCrset.setString(1, "test%");
        Connection aConn = DriverManager.getConnection(DERBY_URL);
        noInitialCrset.execute(aConn);
        aConn.close();
        cursorIndex = 1;
        while (noInitialCrset.next()) {
            cursorIndex++;
            assertEquals(cursorIndex, noInitialCrset.getInt(1));
        }
        assertEquals(6, cursorIndex);

        noInitialCrset = newNoInitialInstance();
        rs = st.executeQuery("select * from USER_INFO");

        noInitialCrset.setUrl(DERBY_URL);
        noInitialCrset.setPageSize(5);
        noInitialCrset.setMaxRows(2);
        noInitialCrset.setCommand("select * from USER_INFO");

        noInitialCrset.execute();

        rs = st.executeQuery("select * from USER_INFO");
        cursorIndex = 0;
        while (noInitialCrset.next() && rs.next()) {
            cursorIndex++;
        }
        // maxRows works here
        assertEquals(2, cursorIndex);
    }

    public void testCreateShared() throws Exception {
        crset.setUsername("testUsername");
        crset.setPassword("testPassword");
        crset.setPageSize(5);
        Listener listener = new Listener(); // a class implements RowSetListener
        crset.addRowSetListener(listener);
        crset.absolute(3); // move to the third row for testing
        // TODO: when the cursor moved, notifyCursorMoved() should be called
        // assertEquals("cursorMoved", listener.getTag());

        CachedRowSet crsetShared = (CachedRowSet) crset.createShared();
        assertEquals("testUsername", crsetShared.getUsername());
        assertEquals("testPassword", crsetShared.getPassword());
        assertEquals(5, crsetShared.getPageSize());
        // check whether modify the attribute of the original is visible to the
        // duplicate
        crset.setUsername("modifyUsername");
        crset.setPageSize(10);
        assertEquals("modifyUsername", crset.getUsername());
        assertEquals("testUsername", crsetShared.getUsername());
        assertEquals(10, crset.getPageSize());
        assertEquals(5, crsetShared.getPageSize());

        // compare the current row, that is the third row
        assertEquals(3, crset.getInt(1));
        assertEquals("test3", crset.getString(2));
        assertEquals(3, crsetShared.getInt(1));
        assertEquals("test3", crsetShared.getString(2));
        // check whether update the duplicate is visible to the original
        crsetShared.updateString(2, "modify3");
        assertEquals("modify3", crsetShared.getString(2));
        assertEquals("modify3", crset.getString(2));
        crsetShared.updateRow();
        crsetShared.acceptChanges();
        assertEquals("rowSetChanged", listener.getTag());

        // when move the duplicate's cursor, the original shouldn't be affected
        crsetShared.absolute(1);
        assertEquals(1, crsetShared.getInt(1));
        assertEquals(3, crset.getInt(1));
    }

    public void testcreateCopyNoConstraints() throws Exception {

        crset.setConcurrency(ResultSet.CONCUR_READ_ONLY);
        crset.setType(ResultSet.TYPE_SCROLL_SENSITIVE);
        crset.setEscapeProcessing(false);
        crset.setMaxRows(10);
        crset.setTransactionIsolation(Connection.TRANSACTION_NONE);
        crset.setQueryTimeout(10);
        crset.setPageSize(10);
        crset.setShowDeleted(true);
        crset.setUsername("username");
        crset.setPassword("password");
        crset.setTypeMap(new HashMap<String, Class<?>>());
        crset.setMaxFieldSize(10);
        crset.setFetchDirection(ResultSet.FETCH_UNKNOWN);

        CachedRowSet copy = crset.createCopyNoConstraints();

        // default is ResultSet.CONCUR_UPDATABLE
        assertEquals(ResultSet.CONCUR_UPDATABLE, copy.getConcurrency());
        // default is ResultSet.TYPE_SCROLL_INSENSITIVE
        assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, copy.getType());
        // default is true
        assertTrue(copy.getEscapeProcessing());
        // default is 0
        assertEquals(0, copy.getMaxRows());
        // default is Connection.TRANSACTION_READ_COMMITTED
        assertEquals(Connection.TRANSACTION_READ_COMMITTED, copy
                .getTransactionIsolation());
        // default is 0
        assertEquals(0, copy.getQueryTimeout());
        // default is false
        assertFalse(copy.getShowDeleted());
        // default is 0
        assertEquals(0, copy.getMaxFieldSize());
        // default is null
        assertNull(copy.getPassword());
        // default is null
        assertNull(copy.getUsername());
        // default is null
        assertNull(copy.getTypeMap());

        if (crset.getKeyColumns() == null) {
            assertNull(copy.getKeyColumns());
        } else {
            int[] keyColumns = crset.getKeyColumns();
            int[] copyKeyColumns = copy.getKeyColumns();

            assertEquals(keyColumns.length, copyKeyColumns.length);
            for (int i = 0; i < keyColumns.length; i++) {
                assertEquals(keyColumns[i], copyKeyColumns[i]);
            }
            assertEquals(crset.getKeyColumns(), copy.getKeyColumns());
        }

        assertEquals(crset.getFetchDirection(), copy.getFetchDirection());
        assertEquals(crset.getPageSize(), copy.getPageSize());

        // TODO uncomment them after implemented
        // assertEquals(crset.isBeforeFirst(), crsetCopy.isBeforeFirst());
        // assertEquals(crset.isAfterLast(), crsetCopy.isAfterLast());
        // assertEquals(crset.isFirst(), crsetCopy.isFirst());
        // assertEquals(crset.isLast(), crsetCopy.isLast());
        // assertEquals(crset.getRow(), copy.getRow());
        // assertNotSame(crset.getWarnings(), copy.getWarnings());
        // assertEquals(crset.getStatement(), copy.getStatement());
        // try {
        // assertEquals(crset.getCursorName(), copy.getCursorName());
        // fail("Should throw SQLException");
        // } catch (SQLException e) {
        // // expected
        // }
        //
        // try {
        // assertEquals(crset.getMatchColumnIndexes(), copy
        // .getMatchColumnIndexes());
        // fail("Should throw SQLException");
        // } catch (SQLException e) {
        // // expected
        // }
        //
        // try {
        // assertEquals(crset.getMatchColumnNames(), copy
        // .getMatchColumnNames());
        // } catch (SQLException e) {
        // // expected
        // }

        assertEquals(crset.isReadOnly(), copy.isReadOnly());
        assertEquals(crset.size(), copy.size());

        // different metaData object
        assertNotSame(crset.getMetaData(), copy.getMetaData());

        isMetaDataEquals(crset.getMetaData(), copy.getMetaData());

        assertEquals(crset.getCommand(), copy.getCommand());

        // check SyncProvider
        assertEquals(crset.getSyncProvider().getProviderID(), copy
                .getSyncProvider().getProviderID());
        assertEquals(crset.getSyncProvider().getProviderGrade(), copy
                .getSyncProvider().getProviderGrade());
        assertEquals(crset.getSyncProvider().getDataSourceLock(), copy
                .getSyncProvider().getDataSourceLock());
        assertEquals(crset.getSyncProvider().getVendor(), copy
                .getSyncProvider().getVendor());
        assertEquals(crset.getSyncProvider().getVersion(), copy
                .getSyncProvider().getVersion());

        assertEquals(crset.getTableName(), copy.getTableName());
        assertEquals(crset.getUrl(), copy.getUrl());

    }

    public void testcreateCopyNoConstraints2() throws Exception {

        // the default value
        assertNull(crset.getCommand());
        assertEquals(ResultSet.CONCUR_UPDATABLE, crset.getConcurrency());
        assertNull(crset.getDataSourceName());
        assertEquals(ResultSet.FETCH_FORWARD, crset.getFetchDirection());
        assertEquals(0, crset.getFetchSize());
        assertEquals(0, crset.getMaxFieldSize());
        assertEquals(0, crset.getMaxRows());
        assertEquals(0, crset.getPageSize());
        assertNull(crset.getPassword());
        assertEquals(0, crset.getQueryTimeout());
        assertEquals(ResultSet.CLOSE_CURSORS_AT_COMMIT, crset
                .getTransactionIsolation());
        assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, crset.getType());
        assertNull(crset.getTypeMap());
        assertEquals(DERBY_URL, crset.getUrl());
        assertNull(crset.getUsername());
        assertTrue(crset.getEscapeProcessing());
        assertNull(crset.getKeyColumns());

        // set value
        crset.setCommand("testCommand");
        crset.setConcurrency(ResultSet.CONCUR_READ_ONLY);
        crset.setDataSourceName("testDataSourceName");
        crset.setFetchDirection(ResultSet.FETCH_REVERSE);
        crset.setMaxFieldSize(100);
        crset.setMaxRows(10);
        crset.setPageSize(10);
        crset.setPassword("passwo");
        crset.setQueryTimeout(100);
        crset.setTableName("testTable");
        crset.setTransactionIsolation(ResultSet.HOLD_CURSORS_OVER_COMMIT);
        crset.setType(ResultSet.TYPE_SCROLL_SENSITIVE);
        crset.setTypeMap(new HashMap<String, Class<?>>());
        crset.setUsername("testUserName");
        crset.setEscapeProcessing(false);
        crset.setKeyColumns(new int[] { 1 });

        // check the changed value
        assertEquals("testCommand", crset.getCommand());
        assertEquals(ResultSet.CONCUR_READ_ONLY, crset.getConcurrency());
        assertEquals("testDataSourceName", crset.getDataSourceName());
        assertEquals(ResultSet.FETCH_REVERSE, crset.getFetchDirection());
        assertEquals(0, crset.getFetchSize());
        assertEquals(100, crset.getMaxFieldSize());
        assertEquals(10, crset.getMaxRows());
        assertEquals(10, crset.getPageSize());
        assertEquals("passwo", crset.getPassword());
        assertEquals(100, crset.getQueryTimeout());
        assertEquals("testTable", crset.getTableName());
        assertEquals(ResultSet.HOLD_CURSORS_OVER_COMMIT, crset
                .getTransactionIsolation());
        assertEquals(ResultSet.TYPE_SCROLL_SENSITIVE, crset.getType());
        assertNotNull(crset.getTypeMap());
        assertNull(crset.getUrl());
        assertEquals("testUserName", crset.getUsername());
        assertFalse(crset.getEscapeProcessing());
        assertTrue(Arrays.equals(new int[] { 1 }, crset.getKeyColumns()));

        // after call createCopyNoConstraints
        CachedRowSet copy = crset.createCopyNoConstraints();
        assertEquals("testCommand", copy.getCommand());
        assertEquals(ResultSet.CONCUR_UPDATABLE, copy.getConcurrency());
        assertEquals("testDataSourceName", copy.getDataSourceName());
        assertEquals(ResultSet.FETCH_REVERSE, copy.getFetchDirection());
        assertEquals(0, copy.getFetchSize());
        assertEquals(0, copy.getMaxFieldSize());
        assertEquals(0, copy.getMaxRows());
        assertEquals(10, copy.getPageSize());
        assertNull(copy.getPassword());
        assertEquals(0, copy.getQueryTimeout());
        assertEquals("testTable", copy.getTableName());
        assertEquals(ResultSet.CLOSE_CURSORS_AT_COMMIT, copy
                .getTransactionIsolation());
        assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, copy.getType());
        assertNull(copy.getTypeMap());
        assertNull(copy.getUrl());
        assertNull(copy.getUsername());
        assertTrue(copy.getEscapeProcessing());
        assertTrue(Arrays.equals(new int[] { 1 }, copy.getKeyColumns()));
    }

    public void testCopySchema() throws Exception {
        // the original's addtribute and meta data
        crset.setCommand("testCommand");
        crset.setConcurrency(ResultSet.CONCUR_UPDATABLE);
        crset.setDataSourceName("testDataSource");
        crset.setFetchDirection(ResultSet.FETCH_UNKNOWN);
        crset.setPageSize(20);
        crset.setMaxRows(20);
        crset.setTableName("USER_INFO");
        /*
         * NOTICE: spec say copy must not has any content, but when run on RI,
         * if call next() before call createCopySchema(), the copy can get the
         * current row's data
         */

        /*
         * NOTICE: when run on RI, if add the listener first, then it will go
         * wrong when call createCopySchema().It's said that clone failed.
         */
        // Listener listener = new Listener();
        // crset.addRowSetListener(listener);
        RowSetMetaData rsmd = (RowSetMetaData) crset.getMetaData();
        // the copy
        CachedRowSet crsetCopySchema = crset.createCopySchema();
        RowSetMetaData rsmdCopySchema = (RowSetMetaData) crsetCopySchema
                .getMetaData();

        // compare the meta data between the duplicate and the original
        assertNotSame(crset.getMetaData(), crsetCopySchema.getMetaData());
        assertNotSame(crset.getOriginal(), crsetCopySchema.getOriginal());
        // assertNotSame(crset.getSyncProvider(), crsetCopySchema
        // .getSyncProvider());

        assertEquals("USER_INFO", crset.getTableName());
        assertEquals("USER_INFO", rsmdCopySchema.getTableName(1));
        assertEquals(DEFAULT_COLUMN_COUNT, rsmdCopySchema.getColumnCount());
        assertEquals(rsmd.getColumnName(1), rsmdCopySchema.getColumnName(1));
        // check the primary key
        // TODO: RI doesn't evalute the keyColumns. The value of
        // crset.getKeyColumns() is null.
        assertEquals(crset.getKeyColumns(), crsetCopySchema.getKeyColumns());

        // check the attributes in the duplicate. These are supposed to be the
        // same as the original
        // System.out.println("crsetCopySchema: " + crsetCopySchema.getInt(1));
        assertFalse(crsetCopySchema.next());
        assertEquals("testCommand", crsetCopySchema.getCommand());
        assertEquals(ResultSet.CONCUR_UPDATABLE, crsetCopySchema
                .getConcurrency());
        assertEquals("testDataSource", crsetCopySchema.getDataSourceName());
        assertEquals(ResultSet.FETCH_UNKNOWN, crsetCopySchema
                .getFetchDirection());
        assertEquals(20, crsetCopySchema.getPageSize());
        assertEquals(20, crsetCopySchema.getMaxRows());

        // fill the duplicate CachedRowSet with data, check the listener
        Listener listener = new Listener();
        crsetCopySchema.addRowSetListener(listener);
        assertNull(listener.getTag());
        rs = st.executeQuery("select * from USER_INFO");
        crsetCopySchema.populate(rs);
        // TODO: in the Harmony implementation, need to call notifyRowSetChanged
        // at the suitable place
        // assertEquals("rowSetChanged", listener.getTag());
        listener.setTag(null);
        // the move of the original's cursor shouldn't affect the duplicate
        crset.next();
        assertNull(listener.getTag());
    }

    public void testCopySchema2() throws Exception {

        // set value
        crset.setCommand("testCommand");
        crset.setConcurrency(ResultSet.CONCUR_READ_ONLY);
        crset.setDataSourceName("testDataSourceName");
        crset.setFetchDirection(ResultSet.FETCH_REVERSE);
        crset.setMaxFieldSize(100);
        crset.setMaxRows(10);
        crset.setPageSize(10);
        crset.setPassword("passwo");
        crset.setQueryTimeout(100);
        crset.setTableName("testTable");
        crset.setTransactionIsolation(ResultSet.HOLD_CURSORS_OVER_COMMIT);
        crset.setType(ResultSet.TYPE_SCROLL_SENSITIVE);
        crset.setTypeMap(new HashMap<String, Class<?>>());
        crset.setEscapeProcessing(false);
        crset.setKeyColumns(new int[] { 1 });

        // call createCopySchema()
        CachedRowSet copy = crset.createCopySchema();
        assertFalse(copy.next());
        assertEquals(crset.getCommand(), copy.getCommand());
        assertEquals(crset.getConcurrency(), copy.getConcurrency());
        assertEquals(crset.getDataSourceName(), copy.getDataSourceName());
        assertEquals(crset.getFetchDirection(), copy.getFetchDirection());
        assertEquals(crset.getMaxFieldSize(), copy.getMaxFieldSize());
        assertEquals(crset.getMaxRows(), copy.getMaxRows());
        assertEquals(crset.getPageSize(), copy.getPageSize());
        assertEquals(crset.getQueryTimeout(), copy.getQueryTimeout());
        assertEquals(crset.getTableName(), copy.getTableName());
        assertEquals(crset.getTransactionIsolation(), copy
                .getTransactionIsolation());
        assertEquals(crset.getType(), copy.getType());
        assertEquals(crset.getUrl(), copy.getUrl());
        assertEquals(crset.getEscapeProcessing(), copy.getEscapeProcessing());
        assertTrue(Arrays.equals(crset.getKeyColumns(), copy.getKeyColumns()));

        // compare the object reference
        assertNotSame(crset.getKeyColumns(), copy.getKeyColumns());
        assertNotSame(crset.getMetaData(), copy.getMetaData());
        assertNotSame(crset.getOriginal(), copy.getOriginal());
        assertNotSame(crset.getTypeMap(), copy.getTypeMap());
    }

    public void testCreateCopy() throws Exception {

        // TODO: lack of the test for CachedRowSet.getOriginal() and
        // CachedRowSet.getOriginalRow()

        crset.absolute(3);

        CachedRowSet crsetCopy = crset.createCopy();

        crsetCopy.updateString(2, "copyTest3");
        crsetCopy.updateRow();
        crsetCopy.acceptChanges();

        assertEquals(crsetCopy.getString(2), "copyTest3");

        assertEquals(crset.getString(2), "test3");

        rs = st.executeQuery("select * from USER_INFO");
        rs.next();
        rs.next();
        rs.next();
        // TODO: Uncomment it when Writer is implemented fully.
        // assertEquals(rs.getString(2), "copyTest3");

        reloadCachedRowSet();
        crset.absolute(2);

        crsetCopy = crset.createCopy();

        assertEquals(crset.isReadOnly(), crsetCopy.isReadOnly());
        // TODO uncomment when isBeforeFirst is implemented
        // assertEquals(crset.isBeforeFirst(), crsetCopy.isBeforeFirst());
        // TODO uncomment when isAfterLast is implemented
        // assertEquals(crset.isAfterLast(), crsetCopy.isAfterLast());
        // TODO uncomment when isFirst is implemented
        // assertEquals(crset.isFirst(), crsetCopy.isFirst());
        // TODO uncomment when isLast is implemented
        // assertEquals(crset.isLast(), crsetCopy.isLast());

        assertEquals(crset.size(), crsetCopy.size());
        // different metaData object
        assertNotSame(crset.getMetaData(), crsetCopy.getMetaData());

        isMetaDataEquals(crset.getMetaData(), crsetCopy.getMetaData());

        assertEquals(crset.getCommand(), crsetCopy.getCommand());
        assertEquals(crset.getConcurrency(), crsetCopy.getConcurrency());

        // uncomment after implemented
        // try {
        // assertEquals(crset.getCursorName(), crsetCopy.getCursorName());
        // fail("Should throw SQLException");
        // } catch (SQLException e) {
        // // expected
        // }
        // try {
        // assertEquals(crset.getMatchColumnIndexes(), crsetCopy
        // .getMatchColumnIndexes());
        // fail("Should throw SQLException");
        // } catch (SQLException e) {
        // // expected
        // }
        //
        // try {
        // assertEquals(crset.getMatchColumnNames(), crsetCopy
        // .getMatchColumnNames());
        // } catch (SQLException e) {
        // // expected
        // }
        // assertEquals(crset.getRow(), crsetCopy.getRow());
        // assertEquals(crset.getStatement(), crsetCopy.getStatement());
        // assertNotSame(crset.getWarnings(), crsetCopy.getWarnings());

        assertEquals(crset.getEscapeProcessing(), crsetCopy
                .getEscapeProcessing());
        assertEquals(crset.getFetchDirection(), crsetCopy.getFetchDirection());
        assertEquals(crset.getFetchSize(), crsetCopy.getFetchSize());
        if (crset.getKeyColumns() == null) {
            assertNull(crsetCopy.getKeyColumns());
        } else {
            int[] keyColumns = crset.getKeyColumns();
            int[] copyKeyColumns = crsetCopy.getKeyColumns();

            assertEquals(keyColumns.length, copyKeyColumns.length);
            for (int i = 0; i < keyColumns.length; i++) {
                assertEquals(keyColumns[i], copyKeyColumns[i]);
            }
            assertEquals(crset.getKeyColumns(), crsetCopy.getKeyColumns());
        }

        assertEquals(crset.getMaxFieldSize(), crsetCopy.getMaxFieldSize());
        assertEquals(crset.getMaxRows(), crsetCopy.getMaxRows());

        assertEquals(crset.getPageSize(), crsetCopy.getPageSize());
        assertEquals(crset.getPassword(), crsetCopy.getPassword());
        assertEquals(crset.getQueryTimeout(), crsetCopy.getQueryTimeout());
        assertEquals(crset.getShowDeleted(), crsetCopy.getShowDeleted());

        assertEquals(crset.getSyncProvider().getProviderID(), crsetCopy
                .getSyncProvider().getProviderID());
        assertEquals(crset.getSyncProvider().getProviderGrade(), crsetCopy
                .getSyncProvider().getProviderGrade());
        assertEquals(crset.getSyncProvider().getDataSourceLock(), crsetCopy
                .getSyncProvider().getDataSourceLock());
        assertEquals(crset.getSyncProvider().getVendor(), crsetCopy
                .getSyncProvider().getVendor());
        assertEquals(crset.getSyncProvider().getVersion(), crsetCopy
                .getSyncProvider().getVersion());

        assertEquals(crset.getTableName(), crsetCopy.getTableName());
        assertEquals(crset.getTransactionIsolation(), crsetCopy
                .getTransactionIsolation());
        assertEquals(crset.getType(), crsetCopy.getType());

        assertEquals(crset.getUrl(), crsetCopy.getUrl());
        assertEquals(crset.getUsername(), crsetCopy.getUsername());

    }

    public void testCreateCopy2() throws Exception {

        CachedRowSet copy = crset.createCopy();

        copy.absolute(3);
        crset.absolute(3);

        copy.updateString(2, "updated");
        assertEquals("updated", copy.getString(2));
        assertEquals("test3", crset.getString(2));
        copy.updateRow();
        copy.acceptChanges();

        assertEquals(copy.getString(2), "updated");
        assertEquals(crset.getString(2), "test3");

        crset.updateString(2, "again");

        assertEquals(copy.getString(2), "updated");
        assertEquals(crset.getString(2), "again");

        crset.updateRow();
        try {
            /*
             * seems ri doesn't release lock when expception throw from
             * acceptChanges(), which will cause test case block at insertData()
             * when next test case setUp, so we must pass current connection to
             * it, and all resource would be released after connection closed.
             */
            crset.acceptChanges(conn);
            // TODO: wait the implementation of Writer
            // fail("Should throw SyncProviderException");
        } catch (SyncProviderException e) {
            // expected
        }

        assertEquals(copy.getString(2), "updated");

        crset.absolute(3);
        // data doesn't change
        assertEquals("again", crset.getString(2));
    }

    public void testCreateCopy3() throws Exception {
        crset.setCommand("SELECT * FROM USER_INFO WHERE ID = ?");
        crset.setInt(1, 3);
        crset.execute();

        assertEquals(12, crset.getMetaData().getColumnCount());
        assertTrue(crset.next());
        assertEquals("test3", crset.getString(2));
        assertFalse(crset.next());

        CachedRowSet crsetCopy = crset.createCopy();
        crsetCopy.execute();
        assertEquals(12, crsetCopy.getMetaData().getColumnCount());
        assertTrue(crsetCopy.next());
        assertEquals("test3", crsetCopy.getString(2));
        assertFalse(crsetCopy.next());

        crsetCopy.setCommand("SELECT * FROM USER_INFO WHERE NAME = ?");
        crsetCopy.setString(1, "test4");
        crsetCopy.execute();
        assertTrue(crsetCopy.next());
        assertEquals(4, crsetCopy.getInt(1));
        assertFalse(crsetCopy.next());

        crset.execute();
        assertTrue(crset.next());
        assertEquals("test3", crset.getString(2));
        assertFalse(crset.next());
    }

    public void testAfterLast() throws Exception {
        try {
            rs.afterLast();
            fail("Should throw SQLException");
        } catch (SQLException e) {
            // expected
        }

        crset.afterLast();
        crset.previous();
        assertEquals(4, crset.getInt(1));
    }

    public void testNextandPreviousPage() throws Exception {

        st.executeUpdate("delete from USER_INFO");
        st.executeUpdate("insert into USER_INFO(ID,NAME) values (1,'1')");
        st.executeUpdate("insert into USER_INFO(ID,NAME) values (2,'2')");
        st.executeUpdate("insert into USER_INFO(ID,NAME) values (3,'3')");
        st.executeUpdate("insert into USER_INFO(ID,NAME) values (4,'4')");
        rs = st.executeQuery("select * from USER_INFO");

        crset.setPageSize(2);
        crset.setCommand("SELECT ID FROM USER_INFO");
        crset.execute();

        for (int j = 0; j < 2; j++)
            crset.next();
        assertFalse(crset.next());

        int i = 0;

        crset.beforeFirst();
        while (crset.nextPage()) {
            while (crset.next()) {
                assertEquals(++i, crset.getInt(1));
            }
        }

        while (crset.previousPage()) {
            crset.afterLast();
            while (crset.previous()) {
                assertEquals(i--, crset.getInt(1));
            }
        }

        while (crset.previousPage()) {
            i = i - crset.getPageSize();
            int j = i;
            while (crset.next()) {
                assertEquals(++j, crset.getInt(1));
            }
        }
    }

    public void testPopulate_LResultSet() throws Exception {
        // insert 15 more rows for test
        insertMoreData(15);

        rs = st.executeQuery("select * from USER_INFO");
        noInitialCrset.setMaxRows(15);
        assertEquals(15, noInitialCrset.getMaxRows());

        noInitialCrset.populate(rs);

        assertTrue(noInitialCrset.isBeforeFirst());
        int cursorIndex = 0;
        while (noInitialCrset.next()) {
            cursorIndex++;
        }
        // setMaxRows no effect, we follow ri
        assertEquals(20, cursorIndex);

        /*
         * The pageSize won't work when call method populate(ResultSet) without
         * second parameter
         */
        noInitialCrset = newNoInitialInstance();
        rs = st.executeQuery("select * from USER_INFO");

        noInitialCrset.setMaxRows(15);
        assertEquals(15, noInitialCrset.getMaxRows());

        noInitialCrset.setPageSize(5);
        assertEquals(5, noInitialCrset.getPageSize());

        noInitialCrset.populate(rs);

        assertTrue(noInitialCrset.isBeforeFirst());
        rs = st.executeQuery("select * from USER_INFO");
        cursorIndex = 0;
        while (noInitialCrset.next() && rs.next()) {
            cursorIndex++;
        }
        /*
         * It's supposed to only get five rows in CachedRowSet as the
         * CachedRowSet's pageSize is 5. However, the pageSize doesn't work in
         * RI. The CachedRowSet gets all the data from ResultSet. We follow ri.
         */
        assertEquals(20, cursorIndex);

        noInitialCrset = newNoInitialInstance();
        rs = st.executeQuery("select * from USER_INFO");
        // cursor move two rows
        rs.next();
        rs.next();

        noInitialCrset.populate(rs);
        assertTrue(noInitialCrset.isBeforeFirst());
        cursorIndex = 0;
        while (noInitialCrset.next()) {
            cursorIndex++;
        }
        assertEquals(18, cursorIndex);
    }

    public void testPopulate_LResultSet_I() throws Exception {
        // insert 15 more rows for test
        insertMoreData(15);

        rs = st.executeQuery("select * from USER_INFO");
        noInitialCrset.setPageSize(5);
        try {
            noInitialCrset.populate(rs, 1);
            fail("Should throw SQLException");
        } catch (SQLException e) {
            // expected
        }

        // create a scrollable and updatable ResultSet
        st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        rs = st.executeQuery("select * from USER_INFO");

        noInitialCrset = newNoInitialInstance();
        noInitialCrset.setPageSize(6);
        noInitialCrset.populate(rs, 6);
        int cursorIndex = 5;
        while (noInitialCrset.next()) {
            cursorIndex++;
            assertEquals(cursorIndex, noInitialCrset.getInt(1));
        }
        assertEquals(11, cursorIndex);

        st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        rs = st.executeQuery("select * from USER_INFO");

        noInitialCrset = newNoInitialInstance();

        noInitialCrset.setPageSize(6);
        noInitialCrset.setMaxRows(5);

        noInitialCrset.populate(rs, 6);
        cursorIndex = 0;
        while (noInitialCrset.next()) {
            cursorIndex++;
            assertEquals(cursorIndex + 5, noInitialCrset.getInt(1));
        }
        // only get MaxRows
        assertEquals(5, cursorIndex);

        st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        rs = st.executeQuery("select * from USER_INFO");

        noInitialCrset = newNoInitialInstance();

        noInitialCrset.setMaxRows(5);
        try {
            noInitialCrset.setPageSize(6);
            fail("Should throw SQLException");
        } catch (SQLException e) {
            // expected, Page size cannot be greater than maxRows
        }

    }

    public void testPopulate_use_copy() throws Exception {
        // insert 15 more rows for test
        insertMoreData(15);

        rs = st.executeQuery("select * from USER_INFO");
        crset.close();
        crset.populate(rs);

        CachedRowSet crsetCopy = crset.createCopy();
        assertEquals(0, crsetCopy.getPageSize());
        noInitialCrset.setPageSize(5);
        // if it doesn't specify the startRow for method populate(), then the
        // pageSize wouldn't work.
        assertTrue(crsetCopy.isBeforeFirst());
        noInitialCrset.populate(crsetCopy);
        assertTrue(crsetCopy.isAfterLast());
        int cursorIndex = 0;
        while (noInitialCrset.next()) {
            cursorIndex++;
            for (int i = 1; i <= DEFAULT_COLUMN_COUNT; i++) {
                assertEquals(cursorIndex, noInitialCrset.getInt(1));
            }
        }
        assertEquals(20, cursorIndex);

        try {
            noInitialCrset.populate(crsetCopy, 0);
            fail("Should throw SQLException");
        } catch (SQLException e) {
            // invalid cursor position
        }

        try {
            noInitialCrset.populate(crsetCopy, -1);
            fail("Should throw SQLException");
        } catch (SQLException e) {
            // invalid cursor position
        }

        try {
            noInitialCrset.populate(crsetCopy, 100);
            fail("Should throw SQLException");
        } catch (SQLException e) {
            // invalid cursor position
        }

        // specify the startRow, then the noInitialCrset will get only 5 rows
        noInitialCrset.populate(crsetCopy, 1);
        assertEquals(5, noInitialCrset.getPageSize());
        assertTrue(noInitialCrset.isBeforeFirst());
        cursorIndex = 0;
        rs = st.executeQuery("select * from USER_INFO");
        while (noInitialCrset.next() && rs.next()) {
            cursorIndex++;
            for (int i = 1; i <= DEFAULT_COLUMN_COUNT; i++) {
                assertEquals(cursorIndex, noInitialCrset.getInt(1));
                assertEquals(rs.getObject(i), noInitialCrset.getObject(i));
            }
        }
        // the pageSize works here
        assertEquals(5, cursorIndex);

        // the noInitialCrset would fetch data from the eleventh row
        noInitialCrset.populate(crsetCopy, 11);
        cursorIndex = 10;
        while (noInitialCrset.next()) {
            cursorIndex++;
            assertEquals(cursorIndex, noInitialCrset.getInt(1));
        }
        assertEquals(15, cursorIndex);
    }

    public void testConstructor() throws Exception {

        assertTrue(noInitialCrset.isReadOnly());
        assertEquals(0, noInitialCrset.size());
        assertNull(noInitialCrset.getMetaData());

        assertNull(noInitialCrset.getCommand());
        assertEquals(ResultSet.CONCUR_UPDATABLE, noInitialCrset
                .getConcurrency());
        // TODO uncomment after impelemented
        // try {
        // crset.getCursorName();
        // fail("Should throw SQLException");
        // } catch (SQLException e) {
        // // expected
        // }
        // try {
        // crset.getMatchColumnIndexes();
        // fail("Should throw SQLException");
        // } catch (SQLException e) {
        // // expected
        // }
        //
        // try {
        // crset.getMatchColumnNames();
        // } catch (SQLException e) {
        // // expected
        // }
        // assertEquals(0, crset.getRow());
        // assertNull(crset.getStatement());

        assertEquals(true, noInitialCrset.getEscapeProcessing());
        assertEquals(Connection.TRANSACTION_READ_COMMITTED, noInitialCrset
                .getTransactionIsolation());

        assertEquals(ResultSet.FETCH_FORWARD, noInitialCrset
                .getFetchDirection());
        assertEquals(0, noInitialCrset.getFetchSize());
        assertNull(noInitialCrset.getKeyColumns());

        assertEquals(0, noInitialCrset.getMaxFieldSize());
        assertEquals(0, noInitialCrset.getMaxRows());

        assertEquals(0, noInitialCrset.getPageSize());
        assertEquals(null, noInitialCrset.getPassword());
        assertEquals(0, noInitialCrset.getQueryTimeout());
        assertEquals(false, noInitialCrset.getShowDeleted());

        assertNull(noInitialCrset.getTableName());
        assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, noInitialCrset
                .getType());

        assertNull(noInitialCrset.getUrl());
        assertNull(noInitialCrset.getUsername());

    }

    public void testRelative() throws Exception {
        /*
         * ri throw SQLException, but spec say relative(1) is identical to next
         */
        try {
            crset.relative(1);
            fail("Should throw SQLException");
        } catch (SQLException e) {
            // expected
        }

        assertTrue(crset.next());
        assertEquals("hermit", crset.getString(2));

        assertTrue(crset.relative(2));
        assertEquals("test3", crset.getString(2));

        assertTrue(crset.relative(-1));
        assertEquals("test", crset.getString(2));

        assertTrue(crset.relative(0));
        assertEquals("test", crset.getString(2));

        assertFalse(crset.relative(-5));
        assertEquals(0, crset.getRow());

        assertTrue(crset.next());
        assertEquals("hermit", crset.getString(2));
        assertTrue(crset.relative(3));
        assertEquals("test4", crset.getString(2));

        assertFalse(crset.relative(3));
        assertEquals(0, crset.getRow());

        assertTrue(crset.isAfterLast());
        assertTrue(crset.previous());

        // non-bug different
        if ("true".equals(System.getProperty("Testing Harmony"))) {
            assertEquals(DEFAULT_ROW_COUNT, crset.getRow());
            assertEquals("test4", crset.getString(2));
        }
    }

    public void testAbsolute() throws Exception {
        // non-bug different
        if ("true".equals(System.getProperty("Testing Harmony"))) {
            assertFalse(crset.absolute(0));
        }

        assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, crset.getType());
        assertTrue(crset.absolute(1));
        assertEquals(1, crset.getInt(1));
        assertTrue(crset.absolute(4));
        assertEquals(4, crset.getInt(1));
        assertTrue(crset.absolute(3));
        assertEquals(3, crset.getInt(1));

        // when position the cursor beyond the first/last row in the result set
        assertFalse(crset.absolute(10));
        assertTrue(crset.isAfterLast());
        assertTrue(crset.previous());
        assertFalse(crset.absolute(-10));
        assertTrue(crset.isBeforeFirst());
        assertTrue(crset.next());

        /*
         * when the given row number is negative, spec says absolute(-1) equals
         * last(). However, the return value of absolute(negative) is false when
         * run on RI. The Harmony follows the spec.
         */
        if (System.getProperty("Testing Harmony") == "true") {
            assertTrue(crset.absolute(-1));
            assertEquals(4, crset.getInt(1));
            assertTrue(crset.absolute(-3));
            assertEquals(2, crset.getInt(1));
            assertFalse(crset.absolute(-5));
        }

        crset.setType(ResultSet.TYPE_FORWARD_ONLY);
        try {
            crset.absolute(1);
            fail("should throw SQLException");
        } catch (SQLException e) {
            // expected
        }

        try {
            crset.absolute(-1);
            fail("should throw SQLException");
        } catch (SQLException e) {
            // expected
        }
    }

    public void testNextAndPrevious() throws Exception {
        /*
         * This method is also used to test isBeforeFirst(), isAfterLast(),
         * isFirst(),isLast()
         */
        // Test for next()
        assertTrue(crset.isBeforeFirst());
        assertFalse(crset.isAfterLast());
        assertFalse(crset.isFirst());
        assertFalse(crset.isLast());
        assertTrue(crset.next());
        assertTrue(crset.isFirst());
        assertEquals(1, crset.getInt(1));

        assertTrue(crset.next());
        assertFalse(crset.isFirst());
        assertTrue(crset.next());
        assertTrue(crset.next());
        assertTrue(crset.isLast());
        assertEquals(4, crset.getInt(1));
        assertFalse(crset.next());
        // assertFalse(crset.next());
        assertFalse(crset.isBeforeFirst());
        assertTrue(crset.isAfterLast());

        // Test for previous()
        assertTrue(crset.previous());
        assertEquals(4, crset.getInt(1));
        assertTrue(crset.isLast());
        assertTrue(crset.previous());
        assertTrue(crset.previous());
        assertTrue(crset.previous());
        assertEquals(1, crset.getInt(1));
        assertTrue(crset.isFirst());
        assertFalse(crset.previous());
        assertTrue(crset.isBeforeFirst());
        // assertFalse(crset.previous());

        assertTrue(crset.next());
        assertTrue(crset.next());
        assertEquals(2, crset.getInt(1));

        // Test for previous()'s Exception
        assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, crset.getType());
        crset.setType(ResultSet.TYPE_FORWARD_ONLY);
        assertEquals(ResultSet.TYPE_FORWARD_ONLY, crset.getType());
        try {
            crset.previous();
            fail("should throw SQLException");
        } catch (SQLException e) {
            // expected
        }
    }

    public void testFirstAndLast() throws Exception {
        /*
         * This method is used to test afterLast(), beforeFist(), first(),
         * last()
         */
        assertTrue(crset.isBeforeFirst());
        assertTrue(crset.first());
        assertTrue(crset.isFirst());
        assertFalse(crset.isBeforeFirst());
        crset.beforeFirst();
        assertTrue(crset.isBeforeFirst());
        assertTrue(crset.last());
        assertTrue(crset.isLast());

        assertTrue(crset.first());
        assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, crset.getType());
        crset.setType(ResultSet.TYPE_FORWARD_ONLY);
        assertEquals(ResultSet.TYPE_FORWARD_ONLY, crset.getType());

        try {
            crset.beforeFirst();
            fail("should throw SQLException");
        } catch (SQLException e) {
            // expected
        }

        try {
            crset.first();
            fail("should throw SQLException");
        } catch (SQLException e) {
            // expected
        }

        try {
            crset.last();
            fail("should throw SQLException");
        } catch (SQLException e) {
            // expected
        }

        assertTrue(crset.isFirst());
    }

    public void testAcceptChanges_Insert() throws Exception {
        /*
         * Insert a new row one time
         */
        crset.moveToInsertRow();
        crset.updateInt(1, 5);
        crset.updateString(2, "test5");
        crset.updateLong(3, 444423L);
        crset.updateBigDecimal(4, new BigDecimal(12));
        crset.updateBigDecimal(5, new BigDecimal(23));
        crset.updateInt(6, 41);
        crset.updateFloat(7, 4.8F);
        crset.updateFloat(8, 4.888F);
        crset.updateDouble(9, 4.9999);
        crset.updateDate(10, new Date(965324512));
        crset.updateTime(11, new Time(452368512));
        crset.updateTimestamp(12, new Timestamp(874532105));
        crset.insertRow();
        crset.moveToCurrentRow();
        crset.acceptChanges(conn);
        // check the new row in CachedRowSet
        crset.beforeFirst();
        String newRowValue = "";
        while (crset.next()) {
            if (crset.getInt(1) == 5) {
                newRowValue = "test5";
            }
        }
        assertEquals("test5", newRowValue);
        // check the new row in DB
        rs = st.executeQuery("select * from USER_INFO where ID = 5");
        assertTrue(rs.next());
        assertEquals(5, rs.getInt(1));

        /*
         * TODO Insert multiple rows one time, uncomment after implemented
         */
        // TODO uncomment it after insert methods are implemented
        // noInitialCrset = newNoInitialInstance();
        // rs = st.executeQuery("select * from USER_INFO");
        // noInitialCrset.populate(rs);
        // noInitialCrset.setReadOnly(false);
        // noInitialCrset.moveToInsertRow();
        // for (int i = 6; i <= 20; i++) {
        // noInitialCrset.updateInt(1, i);
        // noInitialCrset.updateString(2, "test" + i);
        // noInitialCrset.insertRow();
        // }
        // noInitialCrset.moveToCurrentRow();
        // noInitialCrset.acceptChanges(conn);
        // // check the new rows in CachedRowSet
        // assertEquals(20, noInitialCrset.size());
        // // check the new rows in DB
        // rs = st.executeQuery("select * from USER_INFO");
        // int cursorIndex = 0;
        // while (rs.next()) {
        // cursorIndex++;
        // }
        // assertEquals(20, cursorIndex);
    }

    public void testAcceptChanges_InsertException() throws Exception {
        /*
         * Insert a new row. One given column's value exceeds the max range.
         * Therefore, it should throw SyncProviderException.
         */
        crset.moveToInsertRow();
        crset.updateInt(1, 4);
        crset.updateString(2, "test5");
        crset.updateLong(3, 555555L);
        crset.updateInt(4, 200000); // 200000 exceeds the NUMERIC's range
        crset.updateBigDecimal(5, new BigDecimal(23));
        crset.updateFloat(8, 4.888F);
        crset.insertRow();
        crset.moveToCurrentRow();
        try {
            crset.acceptChanges(conn);
            fail("should throw SyncProviderException");
        } catch (SyncProviderException e) {
            // TODO analysis SyncProviderException
        }

        /*
         * Insert a new row. The new row's primary key has existed. Therefore,
         * it should throw SyncProviderException.
         */
        crset = newNoInitialInstance();
        rs = st.executeQuery("select * from USER_INFO");
        crset.populate(rs);
        crset.moveToInsertRow();
        crset.updateInt(1, 4); // The ID valued 4 has existed in db.
        crset.updateString(2, "test5");
        crset.updateBigDecimal(4, new BigDecimal(12));
        crset.updateTimestamp(12, new Timestamp(874532105));
        crset.insertRow();
        crset.moveToCurrentRow();
        try {
            crset.acceptChanges(conn);
            fail("should throw SyncProviderException");
        } catch (SyncProviderException e) {
            // TODO analysis SyncProviderException
        }

        /*
         * Insert a new row. Before inserting the new row, another new row which
         * has the same data is inserted into the DB. However, the current
         * CachedRowSet doesn't know it. In this situation, it should throw
         * SyncProviderException.
         */
        crset = newNoInitialInstance();
        rs = st.executeQuery("select * from USER_INFO");
        crset.populate(rs);
        String insertSQL = "INSERT INTO USER_INFO(ID, NAME, BIGINT_T, NUMERIC_T,DECIMAL_T, SMALLINT_T, "
                + "FLOAT_T, REAL_T, DOUBLE_T, DATE_T, TIME_T, TIMESTAMP_T) VALUES(?, ?, ?, ?, ?, ?,"
                + "?, ?, ?, ?, ?, ? )";
        PreparedStatement preStmt = conn.prepareStatement(insertSQL);
        preStmt.setInt(1, 80);
        preStmt.setString(2, "test" + 80);
        preStmt.setLong(3, 444423L);
        preStmt.setBigDecimal(4, new BigDecimal(12));
        preStmt.setBigDecimal(5, new BigDecimal(23));
        preStmt.setInt(6, 41);
        preStmt.setFloat(7, 4.8F);
        preStmt.setFloat(8, 4.888F);
        preStmt.setDouble(9, 4.9999);
        preStmt.setDate(10, new Date(965324512));
        preStmt.setTime(11, new Time(452368512));
        preStmt.setTimestamp(12, new Timestamp(874532105));
        preStmt.executeUpdate();
        if (preStmt != null) {
            preStmt.close();
        }
        // check the new row in DB
        rs = st.executeQuery("select * from USER_INFO where ID = 80");
        assertTrue(rs.next());
        assertEquals(80, rs.getInt(1));
        assertEquals("test80", rs.getString(2));

        // now call CachedRowSet.insertRow()
        crset.moveToInsertRow();
        crset.updateInt(1, 80);
        crset.updateString(2, "test" + 80);
        crset.updateLong(3, 444423L);
        crset.updateBigDecimal(4, new BigDecimal(12));
        crset.updateBigDecimal(5, new BigDecimal(23));
        crset.updateInt(6, 41);
        crset.updateFloat(7, 4.8F);
        crset.updateFloat(8, 4.888F);
        crset.updateDouble(9, 4.9999);
        crset.updateDate(10, new Date(965324512));
        crset.updateTime(11, new Time(452368512));
        crset.updateTimestamp(12, new Timestamp(874532105));
        crset.insertRow();
        crset.moveToCurrentRow();
        try {
            crset.acceptChanges(conn);
            fail("should throw SyncProviderException");
        } catch (SyncProviderException e) {
            // TODO analysis SyncProviderException
        }
    }

    public void testAcceptChanges_Delete() throws Exception {
        /*
         * Delete all the row. On the first and second row, only two columns
         * have value, all the others are NULL. When run on RI, deleteRow() will
         * go wrong and throw Exception. According to the spec, deleteRow() is
         * supposed to ok.
         */
        crset.beforeFirst();
        while (crset.next()) {
            crset.deleteRow();
        }
        if ("true".equals(System.getProperty("Testing Harmony"))) {
            crset.acceptChanges(conn);
        } else {
            try {
                crset.acceptChanges(conn);
            } catch (NullPointerException e) {
                // RI would throw NullPointerException when deleting a row in
                // which some columns' value are null
            }
        }
        // check DB
        rs = st.executeQuery("select * from USER_INFO");
        int rowCount = 0;
        while (rs.next()) {
            rowCount++;
        }
        if ("true".equals(System.getProperty("Testing Harmony"))) {
            assertEquals(0, rowCount);
        } else {
            assertEquals(4, rowCount);
        }
    }

    public void testAcceptChanges_DeleteException() throws Exception {
        /*
         * Delete a row which has been deleted from database
         */
        int result = st.executeUpdate("delete from USER_INFO where ID = 3");
        assertEquals(1, result);
        // move to the third row which doesn't exist in database
        assertTrue(crset.absolute(3));
        assertEquals(3, crset.getInt(1));
        crset.deleteRow();
        try {
            crset.acceptChanges(conn);
            fail("should throw SyncProviderException");
        } catch (SyncProviderException e) {
            // TODO analysis SyncProviderException
        }

        /*
         * Delete a row which has been updated in database
         */
        crset = newNoInitialInstance();
        rs = st.executeQuery("select * from USER_INFO");
        crset.populate(rs);
        result = st
                .executeUpdate("update USER_INFO set NAME = 'update44' where ID = 4");
        assertEquals(1, result);
        // move to the updated row
        crset.absolute(3);
        assertEquals(4, crset.getInt(1));
        assertEquals("test4", crset.getString(2));
        crset.deleteRow();
        try {
            crset.acceptChanges(conn);
            fail("should throw SyncProviderException");
        } catch (SyncProviderException e) {
            // TODO analysis SyncProviderException
        }
    }

    public void testAcceptChanges_Update() throws Exception {
        // update the first row
        assertTrue(crset.absolute(1));
        crset.updateInt(1, 11);
        crset.updateString(2, "test11");
        crset.updateRow();
        crset.acceptChanges(conn);
        // check DB
        rs = st.executeQuery("select * from USER_INFO where ID = 11");
        assertTrue(rs.next());
        assertEquals(11, rs.getInt(1));
        assertEquals("test11", rs.getString(2));

        // update the third row
        noInitialCrset = newNoInitialInstance();
        rs = st.executeQuery("select * from USER_INFO");
        noInitialCrset.populate(rs);
        assertTrue(noInitialCrset.absolute(1));
        noInitialCrset.updateInt(1, 111);
        noInitialCrset.updateString(2, "update111");
        noInitialCrset.updateRow();
        assertTrue(noInitialCrset.absolute(3));
        noInitialCrset.updateInt(1, 333);
        noInitialCrset.updateString(2, "update333");
        noInitialCrset.updateLong(3, 33333L);
        noInitialCrset.updateRow();
        noInitialCrset.acceptChanges(conn);
        // check DB
        rs = st.executeQuery("select * from USER_INFO where ID = 111");
        assertTrue(rs.next());
        assertEquals(111, rs.getInt(1));
        assertEquals("update111", rs.getString(2));
        rs = st.executeQuery("select * from USER_INFO where ID = 333");
        assertTrue(rs.next());
        assertEquals(333, rs.getInt(1));
        assertEquals("update333", rs.getString(2));
        assertEquals(33333L, rs.getLong(3));
    }

    public void testAcceptChanges_UpdateException() throws Exception {
        /*
         * Update a row which has been deleted from database
         */
        int result = st.executeUpdate("delete from USER_INFO where ID = 3");
        assertEquals(1, result);
        // move to the third row which doesn't exist in database
        assertTrue(crset.absolute(3));
        assertEquals(3, crset.getInt(1));
        crset.updateString(2, "update33");
        crset.updateRow();
        try {
            crset.acceptChanges(conn);
            fail("should throw SyncProviderException");
        } catch (SyncProviderException e) {
            // TODO analysis SyncProviderException
        }

        /*
         * Update a row which has been updated in database
         */
        crset = newNoInitialInstance();
        rs = st.executeQuery("select * from USER_INFO");
        crset.populate(rs);
        result = st
                .executeUpdate("update USER_INFO set NAME = 'update44' where ID = 4");
        assertEquals(1, result);
        // move to the updated row
        assertTrue(crset.absolute(3));
        assertEquals(4, crset.getInt(1));
        assertEquals("test4", crset.getString(2));
        crset.updateString(2, "change4");
        crset.updateRow();
        try {
            crset.acceptChanges(conn);
            fail("should throw SyncProviderException");
        } catch (SyncProviderException e) {
            // TODO analysis SyncProviderException
        }

        /*
         * Update a row in which one column's value is out of range
         */
        crset = newNoInitialInstance();
        rs = st.executeQuery("select * from USER_INFO");
        crset.populate(rs);
        assertEquals(3, crset.size());
        assertTrue(crset.absolute(3));
        assertEquals(4, crset.getInt(1));
        crset.updateString(2, "update4");
        crset.updateLong(3, 555555L);
        crset.updateInt(4, 200000); // 200000 exceeds the NUMERIC's range
        crset.updateBigDecimal(5, new BigDecimal(23));
        crset.updateFloat(8, 4.888F);
        crset.updateRow();
        try {
            crset.acceptChanges(conn);
            fail("should throw SyncProviderException");
        } catch (SyncProviderException e) {
            // TODO analysis SyncProviderException
        }
    }

    public class Listener implements RowSetListener, Cloneable {

        private String tag = null;

        public void cursorMoved(RowSetEvent theEvent) {
            tag = "cursorMoved";
        }

        public void rowChanged(RowSetEvent theEvent) {
            tag = "rowChanged";
        }

        public void rowSetChanged(RowSetEvent theEvent) {
            tag = "rowSetChanged";
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public Listener clone() throws CloneNotSupportedException {
            Listener listener = (Listener) super.clone();
            listener.tag = tag;
            return listener;
        }
    }
}
