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
*/

#include "PMFAction.h"
#include "optpass.h"
#include "inliner.h"
#include "LoopTree.h"
#include "Dominator.h"

namespace Jitrino {

struct HelperInlinerFlags {
    const char* inlinerPipelineName;

    bool insertInitilizers;
    bool doInlining;

#define DECLARE_STANDARD_HELPER_FLAGS(name) \
    bool  name##_doInlining;\
    int   name##_hotnessPercentToInline;\
    const char* name##_className;\
    const char* name##_methodName;\
    const char* name##_signature;\

DECLARE_STANDARD_HELPER_FLAGS(newObj);
DECLARE_STANDARD_HELPER_FLAGS(newArray);
DECLARE_STANDARD_HELPER_FLAGS(objMonEnter);
DECLARE_STANDARD_HELPER_FLAGS(objMonExit);
DECLARE_STANDARD_HELPER_FLAGS(wb);
    
};

class HelperInlinerAction: public Action {
public:
    void init();
    HelperInlinerFlags& getFlags() {return flags;}
protected:
    HelperInlinerFlags flags;
};

DEFINE_SESSION_ACTION_WITH_ACTION(HelperInlinerSession, HelperInlinerAction, inline_helpers, "VM helpers inlining");

void HelperInlinerAction::init() {
    flags.inlinerPipelineName = getStringArg("pipeline", "inliner_pipeline");
    flags.insertInitilizers = getBoolArg("insertInitilizers", false);
    flags.doInlining = true;
    
    
#define READ_STANDARD_HELPER_FLAGS(name)\
    flags.name##_doInlining = getBoolArg(#name, false);\
    if (flags.name##_doInlining) {\
    flags.name##_className = getStringArg(#name"_className", NULL);\
    flags.name##_methodName = getStringArg(#name"_methodName", NULL);\
    flags.name##_hotnessPercentToInline = getIntArg(#name"_hotnessPercent", 0);\
        if (flags.name##_className == NULL || flags.name##_methodName == NULL) {\
            if (Log::isEnabled()) {\
                Log::out()<<"Invalid fast path helper name:"<<flags.name##_className<<"::"<<flags.name##_methodName<<std::endl;\
            }\
            flags.name##_doInlining = false;\
        }\
    }\
    if (!flags.name##_doInlining){\
        flags.name##_className = NULL;\
        flags.name##_methodName = NULL;\
    }\

    READ_STANDARD_HELPER_FLAGS(newObj);
    flags.newObj_signature = "(II)Lorg/vmmagic/unboxed/Address;";

    READ_STANDARD_HELPER_FLAGS(newArray);
    flags.newArray_signature = "(III)Lorg/vmmagic/unboxed/Address;";

    READ_STANDARD_HELPER_FLAGS(objMonEnter);
    flags.objMonEnter_signature = "(Ljava/lang/Object;)V";

    READ_STANDARD_HELPER_FLAGS(objMonExit);
    flags.objMonExit_signature = "(Ljava/lang/Object;)V";

    READ_STANDARD_HELPER_FLAGS(wb);
    flags.wb_signature = "(Lorg/vmmagic/unboxed/Address;Lorg/vmmagic/unboxed/Address;Lorg/vmmagic/unboxed/Address;)V";

}


class HelperInliner {
public:
    HelperInliner(HelperInlinerSession* _sessionAction, MemoryManager& tmpMM, CompilationContext* _cc, Inst* _inst)  
        : flags(((HelperInlinerAction*)_sessionAction->getAction())->getFlags()), localMM(tmpMM), 
        cc(_cc), inst(_inst), action(_sessionAction), method(NULL)
    {
        irm = cc->getHIRManager();
        instFactory = &irm->getInstFactory();
        opndManager = &irm->getOpndManager();
        typeManager = &irm->getTypeManager();
        cfg = &irm->getFlowGraph();
    }

    virtual ~HelperInliner(){};
    
    virtual void run()=0;
protected:
    MethodDesc* ensureClassIsResolvedAndInitialized(const char* className,  const char* methodName, const char* signature);
    virtual void doInline() = 0;
    void inlineVMHelper(MethodCallInst* call);

    HelperInlinerFlags& flags;
    MemoryManager& localMM;
    CompilationContext* cc;
    Inst* inst;
    HelperInlinerSession* action;
    MethodDesc*  method;

//these fields used by almost every subclass -> cache them
    IRManager* irm;
    InstFactory* instFactory;
    OpndManager* opndManager;
    TypeManager* typeManager;
    ControlFlowGraph* cfg;

};
#define DECLARE_HELPER_INLINER(name, flagPrefix)\
class name : public HelperInliner {\
public:\
    name (HelperInlinerSession* session, MemoryManager& tmpMM, CompilationContext* cc, Inst* inst)\
        : HelperInliner(session, tmpMM, cc, inst){}\
    \
    virtual void run() { \
        if (Log::isEnabled())  {\
            Log::out() << "Processing inst:"; inst->print(Log::out()); Log::out()<<std::endl; \
        }\
        method = ensureClassIsResolvedAndInitialized(flags.flagPrefix##_className, flags.flagPrefix##_methodName, flags.flagPrefix##_signature);\
        if (!method) return;\
        doInline();\
    }\
    virtual void doInline();\
};\

DECLARE_HELPER_INLINER(NewObjHelperInliner, newObj)
DECLARE_HELPER_INLINER(NewArrayHelperInliner, newArray)
DECLARE_HELPER_INLINER(ObjMonitorEnterHelperInliner, objMonEnter)
DECLARE_HELPER_INLINER(ObjMonitorExitHelperInliner, objMonExit)
DECLARE_HELPER_INLINER(WriteBarrierHelperInliner, wb)

void HelperInlinerSession::_run(IRManager& irm) {
    CompilationContext* cc = getCompilationContext();
    MemoryManager tmpMM(1024, "Inline VM helpers");
    HelperInlinerAction* action = (HelperInlinerAction*)getAction();
    HelperInlinerFlags& flags = action->getFlags();
    if (!flags.doInlining) {
        return;
    }
    //finding all helper calls
    ControlFlowGraph& fg = irm.getFlowGraph();
    double entryExecCount = fg.hasEdgeProfile() ? fg.getEntryNode()->getExecCount(): 1;
    StlVector<HelperInliner*> helperInliners(tmpMM);
    const Nodes& nodes = fg.getNodesPostOrder();//process checking only reachable nodes.
    for (Nodes::const_iterator it = nodes.begin(), end = nodes.end(); it!=end; ++it) {
        Node* node = *it;
        int nodePercent = fg.hasEdgeProfile() ? (int)(node->getExecCount()*100/entryExecCount) : 0;
        if (node->isBlockNode()) {
            for (Inst* inst = (Inst*)node->getFirstInst(); inst!=NULL; inst = inst->getNextInst()) {
                Opcode opcode = inst->getOpcode();
                switch(opcode) {
                    case Op_NewObj:
                        if (flags.newObj_doInlining && nodePercent >= flags.newObj_hotnessPercentToInline) {
                            helperInliners.push_back(new (tmpMM) NewObjHelperInliner(this, tmpMM, cc, inst));
                        }
                        break;
                    case Op_NewArray:
                        if (flags.newArray_doInlining && nodePercent >= flags.newArray_hotnessPercentToInline) {
                            helperInliners.push_back(new (tmpMM) NewArrayHelperInliner(this, tmpMM, cc, inst));
                        }
                        break;
                    case Op_TauMonitorEnter:
                        if (flags.objMonEnter_doInlining && nodePercent >= flags.objMonEnter_hotnessPercentToInline) {
                            helperInliners.push_back(new (tmpMM) ObjMonitorEnterHelperInliner(this, tmpMM, cc, inst));
                        }
                        break;
                    case Op_TauMonitorExit:
                        if (flags.objMonExit_doInlining && nodePercent >= flags.objMonExit_hotnessPercentToInline) {
                            helperInliners.push_back(new (tmpMM) ObjMonitorExitHelperInliner(this, tmpMM, cc, inst));
                        }
                        break;
                    case Op_TauStRef:
                        if (flags.wb_doInlining && nodePercent >= flags.wb_hotnessPercentToInline) {
                            helperInliners.push_back(new (tmpMM) WriteBarrierHelperInliner(this, tmpMM, cc, inst));
                        }
                        break;

                    default: break;
                }
            }
        }
    }

    //running all inliners
    //TODO: set inline limit!
    for (StlVector<HelperInliner*>::const_iterator it = helperInliners.begin(), end = helperInliners.end(); it!=end; ++it) {
        HelperInliner* inliner = *it;
        inliner->run();
    }
}


MethodDesc* HelperInliner::ensureClassIsResolvedAndInitialized(const char* className, const char* methodName, const char* signature) 
{
    CompilationInterface* ci = cc->getVMCompilationInterface();
    ObjectType* clazz = ci->resolveClassUsingBootstrapClassloader(className);
    if (!clazz) {
        if (Log::isEnabled()) Log::out()<<"Error: class not found:"<<className<<std::endl;
        flags.doInlining=false;
        return NULL;
    }
    //helper class is resolved here -> check if initialized
    if (clazz->needsInitialization()) {
        if (flags.insertInitilizers) {
            instFactory->makeInitType(clazz)->insertBefore(inst);
        }
        return NULL;
    }
    //helper class is initialized here -> inline it.
    MethodDesc* method = ci->resolveMethod(clazz, methodName, signature);
    if (!method) {
        if (Log::isEnabled()) Log::out()<<"Error: method not found:"<<className<<"::"<<methodName<<signature<<std::endl;;
        return NULL;
    }
    assert (method->isStatic());
    return method;

}

typedef StlVector<MethodCallInst*> InlineVector;

#define PRAGMA_INLINE "org/vmmagic/pragma/InlinePragma"

void HelperInliner::inlineVMHelper(MethodCallInst* origCall) {
    InlineVector  methodsToInline(localMM);
    methodsToInline.push_back(origCall);
    while (!methodsToInline.empty()) {
        MethodCallInst* call = *methodsToInline.rbegin();
        methodsToInline.pop_back();
        if (Log::isEnabled()) {
            Log::out()<<"Inlining VMHelper:";call->print(Log::out());Log::out()<<std::endl;
        }

        CompilationInterface* ci = cc->getVMCompilationInterface();
        
        //now inline the call
        CompilationContext inlineCC(cc->getCompilationLevelMemoryManager(), ci, cc->getCurrentJITContext());
        inlineCC.setPipeline(cc->getPipeline());

        Inliner inliner(action, localMM, *irm, false);
        InlineNode* regionToInline = inliner.createInlineNode(inlineCC, call);

        inliner.connectRegion(regionToInline);

        // Optimize inlined region before splicing
        inlineCC.stageId = cc->stageId;
        Inliner::runInlinerPipeline(inlineCC, flags.inlinerPipelineName);
        cc->stageId = inlineCC.stageId;

        //add all methods with pragma inline into the list.
        const Nodes& nodesInRegion = regionToInline->getIRManager().getFlowGraph().getNodes();
        for (Nodes::const_iterator it = nodesInRegion.begin(), end = nodesInRegion.end(); it!=end; ++it) {
            Node* node = *it;
            for (Inst* inst = (Inst*)node->getFirstInst(); inst!=NULL; inst = inst->getNextInst()) {
                if (inst->isMethodCall()) {
                    MethodCallInst* methodCall = inst->asMethodCallInst();
                    MethodDesc* md = methodCall->getMethodDesc();
                    uint32 nThrows = md->getNumThrows();
                    for (uint32 i=0; i<nThrows;i++) {
                        NamedType* type = md->getThrowType(i);
                        const char* name = type->getName();
                        if (!strcmp(name, PRAGMA_INLINE)) {
                            methodsToInline.push_back(methodCall);
                            if (Log::isEnabled()) {
                                Log::out()<<"Found InlinePragma, adding to the queue:";methodCall->print(Log::out());Log::out()<<std::endl;
                            }
                        }
                    }
                }
            }
        }

        //inline the region
        inliner.inlineRegion(regionToInline, false);
    }
}

void NewObjHelperInliner::doInline() {
#if defined  (_EM64T_) || defined (_IPF_)
    return;
#else
    assert(inst->getOpcode() == Op_NewObj);

    Opnd* dstOpnd= inst->getDst();
    Type * type = dstOpnd->getType();
    assert(type->isObject());
    ObjectType* objType = type->asObjectType();

    if (objType->isFinalizable()) {
        if (Log::isEnabled()) Log::out()<<"Skipping as finalizable: "<<objType->getName()<<std::endl;
        return;
    }
    //replace newObj with call to a method

    //the method signature is (int objSize, int allocationHandle)
    int allocationHandle= (int)objType->getAllocationHandle();
    int objSize=objType->getObjectSize();

    Opnd* tauSafeOpnd = opndManager->createSsaTmpOpnd(typeManager->getTauType());
    instFactory->makeTauSafe(tauSafeOpnd)->insertBefore(inst);
    Opnd* objSizeOpnd = opndManager->createSsaTmpOpnd(typeManager->getInt32Type());
    Opnd* allocationHandleOpnd = opndManager->createSsaTmpOpnd(typeManager->getInt32Type());
    Opnd* callResOpnd = opndManager->createSsaTmpOpnd(typeManager->getUnmanagedPtrType(typeManager->getInt8Type()));
    instFactory->makeLdConst(objSizeOpnd, objSize)->insertBefore(inst);
    instFactory->makeLdConst(allocationHandleOpnd, allocationHandle)->insertBefore(inst);
    Opnd* args[2] = {objSizeOpnd, allocationHandleOpnd};
    MethodCallInst* call = instFactory->makeDirectCall(callResOpnd, tauSafeOpnd, tauSafeOpnd, 2, args, method)->asMethodCallInst();
    call->insertBefore(inst);
    inst->unlink();
    assert(call == call->getNode()->getLastInst());

    //convert address type to managed object type
    Edge* fallEdge= call->getNode()->getUnconditionalEdge();
    assert(fallEdge && fallEdge->getTargetNode()->isBlockNode());
    Modifier mod = Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No);
    Node* fallNode = fallEdge->getTargetNode();
    if (fallNode->getInDegree()>1) {
        fallNode = irm->getFlowGraph().spliceBlockOnEdge(fallEdge, instFactory->makeLabel());
    }
    fallNode->prependInst(instFactory->makeConv(mod, type->tag, dstOpnd, callResOpnd));
    
    inlineVMHelper(call);
#endif
}

void NewArrayHelperInliner::doInline() {
#if defined  (_EM64T_) || defined (_IPF_)
    return;
#else
    assert(inst->getOpcode() == Op_NewArray);

    //the method signature is (int objSize, int allocationHandle)
    Opnd* dstOpnd = inst->getDst();
    ArrayType* arrayType = dstOpnd->getType()->asArrayType();
    int allocationHandle = (int)arrayType->getAllocationHandle();
    Type* elemType = arrayType->getElementType();
    int elemSize = 4; //TODO: EM64T references!
    if (elemType->isDouble() || elemType->isInt8()) {
        elemSize = 8;
    } else if (elemType->isInt2() || elemType->isChar()) {
        elemSize = 2;
    } else  if (elemType->isInt1()) {
        elemSize = 1;
    }

    Opnd* tauSafeOpnd = opndManager->createSsaTmpOpnd(typeManager->getTauType());
    instFactory->makeTauSafe(tauSafeOpnd)->insertBefore(inst);
    Opnd* numElements = inst->getSrc(0);
    Opnd* elemSizeOpnd = opndManager->createSsaTmpOpnd(typeManager->getInt32Type());
    Opnd* allocationHandleOpnd = opndManager->createSsaTmpOpnd(typeManager->getInt32Type());
    instFactory->makeLdConst(elemSizeOpnd, elemSize)->insertBefore(inst);
    instFactory->makeLdConst(allocationHandleOpnd, allocationHandle)->insertBefore(inst);
    Opnd* args[3] = {numElements, elemSizeOpnd, allocationHandleOpnd};
    Opnd* callResOpnd = opndManager->createSsaTmpOpnd(typeManager->getUnmanagedPtrType(typeManager->getInt8Type()));
    MethodCallInst* call = instFactory->makeDirectCall(callResOpnd, tauSafeOpnd, tauSafeOpnd, 3, args, method)->asMethodCallInst();
    call->insertBefore(inst);
    inst->unlink();
    assert(call == call->getNode()->getLastInst());

    //convert address type to managed array type
    Edge* fallEdge= call->getNode()->getUnconditionalEdge();
    assert(fallEdge && fallEdge->getTargetNode()->isBlockNode());
    Modifier mod = Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No);
    Node* fallNode = fallEdge->getTargetNode();
    if (fallNode->getInDegree()>1) {
        fallNode = irm->getFlowGraph().spliceBlockOnEdge(fallEdge, instFactory->makeLabel());
    }
    fallNode->prependInst(instFactory->makeConv(mod, arrayType->tag, dstOpnd, callResOpnd));
    
    inlineVMHelper(call);
#endif
}


void ObjMonitorEnterHelperInliner::doInline() {
#if defined  (_EM64T_) || defined (_IPF_)
    return;
#else
    assert(inst->getOpcode() == Op_TauMonitorEnter);

    Opnd* objOpnd = inst->getSrc(0);
    assert(objOpnd->getType()->isObject());
    Opnd* tauSafeOpnd = opndManager->createSsaTmpOpnd(typeManager->getTauType());
    instFactory->makeTauSafe(tauSafeOpnd)->insertBefore(inst);
    Opnd* args[1] = {objOpnd};
    MethodCallInst* call = instFactory->makeDirectCall(opndManager->getNullOpnd(), tauSafeOpnd, tauSafeOpnd, 1, args, method)->asMethodCallInst();
    call->insertBefore(inst);
    inst->unlink();
    
    
    //if call is not last inst -> make it last inst
    if (call != call->getNode()->getLastInst()) {
        cfg->splitNodeAtInstruction(call, true, true, instFactory->makeLabel());
    }

    //every call must have exception edge -> add it
    if (call->getNode()->getExceptionEdge() == NULL) {
        Node* node = call->getNode();
        Node* dispatchNode = node->getUnconditionalEdgeTarget()->getExceptionEdgeTarget();
        if (dispatchNode == NULL) {
            dispatchNode = cfg->getUnwindNode();
            assert(dispatchNode != NULL); //method with monitors must have unwind, so no additional checks is done
        }
        cfg->addEdge(node, dispatchNode);
    }
    
    inlineVMHelper(call);
#endif
}

void ObjMonitorExitHelperInliner::doInline() {
#if defined  (_EM64T_) || defined (_IPF_)
    return;
#else
    assert(inst->getOpcode() == Op_TauMonitorExit);

    Opnd* objOpnd = inst->getSrc(0);
    assert(objOpnd->getType()->isObject());
    Opnd* tauSafeOpnd = opndManager->createSsaTmpOpnd(typeManager->getTauType());
    instFactory->makeTauSafe(tauSafeOpnd)->insertBefore(inst);
    Opnd* args[1] = {objOpnd};
    MethodCallInst* call = instFactory->makeDirectCall(opndManager->getNullOpnd(), tauSafeOpnd, tauSafeOpnd, 1, args, method)->asMethodCallInst();
    call->insertBefore(inst);
    inst->unlink();

    assert(call == call->getNode()->getLastInst());
    assert(call->getNode()->getExceptionEdge()!=NULL);

    inlineVMHelper(call);
#endif
}

void WriteBarrierHelperInliner::doInline() {
#if defined  (_EM64T_) || defined (_IPF_)
    return;
#else
    assert(inst->getOpcode() == Op_TauStRef);

    Opnd* srcOpnd = inst->getSrc(0);
    Opnd* ptrOpnd = inst->getSrc(1);
    Opnd* objBaseOpnd = inst->getSrc(2);
    assert(srcOpnd->getType()->isObject());
    assert(ptrOpnd->getType()->isPtr());
    assert(objBaseOpnd->getType()->isObject());
    Opnd* tauSafeOpnd = opndManager->createSsaTmpOpnd(typeManager->getTauType());
    instFactory->makeTauSafe(tauSafeOpnd)->insertBefore(inst);
    Opnd* args[3] = {objBaseOpnd, ptrOpnd, srcOpnd};
    MethodCallInst* call = instFactory->makeDirectCall(opndManager->getNullOpnd(), tauSafeOpnd, tauSafeOpnd, 3, args, method)->asMethodCallInst();
    call->insertBefore(inst);
    inst->unlink();
    
    if (call != call->getNode()->getLastInst()) {
        cfg->splitNodeAtInstruction(call, true, true, instFactory->makeLabel());
    }

    //every call must have exception edge -> add it
    if (call->getNode()->getExceptionEdge() == NULL) {
        Node* node = call->getNode();
        Node* dispatchNode = node->getUnconditionalEdgeTarget()->getExceptionEdgeTarget();
        if (dispatchNode == NULL) {
            dispatchNode = cfg->getUnwindNode();
            if (dispatchNode == NULL) {
                dispatchNode = cfg->createDispatchNode(instFactory->makeLabel());
                cfg->setUnwindNode(dispatchNode);
                cfg->addEdge(dispatchNode, cfg->getExitNode());
            }
        }
        cfg->addEdge(node, dispatchNode);
    }

    inlineVMHelper(call);
#endif
}


}//namespace

