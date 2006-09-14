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
 * @version $Revision: 1.26.8.1.4.4 $
 *
 */

#include "Log.h"
#include "methodtable.h"
#include "inliner.h"
#include "irmanager.h"
#include "FlowGraph.h"
#include "Inst.h"
#include "Dominator.h"
#include "Loop.h"
#include "simplifier.h"
#include "JavaByteCodeParser.h"
#include "EdgeProfiler.h"
#include "StaticProfiler.h"
#include "optimizer.h"

namespace Jitrino {

#define MAX_INLINE_GROWTH_FACTOR 170
#define MIN_INLINE_STOP 50
#define MIN_BENEFIT_THRESHOLD 200
#define INLINE_LARGE_THRESHOLD 70


#define MAX_INLINE_GROWTH_FACTOR_PROF 500
#define MIN_INLINE_STOP_PROF 100
// no negative profile benefit for nodes with freq >= 1/10 of entry freq
#define MIN_BENEFIT_THRESHOLD_PROF (MIN_BENEFIT_THRESHOLD / 10) 
#define INLINE_LARGE_THRESHOLD_PROF 150


#define CALL_COST 1

#define INLINE_SMALL_THRESHOLD 12
#define INLINE_SMALL_BONUS 300
#define INLINE_MEDIUM_THRESHOLD 50
#define INLINE_MEDIUM_BONUS 200
#define INLINE_LARGE_PENALTY 500
#define INLINE_LOOP_BONUS 200
#define INLINE_LEAF_BONUS 0
#define INLINE_SYNCH_BONUS 50
#define INLINE_RECURSION_PENALTY 300
#define INLINE_EXACT_ARG_BONUS 0
#define INLINE_EXACT_ALL_BONUS 0
#define INLINE_SKIP_EXCEPTION_PATH true


DEFINE_SESSION_ACTION(InlinePass, inline, "Method Inlining");

Inliner::Inliner(SessionAction* argSource, MemoryManager& mm, IRManager& irm, bool doProfileOnly) 
    : _tmpMM(mm), _toplevelIRM(irm), 
      _typeManager(irm.getTypeManager()), _instFactory(irm.getInstFactory()),
      _opndManager(irm.getOpndManager()),
      _hasProfileInfo(irm.getFlowGraph().hasEdgeProfile()),
      _inlineCandidates(mm), _initByteSize(irm.getMethodDesc().getByteCodeSize()), 
      _currentByteSize(irm.getMethodDesc().getByteCodeSize()), 
      _inlineTree(new (mm) InlineNode(irm, 0, 0)),
      translatorAction(NULL)
{
    
    if (irm.getCompilationInterface().isBCMapInfoRequired()) {
        isBCmapRequired = true;
        MethodDesc* meth = irm.getCompilationInterface().getMethodToCompile();
        bc2HIRMapHandler = new VectorHandler(bcOffset2HIRHandlerName, meth);
    } else {
        isBCmapRequired = false;
        bc2HIRMapHandler = NULL;
    }

    const char* translatorName = argSource->getStringArg("translatorActionName", "translator");
    translatorAction = (TranslatorAction*)PMF::getAction(argSource->getPipeline(), translatorName);
    assert(translatorAction);

    _doProfileOnlyInlining = doProfileOnly;
    _useInliningTranslator = !doProfileOnly;
    
    _maxInlineGrowthFactor = ((double)argSource->getIntArg("growth_factor", doProfileOnly ? MAX_INLINE_GROWTH_FACTOR_PROF : MAX_INLINE_GROWTH_FACTOR)) / 100;
    _minInlineStop = argSource->getIntArg("min_stop", doProfileOnly ? MIN_INLINE_STOP_PROF : MIN_INLINE_STOP);
    _minBenefitThreshold = argSource->getIntArg("min_benefit_threshold", doProfileOnly ? MIN_BENEFIT_THRESHOLD_PROF : MIN_BENEFIT_THRESHOLD);
    
    _inlineSmallMaxByteSize = argSource->getIntArg("inline_small_method_max_size", INLINE_SMALL_THRESHOLD);
    _inlineSmallBonus = argSource->getIntArg("inline_small_method_bonus", INLINE_SMALL_BONUS);
    
    _inlineMediumMaxByteSize = argSource->getIntArg("medium_method_max_size", INLINE_MEDIUM_THRESHOLD);
    _inlineMediumBonus = argSource->getIntArg("medium_method_bonus", INLINE_MEDIUM_BONUS);
    
    _inlineLargeMinByteSize = argSource->getIntArg("large_method_min_size", doProfileOnly ? INLINE_LARGE_THRESHOLD_PROF : INLINE_LARGE_THRESHOLD);
    _inlineLargePenalty = argSource->getIntArg("large_method_penalty", INLINE_LARGE_PENALTY);

    _inlineLoopBonus = argSource->getIntArg("loop_bonus", INLINE_LOOP_BONUS);
    _inlineLeafBonus = argSource->getIntArg("leaf_bonus", INLINE_LEAF_BONUS);
    _inlineSynchBonus = argSource->getIntArg("synch_bonus", INLINE_SYNCH_BONUS);
    _inlineRecursionPenalty = argSource->getIntArg("recursion_penalty", INLINE_RECURSION_PENALTY);
    _inlineExactArgBonus = argSource->getIntArg("exact_single_parameter_bonus", INLINE_EXACT_ARG_BONUS);
    _inlineExactAllBonus = argSource->getIntArg("exact_all_parameter_bonus", INLINE_EXACT_ALL_BONUS);

    _inlineSkipExceptionPath = argSource->getBoolArg("skip_exception_path", INLINE_SKIP_EXCEPTION_PATH);
    const char* skipMethods = argSource->getStringArg("skip_methods", NULL);
    if(skipMethods == NULL) {
        _inlineSkipMethodTable = NULL;
    } else {
        std::string skipMethodsStr = skipMethods;
        _inlineSkipMethodTable = new (_tmpMM) Method_Table(skipMethodsStr.c_str(), "SKIP_METHODS", true);
    }

    _usesOptimisticBalancedSync = argSource->getBoolArg("sync_optimistic", false) ? argSource->getBoolArg("sync_optcatch", true) : false;
}

int32 
Inliner::computeInlineBenefit(Node* node, MethodDesc& methodDesc, InlineNode* parentInlineNode, uint32 loopDepth) 
{
    int32 benefit = 0;

    if (Log::isEnabled()) {
        Log::out() << "Computing Inline benefit for "
                               << methodDesc.getParentType()->getName()
                               << "." << methodDesc.getName() << ::std::endl;
    }
    //
    // Size impact
    //
    uint32 size = methodDesc.getByteCodeSize();
    Log::out() << "  size is " << (int) size << ::std::endl;
    if(size < _inlineSmallMaxByteSize) {
        // Large bonus for smallest methods
        benefit += _inlineSmallBonus;
        Log::out() << "  isSmall, benefit now = " << benefit << ::std::endl;
    } else if(size < _inlineMediumMaxByteSize) {
        // Small bonus for somewhat small methods
        benefit += _inlineMediumBonus;
        Log::out() << "  isMedium, benefit now = " << benefit << ::std::endl;
    } else if(size > _inlineLargeMinByteSize) {
        // Penalty for large methods
        benefit -= _inlineLargePenalty * (loopDepth+1); 
        Log::out() << "  isLarge, benefit now = " << benefit << ::std::endl;
    }
    benefit -= size;
    Log::out() << "  Subtracting size, benefit now = " << benefit << ::std::endl;

    //
    // Loop depth impact - add bonus for deep call sites
    //
    benefit += _inlineLoopBonus*loopDepth;
    Log::out() << "  Loop Depth is " << (int) loopDepth
                           << ", benefit now = " << benefit << ::std::endl;

    //
    // Synch bonus - may introduce synch removal opportunities
    //
    if(methodDesc.isSynchronized()){
        benefit += _inlineSynchBonus;
        Log::out() << "  Method is synchronized, benefit now = " << benefit << ::std::endl;
    }
    //
    // Recursion penalty - discourage recursive inlining
    //
    for(; parentInlineNode != NULL; parentInlineNode = parentInlineNode->getParent()) {
        if(&methodDesc == &(parentInlineNode->getIRManager().getMethodDesc())) {
            benefit -= _inlineRecursionPenalty;
            Log::out() << "  Subtracted one recursion level, benefit now = " << benefit << ::std::endl;
        }
    }
    
    //
    // Leaf bonus - try to inline leaf methods
    //
    if(isLeafMethod(methodDesc)) {
        benefit += _inlineLeafBonus;
        Log::out() << "  Added leaf bonus, benefit now = " << benefit << ::std::endl;
    }

    //
    // Exact argument bonus - may introduce specialization opportunities
    //
    Inst* last = (Inst*)node->getLastInst();
    if(last->getOpcode() == Op_DirectCall) {
        MethodCallInst* call = last->asMethodCallInst();
        assert(call != NULL);
        bool exact = true;
        // first 2 opnds are taus; skip them
        assert(call->getNumSrcOperands() >= 2);
        assert(call->getSrc(0)->getType()->tag == Type::Tau);
        assert(call->getSrc(1)->getType()->tag == Type::Tau);
        for(uint32 i = 2; i < call->getNumSrcOperands(); ++i) {
            Opnd* arg = call->getSrc(i);
            assert(arg->getType()->tag != Type::Tau);
            if(arg->getInst()->isConst()) {
                benefit += _inlineExactArgBonus;
                Log::out() << "  Src " << (int) i
                                       << " is const, benefit now = " << benefit << ::std::endl;
            } else if(arg->getType()->isObject() && Simplifier::isExactType(arg)) {
                benefit += _inlineExactArgBonus;
                Log::out() << "  Src " << (int) i
                                       << " is exacttype, benefit now = " << benefit << ::std::endl;
            }  else {
                exact = false;
                Log::out() << "  Src " << (int) i
                                       << " is inexact, benefit now = " << benefit << ::std::endl;
            }
        }
        if(call->getNumSrcOperands() > 2 && exact) {
            benefit += _inlineExactAllBonus;
            Log::out() << "  Added allexact bonus, benefit now = " << benefit << ::std::endl;
        }
    }        
    
    //
    // Use profile information if available
    //
    if(_doProfileOnlyInlining && _toplevelIRM.getFlowGraph().hasEdgeProfile()) {
        double heatThreshold = _toplevelIRM.getHeatThreshold();
        double nodeCount = node->getExecCount();
        double scale =   nodeCount / heatThreshold;
        if(scale > 100)
            scale = 100;
        // Remove any loop bonus as this is already accounted for in block count
        benefit -= _inlineLoopBonus*loopDepth;
        // Scale by call site 'hotness'.
        benefit = (uint32) ((double) benefit * scale);

        Log::out() << "  HeatThreshold=" << heatThreshold
                               << ", nodeCount=" << nodeCount
                               << ", scale=" << scale
                               << "; benefit now = " << benefit
                               << ::std::endl;
    }
    return benefit;
}

class JavaByteCodeLeafSearchCallback : public JavaByteCodeParserCallback {
public:
    JavaByteCodeLeafSearchCallback() {
        leaf = true;
    }
    void parseInit() {}
    // called after parsing ends, but not if an error occurs
    void parseDone() {}
    // called when an error occurs parsing the byte codes
    void parseError() {}
    bool isLeaf() const { return leaf; }
protected:
    bool leaf;

    void offset(uint32 offset) {};
    void offset_done(uint32 offset) {};
    void nop()  {}
    void aconst_null()  {}
    void iconst(int32)  {}
    void lconst(int64)  {}
    void fconst(float)  {}
    void dconst(double)  {}
    void bipush(int8)  {}
    void sipush(int16)  {}
    void ldc(uint32)  {}
    void ldc2(uint32)  {}
    void iload(uint16 varIndex)  {}
    void lload(uint16 varIndex)  {}
    void fload(uint16 varIndex)  {}
    void dload(uint16 varIndex)  {}
    void aload(uint16 varIndex)  {}
    void iaload()  {}
    void laload()  {}
    void faload()  {}
    void daload()  {}
    void aaload()  {}
    void baload()  {}
    void caload()  {}
    void saload()  {}
    void istore(uint16 varIndex,uint32 off)  {}
    void lstore(uint16 varIndex,uint32 off)  {}
    void fstore(uint16 varIndex,uint32 off)  {}
    void dstore(uint16 varIndex,uint32 off)  {}
    void astore(uint16 varIndex,uint32 off)  {}
    void iastore()  {}
    void lastore()  {}
    void fastore()  {}
    void dastore()  {}
    void aastore()  {}
    void bastore()  {}
    void castore()  {}
    void sastore()  {}
    void pop()  {}
    void pop2()  {}
    void dup()  {}
    void dup_x1()  {}
    void dup_x2()  {}
    void dup2()  {}
    void dup2_x1()  {}
    void dup2_x2()  {}
    void swap()  {}
    void iadd()  {}
    void ladd()  {}
    void fadd()  {}
    void dadd()  {}
    void isub()  {}
    void lsub()  {}
    void fsub()  {}
    void dsub()  {}
    void imul()  {}
    void lmul()  {}
    void fmul()  {}
    void dmul()  {}
    void idiv()  {}
    void ldiv()  {}
    void fdiv()  {}
    void ddiv()  {}
    void irem()  {}
    void lrem()  {}
    void frem()  {}
    void drem()  {}
    void ineg()  {}
    void lneg()  {}
    void fneg()  {}
    void dneg()  {}
    void ishl()  {}
    void lshl()  {}
    void ishr()  {}
    void lshr()  {}
    void iushr()  {}
    void lushr()  {}
    void iand()  {}
    void land()  {}
    void ior()  {}
    void lor()  {}
    void ixor()  {}
    void lxor()  {}
    void iinc(uint16 varIndex,int32 amount)  {}
    void i2l()  {}
    void i2f()  {}
    void i2d()  {}
    void l2i()  {}
    void l2f()  {}
    void l2d()  {}
    void f2i()  {}
    void f2l()  {}
    void f2d()  {}
    void d2i()  {}
    void d2l()  {}
    void d2f()  {}
    void i2b()  {}
    void i2c()  {}
    void i2s()  {}
    void lcmp()  {}
    void fcmpl()  {}
    void fcmpg()  {}
    void dcmpl()  {}
    void dcmpg()  {}
    void ifeq(uint32 targetOffset,uint32 nextOffset)  {}
    void ifne(uint32 targetOffset,uint32 nextOffset)  {}
    void iflt(uint32 targetOffset,uint32 nextOffset)  {}
    void ifge(uint32 targetOffset,uint32 nextOffset)  {}
    void ifgt(uint32 targetOffset,uint32 nextOffset)  {}
    void ifle(uint32 targetOffset,uint32 nextOffset)  {}
    void if_icmpeq(uint32 targetOffset,uint32 nextOffset)  {}
    void if_icmpne(uint32 targetOffset,uint32 nextOffset)  {}
    void if_icmplt(uint32 targetOffset,uint32 nextOffset)  {}
    void if_icmpge(uint32 targetOffset,uint32 nextOffset)  {}
    void if_icmpgt(uint32 targetOffset,uint32 nextOffset)  {}
    void if_icmple(uint32 targetOffset,uint32 nextOffset)  {}
    void if_acmpeq(uint32 targetOffset,uint32 nextOffset)  {}
    void if_acmpne(uint32 targetOffset,uint32 nextOffset)  {}
    void goto_(uint32 targetOffset,uint32 nextOffset)  {}
    void jsr(uint32 offset, uint32 nextOffset)  {}
    void ret(uint16 varIndex)  {}
    void tableswitch(JavaSwitchTargetsIter*)  {}
    void lookupswitch(JavaLookupSwitchTargetsIter*)  {}
    void ireturn(uint32 off)  {}
    void lreturn(uint32 off)  {}
    void freturn(uint32 off)  {}
    void dreturn(uint32 off)  {}
    void areturn(uint32 off)  {}
    void return_(uint32 off)  {}
    void getstatic(uint32 constPoolIndex)  {}
    void putstatic(uint32 constPoolIndex)  {}
    void getfield(uint32 constPoolIndex)  {}
    void putfield(uint32 constPoolIndex)  {}
    void invokevirtual(uint32 constPoolIndex)  { leaf = false; }
    void invokespecial(uint32 constPoolIndex)  { leaf = false; }
    void invokestatic(uint32 constPoolIndex)  { leaf = false; }
    void invokeinterface(uint32 constPoolIndex,uint32 count)  { leaf = false; }
    void new_(uint32 constPoolIndex)  {}
    void newarray(uint8 type)  {}
    void anewarray(uint32 constPoolIndex)  {}
    void arraylength()  {}
    void athrow()  {}
    void checkcast(uint32 constPoolIndex)  {}
    int  instanceof(const uint8* bcp, uint32 constPoolIndex, uint32 off)  {return 3;}
    void monitorenter()  {}
    void monitorexit()  {}
    void multianewarray(uint32 constPoolIndex,uint8 dimensions)  {}
    void ifnull(uint32 targetOffset,uint32 nextOffset)  {}
    void ifnonnull(uint32 targetOffset,uint32 nextOffset) {}
};

bool
Inliner::isLeafMethod(MethodDesc& methodDesc) {
    if(methodDesc.isJavaByteCodes()) {
        uint32 size = methodDesc.getByteCodeSize();
        const Byte* bytecodes = methodDesc.getByteCodes();
        ByteCodeParser parser((const uint8*)bytecodes,size);
        JavaByteCodeLeafSearchCallback leafTester;
        parser.parse(&leafTester);
        return leafTester.isLeaf();
    }
    return false;
}

void
Inliner::reset()
{
    _hasProfileInfo = true; // _irm.getFlowGraph().hasEdgeProfile();
    _inlineCandidates.clear();
}

bool
Inliner::canInlineFrom(MethodDesc& methodDesc)
{
    // 
    // Test if calls in this method should be inlined
    //
    bool doInline = !methodDesc.isClassInitializer() && !methodDesc.getParentType()->isLikelyExceptionType();
    Log::out() << "Can inline from " << methodDesc.getParentType()->getName() << "." << methodDesc.getName() << " == " << doInline << ::std::endl;
    return doInline;
}

bool
Inliner::canInlineInto(MethodDesc& methodDesc)
{
    // 
    // Test if a call to this method should be inlined
    //
    bool doSkip = (_inlineSkipMethodTable == NULL) ? false : _inlineSkipMethodTable->accept_this_method(methodDesc);
    if(doSkip)
        Log::out() << "Skipping inlining of " << methodDesc.getParentType()->getName() << "." << methodDesc.getName() << ::std::endl;    
    bool doInline = !doSkip && !methodDesc.isNative() && !methodDesc.isNoInlining() && !methodDesc.getParentType()->isLikelyExceptionType();
    Log::out() << "Can inline this " << methodDesc.getParentType()->getName() << "." << methodDesc.getName() << " == " << doInline << ::std::endl;
    return doInline;
}

void
Inliner::connectRegion(InlineNode* inlineNode) {
    if(inlineNode == _inlineTree.getRoot())
        // This is the top level graph.
        return;
    
    
    IRManager &inlinedIRM = inlineNode->getIRManager();
    ControlFlowGraph &inlinedFlowGraph = inlinedIRM.getFlowGraph();
    Inst *callInst = inlineNode->getCallInst();
    MethodDesc &methodDesc = inlinedIRM.getMethodDesc();
    
    // Mark inlined region
    Opnd *obj = 0;
    if (methodDesc.isClassInitializer()) {
        assert(callInst->getNumSrcOperands() >= 3);
        Opnd *obj = callInst->getSrc(2); // this parameter for DirectCall
        if (obj && !obj->isNull()) obj = 0;
    }
    Inst* entryMarker = 
        obj ? _instFactory.makeMethodMarker(MethodMarkerInst::Entry, 
        &methodDesc, obj)
        : _instFactory.makeMethodMarker(MethodMarkerInst::Entry, &methodDesc);
    Inst* exitMarker = 
        obj ? _instFactory.makeMethodMarker(MethodMarkerInst::Exit, 
        &methodDesc, obj)
        : _instFactory.makeMethodMarker(MethodMarkerInst::Exit, 
        &methodDesc);
    Node* entry = inlinedFlowGraph.getEntryNode();
    entry->prependInst(entryMarker);
    Node* retNode = inlinedFlowGraph.getReturnNode();
    if(retNode != NULL)
        retNode->appendInst(exitMarker);
    
    // Fuse callsite arguments with incoming parameters
    uint32 numArgs = callInst->getNumSrcOperands() - 2; // omit taus
    Opnd *tauNullChecked = callInst->getSrc(0);
    Opnd *tauTypesChecked = callInst->getSrc(1);
    assert(tauNullChecked->getType()->tag == Type::Tau);
    assert(tauTypesChecked->getType()->tag == Type::Tau);

    // Mark tauNullChecked def inst as nonremovable
    // tauSafe can be here. this is not our case.
    Inst* tauNullCheckInst = tauNullChecked->getInst();
    if(tauNullCheckInst->getOpcode() == Op_TauCheckNull) {
        tauNullCheckInst->setDefArgModifier(NonNullThisArg);
    }

    Inst* first = (Inst*)entry->getFirstInst();
    Inst* inst;
    Opnd* thisPtr = 0;
    uint32 j;
    for(j = 0, inst = first->getNextInst(); j < numArgs && inst != NULL;) {
        switch (inst->getOpcode()) {
        case Op_DefArg:
            {
                Opnd* dst = inst->getDst(); 
                Opnd* src = callInst->getSrc(j+2); // omit taus
                Inst* copy = _instFactory.makeCopy(dst, src);
                copy->insertBefore(inst);
                inst->unlink();
                inst = copy;
                ++j;
                if (!thisPtr) {
                    src = thisPtr;
                }
            }
            break;
        case Op_TauHasType:
            {
                // substitute tau parameter for hasType test of parameter
                Opnd* src = inst->getSrc(0);
                if (tauTypesChecked->getInst()->getOpcode() != Op_TauUnsafe) {
                    // it's worth substituting the tau
                    for (uint32 i=0; i<numArgs; ++i) {
                        if (src == callInst->getSrc(i+2)) {
                            // it is a parameter
                            Opnd* dst = inst->getDst();
                            Inst* copy = _instFactory.makeCopy(dst, tauTypesChecked);
                            copy->insertBefore(inst);
                            inst->unlink();
                            inst = copy;
                        }
                    }
                }
            }
            break;
        case Op_TauIsNonNull:
            {
                // substitute tau parameter for isNonNull test of this parameter
                Opnd* src = inst->getSrc(0);
                if (src == thisPtr) {
                    Opnd* dst = inst->getDst();
                    Inst* copy = _instFactory.makeCopy(dst, tauNullChecked);
                    copy->insertBefore(inst);
                    inst->unlink();
                    inst = copy;
                }
            }
            break;
        case Op_TauHasExactType:
        default:
            break;
        }
        inst = inst->getNextInst();
    }
    assert(j == numArgs);
    
    //
    // Convert returns in inlined region into stvar/ldvar
    //
    if(retNode != NULL) {
        Opnd* dst = callInst->getDst();
        VarOpnd* retVar = NULL;
        if(!dst->isNull())
            retVar = _opndManager.createVarOpnd(dst->getType(), false);
        
        // Iterate return sites
        assert(retNode != NULL);
        bool isSsa = inlinedIRM.getInSsa();
        Opnd** phiArgs = isSsa ? new (_toplevelIRM.getMemoryManager()) Opnd*[retNode->getInDegree()] : NULL;
        uint32 phiCount = 0;
        const Edges& inEdges = retNode->getInEdges();
        Edges::const_iterator eiter;
        for(eiter = inEdges.begin(); eiter != inEdges.end(); ++eiter) {
            Node* retSite = (*eiter)->getSourceNode();
            Inst* ret = (Inst*)retSite->getLastInst();
            assert(ret->getOpcode() == Op_Return);
            if(retVar != NULL) {
                Opnd* tmp = ret->getSrc(0);
                assert(tmp != NULL);
                if(isSsa) {
                    SsaVarOpnd* ssaVar = _opndManager.createSsaVarOpnd(retVar);
                    phiArgs[phiCount++] = ssaVar;
                    retSite->appendInst(_instFactory.makeStVar(ssaVar, tmp));
                } else {
                    retSite->appendInst(_instFactory.makeStVar(retVar, tmp));
                }
            }
            ret->unlink();
        }
        if(retVar != NULL) {
            // Insert phi and ldVar after call site
            Node* joinNode = inlinedFlowGraph.splitReturnNode(_instFactory.makeLabel());
            joinNode->setExecCount(retNode->getExecCount());
            joinNode->findTargetEdge(retNode)->setEdgeProb(1.0);
            if(isSsa) {
                SsaVarOpnd* ssaVar = _opndManager.createSsaVarOpnd(retVar);
                joinNode->appendInst(_instFactory.makePhi(ssaVar, phiCount, phiArgs));
                joinNode->appendInst(_instFactory.makeLdVar(dst, ssaVar));
            } else {
                joinNode->appendInst(_instFactory.makeLdVar(dst, retVar));
            }

            // Set return operand.
            assert(inlinedIRM.getReturnOpnd() == NULL);
            inlinedIRM.setReturnOpnd(dst);
        }
    }
    
    //
    // Insert explicit catch_all/monitor_exit/rethrow at exception exits for a synchronized inlined region
    //
    if(methodDesc.isSynchronized() && inlinedIRM.getFlowGraph().getUnwindNode()) {
        // Add monitor exit to unwind.
        Node* unwind = inlinedFlowGraph.getUnwindNode();
        // Test if an exception exit exists
        if(unwind->getInDegree() > 0) {
            Node* dispatch = inlinedFlowGraph.createDispatchNode(_instFactory.makeLabel());
            
            // Insert catch all
            Opnd* ex = _opndManager.createSsaTmpOpnd(_typeManager.getSystemObjectType());
            Node* handler = inlinedFlowGraph.createBlockNode(inlinedIRM.getInstFactory().makeCatchLabel(0, ex->getType()));
            handler->appendInst(_instFactory.makeCatch(ex));
            if(methodDesc.isStatic()) {
                // Release class monitor
                handler->appendInst(_instFactory.makeTypeMonitorExit(methodDesc.getParentType()));
            } else {
                // Release object monitor
                assert(callInst->getNumSrcOperands() > 2);
                Opnd* obj = callInst->getSrc(2); // object is arg 2;
                const TranslatorFlags& traFlags = translatorAction->getFlags();
                if (!traFlags.ignoreSync && !traFlags.syncAsEnterFence) {
                    if (_usesOptimisticBalancedSync) {
                        // We may need to insert an optimistically balanced monitorexit.
                        // Note that we shouldn't have an un-optimistic balanced monitorexit,
                        // since there is at least one edge to UNWIND; this edge should have
                        // prevented balancing for a synchronized method, which would have
                        // a missing monitorexit on the exception path.
                        
                        Node *aRetNode = retNode;
                        bool done = false;
                        int count = 0;
                        // We should have an optbalmonexit on a return path
                        while (!done && aRetNode != NULL && !aRetNode->getInEdges().empty()) {
                            const Edges& retEdges = aRetNode->getInEdges();
                            if (!retEdges.empty()) {
                                Edge *anEdgeToReturn = *retEdges.begin();
                                Node *aMonexitNode = anEdgeToReturn->getSourceNode();
                                Inst *lastInst = (Inst*)aMonexitNode->getLastInst();
                                Inst *firstInst = (Inst*)aMonexitNode->getFirstInst();
                                while (lastInst != firstInst) {
                                    if (lastInst->getOpcode() == Op_OptimisticBalancedMonitorExit) {
                                        Opnd *srcObj = lastInst->getSrc(0);
                                        Opnd *lockAddr = lastInst->getSrc(1);
                                        Opnd *enterDst = lastInst->getSrc(2);
                                        
                                        handler->appendInst(_instFactory.makeOptimisticBalancedMonitorExit(srcObj,
                                                                                                       lockAddr,
                                                                                                       enterDst));
                                        done = true;
                                        break;
                                    }
                                    lastInst = lastInst->getPrevInst();
                                }
                                if (!done) {
                                    // we may have empty nodes or something, so try a predecessor.
                                    assert(count < 10); // try to bound this search
                                    count += 1;
                                    // we have to search further.
                                    aRetNode = aMonexitNode;
                                    assert(aRetNode);
                                    assert(!aRetNode->getInEdges().empty());
                                }
                            } else {
                                assert(0);
                            }
                        }
                        assert(done);
                    } else {
                        Opnd* tauSafe = _opndManager.createSsaTmpOpnd(_typeManager.getTauType());
                        handler->appendInst(_instFactory.makeTauSafe(tauSafe)); // monenter success guarantees non-null
                        handler->appendInst(_instFactory.makeTauMonitorExit(obj, tauSafe));
                    }
                }
            }
            
            // Insert rethrow
            Node* rethrow = inlinedFlowGraph.createBlockNode(_instFactory.makeLabel());
            rethrow->appendInst(_instFactory.makeThrow(Throw_NoModifier, ex));
            
            // Redirect exception exits to monitor_exit
            while (!unwind->getInEdges().empty()) {
                Edge* edge = unwind->getInEdges().front();
                inlinedFlowGraph.replaceEdgeTarget(edge, dispatch);
            }
            inlinedFlowGraph.addEdge(dispatch, handler);
            inlinedFlowGraph.addEdge(handler, rethrow);
            inlinedFlowGraph.addEdge(handler, unwind);
            inlinedFlowGraph.addEdge(rethrow, unwind);
        }
    }
    
    //
    // Add type initialization if necessary
    //
    if ((methodDesc.isStatic() || methodDesc.isInstanceInitializer()) &&
        methodDesc.getParentType()->needsInitialization()) {
            // initialize type for static methods
            Inst* initType = _instFactory.makeInitType(methodDesc.getParentType());
            entry->prependInst(initType);
            inlinedFlowGraph.splitNodeAtInstruction(initType, true, false, _instFactory.makeLabel());
            Node* unwind = inlinedFlowGraph.getUnwindNode();
            if (unwind == NULL) {
                unwind = inlinedFlowGraph.createDispatchNode(_instFactory.makeLabel());
                inlinedFlowGraph.setUnwindNode(unwind);
                inlinedFlowGraph.addEdge(unwind, inlinedFlowGraph.getExitNode());
            }
            inlinedFlowGraph.addEdge(entry, unwind);
        }
}

void
Inliner::inlineAndProcessRegion(InlineNode* inlineNode) {
    IRManager &inlinedIRM = inlineNode->getIRManager();
    DominatorTree* dtree = inlinedIRM.getDominatorTree();
    LoopTree* ltree = inlinedIRM.getLoopTree();
    ControlFlowGraph &inlinedFlowGraph = inlinedIRM.getFlowGraph();
    Node *callNode = inlineNode->getCallNode();
    Inst *callInst = inlineNode->getCallInst();
    MethodDesc &methodDesc = inlinedIRM.getMethodDesc();
    
    if (Log::isEnabled()) {
        Log::out() << "inlineAndProcessRegion "
                   << methodDesc.getParentType()->getName() << "."
                   << methodDesc.getName() << ::std::endl;
    }

    //
    // Scale block counts in the inlined region for this particular call site
    //
    if (callNode != NULL)
        scaleBlockCounts(callNode, inlinedIRM);
    
    //
    // Update priority queue with calls in this region
    //
    processRegion(inlineNode, dtree, ltree);
    
    //
    // If top level flowgraph 
    //
    if (inlineNode == _inlineTree.getRoot()) {
        Log::out() << "inlineNode is root" << ::std::endl;
        return;
    }

    // 
    // Splice region into top level flowgraph at call site
    //
    assert(callInst->getOpcode() == Op_DirectCall);
    Log::out() << "Inlining " << methodDesc.getParentType()->getName() 
        << "." << methodDesc.getName() << ::std::endl;

    Log::out() 
        << "callee = " << methodDesc.getParentType()->getName() << "." << methodDesc.getName() 
        << ::std::endl
        << "caller = " << inlinedIRM.getParent()->getMethodDesc().getParentType()->getName() << "."
        << inlinedIRM.getParent()->getMethodDesc().getName()
        << ::std::endl;

    //
    // update inlining-related information in all 'call' instructions of the inlined method
    //
    
    const Nodes& nodes = inlinedFlowGraph.getNodes();
    Nodes::const_iterator niter;
    uint16 count = 0;

    assert(callInst->isMethodCall());
    InlineInfo& call_ii = *callInst->asMethodCallInst()->getInlineInfoPtr();

    call_ii.printLevels(Log::out());
    Log::out() << ::std::endl;

    for(niter = nodes.begin(); niter != nodes.end(); ++niter) {
        Node* node = *niter;

        Inst* first = (Inst*)node->getFirstInst();
        Inst* i;
        for(i=first->getNextInst(); i != NULL; i=i->getNextInst()) {
            InlineInfo *ii = i->getCallInstInlineInfoPtr();
            // ii should be non-null for each call instruction
            if ( ii ) {
                //
                // InlineInfo order is parent-first 
                //     (ii might be non-empty when translation-level inlining is on)
                //
                uint32 bcOff = ILLEGAL_VALUE;

                if (isBCmapRequired) {
                    uint64 callIinstID = (uint64) callInst->getId();
                    uint64 iinstID = (uint64) i->getId();
                    bcOff = (uint32)bc2HIRMapHandler->getVectorEntry(iinstID);
                    if (iinstID != callIinstID) {
                        uint32 inlinedBcOff = ILLEGAL_VALUE;
                        inlinedBcOff = (uint32)bc2HIRMapHandler->getVectorEntry(callIinstID);
                        bc2HIRMapHandler->setVectorEntry(iinstID, inlinedBcOff);
                    }
                }

                ii->prependLevel(&methodDesc, bcOff);
                // appropriate byte code offset instead of 0 should be propogated here
                // to enable correct inline info
                ii->prependInlineChain(call_ii);
                // ii->inlineChain remains at the end
                
                Log::out() << "      call No." << count++ << " ";
                ii->printLevels(Log::out());
            }
        }
    }

    //
    // Splice the inlined region into the top-level flowgraph
    //
    IRManager* parent = inlinedIRM.getParent();
    assert(parent);
    
    Edge* edgeToInlined = callNode->getUnconditionalEdge();
    ControlFlowGraph& parentCFG = parent->getFlowGraph();
    parentCFG.spliceFlowGraphInline(edgeToInlined, inlinedFlowGraph);
    parentCFG.removeEdge(callNode->getExceptionEdge());
    callInst->unlink();

}


void Inliner::runTranslatorSession(CompilationContext& inlineCC) {
    TranslatorSession* traSession = (TranslatorSession*)translatorAction->createSession(inlineCC.getCompilationLevelMemoryManager());
    traSession->setCompilationContext(&inlineCC);
    inlineCC.setCurrentSessionAction(traSession);
    traSession->run();
    inlineCC.setCurrentSessionAction(NULL);
}

InlineNode*
Inliner::getNextRegionToInline(CompilationContext& inlineCC) {
    // If in DPGO profiling mode, don't inline if profile information is not available.
    if(_doProfileOnlyInlining && !_toplevelIRM.getFlowGraph().hasEdgeProfile()) {
        return NULL;
    }
    
    Node* callNode = 0;
    InlineNode* inlineParentNode = 0;
    MethodCallInst* call = 0;
    MethodDesc* methodDesc = 0;
    uint32 newByteSize = 0;
    
    //
    // Search priority queue for next inline candidate
    //
    bool found = false;
    while(!_inlineCandidates.empty() && !found) {
        // Find new candidate.
        callNode = _inlineCandidates.top().callNode;
        inlineParentNode = _inlineCandidates.top().inlineNode;
        _inlineCandidates.pop();
        
        call = ((Inst*)callNode->getLastInst())->asMethodCallInst();
        assert(call != NULL);
        methodDesc = call->getMethodDesc();
        
        // If candidate would cause top level method to exceed size threshold, throw away.
        
        uint32 methodByteSize = methodDesc->getByteCodeSize();
        methodByteSize = (methodByteSize <= CALL_COST) ? 1 : methodByteSize-CALL_COST;
        newByteSize = _currentByteSize + methodByteSize;
        double factor = ((double) newByteSize) / ((double) _initByteSize);
        if(newByteSize < _minInlineStop || factor <= _maxInlineGrowthFactor
           || (methodByteSize < _inlineSmallMaxByteSize)) {
            found = true;
        } else {
            Log::out() << "Skip inlining " << methodDesc->getParentType()->getName() << "." << methodDesc->getName() << ::std::endl;
            Log::out() << "methodByteSize=" 
                                   << (int)methodByteSize
                                   << ", newByteSize = " << (int)newByteSize
                                   << ", factor=" << factor
                                   << ::std::endl;
        }
    }
    
    if(!found) {
        // No more candidates.  Done with inlining.
        assert(_inlineCandidates.empty());
        Log::out() << "Done inlining " << ::std::endl;
        return NULL;
    }
    
    //
    // Set candidate as current inlined region
    //
    Log::out() << "Opt:   Inline " << methodDesc->getParentType()->getName() << "." << methodDesc->getName() << 
        methodDesc->getSignatureString() << ::std::endl;
    
    // Generate flowgraph for new region
    oldMethodId = _instFactory.getMethodId();
    uint32 id = methodDesc->getUniqueId();
    _instFactory.setMethodId(((uint64)id)<<32);
    
    
    IRManager* inlinedIRM = new (_tmpMM) IRManager(_tmpMM, _toplevelIRM, *methodDesc, NULL);
    
    // Augment inline tree
    InlineNode *inlineNode = new (_tmpMM) InlineNode(*inlinedIRM, call, callNode);
    
    inlineParentNode->addChild(inlineNode);
    
    // Call a translator 
    if (isBCmapRequired) {
        uint32 methodByteSize = methodDesc->getByteCodeSize();
        size_t incSize = methodByteSize * ESTIMATED_HIR_SIZE_PER_BYTECODE;
        MethodDesc* parentMethod = _toplevelIRM.getCompilationInterface().getMethodToCompile();
        incVectorHandlerSize(bcOffset2HIRHandlerName, parentMethod, incSize);
    }

    inlineCC.setHIRManager(inlinedIRM);
    runTranslatorSession(inlineCC);

    // Save state.
    _currentByteSize = newByteSize;
    assert(inlineNode);
    return inlineNode;
}

void
Inliner::processDominatorNode(InlineNode *inlineNode, DominatorNode* dnode, LoopTree* ltree) {
    if(dnode == NULL)
        return;

    //
    // Process this node for inline candidates
    //
    Node* node = dnode->getNode();
    if(node->isBlockNode()) {
        // Search for call site
        Inst* last =(Inst*)node->getLastInst();
        if(last->getOpcode() == Op_DirectCall) {
            // Process call site
            MethodCallInst* call = last->asMethodCallInst();
            assert(call != NULL);
            MethodDesc* methodDesc = call->getMethodDesc();

            Log::out() << "Considering inlining instruction I"
                                   << (int)call->getId() << ::std::endl;
            if(canInlineInto(*methodDesc)) {
                uint32 size = methodDesc->getByteCodeSize();
                int32 benefit = computeInlineBenefit(node, *methodDesc, inlineNode, ltree->getLoopDepth(node));
                assert(size > 0);
                Log::out() << "Inline benefit " << methodDesc->getParentType()->getName() << "." << methodDesc->getName() << " == " << (int) benefit << ::std::endl;
                if(0 < size && benefit > _minBenefitThreshold) {
                    // Inline candidate
                    Log::out() << "Add to queue" << ::std::endl;
                    _inlineCandidates.push(CallSite(benefit, node, inlineNode));
                } else {
                    Log::out() << "Will not inline" << ::std::endl;
                }
            }
        }
    }

    //
    // Skip exception control flow unless directed
    //
    if(node->isBlockNode() || !_inlineSkipExceptionPath)
        processDominatorNode(inlineNode, dnode->getChild(), ltree);

    // 
    // Process siblings in dominator tree
    //
    processDominatorNode(inlineNode, dnode->getSiblings(), ltree);
}

void
Inliner::processRegion(InlineNode* inlineNode, DominatorTree* dtree, LoopTree* ltree) {
    if(!canInlineFrom(inlineNode->getIRManager().getMethodDesc()))
        return;

    //
    // Process region for inline candidates
    //
    processDominatorNode(inlineNode, dtree->getDominatorRoot(), ltree);
}

void 
Inliner::scaleBlockCounts(Node* callSite, IRManager& inlinedIRM) {
    Log::out() << "Scaling block counts for callsite in block "
                           << (int) callSite->getId() << ::std::endl;
    if(!_toplevelIRM.getFlowGraph().hasEdgeProfile()) {
        // No profile information to scale
        Log::out() << "No profile information to scale!" << ::std::endl;
        return;
    }

    //
    // Compute scale from call site count and inlined method count
    //
    double callFreq = callSite->getExecCount();
    ControlFlowGraph &inlinedRegion = inlinedIRM.getFlowGraph();
    double methodFreq = inlinedRegion.getEntryNode()->getExecCount();
    double scale = callFreq / ((methodFreq != 0.0) ? methodFreq : 1.0);

    Log::out() << "callFreq=" << callFreq
                           << ", methodFreq=" << methodFreq
                           << ", scale=" << scale << ::std::endl;
    //
    // Apply scale to each block in inlined region
    //
    const Nodes& nodes = inlinedRegion.getNodes();
    Nodes::const_iterator i;
    for(i = nodes.begin(); i != nodes.end(); ++i) {
        Node* node = *i;
        node->setExecCount(node->getExecCount()*scale);
    }
}


double 
Inliner::getProfileMethodCount(CompilationInterface& compileIntf, MethodDesc& methodDesc) {
    //
    // Get the entry count for a given method 
    //
    CompilationContext* cc = compileIntf.getCompilationContext();
    if ( cc->hasDynamicProfileToUse() ) {
        // Online
        double res = cc->getProfilingInterface()->getProfileMethodCount(methodDesc);
        return res;
    } else {
        return 0;
    }
}


void 
InlineNode::print(::std::ostream& os) {
    MethodDesc& _methodDesc = getIRManager().getMethodDesc();
    os << _methodDesc.getParentType()->getName() << "." << _methodDesc.getName() << _methodDesc.getSignatureString();
}

void 
InlineNode::printTag(::std::ostream& os) {
    MethodDesc& _methodDesc = getIRManager().getMethodDesc();
    os << "M" << (int) _methodDesc.getId(); 

}



uint32
InlineTree::computeCheckSum(InlineNode* node) {
    if(node == NULL)
        return 0;

    IRManager& irm = node->getIRManager();
    MethodDesc& desc = irm.getMethodDesc();
#ifdef PLATFORM_POSIX
    __gnu_cxx::hash<const char*> hash;
#else
    stdext::hash_compare<const char*> hash; 
#endif
    size_t sum = hash(desc.getParentType()->getName());
    sum += hash(desc.getName());
    sum += computeCheckSum(node->getSiblings());
    sum += computeCheckSum(node->getChild());
    return (uint32) sum;
}

static void runInlinerPipeline(CompilationContext& inlineCC, const char* pipeName) {
    PMF::HPipeline p = inlineCC.getCurrentJITContext()->getPMF().getPipeline(pipeName);
    assert(p!=NULL);
    PMF::PipelineIterator pit(p);
    while (pit.next()) {
        SessionAction* sa = pit.getSessionAction();
        sa->setCompilationContext(&inlineCC);
        inlineCC.setCurrentSessionAction(sa);
        inlineCC.stageId++;
        sa->run();
        inlineCC.setCurrentSessionAction(0);
        assert(!inlineCC.isCompilationFailed() &&  !inlineCC.isCompilationFinished());
    }
}

void InlinePass::_run(IRManager& irm) {

    computeDominatorsAndLoops(irm);

   
    CompilationContext* cc = getCompilationContext();
    MemoryManager& mm = cc->getCompilationLevelMemoryManager() ;
    CompilationInterface* ci = cc->getVMCompilationInterface();
    JITInstanceContext* jit = cc->getCurrentJITContext();

    // Set up Inliner
    bool connectEarly = getBoolArg("connect_early", true);
    const char* pipeName = getStringArg("pipeline", "inliner_pipeline");

    MemoryManager tmpMM(1024, "Inliner::tmp_mm");
    Inliner inliner(this, tmpMM, irm, irm.getFlowGraph().hasEdgeProfile());
    InlineNode* rootRegionNode = (InlineNode*) inliner.getInlineTree().getRoot();
    inliner.inlineAndProcessRegion(rootRegionNode);

    // Inline calls
    do {
        CompilationContext inlineCC(mm, ci, jit);
        inlineCC.setPipeline(cc->getPipeline());
        InlineNode* regionNode = inliner.getNextRegionToInline(inlineCC);
        if (regionNode == NULL) {
            break;
        }
        assert(regionNode != rootRegionNode);
        IRManager &regionManager = regionNode->getIRManager();
        assert(inlineCC.getHIRManager() == &regionManager);

        // Connect region arguments to top-level flowgraph
        if(connectEarly) {
            inliner.connectRegion(regionNode);
        }

        // Optimize inlined region before splicing
        inlineCC.stageId = cc->stageId;
        runInlinerPipeline(inlineCC, pipeName);
        cc->stageId = inlineCC.stageId;
        
        // Splice into flow graph and find next region.
        if(!connectEarly) {
            inliner.connectRegion(regionNode);
        }
        OptPass::computeDominatorsAndLoops(regionManager);
        inliner.inlineAndProcessRegion(regionNode);
    } while (true);
    const OptimizerFlags& optimizerFlags = irm.getOptimizerFlags();
    // Print the results to logging / dot file
    if(optimizerFlags.dumpdot) {
        inliner.getInlineTree().printDotFile(irm.getMethodDesc(), "inlinetree");
    }
    if(Log::isEnabled()) {
        Log::out() << indent(irm) << "Opt: Inline Tree" << ::std::endl;
        inliner.getInlineTree().printIndentedTree(Log::out(), "  ");
    }
   Log::out() << "Inline Checksum == " << (int) inliner.getInlineTree().computeCheckSum() << ::std::endl;
}


} //namespace Jitrino
 
