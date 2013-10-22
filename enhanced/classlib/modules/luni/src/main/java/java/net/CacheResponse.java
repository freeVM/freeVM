/* Copyright 2006 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * CacheResponse is used for getting resource from the ResponseCache. 
 * An CacheResponse object provides an <code>InputStream</code> to access the response body,
 * and also a method <code>getHeaders()</code> to fetch the response headers.
 * 
 */
public abstract class CacheResponse {
	/**
	 * Constructor method
	 */
	public CacheResponse() {
		super();
	}

	/**
	 * <p>
	 * Returns an <code>InputStream</code> for the respsonse body access.
	 * </p>
	 * @return an <code>InputStream</code>, which can be used to fetch the response body.
	 * @throws IOException
	 *             if an I/O error is encounted while retrieving the response body.
	 */
	public abstract InputStream getBody() throws IOException;

	/**
	 * <p>
	 * Returns an immutable <code>Map</code>, which contains the response headers information.
	 * @return an immutable <code>Map</code>, which contains the response headers. 
	 * The map is from response header field names to lists of field values.
	 * Field name is a <code>String</code>, and the field values list is a <code>List</code>
	 * of <code>String</code>.The status line as its field name has null as its list of field values.
	 * </p> 
	 * @throws IOException
	 *             if an I/O error is encounted while retrieving the response headers.
	 */
	public abstract Map getHeaders() throws IOException;
}
