
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

#ifndef _ZIP_H_
#define _ZIP_H_

/*
 * ZIP file constants.
 */
#define ZIP_DIRECTORY_SIGNATURE		0x06054b50
#define ZIP_DIRENTRY_SIGNATURE		0x02014b50
#define ZIP_ENTRY_SIGNATURE		0x03044b50
#define ZIP_DIRECTORY_INFO_LEN		22
#define ZIP_LOCAL_HEADER_EXTRA_OFFSET	28
#define ZIP_LOCAL_HEADER_LEN		30

#define ZIP_METHOD_STORED 		0
#define ZIP_METHOD_DEFLATED 		8

/*
 * ZIP structures.
 */
typedef struct _jc_zip		_jc_zip;
typedef struct _jc_zip_entry	_jc_zip_entry;

struct _jc_zip_entry {
	char		*name;
	jshort		method;
	jint		comp_len;
	jint		uncomp_len;
	jint		crc;
	off_t		offset;
};

struct _jc_zip {
	int		fd;
	char		*path;
	int		num_entries;
#if !HAVE_PREAD
	pthread_mutex_t	mutex;		/* protects seek pointer */
#ifndef NDEBUG
	_jc_env		*mutex_owner;
#endif
#endif
	_jc_zip_entry	*entries;
};

#endif	/* _ZIP_H_ */
