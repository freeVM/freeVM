/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

/**
 * @author Sergey A. Krivenko
 * @version $Revision: 1.3 $
 */
package org.apache.harmony.x.management.console.controller;


/**
 * Contains data about MBean operations that may be shown in GUI and
 * handed back to Controller.
 *
 * @author Sergey A. Krivenko
 * @version $Revision: 1.3 $
 */
public class OperationInfo {

    /**
     * Operation's name.
     */
    private String name;
    
    /**
     * Operation's parameters.
     */
    private String[] params;
    
    /**
     * Operation's signature.
     */
    private String[] sign;
    
    /**
     * Operation's return type.
     */
    private String returnType;
    
    /**
     * A flag that shows if it's possible to execute an opeartion from GUI 
     * due to parameter types.
     */
    private boolean executable;
    
    /**
     * Construct the Object.
     * 
     * @param name - A name of the operation.
     * @param params - Specific parameter values. They are null wneh data
     *                 go from Controller to GUI.
     * @param sign - A signature of the operation.
     * @param returnType - A return type of the operation.
     * @param executable - True if this operation may executed from GUI.
     */
    public OperationInfo(String name, String[] params, String[] sign,
            String returnType, boolean executable) {
        
        this.name = name;
        this.params = params;
        this.sign = sign;
        this.returnType = returnType;
        this.executable = executable;
    }
    
    /**
     * Get operation name.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Get parameters.
     */
    public String[] getParams() {
        return this.params;
    }
    
    /**
     * Get return type.
     */
    public String getReturnType() {
        return this.returnType;
    }
    
    /**
     * Get signature.
     */
    public String[] getSign() {
        return this.sign;
    }
    
    /**
     * Check if this operation may be executed from GUI.
     */
    public boolean isExecutable() {
        return this.executable;
    }
    
    /**
     * Set parameter values in the existing instance of this class
     * when data go back to the Controller.
     * 
     * @param params - New parameter values.
     */
    public void setParams(String[] params) {
        this.params = params;
    }
    
    /**
     * Used to show operation data in GUI.
     */
    public String toString() {
        String s = "";
        
        for (int i = 0; i < sign.length; i++) {
            s += sign[i] + ", ";
        }
        
        s = s.substring(0, s.length() - 2);
        
        return getReturnType() + " " + getName() + "(" + s + ")";
    }
}
