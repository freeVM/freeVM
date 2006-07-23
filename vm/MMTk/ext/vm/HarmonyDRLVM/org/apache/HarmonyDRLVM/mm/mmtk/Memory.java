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

import org.mmtk.plan.Plan;
import org.mmtk.policy.ImmortalSpace;
import org.mmtk.utility.Constants;

import org.vmmagic.unboxed.*;
import org.vmmagic.pragma.*;


public class Memory extends org.mmtk.vm.Memory
  implements Constants, Uninterruptible {

    protected final Address getHeapStartConstant() { return Address.fromInt(0x100); } //BOOT_IMAGE_DATA_START
    protected final Address getHeapEndConstant() { return Address.fromInt(0xFFff0000); }  //MAXIMUM_MAPPABLE
    protected final Address getAvailableStartConstant() 
    { 
        System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.Memory.getAvailableStartConstant(), needs fixing now");
        return Address.fromInt(0); //BOOT_IMAGE_CODE_END
    }
    protected final Address getAvailableEndConstant() 
    { 
        System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.Memory.getAvailableEndConstant(), needs fixing now");      
        return Address.fromInt(0); //MAXIMUM_MAPPABLE;
    }
  protected final byte getLogBytesInAddressConstant() { return 2; }
  protected final byte getLogBytesInWordConstant() { return 2; }
  protected final byte getLogBytesInPageConstant() { return 12; }
  protected final byte getLogMinAlignmentConstant() { return 3;}
  protected final byte getMaxAlignmentShiftConstant() { return 3; } //wjw -- I dont have a clue
  protected final int getMaxBytesPaddingConstant() { return 8; } 
  protected final int getAlignmentValueConstant() { return 0x01020304;}
 

    public final ImmortalSpace getVMSpace() throws InterruptiblePragma
    {
        System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.Memory.globalPrepareVMSpace() needs fixing");
        return null;
    }

    public final void globalPrepareVMSpace() 
    {
        System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.Memory.globalPrepareVMSpace() needs fixing");
    }

    public final void collectorPrepareVMSpace() 
    {
        System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.Memory.collectorPrepareVMSpace() needs fixing");
    }

    public final void collectorReleaseVMSpace() 
    {
        System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.Memory.collectorReleaseVMSpace() needs fixing");
    }
    public final void globalReleaseVMSpace() 
    {
        System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.Memory.gobalReleaseVMSpace() needs fixing");
    }
    public final void setHeapRange(int id, Address start, Address end) 
    {
        System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.Memory.setHeapRange() needs fixing");
    }
 /**
   * Maps an area of virtual memory.
   *
   * @param start the address of the start of the area to be mapped
   * @param size the size, in bytes, of the area to be mapped
   * @return 0 if successful, otherwise the system errno
   */
  public final int mmap(Address start, int size) {
      System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.Memory.mmap() needs fixing");
      /*
    Address result = VM_Memory.mmap(start, Extent.fromIntZeroExtend(size),
                                       VM_Memory.PROT_READ | VM_Memory.PROT_WRITE | VM_Memory.PROT_EXEC, 
                                       VM_Memory.MAP_PRIVATE | VM_Memory.MAP_FIXED | VM_Memory.MAP_ANONYMOUS);
    if (result.EQ(start)) return 0;
    if (result.GT(Address.fromIntZeroExtend(127))) {
      VM.sysWrite("mmap with MAP_FIXED on ", start);
      VM.sysWriteln(" returned some other address", result);
      VM.sysFail("mmap with MAP_FIXED has unexpected behavior");
    }
    */
    return 99;
  }
  
  /**
   * Protects access to an area of virtual memory.
   *
   * @param start the address of the start of the area to be mapped
   * @param size the size, in bytes, of the area to be mapped
   * @return <code>true</code> if successful, otherwise
   * <code>false</code>
   */
  public final boolean mprotect(Address start, int size) {
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.Memory.mprotect() needs fixing");
    return false; //return VM_Memory.mprotect(start, Extent.fromIntZeroExtend(size), VM_Memory.PROT_NONE);
  }

  /**
   * Allows access to an area of virtual memory.
   *
   * @param start the address of the start of the area to be mapped
   * @param size the size, in bytes, of the area to be mapped
   * @return <code>true</code> if successful, otherwise
   * <code>false</code>
   */
  public final boolean munprotect(Address start, int size) {
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.Memory.mprotect() needs fixing");
    return false; //VM_Memory.mprotect(start, Extent.fromIntZeroExtend(size), VM_Memory.PROT_READ | VM_Memory.PROT_WRITE | VM_Memory.PROT_EXEC);
  }

  /**
   * Zero a region of memory.
   * @param start Start of address range (inclusive)
   * @param len Length in bytes of range to zero
   * Returned: nothing
   */
  public final void zero(Address start, Extent len) {
      byte zeroByte = 0;
      int numberOfBytes = len.toInt();
      for(int xx=0; xx < numberOfBytes; xx++) 
      {
        start.store(zeroByte);
        start.plus(1);
      }
  }

  /**
   * Zero a range of pages of memory.
   * @param start Start of address range (must be a page address)
   * @param len Length in bytes of range (must be multiple of page size)
   */
  public final void zeroPages(Address start, int len) {
      int zeroInt = 0;
      for(int xx=0; xx < len; len+=4) 
      {
          start.store(zeroInt);
          start.plus(4);
      }
  }

  /**
   * Logs the contents of an address and the surrounding memory to the
   * error output.
   *
   * @param start the address of the memory to be dumped
   * @param beforeBytes the number of bytes before the address to be
   * included
   * @param afterBytes the number of bytes after the address to be
   * included
   */
  public final void dumpMemory(Address start, int beforeBytes,
                                int afterBytes) {
      Address low = start.minus(beforeBytes);
      Address hi = start.plus(afterBytes);
      System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.Memory.dumpMemory() called -------------------------");
      while (low.NE(hi) ) 
      {
        byte b1 = low.loadByte();
        System.out.print(b1 + " ");
        low.plus(1);
      }
      System.out.println();
      System.out.println("--------------------------------------------dumpMemory finished");
  }

  /*
   * Utilities from the VM class
   */

  public final void sync() throws InlinePragma {
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.Memory.sync() was called"); 
  }

  public final void isync() throws InlinePragma {
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.Memory.isync() was called");
  }
}
