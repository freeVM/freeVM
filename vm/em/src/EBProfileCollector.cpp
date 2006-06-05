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
* @author Mikhail Y. Fursov
* @version $Revision: 1.1.2.2.4.3 $
*/

#include "EBProfileCollector.h"

#include <algorithm>
#include <assert.h>

#include "cxxlog.h"
#include <sstream>

#define LOG_DOMAIN "em"


EBProfileCollector::EBProfileCollector(EM_PC_Interface* em, const std::string& name, JIT_Handle genJit, 
                                       EB_ProfilerMode _mode, uint32 _eThreshold, uint32 _bThreshold,
                                       uint32 _initialTimeout, uint32 _timeout) 
                                       : ProfileCollector(em, name, EM_PCTYPE_ENTRY_BACKEDGE, genJit),
                                        mode(_mode), eThreshold(_eThreshold), bThreshold(_bThreshold), 
                                        initialTimeout(_initialTimeout), timeout(_timeout), loggingEnabled(false)
                                       
{
    assert( (mode == EB_PCMODE_SYNC ? (initialTimeout==0 && timeout==0) : timeout > 0) );
    catName = std::string(LOG_DOMAIN) + ".profiler." + name;
    loggingEnabled =  is_info_enabled(LOG_DOMAIN);
    if (!loggingEnabled) {
        loggingEnabled = is_info_enabled(catName.c_str());
    }
    if (loggingEnabled) {
        std::ostringstream msg;
        msg<< "EM: entry-backedge profiler intialized: "<<name
            <<" entry threshold:"<<eThreshold << " backedge threshold:"<<bThreshold
            <<" mode:"<<(mode == EB_PCMODE_ASYNC? "ASYNC": "SYNC");
        INFO2(catName.c_str(), msg.str().c_str());
    }
}

Method_Profile_Handle eb_profiler_create_profile(PC_Handle ph, Method_Handle mh) {
    ProfileCollector* pc = (ProfileCollector*)ph;
    assert(pc->type == EM_PCTYPE_ENTRY_BACKEDGE);
    EBMethodProfile* profile = ((EBProfileCollector*)pc)->createProfile(mh);
    assert(profile!=NULL);
    return (Method_Profile_Handle)profile;
}

void* eb_profiler_get_entry_counter_addr(Method_Profile_Handle mph) {
    MethodProfile* mp = (MethodProfile*)mph;
    assert(mp->pc->type == EM_PCTYPE_ENTRY_BACKEDGE);
    return (void*)&((EBMethodProfile*)mp)->entryCounter;
}


void* eb_profiler_get_backedge_counter_addr(Method_Profile_Handle mph) {
    MethodProfile* mp = (MethodProfile*)mph;
    assert(mp->pc->type == EM_PCTYPE_ENTRY_BACKEDGE);
    return (void*)&((EBMethodProfile*)mp)->backedgeCounter;
}

char eb_profiler_is_in_sync_mode(PC_Handle pch) {
    assert(pch!=NULL);
    ProfileCollector* pc = (ProfileCollector*)pch;
    assert(pc->type == EM_PCTYPE_ENTRY_BACKEDGE);
    return ((EBProfileCollector*)pc)->getMode() == EBProfileCollector::EB_PCMODE_SYNC;
}


void __stdcall eb_profiler_sync_mode_callback(Method_Profile_Handle mph) {
    assert(mph!=NULL);
    MethodProfile* mp = (MethodProfile*)mph;
    assert(mp->pc->type == EM_PCTYPE_ENTRY_BACKEDGE);
    ((EBProfileCollector*)mp->pc)->syncModeJitCallback(mp);
}

uint32 eb_profiler_get_entry_threshold(PC_Handle pch) {
    assert(pch!=NULL);
    ProfileCollector* pc = (ProfileCollector*)pch;
    assert(pc->type == EM_PCTYPE_ENTRY_BACKEDGE);
    return ((EBProfileCollector*)pc)->getEntryThreshold();
}

uint32 eb_profiler_get_backedge_threshold(PC_Handle pch) {
    assert(pch!=NULL);
    ProfileCollector* pc = (ProfileCollector*)pch;
    assert(pc->type == EM_PCTYPE_ENTRY_BACKEDGE);
    return ((EBProfileCollector*)pc)->getBackedgeThreshold();
}


EBProfileCollector::~EBProfileCollector() {
    for (EBProfilesMap::iterator it = profilesByMethod.begin(), end = profilesByMethod.end(); it!=end; ++it) {
        EBMethodProfile* profile = it->second;
        delete profile;
    }
}

MethodProfile* EBProfileCollector::getMethodProfile(Method_Handle mh) const {
     EBProfilesMap::const_iterator it = profilesByMethod.find(mh);
     if (it == profilesByMethod.end()) {
        return NULL;
     }
     return it->second;
}

EBMethodProfile* EBProfileCollector::createProfile(Method_Handle mh) {
    EBMethodProfile* profile = new EBMethodProfile(this, mh);

    profilesLock.lock();

    assert(profilesByMethod.find(mh) == profilesByMethod.end());
    profilesByMethod[mh] = profile;
    if (mode == EB_PCMODE_ASYNC) {
    newProfiles.push_back(profile);
    }

    profilesLock.unlock();

    return profile;
}

static void logReadyProfile(const std::string& catName, const std::string& profilerName, EBMethodProfile* mp) {
    const char* methodName = method_get_name(mp->mh);
    Class_Handle ch = method_get_class(mp->mh);
    const char* className = class_get_name(ch);
    const char* signature = method_get_descriptor(mp->mh);

    std::ostringstream msg;
    msg <<"EM: profiler["<<profilerName.c_str()<<"] profile is ready [e:"
        << mp->entryCounter <<" b:"<<mp->backedgeCounter<<"] "
        <<className<<"::"<<methodName<<signature;
    INFO2(catName.c_str(), msg.str().c_str());
}

void EBProfileCollector::onTimeout() {
    assert(mode == EB_PCMODE_ASYNC);
    if(!newProfiles.empty()) {
        profilesLock.lock();
        greenProfiles.insert(greenProfiles.end(), newProfiles.begin(), newProfiles.end());
        newProfiles.clear();
        profilesLock.unlock();
    }

    for (std::vector<EBMethodProfile*>::iterator it = greenProfiles.begin(), end = greenProfiles.end(); it!=end; ++it) {
        EBMethodProfile* profile = *it;
        if (profile->entryCounter >= eThreshold || profile->backedgeCounter >= bThreshold) {
            tmpProfiles.push_back(profile);
            *it = NULL;
        }
    }
    if (!tmpProfiles.empty()) {
        std::remove(greenProfiles.begin(), greenProfiles.end(), (EBMethodProfile*)NULL);
        greenProfiles.resize(greenProfiles.size() - tmpProfiles.size());
        for (std::vector<EBMethodProfile*>::iterator it = tmpProfiles.begin(), end = tmpProfiles.end(); it!=end; ++it) {
            EBMethodProfile* profile = *it;
            if (loggingEnabled) {
                logReadyProfile(catName, name, profile);
            }
            em->methodProfileIsReady(profile);
        }
        tmpProfiles.clear();
    }
}

void EBProfileCollector::syncModeJitCallback(MethodProfile* mp) {
    assert(mode == EB_PCMODE_SYNC);
    assert(mp->pc == this);
    if (loggingEnabled) {
        logReadyProfile(catName, name, (EBMethodProfile*)mp);
    }
    em->methodProfileIsReady(mp);
}
