/*!
 * @file cfmsgs.c
 *
 * @brief Error message for class file functions.
 *
 * For structure packing mismatches, sprintf requests
 * have been moved to functions in
 * @link jvm/src/stdio.c stdio.c@endlink
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
ARCH_SOURCE_COPYRIGHT_APACHE(cfmsgs, c,
"$URL$",
"$Id$");


#include "jvmcfg.h"
#include "attribute.h"
#include "cfmacros.h"
#include "classfile.h"
#include "util.h"


/*!
 * @brief Display details of what an @c @b constant_pool entry contains.
 *
 * @param  fn     Function name message for sysErrMsg()
 *
 * @param  pcfs   ClassFile structure containing @c @b constant_pool
 *                info
 *
 * @param  cpidx  Index into @c @b constant_pool[] to report
 *
 *
 * @returns @link #rvoid rvoid@endlink  The @c @b pcfs->constant_pool
 *          must be valid for this to produce any meaningful results.
 *
 */

rvoid cfmsgs_typemsg(rchar *fn,
                     ClassFile *pcfs,
                     jvm_constant_pool_index cpidx)
{
    ARCH_FUNCTION_NAME(cfmsgs_typemsg);

    u4      *pu4, *pu4h, *pu4l;
    rulong  *prl8;
    rdouble *prd8;

    jint     vali;
    jlong    vall;
    jfloat   valf;
    jdouble  vald;
    rint     star_len, star_len2;

    if (jvm_constant_pool_index_null == cpidx)
    {
        /*!
         * @todo  HARMONY-6-jvm-cfmsgs.c-1 Widen pointer format
         *         logic for 64-bit pointers
         */
        sysDbgMsg(DMLNORM,
                  fn,
                  "NULL index (%d) into constant_pool 0x%08x\n",
                  cpidx,
                  pcfs);
        return;
    }

    rchar   *msg_hdr = HEAP_GET_DATA(JVMCFG_STDIO_BFR, rfalse);

    u1 tag = CP_TAG(pcfs, cpidx);

    /*
     * Explicitly define length calculations of " %*.*s " constructions
     * @e outside of the sprintf/fprintf that uses it to simplify
     * the calling declarations.  Should make them somewhat easier
     * to read.  Use @c @b star_len for one " %*.*s " syntax,
     * and use both @c @b star_len and @c @b star_len2
     * for " %*.*s   ...   %*.*s " syntax.
     *
     * DO NOT use " %*s " format!!  It will print <i>at least</i> that
     * number of bytes, until a @b \0 or end of heap segment, which
     * may cause @b SIGSEGV!
     */
    star_len = (jvm_class_index_null == pcfs->this_class)
                   ? 7 /* Length of string "unknown" as used below */
                   : CP1_NAME_STRLEN(CONSTANT_Class_info,
                                     pcfs,
                                     pcfs->this_class,
                                     name_index);
    sprintfLocal(msg_hdr,
                 "%*.*s cpidx=0x%02x tag=%d",

                 star_len, star_len,
                 (jvm_class_index_null == pcfs->this_class)
                    ? ((rchar *) "unknown")
                    : (rchar *)PTR_CP1_NAME_STRNAME(CONSTANT_Class_info,
                                                    pcfs,
                                                    pcfs->this_class,
                                                    name_index),
                 cpidx,
                 tag);

    switch(tag)
    {
        case CONSTANT_Class:

            star_len = CP1_NAME_STRLEN(CONSTANT_Class_info,
                                       pcfs,
                                       cpidx,
                                       name_index);
            sysDbgMsg(DMLNORM,
                      fn,
                    "%s Class cpidx=0x%02x clsidx=%d len=%d %*.*s",
                      msg_hdr,
                      PTR_CP_ENTRY_TYPE(CONSTANT_Class_info, pcfs,cpidx)
                        ->name_index,

                      PTR_CP_ENTRY_TYPE(CONSTANT_Class_info, pcfs,cpidx)
                        ->LOCAL_Class_binding.clsidxJVM,

                      star_len,

                      star_len, star_len,
                      PTR_CP1_NAME_STRNAME(CONSTANT_Class_info,
                                           pcfs,
                                           cpidx,
                                           name_index));

            break;

        case CONSTANT_Fieldref:

            star_len = CP2_CLASS_NAME_STRLEN(CONSTANT_Fieldref_info,
                                             pcfs,
                                             cpidx,
                                             class_index);

            star_len2 = CP2_CLASS_NAME_STRLEN(CONSTANT_Fieldref_info,
                                              pcfs,
                                              cpidx,
                                              name_and_type_index);

            sysDbgMsg(DMLNORM,
                      fn,
"%s Fieldref cpidx=0x%02x clsidx=%d fluidx=%d oiflag=%d jvalue=%c len=%d class=%*.*s cpidx=0x%02x len=%d n/t=%*.*s",
                      msg_hdr,
                    PTR_CP_ENTRY_TYPE(CONSTANT_Fieldref_info,pcfs,cpidx)
                      ->class_index,

                    PTR_CP_ENTRY_TYPE(CONSTANT_Fieldref_info,pcfs,cpidx)
                      ->LOCAL_Fieldref_binding.clsidxJVM,
                    PTR_CP_ENTRY_TYPE(CONSTANT_Fieldref_info,pcfs,cpidx)
                      ->LOCAL_Fieldref_binding.fluidxJVM,
                    PTR_CP_ENTRY_TYPE(CONSTANT_Fieldref_info,pcfs,cpidx)
                      ->LOCAL_Fieldref_binding.oiflagJVM,
                    PTR_CP_ENTRY_TYPE(CONSTANT_Fieldref_info,pcfs,cpidx)
                      ->LOCAL_Fieldref_binding.jvaluetypeJVM,

                      star_len,

                      star_len, star_len,
                      PTR_CP2_CLASS_NAME_STRNAME(CONSTANT_Fieldref_info,
                                                 pcfs,
                                                 cpidx,
                                                 class_index),

                    PTR_CP_ENTRY_TYPE(CONSTANT_Fieldref_info,pcfs,cpidx)
                        ->name_and_type_index,

                      star_len2,

                      star_len2, star_len2,
                      PTR_CP2_CLASS_NAME_STRNAME(CONSTANT_Fieldref_info,
                                                 pcfs,
                                                 cpidx,
                                                 name_and_type_index));
            break;

        case CONSTANT_Methodref:

            star_len = CP2_CLASS_NAME_STRLEN(CONSTANT_Methodref_info,
                                              pcfs,
                                              cpidx,
                                              class_index);
            star_len2 = CP2_CLASS_NAME_STRLEN(CONSTANT_Methodref_info,
                                              pcfs,
                                              cpidx,
                                              name_and_type_index);
            sysDbgMsg(DMLNORM,
                      fn,
"%s %s cpidx=0x%02x clsidx=%d mthidx=%d caidx=%d eaidx=%d nmo=%d len=%d class=%*.*s cpidx=0x%02x len=%d n/t=%*.*s",
                      msg_hdr,
                      "Methodref",
                      PTR_CP_ENTRY_TYPE(CONSTANT_Methodref_info,
                                        pcfs,
                                        cpidx)->class_index,

                   PTR_CP_ENTRY_TYPE(CONSTANT_Methodref_info,pcfs,cpidx)
                     ->LOCAL_Methodref_binding.clsidxJVM,
                   PTR_CP_ENTRY_TYPE(CONSTANT_Methodref_info,pcfs,cpidx)
                     ->LOCAL_Methodref_binding.mthidxJVM,
                   PTR_CP_ENTRY_TYPE(CONSTANT_Methodref_info,pcfs,cpidx)
                     ->LOCAL_Methodref_binding.codeatridxJVM,
                   PTR_CP_ENTRY_TYPE(CONSTANT_Methodref_info,pcfs,cpidx)
                     ->LOCAL_Methodref_binding.excpatridxJVM,
                   PTR_CP_ENTRY_TYPE(CONSTANT_Methodref_info,pcfs,cpidx)
                     ->LOCAL_Methodref_binding.nmordJVM,

                      star_len,

                      star_len, star_len,
                     PTR_CP2_CLASS_NAME_STRNAME(CONSTANT_Methodref_info,
                                                pcfs,
                                                cpidx,
                                                class_index),

                      PTR_CP_ENTRY_TYPE(CONSTANT_Methodref_info,
                                        pcfs,
                                        cpidx)->name_and_type_index,

                      star_len2,

                      star_len2, star_len2,
                     PTR_CP2_CLASS_NAME_STRNAME(CONSTANT_Methodref_info,
                                                pcfs,
                                                cpidx,
                                                name_and_type_index));
            break;

        case CONSTANT_InterfaceMethodref:

            star_len = CP2_CLASS_NAME_STRLEN(
                                       CONSTANT_InterfaceMethodref_info,
                                             pcfs,
                                             cpidx,
                                             class_index);
            star_len2 = CP2_CLASS_NAME_STRLEN(
                                       CONSTANT_InterfaceMethodref_info,
                                              pcfs,
                                              cpidx,
                                              name_and_type_index),
            sysDbgMsg(DMLNORM,
                      fn,
"%s %s cpidx=0x%02x clsidx=%d mthidx=%d caidx=%d eaidx=%d nmo=%d len=%d class=%*.*s cpidx=0x%02x len=%d n/t=%*.*s",
                      msg_hdr,
                      "InterfaceMethodref", /* Keep fmt string small */
                      PTR_CP_ENTRY_TYPE(
                                       CONSTANT_InterfaceMethodref_info,
                                       pcfs,
                                       cpidx)->class_index,
                     PTR_CP_ENTRY_TYPE(CONSTANT_InterfaceMethodref_info,
                                       pcfs,
                                       cpidx)
                       ->LOCAL_InterfaceMethodref_binding.clsidxJVM,
                     PTR_CP_ENTRY_TYPE(CONSTANT_InterfaceMethodref_info,
                                       pcfs,
                                       cpidx)
                       ->LOCAL_InterfaceMethodref_binding.mthidxJVM,
                     PTR_CP_ENTRY_TYPE(CONSTANT_InterfaceMethodref_info,
                                       pcfs,
                                       cpidx)
                       ->LOCAL_InterfaceMethodref_binding
                         .codeatridxJVM,
                     PTR_CP_ENTRY_TYPE(CONSTANT_InterfaceMethodref_info,
                                       pcfs,
                                       cpidx)
                       ->LOCAL_InterfaceMethodref_binding
                         .excpatridxJVM,
                     PTR_CP_ENTRY_TYPE(CONSTANT_InterfaceMethodref_info,
                                       pcfs,
                                       cpidx)
                       ->LOCAL_InterfaceMethodref_binding
                         .nmordJVM,

                      star_len,

                      star_len, star_len,
                      PTR_CP2_CLASS_NAME_STRNAME(
                                       CONSTANT_InterfaceMethodref_info,
                                                 pcfs,
                                                 cpidx,
                                                 class_index),

                      PTR_CP_ENTRY_TYPE(
                                       CONSTANT_InterfaceMethodref_info,
                                       pcfs,
                                       cpidx)->name_and_type_index,

                      star_len2,

                      star_len2, star_len2,
                      PTR_CP2_CLASS_NAME_STRNAME(
                                       CONSTANT_InterfaceMethodref_info,
                                                 pcfs,
                                                 cpidx,
                                                 name_and_type_index));
            break;

        case CONSTANT_String:

            star_len = CP1_NAME_STRLEN(CONSTANT_String_info,
                                       pcfs,
                                       cpidx,
                                       string_index);
            sysDbgMsg(DMLNORM,
                      fn,
                "%s String cpidx=0x%02x len=%d UTF8='%*.*s'",
                      msg_hdr,
                     PTR_CP_ENTRY_TYPE(CONSTANT_String_info, pcfs,cpidx)
                       ->string_index,

                      star_len,

                      star_len, star_len,
                      PTR_CP1_NAME_STRNAME(CONSTANT_String_info,
                                           pcfs,
                                           cpidx,
                                           string_index));
            break;

        case CONSTANT_Integer:

            pu4 = &PTR_CP_ENTRY_TYPE(CONSTANT_Integer_info,
                                     pcfs,
                                     cpidx)->bytes;

            vali = GETRI4(pu4);

            sysDbgMsg(DMLNORM, fn, "%s Integer value=%d", msg_hdr,vali);
            break;

        case CONSTANT_Float:

            pu4 = &PTR_CP_ENTRY_TYPE(CONSTANT_Float_info,
                                     pcfs,
                                     cpidx)->bytes;

            valf = (jfloat) GETRI4(pu4);

            sysDbgMsg(DMLNORM, fn, "%s Float value=%f", msg_hdr, valf);
            break;

        case CONSTANT_Long:

            pu4h = &PTR_CP_ENTRY_TYPE(CONSTANT_Long_info, pcfs, cpidx)
                      ->high_bytes;
            pu4l = &PTR_CP_ENTRY_TYPE(CONSTANT_Long_info, pcfs, cpidx)
                      ->low_bytes;

            /*
             * if WORDSIZE/32/64 mismatches -m32/-m64,
             * the <b><code>JBITS * sizeof(u4)</code></b> calculation
             * @e will cause a runtime-visible compiler
             * warning!
             */

/*
 *          vall = (jlong) ((((julong) *pu4h) << (JBITS * sizeof(u4))) |
 *                          ((julong) *pu4l));
 */

/*!
 * @todo  HARMONY-6-jvm-cfmsgs.c-2 Above logic works, 64-bit
 *        logic below needs testing:
 */
            /* LS word always follows MS word */
            prl8 = (rulong *) pu4h;
            vall = (jlong) GETRL8(prl8);

            /*!
             * @todo  HARMONY-6-jvm-cfmsgs.c-3 Make format string
             *        properly reflect 64-bit int
             */
            sysDbgMsg(DMLNORM, fn, "%s Long value=%ld", msg_hdr, vall);
            break;

        case CONSTANT_Double:

            pu4h = &PTR_CP_ENTRY_TYPE(CONSTANT_Double_info, pcfs, cpidx)
                      ->high_bytes;
            pu4l = &PTR_CP_ENTRY_TYPE(CONSTANT_Double_info, pcfs, cpidx)
                      ->low_bytes;

            /*
             * if WORDSIZE/32/64 mismatches -m32/-m64,
             * the <b><code>JBITS * sizeof(u4)</code></b> calculation
             * @e will cause a runtime-visible compiler
             * warning!
             */

/*
 *          vald = (jdouble) ((((julong) *pu4h) <<(JBITS * sizeof(u4)))|
 *                          ((julong) *pu4l));
 */

/*!
 * @todo  HARMONY-6-jvm-cfmsgs.c-4 Above logic works, 64-bit logic
 *        below needs testing:
 */
            /* LS word always follows MS word */
            prd8 = (rdouble *) pu4h;
            vald = (jdouble) GETRL8((rulong *) prd8);

            sysDbgMsg(DMLNORM, fn, "%s Double value=%lf", msg_hdr,vald);
            break;

        case CONSTANT_NameAndType:

            star_len = CP1_NAME_STRLEN(CONSTANT_NameAndType_info,
                                       pcfs,
                                       cpidx,
                                       name_index);
            star_len2 = CP1_NAME_STRLEN(CONSTANT_NameAndType_info,
                                        pcfs,
                                        cpidx,
                                        descriptor_index);
            sysDbgMsg(DMLNORM,
                      fn,
  "%s %s cpidx=0x%02x len=%d name=%*.*s cpidx=0x%02x len=%d desc=%*.*s",
                      msg_hdr,
                      "NameAndType",
                      PTR_CP_ENTRY_TYPE(CONSTANT_NameAndType_info,
                                        pcfs,
                                        cpidx)->name_index,

                      star_len,

                      star_len, star_len,
                      PTR_CP1_NAME_STRNAME(CONSTANT_NameAndType_info,
                                           pcfs,
                                           cpidx,
                                           name_index),

                      PTR_CP_ENTRY_TYPE(CONSTANT_NameAndType_info,
                                        pcfs,
                                        cpidx)->descriptor_index,

                      star_len2,

                      star_len2, star_len2,
                      PTR_CP1_NAME_STRNAME(CONSTANT_NameAndType_info,
                                           pcfs,
                                           cpidx,
                                           descriptor_index));
            break;

        case CONSTANT_Utf8:

            star_len = CP_THIS_STRLEN(pcfs, cpidx);
            sysDbgMsg(DMLNORM,
                      fn,
                      "%s Constant len=%d UTF8='%*.*s'",
                      msg_hdr,

                      star_len,

                      star_len, star_len,
                      PTR_CP_THIS_STRNAME(pcfs, cpidx));
            break;

        default:

            sysDbgMsg(DMLNORM, fn, "%s \a UNKNOWN TAG", msg_hdr);
            break;

    } /* switch (tag) */

    HEAP_FREE_DATA(msg_hdr);

    return;

} /* END of cfmsgs_typemsg() */


/*!
 * @brief Show all entries in the constant pool
 *
 *
 * @param  pcfs    ClassFile to dump contents
 *
 *
 * @returns @link #rvoid rvoid@endlink
 *
 */
rvoid cfmsgs_show_constant_pool(ClassFile *pcfs)
{
    ARCH_FUNCTION_NAME(cfmsgs_show_constant_pool);

    jvm_constant_pool_index cpidx;

    /*
     * Rehearse contents of @c @b constant_pool (Wait until AFTER the
     * @link ClassFile.this_class this_class@endlink item is located
     * so that the message prints the class name instead of a
     * default @b "unknown" for the class name.
     */
    for (cpidx = CONSTANT_CP_START_INDEX;
         cpidx < pcfs->constant_pool_count + CONSTANT_CP_START_INDEX -1;
         cpidx++)
    {
        cfmsgs_typemsg("cpload", pcfs, cpidx);

    } /* for (cpidx) */

} /* END of cfmsgs_show_constant_pool() */


/*!
 * @brief Display details of what an attribute entry contains.
 *
 * @param  fn     Function name message for sysErrMsg()
 *
 * @param  pcfs   ClassFile to dump contents
 *
 * @param  paima  Pointer to an attribute area.  WARNING:  This
 *                pointer MUST be 4-byte aligned to suppress
 *                @b SIGSEGV.  Such logic is already taken care of by
 *                virtue of it being an (attribute_info_mem_align *)
 *                type instead of as (attribute_info *).  This
 *                processing happens in cfattrib_load_attribute()
 *                after reading an attribute from the class file
 *                and storing it into the heap, which is properly
 *                aligned.
 *
 *
 * @returns @link #rvoid rvoid@endlink The
 *          @c @b paima->ai.attribute_index @c @b constant_pool entry
 *          must be valid for this to produce any meaningful
 *          results.
 *
 */

rvoid cfmsgs_atrmsg(const rchar              *fn,
                    ClassFile                *pcfs,
                    attribute_info_mem_align *paima)
{
    ARCH_FUNCTION_NAME(cfmsgs_atrmsg);

    u4      *pu4, *pu4h, *pu4l;
    rulong  *prl8;
    rdouble *prd8;

    jint     vali;
    jlong    vall;
    jfloat   valf;
    jdouble  vald;
    rint     star_len;

    exception_table_entry *pete;
    u2                     etblidx;
    u2                     etbllen;
    u2                     etblentry;

    classfile_attribute_enum cae;

    rchar *msg_hdr = HEAP_GET_DATA(JVMCFG_STDIO_BFR, rfalse);

    u2 cpidx = paima->ai.attribute_name_index;
    u4 len   = paima->ai.attribute_length;
    u2 constantvalue_index;

    star_len = (jvm_class_index_null == pcfs->this_class)
                   ? 7 /* Length of string "unknown" as used below */
                   : CP1_NAME_STRLEN(CONSTANT_Class_info,
                                     pcfs,
                                     pcfs->this_class,
                                     name_index);
    sprintfLocal(msg_hdr,
                 "%*.*s cpidx=0x%02x len=%d ",
                 star_len, star_len,
                 (jvm_class_index_null == pcfs->this_class)
                    ? ((rchar *) "unknown")
                    : (rchar *)PTR_CP1_NAME_STRNAME(CONSTANT_Class_info,
                                                    pcfs,
                                                    pcfs->this_class,
                                                    name_index),
                 cpidx,
                 len);

    cae = cfattrib_atr2enum(pcfs, cpidx);
    switch(cae)
    {
        case LOCAL_CONSTANTVALUE_ATTRIBUTE:
            constantvalue_index =
                       ATR_CONSTANTVALUE_AI(paima)->constantvalue_index;
            switch((int) CP_TAG(pcfs, constantvalue_index))
            {
/*!
 * @todo  HARMONY-6-jvm-cfmsgs.c-5 Verify (jlong) retrieval
 *        of @c @b bytes for both -m32 and -m64 compilations.
 */
                case CONSTANT_Long:

                    pu4h = &PTR_CP_ENTRY_TYPE(CONSTANT_Long_info,
                                              pcfs,
                                              constantvalue_index)
                                ->high_bytes;
                    pu4l = &PTR_CP_ENTRY_TYPE(CONSTANT_Long_info,
                                              pcfs,
                                              constantvalue_index)
                                ->low_bytes;

                    /*
                     * if WORDSIZE/32/64 mismatches -m32/-m64,
                     * the <b><code>JBITS * sizeof(u4)</code></b>
                     * calculation @e will cause a runtime-visible
                     * compiler warning!
                     */
/*
 *                  vall = (jlong) ((((julong) *pu4h) <<
 *                                   (JBITS * sizeof(u4))) |
 *                                  ((julong) *pu4l));
 */

/*!
 * @todo  HARMONY-6-jvm-cfmsgs.c-6 Above logic works, logic
 *        below needs testing:
 */
                    /* LS word always follows MS word */
                    prl8 = (rulong *) pu4h;
                    vall = (jlong) GETRL8(prl8);


                    /*!
                     * @todo  HARMONY-6-jvm-cfmsgs.c-7 Make format
                     *        string properly reflect 64-bit
                     *        (rlong)/(jlong)
                     */
                    sysDbgMsg(DMLNORM,
                              fn, "%s %s long=%ld",
                              msg_hdr,
                              CONSTANT_UTF8_CONSTANTVALUE_ATTRIBUTE,
                              vall);
                    break;

/*!
 * @todo  HARMONY-6-jvm-cfmsgs.c-8 Verify (jfloat) retrieval
 *        of @c @b bytes for both -m32 and -m64 compilations.
 */
                case CONSTANT_Float:

                    pu4 = &PTR_CP_ENTRY_TYPE(CONSTANT_Float_info,
                                             pcfs,
                                             constantvalue_index)
                               ->bytes;
                    valf = (jfloat) (jint) *pu4;

                    sysDbgMsg(DMLNORM,
                              fn, "%s %s float=%f",
                              msg_hdr,
                              CONSTANT_UTF8_CONSTANTVALUE_ATTRIBUTE,
                              valf);
                    break;

/*!
 * @todo  HARMONY-6-jvm-cfmsgs.c-9 Verify (jdouble) retrieval
 *        of @c @b bytes for both -m32 and -m64 compilations.
 */
                case CONSTANT_Double:

                    pu4h = &PTR_CP_ENTRY_TYPE(CONSTANT_Double_info,
                                              pcfs,
                                              constantvalue_index)
                                ->high_bytes;
                    pu4l = &PTR_CP_ENTRY_TYPE(CONSTANT_Double_info,
                                              pcfs,
                                              constantvalue_index)
                                ->low_bytes;

                    /*
                     * if WORDSIZE/32/64 mismatches -m32/-m64,
                     * the <b><code>JBITS * sizeof(u4)</code></b>
                     * calculation @e will cause a runtime-visible
                     * compiler warning!
                     */
/*
 *                  vald = (jdouble) ((((julong) *pu4h) <<
 *                                     (JBITS * sizeof(u4))) |
 *                                    ((julong) *pu4l));
 */

/*!
 * @todo  HARMONY-6-jvm-cfmsgs.c-10 Above logic works, 64-bit
 *         logic below needs testing:
 */
                    /* LS word always follows MS word */
                    prd8 = (rdouble *) pu4h;
                    vald = (jdouble) GETRL8((rulong *) prd8);


                    /*!
                     * @todo  HARMONY-6-jvm-cfmsgs.c-11 Make format
                     *        string properly reflect 64-bit
                     *        (double)/(jdouble)
                     */
                    sysDbgMsg(DMLNORM,
                              fn, "%s %s double=%ld",
                              msg_hdr,
                              CONSTANT_UTF8_CONSTANTVALUE_ATTRIBUTE,
                              vald);
                    break;

/*!
 * @todo  HARMONY-6-jvm-cfmsgs.c-12 Verify (jint) retrieval
 *        of @c @b bytes for both -m32 and -m64 compilations.
 */
                case CONSTANT_Integer:

                    pu4 = &PTR_CP_ENTRY_TYPE(CONSTANT_Integer_info,
                                             pcfs,
                                             constantvalue_index)
                               ->bytes;
                    vali = (jint) *pu4;

                    sysDbgMsg(DMLNORM,
                              fn, "%s %s int_type=%d",
                              msg_hdr,
                              CONSTANT_UTF8_CONSTANTVALUE_ATTRIBUTE,
                              vali);
                    break;

                case CONSTANT_String:

                    star_len =CP_THIS_STRLEN(pcfs, constantvalue_index);
                    sysDbgMsg(DMLNORM,
                              fn,
                        "%s %s String len=%d UTF8='%*.*s'",
                              msg_hdr,
                              CONSTANT_UTF8_CONSTANTVALUE_ATTRIBUTE,
                              star_len,

                              star_len, star_len,
                              PTR_CP_THIS_STRNAME(pcfs,
                                                  constantvalue_index));
                    break;

                default:

                    sysDbgMsg(DMLNORM,
                              fn,
                              "%s %s  unknown tag=%d",
                              msg_hdr,
                              LOCAL_CONSTANT_UTF8_UNKNOWN_ATTRIBUTE,
                              CP_TAG(pcfs, constantvalue_index));
                    break;

            } /* switch CP_TAG(pcfs, constantvalue_index) */

            break;

        case LOCAL_CODE_ATTRIBUTE:

            etbllen = ATR_CODE_AI(paima)->exception_table_length;

            sysDbgMsg(DMLNORM,
                      fn,
         "%s %s   stack=0x%04x locals=0x%04x codelen=0x%04x excplen=%d",
                      msg_hdr,
                      CONSTANT_UTF8_CODE_ATTRIBUTE,
                      ATR_CODE_AI(paima)->max_stack,
                      ATR_CODE_AI(paima)->max_locals,
                      ATR_CODE_AI(paima)->code_length,
                      etbllen);

            /* Display exception table, if present */
            if (0 < etbllen)
            {
                /* Load up exception table one entry at a time */
                for (etblidx = 0; etblidx < etbllen; etblidx++)
                {
                    pete = &(ATR_CODE_AI(paima)
                               ->exception_table)[etblidx];
                    sysDbgMsg(DMLNORM,
                      fn,
                      "%s %s     excpidx=%d "
                  "PC: start=0x%04x end=0x%04x hdlr=0x%04x type=0x%04x",
                      msg_hdr,
                      CONSTANT_UTF8_CODE_ATTRIBUTE,
                      etblidx,
                      pete->start_pc,
                      pete->end_pc,
                      pete->handler_pc,
                      pete->catch_type);
                }
            }
            break;

        case LOCAL_EXCEPTIONS_ATTRIBUTE:

            etbllen = ATR_EXCEPTIONS_AI(paima)->number_of_exceptions;
            sysDbgMsg(DMLNORM,
                      fn,
                      "%s %s numexcp=%d",
                      msg_hdr,
                      CONSTANT_UTF8_EXCEPTIONS_ATTRIBUTE,
                      etbllen);

            /* Display exception index table, if present */
            if (0 < etbllen)
            {
                for (etblidx = 0; etblidx < etbllen; etblidx++)
                {
                    etblentry = (ATR_EXCEPTIONS_AI(paima)
                                   ->exception_index_table)[etblidx];

                    sysDbgMsg(DMLNORM,
                              fn,
                              "%s %s excpidx[%d]=0x%x",
                              msg_hdr,
                              CONSTANT_UTF8_EXCEPTIONS_ATTRIBUTE,
                              etblidx,
                              etblentry);
                }
            }
            break;

        case LOCAL_INNERCLASSES_ATTRIBUTE:

            sysDbgMsg(DMLNORM,
                      fn,
                      "%s %s num=%d",
                      msg_hdr,
                      CONSTANT_UTF8_INNERCLASSES_ATTRIBUTE,
                      ATR_INNERCLASSES_AI(paima)->number_of_classes);
            break;

        case LOCAL_ENCLOSINGMETHOD_ATTRIBUTE:

            sysDbgMsg(DMLNORM,
                      fn,
                      "%s %s clsidx=%d mthidx=%d",
                      msg_hdr,
                      CONSTANT_UTF8_ENCLOSINGMETHOD_ATTRIBUTE,
                      ATR_ENCLOSINGMETHOD_AI(paima)->class_index,
                      ATR_ENCLOSINGMETHOD_AI(paima)->method_index);
            break;

        case LOCAL_SYNTHETIC_ATTRIBUTE:

            sysDbgMsg(DMLNORM,
                      fn,
                      "%s %s",
                      msg_hdr,
                      CONSTANT_UTF8_SYNTHETIC_ATTRIBUTE);
            break;

        case LOCAL_SIGNATURE_ATTRIBUTE:

            sysDbgMsg(DMLNORM,
                      fn,
                      "%s %s sigidx=0x%02x",
                      msg_hdr,
                      CONSTANT_UTF8_SIGNATURE_ATTRIBUTE,
                      ATR_SIGNATURE_AI(paima)->signature_index);
            break;

        case LOCAL_SOURCEFILE_ATTRIBUTE:

            sysDbgMsg(DMLNORM,
                      fn,
                      "%s %s srcidx=0x%02x",
                      msg_hdr,
                      CONSTANT_UTF8_SOURCEFILE_ATTRIBUTE,
                      ATR_SOURCEFILE_AI(paima)->sourcefile_index);
            break;

        case LOCAL_LINENUMBERTABLE_ATTRIBUTE:

            sysDbgMsg(DMLNORM,
                      fn,
                      "%s %s len=%d",
                      msg_hdr,
                      CONSTANT_UTF8_LINENUMBERTABLE_ATTRIBUTE,
                      ATR_LINENUMBERTABLE_AI(paima)
                        ->line_number_table_length);
            break;

        case LOCAL_LOCALVARIABLETABLE_ATTRIBUTE:

            sysDbgMsg(DMLNORM,
                      fn,
                      "%s %s len=%d",
                      msg_hdr,
                      CONSTANT_UTF8_LOCALVARIABLETABLE_ATTRIBUTE,
                      ATR_LOCALVARIABLETABLE_AI(paima)
                        ->local_variable_table_length);
            break;

        case LOCAL_LOCALVARIABLETYPETABLE_ATTRIBUTE:

            sysDbgMsg(DMLNORM,
                      fn,
                      "%s %s len=%d",
                      msg_hdr,
                      CONSTANT_UTF8_LOCALVARIABLETYPETABLE_ATTRIBUTE,
                      ATR_LOCALVARIABLETYPETABLE_AI(paima)
                        ->local_variable_type_table_length);
            break;

        case LOCAL_DEPRECATED_ATTRIBUTE:

            sysDbgMsg(DMLNORM,
                      fn,
                      "%s %s",
                      msg_hdr,
                      CONSTANT_UTF8_DEPRECATED_ATTRIBUTE);
            break;

        case LOCAL_RUNTIMEVISIBLEANNOTATIONS_ATTRIBUTE:

            sysDbgMsg(DMLNORM,
                      fn,
                      "%s %s num=%d",
                      msg_hdr,
                      CONSTANT_UTF8_RUNTIMEVISIBLEANNOTATIONS_ATTRIBUTE,
                      ATR_RUNTIMEVISIBLEANNOTATIONS_AI(paima)
                        ->num_annotations);
            break;

        case LOCAL_RUNTIMEINVISIBLEANNOTATIONS_ATTRIBUTE:

            sysDbgMsg(DMLNORM,
                      fn,
                      "%s %s num=%d",
                      msg_hdr,
                    CONSTANT_UTF8_RUNTIMEINVISIBLEANNOTATIONS_ATTRIBUTE,
                      ATR_RUNTIMEINVISIBLEANNOTATIONS_AI(paima)
                        ->num_annotations);
            break;

        case LOCAL_RUNTIMEVISIBLEPARAMETERANNOTATIONS_ATTRIBUTE:

            sysDbgMsg(DMLNORM,
                      fn,
                      "%s %s num=%d",
                      msg_hdr,
             CONSTANT_UTF8_RUNTIMEVISIBLEPARAMETERANNOTATIONS_ATTRIBUTE,
                      ATR_RUNTIMEVISIBLEPARAMETERANNOTATIONS_AI(paima)
                        ->num_parameters);
            break;

        case LOCAL_RUNTIMEINVISIBLEPARAMETERANNOTATIONS_ATTRIBUTE:

            sysDbgMsg(DMLNORM,
                      fn,
                      "%s %s num=%d",
                      msg_hdr,
           CONSTANT_UTF8_RUNTIMEINVISIBLEPARAMETERANNOTATIONS_ATTRIBUTE,
                      ATR_RUNTIMEINVISIBLEPARAMETERANNOTATIONS_AI(paima)
                        ->num_parameters);
            break;

        case LOCAL_ANNOTATIONDEFAULT_ATTRIBUTE:

            sysDbgMsg(DMLNORM,
                      fn,
                      "%s %s tag=%d",
                      msg_hdr,
                      CONSTANT_UTF8_ANNOTATIONDEFAULT_ATTRIBUTE,
                      ATR_ANNOTATIONDEFAULT_AI(paima)
                        ->default_value.tag);
            break;

        case LOCAL_UNKNOWN_ATTRIBUTE:

        default:

            sysDbgMsg(DMLNORM,
                      fn,
                      "%s %s tag=%d",
                      msg_hdr,
                      LOCAL_CONSTANT_UTF8_UNKNOWN_ATTRIBUTE,
                      CP_TAG(pcfs, cpidx));
            break;

    } /* switch atrIndexToEmum() */

    HEAP_FREE_DATA(msg_hdr);

    return;

} /* END of cfmsgs_atrmsg() */


/* EOF */
