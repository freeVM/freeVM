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
 * @version $Revision: 1.19.8.4.4.3 $
 */

#define IRTRANSFORMER_REGISTRATION_ON 1

#include <fstream>
#include "Stl.h"
#include "Ia32CodeGenerator.h"
#include "Ia32CodeSelector.h"
#include "Log.h"
#include "Ia32IRManager.h"
#include "Ia32Printer.h"

#ifdef PLATFORM_POSIX
    #include <stdarg.h>
    #include <sys/stat.h>
    #include <sys/types.h>
#endif //PLATFORM_POSIX


namespace Jitrino
{
namespace Ia32
{

//___________________________________________________________________________________________________
void _cdecl die(uint32 retCode, const char * message, ...)
{
    ::std::cerr<<"---------- die called (ret code = "<<retCode<<") --------------------------------------"<<std::endl;
    if (message!=NULL){
        va_list args;
        va_start(args, message);
        char str[0x10000];
        vsprintf(str, message, args);
        ::std::cerr<<str<<std::endl;
    }
    exit(retCode);
}


//___________________________________________________________________________________________________
class InstructionFormTranslator : public SessionAction {
    void runImpl(){ irManager->translateToNativeForm(); }
    uint32 getNeedInfo()const{ return 0; }
    uint32 getSideEffects()const{ return 0; }
};

static ActionFactory<InstructionFormTranslator> _native("native");

//___________________________________________________________________________________________________
class UserRequestedDie : public SessionAction {
    void runImpl(){ die(10, getArg("msg")); }
    uint32 getNeedInfo()const{ return 0; }
    uint32 getSideEffects()const{ return 0; }
    bool isIRDumpEnabled(){ return false; }
};

static ActionFactory<UserRequestedDie> _die("die");

//___________________________________________________________________________________________________
class UserRequestedBreakPoint : public SessionAction {
    void runImpl(){ 
        irManager->getFlowGraph()->getEntryNode()->prependInst(irManager->newInst(Mnemonic_INT3));
    }
    uint32 getNeedInfo()const{ return 0; }
    uint32 getSideEffects()const{ return 0; }
    bool isIRDumpEnabled(){ return false; }
};

static ActionFactory<UserRequestedBreakPoint> _break("break");


//================================================================================
//  class CodeGenerator
//================================================================================

//___________________________________________________________________________________________________

void CodeGenerator::genCode(::Jitrino::SessionAction* sa, ::Jitrino::MethodCodeSelector& inputProvider) {
    CompilationContext* cc = sa->getCompilationContext();
    CompilationInterface* ci = cc->getVMCompilationInterface();
    MemoryManager& mm = cc->getCompilationLevelMemoryManager();
    IRManager* irManager = new (mm) IRManager(mm,ci->getTypeManager(),*ci->getMethodToCompile(), *ci);
#ifdef _DEBUG
    irManager->setVerificationLevel(1);
#else
    irManager->setVerificationLevel(0);
#endif     
    cc->setLIRManager(irManager);
    
    MethodDesc* meth = ci->getMethodToCompile();
    if (ci->isBCMapInfoRequired()) {
        StlVector<uint64>* lirMap = new(mm) StlVector<uint64> (mm, 
            (size_t) getVectorSize(bcOffset2HIRHandlerName, meth) * 
            ESTIMATED_LIR_SIZE_PER_HIR + 5, ILLEGAL_VALUE);
        addContainerHandler(lirMap, bcOffset2LIRHandlerName, meth);
    }

    bool slowLdString = sa->getBoolArg("SlowLdString", false); 
    MemoryManager  codeSelectorMemManager(1024, "CodeGenerator::selectCode.codeSelectorMemManager");
    MethodCodeSelector    codeSelector(*ci, mm, codeSelectorMemManager, *irManager, slowLdString);

    inputProvider.selectCode(codeSelector);
}

}}; // namespace Ia32

