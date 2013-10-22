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

package org.microemu.android.device;

import org.microemu.device.impl.Font;

import android.graphics.Paint;

public class AndroidFont implements Font {

	static Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	public AndroidFont(String name, String style, int size) {
		// TODO Auto-generated constructor stub
	}

	public int charWidth(char ch) {
		return (int) paint.measureText(new char[] { ch }, 0, 1);
	}

	public int charsWidth(char[] ch, int offset, int length) {
		return (int) paint.measureText(ch, offset, length);
	}

	public int getBaselinePosition() {
		return -paint.getFontMetricsInt().ascent;
	}

	public int getHeight() {
		return paint.getFontMetricsInt(paint.getFontMetricsInt());
	}

	public int stringWidth(String str) {
		return (int) paint.measureText(str);
	}

}
