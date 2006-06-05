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
 * @author Pavel Afremov, Vera Volynets
 * @version $Revision: 1.3.12.1.4.3 $
 */  
 
package perf;

/*
 * @keyword perf exc
 */
public class ThrowManyExceptions_depth{

    private final static int MAX_THROW = 100000; 
    private final static int MAX_DEPTH = 30;

    
    class TestLazyException extends Exception {
        public static final long serialVersionUID = 0L;
    }
    
    private void runTest() {
        for (int i = 0; i < MAX_THROW; i++) {
	    try {
		depthCreateThrow(MAX_DEPTH);
	    } catch (TestLazyException tle) {}
	}
    }
    
    private void depthCreateThrow(int depth) throws TestLazyException {
        if (depth == 0) {
            throw new TestLazyException();
        } else {
	    depthCreateThrow(depth - 1);
	}
    }

    
    public static void main(String argv[]) {
        ThrowManyExceptions_depth test = new ThrowManyExceptions_depth();
        test.runTest();
	System.out.println ("PASSED");
    }
}


