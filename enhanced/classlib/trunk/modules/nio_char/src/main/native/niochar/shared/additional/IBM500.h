/*
 * THE FILE HAS BEEN AUTOGENERATED BY THE IJH TOOL.
 * Please be aware that all changes made to this file manually
 * will be overwritten by the tool if it runs again.
 */

#include <jni.h>


/* Header for class org.apache.harmony.niochar.charset.additional.IBM500 */

#ifndef _ORG_APACHE_HARMONY_NIOCHAR_CHARSET_ADDITIONAL_IBM500_H
#define _ORG_APACHE_HARMONY_NIOCHAR_CHARSET_ADDITIONAL_IBM500_H

#ifdef __cplusplus
extern "C" {
#endif


#ifdef __cplusplus
}
#endif

#endif /* _ORG_APACHE_HARMONY_NIOCHAR_CHARSET_ADDITIONAL_IBM500_H */


/* Header for class org.apache.harmony.niochar.charset.additional.IBM500$Decoder */

#ifndef _ORG_APACHE_HARMONY_NIOCHAR_CHARSET_ADDITIONAL_IBM500_DECODER_H
#define _ORG_APACHE_HARMONY_NIOCHAR_CHARSET_ADDITIONAL_IBM500_DECODER_H

#ifdef __cplusplus
extern "C" {
#endif


/* Static final fields */

#undef org_apache_harmony_niochar_charset_additional_IBM500_Decoder_INIT
#define org_apache_harmony_niochar_charset_additional_IBM500_Decoder_INIT 0L

#undef org_apache_harmony_niochar_charset_additional_IBM500_Decoder_ONGOING
#define org_apache_harmony_niochar_charset_additional_IBM500_Decoder_ONGOING 1L

#undef org_apache_harmony_niochar_charset_additional_IBM500_Decoder_END
#define org_apache_harmony_niochar_charset_additional_IBM500_Decoder_END 2L

#undef org_apache_harmony_niochar_charset_additional_IBM500_Decoder_FLUSH
#define org_apache_harmony_niochar_charset_additional_IBM500_Decoder_FLUSH 3L


/* Native methods */

/*
 * Method: org.apache.harmony.niochar.charset.additional.IBM500$Decoder.nDecode([CIIJI)I
 */
JNIEXPORT jint JNICALL
Java_org_apache_harmony_niochar_charset_additional_IBM500_00024Decoder_nDecode(JNIEnv *, jobject, 
    jcharArray, jint, jint, jlong, jint);


#ifdef __cplusplus
}
#endif

#endif /* _ORG_APACHE_HARMONY_NIOCHAR_CHARSET_ADDITIONAL_IBM500_DECODER_H */


/* Header for class org.apache.harmony.niochar.charset.additional.IBM500$Encoder */

#ifndef _ORG_APACHE_HARMONY_NIOCHAR_CHARSET_ADDITIONAL_IBM500_ENCODER_H
#define _ORG_APACHE_HARMONY_NIOCHAR_CHARSET_ADDITIONAL_IBM500_ENCODER_H

#ifdef __cplusplus
extern "C" {
#endif


/* Static final fields */

#undef org_apache_harmony_niochar_charset_additional_IBM500_Encoder_INIT
#define org_apache_harmony_niochar_charset_additional_IBM500_Encoder_INIT 0L

#undef org_apache_harmony_niochar_charset_additional_IBM500_Encoder_ONGOING
#define org_apache_harmony_niochar_charset_additional_IBM500_Encoder_ONGOING 1L

#undef org_apache_harmony_niochar_charset_additional_IBM500_Encoder_END
#define org_apache_harmony_niochar_charset_additional_IBM500_Encoder_END 2L

#undef org_apache_harmony_niochar_charset_additional_IBM500_Encoder_FLUSH
#define org_apache_harmony_niochar_charset_additional_IBM500_Encoder_FLUSH 3L


/* Native methods */

/*
 * Method: org.apache.harmony.niochar.charset.additional.IBM500$Encoder.nEncode(JI[CI[I)V
 */
JNIEXPORT void JNICALL
Java_org_apache_harmony_niochar_charset_additional_IBM500_00024Encoder_nEncode(JNIEnv *, jobject, 
    jlong, jint, jcharArray, jint, jintArray);


#ifdef __cplusplus
}
#endif

#endif /* _ORG_APACHE_HARMONY_NIOCHAR_CHARSET_ADDITIONAL_IBM500_ENCODER_H */

