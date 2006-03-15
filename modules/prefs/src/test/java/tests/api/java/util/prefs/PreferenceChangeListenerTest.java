/* Copyright 2005 The Apache Software Foundation or its licensors, as applicable
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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.api.java.util.prefs;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import junit.framework.TestCase;

/**
 * 
 */
public class PreferenceChangeListenerTest extends TestCase {

	PreferenceChangeListener l;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		l = new PreferenceChangeListenerImpl();
	}

	public void testPreferenceChange() {
		l.preferenceChange(new PreferenceChangeEvent(Preferences.userRoot(),
				"", ""));
	}

	public static class PreferenceChangeListenerImpl implements
			PreferenceChangeListener {
		public void preferenceChange(PreferenceChangeEvent pce) {
		}

	}

}
