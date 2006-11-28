/*
 * Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @author Aleksey V. Yantsen
 * @version $Revision: 1.2 $
 */

/**
 * Created on 11.29.2004
 */
package org.apache.harmony.jpda.tests.framework.jdwp;

/**
 * This class provides description of event request. 
 */
public class Event {

    public byte eventKind;
    public byte suspendPolicy;

    /** List of event modifiers. */
    public EventMod[] mods;
    public int modifiers;

    /**
     * Creates new instance with empty data.
     */
    public Event() {
        eventKind = 0;
        suspendPolicy = 0;
        modifiers = -1;
        mods = null;
    }

    /**
     * Create new instance with specified data.
     */
    public Event(byte eventKind, byte suspendPolicy, EventMod[] mods) {
        this.eventKind = eventKind;
        this.suspendPolicy = suspendPolicy;
        this.modifiers = mods.length;
        this.mods = mods;
    }
}
