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
 * @author Michael Danilov
 * @version $Revision$
 */
package java.awt.dnd;

import java.awt.Point;
import java.awt.datatransfer.*;
import java.util.List;

public class DropTargetDropEvent extends DropTargetEvent {

    private static final long serialVersionUID = -1721911170440459322L
;
    private final boolean local;

    public DropTargetDropEvent(DropTargetContext dtc, Point cursorLocn, int dropAction,
            int srcActions)
    {
        super(dtc, cursorLocn, dropAction, srcActions);

        local = false;
    }

    public DropTargetDropEvent(DropTargetContext dtc, Point cursorLocn, int dropAction,
            int srcActions, boolean isLocal)
    {
        super(dtc, cursorLocn, dropAction, srcActions);

        local = isLocal;
    }

    public Point getLocation() {
        return super.getLocation();
    }

    public int getSourceActions() {
        return super.getSourceAction();
    }

    public int getDropAction() {
        return super.getUserAction();
    }

    public boolean isLocalTransfer() {
        return local;
    }

    public List getCurrentDataFlavorsAsList() {
        return context.getCurrentDataFlavorsAsList();
    }

    public Transferable getTransferable() {
        return context.getTransferable();
    }

    public boolean isDataFlavorSupported(DataFlavor df) {
        return context.isDataFlavorSupported(df);
    }

    public DataFlavor[] getCurrentDataFlavors() {
        return context.getCurrentDataFlavors();
    }

    public void dropComplete(boolean success) {
        context.dropComplete(success);
    }

    public void acceptDrop(int dropAction) {
        context.acceptDrop(dropAction);
    }

    public void rejectDrop() {
        context.rejectDrop();
    }

}
