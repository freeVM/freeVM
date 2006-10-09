/*
 *  Copyright 2005 - 2006 The Apache Software Foundation or its licensors, as applicable.
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
 * @author Igor A. Pyankov 
 * @version $Revision: 1.2 $ 
 */ 

package javax.print;

import java.io.InputStream;

import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.standard.MediaSizeName;

import junit.framework.TestCase;

public class PrintAutosenseTest extends TestCase {
    public static void main(String[] args) {
        new PrintAutosenseTest().testPrintAutosense();
    }

    public void testPrintAutosense() {
        System.out.println("======== START PrintAutosenseTest ========");

        PrintService[] services;
        HashDocAttributeSet daset = new HashDocAttributeSet();
        DocPrintJob pj;
        Doc doc;

        daset.add(MediaSizeName.ISO_A4);

        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        System.out.println("====== END PrintAutosenseTest ========");
    }

}