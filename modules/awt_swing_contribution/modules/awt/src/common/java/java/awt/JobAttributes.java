/*
 *  Copyright 2005 - 2006 The Apache Software Software Foundation or its licensors, as applicable.
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
 * @version $Revision$
 */

package java.awt;

public final class JobAttributes implements Cloneable {

    private int copies;
    private int fromPage;
    private int maxPage;
    private int minPage;
    private int pageRanges[][];
    private int firstPage;
    private int lastPage;
    private int toPage;
    private String fileName;
    private String printer;
    private DefaultSelectionType defaultSelection;
    private DestinationType destination;
    private MultipleDocumentHandlingType multiDocHandling;
    private DialogType dialog;
    private SidesType sides;


    /* section of the nested classes */
    public static final class DefaultSelectionType {
        private final int value;
        private final String name;
        private static final String names[] = {"ALL", "RANGE", "SELECTION"};

        public static final DefaultSelectionType ALL
                                    = new DefaultSelectionType(0);
        public static final DefaultSelectionType RANGE
                                    = new DefaultSelectionType(1);
        public static final DefaultSelectionType SELECTION
                                    = new DefaultSelectionType(2);

        private DefaultSelectionType(int i){
            super();
            value = i;
            name = names[i];
        }

        private DefaultSelectionType(){
            this(0);
        }
    }

    public static final class DestinationType {
        private final int value;
        private final String name;
        private static final String names[] = {"FILE", "PRINTER"};

        public static final DestinationType FILE = new DestinationType(0);
        public static final DestinationType PRINTER = new DestinationType(1);

        private DestinationType(int i) {
            super();
            value = i;
            name = names[i];
        }
        private DestinationType() {
            this(0);
        }
    }

    public static final class DialogType{
        private final int value;
        private final String name;
        private static final String names[] = {"COMMON", "NATIVE", "NONE"};

        public static final DialogType COMMON = new DialogType(0);
        public static final DialogType NATIVE = new DialogType(1);
        public static final DialogType NONE = new DialogType(2);

        private DialogType(int i){
            super();
            value = i;
            name = names[i];
        }
        private DialogType(){
            this(0);
        }

    }

    public static final class MultipleDocumentHandlingType {
        private final int value;
        private final String name;
        private static final String names[]
                 = {"SEPARATE_DOCUMENTS_COLLATED_COPIES",
                    "SEPARATE_DOCUMENTS_UNCOLLATED_COPIES"};

        public static final MultipleDocumentHandlingType
                            SEPARATE_DOCUMENTS_COLLATED_COPIES
                                = new MultipleDocumentHandlingType(0);
        public static final MultipleDocumentHandlingType
                            SEPARATE_DOCUMENTS_UNCOLLATED_COPIES
                            = new MultipleDocumentHandlingType(1);

        private MultipleDocumentHandlingType(int i){
            super();
            value = i;
            name = names[i];
        }

        private MultipleDocumentHandlingType(){
            this(0);
        }
    }

    public static final class SidesType{
        private final int value;
        private final String name;
        private static final String names[] = {"ONE_SIDED",
                "TWO_SIDED_LONG_EDGE",  "TWO_SIDED_SHORT_EDGE"};

        public static final SidesType ONE_SIDED = new SidesType(0);
        public static final SidesType TWO_SIDED_LONG_EDGE  = new SidesType(1);
        public static final SidesType TWO_SIDED_SHORT_EDGE = new SidesType(2);

        private SidesType(int i){
            super();
            value = i;
            name = names[i];
        }

        private SidesType(){
            this(0);
        }
    }
    /* end of the nested classes */

    public JobAttributes() {
        setDefaultSelection(DefaultSelectionType.ALL);
        setDestination(DestinationType.PRINTER);
        setDialog(DialogType.NATIVE);
        setMultipleDocumentHandlingToDefault();
        setSidesToDefault();
        setCopiesToDefault();
        setMinPage(1);
        setMaxPage(0x7fffffff);
    }

    public JobAttributes(JobAttributes obj){
            set(obj);
    }

    public JobAttributes(int copies,
            JobAttributes.DefaultSelectionType defaultSelection,
            JobAttributes.DestinationType destination,
            JobAttributes.DialogType dialog,
            String fileName,
            int maxPage,
            int minPage,
            JobAttributes.MultipleDocumentHandlingType multipleDocumentHandling,
            int[][] pageRanges,
            String printer,
            JobAttributes.SidesType sides){

        setCopies(copies);
        setDefaultSelection(defaultSelection);
        setDestination(destination);
        setDialog(dialog);
        setFileName(fileName);
        setMinPage(minPage);
        setMaxPage(maxPage);
        setMultipleDocumentHandling(multipleDocumentHandling);
        setPageRanges(pageRanges);
        setPrinter(printer);
        setSides(sides);
    }

    public void setCopiesToDefault() {
        setCopies(1);
    }

    public void setMultipleDocumentHandlingToDefault() {
        setMultipleDocumentHandling
           (MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES);
    }

    public void setSidesToDefault(){
        setSides(SidesType.ONE_SIDED);
    }

    public int getCopies(){
        return copies;
    }

    public void setCopies(int copies) {
        if(copies <= 0) {
            throw new IllegalArgumentException("Invalid number of copies");
        } else {
            this.copies = copies;
        }
    }

    public int getMaxPage(){
        return maxPage;
    }

    public void setMaxPage(int imaxPage) {
        if (imaxPage <= 0 || imaxPage < minPage) {
            throw new IllegalArgumentException("Invalid value for maxPage");
        } else {
            maxPage = imaxPage;
        }
    }

    public int getMinPage(){
        return minPage;
    }

    public void setMinPage(int iminPage) {
        if (iminPage <= 0 || iminPage > maxPage) {
            throw new IllegalArgumentException("Invalid value for minPage");
        } else {
            minPage = iminPage;
        }
    }

    public int getFromPage() {
        if (fromPage != 0)
            return fromPage;
        if (toPage != 0)
            return getMinPage();
        if (pageRanges != null)
            return firstPage;
        return getMinPage();
    }

    public void setFromPage(int ifromPage) {
        if (ifromPage <= 0 || ifromPage > toPage
                || ifromPage < minPage || ifromPage > maxPage) {
            throw new IllegalArgumentException("Invalid value for fromPage");
        } else {
            fromPage = ifromPage;
        }
    }

    public int getToPage() {
        if (toPage != 0)
            return toPage;
        if (fromPage != 0)
            return fromPage;
        if (pageRanges != null)
            return lastPage;
        return getMinPage();
    }

    public void setToPage(int itoPage) {
        if (itoPage <= 0 || itoPage < fromPage
                || itoPage < minPage
                || itoPage > maxPage) {
            throw new IllegalArgumentException("Invalid value for toPage");
        } else {
            toPage = itoPage;
        }
    }

    public String getPrinter(){
        return printer;
    }

    public void setPrinter(String printer){
        this.printer = printer;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public int[][] getPageRanges() {
        int prl = pageRanges.length;
        int pr[][];
        if (pageRanges != null) {
            pr = new int[prl][2];
            for (int i = 0; i < prl; i++) {
                pr[i][0] = pageRanges[i][0];
                pr[i][1] = pageRanges[i][1];
            }
            return pr;
        }
        pr = new int[1][2];
        if (fromPage != 0 || toPage != 0) {
            pr[0][0] = fromPage;
            pr[0][1] = toPage;
        } else {
            pr[0][0] = minPage;
            pr[0][1] = minPage;
        }
        return pr;
    }

    public void setPageRanges(int[][] pr) {
        String msg = "Invalid value for pageRanges";
        int n1 = 0;
        int n2 = 0;
        int prl = pr.length;

        if(pr == null)
            throw new IllegalArgumentException(msg);

        for(int k = 0; k < prl; k++) {
            if(pr[k] == null || pr[k].length != 2
                    || pr[k][0] <= n2 || pr[k][1] < pr[k][0])
                throw new IllegalArgumentException(msg);

            n2 = pr[k][1];
            if(n1 == 0)
                n1 = pr[k][0];
        }

        if(n1 < minPage || n2 > maxPage)
            throw new IllegalArgumentException(msg);

        pageRanges = new int[prl][2];

        for(int k = 0; k < prl; k++) {
            pageRanges[k][0] = pr[k][0];
            pageRanges[k][1] = pr[k][1];
        }
        firstPage = n1;
        lastPage = n2;
    }

    public DestinationType getDestination() {
        return destination;
    }

    public void setDestination(JobAttributes.DestinationType destination) {
        if(destination == null){
            throw new IllegalArgumentException("Invalid value for destination");
        } else {
            this.destination = destination;
        }
    }

    public DialogType getDialog() {
        return dialog;
    }

    public void setDialog(JobAttributes.DialogType dialog) {
        if(dialog == null) {
            throw new IllegalArgumentException("Invalid value for dialog");
        } else {
            this.dialog = dialog;
        }
    }


    public JobAttributes.DefaultSelectionType getDefaultSelection() {
        return defaultSelection;
    }

    public void setDefaultSelection(
            JobAttributes.DefaultSelectionType a_defaultSelection) {
        if (a_defaultSelection == null) {
            throw new IllegalArgumentException(
                    "Invalid value for defaultSelection");
        } else {
            this.defaultSelection = a_defaultSelection;
        }
    }

    public JobAttributes.MultipleDocumentHandlingType
            getMultipleDocumentHandling(){
        return multiDocHandling;
    }

    public void setMultipleDocumentHandling
        (JobAttributes.MultipleDocumentHandlingType multipleDocumentHandling){

        if(multipleDocumentHandling == null) {
            throw new IllegalArgumentException
                        ("Invalid value for multipleDocumentHandling");
        } else {
            multiDocHandling = multipleDocumentHandling;
        }
    }

    public JobAttributes.SidesType getSides(){
        return sides;
    }

    public void setSides(JobAttributes.SidesType sides){

        if(sides == null) {
            throw new IllegalArgumentException
                        ("Invalid value for attribute sides");
        } else {
            this.sides = sides;
        }
    }

    public void set(JobAttributes obj) {
        copies = obj.copies;
        defaultSelection = obj.defaultSelection;
        destination = obj.destination;
        dialog = obj.dialog;
        fileName = obj.fileName;
        printer = obj.printer;
        multiDocHandling = obj.multiDocHandling;
        firstPage = obj.firstPage;
        lastPage = obj.lastPage;
        sides = obj.sides;
        fromPage = obj.fromPage;
        toPage = obj.toPage;
        maxPage = obj.maxPage;
        minPage = obj.minPage;
        if (obj.pageRanges == null) {
            pageRanges = null;
        } else {
            setPageRanges(obj.pageRanges);
        }
    }

    public String toString(){
        /* The format is based on 1.5 release behavior 
         * which can be revealed by the following code:
         * System.out.println(new JobAttributes());
         */

        String s = "Page-ranges [";
        int k = pageRanges.length-1;
        for(int i = 0; i <= k ; i++)            {
            s += pageRanges[i][0] + "-"
               + pageRanges[i][1] + ((i < k)? ",": "");
        }
        s += "], copies=" + getCopies()
            + ",defSelection=" + getDefaultSelection()
            + ",dest=" + getDestination()
            + ",fromPg=" + getFromPage()
            + ",toPg=" + getToPage()
            + ",minPg=" + getMinPage()
            + ",maxPg=" + getMaxPage()
            + ",multiple-document-handling="
            + getMultipleDocumentHandling()
            + ",fileName=" + getFileName()
            + ",printer=" + getPrinter()
            + ",dialog=" + getDialog()
            + ",sides=" + getSides();
        return s;
    }

    public int hashCode() {
        int hash = this.toString().hashCode();
        return hash;
    }

    public Object clone() {
        JobAttributes ja = new JobAttributes(this);
        return (Object)ja;
    }

    public boolean equals(Object obj){
        if(!(obj instanceof JobAttributes)){
            return false;
        }
        JobAttributes ja = (JobAttributes)obj;

        if(fileName == null){
            if(ja.fileName != null)
                return false;
        } else {
            if(!fileName.equals(ja.fileName))
                return false;
        }

        if(printer == null) {
            if(ja.printer != null){
                return false;
            }
        } else {
            if(!printer.equals(ja.printer)){
                return false;
            }
        }

        if(pageRanges == null) {
            if(ja.pageRanges != null)
                return false;
        } else {
            if(ja.pageRanges == null){
                return false;
            }
            if(pageRanges.length != ja.pageRanges.length){
                return false;
            }
            for(int i = 0; i < pageRanges.length; i++){
                if(pageRanges[i][0] != ja.pageRanges[i][0]
                || pageRanges[i][1] != ja.pageRanges[i][1]){
                    return false;
                }
            }
        }
        if(copies != ja.copies){
            return false;
        }
        if(defaultSelection != ja.defaultSelection){
            return false;
        }
        if(destination != ja.destination){
            return false;
        }
        if(dialog != ja.dialog){
            return false;
        }
        if(maxPage != ja.maxPage){
            return false;
        }
        if(minPage != ja.minPage){
            return false;
        }
        if(multiDocHandling != ja.multiDocHandling){
            return false;
        }
        if(firstPage != ja.firstPage){
            return false;
        }
        if(lastPage != ja.lastPage){
            return false;
        }
        if(sides != ja.sides){
            return false;
        }
        if(toPage != ja.toPage){
            return false;
        }
        if(fromPage != ja.fromPage){
            return false;
        }
        return true;
    }
}


