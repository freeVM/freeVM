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
 * @version $Revision: 1.1.2.3.4.4 $
 */
#include "enc_base.h"
#include <climits>
#include <string.h>
#include "enc_prvt.h"

/**
 * @file
 * @brief Main encoding routines and structures.
 */


#ifdef PLATFORM_POSIX
    #define strcmpi strcasecmp
#endif

#ifdef _EM64T_
// no support in this release, thus no init needed
int EncoderBase::dummy = 0;
#else
int EncoderBase::dummy = EncoderBase::buildTable();
#endif

const unsigned char EncoderBase::size_hash[OpndSize_64+1] = {
    //
    0xFF,   // OpndSize_Null        = 0,
    3,              // OpndSize_8           = 0x1,
    2,              // OpndSize_16          = 0x2,
    0xFF,   // 0x3
    1,              // OpndSize_32          = 0x4,
    0,              // OpndSize_64          = 0x8,
    //
};

const unsigned char EncoderBase::kind_hash[OpndKind_Mem+1] = {
    //
    //gp reg                -> 000 = 0
    //memory                -> 001 = 1
    //immediate             -> 010 = 2
    //xmm reg               -> 011 = 3
    //segment regs  -> 100 = 4
    //fp reg                -> 101 = 5
    //mmx reg               -> 110 = 6
    //
    0xFF,                          // 0    OpndKind_Null=0,
    0<<2,                          // 1    OpndKind_GPReg = 
                                   //           OpndKind_MinRegKind=0x1,
    4<<2,                          // 2    OpndKind_SReg=0x2,

#ifdef _HAVE_MMX_
    6<<2,                          // 3
#else
    0xFF,                          // 3
#endif

    5<<2,                          // 4    OpndKind_FPReg=0x4,
    0xFF, 0xFF, 0xFF,              // 5, 6, 7
    3<<2,                                   //      OpndKind_XMMReg=0x8,
    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 9, 0xA, 0xB, 0xC, 0xD, 
                                              // 0xE, 0xF
    0xFF,                          // OpndKind_MaxRegKind = 
                                   // OpndKind_StatusReg = 
                                   // OpndKind_OtherReg=0x10,
    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x11-0x18
    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,               // 0x19-0x1F
    2<<2,                                   // OpndKind_Immediate=0x20,
    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x21-0x28
    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x29-0x30
    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 0x31-0x38
    0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,               // 0x39-0x3F
    1<<2,                                   // OpndKind_Memory=0x40
};


char* EncoderBase::encode_aux(char* stream, unsigned aux,
                              const Operands& opnds, const OpcodeDesc * odesc,
                              unsigned * pargsCount)
{
    const unsigned byte = aux;
    OpcodeByteKind kind = (OpcodeByteKind)(byte & OpcodeByteKind_KindMask);
    // The '>>' here is to force the switch to be table-based) instead of 
    // set of CMP+Jcc.
    switch(kind>>8) {
    case OpcodeByteKind_SlashR>>8:
        // /r - Indicates that the ModR/M byte of the instruction contains 
        // both a register operand and an r/m operand.
        {
        assert(opnds.count() > 1);
        assert((odesc->opnds[0].kind & OpndKind_Mem) || 
               (odesc->opnds[1].kind & OpndKind_Mem));
        unsigned memidx = odesc->opnds[0].kind & OpndKind_Mem ? 0 : 1;
        unsigned regidx = memidx == 0 ? 1 : 0;
        memidx += *pargsCount;
        regidx += *pargsCount;
        ModRM& modrm = *(ModRM*)stream;
        if (opnds[memidx].is_mem()) {
            stream = encodeModRM(stream, opnds, memidx, odesc);
        }
        else {
            modrm.mod = 3; // 11
            modrm.rm = getHWRegIndex(opnds[memidx].reg());
            ++stream;
        }
        modrm.reg = getHWRegIndex(opnds[regidx].reg());
        *pargsCount += 2;
        }
        break;
    case OpcodeByteKind_SlashNum>>8:
        //  /digit - A digit between 0 and 7 indicates that the 
        //  ModR/M byte of the instruction uses only the r/m 
        //  (register or memory) operand. The reg field contains 
        //  the digit that provides an extension to the instruction's 
        //  opcode.
        {
        const unsigned lowByte = (byte & OpcodeByteKind_OpcodeMask);
        assert(lowByte <= 7);
        ModRM& modrm = *(ModRM*)stream;
        unsigned idx = *pargsCount;
        assert(opnds[idx].is_mem() || opnds[idx].is_reg());
        if (opnds[idx].is_mem()) {
            stream = encodeModRM(stream, opnds, idx, odesc);
        }
        else {
            modrm.mod = 3; // 11
            modrm.rm = getHWRegIndex(opnds[idx].reg());
            ++stream;
        }
        modrm.reg = (char)lowByte;
        *pargsCount += 1;
        }
        break;
    case OpcodeByteKind_plus_i>>8:
        //  +i - A number used in floating-point instructions when one 
        //  of the operands is ST(i) from the FPU register stack. The 
        //  number i (which can range from 0 to 7) is added to the 
        //  hexadecimal byte given at the left of the plus sign to form 
        //  a single opcode byte.
        {
            unsigned idx = *pargsCount;
            const unsigned lowByte = (byte & OpcodeByteKind_OpcodeMask);
            *stream = (char)lowByte + getRegIndex(opnds[idx].reg());
            ++stream;
            *pargsCount += 1;
        }
        break;
    case OpcodeByteKind_ib>>8:
    case OpcodeByteKind_iw>>8:
    case OpcodeByteKind_id>>8:
        //  ib, iw, id - A 1-byte (ib), 2-byte (iw), or 4-byte (id) 
        //  immediate operand to the instruction that follows the 
        //  opcode, ModR/M bytes or scale-indexing bytes. The opcode 
        //  determines if the operand is a signed value. All words 
        //  and double words are given with the low-order byte first.
        {
            unsigned idx = *pargsCount;
            *pargsCount += 1;
            assert(opnds[idx].is_imm());
            if (kind == OpcodeByteKind_ib) {
                *(unsigned char*)stream = (unsigned char)opnds[idx].imm();
                stream += 1;
            }
            else if (kind == OpcodeByteKind_iw) {
                *(unsigned short*)stream = (unsigned short)opnds[idx].imm();
                stream += 2;
            }
            else {
                assert(kind == OpcodeByteKind_id);
                *(unsigned*)stream = (unsigned)opnds[idx].imm();
                stream += 4;
            }
        }
        break;
    case OpcodeByteKind_cb>>8:
        assert(opnds.count() == 1 && opnds[*pargsCount].is_imm()); 
        *(unsigned char*)stream = (unsigned char)opnds[*pargsCount].imm();
        stream += 1;
        *pargsCount += 1;
        break;
    case OpcodeByteKind_cw>>8:
        assert(opnds.count() == 1 && opnds[*pargsCount].is_imm());
        *(unsigned short*)stream = (unsigned short)opnds[*pargsCount].imm();
        stream += 2;
        *pargsCount += 1;
        break;
    case OpcodeByteKind_cd>>8:
        assert(opnds.count() == 1 && opnds[*pargsCount].is_imm());
        *(unsigned*)stream = (unsigned)opnds[*pargsCount].imm();
        stream += 4;
        *pargsCount += 1;
        break;
    //OpcodeByteKind_cp                             = 0x0B00,
    //OpcodeByteKind_co                             = 0x0C00,
    //OpcodeByteKind_ct                             = 0x0D00,
    case OpcodeByteKind_rb>>8:
    case OpcodeByteKind_rw>>8:
    case OpcodeByteKind_rd>>8:
        //  +rb, +rw, +rd - A register code, from 0 through 7, 
        //  added to the hexadecimal byte given at the left of 
        //  the plus sign to form a single opcode byte.
        assert(opnds.count() > 0);
        assert(opnds[*pargsCount].is_reg());
        {
        const unsigned lowByte = (byte & OpcodeByteKind_OpcodeMask);
        *(unsigned char*)stream = (unsigned char)lowByte + 
                                   getHWRegIndex(opnds[*pargsCount].reg());
        ++stream;
        *pargsCount += 1;
        }
        break;
    default:
        assert(false);
        break;
    }
    return stream;
}

char * EncoderBase::encode(char * stream, Mnemonic mn, const Operands& opnds)
{
#ifdef _DEBUG
    if (opnds.count() > 0) {
        if (opnds[0].is_mem()) {
            assert(getRegKind(opnds[0].base()) != OpndKind_SReg);
        }
        else if (opnds.count() >1 && opnds[1].is_mem()) {
            assert(getRegKind(opnds[1].base()) != OpndKind_SReg);
        }
    }
#endif
#ifdef _EM64T_
    // No support for EM64T in this release
    //assert(false); 
    *(unsigned char*)stream = 0xCC;
    return stream+1;
#endif

    const OpcodeDesc * odesc = lookup(mn, opnds);
    if (odesc->opcode_len==1) {
      *(unsigned char*)stream = *(unsigned char*)&odesc->opcode;
    }
    else if (odesc->opcode_len==2) {
      *(unsigned short*)stream = *(unsigned short*)&odesc->opcode;
    }
    else if (odesc->opcode_len==3) {
      *(unsigned short*)stream = *(unsigned short*)&odesc->opcode;
      *(unsigned char*)(stream+2) = odesc->opcode[2];
    }
    else if (odesc->opcode_len==4) {
      *(unsigned*)stream = *(unsigned*)&odesc->opcode;
    }
    stream += odesc->opcode_len;

    unsigned argsCount = odesc->first_opnd;
    
    if (odesc->aux0) {
        stream = encode_aux(stream, odesc->aux0, opnds, odesc, &argsCount);
        if (odesc->aux1) {
            stream = encode_aux(stream, odesc->aux1, opnds, odesc, &argsCount);
        }
    }
    return stream;
}

char* EncoderBase::encodeModRM(char* stream, const Operands& opnds,
                               unsigned idx, const OpcodeDesc * odesc)
{
    const Operand& op = opnds[idx];
    assert(op.is_mem());

    ModRM& modrm = *(ModRM*)stream;
    ++stream;
    SIB& sib = *(SIB*)stream;

    // we need SIB if
    //      we have index & scale (nb: having index w/o base and w/o scale
    //      treated as error)
    //      the base is EBP w/o disp, BUT let's use a fake disp8 
    //      the base is ESP (nb: cant have ESP as index)

    RegName base = op.base();
    // only disp ?..
    if (base == RegName_Null && op.index() == RegName_Null) {
        assert(op.scale() == 0); // 'scale!=0' has no meaning without index
        // ... yes - only have disp
        // ignore disp_fits8, always use disp32.
        modrm.mod = 0;
        modrm.rm = 5;
        *(unsigned*)stream = (unsigned)op.disp();
        stream += 4;
        return stream;
    }
    
    const bool disp_fits8 = CHAR_MIN <= op.disp() && op.disp() <= CHAR_MAX;
    /*&& op.base() != RegName_Null - just checked above*/ 
    if (op.index() == RegName_Null && !equals(op.base(), REG_STACK)) {
        assert(op.scale() == 0); // 'scale!=0' has no meaning without index
        // ... luckily no SIB, only base and may be a disp
        
        // EBP base is a special case. Need to use [EBP] + disp8 form
        if (op.disp() == 0  && !equals(op.base(), RegName_EBP)) {
            modrm.mod = 0; // mod=00, no disp et all
        }
        else if (disp_fits8) {
            modrm.mod = 1; // mod=01, use disp8
            *(unsigned char*)stream = (unsigned char)op.disp();
            ++stream;
        }
        else {
            modrm.mod = 2; // mod=10, use disp32
            *(unsigned*)stream = (unsigned)op.disp();
            stream += 4;
        }
        modrm.rm = getHWRegIndex(op.base());
        return stream;
    }
    
    // cool, we do have SIB.
    ++stream; // bypass SIB in stream
    
    // {E|R}SP cannot be scaled index
    assert(op.index() == RegName_Null || !equals(op.index(), REG_STACK));

    // Only GPRegs can be encoded in the SIB
    assert(op.base() == RegName_Null || 
            getRegKind(op.base()) == OpndKind_GPReg);
    assert(op.index() == RegName_Null ||
            getRegKind(op.index()) == OpndKind_GPReg);

    modrm.rm = 4;   // r/m = 100, means 'we have SIB here'
    if (op.base() == RegName_Null) {
        // no base.
        // already checked above if 
        // the first if() //assert(op.index() != RegName_Null);

        modrm.mod = 0;  // mod=00 - here it means 'no base, but disp32'
        sib.base = 5;   // 101 with mod=00  ^^^

        // encode at least fake disp32 to avoid having [base=ebp]
        *(unsigned*)stream = op.disp();
        stream += 4;

        unsigned sc = op.scale();
        if (sc == 1 || sc==0)   { sib.scale = 0; }    // SS=00
        else if (sc == 2)       { sib.scale = 1; }    // SS=01
        else if (sc == 4)       { sib.scale = 2; }    // SS=10
        else if (sc == 8)       { sib.scale = 3; }    // SS=11
        sib.index = getHWRegIndex(op.index());
        return stream;
    }

    if (op.disp() == 0 && !equals(op.base(), RegName_EBP)) {
        modrm.mod = 0;  // mod=00, no disp
    }
    else if (disp_fits8) {
        modrm.mod = 1;  // mod=01, use disp8
        *(unsigned char*)stream = (unsigned char)op.disp();
        stream += 1;
    }
    else {
        modrm.mod = 2;  // mod=10, use disp32 
        *(unsigned*)stream = (unsigned)op.disp();
        stream += 4;
    }

    if (op.index() == RegName_Null) {
        assert(op.scale() == 0); // 'scale!=0' has no meaning without index
        // the only reason we're here without index, is that we have {E|R}SP 
        // as a base. Another possible reason - EBP without a disp - is 
        // handled above by adding a fake disp8
        assert(op.base() != RegName_Null && equals(op.base(), REG_STACK));
        sib.scale = 0;  // SS = 00
        sib.index = 4;  // SS + index=100 means 'no index'
    }
    else {
        unsigned sc = op.scale();
        if (sc == 1 || sc==0)   { sib.scale = 0; }    // SS=00
        else if (sc == 2)       { sib.scale = 1; }    // SS=01
        else if (sc == 4)       { sib.scale = 2; }    // SS=10
        else if (sc == 8)       { sib.scale = 3; }    // SS=11
        sib.index = getHWRegIndex(op.index());
        // not an error by itself, but the usage of [index*1] instead 
        // of [base] is discouraged
        assert(op.base() != RegName_Null || op.scale() != 1);
    }
    sib.base = getHWRegIndex(op.base());
    return stream;
}

char * EncoderBase::nops(char * stream, unsigned howMany)
{
    // 1-byte NOP: - The True NOP (xchg EAX, EAX)
    static const unsigned char nop1 = 0x90;
    // 2-byte NOP: mov reg, reg     - MOV EAX, EAX: 89 C0
    static const unsigned short nop2 = 0xC089;
    // 3-byte NOP: lea reg, 0 (reg) (8-bit displacement) - 
    // LEA EAX, reg+0x00 : 8D 40 00
    static const unsigned short nop3_01 = 0x408D;
    static const unsigned char nop3_2 = 0x00;
    // 6-byte NOP: lea reg, 0 (reg) (32-bit displacement) -  
    // LEA EAX, reg+0x00000000: 8D 80 00000000
    static const unsigned short nop6_01 = 0x808D;
    static const unsigned int nop6_2345 = 0x00000000;

    char * aligned = stream + howMany;

    for ( ; howMany>=6; howMany -= 6) {
        *(unsigned short*)&stream[0] = nop6_01;
        *(unsigned int*)&stream[2] = nop6_2345;
        stream += sizeof(short) + sizeof(unsigned);
    }
    for ( ; howMany>=3; howMany -= 3) {
        *(unsigned short*)&stream[0] = nop3_01;
        *(unsigned char*)&stream[2] = nop3_2;
        stream += sizeof(short) + sizeof(char);
    }
    
    if (howMany>=2) {
        howMany -= 2;
        *(unsigned short*)&stream[0] = nop2;
        stream += sizeof(short);
    }
    if (howMany) {
        *(unsigned char*)stream = nop1;
        //not used below // stream += sizeof(unsigned char);
    }
    return aligned;
}

char * EncoderBase::prefix(char* stream, InstPrefix pref)
{
    if (pref== InstPrefix_Null) {
        // nothing to do
        return stream;
    }
    *stream = (char)pref;
    return stream + 1;
}


/**
 *
 */
static bool try_match(const EncoderBase::OpcodeDesc& odesc, 
                      const EncoderBase::Operands& opnds, bool strict) {
                      
    assert(odesc.roles.count == opnds.count());
    
    for(unsigned j=0; j<odesc.roles.count; j++) {
        // - the location must match exactly
        if ((odesc.opnds[j].kind & opnds[j].kind()) != opnds[j].kind()) {
            return false;
        }
        if (strict) {
            // the size must match exactly
            if (odesc.opnds[j].size != opnds[j].size()) {
                return false;
            }
        }
        else {
            // must match only for def operands, and dont care about use ones
            // situations like 'mov r8, imm32/mov r32, imm8' so the 
            // destination operand defines the overall size
            if (EncoderBase::getOpndRoles(odesc.roles, j) & OpndRole_Def) {
                if (odesc.opnds[j].size != opnds[j].size()) {
                    return false;
                }
            }
        }
    }
    return true;
}

const EncoderBase::OpcodeDesc * 
EncoderBase::lookup(Mnemonic mn, const Operands& opnds)
{
    const unsigned hash = opnds.hash();
    unsigned opcodeIndex = opcodesHashMap[mn][hash];
    if (opcodeIndex == NOHASH) {
        // fast-path did no work. try to lookup sequentially
        const OpcodeDesc * odesc = opcodes[mn];
        int idx = -1;
        bool found = false;
        for (idx=0; !odesc[idx].last; idx++) {
            const OpcodeDesc& opcode = odesc[idx];
            if (opcode.roles.count != opnds.count()) {
                continue;
            }
            if (try_match(opcode, opnds, true)) {
                found = true;
                break;
            }
        }
        if (!found) {
            for (idx=0; !odesc[idx].last; idx++) {
                const OpcodeDesc& opcode = odesc[idx];
                if (opcode.roles.count != opnds.count()) {
                    continue;
                }
                if (try_match(opcode, opnds, false)) {
                    found = true;
                    break;
                }
            }
        }
        assert(found);
        opcodeIndex = idx;
    }
    assert(opcodeIndex != NOHASH);
    const OpcodeDesc * odesc = &opcodes[mn][opcodeIndex];
    assert(!odesc->last);
    assert(odesc->roles.count == opnds.count());
#if !defined(_EM64T_)
    // tuning was done for IA32 only, so no size restriction on EM64T
    assert(sizeof(OpcodeDesc)==128);
#endif
    return odesc;
}

Mnemonic EncoderBase::str2mnemonic(const char * mn_name)
{
    for (unsigned m = 1; m<Mnemonic_Count; m++) {
        if (!strcmpi(mnemonics[m].name, mn_name)) {
            return (Mnemonic)m;
        }
    }
    return Mnemonic_Null;
}

static const char * conditionStrings[ConditionMnemonic_Count] = {
    "O",
    "NO",
    "B",
    "AE",
    "Z",
    "NZ",
    "BE",
    "A",

    "S",
    "NS",
    "P",
    "NP",
    "L",
    "GE",
    "LE",
    "G",
};

const char * getConditionString(ConditionMnemonic cm) {
    return conditionStrings[cm];
}

static const struct {
        char            sizeString[12];
        OpndSize        size;
}
sizes[] = {
    { "Sz8", OpndSize_8 },
    { "Sz16", OpndSize_16 },
    { "Sz32", OpndSize_32 },
    { "Sz64", OpndSize_64 },
#if !defined(TESTING_ENCODER)
    { "Sz80", OpndSize_80 },
    { "Sz128", OpndSize_128 },
#endif
    { "SzAny", OpndSize_Any },
};


OpndSize getOpndSize(const char * sizeString)
{
    assert(sizeString);
    for (unsigned i = 0; i<COUNTOF(sizes); i++) {
        if (!strcmpi(sizeString, sizes[i].sizeString)) {
            return sizes[i].size;
        }
    }
    return OpndSize_Null;
}

const char * getOpndSizeString(OpndSize size) {
    for( unsigned i = 0; i<COUNTOF(sizes); i++ ) {
        if( sizes[i].size==size ) {
            return sizes[i].sizeString;
        }
    }
    return NULL;
}

static const struct {
    char            kindString[16];
    OpndKind        kind;
}
kinds[] = {
    { "Null", OpndKind_Null },
    { "GPReg", OpndKind_GPReg },
    { "SReg", OpndKind_SReg },
    { "FPReg", OpndKind_FPReg },
    { "XMMReg", OpndKind_XMMReg },
#ifdef _HAVE_MMX_
    { "MMXReg", OpndKind_MMXReg },
#endif
    { "StatusReg", OpndKind_StatusReg },
    { "Reg", OpndKind_Reg },
    { "Imm", OpndKind_Imm },
    { "Mem", OpndKind_Mem },
    { "Any", OpndKind_Any },
};

const char * getOpndKindString(OpndKind kind)
{
    for (unsigned i = 0; i<COUNTOF(kinds); i++) {
        if (kinds[i].kind==kind) {
            return kinds[i].kindString;
        }
    }
    return NULL;
}

OpndKind getOpndKind(const char * kindString)
{
    assert(kindString);
    for (unsigned i = 0; i<COUNTOF(kinds); i++) {
        if (!strcmpi(kindString, kinds[i].kindString)) {
            return kinds[i].kind;
        }
    }
    return OpndKind_Null;
}

/**
 * A mapping between register string representation and its RegName constant.
 */
static const struct {
        char    regstring[6];
        RegName regname;
}

registers[] = {
#ifdef _EM64T_
    {"RAX",         RegName_RAX},
    {"RBX",         RegName_RBX},
    {"RCX",         RegName_RCX},
    {"RDX",         RegName_RDX},
    {"RBP",         RegName_RBP},
    {"RSI",         RegName_RSI},
    {"RDI",         RegName_RDI},
    {"RSP",         RegName_RSP},
    {"R8",          RegName_R8},
    {"R9",          RegName_R9},
    {"R10",         RegName_R10},
    {"R11",         RegName_R11},
    {"R12",         RegName_R12},
    {"R13",         RegName_R13},
    {"R14",         RegName_R14},
    {"R15",         RegName_R15},
#endif

    {"EAX",         RegName_EAX},
    {"ECX",         RegName_ECX},
    {"EDX",         RegName_EDX},
    {"EBX",         RegName_EBX},
    {"ESP",         RegName_ESP},
    {"EBP",         RegName_EBP},
    {"ESI",         RegName_ESI},
    {"EDI",         RegName_EDI},
#ifdef _EM64T_
    {"R8D",         RegName_R8D},
    {"R9D",         RegName_R9D},
    {"R10D",        RegName_R10D},
    {"R11D",        RegName_R11D},
    {"R12D",        RegName_R12D},
    {"R13D",        RegName_R13D},
    {"R14D",        RegName_R14D},
    {"R15D",        RegName_R15D},
#endif

    {"AX",          RegName_AX},
    {"CX",          RegName_CX},
    {"DX",          RegName_DX},
    {"BX",          RegName_BX},
    {"SP",          RegName_SP},
    {"BP",          RegName_BP},
    {"SI",          RegName_SI},
    {"DI",          RegName_DI},

    {"AL",          RegName_AL},
    {"CL",          RegName_CL},
    {"DL",          RegName_DL},
    {"BL",          RegName_BL},
    {"AH",          RegName_AH},
    {"CH",          RegName_CH},
    {"DH",          RegName_DH},
    {"BH",          RegName_BH},

    {"ES",          RegName_ES},
    {"CS",          RegName_CS},
    {"SS",          RegName_SS},
    {"DS",          RegName_DS},
    {"FS",          RegName_FS},
    {"GS",          RegName_GS},

    {"FP0",         RegName_FP0},
/*
    {"FP1",         RegName_FP1},
    {"FP2",         RegName_FP2},
    {"FP3",         RegName_FP3},
    {"FP4",         RegName_FP4},
    {"FP5",         RegName_FP5},
    {"FP6",         RegName_FP6},
    {"FP7",         RegName_FP7},
*/
    {"FP0S",        RegName_FP0S},
    {"FP1S",        RegName_FP1S},
    {"FP2S",        RegName_FP2S},
    {"FP3S",        RegName_FP3S},
    {"FP4S",        RegName_FP4S},
    {"FP5S",        RegName_FP5S},
    {"FP6S",        RegName_FP6S},
    {"FP7S",        RegName_FP7S},

    {"FP0D",        RegName_FP0D},
    {"FP1D",        RegName_FP1D},
    {"FP2D",        RegName_FP2D},
    {"FP3D",        RegName_FP3D},
    {"FP4D",        RegName_FP4D},
    {"FP5D",        RegName_FP5D},
    {"FP6D",        RegName_FP6D},
    {"FP7D",        RegName_FP7D},

    {"XMM0",        RegName_XMM0},
    {"XMM1",        RegName_XMM1},
    {"XMM2",        RegName_XMM2},
    {"XMM3",        RegName_XMM3},
    {"XMM4",        RegName_XMM4},
    {"XMM5",        RegName_XMM5},
    {"XMM6",        RegName_XMM6},
    {"XMM7",        RegName_XMM7},

    {"XMM0S",       RegName_XMM0S},
    {"XMM1S",       RegName_XMM1S},
    {"XMM2S",       RegName_XMM2S},
    {"XMM3S",       RegName_XMM3S},
    {"XMM4S",       RegName_XMM4S},
    {"XMM5S",       RegName_XMM5S},
    {"XMM6S",       RegName_XMM6S},
    {"XMM7S",       RegName_XMM7S},

    {"XMM0D",       RegName_XMM0D},
    {"XMM1D",       RegName_XMM1D},
    {"XMM2D",       RegName_XMM2D},
    {"XMM3D",       RegName_XMM3D},
    {"XMM4D",       RegName_XMM4D},
    {"XMM5D",       RegName_XMM5D},
    {"XMM6D",       RegName_XMM6D},
    {"XMM7D",       RegName_XMM7D},
    
    {"EFLGS",       RegName_EFLAGS},
};


const char * getRegNameString(RegName reg)
{
    for (unsigned i = 0; i<COUNTOF(registers); i++) {
        if (registers[i].regname == reg) {
            return registers[i].regstring;
        }
    }
    return NULL;
}

RegName getRegName(const char * regname)
{
    if (NULL == regname) {
        return RegName_Null;
    }

    for (unsigned i = 0; i<COUNTOF(registers); i++) {
        if (!strcmpi(regname,registers[i].regstring)) {
            return registers[i].regname;
        }
    }
    return RegName_Null;
}
