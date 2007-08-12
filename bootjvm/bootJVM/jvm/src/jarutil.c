/*!
 * @file jarutil.c
 *
 * @brief Java archive utilities.
 *
 * The Java archiver creates, maintains, and extracts Java archive
 * files.  This file format is a Java support utility for storing
 * Java class files, properties files, and any arbitrary data used
 * by Java programs.
 *
 * This file is used both by the JVM java archive extraction and
 * by the JAR utility.  The contents are organized as an API library
 * with function definitions and return codes.
 *
 * This code was written with guidance from the 'contrib/minizip'
 * sample application of ZLIB version 1.2.3.  For further information,
 * please see www.info-zip.org .
 *
 *
 * @section Control
 *
 * \$URL: 
 *
 * \$Id: 
 *
 * Copyright 2006 The Apache Software Foundation
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
 * @version $
 *
 * @date $
 *
 * @author $
 *
 * @section Reference
 *
 */

#include "arch.h"
ARCH_SOURCE_COPYRIGHT_APACHE(jarutil, c,
"$URL$",
"$Id$");


#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <fcntl.h>
#warning Make sure "zlib.h" indicates correct compression library.
#include "zlib.h"

#include "jvmcfg.h"
#include "jar.h"
#include "util.h"

/*!
 * @name Heap management functions to pass to ZLIB compression library.
 *
 * ZLIB requires a heap allocator and deallocator function for its
 * normal operation.  If none is provided, it will choose a default,
 * presumably a straight @b malloc(3) and @b free(3) function.  In order
 * to avoid this default, the current @link #jar_state jar_state@endlink
 * structure pointer is passed in, which names the proper functions
 * to use for memory management.  When ZLIB calls are made,
 * these values are used to redirect the fixed name function
 * calls into the proper heap management functions as used in the rest
 * of the archiving code.
 *
 * The reason that the usual function
 * @link #jar_state.heap_get_data jar_state.heap_get_data@endlink
 * cannot be used dirctly is that the ZLIB function prototypes are
 * somewhat different than the standard @b malloc(3) and @b free(3)
 * library functions in that the allocator takes two parameters for
 * the amount of memory to be reserved.  Beyond this prototype
 * difference, they both function as expected.
 *
 * The definitions here use the ZLIB prototypes and map them into
 * the heap management style used here.
 *
 */

/*@{ */ /* Begin grouped definitions */

/*!
 * @brief Fixed-name heap allocator function for ZLIB calls.
 *
 * This function name is passed to ZLIB for use in its internal
 * memory management when it needs heap allocation.  Its name is
 * fixed so it may be used regardless of the heap allocation approach
 * configured into this code.
 *
 * If no function has been chosen, then the @b simple heap management
 * rules will be invoked.
 *
 * @param opaque  Custom memory management hook provided by ZLIB.
 *                Not used in this implementation.
 *
 * @param items   Number of items of length @b size to reserve
 *
 * @param size    Number of bytes to reserve for each item
 *
 *
 * @returns Untyped pointer to allocated heap area.
 *
 *
 * @see jarutil_zlib_free()
 *
 * @warning This function @e must be used in conjunction with
 *          jarutil_zlib_free() for correct operation of
 *          this mechanism.  Results are undefined otherwise
 *
 */

static /* alloc_func */ voidpf jarutil_zlib_malloc(voidpf opaque,
                                                   uInt items,
                                                   uInt size)
{
    ARCH_FUNCTION_NAME(jarutil_zlib_malloc);

    jar_state *pjs = (jar_state *) opaque;

    rvoid *rc = (pjs->heap_get_data)((rint)(items*size), rfalse);

    /* If no memory available, complain */
    if (rc != (void *) rnull)
    {
        return((voidpf) rc);
    }
/*NOTREACHED*/

    /*
     * In truth, this will never occur with this definition
     * of heap allocation-- an exception will instead
     * kill the program.
     */
    sysErrMsg(arch_function_name, "ZLIB alloc_func out of memory");
    return((voidpf) Z_NULL);

} /* END of jarutil_zlib_malloc() */


/*!
 * @brief Fixed-name heap deallocator function for ZLIB calls.
 *
 * This function name is passed to ZLIB for use in its internal
 * memory management when it needs heap deallocation.  Its name is
 * fixed so it may be used regardless of the heap allocation approach
 * configured into this code.
 *
 * If no function has been chosen, then the @b simple heap management
 * rules will be invoked.
 *
 * @param opaque  Custom memory management hook provided by ZLIB.
 *                Not used in this implementation.
 *
 * @param address Untyped pointer to allocated heap area as returned
 *                previously by jarutil_zlib_malloc().
 *
 * @returns nothing
 *
 *
 * @see jarutil_zlib_malloc()
 *
 * @warning This function @e must be used in conjunction with
 *          jarutil_zlib_malloc() for correct operation of
 *          this mechanism.  Results are undefined otherwise
 *
 */

static /* free_func */ rvoid jarutil_zlib_free(voidpf opaque,
                                               voidpf address)
{
    ARCH_FUNCTION_NAME(jarutil_zlib_free);

    jar_state *pjs = (jar_state *) opaque;

    (pjs->heap_free_data)((rvoid *) address);
    return;

} /* END of jarutil_zlib_free() */


/*@} */ /* End of grouped definitions */


/*!
 * @brief Convert ZLIB return code to text string
 *
 * @param zlib_code  Integer code from <zlib.h> definitions
 *
 *
 * @returns text string representation of @b zlib_code in the
 * context of @b inflateInit() and @b inflate().  (There are
 * other interpretations of these codes for other ZLIB functions.)
 *
 */
static rchar *jarutil_zlib_code(rint zlib_code)
{
    switch(zlib_code)
    {
        /* Non-negative conditions */
        case Z_OK:           return("not finished");
        case Z_NEED_DICT:    return("not finished");
        case Z_STREAM_END:   return("valid data");

        /* Negative errors */
        case Z_ERRNO:        return("system call error");
        case Z_STREAM_ERROR: return("bad parm block");
        case Z_DATA_ERROR:   return("corrupt input data");
        case Z_MEM_ERROR:    return("out of memory");
        case Z_BUF_ERROR:    return("buffer full");
        default:             return("unknown ZLIB code");
    }
/*NOTREACHED*/
} /* END of jarutil_zlib_code() */


/*!
 * @brief Search for location of global trailer in a Java archive file.
 *
 *
 * @param[in,out] pjs                State of open Java archive file.
 *                                   At entrance, the @b fd member
 *                                   contains the file descriptor of
 *                                   a JAR file that has been opened
 *                                   and is ready to examine.
 *                                   At exit, several members are
 *                                   loaded with information about
 *                                   the contents of the file.
 *                                   Of particular interest is the
 *                                   @link jar_global_trailer
                                     trailer@endlink structure, which
 *                                   contains the global trailer
 *                                   information from the JAR file,
 *                                   also its @link jar_global_trailer
                                     trailer_position@endlink
 *                                   member, which contains the offset
 *                                   in that file of the beginning of
 *                                   the global trailer.
 *
 * @returns jar_api_enum status of file system activity,
 *          particularly @b JAR_OKAY when successful.
 *          Other values indicate errors.
 *
 */

jar_api_enum jarutil_locate_global_trailer(jar_state *pjs)
{
    ARCH_FUNCTION_NAME(jarutil_locate_global_trailer);

    ruint max_global_seek = JAR_MAX_TRAILER_SIZE;

    ruint read_back_size  = JAR_NUM_TRAILER_SIGNATURE_BYTES;


    /* Seek to EOF for reading back to directory, report any errors */
    pjs->archive_length = portable_lseek(pjs->fd, 0, SEEK_END);

    if (JAR_MEMBER_IMPOSSIBLE_LSEEK == (rint) pjs->archive_length)
    {
        sysDbgMsg(DML2, pjs->jar_filename, JAR_MSG_END_SEEK);
        pjs->jar_code = JAR_LSEEK;
        pjs->jar_msg  = JAR_MSG_END_SEEK;
        return(pjs->jar_code);
    }

    /* If file is shorter than max trailer, adjust maximum trailer */
    if (max_global_seek > pjs->archive_length)
    {
        max_global_seek = pjs->archive_length;
    }

    /*
     * All failure in heap allocation is fatal and is handled 
     * in a non-local manner, so no error processing is needed.
     */
    pjs->bfr = (pjs->heap_get_data)(JAR_READ_BLOCK_SIZE +
                                      JAR_NUM_TRAILER_SIGNATURE_BYTES,
                                    rfalse);

    /* Most important return value from valid read */
    pjs->trailer_position = JAR_MEMBER_IMPOSSIBLE_LSEEK;

    while (read_back_size < max_global_seek)
    {
        rint read_position;
        rint read_size;
        rint idx;

        if (read_back_size + JAR_READ_BLOCK_SIZE > max_global_seek)
        {
            read_back_size = max_global_seek;
        }
        else
        {
            read_back_size += JAR_READ_BLOCK_SIZE;
        }

        read_position = pjs->archive_length - read_back_size;

        if ((JAR_READ_BLOCK_SIZE + JAR_NUM_TRAILER_SIGNATURE_BYTES) <
            (pjs->archive_length - read_position))
        {
            read_size = JAR_READ_BLOCK_SIZE +
                          JAR_NUM_TRAILER_SIGNATURE_BYTES;
        }
        else
        {
            read_size = pjs->archive_length - read_position;
        }

        if (read_position !=
            portable_lseek(pjs->fd, read_position, SEEK_SET))
        {
            sysDbgMsg(DML2, pjs->jar_filename, JAR_MSG_TRAILER_SEEK);

            /* Clean up heap after error */
            (pjs->heap_free_data)(pjs->bfr);
            pjs->bfr = (rchar *) rnull;

            /* Report invalid seek */
            pjs->jar_code = JAR_LSEEK;
            pjs->jar_msg  = JAR_MSG_TRAILER_SEEK;
            return(pjs->jar_code);
        }

        if (read_size != portable_read(pjs->fd, pjs->bfr, read_size))
        {
            sysDbgMsg(DML2, pjs->jar_filename, JAR_MSG_TRAILER_READ);

            /* Clean up heap after error */
            (pjs->heap_free_data)(pjs->bfr);
            pjs->bfr = (rchar *) rnull;


            /* Report invalid read */
            pjs->jar_code = JAR_READ;
            pjs->jar_msg  = JAR_MSG_TRAILER_READ;
            return(pjs->jar_code);
        }

        for (idx = read_size - JAR_NUM_TRAILER_SIGNATURE_BYTES;
             idx >= 0;
             idx--)
        {
            if ((JAR_TRAILER_SIGNATURE_BYTE1 == pjs->bfr[idx + 0]) &&
                (JAR_TRAILER_SIGNATURE_BYTE2 == pjs->bfr[idx + 1]) &&
                (JAR_TRAILER_SIGNATURE_BYTE3 == pjs->bfr[idx + 2]) &&
                (JAR_TRAILER_SIGNATURE_BYTE4 == pjs->bfr[idx + 3]))
            {
                /* Error if signature found but not rest of trailer */
                if (read_size <
                    (idx + JAR_NUM_TRAILER_SIGNATURE_BYTES
                         + sizeof(rushort) /*volume number */
                         + sizeof(rushort) /*vol w/ start of directory*/
                         + sizeof(rushort) /*dir entries in _this_ vol*/
                         + sizeof(rushort) /*total directory entries */
                         + sizeof(ruint)   /*size of directory */
                         + sizeof(ruint)   /*start of directory (offset)
                                               on starting disk */
                         + sizeof(rushort) /*size of global trailer */))
                {
                    /* Clean up heap before reporting error */
                    (pjs->heap_free_data)(pjs->bfr);
                    pjs->bfr = (rchar *) rnull;

                    sysDbgMsg(DML2,
                              pjs->jar_filename,
                              JAR_MSG_TRAILER_TRUNCATED);

                    /* Report wrong position in global trailer */
                    pjs->jar_code = JAR_BAD_TRAILER;
                    pjs->jar_msg  = JAR_MSG_TRAILER_TRUNCATED;
                    return(pjs->jar_code);
                }

                /* Record global trailer information for return */
                pjs->trailer_position = read_position + idx;

                /* Skip recording signature itself, its use is over */
                pjs->trailer.volume_number =
                   bytegames_getrs2_le((rushort *) &pjs->bfr[idx + 4]);
                pjs->trailer.volume_with_directory_start =
                   bytegames_getrs2_le((rushort *) &pjs->bfr[idx + 6]);
                pjs->trailer.this_volume_directory_entries =
                   bytegames_getrs2_le((rushort *) &pjs->bfr[idx + 8]);
                pjs->trailer.total_directory_entries =
                   bytegames_getrs2_le((rushort *) &pjs->bfr[idx + 10]);
                pjs->trailer.total_directory_size =
                   bytegames_getri4_le((ruint *) &pjs->bfr[idx + 12]);
                pjs->trailer.directory_position =
                   bytegames_getri4_le((ruint *) &pjs->bfr[idx + 16]);
                pjs->trailer.trailer_size =
                   bytegames_getrs2_le((rushort *) &pjs->bfr[idx + 20]);

                /* Clean up heap before finishing */
                (pjs->heap_free_data)(pjs->bfr);
                pjs->bfr = (rchar *) rnull;

                /* check for value mismatches and unsupported values */
                if (
                    (pjs->trailer.volume_number != 0)               ||
                    (pjs->trailer.volume_with_directory_start != 0) ||
                    (pjs->trailer.total_directory_entries != 
                     pjs->trailer.this_volume_directory_entries)    ||

                    /* Also _must_ have directory _before_ trailer */
                    (pjs->trailer_position < 
                     (pjs->trailer.directory_position +
                      pjs->trailer.total_directory_size)))
                {
                    sysDbgMsg(DML2,
                              pjs->jar_filename,
                              JAR_MSG_TRAILER_CONFLICT);

                    pjs->jar_code = JAR_BAD_TRAILER;
                    pjs->jar_msg  = JAR_MSG_TRAILER_CONFLICT;
                    return(pjs->jar_code);
                }

                /* Report that a valid global trailer has been read */
                pjs->jar_code = JAR_OKAY;
                pjs->jar_msg  = (rchar *) rnull;
                return(pjs->jar_code);
            }
        }
    }

    /* Clean up heap and report member not found */
    (pjs->heap_free_data)(pjs->bfr);
    pjs->bfr = (rchar *) rnull;

    sysDbgMsg(DML2, pjs->jar_filename, JAR_MSG_TRAILER_MISSING);

    pjs->jar_code = JAR_TRAILER_NOT_FOUND;
    pjs->jar_msg  = JAR_MSG_TRAILER_MISSING;
    return(pjs->jar_code);

} /* END of jarutil_locate_global_trailer() */


/*!
 * @brief Convert DOS date to generic data.
 *
 * Convert MS-DOS date format from directory entry into form
 * usable by any code.  This is reminiscent of
 * <b><code>(struct tm)</code></b> in the header file @e <time.h>
 * in the naming of the members.
 *
 *
 * @param[in]  dos_date  MS-DOS format date stamp.
 *
 * @param[out] ptm_zip   Pointer to generic format date structure.
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */

rvoid jarutil_dos_date_to_generic(ruint dos_date, tm_zip *ptm_zip)
{
    ARCH_FUNCTION_NAME(jarutil_dos_date_to_generic);

    ruint date_half = (dos_date >> 16) & 0xffff;
    ruint time_half = (dos_date      ) & 0xffff;

    ptm_zip->tm_mday = (ruint)  ((date_half & 0x001f));
    ptm_zip->tm_mon  = (ruint) (((date_half & 0x01e0) >>  5) - 1);
    ptm_zip->tm_year = (ruint) (((date_half & 0xfe00) >>  9) + 1980);

    ptm_zip->tm_hour = (ruint)  ((time_half & 0xf800) >> 11);
    ptm_zip->tm_min  = (ruint)  ((time_half & 0x07e0) >>  5);
    ptm_zip->tm_sec  = (ruint) (((time_half & 0x001f)      ) * 2);

} /* END of jarutil_dos_date_to_generic() */


/*!
 * @brief Search for the directory of a Java archive file.
 *
 * This means searching for the global trailer, then reading it,
 * which includes reading the position of the directory, among
 * other fields.
 *
 * @param[in,out] pjs  State of open Java archive file.  Of particular
 *                     interest is the @link #jar_global_trailer
                       trailer@endlink structure, which contains
 *                     the global trailer information from the JAR
 *                     file, also its @link #jar_global_trailer
                       trailer_position@endlink member, which contains
 *                     the offset in that file of the beginning of
 *                     the global trailer.  Likewise, its
 *                  @link #jar_global_trailer directory_position@endlink
 *                     member contains the offset in that file of the
 *                     beginning of the directory.
 *
 *
 * @returns jar_api_enum status of file system activity,
 *          particularly @b JAR_OKAY when successful.
 *          Other values indicate errors.
 *
 */

jar_api_enum jarutil_locate_zip_directory(jar_state *pjs)
{
    ARCH_FUNCTION_NAME(jarutil_locate_zip_directory);

    jar_api_enum rc;


    /*
     * Search for global trailer at end of Java archive.
     * From here, all member files may be located.
     */
    rc = jarutil_locate_global_trailer(pjs);
    if (JAR_OKAY != rc)
    {
        return(rc);
    }

    /*
     * Calculate gap between start of global trailer (at EOF) and
     * end of directory, which should immediately precede it.
     */
    pjs->trailer_directory_gap =
        pjs->trailer_position -
        pjs->trailer.directory_position -
        pjs->trailer.total_directory_size;

    pjs->jar_code = JAR_OKAY;
    pjs->jar_msg  = (rchar *) rnull;
    return(JAR_OKAY);

} /* END of jarutil_locate_zip_directory() */


/*!
 * @brief Search for next member file in a Java archive file.
 *
 *
 * If run immediately after jarutil_locate_zip_directory() and
 * seeking to the start of the directory via
 *
 * <b><code>portable_lseek(pjs->fd,
                           pjs->trailer.directory_position,
                           SEEK_SET)</code></b>
 *
 * then the directory entry of the first member file will be
 * retrieved.  After this first time, the second and following
 * members will be retrieved.
 *
 *
 * @param[in,out] pjs    State of open Java archive file.  Of
 *                       particular interest is the
 *               @link #jar_directory_entry pjs->directory_entry@endlink
 *                       member, which contains the directory entry of
 *                       that member file in the archive.  A compressed
 *                       read will extract it from the archive.
 *
 * @param[in] direntidx  Number of entries from beginning of directory.
 *                       The first entry is at index zero.  This is
 *                       used only for reporting in error messages.
 *
 *
 * @returns jar_api_enum status of file system activity,
 *          particularly @b JAR_OKAY when successful.
 *          Other values indicate errors.
 *
 */

jar_api_enum jarutil_read_next_directory_entry(jar_state *pjs,
                                               rushort    direntidx)
{
    ARCH_FUNCTION_NAME(jarutil_read_next_directory_entry);

    ruint lseek_next_entry;

    /*
     * Read the directory entry up to before the file name.
     * Use the file name area to read, then byte-swap into
     * the individual fields.  This can be done for two
     * reasons:  (1) the data format is fixed and well established,
     * and (2) the file name field is much large than the requested
     * data.  Therefore, no temporary buffer storage is needed.
     */
#define DIRECTORY_ENTRY_READ_SIZE                               \
           (JAR_NUM_DIRECTORY_SIGNATURE_BYTES +                 \
            sizeof(pjs->directory_entry.version) +              \
            sizeof(pjs->directory_entry.version_needed) +       \
            sizeof(pjs->directory_entry.flags) +                \
            sizeof(pjs->directory_entry.compression_method) +   \
            sizeof(pjs->directory_entry.dos_date) +             \
            sizeof(pjs->directory_entry.crc32) +                \
            sizeof(pjs->directory_entry.compressed_size) +      \
            sizeof(pjs->directory_entry.uncompressed_size) +    \
            sizeof(pjs->directory_entry.size_member_filename) + \
            sizeof(pjs->directory_entry.size_member_extra) +    \
            sizeof(pjs->directory_entry.size_member_comment) +  \
            sizeof(pjs->directory_entry.disk_num_start) +       \
            sizeof(pjs->directory_entry.internal_fa) +          \
            sizeof(pjs->directory_entry.external_fa) +          \
            sizeof(pjs->directory_entry.member_offset))

#define TRANSCRIBE_DIRENT_FIELD(name, type, lefn, idx)                 \
    pjs->directory_entry.name =                                        \
        bytegames_getr##lefn##_le((type *)                             \
                             &pjs->directory_entry.member_filename[idx])

    /*
     * Read next directory entry.
     */

    if (DIRECTORY_ENTRY_READ_SIZE !=
        portable_read(pjs->fd,
                      pjs->directory_entry.member_filename,
                      DIRECTORY_ENTRY_READ_SIZE))
    {
        sysDbgMsg(DML2, pjs->jar_filename, JAR_MSG_DIRENT_READ);

        pjs->jar_code = JAR_READ;
        pjs->jar_msg  = JAR_MSG_DIRENT_READ;
        return(pjs->jar_code);
    }

    /* Transcribe and validate directory signature */
    pjs->directory_entry.magic[0] =
                                pjs->directory_entry.member_filename[0];
    pjs->directory_entry.magic[1] =
                                pjs->directory_entry.member_filename[1];
    pjs->directory_entry.magic[2] =
                                pjs->directory_entry.member_filename[2];
    pjs->directory_entry.magic[3] =
                                pjs->directory_entry.member_filename[3];

    if ((JAR_DIRECTORY_SIGNATURE_BYTE1 !=
                                      pjs->directory_entry.magic[0] ) ||
        (JAR_DIRECTORY_SIGNATURE_BYTE2 !=
                                      pjs->directory_entry.magic[1] ) ||
        (JAR_DIRECTORY_SIGNATURE_BYTE3 !=
                                      pjs->directory_entry.magic[2] ) ||
        (JAR_DIRECTORY_SIGNATURE_BYTE4 !=
                                      pjs->directory_entry.magic[3] ))
    {
        sysDbgMsg(DML2,
                  pjs->jar_filename,
                  JAR_MSG_DIRENT_CORRUPT ": index '%d'",
                  direntidx);

        pjs->jar_code = JAR_BAD_DIRECTORY_ENTRY;
        pjs->jar_msg  = JAR_MSG_DIRENT_CORRUPT;
        return(pjs->jar_code);
    }

    /* Transcribe other fields */
    TRANSCRIBE_DIRENT_FIELD(version,              rushort, s2, 4);
    TRANSCRIBE_DIRENT_FIELD(version_needed,       rushort, s2, 6);
    TRANSCRIBE_DIRENT_FIELD(flags,                rushort, s2, 8);
    TRANSCRIBE_DIRENT_FIELD(compression_method,   rushort, s2, 10);
    TRANSCRIBE_DIRENT_FIELD(dos_date,             ruint,   i4, 12);
    TRANSCRIBE_DIRENT_FIELD(crc32,                ruint,   i4, 16);
    TRANSCRIBE_DIRENT_FIELD(compressed_size,      ruint,   i4, 20);
    TRANSCRIBE_DIRENT_FIELD(uncompressed_size,    ruint,   i4, 24);
    TRANSCRIBE_DIRENT_FIELD(size_member_filename, rushort, s2, 28);
    TRANSCRIBE_DIRENT_FIELD(size_member_extra,    rushort, s2, 30);
    TRANSCRIBE_DIRENT_FIELD(size_member_comment,  rushort, s2, 32);
    TRANSCRIBE_DIRENT_FIELD(disk_num_start,       rushort, s2, 34);
    TRANSCRIBE_DIRENT_FIELD(internal_fa,          rushort, s2, 36);
    TRANSCRIBE_DIRENT_FIELD(external_fa,          ruint,   i4, 38);
    TRANSCRIBE_DIRENT_FIELD(member_offset,        ruint,   i4, 42);

    /* Convert date into generic format */
    jarutil_dos_date_to_generic(pjs->directory_entry.dos_date,
                                &pjs->directory_entry.generic_date);

    /*
     * Verify valid file name length (to avoid read() buffer overflow)
     */
    if (JAR_MAX_MEMBER_FILENAME_LENGTH <
        pjs->directory_entry.size_member_filename)
    {
        sysDbgMsg(DML2,
                  pjs->jar_filename,
                  JAR_MSG_DIRENT_FILENAME_SIZE ": index %d",
                  direntidx);

        pjs->jar_code = JAR_BAD_DIRECTORY_ENTRY;
        pjs->jar_msg  = JAR_MSG_DIRENT_FILENAME_SIZE;
        return(pjs->jar_code);
    }

    /*
     * Verify valid volume number (no multi-volume support)
     */
    if (0 != pjs->directory_entry.disk_num_start)
    {
        sysDbgMsg(DML2, pjs->jar_filename, JAR_MSG_DIRENT_MULTIVOLUME);

        pjs->jar_code = JAR_BAD_DIRECTORY_ENTRY;
        pjs->jar_msg  = JAR_MSG_DIRENT_MULTIVOLUME;
        return(pjs->jar_code);
    }

    /*
     * Read file name and skip 'file extra field' and 'file comment'
     * areas with lseek(2) beyond them to next directory entry.
     */
    if (pjs->directory_entry.size_member_filename !=
        portable_read(pjs->fd,
                      pjs->directory_entry.member_filename,
                      pjs->directory_entry.size_member_filename))
    {
       sysDbgMsg(DML2,pjs->jar_filename,JAR_MSG_DIRENT_READ_MEMBERNAME);

        pjs->jar_code = JAR_READ;
        pjs->jar_msg  = JAR_MSG_DIRENT_READ_MEMBERNAME;
        return(pjs->jar_code);
    }

    /*
     * Insert final NUL byte
     */
    pjs->directory_entry.member_filename
      [pjs->directory_entry.size_member_filename] = '\0';


    /*
     * Seek to next directory entry, report any errors
     */
    lseek_next_entry =
        portable_lseek(pjs->fd,
                       pjs->directory_entry.size_member_extra +
                           pjs->directory_entry.size_member_comment,
                       SEEK_CUR);

    if (JAR_MEMBER_IMPOSSIBLE_LSEEK == lseek_next_entry)
    {
        sysDbgMsg(DML2, pjs->jar_filename, JAR_MSG_DIRENT_SEEK);

        pjs->jar_code = JAR_LSEEK;
        pjs->jar_msg  = JAR_MSG_DIRENT_SEEK;
        return(pjs->jar_code);
    }

    /*
     * Fill in remaining fields with calculated values
     */
    pjs->directory_entry.extra_offset =
        lseek_next_entry -
        pjs->directory_entry.size_member_extra -
        pjs->directory_entry.size_member_comment;

    pjs->directory_entry.comment_offset =
        lseek_next_entry -
        pjs->directory_entry.size_member_comment;

    /*
     * Valid scan
     */
    return(JAR_OKAY);

} /* END of jarutil_read_next_directory_entry() */


/*!
 * @brief Search for a Java archive member file in a Java archive file.
 *
 *
 * @param[in,out] pjs                State of open Java archive file.
 *                                   Of particular interest for input is
 *                                   the @link jar_state#member_filename
                                     pjs->member_filename@endlink ,
 *                                   which states which member to look
 *                                   for in the archive.
 *                                   Of particular interest for output
 *                                   is the @link #jar_directory_entry
                                     pjs->directory_entry@endlink
 *                                   member, which contains the
 *                                   directory entry of that member
 *                                   file in the archive.
 *                                   A compressed read will extract
 *                                   it from the archive.
 *
 *
 * @returns jar_api_enum status of file system activity,
 *          particularly @b JAR_OKAY when successful.
 *          Other values indicate errors.
 *
 */

static jar_api_enum jarutil_locate_member(jar_state *pjs)
{
    ARCH_FUNCTION_NAME(jarutil_locate_member);

    jar_api_enum rc;

    rushort direntidx;

    /*
     * Search for global trailer at end of Java archive,
     * which contains location of directory.  With this
     * information, all member files may be located.
     */
    rc = jarutil_locate_zip_directory(pjs);
    if (JAR_OKAY != rc)
    {
        return(rc);
    }

    /* Seek to location of directory, report any errors */
    if (JAR_MEMBER_IMPOSSIBLE_LSEEK == 
        portable_lseek(pjs->fd,
                       pjs->trailer.directory_position,
                       SEEK_SET))
    {
        sysDbgMsg(DML2, pjs->jar_filename, JAR_MSG_DIRENT_SEEK);

        pjs->jar_code = JAR_LSEEK;
        pjs->jar_msg  = JAR_MSG_DIRENT_SEEK;
        return(pjs->jar_code);
    }

    /* Point directory entry to offset of first one in ZIP file */
    pjs->directory_entry.position = pjs->trailer.directory_position;

    /*
     * Read each directory entry until match is found.
     */
    for (direntidx = 0;
         direntidx < pjs->trailer.total_directory_entries;
         direntidx++)
    {
        /* First time through reads first directory entry */
        rc = jarutil_read_next_directory_entry(pjs, direntidx);
        if (JAR_OKAY != rc)
        {
            return(rc);
        }
        
        /*
         * Check if this slot contains the requested member file.
         * Return match if so.
         */
        if (0 == portable_strcmp(pjs->member_filename,
                                 pjs->directory_entry.member_filename))
        {
            pjs->jar_code = JAR_OKAY;
            pjs->jar_msg  = (rchar *) rnull;
            return(JAR_OKAY);
        }

    } /* for (dirent) */

    /*
     * Valid scan, but requested member not found in archive
     */
    sysDbgMsg(DML2,
              pjs->jar_filename,
              JAR_MSG_DIRENT_MEMBER_NOT_FOUND ": '%s'",
              pjs->member_filename);

    pjs->jar_code = JAR_MEMBER_NOT_FOUND;
    pjs->jar_msg  = JAR_MSG_DIRENT_MEMBER_NOT_FOUND;
    return(pjs->jar_code);

} /* END of jarutil_locate_member() */


/*!
 * @brief Read header information of a Java archive member file entry
 * and verify it against its directory entry.
 *
 * Seek to the starting point of a member file entry,
 * read its header information, and compare that to the equivalent
 * from the directory entry (presumably a legacy requirement).  If
 * valid, then the member data itself may be read.
 *
 * @param[in,out] pjs  State of open Java archive file.  Of particular
 *                     interest is the @link #jar_directory_entry
                       pjs->directory_entry@endlink member, which
 *                     contains the directory entry of that member
 *                     file in the archive.  The @link jar_state.bfr
 *                     jar_state.bfr@endlink field contains a pointer
 *                     to a buffer to use to store the data when it is
 *                     read from the member file.  The
 *                     @link jar_directory_entry.uncompressed_size
                       jar_directory_entry.uncompressed_size@endlink
 *                     field tells how many bytes to read.  The
 *                     @link jar_directory_entry.member_offset
                       jar_directory_entry.member_offset@endlink field
 *                     tells where the member starts in the archive.
 *
 *
 * @returns jar_api_enum status of file system activity,
 *          particularly @b JAR_OKAY when successful.
 *          Other values indicate errors.  If successful,
 *          the @link jar_state.fd jar_state.fd@endlink file will
 *          be in the correct place to read the data area, whether
 *          it be compressed data or uncompressed.
 *
 */

static jar_api_enum jarutil_read_and_verify_member_header(jar_state
                                                                   *pjs)
{
    ARCH_FUNCTION_NAME(jarutil_read_and_verify_member_header);

    /*
     * Seek to beginning of member, read its header,
     * and verify that the header is valid.  If so,
     * seek to the beginning of the uncompressed data
     * area for forthcoming read. 
     */

    if (pjs->directory_entry.member_offset !=
        portable_lseek(pjs->fd,
                       pjs->directory_entry.member_offset,
                       SEEK_SET))
    {
        sysDbgMsg(DML2,
                  pjs->jar_filename,
                  JAR_MSG_MEMBER_SEEK_HEADER ": '%s'",
                  pjs->directory_entry.member_filename);

        pjs->jar_code = JAR_LSEEK;
        pjs->jar_msg  = JAR_MSG_MEMBER_SEEK_HEADER;
        return(pjs->jar_code);
    }

    /*
     * Read the member entry up to before the file name.
     * Use the file name area to read, then byte-swap into
     * the individual fields.  This can be done for two
     * reasons:  (1) the data format is fixed and well established,
     * and (2) the file name field is much large than the requested
     * data.  Therefore, no temporary buffer storage is needed.
     */
#define MEMBER_ENTRY_READ_SIZE                               \
           (JAR_NUM_MEMBER_SIGNATURE_BYTES +                 \
            sizeof(pjs->member_entry.version) +              \
            sizeof(pjs->member_entry.flags) +                \
            sizeof(pjs->member_entry.compression_method) +   \
            sizeof(pjs->member_entry.dos_date) +             \
            sizeof(pjs->member_entry.crc32) +                \
            sizeof(pjs->member_entry.compressed_size) +      \
            sizeof(pjs->member_entry.uncompressed_size) +    \
            sizeof(pjs->member_entry.size_member_filename) + \
            sizeof(pjs->member_entry.size_member_extra))

#define TRANSCRIBE_MEMENT_FIELD(name, type, lefn, idx)               \
    pjs->member_entry.name =                                         \
        bytegames_getr##lefn##_le((type *)                           \
                             &pjs->member_entry.member_filename[idx])

    /*
     * Read this member entry header.
     */

    if (MEMBER_ENTRY_READ_SIZE !=
        portable_read(pjs->fd,
                      pjs->member_entry.member_filename,
                      MEMBER_ENTRY_READ_SIZE))
    {
        sysDbgMsg(DML2,
                  pjs->jar_filename,
                  JAR_MSG_MEMBER_READ_HEADER ": '%s'",
                  pjs->directory_entry.member_filename);

        pjs->jar_code = JAR_READ;
        pjs->jar_msg = JAR_MSG_MEMBER_READ_HEADER;
        return(pjs->jar_code);
    }

    /* Transcribe and validate member signature */
    pjs->member_entry.magic[0] = pjs->member_entry.member_filename[0];
    pjs->member_entry.magic[1] = pjs->member_entry.member_filename[1];
    pjs->member_entry.magic[2] = pjs->member_entry.member_filename[2];
    pjs->member_entry.magic[3] = pjs->member_entry.member_filename[3];

    if ((JAR_MEMBER_SIGNATURE_BYTE1 != pjs->member_entry.magic[0] ) ||
        (JAR_MEMBER_SIGNATURE_BYTE2 != pjs->member_entry.magic[1] ) ||
        (JAR_MEMBER_SIGNATURE_BYTE3 != pjs->member_entry.magic[2] ) ||
        (JAR_MEMBER_SIGNATURE_BYTE4 != pjs->member_entry.magic[3] ))
    {
        sysDbgMsg(DML2,
                  pjs->jar_filename,
                  JAR_MSG_MEMBER_SEEK_HEADER ": '%s'",
                  pjs->directory_entry.member_filename);

        pjs->jar_code = JAR_BAD_MEMBER_ENTRY;
        pjs->jar_msg = JAR_MSG_MEMBER_SEEK_HEADER;
        return(pjs->jar_code);
    }

    /* Transcribe other fields */
    TRANSCRIBE_MEMENT_FIELD(version,              rushort, s2, 4);
    TRANSCRIBE_MEMENT_FIELD(flags,                rushort, s2, 6);
    TRANSCRIBE_MEMENT_FIELD(compression_method,   rushort, s2, 8);
    TRANSCRIBE_MEMENT_FIELD(dos_date,             ruint,   i4, 10);
    TRANSCRIBE_MEMENT_FIELD(crc32,                ruint,   i4, 14);
    TRANSCRIBE_MEMENT_FIELD(compressed_size,      ruint,   i4, 18);
    TRANSCRIBE_MEMENT_FIELD(uncompressed_size,    ruint,   i4, 22);
    TRANSCRIBE_MEMENT_FIELD(size_member_filename, rushort, s2, 26);
    TRANSCRIBE_MEMENT_FIELD(size_member_extra,    rushort, s2, 28);

    /*
     * Verify valid file name length (to avoid read() buffer overflow)
     */
    if (JAR_MAX_MEMBER_FILENAME_LENGTH <
        pjs->member_entry.size_member_filename)
    {
        sysDbgMsg(DML2,
                  pjs->jar_filename,
                  JAR_MSG_MEMBER_FILENAME_SIZE ": '%s'",
                  pjs->directory_entry.member_filename);

        pjs->jar_code = JAR_BAD_MEMBER_ENTRY;
        pjs->jar_msg  = JAR_MSG_MEMBER_FILENAME_SIZE;
        return(pjs->jar_code);
    }

    switch(pjs->member_entry.compression_method)
    {
        case JAR_COMPRESSION_METHOD_STORE:
        case JAR_COMPRESSION_METHOD_DEFLATE:
            if (pjs->directory_entry.compression_method ==
                pjs->member_entry.compression_method)
            {
                break;
            }
            /* ... fall through to error condition: */

        default:
            sysDbgMsg(DML2,
                      pjs->jar_filename,
                      JAR_MSG_COMPRESSION_METHOD ": method %d on '%s'",
                      pjs->member_entry.compression_method,
                      pjs->directory_entry.member_filename);

            pjs->jar_code = JAR_COMPRESSION_METHOD;
            pjs->jar_msg  = JAR_MSG_COMPRESSION_METHOD;
            return(pjs->jar_code);
    }

    if (!(MEMBER_ENTRY_FLAG_BIT3 & pjs->member_entry.flags))
    {
        if ((pjs->directory_entry.crc32 !=
             pjs->member_entry.crc32)                      ||

            (pjs->directory_entry.compressed_size !=
             pjs->member_entry.compressed_size)            ||

            (pjs->directory_entry.uncompressed_size !=
             pjs->member_entry.uncompressed_size))
        {
            sysDbgMsg(DML2,
                      pjs->jar_filename,
                      JAR_MSG_MEMBER_DIRENT_MISMATCH ": '%s'",
                      pjs->directory_entry.member_filename);

            pjs->jar_code = JAR_FIELD_MISMATCH;
            pjs->jar_msg  = JAR_MSG_MEMBER_DIRENT_MISMATCH;
            return(pjs->jar_code);
        }
    }

    if (pjs->directory_entry.size_member_filename !=
        pjs->member_entry.size_member_filename)
    {
        sysDbgMsg(DML2,
                  pjs->jar_filename,
                  JAR_MSG_MEMBER_DIRENT_MISMATCH ": '%s'",
                  pjs->directory_entry.member_filename);

        pjs->jar_code = JAR_FIELD_MISMATCH;
        pjs->jar_msg  = JAR_MSG_MEMBER_DIRENT_MISMATCH;
        return(pjs->jar_code);
    }

    /*
     * Do NOT need to verify valid file name length
     * since member version matches directory version
     * and directory version was valid.
     *
     * Therefore, go read file name and compare against directory.
     *
     * Then go read file extra field and compare against directory.
     */
    if (pjs->member_entry.size_member_filename !=
        portable_read(pjs->fd,
                      pjs->member_entry.member_filename,
                      pjs->member_entry.size_member_filename))
    {
        sysDbgMsg(DML2,
                  pjs->jar_filename,
                  JAR_MSG_MEMBER_READ_MEMBERNAME ": '%s'",
                  pjs->directory_entry.member_filename);

        pjs->jar_code = JAR_READ;
        pjs->jar_msg  = JAR_MSG_MEMBER_READ_MEMBERNAME;
        return(pjs->jar_code);
    }

    /*
     * Insert final NUL byte
     */
    pjs->member_entry.member_filename
      [pjs->member_entry.size_member_filename] = '\0';


    /*
     * Keep checking directory entry against member header...
     */
    if ((pjs->directory_entry.member_offset +
         MEMBER_ENTRY_READ_SIZE +
         pjs->member_entry.size_member_filename +
         pjs->member_entry.size_member_extra) !=
        portable_lseek(pjs->fd,
                       pjs->member_entry.size_member_extra,
                       SEEK_CUR))
    {
        /* Report invalid seek */
        sysDbgMsg(DML2,
                  pjs->jar_filename,
                  JAR_MSG_MEMBER_SEEK_DATA ": '%s'",
                  pjs->directory_entry.member_filename);

        pjs->jar_code = JAR_LSEEK;
        pjs->jar_msg  = JAR_MSG_MEMBER_SEEK_DATA;
        return(pjs->jar_code);
    }

    /*!
     * @todo HARMONY-6-jvm-jarutil.c-2 Make sure file name is the same
     * in the member field as in the directory.  Is this needed, or not?
     *
     */
    if (0 != portable_strcmp(pjs->directory_entry.member_filename,
                             pjs->member_entry.member_filename))
    {
        sysDbgMsg(DML2,
                  pjs->jar_filename,
                  JAR_MSG_MEMBER_DIRENT_MISMATCH ": '%s'",
                  pjs->directory_entry.member_filename);

        pjs->jar_code = JAR_FIELD_MISMATCH;
        pjs->jar_msg  = JAR_MSG_MEMBER_DIRENT_MISMATCH;
        return(pjs->jar_code);
    }

    /* Report success */
    pjs->jar_code  = JAR_OKAY;
    return(pjs->jar_code);

} /* END of jarutil_read_and_verify_member_header() */


/*!
 * @name Read an archive member file from a Java archive.
 *
 * Read a member file from an archive by seeking to its starting point,
 * reading its header information, compariing that to the equivalent
 * from the directory entry, and read the uncompressed data out of
 * the archive into an output buffer.
 *
 * @param[in,out] pjs    State of open Java archive file.  Of particular
 *                       interest is the @link #jar_directory_entry
                         pjs->directory_entry@endlink field, which
 *                       contains the directory entry of that member
 *                       file in the archive.  The @link #jar_state.bfr
 *                       jar_state.bfr@endlink field contains a pointer
 *                       to a buffer to use to store the data when it is
 *                       read from the member file.  The
 *                       @link #jar_directory_entry.uncompressed_size
                         jar_directory_entry.uncompressed_size@endlink
 *                       field tells how many bytes to read.  The
 *                       @link #jar_directory_entry.member_offset
                         jar_directory_entry.member_offset@endlink field
 *                       tells where the member starts in the archive.
 *
 * @param[in] member_bfr Meaningful only for compressed read.
 *                       Scratch buffer of size
 *                       @link #jar_directory_entry.compressed_size
                         jar_directory_entry.compressed_size@endlink
 *                       to hold intermediate compressed data.
 *
 * @returns jar_api_enum status of file system activity,
 *          particularly @b JAR_OKAY when successful.
 *          Other values indicate errors.  When successful,
 *          the buffer pointer will be non-null and will
 *          contain the requested data.  The uncompressed
 *          size will be the length of that buffer in bytes.
 *          At this time, the file pointer will also point
 *          to the first byte following the data that was read.
 *
 */

/*@{ */ /* Begin grouped definitions */

/*!
 * @brief Read an @e uncompressed archive member file.
 *
 */
static jar_api_enum jarutil_read_uncompressed_member(jar_state *pjs)
{
    ARCH_FUNCTION_NAME(jarutil_read_uncompressed_member);

    jar_api_enum rc;


    /* Read member header data.  If valid, read member data itself */
    rc = jarutil_read_and_verify_member_header(pjs);

    if (JAR_OKAY != rc)
    {
        return(rc);
    }

    if (pjs->directory_entry.uncompressed_size !=
        portable_read(pjs->fd,
                      pjs->bfr,
                      pjs->directory_entry.uncompressed_size))
    {
        /* Report invalid read */
        sysDbgMsg(DML2,
                  pjs->jar_filename,
                  JAR_MSG_MEMBER_READ_DATA ": '%s'",
                  pjs->directory_entry.member_filename);

        pjs->jar_code = JAR_READ;
        pjs->jar_msg  = JAR_MSG_MEMBER_READ_DATA;
        return(pjs->jar_code);
    }

    ruint crc = crc32(0,
                      pjs->bfr,
                      pjs->directory_entry.uncompressed_size);

    if (crc != pjs->directory_entry.crc32)
    {
        sysDbgMsg(DML2,
                  pjs->jar_filename,
                  JAR_MSG_MEMBER_CRC32 ": '%s'",
                  pjs->directory_entry.member_filename);

        pjs->jar_code = JAR_CRC32;
        pjs->jar_msg=JAR_MSG_MEMBER_CRC32 "(uncompressed member file)";
        return(pjs->jar_code);
    }

    /* Report successful read */
    pjs->jar_code  = JAR_OKAY;
    return(pjs->jar_code);

} /* END of jarutil_read_uncompressed_member() */


/*!
 * @brief Read a @e compressed archive member file.
 *
 */

static jar_api_enum jarutil_read_compressed_member(jar_state *pjs,
                                                   rchar    *member_bfr)
{
    ARCH_FUNCTION_NAME(jarutil_read_compressed_member);

    jar_api_enum rc;


    /* Read member header data.  If valid, read member data itself */
    rc = jarutil_read_and_verify_member_header(pjs);

    if (JAR_OKAY != rc)
    {
        return(rc);
    }

    /* Done if result is zero size */
    /*!
     * @todo HARMONY-6-jvm-jarutil.c-4 Is returning immediately
     * the best thing to do here?  Or should a zero-sized
     * decompression happen?
     */
#if 0
    if (0 == pjs->directory_entry.uncompressed_size)
    {
        pjs->jar_code  = JAR_OKAY;
        return(pjs->jar_code);
    }
#endif

    if (pjs->directory_entry.compressed_size !=
        portable_read(pjs->fd,
                      member_bfr,
                      pjs->directory_entry.compressed_size))
    {
        /* Report invalid read */
        sysDbgMsg(DML2,
                  pjs->jar_filename,
                  JAR_MSG_MEMBER_READ_DATA ": '%s'",
                  pjs->directory_entry.member_filename);

        pjs->jar_code = JAR_READ;
        pjs->jar_msg  = JAR_MSG_MEMBER_READ_DATA;
        return(pjs->jar_code);
    }

    /* Initialize, run, and finalize the decompressor */
    z_stream *pzs = (pjs->heap_get_data)(sizeof(z_stream), rfalse);

    pzs->total_in  = 0;  /* not specified in reference implementation */
    pzs->total_out = 0;

    pzs->zalloc    = jarutil_zlib_malloc;
    pzs->zfree     = jarutil_zlib_free;

    pzs->opaque    = (voidpf) pjs;

    pzs->next_in   = member_bfr;
    pzs->avail_in  = pjs->directory_entry.compressed_size;

    pzs->next_out  = pjs->bfr;
    pzs->avail_out = pjs->directory_entry.uncompressed_size;

    /*!
     * @internal  @e Always use @b inflateInit2() on all platforms
     * because it apparently does not expect the @e gzip header.
     * Instead, it simply decompresses the data and returns.
     * By using @b inflateInit() for non-Windows platforms, this
     * @e gzip header always makes @b inflate() return an error
     * in @b pzs->msg stating, "incorrect header check".
     *
     */
    int irc = inflateInit2(pzs, -MAX_WBITS);
    if (irc != Z_OK)
    {
        pjs->jar_code = JAR_INFLATEINIT;
        pjs->jar_msg = jarutil_zlib_code(irc);
        sysDbgMsg(DML2,
                  pjs->jar_filename,
                  JAR_MSG_MEMBER_ZLIB_INITD ": '%s'",
                  pjs->directory_entry.member_filename);

        pjs->zlib_code = irc;
        pjs->zlib_msg = pzs->msg;

        (pjs->heap_free_data)(pzs);
        return(pjs->jar_code);
    }

    irc = inflate(pzs, Z_FINISH);

    if ((Z_OK <= irc) && (pzs->msg))
    {
        pjs->jar_code = JAR_INFLATE;
        pjs->jar_msg = jarutil_zlib_code(irc);
        sysDbgMsg(DML2,
                  pjs->jar_filename,
                  JAR_MSG_MEMBER_ZLIB_DECOMPRESS ": '%s'",
                  pjs->directory_entry.member_filename);


        pjs->zlib_code = irc;
        pjs->zlib_msg = pzs->msg;

        inflateEnd(pzs);
        (pjs->heap_free_data)(pzs);
        return(pjs->jar_code);
    }

    if ((0 > irc) || (Z_STREAM_END != irc))
    {
        pjs->jar_code = JAR_INFLATE;
        pjs->jar_msg = jarutil_zlib_code(irc);
        sysDbgMsg(DML2,
                  pjs->jar_filename,
                  JAR_MSG_MEMBER_ZLIB_DECOMPRESS ": '%s'",
                  pjs->directory_entry.member_filename);


        pjs->zlib_code = irc;
        pjs->zlib_msg = pzs->msg;

        inflateEnd(pzs);
        (pjs->heap_free_data)(pzs);
        return(pjs->jar_code);
    }

    ruint crc = crc32(0,
                      pjs->bfr,
                      pjs->directory_entry.uncompressed_size);

    if (crc != pjs->directory_entry.crc32)
    {
        sysDbgMsg(DML2,
                  pjs->jar_filename,
                  JAR_MSG_MEMBER_CRC32 ": '%s'",
                  pjs->directory_entry.member_filename);

        pjs->jar_code = JAR_CRC32;
        pjs->jar_msg = JAR_MSG_MEMBER_CRC32 " (compressed member file)";

        inflateEnd(pzs);
        (pjs->heap_free_data)(pzs);
        return(pjs->jar_code);
    }

    /* Report success */
    pjs->jar_code  = JAR_OKAY;
    inflateEnd(pzs);
    (pjs->heap_free_data)(pzs);
    return(pjs->jar_code);

} /* END of jarutil_read_compressed_member() */

/*@} */ /* END of grouped definitions */


/*!
 * @brief Extract currently located Java archive member file
 * into memory buffer.
 *
 *
 * @param pjs   State of an open Java archive file, the result of
 *              a previous jarutil_find_member().
 *
 * @returns input parameter @b pjs .  Status of action will be
 *          contained in structure members, especially in
 *          @link #jar_state.jar_code jar_code@endlink.
 *
 */
jar_state *jarutil_read_current_member(jar_state *pjs)
{
    ARCH_FUNCTION_NAME(jarutil_read_current_member);

    /*
     * Make minor adjustment if reading JAR manifest file.
     * After reading, append NUL byte for use with
     * manifest_get_main_from_bfr()
     */
    rint adjust_manifest =
        (0 == portable_strcmp(&pjs->directory_entry.member_filename[0],
                              JVMCFG_JARFILE_MANIFEST_FILENAME))
              ? sizeof(rchar)
              : 0;

    /*
     * Since member is known to be available, go read it.
     *
     * If data is not compressed, simply read it.  If it
     * is compressed, allocate and initialize ZLIB interface,
     * then call its initialization function, the decompressor
     * itself, and the finalization function.
     */

    if (JAR_COMPRESSION_METHOD_STORE ==
        pjs->directory_entry.compression_method)
    {
        /* Buffer to hold contents of requested member file */
        pjs->bfr =
           (pjs->heap_get_data)(pjs->directory_entry.uncompressed_size +
                                  adjust_manifest,
                                rfalse);

        if (JAR_OKAY == jarutil_read_uncompressed_member(pjs))
        {
            /* Add NUL byte to bfr if it is a JAR manifest file image */
            if (0 != adjust_manifest)
            {
                pjs->bfr[pjs->directory_entry.uncompressed_size] = '\0';
            }
        }
        else
        {
             (pjs->heap_free_data)(pjs->bfr);
             pjs->bfr = (rchar *) rnull;
        }

        /* Return with member data or with error report */
        return(pjs);
    }
    else
    {
        if (JAR_COMPRESSION_METHOD_DEFLATE ==
            pjs->directory_entry.compression_method)
        {
            /* Temportary buffer to hold compressed member file data */
            rchar *member_bfr =
              (pjs->heap_get_data)(pjs->directory_entry.compressed_size,
                                   rfalse);

            /* Buffer to hold contents of requested member file */
            pjs->bfr =
            (pjs->heap_get_data)(pjs->directory_entry.uncompressed_size+
                                   adjust_manifest,
                                 rfalse);


            pjs->jar_code = jarutil_read_compressed_member(pjs,
                                                            member_bfr);

            if (JAR_OKAY == pjs->jar_code)
            {
                /* Add NUL byte to bfr if it is JAR manifest file img */
                if (0 != adjust_manifest)
                {
                    pjs->bfr[pjs->directory_entry.uncompressed_size] =
                                                                   '\0';
                }
            }
            else
            {
                 (pjs->heap_free_data)(member_bfr);
                 (pjs->heap_free_data)(pjs->bfr);
                 pjs->bfr = (rchar *) rnull;
            }

            /* Return with member data or with error report */
            return(pjs);
        }
        else
        {
/*NOTREACHED*/
            /*
             * This compression method is not supported.
             */

            sysDbgMsg(DML2,
                      pjs->jar_filename,
                      JAR_MSG_COMPRESSION_METHOD ": '%s'",
                      pjs->directory_entry.member_filename);

            portable_close(pjs->fd);

            pjs->fd = -1;  /* file was closed due to error */

            pjs->jar_code = JAR_COMPRESSION_METHOD;
            pjs->jar_msg  = JAR_MSG_COMPRESSION_METHOD;
            pjs->bfr      = (rchar *) rnull;

            /* Return with member data or with error report */
            return(pjs);
        }
    }
/*NOTREACHED*/
} /* END of jarutil_read_current_member() */




/*!
 * @name Examine and/or read Java archive member files in an archive.
 *
 *
 * @param jar_filename    Name of Java archive file to search.
 *
 * @param member_filename Name of member within this Java archive file.
 *
 * @param heap_get_data   Name of
 *                        @link jvm/src/gc.h heap allocation@endlink
 *                        function to be used internally by this
 *                        JAR function for allocating blocks of data.
 *                        Must be paired with matching
 *                        @b heap_free_data function parameter.
 *
 * @param heap_free_data  Name of
 *                        @link jvm/src/gc.h heap freeing@endlink
 *                        function to be used internally by this
 *                        JAR function for freeing allocated blocks
 *                        of data.  Must be paired with matching
 *                        @b heap_get_data function parameter.
 *
 * @returns Pointer to jar_state containing information about the class
 *          file data found in this JAR file, including especially
 *          its @link #jar_state.directory_entry directory_entry@endlink
 *          that contains the
 *          @link #jar_directory_entry.uncompressed_size
            uncompressed_size@endlink member that describes the length
 *          of the @link #jar_state.bfr jar_state.bfr@endlink buffer
 *          area that holds the return data. If the member was found
 *          then @link #jar_state.jar_code jar_code@endlink
 *          will contain @link #JAR_OKAY JAR_OKAY@endlink and the
 *          buffer pointer will be valid.  Otherwise, a different
 *          value will be stored there and the buffer pointer will not
 *          be valid.  <i>In @b all of these cases, the returned pointer
 *          will need to be freed.</i>  There will @e never be a
 *          @link #rnull rnull@endlink pointer returned when the input
 *          parameters are valid.)  If a @link #rnull rnull@endlink
 *          pointer actually @e is returned, then there was an error
 *          in an input parameter.
 *
 */


/*@{ */ /* Begin grouped definitions */

/*!
 * @brief Query for presence a Java archive member file in an archive.
 *
 */

jar_state *jarutil_find_member(const rchar *jar_filename,
                               const rchar *member_filename,
                               rvoid *(* const heap_get_data)(rint,
                                                              rboolean),
                               rvoid  (* const heap_free_data)(rvoid *))
{
    ARCH_FUNCTION_NAME(jarutil_find_member);

    /* Check input parameters */
    if ((rnull == jar_filename)        ||
        (rnull == member_filename)     ||
        (rnull == heap_get_data)       ||
        (rnull == heap_free_data)      ||
        (0 == portable_strlen(jar_filename))    ||
        (0 == portable_strlen(member_filename)) ||
        (JAR_MAX_MEMBER_FILENAME_LENGTH <
         portable_strlen(member_filename)))
    {
        return((jar_state *) rnull);
    }
    /*
     * This structure is used by ALL subsidiary function
     * calls to retain current state of JAR file access.
     */
    jar_state *pjs = (heap_get_data)(sizeof(jar_state), rtrue);

    /* Store input parameters to be passed around the code */
    pjs->jar_filename    = jar_filename;
    pjs->member_filename = member_filename;
    pjs->heap_get_data   = heap_get_data;
    pjs->heap_free_data  = heap_free_data;

    /*
     * Initialize return codes, etc.
     */
    pjs->fd  = JAR_MEMBER_IMPOSSIBLE_OPEN_FD;
    pjs->jar_code  = JAR_OKAY;
    pjs->jar_msg   = (rchar *) rnull;
    pjs->zlib_code = Z_OK; /* Valid decompress sets to Z_STREAM_END */
    pjs->zlib_msg  = (rchar *) rnull;

    /*!
      * @todo HARMONY-6-jvm-jarutil.c-1 Should JAR files be able to
      *       be opened with O_LARGEFILE so that huge archives may
      *       be automatically supported?
      */
    /* Open JAR file for read and report any errors */
    pjs->fd = portable_open(pjs->jar_filename, O_RDONLY);

    if (0 > pjs->fd)
    {
        sysDbgMsg(DML2,
                  pjs->jar_filename,
                  JAR_MSG_OPEN);

        pjs->jar_msg = JAR_MSG_OPEN;
        pjs->jar_code = JAR_OPEN;
        return(pjs);
    }

    /* Search for requested member of this JAR file */
    if (JAR_OKAY != jarutil_locate_member(pjs))
    {
        portable_close(pjs->fd);

        /* file was closed due to error */
        pjs->fd = JAR_MEMBER_IMPOSSIBLE_OPEN_FD;
    }

    /* Report current state, whether good or bad */
    return(pjs);

} /* END of jarutil_find_member() */


/*!
 * @brief Extract a Java archive member file into memory buffer.
 *
 */
jar_state *jarutil_read_member(const rchar *jar_filename,
                               const rchar *member_filename,
                               rvoid *(* const heap_get_data)(rint,
                                                              rboolean),
                               rvoid  (* const heap_free_data)(rvoid *))
{
    ARCH_FUNCTION_NAME(jarutil_read_member);

    jar_state *pjs = jarutil_find_member(jar_filename,
                                         member_filename,
                                         heap_get_data,
                                         heap_free_data);

    if (pjs == (jar_state *) rnull)
    {
        return(pjs);
    }

    /*
     * Since member is known to be available, go read it.
     */
    return(jarutil_read_current_member(pjs));

} /* END of jarutil_read_member() */


/*@} */ /* End of grouped definitions */


/* EOF */
