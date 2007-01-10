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
 * @author Xiao-Feng Li
 */ 

package org.apache.harmony.drlvm.gc_gen;

import org.apache.harmony.drlvm.VMHelper;
import org.vmmagic.unboxed.*;
import org.vmmagic.pragma.*;

public class GCHelper {

    static {System.loadLibrary("gc_gen");}

    public static final int TLS_GC_OFFSET = TLSGCOffset();

    public static Address alloc(int objSize, int allocationHandle)   throws InlinePragma {
  
        Address TLS_BASE = VMHelper.getTlsBaseAddress();

        Address allocator_addr = TLS_BASE.plus(TLS_GC_OFFSET);
        Address allocator = allocator_addr.loadAddress();
        Address free_addr = allocator.plus(0);
        Address free = free_addr.loadAddress();
        Address ceiling = allocator.plus(4).loadAddress();
        
        Address new_free = free.plus(objSize);

        if (new_free.LE(ceiling)) {
            free_addr.store(new_free);
            free.store(allocationHandle);
            return free;
        }
                
        return VMHelper.newResolvedUsingAllocHandleAndSize(objSize, allocationHandle);    
    }

    private static final int ARRAY_LEN_OFFSET = 8;
    private static final int GC_OBJECT_ALIGNMENT = 4; //TODO: EM64 or IPF could have 8!

    public static Address allocArray(int arrayLen, int elemSize, int allocationHandle)  throws InlinePragma {
        if (arrayLen >= 0) {
            int firstElementOffset = ARRAY_LEN_OFFSET + (elemSize==8?8:4);
            int size = firstElementOffset + elemSize*arrayLen;
            size = (((size + (GC_OBJECT_ALIGNMENT - 1)) & (~(GC_OBJECT_ALIGNMENT - 1))));

            Address arrayAddress = alloc(size, allocationHandle); //never null!
            arrayAddress.store(arrayLen, Offset.fromInt(ARRAY_LEN_OFFSET));
            return arrayAddress;
        }
        return VMHelper.newVectorUsingAllocHandle(arrayLen, elemSize, allocationHandle);
    }



    /** NOS (nursery object space) is higher in address than other spaces.
       The boundary currently is produced in GC initialization. It can
       be a constant in future.
    */
    public static final int NOS_BOUNDARY = getNosBoundary();

    public static void write_barrier_slot_rem(Address p_objBase, Address p_objSlot, Address p_source)  throws InlinePragma {

       /* If the slot is in NOS or the target is not in NOS, we simply return*/
        if(p_objSlot.toInt() >= NOS_BOUNDARY || p_source.toInt() < NOS_BOUNDARY) {
            p_objSlot.store(p_source);
            return;
        }

        /* Otherwise, we need remember it in native code. */
        VMHelper.writeBarrier(p_objBase, p_objSlot, p_source);
    }


    private static native int getNosBoundary();
    
    private static native int TLSGCOffset();
}

