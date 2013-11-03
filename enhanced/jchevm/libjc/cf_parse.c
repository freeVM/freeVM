
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * $Id$
 */

#include "libjc.h"

/* Internal functions */
static void	_jc_parse_cpool(_jc_cf_parse_state *s, _jc_classfile *cfile);
static void	_jc_parse_constant(_jc_cf_parse_state *s, _jc_cf_constant *cp);
static void	_jc_parse_field(_jc_cf_parse_state *s, _jc_cf_field *field);
static void	_jc_parse_method(_jc_cf_parse_state *s, _jc_cf_method *method);
static void	_jc_parse_attribute(_jc_cf_parse_state *s, _jc_cf_attr *attr);
static void	_jc_parse_inner_class(_jc_cf_parse_state *s,
			_jc_cf_inner_class *inner);
static void	_jc_parse_class(_jc_cf_parse_state *s, const char **classp,
			int optional);
static void	_jc_parse_fieldref(_jc_cf_parse_state *s, _jc_cf_ref **refp);
static void	_jc_parse_methodref(_jc_cf_parse_state *s, _jc_cf_ref **refp);
static void	_jc_parse_interfacemethodref(_jc_cf_parse_state *s,
			_jc_cf_ref **refp);
static void	_jc_parse_bytecode(_jc_cf_parse_state *s, _jc_cf_code *code,
			_jc_uint16 *offset_map, _jc_uint16 length);
static void	_jc_map_offset(_jc_cf_parse_state *s, _jc_cf_code *code,
			_jc_uint16 length, _jc_uint16 *offset_map,
			_jc_uint16 *targetp);
static void	_jc_parse_local8(_jc_cf_parse_state *s, _jc_cf_code *code,
			_jc_uint16 *indexp);
static void	_jc_parse_local16(_jc_cf_parse_state *s, _jc_cf_code *code,
			_jc_uint16 *indexp);
static void	_jc_parse_cpool_index8(_jc_cf_parse_state *s, int types,
			_jc_cf_constant **ptr, int optional);
static void	_jc_parse_cpool_index16(_jc_cf_parse_state *s, int types,
			_jc_cf_constant **ptr, int optional);
static void	_jc_parse_cpool_index(_jc_cf_parse_state *s, int types,
			_jc_cf_constant **ptr, _jc_uint16 index);
static void	_jc_parse_string(_jc_cf_parse_state *s, const char **utfp,
			int optional);
static void	_jc_parse_integer(_jc_cf_parse_state *s, jint *value);
static void	_jc_parse_float(_jc_cf_parse_state *s, jfloat *valuep);
static void	_jc_parse_long(_jc_cf_parse_state *s, jlong *valuep);
static void	_jc_parse_double(_jc_cf_parse_state *s, jdouble *valuep);
static void	_jc_parse_utf8(_jc_cf_parse_state *s, const u_char **utfp,
			_jc_uint16 *lengthp);
static void	_jc_parse_uint32(_jc_cf_parse_state *s, _jc_uint32 *valuep);
static void	_jc_parse_uint16(_jc_cf_parse_state *s, _jc_uint16 *valuep);
static void	_jc_parse_uint8(_jc_cf_parse_state *s, u_char *valuep);
static void	_jc_scan_constant(_jc_cf_parse_state *s, size_t *lenp);
static void	*_jc_cf_alloc(_jc_cf_parse_state *s, size_t size);
static void	*_jc_cf_zalloc(_jc_cf_parse_state *s, size_t size);
static void	_jc_sub_state(_jc_cf_parse_state *s, _jc_cf_parse_state *t,
			size_t length);
static int	_jc_field_sorter(const void *item1, const void *item2);
static int	_jc_method_sorter(const void *item1, const void *item2);

/*
 * Parse a class file and do some basic validation checks.
 *
 * 'howmuch' determines how much to parse:
 *
 *	0	Just enough to get class name
 *	1	Class name, superclass and superinterfaces
 *	2	The whole thing
 *
 * If unsuccessful an exception is stored.
 */
_jc_classfile *
_jc_parse_classfile(_jc_env *env, _jc_classbytes *bytes, int howmuch)
{
	_jc_classfile *cfile;
	_jc_cf_parse_state s;
	_jc_uint32 magic;
	int i;

	/* Initialize parse state */
	memset(&s, 0, sizeof(s));
	s.env = env;
	s.bytes = bytes->bytes;
	s.length = bytes->length;

	/* Create new classfile object */
	if ((cfile = _jc_vm_zalloc(env, sizeof(*cfile))) == NULL)
		return NULL;
	_jc_uni_alloc_init(&cfile->uni, 1, NULL);
	s.cfile = cfile;

	/* Catch errors here */
	if (sigsetjmp(s.jump, JNI_FALSE) != 0) {
		_jc_destroy_classfile(&cfile);
		return NULL;
	}

	/* Parse initial stuff */
	_jc_parse_uint32(&s, &magic);
	if (_JC_UNLIKELY(magic != 0xcafebabe)) {
		_JC_EX_STORE(env, ClassFormatError,
		    "invalid magic number 0x%08x != 0x%08x", magic, 0xcafebabe);
		siglongjmp(s.jump, 1);
	}
	_jc_parse_uint16(&s, &cfile->minor_version);
	_jc_parse_uint16(&s, &cfile->major_version);
	if (!((cfile->major_version == 45 && cfile->minor_version >= 3)
	    || (cfile->major_version >= 46 && cfile->major_version <= 49))) {
		_JC_EX_STORE(env, UnsupportedClassVersionError,
		    "%u.%u", cfile->major_version, cfile->minor_version);
		siglongjmp(s.jump, 1);
	}

	/* Parse constant pool */
	_jc_parse_cpool(&s, cfile);

	/* Get access flags and name */
	_jc_parse_uint16(&s, &cfile->access_flags);
	_jc_parse_class(&s, &cfile->name, JNI_FALSE);

	/* Check stuff */
	if (*cfile->name == '[' || strchr(cfile->name, '.') != NULL) {
		_JC_EX_STORE(env, ClassFormatError,
		    "invalid class name `%s'", cfile->name);
		siglongjmp(s.jump, 1);
	}
	if (_JC_ACC_TEST(cfile, INTERFACE)) {
		/*
		 * Note: _JC_ACC_SUPER should not be allowed for interfaces
		 * (JVMS 4.1) but we allow it here because jikes 1.15 sets it.
		 * In addition, some compilers fail to add _JC_ACC_ABSTRACT.
		 */
		if ((cfile->access_flags
		      & ~(_JC_ACC_ABSTRACT|_JC_ACC_PUBLIC|_JC_ACC_SUPER))
		    != _JC_ACC_INTERFACE) {
			_JC_EX_STORE(env, ClassFormatError,
			    "invalid interface access flags 0x%04x",
			    cfile->access_flags);
			siglongjmp(s.jump, 1);
		}
	} else {
		if (_JC_ACC_TEST(cfile, FINAL)
		    && _JC_ACC_TEST(cfile, ABSTRACT)) {
			_JC_EX_STORE(env, ClassFormatError,
			    "invalid class access flags 0x%04x",
			    cfile->access_flags);
			siglongjmp(s.jump, 1);
		}
	}

	/* Check for partial parsing */
	if (howmuch <= 0)
		return cfile;

	/* Get superclass; special case java/lang/Object */
	if (strcmp(cfile->name, "java/lang/Object") == 0) {
		_jc_uint16 cp_index;

		_jc_parse_uint16(&s, &cp_index);
		if (cp_index != 0) {
			_JC_EX_STORE(env, ClassFormatError,
			    "superclass specified for `%s'", cfile->name);
			siglongjmp(s.jump, 1);
		}
		if ((cfile->access_flags & (_JC_ACC_PUBLIC|_JC_ACC_ABSTRACT
		    |_JC_ACC_INTERFACE|_JC_ACC_FINAL)) != _JC_ACC_PUBLIC) {
			_JC_EX_STORE(env, ClassFormatError,
			    "invalid class access flags 0x%04x for `%s'",
			    cfile->access_flags, cfile->name);
			siglongjmp(s.jump, 1);
		}
	} else {
		_jc_parse_class(&s, &cfile->superclass, JNI_FALSE);
		if (*cfile->superclass == '[') {
			_JC_EX_STORE(env, ClassFormatError,
			    "invalid superclass `%s'", cfile->superclass);
			siglongjmp(s.jump, 1);
		}
	}

	/* Parse interfaces */
	_jc_parse_uint16(&s, &cfile->num_interfaces);
	if (cfile->num_interfaces > 0) {
		cfile->interfaces = _jc_cf_zalloc(&s,
		    cfile->num_interfaces * sizeof(*cfile->interfaces));
	}
	for (i = 0; i < cfile->num_interfaces; i++) {
		_jc_parse_class(&s, &cfile->interfaces[i], JNI_FALSE);
		if (*cfile->interfaces[i] == '[') {
			_JC_EX_STORE(env, ClassFormatError,
			    "invalid superinterface `%s'",
			    cfile->interfaces[i]);
			siglongjmp(s.jump, 1);
		}
	}

	/* Check for partial parsing */
	if (howmuch <= 1)
		return cfile;

	/* Parse fields */
	_jc_parse_uint16(&s, &cfile->num_fields);
	if (cfile->num_fields > 0) {
		cfile->fields = _jc_cf_zalloc(&s,
		    cfile->num_fields * sizeof(*cfile->fields));
	}
	for (i = 0; i < cfile->num_fields; i++)
		_jc_parse_field(&s, &cfile->fields[i]);

	/* Sort fields */
	qsort(cfile->fields, cfile->num_fields,
	    sizeof(*cfile->fields), _jc_field_sorter);

	/* Parse methods */
	_jc_parse_uint16(&s, &cfile->num_methods);
	if (cfile->num_methods > 0) {
		cfile->methods = _jc_cf_zalloc(&s,
		    cfile->num_methods * sizeof(*cfile->methods));
	}
	for (i = 0; i < cfile->num_methods; i++)
		_jc_parse_method(&s, &cfile->methods[i]);

	/* Sort methods */
	qsort(cfile->methods, cfile->num_methods,
	    sizeof(*cfile->methods), _jc_method_sorter);

	/* Parse attributes */
	_jc_parse_uint16(&s, &cfile->num_attributes);
	if (cfile->num_attributes > 0) {
		cfile->attributes = _jc_cf_zalloc(&s,
		    cfile->num_attributes * sizeof(*cfile->attributes));
	}
	for (i = 0; i < cfile->num_attributes; i++) {
		void *const mark = _jc_uni_mark(&s.cfile->uni);
		_jc_cf_attr *const attr = &cfile->attributes[i];

		_jc_parse_attribute(&s, attr);
		if (strcmp(attr->name, _JC_CF_INNER_CLASSES) == 0)
			cfile->inner_classes = &attr->u.InnerClasses;
		else if (strcmp(attr->name, _JC_CF_SOURCE_FILE) == 0)
			cfile->source_file = attr->u.SourceFile;
		else
			_jc_uni_reset(&s.cfile->uni, mark);
	}

	/* Disallow any extra garbage in the class file */
	if (s.pos != s.length) {
		_JC_EX_STORE(env, ClassFormatError,
		    "extra garbage at end of classfile");
		siglongjmp(s.jump, 1);
	}

	/* Done */
	return cfile;
}

static void
_jc_parse_cpool(_jc_cf_parse_state *s, _jc_classfile *cfile)
{
	size_t strings_size;
	size_t cpool_start;
	int i;

	/* Get number of constants */
	_jc_parse_uint16(s, &cfile->num_constants);
	if (cfile->num_constants == 0) {
		_JC_EX_STORE(s->env, ClassFormatError,
		    "invalid constant pool count of zero");
		siglongjmp(s->jump, 1);
	}

	/* Allocate constants array */
	cfile->constants = _jc_cf_alloc(s,
	    (cfile->num_constants - 1) * sizeof(*cfile->constants));
	memset(cfile->constants, 0,
	    (cfile->num_constants - 1) * sizeof(*cfile->constants));

	/* Record constant types and add up UTF-8 string lengths */
	cpool_start = s->pos;
	for (strings_size = 0, i = 1; i < cfile->num_constants; i++) {
		_jc_cf_constant *const constant = &cfile->constants[i - 1];
		size_t const_size;

		constant->type = s->bytes[s->pos];
		_jc_scan_constant(s, &const_size);
		switch (constant->type) {
		case CONSTANT_Utf8:
			strings_size += (const_size - 3) + 1;
			break;
		case CONSTANT_Long:
		case CONSTANT_Double:
			if (++i >= cfile->num_constants) {
				_JC_EX_STORE(s->env, ClassFormatError,
				    "long/double constant at last index");
				siglongjmp(s->jump, 1);
			}
			break;
		default:
			break;
		}
	}

	/* Copy and nul-terminate all UTF-8 strings */
	if (strings_size > 0)
		cfile->string_mem = _jc_cf_alloc(s, strings_size);
	s->pos = cpool_start;
	for (strings_size = 0, i = 1; i < cfile->num_constants; i++) {
		_jc_cf_constant *const constant = &cfile->constants[i - 1];
		const u_char *utf;
		_jc_uint16 utf_len;

		switch (constant->type) {
		case CONSTANT_Utf8:
			s->pos++;
			_jc_parse_utf8(s, &utf, &utf_len);
			constant->u.Utf8 = cfile->string_mem + strings_size;
			memcpy(cfile->string_mem + strings_size, utf, utf_len);
			cfile->string_mem[strings_size + utf_len] = '\0';
			strings_size += utf_len + 1;
			break;
		case CONSTANT_Long:
		case CONSTANT_Double:
			i++;
			/* FALL THROUGH */
		default:
			_jc_scan_constant(s, NULL);
			break;
		}
	}

	/* Parse String and Class constants */
	s->pos = cpool_start;
	for (i = 1; i < cfile->num_constants; i++) {
		_jc_cf_constant *const constant = &cfile->constants[i - 1];

		switch (constant->type) {
		case CONSTANT_Class:
		case CONSTANT_String:
			_jc_parse_constant(s, constant);
			break;
		case CONSTANT_Long:
		case CONSTANT_Double:
			i++;
			/* FALL THROUGH */
		default:
			_jc_scan_constant(s, NULL);
			break;
		}
	}

	/* Parse NameAndType constants */
	s->pos = cpool_start;
	for (i = 1; i < cfile->num_constants; i++) {
		_jc_cf_constant *const constant = &cfile->constants[i - 1];

		switch (constant->type) {
		case CONSTANT_NameAndType:
			_jc_parse_constant(s, constant);
			break;
		case CONSTANT_Long:
		case CONSTANT_Double:
			i++;
			/* FALL THROUGH */
		default:
			_jc_scan_constant(s, NULL);
			break;
		}
	}

	/* Parse remaining constants */
	s->pos = cpool_start;
	for (i = 1; i < cfile->num_constants; i++) {
		_jc_cf_constant *const constant = &cfile->constants[i - 1];

		switch (constant->type) {
		case CONSTANT_Utf8:
		case CONSTANT_Class:
		case CONSTANT_String:
		case CONSTANT_NameAndType:
			_jc_scan_constant(s, NULL);
			break;
		case CONSTANT_Long:
		case CONSTANT_Double:
			i++;
			/* FALL THROUGH */
		default:
			_jc_parse_constant(s, constant);
			break;
		}
	}
}

static void
_jc_parse_constant(_jc_cf_parse_state *s, _jc_cf_constant *cp)
{
	_jc_cf_constant *cp2;

	_jc_parse_uint8(s, &cp->type);
	switch (cp->type) {
	case CONSTANT_Class:
		_jc_parse_cpool_index16(s, 1 << CONSTANT_Utf8, &cp2, JNI_FALSE);
		cp->u.Class = cp2->u.Utf8;
		break;
	case CONSTANT_Fieldref:
	case CONSTANT_Methodref:
	case CONSTANT_InterfaceMethodref:
		_jc_parse_cpool_index16(s,
		    1 << CONSTANT_Class, &cp2, JNI_FALSE);
		cp->u.Ref.class = cp2->u.Class;
		_jc_parse_cpool_index16(s,
		    1 << CONSTANT_NameAndType, &cp2, JNI_FALSE);
		cp->u.Ref.name = cp2->u.NameAndType.name;
		cp->u.Ref.descriptor = cp2->u.NameAndType.descriptor;
		break;
	case CONSTANT_String:
		_jc_parse_cpool_index16(s, 1 << CONSTANT_Utf8, &cp2, JNI_FALSE);
		cp->u.String = cp2->u.Utf8;
		break;
	case CONSTANT_Integer:
		_jc_parse_integer(s, &cp->u.Integer);
		break;
	case CONSTANT_Float:
		_jc_parse_float(s, &cp->u.Float);
		break;
	case CONSTANT_Long:
		_jc_parse_long(s, &cp->u.Long);
		break;
	case CONSTANT_Double:
		_jc_parse_double(s, &cp->u.Double);
		break;
	case CONSTANT_NameAndType:
		_jc_parse_cpool_index16(s, 1 << CONSTANT_Utf8, &cp2, JNI_FALSE);
		cp->u.NameAndType.name = cp2->u.Utf8;
		_jc_parse_cpool_index16(s, 1 << CONSTANT_Utf8, &cp2, JNI_FALSE);
		cp->u.NameAndType.descriptor = cp2->u.Utf8;
		break;
	case CONSTANT_Utf8:
		_jc_parse_utf8(s, NULL, NULL);
		break;
	default:
		_JC_EX_STORE(s->env, ClassFormatError,
		    "invalid constant pool entry type %u", cp->type);
		siglongjmp(s->jump, 1);
	}
}

static void
_jc_scan_constant(_jc_cf_parse_state *s, size_t *lenp)
{
	size_t length;

	if (s->pos >= s->length) {
		_JC_EX_STORE(s->env, ClassFormatError, "truncated class file");
		siglongjmp(s->jump, 1);
	}
	switch (s->bytes[s->pos]) {
	case CONSTANT_Class:
	case CONSTANT_String:
		length = 3;
		break;
	case CONSTANT_Fieldref:
	case CONSTANT_Methodref:
	case CONSTANT_InterfaceMethodref:
	case CONSTANT_Integer:
	case CONSTANT_Float:
	case CONSTANT_NameAndType:
		length = 5;
		break;
	case CONSTANT_Long:
	case CONSTANT_Double:
		length = 9;
		break;
	case CONSTANT_Utf8:
	    {
		_jc_uint16 utf_len;

		s->pos++;
		_jc_parse_utf8(s, NULL, &utf_len);
		if (lenp != NULL)
			*lenp = 3 + utf_len;
		return;
	    }
	default:
		_JC_EX_STORE(s->env, ClassFormatError,
		    "invalid constant pool entry type %u", s->bytes[s->pos]);
		siglongjmp(s->jump, 1);
	}

	/* Check length overflow */
	if (s->pos + length > s->length) {
		_JC_EX_STORE(s->env, ClassFormatError, "truncated class file");
		siglongjmp(s->jump, 1);
	}

	/* Done */
	if (lenp != NULL)
		*lenp = length;
	s->pos += length;
}

/*
 * Destroy a class file structure.
 */
void
_jc_destroy_classfile(_jc_classfile **cfilep)
{
	_jc_classfile *cfile = *cfilep;

	/* Sanity check */
	if (cfile == NULL)
		return;
	*cfilep = NULL;

	/* Free uni allocator memory and classfile */
	_jc_uni_alloc_free(&cfile->uni);
	_jc_vm_free(&cfile);
}

static void
_jc_parse_field(_jc_cf_parse_state *s, _jc_cf_field *field)
{
	int i;

	/* Parse the field */
	_jc_parse_uint16(s, &field->access_flags);
	_jc_parse_string(s, &field->name, JNI_FALSE);
	_jc_parse_string(s, &field->descriptor, JNI_FALSE);
	_jc_parse_uint16(s, &field->num_attributes);
	if (field->num_attributes > 0) {
		field->attributes = _jc_cf_zalloc(s,
		    field->num_attributes * sizeof(*field->attributes));
	}
	for (i = 0; i < field->num_attributes; i++) {
		void *const mark = _jc_uni_mark(&s->cfile->uni);
		_jc_cf_attr *const attr = &field->attributes[i];

		_jc_parse_attribute(s, attr);
		if (strcmp(attr->name, _JC_CF_CONSTANT_VALUE) == 0)
			field->initial_value = attr->u.ConstantValue;
		else
			_jc_uni_reset(&s->cfile->uni, mark);
	}

	/* Check stuff */
	if (_JC_ACC_TEST(field, PRIVATE)
	    + _JC_ACC_TEST(field, PROTECTED)
	    + _JC_ACC_TEST(field, PUBLIC) > 1) {
		_JC_EX_STORE(s->env, ClassFormatError,
		    "invalid access flags 0x%04x for field `%s'",
		    field->access_flags, field->name);
		siglongjmp(s->jump, 1);
	}
	if ((field->access_flags & (_JC_ACC_FINAL|_JC_ACC_VOLATILE))
	    == (_JC_ACC_FINAL|_JC_ACC_VOLATILE)) {
		_JC_EX_STORE(s->env, ClassFormatError,
		    "invalid access flags 0x%04x for field `%s'",
		    field->access_flags, field->name);
		siglongjmp(s->jump, 1);
	}
	if (_JC_ACC_TEST(s->cfile, INTERFACE)
	    && field->access_flags
	      != (_JC_ACC_PUBLIC|_JC_ACC_STATIC|_JC_ACC_FINAL)) {
		_JC_EX_STORE(s->env, ClassFormatError,
		    "invalid access flags 0x%04x for interface field `%s'",
		    field->access_flags, field->name);
		siglongjmp(s->jump, 1);
	}

	/* Check "ConstantValue" attributes */
	for (i = 0; i < field->num_attributes; i++) {
		_jc_cf_attr *const attr = &field->attributes[i];

		if (strcmp(attr->name, _JC_CF_CONSTANT_VALUE) == 0) {
			switch (attr->u.ConstantValue->type) {
			case CONSTANT_Integer:
				if (strcmp(field->descriptor, "Z") == 0
				    || strcmp(field->descriptor, "B") == 0
				    || strcmp(field->descriptor, "C") == 0
				    || strcmp(field->descriptor, "S") == 0
				    || strcmp(field->descriptor, "I") == 0)
					continue;
				break;
			case CONSTANT_Long:
				if (strcmp(field->descriptor, "J") == 0)
					continue;
				break;
			case CONSTANT_Float:
				if (strcmp(field->descriptor, "F") == 0)
					continue;
				break;
			case CONSTANT_Double:
				if (strcmp(field->descriptor, "D") == 0)
					continue;
				break;
			case CONSTANT_String:
				if (strcmp(field->descriptor,
				    "Ljava/lang/String;") == 0)
					continue;
				break;
			default:
				break;
			}
			_JC_EX_STORE(s->env, ClassFormatError,
			    "mismatched type for `%s' attribute of field `%s'",
			    _JC_CF_CONSTANT_VALUE, field->name);
			siglongjmp(s->jump, 1);
		}
	}
}

static void
_jc_parse_method(_jc_cf_parse_state *s, _jc_cf_method *method)
{
	int i;

	/* Parse the method */
	_jc_parse_uint16(s, &method->access_flags);
	_jc_parse_string(s, &method->name, JNI_FALSE);
	_jc_parse_string(s, &method->descriptor, JNI_FALSE);
	_jc_parse_uint16(s, &method->num_attributes);
	if (method->num_attributes > 0) {
		method->attributes = _jc_cf_zalloc(s,
		    method->num_attributes * sizeof(*method->attributes));
	}
	for (i = 0; i < method->num_attributes; i++) {
		void *const mark = _jc_uni_mark(&s->cfile->uni);
		_jc_cf_attr *const attr = &method->attributes[i];

		_jc_parse_attribute(s, attr);
		if (strcmp(attr->name, _JC_CF_CODE) == 0) {
			if (method->code != NULL) {
				_JC_EX_STORE(s->env, ClassFormatError,
				    "multiple `%s' attributes for method"
				    " `%s%s'", attr->name, method->name,
				    method->descriptor);
				siglongjmp(s->jump, 1);
			}
			method->code = &attr->u.Code;
		} else if (strcmp(attr->name, _JC_CF_EXCEPTIONS) == 0)
			method->exceptions = &attr->u.Exceptions;
		else
			_jc_uni_reset(&s->cfile->uni, mark);
	}
	if ((_JC_ACC_TEST(method, NATIVE) || _JC_ACC_TEST(method, ABSTRACT))
	    != (method->code == NULL)) {
		_JC_EX_STORE(s->env, ClassFormatError,
		    "%sconcrete method `%s%s' %s a `Code' attribute",
		    method->code != NULL ? "non-" : "", method->name,
		    method->descriptor, method->code == NULL ?
		      "contains" : "is missing");
		siglongjmp(s->jump, 1);
	}

	/* Check stuff */
	if (strcmp(method->name, "<init>") == 0
	    && (method->access_flags & ~(_JC_ACC_PRIVATE|_JC_ACC_PROTECTED
	      |_JC_ACC_PUBLIC|_JC_ACC_STRICT|_JC_ACC_SYNTHETIC)) != 0) {
		_JC_EX_STORE(s->env, ClassFormatError,
		    "invalid access flags 0x%04x for method `%s%s'",
		    method->access_flags, method->name, method->descriptor);
		siglongjmp(s->jump, 1);
	}
	if (strcmp(method->name, "<clinit>") == 0
	    && !_JC_ACC_TEST(method, STATIC)) {
		_JC_EX_STORE(s->env, ClassFormatError,
		    "invalid access flags 0x%04x for method `%s%s'",
		    method->access_flags, method->name, method->descriptor);
		siglongjmp(s->jump, 1);
	}

	if (_JC_ACC_TEST(method, PRIVATE)
	    + _JC_ACC_TEST(method, PROTECTED)
	    + _JC_ACC_TEST(method, PUBLIC) > 1) {
		_JC_EX_STORE(s->env, ClassFormatError,
		    "invalid access flags 0x%04x for method `%s%s'",
		    method->access_flags, method->name, method->descriptor);
		siglongjmp(s->jump, 1);
	}
	if (_JC_ACC_TEST(method, ABSTRACT)
	    && (method->access_flags & (_JC_ACC_FINAL|_JC_ACC_NATIVE
	      |_JC_ACC_PRIVATE|_JC_ACC_STATIC|_JC_ACC_STRICT
	      |_JC_ACC_SYNCHRONIZED)) != 0) {
		_JC_EX_STORE(s->env, ClassFormatError,
		    "invalid access flags 0x%04x for method `%s%s'",
		    method->access_flags, method->name, method->descriptor);
		siglongjmp(s->jump, 1);
	}
	if (_JC_ACC_TEST(s->cfile, INTERFACE)
	    && strcmp(method->name, "<clinit>") != 0
	    && method->access_flags != (_JC_ACC_ABSTRACT|_JC_ACC_PUBLIC)) {
		_JC_EX_STORE(s->env, ClassFormatError,
		    "invalid access flags 0x%04x for interface method `%s%s'",
		    method->access_flags, method->name, method->descriptor);
		siglongjmp(s->jump, 1);
	}
}

static void
_jc_parse_attribute(_jc_cf_parse_state *s, _jc_cf_attr *attr)
{
	_jc_cf_parse_state t;

	/* Parse attribute name and length */
	_jc_parse_string(s, &attr->name, JNI_FALSE);
	_jc_parse_uint32(s, &attr->length);
	if (s->pos + attr->length > s->length) {
		_JC_EX_STORE(s->env, ClassFormatError, "truncated class file");
		siglongjmp(s->jump, 1);
	}

	/* Initialize parsing of attribute */
	_jc_sub_state(s, &t, attr->length);
	s->pos += attr->length;

	/* Further parse individual attributes */
	if (strcmp(attr->name, _JC_CF_CONSTANT_VALUE) == 0) {
		_jc_cf_constant *cp;

		if (attr->length != 2) {
			_JC_EX_STORE(s->env, ClassFormatError,
			    "invalid `%s' attribute length %u != %u",
			    attr->name, attr->length, 2);
			siglongjmp(s->jump, 1);
		}
		_jc_parse_cpool_index16(&t,
		    (1 << CONSTANT_Long)
		      | (1 << CONSTANT_Float)
		      | (1 << CONSTANT_Double)
		      | (1 << CONSTANT_Integer)
		      | (1 << CONSTANT_String),
		    &cp, JNI_FALSE);
		attr->u.ConstantValue = cp;
	} else if (strcmp(attr->name, _JC_CF_SOURCE_FILE) == 0) {
		if (attr->length != 2) {
			_JC_EX_STORE(s->env, ClassFormatError,
			    "invalid `%s' attribute length %u != %u",
			    attr->name, attr->length, 2);
			siglongjmp(s->jump, 1);
		}
		_jc_parse_string(&t, &attr->u.SourceFile, JNI_FALSE);
	} else if (strcmp(attr->name, _JC_CF_EXCEPTIONS) == 0) {
		_jc_cf_exceptions *const etab = &attr->u.Exceptions;
		int i;

		_jc_parse_uint16(&t, &etab->num_exceptions);
		if (attr->length != 2 + 2 * etab->num_exceptions) {
			_JC_EX_STORE(s->env, ClassFormatError,
			    "invalid `%s' attribute length %u != %u",
			    attr->name, attr->length,
			    2 + 2 * etab->num_exceptions);
			siglongjmp(s->jump, 1);
		}
		etab->exceptions = _jc_cf_zalloc(&t,
		    etab->num_exceptions * sizeof(*etab->exceptions));
		for (i = 0; i < etab->num_exceptions; i++)
			_jc_parse_class(&t, &etab->exceptions[i], JNI_FALSE);
	} else if (strcmp(attr->name, _JC_CF_INNER_CLASSES) == 0) {
		_jc_cf_inner_classes *const itab = &attr->u.InnerClasses;
		int i;

		_jc_parse_uint16(&t, &itab->num_classes);
		if (attr->length != 2 + 8 * itab->num_classes) {
			_JC_EX_STORE(s->env, ClassFormatError,
			    "invalid `%s' attribute length %u != %u",
			    attr->name, attr->length,
			    2 + 8 * itab->num_classes);
			siglongjmp(s->jump, 1);
		}
		itab->classes = _jc_cf_zalloc(&t,
		    itab->num_classes * sizeof(*itab->classes));
		for (i = 0; i < itab->num_classes; i++)
			_jc_parse_inner_class(&t, &itab->classes[i]);
	} else if (strcmp(attr->name, _JC_CF_CODE) == 0) {
		_jc_cf_bytecode *const code = &attr->u.Code;

		code->bytecode = _jc_cf_alloc(&t, t.length);
		memcpy(code->bytecode, t.bytes, t.length);
		code->length = t.length;
	} else if (strcmp(attr->name, _JC_CF_LINE_NUMBER_TABLE) == 0) {
		_jc_cf_linenums *const ltab = &attr->u.LineNumberTable;
		int i;

		_jc_parse_uint16(&t, &ltab->length);
		ltab->linenums = _jc_cf_alloc(&t,
		    ltab->length * sizeof(*ltab->linenums));
		for (i = 0; i < ltab->length; i++) {
			_jc_cf_linenum *const lnum = &ltab->linenums[i];

			_jc_parse_uint16(&t, &lnum->offset);
			_jc_parse_uint16(&t, &lnum->line);
		}
	}
}

static void
_jc_parse_inner_class(_jc_cf_parse_state *s, _jc_cf_inner_class *inner)
{
	_jc_parse_class(s, &inner->inner, JNI_TRUE);
	_jc_parse_class(s, &inner->outer, JNI_TRUE);
	_jc_parse_string(s, &inner->name, JNI_TRUE);
	_jc_parse_uint16(s, &inner->access_flags);
}

/*
 * Parse a "Code" attribute, including bytecode.
 *
 * The parsed code remains valid only as long as "cfile" does.
 *
 * Stores an exception on failure.
 */
int
_jc_parse_code(_jc_env *env, _jc_classfile *cfile,
	_jc_cf_bytecode *bytecode, _jc_cf_code *code)
{
	_jc_cf_parse_state state;
	_jc_cf_parse_state *const s = &state;
	_jc_uint16 *offset_map;
	_jc_uint16 code_length;
	_jc_uint16 num_attrs;
	_jc_uint32 u32;
	void *mark;
	int i;

	/* Initialize parse state */
	memset(s, 0, sizeof(*s));
	memset(code, 0, sizeof(*code));
	s->env = env;
	s->cfile = cfile;
	s->bytes = bytecode->bytecode;
	s->length = bytecode->length;
	s->pos = 0;

	/* Mark uni allocator */
	mark = _jc_uni_mark(&s->cfile->uni);

	/* Catch errors here */
	if (sigsetjmp(s->jump, JNI_FALSE) != 0) {
		_jc_uni_reset(&s->cfile->uni, mark);
		return JNI_ERR;
	}

	/* Parse bytecode meta-info */
	_jc_parse_uint16(s, &code->max_stack);
	_jc_parse_uint16(s, &code->max_locals);
	_jc_parse_uint32(s, &u32);
	if ((_jc_uint16)u32 != u32) {
		_JC_EX_STORE(s->env, ClassFormatError,
		    "Illegal `%s' attribute bytecode length %u",
		    _JC_CF_CODE, (unsigned int)u32);
		siglongjmp(s->jump, 1);
	}
	code_length = (_jc_uint16)u32;
	if (s->pos + code_length > s->length) {
		_JC_EX_STORE(s->env, ClassFormatError,
		    "`Code' attribute bytecode length overflow");
		siglongjmp(s->jump, 1);
	}

	/* Allocate bytecode offset -> instruction index map and insns array */
	if ((offset_map = _jc_vm_zalloc(s->env,
	    code_length * sizeof(*offset_map))) == NULL)
		siglongjmp(s->jump, 1);
	code->insns = _jc_cf_zalloc(s, code_length * sizeof(*code->insns));

	/* Parse bytecode */
	_jc_parse_bytecode(s, code, offset_map, code_length);

	/* Parse trap table */
	_jc_parse_uint16(s, &code->num_traps);
	if (s->pos + code->num_traps * 8 > s->length) {
		_JC_EX_STORE(s->env, ClassFormatError, "truncated class file");
		goto fail;
	}
	if (code->num_traps > 0) {
		code->traps = _jc_cf_zalloc(s,
		    code->num_traps * sizeof(*code->traps));
	}
	for (i = 0; i < code->num_traps; i++) {
		_jc_cf_trap *const trap = &code->traps[i];

		_jc_parse_uint16(s, &trap->start);
		_jc_map_offset(s, code, code_length, offset_map, &trap->start);
		_jc_parse_uint16(s, &trap->end);
		if (trap->end == code_length)
			trap->end = code->num_insns;
		else {
			_jc_map_offset(s, code, code_length,
			    offset_map, &trap->end);
		}
		_jc_parse_uint16(s, &trap->target);
		_jc_map_offset(s, code, code_length, offset_map, &trap->target);
		if (trap->end <= trap->start) {
			_JC_EX_STORE(s->env, ClassFormatError,
			    "invalid trap table entry");
			goto fail;
		}
		_jc_parse_class(s, &trap->type, JNI_TRUE);
	}

	/* Find and parse the line number table (if any) */
	_jc_parse_uint16(s, &num_attrs);
	for (i = 0; i < num_attrs; i++) {
		void *const mark = _jc_uni_mark(&s->cfile->uni);
		_jc_cf_linenums *linenums;
		_jc_cf_attr attr;
		int j;

		/* Look for "LineNumberTable" */
		memset(&attr, 0, sizeof(attr));
		_jc_parse_attribute(s, &attr);
		if (strcmp(attr.name, _JC_CF_LINE_NUMBER_TABLE) != 0) {
			_jc_uni_reset(&s->cfile->uni, mark);
			continue;
		}

		/* We overwrite the _jc_cf_linenum's with _jc_cf_linemap's! */
		_JC_ASSERT(sizeof(*code->linemaps)
		    <= sizeof(*linenums->linenums));
		linenums = &attr.u.LineNumberTable;
		code->num_linemaps = linenums->length;
		code->linemaps = (_jc_cf_linemap *)linenums->linenums;

		/* Fill in map with offsets converted to instruction indicies */
		for (j = 0; j < linenums->length; j++) {
			const _jc_cf_linenum linenum = linenums->linenums[j];
			_jc_cf_linemap *const linemap = &code->linemaps[j];

			linemap->index = linenum.offset;
			_jc_map_offset(s, code, code_length,
			    offset_map, &linemap->index);
			linemap->line = linenum.line;
		}

		/* Done */
		break;
	}

	/* Done */
	_jc_vm_free(&offset_map);
	return JNI_OK;

fail:
	/* Bail out */
	_jc_vm_free(&offset_map);
	siglongjmp(s->jump, 1);
}

/*
 * Parse Java bytecode.
 */
static void
_jc_parse_bytecode(_jc_cf_parse_state *s, _jc_cf_code *code,
	_jc_uint16 *offset_map, _jc_uint16 code_length)
{
	const size_t start = s->pos;
	const size_t end = s->pos + code_length;
	_jc_cf_insn *insn = code->insns;
	_jc_uint32 insn_offset;
	_jc_uint16 value16;
	u_char value8;
	int inum = 0;
	int i;

	/* Mapping from offset -> instruction should be zeroed */
	_JC_ASSERT(*offset_map == 0);

loop:
	/* Sanity check */
	_JC_ASSERT(code->num_insns == 0);
	_JC_ASSERT(s->pos <= end);
	_JC_ASSERT(inum <= code_length);
	_JC_ASSERT(inum == insn - code->insns);

	/* Done? */
	if (s->pos == end) {
		code->num_insns = inum;
		goto pass2;
	}

	/* Get opcode and save its bytecode offset in the offset map */
	insn_offset = s->pos - start;
	offset_map[insn_offset] = inum;
	insn->opcode = s->bytes[s->pos++];

	/* Is opcode valid? */
	if (_jc_bytecode_names[insn->opcode] == NULL) {
		_JC_EX_STORE(s->env, ClassFormatError,
		    "invalid opcode 0x%02x", insn->opcode);
		siglongjmp(s->jump, 1);
	}

	/* Decode instructions, except leave target offsets alone */
	switch (insn->opcode) {
	case _JC_aload:
	case _JC_astore:
	case _JC_dload:
	case _JC_dstore:
	case _JC_fload:
	case _JC_fstore:
	case _JC_iload:
	case _JC_istore:
	case _JC_lload:
	case _JC_lstore:
	case _JC_ret:
		_jc_parse_local8(s, code, &insn->u.local.index);
		break;
	case _JC_aload_0:
	case _JC_aload_1:
	case _JC_aload_2:
	case _JC_aload_3:
		insn->u.local.index = insn->opcode - _JC_aload_0;
		break;
	case _JC_astore_0:
	case _JC_astore_1:
	case _JC_astore_2:
	case _JC_astore_3:
		insn->u.local.index = insn->opcode - _JC_astore_0;
		break;
	case _JC_dload_0:
	case _JC_dload_1:
	case _JC_dload_2:
	case _JC_dload_3:
		insn->u.local.index = insn->opcode - _JC_dload_0;
		break;
	case _JC_dstore_0:
	case _JC_dstore_1:
	case _JC_dstore_2:
	case _JC_dstore_3:
		insn->u.local.index = insn->opcode - _JC_dstore_0;
		break;
	case _JC_fload_0:
	case _JC_fload_1:
	case _JC_fload_2:
	case _JC_fload_3:
		insn->u.local.index = insn->opcode - _JC_fload_0;
		break;
	case _JC_fstore_0:
	case _JC_fstore_1:
	case _JC_fstore_2:
	case _JC_fstore_3:
		insn->u.local.index = insn->opcode - _JC_fstore_0;
		break;
	case _JC_iload_0:
	case _JC_iload_1:
	case _JC_iload_2:
	case _JC_iload_3:
		insn->u.local.index = insn->opcode - _JC_iload_0;
		break;
	case _JC_istore_0:
	case _JC_istore_1:
	case _JC_istore_2:
	case _JC_istore_3:
		insn->u.local.index = insn->opcode - _JC_istore_0;
		break;
	case _JC_lload_0:
	case _JC_lload_1:
	case _JC_lload_2:
	case _JC_lload_3:
		insn->u.local.index = insn->opcode - _JC_lload_0;
		break;
	case _JC_lstore_0:
	case _JC_lstore_1:
	case _JC_lstore_2:
	case _JC_lstore_3:
		insn->u.local.index = insn->opcode - _JC_lstore_0;
		break;
	case _JC_anewarray:
	case _JC_checkcast:
	case _JC_instanceof:
	case _JC_new:
		_jc_parse_class(s, &insn->u.type.name, JNI_FALSE);
		break;
	case _JC_bipush:
		_jc_parse_uint8(s, &value8);
		insn->u.immediate.value = (signed char)value8;
		break;
	case _JC_getfield:
	case _JC_getstatic:
	case _JC_putfield:
	case _JC_putstatic:
		_jc_parse_fieldref(s, &insn->u.fieldref.field);
		break;
	case _JC_goto:
	case _JC_if_acmpeq:
	case _JC_if_acmpne:
	case _JC_if_icmpeq:
	case _JC_if_icmpne:
	case _JC_if_icmplt:
	case _JC_if_icmpge:
	case _JC_if_icmpgt:
	case _JC_if_icmple:
	case _JC_ifeq:
	case _JC_ifne:
	case _JC_iflt:
	case _JC_ifge:
	case _JC_ifgt:
	case _JC_ifle:
	case _JC_ifnonnull:
	case _JC_ifnull:
	case _JC_jsr:
		_jc_parse_uint16(s, &value16);
		insn->u.branch.target = insn_offset + (jshort)value16;
		break;
	case _JC_jsr_w:
	case _JC_goto_w:
	    {
		jint value32;

		_JC_ASSERT(_JC_jsr_w - _JC_jsr == _JC_goto_w - _JC_goto);
		insn->opcode -= _JC_jsr_w - _JC_jsr;
		_jc_parse_integer(s, &value32);
		insn->u.branch.target = insn_offset + value32;
		break;
	    }
	case _JC_iinc:
		_jc_parse_local8(s, code, &insn->u.iinc.index);
		_jc_parse_uint8(s, &value8);
		insn->u.iinc.value = (signed char)value8;
		break;
	case _JC_invokeinterface:
		_jc_parse_interfacemethodref(s,
		    &insn->u.invoke.method);
		_jc_parse_uint8(s, &value8);
		_jc_parse_uint8(s, &value8);
		break;
	case _JC_invokespecial:
	case _JC_invokestatic:
	case _JC_invokevirtual:
		_jc_parse_methodref(s, &insn->u.invoke.method);
		break;
	case _JC_ldc:
		_jc_parse_cpool_index8(s, (1 << CONSTANT_Integer)
		      | (1 << CONSTANT_Float) | (1 << CONSTANT_String)
		      | (1 << CONSTANT_Class), &insn->u.constant, JNI_FALSE);
		break;
	case _JC_ldc_w:
		_jc_parse_cpool_index16(s, (1 << CONSTANT_Integer)
		      | (1 << CONSTANT_Float) | (1 << CONSTANT_String)
		      | (1 << CONSTANT_Class), &insn->u.constant, JNI_FALSE);
		insn->opcode = _JC_ldc;
		break;
	case _JC_ldc2_w:
		_jc_parse_cpool_index16(s,
		    (1 << CONSTANT_Long) | (1 << CONSTANT_Double),
		    &insn->u.constant, JNI_FALSE);
		insn->opcode = _JC_ldc2_w;
		break;
	case _JC_lookupswitch:
	    {
		const char *const opname = _jc_bytecode_names[insn->opcode];
		_jc_cf_lookupswitch *lsw;
		_jc_uint16 default_target;
		jint num_pairs;
		jint value32;
		int pad;

		/* Parse padding */
		pad = 3 - (((s->pos - start) + 3) % 4);
		for (i = 0; i < pad; i++) {
			_jc_parse_uint8(s, &value8);
			if (value8 != 0) {
				_JC_EX_STORE(s->env, ClassFormatError,
				    "non-zero %s pad byte", opname);
				siglongjmp(s->jump, 1);
			}
		}

		/* Parse default target offset */
		_jc_parse_integer(s, &value32);
		default_target = insn_offset + value32;

		/* Parse number of pairs */
		_jc_parse_integer(s, &num_pairs);
		if (num_pairs < 0) {
			_JC_EX_STORE(s->env, ClassFormatError,
			    "invalid %s #pairs %d", opname, (int)num_pairs);
			siglongjmp(s->jump, 1);
		}

		/* Allocate structure */
		if ((lsw = _jc_cf_alloc(s,
		    sizeof(*lsw) + num_pairs * sizeof(*lsw->pairs))) == NULL)
			siglongjmp(s->jump, 1);
		insn->u.lookupswitch = lsw;
		lsw->default_target = default_target;
		lsw->num_pairs = num_pairs;

		/* Parse match pairs table */
		for (i = 0; i < lsw->num_pairs; i++) {
			_jc_cf_lookup *const lookup = &lsw->pairs[i];

			_jc_parse_integer(s, &lookup->match);
			if (i > 0 && (lookup - 1)->match >= lookup->match) {
				_JC_EX_STORE(s->env, ClassFormatError,
				    "misordered %s table", opname);
				siglongjmp(s->jump, 1);
			}
			_jc_parse_integer(s, &value32);
			lookup->target = insn_offset + value32;
		}
		break;
	    }
	case _JC_tableswitch:
	    {
		const char *const opname = _jc_bytecode_names[insn->opcode];
		_jc_cf_tableswitch *tsw;
		_jc_uint16 default_target;
		jint num_targets;
		jint value32;
		jint high;
		jint low;
		int pad;

		/* Parse padding */
		pad = 3 - (((s->pos - start) + 3) % 4);
		for (i = 0; i < pad; i++) {
			_jc_parse_uint8(s, &value8);
			if (value8 != 0) {
				_JC_EX_STORE(s->env, ClassFormatError,
				    "non-zero %s pad byte", opname);
				siglongjmp(s->jump, 1);
			}
		}

		/* Parse default target offset */
		_jc_parse_integer(s, &value32);
		default_target = insn_offset + value32;

		/* Parse bounds */
		_jc_parse_integer(s, &low);
		_jc_parse_integer(s, &high);
		if (high < low) {
			_JC_EX_STORE(s->env, ClassFormatError,
			    "reversed %s bounds", opname);
			siglongjmp(s->jump, 1);
		}
		num_targets = high - low + 1;

		/* Allocate structure */
		if ((tsw = _jc_cf_alloc(s, sizeof(*tsw)
		    + num_targets * sizeof(*tsw->targets))) == NULL)
			siglongjmp(s->jump, 1);
		insn->u.tableswitch = tsw;
		tsw->default_target = default_target;
		tsw->high = high;
		tsw->low = low;

		/* Parse targets */
		for (i = 0; i < num_targets; i++) {
			_jc_parse_integer(s, &value32);
			tsw->targets[i] = insn_offset + value32;
		}
		break;
	    }
	case _JC_multianewarray:
		_jc_parse_class(s, &insn->u.multianewarray.type, JNI_FALSE);
		_jc_parse_uint8(s, &insn->u.multianewarray.dims);
		break;
	case _JC_newarray:
		_jc_parse_uint8(s, &value8);
		switch (value8) {
		case _JC_boolean:
			insn->u.newarray.type = _JC_TYPE_BOOLEAN;
			break;
		case _JC_char:
			insn->u.newarray.type = _JC_TYPE_CHAR;
			break;
		case _JC_float:
			insn->u.newarray.type = _JC_TYPE_FLOAT;
			break;
		case _JC_double:
			insn->u.newarray.type = _JC_TYPE_DOUBLE;
			break;
		case _JC_byte:
			insn->u.newarray.type = _JC_TYPE_BYTE;
			break;
		case _JC_short:
			insn->u.newarray.type = _JC_TYPE_SHORT;
			break;
		case _JC_int:
			insn->u.newarray.type = _JC_TYPE_INT;
			break;
		case _JC_long:
			insn->u.newarray.type = _JC_TYPE_LONG;
			break;
		default:
			_JC_EX_STORE(s->env, ClassFormatError,
			    "invalide type %d for newarray", value8);
			siglongjmp(s->jump, 1);
		}
		break;
	case _JC_sipush:
		_jc_parse_uint16(s, &value16);
		insn->u.immediate.value = (jshort)value16;
		break;
	case _JC_wide:
		_jc_parse_uint8(s, &value8);
		switch (value8) {
		case _JC_aload:
		case _JC_astore:
		case _JC_dload:
		case _JC_dstore:
		case _JC_fload:
		case _JC_fstore:
		case _JC_iload:
		case _JC_istore:
		case _JC_lload:
		case _JC_lstore:
		case _JC_ret:
			insn->opcode = value8;
			_jc_parse_local16(s, code, &insn->u.local.index);
			break;
		case _JC_iinc:
			insn->opcode = value8;
			_jc_parse_local16(s, code, &insn->u.iinc.index);
			_jc_parse_uint16(s, &value16);
			insn->u.iinc.value = (jshort)value16;
			break;
		default:
			_JC_EX_STORE(s->env, ClassFormatError,
			    "invalid wide extension opcode 0x%02x", value8);
			siglongjmp(s->jump, 1);
		}
		break;
	default:
		break;
	}

	/* Advance */
	inum++;
	insn++;
	goto loop;

pass2:
	/* Convert target bytecode offsets into instruction indicies */
	for (i = 0; i < code->num_insns; i++) {
		_jc_cf_insn *const insn = &code->insns[i];

		switch (insn->opcode) {
		case _JC_goto:
		case _JC_if_acmpeq:
		case _JC_if_acmpne:
		case _JC_if_icmpeq:
		case _JC_if_icmpne:
		case _JC_if_icmplt:
		case _JC_if_icmpge:
		case _JC_if_icmpgt:
		case _JC_if_icmple:
		case _JC_ifeq:
		case _JC_ifne:
		case _JC_iflt:
		case _JC_ifge:
		case _JC_ifgt:
		case _JC_ifle:
		case _JC_ifnonnull:
		case _JC_ifnull:
		case _JC_jsr:
			_jc_map_offset(s, code, code_length,
			    offset_map, &insn->u.branch.target);
			break;
		case _JC_lookupswitch:
		    {
			_jc_cf_lookupswitch *const lsw = insn->u.lookupswitch;
			int j;

			_jc_map_offset(s, code, code_length,
			    offset_map, &lsw->default_target);
			for (j = 0; j < lsw->num_pairs; j++) {
				_jc_map_offset(s, code, code_length,
				    offset_map, &lsw->pairs[j].target);
			}
			break;
		    }
		case _JC_tableswitch:
		    {
			_jc_cf_tableswitch *const tsw = insn->u.tableswitch;
			const jint num_targets = tsw->high - tsw->low + 1;
			int j;

			_jc_map_offset(s, code, code_length,
			    offset_map, &tsw->default_target);
			for (j = 0; j < num_targets; j++) {
				_jc_map_offset(s, code, code_length,
				    offset_map, &tsw->targets[j]);
			}
			break;
		    }
		default:
			break;
		}
	}
}

/*
 * Convert a bytecode offset into an instruction index.
 */
static void
_jc_map_offset(_jc_cf_parse_state *s, _jc_cf_code *code, _jc_uint16 length,
	_jc_uint16 *offset_map, _jc_uint16 *targetp)
{
	_jc_uint16 target = *targetp;

	if (target == 0)
		return;
	if (target >= length || offset_map[target] == 0) {
		_JC_EX_STORE(s->env, ClassFormatError,
		    "invalid branch target %u", target);
		siglongjmp(s->jump, 1);
	}
	*targetp = offset_map[target];
}

/*
 * Parse an 8 bit local index.
 */
static void
_jc_parse_local8(_jc_cf_parse_state *s, _jc_cf_code *code, _jc_uint16 *indexp)
{
	u_char index;

	_jc_parse_uint8(s, &index);
	if (index >= code->max_locals) {
		_JC_EX_STORE(s->env, ClassFormatError,
		    "local index %u >= %u is out of range",
		    index, code->max_locals);
		siglongjmp(s->jump, 1);
	}
	*indexp = index;
}

/*
 * Parse an 16 bit local index.
 */
static void
_jc_parse_local16(_jc_cf_parse_state *s, _jc_cf_code *code, _jc_uint16 *indexp)
{
	_jc_uint16 index;

	_jc_parse_uint16(s, &index);
	if (index >= code->max_locals) {
		_JC_EX_STORE(s->env, ClassFormatError,
		    "local index %u >= %u is out of range",
		    index, code->max_locals);
		siglongjmp(s->jump, 1);
	}
	*indexp = index;
}

/*
 * Parse an 8 bit constant pool index.
 */
static void
_jc_parse_cpool_index8(_jc_cf_parse_state *s, int types,
	_jc_cf_constant **ptr, int optional)
{
	u_char cp_index;

	_jc_parse_uint8(s, &cp_index);
	if (cp_index == 0 && optional) {
		*ptr = NULL;
		return;
	}
	_jc_parse_cpool_index(s, types, ptr, cp_index);
}

/*
 * Parse a 16 bit constant pool index.
 */
static void
_jc_parse_cpool_index16(_jc_cf_parse_state *s, int types,
	_jc_cf_constant **ptr, int optional)
{
	_jc_uint16 cp_index;

	_jc_parse_uint16(s, &cp_index);
	if (cp_index == 0 && optional) {
		*ptr = NULL;
		return;
	}
	_jc_parse_cpool_index(s, types, ptr, cp_index);
}

static void
_jc_parse_cpool_index(_jc_cf_parse_state *s, int types,
	_jc_cf_constant **ptr, _jc_uint16 cp_index)
{
	_jc_classfile *const cfile = s->cfile;
	_jc_cf_constant *c;

	/* Map "uncompressed" constant index to real index */
	if (cp_index < 1 || cp_index >= cfile->num_constants) {
		_JC_EX_STORE(s->env, ClassFormatError,
		    "invalid constant pool index %u", cp_index);
		siglongjmp(s->jump, 1);
	}
	c = &cfile->constants[cp_index - 1];
	if (types != 0 && ((1 << c->type) & types) == 0) {
		_JC_EX_STORE(s->env, ClassFormatError,
		    "unexpected constant pool type %u at index %u",
		    c->type, cp_index);
		siglongjmp(s->jump, 1);
	}
	*ptr = c;
}

static void
_jc_parse_fieldref(_jc_cf_parse_state *s, _jc_cf_ref **refp)
{
	_jc_cf_constant *cp;

	_jc_parse_cpool_index16(s, 1 << CONSTANT_Fieldref, &cp, JNI_FALSE);
	*refp = &cp->u.Ref;
}

static void
_jc_parse_methodref(_jc_cf_parse_state *s, _jc_cf_ref **refp)
{
	_jc_cf_constant *cp;

	_jc_parse_cpool_index16(s, 1 << CONSTANT_Methodref, &cp, JNI_FALSE);
	*refp = &cp->u.Ref;
}

static void
_jc_parse_interfacemethodref(_jc_cf_parse_state *s, _jc_cf_ref **refp)
{
	_jc_cf_constant *cp;

	_jc_parse_cpool_index16(s,
	    1 << CONSTANT_InterfaceMethodref, &cp, JNI_FALSE);
	*refp = &cp->u.Ref;
}

static void
_jc_parse_class(_jc_cf_parse_state *s, const char **classp, int optional)
{
	_jc_cf_constant *cp;

	_jc_parse_cpool_index16(s, 1 << CONSTANT_Class, &cp, optional);
	*classp = (cp != NULL) ? cp->u.Class : NULL;
}

static void
_jc_parse_string(_jc_cf_parse_state *s, const char **utfp, int optional)
{
	_jc_cf_constant *cp;

	_jc_parse_cpool_index16(s, 1 << CONSTANT_Utf8, &cp, optional);
	*utfp = (cp != NULL) ? cp->u.Utf8 : NULL;
}

static void
_jc_parse_integer(_jc_cf_parse_state *s, jint *valuep)
{
	_jc_uint32 value;

	_jc_parse_uint32(s, &value);
	if (valuep != NULL)
		*valuep = (jint)value;
}

static void
_jc_parse_float(_jc_cf_parse_state *s, jfloat *valuep)
{
	u_char b[4];
	int i;

	for (i = 0; i < 4; i++)
		_jc_parse_uint8(s, &b[i]);
	if (valuep != NULL)
		*valuep = _JC_FCONST(b[0], b[1], b[2], b[3]);
}

static void
_jc_parse_long(_jc_cf_parse_state *s, jlong *valuep)
{
	_jc_uint64 value = 0;
	u_char byte;
	int i;

	for (i = 0; i < 8; i++) {
		_jc_parse_uint8(s, &byte);
		value = (value << 8) | byte;
	}
	if (valuep != NULL)
		*valuep = value;
}

static void
_jc_parse_double(_jc_cf_parse_state *s, jdouble *valuep)
{
	u_char b[8];
	int i;

	for (i = 0; i < 8; i++)
		_jc_parse_uint8(s, &b[i]);
	if (valuep != NULL) {
		*valuep = _JC_DCONST(b[0], b[1], b[2], b[3],
		    b[4], b[5], b[6], b[7]);
	}
}

static void
_jc_parse_utf8(_jc_cf_parse_state *s, const u_char **utfp, _jc_uint16 *lengthp)
{
	_jc_uint16 length;
	const u_char *utf;

	/* Get length */
	_jc_parse_uint16(s, &length);
	if (s->pos + length > s->length) {
		_JC_EX_STORE(s->env, ClassFormatError, "truncated class file");
		siglongjmp(s->jump, 1);
	}

	/* Validate UTF-8 encoding */
	utf = s->bytes + s->pos;
	if (_jc_utf_decode(utf, length, NULL) == -1) {
		_JC_EX_STORE(s->env, ClassFormatError,
		    "invalid UTF-8 string encoding");
		siglongjmp(s->jump, 1);
	}

	/* Update position */
	s->pos += length;

	/* Done */
	if (utfp != NULL)
		*utfp = utf;
	if (lengthp != NULL)
		*lengthp = length;
}

static void
_jc_parse_uint32(_jc_cf_parse_state *s, _jc_uint32 *valuep)
{
	_jc_uint32 value = 0;
	u_char byte;
	int i;

	for (i = 0; i < 4; i++) {
		_jc_parse_uint8(s, &byte);
		value = (value << 8) | byte;
	}
	if (valuep != NULL)
		*valuep = value;
}

static void
_jc_parse_uint16(_jc_cf_parse_state *s, _jc_uint16 *valuep)
{
	_jc_uint16 value = 0;
	u_char byte;
	int i;

	for (i = 0; i < 2; i++) {
		_jc_parse_uint8(s, &byte);
		value = (value << 8) | byte;
	}
	if (valuep != NULL)
		*valuep = value;
}

static void
_jc_parse_uint8(_jc_cf_parse_state *s, u_char *valuep)
{
	if (s->pos >= s->length) {
		_JC_EX_STORE(s->env, ClassFormatError, "truncated class file");
		siglongjmp(s->jump, 1);
	}
	if (valuep != NULL)
		*valuep = s->bytes[s->pos];
	s->pos++;
}

static void *
_jc_cf_alloc(_jc_cf_parse_state *s, size_t size)
{
	void *mem;

	if ((mem = _jc_uni_alloc(s->env, &s->cfile->uni, size)) == NULL)
		siglongjmp(s->jump, 1);
	return mem;
}

static void *
_jc_cf_zalloc(_jc_cf_parse_state *s, size_t size)
{
	void *mem;

	if ((mem = _jc_uni_zalloc(s->env, &s->cfile->uni, size)) == NULL)
		siglongjmp(s->jump, 1);
	return mem;
}

static void
_jc_sub_state(_jc_cf_parse_state *s, _jc_cf_parse_state *t, size_t length)
{
	_JC_ASSERT(s->pos + length <= s->length);
	*t = *s;
	t->bytes = s->bytes + s->pos;
	t->length = length;
	t->pos = 0;
}

/*
 * Sorts fields by type/size, name, then signature.
 *
 * This must sort the same as org.dellroad.jc.cgen.Util.fieldComparator.
 */
static int
_jc_field_sorter(const void *item1, const void *item2)
{
	const _jc_cf_field *const field1 = (_jc_cf_field *)item1;
	const _jc_cf_field *const field2 = (_jc_cf_field *)item2;
	const u_char ptype1 = _jc_sig_types[(u_char)*field1->descriptor];
	const u_char ptype2 = _jc_sig_types[(u_char)*field2->descriptor];
	int diff;

	if ((diff = !_JC_ACC_TEST(field1, STATIC)
	    - !_JC_ACC_TEST(field2, STATIC)) != 0)
		return diff;
	if ((diff = _jc_field_type_sort[ptype1]
	    - _jc_field_type_sort[ptype2]) != 0)
		return diff;
	if ((diff = strcmp(field1->name, field2->name)) != 0)
		return diff;
	return strcmp(field1->descriptor, field2->descriptor);
}

/*
 * Sorts methods by name, then signature.
 *
 * This must sort the same as org.dellroad.jc.cgen.Util.methodComparator.
 */
static int
_jc_method_sorter(const void *item1, const void *item2)
{
	const _jc_cf_method *const method1 = (_jc_cf_method *)item1;
	const _jc_cf_method *const method2 = (_jc_cf_method *)item2;
	int diff;

	if ((diff = strcmp(method1->name, method2->name)) != 0)
		return diff;
	return strcmp(method1->descriptor, method2->descriptor);
}

