/*
 *  Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
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
/** 
* @author Alexey V. Varlamov
* @version $Revision: 1.1.2.1.4.3 $
*/  

#include <dlfcn.h>
#include <stdlib.h>
#include <apr-1/apr_dso.h>
#include <apr-1/apr_strings.h>
#include "port_dso.h"

struct apr_dso_handle_t {
	apr_pool_t *pool;
	void *handle;
	const char *errormsg;
};

APR_DECLARE(apr_status_t) port_dso_load_ex(apr_dso_handle_t** handle,
									  const char* path,
									  uint32 mode,
									  apr_pool_t* pool){
    if (mode == PORT_DSO_DEFAULT) {
		return apr_dso_load(handle, path, pool);
	}
	else {
		int flag = (mode & PORT_DSO_BIND_DEFER) ? 
			(RTLD_LAZY | RTLD_GLOBAL) : (RTLD_NOW | RTLD_GLOBAL);
		void* native_handle = dlopen(path, flag);
		*handle = apr_palloc(pool, sizeof(apr_dso_handle_t));
		if (native_handle == NULL) {			(*handle)->errormsg = dlerror();
			return APR_EDSOOPEN;
		}
		(*handle)->handle = native_handle;
		(*handle)->pool = pool;
		(*handle)->errormsg = NULL;
		return APR_SUCCESS;
	}
}

APR_DECLARE(apr_status_t) port_dso_search_path(char** path,
										apr_pool_t* pool) {
	char* res = getenv("LD_LIBRARY_PATH");
	if(res) {
		*path = res;
		return APR_SUCCESS;
	}
	return APR_ENOENT;
}

APR_DECLARE(char *) port_dso_name_decorate(const char* dl_name,
                            apr_pool_t* pool) {
									
	if (!dl_name) {
		return 0;
	}
	return apr_pstrcat(pool, "lib", dl_name, ".so", NULL);
}
