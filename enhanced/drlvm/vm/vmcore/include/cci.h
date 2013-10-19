/*
 *  Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
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
#ifndef __CCI_H__
#define __CCI_H__

// forward declarations
class CodeChunkInfo;

// external declarations
class JIT;
typedef class Target_Exception_Handler* Target_Exception_Handler_Ptr;

struct JIT_Data_Block {
    JIT_Data_Block *next;
    char bytes[1];
};

// Each callee for a given code chunk can have multiple Callee_Info structures,
// one for each call site in the caller.
typedef struct Callee_Info {
    void* caller_ip;        // the IP in the caller where the call was made
    CodeChunkInfo* callee;  // which code chunk was called
    uint64 num_calls;       // number of calls to that chunk
} Callee_Info;

#define NUM_STATIC_CALLEE_ENTRIES 8

class CodeChunkInfo {
    friend struct Method;
public:
    CodeChunkInfo();

    void set_jit(JIT* jit) { _jit = jit; }
    JIT* get_jit() const { return _jit; }

    void set_method(Method* m) { _method = m; }
    Method* get_method() const { return _method; }

    void set_id(int id) { _id = id; }
    int get_id() const { return _id; }

    void set_relocatable(Boolean r) { _relocatable = r; }
    Boolean get_relocatable() const { return _relocatable; }

    void set_heat(unsigned heat) { _heat = heat; }
    unsigned get_heat() const { return _heat; }

    void set_code_block_addr(void* addr) { _code_block = addr; }
    void* get_code_block_addr() const { return _code_block; }

    size_t get_code_block_size() const { return _code_block_size; }
    size_t get_code_block_alignment() const { return _code_block_alignment; }

    unsigned get_num_callees() const { return _num_callees; }
    Callee_Info* get_callees() const { return _callee_info; }

    int get_jit_index() const;

    // Note: _data_blocks can only be used for inline info for now
    Boolean has_inline_info() const { return _data_blocks != NULL; }
    void* get_inline_info() const { return &_data_blocks->bytes[0]; }

    unsigned get_num_target_exception_handlers() const;
    Target_Exception_Handler_Ptr get_target_exception_handler_info(unsigned eh_num) const;

    void record_call_to_callee(CodeChunkInfo *callee, void *caller_return_ip);
    uint64 num_calls_to(CodeChunkInfo* other_chunk) const;

    void print_name() const;
    void print_name(FILE* file) const;
    void print_info(bool print_ellipses=false) const; // does not print callee information; see below
    void print_callee_info() const; // prints the callee information; usually called after print_info()

    static void initialize_code_chunk(CodeChunkInfo* chunk) {
        memset(chunk, 0, sizeof(CodeChunkInfo));
        chunk->_callee_info = chunk->_static_callee_info;
        chunk->_max_callees = NUM_STATIC_CALLEE_ENTRIES;
        chunk->_relocatable = TRUE;
    }

public:
    // The section id of the main code chunk for a method. Using an enum avoids a VC++ bug on Windows.
    enum {main_code_chunk_id = 0};

    // Returns true if this is the main code chunk
    // for a method: i.e, it
    // 1) contains the method's entry point, and
    // 2) contains the various flavors of JIT data for that method.
    static bool is_main_code_chunk(CodeChunkInfo* chunk) {
        assert(chunk);
        return (chunk->get_id() == main_code_chunk_id);
    }

    // Returns true if "id" is the section id of the main code chunk for a method.
    static bool is_main_code_chunk_id(int id) {
        return (id == main_code_chunk_id);
    }

private:
    // The triple (_jit, _method, _id) uniquely identifies a CodeChunkInfo
    JIT* _jit;
    Method* _method;
    int _id;

    bool _relocatable;

    // "Target" handlers
    unsigned _num_target_exception_handlers;
    Target_Exception_Handler_Ptr* _target_exception_handlers;

    // This records for each callee, the number of times it was called
    // by each call IP in the caller. That is, this is a list of Callee_Info
    // structures, each giving a call IP
    // Points to an array of max_callees Callee_Info entries for this code chunk
    Callee_Info* _callee_info;
    unsigned _num_callees;
    unsigned _max_callees;
    // Array used if a small number of callers to avoid mallocs & frees
    Callee_Info _static_callee_info[NUM_STATIC_CALLEE_ENTRIES];

public:
    unsigned _heat;
    void* _code_block;
    void* _jit_info_block;
    size_t _code_block_size;
    size_t _jit_info_block_size;
    size_t _code_block_alignment;
    JIT_Data_Block* _data_blocks;
    CodeChunkInfo* _next;

#ifdef VM_STATS
    uint64 num_throws;
    uint64 num_catches;
    uint64 num_unwind_java_frames_gc;
    uint64 num_unwind_java_frames_non_gc;
#endif
}; // CodeChunkInfo

#endif
