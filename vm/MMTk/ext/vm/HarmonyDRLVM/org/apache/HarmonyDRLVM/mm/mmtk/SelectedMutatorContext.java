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
 * Australian National University. 2005
 */

package org.apache.HarmonyDRLVM.mm.mmtk;

import org.mmtk.plan.nogc.*;
import org.mmtk.plan.marksweep.*;
import org.vmmagic.pragma.*;
import org.mmtk.plan.MutatorContext;

public final class SelectedMutatorContext 
  implements Uninterruptible {

  //public static final MutatorContext singleton = new NoGCMutator();
  public static final MutatorContext singleton = new MSMutator();
  
  /**
   * Return the instance of the SelectedPlan
   */
  public static final MutatorContext get() throws InlinePragma 
  {
    return singleton;
  }
}
