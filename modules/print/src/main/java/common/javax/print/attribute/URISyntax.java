/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package javax.print.attribute;

import java.io.Serializable;
import java.net.URI;

public abstract class URISyntax implements Cloneable, Serializable {
    private final URI uri;

    protected URISyntax(URI uriValue) {
        super();
        if (uriValue == null) {
            throw new NullPointerException("Null uri value");
        }
        uri = uriValue;
    }

    @Override
    public boolean equals(Object object) {
        if ((object instanceof URISyntax) && uri.equals(((URISyntax) object).uri)) {
            return true;
        }
        return false;
    }

    public URI getURI() {
        return uri;
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}
