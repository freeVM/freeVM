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
 * @author Intel, Pavel A. Ozhdikhin
 * @version $Revision: 1.13.24.4 $
 *
 */

#ifndef _SYNC_OPT_H
#define _SYNC_OPT_H

#include <iostream>
#include "open/types.h"
#include "optpass.h"
#include "Opcode.h"
#include "Stl.h"
#include <utility>

namespace Jitrino {

class IRManager;
class MemoryManager;
class InequalityGraph;
class DominatorNode;
class Dominator;
class DomFrontier;
class Node;
class Opnd;
class CSEHashTable;
class Type;
class LoopTree;
class SyncClique;
class SyncOptDfValue;
class BuildSyncCliquesWalker;

struct SyncOptFlags {
    bool debug;
    bool verbose;
    bool redstore;
    bool optimistic; // assume we can balance monitorenter/exit around a call
    bool use_IncRecCount;
    bool transform;
    bool transform2;
    bool balance;
};

class SyncOpt {
    IRManager& irManager;
    MemoryManager &mm;

private:
    SyncOptFlags flags;
public:    
    static void readFlags(Action* argSource, SyncOptFlags* flags);
    static void showFlags(std::ostream& os);

    SyncOpt(IRManager &irManager0, 
            MemoryManager& memManager);

    ~SyncOpt();

    void runPass();

private:
    Type *lockType;
    Type *lockAddrType;

    friend class SyncOptWalker2;
    
    void findBalancedExits(bool optimistic, bool use_IncRecCount);

    StlHashMap<Opnd *, VarOpnd *> lockVar;

    VarOpnd *getLockVar(Opnd *obj);
    VarOpnd *getOldValueOpnd(SyncClique *clique);

private:
    // parts of findBalancedExits pulled out:
    uint32 findBalancedExits_Stage1(bool optimistic, bool use_IncRecCount, uint32 numNodes,
                                    SyncOptDfValue *&entrySolution, 
                                    SyncOptDfValue *&exitSolution);
    void linkStacks(uint32 depth1, SyncClique *stack1,
                    uint32 depth2, SyncClique *stack2,
                    SyncClique *bottomClique);
    void findBalancedExits_Stage2a(Node *node,
                                   uint32 depthIn,
                                   SyncClique *inStack,
                                   uint32 depthOut,
                                   SyncClique *outStack,
                                   BuildSyncCliquesWalker &walker,
                                   StlVector<SyncClique *>&stackspace,
                                   SyncClique *bottomClique);
    void findBalancedExits_Stage2(bool optimistic, bool use_IncRecCount,
                                  SyncOptDfValue *entrySolution,
                                  SyncOptDfValue *exitSolution,
                                  SyncClique *bottomClique,
                                  SyncClique **entryCliques, SyncClique **exitCliques,
                                  StlMap<Inst *, StlVectorSet<SyncClique *> *> &needRecCount,
                                  StlMap<Inst *, SyncClique *> &monitorCliques);
    void findBalancedExits_Stage3(bool optimistic, bool use_IncRecCount,
                                  SyncOptDfValue *entrySolution,
                                  SyncOptDfValue *exitSolution,
                                  SyncClique *bottomClique,
                                  SyncClique **entryCliques, SyncClique **exitCliques,
                                  StlMap<Inst *, StlVectorSet<SyncClique *> *> &needRecCount,
                                  StlMap<Inst *, SyncClique *> &monitorCliques);
    
    bool monitorExitIsBad(Inst *monExit, SyncClique *clique, 
                          SyncClique *cliqueRoot, SyncClique *bottomRoot);

    void insertUnwindMonitorExit(Opnd *syncMethodOpnd,
                                 Node *&tmpDispatchNode, Node *&tmpCatchNode,
                                 Node *&tmpRethrowNode);
    void removeUnwindMonitorExit(Opnd *syncMethodOpnd,
                                 Node *tmpDispatchNode, Node *tmpCatchNode,
                                 Node *tmpRethrowNode);
};

} //namespace Jitrino 

#endif // _MEMORY_OPT_H
