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
 * @author Evgueni Brevnov
 * @version $Revision: 1.1.2.1.4.4 $
 */

#include <bfd.h>
#include <dis-asm.h>
#include <stdarg.h>
#include <string.h>
#include <assert.h>
#include <stdlib.h>

#include "port_disasm.h"
#include <apr_dso.h>
#include <apr_strings.h>
#include <apr_portable.h>

// this is mostly imperical data 
#if defined(_IA32_)
    #define ADDR_SIZE       16
    #define MNEMONIC_SIZE   15
    #define BYTES_PER_LINE  2
    #define BYTES_TOTAL     100
    //BYTES_SIZE = BYTES_PER_LINE * 3
    #define BYTES_SIZE      6
#elif defined(_EM64T_)
    #define ADDR_SIZE       24
    #define MNEMONIC_SIZE   20
    #define BYTES_PER_LINE  2
    #define BYTES_TOTAL     100
    #define BYTES_SIZE      6
#elif defined(_IPF_)
    #define ADDR_SIZE       24
    #define MNEMONIC_SIZE   30
    #define BYTES_PER_LINE  6
    #define BYTES_TOTAL     150
    #define BYTES_SIZE      18
#else
    #define ADDR_SIZE       0
    #define MNEMONIC_SIZE   0
    #define BYTES_PER_LINE  0
    #define BYTES_TOTAL     0
    #define BYTES_SIZE      0
#endif

typedef void (* init_disassemble_info_t)(struct disassemble_info *info,
                                         void *stream,
                                         fprintf_ftype fprintf_func);

typedef void (* disassemble_init_for_target_t)(struct disassemble_info * info);

/*    Private Interface    */

struct port_disassembler_t {
    struct disassemble_info bfd_info;
    port_disasm_info_t port_info;
    int line_size;
    char * real_stream;
    apr_pool_t * user_pool;
    apr_file_t * user_file;
    apr_size_t num_bytes_total;
    apr_size_t num_bytes_used;
};

static apr_pool_t * disasm_pool = NULL;
static disassembler_ftype bfd_decoder = NULL;
static init_disassemble_info_t bfd_init_info = NULL;
static disassemble_init_for_target_t bfd_init_target = NULL;

#if defined(_IA32_) || defined(_EM64T_) || defined(_IPF_)

/*    Memory reading routines    */

static int disasm_read_memory(bfd_vma src,
                              bfd_byte * buffer,
                              unsigned int n,
                              struct disassemble_info * info) {
#if defined(_IA32_)
    memcpy(buffer, (void *)(apr_uint32_t)src, n);
#elif defined(_EM64T_) || defined(_IPF_)
    memcpy(buffer, (void *)src, n);
#endif
    return 0;
}

/*    Address printing routines    */

static void disasm_print_adress_default(bfd_vma memaddr, struct disassemble_info * info) {
#if defined(_IA32_)
    info->fprintf_func(info->stream, "0x%08X", (apr_int32_t)memaddr);
#elif defined(_EM64T_) || defined(_IPF_)
    apr_uint64_t addr = (apr_uint64_t)memaddr;
    apr_uint32_t hi = (apr_uint32_t)((addr >> 32) & UINT_MAX);
    apr_uint32_t lo = (apr_uint32_t)(addr & UINT_MAX);
    info->fprintf_func(info->stream, "0x%08x%08x", hi, lo);
#endif
}

/*    General printing routines    */

static int disasm_sprint_default(void * stream, const char * fmt, ...) {
    va_list args;
    va_start(args, fmt);
    port_disassembler_t * disassembler = (port_disassembler_t *)stream;
    int required_length = apr_vsnprintf(NULL, 0, fmt, args);
    // insure space
    while (required_length >= disassembler->num_bytes_total -
             disassembler->num_bytes_used) {
        void * buf = malloc(disassembler->num_bytes_used);
        memcpy(buf, disassembler->real_stream, disassembler->num_bytes_used);
        apr_pool_clear(disassembler->user_pool);
        disassembler->num_bytes_total *= 2;
        disassembler->real_stream = apr_palloc(disassembler->user_pool,
            disassembler->num_bytes_total);
        memcpy(disassembler->real_stream, buf, disassembler->num_bytes_used);
        free(buf);
    }
    apr_vsnprintf(disassembler->real_stream + disassembler->num_bytes_used,
        required_length + 1, fmt, args);
    disassembler->num_bytes_used += required_length;
    return required_length;
}

static int disasm_fprint_default(void * stream, const char * fmt, ...) {
    va_list args;
    va_start(args, fmt);
    port_disassembler_t * disassembler = (port_disassembler_t *)stream;
    int required_length = apr_vsnprintf(NULL, 0, fmt, args);
    // insure space
    if (required_length >= disassembler->num_bytes_total -
            disassembler->num_bytes_used) {
        apr_file_write(disassembler->user_file, disassembler->real_stream,
            &disassembler->num_bytes_used);
        disassembler->num_bytes_used = 0;
    }
    while (required_length >= disassembler->num_bytes_total -
            disassembler->num_bytes_used) {
        disassembler->num_bytes_total *= 2;
    }
    apr_vsnprintf(disassembler->real_stream + disassembler->num_bytes_used,
        required_length + 1, fmt, args);
    disassembler->num_bytes_used += required_length;
    return 0;
}

static void disasm_print(port_disassembler_t * disassembler,
                         const char * code, 
                         apr_int64_t len) {
    // iterate over the code buffer
    while (len > 0) {
        int bytes_read = 1;
#if defined(_IA32_)
        // print instruction address
        if (disassembler->port_info.print_addr) {
            disassembler->bfd_info.print_address_func((bfd_vma)(apr_uint32_t)code,
                &disassembler->bfd_info);
            disassembler->bfd_info.fprintf_func(disassembler, "\t");
        }        
        // print mnemonic
        if (disassembler->port_info.print_mnemonic) {
            bytes_read = bfd_decoder((bfd_vma)(apr_uint32_t)code, &disassembler->bfd_info);
        }
#elif defined(_EM64T_) || defined(_IPF_)
        // print instruction address
        if (disassembler->port_info.print_addr) {
            disassembler->bfd_info.print_address_func((bfd_vma)code,
                &disassembler->bfd_info);
            disassembler->bfd_info.fprintf_func(disassembler, "\t");
        }
        // print mnemonic
        if (disassembler->port_info.print_mnemonic) {
            bytes_read = bfd_decoder((bfd_vma)code, &disassembler->bfd_info);
        }
#endif
        // print native bytes
        if (disassembler->port_info.print_bytes) {
            disassembler->bfd_info.fprintf_func(disassembler, "\t");
            int i;
            for (i = 0; i < bytes_read; i++) {
                disassembler->bfd_info.fprintf_func(disassembler,
                    "%02X ", ((int)*(code + i)) & 0xff);
            }
        }
        disassembler->bfd_info.fprintf_func(disassembler, "\n");
        code += bytes_read;
        len -= bytes_read;
#ifndef NDEBUG
        if (len < 0) {
            fprintf(stderr, "WARNING: Disassembler read %i byte(s) more "
                "than specified buffer length\n", (apr_int32_t)-len);
        }
#endif
    }
}

#endif // defined(_IA32_) || defined(_EM64T_) || defined(_IPF_)

/*    Public Interface    */

APR_DECLARE(apr_status_t) port_disasm_initialize() {
#if defined(_IA32_) || defined(_EM64T_) || defined(_IPF_)
    static apr_status_t stat = APR_EINIT;
    const char * BFD_LIB_NAME = "libbfd.so";
    const char * OPCODES_LIB_NAME = "libopcodes.so";
    const char * BFD_INIT_INFO_NAME = "init_disassemble_info";
    const char * BFD_INIT_TARGET_NAME = "disassemble_init_for_target";
#if defined (_IA32_) || defined(_EM64T_)
    const char * BFD_PRINT_INSN_NAME = "print_insn_i386_intel";
#elif defined(_IPF_)
    const char * BFD_PRINT_INSN_NAME = "print_insn_ia64";
    apr_dso_handle_t * nnn_lib_handle = NULL;
#endif
    apr_dso_handle_t * bfd_lib_handle = NULL;
    apr_dso_handle_t * opcodes_lib_handle = NULL;
    apr_dso_handle_sym_t bfd_init_info_sym;
    apr_dso_handle_sym_t bfd_init_target_sym;
    apr_dso_handle_sym_t bfd_print_insn_sym;

    if (stat == APR_SUCCESS) {
        return APR_SUCCESS;
    }
    
    if ((stat = apr_pool_create(&disasm_pool, NULL)) != APR_SUCCESS) {
        return stat;
    }

    if ((stat = apr_dso_load(&bfd_lib_handle, BFD_LIB_NAME, disasm_pool))
            != APR_SUCCESS) {
        apr_pool_destroy(disasm_pool);
        disasm_pool = NULL;
        return stat;
    }

#if defined(_IPF_)
    if ((stat = apr_dso_load(&nnn_lib_handle, "libgettextlib-0.14.1.so", disasm_pool))
            != APR_SUCCESS) {
        apr_pool_destroy(disasm_pool);
        disasm_pool = NULL;
        return stat;
    }
#endif

    if ((stat = apr_dso_load(&opcodes_lib_handle, OPCODES_LIB_NAME, disasm_pool))
            != APR_SUCCESS) {
        apr_pool_destroy(disasm_pool);
        disasm_pool = NULL;
        return stat;
    }

    if ((stat = apr_dso_sym(&bfd_init_info_sym,
            opcodes_lib_handle, BFD_INIT_INFO_NAME)) != APR_SUCCESS) {
        // library handle will be closed automatically
        apr_pool_destroy(disasm_pool);
        disasm_pool = NULL;
        return stat;
    }

    if ((stat = apr_dso_sym(&bfd_init_target_sym,
            opcodes_lib_handle, BFD_INIT_TARGET_NAME)) != APR_SUCCESS) {
        // library handle will be closed automatically
        apr_pool_destroy(disasm_pool);
        disasm_pool = NULL;
        return stat;
    }

    if ((stat = apr_dso_sym(&bfd_print_insn_sym,
            opcodes_lib_handle, BFD_PRINT_INSN_NAME)) != APR_SUCCESS) {
        // library handle will be closed automatically
        apr_pool_destroy(disasm_pool);
        disasm_pool = NULL;
        return stat;
    }

    // initialize globals
    bfd_decoder = (disassembler_ftype)bfd_print_insn_sym;
    bfd_init_info = (init_disassemble_info_t)bfd_init_info_sym;
    bfd_init_target = (disassemble_init_for_target_t)bfd_init_target_sym;

    return  stat = APR_SUCCESS;
#else
    return APR_ENOTIMPL;
#endif
}

APR_DECLARE(apr_status_t) port_disassembler_create(port_disassembler_t ** disassembler,
                                                   apr_pool_t * pool) {
#if defined(_IA32_) || defined(_EM64T_) || defined(_IPF_)
    apr_status_t status;
    
    if ((status = port_disasm_initialize()) != APR_SUCCESS) {
        return status;
    }

    *disassembler = (port_disassembler_t *)
        apr_palloc(pool, sizeof(port_disassembler_t));
    
    port_disasm_info_t info = {1, 1, 1};
    // initialize port_info
    port_disasm_set_info(*disassembler, info, NULL);

    // initialize bfd_info
    bfd_init_info(&(*disassembler)->bfd_info, *disassembler, NULL);

    (*disassembler)->bfd_info.read_memory_func = disasm_read_memory;
    (*disassembler)->bfd_info.print_address_func = disasm_print_adress_default;

#if defined(_IA32_)
    (*disassembler)->bfd_info.arch = bfd_arch_i386;
    (*disassembler)->bfd_info.mach = bfd_mach_i386_i386_intel_syntax;
#elif defined(_EM64T_)
    (*disassembler)->bfd_info.arch = bfd_arch_i386;
    (*disassembler)->bfd_info.mach = bfd_mach_x86_64_intel_syntax;
#elif defined(_IPF_)
    (*disassembler)->bfd_info.arch =  bfd_arch_ia64;
    (*disassembler)->bfd_info.mach = bfd_mach_ia64_elf64;
#endif
    (*disassembler)->bfd_info.endian = BFD_ENDIAN_LITTLE;
    (*disassembler)->bfd_info.display_endian = BFD_ENDIAN_LITTLE;

    bfd_init_target(&(*disassembler)->bfd_info);

    // initialize the rest fields
    (*disassembler)->real_stream = NULL;
    (*disassembler)->user_pool = pool;
    (*disassembler)->user_file = NULL;
    (*disassembler)->num_bytes_total = 0;
    (*disassembler)->num_bytes_used = 0;

    return APR_SUCCESS;
#else
    return APR_ENOTIMPL;
#endif
}

APR_DECLARE(apr_status_t) port_disasm_set_info(port_disassembler_t * disassembler,
                                               const port_disasm_info_t new_info,
                                               port_disasm_info_t * old_info) {
#if defined(_IA32_) || defined(_EM64T_) || defined(_IPF_)
    if (old_info != NULL) {
        *old_info = disassembler->port_info;
    }
    disassembler->port_info = new_info;
    disassembler->line_size = 0;
    if (disassembler->port_info.print_addr) {
        disassembler->line_size += ADDR_SIZE;
    }
    if (disassembler->port_info.print_mnemonic) {
        disassembler->line_size += MNEMONIC_SIZE;
    }
    if (disassembler->port_info.print_bytes) {
        disassembler->line_size += BYTES_SIZE;
    }
    return APR_SUCCESS;
#else
    return APR_ENOTIMPL;
#endif
}

APR_DECLARE(apr_status_t) port_disasm(port_disassembler_t * disassembler,
                                      const char * code, 
                                      unsigned int len,
                                      char ** disasm_code) {
#if defined(_IA32_) || defined(_EM64T_) || defined(_IPF_)    
    // check if nothing should be printed
    if (disassembler->line_size == 0) {
        *disasm_code = NULL;
        return APR_SUCCESS;
    } 

    if (disassembler->num_bytes_total == 0) {
        // Calculate required number of bytes
        disassembler->num_bytes_total = disassembler->line_size * len / BYTES_PER_LINE;
        // initialize stream
        disassembler->real_stream = apr_palloc(disassembler->user_pool,
            disassembler->num_bytes_total);
    }

    strcpy(disassembler->real_stream, "");

    disassembler->bfd_info.fprintf_func = disasm_sprint_default;

    disasm_print(disassembler, code, len);

    *disasm_code = disassembler->real_stream;

    disassembler->real_stream = NULL;
    disassembler->num_bytes_total = 0;
    disassembler->num_bytes_used = 0;

	return APR_SUCCESS;
#else
    return APR_ENOTIMPL;
#endif
}

APR_DECLARE(apr_status_t) port_disasm_to_file(port_disassembler_t * disassembler,
                                              const char * code,
                                              unsigned int len,
                                              apr_file_t * thefile) {
#if defined(_IA32_) || defined(_EM64T_) || defined(_IPF_)
    // check if nothing should be printed
    if (disassembler->line_size == 0) {
        return APR_SUCCESS;
    } 
    
    if (disassembler->num_bytes_total == 0) {
        // Calculate required number of bytes
        disassembler->num_bytes_total = disassembler->line_size * BYTES_TOTAL / BYTES_PER_LINE;
        // initialize stream
        disassembler->real_stream = apr_palloc(disassembler->user_pool,
            disassembler->num_bytes_total);
    }
    
    // initialize file
    disassembler->user_file = thefile;

    strcpy(disassembler->real_stream, "");

    disassembler->bfd_info.fprintf_func = disasm_fprint_default;

    disasm_print(disassembler, code, len);

    // flush
    apr_file_write(disassembler->user_file, disassembler->real_stream,
        &disassembler->num_bytes_used);
    apr_file_flush(disassembler->user_file);

    disassembler->user_file = NULL;
    disassembler->num_bytes_used = 0;

	return APR_SUCCESS;
#else
    return APR_ENOTIMPL;
#endif
}
