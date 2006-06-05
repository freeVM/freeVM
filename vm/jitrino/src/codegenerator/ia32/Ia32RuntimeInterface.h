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
 * @author Intel, Nikolay A. Sidelnikov
 * @version $Revision: 1.7.22.4 $
 */

#ifndef _IA32RUNTIMEINTERFACE_H_
#define _IA32RUNTIMEINTERFACE_H_

#include "CodeGenIntfc.h"
namespace Jitrino
{
namespace Ia32{


//============================================================================================
// class RuntimeInterface
//============================================================================================
/** class RuntimeInterface is the Ia32 CG implementation of the 
::RuntimeInterface interface 

It is responsible for runtime operations the JIT (CG in particular) performs:
stack unwinding, root set enumeration, and code patching.

*/


	class RuntimeInterface : public ::Jitrino::RuntimeInterface {
public:
    virtual void  unwindStack(MethodDesc* methodDesc, JitFrameContext* context, bool isFirst) ;

    virtual void  getGCRootSet(MethodDesc* methodDesc, GCInterface* gcInterface, 
		const JitFrameContext* context, bool isFirst);

	virtual bool  canEnumerate(MethodDesc* methodDesc, NativeCodePtr eip);

    virtual void  fixHandlerContext(MethodDesc* methodDesc, JitFrameContext* context, bool isFirst);

    virtual void* getAddressOfThis(MethodDesc* methodDesc, const JitFrameContext* context, bool isFirst);


    virtual void* getAddressOfSecurityObject(MethodDesc* methodDesc, const JitFrameContext* context) { assert(0); return NULL; }

    virtual bool  recompiledMethodEvent(BinaryRewritingInterface & binaryRewritingInterface,
        MethodDesc * methodDesc, void * data);

    virtual bool getBcLocationForNative(MethodDesc* method, uint64 native_pc, uint16 *bc_pc);
    virtual bool getNativeLocationForBc(MethodDesc* method,  uint16 bc_pc, uint64 *native_pc);

    virtual uint32          getInlineDepth(InlineInfoPtr ptr, uint32 offset);
    virtual Method_Handle   getInlinedMethod(InlineInfoPtr ptr, uint32 offset, uint32 inline_depth);

};

}}; // namespace Ia32


#endif //_IA32RUNTIMEINTERFACE_H_
