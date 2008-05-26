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

package org.microemu.app.util;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.microemu.MIDletBridge;
import org.microemu.MIDletContext;
import org.microemu.log.Logger;
import org.microemu.util.ThreadUtils;

/**
 * MIDletContext is used to hold keys to running Threads created by  MIDlet  
 * 
 * @author vlads
 */
public class MIDletThread extends Thread {

	public static int graceTerminationPeriod = 5000;
	
	private static final String THREAD_NAME_PREFIX = "MIDletThread-";
	
	private static boolean terminator = false;
	
	private static Map midlets = new WeakHashMap();
	
    private static int threadInitNumber;
    
    private String callLocation;
    
    private static synchronized int nextThreadNum() {
    	return threadInitNumber++;
    }
    
	public MIDletThread() {
		super(THREAD_NAME_PREFIX + nextThreadNum());
		register(this);
	}
	
	public MIDletThread(Runnable target) {
		super(target, THREAD_NAME_PREFIX + nextThreadNum());
		register(this);
	}
	
	public MIDletThread(Runnable target, String name) {
		super(target, THREAD_NAME_PREFIX + name);
		register(this);
	}
	
	public MIDletThread(String name) {
		super(THREAD_NAME_PREFIX + name);
		register(this);
	}
	
	private static void register(MIDletThread thread) {
		MIDletContext midletContext = MIDletBridge.getMIDletContext();
		if (midletContext == null) {
			Logger.error("Creating thread with no MIDlet context", new Throwable());
			return;
		}
		thread.callLocation  = ThreadUtils.getCallLocation(MIDletThread.class.getName());
		Map threads = (Map)midlets.get(midletContext);
		if (threads == null) {
			threads = new WeakHashMap();
			midlets.put(midletContext, threads);
		}
		threads.put(thread, midletContext);
	}
	
	//TODO overrite run() in user Threads using ASM
	public void run() {
		 try {
			super.run();
		} catch (Throwable e) {
			Logger.debug("MIDletThread throw", e);
		}
		//Logger.debug("thread ends, created from " + callLocation);	
	 }
	
	/**
	 * Terminate all Threads created by MIDlet
	 * @param previousMidletAccess
	 */
	public static void contextDestroyed(final MIDletContext midletContext) {
		if (midletContext == null) {
			return;
		}
		final Map threads = (Map)midlets.remove(midletContext);
		if ((threads != null) && (threads.size() != 0)) {
			terminator = true;
			Thread terminator = new Thread("MIDletThreadsTerminator") {
				public void run() {
					terminateThreads(threads);
				}
			};
			terminator.start();
		}
		MIDletTimer.contextDestroyed(midletContext);
	}
	
	public static boolean hasRunningThreads(MIDletContext midletContext) {
		//return (midlets.get(midletContext) != null);
		return terminator;
	}
	
	private static void terminateThreads(Map threads) {
		long endTime = System.currentTimeMillis() + graceTerminationPeriod;
		for (Iterator iter = threads.keySet().iterator(); iter.hasNext();) {
			Object o = iter.next();
			if (o == null) {
				continue;
			}
			if (o instanceof MIDletThread) {
				MIDletThread t = (MIDletThread) o;
				if (t.isAlive()) {
					Logger.info("wait thread [" + t.getName() + "] end");
					while ((endTime > System.currentTimeMillis()) && (t.isAlive())) {
						try {
							t.join(700);
						} catch (InterruptedException e) {
							break;
						}
					}
					if (t.isAlive()) {
						Logger.warn("MIDlet thread [" + t.getName() + "] still running" + ThreadUtils.getTreadStackTrace(t));
						if (t.callLocation != null) {
							Logger.info("this thread [" + t.getName() + "] was created from " + t.callLocation);
						}
						t.interrupt();
					}
				}
			} else {
				Logger.debug("unrecognized Object [" + o.getClass().getName() + "]");
			}
		};
		Logger.debug("all "+ threads.size() + " thread(s) finished");
		terminator = false;
	}

}
