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
 * @version $Revision: 1.9.16.2.4.3 $
 */

#include "Ia32IRManager.h"

namespace Jitrino
{
namespace Ia32 {

//========================================================================================
// class RCE
//========================================================================================
/**
 *  class RCE performs removing comparisons following instructions which
 *  affected flags in the same way as CMP. In some cases instructions can be
 *  reordered for resolving comparison as available for removing
 *
 *  The algorithm takes one-pass over CFG.
 *
 *  This transformer ensures that
 *
 *  1)  All conditional instructions get the same EFLAGS value as before 
 *      transformation
 *
 *  2)  All reordered instructions do the same effects as before transformation
 *
 *  For example:
 *  
 *  Original code piece:
 *      I29: t50.:int32 (ID:v15(EFLGS):uint32) =AND .t28:int32,t49(1):int32 
 *      I30: (AD:v1:int32) =CopyPseudoInst (AU:t48:int32) 
 *      I31: (AD:v2:int32) =CopyPseudoInst (AU:t25:int32) 
 *      I32: (AD:v3:int8[]) =CopyPseudoInst (AU:t38:int8[]) 
 *      I33: (ID:v15(EFLGS):uint32) =CMP .t50:int32,t51(0):int32 
 *      I34: JNZ BB_12 t52(0):intptr (IU:v15(EFLGS):uint32) 
 *
 *  After optimization:
 *      I29: t50:int32 (ID:v15(EFLGS):uint32) =AND .t28:int32,t49(1):int32 
 *      I30: (AD:v1:int32) =CopyPseudoInst (AU:t48:int32) 
 *      I31: (AD:v2:int32) =CopyPseudoInst (AU:t25:int32) 
 *      I32: (AD:v3:int8[]) =CopyPseudoInst (AU:t38:int8[]) 
 *      I34: JNZ BB_12 t52(0):intptr (IU:v15(EFLGS):uint32) 
 *
 *  The implementation of this transformer is located in Ia32RCE.cpp
 *
 */
    
class RCE : public SessionAction {
    void runImpl();
protected:

    //  check is flags using by conditional instruction affected by instruction
    bool isUsingFlagsAffected(Inst * inst, Inst * condInst);

    //  check instruction inst for possibility of removing
    bool isSuitableToRemove(Inst * inst, Inst * condInst, Inst * cmpInst, Opnd * cmpOp);

};

static ActionFactory<RCE> _rce("rce");

/**
 *  The algorithm finds conditional instruction first, then corresponded 
 *  CMP instruction and arithmetic instruction which affects flags in the same
 *  way as CMP. Combination is considered as available to be reduced if there 
 *  are no instructions between CMP and arithmetic instruction which influence 
 *  to flags or CMP operands.
 *
 *  Also it tries to change improper conditional instruction to more optimizable
 *  kind.
 */
void
RCE::runImpl() 
{
    Inst * inst, * cmpInst, *condInst;
    Opnd * cmpOp = NULL; 
    cmpInst = condInst = NULL;
    const Nodes& nodes = irManager->getFlowGraph()->getNodesPostOrder();
    for (Nodes::const_iterator it = nodes.begin(), end = nodes.end(); it!=end; ++it) {
        Node* node = *it;
        if (node->isBlockNode()) {
            if(node->isEmpty()) {
                continue;
            }
            cmpInst = NULL;
            Inst* prevInst = NULL;
            for(inst = (Inst*)node->getLastInst(); inst != NULL; inst = prevInst) {
            prevInst = inst->getPrevInst();
                //find conditional instruction
                Mnemonic baseMnem = getBaseConditionMnemonic(inst->getMnemonic());
                if (baseMnem != Mnemonic_NULL) {
                    condInst = condInst ? NULL : inst;
                    cmpInst = NULL;
                } else if (condInst) {
                    //find CMP instruction corresponds to conditional instruction
                    if(inst->getMnemonic() == Mnemonic_CMP || inst->getMnemonic() == Mnemonic_UCOMISD || inst->getMnemonic() == Mnemonic_UCOMISS) {
                        if (cmpInst) {
                            //this comparison is redundant because of overrided by cmpInst
                            inst->unlink();
                            continue;
                        }
                        cmpInst = inst;
                        uint32 defCount = inst->getOpndCount(Inst::OpndRole_InstLevel|Inst::OpndRole_Def);
                        if(inst->getOpnd(defCount+1)->isPlacedIn(OpndKind_Imm)) {
                            //try to change conditional instruction to make combination available to optimize
                            cmpOp = inst->getOpnd(defCount);
                            Inst * newCondInst; Mnemonic mnem;
                            int64 val = inst->getOpnd(defCount+1)->getImmValue();
                            
                            if (val == 0) {
                                continue;
                            } else if (val == 1 && ConditionMnemonic(condInst->getMnemonic()-getBaseConditionMnemonic(condInst->getMnemonic())) == ConditionMnemonic_L){
                                mnem = Mnemonic((condInst->getMnemonic() - Mnemonic(ConditionMnemonic_L)) + Mnemonic(ConditionMnemonic_LE));
                            } else if (val == -1 && ConditionMnemonic(condInst->getMnemonic()-getBaseConditionMnemonic(condInst->getMnemonic())) == ConditionMnemonic_G) {
                                mnem = Mnemonic((condInst->getMnemonic() - Mnemonic(ConditionMnemonic_G)) + Mnemonic(ConditionMnemonic_GE));
                            } else if (val == -1 && ConditionMnemonic(condInst->getMnemonic()-getBaseConditionMnemonic(condInst->getMnemonic())) == ConditionMnemonic_B) {
                                mnem = Mnemonic((condInst->getMnemonic() - Mnemonic(ConditionMnemonic_B)) + Mnemonic(ConditionMnemonic_BE));
                            } else {
                                continue;
                            }
                            //replace old conditional instruction
                            if (condInst->hasKind(Inst::Kind_BranchInst)) {
                                BranchInst* br = (BranchInst*)condInst;
                                newCondInst = irManager->newBranchInst(mnem,br->getTrueTarget(), br->getFalseTarget(), condInst->getOpnd(0));
                            } else if (condInst->getForm() == Inst::Form_Native ) {
                                newCondInst = irManager->newInst(mnem, condInst->getOpnd(0), condInst->getOpnd(1));
                            } else {
                                newCondInst = irManager->newInstEx(mnem, condInst->getOpndCount(Inst::OpndRole_InstLevel|Inst::OpndRole_Def), condInst->getOpnd(0), condInst->getOpnd(1));
                            }
                            newCondInst->insertAfter(condInst);
                            condInst->unlink();
                            condInst = newCondInst;
                            inst->setOpnd(defCount+1, irManager->newImmOpnd(inst->getOpnd(defCount+1)->getType(),0));
                        } 
                    //find flags affected instruction precedes cmpInst
                    } else if (isUsingFlagsAffected(inst, condInst)) {
                        if (cmpInst) {
                            if (isSuitableToRemove(inst, condInst, cmpInst, cmpOp))
                            {
                                cmpInst->unlink();//replace cmp
                            } 
                        }
                        condInst = NULL;
                    } else {
                        if (inst->getOpndCount(Inst::OpndRole_Implicit|Inst::OpndRole_Def) || inst->getMnemonic() == Mnemonic_CALL) {
                            condInst = NULL;
                        } else {
                            //check for moving cmpInst operands 
                            if ((inst->getMnemonic() == Mnemonic_MOV) && (inst->getOpnd(0) == cmpOp)) {
                                cmpOp = inst->getOpnd(1);
                            }
                        }
                    } 
                }//end if/else by condInst
            }//end for() by Insts
        }//end if BasicBlock
    }//end for() by Nodes
}

bool
RCE::isUsingFlagsAffected(Inst * inst, Inst * condInst) 
{
    if (!inst->getOpndCount(Inst::OpndRole_Implicit|Inst::OpndRole_Def))
        //instruction doesn't change flags
        return false;
    switch (inst->getMnemonic()) {
        case Mnemonic_SUB:
            //instruction changes all flags
            return true; 
        case Mnemonic_IDIV:
        case Mnemonic_CALL:
            //instruction changes flags in the way doesn't correspond CMP
            return false;
        default:
            //instruction changes particular flags
            ConditionMnemonic mn = ConditionMnemonic(condInst->getMnemonic()-getBaseConditionMnemonic(condInst->getMnemonic()));
            return ( mn == ConditionMnemonic_Z || mn == ConditionMnemonic_NZ) ? true : false;
    }
}

bool RCE::isSuitableToRemove(Inst * inst, Inst * condInst, Inst * cmpInst, Opnd * cmpOp)
{
    /*  cmpInst can be removed if inst defines the same operand which will be
     *  compared with zero by cmpInst or inst is SUB with the same use-operands as cmpInst
     *  Required: Native form of insts
     */
    uint32 cmpOpCount = cmpInst->getOpndCount(Inst::OpndRole_InstLevel|Inst::OpndRole_UseDef);
    if ((cmpOp == inst->getOpnd(0)) && cmpInst->getOpnd(cmpOpCount -1)->isPlacedIn(OpndKind_Imm) && (cmpInst->getOpnd(cmpOpCount -1)->getImmValue() == 0)) {
            return true;
    }
    return false;
}

}} //end namespace Ia32

