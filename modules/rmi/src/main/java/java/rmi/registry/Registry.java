/*
 * Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable
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
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author  Mikhail A. Markov
 * @version $Revision: 1.4.4.1 $
 */
package java.rmi.registry;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * @com.intel.drl.spec_ref
 *
 * @author  Mikhail A. Markov
 * @version $Revision: 1.4.4.1 $
 */
public interface Registry extends Remote {

    /**
     * @com.intel.drl.spec_ref
     */
    public static final int REGISTRY_PORT = 1099;

    /**
     * @com.intel.drl.spec_ref
     */
    public void rebind(String name, Remote obj)
            throws RemoteException, AccessException;

    /**
     * @com.intel.drl.spec_ref
     */
    public void bind(String name, Remote obj)
            throws RemoteException, AlreadyBoundException, AccessException;

    /**
     * @com.intel.drl.spec_ref
     */
    public Remote lookup(String name)
            throws RemoteException, NotBoundException, AccessException;

    /**
     * @com.intel.drl.spec_ref
     */
    public void unbind(String name)
            throws RemoteException, NotBoundException, AccessException;

    /**
     * @com.intel.drl.spec_ref
     */
    public String[] list()
            throws RemoteException, AccessException;
}
