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

import java.util.*;

class Finx {
  static Finx ft;
  static int finalCount = 0;
  static int cmd = 0;

  public static void main(String args[])
  {
	  //cmd = 0		no finalizable object created, main() does cpu intensive task forever

	  //cmd = 1		a bunch of object needing finalization are shoved onto finalize queue
	  //			also, each finalizer runs only a short time

	  //cmd = 2		same as cmd==1 except each finalizer runs cpu intensive task forever

	  //			you can process viewer to see what the JVM is doing w/ threads for cmd = 0, 1 and 2

	  if (args.length != 1) {
		  System.out.println("you need to supply one input arg, read the source to figure out what the arg does");
		  return;
	  }
	  cmd = Integer.valueOf(args[0]);

	  if (cmd > 0)  // cause a bunch of objects to need finalization
	  {
		  // create 100K objects that will need finalization
		  for (int kk = 0; kk < 100 * 1000; kk++)
			  ft = new Finx();

		  //push a bunch (maybe not all) of the above objects onto the finalization queue
		  Object[] oa1 = new Object[1024];
		  for (int xx = 0; xx < 1024; xx++) {
			  Object[] oa2 = new Object[1024];
			  oa1[1] = oa2;
			  oa1 = oa2;
		  }
	  }
	  // this endless loop should keep the cpu 100% busy in user-level code
	  int kk = 0;
	  while(true) {
		  int ss = cpuIntensiveWorkload();
		  System.out.println("##########################################main(), loop count = " + kk++);
	  }
  }

	static int cpuIntensiveWorkload()
	{
		int yy, zz = 23;
		for (int xx = 0; xx < 1000000; xx++) {
			yy = xx * 117;
			zz = yy + xx;
		}
		return zz;
	}

	protected void finalize()
	{
		finalCount++;
		int kk = 0;
		while (true) {
			int qq = cpuIntensiveWorkload();
			System.out.println("finalize()called for object number " + finalCount + " loop count = " + kk++);
			if (cmd == 1) break;  // cmd == 1 causes a short running finalizer
		}
	}
}


 