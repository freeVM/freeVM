/*!
 * @file classpath.c
 *
 * @brief Extract @b CLASSPATH runtime variables from the environment
 * and/or the command line or other appropriate sources.
 *
 * The HEAP_INIT() function must have been called before using
 * these functions so the environment variables can be stored
 * into it and not depend on the argument or environment pointers
 * to always be unchanged.
 *
 * If the @link #JVMCFG_TMPAREA_IN_USE temporary directory area@endlink
 * is in use, the tmparea_init() function must have been called before
 * these functions so the internal @b CLASSPATH can be set up properly.
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
ARCH_SOURCE_COPYRIGHT_APACHE(classpath, c,
"$URL$",
"$Id$");


#include "jvmcfg.h" 
#include "cfmacros.h" 
#include "classfile.h" 
#include "classpath.h" 
#include "exit.h" 
#include "heap.h" 
#include "linkage.h" 
#include "jvm.h" 
#include "nts.h" 
#include "utf.h" 
#include "util.h" 

/*!
 *
 * @brief Initialize @b CLASSPATH search area of the JVM model.
 * 
 * Break @b CLASSPATH apart into its constituent paths.  Run once
 * during startup to parse @b CLASSPATH.  Heap management must be
 * started before calling this function via HEAP_INIT().  The
 * command line must also have been scanned via jvmargv_init().
 *
 * If the @link #JVMCFG_TMPAREA_IN_USE temporary directory area@endlink
 * is in use, the tmparea_init() function must have been called before
 * these functions so the internal @b CLASSPATH can be set up properly.
 *
 *
 * @param  argc    Number of arguments on command line
 *
 * @param  argv    Argument vector from the command line
 *
 * @param  envp    Environment pointer from command line environ
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 *
 * @todo  HARMONY-6-jvm-classpath.c-1 Add proper searching
 *        for @c @b rt.jar file and @c @b Xbootclasspath .
 *        For the moment, they are defined in
 *        @link  config.h config.h@endlink as the
 *        @link #CONFIG_HACKED_RTJARFILE CONFIG_HACKED_RTJARFILE@endlink
 *        and @link #CONFIG_HACKED_BOOTCLASSPATH
          CONFIG_HACKED_BOOTCLASSPATH@endlink
 *        pre-processor symbols and are commented in
 *        @link jvm/src/jvmcfg.h jvmcfg.h@endlink after this
 *        fashion.
 *
 */
static rchar **classpath_list    = CHEAT_AND_USE_NULL_TO_INITIALIZE;
static rint   classpath_list_len = 0;

rvoid classpath_init()
{
    ARCH_FUNCTION_NAME(classpath_init);

    /* Initialize only once */
    if (rnull != classpath_list)
    {
        return;
    }

    rint i;
    rint pathcount;

    /*
     * Prepare to concatenate startup JAR file and the
     * possible temporary directory area 'tmparea' with
     * actual @b CLASSPATH, followed by potential
     * CONFIG_HACKED_xxx definitions.
     */
    rchar *moreclasspath;             /* @b CLASSPATH dlm */
    rint morecplen;
    morecplen = (pjvm->startjar)
                ? (portable_strlen(pjvm->startjar) + sizeof(rchar))
                : 0;
    morecplen +=
#if JVMCFG_TMPAREA_IN_USE
        portable_strlen(tmparea_get())   + sizeof(rchar)   +
#endif
        portable_strlen(pjvm->classpath) + sizeof(rchar);

    /*
     * Compensate for CONFIG_HACKED_xxx definitions, handy development
     * hooks for when @b CLASSPATH is in question.
     */
#ifdef CONFIG_HACKED_BOOTCLASSPATH
                                                 /* @b CLASSPATH dlm */
    morecplen +=
        portable_strlen(CONFIG_HACKED_BOOTCLASSPATH) + sizeof(rchar);
#endif
#ifdef CONFIG_HACKED_RTJARFILE

    /* For $JAVA_HOME/$CONFIG_HACKED_RTJARFILE */
    morecplen += portable_strlen(pjvm->java_home);
    morecplen += sizeof(rchar);
    morecplen += portable_strlen(CONFIG_HACKED_RTJARFILE)+sizeof(rchar);
#endif
    morecplen += sizeof(rchar);/*NUL byte, possibly 1 more than needed*/

    /*
     * Allocate space for classpath image with temp area and
     * possible CONFIG_HACKED_xxx adjustments
     */
    moreclasspath = HEAP_GET_DATA(morecplen, rfalse);

    /*
     * Generate concatenation of
     *
     * 1 pjvm->startjar:
     * 2 pjvm->tmparea:
     * 3 CLASSPATH:
     * 4 @link #CONFIG_HACKED_BOOTCLASSPATH
                CONFIG_HACKED_BOOTCLASSPATH@endlink:
     * 5 @link #CONFIG_HACKED_RTJARFILE CONFIG_HACKED_RTJARFILE@endlink
     */

    moreclasspath[0] = '\0';

    if (pjvm->startjar) /* When using '-jar start.class.jar.file' form*/
    {
         portable_strcat(moreclasspath, pjvm->startjar);
         i = portable_strlen(moreclasspath);
         moreclasspath[i] = CLASSPATH_ITEM_DELIMITER_CHAR;
         moreclasspath[i + 1] = '\0';
    }
#if JVMCFG_TMPAREA_IN_USE
    portable_strcat(moreclasspath, tmparea_get());
    i = portable_strlen(moreclasspath);
    moreclasspath[i] = CLASSPATH_ITEM_DELIMITER_CHAR;
    moreclasspath[i + 1] = '\0';
#endif

    portable_strcat(moreclasspath, pjvm->classpath);

#ifdef CONFIG_HACKED_BOOTCLASSPATH
    i = portable_strlen(moreclasspath);
    moreclasspath[i] = CLASSPATH_ITEM_DELIMITER_CHAR;
    moreclasspath[i + 1] = '\0';
    portable_strcat(moreclasspath, CONFIG_HACKED_BOOTCLASSPATH);
#endif

#ifdef CONFIG_HACKED_RTJARFILE
    i = portable_strlen(moreclasspath);
    moreclasspath[i] = CLASSPATH_ITEM_DELIMITER_CHAR;
    moreclasspath[i + 1] = '\0';
    portable_strcat(moreclasspath, pjvm->java_home);

    i = portable_strlen(moreclasspath);
    moreclasspath[i] = CLASSPATH_ITEM_DELIMITER_CHAR;
    moreclasspath[i + 1] = '\0';
    portable_strcat(moreclasspath, CONFIG_HACKED_RTJARFILE);
#endif
    HEAP_FREE_DATA(pjvm->classpath); /* May or may not be on heap */
    pjvm->classpath = moreclasspath;  /* Keep for duration of pgm run */

    rint tcplen = portable_strlen(moreclasspath);


    /* @warning  NON-STANDARD TERMINATION CONDITION <= VERSUS < */
    for (i = 0, pathcount = 0; i <= tcplen; i++)
    {
        if ((CLASSPATH_ITEM_DELIMITER_CHAR == moreclasspath[i]) ||
            (i == tcplen))
        {
            pathcount++;
        }
    }

    /* Allocate space for list of @b CLASSPATH entries, and using
     * /absolute/path/names */
    classpath_list = HEAP_GET_DATA(pathcount * sizeof(rchar *), rtrue);

    rchar *nextpath;
    rint  thislen;
    classpath_list_len = 0;

    /*!
     * @todo  HARMONY-6-jvm-classpath.c-7 Check `pwd` overflow
     *        and @link #rnull rnull@endlink returned
     */
    rchar *pwd = HEAP_GET_DATA(JVMCFG_SCRIPT_MAX, rfalse);
    portable_getwd(pwd);
    portable_strcat(pwd, JVMCFG_PATHNAME_DELIMITER_STRING);
    rint   pwdlen = portable_strlen(pwd);

    /* @warning  NON-STANDARD TERMINATION CONDITION <= VERSUS < */
    for (i = 0, nextpath = moreclasspath; i <= tcplen; i++)
    {

        /* If found item delimiter OR END OF STRING (SEE ABOVE) */
        if ((CLASSPATH_ITEM_DELIMITER_CHAR == moreclasspath[i]) ||
            (i == tcplen))
        {
            /* calculate length of this @b CLASSPATH entry */
            thislen = (&moreclasspath[i]) - nextpath;

            /*
             * Ignore double-delimiter cases.
             * It does not hurt any thing for @b classpath_list
             * to be longer than the pointers slots allocated
             * in it because @b classpath_list_len limits the
             * range of usage to those validly allocated.
             */
            if (0 == thislen)
            {
                /* Pretend it was valid */
                nextpath = &moreclasspath[i + 1];
                continue;
            }

            /*
             * Allocate enough space for item, plus final '\0'.
             * Add space for prefixed PWD if not already absolute.
             * (For Windows, paths less than 3 characters cannot contain
             *  'VOLUME:\...' form and so are copied directly.)
             * Since we are scanning for a delimiter or EOS,
             * the current length calculation includes the "1 + x"
             * for the '\0'.  The string[x] location is set to '\0'.
             *
             * If the path is of the form,
             * /absolute/path/name or C:\absolute\path\name then
             * simply copy it.  Otherwise prefix /present/working/dir
             * to make it absolute.
             *
             * Do not support Windows relative paths with volume
             * prefix:  D:..\dir\name
             *
             */

            /*!
             * @todo HARMONY-6-jvm-classpath.c-8 Document the lack of
             * support for Windows relative paths with volume
             * prefix:  D:..\\dir\\name
             *
             */
            if (
#if defined(CONFIG_WINDOWS) || defined(CONFIG_CYGWIN)
                (
                 (3 > thislen)                                   ||
                 (isalpha(nextpath[0])                      &&
                  (JVMCFG_PATHNAME_VOLUME_DELIMITER_CHAR ==
                   nextpath[1])                             &&
                  (JVMCFG_PATHNAME_DELIMITER_CHAR ==
                   nextpath[2])))
#if defined(CONFIG_CYGWIN)
                                                                      ||
                (JVMCFG_PATHNAME_ALT_DELIMITER_CHAR == nextpath[0]) 
#endif
#else
                JVMCFG_PATHNAME_DELIMITER_CHAR == nextpath[0]
#endif
               )
            {
                classpath_list[classpath_list_len] =
                    HEAP_GET_DATA(thislen + sizeof(rchar), rfalse);
                /* Store current @b CLASSPATH item, incl. final '\0' */
                portable_memcpy(classpath_list[classpath_list_len],
                                nextpath, 
                                thislen);
                classpath_list[classpath_list_len][thislen] = '\0';
            }
            else
            {
                classpath_list[classpath_list_len] =
                    HEAP_GET_DATA(thislen + pwdlen + sizeof(rchar),
                                  rfalse);

                portable_strcpy(classpath_list[classpath_list_len],
                                pwd);

                /* Store current @b CLASSPATH item, incl. final '\0' */
                portable_memcpy(&classpath_list[classpath_list_len]
                                               [pwdlen],
                                nextpath, 
                                thislen);
                classpath_list[classpath_list_len][thislen + pwdlen] =
                                                                   '\0';
            }

            classpath_list_len++;

            /* Start looking at next @b CLASSPATH item */
            nextpath = &moreclasspath[i + 1];

        } /* if moreclasspath[i] */

    } /* for i */

    /* Clean up */
    HEAP_FREE_DATA(pwd);

    /* Declare this module initialized */
    jvm_classpath_initialized = rtrue;

    return;

} /* END of classpath_init() */


/*!
 * @brief Determine whether or not a @b CLASSPATH entry is a JAR file
 * instead of being a directory name.
 *
 * A JAR file will be named @c @b /path/name/filename.jar, while a
 * file name in a directory will be named
 * @c @b /path/name/ClassName.class .  A JAR file may also be
 * named @c @b /path/name/filename.zip for legacy reasons.
 *
 *
 * @param  pclasspath  String from @b CLASSPATH list
 *
 *
 * @returns @link #rtrue rtrue@endlink if string ends with
 *          @c @b .jar, @link #rfalse rfalse@endlink otherwise.
 *
 */
rboolean classpath_isjar(rchar *pclasspath)
{
    ARCH_FUNCTION_NAME(classpath_isjar);

    rint len, jarlen, ext;

    rchar *ext_list[2] = { CLASSFILE_EXTENSION_JAR,
                           CLASSFILE_EXTENSION_ZIP };

    for (ext = 0; ext < 2; ext++)
    {
        /* Lengths of test string and of extension (w/ name.ext dlm) */
        len = portable_strlen(pclasspath);
        jarlen = portable_strlen(JVMCFG_EXTENSION_DELIMITER_STRING) +
                 portable_strlen(ext_list[ext]);

        /* For VERY short @b CLASSPATH entries,it cannot be a JAR file*/
        if (jarlen >= len)
        {
            continue;
        }

        /* Check if name.ext delimiter present in test string */
        if (JVMCFG_EXTENSION_DELIMITER_CHAR != pclasspath[len - jarlen])
        {
            continue;
        }

        /* Now go test JAR extension since delimiter is present */
        jarlen--;
        if (0 == portable_strncmp(&pclasspath[len - jarlen],
                                  ext_list[ext],
                                  jarlen))
        {
            return(rtrue);
        }
    } /* for ext */

    /* JAR extension not found in list */
    return(rfalse);

} /* END of classpath_isjar() */


/*!
 * @brief Convert class name format external to internal form.
 *
 * The external format is @c @b class.name.format , while the
 * internal format is @c @b class/name/format .
 * Result is unchanged if it is already in internal format.
 * When finished with result, call HEAP_FREE_DATA().
 *
 *
 * @param  clsname   Null-terminated string containing class name.
 *
 *
 * @returns Null terminated string in internal format.
 *          Call HEAP_FREE_DATA() when finished with buffer.
 *
 */
rchar *classpath_external2internal_classname(rchar *clsname)
{
    ARCH_FUNCTION_NAME(classpath_external2internal_classname);

    rint len = portable_strlen(clsname);
    rchar *rc = HEAP_GET_DATA(1 + len, rfalse); /* 1 + for NUL byte */

    portable_memcpy(rc, clsname, 1 + len); /* 1 + for NUL byte */

    return(classpath_external2internal_classname_inplace(rc));

} /* END of classpath_external2internal_classname() */


/*!
 * @brief Convert <em>in place</em> class name format external
 * to internal form.
 *
 * In-place version of classpath_external2internal_classname():
 * Takes an existing buffer and performs the conversion on it
 * @e without heap allocation.  Return the input buffer.
 *
 *
 * @param[in,out] inoutbfr  Existing buffer containing text to be
 *                          translated, also receive output
 *
 *
 * @returns buffer address @b inoutbfr
 *
 */
rchar *classpath_external2internal_classname_inplace(rchar *inoutbfr)
{
    ARCH_FUNCTION_NAME(classpath_external2internal_classname_inplace);

    rint i;
    int len = portable_strlen(inoutbfr);

    for (i = 0; i < len; i++)
    {
        /*
         * Substitute internal/external delimiter character
         * in @b inoutbfr where needed.
         */
        if (CLASSNAME_EXTERNAL_DELIMITER_CHAR == inoutbfr[i])
        {
            inoutbfr[i] = CLASSNAME_INTERNAL_DELIMITER_CHAR;
        }
     }

    return(inoutbfr);

} /* END of classpath_external2internal_classname_inplace() */


#if 0
/*!
 * @brief Adjust class name string for shell expansion artifacts.
 *
 * Insert a backslash character in front of the dollar sign of any
 * inner class name so that shell expansion does not see the dollar
 * sign as a special character representing a shell variable.  Unless
 * this step is taken, such a shell variable, most likely non-existent,
 * will effectively truncate the string before the dollar sign.
 *
 *
 * @param pclass_location  Null-terminated string containing a fully
 *                         qualified class name, typically a path into a
 *                         JAR file.
 *
 *
 * @returns buffer address @b outbfr.  When finished with this buffer,
 *          return it to the heap with
 *          @link #HEAP_FREE_DATA() HEAP_FREE_DATA@endlink.  Notice
 *          that if a buffer has no dollar signs, this function
 *          is equivalent to strcpy(3).
 *
 */
rchar *classpath_inner_class_adjust(rchar *pclass_location)
{
    ARCH_FUNCTION_NAME(classpath_inner_class_adjust);

    int len = portable_strlen(pclass_location);

    rint cllen = (rint) len;
    rint i, j;
    rint numdollar = 0;

#ifndef CONFIG_WINDOWS
    for (i = 0; i < cllen; i++)
    {
        if (CLASSNAME_INNERCLASS_MARKER_CHAR == pclass_location[i])
        {
            numdollar++;
        }
    }
#endif

    rchar *outbfr = HEAP_GET_DATA(cllen + numdollar, rfalse);

    /*
     * @warning  NON-STANDARD TERMINATION CONDITION <= VERSUS <
     *           (so that NUL byte gets copied without any
     *           special consideration).
     */
    for (i = 0, j = 0; i <= cllen; /* Done in body of loop: i++ */ )
    {
        /* Windows platforms don't need these escapes */
#ifndef CONFIG_WINDOWS
        /* Insert escape character before dollar sign where found */
        if (CLASSNAME_INNERCLASS_MARKER_CHAR == pclass_location[i])
        {
            outbfr[j++] = CLASSNAME_INNERCLASS_ESCAPE_CHAR;
        }
#endif

        /* Copy next input character to output buffer */
        outbfr[j++] = pclass_location[i++];
    }

    /* Return buffer expanded with escapes where needed */
    return(outbfr);

} /* END of classpath_inner_class_adjust() */
#endif


/*!
 * @brief Search @b CLASSPATH for a given class name using a prchar.
 *
 * Return heap pointer to a buffer containing its location.
 * If not found, return @link #rnull rnull@endlink.
 * If a class by this name is stored in more than one location, only
 * the first location is returned.  When done with result, call
 * HEAP_FREE_DATA(result) to return buffer to heap area.
 *
 * All CLASSNAME_EXTERNAL_DELIMITER (ASCII period) characters
 * found in the input class name will be unconditionally replaced
 * with CLASSNAME_INTERNAL_DELIMITER (ASCII slash) characters.
 * Therefore, the class file extension CLASSFILE_EXTENSION_DEFAULT
 * may not be appended to the class name.  This constraint permits
 * both internal and external class names to use the same function
 * to search for classes.
 *
 *
 * @param  clsname  Name of class, without @c @b .class extension, as
 *                  as either @c @b some.package.name.SomeClassName
 *                  or @c @b some/package/name/SomeClassName , that
 *                  is, the external and internal forms of the class
 *                  name, respectively.  The string may or may not
 *                  contain class formatting of the form
 *                  @c @b [[[Lsome/package/name/SomeClassName;
 *
 *
 * @returns Heap pointer containing @b CLASSPATH search result.  This
 *          result will be a non-null pointer in one member of this
 *          structure which will indicate either a JAR file member or
 *          a simple class file.  If result is a class file, simply
 *          open it, read, and close.  If it is a JAR file member, read
 *          it with jarutil_read_current_member(), retrieve its buffer
 *          pointer, and close it.  In both cases, free the indicated
 *          pointer when done.  After this, free the heap pointer
 *          that was returned.  (In other words, @e two heap pointers
 *          will need to be freed when done with the result.)  If the
 *          returned heap pointer was @link #rnull rnull@endlink,
 *          then the class was not found in the @b CLASSPATH and an
 *          error needs to be indicated.
 *
 * @todo HARMONY-6-jvm-classpath.c-2 VM Spec section 5.3.1:  Throw
 *       @b NoClassDeffoundError if no match.
 *
 *       Notice that @b clsname must be specified with package
 *       designations using INTERNAL (slash) delimiter form of
 *       the path.  This is what is natively found in the class
 *       files.  Of course, no package name means the simple
 *       default package, that is, an unpackaged class having
 *       no <b><code>package some.package.name</code></b> statement
 *       in source.
 *
 */

classpath_search *classpath_get_from_prchar(rchar *clsname)
{
    ARCH_FUNCTION_NAME(classpath_get_from_prchar);

    rvoid *statbfr; /* Portability library does (struct stat) part */

    rint i;
    rchar *name;
    int baselen;

    if (rtrue == nts_prchar_isclassformatted(clsname))
    {
        /*
         * Convert @c @b [[[Lpath/name/ClassName; into
         * @c @b path/name/ClassName form using array dimensions plus
         * data type specifier character 'L' (ie, add 1)
         */
        jvm_array_dim arraydims = nts_get_prchar_arraydims(clsname);
        name = &clsname[1 + arraydims];

        /* Calc position of end-of-class delimiter */
        rchar *pdlm = portable_strchr(name, BASETYPE_CHAR_L_TERM);
        baselen = portable_strlen(name);

        /* Should @e always be @link #rtrue rtrue@endlink */
        if (rnull != pdlm)
        {
            baselen = pdlm - name;
        }
    }
    else
    {
        /*
         * If this class name string contained no formatting,
         * fake the adjustment above to a formatted class name.
         * Notice that without formatting, there cannot be any
         * array dimensions.
         */
        name = clsname;
        baselen = portable_strlen(name);
    }

    /*
     * Allocate result structure, zero it out since one member
     * must be NULL upon return.
     */
    classpath_search *rc =HEAP_GET_DATA(sizeof(classpath_search),rtrue);

    rchar *class_location = HEAP_GET_DATA(JVMCFG_PATH_MAX, rfalse);

#if 0
    rchar *jarscript = HEAP_GET_DATA(JVMCFG_SCRIPT_MAX, rfalse);
#endif

    /*
     * Search through each entry in @b CLASSPATH for a file by
     * the proper name.
     */

    for (i = 0; i < classpath_list_len; i++)
    {
        /* Test for JAR files in @b CLASSPATH */
        if (rtrue == classpath_isjar(classpath_list[i]))
        {
            /* Convert input parm to internal form, append suffix */
            portable_strncpy(class_location, name, baselen);
            class_location[baselen] = '\0';

            (rvoid) classpath_external2internal_classname_inplace(
                                                        class_location);
            class_location[baselen] = JVMCFG_EXTENSION_DELIMITER_CHAR;
            class_location[baselen + 1] = '\0';
            portable_strcat(class_location,
                            CLASSFILE_EXTENSION_DEFAULT);

            jar_state *pjs = jarutil_find_member(classpath_list[i],
                                                 class_location,
                                                 HEAP_GET_DATA,
                                                 HEAP_FREE_DATA);
            /*
             * Ignore this @b CLASSPATH entry if something was wrong
             * with an input parameter, either the @b CLASSPATH entry
             * itself or the @b clsname.
             */
            if (rnull == pjs)
            {
                continue;
            }

            /* If match found, report to caller.  Else keep looking */
            if (pjs->jar_code != JAR_OKAY)
            {
                if (JAR_MEMBER_IMPOSSIBLE_OPEN_FD != pjs->fd)
                {
                    portable_close(pjs->fd);
                }

                HEAP_FREE_DATA(pjs);
                continue;
            }

            HEAP_FREE_DATA(class_location);

            rc->jarfile_member_state = pjs;

            return(rc);
        }
        else
        {
            /*!
             * @todo  HARMONY-6-jvm-classpath.c-6 Make sure that this
             *        sprintf/stat works with both CONFIG_WINDOWS and
             *        CONFIG_CYGWIN.
             *
             */

            /* Convert input parm (directory name) to internal form */
            sprintfLocal(class_location,
                         "%s%c\0",
                         classpath_list[i],
                         JVMCFG_PATHNAME_DELIMITER_CHAR);

            int clen = portable_strlen(class_location);

            portable_strcat(class_location, name);

            /*
             * Convert input parm to internal format and append
             * class suffix, but convert @e only the @b name part
             * just appended.
             */
            (rvoid) classpath_external2internal_classname_inplace(
                                                 &class_location[clen]);


            class_location[clen + baselen] =
                                        JVMCFG_EXTENSION_DELIMITER_CHAR;
            class_location[clen + baselen + 1] = '\0';

            portable_strcat(class_location,CLASSFILE_EXTENSION_DEFAULT);

            /* Test for existence of valid class file */
            statbfr = portable_stat(class_location);
            HEAP_FREE_DATA(statbfr);

            /* If match found, report to caller.  Else keep looking */
            if (rnull == statbfr)
            {
                HEAP_FREE_DATA(class_location);
            }

            rc->classfile_name = class_location;

            return(rc);
        }
    } /* for i */

    /* Class not found in @b CLASSPATH */
    HEAP_FREE_DATA(class_location);
#if JVMCFG_TMPAREA_IN_USE
    HEAP_FREE_DATA(jarscript);
#endif

    return((classpath_search *) rnull);

} /* END of classpath_get_from_prchar() */


/*!
 * @brief Search @b CLASSPATH for a given class name using
 * a CONSTANT_Utf8_info.
 *
 * Invoke @link #classpath_get_from_prchar()
   classpath_get_from_prchar@endlink after converting @b clsname
 * from CONSTANT_Utf8_info to prchar.
 *
 * For more information, see @link #classpath_get_from_prchar()
   classpath_get_from_prchar@endlink.
 *
 *
 * @param  clsname  Name of class, without @c @b .class
 *                  extension, as either @c @b some.class.name
 *                  or @c @b some/package/name/SomePackageName , that
 *                  is, the internal form of the class name.  The string
 *                  may or may not contain class formatting of the
 *                  form @c @b [[[Lsome/package/name/SomeClassName;
 *
 *
 * @returns Heap pointer containing @b CLASSPATH search result.  If
 *          result is a class file, simply open it, read, and close.
 *          If it is a JAR file member, read it with
 *          jarutil_read_current_member(), retrieve its buffer pointer,
 *          and close it.
 *
 */

classpath_search *classpath_get_from_cp_entry_utf(cp_info_mem_align
                                                               *clsname)
{
    ARCH_FUNCTION_NAME(classpath_get_from_cp_entry_utf);

    rchar *prchar_clsname = utf_utf2prchar(PTR_THIS_CP_Utf8(clsname));

    classpath_search *rc = classpath_get_from_prchar(prchar_clsname);

    HEAP_FREE_DATA(prchar_clsname);

    return(rc);

} /* END of classpath_get_from_cp_entry_utf() */


/*!
 * @brief Free up @b CLASSPATH search result pointer
 *
 * This structure contains two pointers, one of which is always NULL.
 * Free the other one by attempting to free any non-NULL pointer, then
 * free the enclosing structure pointer.
 *
 * 
 * @param pcpsr Heap pointer containing @b CLASSPATH search result to
 *              be freed.
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
rvoid classpath_free_search_result(classpath_search *pcpsr)
{
    /* Nothing to do if NULL pointer */
    if (rnull == pcpsr)
    {
        return;
    }

    if (rnull != pcpsr->classfile_name)
    {
        HEAP_FREE_DATA(pcpsr->classfile_name);
    }

    if (rnull != pcpsr->jarfile_member_state)
    {
        HEAP_FREE_DATA(pcpsr->jarfile_member_state);
    }

    HEAP_FREE_DATA(pcpsr);
    return;

} /* END of classpath_free_search_result() */


/*!
 * @brief Shut down the @b CLASSPATH search area of the JVM model after
 * JVM execution.
 * 
 * @b Parameters: @link #rvoid rvoid@endlink
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
rvoid classpath_shutdown()
{
    ARCH_FUNCTION_NAME(classpath_shutdown);

    rint i;

    for (i = 0; i < classpath_list_len; i++)
    {
        HEAP_FREE_DATA(classpath_list[i]);
    }
    HEAP_FREE_DATA(classpath_list);

    classpath_list = (rchar **) rnull;
    classpath_list_len = 0;

    /* Declare this module uninitialized */
    jvm_classpath_initialized = rfalse;

    return;

} /* END of classpath_shutdown() */


/* EOF */
