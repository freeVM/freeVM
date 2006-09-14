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
 * @author Vyacheslav P. Shakin
 * @version $Revision: 1.24.8.5.2.23 $
 */

#include "Ia32IRManager.h"
#include "Ia32Encoder.h"
#include "Ia32Printer.h"
#include "Log.h"
#include "Ia32Printer.h"
#include "Ia32CodeGenerator.h"
#include "float.h"
#include <math.h>

namespace Jitrino
{
namespace Ia32
{

using namespace std;

//=========================================================================================================
static void appendToInstList(Inst *& head, Inst * listToAppend) {
    if (head==NULL) {
        head=listToAppend;
    } else if (listToAppend!=NULL){
        listToAppend->insertBefore(head);
    }
}

//_________________________________________________________________________________________________
const char * newString(MemoryManager& mm, const char * str, uint32 length)
{
    assert(str!=NULL);
    if (length==EmptyUint32)
        length=strlen(str);
    char * psz=new(mm) char[length+1];
    strncpy(psz, str, length);
    psz[length]=0;
    return psz;
}


//_____________________________________________________________________________________________
IRManager::IRManager(MemoryManager& memManager, TypeManager& tm, MethodDesc& md, CompilationInterface& compIface)
    :memoryManager(memManager), typeManager(tm), methodDesc(md), compilationInterface(compIface),
        opndId(0), instId(0),
        opnds(memManager), gpTotalRegUsage(0), entryPointInst(NULL), _hasLivenessInfo(false),
        internalHelperInfos(memManager), infoMap(memManager), verificationLevel(0),
        hasCalls(false), hasNonExceptionCalls(false), laidOut(false), codeStartAddr(NULL)

{  
    for (uint32 i=0; i<lengthof(regOpnds); i++) regOpnds[i]=NULL;
    fg = new (memManager) ControlFlowGraph(memManager, this);
    fg->setEntryNode(fg->createBlockNode());
    fg->setLoopTree(new (memManager) LoopTree(memManager, fg));
    initInitialConstraints();
    registerInternalHelperInfo("printRuntimeOpndInternalHelper", IRManager::InternalHelperInfo((void*)&printRuntimeOpndInternalHelper, &CallingConvention_STDCALL));
}


//_____________________________________________________________________________________________
void IRManager::addOpnd(Opnd * opnd)
{ 
    assert(opnd->id>=opnds.size());
    opnds.push_back(opnd);
    opnd->id=opnds.size()-1;
}

//_____________________________________________________________________________________________
Opnd * IRManager::newOpnd(Type * type)
{
    Opnd * opnd=new(memoryManager) Opnd(opndId++, type, getInitialConstraint(type));
    addOpnd(opnd);
    return opnd;
}

//_____________________________________________________________________________________________
Opnd * IRManager::newOpnd(Type * type, Constraint c)
{
    c.intersectWith(Constraint(OpndKind_Any, getTypeSize(type)));
    assert(!c.isNull());
    Opnd * opnd=new(memoryManager) Opnd(opndId++, type, c);
    addOpnd(opnd);
    return opnd;
}

//_____________________________________________________________________________________________
Opnd * IRManager::newImmOpnd(Type * type, int64 immediate)
{
    Opnd * opnd = newOpnd(type);
    opnd->assignImmValue(immediate);
    return opnd;
}

//____________________________________________________________________________________________
Opnd * IRManager::newImmOpnd(Type * type, Opnd::RuntimeInfo::Kind kind, void * arg0, void * arg1, void * arg2, void * arg3)
{
    Opnd * opnd=newImmOpnd(type, 0);
    opnd->setRuntimeInfo(new(memoryManager) Opnd::RuntimeInfo(kind, arg0, arg1, arg2, arg3));
    return opnd;
}

//_____________________________________________________________________________________________
ConstantAreaItem * IRManager::newConstantAreaItem(float f)
{
    return new(memoryManager) ConstantAreaItem(
        ConstantAreaItem::Kind_FPSingleConstantAreaItem, sizeof(float), 
        new(memoryManager) float(f)
    );
}

//_____________________________________________________________________________________________
ConstantAreaItem *  IRManager::newConstantAreaItem(double d)
{
    return new(memoryManager) ConstantAreaItem(
        ConstantAreaItem::Kind_FPDoubleConstantAreaItem, sizeof(double), 
        new(memoryManager) double(d)
    );
}

//_____________________________________________________________________________________________
ConstantAreaItem *  IRManager::newSwitchTableConstantAreaItem(uint32 numTargets)
{
    return new(memoryManager) ConstantAreaItem(
        ConstantAreaItem::Kind_SwitchTableConstantAreaItem, sizeof(BasicBlock*)*numTargets, 
        new(memoryManager) BasicBlock*[numTargets]
    );
}

//_____________________________________________________________________________________________
ConstantAreaItem *  IRManager::newInternalStringConstantAreaItem(const char * str)
{
    if (str==NULL)
        str="";
    return new(memoryManager) ConstantAreaItem(
        ConstantAreaItem::Kind_InternalStringConstantAreaItem, strlen(str)+1, 
        (void*)newInternalString(str)
    );
}

//_____________________________________________________________________________________________
ConstantAreaItem * IRManager::newBinaryConstantAreaItem(uint32 size, const void * pv)
{
    return new(memoryManager) ConstantAreaItem(ConstantAreaItem::Kind_BinaryConstantAreaItem, size, pv);
}

//_____________________________________________________________________________________________
Opnd * IRManager::newFPConstantMemOpnd(float f, Opnd * baseOpnd, BasicBlock* bb)
{
    ConstantAreaItem * item=newConstantAreaItem(f);
    Opnd * addr=newImmOpnd(typeManager.getUnmanagedPtrType(typeManager.getSingleType()), Opnd::RuntimeInfo::Kind_ConstantAreaItem, item);
#ifdef _EM64T_
    bb->appendInst(newCopyPseudoInst(Mnemonic_MOV, baseOpnd, addr));
    return newMemOpndAutoKind(typeManager.getSingleType(), MemOpndKind_ConstantArea, baseOpnd);
#else
    return newMemOpndAutoKind(typeManager.getSingleType(), MemOpndKind_ConstantArea, addr);
#endif
}

//_____________________________________________________________________________________________
Opnd * IRManager::newFPConstantMemOpnd(double d, Opnd * baseOpnd, BasicBlock* bb)
{
    ConstantAreaItem * item=newConstantAreaItem(d);
    Opnd * addr=newImmOpnd(typeManager.getUnmanagedPtrType(typeManager.getDoubleType()), Opnd::RuntimeInfo::Kind_ConstantAreaItem, item);
#ifdef _EM64T_
    bb->appendInst(newCopyPseudoInst(Mnemonic_MOV, baseOpnd, addr));
    return newMemOpndAutoKind(typeManager.getDoubleType(), MemOpndKind_ConstantArea, baseOpnd);
#else
    return newMemOpndAutoKind(typeManager.getDoubleType(), MemOpndKind_ConstantArea, addr);
#endif
}

//_____________________________________________________________________________________________
Opnd * IRManager::newSwitchTableConstantMemOpnd(uint32 numTargets, Opnd * index)
{
    ConstantAreaItem * item=newSwitchTableConstantAreaItem(numTargets);
#ifdef _EM64T_
    Opnd * switchTable=newImmOpnd(typeManager.getInt32Type(), Opnd::RuntimeInfo::Kind_ConstantAreaItem, item);
#else
    Opnd * switchTable=newImmOpnd(typeManager.getIntPtrType(), Opnd::RuntimeInfo::Kind_ConstantAreaItem, item);
#endif
    return newMemOpnd(typeManager.getIntPtrType(), MemOpndKind_ConstantArea, 0, index, newImmOpnd(typeManager.getInt32Type(), sizeof(POINTER_SIZE_INT)), switchTable);
}

//_____________________________________________________________________________________________
Opnd * IRManager::newInternalStringConstantImmOpnd(const char * str)
{
    ConstantAreaItem * item=newInternalStringConstantAreaItem(str);
    return newImmOpnd(typeManager.getUnmanagedPtrType(typeManager.getIntPtrType()), Opnd::RuntimeInfo::Kind_ConstantAreaItem, item);
}

//_____________________________________________________________________________________________
Opnd * IRManager::newBinaryConstantImmOpnd(uint32 size, const void * pv)
{
    ConstantAreaItem * item=newBinaryConstantAreaItem(size, pv);
    return newImmOpnd(typeManager.getUnmanagedPtrType(typeManager.getIntPtrType()), Opnd::RuntimeInfo::Kind_ConstantAreaItem, item);
}

//_____________________________________________________________________________________________
SwitchInst * IRManager::newSwitchInst(uint32 numTargets, Opnd * index)
{
    Inst * instList = NULL;
#ifndef _EM64T_
    SwitchInst * inst=new(memoryManager, 1) SwitchInst(Mnemonic_JMP, instId++);
    assert(numTargets>0);
    assert(index!=NULL);
    Opnd * targetOpnd=newSwitchTableConstantMemOpnd(numTargets, index);
#else
    
    ConstantAreaItem * item=newSwitchTableConstantAreaItem(numTargets);
    Opnd * tableAddr = newImmOpnd(typeManager.getInt64Type(), Opnd::RuntimeInfo::Kind_ConstantAreaItem, item);

    SwitchInst * inst=new(memoryManager, 1) SwitchInst(Mnemonic_JMP, instId++, tableAddr);
    assert(numTargets>0);
    assert(index!=NULL);
    Opnd * baseOpnd = newOpnd(typeManager.getInt64Type());
    appendToInstList(instList, newCopyPseudoInst(Mnemonic_MOV, baseOpnd, tableAddr));
    Opnd * targetOpnd =  newMemOpnd(typeManager.getUnmanagedPtrType(typeManager.getIntPtrType()), MemOpndKind_ConstantArea, baseOpnd, index, newImmOpnd(typeManager.getInt32Type(), sizeof(POINTER_SIZE_INT)), 0);
#endif
    inst->insertOpnd(0, targetOpnd, Inst::OpndRole_Explicit);
    inst->assignOpcodeGroup(this);
    appendToInstList(instList, inst);
    return (SwitchInst *)instList;
}

//_____________________________________________________________________________________________
Opnd * IRManager::newRegOpnd(Type * type, RegName reg)
{
    Opnd * opnd = newOpnd(type, Constraint(getRegKind(reg)));
    opnd->assignRegName(reg);
    return opnd;
}

//_____________________________________________________________________________________________
Opnd * IRManager::newMemOpnd(Type * type, MemOpndKind k, Opnd * base, Opnd * index, Opnd * scale, Opnd * displacement, RegName segReg)
{
    Opnd * opnd = newOpnd(type);
    opnd->assignMemLocation(k,base,index,scale,displacement);
        if (segReg != RegName_Null)
            opnd->setSegReg(segReg);
    return opnd;
}

//_________________________________________________________________________________________________
Opnd * IRManager::newMemOpnd(Type * type, Opnd * base, Opnd * index, Opnd * scale, Opnd * displacement, RegName segReg)
{
    return newMemOpnd(type, MemOpndKind_Heap, base, index, scale, displacement, segReg);
}

//_____________________________________________________________________________________________
Opnd * IRManager::newMemOpnd(Type * type, MemOpndKind k, Opnd * base, int32 displacement, RegName segReg)
{
    return newMemOpnd(type, k, base, 0, 0, newImmOpnd(typeManager.getInt32Type(), displacement), segReg);
}

//_____________________________________________________________________________________________
Opnd * IRManager::newMemOpndAutoKind(Type * type, MemOpndKind k, Opnd * opnd0, Opnd * opnd1, Opnd * opnd2)
{  
    Opnd * base=NULL, * displacement=NULL;

    Constraint c=opnd0->getConstraint(Opnd::ConstraintKind_Current);
    if (!(c&OpndKind_GPReg).isNull()){
        base=opnd0;
    }else if (!(c&OpndKind_Imm).isNull()){
        displacement=opnd0;
    }else
        assert(0);

    if (opnd1!=NULL){
        c=opnd1->getConstraint(Opnd::ConstraintKind_Current);
        if (!(c&OpndKind_GPReg).isNull()){
            base=opnd1;
        }else if (!(c&OpndKind_Imm).isNull()){
            displacement=opnd1;
        }else
            assert(0);
    }

    return newMemOpnd(type, k, base, 0, 0, displacement);
}

//_____________________________________________________________________________________________
void IRManager::initInitialConstraints()
{
    for (uint32 i=0; i<lengthof(initialConstraints); i++)
        initialConstraints[i] = createInitialConstraint((Type::Tag)i);
}

//_____________________________________________________________________________________________
Constraint IRManager::createInitialConstraint(Type::Tag t)const
{
    OpndSize sz=getTypeSize(t);
    if (t==Type::Single||t==Type::Double||t==Type::Float)
        return Constraint(OpndKind_XMMReg, sz)|Constraint(OpndKind_Mem, sz);
    if (sz<=Constraint::getDefaultSize(OpndKind_GPReg))
        return Constraint(OpndKind_GPReg, sz)|Constraint(OpndKind_Mem, sz)|Constraint(OpndKind_Imm, sz);
    if (sz==OpndSize_64)
        return Constraint(OpndKind_Mem, sz)|Constraint(OpndKind_Imm, sz); // imm before lowering
    return Constraint(OpndKind_Memory, sz);
}

//_____________________________________________________________________________________________
Inst * IRManager::newInst(Mnemonic mnemonic, Opnd * opnd0, Opnd * opnd1, Opnd * opnd2)
{
    Inst * inst = new(memoryManager, 4) Inst(mnemonic, instId++, Inst::Form_Native);
    uint32 i=0;
    Opnd ** opnds = inst->getOpnds();
    uint32 * roles = inst->getOpndRoles();
    if (opnd0!=NULL){ opnds[i] = opnd0; roles[i] = Inst::OpndRole_Explicit; i++;
    if (opnd1!=NULL){ opnds[i] = opnd1; roles[i] = Inst::OpndRole_Explicit; i++;
    if (opnd2!=NULL){ opnds[i] = opnd2; roles[i] = Inst::OpndRole_Explicit; i++;
    }}}
    inst->opndCount = i;
    inst->assignOpcodeGroup(this);
    return inst;
}

//_____________________________________________________________________________________________
Inst * IRManager::newInst(Mnemonic mnemonic, 
        Opnd * opnd0, Opnd * opnd1, Opnd * opnd2, Opnd * opnd3, 
        Opnd * opnd4, Opnd * opnd5, Opnd * opnd6, Opnd * opnd7
    )
{
    Inst * inst = new(memoryManager, 8) Inst(mnemonic, instId++, Inst::Form_Native);
    uint32 i=0;
    Opnd ** opnds = inst->getOpnds();
    uint32 * roles = inst->getOpndRoles();
    if (opnd0!=NULL){ opnds[i] = opnd0; roles[i] = Inst::OpndRole_Explicit; i++;
    if (opnd1!=NULL){ opnds[i] = opnd1; roles[i] = Inst::OpndRole_Explicit; i++;
    if (opnd2!=NULL){ opnds[i] = opnd2; roles[i] = Inst::OpndRole_Explicit; i++;
    if (opnd3!=NULL){ opnds[i] = opnd3; roles[i] = Inst::OpndRole_Explicit; i++;
    if (opnd4!=NULL){ opnds[i] = opnd4; roles[i] = Inst::OpndRole_Explicit; i++;
    if (opnd5!=NULL){ opnds[i] = opnd5; roles[i] = Inst::OpndRole_Explicit; i++;
    if (opnd6!=NULL){ opnds[i] = opnd6; roles[i] = Inst::OpndRole_Explicit; i++;
    if (opnd7!=NULL){ opnds[i] = opnd7; roles[i] = Inst::OpndRole_Explicit; i++;
    }}}}}}}};   
    inst->opndCount = i;
    inst->assignOpcodeGroup(this);
    return inst;
}

//_____________________________________________________________________________________________
Inst * IRManager::newInstEx(Mnemonic mnemonic, uint32 defCount, Opnd * opnd0, Opnd * opnd1, Opnd * opnd2)
{
    Inst * inst = new(memoryManager, 4) Inst(mnemonic, instId++, Inst::Form_Extended);
    uint32 i=0;
    Opnd ** opnds = inst->getOpnds();
    uint32 * roles = inst->getOpndRoles();
    if (opnd0!=NULL){ 
        opnds[i] = opnd0; roles[i] = Inst::OpndRole_Explicit; i++;
    if (opnd1!=NULL){ 
        opnds[i] = opnd1; roles[i] = Inst::OpndRole_Explicit; i++;
    if (opnd2!=NULL){ 
        opnds[i] = opnd2; roles[i] = Inst::OpndRole_Explicit; i++;
    }}}
    inst->opndCount = i;
    inst->defOpndCount=defCount;
    inst->assignOpcodeGroup(this);
    return inst;
}

//_____________________________________________________________________________________________
Inst * IRManager::newInstEx(Mnemonic mnemonic, uint32 defCount, 
        Opnd * opnd0, Opnd * opnd1, Opnd * opnd2, Opnd * opnd3, 
        Opnd * opnd4, Opnd * opnd5, Opnd * opnd6, Opnd * opnd7
    )
{
    Inst * inst = new(memoryManager, 8) Inst(mnemonic, instId++, Inst::Form_Extended);
    uint32 i=0;
    Opnd ** opnds = inst->getOpnds();
    uint32 * roles = inst->getOpndRoles();
    if (opnd0!=NULL){ 
        opnds[i] = opnd0; roles[i] = Inst::OpndRole_Explicit; i++;
    if (opnd1!=NULL){ 
        opnds[i] = opnd1; roles[i] = Inst::OpndRole_Explicit; i++;
    if (opnd2!=NULL){ 
        opnds[i] = opnd2; roles[i] = Inst::OpndRole_Explicit; i++;
    if (opnd3!=NULL){ 
        opnds[i] = opnd3; roles[i] = Inst::OpndRole_Explicit; i++;
    if (opnd4!=NULL){ 
        opnds[i] = opnd4; roles[i] = Inst::OpndRole_Explicit; i++;
    if (opnd5!=NULL){ 
        opnds[i] = opnd5; roles[i] = Inst::OpndRole_Explicit; i++;
    if (opnd6!=NULL){ 
        opnds[i] = opnd6; roles[i] = Inst::OpndRole_Explicit; i++;
    if (opnd7!=NULL){ 
        opnds[i] = opnd7; roles[i] = Inst::OpndRole_Explicit; i++;
    }}}}}}}};   
    inst->opndCount = i;
    inst->defOpndCount=defCount;
    inst->assignOpcodeGroup(this);
    return inst;
}

//_________________________________________________________________________________________________
Inst * IRManager::newI8PseudoInst(Mnemonic mnemonic, uint32 defCount,
            Opnd * opnd0, Opnd * opnd1, Opnd * opnd2, Opnd * opnd3
        )
{
    Inst * inst=new  (memoryManager, 4) Inst(mnemonic, instId++, Inst::Form_Extended);
    inst->kind = Inst::Kind_I8PseudoInst;
    uint32 i=0;
    Opnd ** opnds = inst->getOpnds();
    assert(opnd0->getType()->isInteger());
    if (opnd0!=NULL){       opnds[i] = opnd0; i++;
    if (opnd1!=NULL){       opnds[i] = opnd1; i++;
    if (opnd2!=NULL){       opnds[i] = opnd2; i++;
    if (opnd3!=NULL){       opnds[i] = opnd3; i++;      assert(opnd3->getSize()==OpndSize_64);
    }}}};   
    inst->defOpndCount=defCount;
    inst->opndCount = i;
    inst->assignOpcodeGroup(this);
    return inst;
}

//_____________________________________________________________________________________________
SystemExceptionCheckPseudoInst * IRManager::newSystemExceptionCheckPseudoInst(CompilationInterface::SystemExceptionId exceptionId, Opnd * opnd0, Opnd * opnd1, bool checksThisForInlinedMethod)
{
    SystemExceptionCheckPseudoInst * inst=new  (memoryManager, 8) SystemExceptionCheckPseudoInst(exceptionId, instId++, checksThisForInlinedMethod);
    uint32 i=0;
    Opnd ** opnds = inst->getOpnds();
    if (opnd0!=NULL){ opnds[i++] = opnd0; 
    if (opnd1!=NULL){ opnds[i++] = opnd1;
    }}      
    inst->opndCount = i;
    inst->assignOpcodeGroup(this);
    return inst;
}



//_________________________________________________________________________________________________
BranchInst * IRManager::newBranchInst(Mnemonic mnemonic, Node* trueTarget, Node* falseTarget,  Opnd * targetOpnd)
{
    BranchInst * inst=new(memoryManager, 2) BranchInst(mnemonic, instId++);
    if (targetOpnd==0)
        targetOpnd=newImmOpnd(typeManager.getInt32Type(), 0);
    inst->insertOpnd(0, targetOpnd, Inst::OpndRole_Explicit);
    inst->assignOpcodeGroup(this);
    inst->setFalseTarget(falseTarget);
    inst->setTrueTarget(trueTarget);
    return inst;
}

//_________________________________________________________________________________________________
JumpInst * IRManager::newJumpInst(Opnd * targetOpnd)
{
    JumpInst * inst=new(memoryManager, 2) JumpInst(instId++);
    if (targetOpnd==0) {
        targetOpnd=newImmOpnd(typeManager.getInt32Type(), 0);
    }
    inst->insertOpnd(0, targetOpnd, Inst::OpndRole_Explicit);
    inst->assignOpcodeGroup(this);
    return inst;
}

//_________________________________________________________________________________________________
EntryPointPseudoInst * IRManager::newEntryPointPseudoInst(const CallingConvention * cc)
{
    // there is nothing wrong with calling this method several times.
    // it's just a self-check, as the currently assumed behaviour is that this method is invoked only once.
    assert(NULL == entryPointInst);

    EntryPointPseudoInst * inst=new(memoryManager, methodDesc.getMethodSig()->getNumParams() * 2) EntryPointPseudoInst(this, instId++, cc);
    fg->getEntryNode()->appendInst(inst);

    inst->assignOpcodeGroup(this);
    entryPointInst = inst;

    if (getCompilationInterface().getCompilationParams().exe_notify_method_entry) {
        Opnd **hlpArgs = new (memoryManager) Opnd* [1];
        hlpArgs[0] = newImmOpnd(typeManager.getIntPtrType(), 
            Opnd::RuntimeInfo::Kind_MethodRuntimeId, &methodDesc);
            Node* prolog = getFlowGraph()->getEntryNode();
            prolog->appendInst(newRuntimeHelperCallInst(CompilationInterface::Helper_MethodEntry,
                    1, (Opnd**)hlpArgs, NULL));
    }


    return inst;
}

//_________________________________________________________________________________________________
CallInst * IRManager::newCallInst(Opnd * targetOpnd, const CallingConvention * cc, 
        uint32 argCount, Opnd ** args, Opnd * retOpnd, InlineInfo* ii)
{
    CallInst * callInst=new(memoryManager, (argCount + (retOpnd ? 1 : 0)) * 2 + 1) CallInst(this, instId++, cc, ii);
    CallingConventionClient & ccc = callInst->callingConventionClient;
    uint32 i=0;
    if (retOpnd!=NULL){
        ccc.pushInfo(Inst::OpndRole_Def, retOpnd->getType()->tag);
        callInst->insertOpnd(i++, retOpnd, Inst::OpndRole_Auxilary|Inst::OpndRole_Def);
    }
    callInst->defOpndCount = i;
    callInst->insertOpnd(i++, targetOpnd, Inst::OpndRole_Explicit|Inst::OpndRole_Use);

    if (argCount>0){
        for (uint32 j=0; j<argCount; j++){
            ccc.pushInfo(Inst::OpndRole_Use, args[j]->getType()->tag);
            callInst->insertOpnd(i++, args[j], Inst::OpndRole_Auxilary|Inst::OpndRole_Use);
        }
    }
    callInst->opndCount = i;
    callInst->assignOpcodeGroup(this);
    return callInst;
}

//_________________________________________________________________________________________________
CallInst * IRManager::newRuntimeHelperCallInst(CompilationInterface::RuntimeHelperId helperId, 
    uint32 numArgs, Opnd ** args, Opnd * retOpnd, InlineInfo* ii)
{
    Inst * instList = NULL;
    Opnd * target=newImmOpnd(typeManager.getUnmanagedPtrType(typeManager.getIntPtrType()), Opnd::RuntimeInfo::Kind_HelperAddress, (void*)helperId);
    const CallingConvention * cc=getCallingConvention(helperId);
    appendToInstList(instList,newCallInst(target, cc, numArgs, args, retOpnd));
    return (CallInst *)instList;
}

//_________________________________________________________________________________________________
CallInst * IRManager::newInternalRuntimeHelperCallInst(const char * internalHelperID, uint32 numArgs, Opnd ** args, Opnd * retOpnd)
{
    const InternalHelperInfo * info=getInternalHelperInfo(internalHelperID);
    assert(info!=NULL);
    Inst * instList = NULL;
    Opnd * target=newImmOpnd(typeManager.getUnmanagedPtrType(typeManager.getIntPtrType()), Opnd::RuntimeInfo::Kind_InternalHelperAddress, (void*)newInternalString(internalHelperID));
    const CallingConvention * cc=info->callingConvention;
    appendToInstList(instList,newCallInst(target, cc, numArgs, args, retOpnd));
    return (CallInst *)instList;
}
//_________________________________________________________________________________________________
Inst* IRManager::newEmptyPseudoInst() {
    return new(memoryManager, 0) EmptyPseudoInst(instId++);
}
//_________________________________________________________________________________________________
MethodMarkerPseudoInst* IRManager::newMethodEntryPseudoInst(MethodDesc* mDesc) {
    return new(memoryManager, 0) MethodMarkerPseudoInst(mDesc, instId++, Inst::Kind_MethodEntryPseudoInst);
}
//_________________________________________________________________________________________________
MethodMarkerPseudoInst* IRManager::newMethodEndPseudoInst(MethodDesc* mDesc) {
    return new(memoryManager, 0) MethodMarkerPseudoInst(mDesc, instId++, Inst::Kind_MethodEndPseudoInst);
}
//_________________________________________________________________________________________________
void IRManager::registerInternalHelperInfo(const char * internalHelperID, const InternalHelperInfo& info)
{
    assert(internalHelperID!=NULL && internalHelperID[0]!=0);
    internalHelperInfos[newInternalString(internalHelperID)]=info;
}

//_________________________________________________________________________________________________
RetInst * IRManager::newRetInst(Opnd * retOpnd)
{
    RetInst * retInst=new  (memoryManager, 4) RetInst(this, instId++);
    assert( NULL != entryPointInst && NULL != entryPointInst->getCallingConventionClient().getCallingConvention() );
    retInst->insertOpnd(0, newImmOpnd(typeManager.getInt16Type(), 0), Inst::OpndRole_Explicit|Inst::OpndRole_Use);
    if (retOpnd!=NULL){
        retInst->insertOpnd(1, retOpnd, Inst::OpndRole_Auxilary|Inst::OpndRole_Use);
        retInst->getCallingConventionClient().pushInfo(Inst::OpndRole_Use, retOpnd->getType()->tag);
        retInst->opndCount = 2;
    } else  retInst->opndCount = 1;

    retInst->assignOpcodeGroup(this);
    return retInst;
}


//_________________________________________________________________________________________________
void IRManager::applyCallingConventions()
{
    const Nodes& nodes = fg->getNodes();
    for (Nodes::const_iterator it = nodes.begin(), end = nodes.end(); it!=end; ++it) {
        Node* node = *it;
        if (node->isBlockNode()){
            for (Inst * inst= (Inst*)node->getFirstInst(); inst!=NULL; inst=inst->getNextInst()){
                if (inst->hasKind(Inst::Kind_EntryPointPseudoInst)){
                    EntryPointPseudoInst * eppi=(EntryPointPseudoInst*)inst;
                    eppi->callingConventionClient.finalizeInfos(Inst::OpndRole_Def, CallingConvention::ArgKind_InArg);
                    eppi->callingConventionClient.layoutAuxilaryOpnds(Inst::OpndRole_Def, OpndKind_Memory);
                }else if (inst->hasKind(Inst::Kind_CallInst)){
                    CallInst * callInst=(CallInst*)inst;

                    callInst->callingConventionClient.finalizeInfos(Inst::OpndRole_Use, CallingConvention::ArgKind_InArg);
                    callInst->callingConventionClient.layoutAuxilaryOpnds(Inst::OpndRole_Use, OpndKind_Any);

                    callInst->callingConventionClient.finalizeInfos(Inst::OpndRole_Def, CallingConvention::ArgKind_RetArg);
                    callInst->callingConventionClient.layoutAuxilaryOpnds(Inst::OpndRole_Def, OpndKind_Null);
                    
                }else if (inst->hasKind(Inst::Kind_RetInst)){
                    RetInst * retInst=(RetInst*)inst;
                    retInst->callingConventionClient.finalizeInfos(Inst::OpndRole_Use, CallingConvention::ArgKind_RetArg);
                    retInst->callingConventionClient.layoutAuxilaryOpnds(Inst::OpndRole_Use, OpndKind_Null);

                    if (retInst->getCallingConventionClient().getCallingConvention()->calleeRestoresStack()){
                        uint32 stackDepth=getEntryPointInst()->getArgStackDepth();
                        retInst->getOpnd(0)->assignImmValue(stackDepth);
                    }
                }
            }
        }
    }
}

//_________________________________________________________________________________________________
CatchPseudoInst * IRManager::newCatchPseudoInst(Opnd * exception)
{
    CatchPseudoInst * inst=new  (memoryManager, 1) CatchPseudoInst(instId++);
    inst->insertOpnd(0, exception, Inst::OpndRole_Def);
    inst->setConstraint(0, RegName_EAX);
    inst->defOpndCount = 1;
    inst->opndCount = 1;
    inst->assignOpcodeGroup(this);
    return inst;
}

//_________________________________________________________________________________________________
GCInfoPseudoInst* IRManager::newGCInfoPseudoInst(const StlVector<Opnd*>& basesAndMptrs) {
#ifdef _DEBUG
    for (StlVector<Opnd*>::const_iterator it = basesAndMptrs.begin(), end = basesAndMptrs.end(); it!=end; ++it) {
        Opnd* opnd = *it;
        assert(opnd->getType()->isObject() || opnd->getType()->isManagedPtr());
    }
#endif
    GCInfoPseudoInst* inst = new(memoryManager, basesAndMptrs.size()) GCInfoPseudoInst(this, instId++);
    Opnd ** opnds = inst->getOpnds();
    Constraint * constraints = inst->getConstraints();
    for (uint32 i = 0, n = basesAndMptrs.size(); i < n; i++){
        Opnd * opnd = basesAndMptrs[i];
        opnds[i] = opnd;
        constraints[i] = Constraint(OpndKind_Any, opnd->getSize());
    }
    inst->opndCount = basesAndMptrs.size();
    inst->assignOpcodeGroup(this);
    return inst;
}

//_________________________________________________________________________________________________
Inst * IRManager::newCopyPseudoInst(Mnemonic mn, Opnd * opnd0, Opnd * opnd1)
{ 
    assert(mn==Mnemonic_MOV||mn==Mnemonic_PUSH||mn==Mnemonic_POP);
    uint32 allOpndCnt = opnd0->getType()->isInt8() ? 4 : 2;
    Inst * inst=new  (memoryManager, allOpndCnt) Inst(mn, instId++, Inst::Form_Extended);
    inst->kind = Inst::Kind_CopyPseudoInst;
    assert(opnd0!=NULL);
    assert(opnd1==NULL||opnd0->getSize()<=opnd1->getSize());

    Opnd ** opnds = inst->getOpnds();
    Constraint * opndConstraints = inst->getConstraints();

    opnds[0] = opnd0;
    opndConstraints[0] = Constraint(OpndKind_Any, opnd0->getSize());
    if (opnd1!=NULL){
        opnds[1] = opnd1;
        opndConstraints[1] = Constraint(OpndKind_Any, opnd0->getSize());
        inst->opndCount = 2;
    }else
        inst->opndCount = 1;
    if (mn != Mnemonic_PUSH)
        inst->defOpndCount = 1;
    inst->assignOpcodeGroup(this);
    return inst;
}

//_________________________________________________________________________________________________
AliasPseudoInst * IRManager::newAliasPseudoInst(Opnd * targetOpnd, Opnd * sourceOpnd, uint32 offset)
{
    assert(sourceOpnd->isPlacedIn(OpndKind_Memory));
    assert(!targetOpnd->hasAssignedPhysicalLocation());
    assert(targetOpnd->canBePlacedIn(OpndKind_Memory));

    Type * sourceType=sourceOpnd->getType();
    OpndSize sourceSize=getTypeSize(sourceType);

    Type * targetType=targetOpnd->getType();
    OpndSize targetSize=getTypeSize(targetType);

#ifdef _DEBUG
    uint32 sourceByteSize=getByteSize(sourceSize);
    uint32 targetByteSize=getByteSize(targetSize);
    assert(getByteSize(sourceSize)>0 && getByteSize(targetSize)>0);
    assert(offset+targetByteSize<=sourceByteSize);
#endif

    uint32 allocOpndNum = sourceOpnd->getType()->isInt8() ? 3 : 2;
    AliasPseudoInst * inst=new  (memoryManager, allocOpndNum) AliasPseudoInst(instId++);

    inst->getOpnds()[0] = targetOpnd;
    inst->getConstraints()[0] = Constraint(OpndKind_Mem, targetSize);

    inst->getOpnds()[1] = sourceOpnd;
    inst->getConstraints()[1] = Constraint(OpndKind_Mem, sourceSize);
    if (sourceOpnd->getType()->isInt8()) {
        inst->getConstraints()[2] = Constraint(OpndKind_Mem, targetSize);
    }

    inst->defOpndCount = 1;
    inst->opndCount = 2;

    inst->offset=offset;

    layoutAliasPseudoInstOpnds(inst);

    inst->assignOpcodeGroup(this);
    return inst;
}

//_________________________________________________________________________________________________
AliasPseudoInst * IRManager::newAliasPseudoInst(Opnd * targetOpnd, uint32 sourceOpndCount, Opnd ** sourceOpnds)
{
    assert(targetOpnd->isPlacedIn(OpndKind_Memory));

    Type * targetType=targetOpnd->getType();
    OpndSize targetSize=getTypeSize(targetType);
    assert(getByteSize(targetSize)>0);
    uint32 allocOpnNum = 0;
    for (uint32 i=0; i<sourceOpndCount; i++) allocOpnNum += getByteSize(getTypeSize(sourceOpnds[i]->getType()));

    allocOpnNum += getByteSize(getTypeSize(targetOpnd->getType()));
    AliasPseudoInst * inst=new  (memoryManager, allocOpnNum) AliasPseudoInst(instId++);

    Opnd ** opnds = inst->getOpnds();
    Constraint * opndConstraints = inst->getConstraints();

    opnds[0] = targetOpnd;
    opndConstraints[0] = Constraint(OpndKind_Mem, targetSize);


    uint32 offset=0;
    for (uint32 i=0; i<sourceOpndCount; i++){
        assert(!sourceOpnds[i]->hasAssignedPhysicalLocation() || 
            (sourceOpnds[i]->isPlacedIn(OpndKind_Memory) && 
            sourceOpnds[i]->getMemOpndKind() == targetOpnd->getMemOpndKind())
            );
        assert(sourceOpnds[i]->canBePlacedIn(OpndKind_Memory));
        Type * sourceType=sourceOpnds[i]->getType();
        OpndSize sourceSize=getTypeSize(sourceType);
        uint32 sourceByteSize=getByteSize(sourceSize);
        assert(sourceByteSize>0);
        assert(offset+sourceByteSize<=getByteSize(targetSize));

        opnds[1 + i] = sourceOpnds[i];
        opndConstraints[1 + i] = Constraint(OpndKind_Mem, sourceSize);

        offset+=sourceByteSize;
    }
    inst->defOpndCount = 1;
    inst->opndCount = sourceOpndCount + 1;

    layoutAliasPseudoInstOpnds(inst);
    inst->assignOpcodeGroup(this);
    return inst;
}

//_________________________________________________________________________________________________
void IRManager::layoutAliasPseudoInstOpnds(AliasPseudoInst * inst)
{
    assert(inst->getOpndCount(Inst::OpndRole_InstLevel|Inst::OpndRole_Def) == 1);
    Opnd * const * opnds = inst->getOpnds();
    Opnd * defOpnd=opnds[0];
    Opnd * const * useOpnds = opnds + 1; 
    uint32 useCount = inst->getOpndCount(Inst::OpndRole_InstLevel|Inst::OpndRole_Use);
    assert(useCount > 0);
    if (inst->offset==EmptyUint32){
        uint32 offset=0;
        for (uint32 i=0; i<useCount; i++){
            Opnd * innerOpnd=useOpnds[i];
            assignInnerMemOpnd(defOpnd, innerOpnd, offset);
            offset+=getByteSize(innerOpnd->getSize());
        }
    }else
        assignInnerMemOpnd(useOpnds[0], defOpnd, inst->offset);
}

//_________________________________________________________________________________________________
void IRManager::addAliasRelation(AliasRelation * relations, Opnd * outerOpnd, Opnd * innerOpnd, uint32 offset)
{
    if (outerOpnd==innerOpnd){
        assert(offset==0);
        return;
    }

    const AliasRelation& outerRel=relations[outerOpnd->getId()];
    if (outerRel.outerOpnd!=NULL){
        addAliasRelation(relations, outerRel.outerOpnd, innerOpnd, outerRel.offset+offset);
        return;
    }

    AliasRelation& innerRel=relations[innerOpnd->getId()];
    if (innerRel.outerOpnd!=NULL){
        addAliasRelation(relations, outerOpnd, innerRel.outerOpnd, offset-(int)innerRel.offset);
    }

#ifdef _DEBUG
    Type * outerType=outerOpnd->getType();
    OpndSize outerSize=getTypeSize(outerType);
    uint32 outerByteSize=getByteSize(outerSize);
    assert(offset<outerByteSize);

    Type * innerType=innerOpnd->getType();
    OpndSize innerSize=getTypeSize(innerType);
    uint32 innerByteSize=getByteSize(innerSize);
    assert(outerByteSize>0 && innerByteSize>0);
    assert(offset+innerByteSize<=outerByteSize);
#endif

    innerRel.outerOpnd=outerOpnd;
    innerRel.offset=offset;

}

//_________________________________________________________________________________________________
void IRManager::getAliasRelations(AliasRelation * relations)
{
    const Nodes& nodes = fg->getNodes();
    for (Nodes::const_iterator it = nodes.begin(), end = nodes.end(); it!=end; ++it) {
        Node* node = *it;
        if (node->isBlockNode()) {
            for (Inst * inst=(Inst*)node->getFirstInst(); inst!=NULL; inst=inst->getNextInst()) {
                if (inst->hasKind(Inst::Kind_AliasPseudoInst)){
                    AliasPseudoInst * aliasInst=(AliasPseudoInst *)inst;
                    Opnd * const * opnds = inst->getOpnds();
                    uint32 useCount = inst->getOpndCount(Inst::OpndRole_InstLevel|Inst::OpndRole_Use);
                    assert(inst->getOpndCount(Inst::OpndRole_InstLevel|Inst::OpndRole_Def) == 1 && useCount > 0);
                    Opnd * defOpnd=opnds[0];
                    Opnd * const * useOpnds = opnds + 1; 
                    if (aliasInst->offset==EmptyUint32){
                        uint32 offset=0;
                        for (uint32 i=0; i<useCount; i++){
                            Opnd * innerOpnd=useOpnds[i];
                            addAliasRelation(relations, defOpnd, innerOpnd, offset);
                            offset+=getByteSize(innerOpnd->getSize());
                        }
                    }else{
                        addAliasRelation(relations, useOpnds[0], defOpnd, aliasInst->offset);
                    }
                }
            }
        }
    }
}

//_________________________________________________________________________________________________
void IRManager::layoutAliasOpnds() 
{
    MemoryManager mm(0x1000, "layoutAliasOpnds");
    uint32 opndCount=getOpndCount();
    AliasRelation * relations=new  (memoryManager) AliasRelation[opndCount];
    getAliasRelations(relations);
    for (uint32 i=0; i<opndCount; i++){
        if (relations[i].outerOpnd!=NULL){
            Opnd * innerOpnd=getOpnd(i);
            assert(innerOpnd->isPlacedIn(OpndKind_Mem));
            assert(relations[i].outerOpnd->isPlacedIn(OpndKind_Mem));
            Opnd * innerDispOpnd=innerOpnd->getMemOpndSubOpnd(MemOpndSubOpndKind_Displacement);
            if (innerDispOpnd==NULL){
                innerDispOpnd=newImmOpnd(typeManager.getInt32Type(), 0);
                innerOpnd->setMemOpndSubOpnd(MemOpndSubOpndKind_Displacement, innerDispOpnd);
            }
            Opnd * outerDispOpnd=relations[i].outerOpnd->getMemOpndSubOpnd(MemOpndSubOpndKind_Displacement);
            uint32 outerDispValue=(uint32)(outerDispOpnd!=NULL?outerDispOpnd->getImmValue():0);
            innerDispOpnd->assignImmValue(outerDispValue+relations[i].offset);
        }
    }
}

//_________________________________________________________________________________________________
uint32 IRManager::assignInnerMemOpnd(Opnd * outerOpnd, Opnd* innerOpnd, uint32 offset)
{
    assert(outerOpnd->isPlacedIn(OpndKind_Memory));

    Opnd * outerDisp=outerOpnd->getMemOpndSubOpnd(MemOpndSubOpndKind_Displacement);
    MemOpndKind outerMemOpndKind=outerOpnd->getMemOpndKind();
    Opnd::RuntimeInfo * outerDispRI=outerDisp!=NULL?outerDisp->getRuntimeInfo():NULL;
    uint64 outerDispValue=outerDisp!=NULL?outerDisp->getImmValue():0;

    Opnd * outerBase=outerOpnd->getMemOpndSubOpnd(MemOpndSubOpndKind_Base);
    Opnd * outerIndex=outerOpnd->getMemOpndSubOpnd(MemOpndSubOpndKind_Index);
    Opnd * outerScale=outerOpnd->getMemOpndSubOpnd(MemOpndSubOpndKind_Scale);

    OpndSize innerSize = innerOpnd->getSize();
    uint32 innerByteSize = getByteSize(innerSize);
        
    Opnd * innerDisp=newImmOpnd(outerDisp!=NULL?outerDisp->getType():typeManager.getInt32Type(), outerDispValue+offset);
    if (outerDispRI){
        Opnd::RuntimeInfo * innerDispRI=new(memoryManager) 
            Opnd::RuntimeInfo(
                outerDispRI->getKind(),
                outerDispRI->getValue(0),
                outerDispRI->getValue(1),
                outerDispRI->getValue(2),
                outerDispRI->getValue(3),
                offset
            );
        innerDisp->setRuntimeInfo(innerDispRI);
    }
    innerOpnd->assignMemLocation(outerMemOpndKind, outerBase, outerIndex, outerScale, innerDisp);
    return innerByteSize;
}

//_________________________________________________________________________________________________
void IRManager::assignInnerMemOpnds(Opnd * outerOpnd, Opnd** innerOpnds, uint32 innerOpndCount)
{
#ifdef _DEBUG
    uint32 outerByteSize = getByteSize(outerOpnd->getSize());
#endif
    for (uint32 i=0, offset=0; i<innerOpndCount; i++){
        offset+=assignInnerMemOpnd(outerOpnd, innerOpnds[i], offset);
        assert(offset<=outerByteSize);
    }
}

//_________________________________________________________________________________________________
uint32 getLayoutOpndAlignment(Opnd * opnd)
{
    OpndSize size=opnd->getSize();
    if (size==OpndSize_80 || size==OpndSize_128)
        return 16;
    if (size==OpndSize_64)
        return 8;
    else
        return 4;
}


//_________________________________________________________________________________________________

Inst * IRManager::newCopySequence(Mnemonic mn, Opnd * opnd0, Opnd * opnd1, uint32 gpRegUsageMask, uint32 flagsRegUsageMask)
{
    if (mn==Mnemonic_MOV)
        return newCopySequence(opnd0, opnd1, gpRegUsageMask, flagsRegUsageMask);
    else if (mn==Mnemonic_PUSH||mn==Mnemonic_POP)
        return newPushPopSequence(mn, opnd0, gpRegUsageMask);
    assert(0);
    return NULL;
}



//_________________________________________________________________________________________________

Inst * IRManager::newMemMovSequence(Opnd * targetOpnd, Opnd * sourceOpnd, uint32 regUsageMask, bool checkSource)
{
    Inst * instList=NULL;
    RegName tmpRegName=RegName_Null, unusedTmpRegName=RegName_Null;
    bool registerSetNotLocked = !isRegisterSetLocked(OpndKind_GPReg);

#ifdef _EM64T_
    for (uint32 reg = RegName_RAX; reg<=RegName_R15/*(uint32)(targetOpnd->getSize()<OpndSize_64?RegName_RBX:RegName_RDI)*/; reg++) {
#else
    for (uint32 reg = RegName_EAX; reg<=(uint32)(targetOpnd->getSize()<OpndSize_32?RegName_EBX:RegName_EDI); reg++) {
#endif
        RegName regName = (RegName) reg;
        if (regName == STACK_REG)
            continue;
        Opnd * subOpnd = targetOpnd->getMemOpndSubOpnd(MemOpndSubOpndKind_Base);
        if(subOpnd && subOpnd->isPlacedIn(regName))
            continue;
        subOpnd = targetOpnd->getMemOpndSubOpnd(MemOpndSubOpndKind_Index);
        if(subOpnd && subOpnd->isPlacedIn(regName))
            continue;

        if (checkSource){
            subOpnd = sourceOpnd->getMemOpndSubOpnd(MemOpndSubOpndKind_Base);
            if(subOpnd && subOpnd->isPlacedIn(regName))
                continue;
            subOpnd = sourceOpnd->getMemOpndSubOpnd(MemOpndSubOpndKind_Index);
            if(subOpnd && subOpnd->isPlacedIn(regName))
                continue;
        }

        tmpRegName=regName;
        if (registerSetNotLocked || (getRegMask(tmpRegName)&regUsageMask)==0){
            unusedTmpRegName=tmpRegName;
            break;
        }
    }

    assert(tmpRegName!=RegName_Null);
    Opnd * tmp=getRegOpnd(tmpRegName);
    Opnd * tmpAdjusted=newRegOpnd(targetOpnd->getType(), tmpRegName);
    Opnd * tmpRegStackOpnd = newMemOpnd(tmp->getType(), MemOpndKind_StackAutoLayout, getRegOpnd(STACK_REG), 0); 


    if (unusedTmpRegName==RegName_Null)
        appendToInstList(instList, newInst(Mnemonic_MOV, tmpRegStackOpnd, tmp));

    appendToInstList(instList, newInst(Mnemonic_MOV, tmpAdjusted, sourceOpnd)); // must satisfy constraints
    appendToInstList(instList, newInst(Mnemonic_MOV, targetOpnd, tmpAdjusted)); // must satisfy constraints

    if (unusedTmpRegName==RegName_Null)
        appendToInstList(instList, newInst(Mnemonic_MOV, tmp, tmpRegStackOpnd));

    return instList;
}



//_________________________________________________________________________________________________

Inst * IRManager::newCopySequence(Opnd * targetBOpnd, Opnd * sourceBOpnd, uint32 regUsageMask, uint32 flagsRegUsageMask)

{ 
    Opnd * targetOpnd=(Opnd*)targetBOpnd, * sourceOpnd=(Opnd*)sourceBOpnd;

    Constraint targetConstraint = targetOpnd->getConstraint(Opnd::ConstraintKind_Location);
    Constraint sourceConstraint = sourceOpnd->getConstraint(Opnd::ConstraintKind_Location);
    
    if (targetConstraint.isNull() || sourceConstraint.isNull()){
        return newCopyPseudoInst(Mnemonic_MOV, targetOpnd, sourceOpnd);
    }

    OpndSize sourceSize=sourceConstraint.getSize();
    uint32 sourceByteSize=getByteSize(sourceSize);
    OpndKind targetKind=(OpndKind)targetConstraint.getKind();
    OpndKind sourceKind=(OpndKind)sourceConstraint.getKind();

#if defined(_DEBUG) || !defined(_EM64T_)
    OpndSize targetSize=targetConstraint.getSize();
    assert(targetSize<=sourceSize); // only same size or truncating conversions are allowed
#endif

    if (targetKind&OpndKind_Reg) {
        if(sourceOpnd->isPlacedIn(OpndKind_Imm) && sourceOpnd->getImmValue()==0 && targetKind==OpndKind_GPReg && 
            !sourceOpnd->getRuntimeInfo() && !(getRegMask(RegName_EFLAGS)&flagsRegUsageMask)) {
            return newInst(Mnemonic_XOR,targetOpnd, targetOpnd);
        }
        else if (targetKind==OpndKind_XMMReg && sourceOpnd->getMemOpndKind()==MemOpndKind_ConstantArea) {
#ifdef _EM64T_
            Opnd * addr = NULL;
            Opnd * base = sourceOpnd->getMemOpndSubOpnd(MemOpndSubOpndKind_Base);
            if(base) {
                Inst * defInst = base->getDefiningInst();
                if(defInst && defInst->getMnemonic() == Mnemonic_MOV) {
                    addr = defInst->getOpnd(1);
                    if(!addr->getRuntimeInfo())
                        addr = NULL;
                }
            }
#else
            Opnd * addr = sourceOpnd->getMemOpndSubOpnd(MemOpndSubOpndKind_Displacement);
#endif
            void * fpPtr = (void *)((ConstantAreaItem *)addr->getRuntimeInfo()->getValue(0))->getValue();
            if( addr && addr->getRuntimeInfo()->getKind()==Opnd::RuntimeInfo::Kind_ConstantAreaItem) {
                if (sourceByteSize==4) {
                    float val = *(float *)fpPtr;
                    if(val == 0 && !signbit(val)) {
                        return newInst(Mnemonic_XORPS,targetOpnd, targetOpnd);
                    }
                }else if (sourceByteSize==8) {
                    double val = *(double *)fpPtr;
                    if(val == 0 && !signbit(val)) {
                        return newInst(Mnemonic_XORPD,targetOpnd, targetOpnd);
                    }
                }
            }
        }
    }

    if ( (targetKind==OpndKind_GPReg||targetKind==OpndKind_Mem) && 
         (sourceKind==OpndKind_GPReg||sourceKind==OpndKind_Mem||sourceKind==OpndKind_Imm)
    ){
        if (sourceKind==OpndKind_Mem && targetKind==OpndKind_Mem){
            Inst * instList=NULL;
#ifndef _EM64T_
            uint32 targetByteSize=getByteSize(targetSize);
            if (sourceByteSize<=4){
                instList=newMemMovSequence(targetOpnd, sourceOpnd, regUsageMask);
            }else{
                Opnd * targetOpnds[IRMaxOperandByteSize/4]; // limitation because we are currently don't support large memory operands
                uint32 targetOpndCount = 0;
                for (uint32 cb=0; cb<sourceByteSize && cb<targetByteSize; cb+=4)
                    targetOpnds[targetOpndCount++] = newOpnd(typeManager.getInt32Type());
                AliasPseudoInst * targetAliasInst=newAliasPseudoInst(targetOpnd, targetOpndCount, targetOpnds);
                layoutAliasPseudoInstOpnds(targetAliasInst);
                for (uint32 cb=0, targetOpndSlotIndex=0; cb<sourceByteSize && cb<targetByteSize; cb+=4, targetOpndSlotIndex++){  
                    Opnd * sourceOpndSlot=newOpnd(typeManager.getInt32Type());
                    appendToInstList(instList, newAliasPseudoInst(sourceOpndSlot, sourceOpnd, cb));
                    Opnd * targetOpndSlot=targetOpnds[targetOpndSlotIndex];
                    appendToInstList(instList, newMemMovSequence(targetOpndSlot, sourceOpndSlot, regUsageMask, true));
                }
                appendToInstList(instList, targetAliasInst);
            }
#else
            instList=newMemMovSequence(targetOpnd, sourceOpnd, regUsageMask);
#endif
            assert(instList!=NULL);
            return instList;
        }else{
#ifdef _EM64T_
            if((targetOpnd->getMemOpndKind() == MemOpndKind_StackAutoLayout) && (sourceKind==OpndKind_Imm) && (sourceOpnd->getSize() == OpndSize_64)) 
                return newMemMovSequence(targetOpnd, sourceOpnd, regUsageMask, false);
            else 
#else
            assert(sourceByteSize<=4);
#endif
                return newInst(Mnemonic_MOV, targetOpnd, sourceOpnd); // must satisfy constraints
        }
    }else if ( 
        (targetKind==OpndKind_XMMReg||targetKind==OpndKind_Mem) && 
        (sourceKind==OpndKind_XMMReg||sourceKind==OpndKind_Mem)
    ){
        if (sourceByteSize==4){
            return newInst(Mnemonic_MOVSS,targetOpnd, sourceOpnd);
        }else if (sourceByteSize==8){
            return newInst(Mnemonic_MOVSD,targetOpnd, sourceOpnd);
        }
    }else if (targetKind==OpndKind_FPReg && sourceKind==OpndKind_Mem){
        return newInst(Mnemonic_FLD, targetOpnd, sourceOpnd);
    }else if (targetKind==OpndKind_Mem && sourceKind==OpndKind_FPReg){
        return newInst(Mnemonic_FSTP, targetOpnd, sourceOpnd);
    }else if (
        (targetKind==OpndKind_FPReg && sourceKind==OpndKind_XMMReg)||
        (targetKind==OpndKind_XMMReg && sourceKind==OpndKind_FPReg)
    ){
        Inst * instList=NULL;
        Opnd * tmp = newMemOpnd(targetOpnd->getType(), MemOpndKind_StackAutoLayout, getRegOpnd(STACK_REG), 0); 
        appendToInstList(instList, newCopySequence(tmp, sourceOpnd, regUsageMask));
        appendToInstList(instList, newCopySequence(targetOpnd, tmp, regUsageMask));
        return instList;
    }
    assert(0);
    return NULL;
}



//_________________________________________________________________________________________________

Inst * IRManager::newPushPopSequence(Mnemonic mn, Opnd * opnd, uint32 regUsageMask)
{
    assert(opnd!=NULL);

    Constraint constraint = opnd->getConstraint(Opnd::ConstraintKind_Location);
    if (constraint.isNull())
        return newCopyPseudoInst(mn, opnd);

    OpndKind kind=(OpndKind)constraint.getKind();
    OpndSize size=constraint.getSize();

    Inst * instList=NULL;

#ifdef _EM64T_
    if ( ((kind==OpndKind_GPReg ||kind==OpndKind_Mem)&& size!=OpndSize_32)||(kind==OpndKind_Imm && size<OpndSize_32)){
            return newInst(mn, opnd);
#else
    if ( kind==OpndKind_GPReg||kind==OpndKind_Mem||kind==OpndKind_Imm ){
        if (size==OpndSize_32){
            return newInst(mn, opnd);
        }else if (size<OpndSize_32){ 
        }else if (size==OpndSize_64){
            if (mn==Mnemonic_PUSH){
                Opnd * opndLo=newOpnd(typeManager.getUInt32Type());
                appendToInstList(instList, newAliasPseudoInst(opndLo, opnd, 0)); 
                Opnd * opndHi=newOpnd(typeManager.getIntPtrType());
                appendToInstList(instList, newAliasPseudoInst(opndHi, opnd, 4)); 
                appendToInstList(instList, newInst(Mnemonic_PUSH, opndHi));
                appendToInstList(instList, newInst(Mnemonic_PUSH, opndLo));
            }else{
                Opnd * opnds[2]={ newOpnd(typeManager.getUInt32Type()), newOpnd(typeManager.getInt32Type()) };
                appendToInstList(instList, newInst(Mnemonic_POP, opnds[0])); 
                appendToInstList(instList, newInst(Mnemonic_POP, opnds[1]));
                appendToInstList(instList, newAliasPseudoInst(opnd, 2, opnds));
            }
            return instList;
        }
#endif
    }
    Opnd * espOpnd=getRegOpnd(STACK_REG);
    Opnd * tmp=newMemOpnd(opnd->getType(), MemOpndKind_StackManualLayout, espOpnd, 0); 
#ifdef _EM64T_
    Opnd * sizeOpnd=newImmOpnd(typeManager.getInt32Type(), sizeof(POINTER_SIZE_INT));
    if(kind==OpndKind_Imm) {
        assert(mn==Mnemonic_PUSH);
        appendToInstList(instList, newInst(Mnemonic_SUB, espOpnd, sizeOpnd));
        appendToInstList(instList, newMemMovSequence(tmp, opnd, regUsageMask));
    } else if (kind == OpndKind_GPReg){
        assert(mn==Mnemonic_PUSH);
        appendToInstList(instList, newInst(Mnemonic_SUB, espOpnd, sizeOpnd));
        appendToInstList(instList, newInst(Mnemonic_MOV, tmp, opnd));
    } else {
        if (mn==Mnemonic_PUSH){
            appendToInstList(instList, newInst(Mnemonic_SUB, espOpnd, sizeOpnd));
            appendToInstList(instList, newCopySequence(tmp, opnd, regUsageMask));
        }else{
            appendToInstList(instList, newCopySequence(opnd, tmp, regUsageMask));
            appendToInstList(instList, newInst(Mnemonic_ADD, espOpnd, sizeOpnd));
        }
    }
#else
    uint32 cb=getByteSize(size);
    uint32 slotSize=4; 
    cb=(cb+slotSize-1)&~(slotSize-1);
    Opnd * sizeOpnd=newImmOpnd(typeManager.getInt32Type(), cb);
    if (mn==Mnemonic_PUSH){
        appendToInstList(instList, newInst(Mnemonic_SUB, espOpnd, sizeOpnd));
        appendToInstList(instList, newCopySequence(tmp, opnd, regUsageMask));
    }else{
        appendToInstList(instList, newCopySequence(opnd, tmp, regUsageMask));
        appendToInstList(instList, newInst(Mnemonic_ADD, espOpnd, sizeOpnd));
    }
#endif
    return instList;
}

//_________________________________________________________________________________________________
const CallingConvention * IRManager::getCallingConvention(CompilationInterface::RuntimeHelperId helperId)const
{
    CompilationInterface::VmCallingConvention callConv=compilationInterface.getRuntimeHelperCallingConvention(helperId);
    switch (callConv){
        case CompilationInterface::CallingConvention_Drl:
            return &CallingConvention_DRL;
        case CompilationInterface::CallingConvention_Stdcall:
            return &CallingConvention_STDCALL;
        case CompilationInterface::CallingConvention_Cdecl:
            return &CallingConvention_CDECL;
        default:
            assert(0);
            return NULL;
    }
}

//_________________________________________________________________________________________________
const CallingConvention * IRManager::getCallingConvention(MethodDesc * methodDesc)const
{
    return &CallingConvention_DRL;
}

//_________________________________________________________________________________________________
Opnd * IRManager::defArg(Type * type, uint32 position)
{
    assert(NULL != entryPointInst);
    Opnd * opnd=newOpnd(type);
    entryPointInst->insertOpnd(position, opnd, Inst::OpndRole_Auxilary|Inst::OpndRole_Def);
    entryPointInst->callingConventionClient.pushInfo(Inst::OpndRole_Def, type->tag);
    return opnd;
}

//_________________________________________________________________________________________________
Opnd * IRManager::getRegOpnd(RegName regName)
{
    assert(getRegSize(regName)==Constraint::getDefaultSize(getRegKind(regName))); // are we going to change this?
    uint32 idx=( (getRegKind(regName) & 0x1f) << 4 ) | ( getRegIndex(regName)&0xf );
    if (!regOpnds[idx]){
#ifdef _EM64T_
        Type * t = (getRegSize(regName) == OpndSize_64 ? typeManager.getUInt64Type() : typeManager.getUInt32Type());
        regOpnds[idx]=newRegOpnd(t, regName);
#else
        regOpnds[idx]=newRegOpnd(typeManager.getUInt32Type(), regName);
#endif
    }
    return regOpnds[idx];
}
void IRManager::calculateTotalRegUsage(OpndKind regKind) {
    assert(regKind == OpndKind_GPReg);
    uint32 opndCount=getOpndCount();
    for (uint32 i=0; i<opndCount; i++){
        Opnd * opnd=getOpnd(i);
        if (opnd->isPlacedIn(regKind))
            gpTotalRegUsage |= getRegMask(opnd->getRegName());
    }
#ifdef _EM64T_
    //FIXME!!
    gpTotalRegUsage |= getRegMask(RegName_R15);
    gpTotalRegUsage |= getRegMask(RegName_R13);
    gpTotalRegUsage |= getRegMask(RegName_R14);
#endif
}
//_________________________________________________________________________________________________
uint32 IRManager::getTotalRegUsage(OpndKind regKind)const {
    return gpTotalRegUsage;
}
//_________________________________________________________________________________________________
bool IRManager::isPreallocatedRegOpnd(Opnd * opnd)
{
    RegName regName=opnd->getRegName();
    if (regName==RegName_Null || getRegSize(regName)!=Constraint::getDefaultSize(getRegKind(regName)))
        return false;
    uint32 idx=( (getRegKind(regName) & 0x1f) << 4 ) | ( getRegIndex(regName)&0xf );
    return regOpnds[idx]==opnd;
}

//_______________________________________________________________________________________________________________
Type * IRManager::getManagedPtrType(Type * sourceType)
{
    return typeManager.getManagedPtrType(sourceType); 
}

//_________________________________________________________________________________________________
Type * IRManager::getTypeFromTag(Type::Tag tag)const
{
    switch (tag) {
        case Type::Void:    
        case Type::Tau:     
        case Type::Int8:    
        case Type::Int16:   
        case Type::Int32:   
        case Type::IntPtr:  
        case Type::Int64:   
        case Type::UInt8:   
        case Type::UInt16:  
        case Type::UInt32:  
        case Type::UInt64:  
        case Type::Single:  
        case Type::Double:  
        case Type::Boolean:  
        case Type::Float:   return typeManager.getPrimitiveType(tag);
        default:            return new(memoryManager) Type(tag);
    }
}

//_____________________________________________________________________________________________
OpndSize IRManager::getTypeSize(Type::Tag tag)
{
    OpndSize size;
    switch (tag) {
        case Type::Int8:
        case Type::UInt8:
        case Type::Boolean:
            size = OpndSize_8;
            break;
        case Type::Int16:   
        case Type::UInt16:
        case Type::Char:
            size = OpndSize_16;
            break;
        case Type::Int32:   
        case Type::UInt32:
            size = OpndSize_32;
            break;
#ifdef _EM64T_
        case Type::IntPtr:   
        case Type::UIntPtr:   
#endif
        case Type::Int64:   
        case Type::UInt64:
            size = OpndSize_64;
            break;
        case Type::Single:
            size = OpndSize_32;
            break;
        case Type::Double:
            size = OpndSize_64;
            break;
        case Type::Float:
            size = OpndSize_80;
            break;
        default:
#ifdef _EM64T_
            size = (tag>=Type::CompressedSystemObject && tag<=Type::CompressedVTablePtr)?OpndSize_32:OpndSize_64;
#else
            size = OpndSize_32;
#endif
            break;
    }
    return size;

}

//_____________________________________________________________________________________________
void IRManager::indexInsts() {
    const Nodes& postOrder = fg->getNodesPostOrder();
    uint32 idx=0;
    for (Nodes::const_reverse_iterator it = postOrder.rbegin(), end = postOrder.rend(); it!=end; ++it) {
        Node* node = *it;
        if (node->isBlockNode()){
            for (Inst* inst = (Inst*)node->getFirstInst(); inst!=NULL; inst = inst->getNextInst()) {
                inst->index=idx++;
            }
        }
    }
}

//_____________________________________________________________________________________________
uint32 IRManager::calculateOpndStatistics(bool reindex)
{
    POpnd * arr=&opnds.front();
    for (uint32 i=0, n=opnds.size(); i<n; i++){
        Opnd * opnd=arr[i];
        if (opnd==NULL) {
            continue;
        }
        opnd->defScope=Opnd::DefScope_Temporary;
        opnd->definingInst=NULL;
        opnd->refCount=0;
        if (reindex) {
            opnd->id=EmptyUint32;
        }
    }

    uint32 index=0;
    uint32 instIdx=0;
    const Nodes& nodes = fg->getNodesPostOrder();
    for (Nodes::const_reverse_iterator  it = nodes.rbegin(), end = nodes.rend(); it!=end; ++it) {
        Node* node = *it;
        if (!node->isBlockNode()) {
            continue;
        }
        int32 execCount=1;//(int32)node->getExecCount()*100;
        for (Inst * inst=(Inst*)node->getFirstInst(); inst!=NULL; inst=inst->getNextInst()){
            inst->index=instIdx++;
            for (uint32 i=0, n=inst->getOpndCount(Inst::OpndRole_InstLevel|Inst::OpndRole_UseDef); i<n; i++){
                Opnd * opnd=inst->getOpnd(i);
                opnd->addRefCount(index, execCount);
                if ((inst->getOpndRoles(i)&Inst::OpndRole_Def)!=0){
                    opnd->setDefiningInst(inst);
                }
            }
        }
    }

    for (uint32 i=0; i<IRMaxRegNames; i++){ // update predefined regOpnds to prevent losing them from the ID space
        if (regOpnds[i]!=NULL) {
            regOpnds[i]->addRefCount(index, 1);
        }
    }
    return index;
}

//_____________________________________________________________________________________________
void IRManager::packOpnds()
{
    static CountTime packOpndsTimer("ia32::packOpnds");
    AutoTimer tm(packOpndsTimer);

    _hasLivenessInfo=false;

    uint32 maxIndex=calculateOpndStatistics(true);

    uint32 opndsBefore=opnds.size();
    opnds.resize(opnds.size()+maxIndex);
    POpnd * arr=&opnds.front();
    for (uint32 i=0; i<opndsBefore; i++){
        Opnd * opnd=arr[i];
        if (opnd->id!=EmptyUint32)
            arr[opndsBefore+opnd->id]=opnd;
    }
    opnds.erase(opnds.begin(), opnds.begin()+opndsBefore);

    _hasLivenessInfo=false;
}

//_____________________________________________________________________________________________
void IRManager::fixLivenessInfo( uint32 * map )
{
    uint32 opndCount = getOpndCount();
    const Nodes& nodes = fg->getNodes();
    for (Nodes::const_iterator it = nodes.begin(), end = nodes.end(); it!=end; ++it) {
        CGNode* node = (CGNode*)*it;
        BitSet * ls = node->getLiveAtEntry();
        ls->resize(opndCount);
    }   
}

//_____________________________________________________________________________________________
void IRManager::calculateLivenessInfo()
{
    static CountTime livenessTimer("ia32::liveness");
    AutoTimer tm(livenessTimer);

    _hasLivenessInfo=false;
    LoopTree* lt = fg->getLoopTree();
    lt->rebuild(false);
    const uint32 opndCount = getOpndCount();    

    const Nodes& nodes = fg->getNodesPostOrder();
    //clean all prev. liveness info
    for (Nodes::const_iterator it = nodes.begin(),end = nodes.end();it!=end; ++it) {
        CGNode* node = (CGNode*)*it;
        node->getLiveAtEntry()->resizeClear(opndCount);
    }
    uint32 loopDepth = lt->getMaxLoopDepth();
    uint32 nIterations = loopDepth + 1;
#ifdef _DEBUG
    nIterations++; //one more extra iteration to prove that nothing changed
#endif 
    BitSet tmpLs(memoryManager, opndCount);
    bool changed = true;
    Node* exitNode = fg->getExitNode();
    for (uint32 iteration=0; iteration < nIterations; iteration++) {
        changed = false;
        for (Nodes::const_iterator it = nodes.begin(),end = nodes.end();it!=end; ++it) {
            CGNode* node = (CGNode*)*it;
            if (node == exitNode) {
#ifndef _EM64T_
                if (!methodDesc.isStatic() 
                    && (methodDesc.isSynchronized() || methodDesc.isMethodClassIsLikelyExceptionType())) 
                {
                    BitSet * exitLs  = node->getLiveAtEntry();
                    EntryPointPseudoInst * entryPointInst = getEntryPointInst();
                    Opnd * thisOpnd = entryPointInst->getOpnd(0);
                    exitLs->setBit(thisOpnd->getId(), true);
                } 
#endif
                continue;
            }
            bool processNode = true;
            if (iteration > 0) {
                uint32 depth = lt->getLoopDepth(node);
                processNode = iteration <= depth;
#ifdef _DEBUG
                processNode = processNode || iteration == nIterations-1; //last iteration will check all blocks
#endif
            }
            if (processNode) {
                getLiveAtExit(node, tmpLs);
                if (node->isBlockNode()){
                    for (Inst * inst=(Inst*)node->getLastInst(); inst!=NULL; inst=inst->getPrevInst()){
                        updateLiveness(inst, tmpLs);
                    }
                }
                BitSet * ls = node->getLiveAtEntry();
                if (iteration == 0 || !ls->isEqual(tmpLs)) {
                    changed = true;
                    ls->copyFrom(tmpLs);
                }
            }
        }
        if (!changed) {
            break;
        }
    }
#ifdef _DEBUG
    assert(!changed);
#endif
    _hasLivenessInfo=true;
}

//_____________________________________________________________________________________________
bool IRManager::ensureLivenessInfoIsValid() {
    return true;
}

//_____________________________________________________________________________________________
void IRManager::getLiveAtExit(const Node * node, BitSet & ls) const
{
    assert(ls.getSetSize()<=getOpndCount());
    const Edges& edges=node->getOutEdges();
    uint32 i=0;
    for (Edges::const_iterator ite = edges.begin(), ende = edges.end(); ite!=ende; ++ite, ++i) {
        Edge* edge = *ite;
        CGNode * succ=(CGNode*)edge->getTargetNode();
        const BitSet * succLs=succ->getLiveAtEntry();
        if (i==0) {
            ls.copyFrom(*succLs);
        } else {
            ls.unionWith(*succLs);
        }
    }
}

//_____________________________________________________________________________________________
void IRManager::updateLiveness(const Inst * inst, BitSet & ls) const 
{
    const Opnd * const * opnds = inst->getOpnds();
    uint32 opndCount = inst->getOpndCount();

    for (uint32 i = 0; i < opndCount; i++){
        const Opnd * opnd = opnds[i];
        uint32 id = opnd->getId();
        if (inst->isLiveRangeEnd(i))
            ls.setBit(id, false);
        else if (inst->isLiveRangeStart(i))
            ls.setBit(id, true);
    }
    for (uint32 i = 0; i < opndCount; i++){
        const Opnd * opnd = opnds[i];
        if (opnd->getMemOpndKind() != MemOpndKind_Null){
            const Opnd * const * subOpnds = opnd->getMemOpndSubOpnds();
            for (uint32 j = 0; j < MemOpndSubOpndKind_Count; j++){
                const Opnd * subOpnd = subOpnds[j];
                if (subOpnd != NULL && subOpnd->isSubjectForLivenessAnalysis())
                    ls.setBit(subOpnd->getId(), true);
            }
        }
    }

}

//_____________________________________________________________________________________________
uint32 IRManager::getRegUsageFromLiveSet(BitSet * ls, OpndKind regKind)const
{
    assert(ls->getSetSize()<=getOpndCount());
    uint32 mask=0;
    BitSet::IterB ib(*ls);
    for (int i = ib.getNext(); i != -1; i = ib.getNext()){
        Opnd * opnd=getOpnd(i);
        if (opnd->isPlacedIn(regKind))
            mask |= getRegMask(opnd->getRegName());
    }
    return mask;
}


//_____________________________________________________________________________________________
void IRManager::getRegUsageAtExit(const Node * node, OpndKind regKind, uint32 & mask)const
{
    assert(node->isBlockNode());
    const Edges& edges=node->getOutEdges();
    mask=0;
    for (Edges::const_iterator ite = edges.begin(), ende = edges.end(); ite!=ende; ++ite) {
        Edge* edge = *ite;
        mask |= getRegUsageAtEntry(edge->getTargetNode(), regKind);
    }
}

//_____________________________________________________________________________________________
void IRManager::updateRegUsage(const Inst * inst, OpndKind regKind, uint32 & mask)const
{
    Inst::Opnds opnds(inst, Inst::OpndRole_All);
    for (Inst::Opnds::iterator it = opnds.begin(); it != opnds.end(); it = opnds.next(it)){
        Opnd * opnd=inst->getOpnd(it);
        if (opnd->isPlacedIn(regKind)){
            uint32 m=getRegMask(opnd->getRegName());
            if (inst->isLiveRangeEnd(it))
                mask &= ~m;
            else if (inst->isLiveRangeStart(it))
                mask |= m;
        }
    }
}

//_____________________________________________________________________________________________
void IRManager::resetOpndConstraints()
{
    for (uint32 i=0, n=getOpndCount(); i<n; i++){
        Opnd * opnd=getOpnd(i);
        opnd->setCalculatedConstraint(opnd->getConstraint(Opnd::ConstraintKind_Initial));
    }
}

void IRManager::finalizeCallSites()
{
#ifdef _EM64T_
    if (!getMethodDesc().isStatic()) {
        entryPointInst->thisOpnd = newMemOpnd(getTypeFromTag(Type::UnmanagedPtr), MemOpndKind_StackAutoLayout, getRegOpnd(STACK_REG), 0); 
        entryPointInst->getBasicBlock()->appendInst(newCopyPseudoInst(Mnemonic_MOV, entryPointInst->thisOpnd, entryPointInst->getOpnd(0)));
    }
#endif

    const Nodes& nodes = fg->getNodes();
    for (Nodes::const_iterator it = nodes.begin(), end = nodes.end(); it!=end; ++it) {
        Node* node = *it;
        if (node->isBlockNode()) {
            for (Inst * inst=(Inst*)node->getLastInst(), * prevInst=NULL; inst!=NULL; inst=prevInst) {
                prevInst=inst->getPrevInst();
                if (inst->getMnemonic() == Mnemonic_CALL) {
                    const CallInst * callInst=(const CallInst*)inst;
                    const StlVector<CallingConventionClient::StackOpndInfo>& stackOpndInfos = 
                        callInst->getCallingConventionClient().getStackOpndInfos(Inst::OpndRole_Use);
                    Inst * instToPrepend=inst;
                    Opnd * const * opnds = callInst->getOpnds();
#ifdef _EM64T_
                    unsigned sz = 0;
                    for (uint32 i=0, n=stackOpndInfos.size(); i<n; i++) {
                        sz += sizeof(POINTER_SIZE_INT); 
                    }
                    if(sz&15) {
                        unsigned corr = 16-(sz&15);
                        node->prependInst(newInst(Mnemonic_SUB, getRegOpnd(STACK_REG), newImmOpnd(typeManager.getInt32Type(), corr)), inst);
                        node->appendInst(newInst(Mnemonic_ADD, getRegOpnd(STACK_REG), newImmOpnd(typeManager.getInt32Type(), corr)), inst);
                    }
                    sz = 0;
#endif
                    for (uint32 i=0, n=stackOpndInfos.size(); i<n; i++) {
#ifdef _EM64T_
                        uint32 index = callInst->getCallingConventionClient().getCallingConvention()->pushLastToFirst()?i:n-1-i;
                        Inst * pushInst=newCopyPseudoInst(Mnemonic_PUSH, opnds[stackOpndInfos[index].opndIndex]);
                        sz+=sizeof(POINTER_SIZE_INT);//getByteSize(opnds[stackOpndInfos[index].opndIndex]->getSize());
#else
                        Inst * pushInst=newCopyPseudoInst(Mnemonic_PUSH, opnds[stackOpndInfos[i].opndIndex]);
#endif
                        pushInst->insertBefore(instToPrepend);
                        instToPrepend=pushInst;
                    }
#ifdef _EM64T_
                    if(sz && !((CallInst *)inst)->getCallingConventionClient().getCallingConvention()->calleeRestoresStack()) {
                        Inst* newIns = newInst(Mnemonic_ADD, getRegOpnd(STACK_REG), newImmOpnd(typeManager.getInt32Type(), sz));
                        newIns->insertAfter(inst);
                    }
#else
                    if(!((CallInst *)inst)->getCallingConventionClient().getCallingConvention()->calleeRestoresStack()) {
                        Inst* newIns = newInst(Mnemonic_ADD, getRegOpnd(RegName_ESP), newImmOpnd(typeManager.getInt32Type(), ((CallInst *)inst)->getArgStackDepth()));
                        newIns->insertAfter(inst);
                    }
#endif
                }
            }
        }
    }
}

//_____________________________________________________________________________________________
void IRManager::calculateStackDepth()
{
    MemoryManager mm(0x100, "calculateStackDepth");
    StlVector<int32> stackDepths(mm, fg->getNodeCount(), -1);
    
    
    const Nodes& nodes = fg->getNodesPostOrder();
    //iterating in topological (reverse postorder) order
    for (Nodes::const_reverse_iterator itn = nodes.rbegin(), endn = nodes.rend(); itn!=endn; ++itn) {
        Node* node = *itn;
        if (node->isBlockNode() || (node->isDispatchNode() && node!=fg->getUnwindNode())) {
            int32 stackDepth=-1;
            const Edges& edges=node->getInEdges();
            for (Edges::const_iterator ite = edges.begin(), ende = edges.end(); ite!=ende; ++ite) {
                Edge* edge = *ite;
                Node * pred=edge->getSourceNode();
                int32 predStackDepth=stackDepths[pred->getDfNum()];
                if (predStackDepth>=0){
                    assert(stackDepth==-1 || stackDepth==predStackDepth);
                    stackDepth=predStackDepth;
                }
            }
            
            if (stackDepth<0) {
                stackDepth=0;
            }
            if (node->isBlockNode()){
                for (Inst * inst=(Inst*)node->getFirstInst(); inst!=NULL; inst=inst->getNextInst()){
                    inst->setStackDepth(stackDepth);

                    Inst::Opnds opnds(inst, Inst::OpndRole_Explicit | Inst::OpndRole_Auxilary | Inst::OpndRole_UseDef);
                    Inst::Opnds::iterator it = opnds.begin();
                    if (it != opnds.end() && inst->getOpnd(it)->isPlacedIn(STACK_REG)) {
                        if (inst->getMnemonic()==Mnemonic_ADD)
                            stackDepth -= (int32)inst->getOpnd(opnds.next(it))->getImmValue();
                        else if (inst->getMnemonic()==Mnemonic_SUB)
                            stackDepth += (int32)inst->getOpnd(opnds.next(it))->getImmValue();
                        else
                            assert(0);
                    }else{
                        if(inst->getMnemonic()==Mnemonic_PUSH) {
                            stackDepth+=getByteSize(inst->getOpnd(it)->getSize());
                        } else if (inst->getMnemonic() == Mnemonic_POP) {
                            stackDepth-=getByteSize(inst->getOpnd(it)->getSize());
                        } else if (inst->getMnemonic() == Mnemonic_CALL && ((CallInst *)inst)->getCallingConventionClient().getCallingConvention()->calleeRestoresStack()) {
                            stackDepth -= ((CallInst *)inst)->getArgStackDepth();
                        }
                    }
                }
                assert(stackDepth>=0);
            }
            stackDepths[node->getDfNum()]=stackDepth;
        }
    }
}

//_____________________________________________________________________________________________
bool IRManager::isOnlyPrologSuccessor(Node * bb) {
    Node * predBB = bb;
    for(; ; ) {
        if(predBB == fg->getEntryNode()) {
            return true;
        }
        if (predBB != bb &&  predBB->getOutDegree() > 1) {
            return false;
        } 
        if (predBB->getInDegree() > 1) {
            return false;
        }
        predBB = predBB->getInEdges().front()->getSourceNode();
    }
}
//_____________________________________________________________________________________________
void IRManager::expandSystemExceptions(uint32 reservedForFlags)
{
    calculateOpndStatistics();
    StlMap<Opnd *, POINTER_SIZE_INT> checkOpnds(getMemoryManager());
    StlVector<Inst *> excInsts(memoryManager);
    const Nodes& nodes = fg->getNodes();
    for (Nodes::const_iterator it = nodes.begin(), end = nodes.end(); it!=end; ++it) {
        Node* node = *it;
        if (node->isBlockNode()){
            Inst * inst=(Inst*)node->getLastInst();
            if (inst && inst->hasKind(Inst::Kind_SystemExceptionCheckPseudoInst)) {
                excInsts.push_back(inst);
                Node* dispatchNode  = node->getExceptionEdgeTarget();
                if (dispatchNode==NULL || dispatchNode==fg->getUnwindNode()) {
                    checkOpnds[inst->getOpnd(0)] = 
                        ((SystemExceptionCheckPseudoInst*)inst)->checksThisOfInlinedMethod() ?
                        (POINTER_SIZE_INT)-1:
                        (POINTER_SIZE_INT)inst;
                }
                
            }

            if(checkOpnds.size() == 0)
                continue;

            for (inst=(Inst*)node->getFirstInst(); inst!=NULL; inst = inst->getNextInst()){
                if (inst->getMnemonic() == Mnemonic_CALL && !inst->hasKind(Inst::Kind_SystemExceptionCheckPseudoInst) && ((CallInst *)inst)->isDirect()) {
                    Opnd::RuntimeInfo * rt = inst->getOpnd(((ControlTransferInst*)inst)->getTargetOpndIndex())->getRuntimeInfo();
                    if(rt->getKind() == Opnd::RuntimeInfo::Kind_MethodDirectAddr &&  !((MethodDesc *)rt->getValue(0))->isStatic()) {
                        Inst::Opnds opnds(inst, Inst::OpndRole_Auxilary | Inst::OpndRole_Use);
                        for (Inst::Opnds::iterator it = opnds.begin(); it != opnds.end(); it = opnds.next(it)){
                            Opnd * opnd = inst->getOpnd(it);
                            if(checkOpnds.find(opnd) != checkOpnds.end()) 
                                checkOpnds[opnd] = (POINTER_SIZE_INT)-1;
                        }
                    }
                } 
            }
        }
    }
    for(StlVector<Inst *>::iterator it = excInsts.begin(); it != excInsts.end(); it++) {
        Inst * lastInst = *it;
        Node* bb = lastInst->getNode();
        switch (((SystemExceptionCheckPseudoInst*)lastInst)->getExceptionId()){
            case CompilationInterface::Exception_NullPointer:
                {
                    Node* oldTarget = bb->getUnconditionalEdge()->getTargetNode(); //must exist
                    Opnd * opnd = lastInst->getOpnd(0);
                    Edge* dispatchEdge =  bb->getExceptionEdge();
                    assert(dispatchEdge!=NULL);
                    Node* dispatchNode= dispatchEdge->getTargetNode();
                    if ((dispatchNode!=fg->getUnwindNode()) ||(checkOpnds[opnd] == (POINTER_SIZE_INT)-1
#ifdef _EM64T_
                          ||!Type::isCompressedReference(opnd->getType()->tag)
#endif
                       )){
                        Node* throwBasicBlock = fg->createBlockNode();
                        throwBasicBlock->appendInst(newRuntimeHelperCallInst(CompilationInterface::Helper_NullPtrException, 0, NULL, NULL));
#ifndef _EM64T_
                        bb->appendInst(newInst(Mnemonic_CMP, opnd, newImmOpnd(opnd->getType(), 0)));
#else
                        bb->appendInst(newInst(Mnemonic_CMP, opnd,newImmOpnd(opnd->getType(), (Type::isCompressedReference(opnd->getType()->tag) || !opnd->getType()->isReference())? 0: (POINTER_SIZE_INT)compilationInterface.getHeapBase())));
#endif
                        bb->appendInst(newBranchInst(Mnemonic_JZ, throwBasicBlock, oldTarget));
                        fg->addEdge(bb, throwBasicBlock, 0);

                        assert(dispatchNode!=NULL);
                        fg->addEdge(throwBasicBlock, dispatchNode, 1);
                        if((checkOpnds[opnd] == (POINTER_SIZE_INT)-1) && (opnd->getDefScope() == Opnd::DefScope_Temporary) && isOnlyPrologSuccessor(bb)) {
                            checkOpnds[opnd] = (POINTER_SIZE_INT)lastInst;
                        }                   
                    } else { //hardware exception handling
                        if (bb->getFirstInst() == lastInst) {
                            fg->removeEdge(dispatchEdge);
                        }
                    }
                    break;
                }
            default:
                assert(0);
        }
        lastInst->unlink();
    }
}
//_____________________________________________________________________________________________
void IRManager::translateToNativeForm()
{
    const Nodes& nodes = fg->getNodes();
    for (Nodes::const_iterator it = nodes.begin(),end = nodes.end();it!=end; ++it) {
        Node* node = *it;
        if (node->isBlockNode()){
            for (Inst * inst=(Inst*)node->getFirstInst(); inst!=NULL; inst=inst->getNextInst()){
                if (inst->getForm()==Inst::Form_Extended) {
                    inst->makeNative(this);
                }
            }
        }
    }
}

//_____________________________________________________________________________________________
void IRManager::eliminateSameOpndMoves()
{
    const Nodes& nodes = fg->getNodes();
    for (Nodes::const_iterator it = nodes.begin(),end = nodes.end();it!=end; ++it) {
        Node* node = *it;
        if (node->isBlockNode()){
            for (Inst * inst=(Inst*)node->getFirstInst(), *nextInst=NULL; inst!=NULL; inst=nextInst){
                nextInst=inst->getNextInst();
                if (inst->getMnemonic()==Mnemonic_MOV && inst->getOpnd(0)==inst->getOpnd(1)) {
                    inst->unlink();
                }
            }
        }
    }
}

//_____________________________________________________________________________________________
void IRManager::resolveRuntimeInfo()
{
    for (uint32 i=0, n=getOpndCount(); i<n; i++){
        Opnd * opnd=getOpnd(i);
        resolveRuntimeInfo(opnd);
    }
}


void IRManager::resolveRuntimeInfo(Opnd* opnd) const {
    if (!opnd->isPlacedIn(OpndKind_Imm))
        return;
    Opnd::RuntimeInfo * info=opnd->getRuntimeInfo();
    if (info==NULL)
        return;
    int64 value=0;
    switch(info->getKind()){
        case Opnd::RuntimeInfo::Kind_HelperAddress: 
            /** The value of the operand is compilationInterface->getRuntimeHelperAddress */
            value=(POINTER_SIZE_INT)compilationInterface.getRuntimeHelperAddress(
                (CompilationInterface::RuntimeHelperId)(POINTER_SIZE_INT)info->getValue(0)
                );
            assert(value!=0);
            break;
        case Opnd::RuntimeInfo::Kind_InternalHelperAddress:
            /** The value of the operand is irManager.getInternalHelperInfo((const char*)[0]).pfn */
            value=(POINTER_SIZE_INT)getInternalHelperInfo((const char*)info->getValue(0))->pfn;
            assert(value!=0);
            break;
        case Opnd::RuntimeInfo::Kind_TypeRuntimeId: 
            /*  The value of the operand is [0]->ObjectType::getRuntimeIdentifier() */
            value=(POINTER_SIZE_INT)((NamedType*)info->getValue(0))->getRuntimeIdentifier();
            break;
        case Opnd::RuntimeInfo::Kind_MethodRuntimeId: 
            value=(POINTER_SIZE_INT)compilationInterface.getRuntimeMethodHandle((MethodDesc*)info->getValue(0));
            break;
        case Opnd::RuntimeInfo::Kind_AllocationHandle: 
            /* The value of the operand is [0]->ObjectType::getAllocationHandle() */
            value=(POINTER_SIZE_INT)((ObjectType*)info->getValue(0))->getAllocationHandle();
            break;
        case Opnd::RuntimeInfo::Kind_StringDescription: 
            /* [0] - Type * - the containing class, [1] - string token */
            assert(0);
            break;
        case Opnd::RuntimeInfo::Kind_Size: 
            /* The value of the operand is [0]->ObjectType::getObjectSize() */
            value=(POINTER_SIZE_INT)((ObjectType*)info->getValue(0))->getObjectSize();
            break;
        case Opnd::RuntimeInfo::Kind_StringAddress:
            /** The value of the operand is the address where the interned version of the string is stored*/
            {
            MethodDesc*  mDesc = (MethodDesc*)info->getValue(0);
            POINTER_SIZE_INT token = (POINTER_SIZE_INT)info->getValue(1);
            value = (POINTER_SIZE_INT) compilationInterface.loadStringObject(mDesc,token);
            }break;
        case Opnd::RuntimeInfo::Kind_StaticFieldAddress:
            /** The value of the operand is [0]->FieldDesc::getAddress() */
            value=(POINTER_SIZE_INT)((FieldDesc*)info->getValue(0))->getAddress();
            break;
        case Opnd::RuntimeInfo::Kind_FieldOffset:
            /** The value of the operand is [0]->FieldDesc::getOffset() */
            value=(POINTER_SIZE_INT)((FieldDesc*)info->getValue(0))->getOffset();
            break;
        case Opnd::RuntimeInfo::Kind_VTableAddrOffset:
            /** The value of the operand is compilationInterface.getVTableOffset(), zero args */
            value=(POINTER_SIZE_INT)compilationInterface.getVTableOffset();
            break;
        case Opnd::RuntimeInfo::Kind_VTableConstantAddr:
            /** The value of the operand is [0]->ObjectType::getVTable() */
            value=(POINTER_SIZE_INT)((ObjectType*)info->getValue(0))->getVTable();
            break;
        case Opnd::RuntimeInfo::Kind_MethodVtableSlotOffset:
            /** The value of the operand is [0]->MethodDesc::getOffset() */
            value=(POINTER_SIZE_INT)((MethodDesc*)info->getValue(0))->getOffset();
            break;
        case Opnd::RuntimeInfo::Kind_MethodIndirectAddr:
            /** The value of the operand is [0]->MethodDesc::getIndirectAddress() */
            value=(POINTER_SIZE_INT)((MethodDesc*)info->getValue(0))->getIndirectAddress();
            break;
        case Opnd::RuntimeInfo::Kind_MethodDirectAddr:
            /** The value of the operand is *[0]->MethodDesc::getIndirectAddress() */
            value=*(POINTER_SIZE_INT*)((MethodDesc*)info->getValue(0))->getIndirectAddress();
            break;
        case Opnd::RuntimeInfo::Kind_ConstantAreaItem:
            /** The value of the operand is address of constant pool item  ((ConstantPoolItem*)[0])->getAddress() */
            value=(POINTER_SIZE_INT)((ConstantAreaItem*)info->getValue(0))->getAddress();
            break;
        default:
            assert(0);
    }
    opnd->assignImmValue(value+info->getAdditionalOffset());
}

//_____________________________________________________________________________________________
bool IRManager::verify()
{
    updateLivenessInfo();
    if (!verifyLiveness())
        return false;
    if (!verifyHeapAddressTypes())
        return false;
    

#ifdef _DEBUG
    //check unwind node;    
    Node* unwind = fg->getUnwindNode();
    Node* exit = fg->getExitNode();
    assert(exit!=NULL);
    assert(unwind == NULL || unwind->getOutDegree() == 1 && unwind->isConnectedTo(true, exit));
    const Edges& exitInEdges = exit->getInEdges();
    for (Edges::const_iterator ite = exitInEdges.begin(), ende = exitInEdges.end(); ite!=ende; ++ite) {
        Edge* edge = *ite;
        Node* source = edge->getSourceNode();
        assert(source == unwind || source->isBlockNode());
    }
    const Nodes& nodes = fg->getNodesPostOrder();//check only reachable nodes.
    for (Nodes::const_iterator it = nodes.begin(), end = nodes.end(); it!=end; ++it) {
        CGNode* node = (CGNode*)*it;
        node->verify();
    }
#endif
    return true;
}

//_____________________________________________________________________________________________
bool IRManager::verifyLiveness()
{
    Node* prolog=fg->getEntryNode();
    bool failed=false;
    BitSet * ls=getLiveAtEntry(prolog);
    assert(ls!=NULL);
    Constraint calleeSaveRegs=getCallingConvention()->getCalleeSavedRegs(OpndKind_GPReg);

    BitSet::IterB lives(*ls);
    for (int i = lives.getNext(); i != -1; i = lives.getNext()){
        Opnd * opnd=getOpnd(i);
        assert(opnd!=NULL);
        if (
            opnd->isSubjectForLivenessAnalysis() && 
            (opnd->isPlacedIn(RegName_EFLAGS)||!isPreallocatedRegOpnd(opnd))
        ){
            VERIFY_OUT("Operand live at entry: " << opnd << ::std::endl);
            VERIFY_OUT("This means there is a use of the operand when it is not yet defined" << ::std::endl);
            failed=true;
        };
    }
    if (failed)
        VERIFY_OUT(::std::endl << "Liveness verification failure" << ::std::endl);
    return !failed;
}

//_____________________________________________________________________________________________
bool IRManager::verifyHeapAddressTypes()
{
    bool failed=false;
    for (uint32 i=0, n=getOpndCount(); i<n; i++){
        Opnd * opnd = getOpnd(i);
        if (opnd->isPlacedIn(OpndKind_Mem) && opnd->getMemOpndKind()==MemOpndKind_Heap){
            Opnd * properTypeSubOpnd=NULL;
            for (uint32 j=0; j<MemOpndSubOpndKind_Count; j++){
                Opnd * subOpnd=opnd->getMemOpndSubOpnd((MemOpndSubOpndKind)j);
                if (subOpnd!=NULL){
                    Type * type=subOpnd->getType();
                    if (type->isManagedPtr()||type->isObject()||type->isMethodPtr()||type->isVTablePtr()||type->isUnmanagedPtr()){
                        if (properTypeSubOpnd!=NULL){
                            VERIFY_OUT("Heap operand " << opnd << " contains more than 1 sub-operands of type Object or ManagedPointer "<<::std::endl);
                            VERIFY_OUT("Opnd 1: " << properTypeSubOpnd << ::std::endl);
                            VERIFY_OUT("Opnd 2: " << subOpnd << ::std::endl);
                            failed=true;
                            break;
                        }
                        properTypeSubOpnd=subOpnd;
                    }
                }
            }
            if (failed)
                break;
            if (properTypeSubOpnd==NULL){
                VERIFY_OUT("Heap operand " << opnd << " contains no sub-operands of type Object or ManagedPointer "<<::std::endl);
                failed=true;
                break;
            }
        }
    }
    if (failed)
        VERIFY_OUT(::std::endl << "Heap address type verification failure" << ::std::endl);
    return !failed;
}

ControlFlowGraph* IRManager::createSubCFG(bool withReturn, bool withUnwind) {
    ControlFlowGraph* cfg = new (memoryManager) ControlFlowGraph(memoryManager, this);
    cfg->setEntryNode(cfg->createBlockNode());
    cfg->setExitNode(cfg->createExitNode());
    if (withReturn) {
        cfg->setReturnNode(cfg->createBlockNode());
        cfg->addEdge(cfg->getReturnNode(), cfg->getExitNode(), 1);
    }
    if (withUnwind) {
        cfg->setUnwindNode(cfg->createDispatchNode());
        cfg->addEdge(cfg->getUnwindNode(), cfg->getExitNode(), 1);
    }
    return cfg;
}


// factory method for ControlFlowGraph
Node* IRManager::createNode(MemoryManager& mm, Node::Kind kind) {
    if (kind == Node::Kind_Block) {
        return new (mm) BasicBlock(mm, *this);
    } 
    return new (mm) CGNode(mm, *this, kind);
}

// factory method for ControlFlowGraph
Edge* IRManager::createEdge(MemoryManager& mm, Node::Kind srcKind, Node::Kind dstKind) {
    if (srcKind == Node::Kind_Dispatch && dstKind == Node::Kind_Block) {
        return new (mm) CatchEdge();
    }
    return ControlFlowGraphFactory::createEdge(mm, srcKind, dstKind);
}



//=============================================================================================
// class Ia32::SessionAction implementation
//=============================================================================================

void SessionAction::run() 
{
    irManager = &getIRManager();

    stageId=Log::getStageId();

    if (isLogEnabled(LogStream::IRDUMP)) 
        Log::printStageBegin(log(LogStream::IRDUMP).out(), stageId, "IA32", getName(), getTagName());

    uint32 needInfo=getNeedInfo();
    if (needInfo & NeedInfo_LivenessInfo)
        irManager->updateLivenessInfo();
    if (needInfo & NeedInfo_LoopInfo)
        irManager->updateLoopInfo();

    runImpl();

    uint32 sideEffects=getSideEffects();
    if (sideEffects & SideEffect_InvalidatesLivenessInfo)
        irManager->invalidateLivenessInfo();
    if (sideEffects & SideEffect_InvalidatesLoopInfo)
        irManager->invalidateLoopInfo();

    debugOutput("after");

    if (!verify())
        crash("\nVerification failure after %s\n", getName());

    if (isLogEnabled(LogStream::IRDUMP)) 
        Log::printStageEnd(log(LogStream::IRDUMP).out(), stageId, "IA32", getName(), getTagName());
}


uint32 SessionAction::getSideEffects()const
{
    return ~(uint32)0;
}

uint32 SessionAction::getNeedInfo()const
{
    return ~(uint32)0;
}

bool SessionAction::verify(bool force)
{   
    if (force || getVerificationLevel()>=2)
        return getIRManager().verify();
    return true;
}   

void SessionAction::dumpIR(const char * subKind1, const char * subKind2)
{
    Ia32::dumpIR(irManager, stageId, "IA32 LIR CFG after ", getName(), getTagName(), 
        subKind1, subKind2);
}

void SessionAction::printDot(const char * subKind1, const char * subKind2)
{
    Ia32::printDot(irManager, stageId, "IA32 LIR CFG after ", getName(), getTagName(), 
        subKind1, subKind2);
}

void SessionAction::debugOutput(const char * subKind)
{
    if (!isIRDumpEnabled())
        return;

    if (isLogEnabled(LogStream::IRDUMP)) {
        irManager->updateLoopInfo();
        irManager->updateLivenessInfo();
        dumpIR(subKind, "opnds");
        dumpIR(subKind, "liveness");
        dumpIR(subKind);
    }

    if (isLogEnabled(LogStream::DOTDUMP)) {
        irManager->updateLoopInfo();
        irManager->updateLivenessInfo();
        printDot(subKind);
        printDot(subKind, "liveness");
    }
}


} //namespace Ia32
} //namespace Jitrino

