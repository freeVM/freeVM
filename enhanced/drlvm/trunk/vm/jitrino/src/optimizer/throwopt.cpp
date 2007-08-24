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
 * @author Intel, Evgueni Brevnov
 * @version $Revision: 1.1 $
 *
 */

#include "irmanager.h"
#include "LoopTree.h"
#include "Dominator.h"
#include "ssa/SSA.h"

namespace Jitrino {

DEFINE_SESSION_ACTION(ThrowOptimizerPass, throwopt, "Throw Instruction Eliminator")

class ThrowInstEliminator {
public:
    ThrowInstEliminator(IRManager& irm);
    void eliminateThrowInst();
private:
    Node * findExceptionHandler(Type * exceptionType, Node * dispatch_node, StlVector<Inst *> & exit_markers);

    IRManager& irManager;
    ControlFlowGraph& flowGraph;
    InstFactory& instFactory;
    OpndManager& opndManager;
};

void ThrowOptimizerPass::_run(IRManager& irm) {
    ThrowInstEliminator throwopt(irm);
    throwopt.eliminateThrowInst();
}


ThrowInstEliminator::ThrowInstEliminator(IRManager& irm):
    irManager(irm), flowGraph(irm.getFlowGraph()),
    instFactory(irm.getInstFactory()), opndManager(irm.getOpndManager()) {}

void ThrowInstEliminator::eliminateThrowInst() {
    const Nodes& nodes = flowGraph.getNodes();
    StlVector<bool> visited_nodes(irManager.getMemoryManager(), flowGraph.getMaxNodeId(), false);
    StlVector<Inst *> pseudo_insts(irManager.getMemoryManager(), 10);
    LoopTree * loop_tree = irManager.getLoopTree();
    Nodes::const_iterator node_iter;
    bool restore_ssa = false;

    loop_tree->rebuild(false);

    for(node_iter = nodes.begin(); node_iter != nodes.end(); ++node_iter) {
        Node * throw_node = *node_iter;
        Inst * throw_inst = (Inst *)throw_node->getLastInst();
        
        if (throw_inst->getOpcode() != Op_Throw) continue;
        
        Opnd * throw_opnd = throw_inst->getSrc(0);
        Type * throw_type = throw_opnd->getType();
        
        pseudo_insts.clear();

        Node * dispatch_node = throw_node->getExceptionEdgeTarget();
        Node * catch_node = findExceptionHandler(throw_type, dispatch_node, pseudo_insts);

        if (catch_node) {
            assert(catch_node->isCatchBlock());
            // Found target block.
            Node * target_node = catch_node;
            if (catch_node->getSecondInst() == NULL) {
                target_node = catch_node->getUnconditionalEdgeTarget();
            }
            
            if (Log::isEnabled()) {
                Log::out() << "Trying to elimination ";
                throw_inst->print(Log::out());
                Log::out() << std::endl;
            }


            // Avoid jumps inside loop.
            Node * throw_loop_header = loop_tree->getLoopHeader(throw_node, false);
            Node * target_loop_header = loop_tree->getLoopHeader(target_node, false);
            if (// Target node inside loop &&
                target_loop_header != NULL &&
                // Throw node is outside the same loop   ||  Target node is loop header
                (target_loop_header != throw_loop_header ||  target_node == target_loop_header)) {
                 
                if (Log::isEnabled()) {
                    Log::out() << "FAILED: Avoid jump inside loop from node ID " << throw_node->getId()
                        << " to node ID " << target_node->getId() << std::endl;
                }                    
                continue;
            }

            Inst * catch_inst = (Inst *)target_node->getSecondInst();
            while (catch_inst->getOpcode() != Op_Catch) {
                catch_inst = (Inst*)catch_inst->next();
            }
            Opnd * catch_opnd = catch_inst->getDst();
            VarOpnd * dst_var = NULL;
            Inst * st_inst = NULL;
            // Detect true target node.
            if (visited_nodes[target_node->getId()]) {
                st_inst = (Inst *)target_node->getLastInst();
                dst_var = st_inst->getDst()->asVarOpnd();
                target_node = target_node->getUnconditionalEdgeTarget();
            } else {
                // Store exception operand in variable.
                Opnd * new_catch_opnd = opndManager.createSsaTmpOpnd(catch_opnd->getType());
                catch_inst->setDst(new_catch_opnd);
                dst_var = opndManager.createVarOpnd(catch_opnd->getType(), false);
                st_inst = instFactory.makeStVar(dst_var, new_catch_opnd);
                target_node->appendInst(st_inst, catch_inst);                
                // Mark the node as visited.
                visited_nodes[target_node->getId()] = true;
                // Split target node if required.
                if (target_node->getInstCount() > 2) {
                    target_node = flowGraph.splitNodeAtInstruction(st_inst, true, false, instFactory.makeLabel());
                } else {
                    target_node = target_node->getUnconditionalEdgeTarget();
                }
                target_node->prependInst(instFactory.makeLdVar(catch_opnd, dst_var));
            }

            // Replace "throw" wtih "stVar" instruction.
            throw_inst->unlink();
            throw_node->appendInst(instFactory.makeStVar(dst_var, throw_opnd));

            // Add direct jump.
            flowGraph.addEdge(throw_node, target_node);

            // Add pseudo instructions from exeption path.            
            if (pseudo_insts.size() > 0) {
                Node * pseudo_node = flowGraph.spliceBlockOnEdge(
                    throw_node->getUnconditionalEdge(), instFactory.makeLabel());
                
                Inst * last_phi_inst = NULL;
                for(StlVector<Inst *>::iterator it = pseudo_insts.begin(); it != pseudo_insts.end(); it++) {
                    Inst * inst = *it;
                    Inst * new_inst = NULL;
                    if (inst->isMethodMarker()) {
                        MethodMarkerInst * marker_inst = (MethodMarkerInst *)inst;
                        new_inst = instFactory.makeMethodMarker(marker_inst->isMethodEntryMarker()
                            ? MethodMarkerInst::Entry : MethodMarkerInst::Exit, marker_inst->getMethodDesc());
                        pseudo_node->appendInst(new_inst);
                    } else if (inst->isPhi()) {
                        // Replace destination operand.
                        Opnd * orig_dst = inst->getDst();
                        VarOpnd * var = orig_dst->asSsaVarOpnd()->getVar();
                        Opnd * dst1 = opndManager.createSsaVarOpnd(var);
                        inst->setDst(dst1);
                        // Create phi instruction to stick source operands on new path.
                        uint32 num_opnds = inst->getNumSrcOperands();
                        Opnd** opnds = new (irManager.getMemoryManager()) Opnd*[num_opnds];
                        for (uint32 i = 0; i < num_opnds; ++i) {
                            opnds[i] = inst->getSrc(i);
                        }
                        Opnd * dst2 = opndManager.createSsaVarOpnd(var);
                        new_inst = instFactory.makePhi(dst2, num_opnds, opnds);
                        // Phi is a header critical instruction thus it must go in the beginning.
                        if (last_phi_inst == NULL) {
                            pseudo_node->prependInst(new_inst);
                        } else {
                            pseudo_node->appendInst(new_inst, last_phi_inst);
                        }
                        last_phi_inst = new_inst;
                        // Create one more phi to stick destination operands.
                        Opnd * dst_opnds[] = {dst1, dst2};
                        new_inst = instFactory.makePhi(orig_dst, 2, dst_opnds);
                        target_node->prependInst(new_inst);
                    } else {
                        assert("Unexcpected type of instruction in dispatch node.");
                    }
                }
            }

            // Rebuild loop tree after modifications.
            loop_tree->rebuild(false);
            restore_ssa = true;

            if (Log::isEnabled()) {
                Log::out() << "SUCCEED: Inserting direct jump from node ID " << throw_node->getId()
                    << " to node ID " << target_node->getId() << std::endl;
            }                    
        }
    }

    if (restore_ssa) {
        OptPass::computeDominators(irManager);
        DominatorTree* dominatorTree = irManager.getDominatorTree();
        
        DomFrontier frontier(irManager.getNestedMemoryManager(), *dominatorTree, &flowGraph);
        SSABuilder ssaBuilder(opndManager, instFactory, frontier, &flowGraph, irManager.getOptimizerFlags());
        bool phiInserted = ssaBuilder.fixupVars(&flowGraph, irManager.getMethodDesc());
        irManager.setInSsa(true);
        if (phiInserted)
            irManager.setSsaUpdated();
    }
}

Node * ThrowInstEliminator::findExceptionHandler(Type * exceptionType, Node * dispatch_node, StlVector<Inst *> & pseudo_insts) {
    const Edges & catch_eges = dispatch_node->getOutEdges();
    Edges::const_iterator edge_iter;

    assert(dispatch_node != NULL);

    // Scan dispatch node for possible method end markers.
    Inst * inst = (Inst *)dispatch_node->getSecondInst();
    while (inst != NULL) {
        pseudo_insts.push_back(inst);
        inst = inst->getNextInst();
    }

    for(edge_iter = catch_eges.begin(); edge_iter != catch_eges.end(); ++edge_iter) {
        Edge * catch_edge = *edge_iter;
        Node * catch_node = catch_edge->getTargetNode();
        
        if (!catch_node->isBlockNode()) continue;
        
        CatchLabelInst * catch_label_inst =
            ((Inst *)catch_node->getFirstInst())->asCatchLabelInst();
        Type * catch_type = catch_label_inst->getExceptionType();
        if (irManager.getTypeManager().isSubTypeOf(exceptionType, catch_type)) {
            return catch_node;
        }
    }

    // Exception handler was not found.
    // Go to next dispatch node.
    dispatch_node = dispatch_node->getExceptionEdgeTarget();

    if (dispatch_node && dispatch_node->isDispatchNode()) {
        return findExceptionHandler(exceptionType, dispatch_node, pseudo_insts);
    }

    return NULL;
}

} //namespace Jitrino
