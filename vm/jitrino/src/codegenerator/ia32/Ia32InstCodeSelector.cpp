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
 * @author Intel, Vyacheslav P. Shakin, Nikolay A. Sidelnikov
 * @version $Revision: 1.23.8.2.4.4 $
 */

#ifdef VM_POSIX
#include "platform.h"
#endif
#include "Log.h"
#include "Ia32InstCodeSelector.h"
#include "Ia32CodeGenerator.h"
#include "Ia32Printer.h"
#include <float.h>
#include <math.h>

#ifdef PLATFORM_POSIX
#define _isnan isnan
#define _finite finite
#endif


namespace Jitrino
{
namespace Ia32{

#ifdef _DEBUG
#define ICS_ASSERT(a) assert(a)
#else
#define ICS_ASSERT(a) 
#endif

//For operating with EFLAGS
#define ZF 0x4000
#define UNORD_FLAGS_MASK 0x4500


ConditionMnemonic getConditionMnemonicFromCompareOperator(CompareOp::Operators cmpOp, CompareOp::Types opType)
{
	switch(cmpOp){
		case CompareOp::Eq:             // equal        
			return ConditionMnemonic_E;
        case CompareOp::Ne:             // int: not equal;                         // fp: not equal or unordered
			return ConditionMnemonic_NE;
		case CompareOp::Gt:             // greater than
			return opType==CompareOp::F||opType==CompareOp::D||opType==CompareOp::S?ConditionMnemonic_A:ConditionMnemonic_G;
		case CompareOp::Gtu:            // int: greater than unsigned;                         // fp: greater than or unordered
			return ConditionMnemonic_A;
		case CompareOp::Ge:             // greater than or equal
			return opType==CompareOp::F||opType==CompareOp::D||opType==CompareOp::S?ConditionMnemonic_AE:ConditionMnemonic_GE;
        case CompareOp::Geu:             // int: greater than or equal unsigned; 
			return ConditionMnemonic_AE;
		default:
			assert(0);

	}
	return ConditionMnemonic_E;
}

CompareOp::Types getCompareOpTypesFromCompareZeroOpTypes(CompareZeroOp::Types t)
{
	switch (t){
		case CompareZeroOp::I4:			return CompareOp::I4;
		case CompareZeroOp::I8:			return CompareOp::I8;
		case CompareZeroOp::I:			return CompareOp::I;
		case CompareZeroOp::Ref:		return CompareOp::Ref;
		case CompareZeroOp::CompRef:	return CompareOp::CompRef;
	}
	assert(0);
	return CompareOp::I4;
}

bool swapIfLastIs(Opnd *& opnd0, Opnd *& opnd1, Constraint c=OpndKind_Imm)
{
	if (opnd1!=NULL && opnd0->isPlacedIn(c)){
		Opnd * opnd=opnd0;
		opnd0=opnd1; opnd1=opnd;
		return true;
	}
	return false;
}

///////////////////////////////////////////////////////////////////////////////////
//
//                     class InstCodeSelector
//
///////////////////////////////////////////////////////////////////////////////////


//_______________________________________________________________________________________________________________
// FP conversion internal helpers (temp solution to be optimized)
unsigned long dNaNMask[2]={0xffffffff, 0x7fffffff};
double dnan = *( double* )dNaNMask;

unsigned long fNaNMask[1]={0x7fffffff};
float fnan = *( float* )dNaNMask;

double	__stdcall	convF4F8	(float v) stdcall__;
double	__stdcall	convF4F8	(float v) {	return (double)v;	}

float	__stdcall	convF8F4	(double v) stdcall__;
float	__stdcall	convF8F4	(double v) {	return (float)v;	}

double	__stdcall	convI4F8	(uint32 v) stdcall__;
double	__stdcall	convI4F8	(uint32 v) {
	return (double)(int32)v;	}

float	__stdcall	convI4F4	(uint32 v) stdcall__;
float	__stdcall	convI4F4	(uint32 v) {	return (float)(int32)v;	}

double	__stdcall	convI8F8	(uint64 v) stdcall__;
double	__stdcall	convI8F8	(uint64 v) {	return (double)(int64)v;	}

float	__stdcall	convI8F4	(uint64 v) stdcall__;
float	__stdcall	convI8F4	(uint64 v) {	return (float)(int64)v;	} 

// FP remainder internal helpers (temp solution to be optimized)
float	__stdcall	remF4	(float v0, float v1)stdcall__;
float	__stdcall	remF4	(float v0, float v1)	{ 
	if(_isnan(v0) || _isnan(v1)|| (!_finite(v0)) || (v1==float(0.0))) {
		return fnan;
	} else if (!_finite(v1)){
		return v0;
	} else {
		return v0-float(int64(v0/v1))*v1;	
	}
}

double	__stdcall	remF8	(double v0, double v1)stdcall__;
double	__stdcall	remF8	(double v0, double v1)	{	
	if(_isnan(v0) || _isnan(v1) || (!_finite(v0)) || (v1==double(0.0))) {
		return dnan;
	} else if (!_finite(v1)){
		return v0;
	} else {
		return v0-double(int64(v0/v1))*v1;	
	}
} 

void __stdcall initialize_array(uint8* array, uint32 elems_offset, uint8* data, uint32 num_elems) stdcall__;
void __stdcall initialize_array(uint8* array, uint32 elems_offset, uint8* data, uint32 num_elems) {
    uint8* array_data = array + elems_offset;
    for (uint32 i = 0; i < num_elems; i++) {
        array_data[i] = data[i];
    }
}

//_______________________________________________________________________________________________________________
uint32 InstCodeSelector::_tauUnsafe;

//_______________________________________________________________________________________________________________
void InstCodeSelector::onCFGInit(IRManager& irManager)
{
// FP conversion internal helpers (temp solution to be optimized)
	irManager.registerInternalHelperInfo("convF4F8", IRManager::InternalHelperInfo((void*)&convF4F8,&CallingConvention_STDCALL));
	irManager.registerInternalHelperInfo("convF8F4", IRManager::InternalHelperInfo((void*)&convF8F4,&CallingConvention_STDCALL));
	irManager.registerInternalHelperInfo("convI4F8", IRManager::InternalHelperInfo((void*)&convI4F8,&CallingConvention_STDCALL));
	irManager.registerInternalHelperInfo("convI4F4", IRManager::InternalHelperInfo((void*)&convI4F4,&CallingConvention_STDCALL));
	irManager.registerInternalHelperInfo("convI8F8", IRManager::InternalHelperInfo((void*)&convI8F8,&CallingConvention_STDCALL));
	irManager.registerInternalHelperInfo("convI8F4", IRManager::InternalHelperInfo((void*)&convI8F4,&CallingConvention_STDCALL));

// FP remainder internal helpers (temp solution to be optimized)
	irManager.registerInternalHelperInfo("remF8", IRManager::InternalHelperInfo((void*)&remF8,&CallingConvention_STDCALL));
	irManager.registerInternalHelperInfo("remF4", IRManager::InternalHelperInfo((void*)&remF4,&CallingConvention_STDCALL));

    irManager.registerInternalHelperInfo("initialize_array", IRManager::InternalHelperInfo((void*)&initialize_array,&CallingConvention_STDCALL));
}

//_______________________________________________________________________________________________________________
//  Constructor

InstCodeSelector::
InstCodeSelector(CompilationInterface&			compIntfc,
                    CfgCodeSelector&			codeSel,
                    IRManager&					irM,
					BasicBlock *				currBasicBlock
					) 
: compilationInterface(compIntfc), codeSelector(codeSel), 	
  irManager(irM), typeManager(irM.getTypeManager()), 
  memManager(0x2000, "InstCodeSelector"),
  currentBasicBlock(currBasicBlock),
  inArgPos(0),
  seenReturn(false),               switchSrcOpnd(NULL), 
  switchNumTargets(0), currPersistentId(),
  currPredOpnd(NULL) 
{
#ifdef _DEBUG
    nextInArg = 0;
#endif
}

//_______________________________________________________________________________________________________________
Inst * InstCodeSelector::appendInsts(Inst *inst)
{
	assert(currentBasicBlock);
    if (compilationInterface.isBCMapInfoRequired()) {
        uint64 bcOffset = codeSelector.bc2HIRmapHandler->getVectorEntry(currentHIRInstrID);
        //POINTER_SIZE_INT instAddr = (POINTER_SIZE_INT) inst;
        uint64 insID = inst->getId();
        if (bcOffset != ILLEGAL_VALUE) codeSelector.bc2LIRmapHandler->setVectorEntry(insID, bcOffset);
    }
	if (Log::cat_cg()->isIREnabled()){
		Inst * i=inst; 
		do {
			Log::out()<<"\tIA32 inst: ";
			IRPrinter::printInst(Log::out(), i); 
			Log::out()<<::std::endl;
			i=i->getNext();
		}while (i!=inst);
	}

	currentBasicBlock->appendInsts(inst);

	return inst;
}

//_______________________________________________________________________________________________________________
//    Mark operand as global temporary

void InstCodeSelector::opndMaybeGlobal(CG_OpndHandle* opnd) 
{
}

//_______________________________________________________________________________________________________________
void InstCodeSelector::copyOpndTrivialOrTruncatingConversion(Opnd *dst, Opnd *src) 
{ 
	assert(dst->getSize()<=src->getSize());
	if (src->getType()->isInteger()&&src->getSize()==OpndSize_64)
		appendInsts(irManager.newI8PseudoInst(Mnemonic_MOV, 1, dst, src));
	else
		appendInsts(irManager.newCopyPseudoInst(Mnemonic_MOV, dst, src));
}

//_______________________________________________________________________________________________________________
//    Copy any kind of the operand
void InstCodeSelector::copyOpnd(Opnd *dst, Opnd *src) 
{ 
	convert(src, dst->getType(), dst);
}


//_______________________________________________________________________________________________________________
void InstCodeSelector::throwLinkingException(Class_Handle encClass, uint32 cp_ndx, uint32 opcode)
{  
	Opnd* encClassArg = irManager.newImmOpnd(getRuntimeIdType(), (POINTER_SIZE_INT)encClass);
 	Opnd* cpIndexArg = irManager.newImmOpnd(typeManager.getUInt32Type(),cp_ndx);
    Opnd* opcodeArg = irManager.newImmOpnd(typeManager.getUInt32Type(), opcode);

	Opnd* args[] = {encClassArg, cpIndexArg, opcodeArg};
	appendInsts(irManager.newRuntimeHelperCallInst(CompilationInterface::Helper_Throw_LinkingException, lengthof(args), args, NULL));
}
//_______________________________________________________________________________________________________________
Opnd * InstCodeSelector::convertIntToInt(Opnd * srcOpnd, Type * dstType, Opnd * dstOpnd)
{
	Type * srcType=srcOpnd->getType();
	assert(isIntegerType(srcType) && isIntegerType(dstType));

	OpndSize srcSize=irManager.getTypeSize(srcType);
    OpndSize dstSize=irManager.getTypeSize(dstType);
	
	if (dstSize==srcSize){ // trivial conversion without changing type
		if (dstOpnd==NULL)
			dstOpnd=srcOpnd;
		else
			copyOpndTrivialOrTruncatingConversion(dstOpnd, srcOpnd);
	}else if (dstSize<=srcSize){ // truncating conversion 
		if (dstOpnd==NULL)
			dstOpnd=irManager.newOpnd(dstType);
		copyOpndTrivialOrTruncatingConversion(dstOpnd, srcOpnd);
	}else{
		if (dstOpnd==NULL)
			dstOpnd=irManager.newOpnd(dstType);
		if (dstSize<OpndSize_64){
			appendInsts(irManager.newInstEx(srcType->isSignedInteger()?Mnemonic_MOVSX:Mnemonic_MOVZX, 1, dstOpnd, srcOpnd));
		}else{
			appendInsts(irManager.newI8PseudoInst(srcType->isSignedInteger()?Mnemonic_MOVSX:Mnemonic_MOVZX, 1, dstOpnd, srcOpnd));
		}
	}

	return dstOpnd;
}

//_______________________________________________________________________________________________________________
Opnd * InstCodeSelector::convertIntToFp(Opnd * srcOpnd, Type * dstType, Opnd * dstOpnd)
{
	assert(srcOpnd->getType()->isInteger() && dstType->isFP());
	OpndSize srcSize=srcOpnd->getSize();
	if (dstOpnd==NULL)
		dstOpnd=irManager.newOpnd(dstType);
	const char * helperName;
	if (srcSize<=OpndSize_32){
		if (srcSize<OpndSize_32)
			srcOpnd=convert(srcOpnd, typeManager.getInt32Type());
		helperName=dstType->isSingle()?"convI4F4":"convI4F8";
	}else{
		assert(srcSize==OpndSize_64);
		helperName=dstType->isSingle()?"convI8F4":"convI8F8";
	}
	Opnd * args[] = {srcOpnd};
	appendInsts(irManager.newInternalRuntimeHelperCallInst(helperName, 1, args, dstOpnd));
	return dstOpnd;
}

//_______________________________________________________________________________________________________________
Opnd * InstCodeSelector::convertFpToInt(Opnd * srcOpnd, Type * dstType, Opnd * dstOpnd)
{
	assert(srcOpnd->getType()->isFP() && dstType->isInteger());
	CompilationInterface::RuntimeHelperId helperId;
	OpndSize dstSize=irManager.getTypeSize(dstType);
	if (dstSize<=OpndSize_32){
		if (dstOpnd==NULL)
			dstOpnd=irManager.newOpnd(typeManager.getInt32Type());
		helperId=srcOpnd->getType()->isSingle()?CompilationInterface::Helper_ConvStoI32:CompilationInterface::Helper_ConvDtoI32;
	}else{
		assert(dstSize==OpndSize_64);
		if (dstOpnd==NULL)
			dstOpnd=irManager.newOpnd(dstType);
		helperId=srcOpnd->getType()->isSingle()?CompilationInterface::Helper_ConvStoI64:CompilationInterface::Helper_ConvDtoI64;
	}

	Opnd * args[] = {srcOpnd};
	appendInsts(irManager.newRuntimeHelperCallInst(helperId, 1, args, dstOpnd));

	if (dstSize<OpndSize_32)
		dstOpnd=convert(dstOpnd, dstType);

	return dstOpnd;
}

//_______________________________________________________________________________________________________________
Opnd * InstCodeSelector::convertFpToFp(Opnd * srcOpnd, Type * dstType, Opnd * dstOpnd)
{
	Type * srcType=srcOpnd->getType();
	assert(srcType->isFP() && dstType->isFP());
	if ((srcType->isSingle() && dstType->isSingle())||!(srcType->isSingle() || dstType->isSingle())){
		if (!dstOpnd)
			return srcOpnd;
		copyOpndTrivialOrTruncatingConversion(dstOpnd, srcOpnd);
		return dstOpnd;
	}
	if (dstOpnd==NULL)
		dstOpnd=irManager.newOpnd(dstType);
	const char * helperName=dstType->isSingle()?"convF8F4":"convF4F8";
	Opnd * args[] = {srcOpnd};
	appendInsts(irManager.newInternalRuntimeHelperCallInst(helperName, 1, args, dstOpnd));
	return dstOpnd;
}

//_______________________________________________________________________________________________________________
Opnd * InstCodeSelector::convert(CG_OpndHandle * oph, Type * dstType, Opnd * dstOpnd)
{
	if (!oph)
		return NULL;
	Opnd * srcOpnd=(Opnd*)oph;

	Type * srcType=srcOpnd->getType();
	bool converted=false;
	if (isIntegerType(srcType)){
		if (isIntegerType(dstType)){
			dstOpnd=convertIntToInt(srcOpnd, dstType, dstOpnd);
			converted=true;
		}else if (dstType->isFP()){
			dstOpnd=convertIntToFp(srcOpnd, dstType, dstOpnd);
			converted=true;
		}	
	}else if (srcType->isFP()){
		if (dstType->isInteger()){
			dstOpnd=convertFpToInt(srcOpnd, dstType, dstOpnd);
			converted=true;
		}else if (dstType->isFP()){
			dstOpnd=convertFpToFp(srcOpnd, dstType, dstOpnd);
			converted=true;
		}	
	}

	if (!converted){
		assert(irManager.getTypeSize(dstType)==srcOpnd->getSize());
		if (dstOpnd==NULL)
			dstOpnd=srcOpnd;
		else
			copyOpndTrivialOrTruncatingConversion(dstOpnd, srcOpnd);
	}

	return dstOpnd;
}


//_______________________________________________________________________________________________________________
//  Convert to integer

CG_OpndHandle* InstCodeSelector::convToInt(ConvertToIntOp::Types       opType,
                                                bool                        isSigned,
                                                ConvertToIntOp::OverflowMod ovfMod,
                                                Type*                       dstType, 
                                                CG_OpndHandle*              src) 
{
	Type * sizeType=NULL;
	switch (opType){
		case ConvertToIntOp::I1:
			sizeType=isSigned?typeManager.getInt8Type():typeManager.getUInt8Type();
			break;
		case ConvertToIntOp::I2:
			sizeType=isSigned?typeManager.getInt16Type():typeManager.getUInt16Type();
			break;
		case ConvertToIntOp::I:
		case ConvertToIntOp::I4:
			sizeType=isSigned?typeManager.getInt32Type():typeManager.getUInt32Type();
			break;
		case ConvertToIntOp::I8:
			sizeType=typeManager.getInt64Type();
			break;
		default: assert(0);
	}
	Opnd * tmpOpnd=convert(src, sizeType); 
	return convert(tmpOpnd, dstType);
}

//_______________________________________________________________________________________________________________
//  Convert to floating-point

CG_OpndHandle*  InstCodeSelector::convToFp(ConvertToFpOp::Types opType, 
                                                Type*                 dstType, 
                                                CG_OpndHandle*        src) 
{
	return convert(src, dstType);
}


//_______________________________________________________________________________________________________________
Opnd * InstCodeSelector::createResultOpnd(Type * dstType)
{
	if (dstType==NULL)
		return NULL;
    return irManager.newOpnd(dstType);
}

//_______________________________________________________________________________________________________________
Opnd * InstCodeSelector::simpleOp_I8(Mnemonic mn, Type * dstType, Opnd * src1, Opnd * src2)
{
	dstType=irManager.getTypeFromTag(Type::Int64);
	src1=convert(src1, dstType); 

	if (src2!=NULL){
		src2=convert(src2, dstType);
	}
	Opnd * dst = irManager.newOpnd(dstType);

	switch(mn){
		case Mnemonic_ADD:
		case Mnemonic_SUB:
		case Mnemonic_AND:
		case Mnemonic_OR:
		case Mnemonic_XOR:
		case Mnemonic_NOT:
			appendInsts(irManager.newI8PseudoInst(mn, 1, dst, (Opnd *)src1, (Opnd *)src2));
			break;
		default:
			assert(0);
			break;
	}

	return dst;
}

//_______________________________________________________________________________________________________________
Opnd * InstCodeSelector::fpOp(Mnemonic mn, Type * dstType, Opnd * src1, Opnd * src2)
{
	Opnd * dst=irManager.newOpnd(dstType);
	Opnd * srcOpnd1=(Opnd*)convert(src1, dstType), * srcOpnd2=(Opnd*)convert(src2, dstType);
	appendInsts(irManager.newInstEx(mn, 1, dst, srcOpnd1, srcOpnd2));
	return dst;
}

//_______________________________________________________________________________________________________________
Opnd * InstCodeSelector::simpleOp_I4(Mnemonic mn, Type * dstType, Opnd * src1, Opnd * src2)
{
	Opnd * dst=irManager.newOpnd(dstType);
	Opnd * srcOpnd1=(Opnd*)convert(src1, dstType), * srcOpnd2=src2==NULL?NULL:(Opnd*)convert(src2, dstType);
	if (Encoder::getMnemonicProperties(mn)&Inst::Properties_Symmetric)
		swapIfLastIs(srcOpnd1, srcOpnd2);
	appendInsts(irManager.newInstEx(mn, 1, dst, srcOpnd1, srcOpnd2));
	return dst;
}

//_______________________________________________________________________________________________________________
//  Add numeric values

CG_OpndHandle* InstCodeSelector::add(ArithmeticOp::Types opType, 
                                        CG_OpndHandle*      src1,
                                        CG_OpndHandle*      src2) 
{
	switch(opType){
		case ArithmeticOp::I4:
		case ArithmeticOp::I:{
			Type * dstType=opType==ArithmeticOp::I?irManager.getManagedPtrType(((Opnd*)src1)->getType()):irManager.getTypeFromTag(Type::Int32);
			return simpleOp_I4(Mnemonic_ADD, dstType, (Opnd*)src1, (Opnd*)src2);
		}
		case ArithmeticOp::I8:
			return simpleOp_I8(Mnemonic_ADD, irManager.getTypeFromTag(Type::Int64), (Opnd*)src1, (Opnd*)src2);
		case ArithmeticOp::D:
		case ArithmeticOp::F:
			return fpOp(Mnemonic_ADDSD, irManager.getTypeFromTag(Type::Double), (Opnd*)src1, (Opnd*)src2);
		case ArithmeticOp::S:
			return fpOp(Mnemonic_ADDSS, irManager.getTypeFromTag(Type::Single), (Opnd*)src1, (Opnd*)src2);
		default:
			ICS_ASSERT(0);
	}
	return NULL;
}

//_______________________________________________________________________________________________________________
//  Sub numeric values

CG_OpndHandle* InstCodeSelector::sub(ArithmeticOp::Types opType, 
                                        CG_OpndHandle*      src1,
                                        CG_OpndHandle*      src2) 
{
	switch(opType){
		case ArithmeticOp::I4:
		case ArithmeticOp::I:{
			Type * dstType=opType==ArithmeticOp::I?irManager.getManagedPtrType(((Opnd*)src1)->getType()):irManager.getTypeFromTag(Type::Int32);
			return simpleOp_I4(Mnemonic_SUB, dstType, (Opnd*)src1, (Opnd*)src2);
		}
		case ArithmeticOp::I8:
			return simpleOp_I8(Mnemonic_SUB, irManager.getTypeFromTag(Type::Int64), (Opnd*)src1, (Opnd*)src2);
		case ArithmeticOp::D:
		case ArithmeticOp::F:
			return fpOp(Mnemonic_SUBSD, irManager.getTypeFromTag(Type::Double), (Opnd*)src1, (Opnd*)src2);
		case ArithmeticOp::S:
			return fpOp(Mnemonic_SUBSS, irManager.getTypeFromTag(Type::Single), (Opnd*)src1, (Opnd*)src2);
		default:
			ICS_ASSERT(0);
	}
	return NULL;
}


//_______________________________________________________________________________________________________________
//  Add integer to a reference

CG_OpndHandle* InstCodeSelector::addRef(RefArithmeticOp::Types opType,
                                           CG_OpndHandle*         refSrc,
                                           CG_OpndHandle*         intSrc) 
{
	return add(opType==RefArithmeticOp::I?ArithmeticOp::I:ArithmeticOp::I4, refSrc, intSrc);
}

//_______________________________________________________________________________________________________________
//  Subtract integer from a reference

CG_OpndHandle* InstCodeSelector::subRef(RefArithmeticOp::Types opType,
                                           CG_OpndHandle*         refSrc,
                                           CG_OpndHandle*         intSrc) 
{
	return sub(opType==RefArithmeticOp::I?ArithmeticOp::I:ArithmeticOp::I4, refSrc, intSrc);
}

//_______________________________________________________________________________________________________________
//  Subtract reference from reference

CG_OpndHandle*    InstCodeSelector::diffRef(bool           ovf, 
                                                 CG_OpndHandle* ref1,
                                                 CG_OpndHandle* ref2) 
{
	return sub(ArithmeticOp::I4, ref1, ref2);
}   

//_______________________________________________________________________________________________________________
//  Subtract reference from reference and scale down by element type.

CG_OpndHandle*    InstCodeSelector::scaledDiffRef(CG_OpndHandle* ref1, 
                                                     CG_OpndHandle* ref2)  
{ 
    Opnd* r1 = (Opnd*)ref1;
	Type * elemRefType = r1->getType();

#ifdef _DEBUG
    Opnd* r2 = (Opnd*)ref2;
	assert( elemRefType->isManagedPtr() && elemRefType==r2->getType() );
#endif
    Type * elemType = ((PtrType *)elemRefType)->getPointedToType();

	uint32 size = getByteSize(irManager.getTypeSize(elemType));
    assert(size > 0);
    uint32 shift;
    switch(size) {
    case 1:
        shift = 0; 
        break; 
    case 2:
        shift = 1; 
        break; 
    case 4:
        shift = 2; 
        break; 
    default:
        assert(0);
        return NULL;
    }
    
	Opnd *dstOpnd = (Opnd *)diffRef(false, ref1, ref2);

    if(shift == 0) {
        return dstOpnd;
    } else {
		return shr(IntegerOp::I4,dstOpnd,irManager.newImmOpnd(typeManager.getUInt8Type(),shift));
    }
}    
 
//_______________________________________________________________________________________________________________
//  Multiply numeric values

CG_OpndHandle* InstCodeSelector::mul(ArithmeticOp::Types opType, 
                                        CG_OpndHandle*      src1,
                                        CG_OpndHandle*      src2) 
{
	Type * dstType;
	Opnd * dst, * srcOpnd1, * srcOpnd2;
	switch(opType){
		case ArithmeticOp::I4:
		case ArithmeticOp::I:
		{
			dstType=irManager.getTypeFromTag(Type::Int32);
			dst=irManager.newOpnd(dstType);
			srcOpnd1=(Opnd*)convert(src1, dstType);
			srcOpnd2=(Opnd*)convert(src2, dstType);
			swapIfLastIs(srcOpnd1, srcOpnd2);
			appendInsts(irManager.newInstEx(Mnemonic_IMUL, 1, dst, srcOpnd1, srcOpnd2));
			return dst;
		}
		case ArithmeticOp::I8:
			dstType=irManager.getTypeFromTag(Type::Int64);
			dst=irManager.newOpnd(dstType);
			srcOpnd1=(Opnd*)convert(src1, dstType);
			srcOpnd2=(Opnd*)convert(src2, dstType);
			swapIfLastIs(srcOpnd1, srcOpnd2);

			appendInsts(irManager.newI8PseudoInst(Mnemonic_IMUL,1,dst,srcOpnd1,srcOpnd2));
			return dst;
		case ArithmeticOp::D:
		case ArithmeticOp::F:
			return fpOp(Mnemonic_MULSD, irManager.getTypeFromTag(Type::Double), (Opnd*)src1, (Opnd*)src2);
		case ArithmeticOp::S:
			return fpOp(Mnemonic_MULSS, irManager.getTypeFromTag(Type::Single), (Opnd*)src1, (Opnd*)src2);
		default:
			ICS_ASSERT(0);
	}

	return NULL;
}

//_______________________________________________________________________________________________________________
//  Get high part of multiply

CG_OpndHandle* InstCodeSelector::mulhi(MulHiOp::Types op,
                                          CG_OpndHandle* src1,
                                          CG_OpndHandle* src2) 
{
    ICS_ASSERT(0);
	return 0;
}


//_______________________________________________________________________________________________________________
Opnd * InstCodeSelector::divOp(DivOp::Types   opType, bool rem, Opnd * src1, Opnd * src2)
{
	Opnd * dst=NULL;
	Type * dstType;
	Opnd * srcOpnd1, * srcOpnd2;
	switch(opType){
		case DivOp::I:
		case DivOp::I4:
		{
			dstType=irManager.getTypeFromTag(Type::Int32);
			Opnd * dstOpnd0=irManager.newOpnd(dstType);
			Opnd * dstOpnd1=irManager.newOpnd(dstType);
			Opnd * srcOpnd1=(Opnd*)convert(src1, dstType);
			appendInsts(irManager.newInstEx(Mnemonic_CDQ, 1, dstOpnd1, srcOpnd1));
			appendInsts(irManager.newInstEx(Mnemonic_IDIV, 2, dstOpnd1, dstOpnd0, dstOpnd1, srcOpnd1, (Opnd*)convert(src2, dstType)));
			dst=rem?dstOpnd1:dstOpnd0;
			break;
		}
		case DivOp::I8:
			dstType=irManager.getTypeFromTag(Type::Int64);
			dst=irManager.newOpnd(dstType);
			srcOpnd1=(Opnd*)convert(src1, dstType);
			srcOpnd2=(Opnd*)convert(src2, dstType);

			if (rem) {
				Opnd * args[]={ srcOpnd1, srcOpnd2 };
				CallInst * callInst=irManager.newRuntimeHelperCallInst(CompilationInterface::Helper_RemI64, 2, args, dst);
				appendInsts(callInst);
			} else {
				appendInsts(irManager.newI8PseudoInst(Mnemonic_IDIV, 1, dst,srcOpnd1,srcOpnd2));
			}
			break;
		case DivOp::D:
		case DivOp::F:
			if(rem) {
				dstType=irManager.getTypeFromTag(Type::Double);
				dst=irManager.newOpnd(dstType);
				Opnd * args[]={ (Opnd*)convert(src1, dstType), (Opnd*)convert(src2, dstType) };
				CallInst * callInst=irManager.newInternalRuntimeHelperCallInst("remF8", 2, args, dst);
				appendInsts(callInst);
			} else {
				return fpOp(Mnemonic_DIVSD, irManager.getTypeFromTag(Type::Double), src1, src2);
			}
			break;

		case DivOp::S:
			if(rem) {
				dstType=irManager.getTypeFromTag(Type::Single);
				dst=irManager.newOpnd(dstType);
				Opnd * args[]={ (Opnd*)convert(src1, dstType), (Opnd*)convert(src2, dstType) };
				CallInst * callInst=irManager.newInternalRuntimeHelperCallInst("remF4", 2, args, dst);
				appendInsts(callInst);
			} else {
				return fpOp(Mnemonic_DIVSS, irManager.getTypeFromTag(Type::Single), src1, src2);
			}
			break;
		default:
			ICS_ASSERT(0);
	}
	return dst;
}

//_______________________________________________________________________________________________________________
//  Divide two numeric values. 

CG_OpndHandle * InstCodeSelector::tau_div(DivOp::Types   op,
                                            CG_OpndHandle* src1,
                                            CG_OpndHandle* src2,
                                            CG_OpndHandle* tau_src1NonZero) 
{
	return divOp(op, false, (Opnd *)src1, (Opnd *)src2);
}

//_______________________________________________________________________________________________________________
//  Get remainder from the division of two numeric values

CG_OpndHandle* InstCodeSelector::tau_rem(DivOp::Types   op,
                                            CG_OpndHandle* src1,
                                            CG_OpndHandle* src2,
                                            CG_OpndHandle* tau_src2NonZero) 
{
	return divOp(op, true, (Opnd *)src1, (Opnd *)src2);
}

//_______________________________________________________________________________________________________________
//  Negate numeric value

CG_OpndHandle* InstCodeSelector::neg(NegOp::Types   opType,
                                          CG_OpndHandle* src) 
{
	Opnd * dst=NULL;
	switch(opType){
		case NegOp::I:
		case NegOp::I4:
		{
			Type * dstType=irManager.getTypeFromTag(Type::Int32);
			dst=irManager.newOpnd(dstType);
			appendInsts(irManager.newInstEx(Mnemonic_NEG, 1, dst, (Opnd*)convert(src, dstType)));
			break;
		}
		case NegOp::I8:
		{
			Type * dstType=irManager.getTypeFromTag(Type::Int64);
			Opnd * dstMOV = irManager.newOpnd(dstType);
			Opnd * src_null = irManager.newImmOpnd(dstType, 0);
			appendInsts(irManager.newI8PseudoInst(Mnemonic_MOV,1,dstMOV,src_null));
			dst = (Opnd *)sub(ArithmeticOp::I8, dstMOV, src);
			break;
		}
		case NegOp::D:
		{
			Type * dstType=irManager.getTypeFromTag(Type::Double);
			dst = irManager.newOpnd(dstType);
			appendInsts(irManager.newInstEx(Mnemonic_FCHS,1,dst,(Opnd*)src));
			break;
		}
		case NegOp::F:
		{
			Type * dstType=irManager.getTypeFromTag(Type::Float);
			dst = irManager.newOpnd(dstType);
			appendInsts(irManager.newInstEx(Mnemonic_FCHS,1,dst,(Opnd*)src));
			break;
		}
		case NegOp::S:
		{
			Type * dstType=irManager.getTypeFromTag(Type::Single);
			dst = irManager.newOpnd(dstType);
			appendInsts(irManager.newInstEx(Mnemonic_FCHS,1,dst,(Opnd*)src));
			break;
		}
		default:
			ICS_ASSERT(0);
	}
	return dst;
}

//_______________________________________________________________________________________________________________
Opnd * InstCodeSelector::minMaxOp(NegOp::Types   opType, bool max, Opnd * src1, Opnd * src2)
{
	Opnd * dst=NULL;
	switch(opType){
		case NegOp::I:
		case NegOp::I4:
		{
			Type * dstType=irManager.getTypeFromTag(Type::Int32);
			src1=convert(src1, dstType);
			src2=convert(src2, dstType);
			dst=irManager.newOpnd(dstType);
			appendInsts(irManager.newCopyPseudoInst(Mnemonic_MOV, dst, (Opnd*)src2));
			appendInsts(irManager.newInst(Mnemonic_CMP, (Opnd*)src1, dst));
			appendInsts(irManager.newInst(max?Mnemonic_CMOVG:Mnemonic_CMOVL, dst, (Opnd*)src1));
			break;
		}
		case NegOp::I8:
		case NegOp::D:
		case NegOp::F:
		case NegOp::S:
			ICS_ASSERT(0);		
		default:
			ICS_ASSERT(0);
	}
	return dst;
}

//_______________________________________________________________________________________________________________
//  Min

CG_OpndHandle* InstCodeSelector::min_op(NegOp::Types   opType,
                                           CG_OpndHandle* src1,
                                           CG_OpndHandle* src2) 
{
	return minMaxOp(opType, false, (Opnd*)src1, (Opnd*)src2);
}

//_______________________________________________________________________________________________________________
//  Max

CG_OpndHandle* InstCodeSelector::max_op(NegOp::Types   opType,
                                           CG_OpndHandle* src1,
                                           CG_OpndHandle* src2) 
{
	return minMaxOp(opType, true, (Opnd*)src1, (Opnd*)src2);
}

//_______________________________________________________________________________________________________________
//    Abs

CG_OpndHandle* InstCodeSelector::abs_op(NegOp::Types   opType, 
                                           CG_OpndHandle* src) 
{
	Opnd * dst=NULL;
	switch(opType){
		case NegOp::I:
		case NegOp::I4:
		{
			Type * dstType=irManager.getTypeFromTag(Type::Int32);
			dst=irManager.newOpnd(dstType);
			Opnd * srcOpnd=convert(src, dstType);
			appendInsts(irManager.newCopyPseudoInst(Mnemonic_MOV, dst, (Opnd*)srcOpnd));
			appendInsts(irManager.newInst(Mnemonic_NEG, dst));
			appendInsts(irManager.newInstEx(Mnemonic_CMOVL, 1, dst, (Opnd*)srcOpnd));
			break;
		}
		case NegOp::I8:
		case NegOp::D:
		case NegOp::F:
		case NegOp::S:
			ICS_ASSERT(0);		
		default:
			ICS_ASSERT(0);
	}
	return dst;
}


//_______________________________________________________________________________________________________________
//    Check if value is finite

CG_OpndHandle*    InstCodeSelector::tau_ckfinite(CG_OpndHandle* src) 
{
	return getTauUnsafe();
}

//_______________________________________________________________________________________________________________
//  Logical and

CG_OpndHandle* InstCodeSelector::and_(IntegerOp::Types opType,
                                           CG_OpndHandle*   src1,
                                           CG_OpndHandle*   src2) 
{
	switch(opType){
		case IntegerOp::I4:
		case IntegerOp::I:{
			Type * dstType=opType==IntegerOp::I?irManager.getManagedPtrType(((Opnd*)src1)->getType()):irManager.getTypeFromTag(Type::Int32);
			return simpleOp_I4(Mnemonic_AND, dstType, (Opnd*)src1, (Opnd*)src2);
		}
		case IntegerOp::I8:
			return simpleOp_I8(Mnemonic_AND, irManager.getTypeFromTag(Type::Int64), (Opnd*)src1, (Opnd*)src2);
		default:
			ICS_ASSERT(0);
	}
	return NULL;
}

//_______________________________________________________________________________________________________________
//  Logical or

CG_OpndHandle* InstCodeSelector::or_(IntegerOp::Types opType,
                                          CG_OpndHandle*   src1,
                                          CG_OpndHandle*   src2) 
{
	switch(opType){
		case IntegerOp::I4:
		case IntegerOp::I:{
			Type * dstType=opType==IntegerOp::I?irManager.getManagedPtrType(((Opnd*)src1)->getType()):irManager.getTypeFromTag(Type::Int32);
			return simpleOp_I4(Mnemonic_OR, dstType, (Opnd*)src1, (Opnd*)src2);
		}
		case IntegerOp::I8:
			return simpleOp_I8(Mnemonic_OR, irManager.getTypeFromTag(Type::Int64), (Opnd*)src1, (Opnd*)src2);
		default:
			ICS_ASSERT(0);
	}
	return NULL;
}

//_______________________________________________________________________________________________________________
//  Logical xor

CG_OpndHandle*    InstCodeSelector::xor_(IntegerOp::Types opType,
                                              CG_OpndHandle* src1,
                                              CG_OpndHandle* src2) 
{
	switch(opType){
		case IntegerOp::I4:
		case IntegerOp::I:{
			Type * dstType=opType==IntegerOp::I?irManager.getManagedPtrType(((Opnd*)src1)->getType()):irManager.getTypeFromTag(Type::Int32);
			return simpleOp_I4(Mnemonic_XOR, dstType, (Opnd*)src1, (Opnd*)src2);
		}
		case IntegerOp::I8:
			return simpleOp_I8(Mnemonic_XOR, irManager.getTypeFromTag(Type::Int64), (Opnd*)src1, (Opnd*)src2);
		default:
			ICS_ASSERT(0);
	}
	return NULL;
}

//_______________________________________________________________________________________________________________
//  Logical not

CG_OpndHandle*    InstCodeSelector::not_(IntegerOp::Types opType, 
                                              CG_OpndHandle*   src) 
{
	switch(opType){
		case IntegerOp::I4:
		case IntegerOp::I:{
			Type * dstType=opType==IntegerOp::I?irManager.getManagedPtrType(((Opnd*)src)->getType()):irManager.getTypeFromTag(Type::Int32);
			return simpleOp_I4(Mnemonic_NOT, dstType, (Opnd*)src, 0);
		}
		case IntegerOp::I8:
			return simpleOp_I8(Mnemonic_NOT, irManager.getTypeFromTag(Type::Int64), (Opnd*)src, 0);
		default:
			ICS_ASSERT(0);
	}
	return NULL;
}

//_______________________________________________________________________________________________________________
Opnd * InstCodeSelector::shiftOp(IntegerOp::Types opType, Mnemonic mn, Opnd * value, Opnd * shiftAmount)
{
	Opnd * dst=NULL;
	Type * dstType;
	switch(opType){
		case IntegerOp::I:
		case IntegerOp::I4:
		{
			dstType=irManager.getTypeFromTag(Type::Int32);
			dst = irManager.newOpnd(dstType);
			appendInsts(irManager.newInstEx(mn, 1, dst, (Opnd*)convert(value, dstType), (Opnd*)convert(shiftAmount, typeManager.getInt8Type())));
			break;
		}
		case IntegerOp::I8:
			dstType=irManager.getTypeFromTag(Type::Int64);
			dst=irManager.newOpnd(dstType);
			appendInsts(irManager.newI8PseudoInst(mn,1,dst,(Opnd*)convert(value, dstType),(Opnd*)convert(shiftAmount, typeManager.getInt32Type())));
			return dst;
		default:
			ICS_ASSERT(0);
	}
	return dst;
}

//_______________________________________________________________________________________________________________
//  Shift left and add

CG_OpndHandle*    InstCodeSelector::shladd(IntegerOp::Types opType, 
                                              CG_OpndHandle*   value, 
                                              uint32           imm, 
                                              CG_OpndHandle*   addto)  
{ 
//    ICS_ASSERT(0);
	Opnd * shiftDest = (Opnd *)shl(opType, value, irManager.newImmOpnd(typeManager.getUInt8Type(), imm));
	ArithmeticOp::Types atype;
	switch (opType) {
		case IntegerOp::I : atype = ArithmeticOp::I; break;
		case IntegerOp::I8 : atype = ArithmeticOp::I8; break;
		default : atype = ArithmeticOp::I4; break;
	}
	return add(atype, addto, shiftDest);
} 


//_______________________________________________________________________________________________________________
//  Shift left

CG_OpndHandle*    InstCodeSelector::shl(IntegerOp::Types opType, 
                                             CG_OpndHandle*   value,
                                             CG_OpndHandle*   shiftAmount) 
{
	return shiftOp(opType, Mnemonic_SHL, (Opnd*)value, (Opnd*)shiftAmount);
}

//_______________________________________________________________________________________________________________
//  Shift right

CG_OpndHandle* InstCodeSelector::shr(IntegerOp::Types opType,
                                        CG_OpndHandle*   value,
                                        CG_OpndHandle*   shiftAmount) 
{
	return shiftOp(opType, Mnemonic_SAR, (Opnd*)value, (Opnd*)shiftAmount);
}

//_______________________________________________________________________________________________________________
//  Shift right unsigned

CG_OpndHandle*    InstCodeSelector::shru(IntegerOp::Types opType,
                                            CG_OpndHandle* value,
                                            CG_OpndHandle* shiftAmount) 
{
	return shiftOp(opType, Mnemonic_SHR, (Opnd*)value, (Opnd*)shiftAmount);
}

//_______________________________________________________________________________________________________________
//  Select a value: (src1 ? src2 : src3)

CG_OpndHandle*  InstCodeSelector::select(CompareOp::Types     opType,
                                            CG_OpndHandle*       src1,
                                            CG_OpndHandle*       src2,
                                            CG_OpndHandle*       src3) 
{
    ICS_ASSERT(0);
	return 0;
}

//_______________________________________________________________________________________________________________
//  Compare two values. Result is an integer.

CG_OpndHandle*  InstCodeSelector::cmp(CompareOp::Operators cmpOp,
                                         CompareOp::Types     opType,
                                         CG_OpndHandle*       src1,
                                         CG_OpndHandle*       src2) 
{
	Opnd * dst=irManager.newOpnd(typeManager.getInt32Type());
	bool swapped=cmpToEflags(cmpOp, opType, (Opnd*)src1, (Opnd*)src2);
	ConditionMnemonic cm=getConditionMnemonicFromCompareOperator(cmpOp, opType);
	if (swapped) 
		cm=swapConditionMnemonic(cm);
	appendInsts(irManager.newCopyPseudoInst(Mnemonic_MOV, dst, irManager.newImmOpnd(typeManager.getInt32Type(), 0)));
	appendInsts(irManager.newInstEx(getMnemonic(Mnemonic_SETcc, cm), 1, dst));
	if (((opType==CompareOp::F) ||(opType==CompareOp::S) ||(opType==CompareOp::D)) && ((cmpOp == CompareOp::Geu) || (cmpOp == CompareOp::Gtu))) {
		appendInsts(irManager.newInstEx(Mnemonic_CMOVP,1,dst,irManager.newImmOpnd(typeManager.getInt32Type(), 0)));
	}
	return dst;
}

//_______________________________________________________________________________________________________________
//    Check if operand is equal to zero. Result is integer.

CG_OpndHandle*    InstCodeSelector::czero(CompareZeroOp::Types opType,
                                             CG_OpndHandle*       src) 
{
	Opnd * dst=irManager.newOpnd(typeManager.getInt32Type());
	cmpToEflags(CompareOp::Eq, getCompareOpTypesFromCompareZeroOpTypes(opType), (Opnd*)src, NULL);
	appendInsts(irManager.newCopyPseudoInst(Mnemonic_MOV, dst, irManager.newImmOpnd(typeManager.getInt32Type(), 0)));
	appendInsts(irManager.newInstEx(Mnemonic_CMOVZ,	 1, dst, irManager.newImmOpnd(typeManager.getInt32Type(), 1)));
	return dst;
}

//_______________________________________________________________________________________________________________
//    Check if operand is not equal to zero. Result is integer.

CG_OpndHandle*    InstCodeSelector::cnzero(CompareZeroOp::Types opType,
                                                CG_OpndHandle*       src) 
{
	Opnd * dst=irManager.newOpnd(typeManager.getInt32Type());
	cmpToEflags(CompareOp::Eq, getCompareOpTypesFromCompareZeroOpTypes(opType), (Opnd*)src, NULL);
	appendInsts(irManager.newCopyPseudoInst(Mnemonic_MOV, dst, irManager.newImmOpnd(typeManager.getInt32Type(), 0)));
	appendInsts(irManager.newInstEx(Mnemonic_CMOVNZ,	 1, dst, irManager.newImmOpnd(typeManager.getInt32Type(), 1)));
	return dst;
}

//_______________________________________________________________________________________________________________
bool InstCodeSelector::cmpToEflags(CompareOp::Operators cmpOp, CompareOp::Types opType,
									Opnd * src1, Opnd * src2
									)
{
	bool swapped=false;
	switch(opType){
		case CompareOp::I:
		case CompareOp::I4:
		case CompareOp::Ref:
		{
			Type * srcType=irManager.getTypeFromTag(Type::Int32);
			Opnd * srcOpnd1=(Opnd*)convert(src1, srcType);
			if (!src2){
				appendInsts(irManager.newInst(Mnemonic_TEST, srcOpnd1, srcOpnd1));
			}else{
				Opnd * srcOpnd2=(Opnd*)convert(src2, srcType);
				swapped=swapIfLastIs(srcOpnd1, srcOpnd2);
				appendInsts(irManager.newInst(Mnemonic_CMP, srcOpnd1, srcOpnd2));
			}
			break;
		}
		case CompareOp::I8:
		{
			Type * srcType=irManager.getTypeFromTag(Type::Int64);
			Opnd * srcOpnd1=(Opnd*)convert(src1, srcType), * srcOpnd2;
			if (!src2) {
				srcOpnd2=irManager.newImmOpnd(srcType, 0);				
			} else {
				srcOpnd2=(Opnd*)convert(src2, srcType);
			}
			swapped=swapIfLastIs(srcOpnd1, srcOpnd2);
			Opnd * dst = irManager.getRegOpnd(RegName_EFLAGS);
			appendInsts(irManager.newI8PseudoInst(Mnemonic_CMP,1,dst,srcOpnd1,srcOpnd2));
			irManager.getCGFlags()->earlyDCEOn = true;
			break;
		}
		case CompareOp::D:
		case CompareOp::F:
		{
			Type * srcType=irManager.getTypeFromTag(Type::Double);
			Opnd * srcOpnd1=(Opnd*)convert(src1, srcType);
			Opnd * srcOpnd2=src2?(Opnd*)convert(src2, srcType):irManager.newFPConstantMemOpnd((double)0.0);
			appendInsts(irManager.newInst(Mnemonic_UCOMISD, srcOpnd1, srcOpnd2));
			if(cmpOp == CompareOp::Eq || cmpOp == CompareOp::Ne) {
				Opnd * ah = irManager.newOpnd(typeManager.getInt32Type());
				appendInsts(irManager.newInst(Mnemonic_LAHF,ah));
				Opnd * dst = (Opnd *)and_(IntegerOp::I4, ah, irManager.newImmOpnd(typeManager.getInt32Type(),UNORD_FLAGS_MASK));
				appendInsts(irManager.newInst(Mnemonic_CMP, dst, irManager.newImmOpnd(typeManager.getInt32Type(),ZF)));
			}
			break;
		}
		case CompareOp::S:
		{
			Type * srcType=irManager.getTypeFromTag(Type::Single);
			Opnd * srcOpnd1=(Opnd*)convert(src1, srcType);
			Opnd * srcOpnd2=src2?(Opnd*)convert(src2, srcType):irManager.newFPConstantMemOpnd((float)0.0);
			appendInsts(irManager.newInst(Mnemonic_UCOMISS, srcOpnd1, srcOpnd2));
			if(cmpOp == CompareOp::Eq || cmpOp == CompareOp::Ne) {
				Opnd * ah = irManager.newOpnd(typeManager.getInt32Type());
				appendInsts(irManager.newInst(Mnemonic_LAHF,ah));
				Opnd * dst = (Opnd *)and_(IntegerOp::I4, ah, irManager.newImmOpnd(typeManager.getInt32Type(),UNORD_FLAGS_MASK));
				appendInsts(irManager.newInst(Mnemonic_CMP, dst, irManager.newImmOpnd(typeManager.getInt32Type(),ZF)));
			}
			break;
		}
		default:
			ICS_ASSERT(0);
	}
	return swapped;
}

//_______________________________________________________________________________________________________________
//  Branch 

void InstCodeSelector::branch(CompareOp::Operators cmpOp,
                                   CompareOp::Types    opType,
                                   CG_OpndHandle* src1,
                                   CG_OpndHandle* src2) 
{
	bool swapped=cmpToEflags(cmpOp, opType, (Opnd*)src1, (Opnd*)src2);
	ConditionMnemonic cm=getConditionMnemonicFromCompareOperator(cmpOp, opType);
	if (((opType==CompareOp::F) ||(opType==CompareOp::S) ||(opType==CompareOp::D)) && ((cmpOp == CompareOp::Geu) || (cmpOp == CompareOp::Gtu))) {
		appendInsts(irManager.newBranchInst(Mnemonic_JP));
	}
	appendInsts(irManager.newBranchInst(getMnemonic(Mnemonic_Jcc, swapped?swapConditionMnemonic(cm):cm)));
}

//_______________________________________________________________________________________________________________
//  Branch if src is zero

void InstCodeSelector::bzero(CompareZeroOp::Types opType,
                                  CG_OpndHandle* src) 
{
	cmpToEflags(CompareOp::Eq, 
		getCompareOpTypesFromCompareZeroOpTypes(opType), (Opnd*)src, 0);
	appendInsts(irManager.newBranchInst(Mnemonic_JZ));
}

//_______________________________________________________________________________________________________________
//  Branch if src is not zero

void InstCodeSelector::bnzero(CompareZeroOp::Types opType,
                                              CG_OpndHandle* src) 
{
	cmpToEflags(CompareOp::Eq, 
		getCompareOpTypesFromCompareZeroOpTypes(opType), (Opnd*)src, 0);
	appendInsts(irManager.newBranchInst(Mnemonic_JNZ));
}

//_______________________________________________________________________________________________________________
//  Switch: generate compare and branch to the default case.

void InstCodeSelector::tableSwitch(CG_OpndHandle* src, uint32 nTargets) 
{
	switchSrcOpnd=(Opnd*)src;
	switchNumTargets=nTargets;
	branch(CompareOp::Geu, CompareOp::I4, src, irManager.newImmOpnd(typeManager.getUInt32Type(), nTargets));
}

//_______________________________________________________________________________________________________________
//  Generate instructions for switch dispatch
//  SwitchTableInst should be the first inst in the block.

void InstCodeSelector::genSwitchDispatch(uint32 numTargets, Opnd *switchSrc)
{
	appendInsts(irManager.newSwitchInst(numTargets, switchSrc));
}

//_______________________________________________________________________________________________________________
//  Throw an exception

void InstCodeSelector::throwException(CG_OpndHandle* exceptionObj, bool createStackTrace) 
{
	Opnd * args[]={ (Opnd*)exceptionObj };
	CallInst * callInst=irManager.newRuntimeHelperCallInst(
		createStackTrace ? 
			CompilationInterface::Helper_Throw_SetStackTrace : 
			CompilationInterface::Helper_Throw_KeepStackTrace, 
		lengthof(args), args, NULL
	);
	appendInsts(callInst);
}

//_______________________________________________________________________________________________________________
//  Throw system exception

void InstCodeSelector::throwSystemException(CompilationInterface::SystemExceptionId id) 
{

	CallInst * callInst=NULL;
    switch (id) {
		case CompilationInterface::Exception_NullPointer:
			callInst=irManager.newRuntimeHelperCallInst(
				CompilationInterface::Helper_NullPtrException, 0, NULL, NULL);
			break;
		case CompilationInterface::Exception_ArrayIndexOutOfBounds:
			callInst=irManager.newRuntimeHelperCallInst(
				CompilationInterface::Helper_ArrayBoundsException, 0, NULL, NULL);
			break;
		case CompilationInterface::Exception_ArrayTypeMismatch:
			callInst=irManager.newRuntimeHelperCallInst(
				CompilationInterface::Helper_ElemTypeException, 0, NULL, NULL);
			break;
		case CompilationInterface::Exception_DivideByZero:
			callInst=irManager.newRuntimeHelperCallInst(
				CompilationInterface::Helper_DivideByZeroException, 0, NULL, NULL);
			break;
		default:
			assert(0);
    }
	appendInsts(callInst);
}

//_______________________________________________________________________________________________________________
//  Load 32-bit integer constant

CG_OpndHandle*    InstCodeSelector::ldc_i4(uint32 val) 
{
	return irManager.newImmOpnd(typeManager.getInt32Type(), val);
}

//_______________________________________________________________________________________________________________
//  Load 64-bit integer constant

CG_OpndHandle*    InstCodeSelector::ldc_i8(uint64 val) 
{	
	return irManager.newImmOpnd(typeManager.getInt64Type(), val);
}	

//_______________________________________________________________________________________________________________
//  get vtable constant (a constant pointer)

CG_OpndHandle* InstCodeSelector::getVTableAddr(Type *       dstType, ObjectType * base) 
{
	return irManager.newImmOpnd(dstType, Opnd::RuntimeInfo::Kind_VTableConstantAddr, base);
}

//_______________________________________________________________________________________________________________
//  Load double FP constant (unoptimized straightforward version)

CG_OpndHandle* InstCodeSelector::ldc_s(float val) 
{
	return irManager.newFPConstantMemOpnd(val);
}

//_______________________________________________________________________________________________________________
//  Load double FP constant (unoptimized straightforward version)

CG_OpndHandle*    InstCodeSelector::ldc_d(double val) 
{
	return irManager.newFPConstantMemOpnd(val);
}

//_______________________________________________________________________________________________________________
//  Load Null

CG_OpndHandle*    InstCodeSelector::ldnull(bool compressed) {
	return irManager.newImmOpnd(typeManager.getNullObjectType(), 0);
}

//_______________________________________________________________________________________________________________
//  Throw null pointer exception if base is NULL.

CG_OpndHandle* InstCodeSelector::tau_checkNull(CG_OpndHandle* base, bool checksThisForInlinedMethod) 
{
	appendInsts(irManager.newSystemExceptionCheckPseudoInst(CompilationInterface::Exception_NullPointer, (Opnd*)base, 0, checksThisForInlinedMethod));
	return getTauUnsafe();
}

//_______________________________________________________________________________________________________________
//  Throw index out of range exception if index is larger than array length

CG_OpndHandle* InstCodeSelector::tau_checkBounds(CG_OpndHandle* arrayLen, 
                                                    CG_OpndHandle* index) 
{
    Type * type1 = ((Opnd*)arrayLen)->getType();
    Type * type2 = ((Opnd*)index)->getType();

    CompareOp::Types cmpType;
	if (type1->isInt4() && type2->isInt4()) 
        cmpType = CompareOp::I4;
    else if (type1->isInt8()) {
        assert(type2->isInt8());
        cmpType = CompareOp::I8;
    } else 
        cmpType = CompareOp::I;

	BasicBlock * throwBasicBlock = irManager.newBasicBlock(0);
	throwBasicBlock->appendInsts(irManager.newRuntimeHelperCallInst(
				CompilationInterface::Helper_ArrayBoundsException, 0, NULL, NULL));

	branch(CompareOp::Geu, cmpType, (Opnd*)index, (Opnd*)arrayLen);
	irManager.newDirectBranchEdge(currentBasicBlock, throwBasicBlock, 0.001);
	return getTauUnsafe();
}

//_______________________________________________________________________________________________________________
//  Throw index out of range exception if (a > b), unsigned if pointer
//  Ignore tau argument.

CG_OpndHandle* InstCodeSelector::tau_checkLowerBound(CG_OpndHandle* a,
                                                        CG_OpndHandle* b) 
{
    Type * type1 = ((Opnd*)a)->getType();
    Type * type2 = ((Opnd*)b)->getType();

    CompareOp::Types cmpType;
    CompareOp::Operators cmpOp; 
	if (type1->isInt4() && type2->isInt4()) {
        cmpType = CompareOp::I4;
        cmpOp = CompareOp::Gt;
	}else if (type1->isInt8()) {
        assert(type2->isInt8());
        cmpType = CompareOp::I8;
        cmpOp = CompareOp::Gt;
	} else {
        cmpType = CompareOp::I;
        cmpOp = CompareOp::Gtu;
	}
	BasicBlock * throwBasicBlock = irManager.newBasicBlock(0);
	throwBasicBlock->appendInsts(irManager.newRuntimeHelperCallInst(
				CompilationInterface::Helper_ArrayBoundsException, 0, NULL, NULL));

	branch(cmpOp, cmpType, (Opnd*)a, (Opnd*)b);
	irManager.newDirectBranchEdge(currentBasicBlock, throwBasicBlock, 0.001);
	return getTauUnsafe();
}

//_______________________________________________________________________________________________________________
//  Throw index out of range exception if (index >=arrayLen (upper bound))
//  Ignore tau argument

CG_OpndHandle* InstCodeSelector::tau_checkUpperBound(CG_OpndHandle* index,
                                                        CG_OpndHandle* arrayLen) 
{
	return tau_checkBounds(arrayLen, index);
}

//_______________________________________________________________________________________________________________
//  Throw array type mismatch exception if src's type is not subclass of
//  array's element type

CG_OpndHandle* InstCodeSelector::tau_checkElemType(CG_OpndHandle* array, 
                                                      CG_OpndHandle* src,
                                                      CG_OpndHandle* tauNullChecked, 
                                                      CG_OpndHandle* tauIsArray) 
{
	BasicBlock * throwBasicBlock = irManager.newBasicBlock(0);
	throwBasicBlock->appendInsts(irManager.newRuntimeHelperCallInst(
				CompilationInterface::Helper_ElemTypeException, 0, NULL, NULL));

	Opnd * args[] = { (Opnd*)src, (Opnd*)array };
	Opnd * flag = irManager.newOpnd(typeManager.getInt32Type());

	appendInsts(irManager.newRuntimeHelperCallInst(
				CompilationInterface::Helper_IsValidElemType, lengthof(args), args, flag));

	bzero(CompareZeroOp::Ref, flag);
	irManager.newDirectBranchEdge(currentBasicBlock, throwBasicBlock, 0.001);

	return getTauUnsafe();
}

//_______________________________________________________________________________________________________________
//  Throw DivideByZeroException if checkZero's argument is 0

CG_OpndHandle*  InstCodeSelector::tau_checkZero(CG_OpndHandle* src) 
{
	BasicBlock * throwBasicBlock = irManager.newBasicBlock(0);
	throwBasicBlock->appendInsts(irManager.newRuntimeHelperCallInst(
				CompilationInterface::Helper_DivideByZeroException, 0, NULL, NULL));
    Opnd * srcOpnd = (Opnd*)src;
    Type * type = srcOpnd->getType();
    CompareZeroOp::Types opType = CompareZeroOp::I;
    switch (type->tag) {
		case Type::Int32:
			opType = CompareZeroOp::I4; break;
		case Type::Int64:
	        opType = CompareZeroOp::I8; break;
	    case Type::IntPtr:
			opType = CompareZeroOp::I;  break;
		default:
			assert(0);
    }
	bzero(opType, src);
	irManager.newDirectBranchEdge(currentBasicBlock, throwBasicBlock, 0.001);
	return getTauUnsafe();
}

//_______________________________________________________________________________________________________________
//  Throw an exception if the result of integer division
//    cannot be represented in the result type, that is, if
//    src1 is minint and src2 is -1

CG_OpndHandle* InstCodeSelector::tau_checkDivOpnds(CG_OpndHandle* src1, 
                                                      CG_OpndHandle* src2) 
{
    ICS_ASSERT(0);
	return 0;
}

//_______________________________________________________________________________________________________________
//  Add immediate offset to an operand

Opnd* InstCodeSelector::addOffset(Opnd *   addr, 
                                        Opnd *   base, 
                                        uint32      offset) 
{
    ICS_ASSERT(0);
	return 0;
}

//_______________________________________________________________________________________________________________
CG_OpndHandle* InstCodeSelector::uncompressRef(CG_OpndHandle *compref) 
{ 
	ICS_ASSERT(0);
    return 0;
}

//_______________________________________________________________________________________________________________
CG_OpndHandle* InstCodeSelector::compressRef(CG_OpndHandle *ref)
{ 
	ICS_ASSERT(0);
    return 0;
}

//_______________________________________________________________________________________________________________
Opnd* InstCodeSelector::buildOffset(uint32 offset, MemoryAttribute::Context context)
{
	ICS_ASSERT(0);
    return 0;
}

//_______________________________________________________________________________________________________________
Opnd* InstCodeSelector::buildOffsetPlusHeapbase(uint32 offset, MemoryAttribute::Context context)
{
	ICS_ASSERT(0);
	return 0;
}

//_______________________________________________________________________________________________________________
CG_OpndHandle * InstCodeSelector::ldFieldOffset(FieldDesc *fieldDesc)
{ 
	return irManager.newImmOpnd(typeManager.getInt32Type(), Opnd::RuntimeInfo::Kind_FieldOffset, fieldDesc);
}

//_______________________________________________________________________________________________________________
CG_OpndHandle * InstCodeSelector::ldFieldOffsetPlusHeapbase(FieldDesc *fieldDesc)
{ 
	ICS_ASSERT(0);
	return 0;
}

//_______________________________________________________________________________________________________________
CG_OpndHandle * InstCodeSelector::ldArrayBaseOffset(Type *elemType)
{ 
	ICS_ASSERT(0);
	return 0;
}

//_______________________________________________________________________________________________________________
CG_OpndHandle * InstCodeSelector::ldArrayBaseOffsetPlusHeapbase(Type *elemType)
{ 
	ICS_ASSERT(0);
	return 0;
}

//_______________________________________________________________________________________________________________
CG_OpndHandle * InstCodeSelector::ldArrayLenOffset(Type *elemType)
{ 
	ICS_ASSERT(0);
	return 0;
}

//_______________________________________________________________________________________________________________
CG_OpndHandle * InstCodeSelector::ldArrayLenOffsetPlusHeapbase(Type *elemType)
{ 
	ICS_ASSERT(0);
	return 0;
}

//_______________________________________________________________________________________________________________
CG_OpndHandle * InstCodeSelector::addOffset(Type *pointerType, CG_OpndHandle* refHandle,
                               CG_OpndHandle* offsetHandle)
{ 
	ICS_ASSERT(0);
    return 0;
}

//_______________________________________________________________________________________________________________
CG_OpndHandle * InstCodeSelector::addOffsetPlusHeapbase(Type *pointerType, 
                                           CG_OpndHandle* compRefHandle, 
                                           CG_OpndHandle* offsetPlusHeapbaseHandle)
{ 
	ICS_ASSERT(0);
    return 0;
}

//_______________________________________________________________________________________________________________
//  Load address of the field at "base + offset"

CG_OpndHandle* InstCodeSelector::ldFieldAddr(Type*          fieldRefType,
                                                CG_OpndHandle* base, 
                                                FieldDesc *    fieldDesc) 
{
	Opnd * offsetOpnd=(Opnd*)ldFieldOffset(fieldDesc);
	return simpleOp_I4(Mnemonic_ADD, fieldRefType, (Opnd*)base, offsetOpnd);
}

//_______________________________________________________________________________________________________________
//  Load static field address

CG_OpndHandle*    InstCodeSelector::ldStaticAddr(Type* fieldRefType, FieldDesc *fieldDesc) 
{
	Opnd * addr=irManager.newImmOpnd(fieldRefType, Opnd::RuntimeInfo::Kind_StaticFieldAddress, fieldDesc);
	return addr;
}

//_______________________________________________________________________________________________________________
//  Compute address of the first array element

CG_OpndHandle*  InstCodeSelector::ldElemBaseAddr(CG_OpndHandle* array) 
{
	ArrayType * arrayType=((Opnd*)array)->getType()->asArrayType();
	Type * elemType=arrayType->getElementType();
	Type * dstType=irManager.getManagedPtrType(elemType);
	Opnd * dst=irManager.newOpnd(dstType);
	appendInsts(irManager.newInstEx(Mnemonic_ADD, 1, dst, (Opnd*)array, irManager.newImmOpnd(typeManager.getInt32Type(), arrayType->getArrayElemOffset())));
	return dst;
}

//_______________________________________________________________________________________________________________
//  Compute address of the array element given 
//  address of the first element and index

CG_OpndHandle*  InstCodeSelector::addElemIndex(CG_OpndHandle * elemBase,
                                                  CG_OpndHandle * index) 
{
	PtrType * ptrType=((Opnd*)elemBase)->getType()->asPtrType();
    Type * elemType = ptrType->getPointedToType();

	uint32 elemSize=getByteSize(irManager.getTypeSize(elemType));

	Opnd * indexOpnd=(Opnd *)index;
	
	Opnd * scaledIndexOpnd=NULL;
	if (indexOpnd->isPlacedIn(OpndKind_Imm)){
		scaledIndexOpnd=irManager.newImmOpnd(indexOpnd->getType(), indexOpnd->getImmValue()*elemSize);
	}else{
		scaledIndexOpnd=irManager.newOpnd(indexOpnd->getType());
		appendInsts(irManager.newInstEx(Mnemonic_IMUL, 1, scaledIndexOpnd, indexOpnd, irManager.newImmOpnd(typeManager.getInt32Type(), elemSize)));
	}
	Opnd * dst=irManager.newOpnd(ptrType);
	appendInsts(irManager.newInstEx(Mnemonic_ADD, 1, dst, (Opnd*)elemBase, scaledIndexOpnd));
	return dst;
}

//_______________________________________________________________________________________________________________
//  Load array length

CG_OpndHandle*    InstCodeSelector::tau_arrayLen(Type *         dstType, 
                                                ArrayType *    arrayType,
                                                Type *         lenType,
                                                CG_OpndHandle* base,
                                                CG_OpndHandle* tauArrayNonNull, 
                                                CG_OpndHandle* tauIsArray) 
{
	Opnd * offset=irManager.newImmOpnd(typeManager.getInt32Type(), arrayType->getArrayLengthOffset());
	Opnd * addr=irManager.newMemOpnd(lenType, (Opnd*)base, 0, 0, offset);
	Opnd * dst=irManager.newOpnd(typeManager.getInt32Type());
	appendInsts(irManager.newCopyPseudoInst(Mnemonic_MOV, dst, addr));
	return dst;
}

//_______________________________________________________________________________________________________________
//  Simple load indirect -- load primitive value

CG_OpndHandle* InstCodeSelector::simpleLdInd(Type * dstType, Opnd * addr,
                                                Type::Tag memType,
                                                Opnd * baseTau,
                                                Opnd * offsetTau) 
{
	Opnd * opnd = irManager.newMemOpndAutoKind(irManager.getTypeFromTag(memType), addr);
	Opnd * dst = irManager.newOpnd(dstType);
	copyOpnd(dst, opnd);
	return dst;
}

//_______________________________________________________________________________________________________________
//  Simple store indirect -- store primitive value

void InstCodeSelector::simpleStInd(Opnd * addr, 
                                      Opnd * src,
                                      Type::Tag memType,
                                      bool      autoCompressRef,
                                      Opnd * baseTau,
                                      Opnd * offsetAndTypeTau) 
{
	Opnd * dst = irManager.newMemOpndAutoKind(irManager.getTypeFromTag(memType), addr);
	copyOpnd(dst, src);
} 


//_______________________________________________________________________________________________________________
//  Get type of the field reference

Type * InstCodeSelector::getFieldRefType(Type *dstType, 
                                              Type::Tag fieldTypeTag) 
{
    ICS_ASSERT(0);
	return 0;
}

//_______________________________________________________________________________________________________________
//  Load static field

CG_OpndHandle* InstCodeSelector::ldStatic(Type *     dstType, 
                                             FieldDesc* fieldDesc, 
                                             Type::Tag  fieldType,
	                                         bool       autoUncompressRef) 
{
	ICS_ASSERT(0);
	return 0;
}

//_______________________________________________________________________________________________________________
//  Load field

CG_OpndHandle* InstCodeSelector::tau_ldField(Type *         dstType,
                                            CG_OpndHandle* base, 
                                            Type::Tag      fieldType, 
                                            FieldDesc*     fieldDesc,
                                            bool           autoUncompressRef,
                                            CG_OpndHandle* tauBaseNonNull,
                                            CG_OpndHandle* tauBaseTypeHasField) 
{
    ICS_ASSERT(0);
	return 0;
}

//_______________________________________________________________________________________________________________
//  Load array element

CG_OpndHandle*  InstCodeSelector::tau_ldElem(Type *         dstType,
                                            CG_OpndHandle* array,
                                            CG_OpndHandle* index,
                                            bool autoUncompressRef,
                                            CG_OpndHandle* tauBaseNonNull, 
                                            CG_OpndHandle* tauIdxIsInBounds) 
{
    ICS_ASSERT(0);
	return 0;
}

//_______________________________________________________________________________________________________________
//  Store static field
//  tauFieldTypeChecked is a redundant argument and should go away

void InstCodeSelector::tau_stStatic(CG_OpndHandle*   src,  
                                   FieldDesc*       fieldDesc,
                                   Type::Tag        fieldType,
                                   bool             autoCompressRef,
                                   CG_OpndHandle*   tauFieldTypeChecked) 
{
    ICS_ASSERT(0);
}

//_______________________________________________________________________________________________________________
//  Store field

void InstCodeSelector::tau_stField(CG_OpndHandle*src, 
                                  CG_OpndHandle* base, 
                                  Type::Tag fieldType,
                                  FieldDesc *fieldDesc,
                                  bool autoCompressRef,
                                  CG_OpndHandle* tauBaseNonNull, 
                                  CG_OpndHandle* tauBaseTypeHasField,
                                  CG_OpndHandle* tauFieldHasType) 
{
    ICS_ASSERT(0);
}

//_______________________________________________________________________________________________________________
//  Store array element

void InstCodeSelector::tau_stElem(CG_OpndHandle* src,
                                 CG_OpndHandle* array, 
                                 CG_OpndHandle* index,
                                 bool autoCompressRef,
                                 CG_OpndHandle* tauBaseNonNull,
                                 CG_OpndHandle* tauAddressInRange, 
                                 CG_OpndHandle* tauElemTypeChecked) 
{
    ICS_ASSERT(0);
}
    


//_______________________________________________________________________________________________________________
//    Compress a reference operand

void InstCodeSelector::compressOpnd(Opnd *dst, Opnd *src) 
{
	ICS_ASSERT(0);
}

//_______________________________________________________________________________________________________________
//    Decompress a reference operand

void InstCodeSelector::decompressOpnd(Opnd *dst, Opnd *src) 
{
	ICS_ASSERT(0);
}

//_______________________________________________________________________________________________________________
// If one operand is a raw reference and the other is a compressed reference 
// change them both to either compressed or raw references so that they can be 
// compared for equality/inequality etc..

void InstCodeSelector::makeComparable(Opnd*& srcOpnd1, Opnd*& srcOpnd2) 
{
	ICS_ASSERT(0);
}

//_______________________________________________________________________________________________________________
//  If pointer points to the enumerated type change type tag to
//  the type tag of the underlying type of the enumerated type.

void InstCodeSelector::simplifyTypeTag(Type::Tag& tag,Type *ptr) 
{
    ICS_ASSERT(0);
}

//_______________________________________________________________________________________________________________
//  Load indirect

CG_OpndHandle* InstCodeSelector::tau_ldInd(Type* dstType, CG_OpndHandle* ptr, 
                                          Type::Tag memType,
                                          bool autoUncompressRef,
                                          bool speculateLoad,
                                          CG_OpndHandle* tauBaseNonNull,
                                          CG_OpndHandle* tauAddressInRange) 
{
	return simpleLdInd(dstType, (Opnd*)ptr, memType, (Opnd*)tauBaseNonNull, (Opnd*)tauAddressInRange);
}

//_______________________________________________________________________________________________________________
//  Store indirect

void InstCodeSelector::tau_stInd(CG_OpndHandle* src, 
                                    CG_OpndHandle* ptr, 
                                    Type::Tag      memType, 
                                    bool           autoCompressRef,
                                    CG_OpndHandle* tauBaseNonNull,
                                    CG_OpndHandle* tauAddressInRange, 
                                    CG_OpndHandle* tauElemTypeChecked) 
{
	return simpleStInd((Opnd*)ptr, (Opnd*)src, memType, autoCompressRef, (Opnd*)tauBaseNonNull, (Opnd*)tauElemTypeChecked);
}

//_______________________________________________________________________________________________________________
//  Load variable address

CG_OpndHandle* InstCodeSelector::ldVarAddr(uint32 varId) 
{
	ICS_ASSERT(0);
	return 0;
}

//_______________________________________________________________________________________________________________
//  Load variable

CG_OpndHandle * InstCodeSelector::ldVar(Type* dstType, uint32 varId) 
{
	if (dstType->tag==Type::Tau)
		return getTauUnsafe();
	Opnd * dst=irManager.newOpnd(dstType);
	copyOpnd(dst, irManager.getOpnd(varId));	
	return dst;
}

//_______________________________________________________________________________________________________________
//  Store variable

void InstCodeSelector::stVar(CG_OpndHandle* src, uint32 varId) 
{
	if (src==getTauUnsafe())
		return;
	copyOpnd(irManager.getOpnd(varId), (Opnd*)src);	
}

//_______________________________________________________________________________________________________________
//  Create new object

CG_OpndHandle* InstCodeSelector::newObj(ObjectType* objType) 
{
	Opnd * helperOpnds[] = {
		irManager.newImmOpnd(typeManager.getInt32Type(), Opnd::RuntimeInfo::Kind_Size, objType),
		irManager.newImmOpnd(getRuntimeIdType(), Opnd::RuntimeInfo::Kind_AllocationHandle, objType)
	};
	Opnd * retOpnd=irManager.newOpnd(objType);

	CallInst * callInst=irManager.newRuntimeHelperCallInst(
		CompilationInterface::Helper_NewObj_UsingVtable,
		2, helperOpnds, retOpnd);
	appendInsts(callInst);
	return retOpnd;
}

//_______________________________________________________________________________________________________________
//  Create new array
//    Call Helper_NewArray(allocationHandle, arrayLength)

CG_OpndHandle* InstCodeSelector::newArray(ArrayType*      arrayType,
                                             CG_OpndHandle* numElems) 
{
	Opnd * helperOpnds[] = {
		irManager.newImmOpnd(getRuntimeIdType(), Opnd::RuntimeInfo::Kind_AllocationHandle, arrayType),
		helperOpnds[1]=(Opnd*)numElems
	};
	Opnd * retOpnd=irManager.newOpnd(arrayType);
	CallInst * callInst=irManager.newRuntimeHelperCallInst(
		CompilationInterface::Helper_NewVector_UsingVtable,
		2, helperOpnds, retOpnd);
	appendInsts(callInst);
	return retOpnd;
}

//_______________________________________________________________________________________________________________
//    Create multidimentional new array
//    Call Helper_NewMultiArray(arrayTypeRuntimeId, numDims, uint32 dimN, ...,uint32 dim1)    

CG_OpndHandle*  InstCodeSelector::newMultiArray(ArrayType*      arrayType, 
                                                     uint32          numDims, 
                                                     CG_OpndHandle** dims) 
{
	Opnd ** helperOpnds = new (memManager) Opnd * [2+numDims];
	helperOpnds[0]=irManager.newImmOpnd(getRuntimeIdType(), Opnd::RuntimeInfo::Kind_TypeRuntimeId, arrayType);
	helperOpnds[1]=irManager.newImmOpnd(typeManager.getInt32Type(), numDims);
    for (uint32 i = 0; i < numDims; i++) 
		helperOpnds[i + 2] = (Opnd*)dims[numDims - 1 - i];
	Opnd * retOpnd=irManager.newOpnd(arrayType);
	CallInst * callInst=irManager.newRuntimeHelperCallInst(
		CompilationInterface::Helper_NewMultiArray,
		2+numDims, helperOpnds, retOpnd);
	appendInsts(callInst);
	return retOpnd;
}

//_______________________________________________________________________________________________________________
//  Load string 

CG_OpndHandle* InstCodeSelector::ldString(MethodDesc* enclosingMethod,
								uint32 stringToken,
								bool uncompress) 
{
	NamedType * parentType=enclosingMethod->getParentType();
	Opnd * helperOpnds[] = {
		irManager.newImmOpnd(getRuntimeIdType(), Opnd::RuntimeInfo::Kind_TypeRuntimeId, parentType),
		irManager.newImmOpnd(typeManager.getInt32Type(), stringToken)
	};
	Opnd * retOpnd=irManager.newOpnd(typeManager.getSystemStringType());

	CallInst * callInst=irManager.newRuntimeHelperCallInst(
		CompilationInterface::Helper_LdString,
		2, helperOpnds, retOpnd);
	appendInsts(callInst);
	return retOpnd;
}

//_______________________________________________________________________________________________________________
//  Load token

CG_OpndHandle * InstCodeSelector::ldToken(Type *dstType,MethodDesc* enclosingMethod,uint32 token) 
{
    ICS_ASSERT(0);
	return 0;
}

//_______________________________________________________________________________________________________________
//  Increment counter for the program instrumentation

void InstCodeSelector::incCounter(Type *counterType,uint32 counter) 
{
    ICS_ASSERT(0);
}

//_______________________________________________________________________________________________________________
//  Return with a value

void InstCodeSelector::ret(CG_OpndHandle* retValue) 
{
    appendInsts(irManager.newRetInst((Opnd*)retValue));
	seenReturn = true;
}

//_______________________________________________________________________________________________________________
//  Return without value

void InstCodeSelector::ret() 
{
	appendInsts(irManager.newRetInst(0));
	seenReturn = true;
} 

//_______________________________________________________________________________________________________________
//    Generate return instruction

void InstCodeSelector::genReturn() 
{
	if (!seenReturn){
		appendInsts(irManager.newRetInst(0));
	}
}

//_______________________________________________________________________________________________________________
//  Define an argument

CG_OpndHandle*    InstCodeSelector::defArg(uint32 position, Type *type) 
{
	return irManager.defArg(type, position);
}

//_______________________________________________________________________________________________________________
// Load address of the memory location that contains function address

CG_OpndHandle* InstCodeSelector::ldFunAddr(Type*        dstType, 
                                              MethodDesc * methodDesc) 
{
	Opnd * addrOpnd=irManager.newImmOpnd(dstType, Opnd::RuntimeInfo::Kind_MethodIndirectAddr, methodDesc);
	return addrOpnd;
}
    
//_______________________________________________________________________________________________________________
//  Load address of the virtual/interface table slot that contains function address
//    The tau operand is irrelevant because we compute the address of the slot not
//    address of the function.
//

CG_OpndHandle* InstCodeSelector::tau_ldVirtFunAddr(Type*        dstType, 
                                                  CG_OpndHandle*   vtableAddr, 
                                                  MethodDesc*      methodDesc,
                                                  CG_OpndHandle *  tauVtableHasDesc) 
{
	Opnd * offsetOpnd=irManager.newImmOpnd(typeManager.getInt32Type(), Opnd::RuntimeInfo::Kind_MethodVtableSlotOffset, methodDesc);
	return simpleOp_I4(Mnemonic_ADD, dstType, (Opnd*)vtableAddr, offsetOpnd);
}

//_______________________________________________________________________________________________________________
//  Load virtual table address

CG_OpndHandle* InstCodeSelector::tau_ldVTableAddr(Type* dstType, 
                                                     CG_OpndHandle* base,
                                                     CG_OpndHandle *tauBaseNonNull) 
{
	Opnd * vtableAddr=irManager.newOpnd(dstType);
	Opnd * sourceVTableAddr=irManager.newMemOpnd(dstType, (Opnd*)base, 0, 0, 
			irManager.newImmOpnd(typeManager.getInt32Type(), Opnd::RuntimeInfo::Kind_VTableAddrOffset)
		);
	copyOpnd(vtableAddr, sourceVTableAddr);
	return vtableAddr;
}

//_______________________________________________________________________________________________________________
//  Add the base of all VTables and an immediate offset to a CG_OpndHandle
//  This gets an absolute address into a field in the VTable

Opnd* InstCodeSelector::addVtableBaseAndOffset(Opnd *       addr, 
                                                     CG_OpndHandle * compPtr,
													 uint32          offset) 
{
	ICS_ASSERT(0);
	return 0;
}

//_______________________________________________________________________________________________________________
//  Load interface table address
//    The tau parameter is irrelevant because VM helper does not expect JIT
//    to verify that the object type has given interface

CG_OpndHandle*  InstCodeSelector::tau_ldIntfTableAddr(Type *         dstType, 
                                                     CG_OpndHandle* base, 
                                                     NamedType*     vtableType,
                                                     CG_OpndHandle* tauBaseHasIntf) 
{
	Opnd * helperOpnds[] = {
		(Opnd*)base,
		irManager.newImmOpnd(getRuntimeIdType(), Opnd::RuntimeInfo::Kind_TypeRuntimeId, vtableType)
	};

	Opnd * retOpnd=irManager.newOpnd(dstType);

	CallInst * callInst=irManager.newRuntimeHelperCallInst(
		CompilationInterface::Helper_LdInterface,
		2, helperOpnds, retOpnd);
	appendInsts(callInst);
	return retOpnd;
}

//_______________________________________________________________________________________________________________
//  Indirect call w/o a tau that indicates that first argument is non-null

CG_OpndHandle* InstCodeSelector::calli(uint32            numArgs, 
                                          CG_OpndHandle**   args, 
                                          Type*             retType, 
                                          CG_OpndHandle*    methodPtr,
                                          InlineInfo*       ii)
{
	return tau_calli(numArgs, args, retType, methodPtr, getTauUnsafe(), getTauUnsafe());
}
                                        
//_______________________________________________________________________________________________________________
//  Indirect call with the tau that indicates that first argument is non-null

CG_OpndHandle* InstCodeSelector::tau_calli(uint32        numArgs, 
                                              CG_OpndHandle**   args, 
                                              Type*             retType, 
                                              CG_OpndHandle*    methodPtr,
                                              CG_OpndHandle*    nonNullFirstArgTau,
                                              CG_OpndHandle*    tauTypesChecked,
                                              InlineInfo*       ii)
{
	Opnd * target=irManager.newMemOpnd(typeManager.getIntPtrType(), (Opnd*)methodPtr);
	Opnd * retOpnd=createResultOpnd(retType);
	CallInst * callInst=irManager.newCallInst(target, irManager.getDefaultManagedCallingConvention(), 
		numArgs, (Opnd **)args, retOpnd, ii);
	appendInsts(callInst);
	return retOpnd;
}

//_______________________________________________________________________________________________________________
//  Direct call to the method. Depending on the code generator flags we
//  either expand direct call into loading method address into a register and
//  indirect call or true direct call that will be patched if method is recompiled.

CG_OpndHandle* InstCodeSelector::call(uint32          numArgs, 
                                         CG_OpndHandle** args, 
                                         Type*           retType,
                                         MethodDesc *    desc,
                                         InlineInfo*       ii)
{
	return tau_call(numArgs, args, retType, desc, getTauUnsafe(), getTauUnsafe());
}

//_______________________________________________________________________________________________________________
//  Direct call to the method. Depending on the code generator flags we
//  either expand direct call into loading method address into a register and
//  indirect call or true direct call that will be patched if method is recompiled.

CG_OpndHandle* InstCodeSelector::tau_call(uint32          numArgs, 
                                             CG_OpndHandle** args, 
                                             Type*           retType,
                                             MethodDesc *    desc,
                                             CG_OpndHandle* tauNullCheckedFirstArg,
                                             CG_OpndHandle* tauTypesChecked,
                                             InlineInfo*       ii)
{
	Opnd * target=irManager.newImmOpnd(typeManager.getIntPtrType(), Opnd::RuntimeInfo::Kind_MethodDirectAddr, desc);
	Opnd * retOpnd=createResultOpnd(retType);
	CallInst * callInst=irManager.newCallInst(target, irManager.getDefaultManagedCallingConvention(), 
		numArgs, (Opnd **)args, retOpnd, ii);
	appendInsts(callInst);
	return retOpnd;
}

//_______________________________________________________________________________________________________________
//  Virtual call.
//  'this' reference is in args[0]

CG_OpndHandle* InstCodeSelector::tau_callvirt(uint32          numArgs,
                                                 CG_OpndHandle** args, 
                                                 Type*           retType,
                                                 MethodDesc *    desc,
                                                 CG_OpndHandle * tauNullChecked,
                                                 CG_OpndHandle * tauTypesChecked,
                                                 InlineInfo*       ii)
{
    CG_OpndHandle * vtableAddr = NULL;
    NamedType* vtableType = desc->getParentType();
    Type* vtableAddrType = typeManager.getVTablePtrType(vtableType);
    CG_OpndHandle * tauUnsafe = getTauUnsafe();
    if (vtableType->isInterface())
        vtableAddr = tau_ldIntfTableAddr(vtableAddrType, args[0], 
                                     vtableType, tauUnsafe);
    else
        vtableAddr = tau_ldVTableAddr(vtableAddrType, args[0], tauNullChecked);
    CG_OpndHandle * virtFunAddr = tau_ldVirtFunAddr(typeManager.getIntPtrType(),
                                                vtableAddr, desc, tauUnsafe);
    return tau_calli(numArgs,args,retType,virtFunAddr,tauNullChecked, tauTypesChecked);
}

//_______________________________________________________________________________________________________________
//  Intrinsic call

CG_OpndHandle* InstCodeSelector::callintr(uint32              numArgs, 
                                             CG_OpndHandle**     args, 
                                             Type*               retType,
                                             IntrinsicCallOp::Id callId) 
{
    ICS_ASSERT(0);
	return 0;
}

//_______________________________________________________________________________________________________________
//  Intrinsic call

CG_OpndHandle* InstCodeSelector::tau_callintr(uint32              numArgs, 
                                                 CG_OpndHandle**     args, 
                                                 Type*               retType,
                                                 IntrinsicCallOp::Id callId,
                                                 CG_OpndHandle*      tauNullsChecked,
                                                 CG_OpndHandle*      tauTypesChecked) 
{
    ICS_ASSERT(0);
	return 0;
}

//_______________________________________________________________________________________________________________
//  JIT helper call

CG_OpndHandle* InstCodeSelector::callhelper(uint32              numArgs, 
                                            CG_OpndHandle**     args, 
                                            Type*               retType,
                                            JitHelperCallOp::Id callId) 
{
   Opnd * dstOpnd=createResultOpnd(retType);
   switch(callId) {
    case InitializeArray:
        assert(numArgs == 4);
        appendInsts(irManager.newInternalRuntimeHelperCallInst("initialize_array", numArgs, (Opnd**)args, dstOpnd));
    	break;
    default:
        assert(0);
    }
    return dstOpnd;
}

//_______________________________________________________________________________________________________________
//  VM helper call

CG_OpndHandle* InstCodeSelector::callvmhelper(uint32              numArgs, 
                                              CG_OpndHandle**     args, 
                                              Type*               retType,
                                              VMHelperCallOp::Id  callId) 
{
    Opnd* dstOpnd=NULL;
    switch(callId) {
    case ThrowLazy:
    {

        Opnd **hlpArgs = new (memManager) Opnd* [numArgs+1];
        MethodDesc* md = ((Opnd*)args[0])->getType()->asMethodPtrType()->getMethodDesc();

        hlpArgs[0] = irManager.newImmOpnd(getRuntimeIdType(), Opnd::RuntimeInfo::Kind_TypeRuntimeId, 
                                          md->getParentType());
        for (uint32 i = 0; i < numArgs-1; i++)
            hlpArgs[i+1] = (Opnd*)args[i+1];
        hlpArgs[numArgs] = irManager.newImmOpnd(getRuntimeIdType(), 
                                               Opnd::RuntimeInfo::Kind_MethodRuntimeId, md);
        appendInsts(irManager.newRuntimeHelperCallInst(CompilationInterface::Helper_Throw_Lazy,
                                                       numArgs+1, (Opnd**)hlpArgs, dstOpnd));
        break;
    }
    default:
        assert(0);
    }
    return dstOpnd;
}

//_______________________________________________________________________________________________________________
//  Box a value

CG_OpndHandle* InstCodeSelector::box(ObjectType    * boxedType, 
                                        CG_OpndHandle * val) 
{
	ICS_ASSERT(0);
    return 0;
}
 
//_______________________________________________________________________________________________________________
//  Unbox object

CG_OpndHandle* InstCodeSelector::unbox(Type * dstType, CG_OpndHandle* objHandle) 
{
	ICS_ASSERT(0);
    return 0;
}

//_______________________________________________________________________________________________________________
//  Load value object

CG_OpndHandle* InstCodeSelector::ldValueObj(Type* objType, CG_OpndHandle *srcAddr) 
{
	ICS_ASSERT(0);
    return 0;
}

//_______________________________________________________________________________________________________________
//  Store value object

void InstCodeSelector::stValueObj(CG_OpndHandle *dstAddr, CG_OpndHandle *src) 
{
	ICS_ASSERT(0);
}

//_______________________________________________________________________________________________________________
//  Initialize value object

void InstCodeSelector::initValueObj(Type* objType, CG_OpndHandle *objAddr) 
{
	ICS_ASSERT(0);
}

//_______________________________________________________________________________________________________________
//  Copy value object

void  InstCodeSelector::copyValueObj(Type* objType, CG_OpndHandle *dstAddr, CG_OpndHandle *srcAddr) 
{
	ICS_ASSERT(0);
}

//_______________________________________________________________________________________________________________
//  Static estimate of fast path success probabilities

#define FAST_PATH_MONITOR_ENTER_SUCCESS_PROB  0.99
#define FAST_PATH_MONITOR_EXIT_SUCCESS_PROB   0.99

//_______________________________________________________________________________________________________________
//  Check if we should inline synchronization

bool InstCodeSelector::inlineSync(CompilationInterface::ObjectSynchronizationInfo& syncInfo) 
{
	return false;
}

//_______________________________________________________________________________________________________________
//  Acquire monitor for an object

void InstCodeSelector::tau_monitorEnter(CG_OpndHandle* obj, CG_OpndHandle* tauIsNonNull) 
{
	Opnd * helperOpnds[] = { (Opnd*)obj };
	CallInst * callInst=irManager.newRuntimeHelperCallInst(CompilationInterface::Helper_ObjMonitorEnter,
		1, helperOpnds, NULL);
	appendInsts(callInst);
}

//_______________________________________________________________________________________________________________
//    Release monitor for an object

void InstCodeSelector::tau_monitorExit(CG_OpndHandle* obj,
                                          CG_OpndHandle* tauIsNonNull) 
{
	Opnd * helperOpnds[] = { (Opnd*)obj };
	CallInst * callInst=irManager.newRuntimeHelperCallInst(CompilationInterface::Helper_ObjMonitorExit,
		1, helperOpnds, NULL);
	appendInsts(callInst);
}

//_______________________________________________________________________________________________________________
//  Load address of the object lock 
// (aka lock owner field in the synchronization header)

CG_OpndHandle* InstCodeSelector::ldLockAddr(CG_OpndHandle* obj) 
{
    ICS_ASSERT(0);
	return 0;
} 

//_______________________________________________________________________________________________________________
CG_OpndHandle* InstCodeSelector::tau_balancedMonitorEnter(CG_OpndHandle* obj, CG_OpndHandle* lockAddr,
                                        CG_OpndHandle* tauIsNonNull)
{
	ICS_ASSERT(0);
	return 0;
}

//_______________________________________________________________________________________________________________
void           InstCodeSelector::balancedMonitorExit(CG_OpndHandle* obj, CG_OpndHandle* lockAddr, 
                                       CG_OpndHandle* oldLock)
{
	ICS_ASSERT(0);
}

//_______________________________________________________________________________________________________________
CG_OpndHandle* InstCodeSelector::tau_optimisticBalancedMonitorEnter(CG_OpndHandle* obj, CG_OpndHandle* lockAddr,
                                                  CG_OpndHandle* tauIsNonNull)
{
	ICS_ASSERT(0);
	return 0;
}

//_______________________________________________________________________________________________________________
void           InstCodeSelector::optimisticBalancedMonitorExit(CG_OpndHandle* obj, CG_OpndHandle* lockAddr, 
                                                 CG_OpndHandle* oldLock)
{
	ICS_ASSERT(0);
}

//_______________________________________________________________________________________________________________
//  Read synchronization fence flag

uint32 InstCodeSelector::getSyncFenceFlag() 
{
	ICS_ASSERT(0);
    return 0;
}

//_______________________________________________________________________________________________________________
//  Monitor enter fence

void InstCodeSelector::monitorEnterFence(CG_OpndHandle* obj) 
{
}

//_______________________________________________________________________________________________________________
//  Monitor exit fence

void InstCodeSelector::monitorExitFence(CG_OpndHandle* obj) 
{
}

//_______________________________________________________________________________________________________________
//		Acquire monitor for a class
//		Helper_TypeMonitorEnter(typeRuntimeId)

void InstCodeSelector::typeMonitorEnter(NamedType *type) 
{
	Opnd * helperOpnds[]={irManager.newImmOpnd(getRuntimeIdType(), Opnd::RuntimeInfo::Kind_TypeRuntimeId, type)};
	CallInst * callInst=irManager.newRuntimeHelperCallInst(CompilationInterface::Helper_TypeMonitorEnter,
		1, helperOpnds, NULL);
	appendInsts(callInst);
}

//_______________________________________________________________________________________________________________
//      Release monitor for a class
//		Helper_TypeMonitorExit(typeRuntimeId)

void InstCodeSelector::typeMonitorExit(NamedType *type) 
{
	Opnd * helperOpnds[]={irManager.newImmOpnd(getRuntimeIdType(), Opnd::RuntimeInfo::Kind_TypeRuntimeId, type)};
	CallInst * callInst=irManager.newRuntimeHelperCallInst(CompilationInterface::Helper_TypeMonitorExit,
		1, helperOpnds, NULL);
	appendInsts(callInst);
}

//_______________________________________________________________________________________________________________
//  Statically cast object to type.
//  This cast requires no runtime check.  It's a compiler assertion. 

CG_OpndHandle* InstCodeSelector::tau_staticCast(ObjectType *   toType, 
                                                   CG_OpndHandle* obj,
                                                   CG_OpndHandle* tauIsType) 
{
	Opnd * dst=irManager.newOpnd(toType);
	copyOpnd(dst, (Opnd*)obj);
	return (Opnd*)dst;
}

//_______________________________________________________________________________________________________________
//		Cast object to type and return the object if casting is legal,
//		otherwise throw an exception. 
//		The cast operation does not produce a tau.

CG_OpndHandle* InstCodeSelector::tau_cast(ObjectType *toType, 
                                         CG_OpndHandle* obj,
                                         CG_OpndHandle* tauCheckedNull) 
{
	Opnd * dst=irManager.newOpnd(toType);
	Opnd * args[]={ (Opnd*)obj, irManager.newImmOpnd(getRuntimeIdType(), Opnd::RuntimeInfo::Kind_TypeRuntimeId, toType) };
	CallInst * callInst=irManager.newRuntimeHelperCallInst(
		CompilationInterface::Helper_Cast,
		lengthof(args), args, dst
	);
	appendInsts(callInst);
	return dst;
}

//_______________________________________________________________________________________________________________
//  Cast object to type and return the tau tied to this cast if casting is legal,
//  otherwise throw an exception. 
//  checkCast is different from cast - checkCast returns the tau while cast returns
//  the casted object (which is the same as the argument object)
//

CG_OpndHandle* InstCodeSelector::tau_checkCast(ObjectType *   toType, 
                                                  CG_OpndHandle* obj,
                                                  CG_OpndHandle* tauCheckedNull) 
{
	Opnd * dst=irManager.newOpnd(toType);
	Opnd * args[]={ (Opnd*)obj, irManager.newImmOpnd(getRuntimeIdType(), Opnd::RuntimeInfo::Kind_TypeRuntimeId, toType) };
	CallInst * callInst=irManager.newRuntimeHelperCallInst(
		CompilationInterface::Helper_Cast,
		lengthof(args), args, dst
	);
	appendInsts(callInst);
	return getTauUnsafe();
}

//_______________________________________________________________________________________________________________
//  Check if object has given type.
//  If it is return the object, else return NULL.

CG_OpndHandle* InstCodeSelector::tau_asType(ObjectType* type,
                                           CG_OpndHandle* obj,
                                           CG_OpndHandle* tauCheckedNull) 
{
	Opnd * instanceOfResult=(Opnd*)tau_instanceOf(type, obj, tauCheckedNull);
	Opnd * dst=irManager.newOpnd(type);
	cmpToEflags(CompareOp::Eq, CompareOp::Ref, (Opnd*)instanceOfResult, NULL);
	appendInsts(irManager.newCopyPseudoInst(Mnemonic_MOV, dst, irManager.newImmOpnd(typeManager.getInt32Type(), 0))); 
	appendInsts(irManager.newInstEx(Mnemonic_CMOVZ,	 1, dst, (Opnd*)obj));
	return dst;
}

//_______________________________________________________________________________________________________________
//    Check if an object has given type.
//    If it is return 1 else return 0.

CG_OpndHandle* InstCodeSelector::tau_instanceOf(ObjectType *type, 
                                                 CG_OpndHandle* obj,
                                                 CG_OpndHandle* tauCheckedNull) 
{
	Opnd * dst=irManager.newOpnd(typeManager.getInt32Type());
	Opnd * args[]={ (Opnd*)obj, irManager.newImmOpnd(getRuntimeIdType(), Opnd::RuntimeInfo::Kind_TypeRuntimeId, type) };
	CallInst * callInst=irManager.newRuntimeHelperCallInst(
		CompilationInterface::Helper_IsInstanceOf,
		lengthof(args), args, dst
	);
	appendInsts(callInst);
	return dst;
}

//_______________________________________________________________________________________________________________
//    Initialize type

void InstCodeSelector::initType(Type* type) 
{
    assert(type->isObject());
    NamedType * namedType = type->asNamedType();
	Opnd * args[]={ irManager.newImmOpnd(getRuntimeIdType(), Opnd::RuntimeInfo::Kind_TypeRuntimeId, namedType) };
	CallInst * callInst=irManager.newRuntimeHelperCallInst(
		CompilationInterface::Helper_InitType,
		lengthof(args), args, NULL
	);
	appendInsts(callInst);
}

//_______________________________________________________________________________________________________________
//    Catch an exception object

CG_OpndHandle*  InstCodeSelector::catchException(Type * exceptionType) 
{
    Opnd * exceptionObj = irManager.newOpnd(exceptionType);
	appendInsts(irManager.newCatchPseudoInst(exceptionObj));
    return exceptionObj;
}


//_______________________________________________________________________________________________________________
//
//  Copy operand
//
CG_OpndHandle * InstCodeSelector::copy(CG_OpndHandle *src) 
{
	if (src==getTauUnsafe())
		return src;
	Opnd * dst=irManager.newOpnd(((Opnd*)src)->getType());
	copyOpnd(dst, (Opnd*)src);
	return dst;
}


//_______________________________________________________________________________________________________________
//  Prefetch a cache line from [base + offset]

void InstCodeSelector::prefetch(CG_OpndHandle* base, uint32 offset, int hints) 
{
}

//_______________________________________________________________________________________________________________
//    Create a tau point definition indicating that control flow reached the current point

CG_OpndHandle*  InstCodeSelector::tauPoint() 
{
	return getTauUnsafe();
}

//_______________________________________________________________________________________________________________
//    Generate a tau split instruction

CG_OpndHandle * InstCodeSelector::genTauSplit(BranchInst *br) 
{
	return getTauUnsafe();
}

//_______________________________________________________________________________________________________________
//    Create a tau edge definition tied to the previous split in the control flow

CG_OpndHandle*  InstCodeSelector::tauEdge() 
{
	return getTauUnsafe();
}

//_______________________________________________________________________________________________________________
//    And several tau operands

CG_OpndHandle*  InstCodeSelector::tauAnd(uint32 numArgs, CG_OpndHandle** args) 
{
	return getTauUnsafe();
}

//_______________________________________________________________________________________________________________
//    Return tauUnsafe operand that indicates that it is unsafe
//    to move operation above any control flow split

CG_OpndHandle*  InstCodeSelector::tauUnsafe() 
{
	return getTauUnsafe();
}

//_______________________________________________________________________________________________________________
//    Return tauSafe operand that indicates that it is always safe to move the instruction

CG_OpndHandle*  InstCodeSelector::tauSafe() 
{
	return getTauUnsafe();
}

//_______________________________________________________________________________________________________________
// result is a predicate
CG_OpndHandle*  InstCodeSelector::pred_cmp(CompareOp::Operators,CompareOp::Types,CG_OpndHandle* src1,CG_OpndHandle* src2)
{
    ICS_ASSERT(0);
    return 0;
}

//_______________________________________________________________________________________________________________
CG_OpndHandle*  InstCodeSelector::pred_czero(CompareZeroOp::Types,CG_OpndHandle* src)
{
    ICS_ASSERT(0);
    return 0;
}

//_______________________________________________________________________________________________________________
CG_OpndHandle*  InstCodeSelector::pred_cnzero(CompareZeroOp::Types,CG_OpndHandle* src)
{
    ICS_ASSERT(0);
    return 0;
}

//_______________________________________________________________________________________________________________
void InstCodeSelector::pred_btrue(CG_OpndHandle* src1)
{
    ICS_ASSERT(0);
}

// END new tau instructions



}}; // namespace Ia32
