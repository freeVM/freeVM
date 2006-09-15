/*
 *  Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package javax.sound.midi.spi;

import javax.sound.midi.MidiDevice;

public abstract class MidiDeviceProvider {

    public abstract MidiDevice getDevice(MidiDevice.Info info);

    public abstract MidiDevice.Info[] getDeviceInfo();

    public boolean isDeviceSupported(MidiDevice.Info info) {
        MidiDevice.Info[] devices = getDeviceInfo();
        for (int i = 0; i < devices.length; i++) {
            if (info.equals(devices[i])) {
                return true;
            }
        }
        return false;
    }
}
