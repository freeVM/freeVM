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
package java.awt.dnd.peer;

import java.awt.dnd.*;
import java.awt.datatransfer.*;

public interface DropTargetContextPeer {

    int  getTargetActions();

    void setTargetActions(int actions);

    DropTarget getDropTarget();

    DataFlavor[] getTransferDataFlavors();

    Transferable getTransferable() throws InvalidDnDOperationException;

    boolean isTransferableJVMLocal();

    void acceptDrag(int dragAction);

    void rejectDrag();

    void acceptDrop(int dropAction);

    void rejectDrop();

    void dropComplete(boolean success);

}
