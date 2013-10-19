/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */

import junit.framework.*;
import java.util.concurrent.atomic.*;

public class AtomicStampedReferenceTest extends JSR166TestCase{
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return new TestSuite(AtomicStampedReferenceTest.class);
    }
    
    /**
     * constructor initializes to given reference and stamp
     */
    public void testConstructor(){
        AtomicStampedReference ai = new AtomicStampedReference(one, 0);
	assertEquals(one,ai.getReference());
	assertEquals(0, ai.getStamp());
        AtomicStampedReference a2 = new AtomicStampedReference(null, 1);
	assertNull(a2.getReference());
	assertEquals(1, a2.getStamp());

    }

    /**
     *  get returns the last values of reference and stamp set
     */
    public void testGetSet(){
        int[] mark = new int[1];
        AtomicStampedReference ai = new AtomicStampedReference(one, 0);
	assertEquals(one,ai.getReference());
	assertEquals(0, ai.getStamp());
        assertEquals(one, ai.get(mark));
        assertEquals(0, mark[0]);
	ai.set(two, 0);
	assertEquals(two,ai.getReference());
	assertEquals(0, ai.getStamp());
        assertEquals(two, ai.get(mark));
        assertEquals(0, mark[0]);
	ai.set(one, 1);
	assertEquals(one,ai.getReference());
	assertEquals(1, ai.getStamp());
        assertEquals(one, ai.get(mark));
        assertEquals(1,mark[0]);
    }

    /**
     *  attemptStamp succeeds in single thread
     */
    public void testAttemptStamp(){
        int[] mark = new int[1];
        AtomicStampedReference ai = new AtomicStampedReference(one, 0);
        assertEquals(0, ai.getStamp());
        assertTrue(ai.attemptStamp(one, 1));
	assertEquals(1, ai.getStamp());
        assertEquals(one, ai.get(mark));
        assertEquals(1, mark[0]);
    }

    /**
     * compareAndSet succeeds in changing values if equal to expected reference
     * and stamp else fails
     */
    public void testCompareAndSet(){
        int[] mark = new int[1];
        AtomicStampedReference ai = new AtomicStampedReference(one, 0);
	assertEquals(one, ai.get(mark));
        assertEquals(0, ai.getStamp());
	assertEquals(0, mark[0]);

        assertTrue(ai.compareAndSet(one, two, 0, 0));
	assertEquals(two, ai.get(mark));
	assertEquals(0, mark[0]);

        assertTrue(ai.compareAndSet(two, m3, 0, 1));
	assertEquals(m3, ai.get(mark));
	assertEquals(1, mark[0]);

        assertFalse(ai.compareAndSet(two, m3, 1, 1));
	assertEquals(m3, ai.get(mark));
	assertEquals(1, mark[0]);
    }

    /**
     * compareAndSet in one thread enables another waiting for reference value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads() {
        final AtomicStampedReference ai = new AtomicStampedReference(one, 0);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!ai.compareAndSet(two, three, 0, 0)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(ai.compareAndSet(one, two, 0, 0));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
            assertEquals(ai.getReference(), three);
            assertEquals(ai.getStamp(), 0);
        }
        catch(Exception e) {
            unexpectedException();
        }
    }

    /**
     * compareAndSet in one thread enables another waiting for stamp value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads2() {
        final AtomicStampedReference ai = new AtomicStampedReference(one, 0);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!ai.compareAndSet(one, one, 1, 2)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(ai.compareAndSet(one, one, 0, 1));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
            assertEquals(ai.getReference(), one);
            assertEquals(ai.getStamp(), 2);
        }
        catch(Exception e) {
            unexpectedException();
        }
    }

    /**
     * repeated weakCompareAndSet succeeds in changing values when equal
     * to expected 
     */
    public void testWeakCompareAndSet(){
        int[] mark = new int[1];
        AtomicStampedReference ai = new AtomicStampedReference(one, 0);
	assertEquals(one, ai.get(mark));
        assertEquals(0, ai.getStamp ());
	assertEquals(0, mark[0]);

        while(!ai.weakCompareAndSet(one, two, 0, 0));
	assertEquals(two, ai.get(mark));
	assertEquals(0, mark[0]);

        while(!ai.weakCompareAndSet(two, m3, 0, 1));
	assertEquals(m3, ai.get(mark));
	assertEquals(1, mark[0]);
    }

}
