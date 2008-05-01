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
package org.apache.harmony.unpack200;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.harmony.pack200.BHSDCodec;
import org.apache.harmony.pack200.Codec;
import org.apache.harmony.unpack200.bytecode.Attribute;
import org.apache.harmony.unpack200.bytecode.CPClass;
import org.apache.harmony.unpack200.bytecode.CPDouble;
import org.apache.harmony.unpack200.bytecode.CPFieldRef;
import org.apache.harmony.unpack200.bytecode.CPFloat;
import org.apache.harmony.unpack200.bytecode.CPInteger;
import org.apache.harmony.unpack200.bytecode.CPInterfaceMethodRef;
import org.apache.harmony.unpack200.bytecode.CPLong;
import org.apache.harmony.unpack200.bytecode.CPMethodRef;
import org.apache.harmony.unpack200.bytecode.CPNameAndType;
import org.apache.harmony.unpack200.bytecode.CPString;
import org.apache.harmony.unpack200.bytecode.CPUTF8;
import org.apache.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.harmony.unpack200.bytecode.NewAttribute;

/**
 * Set of bands relating to a non-predefined attribute
 */
public class NewAttributeBands extends BandSet {

    private final AttributeLayout attributeLayout;

    private int backwardsCallCount;

    private List attributeLayoutElements;

    public NewAttributeBands(Segment segment, AttributeLayout attributeLayout)
            throws IOException {
        super(segment);
        this.attributeLayout = attributeLayout;
        parseLayout();
        attributeLayout.setBackwardsCallCount(backwardsCallCount);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.harmony.unpack200.BandSet#unpack(java.io.InputStream)
     */
    public void unpack(InputStream in) throws IOException, Pack200Exception {
        // does nothing - use parseAttributes instead
    }

    /**
     * Parse the bands relating to this AttributeLayout and return the correct
     * class file attributes as a List of {@link Attribute}
     * 
     * @throws Pack200Exception
     */
    public List parseAttributes(InputStream in, int occurrenceCount)
            throws IOException, Pack200Exception {
        for (Iterator iter = attributeLayoutElements.iterator(); iter.hasNext();) {
            AttributeLayoutElement element = (AttributeLayoutElement) iter
                    .next();
            element.readBands(in, occurrenceCount);
        }

        List attributes = new ArrayList();
        for (int i = 0; i < occurrenceCount; i++) {
            attributes.add(getOneAttribute(i, attributeLayoutElements));
        }
        return attributes;
    }

    /**
     * Get one attribute at the given index from the various bands. The correct
     * bands must have already been read in.
     * 
     * @param index
     * @param elements
     * @return
     */
    private Attribute getOneAttribute(int index, List elements) {
        NewAttribute attribute = new NewAttribute(segment.getCpBands()
                .cpUTF8Value(attributeLayout.getName(),
                        ClassConstantPool.DOMAIN_ATTRIBUTEASCIIZ));
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            AttributeLayoutElement element = (AttributeLayoutElement) iter
                    .next();
            element.addToAttribute(index, attribute);
        }
        return attribute;
    }

    /**
     * Tokenise the layout into AttributeElements
     * 
     * @throws IOException
     */
    private void parseLayout() throws IOException {
        if (attributeLayoutElements == null) {
            attributeLayoutElements = new ArrayList();
            StringReader stream = new StringReader(attributeLayout.getLayout());
            AttributeLayoutElement e;
            while ((e = readNextAttributeElement(stream)) != null) {
                attributeLayoutElements.add(e);
            }
            resolveCalls();
        }
    }

    /**
     * Resolve calls in the attribute layout and returns the number of backwards
     * calls
     * 
     * @param tokens -
     *            the attribute layout as a List of AttributeElements
     */
    private void resolveCalls() {
        int backwardsCalls = 0;
        for (int i = 0; i < attributeLayoutElements.size(); i++) {
            AttributeLayoutElement element = (AttributeLayoutElement) attributeLayoutElements
                    .get(i);
            if (element instanceof Callable) {
                Callable callable = (Callable) element;
                List body = callable.body; // Look for calls in the body
                for (Iterator iter = body.iterator(); iter.hasNext();) {
                    LayoutElement layoutElement = (LayoutElement) iter.next();
                    if (layoutElement instanceof Call) {
                        // Set the callable for each call
                        Call call = (Call) layoutElement;
                        int index = call.callableIndex;
                        if (index == 0) { // Calls the parent callable
                            backwardsCalls++;
                            call.setCallable(callable);
                        } else if (index > 0) { // Forwards call
                            for (int k = i; k < attributeLayoutElements.size(); k++) {
                                AttributeLayoutElement el = (AttributeLayoutElement) attributeLayoutElements
                                        .get(k);
                                if (el instanceof Callable) {
                                    index--;
                                    if (index == 0) {
                                        call.setCallable((Callable) el);
                                        break;
                                    }
                                }
                            }
                        } else { // Backwards call
                            backwardsCalls++;
                            for (int k = i; k >= 0; k--) {
                                AttributeLayoutElement el = (AttributeLayoutElement) attributeLayoutElements
                                        .get(k);
                                if (el instanceof Callable) {
                                    index++;
                                    if (index == 0) {
                                        call.setCallable((Callable) el);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        backwardsCallCount = backwardsCalls;
    }

    private AttributeLayoutElement readNextAttributeElement(StringReader stream)
            throws IOException {
        int nextChar = stream.read();
        if (nextChar == -1) {
            return null;
        }
        if (nextChar == '[') {
            List body = readBody(getStreamUpToMatchingBracket(stream));
            return new Callable(body);
        } else {
            return readNextLayoutElement(stream);
        }
    }

    private LayoutElement readNextLayoutElement(StringReader stream)
            throws IOException {
        int nextChar = stream.read();
        if (nextChar == -1) {
            return null;
        }
        switch (nextChar) {
        // Integrals
        case 'B':
        case 'H':
        case 'I':
        case 'V':
            return new Integral(new String(new char[] { (char) nextChar }));
        case 'S':
        case 'F':
            return new Integral(new String(new char[] { (char) nextChar,
                    (char) stream.read() }));
        case 'P':
            stream.mark(1);
            if (stream.read() != 'O') {
                stream.reset();
                return new Integral("P" + (char) stream.read());
            } else {
                return new Integral("PO" + (char) stream.read());
            }
        case 'O':
            stream.mark(1);
            if (stream.read() != 'S') {
                stream.reset();
                return new Integral("O" + (char) stream.read());
            } else {
                return new Integral("OS" + (char) stream.read());
            }

            // Replication
        case 'N':
            char uint_type = (char) stream.read();
            stream.read(); // '['
            String str = readUpToMatchingBracket(stream);
            return new Replication("" + uint_type, str);

            // Union
        case 'T':
            String int_type = "" + (char) stream.read();
            if (int_type.equals("S")) {
                int_type += (char) stream.read();
            }
            List unionCases = new ArrayList();
            UnionCase c;
            while ((c = readNextUnionCase(stream)) != null) {
                unionCases.add(c);
            }
            stream.read(); // '('
            stream.read(); // '('
            stream.read(); // '['
            List body = null;
            stream.mark(1);
            char next = (char) stream.read();
            if (next != ']') {
                stream.reset();
                body = readBody(getStreamUpToMatchingBracket(stream));
            }
            return new Union(int_type, unionCases, body);

            // Call
        case '(':
            int number = readNumber(stream);
            stream.read(); // ')'
            return new Call(number);
            // Reference
        case 'K':
        case 'R':
            String string = "" + nextChar + (char) stream.read();
            char nxt = (char) stream.read();
            string += nxt;
            if (nxt == 'N') {
                string += (char) stream.read();
            }
            return new Reference(string);
        }
        return null;
    }

    /**
     * Read a UnionCase from the stream
     * 
     * @param stream
     * @return
     * @throws IOException
     */
    private UnionCase readNextUnionCase(StringReader stream) throws IOException {
        stream.mark(2);
        stream.read(); // '('
        char next = (char) stream.read();
        if (next == ')') {
            stream.reset();
            return null;
        }
        List tags = new ArrayList();
        while (next != ')') {
            tags.add(new Integer(readNumber(stream)));
            next = (char) stream.read();
        }
        stream.read(); // '['
        stream.mark(1);
        next = (char) stream.read();
        if (next == ']') {
            return new UnionCase(tags);
        } else {
            stream.reset();
            return new UnionCase(tags,
                    readBody(getStreamUpToMatchingBracket(stream)));
        }
    }

    /**
     * An AttributeLayoutElement is a part of an attribute layout and has one or
     * more bands associated with it, which transmit the AttributeElement data
     * for successive Attributes of this type.
     */
    private interface AttributeLayoutElement {

        /**
         * Read the bands associated with this part of the layout
         * 
         * @param in
         * @param count
         * @throws Pack200Exception
         * @throws IOException
         */
        public void readBands(InputStream in, int count) throws IOException,
                Pack200Exception;

        /**
         * Add the band data for this element at the given index to the
         * attribute
         * 
         * @param index
         * @param attribute
         */
        public void addToAttribute(int index, NewAttribute attribute);

    }

    private abstract class LayoutElement implements AttributeLayoutElement {

        protected int getLength(char uint_type) {
            int length = 0;
            ;
            switch (uint_type) {
            case 'B':
                length = 1;
                break;
            case 'H':
                length = 2;
                break;
            case 'I':
                length = 4;
                break;
            case 'V':
                length = 0;
                break;
            }
            return length;
        }
    }

    private class Integral extends LayoutElement {

        private final String tag;
        private long[] band;

        public Integral(String tag) {
            this.tag = tag;
        }

        public void readBands(InputStream in, int count) throws IOException,
                Pack200Exception {
            band = decodeBandLong(attributeLayout.getName() + "_" + tag, in,
                    getCodec(tag), count);
        }

        public void addToAttribute(int n, NewAttribute attribute) {
            long value = band[n];
            if (tag.equals("B") || tag.equals("FB")) {
                attribute.addInteger(1, value);
            } else if (tag.equals("SB")) {
                attribute.addInteger(1, (byte) value);
            } else if (tag.equals("H") || tag.equals("FH")) {
                attribute.addInteger(2, value);
            } else if (tag.equals("SH")) {
                attribute.addInteger(2, (short) value);
            } else if (tag.equals("I") || tag.equals("FI")) {
                attribute.addInteger(4, value);
            } else if (tag.equals("SI")) {
                attribute.addInteger(4, (int) value);
            } else if (tag.equals("V") || tag.equals("FV") || tag.equals("SV")) {
                // Don't add V's - they shouldn't be written out to the class
                // file
            } else if (tag.startsWith("PO")) {
                char uint_type = tag.substring(2).toCharArray()[0];
                int length = getLength(uint_type);
                attribute.addBCOffset(length, (int) value);
            } else if (tag.startsWith("P")) {
                char uint_type = tag.substring(1).toCharArray()[0];
                int length = getLength(uint_type);
                attribute.addBCIndex(length, (int) value);
            } else if (tag.startsWith("OS")) {
                char uint_type = tag.substring(1).toCharArray()[0];
                int length = getLength(uint_type);
                if (length == 1) {
                    value = (byte) value;
                } else if (length == 2) {
                    value = (short) value;
                } else if (length == 4) {
                    value = (int) value;
                }
                attribute.addBCLength(length, (int) value);
            } else if (tag.startsWith("O")) {
                char uint_type = tag.substring(1).toCharArray()[0];
                int length = getLength(uint_type);
                attribute.addBCLength(length, (int) value);
            }
        }

        long getValue(int index) {
            return band[index];
        }

    }

    /**
     * A replication is an array of layout elements, with an associated count
     */
    private class Replication extends LayoutElement {

        private final Integral countElement;

        private final List layoutElements = new ArrayList();

        public Replication(String tag, String contents) throws IOException {
            this.countElement = new Integral(tag);
            StringReader stream = new StringReader(contents);
            LayoutElement e;
            while ((e = readNextLayoutElement(stream)) != null) {
                layoutElements.add(e);
            }
        }

        public void readBands(InputStream in, int count) throws IOException,
                Pack200Exception {
            countElement.readBands(in, count);
            int arrayCount = 0;
            for (int i = 0; i < count; i++) {
                arrayCount += countElement.getValue(i);
            }
            for (Iterator iter = layoutElements.iterator(); iter.hasNext();) {
                LayoutElement element = (LayoutElement) iter.next();
                element.readBands(in, arrayCount);
            }
        }

        public void addToAttribute(int index, NewAttribute attribute) {
            // Add the count value
            countElement.addToAttribute(index, attribute);

            // Add the corresponding array values
            int offset = 0;
            for (int i = 0; i < index; i++) {
                offset += countElement.getValue(i);
            }
            long numElements = countElement.getValue(index);
            for (int i = offset; i < offset + numElements; i++) {
                for (Iterator iter = layoutElements.iterator(); iter.hasNext();) {
                    LayoutElement element = (LayoutElement) iter.next();
                    element.addToAttribute(i, attribute);
                }
            }
        }
    }

    /**
     * A Union is a type of layout element where the tag value acts as a
     * selector for one of the union cases
     */
    private class Union extends LayoutElement {

        private final Integral unionTag;
        private final List unionCases;
        private final List defaultCaseBody;
        private int[] caseCounts;
        private int defaultCount;

        public Union(String tag, List unionCases, List body) {
            this.unionTag = new Integral(tag);
            this.unionCases = unionCases;
            this.defaultCaseBody = body;
        }

        public void readBands(InputStream in, int count) throws IOException,
                Pack200Exception {
            unionTag.readBands(in, count);
            long[] values = unionTag.band;
            // Count the band size for each union case then read the bands
            caseCounts = new int[unionCases.size()];
            for (int i = 0; i < caseCounts.length; i++) {
                UnionCase unionCase = (UnionCase) unionCases.get(i);
                for (int j = 0; j < values.length; j++) {
                    if (unionCase.hasTag(values[j])) {
                        caseCounts[i]++;
                    }
                }
                unionCase.readBands(in, caseCounts[i]);
            }
            // Count number of default cases then read the default bands
            for (int i = 0; i < values.length; i++) {
                boolean found = false;
                for (Iterator iter = unionCases.iterator(); iter.hasNext();) {
                    UnionCase unionCase = (UnionCase) iter.next();
                    if (unionCase.hasTag(values[i])) {
                        found = true;
                    }
                }
                if (!found) {
                    defaultCount++;
                }
            }
            if (defaultCaseBody != null) {
                for (Iterator iter = defaultCaseBody.iterator(); iter.hasNext();) {
                    LayoutElement element = (LayoutElement) iter.next();
                    element.readBands(in, defaultCount);
                }
            }
        }

        public void addToAttribute(int n, NewAttribute attribute) {
            unionTag.addToAttribute(n, attribute);
            int offset = 0;
            long[] tagBand = unionTag.band;
            long tag = unionTag.getValue(n);
            boolean defaultCase = true;
            for (Iterator iter = unionCases.iterator(); iter.hasNext();) {
                UnionCase element = (UnionCase) iter.next();
                if (element.hasTag(tag)) {
                    defaultCase = false;
                    for (int j = 0; j < n; j++) {
                        if (element.hasTag(tagBand[j])) {
                            offset++;
                        }
                    }
                    element.addToAttribute(offset, attribute);
                }
            }
            if (defaultCase) {
                // default case
                int defaultOffset = 0;
                for (int j = 0; j < n; j++) {
                    boolean found = false;
                    for (Iterator iter = unionCases.iterator(); iter.hasNext();) {
                        UnionCase element = (UnionCase) iter.next();
                        if (element.hasTag(tagBand[j])) {
                            found = true;
                        }
                    }
                    if (!found) {
                        defaultOffset++;
                    }
                }
                if (defaultCaseBody != null) {
                    for (Iterator iter = defaultCaseBody.iterator(); iter
                            .hasNext();) {
                        LayoutElement element = (LayoutElement) iter.next();
                        element.addToAttribute(defaultOffset, attribute);
                    }
                }
            }
        }

    }

    private class Call extends LayoutElement {

        private final int callableIndex;
        private Callable callable;

        public Call(int callableIndex) {
            this.callableIndex = callableIndex;
        }

        public void setCallable(Callable callable) {
            this.callable = callable;
            if (callableIndex < 1) {
                callable.setBackwardsCallable();
            }
        }

        public void readBands(InputStream in, int count) {
            /*
             * We don't read anything here, but we need to pass the extra count
             * to the callable if it's a forwards call. For backwards callables
             * the count is transmitted directly in the attribute bands and so
             * it is added later.
             */
            if (callableIndex > 0) {
                callable.addCount(count);
            }
        }

        public void addToAttribute(int n, NewAttribute attribute) {
            callable.addNextToAttribute(attribute);
        }
    }

    /**
     * Constant Pool Reference
     */
    private class Reference extends LayoutElement {

        private final String tag;

        private Object band;

        private final int length;

        public Reference(String tag) {
            this.tag = tag;
            length = getLength(tag.charAt(tag.length()));
        }

        public void readBands(InputStream in, int count) throws IOException,
                Pack200Exception {
            if (tag.startsWith("KI")) { // Integer
                band = parseCPIntReferences(attributeLayout.getName(), in,
                        Codec.UNSIGNED5, count);
            } else if (tag.startsWith("KJ")) { // Long
                band = parseCPLongReferences(attributeLayout.getName(), in,
                        Codec.UNSIGNED5, count);
            } else if (tag.startsWith("KF")) { // Float
                band = parseCPFloatReferences(attributeLayout.getName(), in,
                        Codec.UNSIGNED5, count);
            } else if (tag.startsWith("KD")) { // Double
                band = parseCPDoubleReferences(attributeLayout.getName(), in,
                        Codec.UNSIGNED5, count);
            } else if (tag.startsWith("KS")) { // String
                band = parseCPStringReferences(attributeLayout.getName(), in,
                        Codec.UNSIGNED5, count);
            } else if (tag.startsWith("RC")) { // Class
                band = parseCPClassReferences(attributeLayout.getName(), in,
                        Codec.UNSIGNED5, count);
            } else if (tag.startsWith("RS")) { // Signature
                band = parseCPSignatureReferences(attributeLayout.getName(),
                        in, Codec.UNSIGNED5, count);
            } else if (tag.startsWith("RD")) { // Descriptor
                band = parseCPDescriptorReferences(attributeLayout.getName(),
                        in, Codec.UNSIGNED5, count);
            } else if (tag.startsWith("RF")) { // Field Reference
                band = parseCPFieldRefReferences(attributeLayout.getName(), in,
                        Codec.UNSIGNED5, count);
            } else if (tag.startsWith("RM")) { // Method Reference
                band = parseCPMethodRefReferences(attributeLayout.getName(),
                        in, Codec.UNSIGNED5, count);
            } else if (tag.startsWith("RI")) { // Interface Method Reference
                band = parseCPInterfaceMethodRefReferences(attributeLayout
                        .getName(), in, Codec.UNSIGNED5, count);
            } else if (tag.startsWith("RU")) { // UTF8 String
                band = parseCPUTF8References(attributeLayout.getName(), in,
                        Codec.UNSIGNED5, count);
            }
        }

        public void addToAttribute(int n, NewAttribute attribute) {
            if (tag.startsWith("KI")) { // Integer
                attribute.addCPConstant(length, ((CPInteger[]) band)[n]);
            } else if (tag.startsWith("KJ")) { // Long
                attribute.addCPConstant(length, ((CPLong[]) band)[n]);
            } else if (tag.startsWith("KF")) { // Float
                attribute.addCPConstant(length, ((CPFloat[]) band)[n]);
            } else if (tag.startsWith("KD")) { // Double
                attribute.addCPConstant(length, ((CPDouble[]) band)[n]);
            } else if (tag.startsWith("KS")) { // String
                attribute.addCPConstant(length, ((CPString[]) band)[n]);
            } else if (tag.startsWith("RC")) { // Class
                attribute.addCPClass(length, ((CPClass[]) band)[n]);
            } else if (tag.startsWith("RS")) { // Signature
                attribute.addCPUTF8(length, ((CPUTF8[]) band)[n]);
            } else if (tag.startsWith("RD")) { // Descriptor
                attribute.addCPNameAndType(length, ((CPNameAndType[]) band)[n]);
            } else if (tag.startsWith("RF")) { // Field Reference
                attribute.addCPFieldRef(length, ((CPFieldRef[]) band)[n]);
            } else if (tag.startsWith("RM")) { // Method Reference
                attribute.addCPMethodRef(length, ((CPMethodRef[]) band)[n]);
            } else if (tag.startsWith("RI")) { // Interface Method Reference
                attribute.addCPIMethodRef(length,
                        ((CPInterfaceMethodRef[]) band)[n]);
            } else if (tag.startsWith("RU")) { // UTF8 String
                attribute.addCPUTF8(length, ((CPUTF8[]) band)[n]);
            }
        }

    }

    private static class Callable implements AttributeLayoutElement {

        private final List body;

        private boolean isBackwardsCallable;

        public Callable(List body) throws IOException {
            this.body = body;
        }

        private int count;
        private int index;

        /**
         * Used by calls when adding band contents to attributes so they don't
         * have to keep track of the internal index of the callable
         * 
         * @param attribute
         */
        public void addNextToAttribute(NewAttribute attribute) {
            for (Iterator iter = body.iterator(); iter.hasNext();) {
                LayoutElement element = (LayoutElement) iter.next();
                element.addToAttribute(index, attribute);
            }
            index++;
        }

        /**
         * Adds the count of a call to this callable (ie the number of calls)
         * 
         * @param count
         */
        public void addCount(int count) {
            this.count += count;
        }

        public void readBands(InputStream in, int count) throws IOException,
                Pack200Exception {
            count += this.count;
            for (Iterator iter = body.iterator(); iter.hasNext();) {
                LayoutElement element = (LayoutElement) iter.next();
                element.readBands(in, count);
            }
        }

        public void addToAttribute(int n, NewAttribute attribute) {
            // Ignore n because bands also contain element parts from calls
            for (Iterator iter = body.iterator(); iter.hasNext();) {
                LayoutElement element = (LayoutElement) iter.next();
                element.addToAttribute(index, attribute);
            }
            index++;
        }

        public boolean isBackwardsCallable() {
            return isBackwardsCallable;
        }

        /**
         * Tells this Callable that it is a backwards callable
         */
        public void setBackwardsCallable() {
            this.isBackwardsCallable = true;
        }
    }

    /**
     * A Union case
     */
    private class UnionCase extends LayoutElement {

        private List body;

        private final List tags;

        public UnionCase(List tags) {
            this.tags = tags;
        }

        public boolean hasTag(long l) {
            return tags.contains(new Integer((int) l));
        }

        public UnionCase(List tags, List body) throws IOException {
            this.tags = tags;
            this.body = body;
        }

        public void readBands(InputStream in, int count) throws IOException,
                Pack200Exception {
            if (body != null) {
                for (Iterator iter = body.iterator(); iter.hasNext();) {
                    LayoutElement element = (LayoutElement) iter.next();
                    element.readBands(in, count);
                }
            }
        }

        public void addToAttribute(int index, NewAttribute attribute) {
            if (body != null) {
                for (Iterator iter = body.iterator(); iter.hasNext();) {
                    LayoutElement element = (LayoutElement) iter.next();
                    element.addToAttribute(index, attribute);
                }
            }
        }
    }

    /**
     * Utility method to get the contents of the given stream, up to the next
     * ']', (ignoring pairs of brackets '[' and ']')
     * 
     * @param stream
     * @return
     * @throws IOException
     */
    private StringReader getStreamUpToMatchingBracket(StringReader stream)
            throws IOException {
        StringBuffer sb = new StringBuffer();
        int foundBracket = -1;
        while (foundBracket != 0) {
            char c = (char) stream.read();
            if (c == ']') {
                foundBracket++;
            }
            if (c == '[') {
                foundBracket--;
            }
            if (!(foundBracket == 0)) {
                sb.append(c);
            }
        }
        return new StringReader(sb.toString());
    }

    /**
     * Returns the {@link BHSDCodec} that should be used for the given layout
     * element
     * 
     * @param layoutElement
     */
    public BHSDCodec getCodec(String layoutElement) {
        if (layoutElement.indexOf("O") >= 0) { //$NON-NLS-1$
            return Codec.BRANCH5;
        } else if (layoutElement.indexOf("P") >= 0) { //$NON-NLS-1$
            return Codec.BCI5;
        } else if (layoutElement.indexOf("S") >= 0 && layoutElement.indexOf("KS") < 0 //$NON-NLS-1$ //$NON-NLS-2$
                && layoutElement.indexOf("RS") < 0) { //$NON-NLS-1$
            return Codec.SIGNED5;
        } else if (layoutElement.indexOf("B") >= 0) { //$NON-NLS-1$
            return Codec.BYTE1;
        } else {
            return Codec.UNSIGNED5;
        }
    }

    /**
     * Utility method to get the contents of the given stream, up to the next
     * ']', (ignoring pairs of brackets '[' and ']')
     * 
     * @param stream
     * @return
     * @throws IOException
     */
    private String readUpToMatchingBracket(StringReader stream)
            throws IOException {
        StringBuffer sb = new StringBuffer();
        int foundBracket = -1;
        while (foundBracket != 0) {
            char c = (char) stream.read();
            if (c == ']') {
                foundBracket++;
            }
            if (c == '[') {
                foundBracket--;
            }
            if (!(foundBracket == 0)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Read a number from the stream and return it
     * 
     * @param stream
     * @return
     * @throws IOException
     */
    private int readNumber(StringReader stream) throws IOException {
        stream.mark(1);
        char first = (char) stream.read();
        boolean negative = first == '-';
        if (!negative) {
            stream.reset();
        }
        stream.mark(100);
        int i;
        int length = 0;
        while ((i = (stream.read())) != -1 && Character.isDigit((char) i)) {
            length++;
        }
        stream.reset();
        char[] digits = new char[length];
        int read = stream.read(digits);
        if (read != digits.length) {
            throw new IOException("Error reading from the input stream");
        }
        return Integer.parseInt((negative ? "-" : "") + new String(digits));
    }

    /**
     * Read a 'body' section of the layout from the given stream
     * 
     * @param stream
     * @return List of LayoutElements
     * @throws IOException
     */
    private List readBody(StringReader stream) throws IOException {
        List layoutElements = new ArrayList();
        LayoutElement e;
        while ((e = readNextLayoutElement(stream)) != null) {
            layoutElements.add(e);
        }
        return layoutElements;
    }

    public int getBackwardsCallCount() {
        return backwardsCallCount;
    }

    /**
     * Once the attribute bands have been read the callables can be informed
     * about the number of times each is subject to a backwards call. This
     * method is used to set this information.
     * 
     * @param backwardsCalls
     *            one int for each backwards callable, which contains the number
     *            of times that callable is subject to a backwards call.
     * @throws IOException
     */
    public void setBackwardsCalls(int[] backwardsCalls) throws IOException {
        int index = 0;
        parseLayout();
        for (Iterator iter = attributeLayoutElements.iterator(); iter.hasNext();) {
            AttributeLayoutElement element = (AttributeLayoutElement) iter
                    .next();
            if (element instanceof Callable
                    && ((Callable) element).isBackwardsCallable()) {
                ((Callable) element).addCount(backwardsCalls[index]);
                index++;
            }
        }
    }

}