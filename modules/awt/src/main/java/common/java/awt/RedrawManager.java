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

import java.awt.event.PaintEvent;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.harmony.awt.ClipRegion;
import org.apache.harmony.awt.gl.MultiRectArea;


/**
 * The collection of component regions that need to be painted
 */
class RedrawManager {

    final Window window;

    private boolean paintNeeded;
    private boolean updateNeeded;

    private final Map paintRegions = new IdentityHashMap();
    private final Map updateRegions = new IdentityHashMap();


    public RedrawManager(Window window) {
        this.window = window;
    }


    public void addPaintRegion(Component c, Rectangle r) {
        synchronized(this) {
            addRegion(paintRegions, c, r);
            paintNeeded = true;
        }
    }

    public void addPaintRegion(Component c, MultiRectArea r) {
        synchronized(this) {
            addRegion(paintRegions, c, r);
            paintNeeded = true;
        }
    }

    public void addUpdateRegion(Component c, Rectangle r) {
        synchronized(this) {
            addRegion(updateRegions, c, r);
            updateNeeded = true;
        }
        awake();
    }

    public void addUpdateRegion(Component c, MultiRectArea r) {
        synchronized(this) {
            addRegion(updateRegions, c, r);
            updateNeeded = true;
        }
        awake();
    }

    public void addUpdateRegion(Component c) {
        addUpdateRegion(c, new Rectangle(0, 0, c.w, c.h));
    }

    /**
     * Post paint events to the queue, with the clip from paintRegions
     */
    private void doPaint() {
        postEvents(paintRegions, PaintEvent.PAINT);
        paintRegions.clear();
        paintNeeded = false;
    }


    /**
     * Translate update regions to HW ancestor,
     * then post update events to the queue, with the clip from updateRegions
     */
    private void doUpdate() {
        synchronized(this) {
            postEvents(updateRegions, PaintEvent.UPDATE);
            updateRegions.clear();
            updateNeeded = false;
        }
    }

    private static void addRegion(Map map, Component c, Rectangle r) {
        MultiRectArea area = (MultiRectArea)map.get(c);
        if (area != null) {
            area.add(r);
        } else {
            area = new MultiRectArea(r);
            map.put(c, area);
        }

    }

    private static void addRegion(Map map, Component c, MultiRectArea r) {
        MultiRectArea area = (MultiRectArea)map.get(c);
        if (area != null) {
            area.add(r);
        } else {
            area = new MultiRectArea(r);
            map.put(c, area);
        }

    }

    public boolean redrawAll() {
        synchronized(this) {
            subtractPaintFromUpdate();

            boolean result = paintNeeded;
            if (paintNeeded) {
                doPaint();
            }
            if (updateNeeded) {
                doUpdate();
            }
            return result;
        }
    }

    private void subtractPaintFromUpdate() {
        for (Iterator it = paintRegions.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Entry)it.next();
            Component c = (Component) entry.getKey();
            MultiRectArea paint = (MultiRectArea) entry.getValue();
            MultiRectArea update = (MultiRectArea) updateRegions.get(c);
            if (update == null) {
                continue;
            }
            update.substract(paint);
            if (update.isEmpty()) {
                updateRegions.remove(c);
            }
        }
    }

    private void awake() {
        window.toolkit.getNativeEventQueue().awake();
    }

    /**
     * Post paint events for the collected regions
     * @param regions - Map<Component, MultiRectArea> - 
     *      regions that need to be painted
     * @param eventId - PaintEvent.PAINT or PaintEvent.UPDATE
     */
    private static void postEvents(Map regions, int eventId) {
        for (Iterator it = regions.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Entry)it.next();
            Component c = (Component) entry.getKey();
            if (!c.visible || !c.behaviour.isDisplayable()) {
                continue;
            }
            MultiRectArea clip = (MultiRectArea)entry.getValue();
            if (!clip.isEmpty()) {
                postPaintEvent(c, clip, eventId);
            }
        }
    }

    private static void postPaintEvent(Component c, MultiRectArea clip, int eventId) {
        PaintEvent event = new PaintEvent(c, eventId,
                new ClipRegion(clip));
        c.toolkit.systemEventQueue.postEvent(event);
    }


    /**
     * Subtract the component's region that waits for painting
     * from the provided component's area.
     * 
     * @return the resulting region, or null if the result is empty
     */
    public MultiRectArea subtractPendingRepaintRegion(Component c, MultiRectArea mra) {
        if (mra.isEmpty()) {
            return null;
        }
        int x = 0, y = 0;
        while (c != null && c.behaviour.isLightweight()) {
            x += c.x;
            y += c.y;
            c = c.parent;
        }
        if (c == null) {
            return null;
        }

        synchronized(this) {
            // Now c is heavyweight for sure
            MultiRectArea paint = (MultiRectArea) paintRegions.get(c);
            MultiRectArea update = (MultiRectArea) updateRegions.get(c);

            if (paint == null && update == null) {
                return mra;
            }
            MultiRectArea diff = new MultiRectArea(mra);
            diff.translate(x, y);

            if (paint != null) {
                diff.substract(paint);
            }
            if (update != null) {
                diff.substract(update);
            }
            if (diff.isEmpty()) {
                return null;
            }
            diff.translate(-x, -y);
            return diff;
        }
    }
}
