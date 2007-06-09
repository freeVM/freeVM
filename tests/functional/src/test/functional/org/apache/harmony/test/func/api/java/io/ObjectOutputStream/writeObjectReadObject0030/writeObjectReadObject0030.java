/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/** 
 */  
package org.apache.harmony.test.func.api.java.io.ObjectOutputStream.writeObjectReadObject0030;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.harmony.test.func.api.java.io.ObjectOutputStream.share.SerializationTestFramework;

class C implements Serializable {
    public String s = "s";

    public boolean writeReplaceCalled = false;

    C(String s) {
        this.s = s;
    }

    private Object writeReplace() {
        writeReplaceCalled = true;
        return new C(s.toUpperCase());
    }
}

public class writeObjectReadObject0030 extends SerializationTestFramework {
    public static void main(String[] args) {
        System.exit(new writeObjectReadObject0030().test(args));
    }

    protected int testIn(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {
        waitAtBarrier();
        Object o = ois.readObject();

        if (!(o instanceof C)) {
            return fail("expected another value");
        }

        C c = (C) o;

        if (c.s.equals("AAA")) {
            return pass();
        }

        return fail("wrong values restored");

    }

    protected int testOut(ObjectOutputStream oos) throws IOException {

        C c = new C("aaa");

        if (c.writeReplaceCalled) {
            return fail("wrong value of flag");
        }

        waitAtBarrier();
        oos.writeObject(c);

        if (!c.writeReplaceCalled) {
            return fail("flag has not been set");
        }

        return pass();
    }
}

