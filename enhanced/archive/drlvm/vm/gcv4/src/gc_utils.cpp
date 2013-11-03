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
 * @author Intel, Salikh Zakirov
 * @version $Revision: 1.1.2.2.4.3 $
 */  


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// System header files
#include <iostream>

// VM interface header files
#include "platform_lowlevel.h"
#include "open/vm_gc.h"
#include "open/gc.h"

// GC header files
#include "gc_cout.h"
#include "gc_header.h"
#include "gc_v4.h"
 
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

#define IS_DESIRED_BIT_VALUE(IS_ZERO_STR, BYTE_ADDR, BIT_INDEX) ( (*BYTE_ADDR & (1 << BIT_INDEX)) == (IS_ZERO_STR ? 0 : (1 << BIT_INDEX)))

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//
// 3 if & branch return compares for every call.
// Split the top 4 bits from the bottom then split the bottom 2 and then figur out which on is set.
inline unsigned int get_right_most_bit (uint8 x)
{
    assert (x);    
    if (x & 0xf) { 
        if (x & 3) {
            if (x & 1) {
                return 0;
            } else { 
                return 1;
            }
        } else if (x & 4) {
            return 2;
        } else {
            return 3;
        }
    } else {
        if (x & 0x30) {
            if (x & 0x10) {
                return 4;
            } else {
                return 5;
            }
        } else { 
            if (x & 0x40) {
                return 6;
            } else {
                return 7;
            }
        }
    }
}

/** The loop through the zero words is where all the time is spent. 
    Some prefetching might be worth considering.
**/
void get_next_set_bit(set_bit_search_info *info)
{                
    uint8 *p_byte_start = info->p_start_byte;
    unsigned int start_bit_index = info->start_bit_index;
    uint8 *p_ceil = info->p_ceil_byte;

    uint8 *p_byte = p_byte_start;

#ifdef _IPF_
    POINTER_SIZE_INT alignment_mask = 0x0000000000000007;
#else
    POINTER_SIZE_INT alignment_mask = 0x00000003;
#endif // _IPF_

    uint8 a_byte = *p_byte;
    // Clear up to bit index.
    uint8 mask = (uint8)(~((1<<start_bit_index) - 1));
    a_byte = (uint8)(a_byte & mask);

    if (a_byte) {
        info->bit_set_index =  get_right_most_bit (a_byte);
        info->p_non_zero_byte = p_byte;
        return;
    }

    // Move to next byte
    p_byte++;

    // Skip "0" bytes till we get to a word boundary
    while ((p_byte < p_ceil) && (*p_byte == 0) && ((POINTER_SIZE_INT) p_byte & alignment_mask)) {   
        p_byte++;
    }

    if (p_byte >= p_ceil) { 
        // Reached the end....there is no "1" after (p_byte_start, start_bit_index)
        info->bit_set_index = 0;
        info->p_non_zero_byte = NULL;
        return;
    }

    if (*p_byte != 0) {
        info->bit_set_index = get_right_most_bit (*p_byte);
        info->p_non_zero_byte = p_byte;
        return;
    }

    // We are definitely at a word boundary
    POINTER_SIZE_INT *p_word = (POINTER_SIZE_INT *) p_byte;
    assert(((POINTER_SIZE_INT) p_word & alignment_mask) == 0);
    // This loop is where we spend all of out time. So lets do some optimization. 
    // Originally we had the conditional (((uint8 *)((POINTER_SIZE_INT) p_word + sizeof(POINTER_SIZE_INT)) < p_ceil) && (*p_word == 0)) 
    // Lets adjust p_ceil to subrtact sizeof(POINTER_SIZE_INT) and hoist it. This will speed up the loop by about 10%.
    uint8 *p_ceil_last_to_check = p_ceil - sizeof(POINTER_SIZE_INT);

    while (((uint8 *)(POINTER_SIZE_INT) p_word < p_ceil_last_to_check) && (*p_word == 0)) {
        p_word++;   // skip a zero word each time
    }

    p_byte = (uint8 *) p_word;

    // Skip past "zero" bytes....
    while ((p_byte < p_ceil) && (*p_byte == 0)) {
        p_byte++;
    }

    if (p_byte >= p_ceil) { 
        // Reached the end....there is no "1" after (p_byte_start, start_bit_index)
        info->bit_set_index = 0;
        info->p_non_zero_byte = NULL;
    } else {
        info->bit_set_index = get_right_most_bit (*p_byte);
        info->p_non_zero_byte = p_byte;
    }

    return;
}


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


bool
get_num_consecutive_similar_bits(uint8 *p_byte_start, unsigned int bit_index_to_search_from, unsigned int *num_consec_bits, uint8 *p_ceil)
{
    if (p_ceil <= p_byte_start) {
        DIE("Unexpected values of input prameters");
    }

    bool is_zero_str = ((*p_byte_start) & (1 << bit_index_to_search_from)) ? false : true;
    uint8 byte_search_value = (uint8)(is_zero_str ? 0x00 : 0xFF);

#ifdef _IPF
    POINTER_SIZE_INT word_search_value = is_zero_str ? 0x0000000000000000 : 0xFFFFffffFFFFffff;
    POINTER_SIZE_INT alignment_mask = 0x0000000000000007;
#else
    POINTER_SIZE_INT word_search_value = is_zero_str ? 0x00000000 : 0xFFFFffff;
    POINTER_SIZE_INT alignment_mask = 0x00000003;
#endif // _IPF_

    assert(((POINTER_SIZE_INT)p_ceil & alignment_mask) == 0);   //????

    uint8 *p_byte = p_byte_start;
    unsigned int bit_index = bit_index_to_search_from + 1;
    unsigned int num_all_same_bits = 1;     // Need to start from the beginning

    while ((bit_index < GC_NUM_BITS_PER_BYTE) && IS_DESIRED_BIT_VALUE(is_zero_str, p_byte, bit_index)) {
        bit_index++;
        num_all_same_bits++;
    }
    
    if (bit_index != GC_NUM_BITS_PER_BYTE) {
        *num_consec_bits = num_all_same_bits;   // failed because some bit in the same byte was dissimilar
        return is_zero_str ? false : true;
    }

    // move to next byte
    p_byte++;

    while ((p_byte < p_ceil) && (*p_byte == byte_search_value) && ((POINTER_SIZE_INT) p_byte & alignment_mask)) {   // skip bytes till we get to a word boundary
        num_all_same_bits += GC_NUM_BITS_PER_BYTE;      
        p_byte++;
    }

    if (p_byte >= p_ceil) { // reached the end
        *num_consec_bits = num_all_same_bits;   
        return is_zero_str ? false : true;
    }
    
    if (*p_byte != byte_search_value) {
        // there might be more bits in this of interest..
        bit_index = 0;
        while ((bit_index < GC_NUM_BITS_PER_BYTE) && IS_DESIRED_BIT_VALUE(is_zero_str, p_byte, bit_index)){
            bit_index++;
            num_all_same_bits++;
        }
        *num_consec_bits = num_all_same_bits;   
        return is_zero_str ? false : true;
    }

    // We are definitely at a word boundary
    POINTER_SIZE_INT *p_word = (POINTER_SIZE_INT *) p_byte;
    assert(((POINTER_SIZE_INT) p_word & alignment_mask) == 0);

    while (((uint8 *)((POINTER_SIZE_INT) p_word + sizeof(POINTER_SIZE_INT)) < p_ceil) && (*p_word == word_search_value)) {
        num_all_same_bits += (sizeof(POINTER_SIZE_INT) * GC_NUM_BITS_PER_BYTE); // jump ahead 32 bits or 64 bits
        p_word++;   // skip a word each time
        p_byte = (uint8 *) p_word;
    }

    while ((p_byte < p_ceil) && (*p_byte == byte_search_value)) {
        num_all_same_bits += GC_NUM_BITS_PER_BYTE;
        p_byte++;
    }
    
    bit_index = 0;
    while ((p_byte < p_ceil) && (bit_index < GC_NUM_BITS_PER_BYTE) && IS_DESIRED_BIT_VALUE(is_zero_str, p_byte, bit_index)){
        bit_index++;
        num_all_same_bits++;
    }

    *num_consec_bits = num_all_same_bits;   
    return is_zero_str ? false : true;
}

// Centralize all mallocs to this routine...
static unsigned int total_memory_alloced = 0;

void *malloc_or_die(unsigned int size)
{
    void *temp = STD_MALLOC(size);
    if (!temp) {
        DIE("Internal malloc_or_die out of C heap memory");
    }
    total_memory_alloced += size;
    return temp;
}
