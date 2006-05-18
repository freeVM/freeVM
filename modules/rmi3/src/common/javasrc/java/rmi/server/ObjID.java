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
 * @version $Revision: 1.6.4.3 $
 */
package java.rmi.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.security.AccessController;
import java.security.SecureRandom;

import org.apache.harmony.rmi.common.GetBooleanPropAction;
import org.apache.harmony.rmi.common.RMIProperties;


/**
 * @com.intel.drl.spec_ref
 *
 * @author  Mikhail A. Markov
 * @version $Revision: 1.6.4.3 $
 */
public final class ObjID implements Serializable {

    private static final long serialVersionUID = -6386392263968365220L;

    /** @com.intel.drl.spec_ref */
    public static final int REGISTRY_ID = 0;

    /** @com.intel.drl.spec_ref */
    public static final int ACTIVATOR_ID = 1;

    /** @com.intel.drl.spec_ref */
    public static final int DGC_ID = 2;

    private long objNum;
    private UID space;

    /*
     * If true then we should use cryptographically strong random number
     * generator.
     */
    private static final boolean useRandom =
            ((Boolean) AccessController.doPrivileged(new GetBooleanPropAction(
                    RMIProperties.RANDOMIDS_PROP))).booleanValue();

    /*
     * Number generator for initializing objNum fields.
     */
    private static final NumberGenerator numGenerator = new NumberGenerator();

    /**
     * @com.intel.drl.spec_ref
     */
    public ObjID(int num) {
        objNum = num;
        space = new UID((short) 0);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public ObjID() {
        objNum = numGenerator.nextLong();
        space = new UID();
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public String toString() {
        return "ObjID[" + objNum + ", " + space + "]";
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof ObjID)) {
            ObjID id = (ObjID) obj;
            return (objNum == id.objNum) && space.equals(id.space);
        }
        return false;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public void write(ObjectOutput out) throws IOException {
        out.writeLong(objNum);
        space.write(out);
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public int hashCode() {
        return (int) objNum;
    }

    /**
     * @com.intel.drl.spec_ref
     */
    public static ObjID read(ObjectInput in) throws IOException {
        ObjID id = new ObjID(-1);
        id.objNum = in.readLong();
        id.space = UID.read(in);
        return id;
    }


    /*
     * Generator used for obtaining long numbers both in secure and insecure
     * variants.
     */
    private static class NumberGenerator {
        // Counter to be used in case of insecure mode.
        static long numCounter = 65536; // 2^16

        // Secure generator.
        SecureRandom sGenerator;

        /*
         * Constructs NumberGenerator for secure/unsecure variants.
         */
        NumberGenerator() {
            if (useRandom) {
                sGenerator = new SecureRandom();
            }
        }

        /*
         * Returns next long number.
         */
        synchronized long nextLong() {
            return useRandom ? sGenerator.nextLong() : numCounter++;
        }
    }
}
