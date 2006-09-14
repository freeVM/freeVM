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
 * @author Intel, Mikhail Y. Fursov
 * @version $Revision$
 *
 */

#ifndef _LOOP_TREE_H_
#define _LOOP_TREE_H_

#include "Tree.h"
#include "Stl.h"
#include "MemoryManager.h"
#include "ControlFlowGraph.h"

namespace Jitrino {

class LoopTree;
class EdgeCoalescerCallback {
public:
    virtual ~EdgeCoalescerCallback(){};
    virtual void coalesce(Node* header, Node* newPreHeader, uint32 numEdges) = 0;
};

class LoopNode : public TreeNode {
public:
    LoopNode* getChild() const   {return (LoopNode*)child;}
    LoopNode* getSiblings() const {return (LoopNode*)siblings;}
    LoopNode* getParent() const {return (LoopNode*)parent;}
    
    bool  inLoop(const Node* node)  const;

    Node* getHeader() const;
    
    // return all nodes in loop;
    const Nodes& getNodesInLoop() const;
    
    // return true is this edge is a loop exit from this loop.
    bool isLoopExit(const Edge* e) const;

    // return true is this edge is a back edge for this loop.
    bool isBackEdge(const Edge* e) const;

private:
    friend class LoopTree;

    LoopNode(MemoryManager& mm, LoopTree* lt, Node* hd)  : loopTree(lt), header(hd), nodesInLoop(mm){}

    void markNodesOfLoop();
    void markNode(Node* node);
    void backwardMarkNode(Node* currentTail);
    void clear();

    LoopTree* loopTree;
    Node* header;  // loop entry point
    Nodes nodesInLoop;
};

typedef StlVector<LoopNode*> LoopNodes;

class LoopTree : public Tree {
    friend class LoopNode;
public:
    LoopTree(MemoryManager& mm, ControlFlowGraph* f, EdgeCoalescerCallback* coalesceCallback = NULL); 

    // rebuilds loop tree if needed.
    void rebuild(bool doNormalization);

    bool hasLoops() const { assert(isValid()); return ((LoopNode*)root)->getChild()!=NULL;} //root node is artificial

    bool  isLoopHeader(const Node* node) const;

    Node* getLoopHeader(const Node* node, bool containingLoop = true) const;
    
    LoopNode* getLoopNode(const Node* node, bool containingLoop) const;

    uint32 getLoopDepth(const Node* node) const;
        
    // true if edge is exit from any loop
    bool isLoopExit(const Edge* e) const;

    bool isBackEdge(const Edge* e) const;
    
    bool isValid() const {  return traversalNum > fg->getModificationTraversalNum(); }

    uint32 getTraversalNum() { return traversalNum; }

    EdgeCoalescerCallback* getCoalesceCallback() const {return coalesceCallback;}
    void setCoalesceCallback(EdgeCoalescerCallback* callback) {coalesceCallback = callback;}
   
    bool isNormalized() const { return normalized;}

    uint32 getMaxLoopDepth() const {return getHeight()-1;}
private:

    void findLoopHeaders(Nodes& headers);
    void findAndCoalesceLoopEdges(const Nodes& headers);
    Edge* coalesceEdges(Edges& edges);
    void formLoopHierarchy(Nodes& headers);
    LoopNode* findEnclosingLoop(Node* header);
    void createLoop(Node* header);

    MemoryManager& mm;
    ControlFlowGraph* fg;
    uint32 traversalNum;

    //nodes by dfn
    // loop header by node->dfn. 
    // Note: if node is loop header this map contains LoopNode for this, but not for containig header
    LoopNodes headerMap; 
    // all loop headers. Used for private needs during loop-tree computation
    Nodes headers; 
    EdgeCoalescerCallback* coalesceCallback;
    //indicates that loop tree  was normalized during previous rebuild
    bool normalized;

};

}

#endif

