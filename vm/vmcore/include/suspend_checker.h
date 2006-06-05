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
 * @author Gregory Shimansky
 * @version $Revision: 1.1.2.1.4.4 $
 */  


#ifndef _SUSPEND_CHECKER_H_
#define _SUSPEND_CHECKER_H_

#include "open/thread.h"

class SuspendEnabledChecker
{
public:
    inline SuspendEnabledChecker()
    {
        assert(tmn_is_suspend_enabled());
    }

    inline ~SuspendEnabledChecker()
    {
        assert(tmn_is_suspend_enabled());
    }
};

class SuspendDisabledChecker
{
public:
    inline SuspendDisabledChecker()
    {
        assert(!tmn_is_suspend_enabled());
    }

    inline ~SuspendDisabledChecker()
    {
        assert(!tmn_is_suspend_enabled());
    }
};

#endif //!_SUSPEND_CHECKER_H_
