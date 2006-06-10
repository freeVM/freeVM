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
 */

#include "MemoryManager.h"
#include "IRBuilder.h"
#include "Inst.h"
#include "CSEHash.h"
#include "Log.h"
#include "CGSupport.h"
#include "irmanager.h"
#include "CompilationContext.h"

namespace Jitrino {

static void UNIMPLEMENTED(char* fun) {
    Log::out() << "   !!!!  IRBuilder: unimplemented: " << fun << "   !!!\n";
}

#if defined(_MSC_VER) && !defined (__ICL) && !defined (__GNUC__)
#pragma warning(disable : 4355)
#endif

IRBuilder::IRBuilder(IRManager& irm)
    : irManager(irm),
      opndManager(irm.getOpndManager()),
      typeManager(irm.getTypeManager()),
      instFactory(irm.getInstFactory()),
      flowGraph(irm.getFlowGraph()),
      irBuilderFlags(*irm.getCompilationContext()->getIRBuilderFlags()),
      tempMemoryManager(0, "IRBuilder::tempMemoryManager"),
      cseHashTable(tempMemoryManager),
      simplifier(irm, this),
      tauMethodSafeOpnd(0),
      offset(0)
{
    currentLabel = NULL;
    irBuilderFlags.isBCMapinfoRequired = irm.getCompilationInterface().isBCMapInfoRequired();

    if (irBuilderFlags.isBCMapinfoRequired) {
        MethodDesc* meth = irm.getCompilationInterface().getMethodToCompile();
        bc2HIRmapHandler = new(irm.getMemoryManager()) VectorHandler(bcOffset2HIRHandlerName, meth);
//#ifdef _DEBUG
//        lostBCMapOffsetHandler = new(tempMemoryManager) MapHandler(lostBCOffsetHandlerName, meth);
//#endif
    }
}

void IRBuilder::invalid() {
    Log::cat_opt()->error << " !!!! ---- IRBuilder::invalid ---- !!!! " << ::std::endl;
    assert(0);
}

Inst* IRBuilder::appendInst(Inst* inst) {
    assert(currentLabel);
    if (irBuilderFlags.isBCMapinfoRequired) {
        //POINTER_SIZE_INT instAddr = (POINTER_SIZE_INT) inst;
        uint64 instID = inst->getId();
        bc2HIRmapHandler->setVectorEntry(instID, offset);
//#ifdef _DEBUG
//        lostBCMapOffsetHandler->setMapEntry((uint64) offset, 0x01);
//#endif
    }
    inst->insertBefore(currentLabel);
    if(Log::cat_opt()->isDebugEnabled()) {
        inst->print(Log::out());
        Log::out() << ::std::endl;
        Log::out().flush();
    }
    return inst;
}

void IRBuilder::killCSE() {
    cseHashTable.kill();
}

void IRBuilder::genLabel(LabelInst* labelInst) {
    cseHashTable.kill();
    currentLabel = labelInst;
    if(Log::cat_opt()->isDebugEnabled()) {
        currentLabel->print(Log::out());
        Log::out() << ::std::endl;
        Log::out().flush();
    }
}

void IRBuilder::genFallThroughLabel(LabelInst* labelInst) {
    currentLabel = labelInst;
    if(Log::cat_opt()->isDebugEnabled()) {
        currentLabel->print(Log::out());
        Log::out() << ::std::endl;
        Log::out().flush();
    }
}

LabelInst* IRBuilder::createLabel() {
    currentLabel = (LabelInst*)instFactory.makeLabel();
    return currentLabel;
}

void IRBuilder::createLabels(uint32 numLabels, LabelInst** labels) {
    for (uint32 i=0; i<numLabels; i++) {
        labels[i] = (LabelInst*)instFactory.makeLabel();
    }
}

LabelInst* IRBuilder::genMethodEntryLabel(MethodDesc* methodDesc) {
    LabelInst* labelInst =
        instFactory.makeMethodEntryLabel(methodDesc);
    currentLabel = labelInst;

    if(Log::cat_opt()->isDebugEnabled()) {
        currentLabel->print(Log::out());
        Log::out() << ::std::endl;
        Log::out().flush();
    }
    return labelInst;
}

void IRBuilder::genMethodEntryMarker(MethodDesc* methodDesc) {
    if (! irBuilderFlags.insertMethodLabels)
        return;
    appendInst(instFactory.makeMethodMarker(MethodMarkerInst::Entry, methodDesc));
}

void IRBuilder::genMethodEndMarker(MethodDesc* methodDesc, Opnd *obj) {
    if (! irBuilderFlags.insertMethodLabels)
        return;
    assert(obj && !obj->isNull());
    appendInst(instFactory.makeMethodMarker(MethodMarkerInst::Exit, methodDesc, obj));
}

void IRBuilder::genMethodEndMarker(MethodDesc* methodDesc) {
    if (! irBuilderFlags.insertMethodLabels)
        return;
    appendInst(instFactory.makeMethodMarker(MethodMarkerInst::Exit, methodDesc));
}

// compute instructions
Opnd*
IRBuilder::genAdd(Type* dstType, Modifier mod, Opnd* src1, Opnd* src2) {
    src1 = propagateCopy(src1);
    src2 = propagateCopy(src2);
    Operation operation(Op_Add, dstType->tag, mod);
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, src1, src2);
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyAdd(dstType, mod, src1, src2);
    }
    if (!dst) {
        dst = createOpnd(dstType);
        Inst *newi = instFactory.makeAdd(mod, dst, src1, src2);
        appendInst(newi);
    }
    insertHash(hashcode, src1, src2, dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genMul(Type* dstType, Modifier mod, Opnd* src1, Opnd* src2) {
    src1 = propagateCopy(src1);
    src2 = propagateCopy(src2);
    Operation operation(Op_Mul, dstType->tag, mod);
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, src1, src2);
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyMul(dstType, mod, src1, src2);
    }
    if (!dst) {
        dst = createOpnd(dstType);
        appendInst(instFactory.makeMul(mod, dst, src1, src2));
    }
    insertHash(hashcode, src1, src2, dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genSub(Type* dstType, Modifier mod, Opnd* src1, Opnd* src2) {
    src1 = propagateCopy(src1);
    src2 = propagateCopy(src2);
    Operation operation(Op_Sub, dstType->tag, mod);
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, src1, src2);
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifySub(dstType, mod, src1, src2);
    }
    if (!dst) {
        dst = createOpnd(dstType);
        appendInst(instFactory.makeSub(mod, dst, src1, src2));
    }
    insertHash(hashcode, src1, src2, dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genDiv(Type* dstType, Modifier mod, Opnd* src1, Opnd* src2) {
    src1 = propagateCopy(src1);
    src2 = propagateCopy(src2);
    Opnd *tauDivOk = 0;
    if(src2->getType()->isInteger())
        tauDivOk = genTauCheckZero(src2);
    else
        tauDivOk = genTauSafe(); // safe by construction
    Operation operation(Op_TauDiv, dstType->tag, mod);
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, src1, src2); // tauDivOk is not needed in hash
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyTauDiv(dstType, mod, src1, src2, tauDivOk);
    }
    if (!dst) {
        dst = createOpnd(dstType);
        appendInst(instFactory.makeTauDiv(mod, dst, src1, src2, tauDivOk));
    }
    insertHash(hashcode, src1, src2, dst->getInst()); // tauDivOk is not needed in hash
    return dst;
}

//
// for CLI: inserts a CheckDivOpnds before the divide
Opnd*
IRBuilder::genCliDiv(Type* dstType, Modifier mod, Opnd* src1, Opnd* src2) {
    src1 = propagateCopy(src1);
    src2 = propagateCopy(src2);

    Operation operation(Op_TauDiv, dstType->tag, mod);
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, src1, src2); // tauDivOk is not needed in hash
    if (dst) return dst;

    Opnd *tauDivOk = 0;
    if(src2->getType()->isInteger()) {
        if (mod.getSignedModifier() == SignedOp) {
            // for CLI: if signed, insert a CheckDivOpnds before the divide
            tauDivOk = genTauCheckDivOpnds(src1, src2);
        } else {
            // if unsigned, still need a zero check
            tauDivOk = genTauCheckZero(src2);
        }
    } else {
        tauDivOk = genTauSafe(); // safe by construction
    }
    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyTauDiv(dstType, mod, src1, src2, tauDivOk);
    }
    if (!dst) {
        dst = createOpnd(dstType);
        appendInst(instFactory.makeTauDiv(mod, dst, src1, src2, tauDivOk));
    }
    insertHash(hashcode, src1, src2, dst->getInst()); // tauDivOk is not needed in hash
    return dst;
}

Opnd*
IRBuilder::genRem(Type* dstType, Modifier mod, Opnd* src1, Opnd* src2) {
    src1 = propagateCopy(src1);
    src2 = propagateCopy(src2);

    Operation operation(Op_TauRem, dstType->tag, mod);
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, src1, src2); // tauDivOk is not needed in hash
    if (dst) return dst;


    Opnd *tauDivOk = 0;
    if(src2->getType()->isInteger())
        tauDivOk = genTauCheckZero(src2);
    else
        tauDivOk = genTauSafe(); // safe by construction

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyTauRem(dstType, mod, src1, src2, tauDivOk);
    }
    if (!dst) {
        dst = createOpnd(dstType);
        appendInst(instFactory.makeTauRem(mod, dst, src1, src2, tauDivOk));
    }
    insertHash(hashcode, src1, src2, dst->getInst()); // tauDivOk is not needed in hash
    return dst;
}

//
// for CLI: inserts a CheckDivOpnds before the divide
//
Opnd*
IRBuilder::genCliRem(Type* dstType, Modifier mod, Opnd* src1, Opnd* src2) {
    src1 = propagateCopy(src1);
    src2 = propagateCopy(src2);

    Operation operation(Op_TauRem, dstType->tag, mod);
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, src1, src2); // tauDivOk is not needed in hash
    if (dst) return dst;


    Opnd *tauDivOk = 0;
    if (src2->getType()->isInteger())
        if (mod.getSignedModifier() == SignedOp)
            // for CLI: if signed, insert a CheckDivOpnds before the divide
            tauDivOk = genTauCheckDivOpnds(src1, src2);
        else
            // if unsigned, still need zero check
            tauDivOk = genTauCheckZero(src2);
    else
        tauDivOk = genTauSafe(); // safe by construction

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyTauRem(dstType, mod, src1, src2, tauDivOk);
    }

    if (!dst) {
        dst = createOpnd(dstType);
        appendInst(instFactory.makeTauRem(mod, dst, src1, src2, tauDivOk));
    }
    insertHash(hashcode, src1, src2, dst->getInst()); // tauDivOk is not needed in hash
    return dst;
}

Opnd*
IRBuilder::genNeg(Type* dstType, Opnd* src) {
    src = propagateCopy(src);
    Operation operation(Op_Neg, dstType->tag, Modifier());
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, src);
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyNeg(dstType, src);
    }
    if (!dst) {
        dst = createOpnd(dstType);
        appendInst(instFactory.makeNeg(dst, src));
    }
    insertHash(Op_Neg, src, dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genMulHi(Type* dstType, Modifier mod, Opnd* src1, Opnd* src2) {
    src1 = propagateCopy(src1);
    src2 = propagateCopy(src2);
    Operation operation(Op_MulHi, dstType->tag, mod);
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, src1, src2);
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyMulHi(dstType, mod, src1, src2);
    }
    if (!dst) {
        dst = createOpnd(dstType);
        appendInst(instFactory.makeMulHi(mod, dst, src1, src2));
    }
    insertHash(hashcode, src1, src2, dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genMin(Type* dstType, Opnd* src1, Opnd* src2) {
    if (irBuilderFlags.genMinMaxAbs &&
        (!dstType->isFloatingPoint() || irBuilderFlags.genFMinMaxAbs)) {

        src1 = propagateCopy(src1);
        src2 = propagateCopy(src2);

        Operation operation(Op_Min, dstType->tag, Modifier());
        uint32 hashcode = operation.encodeForHashing();
        Opnd* dst = lookupHash(hashcode, src1, src2);
        if (dst) return dst;
        
        if (irBuilderFlags.doSimplify) {
            dst = simplifier.simplifyMin(dstType, src1, src2);
        }
        if (!dst) {
            dst = createOpnd(dstType);
            appendInst(instFactory.makeMin(dst, src1, src2));
        }
        insertHash(hashcode, src1, src2, dst->getInst());
        return dst;
    } else {
        // hand-build it

        Type* cmpDstType = typeManager.getInt32Type();
        Type::Tag typeTag = dstType->tag;
        switch (typeTag) {
        case Type::Int32:
        case Type::Int64:
            {
                Opnd *cmpRes = genCmp(cmpDstType, typeTag,
                                      Cmp_GT, src2, src1);
                Opnd *res = genSelect(dstType, cmpRes, 
                                      src1, src2);
                return res;
            }
        case Type::Single:
        case Type::Double:
            {
                // check for NaN
                Opnd *src1IsNaN = genCmp(cmpDstType, typeTag,
                                         Cmp_NE_Un, 
                                         src1, src1);
                Opnd * zero = ((typeTag == Type::Single)
                               ? genLdConstant((float)0)
                               : genLdConstant((double)0));
                // we may have [+-]0.0, which can't be distinguished by cmp
                    Opnd *cmp2aRes = genCmp(cmpDstType, typeTag, 
                                            Cmp_EQ, src1, zero);
                    Opnd *cmp2bRes = genCmp(cmpDstType, typeTag, 
                                            Cmp_EQ, src2, zero);
                    Opnd *bothAreZero = genAnd(cmpDstType,
                                               cmp2aRes, cmp2bRes);
                    // but this expression apparently gets correct min
                    Opnd *minOfZeros =
                        genNeg(dstType,
                               genSub(dstType,
                                      Modifier(Overflow_None)|
                                      Modifier(Exception_Never)|Modifier(Strict_No),
                                      genNeg(dstType, src1),
                                      src2));
                    // otherwise, we can just use a simple min expression
                    Opnd *cmpRes = genCmp(cmpDstType, typeTag,
                                          Cmp_GT, src2, src1);
                    Opnd *simpleMin = genSelect(dstType, cmpRes, 
                                                          src1, src2);
                    Opnd *res =
                        genSelect(dstType,
                                  src1IsNaN,
                                  src1,
                                  genSelect(dstType,
                                            bothAreZero,
                                            minOfZeros,
                                            simpleMin));
                    return res;
            }
        default:
            break;
        }
        assert(0);
        return 0;
    }
}

Opnd*
IRBuilder::genMax(Type* dstType, Opnd* src1, Opnd* src2) {
    if (irBuilderFlags.genMinMaxAbs &&
        (!dstType->isFloatingPoint() || irBuilderFlags.genFMinMaxAbs)) {
        src1 = propagateCopy(src1);
        src2 = propagateCopy(src2);
        Operation operation(Op_Max, dstType->tag, Modifier());
        uint32 hashcode = operation.encodeForHashing();
        Opnd* dst = lookupHash(hashcode, src1, src2);
        if (dst) return dst;
        
        if (irBuilderFlags.doSimplify) {
            dst = simplifier.simplifyMax(dstType, src1, src2);
        }
        if (!dst) {
            dst = createOpnd(dstType);
            appendInst(instFactory.makeMax(dst, src1, src2));
        }
        insertHash(hashcode, src1, src2, dst->getInst());
        return dst;
    } else {
        Type::Tag typeTag = dstType->tag;
        Type* cmpDstType = typeManager.getInt32Type();
        switch (typeTag) {
        case Type::Int32:
        case Type::Int64:
            {
                Opnd *cmpRes = genCmp(cmpDstType, typeTag, Cmp_GT, src1, src2);
                Opnd *res = genSelect(dstType, cmpRes, src1, src2);
                return res;
            }
        case Type::Single:
        case Type::Double:
            {
                // check for NaN
                Opnd *src1IsNaN = genCmp(cmpDstType, typeTag,
                                         Cmp_NE_Un, src1, src1);
                Opnd * zero = ((typeTag == Type::Single)
                               ? genLdConstant((float)0)
                               : genLdConstant((double)0));
                // we may have [+-]0.0, which can't be distinguished by cmp
                Opnd *cmp2aRes = genCmp(cmpDstType, typeTag, Cmp_EQ, src1, zero);
                Opnd *cmp2bRes = genCmp(cmpDstType, typeTag, Cmp_EQ, src2, zero);
                Opnd *bothAreZero = genAnd(cmpDstType, cmp2aRes, cmp2bRes);
                // but this expression apparently gets correct max
                Opnd *maxOfZeros = genSub(dstType, 
                                          Modifier(Overflow_None)|
                                          Modifier(Exception_Never)|Modifier(Strict_No),
                                          src1,
                                          genNeg(dstType, src2));
                Opnd *cmpRes = genCmp(cmpDstType, typeTag,
                                      Cmp_GT, src1, src2);
                Opnd *simpleMin = genSelect(dstType, cmpRes, 
                                            src1, src2);
                Opnd *res = genSelect(dstType,
                                      src1IsNaN,
                                      src1,
                                      genSelect(dstType,
                                                bothAreZero,
                                                maxOfZeros,
                                                simpleMin));
                
                return res;
            }
        default:
            break;
        }
        assert(0);
        return 0;
    }
}

Opnd*
IRBuilder::genAbs(Type* dstType, Opnd* src1) {
    if (irBuilderFlags.genMinMaxAbs &&
        (!dstType->isFloatingPoint() || irBuilderFlags.genFMinMaxAbs)) {

        src1 = propagateCopy(src1);
        Operation operation(Op_Abs, dstType->tag, Modifier());
        uint32 hashcode = operation.encodeForHashing();
        Opnd* dst = lookupHash(hashcode, src1);
        if (dst) return dst;
        
        if (irBuilderFlags.doSimplify) {
            dst = simplifier.simplifyAbs(dstType, src1);
        }
        if (!dst) {
            dst = createOpnd(dstType);
            appendInst(instFactory.makeAbs(dst, src1));
        }
        insertHash(hashcode, src1, dst->getInst());
        return dst;
    } else {
        // hand-build it

        Type::Tag typeTag = src1->getType()->tag;
        Type* cmpDstType = typeManager.getInt32Type();
        switch (typeTag) {
        case Type::Int32:
        case Type::Int64:
            {
                Opnd *zero = ((typeTag == Type::Int32)
                              ? genLdConstant((int32)0)
                              : genLdConstant((int64)0));
                Opnd *cmpRes = genCmp(cmpDstType, typeTag, 
                                                Cmp_GT, zero, src1);
                Opnd *negSrc = genNeg(dstType, src1);
                Opnd *res = genSelect(dstType, cmpRes, negSrc, src1);
                return res;
            }
        case Type::Single:
        case Type::Double:
            {
                Opnd *zero = ((typeTag == Type::Single)
                              ? genLdConstant((float)0)
                              : genLdConstant((double)0));
                Opnd *cmpRes = genCmp(cmpDstType, typeTag, 
                                                Cmp_GTE, zero, src1);
                Opnd *negSrc = genSub(dstType,
                                                Modifier(Overflow_None)|
                                                Modifier(Exception_Never)|Modifier(Strict_No),
                                                zero,
                                                src1);
                Opnd *res = genSelect(dstType, cmpRes, negSrc, src1);
                return res;
            }
        default:
            break;
        }
        assert(0);
        return 0;
    }
}

// bitwise instructions
Opnd*
IRBuilder::genAnd(Type* dstType, Opnd* src1, Opnd* src2) {
    src1 = propagateCopy(src1);
    src2 = propagateCopy(src2);
    Opnd* dst = lookupHash(Op_And, src1, src2);
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyAnd(dstType, src1, src2);
    }
    if (!dst) {
        dst = createOpnd(dstType);
        appendInst(instFactory.makeAnd(dst, src1, src2));
    }
    insertHash(Op_And, src1, src2, dst->getInst());
    return dst;
}
Opnd*
IRBuilder::genOr(Type* dstType, Opnd* src1, Opnd* src2) {
    src1 = propagateCopy(src1);
    src2 = propagateCopy(src2);
    Opnd* dst = lookupHash(Op_Or, src1, src2);
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyOr(dstType, src1, src2);
    }
    if (!dst) {
        dst = createOpnd(dstType);
        appendInst(instFactory.makeOr(dst, src1, src2));
    }
    insertHash(Op_Or, src1, src2, dst->getInst());
    return dst;
}
Opnd*
IRBuilder::genXor(Type* dstType, Opnd* src1, Opnd* src2) {
    src1 = propagateCopy(src1);
    src2 = propagateCopy(src2);
    Opnd* dst = lookupHash(Op_Xor, src1, src2);
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyXor(dstType, src1, src2);
    }
    if (!dst) {
        dst = createOpnd(dstType);
        appendInst(instFactory.makeXor(dst, src1, src2));
    }
    insertHash(Op_Xor, src1, src2, dst->getInst());
    return dst;
}
Opnd*
IRBuilder::genNot(Type* dstType, Opnd* src) {
    src = propagateCopy(src);
    Opnd* dst = lookupHash(Op_Not, src);
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyNot(dstType, src);
    }
    if (!dst) {
        dst = createOpnd(dstType);
        appendInst(instFactory.makeNot(dst, src));
    }
    insertHash(Op_Not, src, dst->getInst());
    return dst;
}

// selection
Opnd*
IRBuilder::genSelect(Type* dstType, Opnd* src1, Opnd* src2, Opnd* src3) {
    src1 = propagateCopy(src1);
    src2 = propagateCopy(src2);
    src3 = propagateCopy(src3);
    Opnd* dst = lookupHash(Op_Select, src1, src2, src3);
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifySelect(dstType, src1, src2, src3);
    }
    if (!dst) {
        dst = createOpnd(dstType);
        appendInst(instFactory.makeSelect(dst, src1, src2, src3));
    }
    insertHash(Op_Select, src1, src2, src3, dst->getInst());
    return dst;
}

// Conversion
Opnd*
IRBuilder::genConv(Type* dstType,
                   Type::Tag toType,
                   Modifier ovfMod,
                   Opnd* src) {
    src = propagateCopy(src);
    Operation operation(Op_Conv, toType, ovfMod);
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, src->getId());
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyConv(dstType, toType, ovfMod, src);
    }
    if (!dst) {
        dst = createOpnd(dstType);
        appendInst(instFactory.makeConv(ovfMod, toType, dst, src));
    }
    insertHash(hashcode, src->getId(), dst->getInst());
    return dst;
}

// Shift
Opnd*
IRBuilder::genShladd(Type* dstType,
                     Opnd* value,
                     Opnd* shiftAmount,
                     Opnd* addTo) {
    value = propagateCopy(value);
    shiftAmount = propagateCopy(shiftAmount);
    addTo = propagateCopy(addTo);
    Opnd* dst = lookupHash(Op_Shladd, value, shiftAmount, addTo);
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyShladd(dstType, value, shiftAmount, addTo);
    }
    if (!dst) {
        dst = createOpnd(dstType);
        appendInst(instFactory.makeShladd(dst, value, shiftAmount, addTo));
    }
    insertHash(Op_Shladd, value, shiftAmount, addTo, dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genShl(Type* dstType,
                  Modifier mod,
                  Opnd* value,
                  Opnd* shiftAmount) {
    value = propagateCopy(value);
    shiftAmount = propagateCopy(shiftAmount);

    Operation operation(Op_Shladd, dstType->tag, mod);
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, value, shiftAmount);
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyShl(dstType, mod, value, shiftAmount);
    }
    if (!dst) {
        dst = createOpnd(dstType);
        appendInst(instFactory.makeShl(mod, dst, value, shiftAmount));
    }
    insertHash(hashcode, value, shiftAmount, dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genShr(Type* dstType,
                  Modifier mods,
                  Opnd* value,
                  Opnd* shiftAmount) {
    value = propagateCopy(value);
    shiftAmount = propagateCopy(shiftAmount);

    Operation operation(Op_Shr, dstType->tag, mods);
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, value, shiftAmount);
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyShr(dstType, mods, value, shiftAmount);
    }
    if (!dst) {
        dst = createOpnd(dstType);
        appendInst(instFactory.makeShr(mods, dst, value, shiftAmount));
    }
    insertHash(hashcode, value, shiftAmount, dst->getInst());
    return dst;
}

// Comparison with predicate result
Opnd*
IRBuilder::genPredCmp(Type* dstType,
                      Type::Tag instType, // source type for inst
                      ComparisonModifier mod,
                      Opnd* src1,
                      Opnd* src2) {
    src1 = propagateCopy(src1);
    src2 = propagateCopy(src2);
    
    Operation operation(Op_PredCmp, instType, mod);
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, src1, src2);
    if (dst) return dst;
    
    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyPredCmp(dstType, instType, mod, src1, src2);
    }
    if (!dst) {
        // result of comparison is always a 32-bit int
        dst = createOpnd(dstType);
        Inst *i = instFactory.makePredCmp(mod, instType, dst, src1, src2);
        appendInst(i);
    }
    insertHash(hashcode, src1, src2, dst->getInst());
    return dst;
}

// Comparison
Opnd*
IRBuilder::genCmp(Type* dstType,
                  Type::Tag instType, // source type for inst
                  ComparisonModifier mod,
                  Opnd* src1,
                  Opnd* src2) {
    src1 = propagateCopy(src1);
    src2 = propagateCopy(src2);

    Operation operation(Op_Cmp, instType, mod);
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, src1, src2);
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyCmp(dstType, instType, mod, src1, src2);
    }
    if (!dst) {
        // result of comparison is always a 32-bit int
        dst = createOpnd(dstType);
        Inst *i = instFactory.makeCmp(mod, instType, dst, src1, src2);
        appendInst(i);
    }
    insertHash(hashcode, src1, src2, dst->getInst());
    return dst;
}

// 3-way Java-like Comparison
//   effect is (src1 cmp src2) ? 1 : (src2 cmp src1) ? -1 : 0
// For Float or Double args, if Cmp_GT_Un, then second compare is Cmp_GT,
// and vice versa, so that
//     Cmp3(...,Cmp_GT_Un,src1,src2) -> 1 if src1 or src2 is NaN
//     Cmp3(...,Cmp_GT,src1,src2) -> -1 if src1 or src2 is NaN
Opnd*
IRBuilder::genCmp3(Type* dstType,
                   Type::Tag instType, // source type for inst
                   ComparisonModifier mod,
                   Opnd* src1,
                   Opnd* src2) {
    src1 = propagateCopy(src1);
    src2 = propagateCopy(src2);
    Operation operation(Op_Cmp3, instType, mod);
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, src1, src2);
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyCmp3(dstType, instType, mod, 
                                      src1, src2);
    }
    if (!dst) {
        // result of comparison is always a 32-bit int
        dst = createOpnd(dstType);
        Inst* i = instFactory.makeCmp3(mod, instType, dst, src1, src2);
        appendInst(i);
    }
    insertHash(hashcode, src1, src2, dst->getInst());
    return dst;
}

// Control flow
void
IRBuilder::genBranch(Type::Tag instType,
                     ComparisonModifier mod,
                     LabelInst* label,
                     Opnd* src1,
                     Opnd* src2) {
    src1 = propagateCopy(src1);
    src2 = propagateCopy(src2);
    if (mod > Cmp_GTE_Un)
        // bad modifier
        invalid();
    if (irBuilderFlags.doSimplify) {
        if (simplifier.simplifyBranch(instType, mod, label, src1, src2)) {
            // simplified branch was emitted;
            return;
        }
    }
    appendInst(instFactory.makeBranch(mod, instType, src1, src2, label));
}

void
IRBuilder::genBranch(Type::Tag instType,
                     ComparisonModifier mod,
                     LabelInst* label,
                     Opnd* src) {
    src = propagateCopy(src);
    if (mod < Cmp_Zero)
        // bad modifier
        invalid();
    if (irBuilderFlags.doSimplify) {
        if (simplifier.simplifyBranch(instType, mod, label, src)) {
            // simplified branch was emitted;
            return;
        }
    }
    appendInst(instFactory.makeBranch(mod, instType, src, label));
}

void
IRBuilder::genPredBranch(LabelInst* label,
                         Opnd* src) {
    src = propagateCopy(src);
    if (irBuilderFlags.doSimplify) {
        if (simplifier.simplifyPredBranch(label, src)) {
            // simplified branch was emitted;
            return;
        }
    }
    appendInst(instFactory.makePredBranch(src, label));
}


void
IRBuilder::genJump(LabelInst* label) {
    appendInst(instFactory.makeJump(label));
}

void
IRBuilder::genJSR(LabelInst* label) {
    appendInst(instFactory.makeJSR(label));
}

void
IRBuilder::genSwitch(uint32 nLabels,
                     LabelInst* labelInsts[],
                     LabelInst* defaultLabel,
                     Opnd* src) {
    src = propagateCopy(src);
    appendInst(instFactory.makeSwitch(src, nLabels, labelInsts, defaultLabel));
}

void
IRBuilder::genThrow(ThrowModifier mod, Opnd* exceptionObj) {
    exceptionObj = propagateCopy(exceptionObj);
    appendInst(instFactory.makeThrow(mod, exceptionObj));
}

void
IRBuilder::genThrowSystemException(CompilationInterface::SystemExceptionId id) {
    appendInst(instFactory.makeThrowSystemException(id));
}

void
IRBuilder::genThrowLinkingException(Class_Handle encClass, uint32 CPIndex, uint32 operation) {
    appendInst(instFactory.makeThrowLinkingException(encClass, CPIndex, operation));
}

Opnd*
IRBuilder::genCatch(Type* exceptionType) {
    Opnd* dst = createOpnd(exceptionType);
    appendInst(instFactory.makeCatch(dst));
    return dst;
}

Opnd*
IRBuilder::genSaveRet() {
    Opnd *dst = createOpnd(typeManager.getIntPtrType());
    appendInst(instFactory.makeSaveRet(dst));
    return dst;
}

void
IRBuilder::genEndFinally() {
    appendInst(instFactory.makeEndFinally());
}

void
IRBuilder::genEndFilter() {
    appendInst(instFactory.makeEndFilter());
}

void
IRBuilder::genEndCatch() {
    appendInst(instFactory.makeEndCatch());
}

void
IRBuilder::genLeave(LabelInst* label) {
    appendInst(instFactory.makeLeave(label));
}

Opnd*
IRBuilder::genPrefetch(Opnd *base, Opnd *offset, Opnd *hints) {
    Opnd *dst = createOpnd(typeManager.getVoidType());
    appendInst(instFactory.makePrefetch(propagateCopy(base), propagateCopy(offset),
                                        propagateCopy(hints)));
    return dst;
}

// Calls
Opnd*
IRBuilder::genDirectCall(MethodDesc* methodDesc,
                         Type* returnType,
                         Opnd* tauNullCheckedFirstArg,
                         Opnd* tauTypesChecked,
                         uint32 numArgs,
                         Opnd* args[],
                      	 InlineInfoBuilder* inlineInfoBuilder)      // NULL if this call is not inlined
{
    if (!tauNullCheckedFirstArg)
        tauNullCheckedFirstArg = genTauUnsafe();
    else
        tauNullCheckedFirstArg = propagateCopy(tauNullCheckedFirstArg);
    if (!tauTypesChecked)
        tauTypesChecked = genTauUnsafe(); 
    else
        tauTypesChecked = propagateCopy(tauTypesChecked);

    if (irBuilderFlags.expandCallAddrs) {
        return genIndirectMemoryCall(returnType, genLdFunAddrSlot(methodDesc), 
                                     tauNullCheckedFirstArg, tauTypesChecked,
                                     numArgs, args,
                                     inlineInfoBuilder); 
    }
    for (uint32 i=0; i<numArgs; i++) {
        args[i] = propagateCopy(args[i]);
    }
    Opnd* dst = createOpnd(returnType);
    appendInstUpdateInlineInfo(instFactory.makeDirectCall(dst,
                                          tauNullCheckedFirstArg, tauTypesChecked,
                                          numArgs, args,
                                          methodDesc),
                               inlineInfoBuilder,
                               methodDesc);

    // Note that type initialization should be made available for this type
    // and all its ancestor types.
    return dst;
}

Opnd*
IRBuilder::genTauVirtualCall(MethodDesc* methodDesc,
                             Type* returnType,
                             Opnd* tauNullCheckedFirstArg,
                             Opnd* tauTypesChecked,
                             uint32 numArgs,
                             Opnd* args[],
                      		 InlineInfoBuilder* inlineInfoBuilder)      // NULL if this call is not inlined
{
    if(!methodDesc->isVirtual())
        // Must de-virtualize - no vtable
        return genDirectCall(methodDesc, returnType,
                             tauNullCheckedFirstArg, tauTypesChecked, 
                             numArgs, args,
                             inlineInfoBuilder);
    for (uint32 i=0; i<numArgs; i++) {
        args[i] = propagateCopy(args[i]);
    }
    // callvirt can throw a null pointer exception
    if (!tauNullCheckedFirstArg || 
        (tauNullCheckedFirstArg->getInst()->getOpcode() == Op_TauUnsafe)) {
        // if no null check yet, do one
        tauNullCheckedFirstArg = genTauCheckNull(args[0]);
    } else {
        tauNullCheckedFirstArg = propagateCopy(tauNullCheckedFirstArg);
    }
    if (!tauTypesChecked || 
        (tauTypesChecked->getInst()->getOpcode() == Op_TauUnsafe)) {
        // if no type check available yet
        tauTypesChecked = genTauHasType(args[0], methodDesc->getParentType());
    } else {
        tauTypesChecked = propagateCopy(tauTypesChecked);
    }
    if (irBuilderFlags.doSimplify) {
        Opnd *dst = simplifier.simplifyTauVirtualCall(methodDesc,
                                                      returnType,
                                                      tauNullCheckedFirstArg,
                                                      tauTypesChecked,
                                                      numArgs,
                                                      args,
                                                      inlineInfoBuilder);
        if (dst) return dst;
    }
    
    if (irBuilderFlags.expandVirtualCallAddrs) {
        return genIndirectMemoryCall(returnType, 
                                     genTauLdVirtFunAddrSlot(args[0], 
                                                             tauNullCheckedFirstArg,
                                                             methodDesc), 
                                     tauNullCheckedFirstArg,
                                     tauTypesChecked,
                                     numArgs, args,
                                     inlineInfoBuilder);
    }
    Opnd *dst = createOpnd(returnType);
    appendInstUpdateInlineInfo(instFactory.makeTauVirtualCall(dst, 
                                              tauNullCheckedFirstArg,
                                              tauTypesChecked,
                                              numArgs, args, 
                                              methodDesc),
                                              inlineInfoBuilder,
                               methodDesc);
    return dst;
}

Opnd*
IRBuilder::genIntrinsicCall(IntrinsicCallId intrinsicId,
                            Type* returnType,
                            Opnd* tauNullCheckedRefArgs,
                            Opnd* tauTypesChecked,
                            uint32 numArgs,
                            Opnd*  args[]) {
    for (uint32 i=0; i<numArgs; i++) {
        args[i] = propagateCopy(args[i]);
    }
    Opnd * dst = createOpnd(returnType);
    tauNullCheckedRefArgs = propagateCopy(tauNullCheckedRefArgs);
    if (!tauTypesChecked) {
        assert(intrinsicId == CharArrayCopy);
        Opnd *tauTypesCheckedSrc = genTauHasType(args[0], args[0]->getType());
        Opnd *tauTypesCheckedDst = genTauHasType(args[2], args[2]->getType());
        tauTypesChecked = genTauAnd(tauTypesCheckedSrc,
                                    tauTypesCheckedDst);
    } else {
        tauTypesChecked = propagateCopy(tauTypesChecked);
    }

    appendInst(instFactory.makeIntrinsicCall(dst, intrinsicId, 
                                             tauNullCheckedRefArgs,
                                             tauTypesChecked,
                                             numArgs, args));
    return dst;
}

Opnd*
IRBuilder::genJitHelperCall(JitHelperCallId helperId,
                            Type* returnType,
                            uint32 numArgs,
                            Opnd*  args[]) {
    for (uint32 i=0; i<numArgs; i++) {
        args[i] = propagateCopy(args[i]);
    }
    Opnd * dst = createOpnd(returnType);
    appendInst(instFactory.makeJitHelperCall(dst, helperId, numArgs, args));
    return dst;
}

Opnd*
IRBuilder::genVMHelperCall(VMHelperCallId helperId,
                            Type* returnType,
                            uint32 numArgs,
                            Opnd*  args[]) {
    for (uint32 i=0; i<numArgs; i++) {
        args[i] = propagateCopy(args[i]);
    }
    Opnd * dst = createOpnd(returnType);
    appendInst(instFactory.makeVMHelperCall(dst, helperId, numArgs, args));
    return dst;
}

void
IRBuilder::genTauTypeCompare(Opnd *arg0, MethodDesc *methodDesc, LabelInst *target,
                             Opnd *tauNullChecked) {
    arg0 = propagateCopy(arg0);     // null check now is in genLdVTable()
    Type* type = methodDesc->getParentType();
    assert(type->isObject());
    // Note that we use the methodDesc's type to obtain the vtable which contains the pointer
    // to the method.  This may be an interface vtable.  genLdVTable figures out which.
    Opnd* vtableThis = genLdVTable(arg0, type);
    Opnd* vtableClass = createOpnd(typeManager.getVTablePtrType(type));
    appendInst(instFactory.makeGetVTableAddr(vtableClass, (ObjectType*)type));

    genBranch(Type::VTablePtr, Cmp_EQ, target, vtableThis, vtableClass);
}

Opnd*
IRBuilder::genIndirectCall(Type* returnType,
                           Opnd* funAddr,
                           Opnd* tauNullCheckedFirstArg,
                           Opnd* tauTypesChecked,
                           uint32 numArgs,
                           Opnd* args[],
                           InlineInfoBuilder* inlineInfoBuilder)      // NULL if this call is not inlined
{
    for (uint32 i=0; i<numArgs; i++) {
        args[i] = propagateCopy(args[i]);
    }
    Opnd* dst = createOpnd(returnType);

    if (!tauNullCheckedFirstArg)
        tauNullCheckedFirstArg = genTauUnsafe();
    else 
        tauNullCheckedFirstArg = propagateCopy(tauNullCheckedFirstArg);
    if (!tauTypesChecked)
		tauTypesChecked = genTauUnsafe(); 
    else
        tauTypesChecked = propagateCopy(tauTypesChecked);

    appendInstUpdateInlineInfo(instFactory.makeIndirectCall(dst, funAddr, 
                                            tauNullCheckedFirstArg, tauTypesChecked,
                                            numArgs, 
                                            args),
                                            inlineInfoBuilder,
                                            NULL); // indirect call -- no method desc
    return dst;
}

Opnd*
IRBuilder::genIndirectMemoryCall(Type* returnType,
                                 Opnd* funAddr,
                                 Opnd* tauNullCheckedFirstArg,
                                 Opnd* tauTypesChecked,
                                 uint32 numArgs,
                                 Opnd* args[],
                                 InlineInfoBuilder* inlineInfoBuilder)      // NULL if this call is not inlined
{
    for (uint32 i=0; i<numArgs; i++) {
        args[i] = propagateCopy(args[i]);
    }

    if (!tauNullCheckedFirstArg)
        tauNullCheckedFirstArg = genTauUnsafe();
    else 
        tauNullCheckedFirstArg = propagateCopy(tauNullCheckedFirstArg);
    if (!tauTypesChecked)
        tauTypesChecked = genTauUnsafe(); 
    else
        tauTypesChecked = propagateCopy(tauTypesChecked);

    Opnd* dst = createOpnd(returnType);
    appendInstUpdateInlineInfo(instFactory.makeIndirectMemoryCall(dst, funAddr, 
                                                  tauNullCheckedFirstArg, 
                                                  tauTypesChecked,
                                                  numArgs, 
                                                  args),
                                                  inlineInfoBuilder,
                                                  NULL); // indirect call -- no method desc
    return dst;
}

void
IRBuilder::genReturn(Opnd* src, Type* retType) {
    src = propagateCopy(src);
    if(Log::cat_opt()->isDebugEnabled()) {
        if (retType != src->getType()) {
            UNIMPLEMENTED("ret type check");
        }
    }
    appendInst(instFactory.makeReturn(src));
}

void
IRBuilder::genReturn() {
    appendInst(instFactory.makeReturn());
}

void
IRBuilder::genRet(Opnd* src) {
    appendInst(instFactory.makeRet(src));
}

// Move instruction
Opnd*
IRBuilder::genCopy(Opnd* src) {
    src = propagateCopy(src);
    Opnd* dst = createOpnd(src->getType());
    appendInst(instFactory.makeCopy(dst, src));
    return dst;
}

Opnd*
IRBuilder::genArgCoercion(Type* argType, Opnd* actualArg) {
    actualArg = propagateCopy(actualArg);
    if (actualArg->getType() == argType)
        return actualArg;
    return actualArg;
}

// actual parameter and variable definitions
Opnd*
IRBuilder::genArgDef(Modifier mod, Type* type) {
    Opnd* dst = opndManager.createArgOpnd(type);
    appendInst(instFactory.makeDefArg(mod, dst));
    DefArgModifier defMod = mod.getDefArgModifier();
    switch (defMod) {
    case NonNullThisArg:
        genTauIsNonNull(dst);
        break;
    case SpecializedToExactType:
        genTauHasExactType(dst, type);
        break;
    case DefArgBothModifiers:
        genTauIsNonNull(dst);
        genTauHasExactType(dst, type);
        break;
    case DefArgNoModifier:
        break;
    default:
        assert(0);
    }
    genTauHasType(dst, type);
    return dst;
}

VarOpnd*
IRBuilder::genVarDef(Type* type, bool isPinned) {
    return opndManager.createVarOpnd(type, isPinned);
}

// Phi-node instruction
Opnd*
IRBuilder::genPhi(uint32 numArgs, Opnd* args[]) {
    for (uint32 i=0; i<numArgs; i++) {
        args[i] = propagateCopy(args[i]);
    }
    Opnd* dst = createOpnd(args[0]->getType());
    appendInst(instFactory.makePhi(dst, numArgs, args));
    return dst;
}

// Pi-node instruction (splits live range for bounds analysis)
Opnd*
IRBuilder::genTauPi(Opnd *src, Opnd *tau, PiCondition *cond) {
    src = propagateCopy(src);
    tau = propagateCopy(tau);
    PiOpnd* dst = createPiOpnd(src);
    appendInst(instFactory.makeTauPi(dst, src, tau, cond));
    return dst;
}

// load instructions
Opnd*
IRBuilder::genLdConstant(int32 val) {
    Operation operation(Op_LdConstant, Type::Int32, Modifier());
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, (uint32) val);
    if (dst) return dst;
    dst = createOpnd(typeManager.getInt32Type());
    appendInst(instFactory.makeLdConst(dst, val));
    insertHash(hashcode, (uint32) val, dst->getInst());
    return dst;
}
Opnd*
IRBuilder::genLdConstant(int64 val) {
    Operation operation(Op_LdConstant, Type::Int64, Modifier());
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, (uint32) (val >> 32), (uint32) (val & 0xffffffff));
    if (dst) return dst;
    dst = createOpnd(typeManager.getInt64Type());
    appendInst(instFactory.makeLdConst(dst, val));
    insertHash(hashcode, (uint32) (val >> 32), (uint32) (val & 0xffffffff), dst->getInst());
    return dst;
}
Opnd* IRBuilder::genLdConstant(float val) {
    ConstInst::ConstValue cv;
    cv.s = val;
    uint32 word1 = cv.dword1;
    uint32 word2 = cv.dword2;
    Operation operation(Op_LdConstant, Type::Single, Modifier());
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, word1, word2);
    if (dst) return dst;
    dst = createOpnd(typeManager.getSingleType());
    appendInst(instFactory.makeLdConst(dst, val));
    insertHash(hashcode, word1, word2, dst->getInst());
    return dst;
}
Opnd*
IRBuilder::genLdConstant(double val) {
    ConstInst::ConstValue cv;
    cv.d = val;
    uint32 word1 = cv.dword1;
    uint32 word2 = cv.dword2;
    Operation operation(Op_LdConstant, Type::Double, Modifier());
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, word1, word2);
    if (dst) return dst;
    dst = createOpnd(typeManager.getDoubleType());
    appendInst(instFactory.makeLdConst(dst, val));
    insertHash(hashcode, word1, word2, dst->getInst());
    return dst;
}
Opnd*
IRBuilder::genLdConstant(Type *ptrtype, ConstInst::ConstValue val) {
    uint32 word1 = val.dword1;
    uint32 word2 = val.dword2;
    Operation operation(Op_LdConstant, ptrtype->tag, Modifier());
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, word1, word2);
    if (dst) return dst;
    dst = createOpnd(ptrtype);
    appendInst(instFactory.makeLdConst(dst, val));
    insertHash(hashcode, word1, word2, dst->getInst());
    return dst;
}
Opnd*
IRBuilder::genLdFloatConstant(double val) {
    ConstInst::ConstValue cv;
    cv.d = val;
    uint32 word1 = cv.dword1;
    uint32 word2 = cv.dword2;
    Operation operation(Op_LdConstant, Type::Float, Modifier());
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, word1, word2);
    if (dst) return dst;
    dst = createOpnd(typeManager.getFloatType());
    appendInst(instFactory.makeLdConst(dst, val));
    insertHash(hashcode, word1, word2, dst->getInst());
    return dst;
}
Opnd*
IRBuilder::genLdFloatConstant(float val) {
    ConstInst::ConstValue cv;
    cv.dword1 = 0;
    cv.dword2 = 0;
    cv.s = val;
    uint32 word1 = cv.dword1;
    uint32 word2 = cv.dword2;
    Operation operation(Op_LdConstant, Type::Float, Modifier());
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, word1, word2);
    if (dst) return dst;
    dst = createOpnd(typeManager.getFloatType());
    appendInst(instFactory.makeLdConst(dst, val));
    insertHash(hashcode, word1, word2, dst->getInst());
    return dst;
}
Opnd*
IRBuilder::genLdNull() {
    Operation operation(Op_LdConstant, Type::NullObject, Modifier());
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode);
    if (dst) return dst;
    dst = createOpnd(typeManager.getNullObjectType());
    appendInst(instFactory.makeLdNull(dst));
    insertHash(hashcode, dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genLdString(MethodDesc* enclosingMethod, uint32 stringToken) {
    bool uncompress = irBuilderFlags.compressedReferences;

    Modifier mod = uncompress ? AutoCompress_Yes : AutoCompress_No;
    Opnd* dst = createOpnd(typeManager.getSystemStringType());

    appendInst(instFactory.makeLdString(mod, dst, enclosingMethod, stringToken));
    return dst;
}

Opnd*
IRBuilder::genLdToken(MethodDesc* enclosingMethod, uint32 metadataToken) {
    Opnd* dst = createOpnd(typeManager.getSystemObjectType());
    appendInst(instFactory.makeLdToken(dst, enclosingMethod, metadataToken));
    return dst;
}

Opnd*
IRBuilder::genLdVar(Type* dstType, VarOpnd* var) {
    if (!var->isAddrTaken()) {
        Opnd *dst = lookupHash(Op_LdVar, var);
        if (dst) return dst;

        dst = createOpnd(dstType);
        appendInst(instFactory.makeLdVar(dst, var));
        insertHash(Op_LdVar, var, dst->getInst());
        return dst;
    } else {
        Opnd *dst = createOpnd(dstType);
        appendInst(instFactory.makeLdVar(dst, var));
        return dst;
    }
}

Opnd*
IRBuilder::genLdVarAddr(VarOpnd* var) {
    Opnd* dst = lookupHash(Op_LdVarAddr, var);
    if (dst) return dst;


    var->setAddrTaken();
    dst = createOpnd(typeManager.getManagedPtrType(var->getType()));
    appendInst(instFactory.makeLdVarAddr(dst, var));
    insertHash(Op_LdVarAddr, var, dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genLdInd(Type* type, Opnd *ptr)
{
    ptr = propagateCopy(ptr);
    Opnd *tauUnsafe = genTauUnsafe();
    bool uncompress = false;
    if (irBuilderFlags.compressedReferences && type->isObject()) {
        assert(!type->isCompressedReference());
        uncompress = true;
    }
    Modifier mod = uncompress ? AutoCompress_Yes : AutoCompress_No;
    Opnd *dst = genTauLdInd(mod, type, type->tag, ptr,
                            tauUnsafe, tauUnsafe);
    return dst;
}

Opnd*
IRBuilder::genTauLdInd(Modifier mod, Type* type, Type::Tag ldType, Opnd* ptr, 
                       Opnd *tauBaseNonNull, Opnd *tauAddressInRange) {
    ptr = propagateCopy(ptr);
    tauBaseNonNull = propagateCopy(tauBaseNonNull);
    tauAddressInRange = propagateCopy(tauAddressInRange);
    Opnd* dst = createOpnd(type);
    appendInst(instFactory.makeTauLdInd(mod, ldType, dst, ptr, 
                                        tauBaseNonNull, tauAddressInRange));
    return dst;
}

Opnd*
IRBuilder::genLdString(Modifier mod, Type* type, 
                       uint32 token, MethodDesc *enclosingMethod)
{
    Opnd* dst = createOpnd(type);
    appendInst(instFactory.makeLdString(mod, dst, enclosingMethod, token));
    return dst;
}

Opnd*
IRBuilder::genLdField(Type* type, Opnd* base, FieldDesc* fieldDesc) {
    if (fieldDesc->isStatic())
        return genLdStatic(type, fieldDesc);
    base = propagateCopy(base);
    Opnd *tauNullCheck = genTauCheckNull(base);
    Opnd *tauAddressInRange = 
        genTauHasType(base, fieldDesc->getParentType());
    
    bool uncompress = false;
    if (irBuilderFlags.compressedReferences && type->isObject()) {
        assert(!type->isCompressedReference());
        uncompress = true;
    }
    Modifier mod = uncompress ? AutoCompress_Yes : AutoCompress_No;
    if (irBuilderFlags.expandMemAddrs) {
        return genTauLdInd(mod, type, type->tag, 
                           genLdFieldAddrNoChecks(type, base, fieldDesc), 
                           tauNullCheck, tauAddressInRange);
    }

    Opnd* dst = createOpnd(type);
    appendInst(instFactory.makeTauLdField(mod, type, dst, base, 
                                          tauNullCheck, tauAddressInRange, 
                                          fieldDesc));
    return dst;
}

void
IRBuilder::genInitType(NamedType* type) {
    if (!type->needsInitialization())
        return;
    Opnd* opnd = lookupHash(Op_InitType, type->getId());
    if (opnd) return; // no need to re-initialize

    insertHash(Op_InitType, type->getId(), 
               appendInst(instFactory.makeInitType(type)));
}

Opnd*
IRBuilder::genLdStatic(Type* type, FieldDesc* fieldDesc) {
    bool uncompress = false;
    if (irBuilderFlags.compressedReferences && type->isObject()) {
        assert(!type->isCompressedReference());
        uncompress = true;
    }
    Modifier mod = uncompress ? AutoCompress_Yes : AutoCompress_No;

    genInitType(fieldDesc->getParentType());
    if (irBuilderFlags.expandMemAddrs) {
        Opnd *tauOk = genTauSafe(); // static field, always safe
        return genTauLdInd(mod, type, type->tag, genLdStaticAddr(type, fieldDesc),
                           tauOk, tauOk);
    }

    Opnd* dst = createOpnd(type);
    appendInst(instFactory.makeLdStatic(mod, type, dst, fieldDesc));
    return dst;
}


Opnd*
IRBuilder::genLdElem(Type* type, Opnd* array, Opnd* index) {
    array = propagateCopy(array);
    index = propagateCopy(index);

    bool uncompress = false;
    if (irBuilderFlags.compressedReferences && type->isObject()) {
        assert(!type->isCompressedReference());
        uncompress = true;
    }
    Modifier mod = uncompress ? AutoCompress_Yes : AutoCompress_No;

    Opnd *tauNullCheck = genTauCheckNull(array);
    Opnd *tauBoundsChecked = genTauCheckBounds(array, index, tauNullCheck);
    Opnd *tauBaseTypeChecked = genTauHasType(array, array->getType());
    Opnd *tauAddressInRange = genTauAnd(tauBoundsChecked, tauBaseTypeChecked);
    
    if (irBuilderFlags.expandMemAddrs) {
        return genTauLdInd(mod, type, type->tag, 
                           genLdElemAddrNoChecks(type, array, index),
                           tauNullCheck, tauAddressInRange);
    }
    Opnd* dst = createOpnd(type);
    appendInst(instFactory.makeTauLdElem(mod, type, dst, array, index,
                                         tauNullCheck, tauAddressInRange));
    return dst;
}

// this is now used just for CLI; the tauNonNull operand is ignored, but the
// check should remain even after optimization. 
Opnd*
IRBuilder::genLdFieldAddr(Type* type, Opnd* base, FieldDesc* fieldDesc) {
    if (fieldDesc->isStatic()) {
        assert(0);
        return genLdStaticAddr(type, fieldDesc);
    }

    base = propagateCopy(base);

    // generate a null check if the field is not static
        ((base->getType()->isObject())
         ? genTauCheckNull(base)
         : genTauUnsafe());
    
    Opnd* dst = lookupHash(Op_LdFieldAddr, base->getId(), fieldDesc->getId());
    if (dst) return dst;

    if (base->getType()->isIntPtr()) {
        // unmanaged pointer
        dst = createOpnd(typeManager.getIntPtrType());
    } else if (irBuilderFlags.compressedReferences && type->isObject()) {
        // until VM type system is upgraded,
        // fieldDesc type will have uncompressed ref type;
        // compress it
        assert(!type->isCompressedReference());
        Type *compressedType = typeManager.compressType(type);
        dst = createOpnd(typeManager.getManagedPtrType(compressedType));
    } else {
        dst = createOpnd(typeManager.getManagedPtrType(type));
    }
    appendInst(instFactory.makeLdFieldAddr(dst, base, fieldDesc));
    insertHash(Op_LdFieldAddr, base->getId(), fieldDesc->getId(), 
               dst->getInst());
    return dst;
}

// null check isn't needed for this address calculation
Opnd*
IRBuilder::genLdFieldAddrNoChecks(Type* type, Opnd* base, FieldDesc* fieldDesc) {
    if (fieldDesc->isStatic()) {
        assert(0);
        return genLdStaticAddrNoChecks(type, fieldDesc);
    }

    base = propagateCopy(base);

    Opnd* dst = lookupHash(Op_LdFieldAddr, base->getId(), fieldDesc->getId());
    if (dst) return dst;

    if (base->getType()->isIntPtr()) {
        // unmanaged pointer
        dst = createOpnd(typeManager.getIntPtrType());
    } else if (irBuilderFlags.compressedReferences && type->isObject()) {
        // until VM type system is upgraded,
        // fieldDesc type will have uncompressed ref type;
        // compress it
        assert(!type->isCompressedReference());
        Type *compressedType = typeManager.compressType(type);
        dst = createOpnd(typeManager.getManagedPtrType(compressedType));
    } else {
        dst = createOpnd(typeManager.getManagedPtrType(type));
    }
    appendInst(instFactory.makeLdFieldAddr(dst, base, fieldDesc));
    insertHash(Op_LdFieldAddr, base->getId(), fieldDesc->getId(), 
               dst->getInst());
    return dst;
}


Opnd*
IRBuilder::genLdStaticAddr(Type* type, FieldDesc* fieldDesc) {
    genInitType(fieldDesc->getParentType());
    return genLdStaticAddrNoChecks(type, fieldDesc);
}

Opnd*
IRBuilder::genLdStaticAddrNoChecks(Type* type, FieldDesc* fieldDesc) {
    Opnd* dst = lookupHash(Op_LdStaticAddr, fieldDesc->getId());
    if (dst) return dst;

    if (fieldDesc->isUnmanagedStatic()) {
        // can't mark an unmanaged pointer as non-null
        dst = createOpnd(typeManager.getIntPtrType());
    } else if (irBuilderFlags.compressedReferences && type->isObject()) {
        // until VM type system is upgraded,
        // fieldDesc type will have uncompressed ref type;
        // compress it
        assert(!type->isCompressedReference());
        Type *compressedType = typeManager.compressType(type);
        dst = createOpnd(typeManager.getManagedPtrType(compressedType));
    } else {
        dst = createOpnd(typeManager.getManagedPtrType(type));
    }
    appendInst(instFactory.makeLdStaticAddr(dst, fieldDesc));
    insertHash(Op_LdStaticAddr, fieldDesc->getId(), dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genLdElemAddr(Type* elemType, Opnd* array, Opnd* index) {
    // null and bounds checks
    index = propagateCopy(index);
    array = propagateCopy(array);
    Opnd *tauNullChecked = genTauCheckNull(array);
    genTauCheckBounds(array, index, tauNullChecked);
    return genLdElemAddrNoChecks(elemType, array, index);
}

Opnd*
IRBuilder::genLdElemAddrNoChecks(Type* elemType, Opnd* array, Opnd* index) {
    Opnd* dst;
    if (irBuilderFlags.expandElemAddrs) {
        //
        // boundscheck array, index
        // ldarraybase array --> base
        // addindex    base, index --> dst
        //
        return genAddScaledIndex(genLdArrayBaseAddr(elemType, array), index);
    } else {
        //
        // Op_LdElemAddr
        //
        dst = lookupHash(Op_LdElemAddr, array, index);
        if (dst) return dst;

        if (irBuilderFlags.compressedReferences && elemType->isObject()) {
            // until VM type system is upgraded,
            // fieldDesc type will have uncompressed ref type;
            // compress it
            assert(!elemType->isCompressedReference());
            Type *compressedType = typeManager.compressType(elemType);
            dst = createOpnd(typeManager.getManagedPtrType(compressedType));
        } else {
            dst = createOpnd(typeManager.getManagedPtrType(elemType));
        }
        appendInst(instFactory.makeLdElemAddr(elemType, dst, array, index));
        insertHash(Op_LdElemAddr, array, index, dst->getInst());
    }

    return dst;
}

Opnd*
IRBuilder::genLdFunAddr(MethodDesc* methodDesc) {
    Opnd* dst = lookupHash(Op_LdFunAddr, methodDesc->getId());
    if (dst) return dst;

    dst = createOpnd(typeManager.getMethodPtrType(methodDesc));
    appendInst(instFactory.makeLdFunAddr(dst, methodDesc));
    insertHash(Op_LdFunAddr, methodDesc->getId(), dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genLdFunAddrSlot(MethodDesc* methodDesc) {
    Opnd* dst = lookupHash(Op_LdFunAddrSlot, methodDesc->getId());
    if (dst) return dst;


    dst = createOpnd(typeManager.getMethodPtrType(methodDesc));
    appendInst(instFactory.makeLdFunAddrSlot(dst, methodDesc));
    insertHash(Op_LdFunAddrSlot, methodDesc->getId(), dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genLdVTable(Opnd* base, Type* type) {
    base = propagateCopy(base);
    Opnd *tauNullChecked = genTauCheckNull(base);

    return genTauLdVTable(base, tauNullChecked, type);
}

Opnd*
IRBuilder::genTauLdVTable(Opnd* base, Opnd *tauNullChecked, Type* type) {
    base = propagateCopy(base);

    SsaOpnd* obj = base->asSsaOpnd();
    assert(obj);
    Opnd* dst = NULL;
    if (type->isInterface()) {
        dst = lookupHash(Op_TauLdIntfcVTableAddr, base->getId(), type->getId());
        if (dst) return dst;

        if (irBuilderFlags.useNewTypeSystem) {
            NamedType* iType = type->asNamedType();
            assert(iType);
            dst = createOpnd(typeManager.getITablePtrObjType(obj, iType));
        } else {
            dst = createOpnd(typeManager.getVTablePtrType(type));
        }
        appendInst(instFactory.makeTauLdIntfcVTableAddr(dst, base, tauNullChecked, type));
        insertHash(Op_TauLdIntfcVTableAddr, base->getId(), type->getId(),
                   dst->getInst());
    } else if (type->isClass()) {
        dst = lookupHash(Op_TauLdVTableAddr, base);
        if (dst) return dst;

        if (irBuilderFlags.useNewTypeSystem) {
            dst = createOpnd(typeManager.getVTablePtrObjType(obj));
        } else {
            dst = createOpnd(typeManager.getVTablePtrType(base->getType()));
        }
        appendInst(instFactory.makeTauLdVTableAddr(dst, base, tauNullChecked));
        insertHash(Op_TauLdVTableAddr, base, dst->getInst());
    } else {
        assert(0); // shouldn't happen
    }
    return dst;
}

Opnd*
IRBuilder::genGetVTable(ObjectType* type) {
    assert(type->isClass() && (!type->isAbstract() || type->isArray()));
    Opnd* dst = lookupHash(Op_GetVTableAddr, type->getId());
    if (dst) return dst;

    dst = createOpnd(typeManager.getVTablePtrType(type));
    appendInst(instFactory.makeGetVTableAddr(dst, type));
    insertHash(Op_GetVTableAddr, type->getId(), dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genLdVirtFunAddr(Opnd* base, MethodDesc* methodDesc) {
    base = propagateCopy(base);
    Opnd* dst = lookupHash(Op_TauLdVirtFunAddr, base->getId(), 
                           methodDesc->getId());
    if (dst) return dst;

    Opnd *tauNullChecked = genTauCheckNull(base);
    Type *methodType = methodDesc->getParentType();
    Opnd* vtableOpnd = genTauLdVTable(base, tauNullChecked, methodType);
    Opnd *tauVtableHasMethod = genTauHasType(base, methodType);

    dst = createOpnd(typeManager.getMethodPtrType(methodDesc));
    appendInst(instFactory.makeTauLdVirtFunAddr(dst, vtableOpnd, 
                                                tauVtableHasMethod,
                                                methodDesc));
    insertHash(Op_TauLdVirtFunAddr, vtableOpnd->getId(), methodDesc->getId(), 
               dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genTauLdVirtFunAddrSlot(Opnd* base, Opnd *tauOk, MethodDesc* methodDesc) {
    base = propagateCopy(base);
    Opnd* dst = lookupHash(Op_TauLdVirtFunAddrSlot, base->getId(), 
                           methodDesc->getId());
    if (dst) return dst;

    Opnd *tauNullChecked = genTauCheckNull(base);
    Opnd* vtableOpnd = genTauLdVTable(base, tauNullChecked, methodDesc->getParentType());
    Opnd *tauVtableHasMethod = tauOk;

    if (irBuilderFlags.useNewTypeSystem) {
        SsaOpnd* obj = base->asSsaOpnd();
        assert(obj);
        dst = createOpnd(typeManager.getMethodPtrObjType(obj, methodDesc));
    } else {
        dst = createOpnd(typeManager.getMethodPtrType(methodDesc));
    }
    appendInst(instFactory.makeTauLdVirtFunAddrSlot(dst, vtableOpnd, 
                                                    tauVtableHasMethod,
                                                    methodDesc));
    insertHash(Op_TauLdVirtFunAddrSlot, vtableOpnd->getId(), 
               methodDesc->getId(), dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genArrayLen(Type* dstType, Type::Tag type, Opnd* array) {
    array = propagateCopy(array);
    
    Opnd* dst = lookupHash(Op_TauArrayLen, array->getId());
    if (dst) return dst;
    
    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyTauArrayLen(dstType, type, array);
        if (dst) return dst;
    }

    Opnd *tauNonNull = genTauCheckNull(array);
    Opnd *tauIsArray = genTauHasType(array, array->getType());
    
    return genTauArrayLen(dstType, type, array, tauNonNull, tauIsArray);
}

Opnd*
IRBuilder::genTauArrayLen(Type* dstType, Type::Tag type, Opnd* array,
                          Opnd* tauNullChecked, Opnd *tauTypeChecked) {
    array = propagateCopy(array);

    Opnd* dst = lookupHash(Op_TauArrayLen, array->getId());
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyTauArrayLen(dstType, type, array, tauNullChecked,
                                             tauTypeChecked);
        if (dst) return dst;
    }
    dst = createOpnd(dstType);
    appendInst(instFactory.makeTauArrayLen(dst, type, array, tauNullChecked,
                                           tauTypeChecked));
    insertHash(Op_TauArrayLen, array->getId(), dst->getInst());
    
    return dst;
}

Opnd*
IRBuilder::genLdArrayBaseAddr(Type* elemType, Opnd* array) {
    array = propagateCopy(array);

    Opnd* dst = lookupHash(Op_LdArrayBaseAddr, array);
    if (dst) return dst;

    if (irBuilderFlags.useNewTypeSystem) {
        SsaOpnd* arrayVal = array->asSsaOpnd();
        assert(arrayVal);
        Type* baseType = typeManager.getArrayBaseType(arrayVal);
        dst = createOpnd(baseType);
    } else {
        if (irBuilderFlags.compressedReferences && elemType->isObject()) {
            // until VM type system is upgraded,
            // fieldDesc type will have uncompressed ref type;
            // compress it
            assert(!elemType->isCompressedReference());
            Type *compressedType = typeManager.compressType(elemType);
            dst = createOpnd(typeManager.getManagedPtrType(compressedType));
        } else {        
            dst = createOpnd(typeManager.getManagedPtrType(elemType));
        }
    }
    appendInst(instFactory.makeLdArrayBaseAddr(elemType, dst, array));
    insertHash(Op_LdArrayBaseAddr, array, dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genAddScaledIndex(Opnd* ptr, Opnd* index) {
    ptr = propagateCopy(ptr);
    index = propagateCopy(index);
    Opnd* dst = lookupHash(Op_AddScaledIndex, ptr, index);
    if (dst) return dst;

    if (irBuilderFlags.useNewTypeSystem) {
        PtrType* ptrType = ptr->getType()->asPtrType();
        assert(ptrType);
        SsaOpnd* indexVar = index->asSsaOpnd();
        assert(indexVar);
        Type* dstType = typeManager.getArrayIndexType(ptrType->getArrayName(), indexVar);
        dst = createOpnd(dstType);
    } else {
        dst = createOpnd(ptr->getType());
    }

    appendInst(instFactory.makeAddScaledIndex(dst, ptr, index));
    insertHash(Op_AddScaledIndex, ptr, index, dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genScaledDiffRef(Opnd* src1, Opnd* src2) {
    src1 = propagateCopy(src1);
    src2 = propagateCopy(src2);
    Opnd* dst = lookupHash(Op_ScaledDiffRef, src1, src2);
    if (dst) return dst;

    dst = createOpnd(typeManager.getInt32Type());
    appendInst(instFactory.makeScaledDiffRef(dst, src1, src2));
    insertHash(Op_ScaledDiffRef, src1, src2, dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genUncompressRef(Opnd *compref)
{
    compref = propagateCopy(compref);
    Opnd* dst = lookupHash(Op_UncompressRef, compref);
    if (dst) return dst;

    Type *comprefType = compref->getType();
    assert(comprefType->isCompressedReference());

    dst = createOpnd(typeManager.uncompressType(comprefType));
    appendInst(instFactory.makeUncompressRef(dst, compref));
    insertHash(Op_UncompressRef, compref, dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genCompressRef(Opnd *uncompref)
{
    Type *uncomprefType = uncompref->getType();

    uncompref = propagateCopy(uncompref);
    Opnd* dst = lookupHash(Op_CompressRef, uncompref);
    if (dst) return dst;

    uncomprefType = uncompref->getType();
    assert(uncomprefType->isReference() && !uncomprefType->isCompressedReference());
    
    dst = createOpnd(typeManager.compressType(uncomprefType));
    appendInst(instFactory.makeCompressRef(dst, uncompref));
    insertHash(Op_CompressRef, uncompref, dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genLdFieldOffset(FieldDesc* fieldDesc)
{
    assert(!fieldDesc->isStatic());

    Opnd *dst = lookupHash(Op_LdFieldOffset, fieldDesc->getId());
    if (dst) return dst;


    dst = createOpnd(typeManager.getOffsetType());
    appendInst(instFactory.makeLdFieldOffset(dst, fieldDesc));
    insertHash(Op_LdFieldOffset, fieldDesc->getId(), dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genLdFieldOffsetPlusHeapbase(FieldDesc* fieldDesc)
{
    assert(!fieldDesc->isStatic());

    Opnd *dst = lookupHash(Op_LdFieldOffsetPlusHeapbase, fieldDesc->getId());
    if (dst) return dst;


    dst = createOpnd(typeManager.getOffsetPlusHeapbaseType());
    appendInst(instFactory.makeLdFieldOffsetPlusHeapbase(dst, fieldDesc));
    insertHash(Op_LdFieldOffsetPlusHeapbase, fieldDesc->getId(), dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genLdArrayBaseOffset(Type *elemType)
{
    Opnd* dst = lookupHash(Op_LdArrayBaseOffset, elemType->getId());
    if (dst) return dst;

    dst = createOpnd(typeManager.getOffsetType());
    appendInst(instFactory.makeLdArrayBaseOffset(dst, elemType));
    insertHash(Op_LdArrayBaseOffset, elemType->getId(), dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genLdArrayBaseOffsetPlusHeapbase(Type *elemType)
{
    Opnd* dst = lookupHash(Op_LdArrayBaseOffsetPlusHeapbase, elemType->getId());
    if (dst) return dst;

    dst = createOpnd(typeManager.getOffsetPlusHeapbaseType());
    appendInst(instFactory.makeLdArrayBaseOffsetPlusHeapbase(dst, elemType));
    insertHash(Op_LdArrayBaseOffsetPlusHeapbase, elemType->getId(), dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genLdArrayLenOffset(Type *elemType)
{
    Opnd* dst = lookupHash(Op_LdArrayLenOffset, elemType->getId());
    if (dst) return dst;

    dst = createOpnd(typeManager.getOffsetType());
    appendInst(instFactory.makeLdArrayLenOffset(dst, elemType));
    insertHash(Op_LdArrayLenOffset, elemType->getId(), dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genLdArrayLenOffsetPlusHeapbase(Type *elemType)
{
    Opnd* dst = lookupHash(Op_LdArrayLenOffsetPlusHeapbase, elemType->getId());
    if (dst) return dst;

    dst = createOpnd(typeManager.getOffsetPlusHeapbaseType());
    appendInst(instFactory.makeLdArrayLenOffsetPlusHeapbase(dst, elemType));
    insertHash(Op_LdArrayLenOffsetPlusHeapbase, elemType->getId(), dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genAddOffset(Type *ptrType, Opnd* ref, Opnd* offset)
{
    ref = propagateCopy(ref);
    offset = propagateCopy(offset);
    Opnd* dst = lookupHash(Op_AddOffset, ref, offset);
    if (dst) return dst;


    assert(!ref->getType()->isCompressedReference());
    assert(offset->getType()->isOffset());

    dst = createOpnd(ptrType);
    appendInst(instFactory.makeAddOffset(dst, ref, offset));
    insertHash(Op_AddOffset, ref, offset, dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genAddOffsetPlusHeapbase(Type *ptrType, Opnd* compref, Opnd* offset)
{
    compref = propagateCopy(compref);
    offset = propagateCopy(offset);
    Opnd* dst = lookupHash(Op_AddOffsetPlusHeapbase, compref, offset);
    if (dst) return dst;


    assert(compref->getType()->isCompressedReference());
    assert(offset->getType()->isOffsetPlusHeapbase());
    
    dst = createOpnd(ptrType);
    appendInst(instFactory.makeAddOffsetPlusHeapbase(dst, compref, offset));
    insertHash(Op_AddOffsetPlusHeapbase, compref, offset, dst->getInst());
    return dst;
}

// store instructions
void
IRBuilder::genStVar(VarOpnd* var, Opnd* src) {
    src = propagateCopy(src);
    appendInst(instFactory.makeStVar(var, src));
    if (irBuilderFlags.doCSE) {
        insertHash(Op_LdVar, var->getId(), src->getInst());
    }
}

void
IRBuilder::genStInd(Type* type,
                    Opnd* ptr,
                    Opnd* src) {
    ptr = propagateCopy(ptr);
    src = propagateCopy(src);

    Type *ptrType = ptr->getType();
    assert(ptrType->isPtr() || ptrType->isIntPtr());
    Type *fieldType = ((PtrType *)ptrType)->getPointedToType();
    bool compress = (fieldType->isCompressedReference() &&
                     !type->isCompressedReference());
    Modifier compressMod = Modifier(compress ? AutoCompress_Yes
                                    : AutoCompress_No);
    
    Opnd *tauUnsafe = genTauUnsafe();

    if (irBuilderFlags.insertWriteBarriers) {
        appendInst(instFactory.makeTauStInd((Modifier(Store_WriteBarrier)|
                                             compressMod),
                                            type->tag, src, ptr, 
                                            tauUnsafe, tauUnsafe, tauUnsafe));
    } else {
        appendInst(instFactory.makeTauStInd((Modifier(Store_NoWriteBarrier)|
                                             compressMod),
                                            type->tag, src, ptr,
                                            tauUnsafe, tauUnsafe, tauUnsafe));
    }
}

void
IRBuilder::genTauStInd(Type* type,
                       Opnd* ptr,
                       Opnd* src,
                       Opnd* tauBaseNonNull, 
                       Opnd *tauAddressInRange,
                       Opnd* tauElemTypeChecked) {
    ptr = propagateCopy(ptr);
    src = propagateCopy(src);

    Type *ptrType = ptr->getType();
    assert(ptrType->isPtr() || ptrType->isIntPtr());
    Type *fieldType = ((PtrType *)ptrType)->getPointedToType();
    if (fieldType->isArrayElement()) {
        fieldType = fieldType->getNonValueSupertype();
    }
    bool compress = (fieldType->isCompressedReference() &&
                     !type->isCompressedReference());
    Modifier compressMod = Modifier(compress ? AutoCompress_Yes
                                    : AutoCompress_No);
    
    if (irBuilderFlags.insertWriteBarriers) {
        appendInst(instFactory.makeTauStInd((Modifier(Store_WriteBarrier)|
                                             compressMod),
                                            type->tag, src, ptr, 
                                            tauBaseNonNull, tauAddressInRange, tauElemTypeChecked));
    } else {
        appendInst(instFactory.makeTauStInd((Modifier(Store_NoWriteBarrier)|
                                             compressMod),
                                            type->tag, src, ptr,
                                            tauBaseNonNull, tauAddressInRange, tauElemTypeChecked));
    }
}

void
IRBuilder::genTauStRef(Type* type, Opnd *objectbase, Opnd* ptr, Opnd* src,
                       Opnd *tauBaseNonNull, Opnd *tauAddressInRange,
                       Opnd *tauElemTypeChecked) {
    objectbase = propagateCopy(objectbase);
    ptr = propagateCopy(ptr);
    src = propagateCopy(src);
    tauBaseNonNull = propagateCopy(tauBaseNonNull);
    tauAddressInRange = propagateCopy(tauAddressInRange);
    tauElemTypeChecked = propagateCopy(tauElemTypeChecked);

    Type *ptrType = ptr->getType();
    assert(ptrType->isPtr());
    Type *fieldType = ((PtrType *)ptrType)->getPointedToType();
    if (fieldType->isArrayElement()) {
        fieldType = fieldType->getNonValueSupertype();
    }
    bool compress = (fieldType->isCompressedReference() &&
                     !type->isCompressedReference());
    Modifier compressMod = Modifier(compress ? AutoCompress_Yes
                                    : AutoCompress_No);

    if (irBuilderFlags.insertWriteBarriers) {
        appendInst(instFactory.makeTauStRef((Modifier(Store_WriteBarrier)|
                                             compressMod),
                                            type->tag, src, objectbase, ptr,
                                            tauBaseNonNull, tauAddressInRange, 
                                            tauElemTypeChecked));
    } else {
        appendInst(instFactory.makeTauStRef((Modifier(Store_NoWriteBarrier)|
                                             compressMod),
                                            type->tag, src, objectbase, ptr,
                                            tauBaseNonNull, tauAddressInRange, 
                                            tauElemTypeChecked));
    }
}

void
IRBuilder::genStField(Type* type,
                      Opnd* base,
                      FieldDesc* fieldDesc,
                      Opnd* src) {
    if (fieldDesc->isStatic()) {
		assert(0); 
        genStStatic(type, fieldDesc, src);
        return;
    }
    base = propagateCopy(base);
    src = propagateCopy(src);
    Opnd *tauBaseNonNull = genTauCheckNull(base);
    Opnd *tauBaseTypeIsOk = genTauHasType(base, fieldDesc->getParentType());
    Type *fieldType = fieldDesc->getFieldType();
    Opnd *tauStoredTypeIsOk = (fieldType->isObject()
                               ? genTauHasType(src, fieldType)
                               : genTauSafe()); // safe, not an object
    if (irBuilderFlags.expandMemAddrs) { // do not expand ldField of stack values
        Opnd *ptr = genLdFieldAddr(type, base, fieldDesc);
        if (irBuilderFlags.insertWriteBarriers) {
            genTauStRef(type, base, ptr, src, 
                        tauBaseNonNull, 
                        tauBaseTypeIsOk,
                        tauStoredTypeIsOk); 
        } else {
            genTauStInd(type, ptr, src, 
                        tauBaseNonNull, 
                        tauBaseTypeIsOk,
                        tauStoredTypeIsOk);
        }
    } else {
        if (irBuilderFlags.insertWriteBarriers &&
            base->getType()->isValue()==false) {
            appendInst(instFactory.makeTauStField((Modifier(Store_WriteBarrier)|
                                                   Modifier(AutoCompress_Yes)), 
                                                  type->tag, src, base,
                                                  tauBaseNonNull, 
                                                  tauBaseTypeIsOk,
                                                  tauStoredTypeIsOk,
                                                  fieldDesc));
        } else {
            appendInst(instFactory.makeTauStField((Modifier(Store_NoWriteBarrier)|
                                                   Modifier(AutoCompress_Yes)), 
                                                  type->tag, src, base,
                                                  tauBaseNonNull, 
                                                  tauBaseTypeIsOk,
                                                  tauStoredTypeIsOk,
                                                  fieldDesc));
        }
    }
}

void
IRBuilder::genStStatic(Type* type, FieldDesc* fieldDesc, Opnd* src) {
    src = propagateCopy(src);
    genInitType(fieldDesc->getParentType());
    Opnd *tauOk = genTauSafe(); // address is always ok
    Type *fieldType = fieldDesc->getFieldType();
    Opnd *tauTypeIsOk = (fieldType->isObject() 
                         ? genTauHasType(src, fieldType)
                         : genTauSafe()); // safe, not an object
    if (irBuilderFlags.expandMemAddrs) {
        genTauStInd(type, genLdStaticAddr(type, fieldDesc), src,
                    tauOk, 
                    tauOk,
                    tauTypeIsOk // safety may depend on a type check
                    );
        return;
    }
    if (irBuilderFlags.insertWriteBarriers) {
        appendInst(instFactory.makeTauStStatic((Modifier(Store_WriteBarrier)|
                                                Modifier(AutoCompress_Yes)),
                                               type->tag, src,
                                               tauTypeIsOk,
                                               fieldDesc));
    } else {
        appendInst(instFactory.makeTauStStatic((Modifier(Store_NoWriteBarrier)|
                                                Modifier(AutoCompress_Yes)), 
                                               type->tag, src,
                                               tauTypeIsOk,
                                               fieldDesc));
    }
}

void
IRBuilder::genStElem(Type* elemType,
                     Opnd* array,
                     Opnd* index,
                     Opnd* src) {
    array = propagateCopy(array);
    src = propagateCopy(src);
    index = propagateCopy(index);
    // check elem type
    Opnd *tauNullChecked = genTauCheckNull(array);
    Opnd *tauBaseTypeChecked = genTauHasType(array, array->getType());
    Opnd *tauElemTypeChecked = 0;
    Opnd *tauBoundsChecked = genTauCheckBounds(array, index, tauNullChecked);
    Opnd *tauAddressInRange = genTauAnd(tauBaseTypeChecked,
                                        tauBoundsChecked);
    if (elemType->isObject()) {
        tauElemTypeChecked = genTauCheckElemType(array, src, tauNullChecked,
                                                 tauBaseTypeChecked);
    } else {
        tauElemTypeChecked = genTauSafe(); // src type is ok if non-object
    }
    if (irBuilderFlags.expandMemAddrs) {
        Opnd *ptr = genLdElemAddr(elemType, array, index);
        if (irBuilderFlags.insertWriteBarriers) {
            genTauStRef(elemType, array, ptr, src, tauNullChecked, tauAddressInRange,
                        tauElemTypeChecked);
        } else {
            genTauStInd(elemType, ptr, src, tauNullChecked, tauAddressInRange,
                        tauElemTypeChecked);
        }
    } else {
        if (irBuilderFlags.insertWriteBarriers) {
            appendInst(instFactory.makeTauStElem((Modifier(Store_WriteBarrier)|
                                                  Modifier(AutoCompress_Yes)), 
                                                 elemType->tag, src, array, index,
                                                 tauNullChecked, 
                                                 tauAddressInRange,
                                                 tauElemTypeChecked));
        } else {
            appendInst(instFactory.makeTauStElem((Modifier(Store_NoWriteBarrier)|
                                                  Modifier(AutoCompress_Yes)), 
                                                 elemType->tag, src, array, index,
                                                 tauNullChecked, 
                                                 tauAddressInRange,
                                                 tauElemTypeChecked));
        }
    }
}

Opnd*
IRBuilder::genNewObj(Type* type) {
    Opnd* dst = createOpnd(type);
    appendInst(instFactory.makeNewObj(dst, type));
    return dst;
}

Opnd*
IRBuilder::genNewArray(NamedType* elemType, Opnd* numElems) {
    numElems = propagateCopy(numElems);
    Opnd* dst = createOpnd(typeManager.getArrayType(elemType));
    appendInst(instFactory.makeNewArray(dst, numElems, elemType));
    return dst;
}

Opnd*
IRBuilder::genMultianewarray(NamedType* arrayType,
                             uint32 dimensions,
                             Opnd** numElems) {
    NamedType* elemType = arrayType;
    // create an array of arrays type
    for (uint32 i=0; i<dimensions; i++) {
        elemType = ((ArrayType*)elemType)->getElementType();
    }
    Opnd* dst = createOpnd(arrayType);
    appendInst(instFactory.makeNewMultiArray(dst, dimensions, numElems, elemType));
    return dst;
}

void
IRBuilder::genMonitorEnter(Opnd* src) {
    src = propagateCopy(src);
    Opnd *tauNullChecked = genTauCheckNull(src);
    appendInst(instFactory.makeTauMonitorEnter(src, tauNullChecked));
}

void
IRBuilder::genMonitorExit(Opnd* src) {
    src = propagateCopy(src);
    Opnd *tauNullChecked = genTauCheckNull(src);
    appendInst(instFactory.makeTauMonitorExit(src, tauNullChecked));
}

Opnd*
IRBuilder::genLdLockAddr(Type* dstType, Opnd* obj) {
    obj = propagateCopy(obj);
    Opnd* dst = lookupHash(Op_LdLockAddr, obj);
    if (dst) return dst;

    dst = createOpnd(dstType);
    appendInst(instFactory.makeLdLockAddr(dst, obj));
    insertHash(Op_LdLockAddr, obj, dst->getInst());
    return dst;
}               

void
IRBuilder::genIncRecCount(Opnd* obj, Opnd *oldLock) {
    obj = propagateCopy(obj);
    oldLock = propagateCopy(oldLock);
    appendInst(instFactory.makeLdLockAddr(obj, oldLock));
}               


Opnd*
IRBuilder::genBalancedMonitorEnter(Type* dstType, Opnd* src, Opnd *lockAddr) {
    // src should already have been checked for null
    src = propagateCopy(src);
    lockAddr = propagateCopy(lockAddr);
    Opnd *tauNullChecked = genTauCheckNull(src);
    return genTauBalancedMonitorEnter(dstType, src, lockAddr, tauNullChecked);
}

Opnd*
IRBuilder::genTauBalancedMonitorEnter(Type* dstType, Opnd* src, Opnd *lockAddr,
                                      Opnd* tauNullChecked) {
    // src should already have been checked for null
    src = propagateCopy(src);
    lockAddr = propagateCopy(lockAddr);
    Opnd *dst = createOpnd(dstType);
    appendInst(instFactory.makeTauBalancedMonitorEnter(dst, src, lockAddr,
                                                       tauNullChecked));
    return dst;
}

void
IRBuilder::genBalancedMonitorExit(Opnd* src, Opnd *lockAddr, Opnd *oldValue) {
    // src should already have been checked for null
    src = propagateCopy(src);
    appendInst(instFactory.makeBalancedMonitorExit(src, lockAddr, oldValue));
}

Opnd*
IRBuilder::genTauOptimisticBalancedMonitorEnter(Type* dstType, Opnd* src, 
                                                Opnd *lockAddr,
                                                Opnd *tauNullChecked) {
    // src should already have been checked for null
    src = propagateCopy(src);
    lockAddr = propagateCopy(lockAddr);
    Opnd *dst = createOpnd(dstType);
    appendInst(instFactory.makeTauOptimisticBalancedMonitorEnter(dst, src, 
                                                                 lockAddr,
                                                                 tauNullChecked));
    return dst;
}

void
IRBuilder::genMonitorEnterFence(Opnd* src) {
    src = propagateCopy(src);
    appendInst(instFactory.makeMonitorEnterFence(src));
}

void
IRBuilder::genMonitorExitFence(Opnd* src) {
    src = propagateCopy(src);
    appendInst(instFactory.makeMonitorExitFence(src));
}

void
IRBuilder::genTypeMonitorEnter(Type* type) {
    appendInst(instFactory.makeTypeMonitorEnter(type));
}

void
IRBuilder::genTypeMonitorExit(Type* type) {
    appendInst(instFactory.makeTypeMonitorExit(type));
}


// type checking
// CastException (succeeds if argument is null, returns casted object)
Opnd*
IRBuilder::genCast(Opnd* src, Type* castType) {
    src = propagateCopy(src);
    Opnd* dst = lookupHash(Op_TauCast, src->getId(), castType->getId());
    if (dst) return dst;

    Opnd *tauCheckedCast = lookupHash(Op_TauCheckCast, src->getId(), castType->getId());
    if (!tauCheckedCast) {
        Opnd *tauNullChecked = lookupHash(Op_TauCheckNull, src->getId());
        if (!tauNullChecked) {
            tauNullChecked = genTauUnsafe();
        }

        tauCheckedCast = genTauCheckCast(src, tauNullChecked, castType);
    }
    
    dst = genTauStaticCast(src, tauCheckedCast, castType);
    insertHash(Op_TauCast, src->getId(), castType->getId(), dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genTauCheckCast(Opnd* src, Opnd *tauNullChecked, Type* castType) {
    src = propagateCopy(src);
    tauNullChecked = propagateCopy(tauNullChecked);
    Opnd* dst = lookupHash(Op_TauCheckCast, src->getId(), castType->getId());
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        bool alwaysThrows = false;
        dst = simplifier.simplifyTauCheckCast(src, tauNullChecked, castType, alwaysThrows);
        if (dst) {
            return dst;
        }
    }
    
    dst = createOpnd(typeManager.getTauType());
    appendInst(instFactory.makeTauCheckCast(dst, src, tauNullChecked, castType));
    insertHash(Op_TauCheckCast, src->getId(), castType->getId(), dst->getInst());
    return dst;
}

// returns src if src is an instance of type, NULL otherwise
Opnd*
IRBuilder::genAsType(Opnd* src, Type* type) {
    if (type->isUserValue()) {
		assert(0);    
    }
    src = propagateCopy(src);

    Opnd* tauCheckedNull = genTauUnsafe();

    Opnd* dst = lookupHash(Op_TauAsType, src->getId(), tauCheckedNull->getId(), type->getId());
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyTauAsType(src, tauCheckedNull, type);
        if (dst) return dst;
    }
    dst = createOpnd(type);
    appendInst(instFactory.makeTauAsType(dst, src, tauCheckedNull, type));
    insertHash(Op_TauAsType, src->getId(), tauCheckedNull->getId(), type->getId(), dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genInstanceOf(Opnd* src, Type* type) {
    src = propagateCopy(src);

    Opnd *tauNullChecked = genTauUnsafe();

    Opnd* dst = lookupHash(Op_TauInstanceOf, src->getId(), 
                           tauNullChecked->getId(), type->getId());
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        dst = simplifier.simplifyTauInstanceOf(src, tauNullChecked, type);
        if (dst) return dst;
    }

    dst = createOpnd(typeManager.getInt32Type());
    appendInst(instFactory.makeTauInstanceOf(dst, src, tauNullChecked, type));
    insertHash(Op_TauInstanceOf, src->getId(), type->getId(), 
               tauNullChecked->getId(), dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genSizeOf(Type* type) {
    Opnd* dst = createOpnd(typeManager.getUInt32Type());
    appendInst(instFactory.makeSizeof(dst, type));
    return dst;
}

Opnd*
IRBuilder::genUnbox(Type* type, Opnd* obj) {
    assert(type->isValue());
    Opnd *src = propagateCopy(obj);
    genTauCheckNull(obj);
    Opnd *two = genCast(src, typeManager.getObjectType(((NamedType*)type)->getVMTypeHandle()));
    Opnd* dst = createOpnd(typeManager.getManagedPtrType(type));
    appendInst(instFactory.makeUnbox(dst, two, type));
    return dst;
}

Opnd*
IRBuilder::genBox(Type* type, Opnd* val) {
    assert(type->isValue());
    val = propagateCopy(val);
    Opnd* dst = createOpnd(typeManager.getObjectType(((NamedType*)type)->getVMTypeHandle()));
    appendInst(instFactory.makeBox(dst, val, type));
    return dst;
}

void
IRBuilder::genCopyObj(Type* type, Opnd* dstValPtr, Opnd* srcValPtr) {
    appendInst(instFactory.makeCopyObj(dstValPtr, srcValPtr, type));
}

void
IRBuilder::genInitObj(Type* type, Opnd* valPtr) {
    appendInst(instFactory.makeInitObj(valPtr, type));
}

Opnd*
IRBuilder::genLdObj(Type* type, Opnd* addrOfValObj) {
    Opnd* dst = createOpnd(type);
    appendInst(instFactory.makeLdObj(dst, addrOfValObj, type));
    return dst;
}

void
IRBuilder::genStObj(Opnd* addrOfDstVal, Opnd* srcVal, Type* type) {
    appendInst(instFactory.makeStObj(addrOfDstVal, srcVal, type));
}

void
IRBuilder::genCopyBlock(Opnd* dstAddr, Opnd* srcAddr, Opnd* size) {
    assert(0);
}

void
IRBuilder::genInitBlock(Opnd* dstAddr, Opnd* srcAddr, Opnd* size) {
    assert(0);
}

Opnd*
IRBuilder::genLocAlloc(Opnd* size) {
    assert(0);
    return NULL;;
}

Opnd*
IRBuilder::genArgList() {
    assert(0);
    return NULL;
}

Opnd*
IRBuilder::genMkRefAny(Type* type, Opnd* ptr) {
    assert(0);
    return NULL;
}

Opnd*
IRBuilder::genRefAnyType(Opnd* typedRef) {
    assert(0);
    return NULL;
}

Opnd*
IRBuilder::genRefAnyVal(Type* type, Opnd* typedRef) {
    assert(0);
    return NULL;
}

//-----------------------------------------------------------------------------
//
// Private helper methods for generating instructions
//
//-----------------------------------------------------------------------------
Opnd*
IRBuilder::propagateCopy(Opnd* opnd) {
    return simplifier.propagateCopy(opnd);
}

Opnd*    IRBuilder::createOpnd(Type* type) {
    if (type->tag == Type::Void)
        return OpndManager::getNullOpnd();
    return opndManager.createSsaTmpOpnd(type);
}

PiOpnd*    IRBuilder::createPiOpnd(Opnd *org) {
    return opndManager.createPiOpnd(org);
}

Opnd* IRBuilder::genTauCheckNull(Opnd* base) {
    base = propagateCopy(base);
    if (! irBuilderFlags.expandNullChecks) {
        assert(0); // not expanding them is not compatible with taus
        return base;
    }

    Opnd* res = lookupHash(Op_TauCheckNull, base);
    if (res) return res;

    // Not advisable to turn off simplification of checknull because
    // IRBuilder calls genTauCheckNull redundantly many times
    bool alwaysThrows = false;
    res = simplifier.simplifyTauCheckNull(base, alwaysThrows);
    if (res && (res->getInst()->getOpcode() != Op_TauUnsafe)) return res;
    Opnd* dst = createOpnd(typeManager.getTauType());
    Inst *inst = appendInst(instFactory.makeTauCheckNull(dst, base));
    insertHash(Op_TauCheckNull, base, inst);

    // We can make the type init for the base object available here
    Type* baseType = base->getType();
    insertHash(Op_InitType, baseType->getId(), inst);
    return dst;
}

Opnd* IRBuilder::genTauCheckZero(Opnd* src) {
    src = propagateCopy(src);


    Opnd* res = lookupHash(Op_TauCheckZero, src);
    if (res) return res;

    bool alwaysThrows = false;
    res = simplifier.simplifyTauCheckZero(src, alwaysThrows);
    if (res && (res->getInst()->getOpcode() != Op_TauUnsafe)) return res;

    Opnd* dst = createOpnd(typeManager.getTauType());
    appendInst(instFactory.makeTauCheckZero(dst, src));
    insertHash(Op_TauCheckZero, src, dst->getInst());
    return dst;
}

Opnd *IRBuilder::genTauCheckDivOpnds(Opnd* src1, Opnd *src2) {
    src1 = propagateCopy(src1);
    src2 = propagateCopy(src2);


    Opnd* res = lookupHash(Op_TauCheckDivOpnds, src1, src2);
    if (res) return res;

    bool alwaysThrows = false;
    res = simplifier.simplifyTauCheckDivOpnds(src1, src2, alwaysThrows);
    if (res && (res->getInst()->getOpcode() != Op_TauUnsafe)) return res;

    Opnd* dst = createOpnd(typeManager.getTauType());
    Inst *inst = appendInst(instFactory.makeTauCheckDivOpnds(dst, src1, src2));
    insertHash(Op_TauCheckDivOpnds, src1, src2, inst);
    return dst;
}

Opnd *IRBuilder::genTauCheckBounds(Opnd* array, Opnd* index, Opnd *tauNullChecked) {
    // just to allow limit studies, omit all bounds checks 
    // if command-line flag is given
    if (irBuilderFlags.suppressCheckBounds)
        return genTauUnsafe(); 
    
    array = propagateCopy(array);
    index = propagateCopy(index);

    // we also hash operation with array as the opnd
    Opnd* res = lookupHash(Op_TauCheckBounds, array, index);
    if (res) return res;

    Opnd *tauArrayTypeChecked = genTauHasType(array, array->getType());
    Opnd* arrayLen = genTauArrayLen(typeManager.getInt32Type(), Type::Int32, array, 
                                    tauNullChecked, tauArrayTypeChecked);

    Opnd* dst = genTauCheckBounds(arrayLen, index);
    // we also hash operation with array as the opnd
    insertHash(Op_TauCheckBounds, array, index, dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genTauCheckElemType(Opnd* array, Opnd* src, Opnd *tauNullChecked,
                               Opnd *tauIsArray) {
    if (! irBuilderFlags.expandElemTypeChecks)
        return genTauUnsafe();

    array = propagateCopy(array);
    src = propagateCopy(src);
    Opnd* res = lookupHash(Op_TauCheckElemType, array, src);
    if (res) return res;

    if (irBuilderFlags.doSimplify) {
        bool alwaysThrows = false;
        res = simplifier.simplifyTauCheckElemType(array, src, alwaysThrows);
        if (res && (res->getInst()->getOpcode() != Op_TauUnsafe)) return res;
    }
    Opnd* dst = createOpnd(typeManager.getTauType());
    Inst* inst = appendInst(instFactory.makeTauCheckElemType(dst, array, src, 
                                                             tauNullChecked,
                                                             tauIsArray));
    insertHash(Op_TauCheckElemType, array, src, inst);
    return dst;
}

Opnd *
IRBuilder::genTauCheckBounds(Opnd* ub, Opnd *index) {
    index = propagateCopy(index);
    ub = propagateCopy(ub);

    Opnd* dst = lookupHash(Op_TauCheckBounds, ub, index);
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        bool alwaysThrows = false;
        dst = simplifier.simplifyTauCheckBounds(ub, index, alwaysThrows);
    }

    if (!(dst && (dst->getInst()->getOpcode() != Op_TauUnsafe))) {
        // need to create one
        dst = createOpnd(typeManager.getTauType());
        appendInst(instFactory.makeTauCheckBounds(dst, ub, index));
    }
    insertHash(Op_TauCheckBounds, ub, index, dst->getInst());
    return dst;
}

Opnd *
IRBuilder::genCheckFinite(Type *dstType, Opnd* src) {
    assert(dstType == src->getType());
    (void) genTauCheckFinite(src);
    return src;
}

Opnd *
IRBuilder::genTauCheckFinite(Opnd* src) {
    src = propagateCopy(src);
    Opnd* dst = lookupHash(Op_TauCheckFinite, src);
    if (dst) return dst;

    if (irBuilderFlags.doSimplify) {
        bool alwaysThrows = false;
        dst = simplifier.simplifyTauCheckFinite(src, alwaysThrows);
        if (dst && (dst->getInst()->getOpcode() != Op_TauUnsafe)) return dst;
    }
    
    dst = createOpnd(typeManager.getTauType());
    appendInst(instFactory.makeTauCheckFinite(dst, src));
    insertHash(Op_TauCheckFinite, src, dst->getInst());
    return dst;
}

//-----------------------------------------------------------------------------
//
// Methods for CSE hashing
//
//-----------------------------------------------------------------------------
Opnd* IRBuilder::lookupHash(uint32 opc) {
    if (! irBuilderFlags.doCSE)
        return NULL;
    Inst* inst = cseHashTable.lookup(opc);
    if (inst) 
        return inst->getDst();
    else 
        return NULL;
}

Opnd* IRBuilder::lookupHash(uint32 opc, uint32 op) {
    if (! irBuilderFlags.doCSE)
        return NULL;
    Inst* inst =  cseHashTable.lookup(opc, op);
    if (inst)
        return inst->getDst();
    else
        return NULL;
}

Opnd* IRBuilder::lookupHash(uint32 opc, uint32 op1, uint32 op2) {
    if (! irBuilderFlags.doCSE)
        return NULL;
    Inst* inst = cseHashTable.lookup(opc, op1, op2);
    if (inst)
        return inst->getDst();
    else
        return NULL;
}

Opnd* IRBuilder::lookupHash(uint32 opc, uint32 op1, uint32 op2, uint32 op3) {
    if (! irBuilderFlags.doCSE)
        return NULL;
    Inst* inst = cseHashTable.lookup(opc, op1, op2, op3);
    if (inst)
        return inst->getDst();
    else
        return NULL;
}

void IRBuilder::insertHash(uint32 opc, Inst* inst) {
    if (! irBuilderFlags.doCSE)
        return;
    cseHashTable.insert(opc, inst);
}

void IRBuilder::insertHash(uint32 opc, uint32 op1, Inst* inst) {
    if (! irBuilderFlags.doCSE)
        return;
    cseHashTable.insert(opc, op1, inst);
}

void IRBuilder::insertHash(uint32 opc, uint32 op1, uint32 op2, Inst* inst) {
    if (! irBuilderFlags.doCSE)
        return;
    cseHashTable.insert(opc, op1, op2, inst);
}

void IRBuilder::insertHash(uint32 opc, uint32 op1, uint32 op2, uint32 op3,
                           Inst* inst) {
    if (! irBuilderFlags.doCSE)
        return;
    cseHashTable.insert(opc, op1, op2, op3, inst);
}

// tau instructions
Opnd*
IRBuilder::genTauSafe() {
    Opnd* dst = lookupHash(Op_TauSafe);
    if (dst) return dst;

    dst = createOpnd(typeManager.getTauType());
    appendInst(instFactory.makeTauSafe(dst));

    insertHash(Op_TauSafe, dst->getInst());
    return dst;
}

// tau instructions
Opnd*
IRBuilder::genTauMethodSafe() {
    Opnd* dst = tauMethodSafeOpnd;
    if (dst) return dst;
    
    dst = createOpnd(typeManager.getTauType());
    Inst *inst = instFactory.makeTauPoint(dst);

    CFGNode *head = getFlowGraph().getEntry();
    Inst *entryLabel = head->getFirstInst();
    // first search for one already there
    Inst *where = entryLabel->next();
    while (where != entryLabel) {
        if (where->getOpcode() != Op_DefArg) {
            break;
        }
        where = where->next();
    }
    // insert before where
    inst->insertBefore(where);

    tauMethodSafeOpnd = dst;
    return dst;
}

Opnd*
IRBuilder::genTauUnsafe() {
    Operation operation(Op_TauUnsafe, Type::Tau, Modifier());
    Opnd* dst = lookupHash(Op_TauUnsafe);
    if (dst) return dst;
    
    dst = createOpnd(typeManager.getTauType());
    appendInst(instFactory.makeTauUnsafe(dst));

    insertHash(Op_TauUnsafe, dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genTauStaticCast(Opnd *src, Opnd *tauCheckedCast, Type *castType) {
    Operation operation(Op_TauStaticCast, castType->tag, Modifier());
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, src->getId(), tauCheckedCast->getId(), castType->getId());
    if (dst) return dst;
    
    dst = createOpnd(castType);
    appendInst(instFactory.makeTauStaticCast(dst, src, tauCheckedCast, castType));
    
    insertHash(hashcode, src->getId(), tauCheckedCast->getId(), castType->getId(), dst->getInst());
    Operation hasTypeOperation(Op_TauHasType, castType->tag, Modifier());
    uint32 hasTypeHashcode = hasTypeOperation.encodeForHashing();
    insertHash(hasTypeHashcode, src->getId(), castType->getId(), tauCheckedCast->getId(),
               dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genTauHasType(Opnd *src, Type *castType) {
    Operation operation(Op_TauHasType, castType->tag, Modifier());
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, src->getId(), castType->getId());
    if (dst) return dst;
    
    dst = createOpnd(typeManager.getTauType());
    appendInst(instFactory.makeTauHasType(dst, src, castType));
    
    insertHash(hashcode, src->getId(), castType->getId(), dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genTauHasExactType(Opnd *src, Type *castType) {
    Operation operation(Op_TauHasExactType, castType->tag, Modifier());
    uint32 hashcode = operation.encodeForHashing();
    Opnd* dst = lookupHash(hashcode, src->getId(), castType->getId());
    if (dst) return dst;
    
    dst = createOpnd(typeManager.getTauType());
    appendInst(instFactory.makeTauHasExactType(dst, src, castType));
    
    insertHash(hashcode, src->getId(), castType->getId(), dst->getInst());
    return dst;
}

Opnd*
IRBuilder::genTauIsNonNull(Opnd *src) {
    uint32 hashcode = Op_TauCheckNull;
    Opnd* dst = lookupHash(hashcode, src->getId());
    if (dst) return dst;
    
    dst = createOpnd(typeManager.getTauType());
    appendInst(instFactory.makeTauIsNonNull(dst, src));
    
    insertHash(hashcode, src->getId(), dst->getInst());

    // We can also make the type init for the base object available here
    Type* baseType = src->getType();
    insertHash(Op_InitType, baseType->getId(), dst->getInst());

    return dst;
}


Opnd*
IRBuilder::genTauAnd(Opnd *src1, Opnd *src2) {
    if (src1->getId() > src2->getId()) {
        Opnd *tmp = src1;
        src1 = src2;
        src2 = tmp;
    }
    Opnd* dst = lookupHash(Op_TauAnd, src1, src2);
    if (dst) return dst;

    dst = createOpnd(typeManager.getTauType());
    Opnd* srcs[2] = { src1, src2 };
    appendInst(instFactory.makeTauAnd(dst, 2, srcs));
    
    insertHash(Op_TauAnd, src1->getId(), src2->getId(), dst->getInst());
    return dst;
}

void
IRBuilder::appendInstUpdateInlineInfo(Inst* inst, InlineInfoBuilder* builder, MethodDesc* target_md)
{
    assert(inst->getCallInstInlineInfoPtr());

    if ( builder ) {
        builder->buildInlineInfoForInst(inst, target_md);
    }
    appendInst(inst);
}

Inst* IRBuilder::getLastGeneratedInst() {
    return currentLabel->prev();
}

} //namespace Jitrino 
