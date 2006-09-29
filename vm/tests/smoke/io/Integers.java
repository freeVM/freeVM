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
 * @author Alexei Fedotov
 * @version $Revision: 1.4.8.1.4.3 $
 */
package io;

import java.io.*;

/**
 * @keyword XXX_bug_2509
 */
import java.util.*;

class Spaghetti implements Serializable {
    public static final long serialVersionUID = 0L;
    Integer s = new Integer(1);
    Spaghetti s1, s2;
    {
        s1 = s2 = this;
    }
}


public class Integers {
    private static final int SPAGHETTI_NUM = 1000;
    private static final Random rnd = new Random();
    

    public static void main(String[] args) throws Exception {
       if("-out".equals(args[0])) {
          ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream
(args[1]));



        Spaghetti[] sa = new Spaghetti[SPAGHETTI_NUM];

        //fill array with empty objects
        for (int i = 0; i < SPAGHETTI_NUM; i++) {
            sa[i] = new Spaghetti();
        }

        //mess it up
        for (int i = 0; i < SPAGHETTI_NUM; i++) {
            sa[i].s1 = sa[rnd.nextInt(SPAGHETTI_NUM)];
            sa[i].s2 = sa[rnd.nextInt(SPAGHETTI_NUM)];
        }

        oos.writeObject(sa);


        oos.close();
       } 
       if("-in".equals(args[0])) {
          ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args
[1]));
          ois.readObject();
          ois.close();
       } 
       System.out.println("PASSED");
    }
}

