/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.microemu.android.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.rms.RecordListener;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

import org.microemu.MicroEmulator;
import org.microemu.RecordStoreManager;
import org.microemu.app.launcher.Launcher;
import org.microemu.log.Logger;
import org.microemu.util.RecordStoreImpl;

import android.content.Context;

public class AndroidRecordStoreManager implements RecordStoreManager {

	private final static String RECORD_STORE_SUFFIX = ".rs";
	
	private Context context;

	private Launcher launcher;

	private Hashtable testOpenRecordStores = new Hashtable();

	private RecordListener recordListener = null;

// TODO move to listRecordStores
/*	private FilenameFilter filter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			if (name.endsWith(RECORD_STORE_SUFFIX)) {
				return true;
			} else {
				return false;
			}
		}
	};*/
	
	public AndroidRecordStoreManager(Context context) {
		this.context = context;
	}

	public void init(MicroEmulator emulator) {
		this.launcher = emulator.getLauncher();
	}

	public String getName() {
		return "Android record store";
	}

	private String getSuiteName() {
		return launcher.getSuiteName();
	}

	public void deleteRecordStore(final String recordStoreName) throws RecordStoreNotFoundException,
			RecordStoreException {
		String storeFileName = getSuiteName() + "." + recordStoreName + RECORD_STORE_SUFFIX;

		RecordStoreImpl recordStoreImpl = (RecordStoreImpl) testOpenRecordStores.get(storeFileName);
		if (recordStoreImpl != null && recordStoreImpl.isOpen()) {
			throw new RecordStoreException();
		}

		try {
			recordStoreImpl = loadFromDisk(storeFileName);
		} catch (FileNotFoundException ex) {
			throw new RecordStoreNotFoundException(recordStoreName);
		}

		context.deleteFile(storeFileName);
		fireRecordStoreListener(RecordListener.RECORDSTORE_DELETE, recordStoreName);
	}

	public RecordStore openRecordStore(String recordStoreName, boolean createIfNecessary) throws RecordStoreException {
		String storeFileName = getSuiteName() + "." + recordStoreName + RECORD_STORE_SUFFIX;

		RecordStoreImpl recordStoreImpl;
		try {
			recordStoreImpl = loadFromDisk(storeFileName);
		} catch (FileNotFoundException e) {
			if (!createIfNecessary) {
				throw new RecordStoreNotFoundException(recordStoreName);
			}
			recordStoreImpl = new RecordStoreImpl(this, recordStoreName);
			saveToDisk(getSuiteName() + "." + recordStoreName + RECORD_STORE_SUFFIX, recordStoreImpl);
		}
		recordStoreImpl.setOpen(true);
		if (recordListener != null) {
			recordStoreImpl.addRecordListener(recordListener);
		}

		testOpenRecordStores.put(storeFileName, recordStoreImpl);

		fireRecordStoreListener(RecordListener.RECORDSTORE_OPEN, recordStoreName);

		return recordStoreImpl;
	}

	public String[] listRecordStores() {
		String[] result= context.fileList();
		if (result != null) {
			if (result.length == 0) {
				result = null;
			} else {
				for (int i = 0; i < result.length; i++) {
					result[i] = result[i].substring(0, result[i].length() - RECORD_STORE_SUFFIX.length());
				}
			}
		}
		return result;
	}

	public void saveChanges(RecordStoreImpl recordStoreImpl) 
			throws RecordStoreNotOpenException, RecordStoreException {
		saveToDisk(getSuiteName() + "." + recordStoreImpl.getName() + RECORD_STORE_SUFFIX, recordStoreImpl);
	}

	public void init() {
	}

	public void deleteStores() {
		String[] stores = listRecordStores();
		for (int i = 0; i < stores.length; i++) {
			String store = stores[i];
			try {
				deleteRecordStore(store);
			} catch (RecordStoreException e) {
				Logger.debug("deleteRecordStore", e);
			}
		}
	}

	private RecordStoreImpl loadFromDisk(String recordStoreFileName) throws FileNotFoundException {
		RecordStoreImpl store = null;
		try {
			DataInputStream dis = new DataInputStream(context.openFileInput(recordStoreFileName));
			store = new RecordStoreImpl(this, dis);
			dis.close();
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			Logger.error("RecordStore.loadFromDisk: ERROR reading " + recordStoreFileName, e);
		}
		return store;
	}

	private void saveToDisk(String recordStoreFileName, final RecordStoreImpl recordStore)
			throws RecordStoreException {
		try {
			DataOutputStream dos = new DataOutputStream(context.openFileOutput(recordStoreFileName, Context.MODE_PRIVATE));
			recordStore.write(dos);
			dos.close();
		} catch (IOException e) {
			Logger.error("RecordStore.saveToDisk: ERROR writting object to " + recordStoreFileName, e);
			throw new RecordStoreException(e.getMessage());
		}
	}

	public int getSizeAvailable(RecordStoreImpl recordStoreImpl) {
		// FIXME should return free space on device
		return 1024 * 1024;
	}

	public void setRecordListener(RecordListener recordListener) {
		this.recordListener = recordListener;
	}

	public void fireRecordStoreListener(int type, String recordStoreName) {
		if (recordListener != null) {
			recordListener.recordStoreEvent(type, System.currentTimeMillis(), recordStoreName);
		}
	}
}
