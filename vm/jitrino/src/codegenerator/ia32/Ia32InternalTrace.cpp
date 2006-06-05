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
 * @version $Revision: 1.8.22.3 $
 */

#include "Ia32InternalTrace.h"
#include "Log.h"
#include "jit_export.h"
#include "Ia32Printer.h"
#include "../../vm/drl/DrlVMInterface.h"
#include "Ia32GCMap.h"

namespace Jitrino
{
namespace Ia32{

//========================================================================================
// class InternalTrace
//========================================================================================

//_________________________________________________________________________________________________
static inline void m_assert(bool cond)  {
#ifdef _DEBUG
        assert(cond);
#else
#ifdef WIN32
        if (!cond) {
            __asm {
                int 3;
            }
        }
#endif
#endif    
    }

static MemoryManager mm(0x100, "printRuntimeOpndInternalHelper");
static DrlVMTypeManager *tm = NULL;
void __stdcall methodEntry(const char * methodName, uint32 argInfoCount, CallingConvention::OpndInfo * argInfos)
{

    JitFrameContext context;
	context.esp=(uint32)(&methodName+4); // must point to the beginning of incoming stack args
	::std::ostream & os=Log::cat_rt()->out(); 
	os<<"__METHOD_ENTRY__:"<<methodName<<::std::endl;
	os<<"\t(";
	printRuntimeArgs(os, argInfoCount, argInfos, &context);
	os<<")"<<::std::endl;

	if (tm == NULL) {
        tm = new (mm) DrlVMTypeManager(mm); tm->init();
    }
    for (uint32 i=0; i<argInfoCount; i++){
        CallingConvention::OpndInfo & info=argInfos[i];
        uint32 cb=0;
        uint8 arg[4*sizeof(uint32)]; 
        for (uint32 j=0; j<info.slotCount; j++){
            if (getRegKind(info.slots[j])==OpndKind_Mem){
                *(uint32*)(arg+cb)=((uint32*)context.esp)[getRegIndex(info.slots[j])];
                cb+=sizeof(uint32);
            }else{
                m_assert((getRegKind(info.slots[j])&OpndKind_Reg)!=0);
                m_assert(0); 			        
            }	
        }
        if (Type::isObject((Type::Tag)info.typeTag)){
            GCMap::checkObject(*tm, *(const void**)(const void*)arg);
        }
    }
}

//____________________________ _____________________________________________________________________
void __stdcall methodExit(const char * methodName)
{
	::std::ostream & os=Log::cat_rt()->out();
	os<<"__METHOD_EXIT__:"<<methodName<<::std::endl;
}

//_________________________________________________________________________________________________
void __stdcall fieldWrite(const void * address)
{	
	::std::ostream & os=Log::cat_rt()->out();
	os<<"__FIELD_WRITE__:"<<address<<" at "<<*(void**)(&address-1)<<::std::endl;
	DrlVMDataInterface dataInterface;
	void * heapBase=dataInterface.getHeapBase();
	void * heapCeiling=dataInterface.getHeapCeiling();
	if (address<heapBase || address>=heapCeiling){
		os<<"PROBABLY STATIC OR INVALID ADDRESS. DYNAMIC ADDRESSES MUST BE IN ["<<heapBase<<","<<heapCeiling<<")"<<::std::endl;
	}
}	

//_________________________________________________________________________________________________
void InternalTrace::runImpl()
{
	MemoryManager mm(0x400, "InternalTrace");

	irManager.registerInternalHelperInfo("itrace_method_entry", IRManager::InternalHelperInfo((void*)&methodEntry, &CallingConvention_STDCALL));
	irManager.registerInternalHelperInfo("itrace_method_exit", IRManager::InternalHelperInfo((void*)&methodExit, &CallingConvention_STDCALL));
	irManager.registerInternalHelperInfo("itrace_field_write", IRManager::InternalHelperInfo((void*)&fieldWrite, &CallingConvention_STDCALL));

	char methodFullName[0x1000]="";
	MethodDesc & md=irManager.getMethodDesc();
	strcat(methodFullName, md.getParentType()->getName());
	strcat(methodFullName, ".");
	strcat(methodFullName, md.getName());
	strcat(methodFullName, " ");
	strcat(methodFullName, md.getSignatureString());

	Opnd * methodNameOpnd=irManager.newInternalStringConstantImmOpnd(methodFullName);
	irManager.setInfo("itraceMethodExitString", methodNameOpnd->getRuntimeInfo()->getValue(0));

	BasicBlock * prolog=irManager.getPrologNode();

	Inst * inst=prolog->getInsts().getFirst();
	if (inst->hasKind(Inst::Kind_EntryPointPseudoInst)){
		EntryPointPseudoInst * entryPointPseudoInst = (EntryPointPseudoInst *)inst;
		entryPointPseudoInst->getCallingConventionClient().finalizeInfos(Inst::OpndRole_Def, CallingConvention::ArgKind_InArg);
		const StlVector<CallingConvention::OpndInfo> & infos=((const EntryPointPseudoInst *)entryPointPseudoInst)->getCallingConventionClient().getInfos(Inst::OpndRole_Def);
		Opnd * argInfoOpnd=irManager.newBinaryConstantImmOpnd(infos.size()*sizeof(CallingConvention::OpndInfo), &infos.front());
		Opnd * args[3]={ methodNameOpnd, 
			irManager.newImmOpnd(irManager.getTypeManager().getInt32Type(), infos.size()), 
			argInfoOpnd,
		};
		Inst * internalTraceInst=irManager.newInternalRuntimeHelperCallInst("itrace_method_entry", 3, args, NULL);
		prolog->prependInsts(internalTraceInst, inst);
	}else{
		Opnd * args[3]={ methodNameOpnd, 
			irManager.newImmOpnd(irManager.getTypeManager().getInt32Type(), 0), 
			irManager.newImmOpnd(irManager.getTypeManager().getIntPtrType(), 0),
		};
		Inst * internalTraceInst=irManager.newInternalRuntimeHelperCallInst("itrace_method_entry", 3, args, NULL);
		prolog->prependInsts(internalTraceInst);
	}

	const Edges& inEdges = irManager.getExitNode()->getEdges(Direction_In);
    for (Edge * edge = inEdges.getFirst(); edge != NULL; edge = inEdges.getNext(edge)) {//check predecessors for epilog(s)
		if (irManager.isEpilog(edge->getNode(Direction_Tail))) {
			BasicBlock * epilog = (BasicBlock*)edge->getNode(Direction_Tail);
			Inst * retInst=epilog->getInsts().getLast();
			assert(retInst->hasKind(Inst::Kind_RetInst));
			Opnd * args[1]={ methodNameOpnd };
			Inst * internalTraceInst=irManager.newInternalRuntimeHelperCallInst("itrace_method_exit", 1, args, NULL);
			epilog->prependInsts(internalTraceInst, retInst);
		}
	}

#ifdef IA32_EXTENDED_TRACE
	for (CFG::NodeIterator it(irManager); it!=NULL; ++it){
		if (((Node*)it)->hasKind(Node::Kind_BasicBlock)){
			BasicBlock * bb=(BasicBlock*)(Node*)it;
			const Insts& insts=bb->getInsts();
			for (Inst * inst=insts.getFirst(); inst!=NULL; inst=insts.getNext(inst)){
				if (inst->getMnemonic()==Mnemonic_MOV){
					Opnd * dest=inst->getOpnd(0);
					if (dest->isPlacedIn(OpndKind_Memory) && dest->getMemOpndKind()==MemOpndKind_Heap){
						Opnd * opnd=irManager.newOpnd(Type::IntPtr);
						bb->prependInsts(irManager.newInst(Mnemonic_LEA, opnd, dest), inst);
						Opnd * args[1]={ opnd };
						bb->prependInsts(irManager.newInternalRuntimeHelperCallInst("itrace_field_write", 1, args, NULL), inst);
					}
				}
			}
		}
	}
#endif
}

}}; //namespace Ia32

