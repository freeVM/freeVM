/* Copyright 2006 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.tests.java.lang;

import java.io.IOException;

import junit.framework.TestCase;
import tests.util.SerializationTester;

public class EnumTest extends TestCase {

    enum Sample {
        LARRY, MOE, CURLY
    }

    Sample larry = Sample.LARRY;

    Sample moe = Sample.MOE;

    enum Empty {
    }
    
    enum Bogus {
        UNUSED
    }   
    
    /**
     * @tests java.lang.Enum#compareTo(java.lang.Enum) 
     */
    public void test_compareToLjava_lang_Enum() {
        assertTrue(0 < Sample.MOE.compareTo(Sample.LARRY));
        assertEquals(0, Sample.MOE.compareTo(Sample.MOE));
        assertTrue(0 > Sample.MOE.compareTo(Sample.CURLY));
        try {
            Sample.MOE.compareTo((Sample)null);
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * @tests java.lang.Enum#equals(Object)
     */
    public void test_equalsLjava_lang_Object() {
        assertFalse(moe.equals("bob"));
        assertTrue(moe.equals(Sample.MOE));
        assertFalse(Sample.LARRY.equals(Sample.CURLY));
        assertTrue(Sample.LARRY.equals(larry));
        assertFalse(Sample.CURLY.equals(null));
    }

    /**
     * @tests java.lang.Enum#getDeclaringClass()
     */
    public void test_getDeclaringClass() {
        assertEquals(Sample.class, moe.getDeclaringClass());
    }

    /**
     * @tests java.lang.Enum#hashCode()
     */
    public void test_hashCode() {
        assertEquals (moe.hashCode(), moe.hashCode());
    }

    /**
     * @tests java.lang.Enum#name()
     */
    public void test_name() {
        assertEquals("MOE", moe.name());
    }

    /**
     * @tests java.lang.Enum#ordinal()
     */
    public void test_ordinal() {
        assertEquals(0, larry.ordinal());
        assertEquals(1, moe.ordinal());
        assertEquals(2, Sample.CURLY.ordinal());
    }

    /**
     * @tests java.lang.Enum#toString()
     */
    public void test_toString() {
        assertTrue(moe.toString().contains("MOE"));
    }

    /**
     * @tests java.lang.Enum#valueOf(Class, String)
     */
    public void test_valueOfLjava_lang_String() {
        assertSame(Sample.CURLY, Sample.valueOf("CURLY"));
        assertSame(Sample.LARRY, Sample.valueOf("LARRY"));
        assertSame(moe, Sample.valueOf("MOE"));
        try {
            Sample.valueOf("non-existant");
            fail("Expected an exception");
        } catch (IllegalArgumentException e){
            // Expected
        }
        try {
            Sample.valueOf(null);
        } catch (NullPointerException e) {
            // May be caused by some compilers' code
        } catch (IllegalArgumentException e) {
            // other compilers will throw this
        }

        
        Sample s = Enum.valueOf(Sample.class, "CURLY");
        assertSame(s, Sample.CURLY);
        s = Enum.valueOf(Sample.class, "LARRY");
        assertSame(larry, s);
        s = Enum.valueOf(Sample.class, "MOE");
        assertSame(s, moe);
        try {
            Enum.valueOf(Bogus.class, "MOE");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        try {
            Enum.valueOf((Class<Sample>)null, "a string");
            fail("Expected an exception");
        } catch (NullPointerException e) {
            // May be caused by some compilers' code
        } catch (IllegalArgumentException e) {
            // other compilers will throw this
        }
        try {
            Enum.valueOf(Sample.class, null);
            fail("Expected an exception");
        } catch (NullPointerException e) {
            // May be caused by some compilers' code
        } catch (IllegalArgumentException e) {
            // other compilers will throw this
        }
        try {
            Enum.valueOf((Class<Sample>)null, (String)null);
            fail("Expected an exception");
        } catch (NullPointerException e) {
            // May be caused by some compilers' code
        } catch (IllegalArgumentException e) {
            // other compilers will throw this
        }
    }

    /**
     * @tests java.lang.Enum#values
     */
    public void test_values() {
        Sample[] myValues = Sample.values();
        assertEquals(3, myValues.length);

        assertEquals(Sample.LARRY, myValues[0]);
        assertEquals(Sample.MOE, myValues[1]);
        assertEquals(Sample.CURLY, myValues[2]);
        
        assertEquals(0, Empty.values().length);
    }
    
    /**
     * @test Serialization/deserilazation compatibility with RI.
     */
    public void test_compatibilitySerialization() throws Exception{
        assertTrue(SerializationTester.assertCompabilityEquals(Sample.CURLY,"serialization/java/lang/EnumTest.golden.1.ser"));
    }
    
    /**
     * @test Serialization/deserilazation compatibility with RI.
     */
    public void test_compatibilitySerialization_inClass() throws Exception{
        assertTrue(SerializationTester.assertCompabilityEquals(new MockEnum(),"serialization/java/lang/EnumTest.golden.2.ser"));
    }
    
    /**
     * @test Serialization/deserilazation compatibility.
     */
    public void test_serialization() throws IOException, ClassNotFoundException{
        Sample object = Sample.CURLY;
        Sample deObject = (Sample) SerializationTester
                .getDeserilizedObject(object);
        assertEquals(object, deObject);
    }
    
    /**
     * test a class that has enums as its fields.
     * 
     * @test Serialization/deserilazation compatibility.
     */
    public void test_serialization_inClass() throws IOException, ClassNotFoundException{
        MockEnum mock = new MockEnum();
        MockEnum test = (MockEnum) SerializationTester
                .getDeserilizedObject(mock);
        assertEquals(mock.i,test.i);
        assertEquals(mock.str,test.str);
        assertEquals(mock.samEnum,test.samEnum);
    }
    
    /**
     * test a class that has enums and a string of same name as its fields.
     * 
     * @test Serialization/deserilazation compatibility with RI.
     */
    public void test_compatibilitySerialization_inClass_Complex() throws Exception{
        assertTrue(SerializationTester.assertCompabilityEquals(new MockEnum2(),"serialization/java/lang/EnumTest.golden.3.ser"));
    }
    
    /**
     * @test Serialization/deserilazation compatibility with Harmony.
     */
    public void test_compatibilitySerialization_inClass_Complex_Harmony() throws Exception{
        assertTrue(SerializationTester.assertCompabilityEquals(new MockEnum2(),"serialization/java/lang/EnumTest.Harmony.ser"));
    }
    
    /**
     * test a class that has enums and a string of same name as its fields.
     * 
     * @test Serialization/deserilazation compatibility.
     */
    public void test_serialization_inClass_Complex() throws Exception{
        MockEnum2 mock = new MockEnum2();
        MockEnum2 test = (MockEnum2) SerializationTester
                .getDeserilizedObject(mock);
        assertEquals(mock.i,test.i);
        assertEquals(mock.str,test.str);
        assertEquals(mock.samEnum,test.samEnum);
    }    
}
