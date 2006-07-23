/*
 *  Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
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

/*
 * (C) Copyright Department of Computer Science,
 * Australian National University. 2004
 *
 * (C) Copyright IBM Corp. 2001, 2003
 */

package org.apache.HarmonyDRLVM.mm.mmtk;

import org.mmtk.utility.Constants;

import org.vmmagic.unboxed.*;
import org.vmmagic.pragma.*;

public final class Statistics extends org.mmtk.vm.Statistics implements Constants, Uninterruptible {
  /**
   * Returns the number of collections that have occured.
   *
   * @return The number of collections that have occured.
   */
  public final int getCollectionCount()
    throws UninterruptiblePragma {
    //return MM_Interface.getCollectionCount();
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.Statistics.getCollectionCount() was called");
    return 0;
  }

  /**
   * Read cycle counter
   */
  public final long cycles() {
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.Statistics.cycles() was called");
    return 0;
  }

  /**
   * Convert cycles to milliseconds
   */
  public final double cyclesToMillis(long c) {
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.Statistics.cyclesToMillis() was called -- does this api make sense??");
    return 0;
  }

  /**
   * Convert cycles to seconds
   */
  public final double cyclesToSecs(long c) {
      System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.Statistics.cyclesToSecs() was called -- does this api make sense??");
      return 0;
  }

  /**
   * Convert milliseconds to cycles
   */
  public final long millisToCycles(double t) {
      System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.Statistics.() was called -- does this api make sense??");
      return 0;
  }

  /**
   * Convert seconds to cycles
   */
  public final long secsToCycles(double t) {
      System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.Statistics.millisToCycles() was called -- does this api make sense??");
      return 0;
  }
}
