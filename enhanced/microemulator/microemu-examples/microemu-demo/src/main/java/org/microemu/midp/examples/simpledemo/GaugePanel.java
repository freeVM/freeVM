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

package org.microemu.midp.examples.simpledemo;

import javax.microedition.lcdui.Gauge;

public class GaugePanel extends BaseExamplesForm implements HasRunnable {

	private boolean cancel = false;

	private Gauge noninteractive = new Gauge("Noninteractive", false, 25, 0);

	private Runnable timerTask = new Runnable() {

		public void run() {
			while (!cancel) {
				if (isShown()) {
					int value = noninteractive.getValue();

					if (noninteractive.getValue() >= 25) {
						noninteractive.setValue(0);
					} else {
						noninteractive.setValue(++value);
					}
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException ex) {
					break;
				}
			}
		}
	};

	public GaugePanel() {
		super("Gauge");
		append(new Gauge("Interactive", true, 25, 0));
		append(noninteractive);
	}

	public void startRunnable() {
		cancel = false;
		Thread thread = new Thread(timerTask, "GaugePanelThread");
		thread.start();
	}

	public void stopRunnable() {
		cancel = true;
	}

}
