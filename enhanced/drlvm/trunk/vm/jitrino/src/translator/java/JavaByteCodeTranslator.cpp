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
 * @author Intel, George A. Timoshenko
 * @version $Revision: 1.34.8.4.4.4 $
 */

#include "Stl.h"
#include "JavaByteCodeTranslator.h"
#include "JavaTranslator.h"
#include "Log.h"
#include "ExceptionInfo.h"
#include "simplifier.h"
#include "methodtable.h"
#include "InlineInfo.h"
#include "open/bytecodes.h"
#include "irmanager.h"
#include "Jitrino.h"
#include "EMInterface.h"

#include <assert.h>
#include <stdio.h>

namespace Jitrino {

    
static bool isMagicClass(Type* type) {
#ifdef _EM64T_
    return false;//magics are not tested on EM64T.
#else
    static const char unboxedName[] = "org/vmmagic/unboxed/";
    static const unsigned nameLen = sizeof(unboxedName)-1;
    const char* name = type->getName();
    return !strncmp(name, unboxedName, nameLen);
#endif    
}

static bool isMagicMethod(MethodDesc* md) {
    return isMagicClass(md->getParentType());
}

Type* convertMagicType2HIR(TypeManager& tm, Type* type) {
    if (!type->isObject() || !type->isNamedType()) {
        return type;
    }
    assert(isMagicClass(type));
    const char* name = type->getName();    
    if (!strcmp(name, "org/vmmagic/unboxed/Address") 
        || !strcmp(name, "org/vmmagic/unboxed/ObjectReference")) 
    {
        return tm.getUnmanagedPtrType(tm.getInt8Type());
    } else if (!strcmp(name, "org/vmmagic/unboxed/Word") 
        || !strcmp(name, "org/vmmagic/unboxed/Offset")
        || !strcmp(name, "org/vmmagic/unboxed/Extent")) 
    {
            return tm.getUIntPtrType();
    } else if (!strcmp(name, "org/vmmagic/unboxed/WordArray")
        || !strcmp(name, "org/vmmagic/unboxed/OffsetArray")
        || !strcmp(name, "org/vmmagic/unboxed/ExtentArray") 
        || !strcmp(name, "org/vmmagic/unboxed/AddressArray") 
        || !strcmp(name, "org/vmmagic/unboxed/ObjectReferenceArray")) 
    {
#ifdef _EM64T_
        return tm.getArrayType(tm.getInt64Type(), false);
#else 
        return tm.getArrayType(tm.getInt32Type(), false);
#endif
    }
    assert(0);
    return NULL;
}



//-----------------------------------------------------------------------------
// inlining policy management
//-----------------------------------------------------------------------------

uint32 JavaByteCodeTranslator::MaxSelfSizeForInlining = 10000; // in bytes
uint32 JavaByteCodeTranslator::MaxInlineSize = 33; // in bytes
uint32 JavaByteCodeTranslator::MaxInlineDepth = 4;
float JavaByteCodeTranslator::MaxRelativeInlineHotness = 0.5;


bool JavaByteCodeTranslator::isProfileAllowsInlining(MethodDesc* inlinee) {
    //do not inline methods with not initialized classes -> these paths are cold!
    CompilationContext* ctx = compilationInterface.getCompilationContext();
    ProfilingInterface* pi = ctx->getProfilingInterface();
    if (pi==NULL || !pi->isProfilingEnabled(ProfileType_EntryBackedge, JITProfilingRole_USE)) {
        return true;
    }
    uint32 inlineeHotness = pi->getProfileMethodCount(*inlinee);
    uint32 myHotness = pi->getProfileMethodCount(methodToCompile);
    if (inlineeHotness >= myHotness * MaxRelativeInlineHotness) {
        return true;
    }
    return false;
}

bool 
JavaByteCodeTranslator::inlineMethod(MethodDesc* methodDesc) {
    if (translationFlags.inlineMethods == false)
        return false;
    bool doSkip = (translationFlags.inlineSkipTable == NULL) ? false : translationFlags.inlineSkipTable->accept_this_method(*methodDesc);
    if(doSkip) {
       Log::out() << "Skipping inlining of " << methodDesc->getParentType()->getName() << "." << methodDesc->getName() << ::std::endl;    
        return false;
    }
    ObjectType * methodClass = (ObjectType*)methodDesc->getParentType();

    bool doInline =
        !doSkip &&
        !methodDesc->isNative() &&
        !methodDesc->isNoInlining() &&
        !methodDesc->isSynchronized() &&
        methodDesc->getNumHandlers()==0 &&
        !methodDesc->isRequireSecObject() &&
        !methodClass->isLikelyExceptionType() &&
        isProfileAllowsInlining(methodDesc) &&
        (methodDesc->isInstanceInitializer() || methodDesc->isFinal() || methodDesc->isStatic() 
        || methodDesc->isPrivate() || methodClass->isFinalClass());

    if(!doInline) {
       Log::out() << "Cannot inline " << methodDesc->getParentType()->getName() << "." << methodDesc->getName() << ::std::endl;
        return false;
    }
    uint32 numByteCodes = methodDesc->getByteCodeSize();
    bool result = (numByteCodes > 0 && numByteCodes < MaxInlineSize) && 
        (inlineDepth < MaxInlineDepth);
    if(result)
       Log::out() << "Translator inline " << methodDesc->getParentType()->getName() << "." << methodDesc->getName() << ::std::endl;    
    return result;
}

bool 
JavaByteCodeTranslator::guardedInlineMethod(MethodDesc* methodDesc) {
    if (translationFlags.guardedInlining == false)
        return false;
    if (compilationInterface.getMethodToCompile()->getByteCodeSize() > MaxSelfSizeForInlining)
        return false;

    if (!isProfileAllowsInlining(methodDesc)) {
        return false;
    }
    

    uint32 numByteCodes = methodDesc->getByteCodeSize();
    bool result = (numByteCodes > 0 && numByteCodes < MaxInlineSize) && 
        (inlineDepth < MaxInlineDepth);
    return result;
}

//-----------------------------------------------------------------------------
// JavaByteCodeTranslator constructors
//-----------------------------------------------------------------------------
// version for non-inlined methods

JavaByteCodeTranslator::JavaByteCodeTranslator(CompilationInterface& ci,
                                 MemoryManager& mm,
                                 IRBuilder& irb,
                                 ByteCodeParser& bcp,
                                 MethodDesc& methodDesc,
                                 TypeManager& typeManager,
                                 JavaFlowGraphBuilder& cfg)
    : 
      memManager(mm), 
      compilationInterface(ci), 
      methodToCompile(methodDesc), 
      parser(bcp), 
      typeManager(*irb.getTypeManager()), 
      irBuilder(irb),
      translationFlags(*irb.getTranslatorFlags()),
      cfgBuilder(cfg),
      // CHECK ? for static sync methods must ensure at least one slot on stack for monitor enter/exit code
      opndStack(mm,methodDesc.getMaxStack()+1),
      returnOpnd(NULL),
      returnNode(NULL),
      isInlinedMethod(false),
      inliningExceptionInfo(NULL),
      inlineDepth(0),
      prepass(memManager,
              typeManager,
              irBuilder.getInstFactory()->getMemManager(),
              methodDesc,
              ci,
              NULL),
      lockAddr(NULL), 
      oldLockValue(NULL),
      thisLevelBuilder(NULL, methodDesc,irb.getBcOffset()),
      inlineBuilder(NULL),
      jsrEntryMap(NULL),
      retOffsets(mm),
      jsrEntryOffsets(mm)
{
    initJsrEntryMap();
    if (methodToCompile.isClassInitializer())
        translationFlags.inlineMethods = false;
    // create a prolog block 
    cfgBuilder.genBlock(irBuilder.genMethodEntryLabel(&methodDesc));
    initLocalVars();
    initArgs();
    //
    // load actual parameters into formal parameters
    //
    for (uint32 i=0,j=0; i<numArgs; i++,j++) {
        //
        // for Java this is the same as a local var!
        //
        Opnd *arg;
        if (i == 0 && methodToCompile.isStatic() == false) {
            //
            // for non-inlined, non-static methods, 'this' pointer should have non-null property set
            //
            arg = irBuilder.genArgDef(NonNullThisArg,argTypes[i]);
        } else {
            arg = irBuilder.genArgDef(DefArgNoModifier,argTypes[i]);
        }
        JavaLabelPrepass::JavaVarType javaType = JavaLabelPrepass::getJavaType(argTypes[i]);
        VarOpnd *var = getVarOpndStVar(javaType,j,arg);
        //
        // longs & doubles take up 2 slots
        //
        if (javaType==JavaLabelPrepass::L || javaType==JavaLabelPrepass::D) 
            j++;
        if (var != NULL)
            irBuilder.genStVar(var,arg);
        stateInfo->stack[j].vars = new (memManager) SlotVar(prepass.getVarInc(0, j));
    }
    // check for synchronized methods
    if (methodToCompile.isSynchronized()) {
        if (methodToCompile.isStatic()) {
            irBuilder.genTypeMonitorEnter(methodToCompile.getParentType());
        } else {
            genLdVar(0,JavaLabelPrepass::A);
            genMethodMonitorEnter();
        }
    }

    if(!prepass.allExceptionTypesResolved()) {
        unsigned problemToken = prepass.getProblemTypeToken();
        assert(problemToken != MAX_UINT32);
        linkingException(problemToken,OPCODE_CHECKCAST); // CHECKCAST is suitable here
        noNeedToParse = true;
    }
}


// version for inlined methods

JavaByteCodeTranslator::JavaByteCodeTranslator(CompilationInterface& ci,
                                 MemoryManager& mm,
                                 IRBuilder& irb,
                                 ByteCodeParser& bcp,
                                 MethodDesc& methodDesc,
                                 TypeManager& typeManager,
                                 JavaFlowGraphBuilder& cfg,
                                 uint32 numActualArgs, 
                                 Opnd** actualArgs,
                                 Opnd** returnopnd, // non-null for IR inlining
                                 Node** returnnode, // returns the block where is a return
                                                       // (only one for inlined methods)
                                 ExceptionInfo *inliningexceptinfo,
                                 uint32 inlDepth, bool startNewBlock,
                                 InlineInfoBuilder* parent,
                                 JsrEntryInstToRetInstMap* parentJsrEntryMap)
    : memManager(mm),
      compilationInterface(ci),
      methodToCompile(methodDesc),
      parser(bcp),
      typeManager(*irb.getTypeManager()),
      irBuilder(irb),
      translationFlags(*irb.getTranslatorFlags()),
      cfgBuilder(cfg),
      // CHECK ? for static sync methods must ensure at least one slot on stack for monitor enter/exit code
      opndStack(mm,methodDesc.getMaxStack()+1),
      returnOpnd(returnopnd),
      returnNode(returnnode),
      isInlinedMethod(true),
      inliningExceptionInfo(inliningexceptinfo),
      inlineDepth(inlDepth),
      prepass(memManager,
              typeManager,
              irBuilder.getInstFactory()->getMemManager(),
              methodDesc,
              ci,
              actualArgs),
      lockAddr(NULL), 
      oldLockValue(NULL),
      thisLevelBuilder(parent, methodDesc, irb.getBcOffset()),
      inlineBuilder(&thisLevelBuilder),
      jsrEntryMap(parentJsrEntryMap),
      retOffsets(mm),
      jsrEntryOffsets(mm)
{
    if ( !jsrEntryMap ) {
        // the case for IR inlining
        initJsrEntryMap();
    }
    if (methodToCompile.isSynchronized() || startNewBlock) {
        // create a new basic block
        LabelInst *label = irBuilder.createLabel();
        cfgBuilder.genBlock(label);
        inliningNodeBegin = label->getNode();
    } else {
        inliningNodeBegin = irBuilder.getCurrentLabel()->getNode();
    }
    // create a prolog instruction
    irBuilder.genMethodEntryMarker(&methodDesc);

    if(!prepass.allExceptionTypesResolved()) {
        unsigned problemToken = prepass.getProblemTypeToken();
        assert(problemToken != MAX_UINT32);
        linkingException(problemToken,OPCODE_CHECKCAST); // CHECKCAST is suitable here
        noNeedToParse = true;
        return;
    }

    initLocalVars();
    initArgs();
    assert(numActualArgs == numArgs);
    // load actual parameters into formal parameters
    for (uint32 i=0,j=0; i<numArgs; i++,j++) {
        // generate argument coercion
        Opnd *arg = irBuilder.genArgCoercion(argTypes[i],actualArgs[i]);
        args[i] = arg;
        JavaLabelPrepass::JavaVarType javaType= JavaLabelPrepass::getJavaType(argTypes[i]);
        VarOpnd *var = getVarOpndStVar(javaType,j,arg);
        if (javaType==JavaLabelPrepass::L || javaType==JavaLabelPrepass::D) 
            j++;
        if (var != NULL)
            irBuilder.genStVar(var,arg);
    }
    // check for synchronized methods
    if (methodToCompile.isSynchronized()) {
        if (methodToCompile.isStatic()) {
            irBuilder.genTypeMonitorEnter(methodToCompile.getParentType());
        } else {
            genLdVar(0,JavaLabelPrepass::A);
            genMethodMonitorEnter();
        }
    }
}

//-----------------------------------------------------------------------------
// initialization helpers
//-----------------------------------------------------------------------------
void 
JavaByteCodeTranslator::initJsrEntryMap()
{
    MemoryManager& ir_mem_manager = irBuilder.getIRManager()->getMemoryManager();
    jsrEntryMap = new (ir_mem_manager) JsrEntryInstToRetInstMap(ir_mem_manager);
}

void 
JavaByteCodeTranslator::initLocalVars() {
    //
    // perform label prepass in this method
    //
    parser.parse(&prepass);
    prepass.parseDone();
    prepassVisited = prepass.bytecodevisited;
    moreThanOneReturn = false;
    jumpToTheEnd      = false;
    lastInstructionWasABranch = false;

    // compute number of labels
    numLabels = prepass.getNumLabels();
    labels = new (memManager) LabelInst*[numLabels+1];
    if (isInlinedMethod && methodToCompile.isSynchronized()) {
        jumpToTheEnd = true;
        numLabels++;
    }
    irBuilder.createLabels(numLabels,labels);
    nextLabel = 0;
    resultOpnd = NULL;

    numVars = methodToCompile.getNumVars();
    numStackVars = prepass.getStateTable()->getMaxStackOverflow()-numVars;
    stateInfo = &prepass.stateInfo;
    for (uint32 k=0; k < numVars+numStackVars; k++) {
        struct StateInfo::SlotInfo *slot = &stateInfo->stack[k];
        slot->type = NULL;
        slot->slotFlags = 0;
        slot->vars = NULL;
    }
    stateInfo->stackDepth = numVars;
    javaTypeMap[JavaLabelPrepass::I]   = typeManager.getInt32Type();
    javaTypeMap[JavaLabelPrepass::L]   = typeManager.getInt64Type();
    javaTypeMap[JavaLabelPrepass::F]   = typeManager.getSingleType();
    javaTypeMap[JavaLabelPrepass::D]   = typeManager.getDoubleType();
    javaTypeMap[JavaLabelPrepass::A]   = typeManager.getSystemObjectType();
    javaTypeMap[JavaLabelPrepass::RET] = typeManager.getIntPtrType();
    prepass.createMultipleDefVarOpnds(&irBuilder);

}

void 
JavaByteCodeTranslator::initArgs() {
    MethodSignatureDesc* methodSignatureDesc = methodToCompile.getMethodSig();
    // incoming argument and return value information
    numArgs = methodSignatureDesc->getNumParams();
    retType = methodSignatureDesc->getReturnType();
    argTypes = new (memManager) Type*[numArgs];
    args = new (memManager) Opnd*[numArgs];
    for (uint16 i=0; i<numArgs; i++) {
        Type* argType = methodSignatureDesc->getParamType(i);
        // argType == NULL if it fails to be resolved. Respective exception
        // will be thrown at the point of usage
        argTypes[i] = argType != NULL ? argType : typeManager.getNullObjectType();
    }
}

//-----------------------------------------------------------------------------
// variable management helpers
//-----------------------------------------------------------------------------
// returns either VarOpnd or Opnd. If returning is operand, do not generate the LdVar, only
// push the operand
//
Opnd* 
JavaByteCodeTranslator::getVarOpndLdVar(JavaLabelPrepass::JavaVarType javaType,uint32 index) {
    if (index >= numVars+numStackVars)
        // error: invalid local variable id
        invalid();
    struct StateInfo::SlotInfo slot = stateInfo->stack[index];
    assert(slot.vars);
    Opnd* var = slot.vars->getVarIncarnation()->getOrCreateOpnd(&irBuilder);
    return var;
}

// returns either VarOpnd or null. If null, does not generate the StVar

VarOpnd* 
JavaByteCodeTranslator::getVarOpndStVar(JavaLabelPrepass::JavaVarType javaType,
                                        uint32 index, 
                                        Opnd* opnd) {
    if (index >= numVars+numStackVars)
        // error: invalid local variable id
        invalid();
    VariableIncarnation* varInc = prepass.getVarInc(currentOffset, index);
    assert(varInc);
    StateInfo::SlotInfo* slot = &stateInfo->stack[index];
    slot->vars = new (memManager) SlotVar(varInc);
    Opnd* var = varInc->getOpnd();
    if (var) {
        assert(var->isVarOpnd());
        return var->asVarOpnd();
    } else {
        slot->type = typeManager.toInternalType(opnd->getType());
        slot->slotFlags = 0;
        if (isNonNullOpnd(opnd))
            StateInfo::setNonNull(slot);
        if (isExactTypeOpnd(opnd))
            StateInfo::setExactType(slot);
        varInc->setTmpOpnd(opnd);
        return NULL;
    }
}

//-----------------------------------------------------------------------------
// operand stack manipulation helpers
//-----------------------------------------------------------------------------
void    
JavaByteCodeTranslator::pushOpnd(Opnd* opnd) {
    assert(opnd->getInst());
    opndStack.push(opnd);
}

Opnd*    
JavaByteCodeTranslator::topOpnd() {
    return opndStack.top();
}

Opnd*    
JavaByteCodeTranslator::popOpnd() {
    Opnd *top = opndStack.pop();
    setStackOpndAliveOpnd(top,true);
    return top;
}

Opnd*    
JavaByteCodeTranslator::popOpndStVar() {
    return opndStack.pop();
}

//
// Called at the end of each basic block to empty out the operand stack
//
void 
JavaByteCodeTranslator::checkStack() {
    int numElems = opndStack.getNumElems();
    for (int i = numElems-1; i >= 0; i--) {
        Opnd* opnd = popOpndStVar();
        JavaLabelPrepass::JavaVarType javaType = 
            JavaLabelPrepass::getJavaType(opnd->getType());

        VarOpnd* var = getVarOpndStVar(javaType,(uint16)(numVars+i),opnd);
        // simple optimization
        if(var != NULL) {
            Inst* srcInst = opnd->getInst();
            assert(srcInst);
            if ((srcInst->getOpcode() != Op_LdVar) || 
                (srcInst->getSrc(0)->getId() != var->getId())) {
                irBuilder.genStVar(var,opnd);
                setStackOpndAliveOpnd(opnd,true);
            } else {
            }
        }
    }
}
//-----------------------------------------------------------------------------
// constant pool resolution helpers
//-----------------------------------------------------------------------------
FieldDesc*    
JavaByteCodeTranslator::resolveField(uint32 cpIndex, bool putfield) {
    return compilationInterface.resolveField(&methodToCompile, cpIndex, putfield);
}

FieldDesc*    
JavaByteCodeTranslator::resolveStaticField(uint32 cpIndex, bool putfield) {
    return compilationInterface.resolveStaticField(&methodToCompile, cpIndex, putfield);
}

MethodDesc*    
JavaByteCodeTranslator::resolveVirtualMethod(uint32 cpIndex) {
    return compilationInterface.resolveVirtualMethod(&methodToCompile,cpIndex);
}

MethodDesc*    
JavaByteCodeTranslator::resolveSpecialMethod(uint32 cpIndex) {
    return compilationInterface.resolveSpecialMethod(&methodToCompile,cpIndex);
}

MethodDesc*    
JavaByteCodeTranslator::resolveStaticMethod(uint32 cpIndex) {
    return compilationInterface.resolveStaticMethod(&methodToCompile,cpIndex);
}

MethodDesc*    
JavaByteCodeTranslator::resolveInterfaceMethod(uint32 cpIndex) {
    return compilationInterface.resolveInterfaceMethod(&methodToCompile,cpIndex);
}

NamedType*    
JavaByteCodeTranslator::resolveType(uint32 cpIndex) {
    return compilationInterface.resolveNamedType(&methodToCompile,cpIndex);
}

NamedType*    
JavaByteCodeTranslator::resolveTypeNew(uint32 cpIndex) {
    return compilationInterface.resolveNamedTypeNew(&methodToCompile,cpIndex);
}

const char*
JavaByteCodeTranslator::methodSignatureString(uint32 cpIndex) {
    return compilationInterface.methodSignatureString(&methodToCompile,cpIndex);
}

uint32 
JavaByteCodeTranslator::labelId(uint32 offset) {
    uint32 labelId = prepass.getLabelId(offset);
    if (labelId == (uint32) -1)
        jitrino_assert(compilationInterface, 0);
    return labelId;
}

//-----------------------------------------------------------------------------
// misc JavaByteCodeParserCallback methods
//-----------------------------------------------------------------------------

// called when invalid byte code is encountered
void 
JavaByteCodeTranslator::invalid() {
    jitrino_assert(compilationInterface,0);
}

// called when an error occurs during the byte code parsing
void 
JavaByteCodeTranslator::parseError() {
    jitrino_assert(compilationInterface,0);
}

void 
JavaByteCodeTranslator::offset(uint32 offset) {

    // set bc offset in ir builder
    if (compilationInterface.isBCMapInfoRequired()) irBuilder.setBcOffset(offset);

    if (prepass.isLabel(offset) == false)
        return;
    if (prepassVisited && prepassVisited->getBit(offset) == false) {
        getNextLabelId(); // skip this DEAD byte code
        return;
    }

    // start a new basic block
    Log::out() << "TRANSLATOR BASICBLOCK " << (int32)offset << " " << ::std::endl;

    // finish the previous basic block, if any work was required
    if (!lastInstructionWasABranch) {
        checkStack();
    }
    lastInstructionWasABranch = false;

    // start with the current basic block
    StateInfo* state = prepass.stateTable->getStateInfo(offset);
    stateInfo->flags = state->flags;
    stateInfo->stackDepth = state->stackDepth;
    stateInfo->exceptionInfo = state->exceptionInfo;
    for(int i=0; i<state->stackDepth; ++i)
        stateInfo->stack[i] = state->stack[i];
    assert(stateInfo != NULL);
    Type* handlerExceptionType = NULL;
    uint32 lblId = getNextLabelId();
    LabelInst* labelInst = getLabel(lblId);
    ::std::vector<LabelInst*> oldLabels;

    ::std::vector<LabelInst*> catchLabels;

    bool isCatchHandler = false;
    for (ExceptionInfo* exceptionInfo = stateInfo->getExceptionInfo();
         exceptionInfo != NULL;
         exceptionInfo = exceptionInfo->getNextExceptionInfoAtOffset()) {
        if (exceptionInfo->isCatchBlock()) {
            CatchBlock* catchBlock = (CatchBlock*)exceptionInfo;
            Log::out() << "TRY REGION " << (int)exceptionInfo->getBeginOffset() 
                << " " << (int)exceptionInfo->getEndOffset() << ::std::endl;
            CatchHandler *first = ((CatchBlock*)exceptionInfo)->getHandlers();
            if (Log::isEnabled()) {
                for (; first != NULL; first = first->getNextHandler()) {
                    Log::out() << " handler " << (int)first->getBeginOffset() << ::std::endl;
                }
            }
            if (catchBlock->getLabelInst() == NULL) {
                Node *dispatchNode = cfgBuilder.createDispatchNode();
                catchBlock->setLabelInst((LabelInst*)dispatchNode->getFirstInst());
                ((LabelInst*)dispatchNode->getFirstInst())->setState(catchBlock);
            }
            if (labelInst->getState() == NULL) 
                labelInst->setState(catchBlock);
            if(Log::isEnabled()) {
                Log::out() << "LABEL "; labelInst->print(Log::out()); Log::out() << labelInst->getState();
                Log::out() << "CATCH ";catchBlock->getLabelInst()->print(Log::out()); Log::out() << ::std::endl;
            }
        } else if (exceptionInfo->isCatchHandler()) {
            // catch handler block
            isCatchHandler = true;
            CatchHandler* handler = (CatchHandler*)exceptionInfo;
            Log::out() << "CATCH REGION " << (int)exceptionInfo->getBeginOffset() 
                << " " << (int)exceptionInfo->getEndOffset() << ::std::endl;
            if (translationFlags.newCatchHandling) {
                handlerExceptionType = (handlerExceptionType == NULL) ?
                    handler->getExceptionType() :
                    typeManager.getCommonObjectType((ObjectType*) handlerExceptionType, (ObjectType*) handler->getExceptionType());
            } else {
                handlerExceptionType = handler->getExceptionType();
            }
            LabelInst *oldLabel = labelInst;
            oldLabels.push_back(oldLabel);

            if (translationFlags.newCatchHandling) {
                labelInst = (LabelInst*)
                    irBuilder.getInstFactory()->makeCatchLabel(
                                             handler->getExceptionOrder(),
                                             handler->getExceptionType());
                catchLabels.push_back(labelInst);
            } else {
                labelInst = (LabelInst*)
                    irBuilder.getInstFactory()->makeCatchLabel(
                                             labelInst->getLabelId(),
                                             handler->getExceptionOrder(),
                                             handlerExceptionType);
                setLabel(lblId,labelInst);
            }
            labelInst->setState(oldLabel->getState());
            exceptionInfo->setLabelInst(labelInst);
            if(Log::isEnabled()) {
                Log::out() << "LABEL "; labelInst->print(Log::out()); Log::out() << labelInst->getState();
                Log::out() << "CATCH "; handler->getLabelInst()->print(Log::out()); Log::out() << ::std::endl;
            }
        } else {jitrino_assert(compilationInterface,0);}    // only catch blocks should occur in Java
    }
    // generate the label instruction
    if(translationFlags.newCatchHandling && !catchLabels.empty()) {
        for(::std::vector<LabelInst*>::iterator iter = catchLabels.begin(); iter != catchLabels.end(); ++iter) {
            LabelInst* catchLabel = *iter;
            irBuilder.genLabel(catchLabel);
            cfgBuilder.genBlock(catchLabel);
        }
        LabelInst* handlerLabel= getLabel(lblId);
        assert(!handlerLabel->isCatchLabel());
        handlerLabel->setState(labelInst->getState());
        irBuilder.genLabel(handlerLabel);
        cfgBuilder.genBlock(handlerLabel);
    } else {
        if (stateInfo->isFallThroughLabel()) {
            irBuilder.genFallThroughLabel(labelInst);
        } else { 
            irBuilder.genLabel(labelInst);
            // empty out the stack operand
            opndStack.makeEmpty();
        }
        cfgBuilder.genBlock(labelInst);
    }
    //
    // Load var operands where current basic block begins
    //
    for (uint32 k=numVars; k < (uint32)stateInfo->stackDepth; k++) {
        if(Log::isEnabled()) {
            Log::out() << "STACK ";stateInfo->stack[k].type->print(Log::out()); Log::out() << ::std::endl;
        }

        genLdVar(k,prepass.getJavaType(stateInfo->stack[k].type));
    }
    if (isCatchHandler) {
        // for catch handler blocks, generate the catch instruction
        pushOpnd(irBuilder.genCatch(handlerExceptionType));
    } else  if (stateInfo->isSubroutineEntry()) {
        pushOpnd(irBuilder.genSaveRet());
    }
}

void 
JavaByteCodeTranslator::offset_done(uint32 offset) {
    if (prepass.isSubroutineEntry(offset) ) {
        jsrEntryOffsets[offset] = irBuilder.getLastGeneratedInst();
    }
}

//
// called when byte code parsing is done
//
void 
JavaByteCodeTranslator::parseDone() 
{
    OffsetToInstMap::const_iterator ret_i, ret_e;
    for (ret_i = retOffsets.begin(), ret_e = retOffsets.end(); ret_i != ret_e; ++ret_i) {
        uint32 ret_offset = ret_i->first;
        Inst* ret_inst = ret_i->second;
        JavaLabelPrepass::RetToSubEntryMap* ret_to_entry_map = prepass.getRetToSubEntryMapPtr();
        JavaLabelPrepass::RetToSubEntryMap::const_iterator sub_i = ret_to_entry_map->find(ret_offset);
        //
        // jsr target should be found for each ret inst
        //
        assert(sub_i != ret_to_entry_map->end());
        uint32 entry_offset = sub_i->second;
        OffsetToInstMap::const_iterator entry_inst_i = jsrEntryOffsets.find(entry_offset);
        assert(entry_inst_i != jsrEntryOffsets.end());
        Inst* entry_inst = entry_inst_i->second;
        jsrEntryMap->insert(std::make_pair(entry_inst, ret_inst));
    }
    irBuilder.getIRManager()->setJsrEntryMap(jsrEntryMap);

    if (isInlinedMethod) {
        CatchBlock *catchSyncBlock = NULL;
        if (methodToCompile.isSynchronized()) {
            // generate fake exception info to catch any exception in the code
            catchSyncBlock = new (memManager) CatchBlock(0,0,methodToCompile.getByteCodeSize(), MAX_UINT32);
            const Nodes& nodes = cfgBuilder.getCFG()->getNodes();
            Nodes::const_iterator niter = ::std::find(nodes.begin(), nodes.end(), inliningNodeBegin);
            for(; niter != nodes.end(); ++niter) {
                Node* node = *niter;
                LabelInst *first = (LabelInst*)node->getFirstInst();
                if (node->isDispatchNode()) continue;
                ExceptionInfo *existingInfo = (ExceptionInfo*)first->getState();
                if (existingInfo == NULL) {
                    first->setState(catchSyncBlock);
                } else {
                    while (true) {
                        ExceptionInfo *next = existingInfo->getNextExceptionInfoAtOffset();
                        if (next == NULL) {
                            existingInfo->setNextExceptionInfoAtOffset(catchSyncBlock);
                            break;
                        } else if (next == catchSyncBlock) {
                            break;
                        }
                        existingInfo = next;
                    }
                }
            }
        }

        ExceptionInfo *exceptionInfo = inliningExceptionInfo;

        // propagate exception info to the inlined basic blocks
        if (exceptionInfo != NULL) {
            const Nodes& nodes = cfgBuilder.getCFG()->getNodes();
            Nodes::const_iterator niter = ::std::find(nodes.begin(), nodes.end(), inliningNodeBegin);
            for(++niter; niter != nodes.end(); ++niter) {
                Node* node = *niter;
                LabelInst *first = (LabelInst*)(node)->getFirstInst();
                ExceptionInfo *existingInfo = (ExceptionInfo*)first->getState();
                if (existingInfo == NULL) {
                    first->setState(exceptionInfo);   
                } else if (existingInfo != exceptionInfo) {
                    while (true) {
                        ExceptionInfo *next = existingInfo->getNextExceptionInfoAtOffset();
                        if (next == NULL) {
                            existingInfo->setNextExceptionInfoAtOffset(exceptionInfo); 
                            break;
                        } else if (next == exceptionInfo) {
                            break;
                        }
                        existingInfo = next;
                    }
                }
            }
        }

        // fix synchronized methods
        if (methodToCompile.isSynchronized()) {
            // generate fake exception dispatch node
            Node *dispatchNode = cfgBuilder.createDispatchNode();
            // propagate exception info (if any)
            exceptionInfo = inliningExceptionInfo;
            catchSyncBlock->setLabelInst((LabelInst*)dispatchNode->getFirstInst());
            ((LabelInst*)dispatchNode->getFirstInst())->setState(catchSyncBlock);

            // generate basic block to contain the monitor exit
            LabelInst *label = irBuilder.createLabel();
            Type *exceptionType = typeManager.getSystemObjectType();
            CatchLabelInst *


            labelInst =  (CatchLabelInst*)
            irBuilder.getInstFactory()->makeCatchLabel(
                                                      label->getLabelId(),
                                                      0/*order*/,
                                                      exceptionType);

            // generate a catch handler to handle any exception
            CatchHandler* 
                handler = new (irBuilder.getInstFactory()->getMemManager())
                               CatchHandler(0,0,0,catchSyncBlock,exceptionType);
            catchSyncBlock->addHandler(handler);
            // propagate exception info (if any)
            handler->setNextExceptionInfoAtOffset(exceptionInfo);
            labelInst->setState(exceptionInfo);
            handler->setLabelInst(labelInst);

            // generate a basic block to contain the monitor exit and re-throw the exception
            irBuilder.genLabel(labelInst);
            cfgBuilder.genBlock(labelInst);
            Opnd *exception = irBuilder.genCatch(exceptionType);
            if (methodToCompile.isStatic()) {
                irBuilder.genTypeMonitorExit(methodToCompile.getParentType());
            } else {
                genLdVar(0,JavaLabelPrepass::A);
                genMethodMonitorExit();
            }
            irBuilder.genThrow(Throw_NoModifier, exception);
        }

        if ( (resultOpnd == NULL) && (retType != typeManager.getVoidType()) ) {
            //
            // Result-operand is required to insert inlined CFG into parent CFG.
            // So be it, creating result-operand to contain a default value.
            //
            assert(retType); // we should NOT start inlining if return-type has not been resolved

            VarOpnd* retVar = irBuilder.genVarDef(retType, false);
            resultOpnd = irBuilder.genLdVar(retType, retVar);
        }

        if (jumpToTheEnd) {
            //
            // generate a return block
            //
            LabelInst* labelInst = getLabel(getNextLabelId());
            labelInst->setState(exceptionInfo);
            irBuilder.genLabel(labelInst);
            cfgBuilder.genBlock(labelInst);
            if (returnNode != NULL) {
                *returnNode = labelInst->getNode();
            }
            //
            // load the return type
            //
            if (resultOpnd != NULL && resultOpnd->isVarOpnd()) {
                resultOpnd = irBuilder.genLdVar(resultOpnd->getType(),(VarOpnd*)resultOpnd);
                if (returnOpnd != NULL) {
                    resultOpnd->getInst()->setDst(*returnOpnd);
                    resultOpnd = *returnOpnd; // voids an extra copy
                }
            }
        }
        // end the method scope
        if ((numArgs > 0) && (!methodToCompile.isStatic())) {
            Opnd *thisOpnd = args[0];
            // resultOpnd is needed to produce method exit event
            irBuilder.genMethodEndMarker(&methodToCompile, thisOpnd, 
                    returnOpnd != NULL ? *returnOpnd : NULL);
        } else {
            irBuilder.genMethodEndMarker(&methodToCompile, 
                    returnOpnd != NULL ? *returnOpnd : NULL);
        }
    }
}

//-----------------------------------------------------------------------------
// byte code callbacks
//-----------------------------------------------------------------------------

void JavaByteCodeTranslator::nop() {}

//-----------------------------------------------------------------------------
// load constant byte codes
//-----------------------------------------------------------------------------
void 
JavaByteCodeTranslator::aconst_null()     {
    pushOpnd(irBuilder.genLdNull());
}
void 
JavaByteCodeTranslator::iconst(int32 val) {
    pushOpnd(irBuilder.genLdConstant(val));
}
void 
JavaByteCodeTranslator::lconst(int64 val) {
    pushOpnd(irBuilder.genLdConstant(val));
}
void 
JavaByteCodeTranslator::fconst(float val) {
    pushOpnd(irBuilder.genLdConstant(val));
}
void 
JavaByteCodeTranslator::dconst(double val){
    pushOpnd(irBuilder.genLdConstant(val));
}
void 
JavaByteCodeTranslator::bipush(int8 val)  {
    pushOpnd(irBuilder.genLdConstant((int32)val));
}
void 
JavaByteCodeTranslator::sipush(int16 val) {
    pushOpnd(irBuilder.genLdConstant((int32)val));
}
void 
JavaByteCodeTranslator::ldc(uint32 constPoolIndex) {
    // load 32-bit quantity or string from constant pool
    Type* constantType = 
        compilationInterface.getConstantType(&methodToCompile,constPoolIndex);
    Opnd* opnd = NULL;
    if (constantType->isSystemString()) {
        opnd = irBuilder.genLdRef(&methodToCompile,constPoolIndex,constantType);
    } else if (constantType->isSystemClass()) {
        NamedType *literalType = resolveType(constPoolIndex);
        if (!literalType) {
            linkingException(constPoolIndex, OPCODE_LDC);
            pushOpnd(irBuilder.genLdNull());
            return;
        } else {
            opnd = irBuilder.genLdRef(&methodToCompile,constPoolIndex,constantType);
        }
    } else {
        const void* constantAddress =
           compilationInterface.getConstantValue(&methodToCompile,constPoolIndex);
        if (constantType->isInt4()) {
            int32 value = *(int32*)constantAddress;
            opnd = irBuilder.genLdConstant(value);
        } else if (constantType->isSingle()) {
            float value = *(float*)constantAddress;
            opnd = irBuilder.genLdConstant((float)value);
        } else {
            // Invalid type!
            jitrino_assert(compilationInterface,0);
        }
    }
    pushOpnd(opnd);
}

void 
JavaByteCodeTranslator::ldc2(uint32 constPoolIndex) {
    // load 64-bit quantity from constant pool
    Type* constantType = 
        compilationInterface.getConstantType(&methodToCompile,constPoolIndex);

    const void* constantAddress =
        compilationInterface.getConstantValue(&methodToCompile,constPoolIndex);
    Opnd *opnd = NULL; 
    if (constantType->isInt8()) {
        int64 value = *(int64*)constantAddress;
        opnd = irBuilder.genLdConstant((int64)value);
    } else if (constantType->isDouble()) {
        double value = *(double*)constantAddress;
        opnd = irBuilder.genLdConstant((double)value);
    } else {
        // Invalid type!
        jitrino_assert(compilationInterface,0);
    }
    pushOpnd(opnd);
}

//-----------------------------------------------------------------------------
// variable access byte codes
//-----------------------------------------------------------------------------
void 
JavaByteCodeTranslator::iload(uint16 varIndex) {
    genLdVar(varIndex,JavaLabelPrepass::I);
}
void 
JavaByteCodeTranslator::lload(uint16 varIndex) {
    genLdVar(varIndex,JavaLabelPrepass::L);
}
void 
JavaByteCodeTranslator::fload(uint16 varIndex) {
    genLdVar(varIndex,JavaLabelPrepass::F);
}
void 
JavaByteCodeTranslator::dload(uint16 varIndex) {
    genLdVar(varIndex,JavaLabelPrepass::D);
}
void 
JavaByteCodeTranslator::aload(uint16 varIndex) {
    genLdVar(varIndex,JavaLabelPrepass::A);
}
void 
JavaByteCodeTranslator::istore(uint16 varIndex,uint32 off) {
    genStVar(varIndex,JavaLabelPrepass::I);
}
void 
JavaByteCodeTranslator::lstore(uint16 varIndex,uint32 off) {
    genStVar(varIndex,JavaLabelPrepass::L);
}
void 
JavaByteCodeTranslator::fstore(uint16 varIndex,uint32 off) {
    genStVar(varIndex,JavaLabelPrepass::F);
}
void 
JavaByteCodeTranslator::dstore(uint16 varIndex,uint32 off) {
    genStVar(varIndex,JavaLabelPrepass::D);
}
void 
JavaByteCodeTranslator::astore(uint16 varIndex,uint32 off) {
    genTypeStVar(varIndex);
}
//-----------------------------------------------------------------------------
// field access byte codes
//-----------------------------------------------------------------------------
Type* 
JavaByteCodeTranslator::getFieldType(FieldDesc* field, uint32 constPoolIndex) {
    Type* fieldType = field->getFieldType();
    if (!fieldType) {
        // some problem with fieldType class handle. Let's try the constant_pool.
        // (For example if the field type class is deleted, the field is beeing resolved successfully
        // but field->getFieldType() returns NULL in this case)
        fieldType = compilationInterface.getFieldType(&methodToCompile,constPoolIndex);
    }
    return fieldType;
}

void 
JavaByteCodeTranslator::getstatic(uint32 constPoolIndex) {
    FieldDesc* field = resolveStaticField(constPoolIndex, false);
    if (field && field->isStatic()) {
        Type* fieldType = getFieldType(field,constPoolIndex);
        assert(fieldType);
        if (isMagicClass(fieldType)) {
            fieldType = convertMagicType2HIR(typeManager, fieldType);
        }
        pushOpnd(irBuilder.genLdStatic(fieldType,field));
        return;
    }
    // generate helper call for throwing respective exception
    linkingException(constPoolIndex, OPCODE_GETSTATIC);
    Type* type = compilationInterface.getFieldType(&methodToCompile, constPoolIndex);
    ConstInst::ConstValue nullValue;
    pushOpnd(irBuilder.genLdConstant(type,nullValue));
}

void 
JavaByteCodeTranslator::putstatic(uint32 constPoolIndex) {
    FieldDesc* field = resolveStaticField(constPoolIndex, true);
    if (field && field->isStatic()) {
        Type* fieldType = getFieldType(field,constPoolIndex);
        assert(fieldType);
        irBuilder.genStStatic(fieldType,field,popOpnd());
        return;
    }
    // generate helper call for throwing respective exception
    linkingException(constPoolIndex, OPCODE_PUTSTATIC);
    popOpnd();
}

void 
JavaByteCodeTranslator::getfield(uint32 constPoolIndex) {
    FieldDesc* field = resolveField(constPoolIndex, false);
    if (field && !field->isStatic()) {
        Type* fieldType = getFieldType(field,constPoolIndex);
        assert(fieldType);
        if (isMagicClass(fieldType)) {
            fieldType = convertMagicType2HIR(typeManager, fieldType);
        }
        pushOpnd(irBuilder.genLdField(fieldType,popOpnd(),field));
        return;
    }
    // generate helper call for throwing respective exception
    linkingException(constPoolIndex, OPCODE_GETFIELD);
    popOpnd();
    pushOpnd(irBuilder.genLdNull());
}

void 
JavaByteCodeTranslator::putfield(uint32 constPoolIndex) {
    FieldDesc* field = resolveField(constPoolIndex, true);
    if (field && !field->isStatic()) {
        Type* fieldType = getFieldType(field,constPoolIndex);
        assert(fieldType);
        Opnd* value = popOpnd();
        Opnd* ref = popOpnd();
        irBuilder.genStField(fieldType,ref,field,value);
        return;
    }
    // generate helper call for throwing respective exception
    linkingException(constPoolIndex, OPCODE_PUTFIELD);
    popOpnd();
    popOpnd();
}
//-----------------------------------------------------------------------------
// array access byte codes
//-----------------------------------------------------------------------------
void JavaByteCodeTranslator::iaload() {
    genArrayLoad(typeManager.getInt32Type());
}
void JavaByteCodeTranslator::laload() {
    genArrayLoad(typeManager.getInt64Type());
}
void JavaByteCodeTranslator::faload() {
    genArrayLoad(typeManager.getSingleType());
}
void JavaByteCodeTranslator::daload() {
    genArrayLoad(typeManager.getDoubleType());
}
void JavaByteCodeTranslator::aaload() {
    genTypeArrayLoad();
}
void JavaByteCodeTranslator::baload() {
    genArrayLoad(typeManager.getInt8Type());
}
void JavaByteCodeTranslator::caload() {
    genArrayLoad(typeManager.getCharType());
}
void JavaByteCodeTranslator::saload() {
    genArrayLoad(typeManager.getInt16Type());
}
void JavaByteCodeTranslator::iastore() {
    genArrayStore(typeManager.getInt32Type());
}
void JavaByteCodeTranslator::lastore() {
    genArrayStore(typeManager.getInt64Type());
}
void JavaByteCodeTranslator::fastore() {
    genArrayStore(typeManager.getSingleType());
}
void JavaByteCodeTranslator::dastore() {
    genArrayStore(typeManager.getDoubleType());
}
void JavaByteCodeTranslator::aastore() {
    genTypeArrayStore();
}
void JavaByteCodeTranslator::bastore() {
    genArrayStore(typeManager.getInt8Type());
}
void JavaByteCodeTranslator::castore() {
    genArrayStore(typeManager.getCharType());
}
void JavaByteCodeTranslator::sastore() {
    genArrayStore(typeManager.getInt16Type());
}
//-----------------------------------------------------------------------------
// stack manipulation byte codes (pops, dups & exchanges)
//-----------------------------------------------------------------------------
void 
JavaByteCodeTranslator::pop() {popOpnd();}

bool 
isCategory2(Opnd* opnd) {
    return (opnd->getType()->isInt8() || opnd->getType()->isDouble());
}

void 
JavaByteCodeTranslator::pop2() {
    Opnd* opnd = popOpnd();
    if (isCategory2(opnd))
        return;
    popOpnd();
}

void 
JavaByteCodeTranslator::dup() {
    pushOpnd(topOpnd());
}

void 
JavaByteCodeTranslator::dup_x1() {
    Opnd* opnd1 = popOpnd();
    Opnd* opnd2 = popOpnd();
    pushOpnd(opnd1);
    pushOpnd(opnd2);
    pushOpnd(opnd1);
}

void 
JavaByteCodeTranslator::dup_x2() {
    Opnd* opnd1 = popOpnd();
    Opnd* opnd2 = popOpnd();
    if (isCategory2(opnd2)) {
        pushOpnd(opnd1);
        pushOpnd(opnd2);
        pushOpnd(opnd1);
        return;
    }
    Opnd* opnd3 = popOpnd();
    pushOpnd(opnd1);
    pushOpnd(opnd3);
    pushOpnd(opnd2);
    pushOpnd(opnd1);
}

void 
JavaByteCodeTranslator::dup2() {
    Opnd* opnd1 = popOpnd();
    if (isCategory2(opnd1)) {
        pushOpnd(opnd1);
        pushOpnd(opnd1);
        return;
    }
    Opnd* opnd2 = popOpnd();
    pushOpnd(opnd2);
    pushOpnd(opnd1);
    pushOpnd(opnd2);
    pushOpnd(opnd1);
}

void 
JavaByteCodeTranslator::dup2_x1() {
    Opnd* opnd1 = popOpnd();
    Opnd* opnd2 = popOpnd();
    if (isCategory2(opnd1)) {
        // opnd1 is a category 2 instruction
        pushOpnd(opnd1);
        pushOpnd(opnd2);
        pushOpnd(opnd1);
    } else {
        // opnd1 is a category 1 instruction
        Opnd* opnd3 = popOpnd();
        pushOpnd(opnd2);
        pushOpnd(opnd1);
        pushOpnd(opnd3);
        pushOpnd(opnd2);
        pushOpnd(opnd1);
    }
}

void 
JavaByteCodeTranslator::dup2_x2() {
    Opnd* opnd1 = popOpnd();
    Opnd* opnd2 = popOpnd();
    if (isCategory2(opnd1)) {
        // opnd1 is category 2
        if (isCategory2(opnd2)) {
            pushOpnd(opnd1);
            pushOpnd(opnd2);
            pushOpnd(opnd1);
        } else {
            // opnd2 is category 1
            Opnd* opnd3 = popOpnd();
            assert(isCategory2(opnd3) == false);
            pushOpnd(opnd1);
            pushOpnd(opnd3);
            pushOpnd(opnd2);
            pushOpnd(opnd1);
        }
    } else {
        assert(isCategory2(opnd2) == false);
        // both opnd1 & opnd2 are category 1
        Opnd* opnd3 = popOpnd();
        if (isCategory2(opnd3)) {
            pushOpnd(opnd2);
            pushOpnd(opnd1);
            pushOpnd(opnd3);
            pushOpnd(opnd2);
            pushOpnd(opnd1);
        } else {
            // opnd1, opnd2, opnd3 all are category 1
            Opnd* opnd4 = popOpnd();
            assert(isCategory2(opnd4) == false);
            pushOpnd(opnd2);
            pushOpnd(opnd1);
            pushOpnd(opnd4);
            pushOpnd(opnd3);
            pushOpnd(opnd2);
            pushOpnd(opnd1);
        }
    }
}

void 
JavaByteCodeTranslator::swap() {
    Opnd* opnd1 = popOpnd();
    Opnd* opnd2 = popOpnd();
    pushOpnd(opnd1);
    pushOpnd(opnd2);
}
//-----------------------------------------------------------------------------
// Arithmetic and logical operation byte codes
//-----------------------------------------------------------------------------
void JavaByteCodeTranslator::iadd() {genAdd(typeManager.getInt32Type());}
void JavaByteCodeTranslator::ladd() {genAdd(typeManager.getInt64Type());}
void JavaByteCodeTranslator::fadd() {genFPAdd(typeManager.getSingleType());}
void JavaByteCodeTranslator::dadd() {genFPAdd(typeManager.getDoubleType());}
void JavaByteCodeTranslator::isub() {genSub(typeManager.getInt32Type());}
void JavaByteCodeTranslator::lsub() {genSub(typeManager.getInt64Type());}
void JavaByteCodeTranslator::fsub() {genFPSub(typeManager.getSingleType());}
void JavaByteCodeTranslator::dsub() {genFPSub(typeManager.getDoubleType());}
void JavaByteCodeTranslator::imul() {genMul(typeManager.getInt32Type());}
void JavaByteCodeTranslator::lmul() {genMul(typeManager.getInt64Type());}
void JavaByteCodeTranslator::fmul() {genFPMul(typeManager.getSingleType());}
void JavaByteCodeTranslator::dmul() {genFPMul(typeManager.getDoubleType());}
void JavaByteCodeTranslator::idiv() {genDiv(typeManager.getInt32Type());}
void JavaByteCodeTranslator::ldiv() {genDiv(typeManager.getInt64Type());}
void JavaByteCodeTranslator::fdiv() {genFPDiv(typeManager.getSingleType());}
void JavaByteCodeTranslator::ddiv() {genFPDiv(typeManager.getDoubleType());}
void JavaByteCodeTranslator::irem() {genRem(typeManager.getInt32Type());}
void JavaByteCodeTranslator::lrem() {genRem(typeManager.getInt64Type());}
void JavaByteCodeTranslator::frem() {genFPRem(typeManager.getSingleType());}
void JavaByteCodeTranslator::drem() {genFPRem(typeManager.getDoubleType());}
void JavaByteCodeTranslator::ineg() {genNeg(typeManager.getInt32Type());}
void JavaByteCodeTranslator::lneg() {genNeg(typeManager.getInt64Type());}
void JavaByteCodeTranslator::fneg() {genNeg(typeManager.getSingleType());}
void JavaByteCodeTranslator::dneg() {genNeg(typeManager.getDoubleType());}
void JavaByteCodeTranslator::iand() {genAnd(typeManager.getInt32Type());}
void JavaByteCodeTranslator::land() {genAnd(typeManager.getInt64Type());}
void JavaByteCodeTranslator::ior()  {genOr(typeManager.getInt32Type());}
void JavaByteCodeTranslator::lor()  {genOr(typeManager.getInt64Type());}
void JavaByteCodeTranslator::ixor() {genXor(typeManager.getInt32Type());}
void JavaByteCodeTranslator::lxor() {genXor(typeManager.getInt64Type());}
void 
JavaByteCodeTranslator::ishl() {
    genShl(typeManager.getInt32Type(), ShiftMask_Masked);
}
void 
JavaByteCodeTranslator::lshl() {
    genShl(typeManager.getInt64Type(), ShiftMask_Masked);
}
void 
JavaByteCodeTranslator::ishr() {
    genShr(typeManager.getInt32Type(),SignedOp, ShiftMask_Masked);
}
void 
JavaByteCodeTranslator::lshr() {
    genShr(typeManager.getInt64Type(),SignedOp, ShiftMask_Masked);
}
void 
JavaByteCodeTranslator::iushr(){
    genShr(typeManager.getInt32Type(),UnsignedOp, ShiftMask_Masked);
}
void 
JavaByteCodeTranslator::lushr(){
    genShr(typeManager.getInt64Type(),UnsignedOp, ShiftMask_Masked);
}
void 
JavaByteCodeTranslator::iinc(uint16 varIndex,int32 amount) {
    VarOpnd* varOpnd = (VarOpnd*)getVarOpndLdVar(JavaLabelPrepass::I,varIndex);
    assert(varOpnd->isVarOpnd());
    Opnd* src1 = irBuilder.genLdVar(typeManager.getInt32Type(),varOpnd);
    Opnd* src2 = irBuilder.genLdConstant((int32)amount);
    Opnd* result = irBuilder.genAdd(typeManager.getInt32Type(),Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No),
                                    src1,src2);
    irBuilder.genStVar(varOpnd,result);
}

//-----------------------------------------------------------------------------
// conversion byte codes
//-----------------------------------------------------------------------------
void 
JavaByteCodeTranslator::i2l() {
    Opnd*    src = popOpnd();
    pushOpnd(irBuilder.genConv(typeManager.getInt64Type(),Type::Int64,Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No),src));
}

void 
JavaByteCodeTranslator::i2f() {
    Opnd*    src = popOpnd();
    pushOpnd(irBuilder.genConv(typeManager.getSingleType(),Type::Single,Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No),src));
}

void 
JavaByteCodeTranslator::i2d() {
    Opnd*    src = popOpnd();
    pushOpnd(irBuilder.genConv(typeManager.getDoubleType(),Type::Double,Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No),src));
}

void 
JavaByteCodeTranslator::l2i() {
    Opnd*    src = popOpnd();
    pushOpnd(irBuilder.genConv(typeManager.getInt32Type(),Type::Int32,Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No),src));
}

void 
JavaByteCodeTranslator::l2f() {
    Opnd*    src = popOpnd();
    pushOpnd(irBuilder.genConv(typeManager.getSingleType(),Type::Single,Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No),src));
}

void 
JavaByteCodeTranslator::l2d() {
    Opnd*    src = popOpnd();
    pushOpnd(irBuilder.genConv(typeManager.getDoubleType(),Type::Double,Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No),src));
}

void 
JavaByteCodeTranslator::f2i() {
    Opnd*    src = popOpnd();
    pushOpnd(irBuilder.genConv(typeManager.getInt32Type(),Type::Int32,Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No),src));
}

void 
JavaByteCodeTranslator::f2l() {
    Opnd*    src = popOpnd();
    pushOpnd(irBuilder.genConv(typeManager.getInt64Type(),Type::Int64,Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No),src));
}

void 
JavaByteCodeTranslator::f2d() {
    Opnd*    src = popOpnd();
    Modifier mod = Modifier(Overflow_None)|Modifier(Exception_Never);
    if (methodToCompile.isStrict())
        mod = mod | Modifier(Strict_Yes);
    else 
        mod = mod | Modifier(Strict_No);
    pushOpnd(irBuilder.genConv(typeManager.getDoubleType(),Type::Double,mod,src));
}

void 
JavaByteCodeTranslator::d2i() {
    Opnd*    src = popOpnd();
    pushOpnd(irBuilder.genConv(typeManager.getInt32Type(),Type::Int32,Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No),src));
}

void 
JavaByteCodeTranslator::d2l() {
    Opnd*    src = popOpnd();
    pushOpnd(irBuilder.genConv(typeManager.getInt64Type(),Type::Int64,Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No),src));
}

void 
JavaByteCodeTranslator::d2f() {
    Opnd*    src = popOpnd();
    Modifier mod = Modifier(Overflow_None)|Modifier(Exception_Never);
    if (methodToCompile.isStrict())
        mod  = mod | Modifier(Strict_Yes);
    else
        mod = mod | Modifier(Strict_No);
    pushOpnd(irBuilder.genConv(typeManager.getSingleType(),Type::Single,mod,src));
}

void 
JavaByteCodeTranslator::i2b() {
    Opnd*    src = popOpnd();
    pushOpnd(irBuilder.genConv(typeManager.getInt32Type(),Type::Int8,Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No),src));
}

void 
JavaByteCodeTranslator::i2c() {
    Opnd*    src = popOpnd();
    pushOpnd(irBuilder.genConv(typeManager.getInt32Type(),Type::UInt16,Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No),src));
}

void 
JavaByteCodeTranslator::i2s() {
    Opnd*    src = popOpnd();
    pushOpnd(irBuilder.genConv(typeManager.getInt32Type(),Type::Int16,Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No),src));
}
//-----------------------------------------------------------------------------
// comparison byte codes
//-----------------------------------------------------------------------------
//
void JavaByteCodeTranslator::lcmp()  {genThreeWayCmp(Type::Int64,Cmp_GT);}
void JavaByteCodeTranslator::fcmpl() {genThreeWayCmp(Type::Single,Cmp_GT);}
void JavaByteCodeTranslator::fcmpg() {genThreeWayCmp(Type::Single,Cmp_GT_Un);}
void JavaByteCodeTranslator::dcmpl() {genThreeWayCmp(Type::Double,Cmp_GT);}
void JavaByteCodeTranslator::dcmpg() {genThreeWayCmp(Type::Double,Cmp_GT_Un);}

//-----------------------------------------------------------------------------
// control transfer byte codes
//-----------------------------------------------------------------------------
void JavaByteCodeTranslator::ifeq(uint32 targetOffset,uint32 nextOffset) {
    genIf1(Cmp_EQ,targetOffset,nextOffset);
}
void JavaByteCodeTranslator::ifne(uint32 targetOffset,uint32 nextOffset) {
    genIf1(Cmp_NE_Un,targetOffset,nextOffset);
}
void JavaByteCodeTranslator::iflt(uint32 targetOffset,uint32 nextOffset) {
    genIf1Commute(Cmp_GT,targetOffset,nextOffset);
}
void JavaByteCodeTranslator::ifge(uint32 targetOffset,uint32 nextOffset) {
    genIf1(Cmp_GTE,targetOffset,nextOffset);
}
void JavaByteCodeTranslator::ifgt(uint32 targetOffset,uint32 nextOffset) {
    genIf1(Cmp_GT,targetOffset,nextOffset);
}
void JavaByteCodeTranslator::ifle(uint32 targetOffset,uint32 nextOffset) {
    genIf1Commute(Cmp_GTE,targetOffset,nextOffset);
}
void JavaByteCodeTranslator::ifnull(uint32 targetOffset,uint32 nextOffset) {
    genIfNull(Cmp_Zero,targetOffset,nextOffset);
}
void JavaByteCodeTranslator::ifnonnull(uint32 targetOffset,uint32 nextOffset) {
    genIfNull(Cmp_NonZero,targetOffset,nextOffset);
}
void JavaByteCodeTranslator::if_icmpeq(uint32 targetOffset,uint32 nextOffset) {
    genIf2(Cmp_EQ,targetOffset,nextOffset);
}
void JavaByteCodeTranslator::if_icmpne(uint32 targetOffset,uint32 nextOffset) {
    genIf2(Cmp_NE_Un,targetOffset,nextOffset);
}
void JavaByteCodeTranslator::if_icmplt(uint32 targetOffset,uint32 nextOffset) {
    genIf2Commute(Cmp_GT,targetOffset,nextOffset);
}
void JavaByteCodeTranslator::if_icmpge(uint32 targetOffset,uint32 nextOffset) {
    genIf2(Cmp_GTE,targetOffset,nextOffset);
}
void JavaByteCodeTranslator::if_icmpgt(uint32 targetOffset,uint32 nextOffset) {
    genIf2(Cmp_GT,targetOffset,nextOffset);
}
void JavaByteCodeTranslator::if_icmple(uint32 targetOffset,uint32 nextOffset) {
    genIf2Commute(Cmp_GTE,targetOffset,nextOffset);
}
void JavaByteCodeTranslator::if_acmpeq(uint32 targetOffset,uint32 nextOffset) {
    Opnd*    src2 = popOpnd();
    Opnd*    src1 = popOpnd();
    if (targetOffset == nextOffset)
        return;
    lastInstructionWasABranch = true;
    checkStack();
    LabelInst *target = getLabel(labelId(targetOffset));
    irBuilder.genBranch(Type::Object,Cmp_EQ,target,src1,src2);
}

void 
JavaByteCodeTranslator::if_acmpne(uint32 targetOffset,uint32 nextOffset) {
    Opnd*    src2 = popOpnd();
    Opnd*    src1 = popOpnd();
    if (targetOffset == nextOffset)
        return;
    lastInstructionWasABranch = true;
    checkStack();
    LabelInst *target = getLabel(labelId(targetOffset));
    irBuilder.genBranch(Type::Object,Cmp_NE_Un,target,src1,src2);
}

void 
JavaByteCodeTranslator::goto_(uint32 targetOffset,uint32 nextOffset) {
    if (targetOffset == nextOffset)
        return;
    lastInstructionWasABranch = true;
    checkStack();
    uint32 lid = labelId(targetOffset);
    LabelInst *target = getLabel(lid);
    irBuilder.genJump(target);
}
//-----------------------------------------------------------------------------
// jsr & ret byte codes for finally statements
//-----------------------------------------------------------------------------
void 
JavaByteCodeTranslator::jsr(uint32 targetOffset, uint32 nextOffset) {
    lastInstructionWasABranch = true;
    checkStack();
    irBuilder.genJSR(getLabel(labelId(targetOffset)));
}

void 
JavaByteCodeTranslator::ret(uint16 varIndex) {
    lastInstructionWasABranch = true;
    checkStack();
    irBuilder.genRet(getVarOpndLdVar(JavaLabelPrepass::RET,varIndex));

    retOffsets[currentOffset] = irBuilder.getLastGeneratedInst();
}
//-----------------------------------------------------------------------------
// multy-way branch byte codes (switches)
//-----------------------------------------------------------------------------
void 
JavaByteCodeTranslator::tableswitch(JavaSwitchTargetsIter* iter) {
    Opnd* opnd = popOpnd();
    lastInstructionWasABranch = true;
    checkStack();
    // subtract the lower bound
    Opnd* bias = irBuilder.genLdConstant((int32)iter->getLowValue());
    Opnd* dst  = irBuilder.genSub(bias->getType(),Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No),opnd,bias);
    LabelInst**    labels = new (memManager) LabelInst*[iter->getNumTargets()];
    for (uint32 i=0; iter->hasNext(); i++) {
        labels[i] = getLabel(labelId(iter->getNextTarget()));
    }
    LabelInst * defaultLabel = getLabel(labelId(iter->getDefaultTarget()));
    irBuilder.genSwitch(iter->getNumTargets(),labels,defaultLabel,dst);
}

void 
JavaByteCodeTranslator::lookupswitch(JavaLookupSwitchTargetsIter* iter) {
    Opnd* opnd = popOpnd();
    lastInstructionWasABranch = true;
    checkStack();
    // generate a sequence of branches
    while (iter->hasNext()) {
        uint32 key;
        uint32 offset = iter->getNextTarget(&key);
        // load the key
        Opnd* value = irBuilder.genLdConstant((int32)key);
        LabelInst *target = getLabel(labelId(offset));
        irBuilder.genBranch(Type::Int32,Cmp_EQ,target,opnd,value);
        // break the basic block
        LabelInst *label = irBuilder.createLabel();
        cfgBuilder.genBlockAfterCurrent(label);
    }
    // generate a jump to the default label
    LabelInst *defaultLabel = getLabel(labelId(iter->getDefaultTarget()));
    irBuilder.genJump(defaultLabel);
}
//-----------------------------------------------------------------------------
// method return byte codes
//-----------------------------------------------------------------------------
void 
JavaByteCodeTranslator::ireturn(uint32 off) {
    genReturn(JavaLabelPrepass::I,off);
    LabelInst *label = irBuilder.createLabel();
    cfgBuilder.genBlockAfterCurrent(label);
}
void 
JavaByteCodeTranslator::lreturn(uint32 off) {
    genReturn(JavaLabelPrepass::L,off);
    LabelInst *label = irBuilder.createLabel();
    cfgBuilder.genBlockAfterCurrent(label);
}
void 
JavaByteCodeTranslator::freturn(uint32 off) {
    genReturn(JavaLabelPrepass::F,off);
    LabelInst *label = irBuilder.createLabel();
    cfgBuilder.genBlockAfterCurrent(label);
}
void 
JavaByteCodeTranslator::dreturn(uint32 off) {
    genReturn(JavaLabelPrepass::D,off);
    LabelInst *label = irBuilder.createLabel();
    cfgBuilder.genBlockAfterCurrent(label);
}
void 
JavaByteCodeTranslator::areturn(uint32 off) {
    genReturn(JavaLabelPrepass::A,off);
    LabelInst *label = irBuilder.createLabel();
    cfgBuilder.genBlockAfterCurrent(label);
}
void 
JavaByteCodeTranslator::return_(uint32 off) {
    genReturn(off);
    LabelInst *label = irBuilder.createLabel();
    cfgBuilder.genBlockAfterCurrent(label);
}
//-----------------------------------------------------------------------------
// LinkingException throw
//-----------------------------------------------------------------------------
void 
JavaByteCodeTranslator::linkingException(uint32 constPoolIndex, uint32 operation) {
    Class_Handle enclosingDrlVMClass = compilationInterface.methodGetClass(&methodToCompile);
    irBuilder.genThrowLinkingException(enclosingDrlVMClass, constPoolIndex, operation);
}
//-----------------------------------------------------------------------------
// for invoke emulation if resolution fails
//-----------------------------------------------------------------------------
void JavaByteCodeTranslator::pseudoInvoke(const char* mdesc) {

    assert(mdesc);
    unsigned numArgs = 0; 
    unsigned index;

    // start just after '(' and go until ')' counting 'numArgs' 
    for( index = 1; mdesc[index] != ')'; index++ ) {
        switch( mdesc[index] ) 
        {
        case 'L':
            // skip method name
            do {
                index++;
                assert( mdesc[index] );
            } while( mdesc[index] != ';' );
            numArgs++;
            break;
        case 'B':
        case 'C':
        case 'D':
        case 'F':
        case 'I':
        case 'J':
        case 'S':
        case 'Z':
            numArgs++;
            break;
        case '[': // do nothing
            break;
        case '(': // we have started from index = 1
        case ')': // must go out earlier
        case 'V': // 'V' can not be in the argument list
            assert(0);
            break;
        default:
            assert(0); // impossible! Verifier must check and catch this
            break;
        }
    } 
    // pop numArgs items
    for (int i=numArgs-1; i>=0; i--)
        popOpnd();
    // move index to the first position after ')'
    index++;
    // recognize and push respective returnType
    Type* retType = 0;
    switch( mdesc[index] ) 
    {
    case 'L':
        retType = typeManager.getNullObjectType();
        break;
    case 'B':
        retType = typeManager.getInt8Type();
    case 'C':
        retType = typeManager.getCharType();
    case 'D':
        retType = typeManager.getDoubleType();
    case 'F':
        retType = typeManager.getSingleType();
    case 'I':
        retType = typeManager.getInt32Type();
    case 'J':
        retType = typeManager.getInt64Type();
    case 'S':
        retType = typeManager.getInt16Type();
    case 'Z':
        retType = typeManager.getBooleanType();
        break;
    case '[': {
        retType = typeManager.getNullObjectType();
        break;

    }
    case '(': // we have already pass it
    case ')': // we have just leave it back
    case 'V':
        retType = typeManager.getVoidType();
        break; // leave stack as is
    default:
        assert(0); // impossible! Verifier must check and catch this
        break;
    }
    assert(retType);
    // push NULL as a return type
    if (retType->tag != Type::Void) {
        pushOpnd(irBuilder.genLdNull());
    }
}
//-----------------------------------------------------------------------------
// method invocation byte codes
//-----------------------------------------------------------------------------
Opnd** 
JavaByteCodeTranslator::popArgs(MethodSignatureDesc* methodSignatureDesc) {
    uint32 numArgs = methodSignatureDesc->getNumParams();
    // pop source operands
    Opnd** srcOpnds = new (memManager) Opnd*[numArgs];
    for (int i=numArgs-1; i>=0; i--) 
        srcOpnds[i] = popOpnd();
    return srcOpnds;
}

void 
JavaByteCodeTranslator::invokevirtual(uint32 constPoolIndex) {
    MethodDesc* methodDesc = resolveVirtualMethod(constPoolIndex);
    if (!methodDesc) {
        linkingException(constPoolIndex, OPCODE_INVOKEVIRTUAL);
        const char* methodSig_string = methodSignatureString(constPoolIndex);
        popOpnd(); // is not static
        pseudoInvoke(methodSig_string);
        return;
    }
    jitrino_assert(compilationInterface,methodDesc);
    MethodSignatureDesc* methodSig = methodDesc->getMethodSig();
    jitrino_assert(compilationInterface,methodSig);
    Opnd** srcOpnds = popArgs(methodSig);
    uint32 numArgs = methodSig->getNumParams();
    Type* returnType = methodSig->getReturnType();

    if (isMagicClass(methodDesc->getParentType())) {
        genMagic(methodDesc, numArgs, srcOpnds, returnType);
        return;
    }
    // callvirt can throw a null pointer exception
    Opnd *tauNullChecked = irBuilder.genTauCheckNull(srcOpnds[0]);
    Opnd* thisOpnd = srcOpnds[0];
    if (methodDesc->getParentType() != thisOpnd->getType()) {
        if(Log::isEnabled()) {
            Log::out()<<"CHECKVIRTUAL "; thisOpnd->printWithType(Log::out()); Log::out() << " : ";
            methodDesc->getParentType()->print(Log::out());
            Log::out() <<"."<<methodDesc->getName()<<" "<< (int)methodDesc->getByteCodeSize()<< ::std::endl;
        }

        Type* type = thisOpnd->getType();
        if (!type->isNullObject() && !type->isInterface()) {
            // needs to refine the method descriptor before doing any optimization
            MethodDesc *overridden = compilationInterface.getOverriddenMethod(
                                     (NamedType*)type,methodDesc);
            if (overridden && overridden != methodDesc) {
                methodDesc = overridden;
            }
        }
    }

    if (returnType) {
        if ((isExactTypeOpnd(thisOpnd) || methodDesc->isFinal()) 
            && inlineMethod(methodDesc)) {
            if (Log::isEnabled()) {
                Log::out() << "XXX inline virtual:"; methodDesc->printFullName(Log::out()); Log::out() << ::std::endl;
            }
            if(methodDesc->isInstanceInitializer()) {
                irBuilder.genInitType(methodDesc->getParentType());
            }
            Opnd* dst =  
                JavaCompileMethodInline(compilationInterface,
                                        memManager,
                                        *methodDesc,
                                        irBuilder,
                                        numArgs,
                                        srcOpnds,
                                        cfgBuilder,
                                        inlineDepth+1,
                                        inlineBuilder,
                                        jsrEntryMap);
            // push the return type
            if (returnType->tag != Type::Void)
                pushOpnd(dst);

            if ( tauNullChecked->getInst()->getOpcode() == Op_TauCheckNull ) {
                tauNullChecked->getInst()->setDefArgModifier(NonNullThisArg);
            }
            return;
        } else if (guardedInlineMethod(methodDesc)) { 
            if (Log::isEnabled()) {
                Log::out() << "XXX guarded inline:"; methodDesc->printFullName(Log::out()); Log::out() << ::std::endl;
            }
            VarOpnd *retVar = NULL;
            if (returnType->tag != Type::Void)
                retVar =irBuilder.genVarDef(returnType,false); 

            LabelInst *inlined = (LabelInst*)irBuilder.getInstFactory()->makeLabel();
            LabelInst *target  = (LabelInst*)irBuilder.getInstFactory()->makeLabel();
            LabelInst *merge   = (LabelInst*)irBuilder.getInstFactory()->makeLabel();

            irBuilder.genTauTypeCompare(thisOpnd,methodDesc,target,
                                        tauNullChecked);
            /********** non- inlined path */
            irBuilder.genFallThroughLabel(inlined);
            cfgBuilder.genBlockAfterCurrent(inlined);
            Opnd *dst = irBuilder.genTauVirtualCall(methodDesc,returnType,
                                                    tauNullChecked, 0,
                                                    numArgs,srcOpnds,
                                                    inlineBuilder);
            if (retVar != NULL)
                irBuilder.genStVar(retVar,dst);
            irBuilder.genJump(merge);
          
            /********** inlined path */  
            irBuilder.genLabel(target);
            cfgBuilder.genBlockAfterCurrent(target);
            dst = JavaCompileMethodInline(compilationInterface,
                                          memManager,
                                          *methodDesc,
                                          irBuilder,
                                          numArgs,
                                          srcOpnds,
                                          cfgBuilder, 
                                          inlineDepth+1,
                                          inlineBuilder,
                                          jsrEntryMap);
            
            if (retVar != NULL && dst != NULL)
                irBuilder.genStVar(retVar,dst); 

            /*if ( tauNullChecked->getInst()->getOpcode() == Op_TauCheckNull ) {
                tauNullChecked->getInst()->setDefArgModifier(NonNullThisArg);
            }*/

            /*********** merge point */
            irBuilder.genLabel(merge);
            cfgBuilder.genBlockAfterCurrent(merge);
            if (retVar != NULL) {
                dst = irBuilder.genLdVar(returnType,retVar);
                pushOpnd(dst);
            }
            return;
        }
    } else { // i.e. returnType == NULL
        // This means that it was not resolved successfully but it can be resolved later
        // inside the callee (with some "magic" custom class loader for example)
        // Or respective exception will be thrown there (in the callee) at the attempt to create (new)
        // an object of unresolved type
        returnType = typeManager.getNullObjectType();
    }

    Opnd* dst = irBuilder.genTauVirtualCall(methodDesc,returnType,
                                            tauNullChecked, 
                                            0, // let IRBuilder handle types
                                            numArgs,
                                            srcOpnds,
                                            inlineBuilder);
    // push the return type
    if (returnType->tag != Type::Void)
        pushOpnd(dst);
}

void 
JavaByteCodeTranslator::invokespecial(uint32 constPoolIndex) {
    MethodDesc* methodDesc = resolveSpecialMethod(constPoolIndex);
    if (!methodDesc) {
        linkingException(constPoolIndex, OPCODE_INVOKESPECIAL);
        const char* methodSig_string = methodSignatureString(constPoolIndex);
        popOpnd(); // is not static
        pseudoInvoke(methodSig_string);
        return;
    }
    jitrino_assert(compilationInterface,methodDesc);
    MethodSignatureDesc* methodSig = methodDesc->getMethodSig();
    jitrino_assert(compilationInterface,methodSig);
    uint32 numArgs = methodSig->getNumParams();
    Opnd** srcOpnds = popArgs(methodSig);
    Type* returnType = methodSig->getReturnType();
    // invokespecial can throw a null pointer exception
    Opnd *tauNullChecked = irBuilder.genTauCheckNull(srcOpnds[0]);
    Opnd* dst;
    
    if (returnType && inlineMethod(methodDesc)) {
        if(Log::isEnabled()) {
            Log::out() << "XXX inline special:";methodDesc->printFullName(Log::out()); Log::out() << ::std::endl;
        }
        if(methodDesc->isInstanceInitializer() || methodDesc->isClassInitializer())
            irBuilder.genInitType(methodDesc->getParentType());

        dst = JavaCompileMethodInline(compilationInterface,
                                      memManager,
                                      *methodDesc,
                                      irBuilder,
                                      numArgs,
                                      srcOpnds,
                                      cfgBuilder,
                                      inlineDepth+1,
                                      inlineBuilder,
                                      jsrEntryMap);

        if ( tauNullChecked->getInst()->getOpcode() == Op_TauCheckNull ) {
            tauNullChecked->getInst()->setDefArgModifier(NonNullThisArg);
        }
    } else {
        if (returnType == NULL) {
            // This means that it was not resolved successfully but it can be resolved later
            // inside the callee (with some "magic" custom class loader for example)
            // Or respective exception will be thrown there (in the callee) at the attempt to create (new)
            // an object of unresolved type
            returnType = typeManager.getNullObjectType();
        }
        dst = irBuilder.genDirectCall(methodDesc,
                                      returnType,
                                      tauNullChecked, 
                                      0, // let IRBuilder check types
                                      numArgs,
                                      srcOpnds,
                                      inlineBuilder);
    }
    // push the return type
    if (returnType->tag != Type::Void)
        pushOpnd(dst);

}

void 
JavaByteCodeTranslator::invokestatic(uint32 constPoolIndex) {
    MethodDesc* methodDesc = resolveStaticMethod(constPoolIndex);
    if (!methodDesc) {
        linkingException(constPoolIndex, OPCODE_INVOKESTATIC);
        const char* methodSig_string = methodSignatureString(constPoolIndex);
        pseudoInvoke(methodSig_string);
        return;
    }
    jitrino_assert(compilationInterface,methodDesc);
    MethodSignatureDesc* methodSig = methodDesc->getMethodSig();
    jitrino_assert(compilationInterface,methodSig);
    uint32 numArgs = methodSig->getNumParams();
    Opnd** srcOpnds = popArgs(methodSig);
    Type *returnType = methodSig->getReturnType();
    if (returnType == NULL) {
        // This means that it was not resolved successfully but it can be resolved later
        // inside the callee (with some "magic" custom class loader for example)
        // Or respective exception will be thrown there (in the callee) at the attempt to create (new)
        // an object of unresolved type
        returnType = typeManager.getNullObjectType();
    }
    //
    //  Try some optimizations for System::arraycopy(...), Min, Max, Abs...
    //
    if (translationFlags.genArrayCopyRepMove == true &&
        genArrayCopyRepMove(methodDesc,numArgs,srcOpnds)) {
        return;
    } else if (translationFlags.genArrayCopy == true &&
        genArrayCopy(methodDesc,numArgs,srcOpnds)) {
        return;
    } else if (translationFlags.genCharArrayCopy == true &&
        genCharArrayCopy(methodDesc,numArgs,srcOpnds,returnType)) {
        return;
    } else if (translationFlags.genMinMaxAbs == true &&
               genMinMax(methodDesc,numArgs,srcOpnds,returnType)) {
        return;
    } else {
        genInvokeStatic(methodDesc,numArgs,srcOpnds,returnType);
    }
}

void 
JavaByteCodeTranslator::invokeinterface(uint32 constPoolIndex,uint32 count) {
    MethodDesc* methodDesc = resolveInterfaceMethod(constPoolIndex);
    if (!methodDesc) {
        linkingException(constPoolIndex, OPCODE_INVOKEINTERFACE);
        const char* methodSig_string = methodSignatureString(constPoolIndex);
        popOpnd(); // is not static
        pseudoInvoke(methodSig_string);
        return;
    }
    jitrino_assert(compilationInterface,methodDesc);
    MethodSignatureDesc* methodSig = methodDesc->getMethodSig();
    jitrino_assert(compilationInterface,methodSig);
    uint32 numArgs = methodSig->getNumParams();
    Opnd** srcOpnds = popArgs(methodSig);
    Type* returnType = methodSig->getReturnType();
    // callintf can throw a null pointer exception
    Opnd *tauNullChecked = irBuilder.genTauCheckNull(srcOpnds[0]);
    Opnd* thisOpnd = srcOpnds[0];
    Opnd* dst;
    if (methodDesc->getParentType() != thisOpnd->getType()) {
        Type * type = thisOpnd->getType();
        if (!type->isNullObject() && !type->isInterface()) {
            // need to refine the method descriptor before doing any optimization
            MethodDesc *overridden = compilationInterface.getOverriddenMethod(
                                  (NamedType*)type,methodDesc);
            if (overridden && overridden != methodDesc && !overridden->getParentType()->isInterface()) {
                methodDesc = overridden;
            }
        }
    }
    if (returnType && (isExactTypeOpnd(thisOpnd) || methodDesc->isFinal())
        && inlineMethod(methodDesc))  {
        if(Log::isEnabled()) {
            Log::out() << "XXX inline interface:"; methodDesc->printFullName(Log::out()); Log::out() << ::std::endl;
        }
        dst =  JavaCompileMethodInline(compilationInterface,
                                       memManager,
                                       *methodDesc,
                                       irBuilder,
                                       numArgs,
                                       srcOpnds,
                                       cfgBuilder,
                                       inlineDepth+1,
                                       inlineBuilder,
                                       jsrEntryMap);

        if ( tauNullChecked->getInst()->getOpcode() == Op_TauCheckNull ) {
            tauNullChecked->getInst()->setDefArgModifier(NonNullThisArg);
        }
    } else {
        if (returnType == NULL) {
            // This means that it was not resolved successfully but it can be resolved later
            // inside the callee (with some "magic" custom class loader for example)
            // Or respective exception will be thrown there (in the callee) at the attempt to create (new)
            // an object of unresolved type
            returnType = typeManager.getNullObjectType();
        }
        dst = irBuilder.genTauVirtualCall(methodDesc,
                                          returnType,
                                          tauNullChecked, 
                                          0, // let IRBuilder handle types
                                          numArgs,
                                          srcOpnds,
                                          inlineBuilder);
    }
    // push the return type
    if (returnType->tag != Type::Void)
        pushOpnd(dst);
}
//-----------------------------------------------------------------------------
// object allocation byte codes
//-----------------------------------------------------------------------------
void 
JavaByteCodeTranslator::new_(uint32 constPoolIndex) {
    NamedType *type = resolveTypeNew(constPoolIndex);
    if (!type) {
        linkingException(constPoolIndex, OPCODE_NEW);
        pushOpnd(irBuilder.genLdNull());
        return;
    }
    jitrino_assert(compilationInterface,type);
    pushOpnd(irBuilder.genNewObj(type));
}
void 
JavaByteCodeTranslator::newarray(uint8 atype) {
    NamedType* type = NULL;
    switch (atype) {
    case 4:    // boolean
        type = typeManager.getBooleanType(); break;
    case 5: // char
        type = typeManager.getCharType(); break;
    case 6: // float
        type = typeManager.getSingleType(); break;
    case 7: // double
        type = typeManager.getDoubleType(); break;
    case 8: // byte
        type = typeManager.getInt8Type(); break;
    case 9: // short
        type = typeManager.getInt16Type(); break;
    case 10: // int
        type = typeManager.getInt32Type(); break;
    case 11: // long
        type = typeManager.getInt64Type(); break;
    default: jitrino_assert(compilationInterface,0);
    }
    Opnd* arrayOpnd = irBuilder.genNewArray(type,popOpnd());
    pushOpnd(arrayOpnd);
    if (translationFlags.optArrayInit) {
        const uint8* byteCodes = parser.getByteCodes();
        const uint32 byteCodeLength = parser.getByteCodeLength();
        uint32 offset = currentOffset + 2/*newarray length*/;
        uint32 length = checkForArrayInitializer(arrayOpnd, byteCodes, offset, byteCodeLength);
        currentOffset += length;
    }
}

void 
JavaByteCodeTranslator::anewarray(uint32 constPoolIndex) {
    NamedType* type = resolveType(constPoolIndex);
    if (!type) {
        linkingException(constPoolIndex, OPCODE_ANEWARRAY);
        pushOpnd(irBuilder.genLdNull());
        return;
    }
    jitrino_assert(compilationInterface,type);
    pushOpnd(irBuilder.genNewArray(type,popOpnd()));
}

void 
JavaByteCodeTranslator::multianewarray(uint32 constPoolIndex,uint8 dimensions) {
    NamedType* arraytype = resolveType(constPoolIndex);
    if (!arraytype) {
        linkingException(constPoolIndex, OPCODE_MULTIANEWARRAY);
        // pop the sizes
        for (int i=dimensions-1; i>=0; i--) {
            popOpnd();
        }
        pushOpnd(irBuilder.genLdNull());
        return;
    }
    jitrino_assert(compilationInterface,arraytype);
    jitrino_assert(compilationInterface,dimensions > 0);
    Opnd** countOpnds = new (memManager) Opnd*[dimensions];
    // pop the sizes
    for (int i=dimensions-1; i>=0; i--) {
        countOpnds[i] = popOpnd();
    }
    pushOpnd(irBuilder.genMultianewarray(arraytype,dimensions,countOpnds));
}

void 
JavaByteCodeTranslator::arraylength() {
    Type::Tag arrayLenType = Type::Int32;
    pushOpnd(irBuilder.genArrayLen(typeManager.getInt32Type(),arrayLenType,popOpnd()));
}

void 
JavaByteCodeTranslator::athrow() {
    lastInstructionWasABranch = true;
    irBuilder.genThrow(Throw_NoModifier, popOpnd());
    LabelInst *label = irBuilder.createLabel();
    cfgBuilder.genBlockAfterCurrent(label);
}

//-----------------------------------------------------------------------------
// type checking byte codes
//-----------------------------------------------------------------------------
void 
JavaByteCodeTranslator::checkcast(uint32 constPoolIndex) {
    NamedType *type = resolveType(constPoolIndex);
    if (!type) {
        linkingException(constPoolIndex, OPCODE_CHECKCAST);
        return; // can be left as is
    }
    jitrino_assert(compilationInterface,type);
    pushOpnd(irBuilder.genCast(popOpnd(),type));
}

int  
JavaByteCodeTranslator::instanceof(const uint8* bcp, uint32 constPoolIndex, uint32 off)   {
    NamedType *type = resolveType(constPoolIndex);
    if (!type) {
        linkingException(constPoolIndex, OPCODE_INSTANCEOF);
        popOpnd(); // emulation of unsuccessful 'instanceof'
        pushOpnd(irBuilder.genLdConstant((int32)0));
        return 3;
    }
    jitrino_assert(compilationInterface,type);
    pushOpnd(irBuilder.genInstanceOf(popOpnd(),type));
    return 3;
}

//
// synchronization
//
void 
JavaByteCodeTranslator::monitorenter() {
   if (translationFlags.ignoreSync) 
        popOpnd();
   else if (translationFlags.syncAsEnterFence)
        irBuilder.genMonitorEnterFence(popOpnd());
   else
        irBuilder.genMonitorEnter(popOpnd());
}

void 
JavaByteCodeTranslator::monitorexit() {
    if (translationFlags.ignoreSync || translationFlags.syncAsEnterFence)
        popOpnd();
    else
        irBuilder.genMonitorExit(popOpnd());
}

//-----------------------------------------------------------------------------
// variable access helpers
//-----------------------------------------------------------------------------
void 
JavaByteCodeTranslator::genLdVar(uint32 varIndex,JavaLabelPrepass::JavaVarType javaType) {
    Opnd *var = getVarOpndLdVar(javaType,varIndex);
    Opnd *opnd;
    if (var->isVarOpnd()) {
        opnd = irBuilder.genLdVar(var->getType(),(VarOpnd*)var);
    } else {
        opnd = var;
    }
    pushOpnd(opnd);
}

void 
JavaByteCodeTranslator::genTypeStVar(uint16 varIndex) {
    Opnd *src = popOpnd();
    JavaLabelPrepass::JavaVarType javaType;
    if (src->getType() == typeManager.getIntPtrType())
        javaType = JavaLabelPrepass::RET;
    else
        javaType = JavaLabelPrepass::A;
    VarOpnd *var = getVarOpndStVar(javaType,varIndex,src);
    if (var != NULL) {
        irBuilder.genStVar(var,src);
    }
}

void 
JavaByteCodeTranslator::genStVar(uint32 varIndex,JavaLabelPrepass::JavaVarType javaType) {
    Opnd *src = popOpnd();
    VarOpnd *var = getVarOpndStVar(javaType,varIndex,src);
    if (var != NULL)
        irBuilder.genStVar(var,src);
}

//-----------------------------------------------------------------------------
// method return helpers
//-----------------------------------------------------------------------------
bool 
JavaByteCodeTranslator::needsReturnLabel(uint32 off) {
    if (!moreThanOneReturn && methodToCompile.getByteCodeSize()-1 != off) {
        if (!jumpToTheEnd) {
           // allocate one more label
           labels[numLabels++] = (LabelInst*)irBuilder.getInstFactory()->makeLabel();
        }
        jumpToTheEnd      = true;
        moreThanOneReturn = true;
    }
    return moreThanOneReturn;
}

// for non-void returns
void 
JavaByteCodeTranslator::genReturn(JavaLabelPrepass::JavaVarType javaType, uint32 off) {
    Opnd *ret = popOpndStVar();
    if (methodToCompile.isSynchronized()) {
        // Create a new block to break exception region.  The monexit exception should
        // go to unwind.
        cfgBuilder.genBlock(irBuilder.createLabel());
        if (methodToCompile.isStatic()) {
            irBuilder.genTypeMonitorExit(methodToCompile.getParentType());
        } else {
            genLdVar(0,JavaLabelPrepass::A);
            genMethodMonitorExit();
        }
    }
    if (isInlinedMethod) {
        if (needsReturnLabel(off)) {
            if (resultOpnd == NULL)  { // create a variable to hold the return value
                resultOpnd = irBuilder.genVarDef(ret->getType(),false);
            } else {
                Type *retType = typeManager.getCommonType(resultOpnd->getType(),ret->getType());
                if (retType != NULL)
                    resultOpnd->setType(retType);
            }
            // generate a StVar
            irBuilder.genStVar((VarOpnd*)resultOpnd,ret);
        } else {
            if (returnNode != NULL)
                *returnNode = irBuilder.getCurrentLabel()->getNode(); 
            resultOpnd = ret;
            if (returnOpnd != NULL) {
                    *returnOpnd = resultOpnd;
            }
        }
        if (jumpToTheEnd)  // insert jump to the end of method
            irBuilder.genJump(getLabel(numLabels-1));
    } else {
        irBuilder.genReturn(ret,javaTypeMap[javaType]);
    }
    opndStack.makeEmpty();
}

// for void returns
void
JavaByteCodeTranslator::genReturn(uint32 off) {
    if (methodToCompile.isSynchronized()) {
        // Create a new block to break exception region. The monexit exception should
        // go to unwind.
        cfgBuilder.genBlock(irBuilder.createLabel());
        if (methodToCompile.isStatic()) {
            irBuilder.genTypeMonitorExit(methodToCompile.getParentType());
        } else {
            genLdVar(0,JavaLabelPrepass::A);
            genMethodMonitorExit();
        }
    }
    if (isInlinedMethod) {
        needsReturnLabel(off);
        if (jumpToTheEnd)
            irBuilder.genJump(getLabel(numLabels-1));
    } else
        irBuilder.genReturn();
}

//-----------------------------------------------------------------------------
// arithmetic & logical helpers
//-----------------------------------------------------------------------------
void 
JavaByteCodeTranslator::genAdd(Type* dstType) {
    Opnd*    src2 = popOpnd();
    Opnd*    src1 = popOpnd();
    pushOpnd(irBuilder.genAdd(dstType,Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No),src1,src2));
}

void 
JavaByteCodeTranslator::genSub(Type* dstType) {
    Opnd*    src2 = popOpnd();
    Opnd*    src1 = popOpnd();
    pushOpnd(irBuilder.genSub(dstType,Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No),src1,src2));
}

void 
JavaByteCodeTranslator::genMul(Type* dstType) {
    Opnd*    src2 = popOpnd();
    Opnd*    src1 = popOpnd();
    pushOpnd(irBuilder.genMul(dstType,Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No),src1,src2));
}

void 
JavaByteCodeTranslator::genDiv(Type* dstType) {
    Opnd*    src2 = popOpnd();
    Opnd*    src1 = popOpnd();
    pushOpnd(irBuilder.genDiv(dstType,Modifier(SignedOp)|Modifier(Strict_No),src1,src2));
}

void 
JavaByteCodeTranslator::genRem(Type* dstType) {
    Opnd*    src2 = popOpnd();
    Opnd*    src1 = popOpnd();
    pushOpnd(irBuilder.genRem(dstType,Modifier(SignedOp)|Modifier(Strict_No),src1,src2));
}

void 
JavaByteCodeTranslator::genNeg(Type* dstType) {
    Opnd*    src = popOpnd();
    pushOpnd(irBuilder.genNeg(dstType,src));
}

void 
JavaByteCodeTranslator::genFPAdd(Type* dstType) {
    Opnd*    src2 = popOpnd();
    Opnd*    src1 = popOpnd();
    Modifier mod = Modifier(Overflow_None)|Modifier(Exception_Never);
    if (methodToCompile.isStrict())
        mod = mod | Modifier(Strict_Yes);
    else
        mod = mod | Modifier(Strict_No);
    pushOpnd(irBuilder.genAdd(dstType,mod,src1,src2));
}

void 
JavaByteCodeTranslator::genFPSub(Type* dstType) {
    Opnd*    src2 = popOpnd();
    Opnd*    src1 = popOpnd();
    Modifier mod = Modifier(Overflow_None)|Modifier(Exception_Never);
    if (methodToCompile.isStrict())
        mod = mod | Modifier(Strict_Yes);
    else
        mod = mod | Modifier(Strict_No);
    pushOpnd(irBuilder.genSub(dstType,mod,src1,src2));
}

void 
JavaByteCodeTranslator::genFPMul(Type* dstType) {
    Opnd*    src2 = popOpnd();
    Opnd*    src1 = popOpnd();
    Modifier mod = Modifier(Overflow_None)|Modifier(Exception_Never);
    if (methodToCompile.isStrict())
        mod  = mod | Modifier(Strict_Yes);
    else
        mod  = mod | Modifier(Strict_No);
    pushOpnd(irBuilder.genMul(dstType,mod,src1,src2));
}

void 
JavaByteCodeTranslator::genFPDiv(Type* dstType) {
    Opnd*    src2 = popOpnd();
    Opnd*    src1 = popOpnd();
    Modifier mod = SignedOp;
    if (methodToCompile.isStrict())
        mod = mod | Modifier(Strict_Yes);
    else
        mod = mod | Modifier(Strict_No);
    pushOpnd(irBuilder.genDiv(dstType,mod,src1,src2));
}

void 
JavaByteCodeTranslator::genFPRem(Type* dstType) {
    Opnd*    src2 = popOpnd();
    Opnd*    src1 = popOpnd();
    Modifier mod = SignedOp;
    if (methodToCompile.isStrict())
        mod = mod | Modifier(Strict_Yes);
    else
        mod = mod | Modifier(Strict_No);
    pushOpnd(irBuilder.genRem(dstType,mod,src1,src2));
}

void 
JavaByteCodeTranslator::genAnd(Type* dstType) {
    Opnd*    src2 = popOpnd();
    Opnd*    src1 = popOpnd();
    pushOpnd(irBuilder.genAnd(dstType,src1,src2));
}

void 
JavaByteCodeTranslator::genOr(Type* dstType) {
    Opnd*    src2 = popOpnd();
    Opnd*    src1 = popOpnd();
    pushOpnd(irBuilder.genOr(dstType,src1,src2));
}

void 
JavaByteCodeTranslator::genXor(Type* dstType) {
    Opnd*    src2 = popOpnd();
    Opnd*    src1 = popOpnd();
    pushOpnd(irBuilder.genXor(dstType,src1,src2));
}

void 
JavaByteCodeTranslator::genShl(Type* type, ShiftMaskModifier mod) {
    Opnd*    shiftAmount = popOpnd();
    Opnd*    value = popOpnd(); 
    pushOpnd(irBuilder.genShl(type,mod,value,shiftAmount));
}

void 
JavaByteCodeTranslator::genShr(Type* type, SignedModifier mod1,
                               ShiftMaskModifier mod2) {
    Opnd*    shiftAmount = popOpnd();
    Opnd*    value = popOpnd(); 
    pushOpnd(irBuilder.genShr(type,Modifier(mod1)|Modifier(mod2), value, shiftAmount));
}

//-----------------------------------------------------------------------------
// array access helpers
//-----------------------------------------------------------------------------
void 
JavaByteCodeTranslator::genArrayLoad(Type* type) {
    Opnd* index = popOpnd();
    Opnd* base = popOpnd();
    pushOpnd(irBuilder.genLdElem(type,base,index));
}

void 
JavaByteCodeTranslator::genTypeArrayLoad() {
    Opnd* index = popOpnd();
    Opnd* base = popOpnd();
    Type *type = base->getType();
    if (!type->isArrayType()) {
        if (type->isNullObject()) {
            irBuilder.genThrowSystemException(CompilationInterface::Exception_NullPointer);
            pushOpnd(irBuilder.genLdNull());
            return;
        }
        if (Log::isEnabled()) {
            Log::out() << "Array type is ";
            type->print(Log::out()); Log::out() << ::std::endl;
            stateInfo->stack[5].type->print(Log::out()); Log::out() << ::std::endl;
            Log::out() << "CONFLICT IN ARRAY ACCESS\n";
        }
        type = typeManager.getSystemObjectType();
    } else
        type = ((ArrayType*)type)->getElementType();
    pushOpnd(irBuilder.genLdElem(type,base,index));
}

void 
JavaByteCodeTranslator::genArrayStore(Type* type) {
    Opnd*    value = popOpnd();
    Opnd*    index = popOpnd();
    Opnd*    base = popOpnd();
    irBuilder.genStElem(type,base,index,value);
}

void 
JavaByteCodeTranslator::genTypeArrayStore() {
    Opnd*    value = popOpnd();
    Opnd*    index = popOpnd();
    Opnd*    base = popOpnd();
    Type *type = base->getType();
    if (!type->isArrayType()) {
        if (type->isNullObject()) {
            irBuilder.genThrowSystemException(CompilationInterface::Exception_NullPointer);
            return;
        }
        type = typeManager.getSystemObjectType();
        Log::out() << "CONFLICT IN ARRAY ACCESS\n";
    } else
        type = ((ArrayType*)type)->getElementType();
    irBuilder.genStElem(type,base,index,value);
}

//-----------------------------------------------------------------------------
// control transfer helpers
//-----------------------------------------------------------------------------
void 
JavaByteCodeTranslator::genIf1(ComparisonModifier mod,
                               int32 targetOffset,
                               int32 nextOffset) {
    Opnd*    src1 = popOpnd();
    if (targetOffset == nextOffset)
        return;
    lastInstructionWasABranch = true;
    checkStack();
    LabelInst *target = getLabel(labelId(targetOffset));
    Opnd*    src2 = irBuilder.genLdConstant((int32)0);
    irBuilder.genBranch(Type::Int32,mod,target,src1,src2);
}

void 
JavaByteCodeTranslator::genIf1Commute(ComparisonModifier mod,
                                      int32 targetOffset,
                                      int32 nextOffset) {
    Opnd*    src1 = popOpnd();
    if (targetOffset == nextOffset)
        return;
    lastInstructionWasABranch = true;
    checkStack();
    LabelInst *target = getLabel(labelId(targetOffset));
    Opnd*    src2 = irBuilder.genLdConstant((int32)0);
    irBuilder.genBranch(Type::Int32,mod,target,src2,src1);
}

void 
JavaByteCodeTranslator::genIf2(ComparisonModifier mod,
                               int32 targetOffset,
                               int32 nextOffset) {
    Opnd*    src2 = popOpnd();
    Opnd*    src1 = popOpnd();
    if (targetOffset == nextOffset)
        return;
    lastInstructionWasABranch = true;
    checkStack();
    LabelInst *target = getLabel(labelId(targetOffset));
    irBuilder.genBranch(Type::Int32,mod,target,src1,src2);
}

void 
JavaByteCodeTranslator::genIf2Commute(ComparisonModifier mod,
                                      int32 targetOffset,
                                      int32 nextOffset) {
    Opnd*    src2 = popOpnd();
    Opnd*    src1 = popOpnd();
    if (targetOffset == nextOffset)
        return;
    lastInstructionWasABranch = true;
    checkStack();
    LabelInst *target = getLabel(labelId(targetOffset));
    irBuilder.genBranch(Type::Int32,mod,target,src2,src1);
}

void 
JavaByteCodeTranslator::genIfNull(ComparisonModifier mod,
                                  int32 targetOffset,
                                  int32 nextOffset) {
    Opnd*    src1 = popOpnd();
    if (targetOffset == nextOffset)
        return;
    lastInstructionWasABranch = true;
    checkStack();
    LabelInst *target = getLabel(labelId(targetOffset));

    if (src1->getType() == typeManager.getNullObjectType()) {
        if (mod == Cmp_Zero)
            irBuilder.genJump(target);
        return;
    }
    irBuilder.genBranch(Type::SystemObject,mod,target,src1);
}

void 
JavaByteCodeTranslator::genThreeWayCmp(Type::Tag cmpType,
                                       ComparisonModifier src1ToSrc2) {
    Opnd* src2 = popOpnd();
    Opnd* src1 = popOpnd();
    Type* dstType = typeManager.getInt32Type();
    pushOpnd(irBuilder.genCmp3(dstType,cmpType,src1ToSrc2,src1,src2));
}

//-----------------------------------------------------------------------------
// method calls helpers
//-----------------------------------------------------------------------------

void 
JavaByteCodeTranslator::genInvokeStatic(MethodDesc * methodDesc,
                                        uint32       numArgs,
                                        Opnd **      srcOpnds,
                                        Type *       returnType) {
    Opnd *dst;

    if (isMagicMethod(methodDesc)) {
        genMagic(methodDesc, numArgs, srcOpnds, returnType);    
        return;
    }
    if (inlineMethod(methodDesc)) {
        if(Log::isEnabled()) {
            Log::out() << "XXX inline static:"; methodDesc->printFullName(Log::out()); Log::out() << ::std::endl;
        }
        irBuilder.genTauSafe(); // always safe, is a static method call
        irBuilder.genInitType(methodDesc->getParentType());
        dst = JavaCompileMethodInline(compilationInterface,
                                      memManager,
                                      *methodDesc,
                                      irBuilder,
                                      numArgs,
                                      srcOpnds,
                                      cfgBuilder,
                                      inlineDepth+1,
                                      inlineBuilder,
                                      jsrEntryMap);
    } else {
        Opnd *tauNullChecked = irBuilder.genTauSafe(); // always safe, is a static method call
        dst = irBuilder.genDirectCall(methodDesc, 
                                      returnType,
                                      tauNullChecked,
                                      0, // let IRBuilder check types
                                      numArgs,
                                      srcOpnds,
                                      inlineBuilder);
    }
    if (returnType->tag != Type::Void)
        pushOpnd(dst);
}

void
JavaByteCodeTranslator::newFallthroughBlock() {
    LabelInst * labelInst = irBuilder.createLabel();
    irBuilder.genFallThroughLabel(labelInst);
    cfgBuilder.genBlockAfterCurrent(labelInst);
}

bool
JavaByteCodeTranslator::methodIsArraycopy(MethodDesc * methodDesc) {

    return (strcmp(methodDesc->getName(),"arraycopy") == 0 &&
            strcmp(methodDesc->getParentType()->getName(),"java/lang/System") ==0);
}

bool
JavaByteCodeTranslator::arraycopyOptimizable(MethodDesc * methodDesc, 
                                             uint32       numArgs,
                                             Opnd **      srcOpnds) {

    //
    //  an ArrayStoreException is thrown and the destination is not modified: 
    //  
    //  - The src argument refers to an object that is not an array. 
    //  - The dest argument refers to an object that is not an array. 
    //  - The src argument and dest argument refer to arrays whose component types are different primitive types. 
    //  - The src argument refers to an array with a primitive component type and the dest argument
    //    refers to an array with a reference component type. 
    //  - The src argument refers to an array with a reference component type and the dest argument
    //    refers to an array with a primitive component type. 
    //
    assert(numArgs == 5);
    Opnd * src = srcOpnds[0];
    Type * srcType = src->getType();
    Opnd * dst = srcOpnds[2];
    Type * dstType = dst->getType();
    assert(srcType->isObject() &&
           srcOpnds[1]->getType()->isInt4() && // 1 - srcPos
           dstType->isObject() &&
           srcOpnds[3]->getType()->isInt4() && // 3 - dstPos
           srcOpnds[4]->getType()->isInt4());  // 4 - length

    bool throwsASE = false;
    bool srcIsArray = srcType->isArray();
    bool dstIsArray = dstType->isArray();
    ArrayType* srcAsArrayType = srcType->asArrayType();
    ArrayType* dstAsArrayType = dstType->asArrayType();
    bool srcIsArrOfPrimitive = srcIsArray && typeManager.isArrayOfPrimitiveElements(srcAsArrayType->getVMTypeHandle());
    bool dstIsArrOfPrimitive = dstIsArray && typeManager.isArrayOfPrimitiveElements(dstAsArrayType->getVMTypeHandle());
    if ( !(srcIsArray && dstIsArray) ) {
         throwsASE = true;
    } else if ( srcIsArrOfPrimitive ) {
        if( !dstIsArrOfPrimitive || srcType != dstType )
            throwsASE = true;
    } else if( dstIsArrOfPrimitive ) {
         throwsASE = true;
    } else { // the both are of objects
        // Here is some inaccuracy. If src is a subclass of dst there is no ASE for sure.
        // If it is not, we should check the assignability of each element being copied.
        // To avoid this we just reject the inlining of System::arraycopy call in this case.
        NamedType* srcElemType = srcAsArrayType->getElementType();
        NamedType* dstElemType = dstAsArrayType->getElementType();
        throwsASE = ! typeManager.isSubClassOf(srcElemType->getVMTypeHandle(),dstElemType->getVMTypeHandle());
    }
    if ( throwsASE )
        return false;
    else
        return true;
}

bool
JavaByteCodeTranslator::genArrayCopyRepMove(MethodDesc * methodDesc, 
                                            uint32       numArgs,
                                            Opnd **      srcOpnds) {

    if( !methodIsArraycopy(methodDesc) ||
        !arraycopyOptimizable(methodDesc,numArgs,srcOpnds) )
    {
        // reject the inlining of System::arraycopy call
        return false;
    }

    if (Log::isEnabled()) {
        Log::out() << "XXX array copy into 'rep move': ";
        methodDesc->printFullName(Log::out());
        Log::out() << ::std::endl;
    }

    assert(numArgs == 5);
    Opnd * src = srcOpnds[0];
    Opnd * srcPos = srcOpnds[1];
    Type * srcPosType = srcPos->getType();
    Opnd * dst = srcOpnds[2];
    Opnd * dstPos = srcOpnds[3];
    Type * dstPosType = dstPos->getType();
    Opnd * len = srcOpnds[4];

    //
    //  Generate exception condition checks:
    //      chknull src
    //      chknull dst
    //      cmpbr srcPos < 0, boundsException
    //      cmpbr dstPos < 0, boundsException
    //      cmpbr len < 0, boundsException
    //      srcEnd = add srcPos, len
    //      srcLen = src.length
    //      cmpbr srcEnd > srcLen, boundsException
    //      dstEnd = add dstPos, len
    //      dstLen = dst.length
    //      cmpbr dstEnd > dstLen, boundsException
    //  Skip trivial:
    //      cmpbr (src == dst) && (dstPos == srcPos), Exit
    //  Choose a direction:
    //      cmpbr (dstPos > srcPos) && (src == dst), Reverse
    //
    //  Intrinsic calls will be codeselected into rep move instruction.
    //  Direct:
    //      IntrinsicCall id=ArrayCopyDirect
    //      goto Exit
    //  Reverse:
    //      srcPos = srcPos + len - 1
    //      dstPos = dstPos + len - 1
    //      IntrinsicCall id=ArrayCopyReverse
    //      goto Exit
    //
    //  boundsException:
    //      chkbounds -1, src
    //  Exit:
    //
    Opnd *tauSrcNullChecked = irBuilder.genTauCheckNull(src);
    Opnd *tauDstNullChecked = irBuilder.genTauCheckNull(dst);
    Opnd *tauNullCheckedRefArgs = irBuilder.genTauAnd(tauSrcNullChecked,tauDstNullChecked);
    
    LabelInst * reverseCopying = irBuilder.createLabel();
    LabelInst * boundsException = irBuilder.createLabel();
    LabelInst * Exit = irBuilder.createLabel();
    Type * intType = typeManager.getInt32Type();
    Type * voidType = typeManager.getVoidType();

    newFallthroughBlock();
    Opnd * zero = irBuilder.genLdConstant((int32)0);
    irBuilder.genBranch(Type::Int32,Cmp_GT,boundsException,zero,srcPos);        

    newFallthroughBlock();
    irBuilder.genBranch(Type::Int32,Cmp_GT,boundsException,zero,dstPos);

    newFallthroughBlock();
    irBuilder.genBranch(Type::Int32,Cmp_GT,boundsException,zero,len);

    Modifier mod = Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No);

    newFallthroughBlock();   
    Opnd * srcLen = irBuilder.genArrayLen(intType,Type::Int32,src);
    Opnd * srcEnd = irBuilder.genAdd(intType,mod,srcPos,len);
    irBuilder.genBranch(Type::Int32,Cmp_GT,boundsException,srcEnd,srcLen);
    
    newFallthroughBlock();
    Opnd * dstEnd = irBuilder.genAdd(intType,mod,dstPos,len);
    Opnd * dstLen = irBuilder.genArrayLen(intType,Type::Int32,dst);
    irBuilder.genBranch(Type::Int32,Cmp_GT,boundsException,dstEnd,dstLen);

    newFallthroughBlock();

    // The case of same arrays and same positions
    Opnd * diff = irBuilder.genCmp3(intType,Type::Int32,Cmp_GT,dstPos,srcPos);
    Opnd * sameArrays = irBuilder.genCmp(intType,Type::IntPtr,Cmp_EQ,src,dst);
    Opnd * zeroDiff = irBuilder.genCmp(intType,Type::Int32,Cmp_EQ,diff,zero);
    Opnd * nothingToCopy = irBuilder.genAnd(intType,sameArrays,zeroDiff);
    irBuilder.genBranch(Type::Int32,Cmp_GT,Exit,nothingToCopy,zero);

    newFallthroughBlock();

    Opnd* tauTypesChecked = irBuilder.genTauSafe();

    // Choosing direction
    Opnd * dstIsGreater = irBuilder.genCmp(intType,Type::Int32,Cmp_GT,diff,zero);
    Opnd * reverseCopy = irBuilder.genAnd(intType,sameArrays,dstIsGreater);
    irBuilder.genBranch(Type::Int32,Cmp_GT,reverseCopying,reverseCopy,zero);

    newFallthroughBlock();

    {   // Direct Copying
    irBuilder.genIntrinsicCall(ArrayCopyDirect,voidType,
                               tauNullCheckedRefArgs,
                               tauTypesChecked,
                               numArgs,srcOpnds);
    irBuilder.genJump(Exit);
    }   // End of Direct Copying

    irBuilder.genLabel(reverseCopying);
    cfgBuilder.genBlockAfterCurrent(reverseCopying);
    {   // Reverse Copying

    Opnd* minusone = irBuilder.genLdConstant((int32)-1);
    Opnd* lastSrcIdx = irBuilder.genAdd(srcPosType,mod,srcEnd,minusone);
    Opnd* lastDstIdx = irBuilder.genAdd(dstPosType,mod,dstEnd,minusone);

    Opnd** reverseArgs = new (memManager) Opnd*[numArgs];
    reverseArgs[0] = srcOpnds[0]; // src
    reverseArgs[1] = lastSrcIdx;  // srcPos+len-1
    reverseArgs[2] = srcOpnds[2]; // dst
    reverseArgs[3] = lastDstIdx;  // dstPos+len-1
    reverseArgs[4] = srcOpnds[4]; // len
    // copy
    irBuilder.genIntrinsicCall(ArrayCopyReverse,voidType,
                               tauNullCheckedRefArgs,
                               tauTypesChecked,
                               numArgs,reverseArgs);
    irBuilder.genJump(Exit);
    }   // End of Reverse Copying

    irBuilder.genLabel(boundsException);
    cfgBuilder.genBlockAfterCurrent(boundsException);
    Opnd * minusOne = irBuilder.genLdConstant((int32)-1);
    irBuilder.genTauCheckBounds(src,minusOne,tauSrcNullChecked);

    irBuilder.genLabel(Exit);
    cfgBuilder.genBlockAfterCurrent(Exit);

    return true;
}

bool
JavaByteCodeTranslator::genArrayCopy(MethodDesc * methodDesc, 
                                     uint32       numArgs,
                                     Opnd **      srcOpnds) {

    if( !methodIsArraycopy(methodDesc) ||
        !arraycopyOptimizable(methodDesc,numArgs,srcOpnds) )
    {
        // reject the inlining of System::arraycopy call
        return false;
    }

    if (Log::isEnabled()) {
        Log::out() << "XXX array copy: "; methodDesc->printFullName(Log::out()); Log::out() << ::std::endl;
    }

    assert(numArgs == 5);
    Opnd * src = srcOpnds[0];
    Type * srcType = src->getType();
    Opnd * srcPos = srcOpnds[1];
    Type * srcPosType = srcPos->getType();
    Opnd * dst = srcOpnds[2];
    Type * dstType = dst->getType();
    Opnd * dstPos = srcOpnds[3];
    Type * dstPosType = dstPos->getType();
    Opnd * len = srcOpnds[4];

    //
    //  Generate exception condition checks:
    //      chknull src
    //      chknull dst
    //      cmpbr srcPos < 0, boundsException
    //      cmpbr dstPos < 0, boundsException
    //      cmpbr len < 0, boundsException
    //      srcEnd = add srcPos, len
    //      srcLen = src.length
    //      cmpbr srcEnd > srcLen, boundsException
    //      dstEnd = add dstPos, len
    //      dstLen = dst.length
    //      cmpbr dstEnd > dstLen, boundsException
    //
    //      diff = Cmp3(dstPos,srcPos)
    //          //  1 if dstPos > srcPos
    //          //  0 if dstPos == srcPos
    //          // -1 if dstPos < srcPos
    //      if (src == dst && diff == 0)  // nothing to do
    //          goto L1:
    //
    //      if (diff > 0)
    //          goto reverseCopying:
    //
    //      indexSrc = srcPos
    //      indexDst = dstPos
    //      increment = 1
    //  copyDirectLoopHeader:
    //      if (indexSrc == srcEnd)
    //          goto L1:
    //      dst[indexDst] = src[indexSrc]
    //      indexSrc += increment
    //      indexDst += increment
    //      goto copyDirectLoopHeader:
    //
    //  reverseCopying:
    //      indexSrc = srcPos + len - 1
    //      indexDst = dstPos + len - 1
    //      decrement = 1
    //  copyReverseLoopHeader:
    //      if (indexSrc < srcPos)
    //          goto L1:
    //      dst[indexDst] = src[indexSrc]
    //      indexSrc -= decrement
    //      indexDst -= decrement
    //      goto copyReverseLoopHeader:
    //
    //  boundsException:
    //      chkbounds -1, src
    //  L1:
    //
    Opnd *tauSrcNullChecked = irBuilder.genTauCheckNull(src);
    Opnd *tauDstNullChecked = irBuilder.genTauCheckNull(dst);
    
    LabelInst * reverseCopying = irBuilder.createLabel();
    LabelInst * boundsException = irBuilder.createLabel();
    LabelInst * L1 = irBuilder.createLabel();
    Type * intType = typeManager.getInt32Type();

    newFallthroughBlock();
    Opnd * zero = irBuilder.genLdConstant((int32)0);
    Opnd * one  = irBuilder.genLdConstant((int32)1);
    irBuilder.genBranch(Type::Int32,Cmp_GT,boundsException,zero,srcPos);        

    newFallthroughBlock();
    irBuilder.genBranch(Type::Int32,Cmp_GT,boundsException,zero,dstPos);

    newFallthroughBlock();
    irBuilder.genBranch(Type::Int32,Cmp_GT,boundsException,zero,len);

    Modifier mod = Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No);

    newFallthroughBlock();   
    Opnd * srcLen = irBuilder.genArrayLen(intType,Type::Int32,src);
    Opnd * srcEnd = irBuilder.genAdd(intType,mod,srcPos,len);
    irBuilder.genBranch(Type::Int32,Cmp_GT,boundsException,srcEnd,srcLen);
    
    newFallthroughBlock();
    Opnd * dstEnd = irBuilder.genAdd(intType,mod,dstPos,len);
    Opnd * dstLen = irBuilder.genArrayLen(intType,Type::Int32,dst);
    irBuilder.genBranch(Type::Int32,Cmp_GT,boundsException,dstEnd,dstLen);

    newFallthroughBlock();

    // The case of same arrays and same positions
    Opnd * diff = irBuilder.genCmp3(intType,Type::Int32,Cmp_GT,dstPos,srcPos);
    Opnd * sameArrays = irBuilder.genCmp(intType,Type::IntPtr,Cmp_EQ,src,dst);
    Opnd * zeroDiff = irBuilder.genCmp(intType,Type::Int32,Cmp_EQ,diff,zero);
    Opnd * nothingToCopy = irBuilder.genAnd(intType,sameArrays,zeroDiff);
    irBuilder.genBranch(Type::Int32,Cmp_GT,L1,nothingToCopy,zero);

    newFallthroughBlock();

    // Choosing direction

    irBuilder.genBranch(Type::Int32,Cmp_GT,reverseCopying,diff,zero);

    newFallthroughBlock();

    {   //Direct Copying

    // indexes for using inside the loop
    VarOpnd* srcPosVar = irBuilder.genVarDef(srcPosType, false);
    VarOpnd* dstPosVar = irBuilder.genVarDef(dstPosType, false);

    irBuilder.genStVar(srcPosVar, srcPos);
    irBuilder.genStVar(dstPosVar, dstPos);

    Opnd* srcPosOpnd = NULL;
    Opnd* dstPosOpnd = NULL;

    // Loop Header
    LabelInst * loopHead = irBuilder.createLabel();
    irBuilder.genLabel(loopHead);
    cfgBuilder.genBlockAfterCurrent(loopHead);

    // loop exit condition (srcIndex = srcStartIndex + len)
    srcPosOpnd = irBuilder.genLdVar(srcPosType,srcPosVar);
    irBuilder.genBranch(Type::Int32,Cmp_EQ,L1,srcPosOpnd,srcEnd);

    newFallthroughBlock();
    // array bounds have been checked directly 
    // the types have been checked above so tauAddressInRange is tauSafe
    Opnd *tauSrcAddressInRange = irBuilder.genTauSafe();
    Opnd *tauDstAddressInRange = irBuilder.genTauSafe();
    Opnd *tauDstBaseTypeChecked = irBuilder.genTauSafe();

    // load indexes
    srcPosOpnd = irBuilder.genLdVar(srcPosType,srcPosVar);
    dstPosOpnd = irBuilder.genLdVar(dstPosType,dstPosVar);

    Type* srcElemType = srcType->asArrayType()->getElementType();
    Type* dstElemType = dstType->asArrayType()->getElementType();

    // copy element
    // (Checks are performed before the loop)
    Opnd* elem = irBuilder.genLdElem(srcElemType,src,srcPosOpnd,
                                     tauSrcNullChecked, tauSrcAddressInRange);
    irBuilder.genStElem(dstElemType,dst,dstPosOpnd,elem,
                        tauDstNullChecked, tauDstBaseTypeChecked, tauDstAddressInRange);

    // increment indexes
    srcPosOpnd = irBuilder.genAdd(srcPosType,mod,srcPosOpnd,one);
    dstPosOpnd = irBuilder.genAdd(dstPosType,mod,dstPosOpnd,one);

    // store indexes
    irBuilder.genStVar(srcPosVar, srcPosOpnd);
    irBuilder.genStVar(dstPosVar, dstPosOpnd);

    // back edge
    irBuilder.genJump(loopHead);
    
    }   // End of Direct Copying

    {   //Reverse Copying
    irBuilder.genLabel(reverseCopying);
    cfgBuilder.genBlockAfterCurrent(reverseCopying);

    // indexes for using inside the loop
    VarOpnd* srcPosVar = irBuilder.genVarDef(srcPosType, false);
    VarOpnd* dstPosVar = irBuilder.genVarDef(dstPosType, false);

    Opnd* lastSrcIdx = irBuilder.genSub(srcPosType,mod,srcEnd,one);
    Opnd* lastDstIdx = irBuilder.genSub(dstPosType,mod,dstEnd,one);

    irBuilder.genStVar(srcPosVar, lastSrcIdx);
    irBuilder.genStVar(dstPosVar, lastDstIdx);

    Opnd* srcPosOpnd = NULL;
    Opnd* dstPosOpnd = NULL;

    // Loop Header
    LabelInst * loopHead = irBuilder.createLabel();
    irBuilder.genLabel(loopHead);
    cfgBuilder.genBlockAfterCurrent(loopHead);

    // loop exit condition (srcIndex < srcPos)
    srcPosOpnd = irBuilder.genLdVar(srcPosType,srcPosVar);
    irBuilder.genBranch(Type::Int32,Cmp_GT,L1,srcPos,srcPosOpnd);

    newFallthroughBlock();
    // array bounds have been checked directly 
    // the types have been checked above so tauAddressInRange is tauSafe
    Opnd *tauSrcAddressInRange = irBuilder.genTauSafe();
    Opnd *tauDstAddressInRange = irBuilder.genTauSafe();
    Opnd *tauDstBaseTypeChecked = irBuilder.genTauSafe();

    // load indexes
    srcPosOpnd = irBuilder.genLdVar(srcPosType,srcPosVar);
    dstPosOpnd = irBuilder.genLdVar(dstPosType,dstPosVar);

    Type* srcElemType = srcType->asArrayType()->getElementType();
    Type* dstElemType = dstType->asArrayType()->getElementType();

    // copy element
    // (Checks are performed before the loop)
    Opnd* elem = irBuilder.genLdElem(srcElemType,src,srcPosOpnd,
                                     tauSrcNullChecked, tauSrcAddressInRange);
    irBuilder.genStElem(dstElemType,dst,dstPosOpnd,elem,
                        tauDstNullChecked, tauDstBaseTypeChecked, tauDstAddressInRange);

    // decrement indexes
    srcPosOpnd = irBuilder.genSub(srcPosType,mod,srcPosOpnd,one);
    dstPosOpnd = irBuilder.genSub(dstPosType,mod,dstPosOpnd,one);

    // store indexes
    irBuilder.genStVar(srcPosVar, srcPosOpnd);
    irBuilder.genStVar(dstPosVar, dstPosOpnd);

    // back edge
    irBuilder.genJump(loopHead);

    }   // End of Reverse Copying


    irBuilder.genLabel(boundsException);
    cfgBuilder.genBlockAfterCurrent(boundsException);
    Opnd * minusOne = irBuilder.genLdConstant((int32)-1);
    irBuilder.genTauCheckBounds(src,minusOne,tauSrcNullChecked);

    irBuilder.genLabel(L1);
    cfgBuilder.genBlockAfterCurrent(L1);

    return true;
}

bool
JavaByteCodeTranslator::genCharArrayCopy(MethodDesc * methodDesc, 
                                         uint32       numArgs,
                                         Opnd **      srcOpnds,
                                         Type *       returnType) {
    //
    //  Check if method is java/lang/System.arraycopy
    //  (Object src, int srcPos, Object dst, int dstPos, int len)
    //
    if (strcmp(methodDesc->getName(),"arraycopy") != 0 ||
        strcmp(methodDesc->getParentType()->getName(),"java/lang/System") !=0)
          return false;
    //
    //  Check if arguments are arrays of characters
    //
    assert(numArgs == 5);
    Opnd * src = srcOpnds[0];
    Opnd * srcPos = srcOpnds[1];
    Opnd * dst = srcOpnds[2];
    Opnd * dstPos = srcOpnds[3];
    Opnd * len = srcOpnds[4];
    assert(src->getType()->isObject() &&
           srcPos->getType()->isInt4() &&
           dst->getType()->isObject() &&
           dstPos->getType()->isInt4() &&
           len->getType()->isInt4());
    Type * srcType = src->getType();
    Type * dstType = dst->getType();
    if (!(srcType->isArray() && dstType->isArray() &&
         ((ArrayType *)srcType)->getElementType()->isChar() &&
         ((ArrayType *)dstType)->getElementType()->isChar()))
         return false;

    if (Log::isEnabled()) {
        Log::out() << "XXX char array copy: "; methodDesc->printFullName(Log::out()); Log::out() << ::std::endl;
    }
    //
    //  Generate exception condition checks:
    //      chknull src
    //      chknull dst
    //      cmpbr srcPos < 0, boundsException
    //      cmpbr dstPos < 0, boundsException
    //      cmpbr len < 0, boundsException
    //      srcEnd = add srcPos, len
    //      srcLen = src.length
    //      cmpbr srcEnd > srcLen, boundsException
    //      dstEnd = add dstPos, len
    //      dstLen = dst.length
    //      cmpbr dstEnd > dstLen, boundsException
    //      callintr charArrayCopy(src,srcPos,dst,dstPos,len)
    //      goto L1:
    //  boundsException:
    //      chkbounds -1, src
    //  L1:
    //
    Opnd *tauSrcNullChecked = irBuilder.genTauCheckNull(src);
    Opnd *tauDstNullChecked = irBuilder.genTauCheckNull(dst);

    LabelInst * boundsException = irBuilder.createLabel();
    LabelInst * L1 = irBuilder.createLabel();
    Type * intType = typeManager.getInt32Type();

    newFallthroughBlock();
    Opnd * zero = irBuilder.genLdConstant((int32)0);
    irBuilder.genBranch(Type::Int32,Cmp_GT,boundsException,zero,srcPos);        

    newFallthroughBlock();
    irBuilder.genBranch(Type::Int32,Cmp_GT,boundsException,zero,dstPos);

    newFallthroughBlock();
    irBuilder.genBranch(Type::Int32,Cmp_GT,boundsException,zero,len);

    newFallthroughBlock();   
    Opnd * srcLen = irBuilder.genArrayLen(intType,Type::Int32,src);
    Opnd * srcEnd = irBuilder.genAdd(intType,Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No),srcPos,len);
    irBuilder.genBranch(Type::Int32,Cmp_GT,boundsException,srcEnd,srcLen);
    
    newFallthroughBlock();
    Opnd * dstEnd = irBuilder.genAdd(intType,Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No),dstPos,len);
    Opnd * dstLen = irBuilder.genArrayLen(intType,Type::Int32,dst);
    irBuilder.genBranch(Type::Int32,Cmp_GT,boundsException,dstEnd,dstLen);

    newFallthroughBlock();
    Opnd *tauNullCheckedRefArgs = 
        irBuilder.genTauAnd(tauSrcNullChecked, tauDstNullChecked);
    Opnd *tauTypesChecked = 0;
    irBuilder.genIntrinsicCall(CharArrayCopy,returnType,
                               tauNullCheckedRefArgs,
                               tauTypesChecked,
                               numArgs,srcOpnds);
    irBuilder.genJump(L1);

    irBuilder.genLabel(boundsException);
    cfgBuilder.genBlockAfterCurrent(boundsException);
    Opnd * minusOne = irBuilder.genLdConstant((int32)-1);
    irBuilder.genTauCheckBounds(src,minusOne,tauSrcNullChecked);

    irBuilder.genLabel(L1);
    cfgBuilder.genBlockAfterCurrent(L1);
    return true;
}

bool
JavaByteCodeTranslator::genMinMax(MethodDesc * methodDesc, 
                                  uint32       numArgs,
                                  Opnd **      srcOpnds,
                                  Type *       returnType) {

    const char *className = methodDesc->getParentType()->getName();
    if (strcmp(className, "java/lang/Math") == 0) {
        const char *methodName = methodDesc->getName();
        //
        //  Check for certain math methods and inline them
        // 
        if (strcmp(methodName, "min") == 0) {
            assert(numArgs == 2);
            Opnd *src0 = srcOpnds[0];
            Opnd *src1 = srcOpnds[1];
            Type *type = src0->getType();
            assert(type == src1->getType());

            IRManager& irm = *irBuilder.getIRManager();
            MethodDesc& md = irm.getMethodDesc();
            if (Log::isEnabled()) {
                Log::out() << "Saw call to java/lang/Math::min from "
                           << md.getParentType()->getName()
                           << "::"
                           << md.getName()
                           << ::std::endl;
            }
            Opnd *res = irBuilder.genMin(type, src0, src1);
            if (res) {
                pushOpnd(res);
                return true;
            }
        } else if (strcmp(methodName, "max") == 0) {
            assert(numArgs == 2);
            Opnd *src0 = srcOpnds[0];
            Opnd *src1 = srcOpnds[1];
            Type *type = src0->getType();
            assert(type == src1->getType());
            
            IRManager& irm = *irBuilder.getIRManager();
            MethodDesc& md = irm.getMethodDesc();
            if (Log::isEnabled()) {
                Log::out() << "Saw call to java/lang/Math::max from "
                           << md.getParentType()->getName()
                           << "::"
                           << md.getName()
                           << ::std::endl;
            }
            
            Opnd *res = irBuilder.genMax(type, src0, src1);
            if (res) {
                pushOpnd(res);
                return true;
            }
            
        } else if (strcmp(methodName, "abs") == 0) {
            assert(numArgs == 1);
            Opnd *src0 = srcOpnds[0];
            Type *type = src0->getType();
            
            IRManager& irm = *irBuilder.getIRManager();
            MethodDesc& md = irm.getMethodDesc();
            if (Log::isEnabled()) {
                Log::out() << "Saw call to java/lang/Math::abs from "
                           << md.getParentType()->getName()
                           << "::"
                           << md.getName()
                           << ::std::endl;
            }
            
            Opnd *res = irBuilder.genAbs(type, src0);
            if (res) {
                pushOpnd(res);
                return true;
            }
        } else {
            return false;
        }
    }
    return false;
}

//------------------------------------------------
//  synchronization helpers
//------------------------------------------------

void
JavaByteCodeTranslator::genMethodMonitorEnter() {
    if (translationFlags.ignoreSync) {
        popOpnd();
        return;
    }
    if (translationFlags.syncAsEnterFence) {
        irBuilder.genMonitorEnterFence(popOpnd());
    } 
    else if (! translationFlags.onlyBalancedSync) {
        irBuilder.genMonitorEnter(popOpnd());
    }
    else {
        assert(lockAddr == NULL && oldLockValue == NULL);
        Opnd * obj = popOpnd();
        Type * lockType = typeManager.getUInt16Type();
        Type * lockAddrType = typeManager.getManagedPtrType(lockType);
        Type * oldValueType = typeManager.getInt32Type();
        lockAddr = irBuilder.genLdLockAddr(lockAddrType,obj);
        oldLockValue = irBuilder.genBalancedMonitorEnter(oldValueType,obj,lockAddr);
    }
}

void
JavaByteCodeTranslator::genMethodMonitorExit() {
    if (translationFlags.ignoreSync || translationFlags.syncAsEnterFence) {
        popOpnd();
        return;
    }
    
    if (! translationFlags.onlyBalancedSync) {
        irBuilder.genMonitorExit(popOpnd());
    }
    else {
        assert(lockAddr != NULL && oldLockValue != NULL);
        irBuilder.genBalancedMonitorExit(popOpnd(),lockAddr,oldLockValue);
    }
}

uint32 JavaByteCodeTranslator::checkForArrayInitializer(Opnd* arrayOpnd, const uint8* byteCodes, uint32 offset, const uint32 byteCodeLength)
{
    assert(offset < byteCodeLength);
    const uint32 MIN_NUMBER_OF_INIT_ELEMS = 2;

    const uint8 BYTE_JAVA_SIZE    = 1;
    const uint8 SHORT_JAVA_SIZE   = 2;
    const uint8 INT_JAVA_SIZE     = 4;
    const uint8 LONG_JAVA_SIZE    = 8;

    // Skip short array initializers.
    // Average length of an array element initializer is 4.
    if ((byteCodeLength - offset)/4 < MIN_NUMBER_OF_INIT_ELEMS) return 0;

    // Size of the array elements
    uint8 elem_size = 0;
    // Number of initialized array elements
    uint32 elems = 0;

    ArrayType* arrayType = arrayOpnd->getType()->asArrayType();
    assert(arrayType);
    Type* elemType = arrayType->getElementType();
    if (elemType->isBoolean() || elemType->isInt1()) {
        elem_size = BYTE_JAVA_SIZE;
    } else if (elemType->isInt2() || elemType->isChar()) {
        elem_size = SHORT_JAVA_SIZE;
    } else if (elemType->isInt4() || elemType->isSingle()) {
        elem_size = INT_JAVA_SIZE;
    } else if (elemType->isInt8() || elemType->isDouble()) {
        elem_size = LONG_JAVA_SIZE;
    } else {
        assert(0);
    }

    ::std::vector<uint64> array_data;

    // Current offset.
    uint32 off = offset;
    uint32 predoff = offset;
    // Array element indexes
    uint64 oldIndex = 0;
    uint64 newIndex = 0;
    // Array element value
    uint64 value = 0;

    bool exitScan = false;
    uint32 tmpOff = 0;

    while (byteCodes[off++] == 0x59/*dup*/) {
        if (off >= byteCodeLength) break;

        // Get array element index
        tmpOff = getNumericValue(byteCodes, off, byteCodeLength, newIndex);
        if (!tmpOff || ((off += tmpOff) >= byteCodeLength)) break;
        if (newIndex != (oldIndex++)) break;

        // Get array element value
        tmpOff = getNumericValue(byteCodes, off, byteCodeLength, value);
        if (!tmpOff || ((off += tmpOff) >= byteCodeLength)) break;

        // Store array element
        switch (byteCodes[off++]) {
            case 0x4f:        // iastore
                assert(elem_size == INT_JAVA_SIZE);
                array_data.push_back((uint64)value);
                break;
            case 0x50:        // lastore
                assert(elem_size == LONG_JAVA_SIZE);
                array_data.push_back((uint64)value);
                break;
            case 0x51:        // fastore
                assert(elem_size == INT_JAVA_SIZE);
                array_data.push_back((uint64)value);
                break;
            case 0x52:        // dastore
                assert(elem_size == LONG_JAVA_SIZE);
                array_data.push_back((uint64)value);
                break;
            case 0x54:        // bastore
                assert(elem_size == BYTE_JAVA_SIZE);
                array_data.push_back((uint64)value);
                break;
            case 0x55:        // castore
                assert(elem_size == SHORT_JAVA_SIZE);
                array_data.push_back((uint64)value);
                break;
            case 0x56:        // sastore
                assert(elem_size == SHORT_JAVA_SIZE);
                array_data.push_back((uint64)value);
                break;
            default:
                exitScan = true;
                break;
        }
        if (exitScan || (off >= byteCodeLength)) break;
        predoff = off;
        elems++;
    }/*end_while*/

    if (elems < MIN_NUMBER_OF_INIT_ELEMS) return 0;

    const uint32 data_size = elems* elem_size;
    uint8* init_array_data = new uint8[data_size];

    for (uint32 i = 0; i < elems; i++) {
        switch (elem_size) {
            case BYTE_JAVA_SIZE:
                init_array_data[i] = (uint8)(array_data[i]);
                break;
            case SHORT_JAVA_SIZE:
                *((uint16*)(init_array_data + (i * SHORT_JAVA_SIZE))) = (uint16)(array_data[i]);
                break;
            case INT_JAVA_SIZE:
                *((uint32*)(init_array_data + (i * INT_JAVA_SIZE))) = (uint32)(array_data[i]);
                break;
            case LONG_JAVA_SIZE:
                *((uint64*)(init_array_data + (i * LONG_JAVA_SIZE))) = (uint64)(array_data[i]);
                break;
           default:
                assert(0);
        }
    }

    Type* returnType = typeManager.getVoidType();
    Opnd* arrayDataOpnd = irBuilder.genLdConstant((POINTER_SIZE_SINT)init_array_data);
    Opnd* arrayElemsOffset = irBuilder.genLdConstant((int32)(arrayType->getArrayElemOffset()));
    Opnd* elemsOpnd = irBuilder.genLdConstant((int32)data_size);

    const uint32 numArgs = 4;
    Opnd* args[numArgs] = {arrayOpnd, arrayElemsOffset, arrayDataOpnd, elemsOpnd};
    irBuilder.genJitHelperCall(InitializeArray, returnType, numArgs, args);


    return predoff - offset;
}

uint32 JavaByteCodeTranslator::getNumericValue(const uint8* byteCodes, uint32 offset, const uint32 byteCodeLength, uint64& value) {
    assert(offset < byteCodeLength);
    uint32 off = offset;
    switch (byteCodes[off++]) {
        case 0x02:        // iconst_m1
            value = (uint64)(-1);
            break;
        case 0x03:        // iconst_0
        case 0x09:        // lconst_0
            value = 0;
            break;
        case 0x04:        // iconst_1
        case 0x0a:        // lconst_1
            value = 1;
            break;
        case 0x05:        // iconst_2
            value = 2;
            break;
        case 0x06:        // iconst_3
            value = 3;
            break;
        case 0x07:        // iconst_4
            value = 4;
            break;
        case 0x08:        // iconst_5
            value = 5;
            break;
        case 0x0b:        // fconst_0
            {
                float val = 0.0f;
                value = (uint64)(*((uint32*)(&val)));
            }
            break;
        case 0x0c:        // fconst_1
            {
                float val = 1.0f;
                value = (uint64)(*((uint32*)(&val)));
            }
            break;
        case 0x0d:        // fconst_2
            {
                float val = 2.0f;
                value = (uint64)(*((uint32*)(&val)));
            }
            break;
        case 0x0e:        // dconst_0
            {
                double val = 0.0;
                value = *((uint64*)(&val));
            }
            break;
        case 0x0f:        // dconst_1
            {
                double val = 1.0;
                value = *((uint64*)(&val));
            }
            break;
        case 0x10:        // bipush
            if (off >= byteCodeLength) return 0;
            value = (uint64)si8(byteCodes + (off++));
            break;
        case 0x11:        // sipush
            if ((off + 1) >= byteCodeLength) return 0;
            value = (uint64)si16(byteCodes + off);
            off += 2;
            break;
        case 0x12:        // ldc
            {
                if (off >= byteCodeLength) return 0;
                uint32 constPoolIndex = su8(byteCodes + (off++));
                // load 32-bit quantity from constant pool
                Type* constantType = compilationInterface.getConstantType(&methodToCompile,constPoolIndex);
                if ( !(constantType->isInt4() || constantType->isSingle()) ) {
                    // only integer and floating-point types 
                    //     are implemented for streamed array loads
                    return 0;
                }
                const void* constantAddress =
                    compilationInterface.getConstantValue(&methodToCompile,constPoolIndex);
                value = *(uint32*)constantAddress;
            }
            break;
        case 0x13:        // ldc_w
            {
                if ((off + 1) >= byteCodeLength) return 0;
                uint32 constPoolIndex = su16(byteCodes + off);
                // load 32-bit quantity from constant pool
                Type* constantType = compilationInterface.getConstantType(&methodToCompile,constPoolIndex);
                if ( !(constantType->isInt4() || constantType->isSingle()) ) {
                    // only integer and floating-point types 
                    //     are implemented for streamed array loads
                    return 0;
                }
                const void* constantAddress =
                    compilationInterface.getConstantValue(&methodToCompile,constPoolIndex);
                value = *(uint32*)constantAddress;
            }
            off += 2;
            break;
        case 0x14:        // ldc2_w
            {
                if ((off + 1) >= byteCodeLength) return 0;
                uint32 constPoolIndex = su16(byteCodes + off);
                // load 64-bit quantity from constant pool
                Type* constantType = compilationInterface.getConstantType(&methodToCompile,constPoolIndex);
                if ( !(constantType->isInt8() || constantType->isDouble()) ) {
                    // only integer and floating-point types 
                    //     are implemented for streamed array loads
                    return 0;
                }
                const void* constantAddress =
                    compilationInterface.getConstantValue(&methodToCompile,constPoolIndex);
                value = *(uint64*)constantAddress;
            }
            off += 2;
            break;
        default:
            return 0;
    }
    return off - offset;
}

void JavaByteCodeTranslator::genMagic(MethodDesc *md, uint32 numArgs, Opnd **srcOpnds, Type *magicRetType) {
    const char* mname = md->getName();
    Type* resType = convertMagicType2HIR(typeManager, magicRetType);
    Opnd* tauSafe = irBuilder.genTauSafe();
    Opnd* arg0 = numArgs > 0 ? srcOpnds[0]: NULL;
    Opnd* arg1 = numArgs > 1 ? srcOpnds[1]: NULL;
    Modifier mod = Modifier(Overflow_None)|Modifier(Exception_Never)|Modifier(Strict_No);

    
    // max, one, zero
    int theConst = 0;
    bool loadConst = false;
    if (!strcmp(mname, "max"))          { loadConst = true; theConst = -1;}
    else if (!strcmp(mname, "one"))     { loadConst = true; theConst =  1;}
    else if (!strcmp(mname, "zero"))    { loadConst = true; theConst =  0;}
    else if (!strcmp(mname, "nullReference")) { loadConst = true; theConst =  0;}
    if (loadConst) {
        //todo: recheck and fix the type of the const:
        ConstInst::ConstValue v; v.i4 = theConst;
        Opnd* res = irBuilder.genLdConstant(typeManager.getUIntPtrType(), v);//todo:clean typing
        if (resType->isPtr()) {
            res = irBuilder.genConv(resType, resType->tag, mod, res);
        }
        pushOpnd(res);
        return;
    }

    //
    // fromXXX, toXXX - static creation from something
    //
    if (!strcmp(mname, "fromInt") 
        || !strcmp(mname, "fromIntSignExtend") 
        || !strcmp(mname, "fromIntZeroExtend")
        || !strcmp(mname, "fromObject")  
        || !strcmp(mname, "toAddress") 
        || !strcmp(mname, "toObjectReference")
        || !strcmp(mname, "toInt")
        || !strcmp(mname, "toLong")
        || !strcmp(mname, "toObjectRef")
        || !strcmp(mname, "toWord")
        || !strcmp(mname, "toAddress")
        || !strcmp(mname, "toObject")
        || !strcmp(mname, "toExtent")
        || !strcmp(mname, "toOffset"))
    {
        assert(numArgs == 1);
        if (resType == arg0->getType()) {
            pushOpnd(irBuilder.genCopy(arg0));
            return;
        } 
        Opnd* res = irBuilder.genConv(resType, resType->tag, mod, arg0);
        pushOpnd(res);
        return;
    }

    //
    // is<Smth> one arg testing
    //
    bool isOp = false;
    if (!strcmp(mname, "isZero")) { isOp = true; theConst = 0; }
    else if (!strcmp(mname, "isMax")) { isOp = true; theConst = ~0; }
    else if (!strcmp(mname, "isNull")) { isOp = true; theConst = 0; }
    if (isOp) {
        assert(numArgs == 1);
        Opnd* res = irBuilder.genCmp(typeManager.getInt32Type(), Type::Int32, Cmp_EQ, arg0, irBuilder.genLdConstant(theConst));
        pushOpnd(res);
        return;
    }


    //
    // EQ, GE, GT, LE, LT, sXX - 2 args compare
    //
    ComparisonModifier cm = Cmp_Mask;
    bool commuteOpnds=false;
    if (!strcmp(mname, "EQ"))         { cm = Cmp_EQ; }
    else if (!strcmp(mname, "equals")){ cm = Cmp_EQ; }
    else if (!strcmp(mname, "NE"))    { cm = Cmp_NE_Un; }
    else if (!strcmp(mname, "GE"))    { cm = Cmp_GTE_Un;}
    else if (!strcmp(mname, "GT"))    { cm = Cmp_GT_Un; }
    else if (!strcmp(mname, "LE"))    { cm = Cmp_GTE_Un; commuteOpnds = true;}
    else if (!strcmp(mname, "LT"))    { cm = Cmp_GT_Un;  commuteOpnds = true;}
    else if (!strcmp(mname, "sGE"))   { cm = Cmp_GTE;}
    else if (!strcmp(mname, "sGT"))   { cm = Cmp_GT; }
    else if (!strcmp(mname, "sLE"))   { cm = Cmp_GTE; commuteOpnds = true;}
    else if (!strcmp(mname, "sLT"))   { cm = Cmp_GT;  commuteOpnds = true;}

    if (cm!=Cmp_Mask) {
        assert(numArgs == 2);
        assert(arg0->getType() == arg1->getType());
        Opnd* op0 = commuteOpnds ? arg1 : arg0;
        Opnd* op1 = commuteOpnds ? arg0 : arg1;
        Opnd* res = irBuilder.genCmp(typeManager.getInt32Type(), Type::Int32, cm, op0, op1);
        pushOpnd(res);
        return;
    }

   
    //
    // plus, minus, xor, or, and ... etc - 1,2 args arithmetics
    //
    if (!strcmp(mname, "plus")) { 
        assert(numArgs==2); 
        if (resType->isPtr()) {
            pushOpnd(irBuilder.genAddScaledIndex(arg0, arg1)); 
        } else {
            pushOpnd(irBuilder.genAdd(resType, mod, arg0, arg1)); 
        }
        return;
    }
    if (!strcmp(mname, "minus")){ 
        assert(numArgs==2); 
        if (resType->isPtr()) {
            Opnd* negArg1 = irBuilder.genNeg(typeManager.getInt32Type(), arg1);
            pushOpnd(irBuilder.genAddScaledIndex(arg0, negArg1)); 
        } else {
            pushOpnd(irBuilder.genSub(resType, mod, arg0, arg1)); 
        }
        return;
    }
    if (!strcmp(mname, "or"))   { assert(numArgs==2); pushOpnd(irBuilder.genOr (resType, arg0, arg1)); return;}
    if (!strcmp(mname, "xor"))  { assert(numArgs==2); pushOpnd(irBuilder.genXor(resType, arg0, arg1)); return;}
    if (!strcmp(mname, "and"))  { assert(numArgs==2); pushOpnd(irBuilder.genAnd(resType, arg0, arg1)); return;}
    if (!strcmp(mname, "not"))  { assert(numArgs==1); pushOpnd(irBuilder.genNot(resType, arg0)); return;}
    if (!strcmp(mname, "diff")) { assert(numArgs==2); pushOpnd(irBuilder.genSub(resType, mod, arg0, arg1)); return;}

    
    //
    // shifts
    //
    Modifier shMod(ShiftMask_Masked);
    if (!strcmp(mname, "lsh"))      {assert(numArgs==2); pushOpnd(irBuilder.genShl(resType, shMod|SignedOp, arg0, arg1));  return;}
    else if (!strcmp(mname, "rsha")){assert(numArgs==2); pushOpnd(irBuilder.genShr(resType, shMod|SignedOp, arg0, arg1)); return;}
    else if (!strcmp(mname, "rshl")){assert(numArgs==2); pushOpnd(irBuilder.genShr(resType, shMod |UnsignedOp, arg0, arg1)); return;}

    
    //
    // loadXYZ.. prepareXYZ..
    //
    if (!strcmp(mname, "loadObjectReference")
        || !strcmp(mname, "loadAddress")
        || !strcmp(mname, "loadWord")
        || !strcmp(mname, "loadByte")
        || !strcmp(mname, "loadChar")
        || !strcmp(mname, "loadDouble")
        || !strcmp(mname, "loadFloat")
        || !strcmp(mname, "loadInt")
        || !strcmp(mname, "loadLong")
        || !strcmp(mname, "loadShort")
        || !strcmp(mname, "prepareWord")
        || !strcmp(mname, "prepareObjectReference")
        || !strcmp(mname, "prepareAddress")
        || !strcmp(mname, "prepareInt"))
    {
        assert(numArgs == 1 || numArgs == 2);
        Opnd* effectiveAddress = arg0;
        if (numArgs == 2) {//load by offset
            effectiveAddress = irBuilder.genAddScaledIndex(arg0, arg1);
        }
        Opnd* res = irBuilder.genTauLdInd(AutoCompress_No, resType, resType->tag, effectiveAddress, tauSafe, tauSafe);
        pushOpnd(res);
        return;
    }

    //
    // store(XYZ)
    //
    if (!strcmp(mname, "store")) {
        assert(numArgs==2 || numArgs == 3);
        Opnd* effectiveAddress = arg0;
        if (numArgs == 3) { // store by offset
            effectiveAddress = irBuilder.genAddScaledIndex(arg0, srcOpnds[2]);
        }
        irBuilder.genTauStInd(arg1->getType(), effectiveAddress, arg1, tauSafe, tauSafe, tauSafe);
        return;
    }

    if (!strcmp(mname, "attempt")) {
        assert(numArgs == 3 || numArgs == 4);
        Opnd* effectiveAddress = arg0;
        if (numArgs == 4) { // offset opnd
            effectiveAddress = irBuilder.genAddScaledIndex(arg0, srcOpnds[3]);
        }
        Opnd* opnds[3] = {effectiveAddress, arg1, srcOpnds[2]};
        Opnd* res = irBuilder.genJitHelperCall(LockedCompareAndExchange, resType, 3, opnds);
        pushOpnd(res);
        return;
    }

    //
    //Arrays
    //
    if (!strcmp(mname, "create")) { assert(numArgs==1); pushOpnd(irBuilder.genNewArray(resType->asNamedType(),arg0)); return;} 
    if (!strcmp(mname, "set")) {
        assert(numArgs == 3);
        Opnd* arg2 = srcOpnds[2];
        Type* opType = convertMagicType2HIR(typeManager, arg2->getType());
        irBuilder.genStElem(opType, arg0, arg1, arg2, tauSafe, tauSafe, tauSafe); 
        return;
    }
    if (!strcmp(mname, "get")) {
        assert(numArgs == 2);
        Opnd* res = irBuilder.genLdElem(resType, arg0, arg1, tauSafe, tauSafe);
        pushOpnd(res);
        return;
    }
    if (!strcmp(mname, "length")) {    
        pushOpnd(irBuilder.genArrayLen(typeManager.getInt32Type(), Type::Int32, arg0));
        return;
    }

    assert(0);
    return;
}

} //namespace Jitrino 
