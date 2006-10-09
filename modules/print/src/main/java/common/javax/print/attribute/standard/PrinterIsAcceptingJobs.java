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
/** 
 * @author Elena V. Sayapina 
 * @version $Revision: 1.5 $ 
 */ 

package javax.print.attribute.standard;

import javax.print.attribute.EnumSyntax;
import javax.print.attribute.PrintServiceAttribute;


public final class PrinterIsAcceptingJobs extends EnumSyntax 
    implements PrintServiceAttribute {


    public static final PrinterIsAcceptingJobs
        NOT_ACCEPTING_JOBS = new PrinterIsAcceptingJobs(0);

    public static final PrinterIsAcceptingJobs
        ACCEPTING_JOBS = new PrinterIsAcceptingJobs(1);

    
    private static final PrinterIsAcceptingJobs[] enumValueTable = { 
        NOT_ACCEPTING_JOBS,
        ACCEPTING_JOBS
    };
    
    private static final String[] stringTable = { "false",
                                                  "true" };

    
    protected PrinterIsAcceptingJobs(int value) {
        super (value);
    }

 
    protected EnumSyntax[] getEnumValueTable() {
        return enumValueTable;
    }

    public final Class getCategory() {
    /* 1.5 support requires the following changes 
       Class<? extends Attribute> getCategory() { */
        return PrinterIsAcceptingJobs.class;
    }

    public final String getName() {
        return "printer-is-accepting-jobs";
    }
    
    protected String[] getStringTable() {
        return stringTable;
    }

    
}
