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
 * @author Salikh Zakirov
 * @version $Revision: 1.1.28.3 $
 *
 * $Id: List.java,v 1.1.28.3 2006/03/28 14:46:27 aycherny Exp $
 */
package gc;

import java.util.LinkedList;

/**
 * @keyword 
 */
public class List {
    public static void main(String[] args) {
        int n = 0;
        int threshold = 128;
        LinkedList x = new LinkedList();
        while (n < 1048576) {
            x.add(new Object());
            n++;
            if (n > threshold) {
                trace(".");
                System.gc();
                threshold *= 2;
            }
        }
        System.out.println("PASSED");
    }

    public static void trace(Object o) {
        System.err.print(o);
        System.err.flush();
    }
}
