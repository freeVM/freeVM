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
/**
 * @author Intel, Nikolay A. Sidelnikov
 * @version $Revision: 1.13.14.3.4.4 $
 */

#ifndef _IA32_STACK_INFO_H_
#define _IA32_STACK_INFO_H_

#include "CodeGenIntfc.h"
#include "open/types.h"
#include "jit_export.h"
#include "MemoryManager.h"
#include "VMInterface.h"
#include "Type.h"
#include "Ia32Constraint.h"
#include "Ia32IRManager.h"
namespace Jitrino
{
namespace Ia32 {

    /**
     *  struct StackDepthInfo performs access to information about call 
     *  instruction. It contains callee save registers masks
     *  and stack depth for this instruction
     */
    
    struct StackDepthInfo {
        uint32 calleeSaveRegs;
        uint32 stackDepth;
        uint32 callSize;

        StackDepthInfo(uint32 sd=0) : calleeSaveRegs(0), stackDepth(sd), callSize(0) {}
        StackDepthInfo(uint32 e, uint32 sd=0, uint32 cz=0) : calleeSaveRegs(e), stackDepth(sd), callSize(cz) {}
    };

    struct DepthEntry {
        POINTER_SIZE_INT eip;
        StackDepthInfo info;
        DepthEntry * next;

        DepthEntry(POINTER_SIZE_INT ip, StackDepthInfo i, DepthEntry * n) : eip(ip), info(i), next(n) {}
    };

    /**
     * map DepthMap contains StackDepthInfos indexed by EIP of corresponded call
     * instruction
     */
    typedef StlMap<POINTER_SIZE_INT, StackDepthInfo> DepthMap;

//========================================================================================
//  class StackInfo
//========================================================================================
/**
 *  class StackInfo performs stack unwinding and fixing handler context.
 *  It can be saved and restored from memory block
 *
 *  This class ensures that
 *
 *  1)  All fields of a JitFrameContext object contain appropriate information
 *
 *  2)  All useful information from StackInfo object saves and restores 
 *      properly
 *
 *  The principle of the filling fields of a JitFrameContext object is a 
 *  hard-coded enumeration of registers.
 *
 *  The implementation of this class is located in file Ia32StackInfo.cpp
 *
 */
    
class StackInfo {
public:
    StackInfo(MemoryManager& mm) 
        : byteSize(0), hashTableSize(0), frameSize(0),
        itraceMethodExitString(0),
        eipOffset(0),
        icalleeMask(0),icalleeOffset(0),
        fcallee(0),foffset(0),
        acallee(0),aoffset(0),
        localOffset(0), 
        calleeSaveRegsMask(0),
        stackDepth(-1),
        offsetOfThis(0){ stackDepthInfo = new(mm) DepthMap(mm);}

    StackInfo() 
        : byteSize(0), hashTableSize(0), frameSize(0),
        itraceMethodExitString(0),
        eipOffset(0),
        icalleeMask(0),icalleeOffset(0),
        fcallee(0),foffset(0),
        acallee(0),aoffset(0),
        localOffset(0), 
        stackDepthInfo(NULL),
        calleeSaveRegsMask(0),
        stackDepth(-1),offsetOfThis(0) {}

    /** writes StackInfo data into memory
     */
    void write(Byte* output);

    /** reads StackInfo data from MethodDesc
     */
    void read(MethodDesc* pMethodDesc, POINTER_SIZE_INT eip, bool isFirst);

    /** Loads JitFrameContext with the next stack frame according to StackInfo
     */
    void unwind(MethodDesc* pMethodDesc, JitFrameContext* context, bool isFirst) const;

    /** Changes ESP to point to the start of stack frame
     */
    void fixHandlerContext(JitFrameContext* context);

    /** stores into StackInfo information about calls instructions such as 
     *  eip, stack depth and callee-save registers masks
     *  The algorithm takes one-pass over CFG.
     */
    void registerInsts(IRManager& irm);

    /** sets method exit string which was created during itrace pass 
        and is alive during runtime
    */
    void setMethodExitString(IRManager& irm);

    /** returns register offset for previos stack frame. 
        MUST be called before unwind()
    */
    POINTER_SIZE_INT * getRegOffset(const JitFrameContext* context, RegName reg) const;

    /** returns stack depth
    */
    POINTER_SIZE_INT getStackDepth() const {
        assert(stackDepth>=0);
        return stackDepth;
    }

    uint32 getFrameSize() const {return frameSize;}

    int getRetEIPOffset() const {return eipOffset;}
    
    uint32 getIntCalleeMask() const {return icalleeMask;}
    
    int getIntCalleeOffset() const {return icalleeOffset;}
    
    uint32 getFPCalleeMask() const {return fcallee;}
    
    int getFPCalleeOffset() const {return foffset;}

    uint32 getApplCalleeMask() const {return acallee;}
    
    int getApplCalleeOffset() const {return aoffset;}

    int getLocalOffset() const {return localOffset;}

    uint32 getOffsetOfThis() const {return offsetOfThis;}

    /** returns byte size of StackInfo data
    */
    POINTER_SIZE_INT getByteSize() const;

    static POINTER_SIZE_INT getByteSize(MethodDesc* md);

    /** read byte size from info block
    *                                                                      
    */
    POINTER_SIZE_INT readByteSize(const Byte* input) const;

private:
    POINTER_SIZE_INT byteSize;
    uint32 hashTableSize;
    uint32  frameSize;
    
    const char * itraceMethodExitString;

    int     eipOffset;

    uint32  icalleeMask;
    int     icalleeOffset;

    uint32  fcallee;
    int     foffset;

    uint32  acallee;
    int     aoffset;

    int     localOffset;
    
    DepthMap * stackDepthInfo;

    uint32 calleeSaveRegsMask;
    int stackDepth;
    uint32 offsetOfThis;

    friend class StackLayouter;
};

}}//namespace
#endif /* _IA32_STACK_INFO_H_ */
