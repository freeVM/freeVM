/*
 *  Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
#ifndef __ANNOTATION_H__
#define __ANNOTATION_H__

struct String;

enum AnnotationValueType {
  //  'B', 'C', 'D', 'F', 'I', 'J', 'S', and 'Z' 's' 'e' 'c' '@' '['
    AVT_BYTE    = 'B',
    AVT_CHAR    = 'C',
    AVT_DOUBLE  = 'D',
    AVT_FLOAT   = 'F',
    AVT_INT     = 'I',
    AVT_LONG    = 'J',
    AVT_SHORT   = 'S',
    AVT_BOOLEAN = 'Z',
    AVT_STRING  = 's',
    AVT_ENUM    = 'e',
    AVT_CLASS   = 'c',
    AVT_ANNOTN  = '@',
    AVT_ARRAY   = '['
};

// forward declarations
struct Annotation;

///////////////////////////////////////////////////////////////////////////////
// Constant Java values
///////////////////////////////////////////////////////////////////////////////
union Const_Java_Value {
    uint32 i;
    int64 j;
    struct {
        uint32 lo_bytes;
        uint32 hi_bytes;
    } l;
    float f;
    double d;
    String* string;
    void* object;
};


// element-value pair of an annotation
struct AnnotationValue {
    union {
        Const_Java_Value const_value;
        String* class_name;
        Annotation* nested;
        struct {
            String* type;
            String* name;
        } enum_const;
        struct {
            AnnotationValue* items;
            uint16 length;
        } array;
    };
    AnnotationValueType tag;
};

struct AnnotationElement {
    String* name;
    AnnotationValue value;
};

struct Annotation {
    String* type;
    AnnotationElement* elements;
    uint16 num_elements;
};

struct AnnotationTable {
    uint16 length;
    Annotation* table[1];
};

#endif

