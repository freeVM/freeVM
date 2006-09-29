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
 * @author Vyacheslav P. Shakin
 * @version $Revision: 1.11.14.2.4.3 $
 */

#include "Ia32IRManager.h"
#include "Ia32Printer.h"

namespace Jitrino
{
namespace Ia32{

//========================================================================================
// class Ia32ConstraintsResolver
//========================================================================================
/**
 *  class Ia32ConstraintsResolver performs resolution of operand constraints
 *  and assigns calculated constraints (Opnd::ConstraintKind_Calculated) to operands.
 *  The resulting calculated constraints of operands determine allowable physical location
 *  for the operand.
 *  
 *  This transformer allows to insert operands into instructions before it 
 *  regardless instruction constraints except that Initial constraints of explicit 
 *  instruction operands must have non-null intersections with corresponding constraints 
 *  of at least one opcode group of the instruction.
 *  
 *  ConstraintResolver analyzes instruction constraints and splits operands when necessary.
 * 
 *  This transformer ensures that 
 *  1)  All instruction constraints for EntryPoints, CALLs and RETs
 *      are set appropriately (IRManager::applyCallingConventions())
 *  2)  All operands has non-null calculated constraints 
 *  3)  All operands fits into instructions they are used in (in terms of instruction constraints)
 *      For example:
 *          Original code piece:
 *              I38: (AD:s65:double) =CopyPseudoInst (AU:t1:double) 
 *              I32: MULSD .s65.:double,.t2:double 
 *              I33: RET t66(0):int16 (AU:s65:double) 
 *          
 *              RET imposes constraint on s65 requiring to place it into FP0 register 
 *              (its FP0D alias in this particular case)
 *              MULSD imposes constraint on s65 requiring to place it into XMM register 
 *                      
 *          After the pass: 
 *              I38: (AD:s65:double) =CopyPseudoInst (AU:t1:double) 
 *              I32: MULSD .s65.:double,.t2:double 
 *              I46: (AD:t75:double) =CopyPseudoInst (AU:s65:double) 
 *              I33: RET t66(20):int16 (AU:t75:double) 
 *          
 *              Thus, ConstraintResolver inserted I46 splitting s65 to s65 and t75
 *              s65 is assigned with Mem|XMM calculated constraint and t75 
 *              is assigned with FP0D calculated calculated constraint
 *
 *  4)  If the live range of an operand crosses a call site and the operand is not redefined 
 *      in the call site, the calculated constraint of the operand is narrowed the callee-save regs 
 *      or memory (stack)
 *      
 *  5)  If the operand (referred as original operand here) is live at entry of a catch handler 
 *      then necessary operand splitting is performed as close as possible to the instruction 
 *      which caused the splitting and original operand is used before and after the instruction. 
 *      
 *  The main principle of the algorithm is anding of instruction constraints into
 *  operand calculated constraints and splitting operands to ensure that the calculated constraint
 *  is not null
 *
 *  This transformer must be inserted before register allocator which relies on 
 *  calculated operand constraints. 
 *
 *  The implementation of this transformer is located in the ConstraintResolverImpl class. 
 *
 */

class ConstraintsResolver : public SessionAction {
    /** runImpl is required override, calls ConstraintsResolverImpl.runImpl */
    void runImpl();
    /** This transformer requires up-to-date liveness info */
    uint32 getNeedInfo()const{ return NeedInfo_LivenessInfo; }
    uint32 getSideEffects()const{ return 0; }
};

static ActionFactory<ConstraintsResolver> _constraints("constraints");

//========================================================================================
// class ConstraintsResolverImpl
//========================================================================================

/**
 *  class Ia32ConstraintsResolverImpl is an implementation of simple constraint resolution algorithm
 *  The algorithm takes one-pass over CFG.
 *
 *  The algorithm works as follows: 
 *      
 *  1)  Creates an array of basic blocks and orders by bb->getExecCount() 
 *      in createBasicBlockArray().
 *      Thus, the algorithm handles hottest basic blocks first and constraints are assigned to operands first 
 *      from the most frequently used instructions
 *
 *  2)  Collects a bit vector of all operands live at entries of all dispatch node entries
 *      in calculateLiveAtDispatchBlockEntries()
 *
 *  3)  For all operands:
 *      - If an operand has already been assigned to some location 
 *        (its location constraint is not null) the calculated constraint is set to 
 *        the location constraint
 *           
 *      - If an operand is live at entry of a dispatch node 
 *        the calculated constraint is set to the constraint 
 *        preserving operand values during exception throwing
 *        This constraint is returned by getDispatchEntryConstraint
 *        In fact this is the constriant for the DRL calling convention
 *
 *      This is done in calculateStartupOpndConstraints()
 *      Originally all calculateed constraints are equial to Initial constraints
 *
 *  4)  Walks through all basic blocks collected and arranged at step 1
 *      in resolveConstraints()
 *
 *      The opndReplaceWorkset array of operand replacements is maintained 
 *      (indexed by from-operand id). 
 *
 *      This is the array of current replacement for operands
 *      and is reset for each basic block (local within basic blocks) 
 *
 *      This array is filled as a result of operand splitting and indicates
 *      which operand must be used instead of original ones for all the instructions
 *      above the one caused splitting
 *      
 *      4.1) Walks throw all instruction of a basic block in backward order 
 *          in resolveConstraints(BasicBlock * bb) 
 *          4.1.1) resolves constraints for each instruction 
 *              in resolveConstraints(Inst * inst);
 *          
 *              To do this already collected calculated constraint of 
 *              either original operand or its current replacement is anded 
 *              with instruction constraint for this operand occurence and
 *              if the result is null, new operand is created and substituted instead
 *                              
 *              4.1.1.1) All def operands of the isntruction are traversed
 *                  and operand splitting is performed after the instruction (when necessary)
 *                  def&use cases are also handled during this step 
 *              4.1.1.2) If the instruction is CALL, all hovering operands of 
 *                  the isntruction are traversed.
 *
 *                  Hovering operands are operands which are live across a call site and are not
 *                  redefined in the call site
 *                  This step ensures operands are saved in callee-save regs or memory 
 *                  and takes into account whether an operand is live at dispatch node entries
 *
 *                  Operand splitting is performed before the instruction (when necessary)
 *              4.1.1.3) All use operands of the instruction are traversed
 *                  and operand splitting is performed before the instruction (when necessary)
 *              
 *      The current implementation doesn't deal properly with conditional memory constraints.
 *      I.e. it doesn't resolve properly things like ADD m, m when both operands are already
 *      assigned.
 * 
 *      For more details please refer to ConstraintsResolverImpl source code
 */

class ConstraintsResolverImpl
{
public:
    ConstraintsResolverImpl(IRManager &irm)
        :irManager(irm), 
        memoryManager(irManager.getOpndCount()*16, "ConstraintsResolverImpl"),
        basicBlocks(memoryManager, 0), originalOpndCount(0),
        liveOpnds(memoryManager,0),
        liveAtDispatchBlockEntry(memoryManager,0),
        needsOriginalOpnd(memoryManager,0),
        hoveringOpnds(memoryManager,0),
        opndReplaceWorkset(memoryManager,0)
    {
    }
    
    void run();

private:
    /** Get the priority of a the node for sorting in createBasicBlockArray */
    double getBasicBlockPriority(Node * node);
    /** Fills the basicBlocks array and orders it according to block exec count (hottest first) */ 
    void createBasicBlockArray();
    /** Fills the liveAtDispatchBlockEntry bit set with operands live at dispatch node entries */ 
    void calculateLiveAtDispatchBlockEntries();
    /** Pre-sets calculated constraints for each operand */ 
    void calculateStartupOpndConstraints();
    /** Scans basicBlocks array and calls resolveConstraints(BasicBlock * bb) for each entry */
    void resolveConstraints();
    /** Traverses instructions of bb and calls resolveConstraints(Inst *) 
     * for each inst 
     */
    void resolveConstraints(Node* bb);
    /**
     Main logic of constraint resolution for each instrution
     */
    void resolveConstraintsWithOG(Inst * inst);

    /** returns constraint describing call-safe locations for opnd in CallInst inst */
    Constraint getCalleeSaveConstraint(Inst * inst, Opnd * opnd);
    /** returns constraint describing safe locations for operands live at dispatch node entries */
    Constraint getDispatchEntryConstraint(Opnd * opnd);


    /** Reference to IRManager */ 
    IRManager&                  irManager;

    /** Private memory manager for this algorithm */ 
    MemoryManager               memoryManager;

    /** Array of basic blocks to be handled */ 
    Nodes                       basicBlocks;

    /** result of irManager.getOpndCount before the pass */ 
    uint32                      originalOpndCount;

    /** Current live set, updated as usual for each instruction in resolveConstraints(Inst*) */ 
    BitSet                      liveOpnds;

    /** Bit set of operands live at dispatch node entries */ 
    BitSet                      liveAtDispatchBlockEntry;

    /** Bit set of operands for which original operand should be used wherever possible.
     *  Currently this is only for operands which are live at dispatch block entries.
     */ 
    BitSet                      needsOriginalOpnd;

    /** Temporary bit set of hovering operands (live across call sites)
     *  Is initialized and used only during resolveConstraints(Inst*)
     */ 
    BitSet                      hoveringOpnds;

    /** An array of current substitutions for operands
     *  Is filled as a result of operand splitting.
     *  Reset for each basic blocks (all replacements are local within basic blocks)
     */
    StlVector<Opnd*>            opndReplaceWorkset;
};


//_________________________________________________________________________________________________
Constraint ConstraintsResolverImpl::getCalleeSaveConstraint(Inst * inst, Opnd * opnd)
{
// This implementation don't take into account operand types 
// and provides only GP call-safe regs (thus only memory for non-integer and non-pointer types)
    assert(inst->getKind()==Inst::Kind_CallInst);
    Constraint c=(Constraint(OpndKind_Memory)|STACK_REG|((CallInst*)inst)->getCalleeSaveRegs()) & opnd->getConstraint(Opnd::ConstraintKind_Initial);
    return c.isNull()?Constraint(OpndKind_Memory, opnd->getSize()):c;
}

//_________________________________________________________________________________________________
Constraint ConstraintsResolverImpl::getDispatchEntryConstraint(Opnd * opnd)
{
// Currently the same result as from getCalleeSaveConstraint
    Constraint c=(Constraint(OpndKind_Memory)|STACK_REG|Constraint(irManager.getEntryPointInst()->getCallingConventionClient().getCallingConvention()->getCalleeSavedRegs(OpndKind_GPReg))) & opnd->getConstraint(Opnd::ConstraintKind_Initial);
    return c.isNull()?Constraint(OpndKind_Memory, opnd->getSize()):c;
}

//_________________________________________________________________________________________________
void ConstraintsResolverImpl::run()
{
//  Set all instruction constraints for EntryPoints, CALLs and RETs
    irManager.applyCallingConventions();
// Initialization 
    originalOpndCount=irManager.getOpndCount();
    liveOpnds.resizeClear(originalOpndCount);
    needsOriginalOpnd.resizeClear(originalOpndCount);
    liveAtDispatchBlockEntry.resizeClear(originalOpndCount);

    opndReplaceWorkset.resize(originalOpndCount);
    for (uint32 i=0; i<originalOpndCount; i++)
        opndReplaceWorkset[i]=NULL;

    hoveringOpnds.resize(originalOpndCount);
// Fill array of basic blocks and order it
    createBasicBlockArray();
// Collect operands live at dispatch blocks
    calculateLiveAtDispatchBlockEntries();
// Pre-set calculated constraints for operands  
    calculateStartupOpndConstraints();
// Resolve constraints
    resolveConstraints();
// This is a local transformation, resize liveness vectors
    irManager.fixLivenessInfo();
}

//_________________________________________________________________________________________________

double ConstraintsResolverImpl::getBasicBlockPriority(Node * node)
{ 
// Use simple heuristics to handle prologs and epilogs after all other nodes.
// This improves performance as prologs and epilogs usually set bad constraints
// to operands (entry points, rets)
    return  
        irManager.getFlowGraph()->getEntryNode() == node ? (double)0 : 
        irManager.isEpilog(node) ? (double)1 : 
        10 + node->getExecCount();
}


//_________________________________________________________________________________________________
void ConstraintsResolverImpl::createBasicBlockArray()
{
    // Filling of basicBlock, simple insertion-based ordering of basic blocks
    const Nodes& nodes = irManager.getFlowGraph()->getNodes();
    for (Nodes::const_iterator it = nodes.begin(), end = nodes.end(); it!=end; ++it) {
        Node* node = *it;
        if (node->isBlockNode()){
            double bbecv = getBasicBlockPriority(node);
            uint32 ibb=0;
            for (uint32 nbb=basicBlocks.size(); ibb<nbb; ++ibb){
                if (bbecv > getBasicBlockPriority(basicBlocks[ibb]))
                    break;
            }
            basicBlocks.insert(basicBlocks.begin()+ibb, node);
        }
    }
}

//_________________________________________________________________________________________________
void ConstraintsResolverImpl::calculateLiveAtDispatchBlockEntries()
{   
    const Nodes& nodes = irManager.getFlowGraph()->getNodes();
    for (Nodes::const_iterator it = nodes.begin(), end = nodes.end(); it!=end; ++it) {
        Node *node = *it;
        if (node->isDispatchNode()) {
            liveAtDispatchBlockEntry.unionWith(*irManager.getLiveAtEntry(node));
        }
    }
}   
    
//_________________________________________________________________________________________________
void ConstraintsResolverImpl::calculateStartupOpndConstraints()
{   
// Reset calculated constraints to null constraints
    irManager.resetOpndConstraints();
// For all operands in the CFG
    for (uint32 i=0; i<originalOpndCount; i++){ 
        Opnd * opnd=irManager.getOpnd(i);

        Constraint c=opnd->getConstraint(Opnd::ConstraintKind_Initial);
        Constraint cl=opnd->getConstraint(Opnd::ConstraintKind_Location);

        if (!cl.isNull()) // Set Calculated to Location
            c=cl; 

        if (liveAtDispatchBlockEntry.getBit(i)){ // Set calculated to the constraint for dispatch entries
            if (cl.isNull()){ // if location is set it must satisfy dispatch entry constraints. 
                c=c & getDispatchEntryConstraint(opnd);
            }
            needsOriginalOpnd.setBit(i);
        }
        // result must not be null as well as opnd initial constrains 
        assert(!c.isNull() && !opnd->getConstraint(Opnd::ConstraintKind_Initial).isNull());
        // set the results
        opnd->setCalculatedConstraint(c); 
    }
}   

//_________________________________________________________________________________________________
void ConstraintsResolverImpl::resolveConstraintsWithOG(Inst * inst)
{   
    // Initialize hoveringOpnds with operands live after the call if the inst is CALL
    if (inst->getMnemonic()==Mnemonic_CALL)
        hoveringOpnds.copyFrom(liveOpnds);

    // first handle all defs
    {Inst::Opnds opnds(inst, Inst::OpndRole_AllDefs);
    for (Inst::Opnds::iterator it = opnds.begin(); it != opnds.end(); it = opnds.next(it)){
        Opnd * originalOpnd=inst->getOpnd(it);

        // We haven't changed def-operands yet for this instruction
        assert(originalOpnd->getId()<originalOpndCount);

        assert(!needsOriginalOpnd.getBit(originalOpnd->getId())||opndReplaceWorkset[originalOpnd->getId()]==NULL);

        // currentOpnd is either the current replacement or the original operand
        Opnd * currentOpnd=opndReplaceWorkset[originalOpnd->getId()];
        if (currentOpnd==NULL)
            currentOpnd=originalOpnd;
        // get what is already collected
        Constraint cc=currentOpnd->getConstraint(Opnd::ConstraintKind_Calculated);
        assert(!cc.isNull());
        // get the weak instruction constraint for this occurrence
        Constraint ci=inst->getConstraint(it, (1 << it) , originalOpnd->getSize());
        assert(!ci.isNull());
        // & the result
        Constraint cr=cc & ci;
        Opnd * opndToSet=currentOpnd;
        if (!cr.isNull()){
            // can substitute currentReplacementOpnd into this position
            currentOpnd->setCalculatedConstraint(cr);
        }else{
            // cannot substitute currentReplacementOpnd into this position, needs splitting
            opndToSet=irManager.newOpnd( originalOpnd->getType(), ci | Constraint(OpndKind_Mem, ci.getSize()) );
            Inst * copySequence=irManager.newCopyPseudoInst(Mnemonic_MOV, currentOpnd, opndToSet);  
            // split after the defining instruction
            copySequence->insertAfter(inst);
            if (inst->getOpndRoles(it)&Inst::OpndRole_Use){
                // This is def&use case (like add t0, t1 for t0)
                if (!needsOriginalOpnd.getBit(originalOpnd->getId())){
                    // use the new operand for all the instructions above
                    opndReplaceWorkset[originalOpnd->getId()]=opndToSet;
                }else{
                    // use the original operand for all the instructions above
                    assert(currentOpnd==originalOpnd);
                    Inst * copySequence=irManager.newCopyPseudoInst(Mnemonic_MOV, opndToSet, originalOpnd); 
                    // split above the instruction
                    copySequence->insertBefore(inst);
                }
            }
        }   
        // Update liveness
        if (inst->isLiveRangeEnd(it)){ // if pure def, not def&use, terminate live range
            liveOpnds.setBit(originalOpnd->getId(), false);
            // also terminate replacement chain
            opndReplaceWorkset[originalOpnd->getId()] = NULL;
        }
        // need to set the new operand into this place of the instruction
        if (opndToSet!=originalOpnd)
            inst->setOpnd(it, opndToSet);

    }}

    // now handle operands hovering over call insts
    if (inst->getMnemonic()==Mnemonic_CALL){
        // for all operands 
        BitSet::IterB ib(hoveringOpnds);
        for (int i = ib.getNext(); i != -1; i = ib.getNext()){
            Opnd * originalOpnd=irManager.getOpnd(i);
            assert(originalOpnd->getId()<originalOpndCount);
            assert(!needsOriginalOpnd.getBit(originalOpnd->getId())||opndReplaceWorkset[originalOpnd->getId()]==NULL);
            // currentOpnd is either the current replacement or the original operand
            Opnd * currentOpnd=opndReplaceWorkset[originalOpnd->getId()];
            if (currentOpnd==NULL)
                currentOpnd=originalOpnd;
            Opnd * opndToSet=NULL;
            // was live and is not redefined by this inst
            if (liveOpnds.getBit(originalOpnd->getId())){ 
                // Instruction-level constraints are constraints describing locations safe across CALLs 
                Constraint ci=getCalleeSaveConstraint(inst, currentOpnd);
                // we have at least memory
                assert(!ci.isNull());
                Constraint cc=currentOpnd->getConstraint(Opnd::ConstraintKind_Calculated);
                assert(!cc.isNull());
                // & the result
                Constraint cr=cc & ci;
                opndToSet=currentOpnd;
                if ((cr.getMask()!=0 || cc.getMask()==0) && !cr.isNull()){
                    // can substitute currentReplacementOpnd into this position
                    opndToSet->setCalculatedConstraint(cr);
                }else{
                    // cannot substitute currentReplacementOpnd into this position, needs splitting
                    // Try to use originalOpnd over this instruction and for the instructions above
                    Constraint co=originalOpnd->getConstraint(Opnd::ConstraintKind_Calculated);
                    Constraint cr=co & ci;
                    if ((cr.getMask()!=0 || co.getMask()==0)&& !cr.isNull()){ 
                        opndToSet=originalOpnd;
                        opndToSet->setCalculatedConstraint(cr);
                    }else{
                        // cannot use original, create a new one
                        opndToSet=irManager.newOpnd(originalOpnd->getType(), ci | Constraint(OpndKind_Mem, ci.getSize()));
                    }
                }   
            }

            if (opndToSet!=NULL){
                if (opndToSet!=currentOpnd){
                    // an operand different to the current replacement 
                    // is required to be over this call site, append splitting below the call site
                    // this is like restoring from a call-safe location under a call
                    Inst * copySequence=irManager.newCopyPseudoInst(Mnemonic_MOV, currentOpnd, opndToSet);  
                    copySequence->insertAfter(inst);
                }
                if (!needsOriginalOpnd.getBit(originalOpnd->getId()))
                    opndReplaceWorkset[originalOpnd->getId()]=opndToSet; // can use the replacement operand above
                else if (opndToSet!=originalOpnd){
                    // add splitting above 
                    // this is like saving into a call-safe location above a call
                    assert(currentOpnd==originalOpnd);
                    Inst * copySequence=irManager.newCopyPseudoInst(Mnemonic_MOV, opndToSet, originalOpnd);
                    copySequence->insertBefore(inst);
                }
            }
        }
    }
    
    // now handle all uses 
    {Inst::Opnds opnds(inst, Inst::OpndRole_AllUses);
    for (Inst::Opnds::iterator it = opnds.begin(); it != opnds.end(); it = opnds.next(it)){
        Opnd * originalOpnd=inst->getOpnd(it);
        
        if ((inst->getOpndRoles(it)&Inst::OpndRole_Def)==0){  // the use&def case was handled above
            assert(originalOpnd->getId()<originalOpndCount);
            assert(!needsOriginalOpnd.getBit(originalOpnd->getId())||opndReplaceWorkset[originalOpnd->getId()]==NULL);
            // currentOpnd is either the current replacement or the original operand
            Opnd * currentOpnd=opndReplaceWorkset[originalOpnd->getId()];
            if (currentOpnd==NULL)
                currentOpnd=originalOpnd;
            // get what is already collected
            Constraint cc=currentOpnd->getConstraint(Opnd::ConstraintKind_Calculated);
            assert(!cc.isNull());

            Constraint ci=inst->getConstraint(it, (1 << it), originalOpnd->getSize());
            assert(!ci.isNull());
            Constraint cr=cc & ci;

            Opnd * opndToSet=currentOpnd;

            if (!cr.isNull()){
                // can substitute currentReplacementOpnd into this position
                currentOpnd->setCalculatedConstraint(cr);
            }else{
                // cannot substitute currentReplacementOpnd into this position, needs splitting
                // split above the inst, force to insert the new operand into the inst, and use
                // currentOpnd above
                opndToSet=irManager.newOpnd(originalOpnd->getType(), ci | Constraint(OpndKind_Mem, ci.getSize()));
                Inst * copySequence=irManager.newCopyPseudoInst(Mnemonic_MOV, opndToSet, currentOpnd);
                copySequence->insertBefore(inst);
            }   
            // update liveness (for def/use case
            if (inst->isLiveRangeStart(it))
                liveOpnds.setBit(originalOpnd->getId(), true);
            // need to set the new operand into this place of the instruction
            if (opndToSet!=originalOpnd)
                inst->setOpnd(it, opndToSet);
        }
    }}  
}   
    
//_________________________________________________________________________________________________
void ConstraintsResolverImpl::resolveConstraints(Node * bb)
{
    assert(bb->isBlockNode());
    // scan all insts of bb in reverse order
    irManager.getLiveAtExit(bb, liveOpnds);
    for (Inst * inst=(Inst*)bb->getLastInst(), * prevInst=NULL; inst!=NULL; inst=prevInst){
        prevInst=inst->getPrevInst();
        resolveConstraintsWithOG(inst);
    }

    // if we come to bb entry with some replacement for an operand and the operand is live at the entry
    // insert copying from the original operand to the replacement operand
    BitSet * ls = irManager.getLiveAtEntry(bb);
    BitSet::IterB ib(*ls);
    for (int i = ib.getNext(); i != -1; i = ib.getNext()){
        Opnd * originalOpnd = irManager.getOpnd(i);
        assert(originalOpnd->getId()<originalOpndCount);
        Opnd * currentOpnd=opndReplaceWorkset[originalOpnd->getId()];
        if (currentOpnd!=NULL){
            if (currentOpnd!=originalOpnd){
//              assert(irManager.getLiveAtEntry(bb)->isLive(originalOpnd));
                Inst * copySequence=irManager.newCopyPseudoInst(Mnemonic_MOV, currentOpnd, originalOpnd);
                bb->prependInst(copySequence);
            }
            opndReplaceWorkset[originalOpnd->getId()]=NULL;
        }
    }
}

//_________________________________________________________________________________________________
void ConstraintsResolverImpl::resolveConstraints()
{   
    // for all basic blocks in the array
    for (uint32 ibb=0, nbb=basicBlocks.size(); ibb<nbb; ++ibb){
        resolveConstraints(basicBlocks[ibb]);
    }
}   

//_________________________________________________________________________________________________
void ConstraintsResolver::runImpl()
{
    // call the private implementation of the algorithm
    ConstraintsResolverImpl(*irManager).run();
}


//_________________________________________________________________________________________________



}}; //namespace Ia32

