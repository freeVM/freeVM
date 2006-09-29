/*
 *  Copyright 2005 - 2006 The Apache Software Foundation or its licensors, as applicable.
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
 * @author Alexey A. Ivanov
 * @version $Revision$
 */
package javax.swing.text;

public interface Position {

    final class Bias {

        public static final Position.Bias Forward = new Bias("Forward");

        public static final Position.Bias Backward = new Bias("Backward");

        /**
         * The bias name.
         */
        private String name;

        /**
         * Creates a new Bias object with the name specified.
         *
         * @param name the name of the object
         */
        private Bias(final String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

    }

    int getOffset();

}

