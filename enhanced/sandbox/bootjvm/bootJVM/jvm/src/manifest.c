/*!
 * @file manifest.c
 *
 * @brief Read JAR manifest file.
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
ARCH_SOURCE_COPYRIGHT_APACHE(manifest, c,
"$URL$",
"$Id$");


#include "jvmcfg.h"
#include "heap.h" 
#include "exit.h"
#include "util.h"

/*!
 * @name Search a JAR manifest file or buffer image for a main
 * class attribute.
 *
 * This attribute MUST be all on one line and without any
 * line continuations breaking up the class name.  Look for
 * a line of text like this, where @b | indicates that the
 * following character is the first one on the line of text:
 *
 * @verbatim
      |
      |Main-Class: name.of.start.class\n
      |
  
   WITHOUT any line continuations,
  
      |
      |Main-Class: name.of.sta\n
      | rt.class\n
      |
  
   @endverbatim
 *
 * @todo  HARMONY-6-jvm-manifest.c-1 Although such a continuation
 *        is supported by the JAR file, this implementation does
 *        not support it (yet).  With this restriction, then
 *        if only one single space separates the name of the
 *        attribute and the class name, then since a line may be
 *        up to 72 characters long (that is
 *        @link #JVMCFG_JARFILE_MANIFEST_LINE_MAX
          JVMCFG_JARFILE_MANIFEST_LINE_MAX@endlink characters), then
 *        the class name may be 61 characters long.
 *
 *
 * @returns Heap pointer to null-terminated startup class name.
 *          Call HEAP_FREE_DATA() when done.  Returns
 *          @link #rnull rnull@endlink if startup class
 *          name was not found.
 *
 */

/*@{ */ /* Begin grouped definitions */

/*!
 * @brief Search a JAR manifest file
 *
 *
 * @param  mnfname   JAR manifest file name, /absolute/path/name
 *         
 */
rchar *manifest_get_main_from_file(rchar *mnfname)
{
    ARCH_FUNCTION_NAME(manifest_get_main_from_file);

    rvoid *mf;  /* Portability library does (FILE) part */

    mf = portable_fopen(mnfname, "r");

    if (rnull == mf)
    {
        return((rchar *) rnull);
    }

    rchar *mnfdata = HEAP_GET_DATA(JVMCFG_STDIO_BFR, rfalse);

    rint mclen =  portable_strlen(JVMCFG_JARFILE_MANIFEST_MAIN_CLASS);

    /*
     * Read until end of file or match located.
     *  \n is at the end of each bfr
     */
    while (mnfdata ==
           (rchar *) portable_fgets(mnfdata, JVMCFG_STDIO_BFR, mf))
    {
        /*
         * Scan for ^Main-Class: attribute name (text starting
         * at beginning of line)
         */
        if (0 != portable_strncmp(mnfdata,
                                  JVMCFG_JARFILE_MANIFEST_MAIN_CLASS,
                                  mclen))
        {
            continue;
        }

        /*
         * Attribute name found.
         *
         * Scan for first non-white character after attribute name.
         * This will be the start of the class name.
         */
        rint i;
        rint mdlen = portable_strlen(mnfdata);
        for (i = mclen; i < mdlen; i++)
        {
            /* if <b>white space</b> is rfalse */
            if (0 == portable_isspace((int) mnfdata[i]))
            {
                break; /* Found first non-white-space character */
            }
        }
        /* If nothing else, the \n at end of line is white space */

        /* But if somehow last line has no \n then check end of bfr */
        if (i == mdlen)
        {
            break;
        }

        /*
         * Class name found.
         *
         * Scan for first white character after class name.
         * This will be the end of the class name.
         */
        rint j;
        for (j = i; j < mdlen; j++)
        {
            /* if <b>white space</b> is @link #rtrue rtrue@endlink */
            if (0 != portable_isspace((int) mnfdata[j]))
            {
                break;  /* Found first white-space character */
            }
        }
        /* If nothing else, the \n at end of line is white space */

        /* But error if no class name */
        if (j == i)
        {
            break;
        }

        /* Allocate space for non-empty class name plus NUL byte */
        rchar *mnfresult = HEAP_GET_DATA(j - i + sizeof(rchar), rfalse);

        portable_memcpy(mnfresult, &mnfdata[i], j - i);
        mnfresult[j - i] = '\0';

        portable_fclose(mf);
        HEAP_FREE_DATA(mnfdata);
        return(mnfresult);
    }


    /* Do not process empty class name, declare error instead */
    portable_fclose(mf);
    HEAP_FREE_DATA(mnfdata);

    return((rchar *) rnull);

} /* END of manifest_get_main_from_file() */


/*!
 * @brief Search a buffer containing a JAR manifest image
 *
 *
 * It is @e assumed that the main class keyword is at the beginning of
 * the line as described above due to the use of portable_strstr() .
 * However, this is not checked.
 *
 * @todo  HARMONY-6-jvm-manifest.c-2 Check for the main class keyword
 *        being found at the beginning of the line, either the first
 *        character of the buffer or immediately following a newline.
 *
 * @warning Make <i>absolutely sure</i> that the final byte of the input
 *          parameter @b mbfr is a @b NUL character for proper string
 *          termination in
 *          @link portable_strstr() portable_strstr()@endlink .
 *          This will probably mean that the allocated buffer will have
 *          either (a) an extra byte containing NUL, or (b) its last
 *          byte will be converted from newline to NUL, or (c) append
 *          a NUL byte to the manifest file buffer when it is extracted
 *          from the Java archive.  The latter approach is actually
 *          implemented, so no further action is needed by the caller.
 *
 * @param  mbfr    JAR manifest file contained in a buffer
 *         
 * @param  mbfrlen Length of JAR manifest file buffer in bytes
 *         
 */
rchar *manifest_get_main_from_bfr(rchar *mbfr, rint mbfrlen)
{
    ARCH_FUNCTION_NAME(manifest_get_main_from_bfr);

    rchar *mclocation =
        portable_strstr(mbfr, JVMCFG_JARFILE_MANIFEST_MAIN_CLASS);

    /* If bfr does not contain keyword, then cannot contain class name*/
    if (rnull != mclocation)
    {
        return((rchar *) rnull);
    }

    rint mclen = portable_strlen(JVMCFG_JARFILE_MANIFEST_MAIN_CLASS);

    rint mcoffset = (mclocation - mbfr) / sizeof(rchar);

    /*
     * Attribute name found.
     *
     * Scan for first non-white character after attribute name.
     * This will be the start of the class name.
     */
    rint i;
    for (i = mclen + mcoffset; i < mbfrlen; i++)
    {
        /* if <b>white space</b> is rfalse */
        if (0 == portable_isspace((int) mbfr[i]))
        {
            break; /* Found first non-white-space character */
        }
    }
    /* If nothing else, the \n at end of line is white space */

    /* But if somehow last line has no \n then check end of bfr */
    if (mbfrlen == i)
    {
        return((rchar *) rnull);
    }

    /*
     * Class name found.
     *
     * Scan for first white character after class name.
     * This will be the end of the class name.
     */
    rint j;
    for (j = i; j < mbfrlen; j++)
    {
        /* if <b>white space</b> is @link #rtrue rtrue@endlink */
        if (0 != portable_isspace((int) mbfr[j]))
        {
            break;  /* Found first white-space character */
        }
    }
    /* If nothing else, the \n at end of line is white space */

    /* But error if no class name */
    if (i == j)
    {
        return((rchar *) rnull);
    }

    /* Allocate space for non-empty class name plus NUL byte */
    rchar *mnfresult = HEAP_GET_DATA(j - i + sizeof(rchar), rfalse);

    portable_memcpy(mnfresult, &mbfr[i], j - i);
    mnfresult[j - i] = '\0';

    return(mnfresult);

} /* END of manifest_get_main_from_bfr() */


/*@} */ /* End of grouped definitions */

/* EOF */
