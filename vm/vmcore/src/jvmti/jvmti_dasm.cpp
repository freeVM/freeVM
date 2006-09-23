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
 * @author Alexander V. Astapchuk
 * @version $Revision: $
 */
/**
 * @file
 * @brief Disassembler for JVMTI implementation.
 */
#include "jvmti_dasm.h"
#include "dec_base.h"

static InstructionDisassembler::Register convertRegName2Register(RegName reg)
{
    switch(reg) {
    case RegName_Null:  return InstructionDisassembler::IA32_REG_NONE;
    case RegName_EAX:   return InstructionDisassembler::IA32_REG_EAX;
    case RegName_EBX:   return InstructionDisassembler::IA32_REG_EBX;
    case RegName_ECX:   return InstructionDisassembler::IA32_REG_ECX;
    case RegName_EDX:   return InstructionDisassembler::IA32_REG_EDX;
    case RegName_ESI:   return InstructionDisassembler::IA32_REG_ESI;
    case RegName_EDI:   return InstructionDisassembler::IA32_REG_EDI;
    case RegName_EBP:   return InstructionDisassembler::IA32_REG_EBP;
    case RegName_ESP:   return InstructionDisassembler::IA32_REG_ESP;
    default:
        // currently not supported and unexpected
        assert(false);
        break;
    }
    return (InstructionDisassembler::Register)-1;
}

static void convertOperand2Opnd(
    InstructionDisassembler::Opnd* pdst, 
    const EncoderBase::Operand& src)
{
    if (src.is_imm()) {
        pdst->kind = InstructionDisassembler::Kind_Imm;
        pdst->imm = (int)src.imm();
    }
    else if (src.is_mem()) {
        pdst->kind = InstructionDisassembler::Kind_Mem;
        pdst->base = convertRegName2Register(src.base());
        pdst->index = convertRegName2Register(src.index());
        pdst->disp = src.disp();
        pdst->scale = src.scale();
    }
    else {
        assert(src.is_reg());
        pdst->kind = InstructionDisassembler::Kind_Reg;
        pdst->reg = convertRegName2Register(src.reg());
    }
}

static const char* get_reg_value(
    InstructionDisassembler::Register reg,
    const Registers* pcontext)
{
    switch(reg) {
    case InstructionDisassembler::IA32_REG_NONE: return NULL;
    case InstructionDisassembler::IA32_REG_EAX:  return (const char*)pcontext->eax;
    case InstructionDisassembler::IA32_REG_EBX:  return (const char*)pcontext->ebx;
    case InstructionDisassembler::IA32_REG_ECX:  return (const char*)pcontext->ecx;
    case InstructionDisassembler::IA32_REG_EDX:  return (const char*)pcontext->edx;
    case InstructionDisassembler::IA32_REG_ESI:  return (const char*)pcontext->esi;
    case InstructionDisassembler::IA32_REG_EDI:  return (const char*)pcontext->edi;
    case InstructionDisassembler::IA32_REG_EBP:  return (const char*)pcontext->ebp;
    case InstructionDisassembler::IA32_REG_ESP:  return (const char*)pcontext->esp;
    default: assert(false);
    }
    return NULL;
}


void InstructionDisassembler::disasm(const NativeCodePtr addr, 
                                     InstructionDisassembler * pidi)
{
    assert(pidi != NULL);
    Inst inst;
    pidi->m_len = DecoderBase::decode(addr, &inst);
    if (pidi->m_len == 0) {
        // Something wrong happened
        pidi->m_type = OPCODEERROR;
        return;
    }
    
    pidi->m_type = UNKNOWN;
    pidi->m_argc = inst.argc;
    for (unsigned i=0; i<inst.argc; i++) {
        convertOperand2Opnd(&pidi->m_opnds[i], inst.operands[i]);
    }
    
    if (inst.mn == Mnemonic_CALL) {
        assert(pidi->m_argc == 1);
        if (inst.operands[0].is_imm()) {
            pidi->m_type = RELATIVE_CALL;
            pidi->m_target = (NativeCodePtr)((char*)addr + pidi->m_len + inst.operands[0].imm());
        }
        else {
            pidi->m_type = INDIRECT_CALL;
        }
    }
    else if (inst.mn == Mnemonic_JMP) {
        assert(pidi->m_argc == 1);
        if (inst.operands[0].is_imm()) {
            pidi->m_type = RELATIVE_JUMP;
            pidi->m_target = (NativeCodePtr)((char*)addr + pidi->m_len + inst.operands[0].imm());
        }
        else {
            pidi->m_type = INDIRECT_JUMP;
        }
    }
    else if (is_jcc(inst.mn)) {
        // relative Jcc is the only possible variant
        assert(pidi->m_argc == 1);
        assert(inst.odesc->opnds[0].kind == OpndKind_Imm);
        pidi->m_cond_jump_type = (CondJumpType)(inst.mn-Mnemonic_Jcc);
        assert(pidi->m_cond_jump_type < CondJumpType_Count);
        pidi->m_type = RELATIVE_COND_JUMP;
    }
}

NativeCodePtr 
InstructionDisassembler::get_target_address_from_context(const Registers* pcontext) const
{
    switch(get_type()) {
    case RELATIVE_JUMP:
    case RELATIVE_COND_JUMP:
    case RELATIVE_CALL:
        return m_target;
    case INDIRECT_JUMP:
    case INDIRECT_CALL:
        // Only CALL/JMP mem/reg expected - single argument
        assert(m_argc == 1);
        {
            const Opnd& op = get_opnd(0);
            if (op.kind == Kind_Reg) {
                assert(op.reg != IA32_REG_NONE);
                return (NativeCodePtr)get_reg_value(op.reg, pcontext);
            }
            else if (op.kind == Kind_Mem) {
                char* base = (char*)get_reg_value(op.base, pcontext);
                char* index = (char*)get_reg_value(op.index, pcontext);
                unsigned scale = op.scale;
                int disp = op.disp;
                char* targetAddrPtr = base + ((long)index)*scale + disp;
                return (NativeCodePtr)*(void**)targetAddrPtr;
            }
        }
        // 0th arg is neither memory, nor register, possibly immediate? 
        // can't happen for INDIRECT_xxx.
        assert(false);
        return NULL;
    default:
        // This method should not be called for non-branch instructions.
        assert(false);
    }
    return NULL;
}

