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
 * @author Evgueni Brevnov
 * @version $Revision: 1.1.2.1.4.4 $
 */

package java.lang;

/**
 * @com.intel.drl.spec_ref 
 * 
 * Serves as a layer between JIT compiler and java application.
 * <p>
 * This class must be implemented according to the common policy for porting
 * interfaces - see the porting interface overview for more detailes.
 * 
 * @api2vm
 */
public final class Compiler {

    /**
     * This class is not supposed to be instantiated.
     */
    private Compiler() {        
    }

    /**
     * This method satisfies the requirements of the specification for the
     * {@link Compiler#command(java.lang.Object) Compiler.command(Object any)}
     * method.
     * 
     * @api2vm
     */
    public static Object command(Object obj) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * This method satisfies the requirements of the specification for the
     * {@link Compiler#compileClass(java.lang.Class)
     * Compiler.compileClass(Class clazz)} method.
     * 
     * @api2vm
     */
    public static boolean compileClass(Class<?> clazz) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * This method satisfies the requirements of the specification for the
     * {@link Compiler#compileClasses(java.lang.String)
     * Compiler.compileClasses(String string)} method.
     * 
     * @api2vm
     */
    public static boolean compileClasses(String name){
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * This method satisfies the requirements of the specification for the
     * {@link Compiler#disable() Compiler.disable()} method.
     * 
     * @api2vm
     */
    public static void disable() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * This method satisfies the requirements of the specification for the
     * {@link Compiler#enable() Compiler.enable()} method.
     * 
     * @api2vm
     */
    public static void enable() {
       throw new UnsupportedOperationException("Not implemented");
    }
}
