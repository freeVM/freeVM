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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang.management;

import java.security.BasicPermission;
import java.security.SecurityPermission;

/**
 * <p>
 * A {@link SecurityPermission} for use with the management system.
 * </p>
 * 
 * @since 1.5
 */
public class ManagementPermission extends BasicPermission {
    private static final long serialVersionUID = 1897496590799378737L;

    /**
     * <p>
     * Constructs and instance with the given name.
     * </p>
     * 
     * @param name The permission name, which must be <code>"monitor"</code>
     *        or <code>"control"</code>.
     * @throws IllegalArgumentException if <code>name</code> is invalid.
     */
    public ManagementPermission(String name) {
        super(name);
        if (!"control".equals(name) && !"monitor".equals(name)) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * <p>
     * Constructs and instance with the given name.
     * </p>
     * 
     * @param name The permission name, which must be <code>"monitor"</code>
     *        or <code>"control"</code>.
     * @param actions This is not used, so it must be <code>null</code> or
     *        empty.
     * @throws IllegalArgumentException if <code>name</code> is invalid.
     */
    public ManagementPermission(String name, String actions) {
        super(name, actions);
        if (!"control".equals(name) && !"monitor".equals(name)) {
            throw new IllegalArgumentException();
        }
        if (actions != null && actions.length() != 0) {
            throw new IllegalArgumentException();
        }
    }
}
