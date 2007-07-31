/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @author Pavel N. Vyssotski
 * @version $Revision: 1.7 $
 */
// OptionParser.cpp

#include <cstring>

#include "AgentBase.h"
#include "MemoryManager.h"
#include "AgentException.h"
#include "Log.h"

#include "OptionParser.h"

using namespace jdwp;
using namespace std;

OptionParser::OptionParser() throw()
{
    m_optionCount = 0;
    m_optionString = 0;
    m_options = 0;
    m_help = false;
    m_suspend = true;
    m_server = false;
    m_timeout = 0;
    m_transport = 0;
    m_address = 0;
    m_log = 0;
    m_kindFilter = 0;
    m_srcFilter = 0;
    m_onuncaught = false;
    m_onthrow = 0;
    m_launch = 0;
}

bool OptionParser::AsciiToBool(const char *str) throw(IllegalArgumentException)
{
    if (strcmp("y", str) == 0) {
        return true;
    } else if (strcmp("n", str) == 0) {
        return false;
    } else {
        throw IllegalArgumentException();
    }
}

void OptionParser::Parse(const char* str) throw(AgentException)
{
    size_t i;
    int k;

    if (str == 0)
        return;

    const size_t len = strlen(str);
    if (len == 0)
        return;

    for (i = 0; i < len; i++) {
        if (str[i] == ',') {
            m_optionCount++;
        } else if (str[i] == '"' || str[i] == '\'') {
            char quote = str[i];
            if (i > 0 && str[i-1] != '=') {
                throw IllegalArgumentException();
            }
            i++;
            while (i < len && str[i] != quote) {
                i++;
            }
            if (i+1 < len && str[i+1] != ',') {
                throw IllegalArgumentException();
            }
        }
    }
    m_optionCount++;

    m_optionString = reinterpret_cast<char*>(AgentBase::GetMemoryManager().
        Allocate(len + 1 JDWP_FILE_LINE));
    strcpy(m_optionString, str);

    m_options = reinterpret_cast<Option*>(AgentBase::GetMemoryManager().
        Allocate(m_optionCount * sizeof(Option) JDWP_FILE_LINE));

    m_options[0].name = m_optionString;
    m_options[0].value = "";
    k = 0;
    bool waitEndOfOption = false;
    for (i = 0; i < len && k < m_optionCount; i++) {
        if ((m_optionString[i] == '=') && (!waitEndOfOption)) {
            waitEndOfOption = true; 
            m_optionString[i] = '\0';
            m_options[k].value = &m_optionString[i+1];
        } else if (m_optionString[i] == ',') {
            waitEndOfOption = false;
            m_optionString[i] = '\0';
            k++;
            m_options[k].name = &m_optionString[i+1];
            m_options[k].value = "";
        } else if (m_optionString[i] == '"' || m_optionString[i] == '\'') {
            char quote = m_optionString[i];
            m_optionString[i] = '\0';
            m_options[k].value = &m_optionString[i+1];
            i++;
            while (i < len && m_optionString[i] != quote) {
                i++;
            }
            if (i < len) {
                m_optionString[i] = '\0';
            }
        }
    }

    for (k = 0; k < m_optionCount; k++) {
        if (strcmp("transport", m_options[k].name) == 0) {
            m_transport = m_options[k].value;
        } else if (strcmp("address", m_options[k].name) == 0) {
            m_address = m_options[k].value;
        } else if (strcmp("timeout", m_options[k].name) == 0) {
            m_timeout = atol(m_options[k].value);
        } else if (strcmp("suspend", m_options[k].name) == 0) {
            m_suspend = AsciiToBool(m_options[k].value);
        } else if (strcmp("server", m_options[k].name) == 0) {
            m_server = AsciiToBool(m_options[k].value);
        } else if (strcmp("launch", m_options[k].name) == 0) {
            m_launch = m_options[k].value;
        } else if (strcmp("onuncaught", m_options[k].name) == 0) {
            m_onuncaught = AsciiToBool(m_options[k].value);
        } else if (strcmp("onthrow", m_options[k].name) == 0) {
            m_onthrow = m_options[k].value;
        } else if (strcmp("help", m_options[k].name) == 0) {
            m_help = true;
#ifndef NDEBUG
        } else if (strcmp("log", m_options[k].name) == 0) {
            m_log = m_options[k].value;
        } else if (strcmp("trace", m_options[k].name) == 0) {
            m_kindFilter = m_options[k].value;
        } else if (strcmp("src", m_options[k].name) == 0) {
            m_srcFilter = m_options[k].value;
#endif // NDEBUG
        }
    }
    if ((m_onthrow != 0) || (m_onuncaught != 0)) {
        if (m_launch == 0) {
            JDWP_ERROR("Specify launch=<command line> when using onthrow or onuncaught option");
            throw IllegalArgumentException();
        }
    }
}

OptionParser::~OptionParser() throw()
{
    if (m_optionString != 0)
        AgentBase::GetMemoryManager().Free(m_optionString JDWP_FILE_LINE);
    if (m_options != 0)
        AgentBase::GetMemoryManager().Free(m_options JDWP_FILE_LINE);
}

const char *OptionParser::FindOptionValue(const char *name) const throw()
{
    for (int i = 0; i < m_optionCount; i++) {
        if (strcmp(name, m_options[i].name) == 0) {
            return m_options[i].value;
        }
    }
    return 0;
}
