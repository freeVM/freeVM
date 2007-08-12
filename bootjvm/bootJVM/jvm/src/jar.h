#ifndef _jar_h_defined_
#define _jar_h_defined_

/*!
 * @file jar.h
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
 * The overall structure of the JAR file looks something like this:
 *
 * @verbatim
   ... data ...
   directory
   global trailer
   EOF
 * @endverbatim
 *
 * @section Control
 *
 * \$URL$
 *
 * \$Id$
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

ARCH_HEADER_COPYRIGHT_APACHE(jar, h,
"$URL$",
"$Id$");

#include "jrtypes.h"

/* Declare full 1-byte structure packing */
#pragma pack(1)

/*!
 * @name Java archiver error code enumerations.
 *
 * These codes report the success or reason for failure of
 * the @b jar_XXX() functions defined in
 * @link jvm/src/jar.c jar.c@endlink.
 *
 */

/*@{ */ /* Begin grouped definitions */

typedef enum
{
    JAR_OKAY             = 0, /**< JAR operation successful */
    JAR_PARM             = 1, /**< API parameter error */
    JAR_OPEN             = 2, /**< Error with @b open(2) on JAR file */
    JAR_LSEEK            = 3, /**< Error with @b lseek(2) on JAR file */
    JAR_READ             = 4, /**< Error with @b read(2) on JAR file */
    JAR_TRAILER_NOT_FOUND= 4,/**< Global trailer not found in JAR file*/
    JAR_BAD_TRAILER      = 6, /**< Global trailer truncated or corrupt*/
  JAR_BAD_DIRECTORY_ENTRY= 7, /**< Directory entry corrupt */
    JAR_BAD_MEMBER_ENTRY = 8, /**< Member file entry header corrupt */
    JAR_MEMBER_NOT_FOUND = 9, /**< Member file not found in JAR file */
    JAR_COMPRESSION_METHOD=10,/**< Compression method not supported */
    JAR_UNCOMPRESSED_SIZE =11,/**< Compression method not supported */
    JAR_FIELD_MISMATCH   = 12,/**< Critical field in directory do not
                                     match same in member header */
    JAR_INFLATEINIT      = 13,/**< ZLIB inflateInit() failed */
    JAR_INFLATE          = 14,/**< ZLIB inflate() failed */
    JAR_CRC32            = 15,/**< crc32 on extracted data failed */

} jar_api_enum;

/*@} */ /* End of grouped definitions */


/*!
 * @brief Maximum size of Java archive global file trailer
 *
 */
#define JAR_MAX_TRAILER_SIZE 65535

/*!
 * @brief Maximum length of Java archive member file name.
 *
 */
#define JAR_MAX_MEMBER_FILENAME_LENGTH 256

/*!
 * @name Compression methods used in JAR files.
 *
 */

/*@{ */ /* Begin grouped definitions */

#define JAR_COMPRESSION_METHOD_STORE 0   /**< No compression used */
#define JAR_COMPRESSION_METHOD_DEFLATE Z_DEFLATED
                                         /**< inflate/deflate method
                                            from @b LIBZ (see zlib.h) */

/*@} */ /* End of grouped definitions */


/*!
 * @brief Length of typical file read operations in global trailer.
 */
#define JAR_READ_BLOCK_SIZE 1024

/*!
 * @name Global trailer signature bytes
 *
 * These are found together in sequence at the beginning
 * of the global trailer and indicate the actual start
 * position of the global trailer.
 *
 * @note Just for your edification, the first two bytes are the
 * initials of the author of the compression algorithm used
 * by 'libz', namely, Phil Katz.  (Nice signature, Phil!
 * Thanks for your efforts and your skill.  I have appreciated
 * your work since the mid-1980's when 'arc' and 'pkarc' came
 * out! --DL)
 */

/*@{ */ /* Begin grouped definitions */

#define JAR_TRAILER_SIGNATURE_BYTE1 0x50   /**< Letter @b 'P' */
#define JAR_TRAILER_SIGNATURE_BYTE2 0x4b   /**< Letter @b 'K' */
#define JAR_TRAILER_SIGNATURE_BYTE3 0x05
#define JAR_TRAILER_SIGNATURE_BYTE4 0x06

/*@} */ /* End of grouped definitions */

/*!
 * @brief Size of global trailer signature in bytes.
 *
 */
#define JAR_NUM_TRAILER_SIGNATURE_BYTES 4


/*!
 * @brief Contents of global trailer of Java archive.
 *
 * @internal Notice that in this implementation, @e only a single volume
 * is supported, even though the trailer contains fields that support
 * multi-volume archives.  This is likely a relic of the days of
 * ZIP files on diskettes.
 *
 *
 */
typedef struct
{
    rushort volume_number; /**< Volume number (only 1 vol supported) */

    rushort volume_with_directory_start; /**< Volume holding start of
                                              directory (only 1 volume
                                              supported) */

    rushort this_volume_directory_entries; /**< Number of entries in
                                                this volume's directory
                                                */

    rushort total_directory_entries;     /**< Number of directory
                                              entries in the whole
                                              directory */

    ruint   total_directory_size;        /**< Length of @b entire
                                              directory in bytes
                                              relative to starting
                                              disk number */

    ruint  directory_position;           /**< Position (offset) of
                                              directory relative to
                                              starting disk */

    ruint   trailer_size;               /**< Length of global trailer
                                             in bytes*/

} jar_global_trailer;


/*!
 * @name Directory signature word
 *
 * These are found together in sequence at the beginning
 * of the directory and indicate the actual start
 * position of the directory.
 *
 * @note See note above on @link #JAR_TRAILER_SIGNATURE_BYTE1
   JAR_TRAILER_SIGNATURE_BYTEx@endlink on contents of these bytes.
 *
 */

/*@{ */ /* Begin grouped definitions */

#define JAR_DIRECTORY_SIGNATURE_BYTE1 0x50   /**< Letter @b 'P' */
#define JAR_DIRECTORY_SIGNATURE_BYTE2 0x4b   /**< Letter @b 'K' */
#define JAR_DIRECTORY_SIGNATURE_BYTE3 0x01
#define JAR_DIRECTORY_SIGNATURE_BYTE4 0x02

/*@} */ /* End of grouped definitions */


/*!
 * @brief Size of directory signature in bytes.
 *
 */
#define JAR_NUM_DIRECTORY_SIGNATURE_BYTES 4


/*!
 * @brief Time stamp in generic form, not platform-specific.
 *
 * This structure is reminiscent of <b><code>(struct tm)</code></b>
 * in @b <time.h> in the naming of the members.
 *
 */
typedef struct
{
    ruint  tm_sec;     /**< Seconds after the minute, 0-59 */

    ruint  tm_min;     /**< Minutes after the hour, 0-59 */

    ruint  tm_hour;    /**< Hours after midnight, 0-23 */

    ruint  tm_mday;    /**< Day of the month, 1-31 */

    ruint  tm_mon;     /**< Months since beginning of year, 0-11 */

    ruint  tm_year;    /**< Years 1980-2044 (MS-DOS min/max year) */

} tm_zip;


/*!
 * @brief Java archive file directory entry.
 *
 * @todo HARMONY-6-jvm-jar.h-1  Why does the reference implementation's
 * structure declare all of these fields as (unsigned long)?  It also
 * says that the places where it reads 2-byte fields are only used
 * as 2-byte fields, so why have 4 bytes in its structure definition?
 *
 */
typedef struct
{
    ruint  position;         /**< Position in ZIP file of
                                        this directory entry */

    rbyte   magic[JAR_NUM_DIRECTORY_SIGNATURE_BYTES];
                             /**< Directory entry signature */

    rushort version;         /**< ZIP version that created this entry */

    rushort version_needed;  /**< ZIP version required to extract
                                  this entry */

    rushort flags;           /**< Status bits and flags */

    rushort compression_method;/**< Compression method used in
                                    creating the entry */

    ruint   dos_date;        /**< Modification data in MS-DOS format */

    ruint   crc32;           /**< CRC-32 of the entry */

    ruint  compressed_size;  /**< Compressed data size in bytes */

    ruint  uncompressed_size;/**< Uncompressed data size in bytes */

    rushort size_member_filename;
                             /**< Member file name length of entry
                                  in bytes */

    rushort size_member_extra;/**< Extra field length in bytes */

    rushort size_member_comment;/**< Member comment length in bytes */

    rushort disk_num_start; /**< Volume number of start of file (must
                                 be zero in this implementation) */

    rushort internal_fa;    /**< internal file attributes */

    ruint   external_fa;    /**< external file attributes */

    ruint   member_offset;  /**< Offset in bytes of where this member
                                 is stored in archive */


    rbyte member_filename[JAR_MAX_MEMBER_FILENAME_LENGTH + 1];
                            /**< Name of member file, plus NUL byte */

    rbyte notused1;         /**< Pad byte, member_filename is odd len*/


    tm_zip  generic_date;   /**< Date in generic format as derived
                                 from directory entry date itself in
                                 @link #jar_directory_entry.dos_date 
                                 jar_directory_fixed.dos_date@endlink */

    ruint   extra_offset;   /**< Offset in bytes of where member's
                                 extra field is stored in archive.  Only
                                 meaningful when @b size_member_extra
                                 is non-zero */

    ruint   comment_offset; /**< Offset in bytes of where member's
                                 comment field is stored in archive.
                                 Only meaningful when
                                 @b size_member_comment is non-zero */

} jar_directory_entry;


/*!
 * @name Member signature word
 *
 * These are found together in sequence at the beginning
 * of each member file's storage area and indicate the
 * actual start position of each member entry.
 *
 * @note See note above on @link #JAR_TRAILER_SIGNATURE_BYTE1
   JAR_TRAILER_SIGNATURE_BYTEx@endlink on contents of these bytes.
 *
 */

/*@{ */ /* Begin grouped definitions */

#define JAR_MEMBER_SIGNATURE_BYTE1 0x50   /**< Letter @b 'P' */
#define JAR_MEMBER_SIGNATURE_BYTE2 0x4b   /**< Letter @b 'K' */
#define JAR_MEMBER_SIGNATURE_BYTE3 0x03
#define JAR_MEMBER_SIGNATURE_BYTE4 0x04

/*@} */ /* End of grouped definitions */


/*!
 * @brief Size of directory signature in bytes.
 *
 */
#define JAR_NUM_MEMBER_SIGNATURE_BYTES 4


/*!
 * @brief Java archive file member file entry.
 *
 * @todo HARMONY-6-jvm-jar.h-2  Need to study more closely the
 * usage of the fields of this structure in the reference
 * implementation.  In particular, the bitwise flags of both
 * this and of the directory entry.  The reference implementation
 * function <b><code>unzlocal_CheckCurrentFileCoherencyHeader</code></b>
 * is probably the place to start.
 *
 */
typedef struct
{
    rbyte   magic[JAR_NUM_DIRECTORY_SIGNATURE_BYTES];
                             /**< Directory entry signature */

    rushort version;         /**< ZIP version that created this entry */

    rushort flags;           /**< Status bits and flags */

#define MEMBER_ENTRY_FLAG_BIT3 0x0008 /**< Suppress comparison of
                                  certain member fields with directory
                                  entry equivalent, namely CRC,
                                  compressed and uncompressed sizes.
                                  When clear, perform comparison and
                                  report error if mismatch found. */


    rushort compression_method;/**< Compression method used in
                                    creating the entry */

    ruint   dos_date;        /**< Modification data in MS-DOS format */

    ruint   crc32;           /**< CRC-32 of the entry */

    ruint  compressed_size;  /**< Compressed data size in bytes */

    ruint  uncompressed_size;/**< Uncompressed data size in bytes */

    rushort size_member_filename;
                             /**< Member file name length of entry
                                  in bytes */

    rushort size_member_extra;/**< Extra field length in bytes */

    rbyte member_filename[JAR_MAX_MEMBER_FILENAME_LENGTH + 1];
                            /**< Name of member file, plus NUL byte */

    rbyte notused1;         /**< Pad byte, member_filename is odd len*/

    ruint   extra_offset;   /**< Offset in bytes of where member's
                                 extra field is stored in archive.  Only
                                 meaningful when @b size_member_extra
                                 is non-zero */

} jar_member_entry;


/*!
 * @brief State of open JAR file.
 *
 */
typedef struct
{
    const rchar *jar_filename;
                       /**< Name of Java archive file */

    ruint  archive_length; /**< Length in bytes of Jave archive file */

    const rchar *member_filename;
                       /**< Name of current archive member file */

    ruint  trailer_position; /**< Position (offset) of global trailer */

    jar_global_trailer trailer; /**< Global trailer at end of archive */

    /*!
     * @todo HARMONY-6-jvm-jar.h-3  This gap field needs to be
     * studied for cases when it is non-zero.  The reference
     * implementation says, "(>0 for sfx)", whatever that means.
     * It is calculated only once, when the JAR trailer has been
     * parsed and is always calculated as,
     *
     *       <b><code>trailer_position -
                      trailer.directory_position -
                      trailer.total_directory_size</code></b>
     */
    ruint  trailer_directory_gap; /**< Effectively number of bytes
                                   *   between global trailer (at EOF)
                                   *   and end of directory (which
                                   *   @e should immediately precede it.
                                   */

    jar_directory_entry directory_entry;
                                  /**< JAR file directory entry of
                                       requested member file */

    jar_member_entry member_entry; /**< JAR file member header of
                                       requested member file */

    rvoid *(* /* const */ heap_get_data)(rint, rboolean);
                       /**< Name of heap allocator for data blocks */

    rvoid  (* /* const */ heap_free_data)(rvoid *);
                       /**< Name of heap de-allocator for data blocks */

    rint fd;           /**< File descriptor of open JAR file.  If ever
                            a presumed open file handle is set to
                            @link #JAR_MEMBER_IMPOSSIBLE_OPEN_FD
                            JAR_MEMBER_IMPOSSIBLE_OPEN_FD@endlink
                            then an error occurred on a previous file
                            operation and the file was closed.  (See
                            @b jar_code and other fields for more
                            information at this stage.) This same value
                            is used to initialize the field before a
                            file is open. */

    ruint  seek_read;  /**< Read pointer for next @b read(2) operation*/

    rchar *bfr;        /**< Generic buffer pointer for read/write */

    jar_api_enum jar_code;
                       /**< Diagnostic state upon failure of request, or
                            @link #JAR_OKAY JAR_OKAY@endlink upon
                            success */

    rchar *jar_msg;    /**< Message returned from Java archive request,
                            if any.  Set to @link #rnull rnull@endlink
                            when there is no message. This is a fixed
                            string and does not need to be freed once
                            read.  */

    rint zlib_code;    /**< Diagnostic state upon failure of ZLIB
                            decompression request, or @b Z_STREAM_END
                            upon success.  (This code is returned by the
                            ZLIB <b><code>inflate()</code></b> function
                            following successful decompression of the
                            last or only segment of a compressed
                            archive member file.) Meaningful when
                           @link #jar_directory_entry.compression_method
                          jar_directory_entry.compression_method@endlink
                            is not @link #JAR_COMPRESSION_METHOD_STORE
                            JAR_COMPRESSION_METHOD_STORE@endlink */

    rchar *zlib_msg;   /**< Message returned from ZLIB decompression
                            request, if any.  Simply copied out, never
                            interpreted.  Set to
                            @link #rnull rnull@endlink when there is
                            no message or when the requested member was
                            not compressed.  This @e appears to be a
                            fixed string and does not need to be freed
                            once read.  */
} jar_state;


/*!
 * @name JAR informational return messages
 *
 */

/*@{ */ /* Begin grouped definitions */

#define JAR_MSG_OPEN               "cannot open archive file"
#define JAR_MSG_END_SEEK           "cannot seek to end of archive file"
#define JAR_MSG_COMPRESSION_METHOD "unsupported compression method"



#define JAR_MSG_TRAILER_SEEK       "cannot seek global trailer"
#define JAR_MSG_TRAILER_READ       "cannot read global trailer"
#define JAR_MSG_TRAILER_TRUNCATED  "truncated global trailer"
#define JAR_MSG_TRAILER_CONFLICT   "conflicting trailer data"
#define JAR_MSG_TRAILER_MISSING    "global trailer not found"



#define JAR_MSG_DIRENT_SEEK        "cannot seek directory entry"
#define JAR_MSG_DIRENT_READ        "cannot read directory entry"
#define JAR_MSG_DIRENT_CORRUPT     "directory entry corrupt"
#define JAR_MSG_DIRENT_FILENAME_SIZE \
                                "file name too large in directory entry"
#define JAR_MSG_DIRENT_MULTIVOLUME "multi-volume archives not supported"
#define JAR_MSG_DIRENT_READ_MEMBERNAME \
                          "cannot read directory entry member file name"
#define JAR_MSG_DIRENT_MEMBER_NOT_FOUND \
                                   "member file not found"



#define JAR_MSG_MEMBER_SEEK_HEADER "cannot seek member header"
#define JAR_MSG_MEMBER_READ_HEADER "cannot read member header"
#define JAR_MSG_MEMBER_CORRUPT     "member header corrupt"
#define JAR_MSG_MEMBER_FILENAME_SIZE \
                                  "file name too large in member header"
#define JAR_MSG_MEMBER_DIRENT_MISMATCH "directory/member field mismatch"
#define JAR_MSG_MEMBER_READ_MEMBERNAME \
                          "cannot read member header file name"
#define JAR_MSG_MEMBER_SEEK_DATA "cannot seek member data area"
#define JAR_MSG_MEMBER_READ_DATA "cannot read member data area"
#define JAR_MSG_MEMBER_CRC32     "CRC-32 error reading member"
#define JAR_MSG_MEMBER_BAD_SIZE  "Uncompressed size too small for data"
#define JAR_MSG_MEMBER_ZLIB_INITD "initializing ZLIB decompression"
#define JAR_MSG_MEMBER_ZLIB_DECOMPRESS "performing ZLIB decompression"
#define JAR_MSG_MEMBER_ZLIB_FINALD "finalizing ZLIB decompression"


/*@} */ /* End of grouped definitions */




/*!
 * @brief Impossible @b lseek(2) position of member in JAR file.
 *
 */
#define JAR_MEMBER_IMPOSSIBLE_LSEEK (-1)


/*!
 * @brief Impossible @b open(2) file handle.
 *
 * This value is used to initialize the
 * @link #jar_state.fd jar_state.fd@endlink field and is stored there
 * after the file is closed.
 *
 */
#define JAR_MEMBER_IMPOSSIBLE_OPEN_FD (-1)



/*!
 * @brief Java archiver @b jar command line arguments, less
 * implementation-specific options.
 *
 */
typedef struct
{
    rboolean  jar_is_a_flag;     /**< jar -{anything} requested */

    rboolean cannot_be_jar_option;/**< The first option may omit
                                       prefixed '-' if desired */

    /*!
     * @name JAR command line switch parsing flags and counters
     *
     */

    /*@{ */ /* Begin grouped definitions */

    rboolean  jar_c_flag;        /**< jar -c requested */
    rboolean  jar_t_flag;        /**< jar -t requested */
    rboolean  jar_x_flag;        /**< jar -x requested */
    rboolean  jar_u_flag;        /**< jar -u requested */

    rboolean  jar_0_flag;        /**< jar -0 requested */
    rboolean  jar_v_flag;        /**< jar -v requested */
    rboolean  jar_M_flag;        /**< jar -M requested */
    rboolean  jar_i_flag;        /**< jar -i requested */

    /*@} */ /* End of grouped definitions */


    rint      jar_next_position; /**< argv[idx] of next parm */

    /*!
     * @name Commands that take an argument
     *
     */

    /*@{ */ /* Begin grouped definitions */

    rboolean  jar_f_flag;        /**< jar -f requested */
    rint      jar_f_position;    /**< argv[idx] of -f parm */
    rchar    *jar_f_argv;        /**< *argv[idx] of -f parm */

    rboolean  jar_m_flag;        /**< jar -c requested */
    rint      jar_m_position;    /**< argv[idx] of -m parm */
    rchar    *jar_m_argv;        /**< *argv[idx] of -m parm */

    rboolean  jar_C_flag;        /**< jar -C requested */
    rint      jar_C_position;    /**< argv[idx] of -C parm */
    rchar    *jar_C_argv;        /**< *argv[idx] of -C parm */

    /*@} */ /* End of grouped definitions */


    /*!
     * @name Remainder of arguments describe the requested JAR members
     *
     */

    /*@{ */ /* Begin grouped definitions */

    rboolean  jar_member_flag;     /**< jar members are requested */
    rint      jar_member_position; /**< argv[idx] of first member */

    /*@} */ /* End of grouped definitions */


} jar_argv;


/* Prototypes for selected functions in 'jarargv.c' */
/* (Prototype for other jarargv_xxx() fns are in 'util.h') */
extern jar_argv *jarargv_init(int        argc,
                              char     **argv,
                              char     **envp,
                              rboolean  *argv_init_flag);


/* Prototypes for functions in 'jarutil.c' */

extern jar_api_enum  jarutil_locate_global_trailer(jar_state *jps);

extern jar_api_enum  jarutil_locate_zip_directory(jar_state *jps);

extern jar_state    *jarutil_read_current_member(jar_state *jps);

extern jar_state    *jarutil_find_member(
                      const rchar *jar_filename,
                      const rchar *member_filename,
                      rvoid *(*const heap_get_data) (rint, rboolean),
                      rvoid (*const heap_free_data) (rvoid *));

extern jar_state    *jarutil_read_member(
                      const rchar *jar_filename,
                      const rchar *member_filename,
                      rvoid *(*const heap_get_data) (rint, rboolean),
                      rvoid (*const heap_free_data) (rvoid *));

/* Restore previous structure packing */
#pragma pack()

#endif /* _jar_h_defined_ */


/* EOF */
