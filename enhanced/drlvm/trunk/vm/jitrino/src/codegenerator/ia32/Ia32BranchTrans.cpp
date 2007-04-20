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
 * @author Nikolay A. Sidelnikov
 * @version $Revision: 1.6 $
 */
#include "Ia32IRManager.h"

namespace Jitrino
{
namespace Ia32 {
//========================================================================================
// class BranchTranslator
//========================================================================================
/**
 *    class BranchTranslator is implementation of replacing branching for a 
 *    single loading of an operand with a conditional CMOVcc or SETcc 
 *    instruction
 *    The algorithm takes one-pass over CFG.
 *
 *    This transformer allows to reduce count of branches
 *
 *    This transformer is recommended to be inserted before all optimizations
 *    because it unites basic blocks
 *
 *    The algorithm works as follows:    
 *        
 *    1)    Finds branch instruction which performs branch to basic blocks with
 *        only instructions MOV with the same def-operand.
 *
 *    2)    If each of thus blocks has only one predecessor they and branch 
 *        instruction is replaced with conditional instruction
 *
 *    The implementation of this transformer is located Ia32BranchTrans.cpp.
 */
class BranchTranslator : public SessionAction {

    void runImpl();
};

static ActionFactory<BranchTranslator> _btr("btr");

static Inst* findDefInstWithMove(Inst* currentInst, Opnd* opnd) {
    bool var = opnd->getDefScope() != Opnd::DefScope_Temporary;
    if (var) {
        //process only moves in current block
        for (Inst* inst= currentInst->getPrevInst(); inst!=NULL; inst = inst->getPrevInst()) {
            Inst::Opnds defs(inst,Inst::OpndRole_Def|Inst::OpndRole_Explicit|Inst::OpndRole_Auxilary);
            if (defs.begin()!= defs.end()) {
                Opnd* tmpOpnd =  inst->getOpnd(defs.begin()); 
                if (tmpOpnd == opnd) {
                    if (inst->getMnemonic()!=Mnemonic_MOV) {
                        //op is not supporter by BTR 
                        return NULL;
                    }
                    return inst;
                }
            }
            
        }
        return NULL; //no more defs for this var/semitemporal in the block
    }
    Inst* inst = opnd->getDefiningInst();
    if (!inst || inst->getMnemonic()!=Mnemonic_MOV) {
        return NULL;
    }
    return inst;
}

static Opnd * getMOVsChainSource(Inst* inst, Opnd * opnd) {
    Inst * instUp = findDefInstWithMove(inst, opnd);
    Opnd * resOpnd = opnd;
    while(instUp!=NULL) {
        assert(instUp->getMnemonic() == Mnemonic_MOV);
        resOpnd = instUp->getOpnd(1);
        instUp = findDefInstWithMove(instUp, resOpnd);
    }
    return resOpnd;
}

static bool branchDirection (int64 v1, int64 v2, OpndSize sz,ConditionMnemonic mn) {
    switch (sz) {
        case OpndSize_8:
            v1 = int64(int8(v1));
            v2 = int64(int8(v2));
            break;
        case OpndSize_16:
            v1 = int64(int16(v1));
            v2 = int64(int16(v2));
            break;
        case OpndSize_32:
            v1 = int64(int32(v1));
            v2 = int64(int32(v2));
            break;
        default:
            break;
    }

    bool branchDirection = false;
    switch (mn) {
        case ConditionMnemonic_E:
            branchDirection = v1 == v2;
            break;
        case ConditionMnemonic_NE:
            branchDirection = v1 != v2;
            break;
        case ConditionMnemonic_G:
            branchDirection = v1 > v2;
            break;
        case ConditionMnemonic_GE:
            branchDirection = v1>= v2;
            break;
        case ConditionMnemonic_L:
            branchDirection = v1 < v2;
            break;
        case ConditionMnemonic_LE:
            branchDirection = v1 <= v2;
            break;
        case ConditionMnemonic_AE:
            branchDirection = (uint64)v1 >= (uint64)v2;
            break;
        case ConditionMnemonic_A:
            branchDirection = (uint64)v1 > (uint64)v2;
            break;
        case ConditionMnemonic_BE:
            branchDirection = (uint64)v1<= (uint64)v2;
            break;
        case ConditionMnemonic_B:
            branchDirection = (uint64)v1 < (uint64)v2;
            break;
        default:
            assert(0);
            break;
    }
    return branchDirection;
}

static void mapDefsPerEdge(StlMap<Edge *, Opnd *>& defsPerEdge, Node* node, Opnd* opnd) {
    assert(opnd->getDefScope() == Opnd::DefScope_Variable);
    const Edges& inEdges = node->getInEdges();
    for (Edges::const_iterator ite = inEdges.begin(), ende = inEdges.end(); ite!=ende; ++ite) {
        Edge* edge = *ite;
        Node* prevNode = edge->getSourceNode();
        Inst* lastInst = (Inst*)prevNode->getLastInst();
        if (lastInst == NULL) {
            defsPerEdge[edge] = NULL;
            continue;
        }
        Opnd* opndDef = getMOVsChainSource(lastInst, opnd);
        defsPerEdge[edge] = opndDef;    
    }
}

void
BranchTranslator::runImpl() 
{
    const Nodes& nodes = irManager->getFlowGraph()->getNodesPostOrder();
    irManager->calculateOpndStatistics();

    bool consts = false;
    getArg("removeConstCompare", consts);

    if (consts) {
        StlMap<Node *, bool> loopHeaders(irManager->getMemoryManager());
        LoopTree * lt = irManager->getFlowGraph()->getLoopTree();
        for (Nodes::const_reverse_iterator it = nodes.rbegin(),end = nodes.rend();it!=end; ++it) {
            Node* bb = *it;
            if (lt->isLoopHeader(bb))
                loopHeaders[bb] = true;
            else
                loopHeaders[bb] = false;
        }

        for (Nodes::const_reverse_iterator it = nodes.rbegin(),end = nodes.rend();it!=end; ++it) {
            Node* bb = *it;
            if (bb->isBlockNode()){
                if(bb->isEmpty())
                    continue;

                Inst * inst = (Inst *)bb->getLastInst();
                //check is last instruction in basic block is a conditional branch instruction
                if(inst && inst->hasKind(Inst::Kind_BranchInst)) {
                    //get successors of bb
                    if(bb->getOutEdges().size() == 1)
                        continue;

                    Node * trueBB = bb->getTrueEdge()->getTargetNode();
                    Node * falseBB = bb->getFalseEdge()->getTargetNode();

                    ConditionMnemonic condMnem = ConditionMnemonic(inst->getMnemonic() - getBaseConditionMnemonic(inst->getMnemonic()));

                    //****start check for constants comparison****

                    Inst * cmpInst = inst->getPrevInst();
                    if (cmpInst && cmpInst->getMnemonic() == Mnemonic_CMP) {
                        Inst::Opnds uses(cmpInst,Inst::OpndRole_Use|Inst::OpndRole_Explicit|Inst::OpndRole_Auxilary);
                        Opnd * cmpOp1 = cmpInst->getOpnd(uses.begin());
                        Opnd * cmpOp2 = cmpInst->getOpnd(uses.begin()+1);

                        if (cmpOp1->getDefScope() == Opnd::DefScope_Temporary) {
                            cmpOp1 = getMOVsChainSource(cmpInst, cmpOp1);
                            if (!cmpOp1->isPlacedIn(OpndKind_Imm)) {
                                for(Inst * copy = (Inst *)bb->getLastInst();copy!=NULL; copy=copy->getPrevInst()) {
                                    Inst::Opnds opnds(copy, Inst::OpndRole_Def|Inst::OpndRole_ForIterator);
                                    for (Inst::Opnds::iterator ito = opnds.begin(); ito != opnds.end(); ito = opnds.next(ito)){
                                        Opnd * opnd = copy->getOpnd(ito);
                                        if (opnd == cmpOp1 && copy->getMnemonic() == Mnemonic_MOV) {
                                            cmpOp1 = copy->getOpnd(1);
                                        } else {
                                            break;
                                        }
                                    }
                                }
                            }
                            if (cmpOp1->isPlacedIn(OpndKind_Imm)) {
                                cmpOp2 = getMOVsChainSource(cmpInst, cmpOp2);
                                if (cmpOp2->isPlacedIn(OpndKind_Imm)) { 
                                    //Two constants are operands of CMP inst
                                    irManager->resolveRuntimeInfo(cmpOp1);
                                    irManager->resolveRuntimeInfo(cmpOp2);
                                    int64 v1 = cmpOp1->getImmValue();
                                    int64 v2 = cmpOp2->getImmValue();
                                    //remove "dead" edges
                                    if (branchDirection(v1,v2,cmpOp1->getSize(),condMnem)) {
                                        irManager->getFlowGraph()->removeEdge(bb->getFalseEdge());
                                    } else {
                                        irManager->getFlowGraph()->removeEdge(bb->getTrueEdge());
                                    }
                                    //remove CMP and Jcc instructions
                                    inst->unlink();
                                    cmpInst->unlink();
                                    continue;
                                }
                            }
                        } 
                        cmpOp1 = getMOVsChainSource(cmpInst, cmpOp1);
                        if (cmpOp1->getDefScope() == Opnd::DefScope_Variable) {
                            if(loopHeaders[bb])
                                continue;
                            cmpOp2 = getMOVsChainSource(cmpInst, cmpOp2);
                            if (cmpOp2->isPlacedIn(OpndKind_Imm)) {
                                if (cmpInst->getPrevInst()== NULL) { //no other side effects in node except branching
                                    StlMap<Edge *, Opnd *> defInsts(irManager->getMemoryManager());
                                    mapDefsPerEdge(defInsts, bb, cmpOp1);
                                    for (StlMap<Edge *, Opnd *>::iterator eit = defInsts.begin(); eit != defInsts.end(); eit++) {
                                        Edge * edge = eit->first;
                                        Opnd * opnd = eit->second;
                                        if (opnd == NULL || !opnd->isPlacedIn(OpndKind_Imm)) {
                                            continue; //can't retarget this edge -> var is not a const
                                        }
                                        if (branchDirection(opnd->getImmValue(), cmpOp2->getImmValue(),cmpOp1->getSize(),condMnem)) {
                                            irManager->getFlowGraph()->replaceEdgeTarget(edge, trueBB);
                                        } else {
                                            irManager->getFlowGraph()->replaceEdgeTarget(edge, falseBB);
                                        }
                                        for(Inst * copy = (Inst *)bb->getFirstInst();copy!=NULL; copy=copy->getNextInst()) {
                                            if (copy != inst && copy !=cmpInst) {
                                                Node * sourceBB = edge->getSourceNode();
                                                Inst * lastInst = (Inst*)sourceBB->getLastInst();
                                                Inst * newInst = copy->getKind() == Inst::Kind_I8PseudoInst?
                                                    irManager->newI8PseudoInst(Mnemonic_MOV,1,copy->getOpnd(0),copy->getOpnd(1)):
                                                    irManager->newCopyPseudoInst(Mnemonic_MOV,copy->getOpnd(0),copy->getOpnd(1));
                                                if (lastInst->getKind()== Inst::Kind_BranchInst) 
                                                    sourceBB->prependInst(newInst, lastInst);
                                                else
                                                    sourceBB->appendInst(newInst);
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (cmpOp1->getDefScope() == Opnd::DefScope_SemiTemporary) {
                            //TODO: merge DefScope_SemiTemporary & DefScope_Variable if-branches

                            //try to reduce ObjMonitorEnter pattern
                            Inst * defInst = cmpInst;
                            bool stopSearch = false;
                            //look for Mnemonic_SETcc def for cmpOp1 in the current block (it has SemiTemporary kind)
                            while (1) {
                                defInst = defInst->getPrevInst();
                                if (defInst == NULL) {
                                    break;
                                }
                                Inst::Opnds defs(defInst,Inst::OpndRole_Def|Inst::OpndRole_Explicit|Inst::OpndRole_Auxilary);
                                for (Inst::Opnds::iterator ito = defs.begin(); ito != defs.end(); ito = defs.next(ito)){
                                    Opnd * opnd = defInst->getOpnd(ito);
                                    if (opnd == cmpOp1) {
                                        Mnemonic mnem = getBaseConditionMnemonic(defInst->getMnemonic());
                                        ConditionMnemonic cm = ConditionMnemonic(defInst->getMnemonic()-mnem);
                                        if (mnem == Mnemonic_SETcc && cmpOp2->isPlacedIn(OpndKind_Imm) && cmpOp2->getImmValue() == 0) {
                                            if(cm == (inst->getMnemonic()- getBaseConditionMnemonic(inst->getMnemonic()))) {
                                                defInst->unlink();
                                                cmpInst->unlink();
                                                inst->unlink();
                                                bb->appendInst(irManager->newBranchInst((Mnemonic)(Mnemonic_Jcc+reverseConditionMnemonic(cm)),((BranchInst*)inst)->getTrueTarget(),((BranchInst*)inst)->getFalseTarget()));
                                                stopSearch = true;
                                                break;
                                            } 
                                        } else {
                                            stopSearch = true;
                                            break;
                                        }
                                    }
                                }
                                Inst::Opnds flags(defInst,Inst::OpndRole_Def|Inst::OpndRole_Implicit);
                                if (stopSearch || ((flags.begin() != flags.end()) && defInst->getOpnd(flags.begin())->getRegName() == RegName_EFLAGS))                                     
                                    break;
                            }
                            continue;
                        }
                    }
                    //****end check for constants comparison****
                }
            }
        }
    }

    bool cmovs = false;
    getArg("insertCMOVs", cmovs);

    if (cmovs) {
        for (Nodes::const_reverse_iterator it = nodes.rbegin(),end = nodes.rend();it!=end; ++it) {
            Node* bb = *it;
            if (bb->isBlockNode()){
                if(bb->isEmpty())
                    continue;

                Inst * inst = (Inst *)bb->getLastInst();
                //check is last instruction in basic block is a conditional branch instruction
                if(inst && inst->hasKind(Inst::Kind_BranchInst)) {
                    //get successors of bb
                    if(bb->getOutEdges().size() == 1)
                        continue;

                    Node * trueBB = bb->getTrueEdge()->getTargetNode();
                    Node * falseBB = bb->getFalseEdge()->getTargetNode();

                    ConditionMnemonic condMnem = ConditionMnemonic(inst->getMnemonic() - getBaseConditionMnemonic(inst->getMnemonic()));

                    //check is both successors have only instruction
                    Inst * trueInst = (Inst *)trueBB->getFirstInst();
                    Inst * falseInst = (Inst *)falseBB->getFirstInst();
                    if(trueBB && falseInst && trueBB->getInstCount() == 1 && falseBB->getInstCount() == 1 && trueInst->getMnemonic() == Mnemonic_MOV && falseInst->getMnemonic() == Mnemonic_MOV && trueInst->getOpnd(0) == falseInst->getOpnd(0) && trueInst->getOpnd(0)->getMemOpndKind() == MemOpndKind_Null) {
                        //check is bb is only predecessor for trueBB and falseBB
                        bool canBeRemoved = true;
                        Node * nextBB = trueBB->getOutEdges().front()->getTargetNode();
                        if (falseBB->getOutEdges().front()->getTargetNode() != nextBB)
                            canBeRemoved = false;

                        const Edges& tEdges  = trueBB->getInEdges();
                        for (Edges::const_iterator  edge = tEdges.begin(); edge != tEdges.end(); ++edge) {
                            Edge * e = *edge;
                            if (e->getSourceNode() != bb)
                                canBeRemoved = false;
                        }
                        const Edges& fEdges  = falseBB->getInEdges();
                        for (Edges::const_iterator  edge = fEdges.begin(); edge != fEdges.end(); ++edge) {
                            Edge * e = *edge;
                            if (e->getSourceNode() != bb)
                                canBeRemoved = false;
                        }
                        if (!canBeRemoved)
                            continue;
     
                            Opnd * tfOp= trueInst->getOpnd(0);
                            Opnd * tsOp= trueInst->getOpnd(1);
                            Opnd * fsOp= falseInst->getOpnd(1);
                            int64 v1 = tsOp->getImmValue();
                            int64 v2 = fsOp->getImmValue();
                        if (tsOp->isPlacedIn(OpndKind_Imm) && 
                            fsOp->isPlacedIn(OpndKind_Imm) && 
                            ((v1==0 && v2==1)|| (v1==1 && v2==0))) 
                        {
                            bb->prependInst(irManager->newCopyPseudoInst(Mnemonic_MOV, tfOp, v1?fsOp:tsOp), inst);
                            bb->prependInst(irManager->newInstEx(Mnemonic(Mnemonic_SETcc+(v1?condMnem:reverseConditionMnemonic(condMnem))), 1, tfOp,tfOp),inst);
                        } else {
                            //insert loading of initial value for operand
                            bb->prependInst(irManager->newCopyPseudoInst(Mnemonic_MOV, tfOp, fsOp), inst);
                            if (tsOp->isPlacedIn(OpndKind_Imm)) {
                                Opnd * tempOpnd = irManager->newOpnd(tsOp->getType());
                                Inst * tempInst = irManager->newCopyPseudoInst(Mnemonic_MOV, tempOpnd, tsOp);
                                bb->prependInst(tempInst, inst);
                                tsOp = tempOpnd;
                            }
                            //insert conditional CMOVcc instruction 
                            bb->prependInst(irManager->newInstEx(Mnemonic(Mnemonic_CMOVcc+condMnem), 1, tfOp,tfOp,tsOp),inst);
                        }
                            //link bb with successor of trueBB and falseBB
                            irManager->getFlowGraph()->replaceEdgeTarget(bb->getFalseEdge(), nextBB);
                            irManager->getFlowGraph()->removeEdge(bb->getTrueEdge());
                            inst->unlink();
                            irManager->getFlowGraph()->removeNode(falseBB);
                            irManager->getFlowGraph()->removeNode(trueBB);
                            
                    }
                } 
            }//end if BasicBlock
        }//end for() by Nodes
    }
    
    irManager->getFlowGraph()->purgeEmptyNodes();
    irManager->getFlowGraph()->purgeUnreachableNodes();
}

} //end namespace Ia32
}
