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
 * @author Pavel Dolgov, Michael Danilov
 * @version $Revision$
 */
package java.awt;

import java.awt.datatransfer.Clipboard;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.im.InputMethodHighlight;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.peer.*;
import java.io.Serializable;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.apache.harmony.awt.datatransfer.DTK;
import org.apache.harmony.awt.gl.*;
import org.apache.harmony.awt.gl.image.*;


class ToolkitImpl extends Toolkit {

    static final Hashtable<Serializable, Image> imageCache = new Hashtable<Serializable, Image>();

    @Override
    public void sync() {
        lockAWT();
        try {
        } finally {
            unlockAWT();
        }
    }

    @Override
    protected TextAreaPeer createTextArea(TextArea a0) throws HeadlessException {
        lockAWT();
        try {
            checkHeadless();
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    public int checkImage(Image image, int width, int height, ImageObserver observer) {
        lockAWT();
        try {
            if(width == 0 || height == 0){
                return ImageObserver.ALLBITS;
            }
            if(!(image instanceof OffscreenImage)){
                return ImageObserver.ALLBITS;
            }
            OffscreenImage oi = (OffscreenImage)image;
            return oi.checkImage(observer);
        } finally {
            unlockAWT();
        }
    }

    @Override
    public Image createImage(ImageProducer producer) {
        lockAWT();
        try {
            return new OffscreenImage(producer);
        } finally {
            unlockAWT();
        }
    }

    @Override
    public Image createImage(byte[] imagedata, int imageoffset, int imagelength) {
        lockAWT();
        try {
            return new OffscreenImage(new ByteArrayDecodingImageSource(imagedata,
                    imageoffset, imagelength));
        } finally {
            unlockAWT();
        }
    }

    @Override
    public Image createImage(URL url) {
        lockAWT();
        try {
            return new OffscreenImage(new URLDecodingImageSource(url));
        } finally {
            unlockAWT();
        }
    }

    @Override
    public Image createImage(String filename) {
        lockAWT();
        try {
            return new OffscreenImage(new FileDecodingImageSource(filename));
        } finally {
            unlockAWT();
        }
    }

    @Override
    public ColorModel getColorModel() throws HeadlessException {
        lockAWT();
        try {
            return GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getDefaultScreenDevice().getDefaultConfiguration().
                    getColorModel();
        } finally {
            unlockAWT();
        }
    }

    @Override
    public FontMetrics getFontMetrics(Font font) {
        lockAWT();
        try {
            return getGraphicsFactory().getFontMetrics(font);
        } finally {
            unlockAWT();
        }
    }

    @Override
    public boolean prepareImage(Image image, int width, int height, ImageObserver observer) {
        lockAWT();
        try {
            if(width == 0 || height == 0){
                return true;
            }
            if(!(image instanceof OffscreenImage)){
                return true;
            }
            OffscreenImage oi = (OffscreenImage)image;
            return oi.prepareImage(observer);
        } finally {
            unlockAWT();
        }
    }

    @Override
    public void beep() {
        lockAWT();
        try {
        } finally {
            unlockAWT();
        }
    }

    @Override
    protected ButtonPeer createButton(Button a0) throws HeadlessException {
        lockAWT();
        try {
            checkHeadless();
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    protected CanvasPeer createCanvas(Canvas a0) {
        lockAWT();
        try {
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    protected CheckboxPeer createCheckbox(Checkbox a0) throws HeadlessException {
        lockAWT();
        try {
            checkHeadless();
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    protected CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem a0)
            throws HeadlessException
    {
        lockAWT();
        try {
            checkHeadless();
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    protected ChoicePeer createChoice(Choice a0) throws HeadlessException {
        lockAWT();
        try {
            checkHeadless();
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    protected DialogPeer createDialog(Dialog a0) throws HeadlessException {
        lockAWT();
        try {
            checkHeadless();
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    public DragSourceContextPeer createDragSourceContextPeer(
            DragGestureEvent dge) throws InvalidDnDOperationException {
        return dtk.createDragSourceContextPeer(dge);
    }

    @Override
    protected FileDialogPeer createFileDialog(FileDialog a0)
            throws HeadlessException
    {
        lockAWT();
        try {
            checkHeadless();
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    protected FramePeer createFrame(Frame a0) throws HeadlessException {
        lockAWT();
        try {
            checkHeadless();
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    protected LabelPeer createLabel(Label a0) throws HeadlessException {
        lockAWT();
        try {
            checkHeadless();
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    protected ListPeer createList(List a0) throws HeadlessException {
        lockAWT();
        try {
            checkHeadless();
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    protected MenuPeer createMenu(Menu a0) throws HeadlessException {
        lockAWT();
        try {
            checkHeadless();
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    protected MenuBarPeer createMenuBar(MenuBar a0) throws HeadlessException {
        lockAWT();
        try {
            checkHeadless();
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    protected MenuItemPeer createMenuItem(MenuItem a0) throws HeadlessException {
        lockAWT();
        try {
            checkHeadless();
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    protected PanelPeer createPanel(Panel a0) {
        lockAWT();
        try {
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    protected PopupMenuPeer createPopupMenu(PopupMenu a0)
            throws HeadlessException
    {
        lockAWT();
        try {
            checkHeadless();
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    protected ScrollPanePeer createScrollPane(ScrollPane a0)
            throws HeadlessException
    {
        lockAWT();
        try {
            checkHeadless();
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    protected ScrollbarPeer createScrollbar(Scrollbar a0)
            throws HeadlessException
    {
        lockAWT();
        try {
            checkHeadless();
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    protected TextFieldPeer createTextField(TextField a0)
            throws HeadlessException
    {
        lockAWT();
        try {
            checkHeadless();
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    protected WindowPeer createWindow(Window a0) throws HeadlessException {
        lockAWT();
        try {
            checkHeadless();
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    public String[] getFontList() {
        lockAWT();
        try {
        } finally {
            unlockAWT();
        }
        return null;
    }

    @Override
    protected FontPeer getFontPeer(String a0, int a1) {
        lockAWT();
        try {
            return null;
        } finally {
            unlockAWT();
        }
    }

    @Override
    public Image getImage(String filename) {
        return getImage(filename, this);
    }

    static Image getImage(String filename, Toolkit toolkit){
        synchronized(imageCache) {
            Image im = imageCache.get(filename);
            if(im == null){
                try{
                    im = toolkit.createImage(filename);
                    imageCache.put(filename, im);
                }catch(Exception e){

                }
            }
            return im;
        }
    }

    @Override
    public Image getImage(URL url) {
        return getImage(url, this);
    }

    static Image getImage(URL url, Toolkit toolkit) {
        synchronized(imageCache) {
            Image im = imageCache.get(url);
            if(im == null){
                try{
                    im = toolkit.createImage(url);
                    imageCache.put(url, im);
                }catch(Exception e){

                }
            }
            return im;
        }
    }

    @Override
    public PrintJob getPrintJob(Frame a0, String a1, Properties a2) {
        lockAWT();
        try {
        } finally {
            unlockAWT();
        }
        return null;
    }

    @Override
    public int getScreenResolution() throws HeadlessException {
        lockAWT();
        try {
            return ((GLGraphicsDevice)GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getDefaultScreenDevice()).getResolution().width;
        } finally {
            unlockAWT();
        }
    }

    @Override
    public Dimension getScreenSize() throws HeadlessException {
        lockAWT();
        try {
            DisplayMode dm =  GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getDefaultScreenDevice().getDisplayMode();
            return new Dimension(dm.getWidth(), dm.getHeight());
        } finally {
            unlockAWT();
        }
    }

    @Override
    public Clipboard getSystemClipboard() throws HeadlessException {
        lockAWT();
        try {
            checkHeadless();

            SecurityManager security = System.getSecurityManager();

            if (security != null) {
                security.checkSystemClipboardAccess();
            }

            if (systemClipboard == null) {
                systemClipboard = DTK.getDTK().getNativeClipboard();
            }

            return systemClipboard;
        } finally {
            unlockAWT();
        }
    }

    @Override
    public Map<java.awt.font.TextAttribute, ?> mapInputMethodHighlight(InputMethodHighlight highlight)
            throws HeadlessException {
        lockAWT();
        try {
            return mapInputMethodHighlightImpl(highlight);
        } finally {
            unlockAWT();
        }
    }

    @Override
    protected EventQueue getSystemEventQueueImpl() {
        return getSystemEventQueueCore().getActiveEventQueue();
    }

}
