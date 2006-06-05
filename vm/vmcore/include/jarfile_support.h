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
 * @author Pavel Pervov
 * @version $Revision: 1.1.2.3.4.3 $
 */  
#ifndef _JAR_FILE_H_
#define _JAR_FILE_H_

#ifndef PLATFORM_POSIX
#pragma warning( disable: 4786 ) // identifier was truncated to 255 characters in the browser information
#endif

#include <string>
#include <map>
#include <vector>
#ifdef PLATFORM_POSIX
#include <unistd.h>
#else
#include <io.h>
#endif

#include "properties.h"
#include "manifest.h"
#include "lock_manager.h"

// flags
//  bit 0
static const int JAR_COMPRESSED = 1;

//  bits 1,2
// For Method 6 - Imploding
static const int JAR_8K_DICTIONARY = 2;
static const int JAR_3SF_TREES_USED = 4;
// For Method 8 - Deflating
enum JarCompressionRatio
{
    JAR_COMPRESS_NORMAL,
    JAR_COMPRESS_MAXIMUM,
    JAR_COMPRESS_FAST,
    JAR_COMPRESS_FASTEST
};

//  bit 3
// For Methos 8 - Deflating
static const int JAR_FIELDS_IN_DESCRIPTOR = 4;

//  bits 4-12
static const int JAR_UNUSED = 0x1FF8;

//  bits 13-15
static const int JAR_APP_SPECIFIC = 0xE000;

// compression methods
enum JarCompressionMethod
{
    JAR_FILE_STORED,
    JAR_FILE_SHRUNK,
    JAR_FILE_REDUCED1,
    JAR_FILE_REDUCED2,
    JAR_FILE_REDUCED3,
    JAR_FILE_REDUCED4,
    JAR_FILE_IMPLODED,
    JAR_FILE_RESERVED_TCA,
    JAR_FILE_DEFLATED,
    JAR_FILE_ENCHANCED_DEFLATE,
    JAR_FILE_PKWARED
};

// internal file attributes
static const int JAR_FILE_TEXT = 1;

// magic numbers to identify entries in the file
const unsigned int JAR_FILEENTRY_MAGIC = 0x04034b50;
const unsigned int JAR_DATADESCRIPTOR_MAGIC = 0x08074b50;
const unsigned int JAR_DIRECTORYENTRY_MAGIC = 0x02014b50;
const unsigned int JAR_EODENTRY_MAGIC = 0x06054b50;

const unsigned int JAR_DIRECTORYENTRY_LEN = 46;

typedef long JarCompressedOffset;

class JarEntry
{
    unsigned short m_version;
    unsigned short m_flags;
    unsigned short m_method;
    int m_sizeCompressed;
    unsigned int m_sizeUncompressed;
    unsigned short m_nameLength;
    unsigned short m_extraLength;
    char* m_fileName;
    unsigned int m_relOffset;
    static const unsigned int sizeFixed = 30;

protected:
    JarCompressedOffset m_contentOffset;
//    std::string m_jarFileName;
    int m_jarFileIdx;

public:
    JarEntry() {}
    // returns relative path in ZIP file
    const char* GetPath() const { return m_fileName; }
    // return length of decompressed content
    unsigned int GetContentSize() const { return m_sizeUncompressed; }
    // returns decompressed content in provided buffer;
    // buffer must have at least GetContentSize() bytes length
    // NB! upon erroneous return from GetContent user cannot rely on buffer content
    bool GetContent( unsigned char* content, JarFile *jf ) const;
    // construct fixed part of JarEntryHeader class from input stream
    void ConstructFixed( const unsigned char* stream );
    // verify that "stream" starts with JAR_DIRECTORYENTRY_MAGIC signature
    static bool IsDirEntryMagic( const unsigned char* stream ){
        return (0x01 == stream[0]) && (0x02 == stream[1]);
    }
    // check JAR_FILEENTRY_MAGIC = 0x04034b50;
    static bool IsFileEntryMagic( const unsigned char* stream ){
        return (0x03 == stream[0]) && (0x04 == stream[1]);
    }
    // check JAR_EODENTRY_MAGIC = 0x06054b50
    static bool IsEODMagic( const unsigned char* stream ){
        return (0x05 == stream[0]) && (0x06 == stream[1]);
    }
    // check PK signature
    static bool IsPK( const unsigned char* stream ){
        return (0x50 == stream[0]) && (0x4b == stream[1]);
    }

protected:
    friend class JarFile;
}; // class JarEntry


class JarFile
{
    int m_jarFileIdx;
    std::multimap<int, JarEntry> m_entries;
    Manifest* m_manifest;
    tl::MemoryPool pool;

    // list of the jar files
    static std::vector<std::string> m_jars;

public:
    JarFile() : m_manifest(NULL), jfh(0) {}
    JarFile( const JarFile& jf ) : jfh(0) {
        m_jarFileIdx = jf.m_jarFileIdx;
        m_entries = jf.m_entries;
        m_manifest = new Manifest( &jf );
    }
    ~JarFile() {
        if (jfh != 0) close(jfh);
        jfh = 0;
        if( m_manifest ) delete m_manifest;
        m_manifest = NULL;
    }
    // parses JAR file and stores its structure inside
    bool Parse( const char* filename );
    // get string hash value
    inline int GetHashValue( const char* je_name ) const{
        // hash fuction is taken from String_Pool class
        int hash = 0;
        const char *t = je_name;
        int c;
        int h1 = 0, h2 = 0;
        while ((c = *t++) != '\0') {
            h1 = h1 + c;
            if((c = *t++) == 0) {
                break;
            }
            h2 = h2 + c;
        }
        hash = (h1 + (h2 << 8)) & 0x7fffffff;
        return hash;
    }
    // looks up JAR entry in stored jar structure
    const JarEntry* Lookup( const char* je_name ) const {
        const int hash = GetHashValue(je_name);
        std::multimap<int, JarEntry>::const_iterator it = m_entries.find(hash);
        if (it != m_entries.end())
        {
            if (strcmp((*it).second.GetPath(),je_name) == 0) 
                return &(*it).second;
            it++;
        }

        while (it != m_entries.end())
        {
            int integr_type_check = it->first;
            if (integr_type_check != hash)
                break;
            if (strcmp((*it).second.GetPath(),je_name) == 0) 
                return &(*it).second;
            it++;
        }

        return NULL;
    }
    // returns manifest from parsed jar archive
    Manifest* Get_Manifest() { return m_manifest; }
    // returns JAR file name
    const char* GetName() { return m_jars[m_jarFileIdx].c_str(); }

    // handle of the jar file
    int jfh;
    Lock_Manager lock;
}; // class JarFile


#ifndef PLATFORM_POSIX
#pragma warning( default: 4786 ) // identifier was truncated to 255 characters in the browser information
#endif

#endif // _JAR_FILE_H_
