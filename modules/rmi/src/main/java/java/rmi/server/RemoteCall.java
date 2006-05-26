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
 * @version $Revision: 1.3.4.1 $
 */
package java.rmi.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;


/**
 * @com.intel.drl.spec_ref
 *
 * @author  Mikhail A. Markov
 * @version $Revision: 1.3.4.1 $
 */
public interface RemoteCall {

    /**
     * @com.intel.drl.spec_ref
     */
    public void done() throws IOException;

    /**
     * @com.intel.drl.spec_ref
     */
    public void executeCall() throws Exception;

    /**
     * @com.intel.drl.spec_ref
     */
    public ObjectOutput getResultStream(boolean success)
            throws IOException, StreamCorruptedException;

    /**
     * @com.intel.drl.spec_ref
     */
    public void releaseInputStream() throws IOException;

    /**
     * @com.intel.drl.spec_ref
     */
    public ObjectInput getInputStream() throws IOException;

    /**
     * @com.intel.drl.spec_ref
     */
    public void releaseOutputStream() throws IOException;

    /**
     * @com.intel.drl.spec_ref
     */
    public ObjectOutput getOutputStream() throws IOException;
}
