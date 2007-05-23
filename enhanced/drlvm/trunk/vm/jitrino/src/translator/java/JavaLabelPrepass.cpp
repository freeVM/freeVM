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
 * @version $Revision: 1.40.12.2.4.4 $
 */
 
#include <stdio.h>
#include <iostream>

#include "Log.h"
#include "IRBuilder.h"
#include "JavaLabelPrepass.h"
#include "TranslatorIntfc.h"
#include "ExceptionInfo.h"
#include "simplifier.h"     // for isExactType and isNonNullObject

namespace Jitrino {

VariableIncarnation::VariableIncarnation(uint32 offset, uint32 block, Type* t)
: definingOffset(offset), definingBlock(block), declaredType(t), opnd(NULL)
{
    _prev = _next = NULL;
}

void VariableIncarnation::setMultipleDefs()
{
    definingOffset = -1;
    definingBlock = -1;
}

void VariableIncarnation::ldBlock(int32 blockNumber)
{
    if (definingBlock!=blockNumber) definingBlock = -1;
}

Type* VariableIncarnation::getDeclaredType()
{
    return declaredType;
}

void VariableIncarnation::setDeclaredType(Type* t)
{
    declaredType = t;
}

void VariableIncarnation::setCommonType(Type* t) {
    declaredType = t;
    VariableIncarnation* tmp;
    tmp = (VariableIncarnation*)_prev;
    while (tmp) {
        tmp->declaredType = t;
        tmp = (VariableIncarnation*)tmp->_prev;
    }
    tmp = (VariableIncarnation*)_next;
    while (tmp) {
        tmp->declaredType = t;
        tmp = (VariableIncarnation*)tmp->_next;
    }
}

void VariableIncarnation::linkIncarnations(VariableIncarnation* vi1, VariableIncarnation* vi2)
{
    if (vi1==vi2) return;
    while (vi1->_next) vi1 = (VariableIncarnation*)vi1->_next;
    VariableIncarnation* tmp = vi2;
    while (tmp->_next) tmp = (VariableIncarnation*)tmp->_next;
    if (vi1==tmp) return;
    while (vi2->_prev) vi2 = (VariableIncarnation*)vi2->_prev;
    vi1->_next = vi2;
    vi2->_prev = vi1;
}

void VariableIncarnation::mergeIncarnations(TypeManager* tm)
{
    mergeIncarnations(0,tm);
}

void VariableIncarnation::mergeIncarnations(Type* t, TypeManager* tm)
{
    VariableIncarnation* vi = this;
    while (vi->_prev) vi = (VariableIncarnation*)vi->_prev;
    // return if there is only one incarnation in chain.
    if (!(t || vi->_next)) return;

    if(Log::isEnabled()) {
        Log::out() << "Merging incarnations:" << ::std::endl;
        if (t) {
            Log::out() << "    assigning common type:";
            t->print(Log::out());
            Log::out() << ::std::endl;
        }
        VariableIncarnation* tmp = vi;
        do {
            tmp->getDeclaredType()->print(Log::out());
            Log::out() << " (" << tmp << ",DO=" << tmp->definingOffset << ") <=>";
            tmp = (VariableIncarnation*)tmp->_next;
        } while (tmp);
        Log::out() << " end-of-list";
    }

    Type* varType = (t ? tm->getCommonType(vi->getDeclaredType(),t) : vi->getDeclaredType());
    // Since we merge only types which are live at use there should be a common type
    assert(varType);
    for (vi = (VariableIncarnation*)vi->_next; vi; vi = (VariableIncarnation*)vi->_next) {
        varType = tm->getCommonType(varType, vi->getDeclaredType());
    }
    assert(varType);
    if(Log::isEnabled()) {
        Log::out() << "    ;;set common type: ";
        varType->print(Log::out());
        Log::out() << ::std::endl;
    }
    setCommonType(varType);
    setMultipleDefs();
}

void VariableIncarnation::linkAndMergeIncarnations(VariableIncarnation* vi1, VariableIncarnation* vi2, Type* t, TypeManager* tm) {
    linkIncarnations(vi1, vi2);
    vi1->mergeIncarnations(t, tm);
}

void VariableIncarnation::linkAndMergeIncarnations(VariableIncarnation* vi1, VariableIncarnation* vi2, TypeManager* tm) {
    linkIncarnations(vi1, vi2);
    vi1->mergeIncarnations(tm);
}

void VariableIncarnation::print(::std::ostream& out) {
    VariableIncarnation* tmp = this;
    do {
        tmp->getDeclaredType()->print(out);
        out << " (" << tmp << ",DO=" << tmp->definingOffset << ") <-i->";
        tmp = (VariableIncarnation*)tmp->_next;
    } while (tmp);
    out << ::std::endl;
}


void SlotVar::print(::std::ostream& out) {
    SlotVar* tmp = this;
    do {
        VariableIncarnation* cur_var_inc = tmp->getVarIncarnation();
        cur_var_inc->getDeclaredType()->print(out);
        out << " (" << cur_var_inc << ",DO=" << cur_var_inc->definingOffset << ",LO=" << linkOffset << ") <->";
        tmp = (SlotVar*)tmp->_next;
    } while (tmp);
    out << ::std::endl;
}


bool SlotVar::addVarIncarnations(SlotVar* var, MemoryManager& mm, uint32 linkOffset) {
    assert(var->_prev == NULL);
    SlotVar* this_sv = this;
    while(this_sv->_next) this_sv = (SlotVar*)this_sv->_next;

    bool added = false;
    bool found = false;
    for (SlotVar* in_var = var; in_var; in_var = (SlotVar*)in_var->_next) {
        found = false;
        VariableIncarnation* var_inc = in_var->getVarIncarnation();
        for (SlotVar* sv = this; sv; sv = (SlotVar*)sv->_next) {
            if (var_inc == sv->getVarIncarnation()) {
                found = true;
                break;
            }
        }
        if (found) continue;
        this_sv->_next = new (mm) SlotVar(var_inc);
        this_sv = (SlotVar*)this_sv->_next;
        added = true;
    }
    if (added) this->linkOffset = linkOffset;
    return added;
}

void SlotVar::mergeVarIncarnations(TypeManager* tm) {
    for (SlotVar* tmp = (SlotVar*)_next; tmp; tmp = (SlotVar*)tmp->_next) {
        VariableIncarnation* var_inc = tmp->getVarIncarnation();
        VariableIncarnation::linkIncarnations(var, var_inc);
        //var_inc ->setMultipleDefs();
    }
    var->mergeIncarnations(tm);
//    _next = NULL;
}


Opnd* VariableIncarnation::getOpnd()
{
    return opnd;
}

Opnd* VariableIncarnation::getOrCreateOpnd(IRBuilder* irBuilder)
{
    if (!opnd)
        createVarOpnd(irBuilder);
    return opnd;
}

void VariableIncarnation::createVarOpnd(IRBuilder* irBuilder)
{
    if (opnd) return;
    opnd = irBuilder->genVarDef(declaredType, false);

    if(Log::isEnabled()) {
        Log::out() << "Create operand for VarIncarnation:" << ::std::endl;
        Log::out() << "    opnd:"; opnd->print(Log::out()); Log::out() << ::std::endl;
        Log::out() << "    VarInc:"; print(Log::out()); Log::out() << ::std::endl;
    }

    VariableIncarnation* tmp;
    tmp = (VariableIncarnation*)_prev;
    while (tmp) {
        tmp->opnd = opnd;
        tmp = (VariableIncarnation*)tmp->_prev;
    }
    tmp = (VariableIncarnation*)_next;
    while (tmp) {
        tmp->opnd = opnd;
        tmp = (VariableIncarnation*)tmp->_next;
    }
}

void VariableIncarnation::setTmpOpnd(Opnd* tmpOpnd)
{
    assert(!opnd);
    opnd = tmpOpnd;
}

void VariableIncarnation::createMultipleDefVarOpnd(IRBuilder* irBuilder)
{
    if (definingOffset==-1) createVarOpnd(irBuilder);
}

void  StateInfo::addExceptionInfo(ExceptionInfo *info)
{
    if ( !info->isCatchBlock() ) {
        info->setNextExceptionInfoAtOffset(exceptionInfo);
        exceptionInfo = info;
    }else{
        ExceptionInfo *exc;
        ExceptionInfo *prev = NULL;
        for (exc = exceptionInfo; exc != NULL; exc = exc->getNextExceptionInfoAtOffset()) {
            if (exc->isCatchBlock() &&
                ((CatchBlock *)exc)->getExcTableIndex() > ((CatchBlock *)info)->getExcTableIndex()) {
                break;
            }
            prev = exc;
        }
        if (prev == NULL) {
            info->setNextExceptionInfoAtOffset(exceptionInfo);
            exceptionInfo = info;
        } else {
            info->setNextExceptionInfoAtOffset(prev->getNextExceptionInfoAtOffset());
            prev->setNextExceptionInfoAtOffset(info);
        }
    }
}

void StateInfo::cleanFinallyInfo(uint32 offset)
{
    for (int32 k=0; k < stackDepth; k++) {
        if (stack[k].jsrLabelOffset == offset) {
            stack[k].jsrLabelOffset = 0;
            stack[k].type = 0;
            stack[k].slotFlags = 0;
            stack[k].vars = NULL;
            stack[k].varNumber = 0;
        }
    }
}

class JavaExceptionParser {
public:
    JavaExceptionParser(MemoryManager& mm,JavaLabelPrepass& p,
                    CompilationInterface& ci,MethodDesc* method) 
        : numCatch(0), memManager(mm), prepass(p), 
          compilationInterface(ci), enclosingMethod(method),
          prevCatch(NULL), nextRegionId(0) {}

    uint32 parseHandlers() {
        uint32 numHandlers = enclosingMethod->getNumHandlers();
        for (uint32 i=0; i<numHandlers; i++) {
            unsigned beginOffset,endOffset,handlerOffset,handlerClassIndex;
            enclosingMethod->getHandlerInfo(i,&beginOffset,&endOffset,
                &handlerOffset,&handlerClassIndex);
            if (!catchBlock(beginOffset,endOffset-beginOffset,
                handlerOffset,0,handlerClassIndex))
            {
                // handlerClass failed to be resolved. LinkingException throwing helper
                // will be generated instead of method's body
                return handlerClassIndex;
            }
        }
        return MAX_UINT32; // all catchBlocks were processed successfully
    }

    void addHandlerForCatchBlock(CatchBlock* block, 
                                      uint32 handlerOffset,
                                      uint32 handlerLength,
                                      Type*  exceptionType) {
        jitrino_assert( exceptionType);
        assert(!exceptionType->isUnresolvedType());//must be resolved by verifier
        Log::out() << "Catch Exception Type = " << exceptionType->getName() << ::std::endl;

        CatchHandler* handler = new (memManager) 
            CatchHandler(nextRegionId++,
                         handlerOffset,
                         handlerOffset+handlerLength,
                         block,
                         exceptionType);
        block->addHandler(handler);
        StateInfo *catchInfo = prepass.stateTable->createStateInfo(handlerOffset);
        catchInfo->setCatchLabel();
        catchInfo->addExceptionInfo(handler);
    }

    CatchBlock* splitBlockWithOffset(CatchBlock* block, uint32 offset)
    {
        CatchBlock* newBlock = 
            new (memManager) CatchBlock(nextRegionId++, 
                                        offset,
                                        block->getEndOffset(),
                                        block->getExcTableIndex());
        assert(prepass.stateTable->getStateInfo(offset));
        prepass.stateTable->getStateInfo(offset)->addExceptionInfo(newBlock);
        block->setEndOffset(offset);

        //
        // copy all handlers
        //

        for (CatchHandler* handler = block->getHandlers(); 
                           handler != NULL; 
                           handler = handler->getNextHandler() ) {
            CatchHandler* newHandler = 
                new (memManager) CatchHandler(nextRegionId++,
                                              handler->getBeginOffset(),
                                              handler->getEndOffset(),
                                              newBlock,
                                              handler->getExceptionType());
            newBlock->addHandler(newHandler);
            assert(prepass.stateTable->getStateInfo(handler->getBeginOffset()));
            prepass.stateTable->getStateInfo(handler->getBeginOffset())->addExceptionInfo(newHandler);
        }
        return newBlock;
    }

    bool catchBlock(uint32 tryOffset,
                            uint32 tryLength,
                            uint32 handlerOffset,
                            uint32 handlerLength,
                            uint32 exceptionTypeToken)  {
 
        Log::out() << "CatchBlock @ " << (int)tryOffset << "," << (int)tryOffset+(int)tryLength
             << " handler @ " << (int)handlerOffset << "," << (int)handlerOffset+(int)handlerLength
             << " exception type " << (int)exceptionTypeToken << ","
             << " numCatch " << numCatch << ::std::endl;

        uint32 endOffset = tryOffset + tryLength;
        prepass.setLabel(handlerOffset);
        prepass.setLabel(handlerOffset+handlerLength);
        prepass.setLabel(tryOffset);
        prepass.setLabel(endOffset);
  
        bool unnested_try_regions_found = false;
        CatchBlock* catchBlock;

        Type* exceptionType = NULL;
        if (exceptionTypeToken != 0) {
            exceptionType = compilationInterface.getNamedType(enclosingMethod->getParentHandle(),exceptionTypeToken, ResolveNewCheck_NoCheck);
            if(!exceptionType) { // the type can not be resolved. LinkingException must be thrown
                return 0;
            }
            if (exceptionType->isUnresolvedObject()) {
                //WORKAROUND! resolving exception type during a compilation session!!!
                //Details: using singleton UnresolvedObjectType we unable to 
                //distinct exception types if there are several unresolved exceptions in a single try block
                //usually verifier loads all exception types caught for in method
                //but verifier is turned off for bootstrap classes
                Log::out()<<"WARNING: resolving type from inside of compilation session!!"<<std::endl;
                exceptionType = compilationInterface.resolveNamedType(enclosingMethod->getParentHandle(),exceptionTypeToken);
            }
        } else {
            exceptionType = prepass.typeManager.getSystemObjectType();
        }

        if (prevCatch != NULL && prevCatch->equals(tryOffset, endOffset) == true) {
            catchBlock = prevCatch;
            addHandlerForCatchBlock(catchBlock, handlerOffset, handlerLength, exceptionType);
        } else {
            prepass.stateTable->createStateInfo(tryOffset);
            // 
            // split all previous CatchBlocks that:
            // 1. intersect with current CatchBlock 
            // 2. are not contained by current CatchBlock
            //    (CB splitting will result in CBs with same excTableIndex, 
            //     but it is no problem since splitted CBs do not intersect)
            // Our aim is to make all try-blocks nested
            //
            for ( JavaLabelPrepass::ExceptionTable::iterator block_it = prepass.exceptionTable.begin();
                  block_it != prepass.exceptionTable.end();
                  block_it++ ) {
                CatchBlock* block = *block_it;
                if ( block->offsetSplits(tryOffset) || block->offsetSplits(endOffset) ) {
                    if ( !unnested_try_regions_found ) {
                        unnested_try_regions_found = true;
                        Log::out() << "unnested try-regions encountered" << std::endl;
                    }
                }
                assert(tryOffset < endOffset);
                if ( block->offsetSplits(tryOffset) ) {
                    block = splitBlockWithOffset(block, tryOffset);
                    prepass.exceptionTable.insert(block_it, block);
                }
                if ( block->offsetSplits(endOffset) ) {
                    prepass.stateTable->createStateInfo(endOffset);
                    block = splitBlockWithOffset(block, endOffset);
                    prepass.exceptionTable.insert(block_it, block);
                }
                assert( !(block->offsetSplits(tryOffset) || block->offsetSplits(endOffset)) );
            }

            //
            // add new CatchBlock
            //
            prevCatch = catchBlock = 
                new (memManager) CatchBlock(nextRegionId++, tryOffset, endOffset, numCatch++);
            prepass.stateTable->getStateInfo(tryOffset)->addExceptionInfo(catchBlock);
            prepass.exceptionTable.push_back(catchBlock);
            addHandlerForCatchBlock(catchBlock, handlerOffset, handlerLength, exceptionType);
        }
        return 1; // all exceptionTypes are OK
    }

    uint32 numCatch;
    MemoryManager&            memManager;
    JavaLabelPrepass&      prepass;
    CompilationInterface&   compilationInterface;
    MethodDesc*             enclosingMethod;
    CatchBlock*             prevCatch;
    uint32                  nextRegionId;
};


JavaLabelPrepass::JavaLabelPrepass(MemoryManager& mm,
                                   TypeManager& tm, 
                                   MemoryManager& irManager,
                                   MethodDesc& md,
                                   CompilationInterface& ci, 
                                   Opnd **actualArgs) 
 : JavaByteCodeParserCallback(mm,md.getByteCodeSize()), 
   memManager(mm), 
   typeManager(tm), 
   methodDesc(md), 
   compilationInterface(ci),
   localVars(mm),
   jsrEntriesMap(mm),
   retToSubEntryMap(mm),
   exceptionTable(mm),
   problemTypeToken(MAX_UINT32)
{
    uint32 numByteCodes = methodDesc.getByteCodeSize();
    //nextIsLabel = false;
    int32Type = typeManager.getInt32Type();
    int64Type = typeManager.getInt64Type();
    singleType = typeManager.getSingleType();
    doubleType= typeManager.getDoubleType();

    numLabels = 0;
    numVars = methodDesc.getNumVars();
    labels = new (memManager) BitSet(memManager,numByteCodes);
    subroutines = new (memManager) BitSet(memManager,numByteCodes);
    int numStack = md.getMaxStack()+1;
    stateInfo.stack  = new (memManager) struct StateInfo::SlotInfo[numVars+numStack];
    stateInfo.stackDepth = numVars;
    for (uint32 k=0; k < numVars+numStack; k++) {
        struct StateInfo::SlotInfo *slot = &stateInfo.stack[k];
        slot->type = NULL;
        slot->slotFlags = 0;
        slot->vars = NULL;
        slot->jsrLabelOffset = 0;
    }
    blockNumber  = 0;
    labelOffsets = NULL;
    // exceptions
    stateTable = new (memManager)  StateTable(memManager,typeManager,*this,20,numVars);

    // 1st count number of catch and finally blocks
    // parse and create exception info
    JavaExceptionParser exceptionTypes(irManager,*this,compilationInterface,&methodDesc);
    // fix exception handlers
    unsigned problemToken = exceptionTypes.parseHandlers();
    if(problemToken != MAX_UINT32)
    {
        problemTypeToken = problemToken;
        noNeedToParse = true;
        numCatchHandlers = 0;
    } else {
        numCatchHandlers = exceptionTypes.numCatch;
    }
    hasJsrLabels = false;
    isFallThruLabel = true;
    numVars = methodDesc.getNumVars();
    methodDesc.getMaxStack();
    uint32 numArgs = methodDesc.getNumParams();
    for (uint32 i=0, j=0; i<numArgs; i++,j++) {
        Type *type;
        struct StateInfo::SlotInfo *slot = &stateInfo.stack[j];
        if (actualArgs != NULL) { 
            // inlined version
            Opnd *actual = actualArgs[i];
            type = actual->getType();
            if(Log::isEnabled()) {
                Log::out() << "PARAM " << (int)i << " sig: ";
                methodDesc.getParamType(i)->print(Log::out());
                Log::out() << " actual: ";
                type->print(Log::out()); Log::out() << ::std::endl;
            }
            slot->type = typeManager.toInternalType(type);
            if (Simplifier::isNonNullObject(actual))   StateInfo::setNonNull(slot);
            if (Simplifier::isNonNullParameter(actual))   StateInfo::setNonNull(slot);
            if (Simplifier::isExactType(actual))       StateInfo::setExactType(slot);
        } else {
            type = methodDesc.getParamType(i);
            if (!type) {
                // linkage error will happen at the usage point of this parameter
                // here we just keep it as NullObj
                type = typeManager.getNullObjectType();
            }
            slot->type = typeManager.toInternalType(type);
        }
        slot->vars  = new (memManager) SlotVar(getOrCreateVarInc(0, j, slot->type, NULL));
        JavaVarType javaType = getJavaType(type);
        if (javaType == L || javaType == D) j++;
    }
    stateTable->setStateInfo(&stateInfo, 0, false);
}

void JavaLabelPrepass::offset(uint32 offset) {
    Log::out() << std::endl << "PREPASS OFFSET " << (int32)offset << ", blockNo=" << blockNumber << std::endl;
    bytecodevisited->setBit(offset,true);
    if (offset==0)
        stateTable->restoreStateInfo(&stateInfo, offset);
    if (labels->getBit(offset) == true/* && !visited->getBit(offset)*/) {
        if (linearPassDone)
            stateTable->restoreStateInfo(&stateInfo, offset);
        setStackVars();
        if (!linearPassDone) {
            Log::out() << "LINEAR " << std::endl;
            propagateStateInfo(offset,isFallThruLabel);
            isFallThruLabel = true;
        }
        Log::out() << "BASICBLOCK " << (int32)offset << " " << blockNumber << std::endl;
        ++blockNumber;
        visited->setBit(offset,true);
        stateTable->restoreStateInfo(&stateInfo,offset);
        if (stateInfo.isCatchLabel()) {
            Type *handlerExceptionType = NULL;
            for (ExceptionInfo* exceptionInfo = stateInfo.exceptionInfo;
                 exceptionInfo != NULL;
                 exceptionInfo = exceptionInfo->getNextExceptionInfoAtOffset()) {
                if (exceptionInfo->isCatchHandler()) {
                    // catch handler block
                    CatchHandler* handler = (CatchHandler*)exceptionInfo;
                    handlerExceptionType = handler->getExceptionType();
                    break;
                }
            }
            if(Log::isEnabled()) { 
                Log::out() << "CATCH " << (int32) offset << " "; 
                handlerExceptionType->print(Log::out()); 
                Log::out() << ::std::endl;
            }
            pushType(handlerExceptionType);
        }
        else if (stateInfo.isSubroutineEntry()) {
            pushType(typeManager.getSystemObjectType());
            stateInfo.stack[stateInfo.stackDepth-1].jsrLabelOffset = offset;
        }
    }
}



void JavaLabelPrepass::setLabel(uint32 offset) {
    if (labels->getBit(offset)) // this label is already seen
        return;
    Log::out() << "SET LABEL " << (int) offset << " " << (int) numLabels << ::std::endl;
    labels->setBit(offset,true);
    numLabels++;
}

struct LabelOffsetVisitor : public BitSet::Visitor {
    LabelOffsetVisitor(uint32* l) : labelOffset(l) {}
    void visit(uint32 elem) {*labelOffset++ = elem;}
    uint32* labelOffset;
};


// called to indicate end of parsing
void JavaLabelPrepass::parseDone() {
    labelOffsets = new (memManager) uint32[numLabels];
    struct LabelOffsetVisitor avisitor(labelOffsets);
    labels->visitElems(avisitor);
}

uint32 JavaLabelPrepass::getLabelId(uint32 offset) {
    if (numLabels == 0)
        return (uint32) -1;
    uint32 lo = 0;
    uint32 hi = numLabels-1;
    if (offset > labelOffsets[hi] || offset < labelOffsets[lo])
        // not in this set
        return (uint32) -1;
    while (hi-lo > 4) {
        uint32 mid = lo + ((hi-lo) >> 1);
        if (offset < labelOffsets[mid]) 
            hi = mid;
        else
            lo = mid;
    }
    // hi-lo <= 4
    while (lo <= hi) {
        if (labelOffsets[lo] == offset)
            return lo;
        lo++;
    }
    return (uint32) -1;
}

VariableIncarnation* JavaLabelPrepass::getVarInc(uint32 offset, uint32 index)
{
    int numStack = methodDesc.getMaxStack()+1;
    uint32 key = offset*(numVars+numStack)+index;
    StlHashMap<uint32,VariableIncarnation*>::iterator iter = localVars.find(key);
    if (iter==localVars.end()) return NULL;
    return (*iter).second;
}

VariableIncarnation* JavaLabelPrepass::getOrCreateVarInc(uint32 offset, uint32 index, Type* type, VariableIncarnation* prev)
{
    int numStack = methodDesc.getMaxStack()+1;
    uint32 key = offset*(numVars+numStack)+index;
    StlHashMap<uint32,VariableIncarnation*>::iterator iter = localVars.find(key);
    VariableIncarnation* var;
    if (iter==localVars.end()) {
        var = new(memManager) VariableIncarnation(offset, blockNumber, type);
        localVars[key] = var;
    } else {
        var = (*iter).second;
    }
    return var;
}

void JavaLabelPrepass::createMultipleDefVarOpnds(IRBuilder* irBuilder)
{
    StlHashMap<uint32,VariableIncarnation*>::iterator iter;
    for(iter = localVars.begin(); iter!=localVars.end(); ++iter) {
        VariableIncarnation* var = (*iter).second;
        var->createMultipleDefVarOpnd(irBuilder);
    }
}

//
// stack operations
//

struct StateInfo::SlotInfo   JavaLabelPrepass::topType() {
    return stateInfo.stack[stateInfo.stackDepth-1];
}

struct StateInfo::SlotInfo JavaLabelPrepass::popType() {
    struct StateInfo::SlotInfo top = stateInfo.stack[--stateInfo.stackDepth];
    assert (stateInfo.stackDepth >= (int)numVars);
    return top;
}

void    JavaLabelPrepass::popAndCheck(Type *type) {
    struct StateInfo::SlotInfo top = popType();
    if( !(top.type == type) )
        assert(0);
}

void    JavaLabelPrepass::popAndCheck(JavaVarType type) {
    struct StateInfo::SlotInfo top = popType();
    if(!(top.type && getJavaType(top.type) == type))
        assert(0);
}

void    JavaLabelPrepass::pushType(Type *type) {
    struct StateInfo::SlotInfo* slot = &stateInfo.stack[stateInfo.stackDepth++];
    slot->type = type;
    slot->slotFlags = 0;
    slot->vars = NULL;
    slot->jsrLabelOffset = 0;
}


void    JavaLabelPrepass::pushType(Type *type, uint32 varNumber) {
    struct StateInfo::SlotInfo* slot = &stateInfo.stack[stateInfo.stackDepth++];
    slot->type           = type;
    slot->slotFlags      = 0;
    slot->varNumber      = varNumber;
    slot->vars            = NULL;
    slot->jsrLabelOffset  = 0;
    StateInfo::setVarNumber(slot);
}


void    JavaLabelPrepass::pushType(struct StateInfo::SlotInfo slot) {
    stateInfo.stack[stateInfo.stackDepth++] = slot;
    stateInfo.stack[stateInfo.stackDepth-1].jsrLabelOffset = 0;
}


void JavaLabelPrepass::setStackVars() {
    if(Log::isEnabled()) {
        Log::out() << "SET STACK VARS:" << ::std::endl;
    }

    for (int i=numVars; i < stateInfo.stackDepth; i++) {
        struct StateInfo::SlotInfo* slot = &stateInfo.stack[i];

        if(Log::isEnabled()) {
            Log::out() << "SLOT " << i << ":" << ::std::endl;
            Log::out() << "       type = ";
            if (slot->type)
                slot->type->print(Log::out());
            else
                Log::out() << "NULL";
            Log::out() << ::std::endl;
            Log::out() << "       vars = ";
            if (slot->vars)
                slot->vars->getVarIncarnation()->print(Log::out());
            else
                Log::out() << "NULL";
            Log::out() << ::std::endl;
        }

        Type* type = slot->type;
        assert(type);
        SlotVar* sv = slot->vars;
        VariableIncarnation* var = getOrCreateVarInc(currentOffset, i, type, NULL);

        // Do not merge stack vars of incompatible types
        if (sv && (sv->getVarIncarnation()->getDeclaredType() == var->getDeclaredType())) {
            VariableIncarnation::linkAndMergeIncarnations(sv->getVarIncarnation(),var,&typeManager);
            var->setMultipleDefs();
        } else {
            slot->vars = new (memManager) SlotVar(var);
        }

        if(Log::isEnabled()) {
            Log::out() << "AFTER" << ::std::endl;
            Log::out() << "       type = ";
            if (slot->type)
                slot->type->print(Log::out());
            else
                Log::out() << "NULL";
            Log::out() << ::std::endl;
            Log::out() << "       vars = ";
            if (slot->vars)
                slot->vars->getVarIncarnation()->print(Log::out());
            else
                Log::out() << "NULL";
            Log::out() << ::std::endl;
        }
    }
    if(Log::isEnabled()) {
        Log::out() << "SET STACK VARS DONE." << ::std::endl;
    }
}


void JavaLabelPrepass::parseError() {
}


//
// branches
//

void JavaLabelPrepass::checkTargetForRestart(uint32 target) {
    // If the target of a branch has been visited, but has no state info, then we
    // will not merge information such as variable incarnations from that first visit
    // with the information from the current branch.
    // Here we try to catch this and then force a revisit to merge the old information in
    if (bytecodevisited->getBit(target) && !stateTable->getStateInfo(target)) {
        // For now let's begin again from the start and hope it propagates to the target
        pushRestart(0);
        getVisited()->clear();

    }
}

void JavaLabelPrepass::propagateStateInfo(uint32 offset, bool isFallThru) {
    setLabel(offset);
    stateTable->setStateInfo(&stateInfo,offset,isFallThru);
}


void JavaLabelPrepass::ifeq(uint32 targetOffset,uint32 nextOffset) {
    popAndCheck(int32Type);
    setStackVars();
    checkTargetForRestart(targetOffset);
    // propagate state info on both ways
    propagateStateInfo(targetOffset,false);
    setLabel(nextOffset);
    isFallThruLabel = targetOffset > nextOffset;
}
void JavaLabelPrepass::ifne(uint32 targetOffset,uint32 nextOffset) {
    popAndCheck(int32Type);
    setStackVars();
    checkTargetForRestart(targetOffset);
    // propagate state info on both ways
    propagateStateInfo(targetOffset,false);
    setLabel(nextOffset);
    isFallThruLabel = targetOffset > nextOffset;
}
void JavaLabelPrepass::iflt(uint32 targetOffset,uint32 nextOffset) {
    popAndCheck(int32Type);
    setStackVars();
    checkTargetForRestart(targetOffset);
    // propagate state info on both ways
    propagateStateInfo(targetOffset,false);
    setLabel(nextOffset);
    isFallThruLabel = targetOffset > nextOffset;
}
void JavaLabelPrepass::ifge(uint32 targetOffset,uint32 nextOffset) {
    popAndCheck(int32Type);
    setStackVars();
    checkTargetForRestart(targetOffset);
    // propagate state info on both ways
    propagateStateInfo(targetOffset,false);
    setLabel(nextOffset);
    isFallThruLabel = targetOffset > nextOffset;
}
void JavaLabelPrepass::ifgt(uint32 targetOffset,uint32 nextOffset) {
    popAndCheck(int32Type);
    setStackVars();
    checkTargetForRestart(targetOffset);
    // propagate state info on both ways
    propagateStateInfo(targetOffset,false);
    setLabel(nextOffset);
    isFallThruLabel = targetOffset > nextOffset;
}
void JavaLabelPrepass::ifle(uint32 targetOffset,uint32 nextOffset) {
    popAndCheck(int32Type);
    setStackVars();
    checkTargetForRestart(targetOffset);
    // propagate state info on both ways
    propagateStateInfo(targetOffset,false);
    setLabel(nextOffset);
    isFallThruLabel = targetOffset > nextOffset;
}
void JavaLabelPrepass::if_icmpeq(uint32 targetOffset,uint32 nextOffset)    {
    popAndCheck(int32Type); 
    popAndCheck(int32Type);
    setStackVars();
    checkTargetForRestart(targetOffset);
    // propagate state info on both ways
    propagateStateInfo(targetOffset,false);
    setLabel(nextOffset);
    isFallThruLabel = targetOffset > nextOffset;
}
void JavaLabelPrepass::if_icmpne(uint32 targetOffset,uint32 nextOffset)    {
    popAndCheck(int32Type); 
    popAndCheck(int32Type);
    setStackVars();
    checkTargetForRestart(targetOffset);
    // propagate state info on both ways
    propagateStateInfo(targetOffset,false);
    setLabel(nextOffset);
    isFallThruLabel = targetOffset > nextOffset;
}
void JavaLabelPrepass::if_icmplt(uint32 targetOffset,uint32 nextOffset)    {
    popAndCheck(int32Type); 
    popAndCheck(int32Type);
    setStackVars();
    checkTargetForRestart(targetOffset);
    // propagate state info on both ways
    propagateStateInfo(targetOffset,false);
    setLabel(nextOffset);
    isFallThruLabel = targetOffset > nextOffset;
}
void JavaLabelPrepass::if_icmpge(uint32 targetOffset,uint32 nextOffset)    {
    popAndCheck(int32Type); 
    popAndCheck(int32Type);
    setStackVars();
    checkTargetForRestart(targetOffset);
    // propagate state info on both ways
    propagateStateInfo(targetOffset,false);
    setLabel(nextOffset);
    isFallThruLabel = targetOffset > nextOffset;
}
void JavaLabelPrepass::if_icmpgt(uint32 targetOffset,uint32 nextOffset)    {
    popAndCheck(int32Type); 
    popAndCheck(int32Type);
    setStackVars();
    checkTargetForRestart(targetOffset);
    // propagate state info on both ways
    propagateStateInfo(targetOffset,false);
    setLabel(nextOffset);
    isFallThruLabel = targetOffset > nextOffset;
}
void JavaLabelPrepass::if_icmple(uint32 targetOffset,uint32 nextOffset)    {
    popAndCheck(int32Type); 
    popAndCheck(int32Type);
    setStackVars();
    checkTargetForRestart(targetOffset);
    // propagate state info on both ways
    propagateStateInfo(targetOffset,false);
    setLabel(nextOffset);
    isFallThruLabel = targetOffset > nextOffset;
}
void JavaLabelPrepass::if_acmpeq(uint32 targetOffset,uint32 nextOffset)    {
    popAndCheck(A);
    popAndCheck(A);
    setStackVars();
    checkTargetForRestart(targetOffset);
    // propagate state info on both ways
    propagateStateInfo(targetOffset,false);
    setLabel(nextOffset);
    isFallThruLabel = targetOffset > nextOffset;
}
void JavaLabelPrepass::if_acmpne(uint32 targetOffset,uint32 nextOffset)    {
    popAndCheck(A);
    popAndCheck(A);
    setStackVars();
    checkTargetForRestart(targetOffset);
    // propagate state info on both ways
    propagateStateInfo(targetOffset,false);
    setLabel(nextOffset);
    isFallThruLabel = targetOffset > nextOffset;
}
void JavaLabelPrepass::ifnull(uint32 targetOffset,uint32 nextOffset)    {
    popAndCheck(A);
    setStackVars();
    checkTargetForRestart(targetOffset);
    // propagate state info on both ways
    propagateStateInfo(targetOffset,false);
    setLabel(nextOffset);
    isFallThruLabel = targetOffset > nextOffset;
}
void JavaLabelPrepass::ifnonnull(uint32 targetOffset,uint32 nextOffset)    {
    popAndCheck(A);
    setStackVars();
    checkTargetForRestart(targetOffset);
    // propagate state info on both ways
    propagateStateInfo(targetOffset,false);
    setLabel(nextOffset);
    isFallThruLabel = targetOffset > nextOffset;
}
void JavaLabelPrepass::goto_(uint32 targetOffset,uint32 nextOffset)        {
    setStackVars();
    checkTargetForRestart(targetOffset);
    propagateStateInfo(targetOffset,false);
}

//
// returns
//
void JavaLabelPrepass::ireturn(uint32 off) {popAndCheck(int32Type); }
void JavaLabelPrepass::lreturn(uint32 off) {popAndCheck(int64Type); }
void JavaLabelPrepass::freturn(uint32 off) {popAndCheck(singleType);}
void JavaLabelPrepass::dreturn(uint32 off) {popAndCheck(doubleType);}
void JavaLabelPrepass::areturn(uint32 off) {popAndCheck(A);         }
void JavaLabelPrepass::return_(uint32 off) {                        }

//
// jsr/ret
//
void JavaLabelPrepass::jsr(uint32 targetOffset, uint32 nextOffset) {
    stateTable->createStateInfo(targetOffset)->setSubroutineEntry();
    setSubroutineEntry(targetOffset);
    hasJsrLabels = true;
    setStackVars();
    // propagate state info on both ways
    propagateStateInfo(targetOffset,false);
    propagateStateInfo(nextOffset,false);

    // store information for filling slot->jsrNextOffset
    // (in case the slot is a returnAddress)
    //
    jsrEntriesMap.insert(std::make_pair(targetOffset,nextOffset));

    getVisited()->setBit(targetOffset,false);
}
void JavaLabelPrepass::ret(uint16 varIndex) { 
    StateInfo::SlotInfo *slot = &stateInfo.stack[varIndex];
    VariableIncarnation* var = slot->vars->getVarIncarnation();
    assert(var);
    var->setMultipleDefs();
    setStackVars();
    uint32 subEntryOffset = slot->jsrLabelOffset;
    stateInfo.cleanFinallyInfo(subEntryOffset);
    assert(retToSubEntryMap.find(currentOffset) == retToSubEntryMap.end() ||
        retToSubEntryMap[currentOffset] == subEntryOffset);
    retToSubEntryMap[currentOffset] = subEntryOffset;
    JsrEntriesMapCIterRange sub_entry_range = jsrEntriesMap.equal_range(subEntryOffset);
    JsrEntryToJsrNextMap::const_iterator iter;
    for (iter = sub_entry_range.first; iter != sub_entry_range.second; iter++) {
        assert((*iter).first == subEntryOffset);
        uint32 jsrTargetOffset = (*iter).second;

        // according to JVM Spec:
        //      When executing the ret instruction, which implements a
        //      return from a subroutine, there must be only one possible
        //      subroutine from which the instruction can be returning. Two
        //      different subroutines cannot "merge" their execution to a
        //      single ret instruction.
        //
        // propagating new objects created in finally section 
        //      to the instruction that follows the JSR
        //
        stateTable->setStateInfoFromFinally(&stateInfo, jsrTargetOffset);
    }
}

//
// switches
//
void JavaLabelPrepass::tableswitch(JavaSwitchTargetsIter* iter) {
    popAndCheck(int32Type);
    setStackVars();
    while (iter->hasNext()) 
        propagateStateInfo(iter->getNextTarget(),false);
    propagateStateInfo(iter->getDefaultTarget(),false);
}

void JavaLabelPrepass::lookupswitch(JavaLookupSwitchTargetsIter* iter) {
    popAndCheck(int32Type);
    setStackVars();
    uint32 dummy;
    while (iter->hasNext()) 
        propagateStateInfo(iter->getNextTarget(&dummy),false);
    propagateStateInfo(iter->getDefaultTarget(),false);
}


//
// remaining instructions
//

void JavaLabelPrepass::nop()                               {}
void JavaLabelPrepass::aconst_null()                       { pushType(typeManager.getNullObjectType()); }
void JavaLabelPrepass::iconst(int32 val)                   { pushType(int32Type); }
void JavaLabelPrepass::lconst(int64 val)                   { pushType(int64Type); }
void JavaLabelPrepass::fconst(float val)                   { pushType(singleType); }
void JavaLabelPrepass::dconst(double val)                  { pushType(doubleType); }
void JavaLabelPrepass::bipush(int8 val)                    { pushType(int32Type); }
void JavaLabelPrepass::sipush(int16 val)                   { pushType(int32Type); }

void JavaLabelPrepass::iload(uint16 varIndex)              { genLoad(int32Type,varIndex); }
void JavaLabelPrepass::lload(uint16 varIndex)              { genLoad(int64Type,varIndex); }
void JavaLabelPrepass::fload(uint16 varIndex)              { genLoad(singleType,varIndex); }
void JavaLabelPrepass::dload(uint16 varIndex)              { genLoad(doubleType,varIndex); }
void JavaLabelPrepass::aload(uint16 varIndex)              { genTypeLoad(varIndex); }

void JavaLabelPrepass::istore(uint16 varIndex,uint32 off)  { genStore(int32Type,varIndex,off); }
void JavaLabelPrepass::lstore(uint16 varIndex,uint32 off)  { genStore(int64Type,varIndex,off); }
void JavaLabelPrepass::fstore(uint16 varIndex,uint32 off)  { genStore(singleType,varIndex,off); }
void JavaLabelPrepass::dstore(uint16 varIndex,uint32 off)  { genStore(doubleType,varIndex,off); }
void JavaLabelPrepass::astore(uint16 varIndex,uint32 off)  { genTypeStore(varIndex,off); }

void JavaLabelPrepass::iaload()                            { genArrayLoad(int32Type); }
void JavaLabelPrepass::laload()                            { genArrayLoad(int64Type); }
void JavaLabelPrepass::faload()                            { genArrayLoad(singleType); }
void JavaLabelPrepass::daload()                            { genArrayLoad(doubleType); }
void JavaLabelPrepass::aaload()                            { genTypeArrayLoad(); }
void JavaLabelPrepass::baload()                            { genArrayLoad(int32Type); }
void JavaLabelPrepass::caload()                            { genArrayLoad(int32Type); }
void JavaLabelPrepass::saload()                            { genArrayLoad(int32Type); }

void JavaLabelPrepass::iastore()                           { genArrayStore(int32Type);}
void JavaLabelPrepass::lastore()                           { genArrayStore(int64Type);}
void JavaLabelPrepass::fastore()                           { genArrayStore(singleType);}
void JavaLabelPrepass::dastore()                           { genArrayStore(doubleType);}
void JavaLabelPrepass::aastore()                           { genTypeArrayStore();}
void JavaLabelPrepass::bastore()                           { genArrayStore(int32Type);}
void JavaLabelPrepass::castore()                           { genArrayStore(int32Type);}
void JavaLabelPrepass::sastore()                           { genArrayStore(int32Type);}

void JavaLabelPrepass::iadd()                              { genBinary(int32Type); }
void JavaLabelPrepass::ladd()                              { genBinary(int64Type); }
void JavaLabelPrepass::fadd()                              { genBinary(singleType); }
void JavaLabelPrepass::dadd()                              { genBinary(doubleType); }
void JavaLabelPrepass::isub()                              { genBinary(int32Type); }
void JavaLabelPrepass::lsub()                              { genBinary(int64Type); }
void JavaLabelPrepass::fsub()                              { genBinary(singleType); }
void JavaLabelPrepass::dsub()                              { genBinary(doubleType); }
void JavaLabelPrepass::imul()                              { genBinary(int32Type); }
void JavaLabelPrepass::lmul()                              { genBinary(int64Type); }
void JavaLabelPrepass::fmul()                              { genBinary(singleType); }
void JavaLabelPrepass::dmul()                              { genBinary(doubleType); }
void JavaLabelPrepass::idiv()                              { genBinary(int32Type); }
void JavaLabelPrepass::ldiv()                              { genBinary(int64Type); }
void JavaLabelPrepass::fdiv()                              { genBinary(singleType); }
void JavaLabelPrepass::ddiv()                              { genBinary(doubleType); }
void JavaLabelPrepass::irem()                              { genBinary(int32Type); }
void JavaLabelPrepass::lrem()                              { genBinary(int64Type); }
void JavaLabelPrepass::frem()                              { genBinary(singleType); }
void JavaLabelPrepass::drem()                              { genBinary(doubleType); }
void JavaLabelPrepass::ineg()                              { genUnary (int32Type); }
void JavaLabelPrepass::lneg()                              { genUnary (int64Type); }
void JavaLabelPrepass::fneg()                              { genUnary (singleType); }
void JavaLabelPrepass::dneg()                              { genUnary (doubleType); }
void JavaLabelPrepass::ishl()                              { genShift (int32Type); }
void JavaLabelPrepass::lshl()                              { genShift (int64Type); }
void JavaLabelPrepass::ishr()                              { genShift (int32Type); }
void JavaLabelPrepass::lshr()                              { genShift (int64Type); }
void JavaLabelPrepass::iushr()                             { genShift (int32Type); }
void JavaLabelPrepass::lushr()                             { genShift (int64Type); }
void JavaLabelPrepass::iand()                              { genBinary(int32Type); }
void JavaLabelPrepass::land()                              { genBinary(int64Type); }
void JavaLabelPrepass::ior()                               { genBinary(int32Type); }
void JavaLabelPrepass::lor()                               { genBinary(int64Type); }
void JavaLabelPrepass::ixor()                              { genBinary(int32Type); }
void JavaLabelPrepass::lxor()                              { genBinary(int64Type); }
void JavaLabelPrepass::i2l()                               { genConv(int32Type,int64Type); }
void JavaLabelPrepass::i2f()                               { genConv(int32Type,singleType); }
void JavaLabelPrepass::i2d()                               { genConv(int32Type,doubleType); }
void JavaLabelPrepass::l2i()                               { genConv(int64Type,int32Type); }
void JavaLabelPrepass::l2f()                               { genConv(int64Type,singleType); }
void JavaLabelPrepass::l2d()                               { genConv(int64Type,doubleType); }
void JavaLabelPrepass::f2i()                               { genConv(singleType,int32Type); }
void JavaLabelPrepass::f2l()                               { genConv(singleType,int64Type); }
void JavaLabelPrepass::f2d()                               { genConv(singleType,doubleType); }
void JavaLabelPrepass::d2i()                               { genConv(doubleType,int32Type); }
void JavaLabelPrepass::d2l()                               { genConv(doubleType,int64Type); }
void JavaLabelPrepass::d2f()                               { genConv(doubleType,singleType); }
void JavaLabelPrepass::i2b()                               { genConv(int32Type,int32Type); }
void JavaLabelPrepass::i2c()                               { genConv(int32Type,int32Type); }
void JavaLabelPrepass::i2s()                               { genConv(int32Type,int32Type); }
void JavaLabelPrepass::lcmp()                              { genCompare(int64Type);}
void JavaLabelPrepass::fcmpl()                             { genCompare(singleType);}
void JavaLabelPrepass::fcmpg()                             { genCompare(singleType);}
void JavaLabelPrepass::dcmpl()                             { genCompare(doubleType);}
void JavaLabelPrepass::dcmpg()                             { genCompare(doubleType);}

void JavaLabelPrepass::new_(uint32 constPoolIndex)         { 
    StateInfo::SlotInfo slot;
    StateInfo::setNonNull(&slot);
    StateInfo::setExactType(&slot);
    
    Type* nType = compilationInterface.getNamedType(methodDesc.getParentHandle(), constPoolIndex, ResolveNewCheck_DoCheck);
    
    if (nType) {
        slot.type = nType;
    } else {
        assert(!typeManager.isLazyResolutionMode());
        slot.type = typeManager.getNullObjectType();
    }
    slot.vars = NULL;
    jitrino_assert( slot.type);
    pushType(slot);
}

void JavaLabelPrepass::newarray(uint8 etype)                { 
    popAndCheck(int32Type);
    NamedType *elemType = NULL;
    switch (etype) {
    case 4:  elemType = typeManager.getBooleanType(); break;
    case 5:  elemType = typeManager.getCharType();    break;
    case 6:  elemType = typeManager.getSingleType();  break;
    case 7:  elemType = typeManager.getDoubleType();  break;
    case 8:  elemType = typeManager.getInt8Type();    break;
    case 9:  elemType = typeManager.getInt16Type();   break;
    case 10: elemType = typeManager.getInt32Type();   break;
    case 11: elemType = typeManager.getInt64Type();   break;
    default: jitrino_assert( 0);
    }
    StateInfo::SlotInfo slot;
    StateInfo::setNonNull(&slot);
    StateInfo::setExactType(&slot);
    slot.type = typeManager.getArrayType(elemType);
    slot.vars = NULL;
    pushType(slot);
}


void JavaLabelPrepass::anewarray(uint32 constPoolIndex)    { 
    popAndCheck(int32Type); 
    StateInfo::SlotInfo slot;
    StateInfo::setNonNull(&slot);
    StateInfo::setExactType(&slot);

    Type* type = compilationInterface.getNamedType(methodDesc.getParentHandle(), constPoolIndex);
    
    if (type) {
        slot.type = typeManager.getArrayType(type);
    } else {
        assert(!typeManager.isLazyResolutionMode());
        slot.type = typeManager.getNullObjectType();
    }
    slot.vars = NULL;
    jitrino_assert( slot.type);
    pushType(slot);
}

void JavaLabelPrepass::arraylength() {
    popAndCheck(A); 
    pushType(int32Type); 
}

void JavaLabelPrepass::athrow() { 
    popAndCheck(A); 
}

void JavaLabelPrepass::checkcast(uint32 constPoolIndex)    { 
    StateInfo::SlotInfo slot = stateInfo.stack[stateInfo.stackDepth - 1];
    if ( (slot.type) &&
         (slot.type->tag == Type::NullObject ) &&
         (slot.vars == NULL) ) {
        return;
    }
    Type* type = compilationInterface.getNamedType(methodDesc.getParentHandle(), constPoolIndex);
    if (!type) {
        assert(!typeManager.isLazyResolutionMode());
        // leave stack as is as in case of success because
        // resolution of item by constPoolIndex fails and
        // respective exception will be thrown
        return;
    }
    popAndCheck(A);
    pushType(type);
}

int JavaLabelPrepass::instanceof(const uint8* bcp, uint32 constPoolIndex, uint32 off)   {
    popType();
    pushType(int32Type);
    return 3;  // length of instanceof
}
void JavaLabelPrepass::monitorenter()                      { popAndCheck(A); }
void JavaLabelPrepass::monitorexit()                       { popAndCheck(A); }

void JavaLabelPrepass::iinc(uint16 varIndex,int32 amount)  { 
    stateInfo.stack[varIndex].vars->getVarIncarnation()->setMultipleDefs();
}

void JavaLabelPrepass::ldc(uint32 constPoolIndex)          {
    // load 32-bit quantity or string from constant pool
    Type* constantType =
                compilationInterface.getConstantType(&methodDesc,constPoolIndex);
    if (constantType->isSystemString() || constantType->isSystemClass()) {
        pushType(constantType);
    } else if (constantType->isInt4()) {
        pushType(int32Type);
    } else if (constantType->isSingle()) {
        pushType(singleType);
    } else {
        jitrino_assert( 0);
    }
}
void JavaLabelPrepass::ldc2(uint32 constPoolIndex)         {
    // load 64-bit quantity from constant pool
    Type* constantType =
                compilationInterface.getConstantType(&methodDesc,constPoolIndex);
    if (constantType->isInt8()) {
        pushType(int64Type);
    } else if (constantType->isDouble()) {
        pushType(doubleType);
    } else {
        jitrino_assert( 0);
    }
}

void JavaLabelPrepass::getstatic(uint32 constPoolIndex)    {
    FieldDesc *fdesc = compilationInterface.getStaticField(methodDesc.getParentHandle(), constPoolIndex, false);
    Type* fieldType = 0;
    if (fdesc && fdesc->isStatic()) {
        fieldType = fdesc->getFieldType();
    }
    if (!fieldType){
        fieldType = compilationInterface.getFieldType(methodDesc.getParentHandle(), constPoolIndex);
     }
    assert(fieldType);
    pushType(typeManager.toInternalType(fieldType));
}

void JavaLabelPrepass::putstatic(uint32 constPoolIndex)    {
    FieldDesc *fdesc = compilationInterface.getStaticField(methodDesc.getParentHandle(), constPoolIndex, true);
    Type* fieldType = fdesc ? fdesc->getFieldType() : NULL;
    if (fieldType){
        popAndCheck(getJavaType(fieldType));
    } else {
        // lazy resolution mode or
        // throwing respective exception helper will be inserted at the Translator
       popType();
    } 
}

void JavaLabelPrepass::getfield(uint32 constPoolIndex)     {
    popAndCheck(A);//obj
    FieldDesc *fdesc = compilationInterface.getNonStaticField(methodDesc.getParentHandle(), constPoolIndex, false);
    Type* fieldType = NULL;
    if (fdesc) {
        fieldType = fdesc->getFieldType();
    }
    if (!fieldType){
        fieldType = compilationInterface.getFieldType(methodDesc.getParentHandle(), constPoolIndex);
    }
    assert(fieldType);
    pushType(typeManager.toInternalType(fieldType));
}

void JavaLabelPrepass::putfield(uint32 constPoolIndex)     {
    FieldDesc *fdesc = compilationInterface.getNonStaticField(methodDesc.getParentHandle(), constPoolIndex, true);
    Type* fieldType = fdesc ? fdesc->getFieldType() : NULL;
    if (fieldType){
        popAndCheck(getJavaType(fieldType));
    } else {
        // throwing respective exception helper will be inserted at the Translator
        // TODO: why not check types for lazy-resolve mode?
        popType();
    }
    popAndCheck(A);
}

void JavaLabelPrepass::invokevirtual(uint32 constPoolIndex){
    MethodDesc *mdesc = compilationInterface.getVirtualMethod(methodDesc.getParentHandle(), constPoolIndex);
    if (mdesc) {// resolution was successful
        invoke(mdesc);
    } else {    // exception happens during resolving/linking
        const char* methodSig_string = methodSignatureString(constPoolIndex);
        popType(); // is not static
        pseudoInvoke(methodSig_string);
    }
}

void JavaLabelPrepass::invokespecial(uint32 constPoolIndex){
    MethodDesc* mdesc = compilationInterface.getSpecialMethod(methodDesc.getParentHandle(),constPoolIndex);
    if (mdesc) {// resolution was successful
        invoke(mdesc);
    } else {    
        // exception happens during resolving/linking or lazy resolution mode
        const char* methodSig_string = methodSignatureString(constPoolIndex);
        popType(); // is not static
        pseudoInvoke(methodSig_string);
    }
}
void JavaLabelPrepass::invokestatic(uint32 constPoolIndex) {
    MethodDesc *mdesc = compilationInterface.getStaticMethod(methodDesc.getParentHandle(), constPoolIndex);
    if (mdesc) {// resolution was successful
        invoke(mdesc);
    } else {    // exception happens during resolving/linking
        const char* methodSig_string = methodSignatureString(constPoolIndex);
        pseudoInvoke(methodSig_string);
    }
}
void JavaLabelPrepass::invokeinterface(uint32 constPoolIndex,uint32 count) {
    MethodDesc *mdesc = compilationInterface.getInterfaceMethod(methodDesc.getParentHandle(), constPoolIndex);
    if (mdesc) {// resolution was successful
        invoke(mdesc);
    } else {    // exception happens during resolving/linking
        const char* methodSig_string = methodSignatureString(constPoolIndex);
        popType(); // is not static
        pseudoInvoke(methodSig_string);
    }
}
void JavaLabelPrepass::multianewarray(uint32 constPoolIndex,uint8 dimensions) {
    for (int i =0; i < dimensions; i++) {
        popAndCheck(int32Type);
    }
    Type *type = compilationInterface.getNamedType(methodDesc.getParentHandle(), constPoolIndex);
    if ( !type ) {
        assert(!typeManager.isLazyResolutionMode());
        type = typeManager.getNullObjectType();
    }
    jitrino_assert( type);
    pushType(type);
}

void JavaLabelPrepass::pseudoInvoke(const char* methodSig) {

    assert(methodSig);
    uint32 numArgs = getNumArgsBySignature(methodSig); 

    // pop numArgs items
    for (int i=numArgs-1; i>=0; i--)
        popType();

    // recognize and push respective returnType
    Type* retType = getRetTypeBySignature(compilationInterface, methodDesc.getParentHandle(), methodSig);
    assert(retType);

    // push the return type
    if (retType->tag != Type::Void) {
        pushType(typeManager.toInternalType(retType));
    }
}

uint32 JavaLabelPrepass::getNumArgsBySignature(const char*& methodSig) 
{
    assert(methodSig);
    assert(*methodSig == '(');
    uint32 numArgs = 0; 

    // start just after '(' and go until ')' counting 'numArgs' 
    for(++methodSig ;*methodSig != ')'; methodSig++) {
        switch( *methodSig ) 
        {
        case 'L':
            // skip class name
            while( *(++methodSig) != ';' ) assert(*methodSig);
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
        default:
            assert(0); // impossible! Verifier must check and catch this
            break;
        }
    }
    // the last observed index should point to ')' after getNumArgsBySignature call
    assert(*methodSig == ')');
    
    return numArgs;
}

Type* JavaLabelPrepass::getRetTypeBySignature(CompilationInterface& ci, Class_Handle enclClass, const char* origSig) 
{
    assert(*origSig== '(' || *origSig == ')');
    while( *(origSig++) != ')' ); // in case getNumArgsBySignature was not run earlier

    Type* retType = NULL;
    uint32 arrayDim = 0;
    const char* methodSig = origSig;


    // collect array dimension if any
    while( *(methodSig) == '[' ) {
        arrayDim++;
        methodSig++;
    }

    bool arrayIsWrapped = false;
    TypeManager& typeManager = ci.getTypeManager();
    switch( *methodSig ) 
    {
    case 'L': {
            if (!typeManager.isLazyResolutionMode()) {
                typeManager.getNullObjectType();
            }
            retType = ci.getTypeFromDescriptor(enclClass, origSig);
            //in lazy resolution mode retType is already valid array type
            arrayIsWrapped = true;
        }
        break;
    case 'B':
        retType = typeManager.getInt8Type();
        break;
    case 'C':
        retType = typeManager.getCharType();
        break;
    case 'D':
        retType = typeManager.getDoubleType();
        break;
    case 'F':
        retType = typeManager.getSingleType();
        break;
    case 'I':
        retType = typeManager.getInt32Type();
        break;
    case 'J':
        retType = typeManager.getInt64Type();
        break;
    case 'S':
        retType = typeManager.getInt16Type();
        break;
    case 'Z':
        retType = typeManager.getBooleanType();
        break;
    case 'V':
        retType = typeManager.getVoidType();
        break; // leave stack as is
    case '[': // all '[' are already skipped
    case '(': // we have already pass it
    case ')': // we have just leave it back
    default: // impossible! Verifier must check and catch this
        assert(0);
        retType = typeManager.getNullObjectType();
        break;
    }
    assert(retType);

    void* arrVMTypeHandle = NULL;
    if(retType == typeManager.getNullObjectType()) {
        assert(!typeManager.isLazyResolutionMode());
        // VM can not operate with an array of NullObjects
        // Let's cheat here
        arrVMTypeHandle = (void*)(POINTER_SIZE_INT)0xdeadbeef;
    }
    if (!arrayIsWrapped && arrayDim > 0) {
        for (;arrayDim > 0; arrayDim--) {
            retType = typeManager.getArrayType(retType, false, arrVMTypeHandle);
        }
    }
    return retType;
}

void JavaLabelPrepass::invoke(MethodDesc* methodDesc) {
    // pop source operands
    for (int i = methodDesc->getNumParams()-1; i>=0; i--)
        popType();
    Type* type = methodDesc->getReturnType();
    // push the return type
    if (type) {
        if ( type->tag != Type::Void ) {
            pushType(typeManager.toInternalType(type));
        }
    } else {
        // type == NULL means that the returnType was not resolved successfully
        // but it can be resolved later inside the callee (with some "magic" custom class loader for example)
        // Or respective exception will be thrown there (in the callee) at the attempt to create (new)
        // an object of unresolved type
        pushType(typeManager.getNullObjectType());
    }
}


void JavaLabelPrepass::pop()                               { popType(); }

void JavaLabelPrepass::pop2() {
    struct StateInfo::SlotInfo type = popType();
    if (isCategory2(type))
        return;
    popType();
}

void JavaLabelPrepass::dup() {
    pushType(topType());
}

void JavaLabelPrepass::dup_x1() {
    struct StateInfo::SlotInfo opnd1 = popType();
    struct StateInfo::SlotInfo opnd2 = popType();
    pushType(opnd1);
    pushType(opnd2);
    pushType(opnd1);
}

void JavaLabelPrepass::dup_x2() {
    struct StateInfo::SlotInfo opnd1 = popType();
    struct StateInfo::SlotInfo opnd2 = popType();
    if (isCategory2(opnd2)) {
        pushType(opnd1);
        pushType(opnd2);
        pushType(opnd1);
        return;
    }
    struct StateInfo::SlotInfo opnd3 = popType();
    pushType(opnd1);
    pushType(opnd3);
    pushType(opnd2);
    pushType(opnd1);
}

void JavaLabelPrepass::dup2() {
    struct StateInfo::SlotInfo opnd1 = popType();
    if (isCategory2(opnd1)) {
        pushType(opnd1);
        pushType(opnd1);
        return;
    }
    struct StateInfo::SlotInfo opnd2 = popType();
    pushType(opnd2);
    pushType(opnd1);
    pushType(opnd2);
    pushType(opnd1);
}

void JavaLabelPrepass::dup2_x1() {
    struct StateInfo::SlotInfo opnd1 = popType();
    struct StateInfo::SlotInfo opnd2 = popType();
    if (isCategory2(opnd1)) {
        // opnd1 is a category 2 instruction
        pushType(opnd1);
        pushType(opnd2);
        pushType(opnd1);
    } else {
        // opnd1 is a category 1 instruction
        struct StateInfo::SlotInfo opnd3 = popType();
        pushType(opnd2);
        pushType(opnd1);
        pushType(opnd3);
        pushType(opnd2);
        pushType(opnd1);
    }
}


void JavaLabelPrepass::dup2_x2() {
    struct StateInfo::SlotInfo opnd1 = popType();
    struct StateInfo::SlotInfo opnd2 = popType();
    if (isCategory2(opnd1)) {
        // opnd1 is category 2
        if (isCategory2(opnd2)) {
            pushType(opnd1);
            pushType(opnd2);
            pushType(opnd1);
        } else {
            // opnd2 is category 1
            struct StateInfo::SlotInfo opnd3 = popType();
            assert(isCategory2(opnd3) == false);
            pushType(opnd1);
            pushType(opnd3);
            pushType(opnd2);
            pushType(opnd1);
        }
    } else {
        assert(isCategory2(opnd2) == false);
        // both opnd1 & opnd2 are category 1
        struct StateInfo::SlotInfo opnd3 = popType();
        if (isCategory2(opnd3)) {
            pushType(opnd2);
            pushType(opnd1);
            pushType(opnd3);
            pushType(opnd2);
            pushType(opnd1);
        } else {
            // opnd1, opnd2, opnd3 all are category 1
            struct StateInfo::SlotInfo opnd4 = popType();
            assert(isCategory2(opnd4) == false);
            pushType(opnd2);
            pushType(opnd1);
            pushType(opnd4);
            pushType(opnd3);
            pushType(opnd2);
            pushType(opnd1);
        }
    }
}

void JavaLabelPrepass::swap() {
    struct StateInfo::SlotInfo opnd1 = popType();
    struct StateInfo::SlotInfo opnd2 = popType();
    pushType(opnd1);
    pushType(opnd2);
}


///////////////////////////////////////////////////////////////////////////////
// Helper methods
///////////////////////////////////////////////////////////////////////////////

void JavaLabelPrepass::genReturn(Type *type) {
}

void JavaLabelPrepass::genStore(Type *type, uint32 index, uint32 offset) {
    stateInfo.stack[index].jsrLabelOffset = stateInfo.stack[stateInfo.stackDepth-1].jsrLabelOffset;
    popAndCheck(type);
    stateInfo.stack[index].type     = type;
    stateInfo.stack[index].slotFlags= 0;
    stateInfo.stack[index].vars = new (memManager) SlotVar(getOrCreateVarInc(offset, index, type, NULL/*prevVar*/));
    propagateLocalVarToHandlers(index);
}

void JavaLabelPrepass::genTypeStore(uint32 index, uint32 offset) {
    struct StateInfo::SlotInfo slot = popType();
    Type *type = slot.type;
    stateInfo.stack[index].type     = type;
    stateInfo.stack[index].slotFlags= slot.slotFlags;
    VariableIncarnation* offset_varinc = getOrCreateVarInc(offset, index, type, NULL/*prevVar*/);
    offset_varinc->setDeclaredType(typeManager.getCommonType(type, offset_varinc->getDeclaredType()));
    stateInfo.stack[index].vars = new (memManager) SlotVar(offset_varinc);
    if(Log::isEnabled()) {
        Log::out() << "genTypeStore: offset=" << offset 
                   << " index=" << index
                   << " vars: ";
        stateInfo.stack[index].vars->print(Log::out());
        Log::out() << ::std::endl;
    }
    stateInfo.stack[index].jsrLabelOffset = slot.jsrLabelOffset;
    propagateLocalVarToHandlers(index);
}

void JavaLabelPrepass::genLoad(Type *type, uint32 index) {
    assert(stateInfo.stack[index].type == type);
    pushType(type,index);
    stateInfo.stack[stateInfo.stackDepth-1].jsrLabelOffset = stateInfo.stack[index].jsrLabelOffset;
    SlotVar* vars = stateInfo.stack[index].vars;
    if (vars) {
        vars->getVarIncarnation()->ldBlock(blockNumber);
        vars->mergeVarIncarnations(&typeManager);
    }
}

void JavaLabelPrepass::genTypeLoad(uint32 index) {
    Type *type = stateInfo.stack[index].type;
    SlotVar* vars = stateInfo.stack[index].vars;
    if (vars) {
        vars->getVarIncarnation()->ldBlock(blockNumber);
        vars->mergeVarIncarnations(&typeManager);
        type = vars->getVarIncarnation()->getDeclaredType();
    }
    pushType(type,index);
    stateInfo.stack[stateInfo.stackDepth-1].jsrLabelOffset = stateInfo.stack[index].jsrLabelOffset;
}

void JavaLabelPrepass::genArrayLoad (Type *type) {
    popAndCheck(int32Type);
    popAndCheck(A);
    pushType(type);
}


void JavaLabelPrepass::genTypeArrayLoad() {
    popAndCheck(int32Type);
    Type* type = popType().type;
    assert(type->isArrayType() || type->isNullObject() || type->isUnresolvedObject());
    if(type->isArrayType()) {
        type = ((ArrayType*)type)->getElementType();
    }
    pushType(type);
}

void JavaLabelPrepass::genArrayStore(Type *type) {
    popAndCheck(type);
    popAndCheck(int32Type);
    type = popType().type;
    assert(type->isArrayType() || type->isNullObject());
}

void JavaLabelPrepass::genTypeArrayStore() {
#ifndef NDEBUG
    Type *type = 
#endif
        popType().type;
    popAndCheck(int32Type);
#ifndef NDEBUG
    type = 
#endif
        popType().type;
    assert(type->isArrayType() || type->isNullObject() || type->isUnresolvedObject());
}

void JavaLabelPrepass::genBinary    (Type *type) {
    popAndCheck(type);
    popAndCheck(type);
    pushType(type);
}

void JavaLabelPrepass::genUnary     (Type *type) {
    popAndCheck(type);
    pushType(type);
}

void JavaLabelPrepass::genShift     (Type *type) {
    popAndCheck(int32Type);
    popAndCheck(type);
    pushType(type);
}

void JavaLabelPrepass::genConv      (Type *from, Type *to) {
    popAndCheck(from);
    pushType(to);
}

void JavaLabelPrepass::genCompare   (Type *type) {
    popAndCheck(type);
    popAndCheck(type);
    pushType(int32Type);
}



const char*     JavaLabelPrepass::methodSignatureString(uint32 cpIndex) {
    return compilationInterface.getSignatureString(&methodDesc,cpIndex);
}

void StateTable::copySlotInfo(StateInfo::SlotInfo& to, StateInfo::SlotInfo& from) {
    to.jsrLabelOffset = from.jsrLabelOffset;
    to.slotFlags = from.slotFlags;
    to.type = from.type;
    to.varNumber = from.varNumber;
    to.vars = from.vars ? new (memManager) SlotVar(from.vars, memManager) : NULL;
}

void  StateTable::setStateInfo(StateInfo *inState, uint32 offset, bool isFallThru) {
    if(Log::isEnabled()) {
        Log::out() << "SETSTATE offset=" <<(int)offset << " depth=" << inState->stackDepth << ::std::endl;
        printState(inState);
    }

    StateInfo *state = hashtable[offset];
    if (state == NULL) {
        state = new (memManager) StateInfo();
        hashtable[offset] = state;
        if (isFallThru)
            state->setFallThroughLabel();
    } else if (!isFallThru)
        state->clearFallThroughLabel();
    assert(getStateInfo(offset) != NULL);

    if (!state->isVisited() ) {
        state->setVisited();
    
        for ( JavaLabelPrepass::ExceptionTable::const_iterator it = prepass.getExceptionTable().begin(),
              end = prepass.getExceptionTable().end(); it != end; ++it ) {

            CatchBlock* except = *it;
            if ( except->hasOffset(offset) ) {
                Log::out() << "try-region begin=" << (int)except->getBeginOffset() 
                                     << " end=" << (int)except->getEndOffset() << ::std::endl;
                ExceptionInfo *prev = state->exceptionInfo;
                bool found  = false;
                for (ExceptionInfo *exc = state->exceptionInfo; 
                     exc != NULL; 
                     exc = exc->getNextExceptionInfoAtOffset()) {                       

                    //
                    // Although exceptionInfo at this offset is built for the first time,
                    // we can find that this part of list was build for another offset.
                    // So, we should not build CatchBlock-chain any more. Note, this only works
                    // for properly nested CatchBlocks
                    //
                    if ( exc->isCatchBlock() && exc->getId() == except->getId() ) {
                        found = true;
                        break;
                    }
                    prev = exc;
                }
                if (!found) {
                    if (prev == NULL)   {
                        state->exceptionInfo = except;
                    } else {
                        prev->setNextExceptionInfoAtOffset(except);
                    }
                }
            }
        }
    }
    int stackDepth = inState->stackDepth;
    if (stackDepth > 0) {
        if (maxDepth < stackDepth) maxDepth = stackDepth;
        Log::out() << "MAXDEPTH " << maxDepth << ::std::endl;
        struct StateInfo::SlotInfo *stack = state->stack;
        if (stack == NULL) {
            stack = new (memManager) StateInfo::SlotInfo[stackDepth+1];
            state->stack = stack;
            for (int i=0; i < stackDepth; i++) {
                copySlotInfo(stack[i], inState->stack[i]);
            }
            state->stackDepth = stackDepth;
        } else { // needs to merge the states
            assert(state->stackDepth == stackDepth);
            if(Log::isEnabled()) {
                Log::out() << " before\n";
                printState(state);
            }
            for (int i=0; i < stackDepth; i++) {
                struct StateInfo::SlotInfo *inSlot = &inState->stack[i];
                struct StateInfo::SlotInfo *slot   = &stack[i];
                if(Log::isEnabled()) {
                    Log::out() << " i = " << i << ::std::endl;
                    Log::out() << "inSlot->type: ";
                    if (inSlot->type) {
                        inSlot->type->print(Log::out());
                    } else {
                        Log::out() << "null";
                    }
                    Log::out() << ::std::endl;
                    Log::out() << "slot->type: ";
                    if (slot->type) {
                        slot->type->print(Log::out());
                    } else {
                        Log::out() << "null";
                    }
                    Log::out() << ::std::endl;
                    if (inSlot->vars) {
                        Log::out() << "inSlot->vars: "; inSlot->vars->print(Log::out());
                    }
                    if (slot->vars) {
                        Log::out() << "slot->vars: "; slot->vars->print(Log::out());
                    }
                }
                mergeSlots(inSlot, slot, offset, i < numVars);
            }
        }
        if(Log::isEnabled()) {
            Log::out() << " after\n";
            printState(state);
        }
    }
}

void StateTable::mergeSlots(StateInfo::SlotInfo* inSlot, StateInfo::SlotInfo* slot, uint32 offset, bool isVar) {
    slot->jsrLabelOffset = inSlot->jsrLabelOffset;
    slot->slotFlags = slot->slotFlags & inSlot->slotFlags;

    SlotVar* in_vars = inSlot->vars;
    SlotVar* vars = slot->vars;
    Type *in_type = inSlot->type;
    Type *type = slot->type;

    if (!!in_vars != !!vars) {
        slot->type = NULL;
        slot->vars = NULL;
        if (type) prepass.getVisited()->setBit(offset,false);
        return;
    }

    if ((in_type == NULL) || (type == NULL)) {
        assert(in_vars == NULL);
        assert(vars == NULL);
        slot->type = NULL;
        if (type) prepass.getVisited()->setBit(offset,false);
        return;
    }

    Type* new_type = typeManager.getCommonType(in_type,type);
    if (new_type != NULL) {
        if (vars) {
            if(Log::isEnabled()) {
                Log::out() << "addVarIncarnations to SlotVar:" << ::std::endl;
                Log::out() << "   vars: ";
                vars->print(Log::out());
                Log::out() << "in_vars: ";
                in_vars->print(Log::out());
            }
            if (vars->addVarIncarnations(in_vars, memManager, offset)) {
                prepass.getVisited()->setBit(offset,false);
            }
            if (!isVar) {
                 vars->mergeVarIncarnations(&typeManager);
            }
            if(Log::isEnabled()) {
                Log::out() << "result_vars: ";
                vars->print(Log::out());
            }
        }
    } else {
        slot->vars = NULL;
    }
    slot->type = new_type;

    if (type != new_type) {
        prepass.getVisited()->setBit(offset,false);
    }
}


void  StateTable::setStateInfoFromFinally(StateInfo *inState, uint32 offset) {
    if(Log::isEnabled()) {
        Log::out() << "SETSTATE FROM FINALLY offset=" <<(int)offset << " depth=" << inState->stackDepth << ::std::endl;
        printState(inState);
    }
    StateInfo *state = hashtable[offset];
    assert(getStateInfo(offset) != NULL);
    int stackDepth = inState->stackDepth;
    if (stackDepth > 0) {
        if (maxDepth < stackDepth) maxDepth = stackDepth;
        Log::out() << "MAXDEPTH " << maxDepth << ::std::endl;
        struct StateInfo::SlotInfo *stack = state->stack;
        if (stack == NULL) {
            // stack must be propagated from JSR to jsrNext earlier
            assert(0);
        }
        assert(state->stackDepth == stackDepth);
        if(Log::isEnabled()) {
            Log::out() << " before\n";
            printState(state);
        }
        for (int i=0; i < stackDepth; i++) {
            struct StateInfo::SlotInfo *inSlot = &inState->stack[i];
            struct StateInfo::SlotInfo *slot   = &stack[i];
            Type *intype = inSlot->type;
            Type *type  = slot->type;
            Log::out() << "STACK " << i << ": "<< type << ::std::endl;
            if (!type && intype) {  // don't merge, just rewrite!
                slot->type      = intype;
                // Consider copying not pointers but SlotVat structures.
                slot->vars      = inSlot->vars;
                slot->slotFlags = inSlot->slotFlags;
                slot->jsrLabelOffset = inSlot->jsrLabelOffset;
                prepass.getVisited()->setBit(offset,false);
            } else if (!intype) {
                continue;
            } else {
                mergeSlots(inSlot, slot, offset, i < numVars);
            }
        }
        if(Log::isEnabled()) {
            Log::out() << " after\n";
            printState(state);
        }
    }
}

void JavaLabelPrepass::print_loc_vars(uint32 offset, uint32 index)
{
    int numStack = methodDesc.getMaxStack()+1;
    uint32 key = offset*(numVars+numStack)+index;
    StlHashMap<uint32,VariableIncarnation*>::iterator iter = localVars.find(key);
    if (iter==localVars.end()) {
        Log::out() << "localVars[offset=" << offset 
                    << "][index=" << index << "] is empty" << ::std::endl;
    } else {
        Log::out() << "localVars[offset=" << offset << "][index=" << index << "].var->declT = "; 
        (*iter).second->getDeclaredType()->print(::std::cout); 
        Log::out() << ::std::endl;
    }
}

void JavaLabelPrepass::propagateLocalVarToHandlers(uint32 varIndex)
{
    assert(varIndex < numVars);
    struct StateInfo::SlotInfo *inSlot = &stateInfo.stack[varIndex];

    for (ExceptionInfo *except = stateInfo.exceptionInfo; 
        except != NULL; 
        except = except->getNextExceptionInfoAtOffset()) {

            if ( except->isCatchBlock() ) {
                CatchBlock* block = (CatchBlock*)except;
                for (CatchHandler* handler = block->getHandlers(); 
                    handler != NULL; 
                    handler = handler->getNextHandler() ) {

                        uint32 handler_offset = handler->getBeginOffset();
                        struct StateInfo::SlotInfo *slot = &stateTable->getStateInfo(handler_offset)->stack[varIndex];
                        Log::out() << "HANDLER SLOT " << varIndex << " merged to offset " << handler_offset << ::std::endl;
                        stateTable->mergeSlots(inSlot, slot, handler_offset, true);
                    }
            }
        }
}


} //namespace Jitrino 
