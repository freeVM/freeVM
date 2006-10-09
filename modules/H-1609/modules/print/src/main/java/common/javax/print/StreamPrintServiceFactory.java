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
 * @version $Revision: 1.3 $ 
 */ 
package javax.print;

import java.io.OutputStream;
import java.util.ArrayList;
import java.io.IOException;
import javax.print.DocFlavor;
import javax.print.StreamPrintService;

import org.apache.harmony.x.print.PSStreamPrintServiceFactory;
import org.apache.harmony.x.print.util.FactoryLocator;


public abstract class StreamPrintServiceFactory {

    //  static part of the class
    private static ArrayList listOfSPSFactories = new ArrayList();      
       
    
    public static StreamPrintServiceFactory[] 
             lookupStreamPrintServiceFactories(
                    DocFlavor flavor, String outputMimeType) {
        
        int l;
        ArrayList selectedSPSFactories = new ArrayList();
        StreamPrintServiceFactory factory;        
        StreamPrintServiceFactory[] a = {}; 
        
        FactoryLocator fl = new FactoryLocator();
        try {
            fl.lookupAllFactories();
        } catch (IOException io) {            
            return (StreamPrintServiceFactory[]) selectedSPSFactories.toArray(a);            
        }
        listOfSPSFactories = fl.getFactoryClasses();

        // no selection      
        if (flavor == null && outputMimeType == null) {  
            return  (StreamPrintServiceFactory[])listOfSPSFactories.toArray(a);
        }
        // make a selection by 
        l = listOfSPSFactories.size();
        for (int i = 0; i < l; i++) {
            factory = (StreamPrintServiceFactory) listOfSPSFactories.get(i);
            if ((outputMimeType == null || 
                    outputMimeType.equalsIgnoreCase(factory.getOutputFormat())
                    ||(factory instanceof PSStreamPrintServiceFactory &&
                    outputMimeType.equalsIgnoreCase("internal/postscript")))
                && (flavor == null || 
                    isElementOf(flavor, factory.getSupportedDocFlavors()))) {

                   selectedSPSFactories.add(factory);
               }
        }
        return (StreamPrintServiceFactory[]) selectedSPSFactories.toArray(a);
    }
    
    /*
     * auxilary method for element search
     */    
    private static boolean isElementOf(DocFlavor flavor, DocFlavor[] flavors) {
        for (int i = 0; i < flavors.length; i++) {
            if (flavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }    
     
    public StreamPrintServiceFactory(){
        super();
    }
      
    public abstract String getOutputFormat();
  
    public abstract DocFlavor[] getSupportedDocFlavors();

    public abstract StreamPrintService getPrintService(OutputStream out);
    
}
