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

package javax.sound.midi;

public class SysexMessage extends MidiMessage {
    public static final int SPECIAL_SYSTEM_EXCLUSIVE = 247;

    public static final int SYSTEM_EXCLUSIVE = 240;

    public SysexMessage() {
        //TODO
        super(null);
    }

    protected SysexMessage(byte[] data) {
        super(data);
    }

    public Object clone() {
        //TODO
        return null;
    }

    public byte[] getData() {
        //TODO
        return null;
    }

    public void setMessage(byte[] data, int length) throws InvalidMidiDataException {
        //TODO
    }

    public void setMessage(int status, byte[] data, int length) throws InvalidMidiDataException {
        //TODO
    }
}
