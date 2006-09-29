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


package org.apache.HarmonyDRLVM.mm.mmtk;

import org.vmmagic.pragma.*;

public class Assert extends org.mmtk.vm.Assert implements Uninterruptible {
  
  protected final boolean getVerifyAssertionsConstant() { return true;}

  /**
   * This method should be called whenever an error is encountered.
   *
   * @param str A string describing the error condition.
   */
  public final void error(String str) {
    System.out.println("******org.apache.HarmonyDRLVM.mm.mmtk.Assert.error(): " + str);
    str = null;
    str.notifyAll();  // this should cause a stack trace and exit
  }

  /**
   * Logs a message and traceback, then exits.
   *
   * @param message the string to log
   */
  public final void fail(String message) { 
      System.out.println("******org.apache.HarmonyDRLVM.mm.mmtk.Assert.fail(): " + message);
      message = null;
      message.notifyAll();  // this should cause a stack trace and exit 
  }

  public final void exit(int rc) throws UninterruptiblePragma {
      System.out.println("******org.apache.HarmonyDRLVM.mm.mmtk.Assert.exit(): " + rc);
      Object obj = new Object();
      obj = null;
      obj.notifyAll();  // this should cause a stack trace and exit
  }

  /**
   * Checks that the given condition is true.  If it is not, this
   * method does a traceback and exits.
   *
   * @param cond the condition to be checked
   */
    public final void _assert(boolean cond) throws InlinePragma 
    {
        if (cond == false) 
        {
            System.out.println("****** org.apache.HarmonyDRLVM.mm.mmtk.Assert._assert()");
            Object obj = new Object();
            obj = null;
            obj.notifyAll();
        }
    }

  public final void _assert(boolean cond, String s) throws InlinePragma {
      System.out.println("******org.apache.HarmonyDRLVM.mm.mmtk.Assert._assert(): " + s);
      s.notifyAll();  // this should cause a stack trace and exit
  }

  public final void dumpStack() {
      System.out.println("******org.apache.HarmonyDRLVM.mm.mmtk.Assert.dumpStack(): ");
      Object obj = new Object();
      obj = null;
      obj.notifyAll();  // this should cause a stack trace and exit
  }

  /**
   * Throw an out of memory exception.  If the context is one where
   * we're already dealing with a problem, first request some
   * emergency heap space.
   */
  public final void failWithOutOfMemoryError()
    throws LogicallyUninterruptiblePragma, NoInlinePragma {
    failWithOutOfMemoryErrorStatic();
  }

  /**
   * Throw an out of memory exception.  If the context is one where
   * we're already dealing with a problem, first request some
   * emergency heap space.
   */
  public static final void failWithOutOfMemoryErrorStatic()
    throws LogicallyUninterruptiblePragma, NoInlinePragma {
    throw new OutOfMemoryError();
  }

  /**
   * Checks if the virtual machine is running.
   * @return <code>true</code> if the virtual machine is running
   */
  public final boolean runningVM() { return true; }

}
