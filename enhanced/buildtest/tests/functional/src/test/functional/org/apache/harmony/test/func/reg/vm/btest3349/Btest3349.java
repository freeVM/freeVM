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
package org.apache.harmony.test.func.reg.vm.btest3349;
import org.apache.harmony.share.Test;

public class Btest3349 extends Test {
    static Test3349 t = null;

    public static void main(String[] args) {
        System.exit(new Btest3349().test());
    }

    public int test() {
        try {
            t = new Test3349();
            return fail("Expected NoClassDefFoundError ");
        } catch (NoClassDefFoundError e) {
            return pass();
        }
    }
}

/** Auxiliary class to be moved to tmp directory */
class Test3349 {}
