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

import org.mmtk.utility.scan.MMType;
import org.mmtk.utility.Constants;
import org.mmtk.utility.alloc.Allocator;

import org.vmmagic.unboxed.*;
import org.vmmagic.pragma.*;

/**
 * $Id: ObjectModel.java,v 1.6 2006/06/19 06:08:15 steveb-oss Exp $ 
 *
 * @author Steve Blackburn
 * @author Perry Cheng
 *
 * @version $Revision: 1.6 $
 * @date $Date: 2006/06/19 06:08:15 $
 */
public final class ObjectModel extends org.mmtk.vm.ObjectModel implements Constants, Uninterruptible {
  /**
   * Copy an object using a plan's allocCopy to get space and install
   * the forwarding pointer.  On entry, <code>from</code> must have
   * been reserved for copying by the caller.  This method calls the
   * plan's <code>getStatusForCopy()</code> method to establish a new
   * status word for the copied object and <code>postCopy()</code> to
   * allow the plan to perform any post copy actions.
   *
   * @param from the address of the object to be copied
   * @return the address of the new object
   */
  public ObjectReference copy(ObjectReference from, int allocator)
    throws InlinePragma {

/*
wjw --- need to call native method to do something like the below to get the object size
unsigned int
get_object_size_bytes(Partial_Reveal_Object *p_obj)
{
    bool arrayp = is_array (p_obj);
    unsigned int sz;
    if (arrayp) {
        sz = vm_vector_size(p_obj->vt()->get_gcvt()->gc_clss, vector_get_length((Vector_Handle)p_obj));
        return sz; 
    } else {
            return p_obj->vt()->get_gcvt()->gc_allocated_size;
    }
}

 */

/*
    Object[] tib = VM_ObjectModel.getTIB(from);
    VM_Type type = VM_Magic.objectAsType(tib[TIB_TYPE_INDEX]);
    
    if (type.isClassType())
      return copyScalar(from, tib, type.asClass(), allocator);
    else
      return copyArray(from, tib, type.asArray(), allocator);
  */
      System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.copy was called");
      return from;  //wjw -- keep the compiler happy for now
  }

    /*
  private ObjectReference copyScalar(ObjectReference from, Object[] tib,
                                       VM_Class type, int allocator)
    throws InlinePragma {
    int bytes = VM_ObjectModel.bytesRequiredWhenCopied(from, type);
    int align = VM_ObjectModel.getAlignment(type, from);
    int offset = VM_ObjectModel.getOffsetForAlignment(type, from);
    SelectedCollectorContext plan = SelectedCollectorContext.get();
    allocator = plan.copyCheckAllocator(from, bytes, align, allocator);
    Address region = MM_Interface.allocateSpace(plan, bytes, align, offset,
                                                allocator, from);
    Object toObj = VM_ObjectModel.moveObject(region, from, bytes, false, type);
    ObjectReference to = ObjectReference.fromObject(toObj);
    plan.postCopy(to, ObjectReference.fromObject(tib), bytes, allocator);
    MMType mmType = (MMType) type.getMMType();
    mmType.profileCopy(bytes);
    return to;
  }

  private ObjectReference copyArray(ObjectReference from, Object[] tib,
                                      VM_Array type, int allocator)
    throws InlinePragma {
    int elements = VM_Magic.getArrayLength(from);
    int bytes = VM_ObjectModel.bytesRequiredWhenCopied(from, type, elements);
    int align = VM_ObjectModel.getAlignment(type, from);
    int offset = VM_ObjectModel.getOffsetForAlignment(type, from);
    SelectedCollectorContext plan = SelectedCollectorContext.get();
    allocator = plan.copyCheckAllocator(from, bytes, align, allocator);
    Address region = MM_Interface.allocateSpace(plan, bytes, align, offset,
                                                allocator, from);
    Object toObj = VM_ObjectModel.moveObject(region, from, bytes, false, type);
    ObjectReference to = ObjectReference.fromObject(toObj);
    plan.postCopy(to, ObjectReference.fromObject(tib), bytes, allocator);
    if (type == VM_Type.CodeArrayType) {
      // sync all moved code arrays to get icache and dcache in sync
      // immediately.
      int dataSize = bytes - VM_ObjectModel.computeHeaderSize(VM_Magic.getObjectType(toObj));
      VM_Memory.sync(to.toAddress(), dataSize);
    }
    MMType mmType = (MMType) type.getMMType();
    mmType.profileCopy(bytes);
    return to;
  }
 */

  /**
   * Copy an object to be pointer to by the to address. This is required 
   * for delayed-copy collectors such as compacting collectors. During the 
   * collection, MMTk reserves a region in the heap for an object as per
   * requirements found from ObjectModel and then asks ObjectModel to 
   * determine what the object's reference will be post-copy.
   * 
   * @param from the address of the object to be copied
   * @param to The target location.
   * @param region The start (or an address less than) the region that was reserved for this object.
   * @return Address The address past the end of the copied object
   */
  public Address copyTo(ObjectReference from, ObjectReference to, Address region)
    throws InlinePragma {
/*
    Object[] tib = VM_ObjectModel.getTIB(from);
    VM_Type type = VM_Magic.objectAsType(tib[TIB_TYPE_INDEX]);
    int bytes;
    
    boolean copy = (from != to);
    
    if (copy) {
      if (type.isClassType()) {
        VM_Class classType = type.asClass();
        bytes = VM_ObjectModel.bytesRequiredWhenCopied(from, classType);
        VM_ObjectModel.moveObject(from, to, bytes, false, classType);
      } else {
      VM_Array arrayType = type.asArray();
        int elements = VM_Magic.getArrayLength(from);
        bytes = VM_ObjectModel.bytesRequiredWhenCopied(from, arrayType, elements);
        VM_ObjectModel.moveObject(from, to, bytes, false, arrayType);
      }
    } else {
      bytes = getCurrentSize(to);
    }
    
    Address start = VM_ObjectModel.objectStartRef(to);
    Allocator.fillAlignmentGap(region, start);
    
    return start.plus(bytes);
*/
      System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.copyTo() was called");
      return Address.fromInt(0);
  }

  /**
   * Return the reference that an object will be refered to after it is copied
   * to the specified region. Used in delayed-copy collectors such as compacting
   * collectors.
   *
   * @param from The object to be copied.
   * @param to The region to be copied to.
   * @return The resulting reference.
   */
  public ObjectReference getReferenceWhenCopiedTo(ObjectReference from, Address to) {
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.getReferenceWhenCopiedTo() was called");
    return from;  // keep the compiler happy -- xObjectReference.fromObject(VM_ObjectModel.getReferenceWhenCopiedTo(from, to));
  }
  
  /**
   * Gets a pointer to the address just past the end of the object.
   * 
   * @param object The objecty.
   */
  public Address getObjectEndAddress(ObjectReference object) {
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.getObjectEndAddress() was called");
    return Address.fromInt(0);  // keep the compiler happy
  }
  
  /**
   * Return the size required to copy an object
   *
   * @param object The object whose size is to be queried
   * @return The size required to copy <code>obj</code>
   */
  public int getSizeWhenCopied(ObjectReference object) {
    //need to use drlvm's get_object_size_bytes()
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.getSizeWhenCopied() was called");
    return 0;  // VM_ObjectModel.bytesRequiredWhenCopied(object);
  }
  
  /**
   * Return the alignment requirement for a copy of this object
   *
   * @param object The object whose size is to be queried
   * @return The alignment required for a copy of <code>obj</code>
   */
  public int getAlignWhenCopied(ObjectReference object) {

/*
    Object[] tib = VM_ObjectModel.getTIB(object);
    VM_Type type = VM_Magic.objectAsType(tib[TIB_TYPE_INDEX]);
    if (type.isArrayType()) {
      return VM_ObjectModel.getAlignment(type.asArray(), object);
    } else {
      return VM_ObjectModel.getAlignment(type.asClass(), object);
    }
*/
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.getAlignWhenCopied() was called");
    return 0;
  }
  
  /**
   * Return the alignment offset requirements for a copy of this object
   *
   * @param object The object whose size is to be queried
   * @return The alignment offset required for a copy of <code>obj</code>
   */
  public int getAlignOffsetWhenCopied(ObjectReference object) {
/*
    Object[] tib = VM_ObjectModel.getTIB(object);
    VM_Type type = VM_Magic.objectAsType(tib[TIB_TYPE_INDEX]);
    if (type.isArrayType()) {
      return VM_ObjectModel.getOffsetForAlignment(type.asArray(), object);
    } else {
      return VM_ObjectModel.getOffsetForAlignment(type.asClass(), object);
    }
    */
      System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.getAlignOffsetWhenCopied() was called");
      return 0;
  }
  
  /**
   * Return the size used by an object
   *
   * @param object The object whose size is to be queried
   * @return The size of <code>obj</code>
   */
  public int getCurrentSize(ObjectReference object) {
      System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.getCurrentSize() was called");
      return 0;
      // return VM_ObjectModel.bytesUsed(object);
  }

  /**
   * Return the next object in the heap under contiguous allocation
   */
  public ObjectReference getNextObject(ObjectReference object) {
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.getNextObject() was called");
    return ObjectReference.fromObject(null);
  }

  /**
   * Return an object reference from knowledge of the low order word
   */
  public ObjectReference getObjectFromStartAddress(Address start) {
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.getObjectFromStartAddress() was called");
    return ObjectReference.fromObject(null);
  }
  
  /**
   * Get the type descriptor for an object.
   *
   * @param ref address of the object
   * @return byte array with the type descriptor
   */
  public byte [] getTypeDescriptor(ObjectReference ref) {
    //VM_Atom descriptor = VM_Magic.getObjectType(ref).getDescriptor();
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.getTypeDescriptor() was called");
    return new byte[10]; // descriptor.toByteArray();
  }

  public int getArrayLength(ObjectReference object) 
    throws InlinePragma {
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.getArrayLength() was called");
    //return VM_Magic.getArrayLength(object.toObject());
    return 0;
  }
  /**
   * Tests a bit available for memory manager use in an object.
   *
   * @param object the address of the object
   * @param idx the index of the bit
   */
  public boolean testAvailableBit(ObjectReference object, int idx) {
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.testAvailableBit() was called");
    //return VM_ObjectModel.testAvailableBit(object.toObject(), idx);
    return false;
  }

  /**
   * Sets a bit available for memory manager use in an object.
   *
   * @param object the address of the object
   * @param idx the index of the bit
   * @param flag <code>true</code> to set the bit to 1,
   * <code>false</code> to set it to 0
   */
  public void setAvailableBit(ObjectReference object, int idx,
                                     boolean flag) {
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.setAvailableBit() was called");
    //VM_ObjectModel.setAvailableBit(object.toObject(), idx, flag);
    return;
  }

  /**
   * Attempts to set the bits available for memory manager use in an
   * object.  The attempt will only be successful if the current value
   * of the bits matches <code>oldVal</code>.  The comparison with the
   * current value and setting are atomic with respect to other
   * allocators.
   *
   * @param object the address of the object
   * @param oldVal the required current value of the bits
   * @param newVal the desired new value of the bits
   * @return <code>true</code> if the bits were set,
   * <code>false</code> otherwise
   */
  public boolean attemptAvailableBits(ObjectReference object,
                                             Word oldVal, Word newVal) {
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.attemptAvailableBits() was called");
    //return VM_ObjectModel.attemptAvailableBits(object.toObject(), oldVal, newVal);
    return false;
  }

  /**
   * Gets the value of bits available for memory manager use in an
   * object, in preparation for setting those bits.
   *
   * @param object the address of the object
   * @return the value of the bits
   */
  public Word prepareAvailableBits(ObjectReference object) {
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.attemptAvailableBits() was called");
    //return VM_ObjectModel.prepareAvailableBits(object.toObject());
    return Word.fromInt(0);
  }

  /**
   * Sets the bits available for memory manager use in an object.
   *
   * @param object the address of the object
   * @param val the new value of the bits
   */
  public void writeAvailableBitsWord(ObjectReference object, Word val) {
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.attemptAvailableBits() was called");
    //VM_ObjectModel.writeAvailableBitsWord(object.toObject(), val);
    return;
  }

  /**
   * Read the bits available for memory manager use in an object.
   *
   * @param object the address of the object
   * @return the value of the bits
   */
  public Word readAvailableBitsWord(ObjectReference object) {
      System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.readAvailableBitsWord() was called");
      //return VM_ObjectModel.readAvailableBitsWord(object);
      return Word.fromInt(0);
  }

  /**
   * Gets the offset of the memory management header from the object
   * reference address.  XXX The object model / memory manager
   * interface should be improved so that the memory manager does not
   * need to know this.
   *
   * @return the offset, relative the object reference address
   */
  /* AJG: Should this be a variable rather than method? */
  public Offset GC_HEADER_OFFSET() {
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.GC_HEADER_OFFSET() was called");
    //return VM_ObjectModel.GC_HEADER_OFFSET;
    return Offset.fromInt(0);
  }

  /**
   * Returns the lowest address of the storage associated with an object.
   *
   * @param object the reference address of the object
   * @return the lowest address of the object
   */
  public Address objectStartRef(ObjectReference object)
    throws InlinePragma {
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.objectStartRef() was called");
    //return VM_ObjectModel.objectStartRef(object);
    return Address.fromInt(0);
  }

  /**
   * Returns an address guaranteed to be inside the storage assocatied
   * with and object.
   *
   * @param object the reference address of the object
   * @return an address inside the object
   */
  public Address refToAddress(ObjectReference object) {
    System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.objectStartRef() was called");
    //return VM_ObjectModel.getPointerInMemoryRegion(object);
    return Address.fromInt(0);
  }

  /**
   * Checks if a reference of the given type in another object is
   * inherently acyclic.  The type is given as a TIB.
   *
   * @return <code>true</code> if a reference of the type is
   * inherently acyclic
   */
  public boolean isAcyclic(ObjectReference typeRef) 
/*
    throws InlinePragma {
    Object type;
    Object[] tib = VM_Magic.addressAsObjectArray(typeRef.toAddress());
    if (true) {  // necessary to avoid an odd compiler bug
      type = VM_Magic.getObjectAtOffset(tib, Offset.fromIntZeroExtend(TIB_TYPE_INDEX));
    } else {
      type = tib[TIB_TYPE_INDEX];
    }
    return VM_Magic.objectAsType(type).isAcyclicReference();
*/
  {
      System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.isAcyclic() was called");
      return false;
  }

  /**
   * Return the type object for a give object
   *
   * @param object The object whose type is required
   * @return The type object for <code>object</code>
   */
  public MMType getObjectType(ObjectReference object) 
    throws InlinePragma {
      System.out.println("org.apache.HarmonyDRLVM.mm.mmtk.ObjectModel.getObjectType() was called");
    /*
    Object obj = object.toObject();
    Object[] tib = VM_ObjectModel.getTIB(obj);
    if (VM.VerifyAssertions) {
      if (tib == null || VM_ObjectModel.getObjectType(tib) != VM_Type.JavaLangObjectArrayType) {
        VM.sysWriteln("getObjectType: objRef = ", object.toAddress(), "   tib = ", VM_Magic.objectAsAddress(tib));
        VM.sysWriteln("               tib's type is not Object[]");
        VM._assert(false);
      }
    }
    VM_Type vmType = VM_Magic.objectAsType(tib[TIB_TYPE_INDEX]);
    if (VM.VerifyAssertions) {
      if (vmType == null) {
        VM.sysWriteln("getObjectType: null type for object = ", object);
        VM._assert(false);
      }
    }
    if (VM.VerifyAssertions) VM._assert(vmType.getMMType() != null);
    return (MMType) vmType.getMMType();
  */
    return new MMType(false, false, false, 0, null);
  }
}

