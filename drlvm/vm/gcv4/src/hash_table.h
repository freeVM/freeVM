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
 * @version $Revision: 1.1.2.1.4.3 $
 */  


#ifndef _hash_table_H_
#define _hash_table_H_

#include "open/types.h"

//
// Provides hash table support for remembered sets, scan sets, etc.
//


class Hash_Table {
public:
    Hash_Table();    

    virtual ~Hash_Table();
    //
    // Add an entry into this hash table, if it doesn't already exist.
    // Returns true if an entry was added.
    bool add_entry_if_required(void *address);

    // Add an entry and return the offset to where it was added or already was present.
    unsigned add_entry(void *address);

    void empty_all();

    inline bool is_empty()
        {return _resident_count == 0 ? true : false;}

    inline bool is_not_empty()
        {return _resident_count == 0 ? false : true;}

    Hash_Table *merge(Hash_Table *rs);

    bool is_present(void *address);

    bool is_not_present(void *address) {

        if (is_present(address)) {
            return false;
        }

        return true;
    }

    void rewind(void);

    void *next(void);

    unsigned int size() {
        return _resident_count;
    }

protected:

    virtual void _extend();

    int _get_offset(void *address);

    //
    // An offset into the primes table corresponding to the
    // current size of the hash table.
    //
    unsigned int _prime_index;

    //
    // The number of entries currently in this hash table.
    //
    int _resident_count;

    int _save_pointer;      // used to record last item served

    //
    // The number of entries that this hash table can accomodate.
    //
    int _size_in_entries;

    int _size_in_bytes;
    //
    // Storage for this hash table.
    // volatile for support extending in different threads.
    //
    volatile void **_table;

    int _threshold_entries;

private:

    unsigned int _do_rs_hash(POINTER_SIZE_INT address, unsigned int table_size);


    void *_next();
    //
    // An entry at location hash_code has just been deleted. We need
    // to scan to the next zero entry and rehash every intervening
    // entry so that this new zero entry doesn't confuse subsequent
    // add_entries from creating duplicates. This needs to be done
    // in a batch, (delete all, then rehash all) to minimize complexity.
    //
    void _rehash(unsigned int hash_code);


    void _rewind();

};



// Provides hash table support for remembered sets, scan sets, etc.
//

class Count_Hash_Table {
public:
    Count_Hash_Table();    

    virtual ~Count_Hash_Table();

    // Add an entry and return the offset to where it was added or already was present.
    unsigned add_entry(void *address);
    
    LONG get_val(void* address);

    void inc_val(void* address, int increment);

    void empty_all();
    
    void zero_counts();

    inline bool is_empty()
        {return _resident_count == 0 ? true : false;}

    inline bool is_not_empty()
        {return _resident_count == 0 ? false : true;}

    bool is_present(void *address);

    bool is_not_present(void *address) {
        if (is_present(address)) {
            return false;
        }
        return true;
    }

    void rewind(void);

    void *next(void);

    unsigned int size() {
        return _resident_count;
    }

protected:

    virtual void _extend();

    int _get_offset(void *address);
    //
    // An offset into the primes table corresponding to the
    // current size of the hash table.
    //
    unsigned int _prime_index;

    //
    // The number of entries currently in this hash table.
    //
    int _resident_count;

    int _save_pointer;      // used to record last item served

    //
    // The number of entries that this hash table can accomodate.
    //
    int _size_in_entries;

    int _size_in_bytes;
    //
    // Storage for this hash table.
    // volatile since it can be extended by different threads.
    //
    volatile void **_key_table;

    LONG *_val_table;


    int _threshold_entries;

private:

    unsigned int _do_rs_hash(POINTER_SIZE_INT address, unsigned int table_size);

    void *_next();

    void _rewind();
};

#endif // _hash_table_H_

