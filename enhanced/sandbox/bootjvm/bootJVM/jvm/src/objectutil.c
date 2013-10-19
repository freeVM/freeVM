/*!
 * @file objectutil.c
 *
 * @brief Utility and glue functions for
 * @link jvm/src/class.c class.c@endlink
 * and @c @b java.lang.Object
 *
 * Various functions in this file tend to get moved around between
 * @link jvm/src/threadutil.c threadutil.c@endlink and
 * @link jvm/src/threadutil.c classutil.c@endlink.
 *
 *
 * @internal Due to the fact that the implementation of the Java object
 *           and the supporting robject structure is deeply embedded in
 *           the core of the development of this software, this file
 *           has contents that come and go during development.  Some
 *           functions get staged here before deciding where they
 *           @e really go; some are interim functions for debugging,
 *           some were glue that eventually went away.  Be careful to
 *           remove prototypes to such functions from the appropriate
 *           header file.
 *
 *
 * @section Control
 *
 * \$URL$
 *
 * \$Id$
 *
 * Copyright 2005 The Apache Software Foundation
 * or its licensors, as applicable.
 *
 * Licensed under the Apache License, Version 2.0 ("the License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * @version \$LastChangedRevision$
 *
 * @date \$LastChangedDate$
 *
 * @author \$LastChangedBy$
 *
 *         Original code contributed by Daniel Lydick on 09/28/2005.
 *
 * @section Reference
 *
 */

#include "arch.h"
ARCH_SOURCE_COPYRIGHT_APACHE(objectutil, c,
"$URL$",
"$Id$");


#include "jvmcfg.h"
#include "classfile.h"
#include "jvm.h"
#include "jvmclass.h"
#include "linkage.h"


/*!
 * @brief Attempt to @c @b synchronize() on an object's
 * monitor lock by contending for it.
 *
 * If acquired, stay in the @b RUNNING state.  If not, go to the
 * @b SYNCHRONIZED state and arbitrate for it in the @b LOCK state.
 *
 *
 * @param  objhashthis  Object table hash of @c @b this object.
 *
 * @param  thridx       Thread table index of a thread requesting
 *                      ownership of this object's monitor lock.
 *
 *
 * @returns @link #rtrue rtrue@endlink if this thread now owns
 *          this object's monitor lock,
 *          otherwise @link #rfalse rfalse@endlink.
 *
 *
 * @todo HARMONY-6-jvm-objectutil.c-1 Make sure
 *       @b IllegalMonitorStateException logic covers all
 *       possibilities or needs to be removed.
 *
 * @todo HARMONY-6-jvm-objectutil.c-2 Make sure thread interruption
 *       logic below here is working.
 *
 * @throws JVMCLASS_JAVA_LANG_NULLPOINTEREXCEPTION
 *         @link #JVMCLASS_JAVA_LANG_NULLPOINTEREXCEPTION
           if the object hash is the null object@endlink,
 *         typically due to bad input parameter.
 *
 * @throws JVMCLASS_JAVA_LANG_ILLEGALMONITORSTATEEXCEPTION
 *         @link #JVMCLASS_JAVA_LANG_ILLEGALMONITORSTATEEXCEPTION
if current thread cannot possibly own the object's monitor lock@endlink,
 *         typically due to bad input parameter.
 *
 */

rboolean objectutil_synchronize(jvm_object_hash  objhashthis,
                                jvm_thread_index thridx)
{
    ARCH_FUNCTION_NAME(objectutil_synchronize);

#if 0
    /* If this thread cannot possibly own the object's monitor lock */
    if (some_condition)
    {
        thread_throw_exception(CURRENT_THREAD,
                               THREAD_STATUS_THREW_EXCEPTION,
                       JVMCLASS_JAVA_LANG_ILLEGALMONITORSTATEEXCEPTION);
/*NOTREACHED*/
    }
#endif

    /* Requested object must be in use and not null */
    if ((OBJECT_STATUS_INUSE & OBJECT(objhashthis).status)     &&
       ((!(OBJECT_STATUS_NULL & OBJECT(objhashthis).status))))
    {
        /* If nobody has it locked, */
        if ((!((OBJECT_STATUS_MLOCK & OBJECT(objhashthis).status))) ||

        /* or if I already own it  */
            ((OBJECT_STATUS_MLOCK & OBJECT(objhashthis).status) &&
             (OBJECT(objhashthis).mlock_thridx == CURRENT_THREAD)))
        {
            /*
             * Not currently locked, or currently own it anyway,
             * so take ownership.
             */
            OBJECT(objhashthis).status |= OBJECT_STATUS_MLOCK;

            OBJECT(objhashthis).mlock_count++;

            OBJECT(objhashthis).mlock_thridx = CURRENT_THREAD;

            /* No longer needed now that thread has ownership */
            THREAD(thridx).locktarget = jvm_object_hash_null;

            return(rtrue);
        }
        else
        {
            /*
             * If currently locked, tell thread model which object
             * is requested and go to @b SYNCHRONIZED to arbitrate
             * for the lock.
             */
            THREAD(thridx).locktarget = objhashthis;

            /*
             * This @e present function gets called later from
             * this @b SYNCHRONIZED @b REQUEST functino call, but
             * not until the state machine reaches the @b PROCESS
             * phase of the @b LOCK state (at a later time).  This
             * means that either this same @c @b else block
             * will get invoked again during lock arbitration in
             * the @b LOCK state or the opposite @c @b if
             * block will be called to acquire the MLOCK.  In both
             * cases, this present function will be invoked at
             * least once more before synchronization is complete.
             *
             */
            (rvoid) threadstate_request_synchronized(thridx);

            return(rfalse);
        }
    }

    /* This thread cannot possibly own the object's monitor lock */
    thread_throw_exception(CURRENT_THREAD,
                           THREAD_STATUS_THREW_EXCEPTION,
                           JVMCLASS_JAVA_LANG_NULLPOINTEREXCEPTION);
/*NOTREACHED*/
    return(rfalse); /* Satisfy compiler */

} /* END of objectutil_synchronize() */


/*!
 * @brief Test if @c @b synchronize() has successfully locked an
 * object's monitor lock on @e any thread (not any one in particular).
 *
 * Test does @e not consider which thread is the owner of
 * this object's monitor lock.  Use @link #objectutil_is_lock_owner
   objectutil_is_lock_owner@endlink to determine if this thread
 * is the owner of the object's monitor lock.
 *
 *
 * @param  objhashthis  Object table hash of @c @b this object.
 *
 *
 * @returns @link #rtrue rtrue@endlink if this object's monitor lock
 *          has been locked by this thread, otherwise
 *          @link #rfalse rfalse@endlink.
 *
 * @throws JVMCLASS_JAVA_LANG_NULLPOINTEREXCEPTION
 *         @link #JVMCLASS_JAVA_LANG_NULLPOINTEREXCEPTION
           if the object hash is the null object@endlink,
 *         typically due to bad input parameter.
 *
 */

rboolean objectutil_is_somehow_locked(jvm_object_hash objhashthis)
{
    ARCH_FUNCTION_NAME(objectutil_is_somwhow_locked);

    /* Requested object must be in use and not null */
    if ((OBJECT_STATUS_INUSE & OBJECT(objhashthis).status)     &&
       ((!(OBJECT_STATUS_NULL & OBJECT(objhashthis).status))))
    {
       /* If object has been locked */
       return((OBJECT_STATUS_MLOCK & OBJECT(objhashthis).status)
              ? rtrue
              : rfalse);
    }

    /* This thread cannot possibly own the object's monitor lock */
    thread_throw_exception(CURRENT_THREAD,
                           THREAD_STATUS_THREW_EXCEPTION,
                           JVMCLASS_JAVA_LANG_NULLPOINTEREXCEPTION);
/*NOTREACHED*/
    return(rfalse); /* Satisfy compiler */

} /* END of objectutil_is_somehow_locked() */


/*!
 * @brief Test if @c @b synchronize() has successfully locked
 * an object's monitor lock on a certain thread.
 *
 * Result does @e not indicate whether or not this thread is the
 * one which owns this object.  @link #objectutil_is_lock_owner
   objectutil_is_lock_owner@endlink must be called to determine
 * if this thread is the owner of this object's monitor lock.
 *
 *
 * @param  objhashthis  Object table hash of @c @b this object.
 *
 * @param  thridx       Thread table index of a thread requesting
 *                      ownership of this object's monitor lock.
 *
 *
 * @returns @link #rtrue rtrue@endlink if this object's monitor lock
 *          has been locked by this thread, otherwise
 *          @link #rfalse rfalse@endlink.
 *
 * @throws JVMCLASS_JAVA_LANG_NULLPOINTEREXCEPTION
 *         @link #JVMCLASS_JAVA_LANG_NULLPOINTEREXCEPTION
           if the object hash is the null object@endlink,
 *         typically due to bad input parameter.
 *
 */

rboolean objectutil_is_lock_owner(jvm_object_hash  objhashthis,
                                  jvm_thread_index thridx)
{
    ARCH_FUNCTION_NAME(objectutil_is_lock_owner);

    /* Requested object must be in use and not null */
    if ((OBJECT_STATUS_INUSE & OBJECT(objhashthis).status)     &&
       ((!(OBJECT_STATUS_NULL & OBJECT(objhashthis).status))))
    {
       /* If object has been locked */
       if (((OBJECT_STATUS_MLOCK & OBJECT(objhashthis).status)) &&

       /* and if I am the owner of the lock */
           (OBJECT(objhashthis).mlock_thridx == thridx))
       {
           /* Then synchronization status is known to be true */
           return(rtrue);
       }
       else
       {
           /* otherwise it is either not locked or I am not the owner */
           return(rfalse);
       }
    }

    /* This thread cannot possibly own the object's monitor lock */
    thread_throw_exception(CURRENT_THREAD,
                           THREAD_STATUS_THREW_EXCEPTION,
                           JVMCLASS_JAVA_LANG_NULLPOINTEREXCEPTION);
/*NOTREACHED*/
    return(rfalse); /* Satisfy compiler */

} /* END of objectutil_is_lock_owner() */


/*!
 * @brief Test which thread has locked an object's monitor lock.
 *
 * In order to query a particular thread, use
 * @link #objectutil_is_lock_owner objectutil_is_lock_owner@endlink
 * instead.
 *
 *
 * @param  objhashthis  Object table hash of @c @b this object.
 *
 *
 * @returns Thread table index of the thread which
 *          owns this object's monitor lock or 
 *          @link #rnull rnull@endlink if not locked.
 *
 * @throws JVMCLASS_JAVA_LANG_NULLPOINTEREXCEPTION
 *         @link #JVMCLASS_JAVA_LANG_NULLPOINTEREXCEPTION
           if the object hash is the null object@endlink,
 *         typically due to bad input parameter.
 *
 */

jvm_thread_index objectutil_locked_by_thread(jvm_object_hash
                                                            objhashthis)
{
    ARCH_FUNCTION_NAME(objectutil_locked_by_thread);

    /* Requested object must be in use and not null */
    if ((OBJECT_STATUS_INUSE & OBJECT(objhashthis).status)     &&
       ((!(OBJECT_STATUS_NULL & OBJECT(objhashthis).status))))
    {
       /* If this object has been locked */
       if ((OBJECT_STATUS_MLOCK & OBJECT(objhashthis).status))
       {
           /* then report which thread locked it */
           return(OBJECT(objhashthis).mlock_thridx);
       }
       else
       {
           /* Thread is  not locked */
           return(jvm_thread_index_null);
       }
    }

    /* This thread cannot possibly own the object's monitor lock */
    thread_throw_exception(CURRENT_THREAD,
                           THREAD_STATUS_THREW_EXCEPTION,
                           JVMCLASS_JAVA_LANG_NULLPOINTEREXCEPTION);
/*NOTREACHED*/
    return(rfalse); /* Satisfy compiler */

} /* END of objectutil_locked_by_thread() */


/*!
 * @brief Release @c @b synchronize from an object monitor lock.
 *
 *
 * @param  objhashthis  Object table hash of @c @b this object.
 *
 * @param  thridx       Thread table index of a thread requesting
 *                      to remove this object's monitor lock.
 *
 *
 * @attention Notice that the @link #rthread.locktarget
              locktarget@endlink is @b NOT cleared here!
 *            This is so that the value may be retained
 *            until the thread successfully arbitrates
 *            for ownership again in threadstate_process_lock().
 *            Once the MLOCK is reacquired there, the
 *            @link #rthread.locktarget locktarget@endlink will
 *            have been cleared by objectutil_synchronize().
 *            The purpose of objectutil_release() is to provide
 *            an avenue in the @b RUNNING state to release an MLOCK
 *            and set up @link #rthread.locktarget locktarget@endlink
 *            for the @b WAIT, @b NOTIFY, @b LOCK, @b ACQUIRE process.
 *
 * @returns @link #rvoid rvoid@endlink
 *
 *
 * @todo HARMONY-6-jvm-objectutil.c-3 Make sure thread interruption
 *       logic below here is working.
 *
 *
 * @throws JVMCLASS_JAVA_LANG_ILLEGALMONITORSTATEEXCEPTION
 *         @link #JVMCLASS_JAVA_LANG_ILLEGALMONITORSTATEEXCEPTION
       if current thread does not own the object's monitor lock@endlink.
 *
 */
rvoid objectutil_unsynchronize(jvm_object_hash  objhashthis,
                               jvm_thread_index thridx)
{
    ARCH_FUNCTION_NAME(objectutil_unsynchronize);

    /* Requested object must be in use and not null */
    if ((!(OBJECT_STATUS_INUSE & OBJECT(objhashthis).status))     ||
        (OBJECT_STATUS_NULL & OBJECT(objhashthis).status))
    {
        /*!
         * @todo HARMONY-6-jvm-objectutil.c-4 Should the not INUSE
         *       condition cause INTERNALERROR?
         */

        thread_throw_exception(CURRENT_THREAD,
                               THREAD_STATUS_THREW_EXCEPTION,
                               JVMCLASS_JAVA_LANG_NULLPOINTEREXCEPTION);
/*NOTREACHED*/
    }

    /* This thread must own the monitor lock in order to unlock it */
    if ((OBJECT_STATUS_MLOCK & OBJECT(objhashthis).status)    &&
        (thridx == (OBJECT(objhashthis).mlock_thridx)))
    {
        /* Decrement lock count and release MLOCK if done */
        OBJECT(objhashthis).mlock_count--;

        if (0 == OBJECT(objhashthis).mlock_count)
        {
            OBJECT(objhashthis).status       &= ~OBJECT_STATUS_MLOCK;

            OBJECT(objhashthis).mlock_thridx  = jvm_thread_index_null;
        }
        return;
    }

    /* This thread does not own the object's monitor lock */
    thread_throw_exception(CURRENT_THREAD,
                           THREAD_STATUS_THREW_EXCEPTION,
                       JVMCLASS_JAVA_LANG_ILLEGALMONITORSTATEEXCEPTION);
/*NOTREACHED*/
    return; /* Satisfy compiler */

} /* END of objectutil_unsynchronize() */


/*!
 * @brief Attempt to @c @b wait() on an object's
 * monitor lock by releasing it and requesting @b RELEASE state.
 *
 * Save the MLOCK to be released for subsequent
 * processing by threadstate_request_release().
 *
 *
 * @param  objhashthis  Object table hash of @c @b this object.
 *
 * @param  thridx       Thread table index of a thread requesting
 *                      to release this object's monitor lock.
 *
 *
 * @returns the result of threadstate_request_release()
 *
 */
rboolean objectutil_release(jvm_object_hash  objhashthis,
                           jvm_thread_index thridx)
{
    ARCH_FUNCTION_NAME(objectutil_release);

    /* Preserve MLOCK object's hash for transition to next states */
    THREAD(thridx).locktarget = objhashthis;

    return(threadstate_request_release(thridx));

} /* END of objectutil_release() */


/* EOF */
