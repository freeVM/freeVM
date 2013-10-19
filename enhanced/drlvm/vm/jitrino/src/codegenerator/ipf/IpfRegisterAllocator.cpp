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
 * @author Intel, Konstantin M. Anisimov, Igor V. Chebykin
 *
 */

#include "IpfRegisterAllocator.h"
#include "IpfIrPrinter.h"
#include "IpfOpndManager.h"
#include "IpfLiveAnalyzer.h"
#include <fstream>

namespace Jitrino {
namespace IPF {

bool greaterSpillCost(RegOpnd *op1, RegOpnd *op2) { return op1->getSpillCost() > op2->getSpillCost(); }

//========================================================================================//
// RegisterAllocator
//========================================================================================//

RegisterAllocator::RegisterAllocator(Cfg &cfg) : 
    mm(cfg.getMM()), 
    cfg(cfg),
    allocSet(mm),
    liveSet(mm) {

    opndManager = cfg.getOpndManager();
}

//----------------------------------------------------------------------------------------//

void RegisterAllocator::allocate() {
    
    IPF_LOG << endl << "  Build Interference Matrix" << endl;
    buildInterferenceMatrix();
    removeSelfDep();

    IPF_LOG << endl << "  Assign Locations" << endl;
    assignLocations();
    
    IPF_LOG << endl << "  Remove Usless \"mov\" Instructions" << endl;
    removeSameRegMoves();
}

//----------------------------------------------------------------------------------------//
// 1. for each opnd build list of opnds which alive during live range of the opnd (RegOpnd::depOpnds)
// 2. build list of opnds which need allocation (allocSet)

void RegisterAllocator::buildInterferenceMatrix() {

    NodeVector &nodes = cfg.search(SEARCH_POST_ORDER);
    for(uint16 i=0; i<nodes.size(); i++) {                 // iterate through CFG nodes

        if(nodes[i]->isBb() == false) continue;            // ignore non BB nodes

        liveSet.clear();                                   // clear live set
        nodes[i]->mergeOutLiveSets(liveSet);               // put in the live set merged live sets of successors

        BbNode       *node       = (BbNode *)nodes[i];
        uint32       execCounter = node->getExecCounter();
        InstIterator currInst    = node->getInsts().end()-1;
        InstIterator firstInst   = node->getInsts().begin()-1;
        
        for (; currInst>firstInst; currInst--) {

            Inst        *inst  = *currInst;
            uint16      numDst = inst->getNumDst();        // number of dst opnds (qp has index 0)
            OpndVector& opnds  = inst->getOpnds();         // get inst's opnds

            checkCoalescing(execCounter, inst);

            LiveAnalyzer::defOpnds(liveSet, inst);         // remove dst opnds from live set
            checkCallSite(inst);                           // if currInst is "call" - all alive opnds cross call site
            for(uint16 i=1; i<=numDst; i++) {              // for each dst opnd
                updateAllocSet(opnds[i], execCounter);     // insert in allocSet and add live set in dep list
            }
            
            LiveAnalyzer::useOpnds(liveSet, inst);         // add src opnds in live set
            updateAllocSet(opnds[0], execCounter);         // insert in allocSet pq opnd and add alive qps in dep list
            for (uint16 i=numDst+1; i<opnds.size(); i++) { // for each src opnd
                updateAllocSet(opnds[i], execCounter);     // insert in allocSet and add live set in dep list
            }
        }
    }
}

//----------------------------------------------------------------------------------------//

void RegisterAllocator::checkCoalescing(uint32 execCounter, Inst *inst) {
    
    if (inst->getInstCode() != INST_MOV)          return; // if inst is not "mov" - ignore

    Opnd *qp  = inst->getOpnd(0);                          // get dst opnd of mov inst
    Opnd *dst = inst->getOpnd(1);                          // get dst opnd of mov inst
    Opnd *src = inst->getOpnd(2);                          // get src opnd of mov inst

    if (qp->getValue() != 0)                      return; // if it is not p0 - ignore
    if (dst->getOpndKind() != src->getOpndKind()) return; // if opnds have different reg types - ignore
    if (src->isConstant() == true)                return; // if src is constant (r0, f1 ...) - ignore

    RegOpnd *dst_ = (RegOpnd *)dst;
    RegOpnd *src_ = (RegOpnd *)src;
    dst_->addCoalesceCand(execCounter, src_);
    src_->addCoalesceCand(execCounter, dst_);
}

//----------------------------------------------------------------------------------------//
// 1. Remove opnd dependency on itself 
// 2. Log out opnd dependencis

void RegisterAllocator::removeSelfDep() {

    for(RegOpndSetIterator it=allocSet.begin(); it!=allocSet.end(); it++) {
        (*it)->getDepOpnds().erase(*it);
    }

    if(LOG_ON) { 
        IPF_LOG << endl << "  Opnd dependensies " << endl; 
        for(RegOpndSetIterator it=allocSet.begin(); it!=allocSet.end(); it++) {
            RegOpnd *opnd = *it;
            IPF_LOG << "      " << setw(4) << left << IrPrinter::toString(opnd) << " depends on: ";
            IPF_LOG << IrPrinter::toString(opnd->getDepOpnds()) << endl;
        }
    }
}
    
//----------------------------------------------------------------------------------------//
// 1. Sort opnd list by Spill Cost
// 2. Assign locations to all not allocated opnds

void RegisterAllocator::assignLocations() {
    
    RegOpndVector opndVector(mm);
    opndVector.insert(opndVector.begin(), allocSet.begin(), allocSet.end());   // create vector of opns to be allocated
    sort(opndVector.begin(), opndVector.end(), greaterSpillCost); // sort them by Spill Cost
    
    for (uint16 i=0; i<opndVector.size(); i++) {
        RegOpnd *opnd = opndVector[i];
        if (opnd->getLocation() != LOCATION_INVALID) continue;    // opnd has already had location
    
        IPF_LOG << "      " << left << setw(5) << IrPrinter::toString(opnd); 
        assignLocation(opnd);                 // assign location for current opnd
        IPF_LOG << " after assignment " << left << setw(5) << IrPrinter::toString(opnd);
        IPF_LOG << " spill cost: " << opnd->getSpillCost() << endl; 
    }
}

//----------------------------------------------------------------------------------------//
// remove useless move insts:
// mov r8 = r8

void RegisterAllocator::removeSameRegMoves() {

    NodeVector &nodes = cfg.search(SEARCH_POST_ORDER);
    for(uint16 i=0; i<nodes.size(); i++) {                                    // iterate through CFG nodes
        if(nodes[i]->isBb() == false) continue;                               // ignore non BB nodes

        BbNode    *node   = (BbNode *)nodes[i];
        InstVector &insts = node->getInsts();
        for (InstVector::iterator it=insts.begin(); it!=insts.end();) {
            Inst *inst = *it;
            if (inst->getInstCode() != INST_MOV)          { it++; continue; }
            Opnd *dst = inst->getOpnd(1);                                     // get dst opnd of mov inst
            Opnd *src = inst->getOpnd(2);                                     // get src opnd of mov inst
            if (dst->getOpndKind() != src->getOpndKind()) { it++; continue; } // if opnds have different reg types - ignore
            if (dst->getValue() != src->getValue())       { it++; continue; } // if opnds allocated on different regs - ignore
            
            it = insts.erase(it);
            IPF_LOG << "      node" << left << setw(4) << node->getId() << IrPrinter::toString(inst) << endl;
        }
    }
}

//----------------------------------------------------------------------------------------//
// find and assign location for target opnd

void RegisterAllocator::assignLocation(RegOpnd *target) {
    
    OpndKind  opndKind    = target->getOpndKind();
    DataKind  dataKind    = target->getDataKind();
    bool      isPreserved = target->getCrossCallSite();

    // build mask of used regs (already assigned opnds)
    RegBitSet  usedMask;
    RegOpndSet &depOpnds = target->getDepOpnds();
    for (RegOpndSet::iterator it=depOpnds.begin(); it!=depOpnds.end(); it++) {
        RegOpnd *opnd = *it;
        int32 location = opnd->getLocation();                   // get location of dep opnd
        if (location >= NUM_G_REG) continue;                    // if opnd is not assigned on reg - continue
        usedMask[location] = true;                              // mark reg busy
    }

    // try to find opnd to coalesce on
    Int2OpndMap &coalesceCands = target->getCoalesceCands();
    for (Int2OpndMap::iterator it=coalesceCands.begin(); it!=coalesceCands.end(); it++) {
        RegOpnd *opnd = it->second;
        int32 location = opnd->getValue();
        if (location > NUM_G_REG)                             continue;
        if (isPreserved && (opnd->getCrossCallSite()==false)) continue;
        if (usedMask[location] == true)                       continue;
        target->setLocation(location);
        return;
    }
    
    int32 location = opndManager->newLocation(opndKind, dataKind, usedMask, isPreserved);
    target->setLocation(location);                              // set location
}    

//----------------------------------------------------------------------------------------//

void RegisterAllocator::updateAllocSet(Opnd *cand_, uint32 execCounter) {

    if (cand_->isReg()      == false) return;    // imm - it does not need allocation
    if (cand_->isMem()      == true)  return;    // mem stack - it does not need allocation
    if (cand_->isConstant() == true)  return;    // constant - it does not need allocation

    RegOpnd *cand = (RegOpnd *)cand_;
    cand->incSpillCost(execCounter);             // increase opnd spill cost 
    allocSet.insert(cand);                       // isert opnd in list for allocation

    // add current live set in opnd dep list (they must be placed on different regs)
    for (RegOpndSetIterator it=liveSet.begin(); it!=liveSet.end(); it++) {
        RegOpnd *opnd = *it;
        cand->insertDepOpnd(opnd);               // cand depends on curr opnd from live set
        opnd->insertDepOpnd(cand);               // curr opnd from live set depends on cand
    }
}

//----------------------------------------------------------------------------------------//
// Check if current inst is "call" and mark all opnds in liveSet as crossing call site

void RegisterAllocator::checkCallSite(Inst *inst) {

    if(Encoder::isBranchCallInst(inst) == false) return; // opnd does not crass call site

    IPF_LOG << "      these opnds cross call site: ";
    IPF_LOG << IrPrinter::toString(liveSet) << endl;

    for(RegOpndSetIterator it=liveSet.begin(); it!=liveSet.end(); it++) {
        (*it)->setCrossCallSite(true);
    }
}

} // IPF
} // Jitrino
