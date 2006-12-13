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
 * @author Intel, Pavel A. Ozhdikhin
 * @version $Revision: 1.15.16.4 $
 *
 */

#ifndef _OPTIMIZER_H_
#define _OPTIMIZER_H_

#include "TranslatorIntfc.h"

namespace Jitrino {

struct MemoptFlags;
struct AbcdFlags;
struct GcmFlags;
struct SyncOptFlags;
struct LoopBuilderFlags;

struct OptimizerFlags {
    
    //global optimizer flags
    bool dumpdot;

    bool cse_final;

    uint32 hash_init_factor;
    uint32 hash_resize_factor;
    uint32 hash_resize_to;
    uint32 hash_node_var_factor;
    uint32 hash_node_tmp_factor;
    uint32 hash_node_constant_factor;

    bool sink_constants;
    bool sink_constants1;

    //simplifier flags
    bool elim_cmp3;
    bool use_mulhi;
    bool lower_divconst;
    bool ia32_code_gen;
    bool do_sxt;
    bool reduce_compref;


    //hvn flag
    bool elim_checks;
    bool gvn_exceptions;
    bool gvn_aggressive;
    bool hvn_exceptions;
    bool hvn_constants;


    //profiler flags
    uint32 profile_threshold;
    bool use_average_threshold;
    bool use_minimum_threshold;
    bool use_fixed_threshold;

    //dce flags
    bool fixup_ssa;
    bool dce2;
    bool preserve_critical_edges;
    

    //ssa
    bool better_ssa_fixup;

    //statprof
    bool statprof_do_loop_heuristics_override;
    const char* statprof_heuristics;

    //gc-mptr-analyzer
    bool gc_build_var_map;

    //devirt
    bool devirt_do_aggressive_guarded_devirtualization;
    bool devirt_use_cha_with_profile;
    int devirt_use_cha_with_profile_threshold;
    bool devirt_skip_exception_path;
    float devirt_block_hotness_multiplier;
    bool devirt_skip_object_methods;
    bool devirt_intf_methods;

    //unguard
    int unguard_dcall_percent;
    int unguard_dcall_percent_of_entry;


    AbcdFlags*              abcdFlags;
    GcmFlags*               gcmFlags;
    MemoptFlags*            memOptFlags;
    SyncOptFlags*           syncOptFlags;
    LoopBuilderFlags*       loopBuilderFlags;    
};

} //namespace Jitrino 

#endif // _OPTIMIZER_H_
