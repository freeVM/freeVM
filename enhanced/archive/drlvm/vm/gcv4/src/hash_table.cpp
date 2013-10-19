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


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

#include <assert.h>
// VM interface header files
//#include "platform_lowlevel.h"
//#include "open/types.h"
#include "hash_table.h"
#include "port_malloc.h"
#include <apr_atomic.h>

// GC includes
#include "gc_cout.h"

#undef LOG_DOMAIN
#define LOG_DOMAIN "gc.hash"

///////////////////////////////////////////////////////////////////////////////////////////////////////////////



//
// Hash codes are unsigned ints which are 32 bit quantities on both ia32 and ipf.
// This naturally limits the size of hash tables to sizes that will fit in 32 bits.
//

// ? Are these really primes.. This limits the size of a remset.
unsigned primes [] = {2017,
                      5501, 
                      10091, 
                      20021, 
                      40009, 
                      80021, 
                      160001, 
                      320009, 
                      640007, 
                      1280023,
                      2560037,
                      5120053    };

const unsigned int NUMBER_OF_PRIMES     = 12;

const double HASH_TABLE_THRESHOLD = 0.6;

Hash_Table::Hash_Table()
{
    _prime_index = 0;

    _size_in_entries = primes[_prime_index++];

    double threshold = HASH_TABLE_THRESHOLD;
 
    _resident_count       = 0;
    _size_in_bytes        = _size_in_entries * (sizeof(void *));
    _threshold_entries    = (unsigned int)(_size_in_entries * threshold);
    _save_pointer         = 0;
    //TRACE("Making new hash table with size_in_entries " << _size_in_entries 
    //    << " and threashhold_entries " << _threshold_entries);
    _table = (volatile void **)STD_MALLOC(_size_in_bytes);
    assert(_table != NULL);
    memset(_table, 0, _size_in_bytes);

    //TRACE("Created hash_table " << (void *)this);
    return;
}

Hash_Table::~Hash_Table()
/* Discard this hash table. */
{
    STD_FREE(_table);
}



//
// Add an entry into this hash table, if it doesn't already exist.
// Returns true if an entry was added.
bool
Hash_Table::add_entry_if_required(void *address)
{
    //TRACE("Adding entry " << address);
    // Before we add the entry, if we might possible overflow
    // extend. Once passed this extent point we know we have
    // enough room for this entry...
    if (_resident_count > _threshold_entries) {
        _extend();
    }
    //
    // Adding a null entry is illegal, since we can't distinguish
    // it from an empty slot.
    //
    assert(address != NULL);
    //
    // Obtain the hash associated with this entry.
    //
    unsigned int hash_code = _do_rs_hash((POINTER_SIZE_INT)address,
                                         _size_in_entries);

    volatile void *target = _table[hash_code];

    if (target == address) {  // already there
        return false;
    }
    //
    // Beyond this point, the table will get modified.
    //
    // The code that was not thread safe simple did  _table[hash_code] = address;
    if (target == NULL) { // empty: try to insert in a thread safe way.

#ifdef GC_THREAD_SAFE_REMSET
        if (apr_atomic_casptr(
            (volatile void **)&(_table[hash_code]), address, NULL) == NULL) {
            // This slot was not taken before we could get to it, great, return.
            _resident_count++;
            return true;
        }
#else
        // This is not thread safe but putting things in remsets is only
        // done while holding the gc_lock.
        _table[hash_code] = address;
        _resident_count++;
        return true;
#endif // GC_THREAD_SAFE_REMSET
    }

    while (TRUE) {
        // This loop is guaranteed to terminate since our residency
        // rate is guaranteed to be less than 90%
        hash_code = (hash_code + 1) % _size_in_entries;
        if (address == _table[hash_code]) { // hit
            return false;
        }

        if (_table[hash_code] == NULL) {// empty slot for now
#ifdef GC_THREAD_SAFE_REMSET
            // Thread unsafe code does _table[hash_code] = address;
            if (apr_atomic_casptr(
                (volatile void **)&(_table[hash_code]), address, NULL) == NULL) {
                // This slot was not taken before we could get to it, great, return.
                _resident_count++;
                return true;
            }
#else
            // This is not thread safe but putting things in remsets is only
            // done while holding the gc_lock.
            _table[hash_code] = address;
            _resident_count++;
            return true;
#endif 
        }
    }
}



//
// Add an entry into this hash table, if it doesn't already exist.
//
unsigned
Hash_Table::add_entry(void *address)
{
    //TRACE("Adding entry " << address);

    // Before we add the entry, if we might possible overflow
    // extend. Once passed this extent point we know we have
    // enough room for this entry...
    if (_resident_count > _threshold_entries) {
        //TRACE("------- EXTENDING HASH TABLE --------");
        _extend();
    }
    //
    // Adding a null entry is illegal, since we can't distinguish
    // it from an empty slot. 
    //
    assert(address != NULL);
    //
    // Obtain the hash associated with this entry.
    //
    int hash_code = _do_rs_hash((POINTER_SIZE_INT)address,
                                         _size_in_entries);

    volatile void *target = _table[hash_code];

    if (target == address) {  // already there
        return hash_code;
    }
    //
    // Beyond this point, the table will get modified.
    //
    // The code that was not thread safe simple did  _table[hash_code] = address;
    if (target == NULL) { // empty: try to insert in a thread safe way.

#ifdef GC_THREAD_SAFE_REMSET
        if (apr_atomic_casptr(
            (volatile void **)&(_table[hash_code]), address, NULL) == NULL) {
            // This slot was not taken before we could get to it, great, return.
            _resident_count++;
            return hash_code;
        }
#else
        // This is not thread safe but putting things in remsets is only
        // done while holding the gc_lock.
        _table[hash_code] = address;
        _resident_count++;
        return hash_code;
#endif // GC_THREAD_SAFE_REMSET
    }

    while (TRUE) {
        // This loop is guaranteed to terminate since our residency
        // rate is guaranteed to be less than 90%
        hash_code = (hash_code + 1);
        if (hash_code >= _size_in_entries) {
            hash_code = hash_code  % _size_in_entries;
        }

        if (address == _table[hash_code]) { // hit
            return hash_code;
        }

        if (_table[hash_code] == NULL) {// empty slot for now
#ifdef GC_THREAD_SAFE_REMSET
            // Thread unsafe code does _table[hash_code] = address;
            if (apr_atomic_casptr(
                (volatile void **)&(_table[hash_code]), address, NULL) == NULL) {
                // This slot was not taken before we could get to it, great, return.
                _resident_count++;
                return hash_code;
            }
#else
            // This is not thread safe but putting things in remsets is only
            // done while holding the gc_lock.
            _table[hash_code] = address;
            _resident_count++;
            return hash_code;
#endif 
        }
    }
}

//
// An entry at location hash_code has just been deleted. We need
// to scan to the next zero entry and rehash every intervening
// entry so that this new zero entry doesn't confuse subsequent
// add_entries from creating duplicates. This needs to be done
// in a batch, (delete all, then rehash all) to minimize complexity.
//
void
Hash_Table::_rehash(unsigned int hash_code)
{
    //TRACE(" Rehashing entry " << hash_code);
    volatile void *address = _table[hash_code];
    //
    // Since we start scanning at the freshly deleted
    // slot, we have to accomodate an initial zero.
    //
    if (address!=0) {
        //TRACE(" In rehash removing " << address);
        _resident_count--;
        _table[hash_code] = 0;
    }
    //
    // Hitting a zero at the next entry indicates that
    // we have scanned far enough. This is guaranteed to
    // terminate since we rehash only , and immediately
    // after, a deletion. (Beyond that, our residency 
    // rate is never near 100% anyway.)
    //
    unsigned int next_entry = (hash_code + 1) % _size_in_entries;
    if (_table[next_entry]!=0) {
        _rehash(next_entry);
    }
    //
    // On the unrecursion path, do the re-insertion of
    // the address that we saved.
    //
    if (address!=0) {
        add_entry((void *)address);
    }
}

void
Hash_Table::empty_all()
{
    memset (_table, 0, _size_in_bytes);
    _resident_count = 0;
    return;
}

Hash_Table *
Hash_Table::merge(Hash_Table *p_Hash_Table)
/* Merge two remembered sets, and return
   the merged one. Typical use includes merging the
   remembered sets of all the cars of a train, or of
   merging the remembered set of a region with the
   relevant portion of the root set. */
{

    void *p_entry;

    p_Hash_Table->rewind();

    while ((p_entry = p_Hash_Table->next()) != NULL) {
        this->add_entry(p_entry);
    }

    return this;
}

unsigned int
Hash_Table::_do_rs_hash(POINTER_SIZE_INT address, unsigned int table_size)
/* A dumb hashing function for insertion of a new entry
   into the remembered set. Need to improve. */
{
    POINTER_SIZE_INT result = address * 42283;
    assert((POINTER_SIZE_INT)(result % table_size) <= (POINTER_SIZE_INT)0xFFFFFFFF);
    return ((unsigned int)result % table_size);
}

void
Hash_Table::_extend()
/* The residency in our remembered set has exceeded a pre-defined
   threshold. Therefore we create a larger remembered set and re-
   hash. 
   Always rehash after doing an extend. */
{
    volatile void **p_save_table       = _table;
    int saved_size_in_entries = _size_in_entries;

    if (_prime_index >= NUMBER_OF_PRIMES) {
        _size_in_entries = _size_in_entries * 2; // Not a prime but really big.
    } else {
        _size_in_entries   = primes[_prime_index++];
    }
    _size_in_bytes     = sizeof(void *) * _size_in_entries;
    _threshold_entries = (unsigned int)(_size_in_entries * HASH_TABLE_THRESHOLD);

    _resident_count     = 0;
    _table = (volatile void **)STD_MALLOC(_size_in_bytes);

    if (_table == NULL) {
        ABORT("STD_MALLOC failed when extending remembered set");
    }

    memset(_table, 0, _size_in_bytes);

    for (int index = 0; index < saved_size_in_entries; index++) {
        if (p_save_table[index] != NULL) {
            this->add_entry((void *)(p_save_table[index]));
        }
    }

    STD_FREE(p_save_table);

    //TRACE(" xxxxxxxxxxxx ROOT TABLE HAS BEEN EXTENDED xxxxxxxxxxxxxxxxxxxxxx");
}

bool
Hash_Table::is_present(void *address)
/* Add an entry into the remembered set. This represents an
   address of a slot of some object in a different space
   that is significant to the space associated with this
   remembered set. */
{
    if (address == NULL) 
        return false;
    // Always rehash after doing an extend.
    unsigned int hash_code = _do_rs_hash((POINTER_SIZE_INT)address,
                                         _size_in_entries);

    volatile void *target = _table[hash_code];

    if (target == address) { // already there
        return true;
    }

    if (target == NULL) { // empty: absent
        return false;
    }
    //
    // Save our position before looping.
    //
    unsigned int saved_hash_code = hash_code;
    //
    // Loop through subsequent entries looking for match.
    //
    while (TRUE) {
        // This loop is guaranteed to terminate since our residency
        // rate is guaranteed to be less than 90%
        hash_code = (hash_code + 1) % _size_in_entries;

        if (_table[hash_code] == NULL) 
            return false;
       
        if (address == _table[hash_code])  // hit
            return true;

        if (hash_code == saved_hash_code) {
            //
            // We have traversed a full circle and are back
            // where we started, so we are sure it isn't there.
            //
            return false;
        }
    }
}

int
Hash_Table::_get_offset(void *address)
/* Add an entry into the remembered set. This represents an
   address of a slot of some object in a different space
   that is significant to the space associated with this
   remembered set. */
{
    if (address == NULL) 
        return -1;

    // Always rehash after doing an extend.
    unsigned int hash_code = _do_rs_hash((POINTER_SIZE_INT)address,
                                         _size_in_entries);

    volatile void *target = _table[hash_code];

    if (target == address) { // already there
        return hash_code;
    }

    if (target == NULL) { // empty: absent
        return -1;
    }
    //
    // Save our position before looping.
    //
    unsigned int saved_hash_code = hash_code;
    //
    // Loop through subsequent entries looking for match.
    //
    while (TRUE) {
        // This loop is guaranteed to terminate since our residency
        // rate is guaranteed to be less than 90%
        hash_code = (hash_code + 1) % _size_in_entries;

        if (_table[hash_code] == NULL) 
            return -1;
        
        if (address == _table[hash_code])  // hit
            return hash_code;

        if (hash_code == saved_hash_code) {
            //
            // We have traversed a full circle and are back
            // where we started, so we are sure it isn't there.
            //
            return -1;
        }
    }
}


void *
Hash_Table::next()
{
    //
    // See if there are any entries in this hash table.
    //
    if (_resident_count == 0) {
        //
        // Nope - bail out.
        //
        return NULL;
    }


    if (_save_pointer >= _size_in_entries) {
        return NULL;
    }

    while (_table[_save_pointer] == NULL) {
        _save_pointer += 1;

        if (_save_pointer == _size_in_entries) {
            return NULL;
        }
    }

    void *p_return = (void *)_table[_save_pointer];
    _save_pointer++;
    return p_return;
}

//
// Start at the beginning for subsequent scans.
//
void
Hash_Table::rewind()
{
    _save_pointer = 0;
}

////////////////////////////////////////////////////////////////////////////////
//
//
// The key val hash table code which looks a lot like the code above.
//
//
/////////////////////////////////////////////////////////////////////////////// 
Count_Hash_Table::Count_Hash_Table()
{
    _prime_index = 0;

    _size_in_entries = primes[_prime_index++];

    double threshold = HASH_TABLE_THRESHOLD;
 
    _resident_count       = 0;
    _size_in_bytes        = _size_in_entries * (sizeof(void *));
    _threshold_entries    = (unsigned int)(_size_in_entries * threshold);
    _save_pointer         = 0;

    _key_table = (volatile void **)STD_MALLOC(_size_in_bytes);
    assert(_key_table != NULL);
    memset(_key_table, 0, _size_in_bytes);

    int val_size_in_bytes = _size_in_entries * (sizeof(LONG));
    _val_table = (LONG *)STD_MALLOC(val_size_in_bytes);
    assert(_val_table != NULL); 
    memset((void *)_val_table, 0, val_size_in_bytes);

    return;
}

Count_Hash_Table::~Count_Hash_Table()
/* Discard this hash table. */
{
    STD_FREE(_key_table);
    STD_FREE((void *)_val_table);
}

//
// Add an entry into this hash table, if it doesn't already exist.
// If it does exist then just return the index to it.
//
unsigned
Count_Hash_Table::add_entry(void *address)
{
    //TRACE("Adding entry " << address);

    // Before we add the entry, if we might possible overflow
    // extend. Once passed this extent point we know we have
    // enough room for this entry...
    if (_resident_count > _threshold_entries) {
        //TRACE("------------ EXTENDING HASH TABLE ----------");
        _extend();
    }
    //
    // Adding a null entry is illegal, since we can't distinguish
    // it from an empty slot. 
    //
    assert(address != NULL);
    //
    // Obtain the hash associated with this entry.
    //
    int hash_code = _do_rs_hash((POINTER_SIZE_INT)address,
                                         _size_in_entries);

    volatile void *target = _key_table[hash_code];

    if (target == address) {  // already there
        return hash_code;
    }
    //
    // Beyond this point, the table will get modified.
    //
    // The code that was not thread safe simple did  _table[hash_code] = address;
    if (target == NULL) { // empty: try to insert in a thread safe way.

#ifdef GC_THREAD_SAFE_REMSET
        if (apr_atomic_casptr(
            (volatile void **)&(_key_table[hash_code]), address, NULL) == NULL) {
            // This slot was not taken before we could get to it, great, return.
            _resident_count++;
            return hash_code;
        }
#else
        // This is not thread safe but putting things in remsets is only
        // done while holding the gc_lock.
        _key_table[hash_code] = address;
        _resident_count++;
        return hash_code;
#endif // GC_THREAD_SAFE_REMSET
    }

    while (TRUE) {
        // This loop is guaranteed to terminate since our residency
        // rate is guaranteed to be less than 90%
        hash_code = (hash_code + 1);
        if (hash_code >= _size_in_entries) {
            hash_code = hash_code  % _size_in_entries;
        }

        if (address == _key_table[hash_code]) { // hit
            return hash_code;
        }

        if (_key_table[hash_code] == NULL) {// empty slot for now
#ifdef GC_THREAD_SAFE_REMSET
            // Thread unsafe code does _table[hash_code] = address;
            if (apr_atomic_casptr(
                (volatile void **)&(_key_table[hash_code]), address, NULL) == NULL) {
                // This slot was not taken before we could get to it, great, return.
                _resident_count++;
                return hash_code;
            }
#else
            // This is not thread safe but putting things in remsets is only
            // done while holding the gc_lock.
            _key_table[hash_code] = address;
            _resident_count++;
            return hash_code;
#endif 
        }
    }
}

LONG Count_Hash_Table::get_val(void* address)
{
    int index = _get_offset(address);
    return _val_table[index];
}


void Count_Hash_Table::inc_val(void* address, int increment)
{
    int index = _get_offset(address);

    assert(sizeof(LONG) == 4);
    assert(sizeof(int) == 4);
    apr_atomic_add32((volatile uint32 *) &(_val_table[index]), (uint32) increment);
}

void
Count_Hash_Table::empty_all()
{
    memset (_key_table, 0, _size_in_bytes);
    int val_size_in_bytes = sizeof(LONG) * _size_in_entries;
    memset ((void *)_val_table, 0, val_size_in_bytes);
    _resident_count = 0;
    return;
}

// Zero out the vals but keep the keys.
void 
Count_Hash_Table::zero_counts()
{
    int val_size_in_bytes = sizeof(LONG) * _size_in_entries;
    memset ((void *)_val_table, 0, val_size_in_bytes);
}

unsigned int
Count_Hash_Table::_do_rs_hash(POINTER_SIZE_INT address, unsigned int table_size)
/* A dumb hashing function for insertion of a new entry
   into the remembered set. Need to improve. */
{
    POINTER_SIZE_INT result = address * 42283;
    assert((POINTER_SIZE_INT)(result % table_size) <= (POINTER_SIZE_INT)0xFFFFFFFF);
    return ((unsigned int)result % table_size);
}

void
Count_Hash_Table::_extend()
/* The residency in our remembered set has exceeded a pre-defined
   threshold. Therefore we create a larger remembered set and re-
   hash. 
   Always rehash after doing an extend. */
{
    volatile void **p_save_key_table       = _key_table;
    LONG *p_save_val_table        = _val_table;
    int saved_size_in_entries = _size_in_entries;

    if (_prime_index >= NUMBER_OF_PRIMES) {
        _size_in_entries = _size_in_entries * 2; // Not a prime but really big.
    } else {
        _size_in_entries   = primes[_prime_index++];
    }
    _size_in_bytes     = sizeof(void *) * _size_in_entries;
    _threshold_entries = (unsigned int)(_size_in_entries * HASH_TABLE_THRESHOLD);

    _resident_count     = 0;
    _key_table = (volatile void **)STD_MALLOC(_size_in_bytes);

    if (_key_table == NULL) {
        ABORT("STD_MALLOC failed when extending key val table");
    }
    memset(_key_table, 0, _size_in_bytes);

    int val_size_in_bytes = sizeof(LONG) * _size_in_entries;
    _val_table = (LONG *)STD_MALLOC(val_size_in_bytes);

    if (_val_table == NULL) {
        ABORT("STD_MALLOC failed when extending key val table");
    }

    memset((void *)_val_table, 0, val_size_in_bytes);

    for (int index = 0; index < saved_size_in_entries; index++) {
        if (p_save_key_table[index] != NULL) {
            int new_index = this->add_entry((void *)(p_save_key_table[index]));
            _val_table[new_index] = p_save_val_table[index];
        } else {
            assert (p_save_val_table[index] == 0);
        }
    }

    STD_FREE(p_save_key_table);
    STD_FREE((void *)p_save_val_table);
}

bool
Count_Hash_Table::is_present(void *address)
/* Add an entry into the remembered set. This represents an
   address of a slot of some object in a different space
   that is significant to the space associated with this
   remembered set. */
{
    if (address == NULL) {
        return false;
    }

    // Always rehash after doing an extend.
    unsigned int hash_code = _do_rs_hash((POINTER_SIZE_INT)address,
                                         _size_in_entries);

    volatile void *target = _key_table[hash_code];

    if (target == address) { // already there
        return true;
    }

    if (target == NULL) { // empty: absent
        return false;
    }
    //
    // Save our position before looping.
    //
    unsigned int saved_hash_code = hash_code;
    //
    // Loop through subsequent entries looking for match.
    //
    while (TRUE) {
        // This loop is guaranteed to terminate since our residency
        // rate is guaranteed to be less than 90%
        hash_code = (hash_code + 1) % _size_in_entries;

        if (_key_table[hash_code] == NULL) 
            return false;
       
        if (address == _key_table[hash_code])  // hit
            return true;

        if (hash_code == saved_hash_code) {
            //
            // We have traversed a full circle and are back
            // where we started, so we are sure it isn't there.
            //
            return false;
        }
    }
}

int
Count_Hash_Table::_get_offset(void *address)
/* Add an entry into the remembered set. This represents an
   address of a slot of some object in a different space
   that is significant to the space associated with this
   remembered set. */
{
    if (address == NULL) {
        return -1;
    }

    // Always rehash after doing an extend.
    unsigned int hash_code = _do_rs_hash((POINTER_SIZE_INT)address,
                                         _size_in_entries);

    volatile void *target = _key_table[hash_code];

    if (target == address) { // already there
        return hash_code;
    }

    if (target == NULL) { // empty: absent
        return -1;
    }
    //
    // Save our position before looping.
    //
    unsigned int saved_hash_code = hash_code;
    //
    // Loop through subsequent entries looking for match.
    //
    while (TRUE) {
        // This loop is guaranteed to terminate since our residency
        // rate is guaranteed to be less than 90%
        hash_code = (hash_code + 1) % _size_in_entries;

        if (_key_table[hash_code] == NULL) 
            return -1;
        
        if (address == _key_table[hash_code])  // hit
            return hash_code;

        if (hash_code == saved_hash_code) {
            //
            // We have traversed a full circle and are back
            // where we started, so we are sure it isn't there.
            //
            return -1;
        }
    }
}

// Returns the next key, use get_val to get the value.
void *
Count_Hash_Table::next()
{
    //
    // See if there are any entries in this hash table.
    //
    if (_resident_count == 0) {
        //
        // Nope - bail out.
        //
        return NULL;
    }


    if (_save_pointer >= _size_in_entries) {
        return NULL;
    }

    while (_key_table[_save_pointer] == NULL) {
        _save_pointer += 1;

        if (_save_pointer == _size_in_entries) {
            return NULL;
        }
    }

    void *p_return = (void *)_key_table[_save_pointer];
    _save_pointer++;
    return p_return;
}

//
// Start at the beginning for subsequent scans.
//
void
Count_Hash_Table::rewind()
{
    _save_pointer = 0;
}

// end file gc\hash_table.cpp
