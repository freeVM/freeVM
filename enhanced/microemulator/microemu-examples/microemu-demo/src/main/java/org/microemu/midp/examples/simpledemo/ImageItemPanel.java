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

import java.io.IOException;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;

public class ImageItemPanel extends BaseExamplesForm {

	public ImageItemPanel() {
		super("ImageItem");

		try {
			Image image = Image.createImage("/org/microemu/midp/examples/simpledemo/image.png");
			append(new ImageItem("Default Layout", image, ImageItem.LAYOUT_DEFAULT, null));
			append(new ImageItem("Left Layout", image, ImageItem.LAYOUT_LEFT, null));
			append(new ImageItem("Center Layout", image, ImageItem.LAYOUT_CENTER, null));
			append(new ImageItem("Right Layout", image, ImageItem.LAYOUT_RIGHT, null));
		} catch (IOException ex) {
			append("Cannot load images");
		}

	}

}
