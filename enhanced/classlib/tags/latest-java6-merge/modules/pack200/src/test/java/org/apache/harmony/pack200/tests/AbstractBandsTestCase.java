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
package org.apache.harmony.pack200.tests;

import junit.framework.TestCase;

import org.apache.harmony.pack200.AttrDefinitionBands;
import org.apache.harmony.pack200.AttributeLayoutMap;
import org.apache.harmony.pack200.Pack200Exception;
import org.apache.harmony.pack200.Segment;
import org.apache.harmony.pack200.SegmentHeader;
import org.apache.harmony.pack200.SegmentOptions;

/**
 *
 */
public abstract class AbstractBandsTestCase extends TestCase {

    protected int numClasses = 1;
    protected int[] numMethods = {1};

    public class MockSegmentHeader extends SegmentHeader {
        public int getClassCount() {
            return numClasses;
        }

        public SegmentOptions getOptions() {
            try {
                return new SegmentOptions(0);
            } catch (Pack200Exception e) {
                return null;
            }
        }
    }

    public class MockAttributeDefinitionBands extends AttrDefinitionBands {

        public MockAttributeDefinitionBands(Segment segment) {
            super(segment);
        }

        public AttributeLayoutMap getAttributeDefinitionMap() {
            try {
                return new AttributeLayoutMap();
            } catch (Pack200Exception e) {
                fail(e.getLocalizedMessage());
            }
            return null;
        }



    }

    public class MockSegment extends Segment {

        protected AttrDefinitionBands getAttrDefinitionBands() {
            return new MockAttributeDefinitionBands(this);
        }

        public SegmentHeader getSegmentHeader() {
            return new MockSegmentHeader();
        }
    }


}
