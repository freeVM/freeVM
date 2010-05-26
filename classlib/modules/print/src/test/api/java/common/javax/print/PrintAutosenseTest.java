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
 * @author Igor A. Pyankov 
 */ 

package javax.print;

import java.io.InputStream;

import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.standard.MediaSizeName;

import junit.framework.TestCase;
import tests.support.Support_Excludes;

public class PrintAutosenseTest extends TestCase {
    public static void main(String[] args) throws Exception {
        new PrintAutosenseTest().testPrintAutosense();
    }

    public void testPrintAutosense() throws Exception {
        if (Support_Excludes.isExcluded()) {
            return;
        }

        System.out.println("======== START PrintAutosenseTest ========");

        PrintService[] services;
        HashDocAttributeSet daset = new HashDocAttributeSet();
        DocPrintJob pj;
        Doc doc;

        daset.add(MediaSizeName.ISO_A4);

        DocFlavor df = DocFlavor.INPUT_STREAM.AUTOSENSE;
        InputStream fis = this.getClass().getResourceAsStream(
                "/Resources/hello_ps.ps");
        services = PrintServiceLookup.lookupPrintServices(df, null);
        TestUtil.checkServices(services);

        for (int j = 0; j < services.length; j++) {
            PrintService printer = services[j];
            if (printer.toString().indexOf("print-to-file") >= 0) {
                doc = new SimpleDoc(fis, df, null);

                pj = printer.createPrintJob();
                pj.print(doc, null);
                System.out.println(fis.toString() + " printed on "
                        + printer.getName());
                break;
            }
        }

        System.out.println("====== END PrintAutosenseTest ========");
    }

}