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
package javax.swing;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleHyperlink;
import javax.accessibility.AccessibleHypertext;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleText;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.WrappedPlainView;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.harmony.luni.util.NotImplementedException;
import org.apache.harmony.x.swing.StringConstants;

/**
 * <p>
 * <i>JEditorPane</i>
 * </p>
 * <h3>Implementation Notes:</h3>
 * <ul>
 * <li>The <code>serialVersionUID</code> fields are explicitly declared as a performance
 * optimization, not as a guarantee of serialization compatibility.</li>
 * </ul>
 */
public class JEditorPane extends JTextComponent {
    private static final long serialVersionUID = -767121239635831550L;

    protected class AccessibleJEditorPane extends JTextComponent.AccessibleJTextComponent {
        private static final long serialVersionUID = -6869835326921704467L;

        @Override
        public String getAccessibleDescription() {
            return getContentType();
        }

        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet set = super.getAccessibleStateSet();
            set.add(AccessibleState.MULTI_LINE);
            return set;
        }
    }

    protected class AccessibleJEditorPaneHTML extends AccessibleJEditorPane {
        private static final long serialVersionUID = -5072331196784098614L;

        AccessibleText text;

        @Override
        public AccessibleText getAccessibleText() {
            if (text == null) {
                text = new JEditorPaneAccessibleHypertextSupport();
            }
            return text;
        }
    }

    protected class JEditorPaneAccessibleHypertextSupport extends AccessibleJEditorPane
            implements AccessibleHypertext {
        private static final long serialVersionUID = -1462897229238717575L;

        //Not implemented
        public class HTMLLink extends AccessibleHyperlink {
            public HTMLLink(Element e) throws NotImplementedException {
                super();
                throw new NotImplementedException();
            }

            @Override
            public boolean doAccessibleAction(int i) throws NotImplementedException {
                throw new NotImplementedException();
            }

            @Override
            public Object getAccessibleActionAnchor(int i) throws NotImplementedException {
                throw new NotImplementedException();
            }

            @Override
            public int getAccessibleActionCount() throws NotImplementedException {
                throw new NotImplementedException();
            }

            @Override
            public String getAccessibleActionDescription(int i) throws NotImplementedException {
                throw new NotImplementedException();
            }

            @Override
            public Object getAccessibleActionObject(int i) throws NotImplementedException {
                throw new NotImplementedException();
            }

            @Override
            public int getEndIndex() throws NotImplementedException {
                throw new NotImplementedException();
            }

            @Override
            public int getStartIndex() throws NotImplementedException {
                throw new NotImplementedException();
            }

            @Override
            public boolean isValid() throws NotImplementedException {
                throw new NotImplementedException();
            }
        }

        public JEditorPaneAccessibleHypertextSupport() throws NotImplementedException {
            super();
            throw new NotImplementedException();
        }

        public AccessibleHyperlink getLink(int linkIndex) throws NotImplementedException {
            throw new NotImplementedException();
        }

        public int getLinkCount() throws NotImplementedException {
            throw new NotImplementedException();
        }

        public int getLinkIndex(int charIndex) throws NotImplementedException {
            throw new NotImplementedException();
        }

        public String getLinkText(int linkIndex) throws NotImplementedException {
            throw new NotImplementedException();
        }
    }

    public static final String HONOR_DISPLAY_PROPERTIES = "JEditorPane.honorDisplayProperties";

    public static final String W3C_LENGTH_UNITS = "JEditorPane.w3cLengthUnits";

    private static final String uiClassID = "EditorPaneUI";

    static final class PlainEditorKit extends DefaultEditorKit implements ViewFactory {
        private static final long serialVersionUID = 1L;

        public View create(Element elem) {
            return new WrappedPlainView(elem);
        }

        @Override
        public ViewFactory getViewFactory() {
            return this;
        }
    }

    private static final String PLAIN_CONTENT_TYPE = "text/plain";

    private static final String HTML_CONTENT_TYPE = "text/html";

    private static final String RTF_CONTENT_TYPE = "text/rtf";

    private static final String REFERENCE_TAIL_PATTERN = "#.*";

    private static List<String> contentTypes = new ArrayList<String>();

    private static List<String> editorKitNames = new ArrayList<String>();

    private static List<ClassLoader> classLoaders = new ArrayList<ClassLoader>();

    private static final String RTF_HEADER = "{\\rtf";

    private static final String HTML_HEADER = "<html";

    private List<String> localContentTypes = new ArrayList<String>();

    private List<EditorKit> localEditorKits = new ArrayList<EditorKit>();

    private String contentType = PLAIN_CONTENT_TYPE;

    private EditorKit editorKit;

    private URL currentPage;

    private AccessibleContext accessible;

    private AccessibleContext accessibleHTML;
    static {
        contentTypes.add(PLAIN_CONTENT_TYPE);
        contentTypes.add(HTML_CONTENT_TYPE);
        contentTypes.add(RTF_CONTENT_TYPE);
        editorKitNames.add("javax.swing.JEditorPane$PlainEditorKit");
        editorKitNames.add("javax.swing.text.html.HTMLEditorKit");
        editorKitNames.add("javax.swing.text.rtf.RTFEditorKit");
        classLoaders.add(null);
        classLoaders.add(null);
        classLoaders.add(null);
    }

    public static EditorKit createEditorKitForContentType(final String contentType) {
        int index = contentTypes.indexOf(contentType);
        if (index < 0) {
            return null;
        }
        String kitName = editorKitNames.get(index);
        Object loader = classLoaders.get(index);
        EditorKit editorKit = null;
        try {
            editorKit = (EditorKit) ((loader != null) ? ((ClassLoader) loader).loadClass(
                    kitName).newInstance() : Class.forName(kitName).newInstance());
        } catch (IllegalAccessException e) {
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        }
        return editorKit;
    }

    public static String getEditorKitClassNameForContentType(final String type) {
        int index = contentTypes.indexOf(type);
        return (index >= 0) ? editorKitNames.get(index) : null;
    }

    public static void registerEditorKitForContentType(final String type,
            final String editorKitName) {
        registerEditorKitForContentType(type, editorKitName, null);
    }

    public static void registerEditorKitForContentType(final String type,
            final String editorKitName, final ClassLoader loader) {

        if (type == null || editorKitName == null) {
            throw new NullPointerException();
        }

        int index = contentTypes.indexOf(type);
        if (index >= 0) {
            contentTypes.remove(index);
            editorKitNames.remove(index);
            classLoaders.remove(index);
        }
        contentTypes.add(type);
        editorKitNames.add(editorKitName);
        classLoaders.add(loader);
    }

    public JEditorPane() {
        setFocusCycleRoot(true);
    }

    public JEditorPane(final String page) throws IOException {
        this();
        setPage(page);
    }

    public JEditorPane(final String type, final String text) {
        this();
        if (type == null) {
            throw new NullPointerException();
        }
        setContentType(type);
        setText(text);
    }

    public JEditorPane(final URL page) throws IOException {
        this();
        setPage(page);
    }

    public synchronized void addHyperlinkListener(final HyperlinkListener listener) {
        listenerList.add(HyperlinkListener.class, listener);
    }

    protected EditorKit createDefaultEditorKit() {
        return new PlainEditorKit();
    }

    public void fireHyperlinkUpdate(final HyperlinkEvent event) {
        HyperlinkListener[] listeners = getHyperlinkListeners();
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].hyperlinkUpdate(event);
        }
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (HTML_CONTENT_TYPE.equals(contentType)) {
            if (accessibleHTML == null) {
                accessibleHTML = new AccessibleJEditorPaneHTML();
            }
            return accessibleHTML;
        }
        if (accessible == null) {
            accessible = new AccessibleJEditorPane();
        }
        return accessible;
    }

    public final String getContentType() {
        return contentType;
    }

    public EditorKit getEditorKit() {
        if (editorKit == null) {
            editorKit = createDefaultEditorKit();
        }
        return editorKit;
    }

    public EditorKit getEditorKitForContentType(final String type) {
        int index = localContentTypes.indexOf(type);
        if (index >= 0) {
            return localEditorKits.get(index);
        }
        EditorKit kit = JEditorPane.createEditorKitForContentType(type);
        return (kit == null) ? createDefaultEditorKit() : kit;
    }

    public synchronized HyperlinkListener[] getHyperlinkListeners() {
        return getListeners(HyperlinkListener.class);
    }

    public URL getPage() {
        return currentPage;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        Container parent = getParent();
        if (parent instanceof JViewport) {
            Dimension min = getMinimumSize();
            if (!getScrollableTracksViewportWidth()) {
                int width = parent.getWidth();
                if (width < min.width) {
                    d.width = min.width;
                }
            }
            if (!getScrollableTracksViewportHeight()) {
                int height = parent.getHeight();
                if (height < min.height) {
                    d.height = min.height;
                }
            }
        }
        return d;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        Container parent = getParent();
        if (parent instanceof JViewport) {
            int height = parent.getHeight();
            Dimension min = getMinimumSize();
            Dimension max = getMaximumSize();
            return height >= min.height && height <= max.height;
        }
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        Container parent = getParent();
        if (parent instanceof JViewport) {
            int width = parent.getWidth();
            Dimension min = getMinimumSize();
            Dimension max = getMaximumSize();
            return width >= min.width && width <= max.width;
        }
        return false;
    }

    private String getContentTypeByInputStream(final InputStream stream) {
        int bufferSize = RTF_HEADER.length();
        byte[] bytes = new byte[bufferSize];
        String buffer = null;
        try {
            int status = stream.read(bytes, 0, 1);
            int index = 0;
            boolean notRtf = false;
            while (status > 0) {
                if (bytes[0] == '<') {
                    notRtf = true;
                    status = stream.read(bytes, 0, bufferSize);
                    if (status < 0 || status < bufferSize) {
                        break;
                    }
                    buffer = new String(bytes);
                    if (("<" + buffer.toLowerCase()).startsWith(HTML_HEADER)) {
                        return HTML_CONTENT_TYPE;
                    }
                } else {
                    if (index < RTF_HEADER.length() && !notRtf) {
                        if ((char) bytes[0] != RTF_HEADER.charAt(index)) {
                            notRtf = true;
                        } else {
                            if (index++ == RTF_HEADER.length() - 1) {
                                return RTF_CONTENT_TYPE;
                            }
                        }
                    }
                    status = stream.read(bytes, 0, 1);
                }
            }
        } catch (IOException e) {
        }
        return PLAIN_CONTENT_TYPE;
    }

    private String getBaseURL(final String url) {
        return (url == null) ? null : url.replaceAll(REFERENCE_TAIL_PATTERN, "");
    }

    protected InputStream getStream(final URL url) throws IOException {
        InputStream inputStream = url.openStream();
        if (url.getProtocol() == "http") {
            getDocument().putProperty(Document.StreamDescriptionProperty,
                    getBaseURL(url.toString()));
        }
        setContentType(getContentTypeByInputStream(inputStream));
        //Perhaps, it is not best solution. I'm going to think about this one
        inputStream.close();
        return url.openStream();
    }

    @Override
    public String getText() {
        StringWriter writer = new StringWriter();
        try {
            super.write(writer);
        } catch (IOException e) {
        }
        return writer.toString();
    }

    @Override
    public String getUIClassID() {
        return uiClassID;
    }

    @Override
    protected String paramString() {
        return super.paramString() + "," + "contentType=" + contentType + "," + "editorKit="
                + editorKit + "," + "document=" + getDocument() + "," + "currentPage="
                + currentPage;
    }

    public void read(final InputStream stream, final Object type) throws IOException {
        if (type instanceof String) {
            setContentType((String) type);
        }
        try {
            Document doc = getDocument();
            doc.putProperty(StringConstants.IGNORE_CHARSET_DIRECTIVE, Boolean.TRUE);
            editorKit.read(new InputStreamReader(stream), doc, 0);
        } catch (BadLocationException e) {
        }
    }

    public synchronized void removeHyperlinkListener(final HyperlinkListener listener) {
        listenerList.remove(HyperlinkListener.class, listener);
    }

    @Override
    public synchronized void replaceSelection(final String s) {
        if (!isEditable()) {
            new DefaultEditorKit.BeepAction().actionPerformed(null);
            return;
        }
        int start = getSelectionStart();
        int end = getSelectionEnd();
        Document doc = getDocument();
        try {
            if (start != end) {
                doc.remove(start, end - start);
            }
            //May be these attributes placed in Document ????
            AttributeSet as = (editorKit instanceof StyledEditorKit) ? ((StyledEditorKit) editorKit)
                    .getInputAttributes()
                    : null;
            if (s != null) {
                doc.insertString(start, s, as);
            }
        } catch (BadLocationException e) {
        }
    }

    public void scrollToReference(final String ref) {
         Document doc = getDocument();
        if (ref == null || !(doc instanceof HTMLDocument)) {
            return;
        }
        HTMLDocument.Iterator it = ((HTMLDocument)doc).getIterator(HTML.Tag.A);
        int offset = 0;
        while (it.isValid()) {
            AttributeSet set = it.getAttributes();
            Object name = set.getAttribute(HTML.Attribute.NAME);
            if (ref.equals(name)) {
                offset = it.getStartOffset();
                break;
            }
            it.next();
        }
        Rectangle rect = null;
        try {
            rect = modelToView(offset);
        } catch (BadLocationException e) {
        }
        Rectangle visibleRect = getVisibleRect();
        if (visibleRect != null) {
            rect.height = visibleRect.height;
        }
        scrollRectToVisible(rect);
    }

    private boolean changeEditoKit(final String contentType) {
        return !(/*(RTF_CONTENT_TYPE.equals(contentType) && editorKit instanceof RTFEditorKit)
                 ||*/ (HTML_CONTENT_TYPE.equals(contentType) && editorKit instanceof HTMLEditorKit)
                 || (PLAIN_CONTENT_TYPE.equals(contentType) && editorKit instanceof PlainEditorKit));
    }

    public final void setContentType(final String type) {
        if (type == null) {
            throw new NullPointerException("Content type is null");
        }

        int index = contentTypes.indexOf(type);
        contentType = (index >= 0) ? (String)contentTypes.get(index)
                                  : PLAIN_CONTENT_TYPE;
        if (changeEditoKit(contentType)) {
            EditorKit kit = getEditorKitForContentType(contentType);
            updateEditorKit((kit != null) ? kit : new PlainEditorKit());
            updateDocument(editorKit);
        } 
    }

    private String getContentTypeByEditorKit(final EditorKit kit) {
        if (kit == null) {
            return PLAIN_CONTENT_TYPE;
        }
        int index = localEditorKits.indexOf(kit);
        if (index >= 0) {
            return localContentTypes.get(index);
        }
        index = editorKitNames.indexOf(kit.getClass().getName());
        if (index >= 0) {
            return contentTypes.get(index);
        }
        return PLAIN_CONTENT_TYPE;
    }

    private void updateEditorKit(final EditorKit kit) {
        if (editorKit != null) {
            editorKit.deinstall(this);
        }
        EditorKit oldEditorKit = editorKit;
        if (kit != null) {
            kit.install(this);
        }
        editorKit = kit;
        firePropertyChange("editorKit", oldEditorKit, kit);
    }

    private void updateDocument(final EditorKit kit) {
        if (kit != null) {
            setDocument(kit.createDefaultDocument());
        }
    }

    public void setEditorKit(final EditorKit kit) {
        String newContentType = getContentTypeByEditorKit(kit);
        updateEditorKit(kit);
        updateDocument(kit);
        contentType = newContentType;
    }

    public void setEditorKitForContentType(final String type, final EditorKit kit) {
        if (type == null || kit == null) {
            throw new NullPointerException();
        }
  
        int index = localContentTypes.indexOf(contentType);
        if (index >= 0) {
            localContentTypes.remove(index);
            localEditorKits.remove(index);
        }
        localContentTypes.add(type);
        localEditorKits.add(kit);
    }

    public void setPage(final String page) throws IOException {
        setPage(new URL(page));
    }

    private void documentLoading(final InputStream str, final Document doc, final URL url)
            throws IOException {
        try {
            editorKit.read(str, doc, 0);
        } catch (ChangedCharSetException e) {
            try {
                doc.putProperty(StringConstants.IGNORE_CHARSET_DIRECTIVE, Boolean.TRUE);
                doc.remove(0, doc.getLength());
                final String htmlAttribute = e.getCharSetSpec();
                final int charSetIndex = htmlAttribute.lastIndexOf("charset=");
                if (charSetIndex >= 0) {
                    String charSet = htmlAttribute.substring(charSetIndex + 8);
                    InputStreamReader reader = new InputStreamReader(url.openStream(), Charset
                            .forName(charSet));
                    editorKit.read(reader, doc, 0);
                }
            } catch (BadLocationException e1) {
            }
        } catch (BadLocationException e) {
        }
    }

    private class AsynchLoad extends Thread {
        InputStream inputStream;
        boolean successfulLoading = true;
        URL url;

        public AsynchLoad(final int priority, final InputStream stream,
                          final URL url) {
           super();
           setPriority(priority);
           inputStream = stream;
           this.url = url;
        }

        @Override
        public void run() {
            try {
                documentLoading(inputStream, getDocument(), url);
            } catch (IOException e) {
                successfulLoading = false;
            }
        }
    }

    public void setPage(final URL page) throws IOException {
        if (page == null) {
            throw new IOException("Page is null");
        } 

        String url = page.toString();
        String baseUrl = getBaseURL(url);
        Document oldDoc = getDocument();
        if (baseUrl != null
            && oldDoc != null
            && baseUrl.equals(oldDoc
                .getProperty(Document.StreamDescriptionProperty))) {

            scrollToReference(page.getRef());
            return;
        }
        InputStream stream = getStream(page);
        if (stream == null) {
            return;
        }
        Document newDoc = editorKit.createDefaultDocument();
        // Perhaps, it is reasonable only for HTMLDocument...
        if (newDoc instanceof HTMLDocument) {
            newDoc.putProperty(Document.StreamDescriptionProperty, baseUrl);
            newDoc.putProperty(StringConstants.IGNORE_CHARSET_DIRECTIVE,
                               new Boolean(false));
            try {
                ((HTMLDocument)newDoc).setBase(new URL(baseUrl));
            } catch (IOException e) {
            }
        }
        // TODO Asynch loading doesn't work with completely.
        // Also page property change event is written incorrectly now
        // (at the asynchrounous loading), because loading may not be
        // completed.
        // int asynchronousLoadPriority = getAsynchronousLoadPriority(newDoc);
        int asynchronousLoadPriority = -1;
        if (asynchronousLoadPriority >= 0) {
            setDocument(newDoc);
            AsynchLoad newThread = new AsynchLoad(asynchronousLoadPriority,
                                                  stream, page);
            newThread.start();
            if (newThread.successfulLoading) {
                changePage(page);
            }
        } else {
            try {
                documentLoading(stream, newDoc, page);
                stream.close();
                setDocument(newDoc);
                changePage(page);
            } catch (IOException e) {
            }
        }
    }

    private void changePage(final URL newPage) {
        URL oldPage = currentPage;
        currentPage = newPage;
        firePropertyChange("page", oldPage, currentPage);
    }

    private int getAsynchronousLoadPriority(final Document doc) {
        if (doc instanceof AbstractDocument) {
            return ((AbstractDocument)doc).getAsynchronousLoadPriority();
        }
        return -1;
    }

    @Override
    public synchronized void setText(final String content) {
        StringReader reader = new StringReader(content == null ? "" : content);

        try {
            read(reader, contentType);
        } catch (IOException e) {
        }
    }
}
