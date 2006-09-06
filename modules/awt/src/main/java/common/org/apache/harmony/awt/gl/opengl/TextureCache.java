/**
 * @author Oleg V. Khaschansky
 * @version $Revision$
 */

package org.apache.harmony.awt.gl.opengl;

import org.apache.harmony.awt.gl.Surface;

import java.util.HashMap;
import java.util.WeakHashMap;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

// XXX - todo - this class could be a prototype for the common resource cache.
// E.g. opengl contexts could be bounded to the thread that created the context and
// this cache will manage the destruction of the contexts.
public class TextureCache {
    private static HashMap ref2texture = new HashMap();

    private static ThreadLocal localInstance = new ThreadLocal() {
        public Object initialValue() {
            return new TextureCache();
        }
    };

    static TextureCache getInstance() {
        return (TextureCache) localInstance.get();
    }

    private ReferenceQueue rq = new ReferenceQueue();
    private WeakHashMap surface2ref = new WeakHashMap();

    void add(Surface key, OGLBlitter.OGLTextureParams texture) {
        WeakReference ref =
                new WeakReference(key, rq);

        surface2ref.put(key, ref);
        ref2texture.put(ref, texture);

        //System.out.println("Entry added: " + key + ";" + texture);
    }

    void cleanupTextures() {
        WeakReference ref;

        while((ref = (WeakReference) rq.poll()) != null) {
            OGLBlitter.OGLTextureParams tp =
                    (OGLBlitter.OGLTextureParams) ref2texture.remove(ref);
            tp.deleteTexture();
            //System.out.println("Entry cleaned up: " + tp);
        }
    }

    OGLBlitter.OGLTextureParams findTexture(Surface key) {
        OGLBlitter.OGLTextureParams tp =
         (OGLBlitter.OGLTextureParams) ref2texture.get(surface2ref.get(key));
        //System.out.println("Entry looked up: " + key + ";" + tp);
        return tp;
    }

    void remove(Surface key) {
        WeakReference ref = (WeakReference) surface2ref.remove(key);
        if (ref != null) {
            ref.clear();
            OGLBlitter.OGLTextureParams tp =
                    (OGLBlitter.OGLTextureParams) ref2texture.remove(ref);
            tp.deleteTexture();
            //System.out.println("Entry removed: " + key + ";" + tp);
        }
    }
}
