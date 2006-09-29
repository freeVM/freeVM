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
 * @author Intel, Konstantin M. Anisimov, Igor V. Chebykin
 * @version $Revision$
 *
 */

#ifndef IRPRINTER_H_
#define IRPRINTER_H_

#include <string>
#include <iostream>
#include <fstream>
#include "IpfCfg.h"

namespace Jitrino {
namespace IPF {

//=======================================================================================//
// IrPrinter
//========================================================================================//

class IrPrinter {
public:
                   IrPrinter(Cfg&, char*);
    void           printCfgDot(char*);
    void           printLayoutDot(char*);
    void           printAsm(ostream&);

    static string  toString(Inst*);
    static string  toString(Opnd*);
    static string  toString(OpndSet&);
    static string  toString(RegOpndSet&);
    static string  toString(OpndVector&);
    static string  toString(InstVector&);
    static string  toString(InstList&);
    static string  toString(NodeKind);
    static string  toString(EdgeKind);
    static string  toString(OpndKind);
    static string  toString(DataKind);

protected:
    void           printEdgeDot(Edge*);
    void           printNodeDot(Node*);
    void           printNodeAsm(BbNode*);

    void           printHead();
    void           printTail();
    
    MemoryManager  &mm;
    Cfg            &cfg;
    char           *logDirName;   // name of log dir
    ostream        *os;           // output stream
    ofstream       *ofs;          // file output stream
};

} // IPF
} // Jitrino

#endif /*IRPRINTER_H_*/
