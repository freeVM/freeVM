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
 * @author Pavel Dolgov
 * @version $Revision$
 */
package java.awt;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.MouseDragGestureRecognizer;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

class DefaultMouseDragGestureRecognizer extends
        MouseDragGestureRecognizer {
    
    private boolean active;

    protected DefaultMouseDragGestureRecognizer(
            DragSource ds, Component c, int act, DragGestureListener dgl) {
        super(ds, c, act, dgl);
        
        events = new ArrayList();
    }

    public void mousePressed(MouseEvent e) {
        events.add(e);
    }

    public void mouseDragged(MouseEvent e) {
        if (active) {
            return;
        }
        events.add(e);
        int distance = 2; // TODO: use desktop property 
        MouseEvent e0 = (MouseEvent)events.get(0);
        if (e0.getPoint().distance(e.getPoint()) >= distance) {
            int act = DnDConstants.ACTION_NONE;
            if (e.isControlDown()) {
                act = e.isShiftDown() ? 
                        DnDConstants.ACTION_LINK : DnDConstants.ACTION_COPY;
            } else {
                act = DnDConstants.ACTION_MOVE;
            }
            act &= super.sourceActions;
            
            if (act != DnDConstants.ACTION_NONE) {
                active = true;
                fireDragGestureRecognized(act, e.getPoint());
            }
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        active = false;
    }
}
