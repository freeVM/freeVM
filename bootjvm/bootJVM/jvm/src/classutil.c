/*!
 * @file classutil.c
 *
 * @brief Utility and glue functions for manipulating Java classes.
 *
 *
 * @internal Due to the fact that the implementation of the Java class
 *           and the supporting rclass structure is deeply embedded in
 *           the core of the development of this software, this file
 *           has some contents that come and go during development.
 *           Some functions get staged here before deciding where they
 *           @e really go; some are interim functions for debugging,
 *           some were glue that eventually went away.  Be careful to
 *           remove prototypes to such functions from the appropriate
 *           header file.
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
ARCH_SOURCE_COPYRIGHT_APACHE(classutil, c,
"$URL$",
"$Id$");


#include "jvmcfg.h"
#include "cfmacros.h"
#include "classfile.h"
#include "field.h"
#include "jvm.h"
#include "jvmclass.h"
#include "linkage.h"
#include "method.h"
#include "nts.h"
#include "utf.h"
#include "util.h"


/*!
 * @name Examine class contents for object hierarchy relationships.
 *
 *
 * If not already present, all classes examined will be loaded here
 * when their constant_pool[] or interfaces[] information is needed.
 *
 * @note <em>There is little runtime check performed</em> to check
 *       whether the input class indices represent classes, interfaces,
 *       or arrays.  Most of this must be done by the invoking function.
 *       For numerous examples,
 *       see @link #opcode_run() opcode_run()@endlink.
 *
 *
 * @param clsidx1  Class table index of a class to compare against the
 *                 second one, namely @c @b clsidx2 .
 *
 * @param clsidx2  Class table index of class to compare @c @b clsidx1
 *                 against.
 *
 */

/*@{ */ /* Begin grouped definitions */

/*!
 * @brief Check if class @b clsidx1 is a subclass of class @b clsidx2 .
 *
 * This effectively asks the OO hierarchy question, "Is @c @b clsidx1
 * a @c @b clsidx2 ?" For an @link #rtrue rtrue@endlink comparison,
 * class @c @b clsidx1 must be either the same class as class
 * @c @b clsidx2 or a subclass of it.  In other words, the true
 * comparison affirms "@c @b clsidx1 <em>is a</em> @c @b clsidx2 ."
 *
 * The superclass of all array types @e must be java.lang.Object
 * per JVM spec section 2.15.  (They "may" be assigned this type, if
 * any at all, but in this implementation, they @e always are so that
 * they have a definite objct type.)
 *
 *
 * @returns @link #rtrue rtrue@endlink if @c @b clsidx1 is the same
 *          class or a subclass of @c @b clsidx2 .
 *          If either input parameter is
 *          @link #jvm_class_index_null jvm_class_index_null@endlink,
 *          then the result is @link #rfalse rfalse@endlink.
 *          If both input parameters are identical, then result
 *          is @link #rtrue rtrue@endlink.
 *
 * @see classutil_direct_superclass_parent_object_of()
 * @see classutil_class_has_a_method()
 * @see classutil_class_has_an_interface_method()
 *
 */
rboolean classutil_class_is_a(jvm_class_index clsidx1,
                              jvm_class_index clsidx2)
{
    ARCH_FUNCTION_NAME(classutil_class_is_a);

    /* Disallow comparison with null class */
    if ((jvm_class_index_null == clsidx1) ||
        (jvm_class_index_null == clsidx2))
    {
        return(rfalse);
    }

    /* Comparison is true if both are the same class */
    if (clsidx1 == clsidx2)
    {
        return(rtrue);
    }

    /*
     * Check arrays for a superclass of _only_ java.lang.Object
     */
    if (CLASS(clsidx1).status & CLASS_STATUS_ARRAY)
    {
        return((clsidx2 == pjvm->class_java_lang_Object)
               ? rtrue
               : rfalse);
    }

    /*
     * Scan for superclasses of @c @b clsidx1
     */
    jvm_class_index clsidxCLS = clsidx1;

    /*
     * Will not fail on entering loop because of @c @b clsidx1 test
     * above.  This is only possible after the first time through.
     */
    while (jvm_class_index_null != clsidxCLS)
    {
        ClassFile *pcfs = CLASS_OBJECT_LINKAGE(clsidxCLS)->pcfs;

        /*
         * If found top of hierarchy, namely java.lang.Object, then
         * search failed.
         */
        if (jvm_constant_pool_index_null == pcfs->super_class)
        {
            break;
        }

        /*
         * @internal Could use class_load_from_cp_entry() instead of
         *           using class_find_by_cp_entry().  If this were done,
         *           then notice that if this request loaded a class, it
         *           would also load @e all of its superclasses.
         *           Therefore, if it is a superclass that matched,
         *           no loading would occur later, but that previously
         *           loaded superclass would be found and returned
         *           immediately.
         *
         */
        clsidxCLS =
            class_find_by_cp_entry(
                PTR_CP1_CLASS_NAME_MEMALIGN(pcfs, pcfs->super_class));

        if (clsidx2 == clsidxCLS)
        {
            return(rtrue);
        }
    }

    /* Superclass of @c @b clsidx1 not found to be @c @b clsidx2 */
    return(rfalse);

} /* END of classutil_class_is_a() */


/*!
 * @brief Check if class of object @b objhash1 is a subclass
 * of class @b clsidx2 .
 *
 *
 * @returns @link #rtrue rtrue@endlink if class of @c @b objhash1 is
 *          the same class or a subclass of @c @b clsidx2 .
 *          If input parameter @b objhash1 is
 *          @link #jvm_object_hash_null jvm_object_hash_null@endlink, or
 *          if input parameter @b clsidx2 is
 *          @link #jvm_class_index_null jvm_class_index_null@endlink,
 *          then the result is @link #rfalse rfalse@endlink.
 *          If the class of @b objhash1 is exactly the same as
 *          @b clsidx2, then result is @link #rtrue rtrue@endlink.
 *
 */
rboolean classutil_object_is_a(jvm_object_hash objhash1,
                               jvm_class_index clsidx2)
{
    ARCH_FUNCTION_NAME(classutil_object_is_a);

    /* Disallow comparison with null class */
    if ((jvm_object_hash_null == objhash1) ||
        (jvm_class_index_null == clsidx2))
    {
        return(rfalse);
    }

    /* Retrieve class of requested object */
    jvm_class_index clsidx1 = OBJECT_CLASS_LINKAGE(objhash1)->clsidx;

    /* Analyze class of requested object */
    return(classutil_class_is_a(clsidx1, clsidx2));

} /* END of classutil_object_is_a() */


/*!
 * @brief Check if class @b clsidx1 implements interface @b clsidx2 .
 *
 * For an @link #rtrue rtrue@endlink comparison, the class @c @b clsidx1
 * must implement the interface defined by class @c @b clsidx2 .
 *
 *
 * @returns @link #rtrue rtrue@endlink if @c @b clsidx1 implements
 *          the interface defined in class index @c @b clsidx2 .
 *          If either input parameter is
 *          @link #jvm_class_index_null jvm_class_index_null@endlink,
 *          then the result is @link #rfalse rfalse@endlink.
 *          If both input parameters are identical, then result
 *          is @link #rfalse rfalse@endlink.
 *
 */
rboolean classutil_class_implements_interface(jvm_class_index clsidx1,
                                              jvm_class_index clsidx2)
{
    ARCH_FUNCTION_NAME(classutil_class_implements_interface);

    ClassFile *pcfs1, *pcfs2;
    u2         ifidx;

    /* Disallow comparison with null class */
    if ((jvm_class_index_null == clsidx1) ||
        (jvm_class_index_null == clsidx2))
    {
        return(rfalse);
    }

    /*
     * Check arrays for implementation of required interfaces
     */
    if (CLASS(clsidx1).status & CLASS_STATUS_ARRAY)
    {
        return(classutil_interface_implemented_by_arrays(clsidx2));
    }

    /*
     * Comparison is false if both are the same class, i.e. there is
     * no interface involved.
     */
    if (clsidx1 == clsidx2)
    {
        return(rfalse);
    }

    /*
     * Cannot be true if comparator class index IS an interface
     * or if comparend class index is NOT an interface.
     */
    pcfs1 = CLASS_OBJECT_LINKAGE(clsidx1)->pcfs;
    pcfs2 = CLASS_OBJECT_LINKAGE(clsidx2)->pcfs;

    if ( (pcfs1->access_flags & ACC_INTERFACE) ||
        (!(pcfs2->access_flags & ACC_INTERFACE)))
    {
        return(rfalse);
    }

    /*
     * Scan for superclasses of @c @b clsidx2 .
     * If no interfaces, then this loop is skipped.
     */
    for (ifidx = 0; ifidx < pcfs1->interfaces_count; ifidx++)
    {
        jvm_class_index clsidxINTFC =
            class_load_from_cp_entry_utf(pcfs1->constant_pool[ifidx],
                                         rfalse,
                                         (jint *) rnull);

        /*
         * Check if this class index matches requested interface
         * or if super-hierarchy matches.
         *
         */
        if ((clsidx2 == clsidxINTFC) ||
           (rtrue == classutil_class_is_a(clsidx2, clsidxINTFC)))
        {
            return(rtrue);
        }
    }

    /* Class of @c @b clsidx1 not found to implement @c @b clsidx2 */
    return(rfalse);

} /* END of classutil_class_implements_interface() */


/*!
 * @brief Check if class @b clsidx1 is a superinterface of class or
 * interface @b clsidx2 .
 *
 * For an @link #rtrue rtrue@endlink comparison, the class index
 * @c @b clsidx1 must be a valid superinterface of class @c @b clsidx2
 * per spec section 2.13.2 .
 *
 *
 * @returns @link #rtrue rtrue@endlink if @c @b clsidx1 is a valid
 *          superinterface of class @c @b clsidx2 .
 *          If either input parameter is
 *          @link #jvm_class_index_null jvm_class_index_null@endlink,
 *          then the result is @link #rfalse rfalse@endlink.
 *          If both input parameters are identical, then result
 *          is @link #rtrue rtrue@endlink.
 *
 */
rboolean classutil_class_is_superinterface_of(jvm_class_index clsidx1,
                                              jvm_class_index clsidx2)
{
    ARCH_FUNCTION_NAME(classutil_class_is_superinterface_of);

    ClassFile *pcfs1, *pcfs2;
    u2         ifidx;

    /* Disallow comparison with null class */
    if ((jvm_class_index_null == clsidx1) ||
        (jvm_class_index_null == clsidx2))
    {
        return(rfalse);
    }

    /*
     * Arrays are not interfaces and so cannot be superinterfaces
     */
    if (CLASS(clsidx1).status & CLASS_STATUS_ARRAY)
    {
        return(rfalse);
    }

    /* Can only be true if comparator class index is an interface */
    pcfs1 = CLASS_OBJECT_LINKAGE(clsidx1)->pcfs;
    if (!(pcfs1->access_flags & ACC_INTERFACE))
    {
        return(rfalse);
    }

    /* Comparison is false if both are the same class */
    if (clsidx1 == clsidx2)
    {
        return(rfalse);
    }

    /*
     * Scan for superinterfaces of @c @b clsidx2 .
     *
     * If starting at top of hierarchy, namely java.lang.Object class
     * or highest-level interface, then search cannot succeed.
     */
    pcfs2 = CLASS_OBJECT_LINKAGE(clsidx2)->pcfs;

    if (jvm_constant_pool_index_null == pcfs2->super_class)
    {
        return(rfalse);
    }

    /* If no interfaces, then this loop is skipped */
    for (ifidx = 0; ifidx < pcfs2->interfaces_count; ifidx++)
    {
        /* Since 'clsidx1' is valid, this really means 'find_by' */
        jvm_class_index clsidxINTFC =
            class_load_from_cp_entry_utf(
                pcfs2->constant_pool[pcfs2->interfaces[ifidx]],
                rfalse,
                (jint *) rnull);

        /*
         * Check if this class index matches requested interface
         * or if super-hierarchy matches.
         *
         */
        /* Check if this class index matches requested interface */
        if ((clsidx1 == clsidxINTFC) ||
            (rtrue == classutil_class_is_a(clsidxINTFC, clsidx1)))
        {
            return(rtrue);
        }
    }

    /* @c @b clsidx1 not found to be superinterface of @c @b clsidx2 */
    return(rfalse);

} /* END of classutil_class_is_superinterface_of() */


/*!
 * @brief Check if class/interface @b clsidx1 is in the same
 * runtime package as class/interface @b clsidx2 .
 *
 * This function implements the package visibility logic
 * defined in the JVM spec section 5.4.4 .
 *
 *
 * @returns @link #rtrue rtrue@endlink if @c @b clsidx1 is in the
 *          same runtime package as @c @b clsidx2 , otherwise
 *          @link #rfalse rfalse@endlink.
 *
 */

rboolean classutil_class_same_package_as(jvm_class_index clsidx1,
                                         jvm_class_index clsidx2)
{
    ARCH_FUNCTION_NAME(classutil_same_package_as);

    /* Disallow navigation of null classes */
    if ((jvm_class_index_null == clsidx1) ||
        (jvm_class_index_null == clsidx1))
    { 
        return(rfalse);
    }

    /* Class is visible if it is in the same class */
    if (clsidx1 ==  clsidx2)
    {
        return(rtrue);
    }

    /*
     * @todo   HARMONY-6-jvm-classutil.c-3 Is there anything here that
     *         needs to be done specifically for arrays like is done
     *         in other functions in this source file?
     */

    ClassFile *pcfs1 = CLASS_OBJECT_LINKAGE(clsidx1)->pcfs;
    ClassFile *pcfs2 = CLASS_OBJECT_LINKAGE(clsidx2)->pcfs;

    /*
     * Class is accessible if it is in the same runtime package.
     * This may be discovered by comparing the first part of the
     * path names (before final path delimiter) for equality.
     */

    CONSTANT_Utf8_info *putfname1 = PTR_CP1_CLASS_NAME(pcfs1,
                                                     pcfs1->this_class);

    CONSTANT_Utf8_info *putfname2 = PTR_CP1_CLASS_NAME(pcfs2,
                                                     pcfs2->this_class);

    /* Truncate class names from both strings, compare remainder */
    rchar *prcharclsfmt1 = utf_utf2prchar(putfname1);
    rchar *prcharclsfmt2 = utf_utf2prchar(putfname2);

    /* Strip class formatting, if any, before comparison */
    /*!
     * @todo   HARMONY-6-jvm-classutil.c-2 Is this stripping really
     *         necessary?  Or are the input parameters going to always
     *         be guaranteed to be @e only class names without
     *         any formatting?
     */
    rchar *prcharname1 =
        nts_prchar2prchar_unformatted_classname(prcharclsfmt1);
    rchar *prcharname2 =
        nts_prchar2prchar_unformatted_classname(prcharclsfmt2);

    HEAP_FREE_DATA(prcharclsfmt1);
    HEAP_FREE_DATA(prcharclsfmt2);

    /* Locate end of path component, if any */
    rchar *pnameonly1 = portable_strrchr(prcharname1,
                                     CLASSNAME_INTERNAL_DELIMITER_CHAR);
    rchar *pnameonly2 = portable_strrchr(prcharname2,
                                     CLASSNAME_INTERNAL_DELIMITER_CHAR);

    /* Check for two unpackaged classes, accessible if so */
    if ((rnull == pnameonly1) && (rnull == pnameonly2))
    {
        HEAP_FREE_DATA(prcharname1);
        HEAP_FREE_DATA(prcharname2);
        return(rtrue);
    }

    /* Not accessible if only one is unpackaged */
    if (((rnull == pnameonly1) && (rnull != pnameonly2)) ||
        ((rnull != pnameonly1) && (rnull == pnameonly2)))
    {
        HEAP_FREE_DATA(prcharname1); /* Ignores NULL pointers */
        HEAP_FREE_DATA(prcharname2); /* Ignores NULL pointers */
        return(rfalse);
    }

    /*
     * Both classes area packaged, so compare null-terminated
     * package name strings
     */
    *pnameonly1 = '\0';
    *pnameonly2 = '\0';

    rboolean rc = (0 == portable_strcmp(prcharname1, prcharname2))
                  ? rtrue
                  : rfalse;

    HEAP_FREE_DATA(prcharname1);
    HEAP_FREE_DATA(prcharname2);
    return(rc);

} /* END of classutil_class_same_package_as() */


/*!
 * @brief Check if class/interface @b clsidx1 is accessible
 * to class/interface @b clsidx2 .
 *
 * This function implements the class accessibility logic
 * defined in the JVM spec section 5.4.4 .
 *
 * For an @link #rtrue rtrue@endlink comparison, the class @c @b clsidx1
 * must either be:
 *
 * <ul>
 * <li>
 *  <b>(1)</b> a @c @b public class, that is,
 *             @link #ACC_PUBLIC ACC_PUBLIC@endlink, or...
 * </li>
 * <li>
 *  <b>(2)</b> in the same runtime package as @c @b clsidx2 .
 * </li>
 * </ul>
 *
 *
 * @returns @link #rtrue rtrue@endlink if any of the above conditions
 *          are true, otherwise @link #rfalse rfalse@endlink.
 *
 */

rboolean classutil_class_is_accessible_to(jvm_class_index clsidx1,
                                          jvm_class_index clsidx2)
{
    ARCH_FUNCTION_NAME(classutil_class_is_accessible_to);

    /* Disallow navigation of null classes */
    if ((jvm_class_index_null == clsidx1) ||
        (jvm_class_index_null == clsidx2))
    { 
        return(rfalse);
    }

    ClassFile *pcfs1 = CLASS_OBJECT_LINKAGE(clsidx1)->pcfs;

    /* Class is accessible if it is public or the same class */
    if ((clsidx1 ==  clsidx2) || (ACC_PUBLIC & pcfs1->access_flags))
    {
        return(rtrue);
    }

    /* Check if they are in the same package and report result */
    return(classutil_class_same_package_as(clsidx1, clsidx2));

} /* END of classutil_class_is_accessible_to() */


/*!
 * @brief Check if interface class @b clsidx1 is one of
 *        the interfaces that must be implemented by arrays.
 *
 * For an @link #rtrue rtrue@endlink comparison, the class @c @b clsidx1
 * must be one of the interfaces defined in the JVM spec section 2.15
 * that are required to be implemented for all array types.
 *
 *
 * @returns @link #rtrue rtrue@endlink if @c @b clsidx1 is one of
 *          the valid interfaces available to arrays.
 *          If the input parameter is
 *          @link #jvm_class_index_null jvm_class_index_null@endlink,
 *          then the result is @link #rfalse rfalse@endlink.
 *
 *
 * @todo   HARMONY-6-jvm-classutil.c-1 Need to verify that the array
 *         design can properly implement the 'clone' method.
 *
 */
rboolean classutil_interface_implemented_by_arrays(
                                                jvm_class_index clsidx1)
{
    ARCH_FUNCTION_NAME(classutil_interface_implemented_by_arrays);

    /* Disallow evaluation of null class */
    if (jvm_class_index_null == clsidx1)
    {
        return(rfalse);
    }

    if (clsidx1 == 
       class_load_from_prchar(JVMCLASS_JAVA_LANG_CLONEABLE,
                              rfalse,
                              (jint *) rnull))
    {
        return(rtrue);
    }
    else
    if (clsidx1 ==
        class_load_from_prchar(JVMCLASS_JAVA_IO_SERIALIZABLE,
                               rfalse,
                               (jint *) rnull))
    {
        return(rtrue);
    }
    return(rfalse);

} /* END of classutil_interface_implemented_by_arrays() */

/*@} */ /* End of grouped definitions */


/*!
 * @name Check if field/method reference @c @b fldref1 / @c @b mthref1
 * is accessible to class @b clsidx2 .
 *
 * These functions implement the field and method accessibility logic
 * defined in the JVM spec section 5.4.4 .
 *
 * For an @link #rtrue rtrue@endlink comparison,
 * @c @b fldref1 / @c @b mthref1 must be one of:
 *
 * <ul>
 * <li>
 *  <b>(1)</b> a @c @b public field/method, that is,
 *             @link #ACC_PUBLIC ACC_PUBLIC@endlink, or...
 * </li>
 * <li>
 *  <b>(2)</b> a @c @b protected field/method, that is,
 *             @link #ACC_PROTECTED ACC_PROTECTED@endlink, whose class
 *             is either @c @b clsidx2 itself or a
 *             superclass of @c @b clsidx2 , or...
 * </li>
 * <li>
 *  <b>(3)</b> a @c @b protected field/method, that is,
 *             @link #ACC_PROTECTED ACC_PROTECTED@endlink, whose class
 *             is in the same runtime package as @c @b clsidx2 .
 * </li>
 * <li>
 *  <b>(4)</b> a package private field/method (neither @c @b public
 *             nor @c @b protected nor @c @b private ) whose class
 *             is in the same runtime package as @c @b clsidx2 .
 * </li>
 * <li>
 *  <b>(5)</b> a @c @b private field/method, that is,
 *             @link #ACC_PRIVATE ACC_PRIVATE@endlink,
 *             in class @c @b clsidx2 .
 * </li>
 * </ul>
 *
 * @warning This final condition is @e not testable in this function
 *          since @c @b clsidx1 cannot access @c @b private members
 *          of @c @b clsidx2 .  This condition will always return
 *          @link #rfalse rfalse@endlink.
 *
 * @param fldref1  Field reference to a class containing a field
 *                 to be examined.
 *
 * @param mthref1  Method reference to a class containing a method
 *                 to be examined.
 *
 * @param clsidx1  Class table index of class containing @c @b fldref1
 *                 or @c @b mthref1 .
 *
 * @param access_flags1  Access flags for member @c @b fldref1 or
 *                 @c @b mthref1 .
 *
 * @param clsidx2  Class table index of class to compare @c @b fldref1
 *                 or @c @b mthref1 against.
 *
 *
 * @returns @link #rtrue rtrue@endlink if any of the above conditions
 *          are true, otherwise @link #rfalse rfalse@endlink.
 *
 */

/*@{ */ /* Begin grouped definitions */

/*!
 * @brief Common code for checking if field or method is accessible
 * to a certain class.
 *
 * 
 */
static rboolean classutil_member_is_accessible_to(
           jvm_class_index clsidx1,
           jvm_access_flags access_flags1,
           jvm_class_index clsidx2)
{
    ARCH_FUNCTION_NAME(classutil_member_is_accessible_to);

    /* Disallow navigation of null classes */
    if ((jvm_class_index_null == clsidx1) ||
        (jvm_class_index_null == clsidx2))
    {
        return(rfalse);
    }

    /*
     * Field/Method is accessible if it is the same class
     * (Not explicitly stated in requirement, but
     * implied throughout by its structure.)
     */
    if (clsidx1 ==  clsidx2)
    {
        return(rtrue);
    }

    /* Field/Method is accessible if it is public */
    if (ACC_PUBLIC & access_flags1)
    {
        return(rtrue);
    }

    /*
     * Field/Method is accessible if it is protected and its class
     * is either clsidx2 itself or a superclass of clsidx2
     * (that is, clsidx2 is the same as or a subclass of
     * the class of fldref1) or it is in the same runtime
     * package as clsidx2 .
     */
    if (ACC_PROTECTED & access_flags1)
    {
        if (rtrue == classutil_class_is_a(clsidx2, clsidx1))
        {
            return(rtrue);
        }

        if (rtrue == classutil_class_same_package_as(clsidx1, clsidx2))
        {
            return(rtrue);
        }
    }

    /*
     * Field/Method is accessible if it is package private (neither
     * public nor protected nor private) and it is in the same runtime
     * package as clsidx2 .
     */
    if (!((ACC_PUBLIC | ACC_PROTECTED | ACC_PRIVATE) & access_flags1))
    {
        if (rtrue == classutil_class_same_package_as(clsidx1, clsidx2))
        {
            return(rtrue);
        }
    }

    /*
     * Field/Method is accessible if it is private and its class
     * is clsidx2 .  THIS MUST BE FALSE PER FUNCTION PROTOTYPE
     * BECAUSE 'clsidx1' CANNOT ACCESS ANYTHING 'private' IN 'clsidx2'
     */
    return(rfalse);

} /* END of classutil_member_is_accessible_to() */


/*!
 * @brief Check if field @b fldref1 is accessible to class @b clsidx2 .
 *
 */
rboolean classutil_field_is_accessible_to(
                                        CONSTANT_Fieldref_info *fldref1,
                                        jvm_class_index         clsidx2)
{
    ARCH_FUNCTION_NAME(classutil_field_is_accessible_to);

    /* Retrieve access_flags for this field */
    jvm_class_index clsidx1 = fldref1->LOCAL_Fieldref_binding.clsidxJVM;

    /*
     * @todo   HARMONY-6-jvm-classutil.c-4 Does there need to be a
     *         possible linkage_resolve_class() here?  Could
     *         @c @b clsidx1 possibly be
     *         @link #jvm_class_index_null jvm_class_index_null@endlink
     *         here?
     */
    ClassFile      *pcfs    = CLASS_OBJECT_LINKAGE(clsidx1)->pcfs;

    jvm_field_lookup_index
                    fluidx1 = fldref1->LOCAL_Fieldref_binding.fluidxJVM;

    rboolean        oiflag1 = fldref1->LOCAL_Fieldref_binding.oiflagJVM;

    field_info     *pfld    = pcfs
                              ->fields
                                [(rtrue == oiflag1)
                                 ? CLASS(clsidx1)
                                     .object_instance_field_lookup
                                      [fluidx1]

                                 : CLASS(clsidx1)
                                     .class_static_field_lookup
                                      [fluidx1]
                                ];

    jvm_access_flags
              access_flags1 = pfld->access_flags;

    /* Process common portion */
    return(classutil_member_is_accessible_to(clsidx1,
                                             access_flags1,
                                             clsidx2));

} /* END of classutil_field_is_accessible_to() */


/*!
 * @brief Check if method @b mthref1 is accessible to class @b clsidx2 .
 *
 */
rboolean classutil_method_is_accessible_to(
                                       CONSTANT_Methodref_info *mthref1,
                                       jvm_class_index          clsidx2)
{
    ARCH_FUNCTION_NAME(classutil_method_is_accessible_to);

    /* Retrieve access_flags for this method */
    jvm_class_index clsidx1 = mthref1
                              ->LOCAL_Methodref_binding
                                .clsidxJVM;

    /*
     * @todo   HARMONY-6-jvm-classutil.c-5 Does there need to be a
     *         possible linkage_resolve_class() here?  Could
     *         @c @b clsidx1 possibly be
     *         @link #jvm_class_index_null jvm_class_index_null@endlink
     *         here?
     */
    ClassFile      *pcfs    = CLASS_OBJECT_LINKAGE(clsidx1)->pcfs;

    jvm_method_index
                    mthidx1 = mthref1
                              ->LOCAL_Methodref_binding
                                .mthidxJVM;

    method_info    *pmth    = pcfs->methods[mthidx1];

    jvm_access_flags
              access_flags1 = pmth->access_flags;

    /* Process common portion */
    return(classutil_member_is_accessible_to(clsidx1, 
                                             access_flags1,
                                             clsidx2));

} /* END of classutil_method_is_accessible_to() */


/*!
 * @brief Check if interface method @b mthref1 is accessible
 * to class @b clsidx2 .
 *
 * @bug HARMONY-6-classutil.c-1001 Probably need to adjust
 *      classutil_member_is_accessible_to() to traverse interfaces
 *      to check accessibility of interface methods.  Could it be
 *      that this function() should re-implement that one, but check
 *      both class interitance and interface inheritance?  Should
 *      that one consider both sometimes or always?  Perhaps with
 *      an input parameter flag (suggest CONSTANT_xxx tag)?
 */
rboolean classutil_interface_method_is_accessible_to(
                              CONSTANT_InterfaceMethodref_info *mthref1,
                              jvm_class_index                   clsidx2)
{
    ARCH_FUNCTION_NAME(classutil_interface_method_is_accessible_to);

    /* Retrieve access_flags for this interface method */

    jvm_class_index clsidx1 = mthref1
                                ->LOCAL_InterfaceMethodref_binding
                                  .clsidxJVM;

    /*
     * @todo   HARMONY-6-jvm-classutil.c-6 Does there need to be a
     *         possible linkage_resolve_class() here?  Could
     *         @c @b clsidx1 possibly be
     *         @link #jvm_class_index_null jvm_class_index_null@endlink
     *         here?
     */
    ClassFile      *pcfs    = CLASS_OBJECT_LINKAGE(clsidx1)->pcfs;

    jvm_method_index
                    mthidx1 = mthref1
                                ->LOCAL_InterfaceMethodref_binding
                                  .mthidxJVM;

    method_info    *pmth    = pcfs->methods[mthidx1];

    jvm_access_flags
              access_flags1 = pmth->access_flags;

    /* Process common portion */
    return(classutil_member_is_accessible_to(clsidx1, 
                                             access_flags1,
                                             clsidx2));

} /* END of classutil_interface_method_is_accessible_to() */


/*@} */ /* End of grouped definitions */


/*!
 * @brief Locate an object of class @b clsidx1 that is a direct
 * superclass parent object (any ancestor) of object @b objhash2 .
 *
 * For choosing @e any superclass object, this effectively performs
 * the OO hierarchy operation, "Find the parent object of @c @b objhash2
 * that is either a @c @b clsidx1 or one of its superclasses."
 *
 * Another way to put this OO hierarchy operation is, "Find the
 * parent object of @c @b objhash2 whose class is @e exactly
 * a @c @b clsidx1 , thus is @e neither a subclass @e nor a superclass
 * of @c @b clsidx1 ."
 *
 * The superclass of all array types @e must be java.lang.Object
 * per JVM spec section 2.15.  (They "may" be assigned this type, if
 * any at all, but in this implementation, they @e always are so that
 * they have a definite objct type.)
 *
 *
 * @param clsidx1   Class table index of the class of an object that is
 *                  a parent of @c @b objhash2 .
 *
 * @param objhash2  Object for which to search for a parent of class
 *                  @c @b clsidx1 .
 *
 *
 * @returns matching parent object hash of @c @b objhash2 that is a
 *          class @c @b clsidx1 , but is @e not either a subclass
 *          of @c @b clsidx1 @e nor a superclass of @c @b clsidx1 .
 *          If input parameter @c @b clsidx1 is
 *          @link #jvm_class_index_null jvm_class_index_null@endlink or
 *          if input parameter @c @b objhash2 is
 *          @link #jvm_object_hash_null jvm_object_hash_null@endlink,
 *          then the result is
 *          @link #jvm_object_hash_null jvm_object_hash_null@endlink.
 *          If @c @b objhash2 is exactly of class @c @b clsidx1, then
 *          result is @c @b objhash2 itself.
 *
 *
 * @see classutil_class_is_a()
 * @see classutil_class_has_a_field()
 * @see classutil_class_has_a_method()
 * @see classutil_class_has_an_interface_method()
 *
 */
jvm_object_hash classutil_direct_superclass_parent_object_of(
                    jvm_class_index clsidx1,
                    jvm_object_hash objhash2)
{
    ARCH_FUNCTION_NAME(classutil_direct_superclass_parent_object_of);

    /* Disallow trivial class and trivial object cases */
    if ((jvm_class_index_null == clsidx1) ||
        (jvm_object_hash_null == objhash2))
    {
        return(jvm_object_hash_null);
    }

    /* Extract class of requested object */
    jvm_class_index clsidx2 = OBJECT_CLASS_LINKAGE(objhash2)->clsidx;

    /* Disallow comparison with null class (should never happen) */
    if (jvm_class_index_null == clsidx2)
    {
/*NOTREACHED*/
        return(jvm_object_hash_null);
    }

    /*
     * Check arrays for a superclass of _only_ java.lang.Object
     */
    if (CLASS(clsidx2).status & CLASS_STATUS_ARRAY)
    {
        return((clsidx1 == pjvm->class_java_lang_Object)

                 /* Namely, java.lang.Object */
               ? OBJECT(objhash2).objhash_super_class

                 /* Namely, "you can't do this" */
               : jvm_object_hash_null);
    }

    /* Comparison is true if object class is same as requested class */
    if (clsidx1 == clsidx2)
    {
        return(objhash2);
    }

    /*
     * If any object of class clsidx1 is not accessible
     * to object objhash2, then the inquiry fails. 
     */
    if (rfalse == classutil_class_is_accessible_to(clsidx1, clsidx2))
    {
        return(jvm_object_hash_null);
    }

    /*
     * Scan for superclasses of @c @b objhash2
     */
    jvm_object_hash objhashSUPER = objhash2;

    /*
     * Will never fail while() condition on first iteration unless
     * searching for superclass of java.lang.Object.
     *
     * If found top of hierarchy during search, namely java.lang.Object,
     * then search fails on second and further iterations.
     */
    while (jvm_object_hash_null !=
           (objhashSUPER = OBJECT(objhashSUPER).objhash_super_class))
    {

        jvm_class_index clsidxCLS =
            OBJECT_CLASS_LINKAGE(objhashSUPER)->clsidx;

        ClassFile *pcfs = CLASS_OBJECT_LINKAGE(clsidxCLS)->pcfs;

        /*
         * @internal Could use class_load_from_cp_entry() instead of
         *           using class_find_by_cp_entry().  If this were done,
         *           then notice that if this request loaded a class, it
         *           would also load @e all of its superclasses.
         *           Therefore, if it is a superclass that matched,
         *           no loading would occur later, but that previously
         *           loaded superclass would be found and returned
         *           immediately.
         *
         */
        clsidxCLS =
            class_find_by_cp_entry(
                PTR_CP1_CLASS_NAME_MEMALIGN(pcfs, pcfs->this_class));

        if (clsidx1 == clsidxCLS)
        {
            return(objhashSUPER);
        }
    }

    /*
     * Superclass of @c @b objhash2 was
     * not found to be a @c @b clsidx1
     */
    return(jvm_object_hash_null);

} /* END of classutil_direct_superclass_parent_object_of() */


/*!
 * @name Locate in an object or one of its parent objects the class
 * that <em>has a</em> member field, method, or interface method.
 *
 * For object @b objhash1 or and of its parents, this effectively
 * performs the OO hierarchy operation, "Find the object, either
 * @c @b objhash1 or one of its parent objects, that <em>has a</em>
 * field/method/interface method @c @b fldref2 / @c @b mthref2 as
 * set forth by @c @b pcfs2 that is (optionally) accessible to
 * @c @b objhash1 ."
 *
 * The superclass of all array types @e must be java.lang.Object
 * per JVM spec section 2.15.  (They "may" be assigned this type, if
 * any at all, but in this implementation, they @e always are so that
 * they have a definite objct type.)
 *
 *
 * @param rc[out]      Result of search.  Passed out through this
 *                     structure pointer so both the class index and
 *                     the field or method index may be easily passed
 *                     out. Normally return the matching parent class of
 *                     @c @b objhash1 that has a field/method/interface
 *                     method @c @b fldref2 / @c @b mthref2 as defined
 *                     in @c @b pcfs2 .  If input parameter
 *                     @c @b objhash1 is @link #jvm_object_hash_null
                       jvm_object_hash_null@endlink or if input
 *                     parameter @c @b fldref2 / @c @b mthref2 is a
 *                     null pointer, or similarly if input parameter
 *                     @c @b pcfs2 is a null pointer, then the result
 *                     is @link #jvm_class_index_null
                       jvm_class_index_null@endlink and
 *                     @link #jvm_field_index_bad
                       jvm_field_index_bad@endlink /
 *                     @link #jvm_method_index_bad
                       jvm_method_index_bad@endlink.  If @c @b objhash1
 *                     contains @c @b fldref2 / @c @b mthref2 itself,
 *                     then result is @c @b objhash1 directly.
 *
 * @param objhash1[in] Object for which to search that contains field
 *                     / method @c @b fldref2 / @c @b mthref2 .
 *
 * @param clsidx1[in]  Class index of @c @b objhash1 for which to
 *                     search that contains field / method
 *                     @c @b fldref2 / @c @b mthref2 .
 *
 * @param pcfs2[in]    ClassFile structure containing field or method
 *                     or interface method reference.
 *
 * @param fldref2[in]  Field reference from @c @b pcfs2 to locate in
 *                     @c @b objhash1 or one of its parent objects.
 *
 * @param mthref2[in]  Method / interface method reference from
 *                     @c @b pcfs2 to locate in @c @b objhash1 or
 *                     one of its parent objects.
 *
 * @param name_and_type_index2[in]
                       Name and type index from either
 *                     method reference or interface method reference,
 *                     whichever is available to calling function.
 *
 * @param isaccessible[in]
                       When @link #rtrue rtrue@endlink, check that
 *                     @c @b fldref2 / @c @b mthref2 is accessible to
 *                     @c @b objhash1 .  For example, @b INVOKEVIRTUAL
 *                     uses this accessibility test where
 *                     @b INVOKESPECIAL does not.
 *
 * @param member_type  One of @link #MEMBER_TYPE_FIELD
 *                     MEMBER_TYPE_xxx@endlink to inform common function
 *                     which type of class member is being processed,
 *                     a @link #MEMBER_TYPE_FIELD field@endlink, a
 *                     @link #MEMBER_TYPE_METHOD method@endlink, or an
 *                     @link #MEMBER_TYPE_INTERFACE_METHOD
                       interface method@endlink.
 *
 *
 * @see classutil_class_is_a()
 * @see classutil_direct_superclass_parent_object_of()
 *
 */

/*@{ */ /* Begin grouped definitions */

/*!
 * @brief Set class member resolution status to "not found"
 *
 * This is used both in classutil_class_has_a_common_member()
 * and in its callers when performing their unique tests after a
 * positive result there.
 *
 */
#define INITIALIZE_CLASS_RESOLVE_MEMBER(rc)         \
    rc->clsidx = jvm_class_index_null;              \
    rc->mthidx = jvm_method_index_bad;              \
    rc->fldidx = jvm_field_index_bad; /* Extra ; */

#define MEMBER_TYPE_FIELD            1 /**< Common member is a field */
#define MEMBER_TYPE_METHOD           2 /**< Common member is a method */
#define MEMBER_TYPE_INTERFACE_METHOD 3 /**< Common member is an
                                            interface method */
/*!
 * @brief Code common to classutil_class_has_a_method() and
 * classutil_class_has_an_interface_method() and
 * classutil_class_has_an_field() .
 *
 *
 * @returns @link #rtrue rtrue@endlink if a valid common member was
 *          found, otherwise @link #rfalse rfalse@endlink.
 *
 */
static rboolean classutil_class_has_a_common_member(
          class_resolve_member    *rc,
          jvm_class_index          clsidx1,
          ClassFile               *pcfs2,
          u2                       name_and_type_index2,
          rboolean                 isaccessible,
          rint                     member_type)
{
    ARCH_FUNCTION_NAME(classutil_class_has_a_common_method);

    /* Disallow null result pointer (should never happen) */
    if (rnull == rc)
    {
/*NOTREACHED*/
        sysErrMsg(arch_function_name, "bad result pointer");
        return(rfalse);
    }

    /* Initialize result to "class not found with matching member" */
    INITIALIZE_CLASS_RESOLVE_MEMBER(rc);

    /* Disallow trivial class parameter case */
    if (rnull == pcfs2)
    {
        sysErrMsg(arch_function_name, "bad class file struct pointer");
        return(rfalse);
    }

    /* Point to constant_pool method name and type descriptor */
    CONSTANT_NameAndType_info *pcpma_NameAndType =
        PTR_THIS_CP_NameAndType(
            pcfs2->constant_pool[name_and_type_index2]);

    /*
     * Comparison is true if class is same as resolved member
     * (on first time through) or of a superclass of @c @b clsidx1
     * (on second through final time)
     */
    jvm_class_index clsidxCLS = clsidx1;

    while (jvm_class_index_null != clsidxCLS)
    {
        jvm_field_index  fldidxCLS;
        jvm_method_index mthidxCLS;

        /* Process for the varils member types */
        switch (member_type)
        {
            case MEMBER_TYPE_FIELD:
                fldidxCLS =
                    field_find_by_cp_entry(
                        clsidxCLS,

                        pcfs2->constant_pool
                               [pcpma_NameAndType->name_index],

                        pcfs2->constant_pool
                               [pcpma_NameAndType->descriptor_index]);

                /*
                 * Match found if field has same name and type
                 * as name_and_type_index2
                 */
                if (jvm_field_index_bad != fldidxCLS)
                {
                    if ((rfalse == isaccessible) ||
                        /*
                         * Call classutil_field_is_accessible_to()
                         * from invoking function for simplicity.
                         */
                        (rtrue  == classutil_class_is_accessible_to(
                                       rc->clsidx,
                                       clsidx1)))
                    {
                        /* Report matching class index and field index*/
                        rc->clsidx = clsidxCLS;
                        rc->fldidx = fldidxCLS;
                        return(rtrue);
                    }
                }
                break;

            case MEMBER_TYPE_METHOD:
            case MEMBER_TYPE_INTERFACE_METHOD:
                mthidxCLS =
                    method_find_by_cp_entry(
                        clsidxCLS,

                        pcfs2->constant_pool
                               [pcpma_NameAndType->name_index],

                        pcfs2->constant_pool
                               [pcpma_NameAndType->descriptor_index]);

                /*
                 * Match found if method has same name and type
                 * as name_and_type_index2
                 */
                if (jvm_method_index_bad != mthidxCLS)
                {
                    if ((rfalse == isaccessible) ||
                        (rtrue  ==
                        /*
                         * Call classutil_method_is_accessible_to()
                         * from invoking function for simplicity.
                         */
                         classutil_class_is_accessible_to(rc->clsidx,
                                                          clsidx1)))
                    {
                        /*Report matching class index and method index*/
                        rc->clsidx = clsidxCLS;
                        rc->mthidx = mthidxCLS;
                        return(rtrue);
                    }
                }
                break;

        } /* switch (member_type) */

        /* Retrieve next superclass, but java.lang.Object for arrays */
        clsidxCLS =
            (CLASS(clsidx1).status & CLASS_STATUS_ARRAY)
                ? pjvm->class_java_lang_Object
                : OBJECT_CLASS_LINKAGE(OBJECT(CLASS(clsidxCLS)
                                                    .class_objhash)
                                       .objhash_super_class)
                    ->clsidx;
    }

    /* Valid member not found */
    return(rfalse);

} /* END of classutil_class_object_has_a_common_member() */


/*!
 * @brief Locate class index of class of @c @b objhash1 or one of its
 * parent objects that <em>has a</em> member method @c @b mthref2 .
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
rvoid classutil_class_has_a_method(
          class_resolve_member    *rc,
          jvm_object_hash          objhash1,
          ClassFile               *pcfs2,
          CONSTANT_Methodref_info *mthref2,
          rboolean                 isaccessible)
{
    ARCH_FUNCTION_NAME(classutil_class_has_a_method);

    jvm_class_index clsidx1 = OBJECT_CLASS_LINKAGE(objhash1)->clsidx;

    /* Disallow trivial null method reference cases */
    if (rnull == mthref2)
    {
        sysErrMsg(arch_function_name, "bad method reference pointer");
        return;
    }

    u2 name_and_type_index2 = mthref2->name_and_type_index;

    if (rfalse == classutil_class_has_a_common_member(
                      rc,
                      clsidx1,
                      pcfs2,
                      name_and_type_index2,
                      isaccessible,
                      MEMBER_TYPE_METHOD))
    {
        return;
    }

    if (rfalse == classutil_method_is_accessible_to(mthref2,clsidx1))
    {
        /* Clear result to "class not found with matching member" */
        INITIALIZE_CLASS_RESOLVE_MEMBER(rc);
    }

    return;

} /* END of classutil_class_has_a_method() */


/*!
 * @brief Locate class index of class of @c @b objhash1 or one of its
 * parent objects that <em>has a</em> member method @c @b mthref2 .
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
rvoid classutil_class_has_an_interface_method(
          class_resolve_member             *rc,
          jvm_object_hash                   objhash1,
          ClassFile                        *pcfs2,
          CONSTANT_InterfaceMethodref_info *mthref2)
{
    ARCH_FUNCTION_NAME(classutil_class_has_an_interface_method);

    jvm_class_index clsidx1 = OBJECT_CLASS_LINKAGE(objhash1)->clsidx;

    /* Disallow trivial null method reference cases */
    if (rnull == mthref2)
    {
        sysErrMsg(arch_function_name, "bad method reference pointer");
        return;
    }

    u2 name_and_type_index2 = mthref2->name_and_type_index;

    if (rfalse == classutil_class_has_a_common_member(
                      rc,
                      objhash1,
                      pcfs2,
                      name_and_type_index2,
                      rfalse,
                      MEMBER_TYPE_INTERFACE_METHOD))
    {
        return;
    }

    if (rfalse ==
        classutil_interface_method_is_accessible_to(mthref2, clsidx1))
    {
        /* Clear result to "class not found with matching member" */
        INITIALIZE_CLASS_RESOLVE_MEMBER(rc);
    }

    return;

} /* END of classutil_class_has_an_interface_method() */


/*!
 * @brief Locate class index of class of @c @b objhash1 or one of its
 * parent objects that <em>has a</em> member field @c @b fldref2 .
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
rvoid classutil_class_has_a_field(class_resolve_member   *rc,
                                  jvm_object_hash         objhash1,
                                  ClassFile              *pcfs2,
                                  CONSTANT_Fieldref_info *fldref2)
{
    ARCH_FUNCTION_NAME(classutil_class_has_a_field);

    jvm_class_index clsidx1 = OBJECT_CLASS_LINKAGE(objhash1)->clsidx;

    /* Disallow trivial null method reference cases */
    if (rnull == fldref2)
    {
        sysErrMsg(arch_function_name, "bad field reference pointer");
        return;
    }

    u2 name_and_type_index2 = fldref2->name_and_type_index;

    if (rfalse == classutil_class_has_a_common_member(
                      rc,
                      objhash1,
                      pcfs2,
                      name_and_type_index2,
                      rfalse,
                      MEMBER_TYPE_FIELD))
    {
        return;
    }

    if (rfalse == classutil_field_is_accessible_to(fldref2, clsidx1))
    {
        /* Clear result to "class not found with matching member" */
        INITIALIZE_CLASS_RESOLVE_MEMBER(rc);
    }

    return;

} /* END of classutil_class_has_a_field() */


/*@} */ /* End of grouped definitions */


/* EOF */
