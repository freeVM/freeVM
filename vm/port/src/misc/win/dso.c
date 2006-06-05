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

#include <windows.h>
#include <string.h>
#include <apr-1/apr_dso.h>
#include "port_dso.h"
#include "port_sysinfo.h"
#include <apr-1/apr_strings.h>
#include <apr-1/apr_env.h>

struct apr_dso_handle_t {
	apr_pool_t *pool;
	HINSTANCE handle;
	apr_status_t error;
};

APR_DECLARE(apr_status_t) port_dso_load_ex(apr_dso_handle_t** handle,
									  const char* path,
									  uint32 mode,
									  apr_pool_t* pool){
	/*
	* FIXME Windows does not support lazy dll resolution a la Linux's RTLD_LAZY.
	* Proper support for it requires hacking of APR DSO functions. 
	* Just ignore the <code>mode<code> param for now.
	*/

    /*if (mode == PORT_DSO_DEFAULT || !path) {*/
    	char *self_path;
		if (!path) {
			port_executable_name(&self_path, pool);
			return apr_dso_load(handle, (const char*)self_path, pool);
		}

		return apr_dso_load(handle, path, pool);
	/*} else {
		HINSTANCE native_handle = NULL;
		DWORD flag = (mode & PORT_DSO_BIND_DEFER) ? 
			(DONT_RESOLVE_DLL_REFERENCES) : (0);
		char *cp = apr_pstrdup(pool, path);
		char *p = cp;
		while ((p = strchr(p, '/')) != NULL) {
			*p = '\\';
		}

		native_handle = LoadLibraryEx(cp, NULL, flag);
		*handle = apr_palloc(pool, sizeof(apr_dso_handle_t));
		if (native_handle == 0) {
			native_handle = LoadLibraryEx(cp, NULL, flag | LOAD_WITH_ALTERED_SEARCH_PATH);
			if (native_handle == 0) {
				DWORD sys_err = apr_get_os_error();
				(*handle)->error = sys_err;
				return sys_err;
			}
		}
		(*handle)->handle = native_handle;
		(*handle)->pool = pool;
		(*handle)->error = APR_SUCCESS;
		return APR_SUCCESS;
	}*/
}
 
APR_DECLARE(apr_status_t) port_dso_search_path(char** path,
										apr_pool_t* pool) {
	return apr_env_get(path, "PATH", pool);
}

APR_DECLARE(char *) port_dso_name_decorate(const char* dl_name,
                            apr_pool_t* pool) {
									
	if (!dl_name) {
		return 0;
	}
	return apr_pstrcat(pool, dl_name, ".dll", NULL);
}
