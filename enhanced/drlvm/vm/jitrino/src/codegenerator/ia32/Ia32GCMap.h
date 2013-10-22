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
 * @author Intel, Mikhail Y. Fursov
 * @version $Revision: 1.10.14.2.4.4 $
 */

#ifndef _IA32_GC_MAP_H_
#define _IA32_GC_MAP_H_

#include "Stl.h"
#include "MemoryManager.h"
#include "Ia32IRManager.h"
#include "Ia32StackInfo.h"
#include "Ia32BCMap.h"
#include "DrlVMInterface.h"
namespace Jitrino
{
namespace Ia32 {
#ifdef _DEBUG
#define GC_MAP_DUMP_ENABLED
#endif

    class GCSafePointsInfo;
    class GCSafePoint;
    class GCSafePointOpnd;
    class StackInfo;

    class GCMap {
        typedef StlVector<GCSafePoint*> GCSafePoints;
    public:
        GCMap(MemoryManager& mm);

        void registerInsts(IRManager& irm);

        POINTER_SIZE_INT getByteSize() const ;
        static POINTER_SIZE_INT readByteSize(const Byte* input);
        void write(Byte*);
        const GCSafePointsInfo* getGCSafePointsInfo() const {return offsetsInfo;}
        
        static const POINTER_SIZE_INT* findGCSafePointStart(const POINTER_SIZE_INT* image, POINTER_SIZE_INT ip);
        static void checkObject(DrlVMTypeManager& tm, const void* p);

    private:
        void processBasicBlock(IRManager& irm, const Node* block);
        void registerGCSafePoint(IRManager& irm, const BitSet& ls, Inst* inst);
        void registerHardwareExceptionPoint(Inst* inst);
        bool isHardwareExceptionPoint(const Inst* inst) const;

        
        
        MemoryManager& mm;
        GCSafePoints gcSafePoints;
        GCSafePointsInfo* offsetsInfo;

    };

    class GCSafePoint {
        friend class GCMap;
        typedef StlVector<GCSafePointOpnd*> GCOpnds;
    public:
        GCSafePoint(MemoryManager& mm, POINTER_SIZE_INT _ip):gcOpnds(mm), ip(_ip) {
#ifdef _DEBUG
            instId = 0;
            hardwareExceptionPoint = false;
#endif
        }
        GCSafePoint(MemoryManager& mm, const POINTER_SIZE_INT* image);

        POINTER_SIZE_INT getUint32Size() const;
        void write(POINTER_SIZE_INT* image) const;
        uint32 getNumOpnds() const {return gcOpnds.size();}
        static POINTER_SIZE_INT getIP(const POINTER_SIZE_INT* image);

        void enumerate(GCInterface* gcInterface, const JitFrameContext* c, const StackInfo& stackInfo) const;
    
    private:
        //return address in memory where opnd value is saved
        POINTER_SIZE_INT getOpndSaveAddr(const JitFrameContext* ctx, const StackInfo& sInfo,const GCSafePointOpnd* gcOpnd) const;
        GCOpnds gcOpnds;
        POINTER_SIZE_INT ip;
#ifdef _DEBUG
        POINTER_SIZE_INT instId;
        bool hardwareExceptionPoint;
    public: 
        bool isHardwareExceptionPoint() const {return hardwareExceptionPoint;}
#endif 
    };

    class GCSafePointOpnd {
        friend class GCSafePoint;
        static const uint32 OBJ_MASK  = 0x1;
        static const uint32 REG_MASK  = 0x2;
#ifdef _EM64T_
        static const uint32 COMPRESSED_MASK  = 0x4;
#endif

#ifdef _DEBUG
        // flags + val + mptrOffset + firstId
        static const uint32 IMAGE_SIZE_UINT32 = 4; //do not use sizeof due to the potential struct layout problems
#else 
        // flags + val + mptrOffset 
        static const POINTER_SIZE_INT IMAGE_SIZE_UINT32 = 3;
#endif 

    public:
        
#ifdef _EM64T_
        GCSafePointOpnd(bool isObject, bool isOnRegister, int32 _val, int32 _mptrOffset, bool isCompressed=false) : val(_val), mptrOffset(_mptrOffset) {
            flags = flags | (isCompressed ? COMPRESSED_MASK: 0);
#else
        GCSafePointOpnd(bool isObject, bool isOnRegister, int32 _val, int32 _mptrOffset) : val(_val), mptrOffset(_mptrOffset) {
#endif
            flags = isObject ? OBJ_MASK : 0;
            flags = flags | (isOnRegister ? REG_MASK: 0);
#ifdef _DEBUG
            firstId = 0;
#endif
        }
        
        bool isObject() const {return (flags & OBJ_MASK)!=0;}
        bool isMPtr() const  {return !isObject();}
        
        bool isOnRegister() const { return (flags & REG_MASK)!=0;}
        bool isOnStack() const {return !isOnRegister();}
        
#ifdef _EM64T_
        bool isCompressed() const { return (flags & COMPRESSED_MASK)!=0;}
#endif      
        RegName getRegName() const { assert(isOnRegister()); return RegName(val);}
        int32 getDistFromInstESP() const { assert(isOnStack()); return val;}

        int32 getMPtrOffset() const {return mptrOffset;}
        void getMPtrOffset(int newOffset) {mptrOffset = newOffset;}

#ifdef _DEBUG
        uint32 firstId;
#endif

    private:
        GCSafePointOpnd(uint32 _flags, int32 _val, int32 _mptrOffset) : flags(_flags), val(_val), mptrOffset(_mptrOffset) {}

        //first bit is location, second is type
        uint32 flags;        
        //opnd placement ->Register or offset
        int32 val; 
        int32 mptrOffset;
    };


    class GCMapCreator : public SessionAction {
        void runImpl();
        uint32 getNeedInfo()const{ return NeedInfo_LivenessInfo;}
#ifdef  GC_MAP_DUMP_ENABLED
        uint32 getSideEffects() {return Log::isEnabled();}
        bool isIRDumpEnabled(){ return true;}
#else 
        uint32 getSideEffects() {return 0;}
        bool isIRDumpEnabled(){ return false;}
#endif
    };        

    class InfoBlockWriter : public SessionAction {
        void runImpl();
        uint32 getNeedInfo()const{ return 0; }
        uint32 getSideEffects()const{ return 0; }
        bool isIRDumpEnabled(){ return false; }
    };

}} //namespace

#endif /* _IA32_GC_MAP_H_ */
