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
 * @author Intel, Konstantin M. Anisimov, Igor V. Chebykin
 * @version $Revision$
 *
 */

#ifndef IPFRUNTIMESUPPORT_H_
#define IPFRUNTIMESUPPORT_H_

#include "IpfCfg.h"
#include "IpfOpndManager.h"
#include "IpfRuntimeInterface.h"

namespace Jitrino {
namespace IPF {

//========================================================================================//
// Forward declarations
//========================================================================================//

struct TryRegion;
struct MptrDef;
struct SafePoint;

//========================================================================================//
// Typedefs
//========================================================================================//

typedef vector < TryRegion* >     RegionVector;
typedef vector < SafePoint >      SafePointVector;
typedef map < RegOpnd*, MptrDef > MptrDefMap;

typedef MptrDefMap::iterator      MptrDefMapIterator; 

//========================================================================================//
// TryRegion
//========================================================================================//

struct TryRegion {
    TryRegion(Byte*, Byte*, Byte*, ObjectType*, bool);

    Byte       *startAddr;
    Byte       *endAddr;
    Byte       *handlerAddr;
    ObjectType *exceptionType;
    bool       isExceptionObjDead;
};
    
//========================================================================================//
// MptrDef
//========================================================================================//

class MptrDef {
public:
    MptrDef() { node=NULL; inst=NULL; base=NULL; }
    MptrDef(BbNode *node, Inst *inst, RegOpnd *base) : node(node), inst(inst), base(base) {}
    
    BbNode  *node;            // node in which mptr defined
    Inst    *inst;            // inst defining mptr (if inst==NULL - base is merged)
    RegOpnd *base;            // base the mptr depends on (add mptr = 16, base)
};

//========================================================================================//
// InstPosition
//========================================================================================//

class SafePoint {
public:
    SafePoint() { node=NULL; inst=NULL; }
    SafePoint(BbNode *node, Inst *inst) : node(node), inst(inst) {}
    
    BbNode        *node;
    Inst          *inst;
    RegOpndVector alivePtrs;
};

//========================================================================================//
// RuntimeSupport
//========================================================================================//

class RuntimeSupport {
public:
                         RuntimeSupport(Cfg&, CompilationInterface&);
    void                 makeRuntimeInfo();
    void                 buildRootSet();

protected:
    MemoryManager        &mm;
    Cfg                  &cfg;
    CompilationInterface &compilationInterface;
    OpndManager          *opndManager;

    // Exception registration
    void                 registerExceptionHandlers();
    void                 makeRegion(Byte* startAddr, Byte* endAddr, Node* dispatchNode);

    RegionVector         tryRegions;

    // Make info block which will be used in stack unwind routine
    StackInfo            *makeStackInfo();
    
    // Build root set and extend bases live range
    void                 newSafePoint(BbNode*, Inst*, RegOpndSet&);
    void                 defMptr(BbNode*, Inst*);
    void                 mergeBase(BbNode*, Inst*, RegOpnd*, RegOpnd*);
    void                 replaceBase(Inst*, Opnd*, Opnd*);
    void                 insertMovInst(BbNode*, Inst*, Opnd*, Opnd*);
    void                 insertBases(Inst*, RegOpndVector&);

    void                 makeRootSetInfo(Uint32Vector&);
    void                 writeSpInfo(Uint32Vector&, uint64, RegOpndVector&);
    int32                toInt32(RegOpnd*);

    SafePointVector      safePoints;       // after buildRootSet() vector contains all safe points
    MptrDefMap           mptr2def;         //
};

} // IPF
} // Jitrion

#endif /*IPFRUNTIMESUPPORT_H_*/
