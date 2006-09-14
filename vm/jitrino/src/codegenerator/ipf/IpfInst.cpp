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
 * @author Intel, Konstantin M. Anisimov, Igor V. Chebykin
 * @version $Revision$
 *
 */

#include "IpfCfg.h"

namespace Jitrino {
namespace IPF {

//========================================================================================//
// Inst
//========================================================================================//

Inst::Inst(InstCode instCode_, Opnd *op1, Opnd *op2, Opnd *op3, Opnd *op4, Opnd *op5, Opnd *op6) { 

    instCode = instCode_;
    if(op1 != NULL) opndList.push_back(op1);
    if(op2 != NULL) opndList.push_back(op2);
    if(op3 != NULL) opndList.push_back(op3);
    if(op4 != NULL) opndList.push_back(op4);
    if(op5 != NULL) opndList.push_back(op5);
    if(op6 != NULL) opndList.push_back(op6);
}

//----------------------------------------------------------------------------------------//

Inst::Inst(InstCode instCode_, Completer comp1, 
           Opnd *op1, Opnd *op2, Opnd *op3, Opnd *op4, Opnd *op5, Opnd *op6) { 

    instCode = instCode_;
    compList.push_back(comp1);
    if(op1 != NULL) opndList.push_back(op1);
    if(op2 != NULL) opndList.push_back(op2);
    if(op3 != NULL) opndList.push_back(op3);
    if(op4 != NULL) opndList.push_back(op4);
    if(op5 != NULL) opndList.push_back(op5);
    if(op6 != NULL) opndList.push_back(op6);
}

//----------------------------------------------------------------------------------------//

Inst::Inst(InstCode instCode_, Completer comp1, Completer comp2, 
           Opnd *op1, Opnd *op2, Opnd *op3, Opnd *op4, Opnd *op5, Opnd *op6) { 

    instCode = instCode_;
    compList.push_back(comp1);
    compList.push_back(comp2);
    if(op1 != NULL) opndList.push_back(op1);
    if(op2 != NULL) opndList.push_back(op2);
    if(op3 != NULL) opndList.push_back(op3);
    if(op4 != NULL) opndList.push_back(op4);
    if(op5 != NULL) opndList.push_back(op5);
    if(op6 != NULL) opndList.push_back(op6);
}

//----------------------------------------------------------------------------------------//

Inst::Inst(InstCode instCode_, Completer comp1, Completer comp2, Completer comp3,
           Opnd *op1, Opnd *op2, Opnd *op3, Opnd *op4, Opnd *op5, Opnd *op6) { 

    instCode = instCode_;
    compList.push_back(comp1);
    compList.push_back(comp2);
    compList.push_back(comp3);
    if(op1 != NULL) opndList.push_back(op1);
    if(op2 != NULL) opndList.push_back(op2);
    if(op3 != NULL) opndList.push_back(op3);
    if(op4 != NULL) opndList.push_back(op4);
    if(op5 != NULL) opndList.push_back(op5);
    if(op6 != NULL) opndList.push_back(op6);
}

} // IPF
} // Jitrino
